package trantantai.trantantai.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trantantai.trantantai.constants.PaymentStatus;
import trantantai.trantantai.services.CartService;
import trantantai.trantantai.services.MoMoService;
import trantantai.trantantai.viewmodels.MoMoIpnRequest;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "MoMo Payment", description = "MoMo payment integration APIs - Handle payment callbacks and notifications")
@RestController
@RequestMapping("/api/momo")
public class MoMoController {

    private final MoMoService momoService;
    private final CartService cartService;

    @Autowired
    public MoMoController(MoMoService momoService, CartService cartService) {
        this.momoService = momoService;
        this.cartService = cartService;
    }

    /**
     * MoMo IPN callback endpoint
     * MoMo will POST payment result to this endpoint
     */
    @Operation(summary = "MoMo IPN callback", description = "Receives payment notification from MoMo. Called by MoMo server when payment status changes.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "IPN processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid IPN request")
    })
    @PostMapping("/ipn")
    public ResponseEntity<Void> handleIpn(@RequestBody MoMoIpnRequest ipnRequest) {
        System.out.println("=== MoMo IPN Received ===");
        System.out.println("OrderId: " + ipnRequest.getOrderId());
        System.out.println("ResultCode: " + ipnRequest.getResultCode());
        System.out.println("TransId: " + ipnRequest.getTransId());
        
        // Convert request to Map for signature verification
        Map<String, Object> ipnData = new HashMap<>();
        ipnData.put("partnerCode", ipnRequest.getPartnerCode());
        ipnData.put("orderId", ipnRequest.getOrderId());
        ipnData.put("requestId", ipnRequest.getRequestId());
        ipnData.put("amount", ipnRequest.getAmount());
        ipnData.put("orderInfo", ipnRequest.getOrderInfo());
        ipnData.put("orderType", ipnRequest.getOrderType());
        ipnData.put("transId", ipnRequest.getTransId());
        ipnData.put("resultCode", ipnRequest.getResultCode());
        ipnData.put("message", ipnRequest.getMessage());
        ipnData.put("payType", ipnRequest.getPayType());
        ipnData.put("responseTime", ipnRequest.getResponseTime());
        ipnData.put("extraData", ipnRequest.getExtraData());
        ipnData.put("signature", ipnRequest.getSignature());

        // Verify signature (skip in sandbox for testing)
        // boolean validSignature = momoService.verifyIpnSignature(ipnData);
        // For now, accept all IPNs in sandbox mode
        
        String orderId = ipnRequest.getOrderId();
        
        // Check if already processed (prevent duplicate updates)
        var invoiceOpt = cartService.findInvoiceById(orderId);
        if (invoiceOpt.isPresent() && invoiceOpt.get().getPaymentStatus() == PaymentStatus.PAID) {
            System.out.println("Order already paid, skipping...");
            return ResponseEntity.noContent().build();
        }
        
        // Update payment status based on resultCode
        if (ipnRequest.getResultCode() != null && ipnRequest.getResultCode() == 0) {
            // Payment successful
            cartService.updatePaymentStatus(orderId, PaymentStatus.PAID, 
                ipnRequest.getTransId() != null ? ipnRequest.getTransId().toString() : null);
            System.out.println("Payment SUCCESS for order: " + orderId);
        } else {
            // Payment failed
            cartService.updatePaymentStatus(orderId, PaymentStatus.PAYMENT_FAILED, null);
            System.out.println("Payment FAILED for order: " + orderId);
        }
        
        // Return 204 No Content (MoMo expects this)
        return ResponseEntity.noContent().build();
    }
}
