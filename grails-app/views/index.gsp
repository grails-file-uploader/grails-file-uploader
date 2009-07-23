<html>
	<head>
		<title>File Uploader plugin</title>
	</head>
	<body>
		<h3>File Uploader plugin</h3>
		<br /><br />
		<fileuploader:form 	upload="avatar" 
							successAction="successAction"
							successController="successController"
							errorAction="errorAction"
							errorController="errorController"/>
							
		<br /><br />
		<fileuploader:download 	id="1"
								errorAction="errorAction"
								errorController="errorController">download</fileuploader:download>
	</body>
</html>
