<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        ${msg("livingPlaceTitle")}
    <#elseif section = "form">
        <#if formModel.options?has_content>
            <div>${msg("livingPlaceOptions")}</div>
            <ul class="form-model-options">
                <#list formModel.options as inputOption>
                    <li>${inputOption}</li>
                </#list>
            </ul>
        </#if>
        <form id="kc-location-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!} ${messagesPerField.printIfExists('location',properties.kcFormGroupErrorClass!)}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="location" class="${properties.kcLabelClass!}">${msg("livingPlaceLabel")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="location" class="${properties.kcInputClass!}" name="livingPlace" />
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("enterLivingPlace")}"/>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
