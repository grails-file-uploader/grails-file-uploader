<html>
	<head>
		<title>File Uploader plugin</title>
	</head>
	<body>
		<h3>File Uploader plugin</h3>
		<br /><br />
		<fileuploader:form 	upload="avatar" 
							successAction="success"
							successController="test"
							errorAction="error"
							errorController="test"/>
							
		<br /><br />
		<g:each var="f" in="${com.lucastex.grails.fileuploader.UFile.list()}">
		Nome: ${f.name} <br />
		Path: ${f.path} <br />
		Tamanho: ${f.size} <br />
		Extension: ${f.extension} <br />
		Data: ${f.dateUploaded} <br />
		Qt Downloads: ${f.downloads} <br />
		<fileuploader:download 	id="${f.id}"
								errorAction="error"
								errorController="test">download</fileuploader:download><br /><br /><br />
		</g:each>
	</body>
</html>
