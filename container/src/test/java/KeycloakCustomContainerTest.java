import org.junit.jupiter.api.*;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.junit.jupiter.Testcontainers;
import sut.SystemUnderTest;

import java.util.Optional;

@Tag("integration") // to run in verify phase of mvn
@Testcontainers
class KeycloakCustomContainerTest {

    private static SystemUnderTest sut;

    @BeforeAll
    static void beforeAll() {
        sut = SystemUnderTest.start();
    }

    protected SystemUnderTest sut() {
        return sut;
    }

    @AfterAll
    static void afterAll() {
        sut.stop();
    }

    @Test
    void test_startup() {
        Assertions.assertTrue(sut().keycloak.isRunning());
    }

    @Test
    void test_import_realm() {
        Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(sut().keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(sut().keycloak.getAdminUsername())
                .password(sut().keycloak.getAdminPassword())
                .build();

        Optional<RealmRepresentation> example1 = keycloakAdminClient.realms().findAll().stream().filter(realmRepresentation -> realmRepresentation.getRealm().equals("example1")).findFirst();
        Assertions.assertTrue(example1.isPresent(), "Realm `example1` should exist. Realm import via keycloak-config-cli failed.");
    }
}
