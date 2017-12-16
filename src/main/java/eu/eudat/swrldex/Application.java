package eu.eudat.swrldex;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.eudat.swrldex.core.RuleEngine;
import eu.eudat.swrldex.health.AppHealthCheck;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;


public class Application extends io.dropwizard.Application<Configuration> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        new Application().run(args);


        try {
            byte[] bytes = Files.readAllBytes(Paths.get("event.json"));
            String input = new String(bytes, "UTF-8");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject env = gson.fromJson(input, JsonObject.class);
            new RuleEngine().event(env);
        } catch (Exception ex) {
            log.error("exception: ", ex);
            ex.printStackTrace();
        }
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
