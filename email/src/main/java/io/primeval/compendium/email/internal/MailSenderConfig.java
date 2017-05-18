package io.primeval.compendium.email.internal;

public @interface MailSenderConfig {

    ConnectionType smtp_connection() default ConnectionType.TLS;

    String smtp_host() default "localhost";

    int smtp_port() default 587;

    String smtp_from();

    String smtp_username();

    String _smtp_password();

    String[] tls_trustedservers() default {};

}
