package eu.eudat.swrldex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.eudat.swrldex.core.DirectiveEngine;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("")
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
            System.out.println("    input: \n" + input);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject env = gson.fromJson(input, JsonObject.class);
            System.out.println("    json: \n" + env);

            JsonObject ret = engine.event(env);

            return gson.toJson(ret);
        } catch (Exception ex) {
            log.error("exception: ", ex);
            ex.printStackTrace();
            return "{'error':'Internal error'}";
        }
    }
}

