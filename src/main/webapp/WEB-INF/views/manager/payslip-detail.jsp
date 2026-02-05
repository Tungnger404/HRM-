<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Manager - Payslip Detail</h2>

<p><b>Payslip ID:</b> ${p.payslipId}</p>
<p><b>Employee:</b> ${p.employeeName} (empId=${p.empId})</p>
<p><b>Net Salary:</b> ${p.netSalary}</p>

<p><b>Base Salary:</b> ${p.baseSalary}</p>
<p><b>Work Days:</b> ${p.actualWorkDays} / ${p.standardWorkDays}</p>
<p><b>OT Hours:</b> ${p.otHours}</p>

<h3>Items</h3>
<table border="1" cellpadding="6">
  <tr>
    <th>Type</th>
    <th>Name</th>
    <th>Amount</th>
    <th>Manual</th>
  </tr>
  <c:forEach var="it" items="${p.items}">
    <tr>
      <td>${it.type}</td>
      <td>${it.name}</td>
      <td>${it.amount}</td>
      <td>${it.manual}</td>
    </tr>
  </c:forEach>
</table>

<p>
  <a href="${pageContext.request.contextPath}/manager/inquiries?status=OPEN">View OPEN Inquiries</a>
</p>
