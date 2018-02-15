package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.request.SendEmailFromTemplateRequest
import org.springframework.scheduling.annotation.Async

interface SendEmailFromTemplate {
    @Async
    fun execute(request: SendEmailFromTemplateRequest)
}
