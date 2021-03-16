<#include "header.ftl">
<body>
<div class="wrapper">
    <!-- Sidebar  -->
    <nav id="sidebar">
        <div class="sidebar-header">
            <h3><img src="/static/logo.png" width="32">holeaf</h3>
        </div>

        <ul class="list-unstyled components">
            <#if page=="profile">
            <li class="active"><#else>
            <li></#if>
                <a href="/profile">О пользователе</a>
            </li>
            <#if permissions.storeSeller>
                <#if page=="districts">
                    <li class="active"><#else>
                    <li></#if>
                <a href="/districts">Управление районами</a>
                </li>
            </#if>
            <#if permissions.telemetry>
                <#if page=="cars">
                    <li class="active"><#else><li></#if>
                <a href="/cars">Управление машинами полиции</a>
                </li>
            </#if>
            <#if permissions.keys>
                <#if page=="keys">
                    <li class="active"><#else>
                    <li></#if>
                <a href="/keys">Управление ключами шифрования</a>
                </li>
            </#if>
            <#if permissions.stats>
                <#if page=="reports">
                    <li class="active"><#else>
                    <li></#if>
                <a href="/reports" id="reports">Отчёты</a>
                </li>
            </#if>
            <#if permissions.supplies>
                <#if page=="supply">
                    <li class="active"><#else>
                    <li></#if>
                <a href="/supply">Поставки товара</a>
                </li>
            </#if>
            <#if permissions.storeSeller>
                <#if page=="store">
                    <li class="active"><#else>
                    <li></#if>
                <a href="/store">Товары</a>
                </li>
            </#if>
            <#if permissions.users>
            <#if page=="users">
            <li class="active"><#else>
            <li></#if>
                <a href="/users">Пользователи</a>
                </#if>
        </ul>
    </nav>

    <!-- Page Content  -->
    <div id="content">

        <nav class="navbar navbar-expand-lg navbar-light header-background">
            <button type="button" id="sidebarCollapse" class="navbar-btn">
                <span></span>
                <span></span>
                <span></span>
            </button>
            <div class="container-fluid">
                <div class="navbar-text header-text">
                    ${title}
                </div>
            </div>
        </nav>