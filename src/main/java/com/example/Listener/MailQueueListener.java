package com.example.Listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {
    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    @RabbitHandler
    public void sendMailMessage(Map<String, String> data) {
        String type = data.get("type");
        String email = data.get("email");
        String code = data.get("code");

        SimpleMailMessage message = switch (type) {
            case "register" -> this.createMessage("欢迎注册我们的网站", "您的邮箱注册验证码为：" + code + "，\t有效时间为15分钟， 请勿向他人泄露验证码信息。", email);
            case "reset" -> this.createMessage("重置密码验证邮件", "您好，您正在进行重置密码操作，验证码为：" + code + "，\t有效时间为15分钟，如非本人操作，请无视，请勿向他人泄露验证码信息。", email);
            default -> null;
        };
        if (message == null) return;
        sender.send(message);
    }

    private SimpleMailMessage createMessage(String title, String content, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}
