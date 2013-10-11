<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'UFile.label', default: 'UFile')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
</head>
<body>
    <div class="page-header">
        <h1 class="inline">
            <g:message code="default.list.label" args="[entityName]" />
        </h1>
        <div class="pull-right col-lg-4">
            <g:form action="list" name="search">
                <div class="input-group">
                    <g:textField name="query" value="${params.query}" autofocus="" class="form-control"
                        placeholder="Search" />
                    <div class="input-group-btn">
                        <button type="submit" class="btn btn-default" ><i class="icon-search"></i></button>
                    </div>
                </div>
            </g:form>
        </div>
    </div>
    <table class="table table-bordered table-hover table-striped">
        <thead>
            <tr>
                <th><g:checkBox name="ufile" class="check-uncheck" data-checkbox-name="ufileId" /></th>
                <g:sortableColumn property="dateUploaded" title="${message(code: 'UFile.dateUploaded.label', default: 'Date Uploaded')}"
                    width="170px" />
                <g:sortableColumn property="size" title="${message(code: 'UFile.size.label', default: 'Size')}" />
                <g:sortableColumn property="path" title="${message(code: 'UFile.path.label', default: 'Path')}" />
                <g:sortableColumn property="downloads" title="${message(code: 'UFile.downloads.label', default: 'Downloads')}" />
                <g:sortableColumn property="type" title="${message(code: 'UFile.type.label', default: 'Type')}" />
            </tr>
        </thead>
        <tbody>
            <g:each in="${UFileInstanceList}" var="ufileInstance">
                <tr>
                    <td>
                        <g:if test="${ufileInstance.canMoveToCDN() && ufileInstance.fileExists }">
                            <g:checkBox name="ufileId" value="${ufileInstance.id }" checked="false" />
                        </g:if>
                    </td>
                    <td><g:formatDate date="${ufileInstance.dateUploaded}" format="MM/dd/yyyy hh:mm a" /></td>
                    <td>${fieldValue(bean: ufileInstance, field: "size")}</td>
                    <td>
                        <a href="${fileuploader.resolvePath(instance: ufileInstance) }" rel="tooltip" title="${ufileInstance.path }"
                            data-container="body">${ufileInstance.name}</a>
                        <g:if test="${!ufileInstance.fileExists && ufileInstance.canMoveToCDN() }">
                            <i class="icon-exclamation-sign pull-right text-danger" title="File missing"></i>
                        </g:if>
                    </td>
                    <td>${fieldValue(bean: ufileInstance, field: "downloads")}</td>
                    <td style="text-transform: capitalize;">
                        ${fieldValue(bean: ufileInstance, field: "type").toLowerCase().replaceAll('_', ' ')}
                    </td>
                </tr>
            </g:each>
            <g:if test="${!UFileInstanceList }">
                <tr>
                    <td colspan="6">
                        No records found. <g:link action="create">Create new</g:link>.
                    </td>
                </tr>
            </g:if>
        </tbody>
    </table>
    <div class="row">
        <div class="col-sm-10">
            <ul class="pagination" style="margin: 0">
                <g:paginate total="${UFileInstanceTotal}" />
            </ul>
        </div>
        <div class="col-sm-2">
            <a href="" id="move-tocdn-link" class="btn btn-primary pull-right">
                <i class="icon-cloud-upload"></i> &nbsp;Move To Cloud</a>
        </div>
    </div>
    <r:script>
        $('input[name=ufileId],input[name=ufile]').change(function() {
            selectedCount = $('input[name=ufileId]:checked').length;
            $("a#move-tocdn-link").disable(selectedCount == 0);
        });
        var selectedCount = $('input[name=ufileId]:checked').length;
        $("a#move-tocdn-link").click(function() {
            try {
                $(this).disable(true);
                blockPage(true);
            } catch(e) {}
            $.ajax({
                method: "POST",
                data: $("[name=ufileId]:checked").serialize(),
                url: "/fileUploader/moveToCloud",
                success: function() {
                    window.location.reload();
                }
            })
            return false;
        }).disable(selectedCount == 0)
    </r:script>
</body>
</html>