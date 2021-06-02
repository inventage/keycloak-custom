Keycloak-Custom
===============

This project is aimed to create a custom Keycloak server. The following features are supported:

- installation via the Keycloak distribution at [Maven Central](https://mvnrepository.com/artifact/org.keycloak/keycloak-server-dist)
- Wildfly setup via CLI scripts at build time
- package as Docker image
- Keycloak configuration via Keycloak Admin CLI
- development of custom extensions

This project is based on Maven and contains the following Maven modules:

- keycloak-server
- keycloak-config  
- docker-compose  
- extensions

There are similar proposals available on the internet:

- [Keycloak Project Example](https://github.com/thomasdarimont/keycloak-project-example)
- [Keycloak Docker Quickstart](https://github.com/OpenPj/keycloak-docker-quickstart)

Requirements
------------

In order to use this project, you need to install the following components:

- Java 11
- Docker 
- jq

Module keycloak-server
----------------------

This module installs the official Keycloak distribution into the folder referenced by the property `${keycloak.dir}`. The default value is [keycloak-server/target/wildfly](./keycloak-server/target/wildfly) (as defined by `${project.build.directory}/wildfly`).

The following Maven command does the installation and setup of Keycloak:

```shell
./mvnw clean package
```

After a successful execution, Keycloak can be started by the IntelliJ Run Configuration named `Keycloak standalone-ha (node1)`.

### Wildfly configuration

For some features Keycloak relies on the underlying Wildfly server. Their configuration has to be done within Wildfly. The Wildfly CLI scripting is used for this. The main CLI scripts are [standalone-configuration.cli](keycloak-server/src/main/resources/wildfly/cli/standalone-configuration.cli) and [standalone-ha-configuration.cli](keycloak-custom/src/main/resources/wildfly/cli/standalone-ha-configuration.cli) The following features are configured:

- access log
- logging
- proxy support in front of Keycloak
- datasource
- Postgres JDBC driver
- truststore for client TLS connections
- keystore for server TLS connections
- distributed caching (only for high availability mode)

These CLI scripts are launched during the build of the `keycloak-server` module. The Launch is defined by the `exec-maven-plugin` in the [pom.xml](./keycloak-custom/pom.xml), as the executions `patch-standalone` and `patch-standalone-ha`.

#### Logging

The logging.cli also adds two loggers for the categories `org.keycloak` and `com.inventage` (= java packages). Their log level can be configured by the following environment variables:

- KEYCLOAK_LOGLEVEL : default log level is INFO
- INVENTAGE_KEYCLOAK_LOGLEVEL : default log level is DEBUG

#### Datasource

The keycloak-datasource.cli uses the following environment variables for configuring the `KeycloakDS` datasource:

- DB_VENDOR : JDBC driver name as defined in datasources:datasources/drivers/driver@name
- DB_CONNECTION_URL : JDBC connection URL, specific for the used JDBC driver
- DB_USER : username for the JDBC connection
- DB_PASSWORD: password for the JDBC connection

When defining the value of these variables, new variables can be used. The `DB_CONNECTION_URL` variable could be defined as:

```
DB_CONNECTION_URL=jdbc:postgresql://${env.DB_ADDR:postgres}:${env.DB_PORT:5432}/${env.DB_DATABASE:keycloak}${env.JDBC_PARAMS:}
```

By this way, if only the database name is different for various deployments, the default value `keycloak` can be overridden by the `DB_DATABASE` variable.

#### Postgres JDBC driver

The JDBC drivers within Wildfly are realized by modules. As an example the Postgres JDBC driver is added by the Module [module.xml](keycloak-server/src/main/resources/wildfly/modules/system/layers/keycloak/org/postgresql/main/module.xml)

#### TLS truststore

The keycloak-truststore.cli uses the following environment variables for configuring the truststore:

- KEYCLOAK_SSL_CUSTOM_TRUSTSTORE_HOSTNAME_VERIFICATION_POLICY : default WILDCARD
- KEYCLOAK_SSL_CUSTOM_TRUSTSTORE_DISABLED : default true, so that Java JSSE configuration is used
- KEYCLOAK_SSL_CUSTOM_TRUSTSTORE_PATH
- KEYCLOAK_SSL_CUSTOM_TRUSTSTORE_PASSWORD

For demonstration and development purposes a custom truststore ([truststore-for-development-purpose-only.jks](keycloak-server/src/main/resources/wildfly/standalone/configuration/truststore-for-development-purpose-only.jks)) is provided.

#### TLS keystore

For incoming HTTPS connections the Wildfly server must be configured. It is done in the ssl.cli script.

For demonstration and development purposes a custom keystore ([keystore-for-development-purpose-only.jks](keycloak-server/src/main/resources/wildfly/standalone/configuration/keystore-for-development-purpose-only.jks)) is provided.

See also [Keycloak Server Installation - Setting up HTTPS/SSL](https://www.keycloak.org/docs/latest/server_installation/index.html#_setting_up_ssl).

### Docker image

The following Maven command builds also the docker image:

```shell
./mvnw clean install
```

### Launching Keycloak

- IntelliJ Run Configuration `Keycloak standalone-ha (node1)`

Module keycloak-config
----------------------

This module configures Keycloak via the admin CLI. Please refer to the [keycloak.ch tutorial 1](https://keycloak.ch/keycloak-tutorials/tutorial-1-installing-and-running-keycloak/) for a detailed explanation of the CLI based configuration.

The execution is done by the following shell scripts in the shown order:

```
keycloak-configuration.sh 
    -> keycloak-configuration-helpers.sh
    -> realms.sh
        -> realm_master.sh
        -> realm_example1.sh
```

To support the usage for different environments (DEV, TEST, PROD), the configuration is heavily based on environment variables.

Module docker-compose
---------------------

This module contains the docker-compose file for starting the custom Keycloak docker image built by the `keycloak-server` module.

Module extensions
-----------------

### Extenstion NoOperationAuthenticator

This extension is an example of a custom Keycloak Authenticator implementation.

Tooling
-------

The [Takari Maven Wrapper](https://github.com/takari/maven-wrapper) is used for the Maven setup for this project.