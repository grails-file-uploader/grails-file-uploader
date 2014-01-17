<%@ page import="com.bowerstudios.fileManager.Example" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'example.label', default: 'Example')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
		
<%--		<r:require modules="bootstrap-file-upload"/>--%>
	</head>
	<body>
		<a href="#show-example" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-example" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list example">
			
				<g:if test="${exampleInstance?.firstName}">
				<li class="fieldcontain">
					<span id="firstName-label" class="property-label"><g:message code="example.firstName.label" default="First Name" /></span>
					
						<span class="property-value" aria-labelledby="firstName-label"><g:fieldValue bean="${exampleInstance}" field="firstName"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${exampleInstance?.lastName}">
				<li class="fieldcontain">
					<span id="lastName-label" class="property-label"><g:message code="example.lastName.label" default="Last Name" /></span>
					
						<span class="property-value" aria-labelledby="lastName-label"><g:fieldValue bean="${exampleInstance}" field="lastName"/></span>
					
				</li>
				</g:if>
				
				
				
				
				<g:if test="${exampleInstance?.files}">
				<li class="fieldcontain">
					<span id="files-label" class="property-label">
					Displaying the file download links using gsp:</span>
					
					<g:each in="${exampleInstance.files}" var="f">
						<span class="property-value" aria-labelledby="files-label">
							<localUpload:download fileId="${f.id}" target="_blank" >${f.name}</localUpload:download> - <localUpload:prettysize size="${f.sizeInBytes }"/>
						</span>
						</g:each>
				</li>
				</g:if>
				
				
				
				
				<li class="fieldcontain">
					<span id="files-form-label" class="property-label">
					Displaying the file upload form using gsp which redirects back
					to the show action of this controller after it successfully
					uploads the file (allows only single file submission)</span>
					
					<span class="property-value" aria-labelledby="files-form-label">
						<localUpload:form bucket="docs" saveAssoc="example"
							id="${exampleInstance.id}"/>
					</span>
				</li>
				
				
				
				
				<li class="fieldcontain">
					<span id="files-form-multiple-label" class="property-label">
					Displaying the file upload form using gsp which redirects back
					to the show action of this controller after it successfully
					uploads the file (allows multiple file submission)</span>
					
					<span class="property-value" aria-labelledby="files-form-multiple-label">
						<localUpload:form bucket="docs" saveAssoc="example"
							multiple="true" id="${exampleInstance.id}"/>
					</span>
				</li>
				
				
				
				
<%--				<li class="fieldcontain">--%>
<%--					<span id="files-ajax-form-label" class="property-label">--%>
<%--					Displaying the file upload form using the ajax</span>--%>
<%--					--%>
<%--					<span class="property-value" aria-labelledby="files-ajax-form-label">--%>
<%--						<bsfu:fileUpload action="ajaxUpload" controller="localUpload" --%>
<%--							formData="${[id:exampleInstance.id, saveAssoc:'example'] }"/>--%>
<%--					</span>--%>
<%--				</li>--%>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${exampleInstance?.id}" />
					<g:link class="edit" action="edit" id="${exampleInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
