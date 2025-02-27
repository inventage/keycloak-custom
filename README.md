Keycloak-Custom
===

#### Admin Users Setup

Since [Keycloak 26 there are Bootstrap Admin Users](https://www.keycloak.org/docs/latest/upgrading/#admin-bootstrapping-and-recovery), who are used for the first start and setup of Keycloak. Afterward, this user should be deleted.
Hence, during the first setup of Keycloak we add a client with a service account with realm admin role:

Keycloak-config-cli uses as default the bootstrap admin user (`KC_BOOTSTRAP_ADMIN_*`). The service account user can be used for further setup runs as soon as the `keycloak-setup.sh` has run once.
Set the `KEYCLOAK_GRANTTYPE` to `client_credentials` and set `KEYCLOAK_CLIENTID` to `KEYCLOAK_CONFIG_CLI_CLIENT_ID` in order to change the authentication type of keycloak-config-cli to `client_credentials`.

```shell
# KC_BOOTSTRAP_ADMIN_USERNAME
KC_BOOTSTRAP_ADMIN_USERNAME=admin

# KC_BOOTSTRAP_ADMIN_PASSWORD
KC_BOOTSTRAP_ADMIN_PASSWORD=admin

# used for creating the client for keycloak-config-cli in realm-master.json
KEYCLOAK_CONFIG_CLI_CLIENT_ID=keycloak-config-cli

# used for creating the client for keycloak-config-cli in realm-master.json
KEYCLOAK_CONFIG_CLI_CLIENT_SECRET=keycloak-config-cli

# KEYCLOAK_GRANTTYPE property used by keycloak-config-cli either password or client_credentials
#KEYCLOAK_GRANTTYPE=client_credentials

# KEYCLOAK_CLIENTID property used by keycloak-config-cli when grant-type is client_credentials
#KEYCLOAK_CLIENTID=${KEYCLOAK_CONFIG_CLI_CLIENT_ID}

# KEYCLOAK_CLIENTSECRET property used by keycloak-config-cli when grant-type is client_credentials
KEYCLOAK_CLIENTSECRET=${KEYCLOAK_CONFIG_CLI_CLIENT_SECRET}
```

Project Template
---

This project is based on the [custom Keycloak template](https://github.com/inventage/keycloak-custom). It is structured as a multi-module Maven build and contains the following top-level modules:

- `config`: provides the build stage configuration and the setup of Keycloak
- `container`: creates the custom docker image
- `docker-compose`: provides a sample for launching the custom docker image
- `extensions`: provides samples for Keycloak SPI implementations
- `helm`: provides a sample for installing the custom container image in Kubernetes using the Codecentric Helm Chart
- `server`: provides a Keycloak installation for local development & testing
- `themes`: provides samples for custom themes

Please see the tutorial [building a custom Keycloak container image](https://keycloak.ch/keycloak-tutorials/tutorial-custom-keycloak/) for the details of this project.

[Keycloak]: https://keycloak.org

