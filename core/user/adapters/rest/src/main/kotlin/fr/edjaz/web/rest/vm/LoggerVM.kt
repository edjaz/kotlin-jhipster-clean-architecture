package fr.edjaz.web.rest.vm

import ch.qos.logback.classic.Logger

/**
 * View Model object for storing a Logback logger.
 */
class LoggerVM {

    var name: String? = null

    var level: String? = null

    constructor(logger: Logger) {
        this.name = logger.name
        this.level = logger.effectiveLevel.toString()
    }

    constructor() {
        // Empty public constructor used by Jackson.
    }

    override fun toString(): String {
        return "LoggerVM{" +
                "name='" + name + '\''.toString() +
                ", level='" + level + '\''.toString() +
                '}'.toString()
    }
}
