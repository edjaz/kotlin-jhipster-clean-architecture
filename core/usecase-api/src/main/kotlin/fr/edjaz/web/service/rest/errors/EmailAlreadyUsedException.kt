package fr.edjaz.web.service.rest.errors

class EmailAlreadyUsedException : BadRequestAlertException(ErrorConstants.EMAIL_ALREADY_USED_TYPE, "Email address already in use", "userManagement", "emailexists")
