Keycloak-Custom
===============

This project creates a custom [Keycloak] server based on [Keycloak.X]. The following features are supported:

- installation via the [Keycloak distribution at Maven Central](https://mvnrepository.com/artifact/org.keycloak/keycloak-quarkus-dist)
- configuration via [Admin CLI] of Keycloak
- configuration via [keycloak-config-cli] of Adorsys
- development of custom extensions
- package as Docker image

This project is based on Maven and contains the following top-level Maven modules:

- server : provides a Keycloak installation for local testing
- config  : provides the build stage configuration and the setup of Keycloak
- container : provides the custom docker image
- docker-compose : provides a sample for launching the custom docker image
- extensions : provides samples for Keycloak SPI implementations

![Maven modules](.docs/keycloak-custom_modules.png)

There are similar projects available on the internet:

- [Keycloak Project Example](https://github.com/thomasdarimont/keycloak-project-example)
- [Keycloak Docker Quickstart](https://github.com/OpenPj/keycloak-docker-quickstart)

Requirements
------------

In order to use this project, you need to install the following components:

- Java 11
- Docker 
- [jq](https://stedolan.github.io/jq/)

Module server
-------------

This module installs the official Keycloak distribution via a Maven dependency into the folder referenced by the property [${keycloak.dir}]. The default value is `server/target/keycloak`.

The following Maven command does the installation:

```shell
./mvnw clean initialize
```

After a successful execution the newly created Keycloak installation could be started with the factory settings by executing the `kc.sh start-dev` script from the `server/target/keycloak/bin` directory. Because we want to apply a custom configuration to this installation, we wait with starting up Keycloak until we have introduced the `config` module.

For passing a defined set of environment variables to the above script, we use the wrapper script [run-keycloak.sh] from this module. The current directory must be `server/target/keycloak` when executing the [run-keycloak.sh] script.

Module config
-------------

This module copies its configuration files during the Maven `generate-resources` phase to the Keycloak installation within the `server` module.

### Configuration

The [configuration of Keycloak] is done into two steps: build stage and runtime stage. A few properties can only be set in the build stage. If they must be changed, the `kc.sh` script must be executed with the `build` command. In development mode (see below) the `build` command is automatically executed during every start.

#### Build stage

In this project all properties of the build stage are configured in the [keycloak.conf] at `config/src/main/resources/keycloak/conf/`.

We set 3 properties:

```properties
# db is the name of the database vendor: dev-file, dev-mem, mariadb, mssql, mysql, oracle, postgres
db=postgres

# features is a comma-separated list of features to be enabled
features=declarative-user-profile

# metrics-enabled is for exposing metrics (/metrics) and health check (/health) endpoints.
metrics-enabled=true
```

In the `generate-resources` phase of a Maven build this [keycloak.conf] file is copied to [${keycloak.dir}].

Please see [Keycloak/Guides/Server/All configuration/Build options](https://www.keycloak.org/server/all-config?f=build) for the list of all available build stage properties.

#### Runtime stage

All properties of the runtime stage are set as environment variables.

In this project we are using three `.env` files (in `./docker-compose/src/main/resources`) for maintaining the environment variables:

##### keycloak.common.env

##### keycloak.specific.env

##### secrets.env

Sensitive properties can be stored in the `secrets.env` file. This file is not under version control and must be available when running `docker-compose up`.

````properties
# KEYCLOAK_ADMIN is the username of the initial admin user
KEYCLOAK_ADMIN=admin

# KEYCLOAK_ADMIN_PASSWORD is the password of the initial admin user
KEYCLOAK_ADMIN_PASSWORD=admin
````

### Launching Keycloak

Keycloak provides the `bin/kc.sh` script for launching it. Keycloak can be launched in two modes: development (`start-dev`) or production (`start`).

In this project we

For launching Keycloak from within this project we use the wrapper script [run-keycloak.sh] from the `server` module. This script is not used for launching Keycloak outside of this project. The main purpose of this script is to provide the set of environment variables to be used. The `--debug` flag is set, so that a debugger can be attached.

The [run-keycloak.sh] script takes one or more arguments. The first argument is the command be executed `start-dev` or `start`. Every further argument must be a filesystem path to a properties file. Every contained property will be set as an environment variable. Later files override earlier files.

```shell
$ cd ./server/target/keycloak
$ ../../src/test/resources/run-keycloak.sh start-dev <properties.env>
```

#### Development mode

For an easy usage this project provides also the IntelliJ run configuration `run-keycloak.sh start-dev` is for starting Keycloak in `development` mode. 

```shell
$ cd ./server/target/keycloak
$ ../../src/test/resources/run-keycloak.sh start-dev \
  ../../../docker-compose/src/main/resources/keycloak.common.env \
  ../../../docker-compose/src/main/resources/keycloak.specific.env \
  ../../../docker-compose/src/main/resources/secrets.env
```

#### Production mode

For a simple use this project provides also the IntelliJ run configuration `run-keycloak.sh start` is for starting Keycloak in `production` mode.

```shell
$ cd ./server/target/keycloak
$ ../../src/test/resources/run-keycloak.sh start \
  ../../../docker-compose/src/main/resources/keycloak.common.env \
  ../../../docker-compose/src/main/resources/keycloak.specific.env \
  ../../../docker-compose/src/main/resources/secrets.env
```

```shell
ERROR: You can not 'start' the server in development mode. Please re-build the server first, using 'kc.sh build' for the default production mode.
```

### Setup



Module container
----------------

In the `install` phase of a Maven build a custom docker image is built. The `container` module 

Module docker-compose
---------------------

This module contains the docker-compose file for starting the custom Keycloak docker image built by the `container` module.

Module extensions
-----------------

The `extensions` module is configured, so that every contained extension module can easily deploy its artifact to the keycloak-server module.

### Extension NoOperationAuthenticator

This extension is an example of a custom Keycloak Authenticator implementation.

### Extension NoOperationProtocolMapper

This extension is an example of a custom Keycloak Protocol Mapper implementation.

Tooling
-------

The [Takari Maven Wrapper](https://github.com/takari/maven-wrapper) is used for the Maven setup for this project.



[Keycloak]: https://keycloak.org
[Keycloak.X]: https://www.keycloak.org/migration/migrating-to-quarkus
[Admin CLI]: https://www.keycloak.org/docs/latest/server_admin/index.html#admin-cli
[configuration of Keycloak]: https://www.keycloak.org/server/configuration
[keycloak-config-cli]: https://github.com/adorsys/keycloak-config-cli
[keycloak.conf]: ./config/src/main/resources/keycloak/conf/keycloak.conf
[${keycloak.dir}]: ./server/target/keycloak
[run-keycloak.sh]: ./server/src/test/resources/run-keycloak.sh