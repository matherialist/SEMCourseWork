<#assign page="store">
<#assign title="Управление ассортиментом товаров">
<#include "navigation.ftl">
<div class="internal-header">Ввод данных о товаре на складе</div>
<form action="/store" method="post" enctype="multipart/form-data">
    <input type="hidden" name="id" value="${item.id}">
    <#if message!="">
        <div class="alert alert-danger">${message}</div>
    </#if>
    <div class="form-group">
        <label for="title">Название товара</label>
        <input class="form-control" type="text" name="title" value="${item.title}">
    </div>
    <div class="form-group">
        <label for="description">Описание товара</label>
        <textarea class="form-control" rows="6" name="description">${item.description}</textarea>
    </div>
    <div class="form-group">
        <label for="price">Цена товара</label>
        <input class="form-control" type="text" name="price" value="${item.price}">
    </div>
    <div class="form-group">
        <label for="amount">Количество товара</label>
        <input class="form-control" type="text" name="amount" value="${item.amount}">
    </div>
    <div class="form-group">
        <label for="photo">Фотография</label>
        <div class="form-control">
            <img name="photo" id="photo" src="data:image/jpeg;base64,${item.photo}" width="128">
            <br/>
            <input type="file" name="photo">
        </div>
    </div>
    <input type="submit" class="btn internal-button" value="Сохранить изменения">
    </div>
</form>
<#include "footer.ftl">
