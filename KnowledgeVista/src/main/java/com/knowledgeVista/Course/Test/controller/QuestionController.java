package com.knowledgeVista.Course.Test.controller;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.knowledgeVista.Course.CourseDetail;
import com.knowledgeVista.Course.Repository.CourseDetailRepository;
import com.knowledgeVista.Course.Test.CourseTest;
import com.knowledgeVista.Course.Test.MuserTestActivity;
import com.knowledgeVista.Course.Test.Question;
import com.knowledgeVista.Course.Test.Repository.MusertestactivityRepo;
import com.knowledgeVista.Course.Test.Repository.QuestionRepository;
import com.knowledgeVista.Course.Test.Repository.TestRepository;
import com.knowledgeVista.User.Muser;
import com.knowledgeVista.User.Repository.MuserRepositories;
import com.knowledgeVista.User.SecurityConfiguration.JwtUtil;

@RestController
public class QuestionController {
	 @Autowired
	 private JwtUtil jwtUtil;
	 @Autowired
	    private QuestionRepository questionRepository;
	 @Autowired
	    private TestRepository testrepo;
		@Autowired
		private MuserRepositories muserRepository;
		@Autowired
		private CourseDetailRepository coursedetailrepository;
		@Autowired
		private MusertestactivityRepo muserActivityRepo;
		
