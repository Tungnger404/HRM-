<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Employees - List</title>
    <style>
        body{font-family:system-ui,Arial;margin:0;background:#f6f7fb;color:#111}
        .wrap{max-width:1100px;margin:24px auto;padding:0 16px}
        .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:16px}
        .top{display:flex;gap:12px;align-items:center;justify-content:space-between;flex-wrap:wrap}
        .title{font-size:20px;font-weight:800}
        .actions{display:flex;gap:10px;flex-wrap:wrap}
        input[type="text"]{height:38px;padding:0 12px;border:1px solid #e5e7eb;border-radius:10px;outline:none}
        input[type="text"]:focus{border-color:#f5b400;box-shadow:0 0 0 4px rgba(245,180,0,.2)}
        .btn{height:38px;padding:0 12px;border:1px solid #e5e7eb;border-radius:10px;background:#fff;cursor:pointer;font-weight:700}
        .btn-primary{background:#f5b400;border-color:#f5b400}
        .table{width:100%;border-collapse:collapse;margin-top:12px}
        .table th,.table td{padding:10px;border-bottom:1px solid #eee;text-align:left;font-size:14px}
        .table th{color:#6b7280;font-weight:800}
        .badge{display:inline-block;padding:3px 10px;border-radius:999px;font-size:12px;border:1px solid #e5e7eb;background:#f9fafb}
        .msg{margin:12px 0;padding:10px 12px;border-radius:10px;border:1px solid #bbf7d0;background:#ecfdf5;color:#065f46}
        .msg-err{border:1px solid #fecaca;background:#fef2f2;color:#991b1b}
    </style>
</head>
<body>
<div class="wrap">
    <div class="card">
        <div class="top">
            <div class="title">Employee List</div>

            <div class="actions">
                <form method="get" action="${pageContext.request.contextPath}/employees">
                    <input type="text" name="q" placeholder="Search name..." value="${q}">
                    <button class="btn" type="submit">Search</button>
                </form>

                <button class="btn btn-primary" type="button"
                        onclick="location.href='${pageContext.request.contextPath}/employees/new'">
                    + Add Employee
                </button>
            </div>
        </div>

        <c:if test="${not empty msg}">
            <div class="msg">${msg}</div>
        </c:if>
        <c:if test="${not empty err}">
            <div class="msg msg-err">${err}</div>
        </c:if>

        <table class="table">
            <thead>
            <tr>
                <th>Emp ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Status</th>
                <th style="width:180px">Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${employees}" var="e">
                <tr>
                    <td>${e.empId}</td>
                    <td>${e.fullName}</td>
                    <td><c:out value="${e.email}"/></td>
                    <td><c:out value="${e.phone}"/></td>
                    <td><span class="badge">${e.employmentStatus}</span></td>
                    <td>
                        <button class="btn" type="button"
                                onclick="location.href='${pageContext.request.contextPath}/employees/${e.empId}'">
                            View / Edit
                        </button>
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${empty employees}">
                <tr>
                    <td colspan="6" style="color:#6b7280;padding:14px;">No employees found.</td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
