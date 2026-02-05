<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<h2 style="text-align:center;">DANH SÁCH BẢNG LƯƠNG</h2>

<c:if test="${not empty msg}">
    <p style="color:green;">${msg}</p>
</c:if>

<div style="display:flex; align-items:center; justify-content:center; gap:12px; margin:16px 0;">
    <form method="get" action="${pageContext.request.contextPath}/manager/payroll/payslips"
          style="display:flex; gap:8px; align-items:center;">
        <input name="q" value="${fn:escapeXml(q)}" placeholder="Search (name / empId)"
               style="width:260px; padding:6px;"/>
        <select name="status" style="padding:6px;">
            <c:forEach var="op" items="${statusOptions}">
                <option value="${op}" <c:if test="${op == status}">selected</c:if>>
                    <c:choose>
                        <c:when test="${op == ''}">All Status</c:when>
                        <c:otherwise>${op}</c:otherwise>
                    </c:choose>
                </option>
            </c:forEach>
        </select>
        <button type="submit" style="padding:6px 12px;">Search</button>
    </form>
</div>

<form method="post" action="${pageContext.request.contextPath}/manager/payroll/payslips/bulk"
      style="display:flex; flex-direction:column; align-items:center; gap:10px;">

    <div style="display:flex; gap:10px;">
        <button type="submit" name="action" value="approve" style="padding:6px 18px;">Approve</button>
        <button type="submit" name="action" value="reject" style="padding:6px 18px;">Reject</button>
    </div>

    <table border="1" cellpadding="6" cellspacing="0"
           style="border-collapse:collapse; min-width:900px; margin-top:10px;">
        <thead>
        <tr>
            <th>Select</th>
            <th>Stt</th>
            <th>Mã NV</th>
            <th>Name</th>
            <th>Kỳ lương</th>
            <th>Thời gian</th>
            <th>Tổng lương (Net)</th>
            <th>Trạng thái</th>
            <th>Xem</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="r" items="${rows}" varStatus="st">
            <tr>
                <td style="text-align:center;">
                    <input type="checkbox" name="batchIds" value="${r.batchId}"/>
                </td>
                <td>${st.index + 1}</td>
                <td>${r.empCode}</td>
                <td>${r.fullName}</td>
                <td>${r.month}/${r.year}</td>
                <td>${r.startDate} - ${r.endDate}</td>
                <td>${r.netSalary}</td>
                <td>${r.statusLabel}</td>
                <td>
                    <a href="${pageContext.request.contextPath}/manager/payroll/payslips/${r.payslipId}">
                        <button type="button">View</button>
                    </a>
                </td>
            </tr>
        </c:forEach>

        <c:if test="${empty rows}">
            <tr>
                <td colspan="9" style="text-align:center; color:#888;">Chưa có dữ liệu payslips</td>
            </tr>
        </c:if>
        </tbody>
    </table>
</form>
