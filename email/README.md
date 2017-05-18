# primeval-cmpn-email [![Build Status](https://travis-ci.org/primeval-io/primeval-compendium.svg?branch=master)](https://travis-ci.org/primeval-io/primeval-compendium) [![Gitter primeval-io/Lobby](https://badges.gitter.im/primeval-io/Lobby.svg)](https://gitter.im/primeval-io/Lobby)

Primeval Compendium Email.


# OSGi Services

`MailSender` lets you send very simple mails (no attachment). 

Configuration:

```java
public @interface MailSenderConfig {

    ConnectionType smtp_connection() default ConnectionType.TLS;

    String smtp_host() default "localhost";

    int smtp_port() default 587;

    String smtp_from();

    String smtp_username();

    String _smtp_password();

    String[] tls_trustedservers() default {};
}


```


# Getting help

Post a new GitHub issue or join on [Gitter](https://gitter.im/primeval-io/Lobby).
 

# Author

primeval-compendium was developed by Simon Chemouil.

# Copyright

(c) 2016-2017, Simon Chemouil, Lambdacube

primeval-compendium is part of the Primeval project.
