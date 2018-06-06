package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.request.SendEmailRequest
import io.github.jhipster.config.JHipsterProperties
import org.apache.commons.lang3.CharEncoding
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SendEmailImpl(
    private val jHipsterProperties: JHipsterProperties
    , private val javaMailSender: JavaMailSender

) : SendEmail {


    private val log = LoggerFactory.getLogger(this::class.java)

    @Async
    override fun execute(request: SendEmailRequest) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            request.isMultipart, request.isHtml, request.to, request.subject, request.content)

        // Prepare message using a Spring helper
        val mimeMessage = javaMailSender.createMimeMessage()
        try {
            val message = MimeMessageHelper(mimeMessage, request.isMultipart, CharEncoding.UTF_8)
            message.setTo(request.to!!)
            message.setFrom(jHipsterProperties.mail.from)
            message.setSubject(request.subject)
            message.setText(request.content, request.isHtml)
            javaMailSender.send(mimeMessage)
            log.debug("Sent email to User '{}'", request.to)
        } catch (e: Exception) {
            if (log.isDebugEnabled) {
                log.warn("Email could not be sent to user '{}'", request.to, e)
            } else {
                log.warn("Email could not be sent to user '{}': {}", request.to, e.message)
            }
        }
    }
}
