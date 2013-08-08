
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
</head>
<body>
    <div class="page-header">
        <h1>
            <g:message code="default.show.label" args="[entityName]" />
            <span class="pull-right">
                <div class="btn-group">
                    <g:link action="list"><i class="icon-th-list"></i></g:link>
                    <button class="btn dropdown-toggle" data-toggle="dropdown">
                        <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu">
                        <li><g:link action="create"><i class="icon-plus"></i> Create</g:link></li>
                    </ul>
                </div>
            </span>
        </h1>
    </div>
    <dl class="dl-horizontal">
        
        <g:if test="${UFileInstance?.size}">
            <dt>
				<g:message code="UFile.size.label" default="Size" />
            </dt>
            <dd>
				
					<g:fieldValue bean="${UFileInstance}" field="size"/>
				
            </dd>
        </g:if>
		
        <g:if test="${UFileInstance?.path}">
            <dt>
				<g:message code="UFile.path.label" default="Path" />
            </dt>
            <dd>
				
					<g:fieldValue bean="${UFileInstance}" field="path"/>
				
            </dd>
        </g:if>
		
        <g:if test="${UFileInstance?.name}">
            <dt>
				<g:message code="UFile.name.label" default="Name" />
            </dt>
            <dd>
				
					<g:fieldValue bean="${UFileInstance}" field="name"/>
				
            </dd>
        </g:if>
		
        <g:if test="${UFileInstance?.dateUploaded}">
            <dt>
				<g:message code="UFile.dateUploaded.label" default="Date Uploaded" />
            </dt>
            <dd>
				
					<g:formatDate date="${UFileInstance?.dateUploaded}" />
				
            </dd>
        </g:if>
		
        <g:if test="${UFileInstance?.downloads}">
            <dt>
				<g:message code="UFile.downloads.label" default="Downloads" />
            </dt>
            <dd>
				
					<g:fieldValue bean="${UFileInstance}" field="downloads"/>
				
            </dd>
        </g:if>
		
        <g:if test="${UFileInstance?.extension}">
            <dt>
				<g:message code="UFile.extension.label" default="Extension" />
            </dt>
            <dd>
				
					<g:fieldValue bean="${UFileInstance}" field="extension"/>
				
            </dd>
        </g:if>
		
    </dl>
    <g:form>
        <fieldset class="form-actions">
            <g:hiddenField name="id" value="${UFileInstance?.id}" />
            <g:link class="btn btn-primary" action="edit" id="${UFileInstance?.id}">
                <g:message code="default.button.edit.label" default="Edit" />
            </g:link>
            <g:actionSubmit class="btn btn-danger" action="delete"
                value="${message(code: 'default.button.delete.label')}"
                onclick="return confirm('${message(code: 'default.button.delete.confirm.message')}');" />
        </fieldset>
    </g:form>
</body>
</html>