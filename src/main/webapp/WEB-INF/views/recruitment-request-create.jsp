<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <title>Recruitment Request</title>

    <style>
        body { font-family: Arial; padding: 30px; }

        /* ===== TAB ===== */
        .tabs {
            margin-bottom: 20px;
        }

        .tab-btn {
            padding: 10px 20px;
            border: 1px solid #ccc;
            cursor: pointer;
            background: #f5f5f5;
        }

        .tab-btn.active {
            background: #007bff;
            color: white;
        }

        .tab-content {
            display: none;
            margin-top: 20px;
        }

        .tab-content.active {
            display: block;
        }

        /* ===== FORM ===== */
        label { display: block; margin-top: 15px; }
        input, select, textarea {
            width: 300px;
            padding: 6px;
            margin-top: 5px;
        }

        button {
            margin-top: 20px;
            padding: 10px 20px;
        }

        /* ===== TABLE ===== */
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }

        th, td {
            border: 1px solid #ccc;
            padding: 8px;
            text-align: left;
        }

        .badge {
            padding: 4px 10px;
            border-radius: 5px;
            color: white;
            font-size: 13px;
        }

        .pending { background: orange; }
        .approved { background: green; }
        .rejected { background: red; }
    </style>

    <script>
        function openTab(tabId, btn) {
            document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));

            document.getElementById(tabId).classList.add('active');
            btn.classList.add('active');
        }
    </script>
</head>

<body>

<h2>Recruitment Request</h2>

<!-- ===== TABS ===== -->
<div class="tabs">
    <button class="tab-btn active"
            onclick="openTab('createTab', this)">
        Create Request
    </button>

    <button class="tab-btn"
            onclick="openTab('listTab', this)">
        My Requests
    </button>
</div>

<!-- ================= TAB 1: CREATE ================= -->
<div id="createTab" class="tab-content active">

    <form action="${pageContext.request.contextPath}/recruitment-request/create"
          method="post">

        <input type="hidden" name="creatorId" value="1"/>

        <label>Department</label>
        <select name="departmentId" required>
            <c:forEach items="${departments}" var="d">
                <option value="${d.deptId}">${d.deptName}</option>
            </c:forEach>
        </select>

        <label>Job Position</label>
        <select name="jobId" required>
            <c:forEach items="${jobs}" var="j">
                <option value="${j.jobId}">${j.title}</option>
            </c:forEach>
        </select>

        <label>Quantity</label>
        <input type="number" name="quantity" required/>

        <label>Deadline</label>
        <input type="date" name="deadline" required/>

        <label>Reason</label>
        <textarea name="reason" rows="4"></textarea>

        <button type="submit">Submit Request</button>

        <c:if test="${param.success eq 'true'}">
            <p style="color:green;margin-top:10px;">✅ Submit Request successfully</p>
        </c:if>

    </form>
</div>

<!-- ================= TAB 2: MY REQUESTS ================= -->
<div id="listTab" class="tab-content">

    <table>
        <tr>
            <th>Job Position</th>
            <th>Quantity</th>
            <th>Status</th>
            <th>HR Response</th>
        </tr>

        <c:forEach var="r" items="${requests}">
            <tr>
                <td>${r.jobPosition.title}</td>
                <td>${r.quantity}</td>

                <td>
                    <c:choose>
                        <c:when test="${r.status == 'PENDING'}">
                            <span class="badge pending">Pending</span>
                        </c:when>
                        <c:when test="${r.status == 'APPROVED'}">
                            <span class="badge approved">Approved</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge rejected">Rejected</span>
                        </c:otherwise>
                    </c:choose>
                </td>

                <td>
                    <c:if test="${r.status == 'REJECTED'}">
                        ${r.reason}
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </table>

</div>

</body>
</html>
