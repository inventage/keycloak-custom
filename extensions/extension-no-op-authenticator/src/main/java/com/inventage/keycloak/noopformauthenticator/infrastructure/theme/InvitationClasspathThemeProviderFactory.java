package com.inventage.keycloak.noopformauthenticator.infrastructure.theme;

import org.jboss.logging.Logger;
import org.keycloak.theme.ClasspathThemeResourceProviderFactory;
import org.keycloak.theme.ThemeResourceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

/**
 *
 */
public class InvitationClasspathThemeProviderFactory extends ClasspathThemeResourceProviderFactory {

    private static final Logger LOGGER = Logger.getLogger(InvitationClasspathThemeProviderFactory.class);

    private ThemeResourceProvider resourceProviderFactory = new ClasspathThemeResourceProviderFactory("no-op-form-authenticator-classpath", ClasspathThemeResourceProviderFactory.class.getClassLoader());

    public InvitationClasspathThemeProviderFactory() {
        super("no-op-form-authenticator-classpath", InvitationClasspathThemeProviderFactory.class.getClassLoader());
    }

    @Override
    public URL getTemplate(String name) throws IOException {
        final URL template = super.getTemplate(name);
        if (template != null) {
            return template;
        }
        LOGGER.debugf("getTemplate: for name '%s'", name);
        return resourceProviderFactory.getTemplate(name);
    }

    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        final InputStream resourceAsStream = super.getResourceAsStream(path);
        if (resourceAsStream != null) {
            return resourceAsStream;
        }
        LOGGER.debugf("getResourceAsStream: for path '%s'", path);
        return resourceProviderFactory.getResourceAsStream(path);
    }

    @Override
    public Properties getMessages(String baseBundlename, Locale locale) throws IOException {
        LOGGER.debugf("getMessages: for baseBundlename '%s'", baseBundlename);
        final Properties fallbackMessages = resourceProviderFactory.getMessages(baseBundlename, locale);
        final Properties messages = super.getMessages(baseBundlename, locale);
        if (messages != null) {
            fallbackMessages.putAll(messages);
        }
        return fallbackMessages;
    }

}
