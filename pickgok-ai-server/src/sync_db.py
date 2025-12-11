import sys
import logging
import pandas as pd
import mysql.connector
from pathlib import Path
from tqdm import tqdm


# --- Configuration ---
class Config:
    BASE_DIR = Path(__file__).resolve().parent.parent
    WEB_DATA_DIR = (
        BASE_DIR.parent / "pickgok-web-server" / "src" / "main" / "webapp" / "data"
    )
    METADATA_CSV_PATH = WEB_DATA_DIR / "fma_metadata" / "tracks.csv"

    DB_CONFIG = {
        "host": "localhost",
        "user": "root",
        "password": "0000",  # <--- 비밀번호 확인!
        "database": "pick_gok",
    }


logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(message)s")
logger = logging.getLogger("SyncDB")


def load_metadata():
    if not Config.METADATA_CSV_PATH.exists():
        logger.error(f"❌ CSV File not found: {Config.METADATA_CSV_PATH}")
        sys.exit(1)

    logger.info("📂 Loading metadata from CSV...")
    try:
        # CSV 로드 및 전처리 (필요한 컬럼만)
        tracks = pd.read_csv(Config.METADATA_CSV_PATH, index_col=0, header=[0, 1])
        small_subset = tracks[tracks[("set", "subset")] == "small"]

        df = pd.DataFrame(
            {
                "track_id": small_subset.index,
                "title": small_subset[("track", "title")],
                "artist": small_subset[("artist", "name")],
                "genre": small_subset[("track", "genre_top")],
                "duration": small_subset[("track", "duration")],
            }
        ).set_index("track_id")

        df["genre"] = df["genre"].fillna("Unknown")
        return df
    except Exception as e:
        logger.error(f"CSV Load Error: {e}")
        sys.exit(1)


def migrate_to_db(df):
    logger.info(f"🚀 Starting Migration for {len(df)} tracks...")
    conn = None
    try:
        conn = mysql.connector.connect(**Config.DB_CONFIG)
        cursor = conn.cursor()

        query = """
            INSERT INTO tracks (track_id, title, artist, genre, duration, file_path) 
            VALUES (%s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE 
                title=VALUES(title), artist=VALUES(artist), 
                genre=VALUES(genre), duration=VALUES(duration)
        """

        batch = []
        for tid, row in tqdm(df.iterrows(), total=len(df)):
            # 웹 경로 생성 (/data/fma_small/000/000123.mp3)
            tid_str = "{:06d}".format(int(tid))
            web_path = f"/data/fma_small/{tid_str[:3]}/{tid_str}.mp3"

            batch.append(
                (
                    int(tid),
                    str(row["title"])[:255],
                    str(row["artist"])[:255],
                    str(row["genre"]),
                    int(float(row["duration"])),
                    web_path,
                )
            )

            if len(batch) >= 1000:
                cursor.executemany(query, batch)
                conn.commit()
                batch = []

        if batch:
            cursor.executemany(query, batch)
            conn.commit()

        logger.info("✅ Database Migration Complete.")

    except Exception as e:
        logger.error(f"❌ Migration Failed: {e}")
    finally:
        if conn:
            conn.close()


if __name__ == "__main__":
    df = load_metadata()
    migrate_to_db(df)
