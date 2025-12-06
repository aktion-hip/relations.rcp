/**
	This package is part of the servlet framework used for the application VIF.
	Copyright (C) 2001-2015, Benno Luthiger

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.hip.kernel.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hip.kernel.exc.VException;
import org.hip.kernel.util.Debug;

/** This is a simple mail client, which can be used to send <code>HMultipartMessages </code> (MimeMessages with
 * Mulitpart content).
 *
 * The SMTP mail host is either set manually or retrieved from the JNDI context (JNDI resource "mail/Session").
 *
 * @author: Benno Luthiger */
public class MailClient {

    /** Key for smtp host property */
    private static final String JNDI_RESOURCE_NAME = "mail/Session";

    /** SMTP mail host */
    private String mailHost;

    /** Default constructor creates new instance of MailClient. */
    public MailClient() throws VException {
        super();
    }

    /** Returns address of the mail host.
     *
     * @return java.lang.String */
    public String getMailHost() {
        return mailHost;
    }

    /** Sends the specified message via the configured mail host.
     *
     * @param inMessage {@link VMultiPartMessage}
     * @throws NamingException */
    public void sendMail(final VMultiPartMessage inMessage) throws MessagingException, NamingException {
        // pre: at least a primary receiver has to be specifed
        if (inMessage.getToAddresses() == null || inMessage.getToAddresses().length == 0) {
            throw new MessagingException("Primary Receivers (TO) not specified ");
        }

        Transport.send(createMessage(inMessage));
    }

    /** Sends the specified message asynchronously via the configured mail host.
     *
     * @param inMessage {@link VMultiPartMessage}
     * @param inCallback {@link ExceptionCallback} the callback to excecute in case of an error
     * @throws MessagingException
     * @throws NamingException */
    public void sendMail(final VMultiPartMessage inMessage, final ExceptionCallback inCallback)
            throws MessagingException, NamingException {
        // pre: at least a primary receiver has to be specifed
        if (inMessage.getToAddresses() == null || inMessage.getToAddresses().length == 0) {
            throw new MessagingException("Primary Receivers (TO) not specified ");
        }

        final MimeMessage lMessage = createMessage(inMessage);
        final MailSender lSender = new MailSender(lMessage, inCallback);
        (new Thread(lSender)).start();
    }

    /** @param inMessage {@link VMultiPartMessage}
     * @return {@link MimeMessage}
     * @throws NamingException
     * @throws MessagingException */
    private MimeMessage createMessage(final VMultiPartMessage inMessage) throws NamingException, MessagingException {
        final Session lSession = createSession();
        final MimeMessage lMessage = new MimeMessage(lSession);
        lMessage.setRecipients(MimeMessage.RecipientType.TO, inMessage.getToAddresses());
        if (inMessage.getFromAddress() != null) {
            lMessage.setFrom(inMessage.getFromAddress());
        }
        if (inMessage.getCcAddresses() != null && inMessage.getCcAddresses().length != 0) {
            lMessage.setRecipients(MimeMessage.RecipientType.CC, inMessage.getCcAddresses());
        }
        if (inMessage.getBccAddresses() != null && inMessage.getBccAddresses().length != 0) {
            lMessage.setRecipients(MimeMessage.RecipientType.BCC, inMessage.getBccAddresses());
        }
        lMessage.setSubject(inMessage.getSubject());
        lMessage.setSentDate(new Date());
        lMessage.setContent(inMessage.getContent());
        lMessage.saveChanges();
        return lMessage;
    }

    private Session createSession() throws NamingException {
        final String lMailHost = getMailHost();
        // Try the mail host if we have one set manually.
        if (lMailHost != null && lMailHost.length() > 0) {
            final Properties lHostProps = new Properties();
            lHostProps.put("mail.smtp.host", getMailHost());
            return Session.getInstance(lHostProps);
        }
        // Else, get the mail host from the JNDI context.
        final Context lJNDIContext = new InitialContext();
        final Context lEnvironment = (Context) lJNDIContext.lookup("java:comp/env");
        return (Session) lEnvironment.lookup(JNDI_RESOURCE_NAME);
    }

    /** Sets the mail host Address.
     *
     * @param inMailHost java.lang.String */
    public void setMailHost(final String inMailHost) {
        mailHost = inMailHost;
    }

    /** @param java.lang.String */
    @Override
    public String toString() {
        return Debug.classMarkupString(this, "mailHost='" + (mailHost == null ? "null" : mailHost) + "'");
    }

    // ---

    /** Helper class to send the mail asynchronously. */
    private static class MailSender implements Runnable {
        private final MimeMessage message;
        private final ExceptionCallback callback;

        protected MailSender(final MimeMessage inMessage, final ExceptionCallback inCallback) {
            message = inMessage;
            callback = inCallback;
        }

        @Override
        public void run() {
            try {
                Transport.send(message);
            } catch (final MessagingException exc) {
                callback.handleException(exc);
            }
        }
    }

}
