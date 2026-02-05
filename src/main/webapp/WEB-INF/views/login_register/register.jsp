<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>SkillUp - Đăng ký</title>

    <style>
        :root{
            --brand: #f5b400;
            --text: #111827;
            --muted: #6b7280;
            --border: #e5e7eb;
            --danger: #ef4444;
            --bg: #ffffff;
            --radius: 12px;
            --shadow: 0 10px 30px rgba(0,0,0,.08);
            --focus: 0 0 0 4px rgba(245,180,0,.25);
            --font: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial;
        }
        *{ box-sizing:border-box; }
        body{
            margin:0;
            font-family: var(--font);
            color: var(--text);
            background:#f8fafc;
        }

        .page{
            min-height:100vh;
            display:grid;
            grid-template-columns: 1fr 1fr;
        }

        .left{
            background: var(--bg);
            display:flex;
            align-items:center;
            justify-content:center;
            padding: 48px 28px;
        }

        .right{
            background: var(--brand);
            display:flex;
            align-items:center;
            justify-content:center;
            padding: 48px 28px;
        }

        .card{ width: min(520px, 100%); }

        .brand{
            font-weight: 800;
            font-size: 34px;
            color: var(--brand);
            margin-bottom: 18px;
            letter-spacing:.2px;
        }

        h1{
            margin: 0 0 22px 0;
            font-size: 34px;
            line-height: 1.15;
            font-weight: 800;
        }

        .hint{ color: var(--muted); font-size: 14px; margin-top:-10px; margin-bottom: 14px; }

        .grid{
            display:grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }
        .field{ margin: 10px 0; }
        label{
            display:block;
            font-size: 14px;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .input{ position:relative; }

        .input input{
            width:100%;
            height:46px;
            padding: 0 44px;
            border: 1px solid var(--border);
            border-radius: 10px;
            outline:none;
            font-size: 15px;
            transition: .15s ease;
            background:#fff;
        }
        .input input::placeholder{ color:#9ca3af; }
        .input input:focus{
            border-color: var(--brand);
            box-shadow: var(--focus);
        }

        .icon-left, .icon-right{
            position:absolute;
            top:50%;
            transform: translateY(-50%);
            display:flex;
            align-items:center;
            justify-content:center;
        }
        .icon-left{ left: 14px; color:#9ca3af; }
        .icon-right{
            right: 10px;
            width: 34px; height: 34px;
            border-radius: 8px;
            cursor:pointer;
            color:#9ca3af;
            transition:.15s ease;
        }
        .icon-right:hover{ background:#f3f4f6; }

        .btn{
            width:100%;
            height:48px;
            border:none;
            border-radius:10px;
            background: var(--brand);
            color:#111827;
            font-weight: 800;
            font-size:16px;
            margin-top: 12px;
            cursor:pointer;
            transition:.15s ease;
            box-shadow: 0 8px 18px rgba(245,180,0,.25);
        }
        .btn:hover{ filter:brightness(.98); transform: translateY(-1px); }
        .btn:active{ transform: translateY(0); }

        .bottom{
            margin-top: 14px;
            text-align:center;
            font-size:14px;
            color:#111827;
        }
        .bottom a{
            color:#7c3aed;
            font-weight: 800;
            text-decoration:none;
        }
        .bottom a:hover{ text-decoration:underline; }

        .msg{
            margin-top: 10px;
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 14px;
        }
        .msg-error{ background:#fef2f2; border:1px solid #fecaca; color:#991b1b; }
        .msg-ok{ background:#ecfdf5; border:1px solid #bbf7d0; color:#065f46; }

        .art-wrap{ width: min(720px, 100%); display:flex; justify-content:center; align-items:center; }
        .art{
            width: min(560px, 92%);
            background: #ffffffcc;
            border-radius: 10px;
            padding: 18px;
            box-shadow: var(--shadow);
        }
        .art img{ width: 100%; height:auto; display:block; border-radius: 8px; }

        @media (max-width: 980px){
            .page{ grid-template-columns: 1fr; }
            .right{ display:none; }
            .grid{ grid-template-columns: 1fr; }
        }
    </style>
</head>

<body>
<div class="page">
    <!-- LEFT -->
    <div class="left">
        <div class="card">
            <div class="brand">SkillUp</div>
            <h1>Đăng ký</h1>
            <div class="hint">Tạo tài khoản mới để bắt đầu học tập.</div>

            <c:if test="${not empty error}">
                <div class="msg msg-error">${error}</div>
            </c:if>
            <c:if test="${not empty msg}">
                <div class="msg msg-ok">${msg}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/register">
                <div class="grid">
                    <div class="field">
                        <label>Họ và tên</label>
                        <div class="input">
              <span class="icon-left">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M20 21a8 8 0 1 0-16 0" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                  <path d="M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" stroke="currentColor" stroke-width="1.6"/>
                </svg>
              </span>
                            <input name="fullName" placeholder="Nhập họ và tên" required />
                        </div>
                    </div>

                    <div class="field">
                        <label>Số điện thoại</label>
                        <div class="input">
              <span class="icon-left">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M22 16.92V20a2 2 0 0 1-2.18 2A19.8 19.8 0 0 1 3 5.18 2 2 0 0 1 5 3h3.09a2 2 0 0 1 2 1.72c.12.86.3 1.7.54 2.5a2 2 0 0 1-.45 2.11L9.1 10.9a16 16 0 0 0 4 4l1.57-1.08a2 2 0 0 1 2.11-.45c.8.24 1.64.42 2.5.54A2 2 0 0 1 22 16.92Z"
                        stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
                            <input name="phone" placeholder="Nhập số điện thoại" />
                        </div>
                    </div>
                </div>

                <div class="field">
                    <label>Email</label>
                    <div class="input">
            <span class="icon-left">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M4 7.5C4 6.12 5.12 5 6.5 5h11C19.88 5 21 6.12 21 7.5v9C21 17.88 19.88 19 18.5 19h-11C5.12 19 4 17.88 4 16.5v-9Z" stroke="currentColor" stroke-width="1.6"/>
                <path d="M6 7l6 5 6-5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
                        <input name="email" type="email" placeholder="Nhập địa chỉ email" required />
                    </div>
                </div>

                <div class="grid">
                    <div class="field">
                        <label>Mật khẩu</label>
                        <div class="input">
              <span class="icon-left">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M7 10V8.5C7 5.46 9.46 3 12.5 3S18 5.46 18 8.5V10" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                  <path d="M6.5 10h12c.83 0 1.5.67 1.5 1.5v7c0 .83-.67 1.5-1.5 1.5h-12C5.67 20 5 19.33 5 18.5v-7C5 10.67 5.67 10 6.5 10Z" stroke="currentColor" stroke-width="1.6"/>
                </svg>
              </span>
                            <input id="pw1" name="password" type="password" placeholder="Tạo mật khẩu" required />
                            <span class="icon-right" onclick="togglePassword('pw1', this)" title="Hiện/ẩn mật khẩu">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="1.6"/>
                  <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" stroke="currentColor" stroke-width="1.6"/>
                </svg>
              </span>
                        </div>
                    </div>

                    <div class="field">
                        <label>Xác nhận mật khẩu</label>
                        <div class="input">
              <span class="icon-left">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M7 10V8.5C7 5.46 9.46 3 12.5 3S18 5.46 18 8.5V10" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                  <path d="M6.5 10h12c.83 0 1.5.67 1.5 1.5v7c0 .83-.67 1.5-1.5 1.5h-12C5.67 20 5 19.33 5 18.5v-7C5 10.67 5.67 10 6.5 10Z" stroke="currentColor" stroke-width="1.6"/>
                </svg>
              </span>
                            <input id="pw2" name="confirmPassword" type="password" placeholder="Nhập lại mật khẩu" required />
                            <span class="icon-right" onclick="togglePassword('pw2', this)" title="Hiện/ẩn mật khẩu">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                  <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="1.6"/>
                  <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" stroke="currentColor" stroke-width="1.6"/>
                </svg>
              </span>
                        </div>
                    </div>
                </div>

                <button class="btn" type="submit">Đăng ký</button>
            </form>

            <div class="bottom">
                Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
            </div>
        </div>
    </div>

    <!-- RIGHT -->
    <div class="right">
        <div class="art-wrap">
            <div class="art">
                <img
                        src="https://storyset.com/illustration/online-learning/amico"
                        onerror="this.src='https://i.imgur.com/4M7IWwP.png'"
                        alt="Illustration" />
            </div>
        </div>
    </div>
</div>

<script>
    function togglePassword(inputId){
        const input = document.getElementById(inputId);
        if(!input) return;
        input.type = (input.type === 'password') ? 'text' : 'password';
    }
</script>
</body>
</html>
