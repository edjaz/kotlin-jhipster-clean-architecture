package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.request.SendEmailFromTemplateRequest
import fr.edjaz.web.service.mail.request.SendEmailUserRequest
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SendActivationEmailImpl(
    private val sendEmailFromTemplate: SendEmailFromTemplate

) : SendActivationEmail {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Async
    override fun execute(request: SendEmailUserRequest) {
        log.debug("Sending activation email to '{}'", request.user.email)
        sendEmailFromTemplate.execute(SendEmailFromTemplateRequest("activationEmail", "email.activation.title", request.user))
    }
}
