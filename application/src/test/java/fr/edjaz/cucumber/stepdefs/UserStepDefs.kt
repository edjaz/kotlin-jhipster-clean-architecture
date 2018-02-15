package fr.edjaz.cucumber.stepdefs

import cucumber.api.java.Before
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import fr.edjaz.web.rest.UserResource

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class UserStepDefs : StepDefs() {

    @Autowired
    private val userResource: UserResource? = null

    private var restUserMockMvc: MockMvc? = null

    @Before
    fun setup() {
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource).build()
    }

    @When("^I search user '(.*)'$")
    @Throws(Throwable::class)
    fun i_search_user_admin(userId: String) {
        actions = restUserMockMvc!!.perform(get("/api/users/" + userId)
                .accept(MediaType.APPLICATION_JSON))
    }

    @Then("^the user is found$")
    @Throws(Throwable::class)
    fun the_user_is_found() {
        actions!!
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
    }

    @Then("^his last name is '(.*)'$")
    @Throws(Throwable::class)
    fun his_last_name_is(lastName: String) {
        actions!!.andExpect(jsonPath("$.lastName").value(lastName))
    }

}
