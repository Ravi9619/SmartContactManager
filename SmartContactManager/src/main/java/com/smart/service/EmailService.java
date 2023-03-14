package com.smart.service;


import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;


@Service
public class EmailService {
	
	public boolean sendEmail(String subject, String message, String to) {
		
		boolean f = false;
		//variablefor gmail
		String from = "dubeyravi9619@gmail.com";
		
		String host = "smtp.gmail.com";
		
		//get system propties
		Properties properties = System.getProperties();
		System.out.println("PROPERTIES "+properties);
		
		//setting important important information to properties object
		
		//host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");
		
		//Step 1: to get session object
		Session session = Session.getInstance(properties, new Authenticator() {

	            protected PasswordAuthentication getPasswordAuthentication() {

	                return new PasswordAuthentication("dubeyravi9619@gmail.com", "Ravi0723@");
	            }
	        });
		
		session.setDebug(true);
		
		//step2 : compose the message [text,multimedia]
		MimeMessage m = new MimeMessage(session);
		
		try {
			
			m.setFrom(new InternetAddress(from));
			
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			m.setSubject(subject);
			
			m.setText(message);
			
			Transport.send(m);
			
			System.out.println("Sent Success....");
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return f;
	}
	
}
