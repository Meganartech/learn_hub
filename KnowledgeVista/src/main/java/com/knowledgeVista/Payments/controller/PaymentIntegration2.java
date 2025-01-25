package com.knowledgeVista.Payments.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.paypal.orders.*;
import com.paypal.core.PayPalHttpClient;
import com.paypal.core.PayPalEnvironment;
import com.paypal.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.knowledgeVista.Batch.Batch;
import com.knowledgeVista.Batch.Repo.BatchRepository;
import com.knowledgeVista.Course.CourseDetail;
import com.knowledgeVista.Course.Repository.CourseDetailRepository;
import com.knowledgeVista.Notification.Service.NotificationService;
import com.knowledgeVista.Payments.Course_PartPayment_Structure;
import com.knowledgeVista.Payments.InstallmentDetails;
import com.knowledgeVista.Payments.Orderuser;
import com.knowledgeVista.Payments.Paypalsettings;
import com.knowledgeVista.Payments.repos.OrderuserRepo;
import com.knowledgeVista.Payments.repos.partpayrepo;
import com.knowledgeVista.Payments.repos.paypalrepo;
import com.knowledgeVista.User.Muser;
import com.knowledgeVista.User.Repository.MuserRepositories;
import com.knowledgeVista.User.SecurityConfiguration.JwtUtil;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.PurchaseUnitRequest;
import jakarta.servlet.http.HttpServletRequest;
@Service
public class PaymentIntegration2 {
	@Value("${currency}")
	private String currency;
	@Autowired
	private paypalrepo paypalrepo;
	@Autowired
	private partpayrepo partpayrepo;
	  @Value("${paypal.mode}")
	    private String paypalMode;
	  @Autowired
		private OrderuserRepo ordertablerepo;
		@Autowired
		private MuserRepositories muserRepository;
		@Autowired
		private NotificationService notiservice;
		
		@Autowired
		private CourseDetailRepository coursedetail;
		@Autowired
		private BatchRepository batchrepo;
		@Autowired
		private JwtUtil jwtUtil;

	    private static final String API_URL = "https://v6.exchangerate-api.com/v6/7bd3206191151fb9958f2ae9/pair/INR/USD/1";

	    public double convertINRtoUSD(double amountInINR) {
	        try {
	            // Initialize RestTemplate to make the API call
	            RestTemplate restTemplate = new RestTemplate();

	            // Send GET request to the API and get the response as a String
	            String response = restTemplate.getForObject(API_URL, String.class);

	            // Parse the conversion rate directly from the response string
	            int startIdx = response.indexOf("conversion_rate") + 17;
	            int endIdx = response.indexOf(",", startIdx);
	            String conversionRateStr = response.substring(startIdx, endIdx).trim();

	            // Convert the conversion rate to a double
	            double conversionRate = Double.parseDouble(conversionRateStr);

	            // Convert INR to USD
	            double amountInUSD = amountInINR * conversionRate;

	            BigDecimal roundedAmount = new BigDecimal(amountInUSD).setScale(2, RoundingMode.HALF_UP);

	            // Print the conversion rate and the result
	            System.out.println("Conversion Rate (INR to USD): " + conversionRate);
	            System.out.println(amountInINR + " INR is approximately " + roundedAmount + " USD.");

	            // Return the rounded value as a double
	            return roundedAmount.doubleValue();

	        } catch (Exception e) {
	            // Handle any errors that might occur during the process
	        	logger.error(e.getMessage());
	            System.err.println("Error fetching conversion rate: " + e.getMessage());
	            return -1;  // Indicating failure
	        }
	    }
	private static final Logger logger = LoggerFactory.getLogger(PaymentIntegration.class);
	private Orderuser saveOrderDetails( String userName,String email, Long amt, String orderId, String status,
			String institution,  Long userId, Long installmentNumber,String PaymentType,String batchName,Long batchId) {
		Orderuser orderTable = new Orderuser();
		orderTable.setOrderId(orderId);
		orderTable.setUserId(userId);
		orderTable.setPaymentType(PaymentType);
		orderTable.setInstitutionName(institution);
		orderTable.setUsername(userName);
		orderTable.setBatchName(batchName);
		orderTable.setBatchId(batchId);
		orderTable.setEmail(email);
		orderTable.setInstallmentnumber(installmentNumber);
		orderTable.setDate(new java.util.Date());
		orderTable.setStatus(status);
		return ordertablerepo.save(orderTable);
	}

