<#assign page="users">
<#assign title="Управление пользователями">
<#include "navigation.ftl">
<div class="internal-header">Изменение пользователя</div>
<div class="row">
    <div class="col-6">

        <div class="internal-header">Выбор пользователя</div>
        <table class="table">
            <#list users as user>
                <tr class="<#if user.id==data.id>active-item</#if>">
                    <td><a href="/users/${user.id}">${user.user}</a></td>
                    <td><a href="/users/${user.id}">${user.email}</a></td>
                    <td><a href="/users/${user.id}">${user.rolename}</a></td>
                </tr>
            </#list>
        </table>
    </div>
    <div class="col-6 border-left">
        <form action="/users/${data.id}" method="post" enctype="multipart/form-data">
            <#if message!="">
                <div class="alert alert-danger">${message}</div>
            </#if>
            <div class="form-group">
                <label for="title">Имя пользователя</label>
                <input class="form-control" id="username" readonly name="username" rows="10" value="${data.user}">
            </div>
            <div class="form-group">
                <label for="email">Электронная почта</label>
                <input class="form-control" id="email" name="email" value="${data.email}">
            </div>
            <div class="form-group">
                <label for="role">Роль</label>
                <select class="form-control" name="role">
                    <option value="0">Неизвестно</option>
                    <#list roles as role>
                        <option value="${role.id}" <#if role.id==data.role>selected</#if>>${role.name}</option>
                    </#list>
                </select>
            </div>
            <div class="form-group">
                <label for="photo">Фотография</label>
                <#if photo!="">
                    <img src="${photo}" width="128"/>
                </#if>
                <input type="file" name="photo">
            </div>
            <input type="submit" class="btn internal-button" value="Сохранить изменения">
            <input type="hidden" name="id" value="${data.id}"/>
        </form>
    </div>
</div>
<#include "footer.ftl">
