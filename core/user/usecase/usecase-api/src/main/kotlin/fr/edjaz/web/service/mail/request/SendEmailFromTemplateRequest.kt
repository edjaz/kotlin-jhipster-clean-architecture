package fr.edjaz.web.service.mail.request

data class SendEmailFromTemplateRequest(
        val templateName: String,
        val titleKey: String,
        val user: MailUser
)