	  	 private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);


		public ResponseEntity<?> calculateMarks( List<Map<String, Object>> answers, Long courseId, String token) {
		   try {
		 

		    String email=jwtUtil.getUsernameFromToken(token);
	         String institution="";
		     Optional<Muser> opuser =muserRepository.findByEmail(email);
		     if(opuser.isPresent()) {
		    	 Muser user=opuser.get();
		    	 institution=user.getInstitutionName();
		    	 boolean adminIsactive=muserRepository.getactiveResultByInstitutionName("ADMIN", institution);
		   	    	if(!adminIsactive) {
		   	    	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("inactive institution");
		   	    	}
		     }else {
	             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not Found");
		     }

		    Optional<CourseDetail> opcourse = coursedetailrepository.findByCourseIdAndInstitutionName(courseId, institution);
		    if (opuser.isPresent() && opcourse.isPresent()) {
		        CourseDetail course = opcourse.get();
		        Muser user = opuser.get();
		        Optional<CourseTest> optest = testrepo.findByCourseDetail(course);

		        if (optest.isPresent()) {
		            CourseTest test = optest.get();

		            MuserTestActivity activity = new MuserTestActivity();
		            activity.setCourse(course);
		            activity.setUser(user);
		            activity.setTest(test);
		            activity.setTestDate(LocalDateTime.now().toLocalDate());

		            double passpercentage = test.getPassPercentage();
		            Long noofQuestion = test.getNoOfQuestions();

		            int totalMarks = 0;
		            for (Map<String, Object> answer : answers) {
		                long questionId = Long.parseLong(answer.get("questionId").toString());
		                String selectedAnswer = answer.get("selectedAnswer").toString();

		                Question question = questionRepository.findById(questionId)
		                        .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

		                if (question.getAnswer().equals(selectedAnswer)) {
		                    totalMarks++;
		                }
		            }

		            Double markacquired = ((double) totalMarks / noofQuestion) * 100;
		            activity.setPercentage(markacquired);
		            
		           Long count= muserActivityRepo.countByUser(user);
		           activity.setNthAttempt(count+1);
		            muserActivityRepo.save(activity);

		            String message;
		            String result;
		            if (markacquired >= passpercentage) {
		                message = "Congratulations! You passed the exam with " + Math.round(markacquired) + "%";
		                result = "pass";
		            } else {
		                message = "You have got " + Math.round(markacquired) + "%";
		                result = "fail";
		            }

		            Map<String, String> response = new HashMap<>();
		            response.put("message", message);
		            response.put("result", result);
		            return ResponseEntity.ok(response);
		        } else {
		            // Handle the case when the test is not present
		            return ResponseEntity.status(HttpStatus.NOT_FOUND ).body("Test Not Found");
		        }
		    } else {
		    	  return ResponseEntity.status(HttpStatus.NOT_FOUND ).body("User or Course Not Found");
		    }
		   }catch(Exception e) {
			   logger.error("", e);;
			   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		   }
		}

		public ResponseEntity<?> getQuestion( Long questionId, String token) {
	    	  try {
			        // Validate JWT token
			        if (!jwtUtil.validateToken(token)) {
			            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			        }
			        // Extract role from JWT token
			        String role = jwtUtil.getRoleFromToken(token);
			        String email=jwtUtil.getUsernameFromToken(token);
			         String institution="";
				     Optional<Muser> opuser =muserRepository.findByEmail(email);
				     if(opuser.isPresent()) {
				    	 Muser user=opuser.get();
				    	 institution=user.getInstitutionName();
				    	 boolean adminIsactive=muserRepository.getactiveResultByInstitutionName("ADMIN", institution);
				   	    	if(!adminIsactive) {
				   	    	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
				   	    	}
				     }else {
			             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
				     }
			        // Check if user has admin or trainer role
			        if ("ADMIN".equals(role) || "TRAINER".equals(role)) {
			        	Question existingQuestion = questionRepository.findById(questionId)
				                .orElse(null);
				     String questioninstitution=existingQuestion.getTest().getCourseDetail().getInstitutionName();
				        if ((!questioninstitution.equals(institution)) || (existingQuestion.equals(null))) {
				            return ResponseEntity.notFound().build();
				        }else {
				        	existingQuestion.setTest(null);
				        	return ResponseEntity.ok(existingQuestion);
				        }
			        }else {

			            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			        }
	    	  }catch (Exception e) {
			        // Handle any unexpected exceptions here
			        // You can log the error or return an appropriate response
	    		  e.printStackTrace();    logger.error("", e);;
			        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
			    }

	    }
	 
	 
		public ResponseEntity<?> deleteQuestion( Long questionId, String token) {
		    try {
		        // Validate JWT token
		        if (!jwtUtil.validateToken(token)) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		        }

		        // Extract role from JWT token
		        String role = jwtUtil.getRoleFromToken(token);
		        String email=jwtUtil.getUsernameFromToken(token);
		         String institution="";
			     Optional<Muser> opuser =muserRepository.findByEmail(email);
			     if(opuser.isPresent()) {
			    	 Muser user=opuser.get();
			    	 institution=user.getInstitutionName();
			    	 boolean adminIsactive=muserRepository.getactiveResultByInstitutionName("ADMIN", institution);
			   	    	if(!adminIsactive) {
			   	    	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			   	    	}
			     }else {
		             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			     }
		        // Check if user has admin or trainer role
		        if ("ADMIN".equals(role) || "TRAINER".equals(role)) {
		            // Fetch the question from the repository based on the provided ID
		            Question question = questionRepository.findById(questionId).orElse(null);
		            String questioninstitution=question.getTest().getCourseDetail().getInstitutionName();
		            
			        if ((questioninstitution.equals(institution)) || (!question.equals(null))) {
		            	Long noofques=question.getTest().getNoOfQuestions()-1;
		            	question.getTest().setNoOfQuestions(noofques);
		            	questionRepository.save(question);
		                questionRepository.delete(question);
		                // Return a response indicating successful deletion
		                return ResponseEntity.ok("Question with ID " + questionId + " deleted successfully");
		            } else {
		                // Return a response indicating that the question was not found
		                return ResponseEntity.notFound().build();
		            }
		        } else {
		            // Return unauthorized response if user does not have required role
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		        }
		    } catch (Exception e) {
		        // Handle any unexpected exceptions here
		        // You can log the error or return an appropriate response
		    	e.printStackTrace();    logger.error("", e);;
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
		    }
		}

	    //--------------------WORKING------
		public ResponseEntity<?> updateQuestion( Long questionId,String questionText,
							String option1, String option2,
							String option3, String option4,
							String answer, String token) {
		    try {
		        // Validate JWT token
		        if (!jwtUtil.validateToken(token)) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		        }
		        // Extract role from JWT token
		        String role = jwtUtil.getRoleFromToken(token);
		        String email=jwtUtil.getUsernameFromToken(token);
		         String institution="";
			     Optional<Muser> opuser =muserRepository.findByEmail(email);
			     if(opuser.isPresent()) {
			    	 Muser user=opuser.get();
			    	 institution=user.getInstitutionName();
			    	 boolean adminIsactive=muserRepository.getactiveResultByInstitutionName("ADMIN", institution);
			   	    	if(!adminIsactive) {
			   	    	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			   	    	}
			     }else {
		             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			     }
		        // Check if user has admin or trainer role
		        if ("ADMIN".equals(role) || "TRAINER".equals(role)) {
		            Question existingQuestion = questionRepository.findById(questionId)
		                    .orElse(null);
		            String questioninstitution=existingQuestion.getTest().getCourseDetail().getInstitutionName();
			        if ((!questioninstitution.equals(institution)) || (existingQuestion.equals(null))) {
		                return ResponseEntity.notFound().build();
		            }
		            existingQuestion.setQuestionText(questionText);
		            existingQuestion.setOption1(option1);
		            existingQuestion.setOption2(option2);
		            existingQuestion.setOption3(option3);
		            existingQuestion.setOption4(option4);
		            existingQuestion.setAnswer(answer);
		            questionRepository.save(existingQuestion);

		            // Return the updated Question object in the response
		            return ResponseEntity.ok().body("{\"message\": \"Question updated successfully\"}");
		        } else {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		        } 
		    } catch (Exception e) {
		        // Handle any unexpected exceptions here
		        // You can log the error or return an appropriate response
		    	e.printStackTrace();    logger.error("", e);;
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"" + e.getMessage() + "\"}");
		    }
		}
		
		
		public ResponseEntity<?> Addmore( Long testId, String questionText, String option1,
		        String option2,String option3, String option4,
		        String answer,String token) {
		    try {
		        // Validate JWT token
		        if (!jwtUtil.validateToken(token)) {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		        }
		        // Extract role from JWT token
		        String role = jwtUtil.getRoleFromToken(token);
		        String email=jwtUtil.getUsernameFromToken(token);
		         String institution="";
			     Optional<Muser> opuser =muserRepository.findByEmail(email);
			     if(opuser.isPresent()) {
			    	 Muser user=opuser.get();
			    	 institution=user.getInstitutionName();
			    	 boolean adminIsactive=muserRepository.getactiveResultByInstitutionName("ADMIN", institution);
			   	    	if(!adminIsactive) {
			   	    	 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			   	    	}
			     }else {
		             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			     }
		        // Check if user has admin or trainer role
		        if ("ADMIN".equals(role) || "TRAINER".equals(role)) {
		            CourseTest test = testrepo.findById(testId)
		                    .orElse(null);
		         String testinstitution= test.getCourseDetail().getInstitutionName();
		            if ((!testinstitution.equals(institution))||(test.equals(null))) {
		                return ResponseEntity.notFound().build();
		            }
		            test.setNoOfQuestions(test.getNoOfQuestions() +1);
		            Question ques=new Question();
		            ques.setQuestionText(questionText);
		            ques.setOption1(option1);
		            ques.setOption2(option2);
		            ques.setOption3(option3);
		            ques.setOption4(option4);
		            ques.setAnswer(answer);
		            ques.setTest(test);
		            questionRepository.save(ques);

		            // Return the updated Question object in the response
		            return ResponseEntity.ok().body("{\"message\": \"Question updated successfully\"}");
		        } else {
		            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		        } 
		    } catch (Exception e) {
		        // Handle any unexpected exceptions here
		        // You can log the error or return an appropriate response
		    	e.printStackTrace();    logger.error("", e);;
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"" + e.getMessage() + "\"}");
		    }
		}
}


