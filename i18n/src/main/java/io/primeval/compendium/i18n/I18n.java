package io.primeval.compendium.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface I18n {

    Locale defaultLocale();
    
    List<Locale> supportedLocales();

    String get(Locale locale, String key, Object... args);

    Map<String, String> getAllMessages(Locale locale);

}
