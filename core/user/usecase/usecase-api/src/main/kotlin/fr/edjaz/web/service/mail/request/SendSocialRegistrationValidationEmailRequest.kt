package fr.edjaz.web.service.mail.request

data class SendSocialRegistrationValidationEmailRequest(
    val provider: String,
    val user: MailUser
)
