package geneos_notification.controllers;

import java.net.InetAddress;
import javax.mail.*;
import javax.mail.internet.*;

import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;


public class EmailController {

    private static String SMTP_HOST_NAME; 
    private static String SMTP_AUTH_USER; 
    private static String SMTP_AUTH_PWD;
    private static String IP;
    private static String PORT;
    private static String HT_PROTOCOL;
    static LtA logA = new LogObject();

    public static void sendMail(String username, int random, String android_id) throws Exception{
       new EmailController().send(username, random, android_id);
    }

    public void send(String username, int random, String android_id){
    	try{
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
        message.setContent("Hello " +username+ ", \n\n Please follow the attached link to verify you device \n\n " + HT_PROTOCOL + "://" + IP + 
        		":" + PORT + "/verifydev?dev_id=" + android_id + "&verification="+random, "text/plain");
        message.setFrom(new InternetAddress("helpdeskautomation@itrsgroup.com"));
        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress(username));

        transport.connect();
        transport.sendMessage(message,
            message.getRecipients(Message.RecipientType.TO));
        transport.close();
    	}
    	catch(Exception e){
    		logA.doLog("Email" , "[Email]Error in creation/transmission of Email to user : " + username + " \nError is : " + e.toString(), "Critical");
    	}
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           String username = SMTP_AUTH_USER;
           String password = SMTP_AUTH_PWD;
           return new PasswordAuthentication(username, password);
        }
    }
    
    public static void setDetails(String H, String U, String P, String address, String prt, boolean secured)
    {
    	SMTP_HOST_NAME = H;
    	SMTP_AUTH_USER = U;
    	SMTP_AUTH_PWD = P;
    	IP = address;
    	PORT = prt;
    	HT_PROTOCOL = "http";
    	if(secured)
    		HT_PROTOCOL = "https";
    }
}