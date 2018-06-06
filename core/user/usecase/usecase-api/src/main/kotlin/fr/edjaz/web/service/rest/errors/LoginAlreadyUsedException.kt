package fr.edjaz.web.service.rest.errors

class LoginAlreadyUsedException : BadRequestAlertException(ErrorConstants.LOGIN_ALREADY_USED_TYPE, "Login already in use", "userManagement", "userexists")
