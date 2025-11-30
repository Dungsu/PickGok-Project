import os
import sys
import librosa
import numpy as np
from pathlib import Path


# --- Configuration ---
class Config:
    # 1. 현재 파일(src)의 위치 기준
    CURRENT_FILE = Path(__file__).resolve()
    AI_SERVER_ROOT = CURRENT_FILE.parent.parent
    PROJECT_ROOT = AI_SERVER_ROOT.parent

    # 2. 데이터 경로 (Monorepo 구조 반영)
    DATA_DIR = PROJECT_ROOT / "pickgok-web-server" / "src" / "main" / "webapp" / "data"

    # 3. 세부 경로
    AUDIO_DIR = DATA_DIR / "fma_small"
    METADATA_CSV_PATH = DATA_DIR / "fma_metadata" / "tracks.csv"

    # 4. 모델 경로 (추가됨)
    MODEL_DIR = AI_SERVER_ROOT / "models"
    INDEX_PATH = MODEL_DIR / "music.index"
    METADATA_JSON_PATH = MODEL_DIR / "metadata.json"


def print_header(title):
    print("\n" + "=" * 60)
    print(f"🔍 [Diagnostic] {title}")
    print("=" * 60)


def check_paths():
    """1단계: 데이터 폴더 구조 확인"""
    print_header("Step 1: Data Directory Check")

    print(f" * Web Data Dir: {Config.DATA_DIR}")
    print(f" * Audio Dir   : {Config.AUDIO_DIR}")

    if not Config.DATA_DIR.exists():
        print(f"❌ [FAIL] Web Data Directory not found.")
        return False

    if not Config.AUDIO_DIR.exists():
        print(f"❌ [FAIL] Audio Directory not found.")
        return False

    # 하위 폴더 확인 (000, 001...)
    subfolders = [f for f in Config.AUDIO_DIR.iterdir() if f.is_dir()]
    if len(subfolders) == 0:
        print("❌ [FAIL] No subfolders found inside fma_small.")
        return False

    first_folder = subfolders[0]
    mp3_files = list(first_folder.glob("*.mp3"))

    if mp3_files:
        print(f"✅ [PASS] Audio files found. Sample: {mp3_files[0].name}")
        return mp3_files[0]
    else:
        print(f"❌ [FAIL] No .mp3 files found in '{first_folder.name}'.")
        return False


def check_models():
    """2단계: AI 모델 파일 확인 (추가됨)"""
    print_header("Step 2: AI Model Files Check")

    print(f" * Model Dir: {Config.MODEL_DIR}")

    if not Config.MODEL_DIR.exists():
        print(f"❌ [FAIL] Model directory missing.")
        return False

    missing_files = []
    if not Config.INDEX_PATH.exists():
        missing_files.append("music.index")
    if not Config.METADATA_JSON_PATH.exists():
        missing_files.append("metadata.json")

    if missing_files:
        print(f"❌ [FAIL] Missing model files: {', '.join(missing_files)}")
        print("   👉 Tip: 'initialize_system.py'를 실행하여 모델을 생성해야 합니다.")
        return False

    print("✅ [PASS] All model files (index, metadata) found.")
    return True


def check_audio_decoding(sample_file_path):
    """3단계: 오디오 라이브러리 테스트"""
    print_header("Step 3: Audio Library Test")

    try:
        print("... Testing audio decoding (Librosa) ...")
        # 1초만 로딩해서 속도 최적화
        librosa.load(sample_file_path, sr=22050, duration=1)
        print(f"✅ [PASS] Librosa & FFmpeg are working correctly.")
        return True
    except Exception as e:
        print(f"❌ [FAIL] Audio decoding failed: {e}")
        return False


def main():
    # 1. 데이터 경로 체크
    sample_file = check_paths()
    if not sample_file:
        sys.exit(1)  # 실패 시 종료

    # 2. 모델 파일 체크 (여기서 실패하면 초기화 유도됨)
    if not check_models():
        sys.exit(1)

    # 3. 오디오 라이브러리 체크
    if not check_audio_decoding(sample_file):
        sys.exit(1)

    print_header("🎉 Final Result: SYSTEM READY")
    print("모든 준비가 완료되었습니다. 서버를 시작합니다.")
    sys.exit(0)  # 성공


if __name__ == "__main__":
    main()