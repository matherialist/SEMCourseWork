<#assign page="cars">
<#assign title="Управление машинами">
<#include "navigation.ftl">
<div class="internal-header">Выбор машины полиции</div>
    <table class="table">
    <#list cars as car>
        <tr>
            <td><a href="/cars/${car}">${car}</a></td>
            <td><a href="/cars/${car}/delete">Удалить</a></td>
        </tr>
    </#list>
    </table>
    <hr/>
    <form action="/cars" method="POST">
        <input type="hidden" name="id" value="${next}">
        <input type="hidden" name="car-telemetry" value="[]">
        <input type="submit" class="btn internal-button" value="Зарегистрировать новую машину">
    </form>

<#include "footer.ftl">