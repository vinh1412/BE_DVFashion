/*
 * @ {#} EmailServiceImpl.java   1.0     26/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.ForgotPasswordRequest;
import vn.edu.iuh.fit.dtos.request.ResetPasswordMailRequest;
import vn.edu.iuh.fit.dtos.response.OrderItemResponse;
import vn.edu.iuh.fit.dtos.response.OrderResponse;
import vn.edu.iuh.fit.dtos.response.PaymentResponse;
import vn.edu.iuh.fit.dtos.response.ShippingInfoResponse;
import vn.edu.iuh.fit.entities.PasswordResetToken;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.enums.OrderStatus;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.TokenRefreshException;
import vn.edu.iuh.fit.repositories.PasswordResetTokenRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.EmailService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/*
 * @description: Service implementation for handling email-related operations
 * @author: Tran Hien Vinh
 * @date:   26/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final UserRepository userRepository;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final JavaMailSender mailSender;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    @Override
    public void sendPasswordResetEmail(ForgotPasswordRequest request) {
        // Check if user exists
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Not found user with email"));

        if (!user.isActive()) {
            throw new NotFoundException("User with email is not activated");
        }

        // Generate token
        String token = UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(30)) // Token valid for 30 minutes
                .used(false)
                .build();

        // Save token
        passwordResetTokenRepository.save(resetToken);

        // Create reset link
        String link = resetPasswordUrl + "/" + token;

        // Send email
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = """
                                    <html>
                                        <head>
                                            <meta charset="UTF-8">
                                        </head>
                                        <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; font-size: medium;">
                                            <div class="email-container">
                                                <div class="header" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">Xin chào <strong>%s</strong>,</p>
                                                </div>
                    
                                                <div class="content" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">
                                                        Chúng tôi nhận được yêu cầu đặt lại mật khẩu của bạn tại <strong>DVFashion</strong>.\s
                                                        Nếu bạn không yêu cầu, bạn có thể bỏ qua email này.\s
                                                        Nếu thực sự bạn quên mật khẩu, hãy <strong>click ngay vào nút bên dưới\s
                                                        hoặc copy đường link này</strong> vào trình duyệt để DVFashion đặt lại mật khẩu cho tài khoản.
                                                    </p>
                                                </div>
                    
                                                <div class="link-container" style="margin: 25px 0; padding: 15px; background-color: #f8f9fa; border: 2px solid #e9ecef; border-radius: 8px; text-align: center;">
                                                    <a href="%s" style="display: inline-block; background-color: #007bff; color: white !important; padding: 12px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; margin: 10px 0; font-size: medium;">ĐẶT LẠI MẬT KHẨU</a>
                                                    <br><br>
                                                    <div style="font-size: medium; margin: 10px 0; color: #212529;">Hoặc copy link này:</div>
                                                    <a href="%s" style="display: inline-block; font-size: medium; color: #007bff; text-decoration: underline; word-break: break-all; line-height: 1.4;">%s</a>
                                                </div>
                    
                                                <p style="font-size: medium; color: #dc3545; font-weight: bold; margin: 15px 0;">⚠️ Link này chỉ có hiệu lực trong 30 phút.</p>
                    
                                                <div class="content" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">Hi vọng bạn sẽ không quên mật khẩu của mình, nhưng nếu có quên thì chúng tôi rất sẵn sàng hỗ trợ bạn.</p>
                                                </div>
                    
                                                <div class="support-info" style="background-color: #e7f3ff; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;">
                                                    <p style="font-size: medium; margin: 0 0 10px 0; color: #212529;">
                                                        <strong>🤙🏻 Cần hỗ trợ?</strong><br>
                                                        Hotline: <strong>123456</strong><br>
                                                        Email: <a href="mailto:test@gmail.com" style="color: #007bff; text-decoration: underline;">test@gmail.com</a>
                                                    </p>
                                                    <p style="font-size: medium; margin: 0; color: #212529;">
                                                        DVFashion rất sẵn sàng để hỗ trợ khách hàng kịp thời nhất để\s
                                                        khách hàng luôn có trải nghiệm mua hàng tuyệt vời nhất.
                                                    </p>
                                                </div>
                    
                                                <div class="footer" style="margin-top: 30px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">Trân trọng cảm ơn quý khách,</p>
                                                    <p style="font-size: medium; margin: 0; color: #212529;"><strong>Đội ngũ DVFashion</strong></p>
                                                </div>
                                            </div>
                                        </body>
                                    </html>
                    """.formatted(user.getFullName(), link, link, link);

            helper.setTo(user.getEmail());
            helper.setSubject("Lấy lại mật khẩu đăng nhập");
            helper.setText(htmlContent, true); // true = send HTML

            // Send the email
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error while sending email", e);
        }
    }

    @Override
    public User validatePasswordResetToken(String token) {
        // Find token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Token is not valid"));

        // Check if token is used or expired
        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenRefreshException("Token has expired or already used");
        }

        // Return user
        return resetToken.getUser();
    }

    @Override
    public void resetPassword(ResetPasswordMailRequest request) {
        // Find token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new TokenRefreshException("Token is not valid"));

        // Check if token is used or expired
        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenRefreshException("Token has expired or already used");
        }

        // Get user and update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public void sendVerificationCode(String email, String fullName, String password, String verificationCode) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = """
                                    <html>
                                        <head>
                                            <meta charset="UTF-8">
                                        </head>
                                        <body style="font-family: Arial, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; font-size: medium;">
                                            <div class="email-container">
                                                <div class="header" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">Xin chào <strong>%s</strong>,</p>
                                                </div>
                    
                                                <div class="content" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">
                                                        Cảm ơn bạn đã đăng ký tài khoản tại <strong>DVFashion</strong>.<br>
                                                        Vui lòng sử dụng tài khoản và mật khẩu bên dưới để đăng nhập vào hệ thống.
                                                    </p>
                                                </div>
                    
                                                <div>
                                                <span style="font-size: medium; margin: 0; color: #212529;">Username: </span>
                                                <div class="code-container" style="margin: 25px 0; padding: 15px; background-color: #f8f9fa; border: 2px solid #e9ecef; border-radius: 8px; text-align: center;">
                                                    <span style="display: inline-block; font-size: xx-large; font-weight: bold; letter-spacing: 5px; color: #007bff;">%s</span>
                                                </div>
                                                </div>
                    
                                                 <div>
                                                <span style="font-size: medium; margin: 0; color: #212529;">Password: </span>
                                                <div class="code-container" style="margin: 25px 0; padding: 15px; background-color: #f8f9fa; border: 2px solid #e9ecef; border-radius: 8px; text-align: center;">
                                                    <span style="display: inline-block; font-size: xx-large; font-weight: bold; letter-spacing: 5px; color: #007bff;">%s</span>
                                                </div>
                                                </div>
                    
                                                <div class="content" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">
                                                        Sau khi đăng nhập tài khoản vui lòng bạn nhập mã xác thực bên dưới để được cấp phép sử dụng tài khoản
                                                    </p>
                                                </div>
                    
                                                <div class="code-container" style="margin: 25px 0; padding: 15px; background-color: #f8f9fa; border: 2px solid #e9ecef; border-radius: 8px; text-align: center;">
                                                    <span style="display: inline-block; font-size: xx-large; font-weight: bold; letter-spacing: 5px; color: #007bff;">%s</span>
                                                </div>
                    
                                                <p style="font-size: medium; color: #dc3545; font-weight: bold; margin: 15px 0;">⚠️ Mã xác nhận chỉ có hiệu lực trong 24h.</p>
                    
                                                <div class="content" style="margin-bottom: 20px;">
                                                    <p style="font-size: medium; margin: 0; color: #212529;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
                                                </div>
                    
                                                <div class="support-info" style="background-color: #e7f3ff; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;">
                                                    <p style="font-size: medium; margin: 0 0 10px 0; color: #212529;">
                                                        <strong>🤙🏻 Cần hỗ trợ?</strong><br>
                                                        Hotline: <strong>123456</strong><br>
                                                        Email: <a href="mailto:test@gmail.com" style="color: #007bff; text-decoration: underline;">test@gmail.com</a>
                                                    </p>
                                                </div>
                                            </div>
                                        </body>
                                    </html>
                    """.formatted(fullName, email, password, verificationCode);

            helper.setTo(email);
            helper.setSubject("Mã xác nhận đăng ký tài khoản");
            helper.setText(htmlContent, true);

            // Send the email
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendOrderConfirmationEmail(OrderResponse orderResponse, String customerEmail) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String itemsHtml = buildOrderItemsHtml(orderResponse.items()); // Giữ nguyên helper method của bạn

            String htmlContent = """
                    <html>
                      <head>
                        <meta charset="UTF-8">
                        <style>
                          body {
                            font-family: 'Segoe UI', Arial, sans-serif;
                            background-color: #f4f6f8;
                            margin: 0;
                            padding: 0;
                            font-size: 16px;
                          }
                          .container {
                            max-width: 900px; /* tăng chiều ngang */
                            background-color: #ffffff;
                            margin: 40px auto;
                            border-radius: 12px;
                            box-shadow: 0 3px 10px rgba(0,0,0,0.12);
                            overflow: hidden;
                          }
                          .header {
                            background-color: #007bff;
                            color: #ffffff;
                            padding: 30px;
                            text-align: center;
                          }
                          .header h2 {
                            font-size: 28px;
                            margin: 0;
                          }
                          .content {
                            padding: 35px 45px;
                            color: #333333;
                            font-size: 17px;
                          }
                          .content h3 {
                            color: #007bff;
                            border-bottom: 2px solid #007bff30;
                            padding-bottom: 10px;
                            font-size: 22px;
                          }
                          .item {
                            border-bottom: 1px solid #e9ecef;
                            padding: 15px 0;
                            font-size: 16px;
                          }
                          .total-summary {
                            background-color: #f8f9fa;
                            padding: 20px;
                            border-radius: 8px;
                            margin-top: 25px;
                          }
                          .total-summary p {
                            margin: 8px 0;
                          }
                          .total {
                            font-size: 22px;
                            color: #007bff;
                            font-weight: bold;
                          }
                          .footer {
                            background-color: #f1f3f5;
                            text-align: center;
                            padding: 20px;
                            font-size: 15px;
                            color: #6c757d;
                          }
                          a {
                            color: #007bff;
                            text-decoration: none;
                          }
                        </style>
                      </head>
                      <body>
                        <div class="container">
                          <div class="header">
                            <h2>🛍️ Xác nhận đơn hàng #%s</h2>
                          </div>
                    
                          <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Cảm ơn bạn đã đặt hàng tại <strong>DVFashion</strong>! Đơn hàng của bạn đã được tiếp nhận và đang được xử lý.</p>
                    
                            <h3>Thông tin đơn hàng</h3>
                            <p><strong>Mã đơn hàng:</strong> %s</p>
                            <p><strong>Ngày đặt:</strong> %s</p>
                            <p><strong>Trạng thái:</strong> <span style="color: #28a745;">%s</span></p>
                            <p><strong>Phương thức thanh toán:</strong> %s</p>
                    
                            <h3>Sản phẩm đã đặt</h3>
                            %s
                    
                            <h3>Thông tin giao hàng</h3>
                            <p><strong>Người nhận:</strong> %s</p>
                            <p><strong>Số điện thoại:</strong> %s</p>
                            <p><strong>Địa chỉ:</strong> %s</p>
                    
                            <div class="total-summary">
                              <p><strong>Tổng tiền hàng:</strong> %s VND</p>
                              <p><strong>Phí vận chuyển:</strong> %s VND</p>
                              %s
                              <hr>
                              <p class="total">Tổng thanh toán: %s VND</p>
                            </div>
                    
                            <div style="background-color: #e7f3ff; border-left: 5px solid #007bff; padding: 20px; margin-top: 35px; font-size: 17px;">
                              <p><strong>📞 Cần hỗ trợ?</strong><br>
                              Hotline: <strong>123456</strong><br>
                              Email: <a href="mailto:test@gmail.com">test@gmail.com</a></p>
                            </div>
                          </div>
                    
                          <div class="footer">
                            <p>© 2025 DVFashion — Cảm ơn bạn đã mua sắm cùng chúng tôi 💙</p>
                          </div>
                        </div>
                      </body>
                    </html>
                    """.formatted(
                    orderResponse.orderNumber(),
                    orderResponse.customerName(),
                    orderResponse.orderNumber(),
                    orderResponse.orderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    getStatusDisplayName(orderResponse.status()),
                    getPaymentMethodDisplayName(orderResponse.payment()),
                    itemsHtml,
                    orderResponse.shippingInfo().fullName(),
                    orderResponse.shippingInfo().phone(),
                    buildShippingAddress(orderResponse.shippingInfo()),
                    formatCurrency(orderResponse.subtotal()),
                    formatCurrency(orderResponse.shippingFee()),
                    orderResponse.discountAmount() != null ?
                            "<p><strong>Giảm giá:</strong> -" + formatCurrency(orderResponse.discountAmount()) + " VND</p>" : "",
                    formatCurrency(orderResponse.totalAmount())
            );

            helper.setTo(customerEmail);
            helper.setSubject("Xác nhận đơn hàng #" + orderResponse.orderNumber());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Error sending order confirmation email: {}", e.getMessage());
            throw new RuntimeException("Error while sending order confirmation email", e);
        }
    }

    @Override
    public void sendOrderStatusUpdateEmail(OrderResponse orderResponse, String customerEmail, OrderStatus oldStatus, OrderStatus newStatus) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String statusMessage = getStatusUpdateMessage(newStatus);
            String statusColor = getStatusColor(newStatus);

            String htmlContent = """
                    <html>
                      <head>
                        <meta charset="UTF-8">
                        <style>
                          body {
                            font-family: 'Segoe UI', Arial, sans-serif;
                            background-color: #f4f6f8;
                            margin: 0;
                            padding: 0;
                            font-size: 16px;
                          }
                          .container {
                            max-width: 900px;
                            background-color: #ffffff;
                            margin: 40px auto;
                            border-radius: 12px;
                            box-shadow: 0 3px 10px rgba(0,0,0,0.12);
                            overflow: hidden;
                          }
                          .header {
                            background-color: #007bff;
                            color: #ffffff;
                            padding: 30px;
                            text-align: center;
                          }
                          .header h2 {
                            font-size: 28px;
                            margin: 0;
                          }
                          .content {
                            padding: 35px 45px;
                            color: #333333;
                            font-size: 17px;
                          }
                          .status-box {
                            background-color: #f8f9fa;
                            border-radius: 8px;
                            text-align: center;
                            padding: 25px;
                            margin: 25px 0;
                            font-size: 17px;
                          }
                          .status-box p {
                            margin: 8px 0;
                          }
                          .footer {
                            background-color: #f1f3f5;
                            text-align: center;
                            padding: 20px;
                            font-size: 15px;
                            color: #6c757d;
                          }
                          a { color: #007bff; text-decoration: none; }
                        </style>
                      </head>
                      <body>
                        <div class="container">
                          <div class="header">
                            <h2>🔔 Cập nhật đơn hàng #%s</h2>
                          </div>
                    
                          <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Đơn hàng của bạn đã có cập nhật mới:</p>
                    
                            <div class="status-box">
                              <p><strong>Trạng thái cũ:</strong> <span style="color:#6c757d;">%s</span></p>
                              <p style="font-size:28px;">↓</p>
                              <p><strong>Trạng thái mới:</strong> <span style="color:%s; font-weight:bold;">%s</span></p>
                            </div>
                    
                            <div style="background-color:#e7f3ff; padding:20px; border-radius:8px; font-size:17px;">
                              <p><strong>📋 Mã đơn hàng:</strong> %s</p>
                              <p><strong>Tổng tiền:</strong> %s VND</p>
                              <p><strong>Ngày đặt:</strong> %s</p>
                            </div>
                    
                            <div style="background-color:#fff3cd; padding:20px; border-radius:8px; margin-top:20px; font-size:17px;">
                              <p><strong>📝 %s</strong></p>
                            </div>
                    
                            <div style="background-color: #e7f3ff; border-left: 5px solid #007bff; padding: 20px; margin-top: 35px; font-size:17px;">
                              <p><strong>📞 Cần hỗ trợ?</strong><br>
                              Hotline: <strong>123456</strong><br>
                              Email: <a href="mailto:test@gmail.com">test@gmail.com</a></p>
                            </div>
                          </div>
                    
                          <div class="footer">
                            <p>© 2025 DVFashion — Cảm ơn bạn đã tin tưởng 💙</p>
                          </div>
                        </div>
                      </body>
                    </html>
                    """.formatted(
                    orderResponse.orderNumber(),
                    orderResponse.customerName(),
                    getStatusDisplayName(oldStatus),
                    statusColor,
                    getStatusDisplayName(newStatus),
                    orderResponse.orderNumber(),
                    formatCurrency(orderResponse.totalAmount()),
                    orderResponse.orderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    statusMessage
            );


            helper.setTo(customerEmail);
            helper.setSubject("Cập nhật đơn hàng #" + orderResponse.orderNumber() + " - " + getStatusDisplayName(newStatus));
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Error sending order status update email: {}", e.getMessage());
            throw new RuntimeException("Error while sending order status update email", e);
        }
    }

    @Override
    public void sendOrderCancellationEmail(OrderResponse orderResponse, String customerEmail, String cancellationReason) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlContent = """
                <html>
                  <head>
                    <meta charset="UTF-8">
                    <style>
                      body {
                        font-family: 'Segoe UI', Arial, sans-serif;
                        background-color: #f4f6f8;
                        margin: 0;
                        padding: 0;
                        font-size: 16px;
                      }
                      .container {
                        max-width: 900px;
                        background-color: #ffffff;
                        margin: 40px auto;
                        border-radius: 12px;
                        box-shadow: 0 3px 10px rgba(0,0,0,0.12);
                        overflow: hidden;
                      }
                      .header {
                        background-color: #dc3545;
                        color: #ffffff;
                        padding: 30px;
                        text-align: center;
                      }
                      .header h2 {
                        font-size: 28px;
                        margin: 0;
                      }
                      .content {
                        padding: 35px 45px;
                        color: #333333;
                        font-size: 17px;
                      }
                      .reason-box {
                        background-color: #fff3cd;
                        border-left: 6px solid #ffc107;
                        padding: 20px;
                        border-radius: 8px;
                        margin: 25px 0;
                        font-size: 17px;
                      }
                      .summary-box {
                        background-color: #f8f9fa;
                        border-radius: 8px;
                        padding: 20px;
                        margin-top: 20px;
                        font-size: 17px;
                      }
                      .summary-box p { margin: 8px 0; }
                      .footer {
                        background-color: #f1f3f5;
                        text-align: center;
                        padding: 20px;
                        font-size: 15px;
                        color: #6c757d;
                      }
                      a { color: #007bff; text-decoration: none; }
                    </style>
                  </head>
                  <body>
                    <div class="container">
                      <div class="header">
                        <h2>❌ Đơn hàng #%s đã bị hủy</h2>
                      </div>
                
                      <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chúng tôi rất tiếc khi phải thông báo rằng đơn hàng của bạn đã bị <strong>hủy</strong>.</p>
                
                        <div class="reason-box">
                          <p><strong>📝 Lý do hủy:</strong> %s</p>
                        </div>
                
                        <div class="summary-box">
                          <p><strong>Mã đơn hàng:</strong> %s</p>
                          <p><strong>Ngày đặt:</strong> %s</p>
                          <p><strong>Tổng thanh toán:</strong> %s VND</p>
                          <p><strong>Trạng thái hiện tại:</strong> <span style="color:#dc3545; font-weight:bold;">Đã hủy</span></p>
                        </div>
                
                        <p style="margin-top:25px;">
                          Nếu bạn đã thanh toán trước bằng <strong>%s</strong>, số tiền sẽ được hoàn lại theo chính sách của DVFashion.
                          Vui lòng kiểm tra email từ <strong>PayPal</strong> (hoặc tài khoản ngân hàng) để xác nhận giao dịch hoàn tiền.
                        </p>
                
                        <div style="background-color:#e7f3ff; border-left:5px solid #007bff; padding:20px; margin-top:35px; font-size:17px;">
                          <p><strong>📞 Cần hỗ trợ?</strong><br>
                          Hotline: <strong>123456</strong><br>
                          Email: <a href="mailto:test@gmail.com">test@gmail.com</a></p>
                        </div>
                      </div>
                
                      <div class="footer">
                        <p>© 2025 DVFashion — Mong sớm được phục vụ bạn trong lần mua sắm tới 💙</p>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(
                    orderResponse.orderNumber(),
                    orderResponse.customerName(),
                    cancellationReason != null && !cancellationReason.isBlank() ? cancellationReason : "Không có lý do cụ thể",
                    orderResponse.orderNumber(),
                    orderResponse.orderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    formatCurrency(orderResponse.totalAmount()),
                    getPaymentMethodDisplayName(orderResponse.payment())
            );

            helper.setTo(customerEmail);
            helper.setSubject("Hủy đơn hàng #" + orderResponse.orderNumber() + " - DVFashion");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("✅ Sent order cancellation email for order #{}", orderResponse.orderNumber());

        } catch (MessagingException e) {
            log.error("Error sending order cancellation email: {}", e.getMessage());
            throw new RuntimeException("Error while sending order cancellation email", e);
        }
    }

    // Helper methods
    private String buildOrderItemsHtml(List<OrderItemResponse> items) {
        StringBuilder html = new StringBuilder();
        for (OrderItemResponse item : items) {
            html.append(String.format("""
                            <div class="item">
                                <p><strong>%s - %s</strong></p>
                                <p>Size: %s | Số lượng: %d | Đơn giá: %s VND</p>
                                <p><strong>Thành tiền: %s VND</strong></p>
                            </div>
                            """,
                    item.productName(),
                    item.color(),
                    item.sizeName(),
                    item.quantity(),
                    formatCurrency(item.unitPrice()),
                    formatCurrency(item.totalPrice())
            ));
        }
        return html.toString();
    }

    private String buildShippingAddress(ShippingInfoResponse shipping) {
        return String.format("%s", shipping.fullAddress());
    }

    private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xác nhận";
            case CONFIRMED -> "Đã xác nhận";
            case PROCESSING -> "Đang xử lý";
            case SHIPPED -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng";
            case CANCELED -> "Đã hủy";
            case RETURNED -> "Đã trả hàng";
        };
    }

    private String getStatusColor(OrderStatus status) {
        return switch (status) {
            case PENDING -> "#ffc107";
            case CONFIRMED -> "#007bff";
            case PROCESSING -> "#17a2b8";
            case SHIPPED -> "#fd7e14";
            case DELIVERED -> "#28a745";
            case CANCELED -> "#dc3545";
            case RETURNED -> "#6c757d";
        };
    }

    private String getStatusUpdateMessage(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Đơn hàng của bạn đã được xác nhận và sẽ được xử lý sớm nhất.";
            case PROCESSING -> "Đơn hàng đang được chuẩn bị và đóng gói.";
            case SHIPPED ->
                    "Đơn hàng đã được giao cho đơn vị vận chuyển. Bạn sẽ sớm nhận được hàng.";
            case DELIVERED -> "Đơn hàng đã được giao thành công. Cảm ơn bạn đã mua hàng!";
            case CANCELED -> "Đơn hàng đã bị hủy. Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.";
            case RETURNED -> "Đơn hàng đã được trả lại. Chúng tôi sẽ xử lý hoàn tiền sớm nhất.";
            default -> "Trạng thái đơn hàng đã được cập nhật.";
        };
    }

    private String getPaymentMethodDisplayName(PaymentResponse payment) {
        if (payment == null) return "Chưa xác định";
        return switch (payment.paymentMethod()) {
            case CASH_ON_DELIVERY -> "Thanh toán khi nhận hàng";
            case PAYPAL -> "PayPal";
            default -> payment.paymentMethod().toString();
        };
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }
}
