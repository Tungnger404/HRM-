<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Create Recruitment Request</title>
    <style>
        body { font-family: Arial; padding: 30px; }
        label { display: block; margin-top: 15px; }
        input, select, textarea {
            width: 300px;
            padding: 6px;
            margin-top: 5px;
        }
        button {
            margin-top: 20px;
            padding: 10px 20px;
        }
        #toast-success {
            position: fixed;
            top: 20px;
            right: 20px;
            background-color: #4CAF50;
            color: white;
            padding: 15px 25px;
            border-radius: 6px;
            font-size: 14px;
            box-shadow: 0 4px 10px rgba(0,0,0,0.2);
            animation: slideIn 0.5s ease, fadeOut 0.5s ease 3s forwards;
            z-index: 9999;
        }

        @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }

        @keyframes fadeOut {
            to { opacity: 0; transform: translateX(100%); }
        }
    </style>
</head>
<body>

<h2>Create Recruitment Request</h2>

<form action="${pageContext.request.contextPath}/recruitment-request/create" method="post">

    <input type="hidden" name="creatorId" value="1"/>

    <label>Department</label>
    <select name="departmentId" required>
        <c:forEach items="${departments}" var="d">
            <option value="${d.deptId}">${d.deptName}</option>
        </c:forEach>
    </select>

    <label>Job Position</label>
    <select name="jobId" required>
        <c:forEach items="${jobs}" var="j">
            <option value="${j.jobId}">${j.title}</option>
        </c:forEach>
    </select>

    <label>Quantity</label>
    <input type="number" name="quantity" required />

    <label>Deadline</label>
    <input type="date" name="deadline" required />

    <label>Reason</label>
    <textarea name="reason" rows="4"></textarea>

    <button type="submit">Create Request</button>
    <c:if test="${param.success eq 'true'}">
        <div id="toast-success">✅ Create request successfully</div>
    </c:if>
</form>

</body>
</html>
