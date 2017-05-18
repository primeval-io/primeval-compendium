package io.primeval.compendium.email;

import org.osgi.util.promise.Promise;

public interface MailSender {

    Promise<Void> send(String from, String to, String subject, String plainTextBody);

}
