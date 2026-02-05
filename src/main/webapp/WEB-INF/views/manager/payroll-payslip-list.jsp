<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>DANH SÁCH BẢNG LƯƠNG</h2>

<form method="get" style="margin: 12px 0;">
    <input name="kw" value="${kw}" placeholder="Search name..." />
    <select name="status">
        <option value="">-- All status --</option>
        <option value="DRAFT" ${status=='DRAFT'?'selected':''}>Draft</option>
        <option value="PENDING_APPROVAL" ${status=='PENDING_APPROVAL'?'selected':''}>Pending</option>
        <option value="APPROVED" ${status=='APPROVED'?'selected':''}>Approved</option>
        <option value="PAID" ${status=='PAID'?'selected':''}>Completed</option>
    </select>
    <button type="submit">Search</button>
</form>

<table border="1" cellpadding="6" cellspacing="0">
    <thead>
    <tr>
        <th>Stt</th>
        <th>Mã NV</th>
        <th>Name</th>
        <th>Kỳ lương</th>
        <th>Thời gian</th>
        <th>Net</th>
        <th>Trạng thái</th>
        <th>Xem</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="r" items="${rows}" varStatus="st">
        <tr>
            <td>${st.index + 1}</td>
            <td>${r.empCode}</td>
            <td>${r.fullName}</td>
            <td>${r.month}/${r.year}</td>
            <td>${r.startDate} - ${r.endDate}</td>
            <td>${r.netSalary}</td>
            <td>${r.statusLabel}</td>
            <td>
                <a href="${pageContext.request.contextPath}/manager/payroll/payslips/${r.payslipId}">View</a>
            </td>
        </tr>
    </c:forEach>

    <c:if test="${empty rows}">
        <tr><td colspan="8">No data</td></tr>
    </c:if>
    </tbody>
</table>
