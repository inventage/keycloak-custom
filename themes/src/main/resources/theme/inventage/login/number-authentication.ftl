<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title" || section = "header">
        ${msg("numauth.title")}
    <#elseif section = "form">
        <#if message?has_content && message.type = "error">
            <#assign errorClass = "kc-form-group--error" >
        </#if>
        <div class="kc-grid-row">
            <div class="kc-grid-column-full"><p>${msg("numauth.add.number")}</p></div>
            <div class="kc-grid-column-full"><p>${msg("numauth.howto", numberToAdd, chosenNumber)}</p></div>
            <form id="kc-totp-login-form" class="${properties.kcFormClass!} kc-grid-column-two-thirds" action="${url.loginAction}" method="post">
                <div class="kc-form-group  ${errorClass!""} ">
                    <label for="number" class="kc-label">${msg("numauth.entered.number.label")}</label>
                    <input id="number" name="enteredNumber" type="text" class="kc-input kc-input--width-5" autocomplete="false"/>
                </div>
                <input class="kc-button" name="login" id="kc-login" type="submit" value="${msg("doSubmit")}"/>
            </form>
        </div>
        <#if client?? && client.baseUrl?has_content>
            <p><a id="backToApplication" href="${client.baseUrl}">${msg("backToApplication")?no_esc}</a></p>
        </#if>
    </#if>
</@layout.registrationLayout>

