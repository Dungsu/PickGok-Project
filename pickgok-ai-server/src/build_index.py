import os
import sys
import json
import logging
import pandas as pd
import numpy as np
import librosa
import faiss
import mysql.connector
from pathlib import Path
from tqdm import tqdm
from concurrent.futures import ProcessPoolExecutor, as_completed


class Config:
    BASE_DIR = Path(__file__).resolve().parent.parent
    # 웹 서버의 실제 파일 경로 (수정 필요 시 변경)
    WEB_DATA_DIR = (
        BASE_DIR.parent / "pickgok-web-server" / "src" / "main" / "webapp" / "data"
    )
    OUTPUT_DIR = BASE_DIR / "models"

    DB_CONFIG = {
        "host": "localhost",
        "user": "root",
        "password": "1234",  # <--- 여기 비밀번호 꼭 확인하세요!
        "database": "pick_gok",
    }

    SAMPLE_RATE = 22050
    DURATION = 29.0
    N_MFCC = 20
    MAX_WORKERS = max(1, (os.cpu_count() or 2) // 2)


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("Builder")


def get_db_tracks():
    try:
        conn = mysql.connector.connect(**Config.DB_CONFIG)
        query = "SELECT track_id, file_path FROM tracks"
        df = pd.read_sql(query, conn)
        conn.close()
        return df
    except Exception as e:
        logger.error(f"DB Error: {e}")
        sys.exit(1)


def analyze_track(args):
    tid, rel_path = args
    # 경로 변환: /data/fma_small/... -> C:/.../webapp/data/fma_small/...
    clean_path = rel_path.replace("/data/", "").replace("/", os.sep)
    full_path = Config.WEB_DATA_DIR / clean_path

    if not full_path.exists():
        return None  # 파일 없으면 스킵

    try:
        y, sr = librosa.load(full_path, sr=Config.SAMPLE_RATE, duration=Config.DURATION)
        mfcc = librosa.feature.mfcc(y=y, sr=sr, n_mfcc=Config.N_MFCC)
        feat = np.concatenate((np.mean(mfcc, axis=1), np.var(mfcc, axis=1)))
        return tid, feat.astype("float32")
    except:
        return None


def main():
    os.makedirs(Config.OUTPUT_DIR, exist_ok=True)
    df = get_db_tracks()
    logger.info(f"DB에서 {len(df)}곡 조회됨. 분석 시작...")

    tasks = [(r["track_id"], r["file_path"]) for _, r in df.iterrows()]
    features = []
    valid_ids = []

    with ProcessPoolExecutor(max_workers=Config.MAX_WORKERS) as exe:
        futures = {exe.submit(analyze_track, t): t[0] for t in tasks}
        for f in tqdm(as_completed(futures), total=len(tasks), desc="Processing"):
            res = f.result()
            if res:
                valid_ids.append(res[0])
                features.append(res[1])

    if not features:
        logger.error("분석된 데이터가 없습니다.")
        return

    # FAISS 저장
    mat = np.vstack(features)
    idx = faiss.IndexFlatL2(mat.shape[1])
    idx.add(mat)
    faiss.write_index(idx, str(Config.OUTPUT_DIR / "music.index"))

    # Metadata 저장
    meta = [{"faiss_id": i, "track_id": int(tid)} for i, tid in enumerate(valid_ids)]
    with open(Config.OUTPUT_DIR / "metadata.json", "w") as f:
        json.dump(meta, f)

    logger.info("✅ 인덱스 빌드 완료.")


if __name__ == "__main__":
    main()
