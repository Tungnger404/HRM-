<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Payroll Inquiries</h2>

<p>
    Filter:
    <a href="${pageContext.request.contextPath}/manager/inquiries">All</a> |
    <a href="${pageContext.request.contextPath}/manager/inquiries?status=OPEN">OPEN</a> |
    <a href="${pageContext.request.contextPath}/manager/inquiries?status=RESOLVED">RESOLVED</a>
</p>

<table border="1" cellpadding="6">
    <tr>
        <th>ID</th>
        <th>Payslip</th>
        <th>EmpId</th>
        <th>Status</th>
        <th>Created</th>
        <th>Action</th>
    </tr>
    <c:forEach var="i" items="${inquiries}">
        <tr>
            <td>${i.id}</td>
            <td>${i.payslipId}</td>
            <td>${i.empId}</td>
            <td>${i.status}</td>
            <td>${i.createdAt}</td>
            <td>
                <a href="${pageContext.request.contextPath}/manager/inquiries/${i.id}">View</a>
            </td>
        </tr>
    </c:forEach>
</table>
