/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inventage.keycloak.registration;

import com.google.auto.service.AutoService;
import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationUserCreation;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.ValidationException;

import java.util.List;


/**
 * This class extends the built-in RegistrationUserCreation class. We modified the following functionalities:
 * (1): We do not create a user account immediately after the user fills out the form correctly. We want to delay the creation
 * as long as possible, because it is possible that a user cancels the registration just before setting up a password or passkey.
 * <p>
 * (2): We added a check to make sure that an email has not already been used for another account.This check would normally be done
 * in the built-in RegistrationProfile class. The reason we don't just add the built-in RegistrationProfile class to the registration
 * flow is that it presumes that the user account is already set/created.
 * <p>
 * IMPORTANT: This FormAction should not be used in combination with other built-in FormActions ("or Authenticators") as most of them
 * presume that the user account is already set/created.
 **/
@AutoService(org.keycloak.authentication.FormActionFactory.class)
public class RegistrationUserCreationNoAccount extends RegistrationUserCreation {

    public static final String PROVIDER_ID = "user-creation-no-account";

    @Override
    public String getHelpText() {
        return "Passkey Tutorial: This action is not allowed to contain other built-in actions in this registration form, as other built-in action require that the user account is already set/created. This form does not create a user account!";
    }

    //Copied from org.keycloak.authentication.forms.RegistrationProfile as the validation check of RegistrationUserCreation does not check for invalid emails.
    @Override
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        UserProfileProvider profileProvider = context.getSession().getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.REGISTRATION_PROFILE, formData);

        //We check if the email address is already in use. If yes, we return an error that is displayed to the user.
        try {
            profile.validate();
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());

            if (pve.hasError(Messages.EMAIL_EXISTS, Messages.INVALID_EMAIL)) {
                context.getEvent().detail(Details.EMAIL, profile.getAttributes().getFirstValue(UserModel.EMAIL));
            }

            if (pve.hasError(Messages.EMAIL_EXISTS)) {
                context.error(Errors.EMAIL_IN_USE);
            } else
                context.error(Errors.INVALID_REGISTRATION);

            context.validationError(formData, errors);
            return;
        }

        super.validate(context);
    }

    @Override
    public void success(FormContext context) {
        //Following successful filling of the form, we store the required user information in the authentication session notes. This stored information is then retrieved at a later time to create the user account.//Following successful filling of the form, we store the required user information in the authentication session notes. This stored information is then retrieved at a later time to create the user account.
        Utils.storeUserDataInAuthSessionNotes(context);
    }

    @Override
    public String getDisplayType() {
        return "Registration User Creation with no Account";
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }


    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
