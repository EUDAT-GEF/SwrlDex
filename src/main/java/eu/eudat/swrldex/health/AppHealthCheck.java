package eu.eudat.swrldex.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.eudat.swrldex.core.DirectiveEngine;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

public class AppHealthCheck extends HealthCheck {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AppHealthCheck.class);

    @Override
    public Result check() throws Exception {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("event.json"));
            String input = new String(bytes, "UTF-8");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject env = gson.fromJson(input, JsonObject.class);
            new DirectiveEngine().event(env);
        } catch (Exception ex) {
            log.error("exception while healthchecking: ", ex);
            ex.printStackTrace();
            return Result.unhealthy(ex.getMessage());
        }
        return Result.healthy();
    }
}
