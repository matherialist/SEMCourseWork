<#assign page="profile">
<#assign title="О пользователе">
<#include "navigation.ftl">
    <table class="table">
        <tr>
            <td>Имя пользователя</td>
            <td>${profile.user}</td>
        </tr>
        <tr>
            <td>Электронная почта</td>
            <td>${profile.email}</td>
        </tr>
        <tr>
            <td>Роль</td>
            <td>${rolename}</td>
        </tr>
        <tr>
            <td>Фотография</td>
            <td>
         <#if photo!="">
             <img src="${photo}" width="128"/>
            </#if>
            <form enctype="multipart/form-data" action="/profile" method="post">
                <input type="hidden" name="id" value="${profile.id}">
                <input type="file" name="photo">
                <input type="submit" value="Заменить">
            </form>
            </td>
        </tr>
    </table>
    <a href="/logout"><button type="button" class="btn btn-danger">Выход из системы</button>
<#include "footer.ftl">