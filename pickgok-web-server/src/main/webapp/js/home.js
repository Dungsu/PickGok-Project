console.log(">>> Script Loaded (v3.0 - History Log & AutoPlay Toggle)");

document.addEventListener('DOMContentLoaded', function() {
    const playBtn = document.getElementById('playBtn');
    const audio = document.getElementById('audioPlayer');
    const progressFill = document.getElementById('progressFill');
    
    const heartBtn = document.querySelector('.btn-heart');
    const xBtn = document.querySelector('.btn-x'); // X 버튼
    const currentTrackIdInput = document.getElementById('currentTrackId');
    const autoPlayToggle = document.getElementById('autoPlayToggle'); // 토글

    const contextPath = window.contextPath || '';

    // ---------------------------------------------------------
    // 1. 플레이어 기본 기능
    // ---------------------------------------------------------
    if (playBtn && audio) {
        playBtn.addEventListener('click', () => {
            const icon = playBtn.querySelector('i');
            if (audio.paused) {
                audio.play().then(() => {
                    icon.classList.remove('fa-play');
                    icon.classList.add('fa-pause');
                    // 재생 카운트 (생략 가능하거나 유지)
                }).catch(error => console.error("Play error:", error));
            } else {
                audio.pause();
                icon.classList.remove('fa-pause');
                icon.classList.add('fa-play');
            }
        });

        audio.addEventListener('timeupdate', () => {
            if (audio.duration) progressFill.style.width = (audio.currentTime / audio.duration) * 100 + '%';
        });
        
        audio.addEventListener('ended', () => {
            playBtn.querySelector('i').classList.remove('fa-pause');
            playBtn.querySelector('i').classList.add('fa-play');
            progressFill.style.width = '0%';
        });
    }

    // ---------------------------------------------------------
    // 2. 사용자 액션 통합 처리 (Like / Skip)
    // ---------------------------------------------------------
    function handleUserAction(actionType) {
        // 로그인 체크
        if (typeof window.isLoggedIn !== 'undefined' && !window.isLoggedIn) {
            if(confirm("로그인이 필요한 서비스입니다.\n로그인 페이지로 이동하시겠습니까?")) {
                location.href = `${contextPath}/views/user/login.jsp`;
            }
            return;
        }

        const trackId = currentTrackIdInput.value;
        if (!trackId) return;

        // 요청 보낼 액션명 결정
        // actionType이 'heart'인 경우 -> 현재 상태에 따라 'add' 또는 'remove'
        // actionType이 'x'인 경우 -> 'skip'
        
        let serverAction = '';
        
        if (actionType === 'heart') {
            const icon = heartBtn.querySelector('i');
            const isLiked = icon.classList.contains('fa-solid');
            serverAction = isLiked ? 'remove' : 'add';
        } else if (actionType === 'x') {
            serverAction = 'skip';
        }

        console.log(`>>> User Action: ${serverAction} (Track ID: ${trackId})`);

        // AJAX 요청
        fetch(`${contextPath}/like`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'track_id=' + encodeURIComponent(trackId) + '&action=' + serverAction
        })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                // 1. UI 아이콘 업데이트 (하트인 경우만)
                if (serverAction === 'add') {
                    updateHeartUI(true);
                } else if (serverAction === 'remove') {
                    updateHeartUI(false);
                }

                // 2. 다음 곡으로 넘어가기 (remove가 아닐 때)
                // 좋아요(add)나 싫어요(skip)일 때는 다음 곡 정보를 받아서 화면 갱신
                if (serverAction !== 'remove' && data.nextTrack) {
                    loadNextTrack(data.nextTrack);
                }
            } else {
                alert(data.message);
            }
        })
        .catch(err => console.error("AJAX Error:", err));
    }

    // 하트 아이콘 변경 함수
    function updateHeartUI(isFilled) {
        const icon = heartBtn.querySelector('i');
        if (isFilled) {
            icon.classList.remove('fa-regular');
            icon.classList.add('fa-solid');
            icon.style.color = '#e74c3c';
            heartBtn.style.borderColor = '#e74c3c';
        } else {
            icon.classList.remove('fa-solid');
            icon.classList.add('fa-regular');
            icon.style.color = '';
            heartBtn.style.borderColor = '#444';
        }
    }

    // ---------------------------------------------------------
    // 3. 다음 곡 로딩 및 자동 재생 제어
    // ---------------------------------------------------------
    function loadNextTrack(track) {
        // 화면 갱신
        document.querySelector('.music-title').innerText = track.title;
        document.querySelector('.artist-name').innerText = track.artist;
        
        audio.src = contextPath + track.filePath;
        audio.load();
        
        currentTrackIdInput.value = track.trackId;
        
        // 새 곡이므로 하트 초기화 (빈 하트)
        updateHeartUI(false);

        // ★ [핵심] 토글 상태 확인 후 자동 재생 결정
        const isAutoPlay = autoPlayToggle && autoPlayToggle.checked;
        
        if (isAutoPlay) {
            console.log(">>> Auto Play: ON");
            audio.play().then(() => {
                playBtn.querySelector('i').classList.remove('fa-play');
                playBtn.querySelector('i').classList.add('fa-pause');
            }).catch(e => console.log("Autoplay blocked:", e));
        } else {
            console.log(">>> Auto Play: OFF (Paused)");
            playBtn.querySelector('i').classList.remove('fa-pause');
            playBtn.querySelector('i').classList.add('fa-play');
        }
    }

    // ---------------------------------------------------------
    // 4. 이벤트 리스너 연결
    // ---------------------------------------------------------
    if (heartBtn) {
        heartBtn.onclick = () => handleUserAction('heart');
    }
    if (xBtn) {
        // 기존 onclick="location.reload()" 속성을 제거해야 함 (JSP에서 제거하거나 여기서 무시)
        xBtn.onclick = (e) => {
            e.preventDefault(); // 기본 동작 막기
            handleUserAction('x');
        };
    }
});