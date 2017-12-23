package eu.eudat.swrldex.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

public class JsonDumper {
    private OntologyHelper oh;

    public JsonDumper(OntologyHelper oh) {
        this.oh = oh;
    }

    public JsonObject dump(OntologyHelper.OIndividual ind) {
        JsonObject out = new JsonObject();
        for (Map.Entry<String, OntologyHelper.OIndividual> e : ind.getProps().entrySet()) {
            if (out.has(e.getKey())) {
                JsonElement x = out.get(e.getKey());
                System.out.println("overwriting " + e.getKey() + " = " + x + " -> " + e.getValue());
            }
            out.add(e.getKey(), dump(e.getValue()));
        }
        for (Map.Entry<String, JsonPrimitive> e : ind.getDataProps().entrySet()) {
            if (out.has(e.getKey())) {
                JsonElement x = out.get(e.getKey());
                System.out.println("overwriting " + e.getKey() + " = " + x + " -> " + e.getValue());
            }
            out.add(e.getKey(), e.getValue());
        }
        return out;
    }
}
