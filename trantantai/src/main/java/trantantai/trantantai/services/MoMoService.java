package trantantai.trantantai.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import trantantai.trantantai.config.MoMoConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class MoMoService {

    private final MoMoConfig momoConfig;
    private final RestTemplate restTemplate;

    @Autowired
    public MoMoService(MoMoConfig momoConfig) {
        this.momoConfig = momoConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Create MoMo payment request using direct API call
     * @param orderId Invoice ID from database
     * @param amount Amount in VND
     * @param orderInfo Description for the order
     * @return Map containing payUrl, qrCodeUrl, deeplink, resultCode etc.
     */
    public Map<String, Object> createPayment(String orderId, long amount, String orderInfo) throws Exception {
        String requestId = orderId + "_" + System.currentTimeMillis();
        String requestType = "captureWallet";
        String extraData = "";
        
        // Build raw signature data (alphabetically ordered)
        String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + momoConfig.getIpnUrl() +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + momoConfig.getPartnerCode() +
                "&redirectUrl=" + momoConfig.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=" + requestType;
        
        String signature = signHmacSHA256(rawSignature, momoConfig.getSecretKey());
        
        // Build request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", momoConfig.getPartnerCode());
        requestBody.put("partnerName", "BookHaven");
        requestBody.put("storeId", "BookHavenStore");
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", momoConfig.getReturnUrl());
        requestBody.put("ipnUrl", momoConfig.getIpnUrl());
        requestBody.put("lang", "vi");
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", requestType);
        requestBody.put("signature", signature);
        requestBody.put("autoCapture", true);
        
        // Make API call
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                momoConfig.getEndpoint(),
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                System.out.println("MoMo API Response: " + responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            System.err.println("MoMo API Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return null;
    }

    /**
     * Verify IPN signature from MoMo
     * @param ipnData Map of IPN request parameters
     * @return true if signature is valid
     */
    public boolean verifyIpnSignature(Map<String, Object> ipnData) {
        try {
            String receivedSignature = (String) ipnData.get("signature");
            if (receivedSignature == null) return false;

            // Build raw signature data in alphabetical order
            String rawData = "accessKey=" + momoConfig.getAccessKey() +
                "&amount=" + ipnData.get("amount") +
                "&extraData=" + (ipnData.get("extraData") != null ? ipnData.get("extraData") : "") +
                "&message=" + ipnData.get("message") +
                "&orderId=" + ipnData.get("orderId") +
                "&orderInfo=" + ipnData.get("orderInfo") +
                "&orderType=" + ipnData.get("orderType") +
                "&partnerCode=" + ipnData.get("partnerCode") +
                "&payType=" + ipnData.get("payType") +
                "&requestId=" + ipnData.get("requestId") +
                "&responseTime=" + ipnData.get("responseTime") +
                "&resultCode=" + ipnData.get("resultCode") +
                "&transId=" + ipnData.get("transId");

            String calculatedSignature = signHmacSHA256(rawData, momoConfig.getSecretKey());
            return calculatedSignature.equals(receivedSignature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Query transaction status from MoMo
     * @param orderId Order ID to query
     * @param requestId Original request ID (can be same as orderId if unknown)
     * @return Map containing resultCode, message, transId etc.
     */
    public Map<String, Object> queryTransactionStatus(String orderId, String requestId) {
        try {
            // Build raw signature data for query (alphabetically ordered)
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&orderId=" + orderId +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&requestId=" + requestId;
            
            String signature = signHmacSHA256(rawSignature, momoConfig.getSecretKey());
            
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", momoConfig.getPartnerCode());
            requestBody.put("requestId", requestId);
            requestBody.put("orderId", orderId);
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");
            
            // Make API call
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                momoConfig.getQueryEndpoint(),
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                System.out.println("MoMo Query Response: " + responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            System.err.println("MoMo Query Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    private String signHmacSHA256(String data, String secretKey) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Getters for config values
    public String getReturnUrl() { return momoConfig.getReturnUrl(); }
    public String getIpnUrl() { return momoConfig.getIpnUrl(); }
}
