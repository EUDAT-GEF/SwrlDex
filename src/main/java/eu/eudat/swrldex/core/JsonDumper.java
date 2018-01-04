package eu.eudat.swrldex.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Map;

public class JsonDumper {
    private OntologyHelper oh;

    public JsonDumper(OntologyHelper oh) {
        this.oh = oh;
    }

    public JsonObject dump(OntologyHelper.OIndividual ind) {
        JsonObject out = new JsonObject();
        for (Map.Entry<String, List<OntologyHelper.OIndividual>> e : ind.getProps().entrySet()) {
            String k = e.getKey();
            List<OntologyHelper.OIndividual> vs = e.getValue();
            if (out.has(k)) {
                JsonElement x = out.get(k);
                System.out.println("overwriting " + k + " = " + x + " -> " + vs);
            }
            if (vs.size() == 1) {
                JsonObject o = dump(vs.get(0));
                if (o.size() > 0) {
                    out.add(k, o);
                }
            } else if (vs.size() > 1) {
                JsonArray a = new JsonArray();
                for (OntologyHelper.OIndividual i: vs) {
                    JsonObject o = dump(i);
                    if (o.size() > 0) {
                        out.add(k, o);
                    }
                }
                out.add(k, a);
            }
        }
        for (Map.Entry<String, List<JsonPrimitive>> e : ind.getDataProps().entrySet()) {
            String k = e.getKey();
            List<JsonPrimitive> vs = e.getValue();
            if (out.has(k)) {
                JsonElement x = out.get(k);
                System.out.println("overwriting " + k + " = " + x + " -> " + vs);
            }
            if (vs.size() == 1) {
                out.add(k, vs.get(0));
            } else if (vs.size() > 1) {
                JsonArray a = new JsonArray();
                for (JsonPrimitive i: vs) {
                    a.add(i);
                }
                out.add(k, a);
            }
        }
        return out;
    }
}
