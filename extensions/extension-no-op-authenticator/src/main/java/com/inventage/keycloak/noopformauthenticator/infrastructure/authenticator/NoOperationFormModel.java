package com.inventage.keycloak.noopformauthenticator.infrastructure.authenticator;

import java.util.ArrayList;
import java.util.List;

public class NoOperationFormModel {

    private final List<String> options;

    public NoOperationFormModel() {
        options = new ArrayList<>();
        options.add("Option1");
        options.add("Option2");
    }

    /**
     * Method is being used in the freemarker template, which is why we explicitly ignore the unused method warning.
     *
     * @return List of options.
     */
    @SuppressWarnings("unused")
    public List<String> getOptions() {
        return options;
    }

}
