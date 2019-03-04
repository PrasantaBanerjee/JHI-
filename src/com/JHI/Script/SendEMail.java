package com.JHI.Script;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class SendEMail {
public static void main(String args[]) throws EmailException {
	
	  // Create the attachment
	  EmailAttachment attachment = new EmailAttachment();
	  attachment.setPath(".\\Reports\\nn.txt");
	  attachment.setDisposition(EmailAttachment.ATTACHMENT);
	  attachment.setDescription("Report");
	  attachment.setName("Test");

	  // Create the email message
	  MultiPartEmail email = new MultiPartEmail();
	  email.setHostName("smtp.googlemail.com");
	  email.setSmtpPort(465);
	  email.setAuthenticator(new DefaultAuthenticator("prasanta.banerjee77@gmail.com", "GOODkid77"));
	  email.setSSLOnConnect(true);
	  email.addTo("xyz@test.com");
	  email.setFrom("prasanta.banerjee77@gmail.com");
	  email.setSubject("Test Execution Status");
	  email.setMsg("Hi, PFA the Test Execution Report.");

	  // add the attachment
	  email.attach(attachment);

	  // send the email
	  email.send();
	  
	  System.out.println("Mailsent");
}
}
