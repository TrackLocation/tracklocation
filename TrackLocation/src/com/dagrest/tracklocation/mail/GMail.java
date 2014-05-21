package com.dagrest.tracklocation.mail;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.dagrest.tracklocation.utils.CommonConst;

import android.util.Log;

public class GMail {
	final String emailPort = "587"; // gmail's smtp port
	final String smtpAuth = "true";
	final String starttls = "true";
	final String emailHost = "smtp.gmail.com";

	private String fromEmail;
	private String fromPassword;
	private List<String> toEmailList;
	private String emailSubject;
	private String emailBody;

	private Properties emailProperties;
	private Session mailSession;
	private MimeMessage emailMessage;

	public GMail() {

	}

	public GMail(String fromEmail, String fromPassword,
	  List<String> toEmailList, String emailSubject, String emailBody) {
		this.fromEmail = fromEmail;
		this.fromPassword = fromPassword;
		this.toEmailList = toEmailList;
		this.emailSubject = emailSubject;
		this.emailBody = emailBody;
	
		emailProperties = System.getProperties();
		emailProperties.put("mail.smtp.port", emailPort);
		emailProperties.put("mail.smtp.auth", smtpAuth);
		emailProperties.put("mail.smtp.starttls.enable", starttls);
		Log.i(CommonConst.LOG_TAG, "Set mail server properties.");
	}

	public MimeMessage createEmailMessage() throws AddressException,
	  MessagingException, UnsupportedEncodingException {

		mailSession = Session.getDefaultInstance(emailProperties, null);
		emailMessage = new MimeMessage(mailSession);
	
		emailMessage.setFrom(new InternetAddress(fromEmail, fromEmail));
		for (String toEmail : toEmailList) {
			Log.i(CommonConst.LOG_TAG,"ToEmail: " + toEmail);
			emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
		}

		emailMessage.setSubject(emailSubject);
		emailMessage.setContent(emailBody, "text/html"); // for a html email
		// emailMessage.setText(emailBody); // for a text email
		return emailMessage;
	}

	public void sendEmail() throws AddressException, MessagingException {
		Transport transport = mailSession.getTransport("smtp");
		transport.connect(emailHost, fromEmail, fromPassword);
		Log.i(CommonConst.LOG_TAG," all recipients: " + emailMessage.getAllRecipients());
		transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
		transport.close();
		Log.i(CommonConst.LOG_TAG, "Email sent successfully.");
	}
}
