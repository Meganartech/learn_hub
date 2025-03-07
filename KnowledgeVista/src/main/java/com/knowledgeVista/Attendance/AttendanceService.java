package com.knowledgeVista.Attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.knowledgeVista.Attendance.Repo.AttendanceRepo;
import com.knowledgeVista.User.Repository.MuserRepositories;
import com.knowledgeVista.User.SecurityConfiguration.JwtUtil;


@Service
public class AttendanceService {
	@Autowired
	private AttendanceRepo attendanceRepo;
	 @Autowired
	 private JwtUtil jwtUtil;
	 @Autowired
		private MuserRepositories muserRepository;
	private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

	
	public ResponseEntity<?> getAttendance(String token, Long userId, Pageable pageable) {
	    try {
	        if (!jwtUtil.validateToken(token)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
	        }
	        String role = jwtUtil.getRoleFromToken(token);
	        if (userId == null) {
	            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	        }
	        if ("ADMIN".equals(role) || "TRAINER".equals(role)) {
	            Page<AttendanceDto> attendancePage = attendanceRepo.findAttendanceByUserId(userId, pageable);
	            double percentage = calculateAttendance(userId);
	            Map<String, Object> response = new HashMap<>();
	            response.put("attendance", attendancePage);
	            response.put("percentage", percentage);
	            return ResponseEntity.ok(response);
	        } else {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Students Cannot access This Page");
	        }
	    } catch (Exception e) {
	        logger.error("Error at getAttendance in AttendanceService for user ID {}: {}", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching attendance data");
	    }
	}
	

	  private double calculateAttendance(Long userId) {
		  try {
			  Long totalOccurance=attendanceRepo.countClassesForUser(userId);
			  Long presentCount=attendanceRepo.countClassesPresentForUser(userId);
			  if (totalOccurance == null || totalOccurance == 0) {
				    return 0.0; // Avoid division by zero
				}

				double percentage = ((double) presentCount / totalOccurance) * 100;
				return percentage;
			  
		  }catch (Exception e) {
			
			  logger.error("error calculating AttendancePercentage"+e);
			  return -1.0;
		}
	  }
public ResponseEntity<?>updateAttendance(String token,Long id,String status){
	try {
		if (!jwtUtil.validateToken(token)) {
   		 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
   	 }  	
        String role=jwtUtil.getRoleFromToken(token); 
        if("ADMIN".equals(role)||"TRANER".equals(role)) {
        	Optional<Attendancedetails>opattendance= attendanceRepo.findById(id);
        	if(opattendance.isPresent()) {
        		Attendancedetails attendance=opattendance.get();
        		if("PRESENT".equals(status)||"ABSENT".equals(status)) {
        		attendance.setStatus(status);
        		}
        		attendanceRepo.save(attendance);
        		return ResponseEntity.ok("saved");
        	}else {
        		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Attendance Not Found");
        	}
        }else {
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Student cannot access this Page");
        }
	}catch (Exception e) {
		// TODO: handle exception
		logger.error("Error Updating Attendance"+e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}
}
public ResponseEntity<?> getMyAttendance(String token, Pageable pageable) {
    try {
    	 if (!jwtUtil.validateToken(token)) {
	    		 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
	    	 }  	
	         String role=jwtUtil.getRoleFromToken(token); 
	         if("USER".equals(role)) {
	        	 String email=jwtUtil.getUsernameFromToken(token);
	        	 Long userId=muserRepository.findidByEmail(email);
	        	 if(userId==null) {
	        		 return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	        	 }
	        	 Page<AttendanceDto> attendancePage = attendanceRepo.findAttendanceByUserId(userId, pageable);		          
	        	 double percentage = calculateAttendance(userId);
		            Map<String, Object> response = new HashMap<>();
		            response.put("attendance", attendancePage);
		            response.put("percentage", percentage);
		            return ResponseEntity.ok(response);
	         }else
	         {return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Only Students Can access This Page");      
	    	 
	         }
    } catch (Exception e) {
        logger.error("Error at getAttendance in AttendanceService for user ID {}: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching attendance data");
    }
}
}
