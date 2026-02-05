<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
  <title>Recruitment Request List (HR)</title>

  <style>
    body {
      font-family: Arial, sans-serif;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 16px;
    }
    th, td {
      border: 1px solid #ccc;
      padding: 8px;
      text-align: left;
    }
    th {
      background-color: #f2f2f2;
    }
    .status {
      font-weight: bold;
    }
    .SUBMITTED { color: #ff9800; }
    .APPROVED { color: #4caf50; }
    .REJECTED { color: #f44336; }
  </style>
</head>

<body>

<h2>Recruitment Request List (HR)</h2>

<table>
  <thead>
  <tr>
    <th>ID</th>
    <th>Department</th>
    <th>Job Position</th>
    <th>Quantity</th>
    <th>Deadline</th>
    <th>Status</th>
    <th>Action</th>
  </tr>
  </thead>

  <tbody>
  <c:forEach var="req" items="${requests}">
    <tr>
      <td>${req.reqId}</td>
      <td>${req.department.deptName}</td>
      <td>${req.jobPosition.title}</td>
      <td>${req.quantity}</td>
      <td>${req.deadline}</td>
      <td class="status ${req.status}">
          ${req.status}
      </td>
      <td>
        <a href="${pageContext.request.contextPath}/hr/recruitment-request/${req.reqId}">
          View
        </a>
      </td>
    </tr>
  </c:forEach>

  <c:if test="${empty requests}">
    <tr>
      <td colspan="7">No recruitment requests found.</td>
    </tr>
  </c:if>
  </tbody>
</table>

</body>
</html>
