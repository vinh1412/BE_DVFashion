/*
 * @ {#} EmailServiceImpl.java   1.0     26/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.ForgotPasswordRequest;
import vn.edu.iuh.fit.dtos.request.ResetPasswordMailRequest;
import vn.edu.iuh.fit.entities.PasswordResetToken;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.exceptions.NotFoundException;
import vn.edu.iuh.fit.exceptions.TokenRefreshException;
import vn.edu.iuh.fit.repositories.PasswordResetTokenRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.EmailService;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * @description: Service implementation for handling email-related operations
 * @author: Tran Hien Vinh
 * @date:   26/08/2025
 * @version:    1.0
 */
@Service
@RequiredArgsConstructor
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
}
