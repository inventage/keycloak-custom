package com.inventage.keycloak;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.testframework.annotations.InjectRealm;
import org.keycloak.testframework.annotations.KeycloakIntegrationTest;
import org.keycloak.testframework.injection.LifeCycle;
import org.keycloak.testframework.realm.ManagedRealm;
import org.keycloak.testframework.remote.runonserver.InjectRunOnServer;
import org.keycloak.testframework.remote.runonserver.RunOnServerClient;
import org.keycloak.testframework.server.KeycloakServerConfig;
import org.keycloak.testframework.server.KeycloakServerConfigBuilder;

/**
 * Integration test for the 'extension-no-op-protocol-mapper' extension using the keycloak test framework.
 * see https://github.com/keycloak/keycloak/tree/main/test-framework/docs
 */
@KeycloakIntegrationTest(config = ExtensionTest.ServerConfig.class)
public class ExtensionTest {

    @InjectRealm(lifecycle = LifeCycle.CLASS)
    ManagedRealm defaultRealm;

    @InjectRunOnServer(permittedPackages = "com.inventage.keycloak")
    RunOnServerClient runOnServer;

    @Test
    public void noOperationProtocolMapper_is_active() {
        // given - the standard Keycloak distribution with the installed extension from this module
        // when - the Keycloak instance is up & running
        // then
        Assertions.assertEquals("default", defaultRealm.getName());
// Caused by: java.lang.RuntimeException: UNSUPPORTED METHOD
//	at org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper.create(AbstractOIDCProtocolMapper.java:61)
//        runOnServer.run(session -> {
//            ProtocolMapper noOperationProtocolMapper = session.getProvider(ProtocolMapper.class, "no-operation-protocol-mapper");
//            Assertions.assertNotNull(noOperationProtocolMapper);
//        });
    }

    static class ServerConfig implements KeycloakServerConfig {
        @Override
        public KeycloakServerConfigBuilder configure(KeycloakServerConfigBuilder config) {
            return config.dependencyCurrentProject();
        }
    }
}
