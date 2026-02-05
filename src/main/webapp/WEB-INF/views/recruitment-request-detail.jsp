<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Recruitment Request Detail</title>
    <style>
        .container { width: 70%; margin: auto; }
        table { width: 100%; border-collapse: collapse; }
        td { padding: 10px; border: 1px solid #ccc; }
        .btn { padding: 8px 16px; margin-right: 10px; }
        .approve { background: #28a745; color: white; }
        .reject { background: #dc3545; color: white; }
    </style>

    <script>
        function showRejectPopup() {
            document.getElementById("rejectModal").style.display = "block";
        }
        function hideRejectPopup() {
            document.getElementById("rejectModal").style.display = "none";
        }
    </script>
</head>

<body>
<div class="container">
    <h2>Recruitment Request Detail</h2>

    <table>
        <tr><td>ID</td><td>${request.reqId}</td></tr>
        <tr><td>Department</td><td>${request.department.deptName}</td></tr>
        <tr><td>Job Position</td><td>${request.jobPosition.title}</td></tr>
        <tr><td>Quantity</td><td>${request.quantity}</td></tr>
        <tr><td>Deadline</td><td>${request.deadline}</td></tr>
        <tr><td>Status</td><td>${request.status}</td></tr>
        <tr><td>Reason</td><td>${request.reason}</td></tr>
        <tr><td>Created By</td><td>${request.createdBy.fullName}</td></tr>
        <tr><td>Created At</td><td>${request.createdAt}</td></tr>
    </table>

    <br>

    <!-- APPROVE -->
    <form action="${pageContext.request.contextPath}/hr/recruitment-request/${request.reqId}/approve"
          method="post" style="display:inline">
        <button class="btn approve">APPROVE</button>
    </form>

    <!-- REJECT -->
    <button class="btn reject" onclick="showRejectPopup()">REJECT</button>
</div>

<!-- REJECT POPUP -->
<div id="rejectModal"
     style="display:none; position:fixed; top:30%; left:35%;
            background:#fff; padding:20px; border:1px solid #ccc;">
    <h3>Reject Reason</h3>
    <form action="${pageContext.request.contextPath}/hr/recruitment-request/${request.reqId}/reject"
          method="post">
        <textarea name="reason" required style="width:300px;height:80px"></textarea>
        <br><br>
        <button type="submit" class="btn reject">Submit</button>
        <button type="button" class="btn" onclick="hideRejectPopup()">Cancel</button>
    </form>
</div>

</body>
</html>
