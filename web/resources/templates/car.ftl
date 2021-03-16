<#assign page="cars">
<#assign title="Управление машинами">
<#include "navigation.ftl">
<div class="internal-header">Ввод данных о телеметрии машины ${id}</div>
<form action="/cars" method="post">
<div class="form-group">
    <label for="car-telemetry">Редактировании телеметрии</label>
    <#if message!="">
        <div class="alert alert-danger">${message}</div>
    </#if>
    <textarea class="form-control" id="car-telemetry" name="car-telemetry" rows="10">${data}</textarea>
    <input type="submit" class="btn internal-button" value="Сохранить изменения">
    <input type="hidden" name="id" value="${id}"/>
</div>

</form>
<#include "footer.ftl">
