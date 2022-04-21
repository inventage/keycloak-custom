<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("sms.title")}
    <#elseif section = "header">
        ${msg("sms.title")}
    <#elseif section = "form">
        <#if message?has_content && message.type = "error">
            <#assign errorClass = "kc-form-group--error" >
        </#if>
        <div class="kc-grid-row">
            <form id="kc-sms-login-form" class="${properties.kcFormClass!} kc-grid-column-two-thirds" action="${url.loginAction}" method="post">
                <div class="kc-form-group ${errorClass!""}">
                    <label for="phone-number" class="kc-label">${msg("sms.phoneNumber")}</label>
                    <input type="text" id="phone-number" class="kc-input" name="phone-number" autocomplete="false" />
                    <input class="kc-button" type="submit" value="${msg("doSubmit")}"/>
                </div>
            </form>
        </div>
    </#if>
</@layout.registrationLayout>