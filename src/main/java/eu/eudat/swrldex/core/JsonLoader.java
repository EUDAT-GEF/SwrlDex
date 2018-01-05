package eu.eudat.swrldex.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

class JsonLoader {

    private OntologyHelper oh;

    JsonLoader(OntologyHelper oh) {
        this.oh = oh;
    }

    public void load(OntologyHelper.OIndividual parent, JsonObject object, OntologyHelper.OIndividual parentOut) {
        for (Map.Entry<String, JsonElement> e : object.entrySet()) {
//            System.out.println("> " + e);
            String prop = e.getKey();
            JsonElement obj = e.getValue();
            addJsonElementProp(parent, prop, obj, parentOut);
        }
    }

    private void addJsonElementProp(OntologyHelper.OIndividual parent, String prop, JsonElement obj,
                                    OntologyHelper.OIndividual parentOut) {
        if (obj.isJsonNull()) {
            // ignore this
        } else if (obj.isJsonPrimitive()) {
            addJsonPrimitiveProp(parent, prop, obj.getAsJsonPrimitive());
        } else if (obj.isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray();
            for (JsonElement x: arr) {
                addJsonElementProp(parent, prop, x, parentOut);
            }
        } else if (obj.isJsonObject()) {
            OntologyHelper.OClass cls = oh.cls(prop.toUpperCase());
            OntologyHelper.OIndividual ind = oh.ind(newIndividual(prop));
            OntologyHelper.OIndividual indOut = oh.ind(newIndividual(prop));

            ind.addType(cls);
            parent.addProp(prop, ind);
            parentOut.addProp(prop, indOut);

            for (Map.Entry<String, JsonElement> e: obj.getAsJsonObject().entrySet()) {
//                System.out.println(">    " + e);
                addJsonElementProp(ind, e.getKey(), e.getValue(), indOut);
            }
        } else {
            throw new IllegalStateException("should never get here");
        }
    }

    private void addJsonPrimitiveProp(OntologyHelper.OIndividual parent, String prop, JsonPrimitive p) {
        if (p.isNumber()) {
            parent.addProp(prop, p.getAsNumber().doubleValue());
        } else if (p.isBoolean()) {
            parent.addProp(prop, p.getAsBoolean());
        } else {
            parent.addProp(prop, p.getAsString());
        }
    }

    private Map<String, Integer> counterMap = new HashMap<>();
    private String newIndividual(String prop) {
        if (!counterMap.containsKey(prop)) {
            counterMap.put(prop, 1);
        }
        int idx = counterMap.get(prop);
        counterMap.put(prop, idx+1);

        return idx == 1 ? prop : (prop + "_" + idx);
    }
}
