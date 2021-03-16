<#assign page="supply">
<#assign title="Поставки товара">
<#include "navigation.ftl">
<div class="internal-header">Выбор договора поставки</div>
    <table class="table">
    <tr><th>Открыт</th><th>Закрыт</th><th>Общая сумма</th><th>Состояние</th><th>&nbsp;</th></tr>
    <#list contracts as contract>
        <#if (permissions.storeSeller) || (permissions.supplies && (contract.status==1 || contract.status==2))>
        <tr>
            <td><a href="/supply/${contract.id}">${contract.openDate}</a></td>
            <td><a href="/supply/${contract.id}">${contract.closeDate}</a></td>
            <td><a href="/supply/${contract.id}">${contract.totalCost}</a></td>
            <td>
                <a href="/supply/${contract.id}">
                    <#if contract.status==0>
                        Черновик
                    <#elseif contract.status==1>
                        Отправлен поставщику
                    <#elseif  contract.status==2>
                        Выполнен
                    <#elseif contract.status==3>
                        Отклонён
                    </#if></a>
            </td>
            <td>
                <#if contract.status==0>
                <a href="/supply/${contract.id}/delete">Удалить</a></td>
            </#if>
        </tr>
        </#if>
    </#list>
    </table>
    <hr/>
    <#if permissions.storeSeller>

    <form action="/supply" method="POST">
        <input type="hidden" name="id" value="${next}">
        <!--проверка роли-->
        <input type="submit" class="btn internal-button" value="Создать новый заказ на поставку">
    </form>
    </#if>

<#include "footer.ftl">