package fr.edjaz.web.websocket.dto

import java.time.Instant

/**
 * DTO for storing a user's activity.
 */
class ActivityDTO {

    var sessionId: String? = null

    var userLogin: String? = null

    var ipAddress: String? = null

    var page: String? = null

    var time: Instant? = null

    override fun toString(): String {
        return "ActivityDTO{" +
                "sessionId='" + sessionId + '\''.toString() +
                ", userLogin='" + userLogin + '\''.toString() +
                ", ipAddress='" + ipAddress + '\''.toString() +
                ", page='" + page + '\''.toString() +
                ", time='" + time + '\''.toString() +
                '}'.toString()
    }
}
