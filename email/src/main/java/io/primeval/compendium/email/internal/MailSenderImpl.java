package io.primeval.compendium.email.internal;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;

import io.primeval.codex.io.IODispatcher;
import io.primeval.compendium.email.MailSender;

@Component(name = MailSenderImpl.NAME, configurationPolicy = ConfigurationPolicy.REQUIRE)
public final class MailSenderImpl implements MailSender {
    public static final String NAME = "primeval.compendium.mailsender";

    private static final String CONFHOST = "mail.smtp.host";
    private static final String CONFPORT = "mail.smtp.port";
    private static final String CONFAUTH = "mail.smtp.auth";

    private String defaultFrom;
    private Properties mailProperties;
    private Authenticator sslAuthenticator;
    private IODispatcher ioDispatcher;
    protected ConnectionType connection;
    private MailSenderConfig config;

    @Activate
    public void activate(MailSenderConfig config) {
        applyConfig(config);
    }

    @Modified
    public void updated(MailSenderConfig config) {
        applyConfig(config);
    }

    private void applyConfig(MailSenderConfig config) {
        this.config = config;

        mailProperties = new Properties();

        mailProperties.put(CONFHOST, config.smtp_host());
        mailProperties.put(CONFPORT, config.smtp_port());
        mailProperties.put("mail.smtps.quitwait", false);

        List<String> trustedServers = Arrays.asList(config.tls_trustedservers());
        if (!trustedServers.isEmpty()) {
            mailProperties.put("mail.smtp.ssl.trust", trustedServers.stream().collect(Collectors.joining(",")));
        }

        connection = config.smtp_connection();

        switch (connection) {
        case SSL:
            mailProperties.put(CONFAUTH, Boolean.toString(true));
            mailProperties.put("mail.smtp.ssl.enable", "true");
            mailProperties.put("mail.smtp.socketFactory.port", Integer.toString(config.smtp_port()));
            mailProperties.put("mail.smtp.socketFactory.class", javax.net.ssl.SSLSocketFactory.class.getName());
            sslAuthenticator = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.smtp_username(), config._smtp_password());
                }
            };
            break;
        case TLS:
            mailProperties.put(CONFAUTH, Boolean.toString(true));
            mailProperties.put("mail.smtp.starttls.enable", Boolean.toString(true));
            break;
        case NO_AUTH:
        default:
            mailProperties.put(CONFAUTH, Boolean.toString(false));
            break;
        }
    }

    @Override
    public Promise<Void> send(String from, String to, String subject, String plainTextBody) {
        return send(new Mail(from, to, subject, plainTextBody));

    }

    private Promise<Void> send(Mail mail) {
        return ioDispatcher.dispatch(() -> {
            if (mail.to == null || mail.to.isEmpty()) {
                throw new IllegalArgumentException("The given 'to' is null or empty");
            }

            String from = Optional.ofNullable(mail.from).orElse(defaultFrom);

            Transport transport = null;
            final ClassLoader original = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(MailSenderImpl.class.getClassLoader());
                Session session = Session.getInstance(mailProperties, sslAuthenticator);

                session.setDebug(false);
                // create a message
                MimeMessage msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(from));
                msg.setRecipients(Message.RecipientType.TO, mail.to);

                msg.setSubject(mail.subject);
                Date sent = new Date();
                msg.setSentDate(sent);

                Multipart mp = new MimeMultipart();
                MimeBodyPart body = new MimeBodyPart();
                body.setText(mail.body, StandardCharsets.UTF_8.name(), "plain");
                mp.addBodyPart(body);
                msg.setContent(mp);
                

                if (connection == ConnectionType.SSL) {
                    transport = session.getTransport("smtps");
                } else {
                    transport = session.getTransport("smtp");
                }

                if (connection == ConnectionType.NO_AUTH) {
                    transport.connect(config.smtp_host(), config.smtp_port(), null, null);
                } else {
                    transport.connect(config.smtp_host(), config.smtp_port(), config.smtp_username(),
                            config._smtp_password());
                }
                transport.sendMessage(msg, msg.getAllRecipients());
            } finally {
                Thread.currentThread().setContextClassLoader(original);
                if (transport != null) {
                    transport.close();
                }
            }
        });
    }

    @Reference
    public void setIODispatcher(IODispatcher ioDispatcher) {
        this.ioDispatcher = ioDispatcher;
    }

}
