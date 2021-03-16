<#assign page="districts">
<#assign title="Управление районами">
<#include "navigation.ftl">
<div class="internal-header">Выбор района</div>
    <table class="table">
    <#list districts as district>
        <tr>
            <td><a href="/districts/${district.id}">${district.title}</a></td>
            <td><a href="/districts/${district.id}/delete">Удалить</a></td>
        </tr>
    </#list>
    </table>
    <hr/>
    <form action="/districts" method="POST">
        <input type="hidden" name="id" value="${next}">
        <input type="hidden" name="title" value="Новый район">
        <input type="hidden" name="latitude_top" value="0.0">
        <input type="hidden" name="longitude_top" value="0.0">
        <input type="hidden" name="latitude-bot" value="0.0">
        <input type="hidden" name="longitude-bot" value="0.0">
        <input type="submit" class="btn internal-button" value="Зарегистрировать новый район">
    </form>

<#include "footer.ftl">