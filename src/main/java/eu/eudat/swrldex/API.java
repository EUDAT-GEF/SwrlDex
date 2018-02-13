package eu.eudat.swrldex;

import com.google.gson.*;
import eu.eudat.swrldex.core.DirectiveEngine;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/api")
public class API {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(API.class);

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private DirectiveEngine engine;
    ExecutorService executorService = Executors.newCachedThreadPool();

    public API(DirectiveEngine engine) {
        this.engine = engine;
    }

    @GET
    @Path("/info")
    @Produces(MediaType.TEXT_PLAIN)
    public String getInfo() {
        return "info";
    }

    @POST
    @Path("/events")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response acceptEvent(String input) {
        try {
            final JsonObject env = gson.fromJson(input, JsonObject.class);
            JsonObject ret;
            if (isPrecedingEvent(env)) {
                ret = engine.event(env);
            } else {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        engine.event(env);
                    }
                });
                ret = new JsonObject();
            }
            String retstr = gson.toJson(ret);
            return Response.ok(retstr).build();
        } catch (Exception ex) {
            log.error("exception: ", ex);
            return Response.serverError().build();
        }
    }

    private boolean isPrecedingEvent(JsonObject env) {
        boolean _default = true;
        JsonElement event = env.get("event");
        if (!event.isJsonObject()) {
            return _default ;
        }
        JsonElement precedingEl = event.getAsJsonObject().get("preceding");
        if (!precedingEl.isJsonPrimitive()) {
            return _default ;
        }
        JsonPrimitive preceding = precedingEl.getAsJsonPrimitive();
        if (!preceding.isBoolean()) {
            return _default ;
        }
        return preceding.getAsBoolean();
    }
}
