<#include "header.ftl">
<body style="height: 100%">
<div id="content" style="display:grid; width: 100%; height: 1vh; place-content: center; justify-items: center">
    <div style="text-align:center"><img src="/static/logo.png" width="128"></div>
    <h1 style>holeaf</h1>
    <form method="POST" action="#">
        <#if message!="">
            <div class="alert-danger p-3 mb-3 text-center">${message}</div>
        </#if>
        <div class="form-group">
            <label for="username">Имя для входа</label>
            <input type="text" id="username" class="form-control" name="username"/>
        </div>
        <div class="form-group">
            <label for="password">Пароль</label>
            <input type="password" id="password" class="form-control" name="password"/>
        </div>
        <button type="submit" class="btn btn-primary header-background" style="width: 100%">Вход в систему</button>
    </form>
</div>
</body>