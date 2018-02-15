package fr.edjaz.web.rest

import fr.edjaz.web.config.Constants
import fr.edjaz.web.service.social.CreateSocialUser
import org.slf4j.LoggerFactory
import org.springframework.social.connect.web.ProviderSignInUtils
import org.springframework.social.support.URIBuilder
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.view.RedirectView

@RestController
@RequestMapping("/social")
class SocialController(private val providerSignInUtils: ProviderSignInUtils
,private val createSocialUser: CreateSocialUser) {

    private val log = LoggerFactory.getLogger(SocialController::class.java)

    @GetMapping("/signup")
    fun signUp(webRequest: WebRequest, @CookieValue(name = "NG_TRANSLATE_LANG_KEY", required = false, defaultValue = Constants.DEFAULT_LANGUAGE) langKey: String): RedirectView {
        try {
            val connection = providerSignInUtils.getConnectionFromSession(webRequest)
            createSocialUser.execute(connection, langKey.replace("\"", ""))
            return RedirectView(URIBuilder.fromUri("/#/social-register/" + connection.key.providerId)
                    .queryParam("success", "true")
                    .build().toString(), true)
        } catch (e: Exception) {
            log.error("Exception creating social user: ", e)
            return RedirectView(URIBuilder.fromUri("/#/social-register/no-provider")
                    .queryParam("success", "false")
                    .build().toString(), true)
        }

    }
}
