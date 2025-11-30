<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>PickGok - 회원가입</title>
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<style>
body {
	justify-content: center;
	align-items: center;
	flex-direction: column;
}

.auth-box {
	width: 400px;
	padding: 40px;
	background: #1e1e1e;
	border: 1px solid #333;
	border-radius: 10px;
	text-align: center;
}

.input-group {
	margin-bottom: 20px;
	text-align: left;
	position: relative;
}

.input-group label {
	display: block;
	color: #888;
	margin-bottom: 5px;
	font-size: 0.9rem;
}

/* 입력창 스타일 */
.input-wrapper {
	position: relative;
	display: flex;
	align-items: center;
}

.input-wrapper input {
	width: 100%;
	padding: 12px;
	background: #252525;
	border: 1px solid #444;
	color: #fff;
	border-radius: 5px;
	outline: none;
}

.input-wrapper input:focus {
	border-color: #2ecc71;
}

/* 비밀번호 눈 아이콘 */
.eye-icon {
	position: absolute;
	right: 15px;
	color: #888;
	cursor: pointer;
}

.eye-icon:hover {
	color: #fff;
}

/* 중복확인 버튼 */
.btn-check {
	position: absolute;
	right: 5px;
	top: 5px;
	bottom: 5px;
	background: #333;
	border: 1px solid #555;
	color: #ccc;
	padding: 0 10px;
	border-radius: 3px;
	cursor: pointer;
	font-size: 0.8rem;
}

.btn-check:hover {
	background: #444;
	color: white;
}

/* 검증 메시지 */
.msg {
	font-size: 0.8rem;
	margin-top: 5px;
	display: block;
	height: 15px;
}

.msg.success {
	color: #2ecc71;
}

.msg.error {
	color: #ff5e57;
}

.btn-submit {
	width: 100%;
	padding: 12px;
	background: #2ecc71;
	border: none;
	color: #000;
	font-weight: bold;
	border-radius: 5px;
	cursor: pointer;
	margin-top: 10px;
	opacity: 0.5;
	pointer-events: none;
}

.btn-submit.active {
	opacity: 1;
	pointer-events: auto;
}

