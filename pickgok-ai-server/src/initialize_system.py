import os
import sys
import json
import time
import logging
import pandas as pd
import numpy as np
import librosa
import faiss
import mysql.connector
from pathlib import Path
from tqdm import tqdm
from concurrent.futures import ProcessPoolExecutor, as_completed
from typing import List, Tuple, Optional


# --- Configuration ---
class Config:
    # 1. 현재 파일(src)의 위치 기준
    CURRENT_FILE = Path(__file__).resolve()
    AI_SERVER_ROOT = CURRENT_FILE.parent.parent  # pickgok-ai-server
    PROJECT_ROOT = AI_SERVER_ROOT.parent         # PickGok-Project (Root)

    # 2. 데이터 경로 (웹 서버의 data 폴더 참조)
    WEB_DATA_DIR = PROJECT_ROOT / "pickgok-web-server" / "src" / "main" / "webapp" / "data"

    # 3. 세부 경로
    AUDIO_DIR = WEB_DATA_DIR / "fma_small"
    METADATA_CSV_PATH = WEB_DATA_DIR / "fma_metadata" / "tracks.csv"

    # 4. 모델 출력 경로
    OUTPUT_DIR = AI_SERVER_ROOT / "models"

    # 오디오 처리 설정
    SAMPLE_RATE = 22050
    DURATION = 29.0
    N_MFCC = 20

    # 시스템 설정 (CPU 코어 절반 사용)
    MAX_WORKERS = max(1, (os.cpu_count() or 2) // 2)

    # MySQL 접속 정보
    DB_CONFIG = {
        "host": "localhost",
        "user": "root",
        "password": "0000",  # [중요] 설정한 비밀번호 확인
        "database": "pick_gok",
        "auth_plugin": "mysql_native_password",
    }

    # 로깅 설정
    LOG_FORMAT = "%(asctime)s - %(levelname)s - %(message)s"


# 초기화
logging.basicConfig(level=logging.INFO, format=Config.LOG_FORMAT)
logger = logging.getLogger("InitSystem")
os.makedirs(Config.OUTPUT_DIR, exist_ok=True)

# =========================================================
# [Common] 데이터 로드 유틸리티
# =========================================================


def load_tracks_metadata() -> pd.DataFrame:
    """tracks.csv 로드 및 전처리"""
    if not Config.METADATA_CSV_PATH.exists():
        logger.error(f"❌ File not found: {Config.METADATA_CSV_PATH}")
        sys.exit(1)

    logger.info(f"Loading metadata from: {Config.METADATA_CSV_PATH}")
    try:
        tracks = pd.read_csv(Config.METADATA_CSV_PATH, index_col=0, header=[0, 1])
        small_subset = tracks[tracks[("set", "subset")] == "small"]

        df_clean = pd.DataFrame(
            {
                "track_id": small_subset.index,
                "title": small_subset[("track", "title")],
                "artist": small_subset[("artist", "name")],
                "genre": small_subset[("track", "genre_top")],
                "duration": small_subset[("track", "duration")],
            }
        ).set_index("track_id")

        # NaN 값 처리
        df_clean["genre"] = df_clean["genre"].fillna("Unknown")

        logger.info(f"✅ Metadata loaded. Total tracks: {len(df_clean)}")
        return df_clean

    except Exception as e:
        logger.error(f"Failed to load metadata: {e}")
        sys.exit(1)


def get_audio_path(track_id: int) -> Path:
    """실제 파일 시스템 경로 (Librosa용)"""
    tid_str = "{:06d}".format(track_id)
    folder_code = tid_str[:3]
    return Config.AUDIO_DIR / folder_code / f"{tid_str}.mp3"


def get_web_audio_path(track_id: int) -> str:
    """Java 웹 서버용 상대 경로 (DB 저장용)"""
    tid_str = "{:06d}".format(track_id)
    folder_code = tid_str[:3]
    # 웹 서버에서는 webapp/data 폴더가 /data URL로 매핑된다고 가정
    return f"/data/fma_small/{folder_code}/{tid_str}.mp3"


# =========================================================
# [Phase 1] AI 모델 빌드
# =========================================================


def extract_features(track_id: int) -> Optional[Tuple[int, np.ndarray]]:
    file_path = get_audio_path(track_id)
    if not file_path.exists():
        return None

    try:
        y, sr = librosa.load(file_path, sr=Config.SAMPLE_RATE, duration=Config.DURATION)
        mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=Config.N_MFCC)
        mfcc_mean = np.mean(mfcc, axis=1)
        mfcc_var = np.var(mfcc, axis=1)
        feature_vector = np.concatenate((mfcc_mean, mfcc_var))
        return track_id, feature_vector.astype("float32")
    except Exception:
        return None


