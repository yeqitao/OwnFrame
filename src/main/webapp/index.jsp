<%@ page isELIgnored="false"%>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>
</head>
<body>
<h2>Hello World!</h2>

time: ${requestScope.time}
<br>
time: ${requestScope.get("time")}
<br>
time: <%=request.getAttribute("time")%>
<br><br>
time: ${time}


time: ${user.id}
time: ${user.name}

</body>
</html>
