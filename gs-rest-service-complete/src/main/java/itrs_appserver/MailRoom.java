package itrs_appserver;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;


import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

import java.util.Properties;


public class MailRoom {

    private static final String SMTP_HOST_NAME = "smtp.hostedservice2.net";
    private static final String SMTP_AUTH_USER = "HelpdeskAutomation@itrsgroup.com";
    private static final String SMTP_AUTH_PWD  = "9AHNekkeJwUE7XD";

    public static void sendMail(String username) throws Exception{
       new MailRoom().test(username);
    }

    public void test(String username) throws Exception{
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.auth", "true");

        Authenticator auth = new SMTPAuthenticator();
        Session mailSession = Session.getDefaultInstance(props, auth);
        // uncomment for debugging infos to stdout
        // mailSession.setDebug(true);
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setContent("This is a test from " + username, "text/plain");
        message.setFrom(new InternetAddress("clee@itrsgroup.com"));
        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress("cmorley@itrsgroup.com"));

        transport.connect();
        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = SMTP_AUTH_USER;
           String password = SMTP_AUTH_PWD;
           return new PasswordAuthentication(username, password);
        }
    }
}
/*
public class MailRoom
{
   public static void main(String[] args)
   {    
      String to = "cmorley@itrsgroup.com";

      String from = "cmorleyr@itrsgroup.com";

      String host = "smtp.hostedservice2.net";

      Properties properties = System.getProperties();

      properties.setProperty("mail.smtp.host", host);

      Session session = Session.getDefaultInstance(properties);

      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

         // Set Subject: header field
         message.setSubject("Verification code for device");

         // Now set the actual message
         message.setText("This is actual message : code 123456");

         // Send message
         Transport.send(message);
         System.out.println("Sent message successfully....");
      }catch (MessagingException mex) {
         mex.printStackTrace();
      }
   }
}*/