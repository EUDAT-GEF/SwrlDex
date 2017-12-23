package eu.eudat.swrldex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.eudat.swrldex.core.DirectiveEngine;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class API {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(API.class);
    DirectiveEngine engine = new DirectiveEngine();

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
    public String acceptEvent(String input) {
        try {
            System.out.println("--- event");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject env = gson.fromJson(input, JsonObject.class);
            System.out.println("    input json: \n" + gson.toJson(env));

            JsonObject ret = engine.event(env);
            System.out.println("    output json: \n" + gson.toJson(ret));

            return gson.toJson(ret);
        } catch (Exception ex) {
            log.error("exception: ", ex);
            ex.printStackTrace();
            return "{'error':'Internal error'}";
        }
    }
}

