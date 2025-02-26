Keycloak-Custom
===

#### Admin Users Setup

Since [Keycloak 26 there are Bootstrap Admin Users](https://www.keycloak.org/docs/latest/upgrading/#admin-bootstrapping-and-recovery), who are used for the first start and setup of Keycloak. Afterward, this user should be deleted.
Hence, during the first setup of Keycloak we add two additional admin users:

- Admin user für keycloak-config-cli
- Admin user für Admin Web Console

Keycloak-config-cli uses the user which is configured with `KEYCLOAK_CONFIG_CLI_*` and kcadm uses `KEYCLOAK_CLI_*` during the setup.

```shell
# KC_BOOTSTRAP_ADMIN_USERNAME
KC_BOOTSTRAP_ADMIN_USERNAME=bootstrap

# KC_BOOTSTRAP_ADMIN_PASSWORD
KC_BOOTSTRAP_ADMIN_PASSWORD=bootstrap (overwrite!)

# KEYCLOAK_CONFIG_CLI_SETUP_USERNAME is the username used in realm-master.json for creating user used by keycloak-config-cli
KEYCLOAK_CONFIG_CLI_SETUP_USERNAME=keycloak-config-cli
# KEYCLOAK_CONFIG_CLI_SETUP_PASSWORD is the password used in realm-master.json for the created keycloak-config-cli user
# WEB_CONSOLE_ADMIN_USERNAME is the username for access the web admin console
WEB_CONSOLE_ADMIN_USERNAME=admin
# WEB_CONSOLE_ADMIN_PASSWORD is the password used in realm-master.json for the created admin user

(The passwords of the new admin users are hidden in another file)

# Replace the 4 'KC_BOOTSTRAP_ADMIN_*' variable names with 'KEYCLOAK_CONFIG_CLI_SETUP_*' as soon as the bootstrap user has been deleted
KEYCLOAK_CONFIG_CLI_USERNAME=${KC_BOOTSTRAP_ADMIN_USERNAME}
KEYCLOAK_CONFIG_CLI_PASSWORD=${KC_BOOTSTRAP_ADMIN_PASSWORD}
KEYCLOAK_CLI_USERNAME=KC_BOOTSTRAP_ADMIN_USERNAME
KEYCLOAK_CLI_PASSWORD=KC_BOOTSTRAP_ADMIN_PASSWORD
```

After the first run of the setup (`kc-with-setup.sh`) the configuration has to be adapted such that config tools are utilizing the new users.
This means `KEYCLOAK_CONFIG_CLI_*` has to be set to the value of `KEYCLOAK_CONFIG_CLI_SETUP_*` and `KEYCLOAK_CLI_*` to the environment variable names `KC_BOOTSTRAP_ADMIN_*`  

```shell
# Replace the 4 'KC_BOOTSTRAP_ADMIN_*' variable names with 'KEYCLOAK_CONFIG_CLI_SETUP_*' as soon as the bootstrap user has been deleted
KEYCLOAK_CONFIG_CLI_USERNAME=${KEYCLOAK_CONFIG_CLI_SETUP_USERNAME}
KEYCLOAK_CONFIG_CLI_PASSWORD=${KEYCLOAK_CONFIG_CLI_SETUP_PASSWORD}
KEYCLOAK_CLI_USERNAME=KEYCLOAK_CONFIG_CLI_SETUP_USERNAME
KEYCLOAK_CLI_PASSWORD=KEYCLOAK_CONFIG_CLI_SETUP_PASSWORD
```

After that, the bootstrap admin user can be safely deleted.

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

