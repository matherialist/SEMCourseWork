<#assign page="store">
<#assign title="Управление ассортиментом товаров">
<#include "navigation.ftl">
<div class="internal-header">Товары на складе</div>
    <table class="table">
    <tr><th>&nbsp;</th><th>Название</th><th>Цена поставки</th><th>Количество</th><th>&nbsp;</th></tr>
    <#list store as item>
        <tr>
            <td><img src="data:image/jpeg;base64,${item.photo}" width="32"></td>
            <td><a href="/store/${item.id}">${item.title}</a></td>
            <td>${item.price}</td>
            <td>${item.amount}</td>
            <td><a href="/store/${item.id}/delete">Удалить</a></td>
        </tr>
    </#list>
    </table>
    <hr/>
    <form action="/store" method="POST" enctype="multipart/form-data">
        <input type="hidden" name="id" value="${next}">
        <input type="hidden" name="title" value="Новый товар">
        <input type="hidden" name="price" value="1">
        <input type="hidden" name="amount" value="1">
        <input type="submit" class="btn internal-button" value="Зарегистрировать новый товар на складе">
    </form>
<#include "footer.ftl">