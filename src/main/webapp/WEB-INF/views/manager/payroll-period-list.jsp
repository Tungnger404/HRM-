<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Payroll Periods</h2>

<table border="1" cellpadding="6">
  <tr>
    <th>Period</th>
    <th>Status</th>
    <th>Locked</th>
    <th>Action</th>
  </tr>
  <c:forEach var="p" items="${periods}">
    <tr>
      <td>${p.month}/${p.year}</td>
      <td>${p.status}</td>
      <td>${p.locked}</td>
      <td>
        <a href="${pageContext.request.contextPath}/manager/payroll/periods/${p.id}/batches">View Batches</a>
        <c:if test="${!p.locked}">
          <form method="post" action="${pageContext.request.contextPath}/manager/payroll/periods/${p.id}/generate" style="display:inline;">
            <button type="submit">Generate Draft</button>
          </form>
        </c:if>
      </td>
    </tr>
  </c:forEach>
</table>