	public void notifiinstallment(Long courseId, Long userId) {
		Optional<CourseDetail> courseOptional = coursedetail.findById(courseId);
		Optional<Muser> optionalUser = muserRepository.findById(courseId);

		if (courseOptional.isPresent() && optionalUser.isPresent()) {
			CourseDetail course = courseOptional.get();
			Optional<Course_PartPayment_Structure> opPartpay = partpayrepo.findBycourse(course);
			if (opPartpay.isPresent()) {
				Course_PartPayment_Structure partpay = opPartpay.get();
				List<InstallmentDetails> installmentslist = partpay.getInstallmentDetail();
				int count = ordertablerepo.findCountByUserIDAndCourseID(userId, courseId, "paid");
				int installmentlength = installmentslist.size();
				if (installmentlength > count) {
					InstallmentDetails installment = installmentslist.get(count);
					Long Duration = installment.getDurationInDays();
					LocalDate startdate = LocalDate.now();
					LocalDate datetonotify = startdate.plusDays(Duration);
					String heading = " Installment Pending!";
					String link = "/dashboard/course";
					String notidescription = "Installment date Of " + course.getCourseName() + " for installment "
							+ installment.getInstallmentNumber() + " was pending";

					Long NotifyId = notiservice.createNotification("Payment", "system", notidescription, "system",
							heading, link);
					if (NotifyId != null) {
						List<Long> ids = new ArrayList<>();
						ids.add(userId);
						notiservice.SpecificCreateNotification(NotifyId, ids, datetonotify);
					}
				}
			}
		}

	}

	public ResponseEntity<?> handlePaypalCheckout(String userName,String email,Long userid ,String BatchName,Long batchId,String institutionName, Long amt,HttpServletRequest httpRequest) {
	    try {
	        // Fetch PayPal settings for the institution
	        Optional<Paypalsettings> opdataList = paypalrepo.FindByInstitutionName(institutionName);
	        if (opdataList.isPresent()) {
	            Paypalsettings data = opdataList.get();
	            String clientId = data.getPaypal_client_id();
	            String clientSecret = data.getPaypal_secret_key();

	            // Determine the mode: Use "live" or "sandbox" dynamically based on some condition
	            String mode = paypalMode;
	            
	            // Select environment dynamically based on mode
	            PayPalHttpClient paypalClient;
	            if ("live".equalsIgnoreCase(mode)) {
	                // Use Live Environment (for production)
	                PayPalEnvironment environment = new PayPalEnvironment.Live(clientId, clientSecret);
	                paypalClient = new PayPalHttpClient(environment);
	            } else {
	                // Use Sandbox Environment (for development)
	                PayPalEnvironment environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
	                paypalClient = new PayPalHttpClient(environment);
	            }

	            String clientBaseUrl = httpRequest.getHeader("Origin");
	            if (clientBaseUrl == null) {
	                String referer = httpRequest.getHeader("Referer");
	                if (referer != null) {
	                    System.out.println("Client Base URL: " + referer);
	                    clientBaseUrl = referer.split("/")[0] + "//" + referer.split("/")[2];
	                }
	            }

	            System.out.println("Client Base URL: " + clientBaseUrl);

	            // Define success and cancel URLs
	            String successUrl = clientBaseUrl + "/updatePaypalPayment";
	            String cancelUrl = clientBaseUrl + "/dashboard/course";
	            double amtfinal=amt;
	            // Create PayPal Order
	            OrderRequest orderRequest = new OrderRequest();
	            orderRequest.checkoutPaymentIntent("CAPTURE");
	            if(currency.equals("INR")) {
	            	amtfinal = this.convertINRtoUSD(amt); 
	            	if(amtfinal==-1) {
	            		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot Convert Amount to USD");
	            	}
	            }

	            // Set up purchase unit (product data and price)
	            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
	            	    .amountWithBreakdown(new AmountWithBreakdown().currencyCode("USD").value(String.valueOf(amtfinal)))
	            	    .description(BatchName);
	            orderRequest.purchaseUnits(Collections.singletonList(purchaseUnit));

	            ApplicationContext appContext = new ApplicationContext()
	                    .brandName(institutionName)
	                    .landingPage("BILLING")
	                    .cancelUrl(cancelUrl)
	                    .returnUrl(successUrl)
	                    .userAction("PAY_NOW");

	            orderRequest.applicationContext(appContext);

	            OrdersCreateRequest paypalOrderRequest = new OrdersCreateRequest().requestBody(orderRequest);
	            HttpResponse<Order> response = paypalClient.execute(paypalOrderRequest);

	            if (response.statusCode() == 201) {
	                Order order = response.result();
	                saveOrderDetails(userName, email, amt,order.id(), "CREATED",
							institutionName, userid,  0L,"PAYPAL",BatchName,batchId);
	                Map<String, String> approvalLink = new HashMap<>();
	                for (LinkDescription link : order.links()) {
	                    if ("approve".equals(link.rel())) {
	                        approvalLink.put("approvalUrl", link.href());
	                        approvalLink.put("orderid", order.id());
	                        break;
	                    }
	                }

	                return ResponseEntity.ok(approvalLink);
	            } else {
	                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: PayPal Order creation failed");
	            }
	        } else {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("PayPal Payment details not found");
	        }
	    } catch (Exception e) {
	    	 if (e.getMessage().contains("DECIMAL_PRECISION")) {
	    		 System.out.println(e.getMessage());
	    	        // Handle the case where there are too many decimals
	    	        logger.error("Error with decimal precision: The amount has too many decimal places.");
	    	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid amount format.");
	    	    }else {
	    	    	logger.error("Error creating Stripe Checkout: ", e);
	    			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Paypal keys are invalid. Please check your Paypal keys.");
	    	    }
	    }
	}
	
