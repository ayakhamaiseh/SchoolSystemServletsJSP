package intern.schoolSystem.administration_system;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectPackages({"com.progressoft.administration_system.database", "com.progressoft.administration_system.subject",
        "com.progressoft.administration_system.student"})
public class TestSuite {

}
