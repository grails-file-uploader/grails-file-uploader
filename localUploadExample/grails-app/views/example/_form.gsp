<%@ page import="com.bowerstudios.fileManager.Example" %>

<div class="fieldcontain ${hasErrors(bean: exampleInstance, field: 'firstName', 'error')} ">
	<label for="firstName">
		<g:message code="example.firstName.label" default="First Name" />
		
	</label>
	<g:textField name="firstName" value="${exampleInstance?.firstName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: exampleInstance, field: 'lastName', 'error')} ">
	<label for="lastName">
		<g:message code="example.lastName.label" default="Last Name" />
		
	</label>
	<g:textField name="lastName" value="${exampleInstance?.lastName}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: exampleInstance, field: 'files', 'error')} ">
	<label for="files">
		<g:message code="example.files.label" default="Attachments" />
	</label>

	<span>
		<%-- I had a collision with my domain model's "files" attribute, so I used attachments here instead--%>
		<g:hiddenField name="fileParam" value="attachments"/>
	
		<g:each in="${exampleInstance?.files}" var="f">
			<localUpload:download fileId="${f.id}">${f.name}</localUpload:download>
		</g:each>
		
		<g:hiddenField name="bucket" value="docs" />
		<input type="file" name="attachments" multiple="multiple" />
	</span>
</div>
