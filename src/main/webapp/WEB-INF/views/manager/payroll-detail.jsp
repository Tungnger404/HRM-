<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Payroll Batch Detail</h2>
<p><b>Batch:</b> ${b.batchName}</p>
<p><b>Status:</b> ${b.status}</p>
<p><b>Total Gross:</b> ${b.totalGross}</p>
<p><b>Total Net:</b> ${b.totalNet}</p>

<p>
    <a href="${pageContext.request.contextPath}/manager/payroll/batches/${b.batchId}/export">Export Excel</a>
</p>

<c:if test="${b.status == 'DRAFT'}">
    <form method="post" action="${pageContext.request.contextPath}/manager/payroll/batches/${b.batchId}/submit">
        <button type="submit">Submit For Approval</button>
    </form>
</c:if>

<c:if test="${b.status == 'PENDING_APPROVAL'}">
    <form method="post" action="${pageContext.request.contextPath}/manager/payroll/batches/${b.batchId}/approve" style="display:inline;">
        <button type="submit">Approve</button>
    </form>
    <form method="post" action="${pageContext.request.contextPath}/manager/payroll/batches/${b.batchId}/reject" style="display:inline;">
        <button type="submit">Reject</button>
    </form>
</c:if>

<h3>Payslips</h3>
<table border="1" cellpadding="6">
    <tr>
        <th>PayslipId</th>
        <th>Employee</th>
        <th>Total Income</th>
        <th>Total Deduction</th>
        <th>Net</th>
    </tr>
    <c:forEach var="p" items="${b.payslips}">
        <tr>
            <td>
                <a href="${pageContext.request.contextPath}/manager/payroll/payslips/${p.payslipId}">
                        ${p.payslipId}
                </a>
            </td>
        </tr>
    </c:forEach>
</table>
