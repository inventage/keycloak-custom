<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username') displayInfo=(realm.password && realm.registrationAllowed && !registrationDisabled??); section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
        <div id="kc-form">
            <form id="webauth" action="${url.loginAction}" method="post">
                <input type="hidden" id="clientDataJSON" name="clientDataJSON"/>
                <input type="hidden" id="authenticatorData" name="authenticatorData"/>
                <input type="hidden" id="signature" name="signature"/>
                <input type="hidden" id="credentialId" name="credentialId"/>
                <input type="hidden" id="userHandle" name="userHandle"/>
                <input type="hidden" id="error" name="error"/>
            </form>
            <div id="kc-form-wrapper">
                <#if realm.password>
                    <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}"
                          method="post">
                        <#if !usernameHidden??>
                            <div class="${properties.kcFormGroupClass!}">
                                <label for="username"
                                       class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if></label>

                                <input tabindex="1" id="username"
                                       aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"
                                       class="${properties.kcInputClass!}" name="username"
                                       value=""
                                       type="text" autofocus autocomplete="off"/>

                                <#if messagesPerField.existsError('username')>
                                    <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}"
                                          aria-live="polite">
                                        ${kcSanitize(messagesPerField.get('username'))?no_esc}
                                    </span>
                                </#if>
                            </div>
                        </#if>

                        <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                            <div id="kc-form-options">
                                <#if realm.rememberMe && !usernameHidden??>
                                    <div class="checkbox">
                                        <label>
                                            <#if login.rememberMe??>
                                                <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"
                                                       checked> ${msg("rememberMe")}
                                            <#else>
                                                <input tabindex="3" id="rememberMe" name="rememberMe"
                                                       type="checkbox"> ${msg("rememberMe")}
                                            </#if>
                                        </label>
                                    </div>
                                </#if>
                            </div>
                        </div>

                        <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                            <input tabindex="4"
                                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                   name="login" id="kc-login" type="submit" value="Continue"/>
                        </div>
                    </form>
                    <div id="showPasskeyOption">
                        <div style="border-bottom: 1px solid;  text-align: center;  height: 10px; margin-bottom: 10px; margin-top: 10px;">
                            <span style="background: #fff; padding: 0 5px;">Other Sign in Options</span>
                        </div>
                        <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                            <input tabindex="4"
                                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                   name="login" id="kc-login" type="submit" value="Sign in with Passkey"
                                   onclick="webAuthnAuthenticate()"/>
                        </div>
                    </div>
                </#if>
            </div>
        </div>

    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration">
                <span>${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a></span>
            </div>
        </#if>
    <#elseif section = "socialProviders" >
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="${properties.kcFormSocialAccountSectionClass!}">
                <hr/>
                <h4>${msg("identity-provider-login-label")}</h4>

                <ul class="${properties.kcFormSocialAccountListClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountListGridClass!}</#if>">
                    <#list social.providers as p>
                        <a id="social-${p.alias}"
                           class="${properties.kcFormSocialAccountListButtonClass!} <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>"
                           type="button" href="${p.loginUrl}">
                            <#if p.iconClasses?has_content>
                                <i class="${properties.kcCommonLogoIdP!} ${p.iconClasses!}" aria-hidden="true"></i>
                                <span class="${properties.kcFormSocialAccountNameClass!} kc-social-icon-text">${p.displayName!}</span>
                            <#else>
                                <span class="${properties.kcFormSocialAccountNameClass!}">${p.displayName!}</span>
                            </#if>
                        </a>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>
    <script type="text/javascript" src="${url.resourcesCommonPath}/node_modules/jquery/dist/jquery.min.js"></script>
    <script type="text/javascript" src="${url.resourcesPath}/js/base64url.js"></script>
    <script type="text/javascript">
        if (!window.PublicKeyCredential) {
            if (document.getElementById("showPasskeyOption") != null) {
                document.getElementById("showPasskeyOption").style.display = "none";
                console.log("Webauthn not supported")
            }
        }

        function webAuthnAuthenticate() {
            let isUserIdentified = ${isUserIdentified};
            if (!isUserIdentified) {
                doAuthenticate([]);
                return;
            }
            checkAllowCredentials();
        }

        function checkAllowCredentials() {
            let allowCredentials = [];
            let authn_use = document.forms['authn_select'].authn_use_chk;

            if (authn_use !== undefined) {
                if (authn_use.length === undefined) {
                    allowCredentials.push({
                        id: base64url.decode(authn_use.value, {loose: true}),
                        type: 'public-key',
                    });
                } else {
                    for (let i = 0; i < authn_use.length; i++) {
                        allowCredentials.push({
                            id: base64url.decode(authn_use[i].value, {loose: true}),
                            type: 'public-key',
                        });
                    }
                }
            }
            doAuthenticate(allowCredentials);
        }


        function doAuthenticate(allowCredentials) {

            // Check if WebAuthn is supported by this browser
            if (!window.PublicKeyCredential) {
                $("#error").val("${msg("webauthn-unsupported-browser-text")?no_esc}");
                $("#webauth").submit();
                return;
            }

            let challenge = "${challenge}";
            let userVerification = "${userVerification}";
            let rpId = "${rpId}";
            let publicKey = {
                rpId: rpId,
                challenge: base64url.decode(challenge, {loose: true})
            };

            let createTimeout = ${createTimeout};
            if (createTimeout !== 0) publicKey.timeout = createTimeout * 1000;

            if (allowCredentials.length) {
                publicKey.allowCredentials = allowCredentials;
            }

            if (userVerification !== 'not specified') publicKey.userVerification = userVerification;

            navigator.credentials.get({publicKey})
                .then((result) => {
                    window.result = result;

                    let clientDataJSON = result.response.clientDataJSON;
                    let authenticatorData = result.response.authenticatorData;
                    let signature = result.response.signature;

                    $("#clientDataJSON").val(base64url.encode(new Uint8Array(clientDataJSON), {pad: false}));
                    $("#authenticatorData").val(base64url.encode(new Uint8Array(authenticatorData), {pad: false}));
                    $("#signature").val(base64url.encode(new Uint8Array(signature), {pad: false}));
                    $("#credentialId").val(result.id);
                    if (result.response.userHandle) {
                        $("#userHandle").val(base64url.encode(new Uint8Array(result.response.userHandle), {pad: false}));
                    }
                    $("#webauth").submit();
                })
                .catch((err) => {
                    $("#error").val(err);
                    $("#webauth").submit();
                })
            ;
        }
    </script>
</@layout.registrationLayout>