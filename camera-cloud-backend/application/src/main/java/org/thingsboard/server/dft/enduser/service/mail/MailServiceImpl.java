package org.thingsboard.server.dft.enduser.service.mail;

import com.fasterxml.jackson.databind.JsonNode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.core.NestedRuntimeException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.service.mail.MailExecutorService;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    public static final String MAIL_PROP = "mail.";

    public static final String UTF_8 = "UTF-8";

    private final MailExecutorService mailExecutorService;

    public static final String TARGET_EMAIL = "targetEmail";

    private final MessageSource messages;

    private final Configuration freemarkerConfig;

    private final AdminSettingsService adminSettingsService;

    private JavaMailSenderImpl mailSender;

    private String mailFrom;

    @PostConstruct
    private void init() {
        updateMailConfiguration();
    }

    @Override
    public void updateMailConfiguration() {
        AdminSettings settings = adminSettingsService.findAdminSettingsByKey(new TenantId(EntityId.NULL_UUID), "mail");
        if (settings != null) {
            JsonNode jsonConfig = settings.getJsonValue();
            mailSender = createMailSender(jsonConfig);
            mailFrom = jsonConfig.get("mailFrom").asText();
        } else {
            throw new IncorrectParameterException("Failed to date mail configuration. Settings not found!");
        }
    }

    private void sendMail(JavaMailSenderImpl mailSender,
                          String mailFrom, String email,
                          String subject, String message) throws ThingsboardException {
        try {
            MimeMessage mimeMsg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, UTF_8);
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw handleException(e);
        }
    }

    @Override
    public void sendResetPasswordEmailAsync(String passwordResetLink, String email, String fullName) {
        mailExecutorService.execute(() -> {
            try {
                this.sendResetPasswordEmail(passwordResetLink, email, fullName);
            } catch (ThingsboardException e) {
                log.error("Error occurred: {} ", e.getMessage());
            }
        });
    }

    @Override
    public void sendResetPasswordEmail(String passwordResetLink, String email, String fullName) throws ThingsboardException {
        String subject = "Quên mật khẩu Cloud Camera";

        Map<String, Object> model = new HashMap<>();
        model.put("passwordResetLink", passwordResetLink);
        model.put("endUserEmail", email);
        model.put("fullName", fullName);

        String message = mergeTemplateIntoString("end.user.password.reset.ftl", model);

        sendMail(mailSender, mailFrom, email, subject, message);
    }

    private String mergeTemplateIntoString(String templateLocation,
                                           Map<String, Object> model) throws ThingsboardException {
        try {
            Template template = freemarkerConfig.getTemplate(templateLocation);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    protected ThingsboardException handleException(Exception exception) {
        String message;
        if (exception instanceof NestedRuntimeException) {
            message = ((NestedRuntimeException) exception).getMostSpecificCause().getMessage();
        } else {
            message = exception.getMessage();
        }
        log.warn("Unable to send mail: {}", message);
        return new ThingsboardException(String.format("Unable to send mail: %s", message),
                ThingsboardErrorCode.GENERAL);
    }

    private JavaMailSenderImpl createMailSender(JsonNode jsonConfig) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(jsonConfig.get("smtpHost").asText());
        mailSender.setPort(parsePort(jsonConfig.get("smtpPort").asText()));
        mailSender.setUsername(jsonConfig.get("username").asText());
        mailSender.setPassword(jsonConfig.get("password").asText());
        mailSender.setJavaMailProperties(createJavaMailProperties(jsonConfig));
        return mailSender;
    }

    private int parsePort(String strPort) {
        try {
            return Integer.parseInt(strPort);
        } catch (NumberFormatException e) {
            throw new IncorrectParameterException(String.format("Invalid smtp port value: %s", strPort));
        }
    }

    private Properties createJavaMailProperties(JsonNode jsonConfig) {
        Properties javaMailProperties = new Properties();
        String protocol = jsonConfig.get("smtpProtocol").asText();
        javaMailProperties.put("mail.transport.protocol", protocol);
        javaMailProperties.put(MAIL_PROP + protocol + ".host", jsonConfig.get("smtpHost").asText());
        javaMailProperties.put(MAIL_PROP + protocol + ".port", jsonConfig.get("smtpPort").asText());
        javaMailProperties.put(MAIL_PROP + protocol + ".timeout", jsonConfig.get("timeout").asText());
        javaMailProperties.put(MAIL_PROP + protocol + ".auth", String.valueOf(StringUtils.isNotEmpty(jsonConfig.get("username").asText())));
        boolean enableTls = false;
        if (jsonConfig.has("enableTls")) {
            if (jsonConfig.get("enableTls").isBoolean() && jsonConfig.get("enableTls").booleanValue()) {
                enableTls = true;
            } else if (jsonConfig.get("enableTls").isTextual()) {
                enableTls = "true".equalsIgnoreCase(jsonConfig.get("enableTls").asText());
            }
        }
        javaMailProperties.put(MAIL_PROP + protocol + ".starttls.enable", enableTls);
        if (enableTls && jsonConfig.has("tlsVersion") && !jsonConfig.get("tlsVersion").isNull()) {
            String tlsVersion = jsonConfig.get("tlsVersion").asText();
            if (StringUtils.isNoneEmpty(tlsVersion)) {
                javaMailProperties.put(MAIL_PROP + protocol + ".ssl.protocols", tlsVersion);
            }
        }

        boolean enableProxy = jsonConfig.has("enableProxy") && jsonConfig.get("enableProxy").asBoolean();

        if (enableProxy) {
            javaMailProperties.put(MAIL_PROP + protocol + ".proxy.host", jsonConfig.get("proxyHost").asText());
            javaMailProperties.put(MAIL_PROP + protocol + ".proxy.port", jsonConfig.get("proxyPort").asText());
            String proxyUser = jsonConfig.get("proxyUser").asText();
            if (StringUtils.isNoneEmpty(proxyUser)) {
                javaMailProperties.put(MAIL_PROP + protocol + ".proxy.user", proxyUser);
            }
            String proxyPassword = jsonConfig.get("proxyPassword").asText();
            if (StringUtils.isNoneEmpty(proxyPassword)) {
                javaMailProperties.put(MAIL_PROP + protocol + ".proxy.password", proxyPassword);
            }
        }
        return javaMailProperties;
    }
}
