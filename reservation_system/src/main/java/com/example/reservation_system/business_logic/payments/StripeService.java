package com.example.reservation_system.business_logic.payments;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingRepository;
import com.example.reservation_system.business_logic.bookings.BookingStatus;
import com.example.reservation_system.business_logic.invoice.InvoiceService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/*
The Stripe Java library is a client/wrapper that makes it easy 
to integrate Stripe's payment services into your Java application.
 */

@Service 
public class StripeService {
    /*
    @Value - These annotations inject configuration values from external 
    configuration files (like application.properties or 
    application.yml) into your Java code so you
     don't hardcode sensitive data like API keys directly in your source code
     */
    @Value("${stripe.secret-key}")
    /* This key authenticates your server when making requests to Stripe's API. */
    private String stripeSecretKey;

    /*
    Webhook = "Reverse API Call"
    Normal API call (you → Stripe):
    Your Server  ──request──>  Stripe API
                <──response──
    Webhook (Stripe → you):
    Stripe  ──HTTP POST──>  Your Server
        (event notification)

    Payment processing is asynchronous - things happen later:
    Timeline:
    10:00 AM - Customer submits payment
    10:00 AM - Your server creates PaymentIntent
    10:00 AM - Returns "processing..." to customer
            ⏰ WAITING... (seconds to minutes)
    10:02 AM - Stripe finishes processing payment
    10:02 AM - Stripe sends WEBHOOK to your server ← THIS!
    10:02 AM - Your server updates order status
    */

    @Value("${stripe.webhook-sercret")
    private String webhookSecret;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    public StripeService(BookingRepository bookingRepository, PaymentRepository paymentRepository, InvoiceService invoiceService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
    }

    @PostConstruct 
    public void init() {
        Stripe.apiKey = stripeSecretKey; 
    }

    /**
     * Creates a Stripe PaymentIntent for a booking
     * @param booking The booking to create payment intent for
     * @return PaymentIntent client secret for frontend
     * @throws StripeException if Stripe API call fails
     */


    public String createPaymentIntent(Booking booking) throws StripeException {
         // Convert BigDecimal to cents (Stripe uses smallest currency unit)
         long amountInCents = booking.getTotal_amount()
         .multiply(BigDecimal.valueOf(100))
         .longValue();

         Map<String , String> metadata = createMetadata(booking);

         PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
             .setAmount(amountInCents)
             .setCurrency(booking.getCurrency().toLowerCase())
             .setAutomaticPaymentMethods(
                 PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                     .setEnabled(true)
                     .build()
             );

         for (Map.Entry<String, String> entry : metadata.entrySet()) {
             paramsBuilder.putMetadata(entry.getKey(), entry.getValue());
         }

         PaymentIntentCreateParams params = paramsBuilder.build();
         PaymentIntent paymentIntent = PaymentIntent.create(params);

         //Store payment intent ID in booking

         String paymentIntentId = paymentIntent.getId();
         String numericPart = paymentIntentId.replace("pi_" , "");

         try {
            booking.setPayment_intent_id(Long.parseLong(numericPart));
            bookingRepository.save(booking);
         }catch (NumberFormatException e) {
            throw new IllegalStateException("Failed to parse payment intent ID :" + paymentIntentId );
         }

         return paymentIntent.getClientSecret();
    }

     /**
     * Handles successful payment from webhook
     * @param paymentIntentId Stripe PaymentIntent ID
     * @return Updated booking
     */

     public Booking handlePaymentSuccess(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        //Find booking by payment intent ID
        String paymentIntentIdStr = paymentIntent.getId();
        Long paymentIntentIdLong = Long.parseLong(paymentIntentIdStr.replace("pi_" , ""));

        Booking booking = bookingRepository.findAll().stream()
            .filter(b -> b.getPayment_intent_id() != null && 
                    b.getPayment_intent_id().equals(paymentIntentIdLong))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Booking not found for payment intent: " + paymentIntentId));
     
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdated_at(java.time.LocalDate.now());
        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setProvider("stripe");
        payment.setProvider_ref(paymentIntentId);
        payment.setAmount(paymentIntent.getAmount().intValue());
        payment.setStatus(PayementStatus.COMPLETED);
        payment.setCreated_at(java.time.LocalDate.now());
        payment.setBooking(booking);
        paymentRepository.save(payment);

        // Generate professional PDF invoice (idempotent)
        try {
            invoiceService.createInvoiceForBooking(booking);
        } catch (Exception e) {
            // Log but do not fail webhook; invoice can be generated later via API
        }

        return booking;
    }

    /*
     * Handles failed payment from webhook
     * @param paymentIntentId Stripe PaymentIntent ID
    */

    public void handlePaymentFailure(String paymentIntedId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntedId);

        String paymentIntendIdStr = paymentIntent.getId();
        Long paymentIntendIdLong = Long.parseLong(paymentIntendIdStr.replace("pi_" , "" ));

        Booking booking = bookingRepository.findAll().stream()
            .filter(b -> b.getPayment_intent_id() != null && 
                    b.getPayment_intent_id().equals(paymentIntendIdLong))
            .findFirst()
            .orElse(null);

        if(booking != null) {
            Payment payment = new Payment();
            payment.setProvider("stripe");
            payment.setProvider_ref(paymentIntedId);
            payment.setAmount(paymentIntent.getAmount().intValue());
            payment.setStatus(PayementStatus.REJECTED);
            payment.setCreated_at(java.time.LocalDate.now());
            payment.setBooking(booking);
            paymentRepository.save(payment);
        }
    }

    private Map<String , String> createMetadata(Booking booking) {
        Map<String , String> metadata = new HashMap<>();
        metadata.put("booking_id" , booking.getId().toString());
        metadata.put("user_id" , booking.getAppUser().toString());
        metadata.put("check_in" , booking.getCheck_in().toString());
        metadata.put("check_out" , booking.getCheck_out().toString());
        return metadata;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}
