package Utils;

import java.util.Properties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailUtils {

    // QUAN TRỌNG: Hãy điền đúng Gmail và Mật khẩu ứng dụng 16 ký tự (VIẾT LIỀN, KHÔNG CÓ KHOẢNG TRẮNG)
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
}