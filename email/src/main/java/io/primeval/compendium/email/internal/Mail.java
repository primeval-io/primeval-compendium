package io.primeval.compendium.email.internal;

// internal class, it *will* change to support richer semantics.
public final class Mail {

    public final String from;
    public final String to;
    public final String subject;
    public final String body;

    public Mail(String from, String to, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

}
