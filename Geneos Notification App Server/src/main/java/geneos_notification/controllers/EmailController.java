package geneos_notification.controllers;

import java.net.InetAddress;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;


public class EmailController {

    private static String SMTP_HOST_NAME; 
    private static String SMTP_AUTH_USER; 
    private static String SMTP_AUTH_PWD;

    public static void sendMail(String username, int random, String android_id) throws Exception{
       new EmailController().test(username, random, android_id);
    }

    public void test(String username, int random, String android_id) throws Exception{
        InetAddress Inet = InetAddress.getLocalHost();
        String IP = Inet.getHostAddress();

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
        message.setSubject("Geneos Notifier Device Verification");
        message.setContent("Hello " +username+ ", \n Please follow the attached link to verify you device \n\n http://" + IP + 
        		":8080/verifydev?dev_id=" + android_id + "&verification="+random, "text/plain");
        message.setFrom(new InternetAddress("helpdeskautomation@itrsgroup.com"));
        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress(username));

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
    
    public static void setDetails(String H, String U, String P)
    {
    	SMTP_HOST_NAME = H;
    	SMTP_AUTH_USER = U;
    	SMTP_AUTH_PWD = P;
    }
}