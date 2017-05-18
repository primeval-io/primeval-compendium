# primeval-cmpn-i18n [![Build Status](https://travis-ci.org/primeval-io/primeval-compendium.svg?branch=master)](https://travis-ci.org/primeval-io/primeval-compendium) [![Gitter primeval-io/Lobby](https://badges.gitter.im/primeval-io/Lobby.svg)](https://gitter.im/primeval-io/Lobby)

Primeval Compendium Internationalization.


# OSGi Services

`I18n` lets you provide bundle internationalisation. /!\ work-in-progress, quick and bad implementation /!\

Configuration:

```java
public @interface I18nConfig {

    String defaultLocale();

    String[] supportedLocales() default {};

}

```


# Getting help

Post a new GitHub issue or join on [Gitter](https://gitter.im/primeval-io/Lobby).
 

# Author

primeval-compendium was developed by Simon Chemouil.

# Copyright

(c) 2016-2017, Simon Chemouil, Lambdacube

primeval-compendium is part of the Primeval project.
