package telraam.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import telraam.App;
import telraam.AppConfiguration;

import javax.validation.Validator;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BatonResourceTest {

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private static final Validator validator = Validators.newValidator();
    private static final YamlConfigurationFactory<AppConfiguration> factory =
            new YamlConfigurationFactory<>(AppConfiguration.class, validator, objectMapper, "dw");

    private static final String testConfigPath = ResourceHelpers.resourceFilePath("telraam/testConfig.yml");
    private static final DropwizardTestSupport<AppConfiguration> SUPPORT =
            new DropwizardTestSupport<>(App.class, testConfigPath);

    public static AppConfiguration loadTestConfig() throws IOException {
//        File file = new File(testConfigPath);
//        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()); // jackson databind
//        return mapper.readValue(file, AppConfiguration.class);
        return null;
    }

    @BeforeAll
    public static void migrateDb() throws IOException, ConfigurationException {

        final File yml = new File(testConfigPath);
        final AppConfiguration appConfiguration = factory.build(yml);

        //AppConfiguration appConfiguration = loadTestConfig();
        Flyway flyway = Flyway.configure().dataSource(
                appConfiguration.getDataSourceFactory().getUrl(),
                appConfiguration.getDataSourceFactory().getUser(),
                appConfiguration.getDataSourceFactory().getPassword()).load();
        flyway.migrate();

        SUPPORT.before();
    }

    @AfterAll
    public static void afterAll() {
        SUPPORT.after();
    }

    private static String createTempFile() {
        try {
            return File.createTempFile("test-example", null).getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testTest() {
        assertTrue(true);
    }
}
