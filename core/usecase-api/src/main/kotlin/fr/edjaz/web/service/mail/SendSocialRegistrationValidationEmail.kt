package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.request.SendSocialRegistrationValidationEmailRequest
import org.springframework.scheduling.annotation.Async

interface SendSocialRegistrationValidationEmail {
    @Async
    fun execute(request: SendSocialRegistrationValidationEmailRequest)
}
