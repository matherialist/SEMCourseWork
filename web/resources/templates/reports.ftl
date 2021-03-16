<#assign page="reports">
<#assign title="Просмотр отчётов">
<#include "navigation.ftl">
<div class="container" xmlns="http://www.w3.org/1999/html">
    <div class="internal-header">Отчёты о продажах</div>
    <form action="/reports" method="POST">
        <div class="row">
            <div class="col-5">
                <div class="input-group" date-provide="datepicker">
                    <label for="from">От даты</label>
                    <input type="text" class="form-control datepicker" name="from" value="${from}">
                    <div class="input-group-addon">
                        <span class="glyphicon glyphicon-th"></span>
                    </div>
                </div>
            </div>
            <div class="col-5">
                <div class="input-group" date-provide="datepicker">
                    <label for="to">До даты</label>
                    <input type="text" class="form-control datepicker" name="to" value="${to}">
                    <div class="input-group-addon">
                        <span class="glyphicon glyphicon-th"></span>
                    </div>
                </div>
            </div>
        </div>
        <div class="row pt-4">
            <div class="col-3">
                <button class="btn internal-button" name="productReport" id="productReport">Отчёт по товарам</button>
            </div>
            <div class="col-3">
                <button class="btn internal-button" name="dailyReport" id="dailyReport">Отчёт по дням</button>
            </div>
        </div>
        <#if type=="daily">
            <div class="report-header">Ежедневный отчёт о продажах с ${from} по ${to}</div>
            <table class="table">
                <tr><th>Дата</th><th>Сумма</th></tr>
                <#list data as day>
                    <tr>
                        <td>${day.date}</td>
                        <td>${day.totalCost}</td>
                    </tr>
                </#list>
            </table>
        <#elseif type=="product">
            <div class="report-header">Отчёт по продажам товаров с ${from} по ${to}</div>
            <table class="table">
                <tr><th>Наименование</th><th>Количество</th><th>Стоимость</th></tr>
                <#list data as day>
                    <tr>
                        <td>${day.itemName}</td>
                        <td>${day.amount}</td>
                        <td>${day.totalCost}</td>
                    </tr>
                </#list>
            </table>
        </#if>
    </form>
</div>
<#include "footer.ftl">