	private ResponseEntity<String> SetBatchToUser(Orderuser savedorder){
		Optional<Muser> optionalUser = muserRepository.findById(savedorder.getUserId());
		if(savedorder.getBatchId()==null) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("batchId Not Found");
		}
		
			Optional<Batch> opbatch= batchrepo.findById(savedorder.getBatchId());
		
		       
		if (optionalUser.isPresent() && opbatch.isPresent()) {
			Muser user = optionalUser.get();
			String institution = user.getInstitutionName();
			Batch batch = opbatch.get();
			// for notification
			List<Muser> trainers = batch.getTrainers();
			List<Long> ids = new ArrayList<>();

			for (Muser trainer : trainers) {
				ids.add(trainer.getUserId());
			}

			if (!user.getBatches().contains(batch)) {
				user.getBatches().add(batch);
				muserRepository.save(user);
			}
			if (!batch.getUsers().contains(user)) {
				batch.getUsers().add(user);
				batchrepo.save(batch);
			}
			
			String courseUrl = batch.getBatchUrl();
			String heading = " Payment Credited !";
			String link = "/payment/transactionHitory";
			String linkfortrainer = "/payment/trainer/transactionHitory";
			String notidescription = "The payment for Batch " + batch.getBatchTitle() + " was Credited  By "
					+ user.getUsername() + " for installment" + savedorder.getInstallmentnumber();

			Long NotifyId = notiservice.createNotification("Payment", user.getUsername(), notidescription,
					user.getEmail(), heading, link);
			Long NotifyIdfortrainer = notiservice.createNotification("Payment", user.getUsername(),
					notidescription, user.getEmail(), heading, linkfortrainer);

			if (NotifyId != null) {
				List<String> notiuserlist = new ArrayList<>();
				notiuserlist.add("ADMIN");
				notiservice.CommoncreateNotificationUser(NotifyId, notiuserlist, institution);
			}
			if (ids != null) {
				notiservice.SpecificCreateNotification(NotifyIdfortrainer, ids);
			}

			return ResponseEntity.ok().body(courseUrl);

		} else {
			String errorMessage = "";
			if (!optionalUser.isPresent()) {
				errorMessage += "User with ID " + savedorder.getUserId() + " not found. ";
			}
			if (!opbatch.isPresent()) {
				errorMessage += "Course with ID " + savedorder.getBatchId() + " not found. ";
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage.trim());
		}
		
	}

	public ResponseEntity<String> updatePayPalPayment(Map<String, String> requestData, String token) {
	    try {
	        String orderId = requestData.get("orderId");
	        String payerId = requestData.get("PayerID");
	        String paypalToken = requestData.get("token");

	        if (orderId == null || payerId == null || paypalToken == null) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order ID, PayerID, or Token is missing");
	        }

	        Optional<Orderuser> orderUserOptional = ordertablerepo.findByOrderId(orderId);
	        if (orderUserOptional.isPresent()) {
	            Orderuser orderUser = orderUserOptional.get();

	            // Create PayPalHttpClient instance
	            PayPalHttpClient client = createPayPalHttpClient(token);
                if(client==null) {
                	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error Getting Payment details");
                }
	            // Create request to retrieve payment details
	            OrdersGetRequest request = new OrdersGetRequest(paypalToken);
	            HttpResponse<Order> response = client.execute(request);

	            Order order = response.result();

	            if (order != null) {
	                // Extract details from order
	                String transactionId = order.id();
	                
	                // Parse the amount (String -> double)
	                String amountString = order.purchaseUnits().get(0).amountWithBreakdown().value();
	                double amountPaidInCents = Double.parseDouble(amountString) * 100; // Convert to cents (from dollars)
	                
	                // Get payment status
	                String status = order.status();

	                // Update the OrderUser entity with payment details
	                orderUser.setPaymentId(transactionId);
	                if(status.equals("APPROVED")) {
	                	 orderUser.setStatus("paid");	
	                }
	               
	                orderUser.setAmountReceived((int) amountPaidInCents); // Amount in cents
	                orderUser.setDate(new Date()); // Current date for simplicity

	                Orderuser savedOrder = ordertablerepo.save(orderUser);
	                return this.SetBatchToUser(savedOrder);

	               	            } else {
	                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("PayPal payment details not found");
	            }
	        } else {
	            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Order not found");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        logger.error("General Error: ", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Error updating payment ID: " + e.getMessage());
	    }
	}

	private PayPalHttpClient createPayPalHttpClient(String token) {
		String email=jwtUtil.getUsernameFromToken(token);
		if(email.equals(null)) {
			return null;
		}
		String institutionName=muserRepository.findinstitutionByEmail(email);
		if(institutionName.equals(null)) {
			return null;
		}
		Optional<Paypalsettings> keys=paypalrepo.FindByInstitutionName(institutionName);
		if(!keys.isPresent()) {
			return null;
		}
		Paypalsettings key=keys.get();
	    // Set up PayPal environment (Sandbox or Production)
	    PayPalEnvironment environment = new PayPalEnvironment.Sandbox(key.getPaypal_client_id(), key.getPaypal_secret_key());
	    return new PayPalHttpClient(environment);
	}}

