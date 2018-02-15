package fr.edjaz.web.rest.errors

import fr.edjaz.web.rest.util.HeaderUtil
import fr.edjaz.web.service.rest.errors.BadRequestAlertException
import fr.edjaz.web.service.rest.errors.ErrorConstants
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.DefaultProblem
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.spring.web.advice.ProblemHandling
import org.zalando.problem.spring.web.advice.validation.ConstraintViolationProblem
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807)
 */
@ControllerAdvice
class ExceptionTranslator : ProblemHandling {

    /**
     * Post-process Problem payload to add the message key for front-end if needed
     */
    override fun process(entity: ResponseEntity<Problem>, request: NativeWebRequest): ResponseEntity<Problem>? {
        if (entity == null || entity.body == null) {
            return entity
        }
        val problem = entity.body
        if (!(problem is ConstraintViolationProblem || problem is DefaultProblem)) {
            return entity
        }
        val builder = Problem.builder()
            .withType(if (Problem.DEFAULT_TYPE == problem.type) ErrorConstants.DEFAULT_TYPE else problem.type)
            .withStatus(problem.status)
            .withTitle(problem.title)
            .with("path", request.getNativeRequest(HttpServletRequest::class.java).requestURI)

        if (problem is ConstraintViolationProblem) {
            builder
                .with("violations", problem.violations)
                .with("message", ErrorConstants.ERR_VALIDATION)
            return ResponseEntity(builder.build(), entity.headers, entity.statusCode)
        } else {
            builder
                .withCause((problem as DefaultProblem).cause)
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance())
            problem.getParameters().forEach({ key, value -> builder.with(key, value) })
            if (!problem.getParameters().containsKey("message") && problem.getStatus() != null) {
                builder.with("message", "error.http." + problem.getStatus()!!.statusCode)
            }
            return ResponseEntity(builder.build(), entity.headers, entity.statusCode)
        }
    }

    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, request: NativeWebRequest): ResponseEntity<Problem> {
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors.stream()
            .map { f -> FieldErrorVM(f.objectName, f.field, f.code) }
            .collect(Collectors.toList())

        val problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with("message", ErrorConstants.ERR_VALIDATION)
            .with("fieldErrors", fieldErrors)
            .build()
        return create(ex, problem, request)
    }

    @ExceptionHandler(BadRequestAlertException::class)
    fun handleBadRequestAlertException(ex: BadRequestAlertException, request: NativeWebRequest): ResponseEntity<Problem> {
        return create(ex, request, HeaderUtil.createFailureAlert(ex.entityName, ex.errorKey, ex.message!!))
    }

    @ExceptionHandler(ConcurrencyFailureException::class)
    fun handleConcurrencyFailure(ex: ConcurrencyFailureException, request: NativeWebRequest): ResponseEntity<Problem> {
        val problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with("message", ErrorConstants.ERR_CONCURRENCY_FAILURE)
            .build()
        return create(ex, problem, request)
    }
}