.btn-submit:hover.active {
	background: #27ae60;
}
</style>
</head>
<body>
	<div class="auth-box">
		<h2 style="color: white;">JOIN US</h2>
		<form id="joinForm" action="${pageContext.request.contextPath}/join"
			method="post">

			<div class="input-group">
				<label>아이디</label>
				<div class="input-wrapper">
					<input type="text" name="userid" id="userid"
						placeholder="영문/숫자 4~20자" required>
					<button type="button" class="btn-check"
						onclick="checkDuplicate('id')">중복확인</button>
				</div>
				<span class="msg" id="idMsg"></span>
			</div>

			<div class="input-group">
				<label>비밀번호</label>
				<div class="input-wrapper">
					<input type="password" name="password" id="password"
						placeholder="비밀번호 입력" required onkeyup="checkPasswordMatch()">
					<i class="fa-solid fa-eye eye-icon"
						onclick="togglePw('password', this)"></i>
				</div>
			</div>

			<div class="input-group">
				<label>비밀번호 확인</label>
				<div class="input-wrapper">
					<input type="password" id="passwordConfirm" placeholder="한 번 더 입력"
						required onkeyup="checkPasswordMatch()"> <i
						class="fa-solid fa-eye eye-icon"
						onclick="togglePw('passwordConfirm', this)"></i>
				</div>
				<span class="msg" id="pwMsg"></span>
			</div>

			<div class="input-group">
				<label>이름 (닉네임)</label>
				<div class="input-wrapper">
					<input type="text" name="name" id="name" required
						onkeyup="checkFormValidity()">
				</div>
			</div>

			<div class="input-group">
				<label>이메일</label>
				<div class="input-wrapper">
					<input type="email" name="email" id="email"
						placeholder="example@email.com" required>
					<button type="button" class="btn-check"
						onclick="checkDuplicate('email')">중복확인</button>
				</div>
				<span class="msg" id="emailMsg"></span>
			</div>

			<button type="submit" class="btn-submit" id="submitBtn">가입하기</button>
		</form>

		<div style="margin-top: 20px; font-size: 0.85rem; color: #666;">
			이미 계정이 있으신가요? <a href="login.jsp"
				style="color: #2ecc71; text-decoration: none;">로그인</a>
		</div>
	</div>

	<script>
        // 상태 변수 (모두 true여야 가입 가능)
        let isIdChecked = false;
        let isEmailChecked = false;
        let isPwMatch = false;

        // 1. 중복 확인 (AJAX)
        function checkDuplicate(type) {
            const value = document.getElementById(type === 'id' ? 'userid' : 'email').value;
            const msgBox = document.getElementById(type === 'id' ? 'idMsg' : 'emailMsg');

            if (!value) {
                alert("내용을 입력해주세요.");
                return;
            }

            fetch('${pageContext.request.contextPath}/checkDuplicate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'type=' + type + '&value=' + encodeURIComponent(value)
            })
            .then(res => res.json())
            .then(data => {
                if (data.exists) {
                    msgBox.innerText = "❌ 이미 사용 중입니다.";
                    msgBox.className = "msg error";
                    if(type === 'id') isIdChecked = false;
                    if(type === 'email') isEmailChecked = false;
                } else {
                    msgBox.innerText = "✅ 사용 가능합니다.";
                    msgBox.className = "msg success";
                    if(type === 'id') isIdChecked = true;
                    if(type === 'email') isEmailChecked = true;
                }
                checkFormValidity();
            })
            .catch(err => console.error(err));
        }

        // 2. 비밀번호 일치 확인
        function checkPasswordMatch() {
            const pw = document.getElementById('password').value;
            const pwConfirm = document.getElementById('passwordConfirm').value;
            const msgBox = document.getElementById('pwMsg');

            if (pw === "" && pwConfirm === "") {
                msgBox.innerText = "";
                isPwMatch = false;
            } else if (pw === pwConfirm) {
                msgBox.innerText = "✅ 비밀번호가 일치합니다.";
                msgBox.className = "msg success";
                isPwMatch = true;
            } else {
                msgBox.innerText = "❌ 비밀번호가 일치하지 않습니다.";
                msgBox.className = "msg error";
                isPwMatch = false;
            }
            checkFormValidity();
        }

        // 3. 비밀번호 보기 토글
        function togglePw(inputId, icon) {
            const input = document.getElementById(inputId);
            if (input.type === "password") {
                input.type = "text";
                icon.classList.remove("fa-eye");
                icon.classList.add("fa-eye-slash");
            } else {
                input.type = "password";
                icon.classList.remove("fa-eye-slash");
                icon.classList.add("fa-eye");
            }
        }

        // 4. 폼 유효성 검사 (가입 버튼 활성화)
        function checkFormValidity() {
            const name = document.getElementById('name').value;
            const submitBtn = document.getElementById('submitBtn');

            // 아이디 중복확인 OK, 이메일 중복확인 OK, 비번 일치 OK, 이름 입력됨
            if (isIdChecked && isEmailChecked && isPwMatch && name.trim() !== "") {
                submitBtn.classList.add('active');
            } else {
                submitBtn.classList.remove('active');
            }
        }

        // 입력값 변경 시 중복확인 초기화 (다시 확인받게 함)
        document.getElementById('userid').addEventListener('input', () => {
            isIdChecked = false;
            document.getElementById('idMsg').innerText = "";
            checkFormValidity();
        });
        document.getElementById('email').addEventListener('input', () => {
            isEmailChecked = false;
            document.getElementById('emailMsg').innerText = "";
            checkFormValidity();
        });
    </script>
</body>
</html>