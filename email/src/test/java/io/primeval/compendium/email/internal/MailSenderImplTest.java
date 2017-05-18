package io.primeval.compendium.email.internal;

import java.lang.annotation.Annotation;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.osgi.util.promise.Promise;

import io.primeval.codex.test.rules.WithCodex;
import io.primeval.codex.test.rules.WithCodexIO;

@Ignore // Should run this only with the right profile...
// keeping this for further testing
public class MailSenderImplTest {

    private static final String EMAIL_FROM_TO = "<email@example.com>";
    private static final String GMAIL_USERNAME = "<username>";
    private static final String GMAIL_PASSWORD = "<password>";

    public static WithCodex withCodex = new WithCodex();

    public static WithCodexIO withCodexIO = new WithCodexIO(withCodex);

    @ClassRule
    public static RuleChain chain = RuleChain.outerRule(withCodex).around(withCodexIO);

    @Test
    public void sendTLSMail() throws Exception {
        MailSenderImpl mailSenderImpl = new MailSenderImpl();
        mailSenderImpl.setIODispatcher(withCodexIO.getIoDispatcher());
        mailSenderImpl.activate(new MailSenderConfig() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return MailSenderConfig.class;
            }

            @Override
            public String[] tls_trustedservers() {
                return new String[0];
            }

            @Override
            public String smtp_username() {
                return GMAIL_USERNAME;
            }

            @Override
            public int smtp_port() {
                return 587;
            }

            @Override
            public String smtp_host() {
                return "smtp.gmail.com";
            }

            @Override
            public String smtp_from() {
                return EMAIL_FROM_TO;
            }

            @Override
            public ConnectionType smtp_connection() {
                return ConnectionType.TLS;
            }

            @Override
            public String _smtp_password() {
                return GMAIL_PASSWORD;
            }
        });

        Promise<Void> send = mailSenderImpl.send(EMAIL_FROM_TO, EMAIL_FROM_TO, "Test TLS",
                "hello test TLS");
        send.getValue();
    }

    @Test
    public void sendSSLMail() throws Exception {
        MailSenderImpl mailSenderImpl = new MailSenderImpl();
        mailSenderImpl.setIODispatcher(withCodexIO.getIoDispatcher());
        mailSenderImpl.activate(new MailSenderConfig() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return MailSenderConfig.class;
            }

            @Override
            public String[] tls_trustedservers() {
                return new String[0];
            }

            @Override
            public String smtp_username() {
                return GMAIL_USERNAME;
            }

            @Override
            public int smtp_port() {
                return 465;
            }

            @Override
            public String smtp_host() {
                return "smtp.gmail.com";
            }

            @Override
            public String smtp_from() {
                return EMAIL_FROM_TO;
            }

            @Override
            public ConnectionType smtp_connection() {
                return ConnectionType.SSL;
            }

            @Override
            public String _smtp_password() {
                return GMAIL_PASSWORD;
            }
        });

        Promise<Void> send = mailSenderImpl.send(EMAIL_FROM_TO, EMAIL_FROM_TO, "Test SSL",
                "hello test SSL");
        send.getValue();
    }

}
