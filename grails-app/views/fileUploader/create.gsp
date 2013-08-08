<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
    <title><g:message code="default.create.label" args="[entityName]" /></title>
</head>
<body>
    <div class="page-header">
        <h1>
            <g:message code="default.create.label" args="[entityName]" />
        </h1>
    </div>
    <g:form action="save"  class="form-horizontal">
        <fieldset>
            <g:render template="form" />
            <div class="form-actions">
                <g:submitButton name="create" class="btn btn-primary"
                    value="${message(code: 'default.button.create.label')}" />
            </div>
        </fieldset>
    </g:form>
</body>
</html>