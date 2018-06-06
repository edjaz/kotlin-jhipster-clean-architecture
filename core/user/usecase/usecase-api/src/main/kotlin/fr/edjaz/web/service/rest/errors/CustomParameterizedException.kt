package fr.edjaz.web.service.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional

import java.util.HashMap

import org.zalando.problem.Status.BAD_REQUEST

/**
 * Custom, parameterized exception, which can be translated on the client side.
 * For example:
 *
 * <pre>
 * throw new CustomParameterizedException(&quot;myCustomError&quot;, &quot;hello&quot;, &quot;world&quot;);
</pre> *
 *
 * Can be translated with:
 *
 * <pre>
 * "error.myCustomError" :  "The server says {{param0}} to {{param1}}"
</pre> *
 */
class CustomParameterizedException(message: String, paramMap: Map<String, Any>) : AbstractThrowableProblem(ErrorConstants.PARAMETERIZED_TYPE, "Parameterized Exception", BAD_REQUEST, null, null, null, toProblemParameters(message, paramMap)) {
    override fun getCause(): Exceptional? {
        if(cause==null){
            return null
        }
        return super.cause as Exceptional
    }
    constructor(message: String, vararg params: String) : this(message, toParamMap(*params)) {}

    companion object {

        private val serialVersionUID = 1L

        private val PARAM = "param"

        fun toParamMap(vararg params: String): Map<String, Any> {
            val paramMap = HashMap<String, Any>()
            if (params != null && params.size > 0) {
                for (i in params.indices) {
                    paramMap[PARAM + i] = params[i]
                }
            }
            return paramMap
        }

        fun toProblemParameters(message: String, paramMap: Map<String, Any>): Map<String, Any> {
            val parameters = HashMap<String, Any>()
            parameters["message"] = message
            parameters["params"] = paramMap
            return parameters
        }
    }
}
