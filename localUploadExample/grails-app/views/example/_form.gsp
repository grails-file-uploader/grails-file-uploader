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

