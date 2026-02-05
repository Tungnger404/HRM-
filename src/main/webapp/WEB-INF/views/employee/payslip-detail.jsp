<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Payslip Detail</h2>

<div>
    <p><b>Employee:</b> ${p.employeeName}</p>
    <p><b>Net Salary:</b> ${p.netSalary}</p>
    <p>
        <a href="${pageContext.request.contextPath}/employee/payslips/${p.payslipId}/download">Download PDF</a>
    </p>
</div>

<h3>Items</h3>
<table border="1" cellpadding="6">
    <tr>
        <th>Type</th>
        <th>Name</th>
        <th>Amount</th>
    </tr>
    <c:forEach var="it" items="${p.items}">
        <tr>
            <td>${it.type}</td>
            <td>${it.name}</td>
            <td>${it.amount}</td>
        </tr>
    </c:forEach>
</table>

<h3>Submit Payroll Inquiry</h3>
<form method="post" action="${pageContext.request.contextPath}/employee/payslips/${p.payslipId}/inquiry">
    <textarea name="question" rows="4" cols="60" placeholder="Enter your question..."></textarea><br/>
    <button type="submit">Submit</button>
</form>
