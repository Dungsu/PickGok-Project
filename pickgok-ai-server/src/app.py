import sys
import json
import logging
import subprocess
import time
import mysql.connector
from pathlib import Path
from flask import Flask, request, jsonify
import faiss


# --- Configuration ---
class Config:
    BASE_DIR = Path(__file__).resolve().parent.parent
    MODEL_DIR = BASE_DIR / "models"
    INDEX_PATH = MODEL_DIR / "music.index"
    METADATA_JSON_PATH = MODEL_DIR / "metadata.json"

    # 스크립트 경로들
    BUILD_SCRIPT = BASE_DIR / "src" / "build_index.py"
    SYNC_SCRIPT = BASE_DIR / "src" / "sync_db.py"  # <--- 추가됨!

    DB_CONFIG = {
        "host": "localhost",
        "user": "root",
        "password": "1234",
        "database": "pick_gok",
    }


logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

APP = Flask(__name__)
FAISS_INDEX = None
TRACK_TO_FAISS = {}
FAISS_TO_TRACK = {}


# =========================================================
# [Helper] 스크립트 실행기
# =========================================================
def run_script(script_path, name):
    print(f"\n🔨 Running {name}...")
    try:
        subprocess.run([sys.executable, str(script_path)], check=True)
        print(f"✅ {name} Completed Successfully.\n")
        return True
    except subprocess.CalledProcessError:
        logger.error(f"❌ {name} Failed.")
        return False


# =========================================================
# [Smart Logic] 상태 점검 및 복구
# =========================================================
def get_counts():
    db_count = -1
    index_count = 0
    try:
        conn = mysql.connector.connect(**Config.DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("SELECT COUNT(*) FROM tracks")
        db_count = cursor.fetchone()[0]
        conn.close()
    except:
        pass

    if Config.METADATA_JSON_PATH.exists():
        try:
            with open(Config.METADATA_JSON_PATH, "r", encoding="utf-8") as f:
                index_count = len(json.load(f))
        except:
            index_count = -1

    return db_count, index_count


def check_system_integrity():
    print("\n" + "=" * 60)
    print("🔎 [System Check] Integrity Diagnosis")

    db_cnt, idx_cnt = get_counts()
    print(f"   📂 DB Tracks   : {db_cnt}")
    print(f"   📄 Index Tracks: {idx_cnt}")
    print("=" * 60)

    # CASE 1: DB 연결 실패 또는 트랙 0개 -> 마이그레이션 필요
    if db_cnt <= 0:
        print("\n⚠️  DB가 비어있거나 연결되지 않았습니다.")
        choice = input(">>> 'sync_db.py'를 실행하여 데이터를 채우시겠습니까? (y/n): ")
        if choice.lower() == "y":
            if not run_script(Config.SYNC_SCRIPT, "Database Sync"):
                return False
            # DB 채웠으니 다시 카운트 확인
            db_cnt, _ = get_counts()
        else:
            return False

    # CASE 2: 인덱스 파일 없음 OR 개수 불일치 -> 인덱싱 필요
    if idx_cnt == 0 or db_cnt != idx_cnt:
        msg = "인덱스 파일 없음" if idx_cnt == 0 else "데이터 개수 불일치"
        print(f"\n⚠️  {msg} 감지!")
        choice = input(
            ">>> 'build_index.py'를 실행하여 인덱스를 맞추시겠습니까? (y/n): "
        )
        if choice.lower() == "y":
            return run_script(Config.BUILD_SCRIPT, "Index Build")
        elif idx_cnt > 0:
            print("⏩ 경고 무시하고 진행합니다.")
            return True
        else:
            return False  # 인덱스도 없는데 실행 불가

    print("\n✅ 모든 데이터가 정상입니다.")
    return True


# =========================================================
# [Core Logic] 리소스 로드
# =========================================================
def load_resources():
    global FAISS_INDEX, TRACK_TO_FAISS, FAISS_TO_TRACK
    if not Config.INDEX_PATH.exists():
        return False
    try:
        FAISS_INDEX = faiss.read_index(str(Config.INDEX_PATH))
        with open(Config.METADATA_JSON_PATH, "r", encoding="utf-8") as f:
            metadata = json.load(f)
        TRACK_TO_FAISS = {int(i["track_id"]): int(i["faiss_id"]) for i in metadata}
        FAISS_TO_TRACK = {int(i["faiss_id"]): int(i["track_id"]) for i in metadata}
        logger.info(f"Loaded resources. Total: {len(metadata)}")
        return True
    except Exception as e:
        logger.error(f"Load Error: {e}")
        return False


# ... (API 라우트는 그대로 두시면 됩니다) ...
@APP.route("/recommend", methods=["POST"])
def recommend():
    if not FAISS_INDEX:
        return jsonify({"error": "Not ready"}), 503
    try:
        data = request.get_json()
        seed = int(data.get("track_id"))
        k = data.get("k", 5)
        if seed not in TRACK_TO_FAISS:
            return jsonify({"error": "Not found"}), 404

        fid = TRACK_TO_FAISS[seed]
        vec = FAISS_INDEX.reconstruct(fid).reshape(1, -1)
        dists, idxs = FAISS_INDEX.search(vec, k + 1)

        recs = []
        for d, i in zip(dists[0], idxs[0]):
            if i != -1 and i != fid:
                recs.append({"track_id": FAISS_TO_TRACK[i], "distance": float(d)})
        return jsonify({"status": "success", "recommendations": recs})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    print("\n🚀 PickGok AI Server Launcher")
    if check_system_integrity():
        if load_resources():
            APP.run(host="0.0.0.0", port=5000)
        else:
            print("❌ 로드 실패")
    else:
        print("❌ 실행 취소됨")
