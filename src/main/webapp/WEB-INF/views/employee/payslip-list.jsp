<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>My Payslips</h2>

<table border="1" cellpadding="6">
    <tr>
        <th>ID</th>
        <th>Total Income</th>
        <th>Total Deduction</th>
        <th>Net</th>
        <th>Action</th>
    </tr>
    <c:forEach var="p" items="${payslips}">
        <tr>
            <td>${p.payslipId}</td>
            <td>${p.totalIncome}</td>
            <td>${p.totalDeduction}</td>
            <td>${p.netSalary}</td>
            <td><a href="${pageContext.request.contextPath}/employee/payslips/${p.payslipId}">View</a></td>
        </tr>
    </c:forEach>
</table>
