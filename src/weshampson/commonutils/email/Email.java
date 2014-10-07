/*
 * Copyright (C) 2014 Wes Hampson.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package weshampson.commonutils.email;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import weshampson.commonutils.logging.Level;
import weshampson.commonutils.logging.Logger;

/**
 *
 * @author  Wes Hampson
 * @version 0.1.0 (Aug 30, 2014)
 * @since   0.1.0 (Aug 30, 2014)
 */
public class Email {
    public static void sendEmail(EmailProvider emailProvider, String emailRecipient, final String emailUsername, final String emailPassword, String emailSubject, String emailText) throws AddressException, MessagingException {
        Logger.log(Level.INFO, "Provider: " + emailProvider.getDomainName());
        Logger.log(Level.INFO, "Username: " + emailUsername);
        Properties emailProperties = new Properties();
        emailProperties.put("mail.smtp.auth", "true");
        emailProperties.put("mail.smtp.host", emailProvider.getSMTPAddress());
        emailProperties.put("mail.smtp.port", Integer.toString(emailProvider.getSMTPPort()));
        emailProperties.put("mail.smtp.starttls.enable", "true");
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return(new PasswordAuthentication(emailUsername, emailPassword));
            }
        };
        Session emailSession = Session.getInstance(emailProperties, authenticator);
        Message emailMessage = new MimeMessage(emailSession);
        emailMessage.setFrom(new InternetAddress(emailUsername));
        emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailRecipient));
        emailMessage.setSubject(emailSubject);
        emailMessage.setText(emailText);
        Transport.send(emailMessage);
        Logger.log(Level.INFO, "Email successfully sent to " + emailRecipient + ".");
    }
    public static enum EmailProvider {
        AOL("aol.com", "smtp.aol.com", 587),
        GMAIL("gmail.com", "smtp.gmail.com", 587),
        HOTMAIL("hotmail.com", "smtp.live.com", 587),
        LIVE("live.com", "smtp.live.com", 587),
        MSN("msn.com", "smtp.live.com", 587),
        OUTLOOK("outlook.com", "smtp.live.com", 587),
        YAHOO("yahoo.com", "smtp.mail.yahoo.com", 587);
        private final String domainName;
        private final String sMTPAddress;
        private final int sMTPPortTLS;
        private EmailProvider(String domainName, String smtpAddress, int smtpPortTLS) {
            this.domainName = domainName;
            this.sMTPAddress = smtpAddress;
            this.sMTPPortTLS = smtpPortTLS;
        }
        public String getDomainName() {
            return(domainName);
        }
        public String getSMTPAddress() {
            return(sMTPAddress);
        }
        public int getSMTPPort() {
            return(sMTPPortTLS);
        }
        @Override
        public String toString() {
            return("@" + domainName);
        }
    }
}