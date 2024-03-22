package connector;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.testcontainers.junit.jupiter.Testcontainers;

@RunWith(Cucumber.class)
@CucumberOptions(
		plugin = {"pretty", "html:target/cucumber-report.html"},
		features = {"src/test/resources/features"}
)
@Testcontainers
public class RunCucumberTest {
}
