import os
import sys
from pathlib import Path
import librosa

# 1. 경로 설정 (로그에 찍힌 경로 기반)
BASE_DIR = Path(__file__).resolve().parent.parent # pickgok-ai-server
PROJECT_ROOT = BASE_DIR.parent # PickGok
WEB_DATA_DIR = PROJECT_ROOT / "pickgok-web-server" / "src" / "main" / "webapp" / "data"
AUDIO_DIR = WEB_DATA_DIR / "fma_small"

# 2. 테스트 대상 파일 (000 폴더의 000002.mp3)
target_id = 2
tid_str = '{:06d}'.format(target_id)
folder_code = tid_str[:3]
file_path = AUDIO_DIR / folder_code / f"{tid_str}.mp3"

print("="*60)
print(f"🔍 [DEBUG] Path Diagnosis")
print(f"   - Expected Audio Dir: {AUDIO_DIR}")
print(f"   - Target File Path  : {file_path}")
print("="*60)

# 3. 파일 존재 여부 확인
if not file_path.exists():
    print(f"❌ [FAIL] File NOT found at: {file_path}")
    print("-" * 60)
    print("👉 [해결 체크리스트]")
    print("1. 탐색기로 위 경로에 실제 파일이 있는지 확인하세요.")
    print("2. 혹시 'fma_small' 폴더 안에 또 'fma_small'이 들어있지 않은지 확인하세요.")

    # 폴더 구조 힌트
    if AUDIO_DIR.exists():
        print(f"   (Info: '{AUDIO_DIR.name}' folder exists. Listing contents...)")
        try:
            items = list(AUDIO_DIR.iterdir())[:5]
            for item in items:
                print(f"    - {item.name}")
        except: pass
    else:
        print(f"   (Error: '{AUDIO_DIR}' folder itself does not exist!)")

else:
    print(f"✅ [PASS] File found!")
    print("   - Trying to load audio with Librosa...")

    try:
        # 4. 오디오 로딩 테스트 (FFmpeg 확인)
        y, sr = librosa.load(file_path, sr=22050, duration=5)
        print(f"✅ [SUCCESS] Audio loaded successfully. (Shape: {y.shape})")
        print("   -> 코드는 정상입니다. build_index.py를 다시 실행해보세요.")
    except Exception as e:
        print(f"❌ [FAIL] Librosa load error:")
        print(f"   {e}")
        print("-" * 60)
        print("👉 [해결책] FFmpeg가 설치되지 않았거나 환경변수에 없습니다.")
        print("   1. https://www.gyan.dev/ffmpeg/builds/ 에서 ffmpeg-git-full.7z 다운로드")
        print("   2. 압축 해제 후 'bin' 폴더 경로를 Windows 환경변수 'Path'에 추가")
        print("   3. 재부팅 후 다시 시도")

print("="*60)