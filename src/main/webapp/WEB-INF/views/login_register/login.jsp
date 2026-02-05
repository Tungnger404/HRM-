<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>SkillUp - Đăng nhập</title>

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

        .field{ margin: 14px 0; }

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

        .row{
            display:flex;
            align-items:center;
            justify-content:space-between;
            gap: 14px;
            margin-top: 8px;
        }
        .check{
            display:flex;
            align-items:center;
            gap:10px;
            font-size:14px;
        }
        .check input{
            width:16px; height:16px;
            accent-color: var(--brand);
            cursor:pointer;
        }

        .link-danger{
            color: var(--danger);
            font-weight: 700;
            text-decoration:none;
            font-size:14px;
        }
        .link-danger:hover{ text-decoration:underline; }

        .btn{
            width:100%;
            height:48px;
            border:none;
            border-radius:10px;
            background: var(--brand);
            color:#111827;
            font-weight: 800;
            font-size:16px;
            margin-top: 16px;
            cursor:pointer;
            transition:.15s ease;
            box-shadow: 0 8px 18px rgba(245,180,0,.25);
        }
        .btn:hover{ filter:brightness(.98); transform: translateY(-1px); }
        .btn:active{ transform: translateY(0); }

        .divider{
            display:flex;
            align-items:center;
            gap: 12px;
            margin: 22px 0 14px;
            color:#9ca3af;
            font-size: 13px;
            justify-content:center;
        }
        .divider:before, .divider:after{
            content:"";
            height:1px;
            background:#e5e7eb;
            flex:1;
            max-width: 160px;
        }

        .google-btn{
            width: 100%;
            height: 48px;
            border: 1px solid var(--border);
            background:#fff;
            border-radius: 999px;
            display:flex;
            align-items:center;
            justify-content:center;
            gap:10px;
            cursor:pointer;
            font-weight: 700;
            color:#111827;
            transition:.15s ease;
        }
        .google-btn:hover{ background:#f9fafb; }

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

        /* right illustration */
        .art-wrap{
            width: min(720px, 100%);
            display:flex;
            justify-content:center;
            align-items:center;
        }
        .art{
            width: min(560px, 92%);
            background: #ffffffcc;
            border-radius: 10px;
            padding: 18px;
            box-shadow: var(--shadow);
        }
        .art img{
            width: 100%;
            height: auto;
            display:block;
            border-radius: 8px;
        }

        /* message */
        .msg{
            margin-top: 10px;
            padding: 10px 12px;
            border-radius: 10px;
            font-size: 14px;
        }
        .msg-error{ background:#fef2f2; border:1px solid #fecaca; color:#991b1b; }
        .msg-ok{ background:#ecfdf5; border:1px solid #bbf7d0; color:#065f46; }

        @media (max-width: 980px){
            .page{ grid-template-columns: 1fr; }
            .right{ display:none; }
            .card{ max-width: 520px; }
        }
    </style>
</head>

<body>
<div class="page">
    <!-- LEFT: FORM -->
    <div class="left">
        <div class="card">
            <div class="brand">SkillUp</div>
            <h1>Đăng nhập</h1>

            <!-- Nếu controller set error/msg -->
            <c:if test="${not empty error}">
                <div class="msg msg-error">${error}</div>
            </c:if>
            <c:if test="${not empty msg}">
                <div class="msg msg-ok">${msg}</div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/login">
                <div class="field">
                    <label>Email</label>
                    <div class="input">
            <span class="icon-left">
              <!-- mail icon -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M4 7.5C4 6.12 5.12 5 6.5 5h11C19.88 5 21 6.12 21 7.5v9C21 17.88 19.88 19 18.5 19h-11C5.12 19 4 17.88 4 16.5v-9Z" stroke="currentColor" stroke-width="1.6"/>
                <path d="M6 7l6 5 6-5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
                        <input name="email" type="email" placeholder="Nhập địa chỉ email" required />
                    </div>
                </div>

                <div class="field">
                    <label>Mật khẩu</label>
                    <div class="input">
            <span class="icon-left">
              <!-- lock icon -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M7 10V8.5C7 5.46 9.46 3 12.5 3S18 5.46 18 8.5V10" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                <path d="M6.5 10h12c.83 0 1.5.67 1.5 1.5v7c0 .83-.67 1.5-1.5 1.5h-12C5.67 20 5 19.33 5 18.5v-7C5 10.67 5.67 10 6.5 10Z" stroke="currentColor" stroke-width="1.6"/>
              </svg>
            </span>

                        <input id="password" name="password" type="password" placeholder="Nhập mật khẩu" required />

                        <span class="icon-right" onclick="togglePassword('password', this)" title="Hiện/ẩn mật khẩu">
              <!-- eye icon -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" stroke="currentColor" stroke-width="1.6"/>
                <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" stroke="currentColor" stroke-width="1.6"/>
              </svg>
            </span>
                    </div>
                </div>

                <div class="row">
                    <label class="check">
                        <input type="checkbox" name="remember" value="1" />
                        Ghi nhớ đăng nhập
                    </label>

                    <a class="link-danger" href="${pageContext.request.contextPath}/forgot-password">Quên mật khẩu?</a>
                </div>

                <button class="btn" type="submit">Đăng nhập</button>
            </form>

            <div class="divider">hoặc tiếp tục với</div>

            <button class="google-btn" type="button"
                    onclick="location.href='${pageContext.request.contextPath}/oauth2/authorization/google'">
                <!-- google icon -->
                <svg width="18" height="18" viewBox="0 0 48 48">
                    <path fill="#FFC107" d="M43.611 20.083H42V20H24v8h11.303C33.678 32.644 29.234 36 24 36c-6.627 0-12-5.373-12-12s5.373-12 12-12c3.059 0 5.842 1.154 7.961 3.039l5.657-5.657C34.968 6.053 29.716 4 24 4 12.955 4 4 12.955 4 24s8.955 20 20 20 20-8.955 20-20c0-1.341-.138-2.65-.389-3.917z"/>
                    <path fill="#FF3D00" d="M6.306 14.691l6.571 4.819C14.655 16.108 18.961 12 24 12c3.059 0 5.842 1.154 7.961 3.039l5.657-5.657C34.968 6.053 29.716 4 24 4c-7.682 0-14.344 4.337-17.694 10.691z"/>
                    <path fill="#4CAF50" d="M24 44c5.123 0 10.179-1.961 13.84-5.657l-6.39-5.409C29.463 34.436 26.883 36 24 36c-5.212 0-9.642-3.334-11.271-7.946l-6.522 5.025C9.505 39.556 16.227 44 24 44z"/>
                    <path fill="#1976D2" d="M43.611 20.083H42V20H24v8h11.303c-1.246 3.134-3.67 5.639-6.853 6.934l.001-.001 6.39 5.409C34.399 40.739 44 35 44 24c0-1.341-.138-2.65-.389-3.917z"/>
                </svg>
                Tiếp tục sử dụng dịch vụ bằng Google
            </button>

            <div class="bottom">
                Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký</a>
            </div>
        </div>
    </div>

    <!-- RIGHT: ILLUSTRATION -->
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
    function togglePassword(inputId, btn){
        const input = document.getElementById(inputId);
        if(!input) return;
        input.type = (input.type === 'password') ? 'text' : 'password';
    }
</script>
</body>
</html>
