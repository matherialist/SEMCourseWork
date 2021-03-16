<#assign page="keys">
<#assign title="Управление ключами шифрования">
<#include "navigation.ftl">
<div class="container" xmlns="http://www.w3.org/1999/html">
    <div class="internal-header">Ввод логина пользователя</div>
    <form action="keys" method="POST">
        <div class="input-group">
            <input type="text" class="form-control" name="username">
            <div class="input-group-append">
                <input type="submit" class="btn internal-button" value="Запросить ключи шифрования">
            </div>
        </div>
    </form>
    <#if found == 1>
        <div class="internal-header">Пользователь обнаружен</div>
        <div class="input-group">
            <input type="text" class="form-control" readonly value="${key}" id="answer">
        </div>
    <#else>
        <#if key!="">
            <div class="internal-header-error">Пользователь не обнаружен</div>
        </#if>
    </#if>
</div>
<#include "footer.ftl">