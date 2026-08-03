package Utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailUtils {

    // IMPORTANT: Use your Gmail + 16-char App Password (no spaces)
    private static final String FROM_EMAIL = "nhanpth.ce191725@gmail.com"; 
    private static final String APP_PASSWORD = "vgcngfzhdhdpfyqh"; 

    public static boolean sendOTP(String toEmail, String otpCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("MÃ XÁC MINH OTP - FASHION STORE");
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #000; max-width: 500px;'>"
                    + "<h2 style='text-transform: uppercase; font-weight: 900;'>FASHION STORE</h2>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản. Đây là mã xác minh OTP của bạn:</p>"
                    + "<div style='background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; border: 2px dashed #000;'>"
                    + otpCode
                    + "</div>"
                    + "</div>";
                    
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.out.println("====== CHI TIẾT LỖI GỬI MAIL TẠI ĐÂY ======");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendOTPForPasswordReset(String toEmail, String otpCode, int expiryMinutes) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("ĐẶT LẠI MẬT KHẨU - FASHION STORE");
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 500px; background-color: #f9f9f9;'>"
                    + "<h2 style='text-transform: uppercase; font-weight: 900; color: #333;'>FASHION STORE</h2>"
                    + "<h3 style='color: #555;'>Yêu cầu đặt lại mật khẩu</h3>"
                    + "<p>Xin chào,</p>"
                    + "<p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Đây là mã xác minh OTP của bạn:</p>"
                    + "<div style='background-color: #fff; padding: 20px; text-align: center; font-size: 32px; font-weight: bold; letter-spacing: 8px; border: 2px dashed #4CAF50; margin: 20px 0; border-radius: 8px;'>"
                    + otpCode
                    + "</div>"
                    + "<p style='color: #666; font-size: 14px;'>Mã này sẽ hết hạn sau <strong>" + expiryMinutes + " phút</strong>.</p>"
                    + "<div style='background-color: #fff3cd; padding: 15px; border-radius: 8px; border: 1px solid #ffc107; margin: 20px 0;'>"
                    + "<p style='margin: 0; color: #856404; font-size: 14px;'><strong>Lưu ý bảo mật:</strong></p>"
                    + "<ul style='margin: 10px 0 0 0; padding-left: 20px; color: #856404; font-size: 13px;'>"
                    + "<li>Không chia sẻ mã này với bất kỳ ai</li>"
                    + "<li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>"
                    + "<li>Không nhấp vào các liên kết đáng ngờ</li>"
                    + "</ul>"
                    + "</div>"
                    + "<p>Trân trọng,<br><strong>Fashion Store Team</strong></p>"
                    + "</div>";
                    
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.out.println("====== CHI TIẾT LỖI GỬI MAIL (Password Reset) ======");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean sendStaffCredentials(String toEmail, String fullName, String email, String password) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("TÀI KHOẢN NHÂN VIÊN - FASHION STORE");
            
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 500px; background-color: #f9f9f9;'>"
                    + "<h2 style='text-transform: uppercase; font-weight: 900; color: #333;'>FASHION STORE</h2>"
                    + "<h3 style='color: #555;'>Thông Tin Tài Khoản Nhân Viên</h3>"
                    + "<p>Xin chào <strong>" + fullName + "</strong>,</p>"
                    + "<p>Tài khoản nhân viên của bạn đã được tạo thành công. Vui lòng đăng nhập với thông tin bên dưới:</p>"
                    + "<div style='background-color: #fff; padding: 20px; border-radius: 8px; border: 1px solid #ddd; margin: 20px 0;'>"
                    + "<p style='margin: 5px 0;'><strong>Email:</strong> " + email + "</p>"
                    + "<p style='margin: 5px 0;'><strong>Mật khẩu:</strong> <code style='background-color: #f0f0f0; padding: 2px 8px; border-radius: 4px; font-size: 16px;'>" + password + "</code></p>"
                    + "</div>"
                    + "<p style='color: #e74c3c;'><strong>Lưu ý:</strong> Vui lòng đổi mật khẩu ngay sau khi đăng nhập lần đầu để đảm bảo bảo mật.</p>"
                    + "<p>Trân trọng,<br><strong>Fashion Store Team</strong></p>"
                    + "</div>";
                    
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.out.println("====== CHI TIẾT LỖI GỬI MAIL TẠI ĐÂY ======");
            e.printStackTrace();
            return false;
        }
    }
    public static boolean sendOrderExpiredNotification(String toEmail, String customerName,
            String orderId, LocalDateTime placedAt, BigDecimal totalAmount) {

        String safeName = customerName == null || customerName.trim().isEmpty()
                ? "Customer" : customerName.trim();
        String checkoutTime = placedAt == null ? "Unknown"
                : placedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String amount = totalAmount == null ? "0 VND"
                : NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                        .format(totalAmount) + " VND";

        Properties props = createMailProperties();
        Session session = createMailSession(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("ORDER EXPIRED - FASHION STORE");

            String htmlContent = "<div style='font-family:Arial,sans-serif;padding:20px;"
                    + "border:1px solid #ddd;max-width:560px'>"
                    + "<h2 style='margin-top:0'>FASHION STORE</h2>"
                    + "<p>Hello <strong>" + escapeHtml(safeName) + "</strong>,</p>"
                    + "<p>Your order <strong>" + escapeHtml(orderId) + "</strong> "
                    + "was not confirmed within 2 days and has been automatically removed.</p>"
                    + "<p><strong>Checkout time:</strong> " + checkoutTime + "<br>"
                    + "<strong>Order total:</strong> " + amount + "</p>"
                    + "<p>If the order was paid by Wallet, the amount was returned automatically. "
                    + "For VNPay, the project has recorded the refund status.</p>"
                    + "<p>You can return to the store and place a new order at any time.</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=UTF-8");
            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.out.println("sendOrderExpiredNotification error for order " + orderId);
            e.printStackTrace();
            return false;
        }
    }

    private static Properties createMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }

    private static Session createMailSession(Properties props) {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}