package fr.edjaz.web.service.mail

import fr.edjaz.web.service.mail.MailContants.BASE_URL
import fr.edjaz.web.service.mail.MailContants.USER
import fr.edjaz.web.service.mail.request.SendEmailRequest
import fr.edjaz.web.service.mail.request.SendSocialRegistrationValidationEmailRequest
import io.github.jhipster.config.JHipsterProperties
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring4.SpringTemplateEngine
import java.util.*

@Service
class SendSocialRegistrationValidationEmailImpl(
    private val jHipsterProperties: JHipsterProperties
    , private val messageSource: MessageSource
    , private val templateEngine: SpringTemplateEngine
    , private val sendEmail: SendEmail

) : SendSocialRegistrationValidationEmail {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Async
    override fun execute(request: SendSocialRegistrationValidationEmailRequest) {
        log.debug("Sending social registration validation email to '{}'", request.user.email)
        val locale = Locale.forLanguageTag(request.user.langKey!!)
        val context = Context(locale)
        context.setVariable(USER, request.user)
        context.setVariable(BASE_URL, jHipsterProperties.mail.baseUrl)
        context.setVariable("provider", StringUtils.capitalize(request.provider))
        val content = templateEngine.process("socialRegistrationValidationEmail", context)
        val subject = messageSource.getMessage("email.social.registration.title", null, locale)
        sendEmail.execute(SendEmailRequest(request.user.email!!, subject, content, false, true))
    }
}
