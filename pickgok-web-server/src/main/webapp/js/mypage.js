document.addEventListener('DOMContentLoaded', function() {
    const contextPath = window.contextPath;

    // --- [1] 저장된 장르 불러오기 ---
    const selectedGenres = new Set();
    if (window.savedGenresString) {
        const savedList = window.savedGenresString.split(',');
        savedList.forEach(g => {
            const genre = g.trim();
            if(genre) {
                selectedGenres.add(genre);
                const btn = document.querySelector(`.genre-btn[data-genre="${genre}"]`);
                if(btn) btn.classList.add('selected');
            }
        });
    }

    // --- [2] 탭 전환 ---
    window.showTab = function(tabId) {
        document.querySelectorAll('.section').forEach(el => el.classList.remove('active'));
        document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
        
        document.getElementById(tabId).classList.add('active');
        // 현재 클릭된 버튼이 아니라, 해당 함수를 호출하는 버튼들을 찾아 활성화
        // (HTML onclick에서 this를 넘기지 않았으므로 쿼리로 찾음)
        const targetBtn = document.querySelector(`.tab-btn[onclick*="'${tabId}'"]`);
        if(targetBtn) targetBtn.classList.add('active');

        if (tabId === 'info') {
            document.getElementById('pw-check-step').style.display = 'block';
            document.getElementById('edit-form-step').style.display = 'none';
            document.getElementById('check-pw').value = '';
        }
    };

    // --- [3] 장르 토글 ---
    window.toggleGenre = function(btn, genre) {
        if (selectedGenres.has(genre)) {
            selectedGenres.delete(genre);
            btn.classList.remove('selected');
        } else {
            if (selectedGenres.size >= 3) {
                alert("최대 3개까지만 선택 가능합니다.");
                return;
            }
            selectedGenres.add(genre);
            btn.classList.add('selected');
        }
    };

    window.saveGenres = function() {
        const genreString = Array.from(selectedGenres).join(",");
        fetch(`${contextPath}/updateGenre`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'genres=' + encodeURIComponent(genreString)
        })
        .then(res => res.json())
        .then(data => {
            if(data.status === 'success') alert("취향이 저장되었습니다!");
            else alert("저장 실패");
        });
    };

	// --- [3] 테마 설정 (수정됨) ---
	    window.setTheme = function(theme) {
	        // [핵심] html 태그에 data-theme 속성을 부여해야 home.css의 선택자가 작동함
	        document.documentElement.setAttribute('data-theme', theme);
	        
	        // UI 업데이트 (선택된 박스 강조)
	        document.querySelectorAll('.theme-option').forEach(el => el.classList.remove('active'));
	        const activeBtn = document.getElementById('theme-' + theme);
	        if(activeBtn) activeBtn.classList.add('active');
	        
	        // 저장
	        localStorage.setItem('pickgok-theme', theme); 
	    };
    // --- [5] 비밀번호 확인 ---
    window.verifyPassword = function() {
        const pw = document.getElementById('check-pw').value;
        fetch(`${contextPath}/checkPassword`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'password=' + encodeURIComponent(pw)
        })
        .then(res => res.json())
        .then(data => {
            if (data.valid) {
                document.getElementById('pw-check-step').style.display = 'none';
                document.getElementById('edit-form-step').style.display = 'block';
            } else {
                document.getElementById('pw-msg').innerText = "비밀번호 불일치";
            }
        });
    };
});