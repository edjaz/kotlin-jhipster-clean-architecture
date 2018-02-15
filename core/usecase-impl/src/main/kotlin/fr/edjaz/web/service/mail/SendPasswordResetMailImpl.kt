package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.request.SendEmailFromTemplateRequest
import fr.edjaz.web.service.mail.request.SendEmailUserRequest
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SendPasswordResetMailImpl(
    private val sendEmailFromTemplate: SendEmailFromTemplate

) : SendPasswordResetMail{

    private val log = LoggerFactory.getLogger(this::class.java)

    @Async
    override fun execute(request: SendEmailUserRequest) {
        log.debug("Sending password reset email to '{}'", request.user.email)
        sendEmailFromTemplate.execute(SendEmailFromTemplateRequest("passwordResetEmail", "email.reset.title", request.user))
    }
}
