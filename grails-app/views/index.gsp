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
		<g:each in="${grailsApplication.domainClasses}" var="x">
			<g:each in="${x.properties}" var="property">
				${property} <br />
			</g:each>
		</g:each>		
	</body>
</html>
