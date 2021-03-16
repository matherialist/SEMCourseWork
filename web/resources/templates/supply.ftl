<#assign page="supply">
<#assign title="Поставки товара">
<#include "navigation.ftl">
<div class="internal-header">Ввод данных о договоре поставки</div>
<div class="row">
    <div class="col-6">
        <table class="table">
            <tr>
                <th>Открыт</th>
                <th>Закрыт</th>
                <th>Общая сумма</th>
                <th>Состояние</th>
                <th>&nbsp;</th>
            </tr>
            <#list contracts as cnt>
                <tr class="<#if cnt.id==contract.id>active-item</#if>">
                    <td><a href="/supply/${cnt.id}">${cnt.openDate}</a></td>
                    <td><a href="/supply/${cnt.id}">${cnt.closeDate}</a></td>
                    <td><a href="/supply/${cnt.id}">${cnt.totalCost}</a></td>
                    <td>
                        <a href="/supply/${cnt.id}">
                            <#if cnt.status==0>
                                Черновик
                            <#elseif cnt.status==1>
                                Отправлен поставщику
                            <#elseif cnt.status==2>
                                Выполнен
                            <#elseif cnt.status==3>
                                Отклонён
                            </#if></a>
                    </td>
                    <td>
                        <#if cnt.status==0>
                        <a href="/supply/${cnt.id}/delete">Удалить</a>
                        </#if>
                    </td>
                </tr>
            </#list>
        </table>
        <hr/>
        <form action="/supply" method="POST">
            <input type="hidden" name="id" value="${next}">
            <!--проверка роли-->
            <input type="submit" class="btn internal-button" value="Создать новый заказ на поставку">
        </form>

    </div>
    <div class="col-6 border-left">
        <form action="/supply/${contract.id}" method="post">
            <#if message!="">
                <div class="alert alert-danger">${message}</div>
            </#if>
            <!-- для продавца -->
            <#if permissions.storeSeller && contract.status==0>
                <input type="submit" name="create" class="btn internal-button" value="Создать заказ">
            </#if>
            <!-- для поставщика -->
            <#if permissions.supplies && contract.status==1>
                <input type="submit" name="accept" class="btn internal-button" value="Выполнить заказ">
                <input type="submit" name="reject" class="btn internal-button" value="Отклонить заказ">
            </#if>
            <input type="hidden" name="id" value="${contract.id}"/>
        </form>
        <div class="internal-header">Товары</div>
        <ul>
            <table class="table">
                <tr>
                    <th>Название</th>
                    <th>Количество</th>
                    <th>Цена</th>
                    <th>&nbsp;</th>
                </tr>
                <#list items as item>
                    <tr>
                        <td>${item.itemName}</td>
                        <td>${item.amount}</td>
                        <td>${item.price}</td>
                        <td>
                            <#if permissions.storeSeller && contract.status==0>
                            <a href="/supply/${contract.id}/${item.itemId}/delete">Удалить</a>
                            </#if>
                        </td>
                    </tr>
                </#list>
            </table>
        </ul>
        <hr/>
        <#if permissions.storeSeller && contract.status==0>

            <form action="/supply/${contract.id}/item" method="post">
                <div class="row">
                    <input type="hidden" name="id" value="${contract.id}"/>
                    <div class="col-6">
                        <div class="form-group">
                            <label for="item">Выберите товар</label>
                            <select class="form-control" name="item">
                                <#list allitems as item>
                                    <option value="${item.id}">${item.title}</option>
                                </#list>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="amount">Количество</label>
                            <input type="number" name="amount" class="form-control">
                        </div>
                        <div class="form-group">
                            <label for="price">Цена за единицу товара</label>
                            <input type="number" name="price" class="form-control">
                        </div>
                        <div class="col-6">
                            <input type="submit" class="btn internal-button" value="Добавить товар">
                        </div>
                    </div>
                </div>
            </form>
        </#if>
    </div>
</div>
<#include "footer.ftl">
