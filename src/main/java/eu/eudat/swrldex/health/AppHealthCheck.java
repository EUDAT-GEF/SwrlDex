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

    private Gson gson = new GsonBuilder().create();

    private DirectiveEngine engine;

    public AppHealthCheck(DirectiveEngine engine) {
        this.engine = engine;
    }


    @Override
    public Result check() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get("test_event.json"));
            String input = new String(bytes, "UTF-8");
            JsonObject in = gson.fromJson(input, JsonObject.class);
            JsonObject out = engine.event(in);

            return Result.healthy("Generated output: \n" + gson.toJson(out));
        } catch (Exception ex) {
            log.error("exception while healthchecking: ", ex);
            ex.printStackTrace();
            return Result.unhealthy(ex.getMessage());
        }
    }
}
