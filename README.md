Keycloak-Custom
===

Project Template
---

This project is based on the [custom Keycloak template](https://github.com/inventage/keycloak-custom). It is structured
as a multi-module Maven build and contains the following top-level modules:

- `config`: provides the build stage configuration and the setup of Keycloak
- `container`: creates the custom docker image
- `docker-compose`: provides a sample for launching the custom docker image
- `extensions`: provides samples for Keycloak SPI implementations
- `helm`: provides a sample for installing the custom container image in Kubernetes using the Codecentric Helm Chart
- `server`: provides a Keycloak installation for local development & testing
- `themes`: provides samples for custom themes

Please see the
tutorial [building a custom Keycloak container image](https://keycloak.ch/keycloak-tutorials/tutorial-custom-keycloak/)
for the details of this project.


Configuration of Keycloak
---

Since [Keycloak 26 there are temporary bootstrap admin accounts for user and client](https://www.keycloak.org/docs/latest/upgrading/#admin-bootstrapping-and-recovery),
which are created and used during the first start of Keycloak. Afterward, this user/client should be deleted.
Hence, during the first setup of Keycloak we add a permanent client with a service account. This account can then be used for future setup runs.
Our setup script uses two **configuration tools**: [keycloak-config-cli](https://github.com/adorsys/keycloak-config-cli) and [kcadm](https://github.com/keycloak/keycloak/blob/main/integration/client-cli/admin-cli/src/main/bin/kcadm.sh).

The following table shows which admin accounts are created after the first start of Keycloak and the first execution of the setup script:

| Configuration Account       | Temporary           | Permanent                                  |
|-----------------------------|---------------------|--------------------------------------------|
| Service Account `client-id` | `temp-client-admin` | `keycloak-config-cli`                      |
| Admin User `username`        | `temp-admin`        | (not configured, must be created manually) |

Below you can find a table with all the environment variables which are used during setup and which can be changed individually.
The setup script creates in the first run a permanent service account specifically created for the configuration tools. 
The service account can be configured with the properties `KEYCLOAK_CONFIG_CLI_CLIENT_*`.
The configuration tools use for authentication as default the bootstrap admin client (`KC_BOOTSTRAP_ADMIN_CLIENT_*`) which is configured in
`KEYCLOAK_CLIENT*`.
It is possible to let the configuration tools use the service account after the first setup run.
You can achieve this by setting `KEYCLOAK_CLIENTID` to `${KEYCLOAK_CONFIG_CLI_CLIENT_ID}` and `KEYCLOAK_CLIENTSECRET` to `${KC_BOOTSTRAP_ADMIN_CLIENT_SECRET}`.

| Environment Variable Name           | Description                                                                       | Default Value                         |
|-------------------------------------|-----------------------------------------------------------------------------------|---------------------------------------|
| `KC_BOOTSTRAP_ADMIN_USERNAME`       | Bootstrap admin username                                                          | `temp-admin`                          |
| `KC_BOOTSTRAP_ADMIN_PASSWORD`       | Bootstrap admin password                                                          | `admin` (**Please change!**)          |
| `KC_BOOTSTRAP_ADMIN_CLIENT_ID`      | Bootstrap Admin Client Id                                                         | `temp-client-admin`                   |
| `KC_BOOTSTRAP_ADMIN_CLIENT_SECRET`  | Bootstrap Admin Client Secret                                                     | `admin` (**Please change!**)          |
| `KEYCLOAK_CONFIG_CLI_CLIENT_ID`     | Used for creating the regular client for keycloak-config-cli in `realm-master.json` | `keycloak-config-cli`                 |
| `KEYCLOAK_CONFIG_CLI_CLIENT_SECRET` | Used for creating the regular client for keycloak-config-cli in `realm-master.json` | `keycloak-config-cli`                 |
| `KEYCLOAK_GRANTTYPE`                | Property used by keycloak-config-cli, it is either `password` or `client_credentials` | `client_credentials`                  |
| `KEYCLOAK_CLIENTID`                 | Property used by keycloak-config-cli and kcadm                                    | `${KC_BOOTSTRAP_ADMIN_CLIENT_ID}`     |
| `KEYCLOAK_CLIENTSECRET`             | Property used by keycloak-config-cli and kcadm                                    | `${KC_BOOTSTRAP_ADMIN_CLIENT_SECRET}` |

[Keycloak]: https://keycloak.org
[Keycloak Admin bootstrap and recovery]: https://www.keycloak.org/server/bootstrap-admin-recovery

