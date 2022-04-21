<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("numauth.title")}
    <#elseif section = "header">
        ${msg("numauth.title")}
    <#elseif section = "form">
        <#if message?has_content && message.type = "error">
            <#assign errorClass = "kc-form-group--error" >
        </#if>
        <div class="kc-grid-row">
            <div class="kc-grid-column-full">
                <p>${msg("numauth.chooseNumber")}</p>
            </div>
            <form id="kc-totp-login-form" class="${properties.kcFormClass!} kc-grid-column-two-thirds" action="${url.loginAction}" method="post">
                <div class="kc-form-group ${errorClass!""}">
                    <label for="number" class="kc-label">${msg("numauth.yournumber")}</label>
                    <input type="text" id="number" class="kc-input" name="number" autocomplete="false" />
                </div>

                <div class="kc-form-group">
                    <input class="kc-button" type="submit" value="${msg("doSubmit")}"/>
                </div>
            </form>
        </div>
    </#if>
</@layout.registrationLayout>