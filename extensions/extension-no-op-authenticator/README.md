No Operation Authenticator Extension
===

This extension provides two implementations for the [AuthenticatorSpi] of Keycloak. The `No Operation Form Authenticator` also implements the [ThemeResourceSpi] for providing a custom form for user input.

No Operation Authenticator
---

The `@AutoService` annotation from [Google Auto project](https://github.com/google/auto/tree/main) is used for registering the extension at the [java.lang.ServiceLoader](https://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) by automatic generation of the META-INF metadata.

check: http://localhost:8080/admin/master/console/#/master/providers

No Operation Form Authenticator
---

check: http://localhost:8080/admin/master/console/#/master/providers