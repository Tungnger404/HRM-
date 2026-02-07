<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"/>
    <title>Employee - Detail</title>
    <style>
        body{font-family:system-ui,Arial;margin:0;background:#f6f7fb;color:#111}
        .wrap{max-width:900px;margin:24px auto;padding:0 16px}
        .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:16px}
        .top{display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap}
        .title{font-size:20px;font-weight:800}
        .btn{height:38px;padding:0 12px;border:1px solid #e5e7eb;border-radius:10px;background:#fff;cursor:pointer;font-weight:700}
        .btn-primary{background:#f5b400;border-color:#f5b400}
        .btn-danger{background:#fee2e2;border-color:#fecaca}
        .grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:12px}
        .field label{display:block;font-size:13px;font-weight:800;color:#6b7280;margin-bottom:6px}
        .field input,.field select{width:100%;height:40px;padding:0 12px;border:1px solid #e5e7eb;border-radius:10px;outline:none}
        .field input:focus,.field select:focus{border-color:#f5b400;box-shadow:0 0 0 4px rgba(245,180,0,.2)}
        .row-actions{display:flex;gap:10px;margin-top:14px;flex-wrap:wrap}
        .msg{margin:12px 0;padding:10px 12px;border-radius:10px;border:1px solid #bbf7d0;background:#ecfdf5;color:#065f46}
        .msg-err{border:1px solid #fecaca;background:#fef2f2;color:#991b1b}
        @media(max-width:720px){.grid{grid-template-columns:1fr}}
    </style>
</head>
<body>
<div class="wrap">
    <div class="card">
        <div class="top">
            <div class="title">Employee Detail #${employee.empId}</div>
            <button class="btn" type="button"
                    onclick="location.href='${pageContext.request.contextPath}/employees'">← Back</button>
        </div>

        <c:if test="${not empty msg}">
            <div class="msg">${msg}</div>
        </c:if>
        <c:if test="${not empty err}">
            <div class="msg msg-err">${err}</div>
        </c:if>

        <!-- UPDATE FORM (bind theo EmployeeAdd) -->
        <form method="post" action="${pageContext.request.contextPath}/employees/save">
            <!-- DTO field: empId -->
            <input type="hidden" name="empId" value="${employee.empId}"/>

            <div class="grid">
                <div class="field">
                    <label>Full Name</label>
                    <input name="fullName" value="${employee.fullName}" required>
                </div>

                <div class="field">
                    <label>Employment Status</label>
                    <select name="employmentStatus">
                        <option value="Active" ${employee.employmentStatus=='Active'?'selected':''}>Active</option>
                        <option value="On Leave" ${employee.employmentStatus=='On Leave'?'selected':''}>On Leave</option>
                        <option value="Resigned" ${employee.employmentStatus=='Resigned'?'selected':''}>Resigned</option>
                    </select>
                </div>

                <div class="field">
                    <label>Email</label>
                    <input name="email" type="email" value="${employee.email}">
                </div>

                <div class="field">
                    <label>Phone</label>
                    <input name="phone" value="${employee.phone}">
                </div>

                <div class="field">
                    <label>Date of Birth</label>
                    <input name="dateOfBirth" type="date" value="${employee.dateOfBirth}">
                </div>

                <div class="field">
                    <label>Hire Date</label>
                    <input name="hireDate" type="date" value="${employee.hireDate}">
                </div>
            </div>

            <div class="row-actions">
                <button class="btn btn-primary" type="submit">Save changes</button>

                <!-- Delete không lồng form -->
                <button class="btn btn-danger" type="button"
                        onclick="if(confirm('Delete this employee?')) document.getElementById('delForm').submit();">
                    Delete
                </button>
            </div>
        </form>

        <form id="delForm" method="post"
              action="${pageContext.request.contextPath}/employees/${employee.empId}/delete"></form>

    </div>
</div>
</body>
</html>
