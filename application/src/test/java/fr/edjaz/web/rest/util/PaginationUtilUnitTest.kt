package fr.edjaz.web.rest.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

import java.util.ArrayList

import org.junit.Test
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders

/**
 * Tests based on parsing algorithm in app/components/util/pagination-util.service.js
 *
 * @see PaginationUtil
 */
class PaginationUtilUnitTest {

    @Test
    fun generatePaginationHttpHeadersTest() {
        val baseUrl = "/api/_search/example"
        val content = ArrayList<String>()
        val page = PageImpl(content, PageRequest(6, 50), 400L)
        val headers = PaginationUtil.generatePaginationHttpHeaders(page, baseUrl)
        val strHeaders = headers[HttpHeaders.LINK]
        assertNotNull(strHeaders)
        assertTrue(strHeaders!!.size == 1)
        val headerData = strHeaders.get(0)
        assertTrue(headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 4)
        val expectedData = ("</api/_search/example?page=7&size=50>; rel=\"next\","
                + "</api/_search/example?page=5&size=50>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50>; rel=\"last\","
                + "</api/_search/example?page=0&size=50>; rel=\"first\"")
        assertEquals(expectedData, headerData)
        val xTotalCountHeaders = headers["X-Total-Count"]
        assertTrue(xTotalCountHeaders!!.size == 1)
        assertTrue(java.lang.Long.valueOf(xTotalCountHeaders.get(0)) == 400L)
    }

    @Test
    fun commaTest() {
        val baseUrl = "/api/_search/example"
        val content = ArrayList<String>()
        val page = PageImpl(content)
        val query = "Test1, test2"
        val headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl)
        val strHeaders = headers[HttpHeaders.LINK]
        assertNotNull(strHeaders)
        assertTrue(strHeaders!!.size == 1)
        val headerData = strHeaders.get(0)
        assertTrue(headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 2)
        val expectedData = "</api/_search/example?page=0&size=0&query=Test1%2C+test2>; rel=\"last\"," + "</api/_search/example?page=0&size=0&query=Test1%2C+test2>; rel=\"first\""
        assertEquals(expectedData, headerData)
        val xTotalCountHeaders = headers["X-Total-Count"]
        assertTrue(xTotalCountHeaders!!.size == 1)
        assertTrue(java.lang.Long.valueOf(xTotalCountHeaders.get(0)) == 0L)
    }

    @Test
    fun multiplePagesTest() {
        val baseUrl = "/api/_search/example"
        val content = ArrayList<String>()

        // Page 0
        var page: Page<String> = PageImpl(content, PageRequest(0, 50), 400L)
        var query = "Test1, test2"
        var headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl)
        var strHeaders = headers[HttpHeaders.LINK]
        assertNotNull(strHeaders)
        assertTrue(strHeaders!!.size == 1)
        var headerData = strHeaders.get(0)
        assertTrue(headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 3)
        var expectedData = ("</api/_search/example?page=1&size=50&query=Test1%2C+test2>; rel=\"next\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"")
        assertEquals(expectedData, headerData)
        var xTotalCountHeaders = headers["X-Total-Count"]
        assertTrue(xTotalCountHeaders!!.size == 1)
        assertTrue(java.lang.Long.valueOf(xTotalCountHeaders.get(0)) == 400L)

        // Page 1
        page = PageImpl(content, PageRequest(1, 50), 400L)
        query = "Test1, test2"
        headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl)
        strHeaders = headers[HttpHeaders.LINK]
        assertNotNull(strHeaders)
        assertTrue(strHeaders!!.size == 1)
        headerData = strHeaders.get(0)
        assertTrue(headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 4)
        expectedData = ("</api/_search/example?page=2&size=50&query=Test1%2C+test2>; rel=\"next\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"")
        assertEquals(expectedData, headerData)
        xTotalCountHeaders = headers["X-Total-Count"]
        assertTrue(xTotalCountHeaders!!.size == 1)
        assertTrue(java.lang.Long.valueOf(xTotalCountHeaders.get(0)) == 400L)

        // Page 6
        page = PageImpl(content, PageRequest(6, 50), 400L)
        headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl)
        strHeaders = headers[HttpHeaders.LINK]
        assertNotNull(strHeaders)
        assertTrue(strHeaders!!.size == 1)
        headerData = strHeaders.get(0)
        assertTrue(headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 4)
        expectedData = ("</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"next\","
                + "</api/_search/example?page=5&size=50&query=Test1%2C+test2>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"")
        assertEquals(expectedData, headerData)
        xTotalCountHeaders = headers["X-Total-Count"]
        assertTrue(xTotalCountHeaders!!.size == 1)
        assertTrue(java.lang.Long.valueOf(xTotalCountHeaders.get(0)) == 400L)

        // Page 7
        page = PageImpl(content, PageRequest(7, 50), 400L)
        headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl)
        strHeaders = headers[HttpHeaders.LINK]
        assertNotNull(strHeaders)
        assertTrue(strHeaders!!.size == 1)
        headerData = strHeaders.get(0)
        assertTrue(headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 3)
        expectedData = ("</api/_search/example?page=6&size=50&query=Test1%2C+test2>; rel=\"prev\","
                + "</api/_search/example?page=7&size=50&query=Test1%2C+test2>; rel=\"last\","
                + "</api/_search/example?page=0&size=50&query=Test1%2C+test2>; rel=\"first\"")
        assertEquals(expectedData, headerData)
    }

    @Test
    fun greaterSemicolonTest() {
        val baseUrl = "/api/_search/example"
        val content = ArrayList<String>()
        val page = PageImpl(content)
        val query = "Test>;test"
        val headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, baseUrl)
        val strHeaders = headers[HttpHeaders.LINK]
        assertNotNull(strHeaders)
        assertTrue(strHeaders!!.size == 1)
        val headerData = strHeaders.get(0)
        assertTrue(headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 2)
        val linksData = headerData.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        assertTrue(linksData.size == 2)
        assertTrue(linksData[0].split(">;".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 2)
        assertTrue(linksData[1].split(">;".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 2)
        val expectedData = "</api/_search/example?page=0&size=0&query=Test%3E%3Btest>; rel=\"last\"," + "</api/_search/example?page=0&size=0&query=Test%3E%3Btest>; rel=\"first\""
        assertEquals(expectedData, headerData)
        val xTotalCountHeaders = headers["X-Total-Count"]
        assertTrue(xTotalCountHeaders!!.size == 1)
        assertTrue(java.lang.Long.valueOf(xTotalCountHeaders.get(0)) == 0L)
    }
}
