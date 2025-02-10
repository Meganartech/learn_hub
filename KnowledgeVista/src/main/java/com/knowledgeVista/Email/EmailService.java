package com.knowledgeVista.Email;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	  
	  @Autowired
	  private MailkeysRepo mailkeyrepo;
	  
	  public ResponseEntity<?> sendHtmlEmail(String InstitutionName,List<String> to, List<String> cc, List<String> bcc, String subject, String body) throws MessagingException {
		 System.out.println("hereee..in send mail");
		  JavaMailSender mailSender = getJavaMailSender(InstitutionName);
          if(mailSender==null) {
        	  System.out.println("hereee..in mailsender not found mail");
        	  return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
          }
		  MimeMessage mimeMessage = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
	        String from= this.getfrom(InstitutionName);
	        if(from==null) {
	        	 return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	        }
           helper.setFrom(from);
	        if (to != null && !to.isEmpty()) {
	            helper.setTo(to.toArray(new String[0]));
	        }

	        if (cc != null && !cc.isEmpty()) {
	            helper.setCc(cc.toArray(new String[0]));
	        }

	        if (bcc != null && !bcc.isEmpty()) {
	            helper.setBcc(bcc.toArray(new String[0]));
	        }

	        helper.setSubject(subject);
	        helper.setText(body, true);  

	        mailSender.send(mimeMessage);
	        System.out.println("Sendeded"+to);
	       return ResponseEntity.ok("Mail Sent");
	    }
	  public String getfrom(String institution) {
		  Optional<Mailkeys> opkeys = mailkeyrepo.FindMailkeyByInstituiton(institution);
		  if(opkeys.isPresent()) {
			  Mailkeys keys =opkeys.get();
			  return keys.getEmailid();
		  } else {
			    return null;   
			    }
	  }
	  
	  public JavaMailSender getJavaMailSender(String institution) {
		  Optional<Mailkeys> opkeys = mailkeyrepo.FindMailkeyByInstituiton(institution);
		  if(opkeys.isPresent()) {
			  Mailkeys keys =opkeys.get();
			  JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		        mailSender.setHost(keys.getHostname()); // Set host from Mailkeys
		        mailSender.setPort(Integer.parseInt(keys.getPort())); // Set port
		        
		        mailSender.setUsername(keys.getEmailid()); // Set username (email ID)
		        mailSender.setPassword(keys.getPassword()); // Set password

		        // Optional properties for TLS/SSL, protocol, etc.
		        Properties props = mailSender.getJavaMailProperties();
		        props.put("mail.transport.protocol", "smtp");
		        props.put("mail.smtp.auth", "true");
		        props.put("mail.smtp.starttls.enable", "true");
		        props.put("mail.debug", "true"); // Optional, set to true for debugging
		        
		        return mailSender;  // Return the configured mail sender
		    } else {
		    return null;   
		    }
	  }
	  
}
