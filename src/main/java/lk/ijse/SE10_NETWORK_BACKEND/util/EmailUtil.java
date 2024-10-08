package lk.ijse.SE10_NETWORK_BACKEND.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lk.ijse.SE10_NETWORK_BACKEND.customObj.MailBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Random;

@Component
public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Method to load the HTML template and replace multiple placeholders
    private String loadHtmlTemplate(String templateName, Map<String, String> replacements) throws IOException {
        String templatePath = "src/main/resources/templates/" + templateName + ".html";
        String content = new String(Files.readAllBytes(Paths.get(templatePath)));
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String placeholder = "\\{" + entry.getKey() + "\\}";
            content = content.replaceAll(placeholder, entry.getValue());
        }
        return content;
    }

    // Send an HTML email with dynamic values from a template
    public void sendHtmlMessage(MailBody mailBody) throws MessagingException, IOException {
        String htmlContent = loadHtmlTemplate(mailBody.templateName(), mailBody.replacements());
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null; // true indicates multipart
        helper = new MimeMessageHelper(message, true);
        helper.setTo(mailBody.to());
        helper.setFrom(fromEmail);
        helper.setSubject(mailBody.subject());
        helper.setText(htmlContent, true); // true indicates HTML
        javaMailSender.send(message);
        logger.info("HTML Mail Sent");
    }

    public Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
