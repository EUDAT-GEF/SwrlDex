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

    public void load(String parentClass, String parentInd, JsonObject object) {
        for (Map.Entry<String, JsonElement> e : object.entrySet()) {
//            System.out.println("> " + e);
            String prop = e.getKey();
            JsonElement obj = e.getValue();
            addJsonElementProp(parentClass, parentInd, prop, obj);
        }
    }

    private void addJsonElementProp(String parentClass, String parentInd, String prop, JsonElement obj) {
        if (obj.isJsonNull()) {
            // ignore this
        } else if (obj.isJsonPrimitive()) {
            addJsonPrimitiveProp(parentInd, prop, obj.getAsJsonPrimitive());
        } else if (obj.isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray();
            for (JsonElement x: arr) {
                addJsonElementProp(parentClass, parentInd, prop, x);
            }
        } else if (obj.isJsonObject()) {
            String newClass = prop.toUpperCase();
            if (!oh.hasClass(newClass)) {
                oh.addSubClass(parentClass, newClass);
                oh.addProp(parentClass, prop, newClass);
            }

            String newInd = newIndividual(prop);
            oh.addIndividual(newClass, newInd);
            oh.addProp(parentInd, prop, newInd);
            for (Map.Entry<String, JsonElement> e: obj.getAsJsonObject().entrySet()) {
//                System.out.println(">    " + e);
                addJsonElementProp(parentClass, newInd, e.getKey(), e.getValue());
            }
        } else {
            throw new IllegalStateException("should never get here");
        }
    }

    private void addJsonPrimitiveProp(String parent, String prop, JsonPrimitive p) {
        if (p.isNumber()) {
            oh.addDataProp(parent, prop, p.getAsNumber().doubleValue());
        } else if (p.isBoolean()) {
            oh.addDataProp(parent, prop, p.getAsBoolean());
        } else {
            oh.addDataProp(parent, prop, p.getAsString());
        }
    }

    private static Map<String, Integer> counterMap = new HashMap<>();
    private String newIndividual(String prop) {
        if (!counterMap.containsKey(prop)) {
            counterMap.put(prop, 1);
        }
        int idx = counterMap.get(prop);
        counterMap.put(prop, idx+1);
        return prop + "_" + idx;
    }
}
