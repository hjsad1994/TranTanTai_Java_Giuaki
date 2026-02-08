package trantantai.trantantai.controllers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import trantantai.trantantai.constants.PaymentMethod;
import trantantai.trantantai.constants.PaymentStatus;
import trantantai.trantantai.services.MoMoService;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.services.CartService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final MoMoService momoService;

    @Autowired
    public CartController(CartService cartService, MoMoService momoService) {
        this.cartService = cartService;
        this.momoService = momoService;
    }

    @GetMapping
    public String showCart(HttpSession session, @NotNull Model model) {
        // Validate cart and remove items of deleted books
        int removedCount = cartService.validateAndCleanCart(session);
        if (removedCount > 0) {
            model.addAttribute("warning", removedCount + " sản phẩm đã bị xóa khỏi giỏ hàng vì không còn tồn tại.");
        }

        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalPrice", cartService.getSumPrice(session));
        model.addAttribute("totalQuantity", cartService.getSumQuantity(session));
        return "book/cart";
    }

    @GetMapping("/removeFromCart/{id}")
    public String removeFromCartRedirect(HttpSession session, @PathVariable String id) {
        var cart = cartService.getCart(session);
        cart.removeItems(id);
        return "redirect:/cart";
    }

    @PostMapping("/removeFromCart/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(HttpSession session, @PathVariable String id) {
        var cart = cartService.getCart(session);
        cart.removeItems(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartCount", cartService.getSumQuantity(session));
        response.put("cartTotal", cartService.getSumPrice(session));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/updateCart/{id}/{quantity}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCart(HttpSession session,
                             @PathVariable String id,
                             @PathVariable int quantity) {
        var cart = cartService.getCart(session);
        cart.updateItems(id, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartCount", cartService.getSumQuantity(session));
        response.put("cartTotal", cartService.getSumPrice(session));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/updateQuantity")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(HttpSession session,
                             @RequestParam String id,
                             @RequestParam int quantity) {
        var cart = cartService.getCart(session);
        cart.updateItems(id, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartCount", cartService.getSumQuantity(session));
        response.put("cartTotal", cartService.getSumPrice(session));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/clearCart")
    public String clearCart(HttpSession session) {
        cartService.removeCart(session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Validate cart and remove items of deleted books
        int removedCount = cartService.validateAndCleanCart(session);
        if (removedCount > 0) {
            redirectAttributes.addFlashAttribute("warning",
                removedCount + " sản phẩm đã bị xóa khỏi giỏ hàng vì không còn tồn tại.");
        }

        var cart = cartService.getCart(session);

        // Check if cart is empty
        if (cart.getCartItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống. Vui lòng thêm sách trước khi thanh toán.");
            return "redirect:/cart";
        }

        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cartService.getSumPrice(session));
        model.addAttribute("totalQuantity", cartService.getSumQuantity(session));

        return "book/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(HttpSession session,
                            @RequestParam(defaultValue = "COD") String paymentMethod,
                            RedirectAttributes redirectAttributes) {
        var cart = cartService.getCart(session);

        // Check if cart is empty
        if (cart.getCartItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống. Không thể đặt hàng.");
            return "redirect:/cart";
        }

        // Handle COD payment (existing flow)
        if ("COD".equals(paymentMethod)) {
            cartService.saveCart(session, PaymentMethod.COD);
            redirectAttributes.addFlashAttribute("success",
                "Đặt hàng thành công! Bạn sẽ thanh toán khi nhận hàng.");
            return "redirect:/cart";
        }

        // Handle MOMO payment
        if ("MOMO".equals(paymentMethod)) {
            try {
                // Create pending invoice
                Invoice invoice = cartService.saveCart(session, PaymentMethod.MOMO);
                if (invoice == null) {
                    redirectAttributes.addFlashAttribute("error", "Không thể tạo đơn hàng.");
                    return "redirect:/cart";
                }

                // Create MoMo payment
                long amount = Math.round(invoice.getPrice());
                String orderInfo = "Thanh toán đơn hàng BookHaven #" + invoice.getId();
                
                var momoResponse = momoService.createPayment(invoice.getId(), amount, orderInfo);
                
                // Log full response for debugging
                System.out.println("=== MoMo Response ===");
                System.out.println(momoResponse);

                // Check resultCode from Map response
                Integer resultCode = momoResponse != null ? (Integer) momoResponse.get("resultCode") : null;
                String message = momoResponse != null ? (String) momoResponse.get("message") : "No response";
                
                if (resultCode != null && resultCode == 0) {
                    // Store payment info in session for display
                    session.setAttribute("momoOrderId", invoice.getId());
                    session.setAttribute("momoQrCodeUrl", momoResponse.get("qrCodeUrl"));
                    session.setAttribute("momoPayUrl", momoResponse.get("payUrl"));
                    session.setAttribute("momoDeeplink", momoResponse.get("deeplink"));
                    session.setAttribute("momoAmount", amount);
                    
                    // Log QR URL for debugging
                    System.out.println("QR Code URL: " + momoResponse.get("qrCodeUrl"));
                    System.out.println("Pay URL: " + momoResponse.get("payUrl"));
                    
                    return "redirect:/cart/momo-payment";
                } else {
                    // MoMo API failed - update invoice status
                    cartService.updatePaymentStatus(invoice.getId(), PaymentStatus.PAYMENT_FAILED, null);
                    String errorMsg = "MoMo Error: " + (resultCode != null ? "Code " + resultCode + " - " : "") + message;
                    System.err.println(errorMsg);
                    redirectAttributes.addFlashAttribute("error", errorMsg);
                    return "redirect:/cart/checkout";
                }
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", 
                    "Lỗi thanh toán MoMo: " + e.getMessage());
                return "redirect:/cart/checkout";
            }
        }

        return "redirect:/cart";
    }

    @GetMapping("/momo-payment")
    public String showMoMoPayment(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String orderId = (String) session.getAttribute("momoOrderId");
        
        if (orderId == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin thanh toán.");
            return "redirect:/cart";
        }
        
        model.addAttribute("orderId", orderId);
        model.addAttribute("qrCodeUrl", session.getAttribute("momoQrCodeUrl"));
        model.addAttribute("payUrl", session.getAttribute("momoPayUrl"));
        model.addAttribute("deeplink", session.getAttribute("momoDeeplink"));
        model.addAttribute("amount", session.getAttribute("momoAmount"));
        
        return "book/momo-payment";
    }

    @GetMapping("/momo-return")
    public String handleMoMoReturn(HttpSession session, 
                                   @RequestParam(required = false) String orderId,
                                   @RequestParam(required = false) Integer resultCode,
                                   RedirectAttributes redirectAttributes) {
        // Get orderId from param or session
        if (orderId == null) {
            orderId = (String) session.getAttribute("momoOrderId");
        }
        
        if (orderId != null) {
            var invoiceOpt = cartService.findInvoiceById(orderId);
            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();
                
                if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
                    // Payment confirmed - clear cart and session
                    cartService.removeCart(session);
                    session.removeAttribute("momoOrderId");
                    session.removeAttribute("momoQrCodeUrl");
                    session.removeAttribute("momoPayUrl");
                    session.removeAttribute("momoDeeplink");
                    session.removeAttribute("momoAmount");
                    
                    redirectAttributes.addFlashAttribute("success", 
                        "Thanh toán MoMo thành công! Cảm ơn bạn đã mua hàng.");
                    return "redirect:/cart";
                } else if (invoice.getPaymentStatus() == PaymentStatus.PAYMENT_FAILED) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Thanh toán MoMo thất bại. Vui lòng thử lại.");
                    return "redirect:/cart/checkout";
                } else {
                    // Still pending - redirect back to payment page
                    redirectAttributes.addFlashAttribute("info", 
                        "Đang chờ xác nhận thanh toán từ MoMo...");
                    return "redirect:/cart/momo-payment";
                }
            }
        }
        
        redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
        return "redirect:/cart";
    }

    @GetMapping("/check-payment-status/{invoiceId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable String invoiceId) {
        Map<String, Object> response = new HashMap<>();
        
        var invoiceOpt = cartService.findInvoiceById(invoiceId);
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            
            // If already paid or failed, return immediately
            if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
                response.put("status", PaymentStatus.PAID.name());
                response.put("paid", true);
                return ResponseEntity.ok(response);
            }
            
            if (invoice.getPaymentStatus() == PaymentStatus.PAYMENT_FAILED) {
                response.put("status", PaymentStatus.PAYMENT_FAILED.name());
                response.put("paid", false);
                return ResponseEntity.ok(response);
            }
            
            // If pending, query MoMo API directly to check status
            if (invoice.getPaymentStatus() == PaymentStatus.PENDING_PAYMENT) {
                try {
                    String requestId = invoiceId + "_query_" + System.currentTimeMillis();
                    var momoResponse = momoService.queryTransactionStatus(invoiceId, requestId);
                    
                    if (momoResponse != null) {
                        Integer resultCode = (Integer) momoResponse.get("resultCode");
                        System.out.println("MoMo Query Result for " + invoiceId + ": resultCode=" + resultCode);
                        
                        if (resultCode != null && resultCode == 0) {
                            // Payment successful - update database
                            Object transId = momoResponse.get("transId");
                            String transIdStr = transId != null ? transId.toString() : null;
                            cartService.updatePaymentStatus(invoiceId, PaymentStatus.PAID, transIdStr);
                            
                            response.put("status", PaymentStatus.PAID.name());
                            response.put("paid", true);
                            return ResponseEntity.ok(response);
                        } else if (resultCode != null && (resultCode == 1000 || resultCode == 1001 || resultCode == 1002 || resultCode == 1003 || resultCode == 1004 || resultCode == 1005 || resultCode == 1006)) {
                            // Transaction pending/processing - still waiting
                            response.put("status", PaymentStatus.PENDING_PAYMENT.name());
                            response.put("paid", false);
                            return ResponseEntity.ok(response);
                        } else if (resultCode != null && resultCode != 0) {
                            // Payment failed
                            cartService.updatePaymentStatus(invoiceId, PaymentStatus.PAYMENT_FAILED, null);
                            response.put("status", PaymentStatus.PAYMENT_FAILED.name());
                            response.put("paid", false);
                            return ResponseEntity.ok(response);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error querying MoMo: " + e.getMessage());
                }
            }
            
            // Default: return current status from database
            response.put("status", invoice.getPaymentStatus().name());
            response.put("paid", invoice.getPaymentStatus() == PaymentStatus.PAID);
            return ResponseEntity.ok(response);
        }
        
        response.put("error", "Invoice not found");
        return ResponseEntity.notFound().build();
    }
}
