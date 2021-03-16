<#assign page="districts">
<#assign title="Управление районами">
<#include "navigation.ftl">
<div class="internal-header">Ввод данных о районе</div>
<div class="row">
    <div class="col-6">
        <table class="table">
            <#list districts as district>
                <tr class="<#if district.id==data.id>active-item</#if>">
                    <td><a href="/districts/${district.id}">${district.title}</a></td>
                    <td><a href="/districts/${district.id}/delete">Удалить</a></td>
                </tr>
            </#list>
        </table>
        <hr/>
        <form action="/districts" method="POST">
            <input type="hidden" name="id" value="${next}">
            <input type="hidden" name="title" value="Новый район">
            <input type="hidden" name="latitude_top" value="0.0">
            <input type="hidden" name="longitude_top" value="0.0">
            <input type="hidden" name="latitude-bot" value="0.0">
            <input type="hidden" name="longitude-bot" value="0.0">
            <input type="submit" class="btn internal-button" value="Зарегистрировать новый район">
        </form>
    </div>
    <div class="col-6 border-left">
        <form action="/districts" method="post">
            <#if message!="">
                <div class="alert alert-danger">${message}</div>
            </#if>
            <div class="form-group">
                <label for="title">Название района</label>
                <input class="form-control" id="title" name="title" rows="10" value="${data.title}">
            </div>
            <div class="form-group">
                <label for="latitude_top">Широта левой-верхней точки</label>
                <input class="form-control" id="latitude_top" name="latitude_top" value="${data.latitude_top}">
            </div>
            <div class="form-group">
                <label for="longitude_top">Долгота левой-верхней точки</label>
                <input class="form-control" id="longitude_top" name="longitude_top" value="${data.longitude_top}">
            </div>
            <div class="form-group">
                <label for="latitude_bot">Широта правой-нижней точки</label>
                <input class="form-control" id="latitude_bot" name="latitude_bot" value="${data.latitude_bot}">
            </div>
            <div class="form-group">
                <label for="longitude_bot">Долгота правой-нижней точки</label>
                <input class="form-control" id="longitude_bot" name="longitude_bot" value="${data.longitude_bot}">
            </div>

            <input type="submit" class="btn internal-button" value="Сохранить изменения" id="save_changes">
            <input type="hidden" name="id" value="${id}"/>
        </form>
        <div class="internal-header">Курьеры</div>
        <ul>
            <table class="table">
                <#list couriers as courier>
                    <tr>
                        <td>${courier.login}</td>
                        <td><a href="/districts/${id}/${courier.id}/delete">Удалить</a></td>
                    </tr>
                </#list>
            </table>
        </ul>
        <#if (all)?has_content>
            <form action="/districts/${id}/courier" method="post">
                <div class="row">
                    <input type="hidden" name="id" value="${id}"/>
                    <div class="col-6">
                        <select class="form-control" name="courier">
                            <#list all as user>
                                <option value="${user.id}">${user.login}</option>
                            </#list>
                        </select>
                    </div>
                    <div class="col-6">
                        <input type="submit" class="btn internal-button" value="Добавить курьера" id="add_courier">
                    </div>
                </div>
            </form>
        </#if>
    </div>
</div>
<#include "footer.ftl">
