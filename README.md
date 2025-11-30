# 🎵 PickGok (픽곡) - AI 기반 음악 추천 플랫폼

> **"당신의 취향을 저격하는 음악 틴더(Tinder for Music)"**

**PickGok**은 사용자가 듣고 있는 음악의 **오디오 파형(Audio Signal)**을 분석하여, 분위기와 음색이 유사한 곡을 실시간으로 추천해 주는 하이브리드 웹 플랫폼입니다.

![Project Status](https://img.shields.io/badge/Status-Development-green)
![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Python](https://img.shields.io/badge/Python-3.9%2B-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-lightblue)

---

## 📚 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [시스템 아키텍처](#-시스템-아키텍처)
3. [기술 스택](#-기술-스택)
4. [주요 기능](#-주요-기능)
5. [설치 및 실행 가이드](#-설치-및-실행-가이드)
6. [디렉터리 구조](#-디렉터리-구조)

---

## 💡 프로젝트 소개

기존의 음악 추천은 주로 '태그'나 '장르' 텍스트에 의존했습니다. PickGok은 **Librosa**를 통해 오디오 자체의 특징(MFCC)을 추출하고, **FAISS 벡터 검색 엔진**을 사용하여 **"실제로 들리는 느낌이 비슷한 곡"**을 찾아냅니다.

사용자는 Tinder 앱처럼 마음에 드는 곡엔 **하트(Like)**를, 마음에 들지 않는 곡은 **X(Skip)**를 눌러 자신의 취향을 수집할 수 있습니다.

---

## 🏗 시스템 아키텍처

이 프로젝트는 **Java 웹 서버(Frontend/Controller)**와 **Python AI 서버(Recommendation Engine)**가 협력하는 **2-Tier Architecture**입니다.

```mermaid
graph LR
    User[User (Web Browser)] -->|HTTP Request| Java[Java Web Server (Tomcat)]
    Java -->|SQL Query| DB[(MySQL Database)]
    Java -->|REST API (JSON)| Python[Python AI Server (Flask)]
    Python -->|Vector Search| FAISS[FAISS Index]
    DB <-->|Metadata Sync| Python
```

- **Java Web Server:** 사용자 요청 처리, 세션 관리, DB CRUD, UI 렌더링 (JSP).
- **Python AI Server:** 오디오 특징 벡터 검색, 유사곡 ID 리스트 반환.
- **MySQL:** 사용자 정보, 곡 메타데이터, 플레이리스트, 재생 기록 저장.

---

## 🛠 기술 스택

### Backend (Web)

- **Language:** Java (JDK 17)
- **Framework:** JSP / Servlet (Model 2 MVC Pattern)
- **Server:** Apache Tomcat 10.1 (Jakarta EE)
- **Database:** MySQL 8.0 (Connector/J)

### AI & Analysis

- **Language:** Python 3.9+
- **Serving:** Flask (Micro-framework)
- **Library:**
  - `Librosa`: 오디오 특징 추출 (MFCC)
  - `FAISS`: 고성능 벡터 유사도 검색
  - `Pandas/NumPy`: 데이터 전처리

### Frontend

- **Language:** HTML5, CSS3, JavaScript (ES6+)
- **Style:** Custom CSS (Dark Theme), FontAwesome Icons
- **Communication:** Fetch API (AJAX)

---

## ✨ 주요 기능

1.  **🎶 AI 유사곡 추천**
    - 현재 재생 중인 곡과 오디오 특징이 가장 유사한 Top 5 곡을 실시간 추천.
2.  **❤️ 좋아요 보관함 (Music Tinder)**
    - 마음에 드는 곡을 '좋아요'하면 즉시 내 보관함에 저장되고, 다음 추천에 반영됨.
3.  **📊 실시간 랭킹 시스템**
    - 사용자들이 많이 재생한 곡을 집계하여 '인기 차트 Top 10' 제공 (관리자 대시보드).
4.  **👤 회원 시스템**
    - 회원가입/로그인, 마이페이지 정보 수정, 관리자 전용 회원 관리 기능.

---

## 🚀 설치 및 실행 가이드

### 1. 사전 준비 (Prerequisites)

- Java JDK 17 이상
- Python 3.9 이상
- MySQL Server 8.0 이상
- Apache Tomcat 10.1
- Eclipse IDE & VS Code

### 2. 데이터베이스 설정

`database/init.sql` 파일을 실행하여 DB와 테이블을 생성합니다. (계정: `root` / `0000` 기준)

```sql
source ./database/init.sql
```

### 3. Python AI 서버 설정

```bash
# 1. 가상환경 생성 및 활성화
cd PickGok-Project
python -m venv .venv
source .venv/bin/activate  # Windows: .\.venv\Scripts\Activate

# 2. 라이브러리 설치
pip install -r pickgok-ai-server/requirements.txt

# 3. 데이터 마이그레이션 (최초 1회)
# (FMA 데이터셋을 DB에 적재하고 인덱스를 생성합니다)
cd pickgok-ai-server/src
python migrate_db.py

# 4. 서버 실행
python app.py
```

### 4. Java 웹 서버 실행 (Eclipse)

1.  Eclipse에서 `File > Open Projects from File System`으로 `pickgok-web-server` 폴더를 엽니다.
2.  `src/main/webapp/data` 폴더에 FMA 음악 파일(`fma_small`)이 있는지 확인합니다.
3.  Tomcat 서버에 프로젝트를 추가하고 **Start** 합니다.
4.  브라우저에서 `http://localhost:8080/PickGok` 접속.

---

## 📂 디렉터리 구조

```text
PickGok-Project/
│
├── README.md                  # 프로젝트 설명서
├── .gitignore                 # Git 무시 설정
│
├── .venv/                     # Python 가상 환경
│   ├── Lib/
│   └── Scripts/
│
├── database/
│   └── init.sql               # DB 테이블 생성 쿼리
│
├── pickgok-ai-server/         # [Python] AI 서버
│   ├── src/
│   │   ├── app.py             # Flask 서버
│   │   ├── build_index.py     # AI 모델 빌드
│   │   └── migrate_db.py      # DB 마이그레이션
│   ├── models/                # FAISS 인덱스 파일 저장소
│   └── requirements.txt       # 라이브러리 목록
│
└── pickgok-web-server/        # [Java] 웹 서버
    ├── .settings/
    ├── build/
    ├── src/
    │   └── main/
    │       ├── java/          # Java 소스 (com.pickgok...)
    │       └── webapp/        # 웹 리소스 루트
    │           ├── META-INF/
    │           ├── WEB-INF/
    │           │   ├── lib/   # jar 파일 (mysql-connector, gson)
    │           │   └── web.xml
    │           │
    │           ├── css/       # 스타일 시트 (home.css, mypage.css)
    │           ├── img/       # 이미지 (album_cover.jpg, logo.png)
    │           ├── js/        # 자바스크립트 (home.js, mypage.js)
    │           ├── data/      # 음악 파일 (fma_small/...)
    │           │
    │           ├── views/     # JSP 뷰 페이지 폴더
    │           │   ├── admin/ # 관리자용 JSP (dashboard.jsp, member_list.jsp, recommend_result.jsp)
    │           │   ├── user/  # 회원용 JSP (login.jsp, join.jsp, mypage.jsp)
    │           │   └── home.jsp  # 메인 화면 (실제 UI)
    │           │
    │           └── index.jsp  # 리다이렉트용 (접속 시 views/home.jsp로 토스)
    │
    └── .project, .classpath   # Eclipse 설정 파일
```

---

**Author:** 동양미래대학교 컴퓨터소프트웨어학과 신동수, 유건희
**License:** MIT License
