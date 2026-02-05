<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Payroll Batches (Period ${periodId})</h2>

<table border="1" cellpadding="6">
  <tr>
    <th>Batch</th>
    <th>Status</th>
    <th>Total Gross</th>
    <th>Total Net</th>
    <th>Action</th>
  </tr>
  <c:forEach var="b" items="${batches}">
    <tr>
      <td>${b.name}</td>
      <td>${b.status}</td>
      <td>${b.totalGross}</td>
      <td>${b.totalNet}</td>
      <td><a href="${pageContext.request.contextPath}/manager/payroll/batches/${b.id}">View</a></td>
    </tr>
  </c:forEach>
</table>
