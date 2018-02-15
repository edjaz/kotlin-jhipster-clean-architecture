package fr.edjaz.cucumber.stepdefs

import fr.edjaz.AppApp

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.ResultActions

import org.springframework.boot.test.context.SpringBootTest

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = arrayOf(AppApp::class))
abstract class StepDefs {

    protected var actions: ResultActions? = null

}
