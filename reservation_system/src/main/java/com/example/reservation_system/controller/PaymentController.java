package com.example.reservation_system.controller;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingService;
import com.example.reservation_system.business_logic.payments.StripeService;
import com.example.reservation_system.model.AppUser;
import com.example.reservation_system.model.AppUserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Setter
@Getter
@RestController 
@RequestMapping("/api/payments")
public class PaymentController {
    private final StripeService stripeService ;
    private final BookingService bookingService;
    private final AppUserService appUserService;

    public PaymentController(StripeService stripeService ,  BookingService bookingService , AppUserService appUserService) {
        this.stripeService =  stripeService;
        this.bookingService  = bookingService ;
        this.appUserService = appUserService;
    }

    /*
    * Creates a Stripe PaymentIntent for a booking
     * POST /api/payments/create-intent
    */

    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent (
        @Valid @RequestBody CreatePaymentIntentRequest request) {

        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            AppUser currentUser = appUserService.findByUsername(username )
                .orElseThrow( () -> new RuntimeException("User name not found"));
                
            Booking booking = bookingService.findById(request.getBookingId());

            if (!booking.getAppUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            };

            // Verify booking is in PENDING_PAYMENT status
            if(booking.getStatus() != com.example.reservation_system.business_logic.bookings.BookingStatus.PENDING_PAYMENT) {
                return ResponseEntity.badRequest() 
                    .body(new CreatePaymentIntentResponse(null , "Booking is not pending payment status"));
            }

            //Create payment intent
            String clientSecret = stripeService.createPaymentIntent(booking);

            return  ResponseEntity.ok(new CreatePaymentIntentResponse(clientSecret , null));
        }catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CreatePaymentIntentResponse(null , "Failed to create payment intent" + e.getMessage()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) 
                .body(new CreatePaymentIntentResponse(null , "Error " + e.getMessage()));
        }
    }


     /**
     * Handles Stripe webhook events
     * POST /api/payments/webhook
     */
     @PreAuthorize("permitAll()")
     @PostMapping("/webhook")
     public ResponseEntity<String> handleWebhook(
        @RequestBody String payload , 
        @RequestHeader("Stripe-Signature") String sigHeader) {

        String webhookSecret = stripeService.getWebhookSecret();

        if (webhookSecret == null || webhookSecret.isEmpty()) {
            return handleWebhookEvent(payload);
        }

        Event event; 

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        }catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid signature");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error processing webhook " + e.getMessage());
        }
        return handleWebhookEvent(event);
    }

    private ResponseEntity<String> handleWebhookEvent(String payload) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) 
            .body("Webhook secret must be configured for webhook processing");
    }

    private ResponseEntity<String> handleWebhookEvent(Event event) {
        // Handle the event
        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

            if (paymentIntent != null) {
                try {
                    stripeService.handlePaymentSuccess(paymentIntent.getId());
                    return ResponseEntity.ok("Payment succeeded");
                }catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error handling payment success: " +  e.getMessage());
                }
            }
        }else if ("payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject().orElse(null);

            if (paymentIntent != null) {
                try {
                    stripeService.handlePaymentFailure(paymentIntent.getId());
                    return ResponseEntity.ok("Payment failed handle");
                }catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error handling payment failure: " + e.getMessage());
                }
            }
        }

        return ResponseEntity.ok("Event received");
    }

    public static class CreatePaymentIntentRequest {
        @Setter
        @Getter 
        private Long bookingId ;
    }

    public static class CreatePaymentIntentResponse {
        @Getter
        @Setter 
        private String clientSecret;

        @Getter 
        @Setter 
        private String error;

        public CreatePaymentIntentResponse (String clientSecret ,  String error) {
            this.clientSecret = clientSecret;
            this.error = error;
        }
    }
}
