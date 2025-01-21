package com.knowledgeVista.Email;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgeVista.User.Muser;
import com.knowledgeVista.User.Repository.MuserRepositories;
import com.knowledgeVista.User.SecurityConfiguration.JwtUtil;



@RestController
@CrossOrigin
public class EmailController {
	  @Autowired
	    private EmailService emailService;
	  @Autowired
	  private MailkeysRepo mailkeyrepo;
	  @Autowired
		private MuserRepositories muserRepository;
		 @Autowired
		 private JwtUtil jwtUtil;
	  	 private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

		 

		//  @GetMapping("/logs")
        //  private ResponseEntity<?> sendlogfile() {
        //      try {
        //   	   List<String> to = Arrays.asList("akshayalatha786@gmail.com");
     	//         List<String> cc = new ArrayList<>(); // No CC
     	//         List<String> bcc = new ArrayList<>(); // No BCC
     	//         String subject = "Log";
     	//         String body = "Please find the attached log file.";
   	        
     	//         return emailService.sendHtmlEmail("Aks", to, cc, bcc, subject, body);
   	          
        //      } catch (Exception e) {
        //     	 e.printStackTrace();    logger.error("", e);;
        //          return null;
        //      }
        //  }
//	  	 public ResponseEntity<?> sendEmail( String email, Meeting Meet) {
//		      try {
//		    	 
//			         String institutionName = muserRepository.findinstitutionByEmail(email);
//			         if(institutionName==null || institutionName.isEmpty()) {
//			        	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("InstitutionName Not Found");
//			         }
//			        	 return emailService.sendHtmlEmail(institutionName,
//			   	              Meet.getSettings().getMeetingInvitees();
//			   	              emailRequest.getSubject(),
//			   	              emailRequest.getBody()
//			   	          );
//			         }
//			        
//		          
//		      } catch (Exception e) {
//		          e.printStackTrace();    logger.error("", e);;
//		          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//		      }
//		  }

	  public ResponseEntity<?> sendMail( String token, EmailRequest emailRequest) {
	      try {
	    	  if (!jwtUtil.validateToken(token)) {
		             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		         }
		         String email = jwtUtil.getUsernameFromToken(token);
		         Optional<Muser> optionalUser = muserRepository.findByEmail(email);
		         if(optionalUser.isPresent()) {
		        	 Muser user=optionalUser.get();
		        	 String institutionName=user.getInstitutionName();
		        	 return emailService.sendHtmlEmail(institutionName,
		   	              emailRequest.getTo(),
		   	              emailRequest.getCc(),
		   	              emailRequest.getBcc(),
		   	              emailRequest.getSubject(),
		   	              emailRequest.getBody()
		   	          );
		         }else {
		        	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
		         }
		        
	          
	      } catch (Exception e) {
	          e.printStackTrace();    logger.error("", e);;
	          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	      }
	  }
	  
	  public ResponseEntity<?>getMailkeys( String token){
		   try {
	    	   if (!jwtUtil.validateToken(token)) {
		             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		         }
	    	   String email = jwtUtil.getUsernameFromToken(token);
		         Optional<Muser>opmuser=muserRepository.findByEmail(email);
		         if(opmuser.isPresent()) {
		        	 Muser user=opmuser.get();
		        	 String Institution=user.getInstitutionName();
		        	 Optional<Mailkeys> opkeys=mailkeyrepo.FindMailkeyByInstituiton(Institution);
		        	 if(opkeys.isPresent()) {
		        		 return ResponseEntity.ok(opkeys); 
		        	 }else {
		        		 return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		        	 }
		        	
		        	 
		         }else {
		        	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		         }
	    	  
	      }
	      
	       catch (Exception e) {
	          e.printStackTrace();    logger.error("", e);;
	          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	      
	      }
	  }
	  public ResponseEntity<?>UpdateMailkeys( String token, Mailkeys mailkeys){
		  try {
	    	   if (!jwtUtil.validateToken(token)) {
		             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		         }
	    	   String role = jwtUtil.getRoleFromToken(token);
		         if(!role.equals("ADMIN")) {
		        	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
		         }
		         mailkeyrepo.save(mailkeys);
		         return ResponseEntity.ok("updated");
		  } catch (Exception e) {
	          e.printStackTrace();    logger.error("", e);;
	          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	      
	      }
	  }
	  
	  public ResponseEntity<?> saveMail( String token, Mailkeys mailkeys) {
	      try {
	    	   if (!jwtUtil.validateToken(token)) {
		             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		         }

		         String role = jwtUtil.getRoleFromToken(token);
		         if(!role.equals("ADMIN")) {
		        	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
		         }
		         String email = jwtUtil.getUsernameFromToken(token);
		         Optional<Muser>opmuser=muserRepository.findByEmail(email);
		         if(opmuser.isPresent()) {
		        	 Muser user=opmuser.get();
		        	 String Institution=user.getInstitutionName();
		        	 mailkeys.setInstitution(Institution);
		            Mailkeys mailkey= mailkeyrepo.save(mailkeys);
		        	 return ResponseEntity.ok(mailkey);
		         }else {
		        	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		         }
	    	  
	      }
	      
	       catch (Exception e) {
	          e.printStackTrace();    logger.error("", e);;
	          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	      
	      }
	  }
	  
}	  