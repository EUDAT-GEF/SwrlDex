package eu.eudat.swrldex;


import eu.eudat.swrldex.health.AppHealthCheck;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.LoggerFactory;


public class Application extends io.dropwizard.Application<Configuration> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
//        new Application().run(args);
        new AppHealthCheck().check();
    }

    @Override
    public void run(Configuration config, Environment env) {
        env.jersey().register(new API());
        env.healthChecks().register("SwrlDex", new AppHealthCheck());
        log.info("SwrlDex start");
    }

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }
}
