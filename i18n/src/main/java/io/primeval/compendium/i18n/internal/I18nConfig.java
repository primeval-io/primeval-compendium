package io.primeval.compendium.i18n.internal;

public @interface I18nConfig {

    String defaultLocale();

    String[] supportedLocales() default {};

}