def build_ai_model(df: pd.DataFrame):
    logger.info(
        f">>> [Phase 1] Starting AI Model Build (Workers: {Config.MAX_WORKERS})"
    )
    logger.info(f"    - Output Dir: {Config.OUTPUT_DIR}")
    start_time = time.time()

    track_ids = df.index.tolist()
    features = []
    valid_ids = []

    # 병렬 처리로 특징 추출
    with ProcessPoolExecutor(max_workers=Config.MAX_WORKERS) as executor:
        future_to_id = {
            executor.submit(extract_features, tid): tid for tid in track_ids
        }

        for future in tqdm(
                as_completed(future_to_id),
                total=len(track_ids),
                desc="AI: Extracting Features",
        ):
            result = future.result()
            if result is not None:
                tid, vec = result
                valid_ids.append(tid)
                features.append(vec)

    if not features:
        logger.error("❌ No features extracted. AI Model build failed.")
        return

    # FAISS 인덱스 생성
    d = features[0].shape[0]
    feature_matrix = np.vstack(features)
    index = faiss.IndexFlatL2(d)
    index.add(feature_matrix)

    # 모델 저장
    faiss.write_index(index, str(Config.OUTPUT_DIR / "music.index"))

    # 메타데이터 JSON 저장 (Track ID 매핑용)
    mapping = []
    for i, original_id in enumerate(valid_ids):
        try:
            info = df.loc[original_id]
            mapping.append(
                {
                    "faiss_id": i,
                    "track_id": int(original_id),
                    "title": str(info["title"]),
                    "artist": str(info["artist"]),
                }
            )
        except KeyError:
            continue

    with open(Config.OUTPUT_DIR / "metadata.json", "w", encoding="utf-8") as f:
        json.dump(mapping, f, ensure_ascii=False, indent=2)

    elapsed = time.time() - start_time
    logger.info(f"✅ [Phase 1] AI Model Built & Saved in {elapsed:.2f}s")


# =========================================================
# [Phase 2] DB 마이그레이션
# =========================================================


def test_db_connection() -> bool:
    logger.info("Checking Database Connection...")
    conn = None
    try:
        conn = mysql.connector.connect(**Config.DB_CONFIG)
        logger.info("✅ Database connected successfully.")
        return True
    except mysql.connector.Error as err:
        logger.error(f"❌ DB Connection Failed: {err}")
        return False
    finally:
        if conn:
            conn.close()


def migrate_database(df: pd.DataFrame):
    logger.info(">>> [Phase 2] Starting Database Migration")
    conn = None
    try:
        conn = mysql.connector.connect(**Config.DB_CONFIG)
        cursor = conn.cursor()

        query = """
                INSERT INTO tracks (track_id, title, artist, genre, duration, file_path)
                VALUES (%s, %s, %s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE
                                         title = VALUES(title),
                                         artist = VALUES(artist),
                                         genre = VALUES(genre),
                                         file_path = VALUES(file_path) \
                """

        data_batch = []
        batch_size = 1000

        for track_id, row in tqdm(
                df.iterrows(), total=len(df), desc="DB: Migrating Data"
        ):
            title = str(row["title"])[:255]
            artist = str(row["artist"])[:255]
            genre = str(row["genre"])
            duration = int(float(row["duration"]))
            # [핵심] 웹 오디오 경로 사용 (/data/...)
            file_path = get_web_audio_path(int(track_id))

            data_batch.append(
                (int(track_id), title, artist, genre, duration, file_path)
            )

            if len(data_batch) >= batch_size:
                cursor.executemany(query, data_batch)
                conn.commit()
                data_batch = []

        if data_batch:
            cursor.executemany(query, data_batch)
            conn.commit()

        logger.info("✅ [Phase 2] Database Migration Complete.")

    except mysql.connector.Error as err:
        logger.error(f"❌ Database Migration Failed: {err}")
    finally:
        if conn:
            conn.close()


# =========================================================
# Main Execution Flow
# =========================================================


def main():
    print("\n" + "=" * 50)
    print("🎵 PickGok System Initialization")
    print("=" * 50 + "\n")

    # 1. DB 연결 테스트
    if not test_db_connection():
        return

    # 2. 경로 확인 로그
    logger.info(f"Project Root: {Config.PROJECT_ROOT}")
    logger.info(f"Web Data Dir: {Config.WEB_DATA_DIR}")
    logger.info(f"Audio Dir   : {Config.AUDIO_DIR}")
    logger.info(f"Models Dir  : {Config.OUTPUT_DIR}")

    # 3. 메타데이터 로드
    df = load_tracks_metadata()

    # 4. AI 모델 빌드 (Phase 1)
    build_ai_model(df)

    # 5. DB 마이그레이션 (Phase 2)
    migrate_database(df)

    print("\n" + "=" * 50)
    print("🎉 All Systems Initialized Successfully!")
    print("=" * 50 + "\n")


if __name__ == "__main__":
    main()