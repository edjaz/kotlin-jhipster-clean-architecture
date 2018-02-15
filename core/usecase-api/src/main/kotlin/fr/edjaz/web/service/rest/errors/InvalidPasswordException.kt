package fr.edjaz.web.service.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status

class InvalidPasswordException : AbstractThrowableProblem(ErrorConstants.INVALID_PASSWORD_TYPE, "Incorrect password", Status.BAD_REQUEST) {
    override fun getCause(): Exceptional? {
        if(cause==null){
            return null
        }
        return super.cause as Exceptional
    }
}
