package com.inventage.keycloak;

import com.inventage.keycloak.noopauthenticator.infrastructure.authenticator.NoOperationAuthenticatorFactory;
import com.inventage.keycloak.noopformauthenticator.infrastructure.authenticator.NoOperationFormAuthenticatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.authentication.Authenticator;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.remote.runonserver.InjectRunOnServer;
import org.keycloak.testframework.remote.runonserver.RunOnServerClient;
import org.keycloak.testframework.server.KeycloakServerConfig;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;

/**
 * Integration test for the 'extension-no-op-authenticator' extension using the keycloak test framework.
 * see https://github.com/keycloak/keycloak/tree/main/test-framework/docs
 */
@KeycloakIntegrationTest(config = ExtensionTest.ServerConfig.class)
public class ExtensionTest {

    @InjectRealm(lifecycle = LifeCycle.CLASS)
    ManagedRealm defaultRealm;

    @InjectRunOnServer(permittedPackages = "com.inventage.keycloak")
    RunOnServerClient runOnServer;

    @Test
    public void noOperationAuthenticator_is_active() {
        // given - the standard Keycloak distribution with the installed extension from this module
        // when - the Keycloak instance is up & running
        // then
        Assertions.assertEquals("default", defaultRealm.getName());
        runOnServer.run(session -> {
            Authenticator noOperationAuthenticator = session.getProvider(Authenticator.class, NoOperationAuthenticatorFactory.PROVIDER_ID);
            Assertions.assertNotNull(noOperationAuthenticator);
        });
    }

    @Test
    public void noOperationFormAuthenticator_is_active() {
        // given - the standard Keycloak distribution with the installed extension from this module
        // when - the Keycloak instance is up & running
        // then
        Assertions.assertEquals("default", defaultRealm.getName());
        runOnServer.run(session -> {
            Authenticator noOperationFormAuthenticator = session.getProvider(Authenticator.class, NoOperationFormAuthenticatorFactory.PROVIDER_ID);
            Assertions.assertNotNull(noOperationFormAuthenticator);
        });
    }

    static class ServerConfig implements KeycloakServerConfig {
        @Override
        public KeycloakServerConfigBuilder configure(KeycloakServerConfigBuilder config) {
            return config.dependencyCurrentProject();
        }
    }
}
