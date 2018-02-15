package fr.edjaz.cucumber

import org.junit.runner.RunWith


import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber

@RunWith(Cucumber::class)
@CucumberOptions(plugin = arrayOf("pretty"), features = arrayOf("src/test/features"))
class CucumberTest
