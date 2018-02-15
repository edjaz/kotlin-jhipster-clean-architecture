package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.MailContants.BASE_URL
import fr.edjaz.web.service.mail.MailContants.USER
import fr.edjaz.web.service.mail.request.SendEmailFromTemplateRequest
import fr.edjaz.web.service.mail.request.SendEmailRequest
import io.github.jhipster.config.JHipsterProperties
import org.springframework.context.MessageSource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring4.SpringTemplateEngine
import java.util.*

@Service
class SendEmailFromTemplateImpl(
    private val jHipsterProperties: JHipsterProperties
    , private val messageSource: MessageSource
    , private val templateEngine: SpringTemplateEngine
    , private val sendEmail: SendEmail

) : SendEmailFromTemplate {

    @Async
    override fun execute(request: SendEmailFromTemplateRequest) {
        val locale = Locale.forLanguageTag(request.user.langKey!!)
        val context = Context(locale)
        context.setVariable(USER, request.user)
        context.setVariable(BASE_URL, jHipsterProperties.mail.baseUrl)
        val content = templateEngine.process(request.templateName, context)
        val subject = messageSource.getMessage(request.titleKey, null, locale)
        sendEmail.execute(SendEmailRequest(request.user.email!!, subject, content, false, true))
    }
}
