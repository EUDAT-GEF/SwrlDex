package eu.eudat.swrldex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.eudat.swrldex.core.DirectiveEngine;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api")
public class API {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(API.class);

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private DirectiveEngine engine;

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
            JsonObject env = gson.fromJson(input, JsonObject.class);
            JsonObject ret = engine.event(env);
            String retstr = gson.toJson(ret);
            return Response.ok(retstr).build();
        } catch (Exception ex) {
            log.error("exception: ", ex);
            return Response.serverError().build();
        }
    }
}
