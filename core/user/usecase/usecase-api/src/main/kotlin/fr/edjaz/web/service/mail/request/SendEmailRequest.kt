package fr.edjaz.web.service.mail.request

data class SendEmailRequest(
        val to: String,
        val subject: String,
        val content: String,
        val isMultipart: Boolean,
        val isHtml: Boolean
)
