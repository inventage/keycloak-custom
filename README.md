Keycloak-Custom
===============

This project creates a custom [Keycloak] server based on [Keycloak.X]. It is structured as a multi-module Maven build and contains the following top-level modules:

- config  : provides the build stage configuration and the setup of Keycloak
- container : creates the custom docker image
- docker-compose : provides a sample for launching the custom docker image
- extensions : provides samples for Keycloak SPI implementations
- server : provides a Keycloak installation for local development & testing
- themes : provides samples for custom themes

Please see the tutorial [building a custom Keycloak container image](https://keycloak.ch/keycloak-tutorials/tutorial-custom-keycloak/) for the details of this project.


[Keycloak]: https://keycloak.org
[Keycloak.X]: https://www.keycloak.org/migration/migrating-to-quarkus


Reshare-hub specific keycloak with plugins for LMS authn and other config


In development, after checkout create a

Build with
./mvnw clean install

Push to nexus with
docker push NAME[:TAG]
docker image push docker.libsdev.k-int.com/reshare/hub-authn --all-tags


IF you add a new custom authentication provider and want that provider bundled into the docker image, don't forget to edit
container/pom.xml and add your artefact ID to includeArtifactIds AND add your new authenticator to the list of dependencies in the
container project.





https://github.com/adorsys/keycloak-config-cli/blob/main/src/main/java/de/adorsys/keycloak/config/provider/KeycloakProvider.java
https://www.keycloak.org/docs/latest/server_development/#_user-storage-spi
