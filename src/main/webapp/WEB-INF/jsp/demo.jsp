<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
    	time: ${requestScope.time}
		<br>
		time: ${requestScope.get("time")}
		<br>
		time: <%=request.getAttribute("time")%>
		<br><br>
		time: ${time}
 	</body>
</html>