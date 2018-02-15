package fr.edjaz.web.rest.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.format.support.DefaultFormattingConversionService
import org.springframework.format.support.FormattingConversionService
import org.springframework.http.MediaType

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException

import org.assertj.core.api.Assertions.assertThat

/**
 * Utility class for testing REST controllers.
 */
object TestUtil {

    /** MediaType for JSON UTF8  */
    val APPLICATION_JSON_UTF8 = MediaType(
            MediaType.APPLICATION_JSON.type,
            MediaType.APPLICATION_JSON.subtype, StandardCharsets.UTF_8)

    /**
     * Convert an object to JSON byte array.
     *
     * @param object
     * the object to convert
     * @return the JSON byte array
     * @throws IOException
     */
    @Throws(IOException::class)
    fun convertObjectToJsonBytes(`object`: Any): ByteArray {
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        val module = JavaTimeModule()
        mapper.registerModule(module)

        return mapper.writeValueAsBytes(`object`)
    }

    /**
     * Create a byte array with a specific size filled with specified data.
     *
     * @param size the size of the byte array
     * @param data the data to put in the byte array
     * @return the JSON byte array
     */
    fun createByteArray(size: Int, data: String): ByteArray {
        val byteArray = ByteArray(size)
        for (i in 0 until size) {
            byteArray[i] = java.lang.Byte.parseByte(data, 2)
        }
        return byteArray
    }

    /**
     * A matcher that tests that the examined string represents the same instant as the reference datetime.
     */
    class ZonedDateTimeMatcher(private val date: ZonedDateTime) : TypeSafeDiagnosingMatcher<String>() {

        override fun matchesSafely(item: String, mismatchDescription: Description): Boolean {
            try {
                if (!date.isEqual(ZonedDateTime.parse(item))) {
                    mismatchDescription.appendText("was ").appendValue(item)
                    return false
                }
                return true
            } catch (e: DateTimeParseException) {
                mismatchDescription.appendText("was ").appendValue(item)
                        .appendText(", which could not be parsed as a ZonedDateTime")
                return false
            }

        }

        override fun describeTo(description: Description) {
            description.appendText("a String representing the same Instant as ").appendValue(date)
        }
    }

    /**
     * Creates a matcher that matches when the examined string reprensents the same instant as the reference datetime
     * @param date the reference datetime against which the examined string is checked
     */
    fun sameInstant(date: ZonedDateTime): ZonedDateTimeMatcher {
        return ZonedDateTimeMatcher(date)
    }

    /**
     * Verifies the equals/hashcode contract on the domain object.
     */
    @Throws(Exception::class)
    fun equalsVerifier(clazz: Class<*>) {
        val domainObject1 = clazz.getConstructor().newInstance()
        assertThat(domainObject1.toString()).isNotNull()
        assertThat(domainObject1).isEqualTo(domainObject1)
        assertThat(domainObject1.hashCode()).isEqualTo(domainObject1.hashCode())
        // Test with an instance of another class
        val testOtherObject = Any()
        assertThat(domainObject1).isNotEqualTo(testOtherObject)
        assertThat(domainObject1).isNotEqualTo(null)
        // Test with an instance of the same class
        val domainObject2 = clazz.getConstructor().newInstance()
        assertThat(domainObject1).isNotEqualTo(domainObject2)
        // HashCodes are equals because the objects are not persisted yet
        assertThat(domainObject1.hashCode()).isEqualTo(domainObject2.hashCode())
    }

    /**
     * Create a FormattingConversionService which use ISO date format, instead of the localized one.
     * @return the FormattingConversionService
     */
    fun createFormattingConversionService(): FormattingConversionService {
        val dfcs = DefaultFormattingConversionService()
        val registrar = DateTimeFormatterRegistrar()
        registrar.setUseIsoFormat(true)
        registrar.registerFormatters(dfcs)
        return dfcs
    }
}
