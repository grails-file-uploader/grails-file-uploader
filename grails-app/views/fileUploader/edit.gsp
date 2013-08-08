<html>
<head>
<meta name="layout" content="main">
<g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
    <div class="page-header">
        <h1>
            <g:message code="default.edit.label" args="[entityName]" />
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
    <g:form class="form-horizontal" >
        <g:hiddenField name="id" value="${UFileInstance.id}" />
        <g:hiddenField name="version" value="${UFileInstance.version}" />
        <fieldset class="form">
            <g:render template="form" />
            <div class="form-actions">
                <g:actionSubmit class="btn btn-primary" action="update"
                    value="${message(code: 'default.button.update.label')}" />
                <g:actionSubmit class="btn btn-danger" action="delete"
                    value="${message(code: 'default.button.delete.label')}"
                    onclick="return confirm('${message(code: 'default.button.delete.confirm.message')}');" />
            </div>
        </fieldset>
    </g:form>
</body>
</html>