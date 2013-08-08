<g:hasErrors bean="${UFileInstance}">
    <ul class="text-error">
        <g:eachError bean="${UFileInstance}" var="error">
            <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>>
                <g:message error="${error}" /></li>
        </g:eachError>
    </ul>
</g:hasErrors>

<div class="control-group ${hasErrors(bean: UFileInstance, field: 'size', 'error')}">
    <label class="control-label" for="size">
        <g:message code="UFile.size.label" default="Size" />
    </label>
    <div class="controls">
        <g:field name="size" type="number" min="0" value="${UFileInstance.size}" required=""/>
    </div>
</div>

<div class="control-group ${hasErrors(bean: UFileInstance, field: 'path', 'error')}">
    <label class="control-label" for="path">
        <g:message code="UFile.path.label" default="Path" />
    </label>
    <div class="controls">
        <g:textField name="path" required="" value="${UFileInstance?.path}"/>
    </div>
</div>

<div class="control-group ${hasErrors(bean: UFileInstance, field: 'name', 'error')}">
    <label class="control-label" for="name">
        <g:message code="UFile.name.label" default="Name" />
    </label>
    <div class="controls">
        <g:textField name="name" required="" value="${UFileInstance?.name}"/>
    </div>
</div>

<div class="control-group ${hasErrors(bean: UFileInstance, field: 'dateUploaded', 'error')}">
    <label class="control-label" for="dateUploaded">
        <g:message code="UFile.dateUploaded.label" default="Date Uploaded" />
    </label>
    <div class="controls">
        <g:datePicker name="dateUploaded" precision="day"  value="${UFileInstance?.dateUploaded}"  />
    </div>
</div>

<div class="control-group ${hasErrors(bean: UFileInstance, field: 'downloads', 'error')}">
    <label class="control-label" for="downloads">
        <g:message code="UFile.downloads.label" default="Downloads" />
    </label>
    <div class="controls">
        <g:field name="downloads" type="number" value="${UFileInstance.downloads}" required=""/>
    </div>
</div>

<div class="control-group ${hasErrors(bean: UFileInstance, field: 'extension', 'error')}">
    <label class="control-label" for="extension">
        <g:message code="UFile.extension.label" default="Extension" />
    </label>
    <div class="controls">
        <g:textField name="extension" value="${UFileInstance?.extension}"/>
    </div>
</div>

