<#assign page="users">
<#assign title="Управление пользователями">
<#include "navigation.ftl">
<div class="internal-header">Выбор пользователя</div>
<table class="table">
    <#list users as user>
        <tr>
            <td><a href="/users/${user.id}">${user.user}</a></td>
            <td><a href="/users/${user.id}">${user.email}</a></td>
            <td><a href="/users/${user.id}">${user.rolename}</a></td>
        </tr>
    </#list>
</table>
<#include "footer.ftl">