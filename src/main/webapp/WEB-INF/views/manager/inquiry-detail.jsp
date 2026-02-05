<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Inquiry Detail</h2>

<p><b>ID:</b> ${inq.id}</p>
<p><b>Payslip:</b> ${inq.payslipId}</p>
<p><b>Employee:</b> ${inq.empId}</p>
<p><b>Status:</b> ${inq.status}</p>

<h3>Question</h3>
<p>${inq.question}</p>

<h3>Answer</h3>
<c:if test="${inq.status == 'RESOLVED'}">
    <p>${inq.answer}</p>
</c:if>

<c:if test="${inq.status != 'RESOLVED'}">
    <form method="post" action="${pageContext.request.contextPath}/manager/inquiries/${inq.id}/resolve">
        <textarea name="answer" rows="4" cols="80" placeholder="Enter answer..."></textarea><br/>
        <button type="submit">Resolve</button>
    </form>
</c:if>

<p>
    <a href="${pageContext.request.contextPath}/manager/inquiries">Back</a>
</p>
