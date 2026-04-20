package com.thinkschool.coach.service;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.thinkschool.coach.constants.FileLocation;
import com.thinkschool.coach.utility.FileReader;

import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

@Service
public class MailService {
	private static final Logger logger = LoggerFactory.getLogger(MailService.class);
	
	private static final String fromEmail = System.getenv("EMAIL_ID");
	private static final String password = System.getenv("EMAIL_PASSWORD");
	
	public void sendMail(String userName,String toEmail, byte[] report) {
		 // SMTP config
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject("Communication Analysis Report Generated");

            // Text body
            MimeBodyPart textPart = new MimeBodyPart();
            String emailContent = FileReader.getMessage(FileLocation.MAIL_CONTENT);
            emailContent = emailContent.replace("${{name}}", userName);
            textPart.setText(emailContent);

            // PDF attachment from byte[]
            MimeBodyPart attachmentPart = new MimeBodyPart();

            ByteArrayDataSource dataSource =
                new ByteArrayDataSource(report, "application/pdf");

            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName("Communication Analysis Report.pdf");

            // Combine parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);

            logger.info("Email sent with Report to "+toEmail);

        } catch (Exception e) {
        	logger.error("Exception Occured : "+e.getMessage(),e);
        }
	}
}
