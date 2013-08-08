<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
</head>
<body>
    <div class="page-header">
        <h1>
            <g:message code="default.list.label" args="[entityName]" />
            <span class="pull-right">
                <g:form action="list" name="search" class="pull-right">
                    <div class="input-append">
                        <g:textField name="query" value="${params.query}" autofocus="" placeholder="Search" />
                        <div class="btn-group">
                            <button type="submit" class="btn" ><i class="icon-search"></i></button>
                            <button class="btn dropdown-toggle" data-toggle="dropdown">
                                <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu">
                                <li><g:link action="create"><i class="icon-plus"></i> Create</g:link></li>
                            </ul>
                        </div>
                    </div>
                </g:form>
            </span>
        </h1>
    </div>
    <table class="table table-bordered table-hover table-striped">
        <thead>
            <tr>
            
                <g:sortableColumn property="size" title="${message(code: 'UFile.size.label', default: 'Size')}" />
					
                <g:sortableColumn property="path" title="${message(code: 'UFile.path.label', default: 'Path')}" />
					
                <g:sortableColumn property="name" title="${message(code: 'UFile.name.label', default: 'Name')}" />
					
                <g:sortableColumn property="dateUploaded" title="${message(code: 'UFile.dateUploaded.label', default: 'Date Uploaded')}" />
					
                <g:sortableColumn property="downloads" title="${message(code: 'UFile.downloads.label', default: 'Downloads')}" />
					
                <g:sortableColumn property="extension" title="${message(code: 'UFile.extension.label', default: 'Extension')}" />
					
            </tr>
        </thead>
        <tbody>
            <g:each in="${UFileInstanceList}" var="UFileInstance">
                <tr>
                
                    <td><g:link action="show" id="${UFileInstance.id}">${fieldValue(bean: UFileInstance, field: "size")}</g:link></td>
					
                    <td>${fieldValue(bean: UFileInstance, field: "path")}</td>
					
                    <td>${fieldValue(bean: UFileInstance, field: "name")}</td>
					
                    <td><g:formatDate date="${UFileInstance.dateUploaded}" /></td>
					
                    <td>${fieldValue(bean: UFileInstance, field: "downloads")}</td>
					
                    <td>${fieldValue(bean: UFileInstance, field: "extension")}</td>
					
                </tr>
            </g:each>
            <g:if test="${!UFileInstanceList }">
                <tr>
                    <td>
                        No records found. <g:link action="create">Create new</g:link>.
                    </td>
                </tr>
            </g:if>
        </tbody>
    </table>
    <div class="pagination">
        <g:paginate total="${UFileInstanceTotal}" />
    </div>
</body>
</html>