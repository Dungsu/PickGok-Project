import os
import sys
import json
import logging
import subprocess
from pathlib import Path
from flask import Flask, request, jsonify
import numpy as np
import faiss


# --- Configuration ---
class Config:
    # 1. 현재 파일(src/app.py)의 위치 기준 -> pickgok-ai-server
    BASE_DIR = Path(__file__).resolve().parent.parent

    # 2. 모델 파일 경로 (AI 서버 내 models 폴더)
    MODEL_DIR = BASE_DIR / "models"
    INDEX_PATH = MODEL_DIR / "music.index"
    METADATA_JSON_PATH = MODEL_DIR / "metadata.json"

    # 3. 관리 스크립트 경로
    SRC_DIR = BASE_DIR / "src"
    DIAGNOSE_SCRIPT = SRC_DIR / "diagnose_system.py"
    INIT_SCRIPT = SRC_DIR / "initialize_system.py"


# 로깅 설정
logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# --- Global Resources ---
APP = Flask(__name__)
FAISS_INDEX = None
TRACK_TO_FAISS = {}
FAISS_TO_TRACK = {}


# =========================================================
# [System Check Logic] 진단 및 초기화 제안
# =========================================================
def run_script(script_path):
    """외부 파이썬 스크립트 실행"""
    try:
        # 현재 파이썬 인터프리터로 스크립트 실행
        result = subprocess.run([sys.executable, str(script_path)], check=True)
        return result.returncode == 0
    except subprocess.CalledProcessError:
        return False


def perform_preflight_check():
    """서버 시작 전 시스템 상태 점검"""
    print("\n" + "=" * 60)
    print("🔍 [Pre-flight Check] Running System Diagnosis...")
    print("=" * 60)

    # 1. 진단 스크립트 확인
    if not Config.DIAGNOSE_SCRIPT.exists():
        logger.error(f"Diagnostic script not found: {Config.DIAGNOSE_SCRIPT}")
        return False

    # 2. diagnose_system.py 실행
    is_healthy = run_script(Config.DIAGNOSE_SCRIPT)

    if is_healthy:
        print("\n✅ System is healthy. Starting Server...\n")
        return True
    else:
        print("\n❌ System diagnosis failed or issues found.")

        # 3. 초기화 제안
        response = (
            input(">>> Do you want to run 'initialize_system.py' to fix/setup? (y/n): ")
            .strip()
            .lower()
        )
        if response == "y":
            print("\n🚀 Running System Initialization...")
            if run_script(Config.INIT_SCRIPT):
                print("\n✅ Initialization Complete. Retrying startup...\n")
                return True  # 초기화 성공 시 서버 시작 시도
            else:
                logger.error("Initialization failed.")
                return False
        else:
            logger.warning("Startup aborted by user.")
            return False


# =========================================================
# [Core Logic] 리소스 로드 및 API
# =========================================================


def load_resources():
    global FAISS_INDEX, TRACK_TO_FAISS, FAISS_TO_TRACK

    if not Config.INDEX_PATH.exists() or not Config.METADATA_JSON_PATH.exists():
        logger.error("❌ Critical Error: Model files not found!")
        return False

    try:
        # FAISS 인덱스 로드
        FAISS_INDEX = faiss.read_index(str(Config.INDEX_PATH))

        # 메타데이터 로드 (JSON)
        with open(Config.METADATA_JSON_PATH, "r", encoding="utf-8") as f:
            metadata_list = json.load(f)

        for item in metadata_list:
            t_id = int(item["track_id"])
            f_id = int(item["faiss_id"])
            TRACK_TO_FAISS[t_id] = f_id
            FAISS_TO_TRACK[f_id] = t_id

        logger.info(f"✅ Resources loaded. Total Tracks: {len(metadata_list)}")
        return True
    except Exception as e:
        logger.error(f"❌ Error loading resources: {e}")
        return False


@APP.route("/recommend", methods=["POST"])
def recommend():
    if not FAISS_INDEX:
        return jsonify({"error": "AI Server not ready"}), 503

    try:
        data = request.get_json()
        seed_id = data.get("track_id")
        k = data.get("k", 5)

        if seed_id is None:
            return jsonify({"error": "Missing 'track_id'"}), 400

        seed_id = int(seed_id)
        if seed_id not in TRACK_TO_FAISS:
            return jsonify({"error": f"Track ID {seed_id} not found."}), 404

        faiss_id = TRACK_TO_FAISS[seed_id]

        # 벡터 복원 및 검색
        query_vector = FAISS_INDEX.reconstruct(faiss_id).reshape(1, -1)
        distances, indices = FAISS_INDEX.search(query_vector, k + 1)

        recommendations = []
        for dist, idx in zip(distances[0], indices[0]):
            if idx == -1 or idx == faiss_id:
                continue
            rec_track_id = FAISS_TO_TRACK.get(idx)
            if rec_track_id:
                recommendations.append(
                    {"track_id": rec_track_id, "distance": float(dist)}
                )

        return jsonify(
            {"status": "success", "seed": seed_id, "recommendations": recommendations}
        )

    except Exception as e:
        logger.error(f"Recommendation failed: {e}")
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    # 1. 사전 점검 수행
    if perform_preflight_check():
        # 2. 리소스 로드 및 서버 시작
        if load_resources():
            APP.run(host="0.0.0.0", port=5000)
        else:
            logger.error("Failed to load resources after check.")
            sys.exit(1)
    else:
        sys.exit(1)