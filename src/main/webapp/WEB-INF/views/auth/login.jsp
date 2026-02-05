<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<h2>Login</h2>

<c:if test="${param.error != null}">
    <p style="color:red;">
        Login failed:
        <c:out value="${sessionScope['SPRING_SECURITY_LAST_EXCEPTION'].message}"/>
    </p>
</c:if>
<c:if test="${param.logout != null}">
    <p style="color:green;">Logged out</p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/login">
    <div>
        <label>Username</label><br/>
        <input name="username"/>
    </div>
    <div>
        <label>Password</label><br/>
        <input type="password" name="password"/>
    </div>
    <button type="submit">Login</button>
</form>
