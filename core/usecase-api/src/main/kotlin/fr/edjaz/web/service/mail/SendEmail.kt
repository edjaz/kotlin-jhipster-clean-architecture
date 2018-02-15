package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.request.SendEmailRequest
import org.springframework.scheduling.annotation.Async

interface SendEmail {
    @Async
    fun execute(request: SendEmailRequest)

}
