package com.archer.task.service.mail;

import com.archer.task.config.ScheduleTaskProperties;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    private final ScheduleTaskProperties scheduleTaskProperties;

    public void sendJsonAttachment(String jsonContent, String fileName) {
        try {
            String to = scheduleTaskProperties.getTo();
            String subject = scheduleTaskProperties.getSubject();
            String from = scheduleTaskProperties.getMail().getFrom();

            if (to == null || to.isBlank()) {
                log.warn("邮件接收人未配置，跳过邮件发送");
                return;
            }
            if (from == null || from.isBlank()) {
                log.warn("邮件发送人未配置，跳过邮件发送");
                return;
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText("", false);

            ByteArrayResource attachment = new ByteArrayResource(jsonContent.getBytes(StandardCharsets.UTF_8));
            helper.addAttachment(fileName, attachment);

            mailSender.send(mimeMessage);
            log.info("邮件发送成功，收件人: {}, 主题: {}, 附件: {}", to, subject, fileName);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
        }
    }
}
