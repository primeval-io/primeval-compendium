package io.primeval.compendium.i18n.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.primeval.compendium.i18n.I18n;

@Component(immediate = true, name = I18nImpl.NAME, configurationPolicy = ConfigurationPolicy.REQUIRE)
public final class I18nImpl implements I18n {

    public static final String NAME = "primeval.compendium.i18n";
    private static final Logger LOGGER = LoggerFactory.getLogger(I18n.class);
    private static final String I18N_RESOURCES = "I18n-Resources";

    private final Map<BundleRevision, Map<Locale, Map<String, String>>> i18nData = new ConcurrentHashMap<>();
    private Locale defaultLocale;
    private BundleTracker<BundleRevision> bundleTracker;
    private List<Locale> supportedLocales;

    @Activate
    public void activate(I18nConfig config, BundleContext bundleContext) {
        applyConfig(config);
        bundleTracker = new BundleTracker<>(bundleContext, Bundle.ACTIVE,
                new BundleTrackerCustomizer<BundleRevision>() {

                    @Override
                    public BundleRevision addingBundle(Bundle bundle, BundleEvent event) {
                        String i18nResources = bundle.getHeaders().get(I18N_RESOURCES);
                        if (i18nResources != null) {
                            BundleRevision bundleRev = bundle.adapt(BundleRevision.class);
                            if (!i18nResources.endsWith("/")) {
                                i18nResources += "/";
                            }
                            Enumeration<String> entryPaths = bundle.getEntryPaths(i18nResources);

                            while (entryPaths.hasMoreElements()) {
                                String f = entryPaths.nextElement();
                                URL entry = bundle.getEntry(f);
                                File file = new File(entry.getFile());
                                String fn = file.getName();
                                Locale locale = getLocale(fn);
                                try (InputStream s = entry.openStream()) {
                                    Properties properties = new Properties();
                                    properties.load(s);
                                    Map<String, String> props = i18nData
                                            .computeIfAbsent(bundleRev, k -> new ConcurrentHashMap<>())
                                            .computeIfAbsent(locale, k -> new ConcurrentHashMap<>());
                                    properties.stringPropertyNames()
                                            .forEach(pn -> props.put(pn, properties.getProperty(pn)));

                                } catch (IOException e) {
                                    LOGGER.error("Error while trying to read i18n properties {} in bundle {}", f,
                                            bundle, e);
                                }
                            }

                            // new PropertyResourceBundle(stream)

                            return bundleRev;
                        }
                        return null;
                    }

                    @Override
                    public void modifiedBundle(Bundle bundle, BundleEvent event, BundleRevision bundleRev) {

                    }

                    @Override
                    public void removedBundle(Bundle bundle, BundleEvent event, BundleRevision bundleRev) {
                        i18nData.remove(bundleRev);
                    }
                });
        bundleTracker.open();
    }

    @Deactivate
    public void deactivate() {
        bundleTracker.close();
    }

    @Modified
    public void modified(I18nConfig config) {
        applyConfig(config);
    }

    private void applyConfig(I18nConfig config) {
        this.defaultLocale = Locale.forLanguageTag(config.defaultLocale());
        this.supportedLocales = Collections.unmodifiableList(
                Stream.of(config.supportedLocales()).map(Locale::forLanguageTag).collect(Collectors.toList()));
    }

    public Locale getLocale(String name) {
        final int index = name.indexOf('_');
        if (index != -1) {
            String locale = name.substring(index + 1);
            int lastDot = locale.lastIndexOf('.');
            if (lastDot != -1) {
                locale = locale.substring(0, lastDot);
            }
            return Locale.forLanguageTag(locale.replace("_", "-"));
        } else {
            return Locale.ENGLISH;
        }
    }

    @Override
    public Locale defaultLocale() {
        return defaultLocale;
    }

    @Override
    public List<Locale> supportedLocales() {
        return supportedLocales;
    }

    @Override
    public String get(Locale locale, String key, Object... args) {
        // TODO rewrite this.
        return i18nData.values().stream().map(v -> Optional.ofNullable(v.get(locale)).map(l -> l.get(key)).orElse(null))
                .filter(p -> p != null).findFirst().map(s -> MessageFormat.format(s, args)).orElse(null);
    }

    @Override
    public Map<String, String> getAllMessages(Locale locale) {
        // TODO Auto-generated method stub
        return null;
    }

}
