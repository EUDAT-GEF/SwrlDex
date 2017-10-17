package eu.eudat.swrldex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.eudat.swrldex.core.Tutorial;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("")
public class API {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(API.class);

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
    public String acceptEvent(String jsonstring) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject env = gson.fromJson(jsonstring, JsonObject.class);

        try {
            System.out.println(env);
            Tutorial.test();
//            RuleEngine engine = new RuleEngine();
//            engine.event(env);
        } catch (Exception ex) {
            log.error("exception: ", ex);
            ex.printStackTrace();
        }
        return gson.toJson(env);
    }
}
