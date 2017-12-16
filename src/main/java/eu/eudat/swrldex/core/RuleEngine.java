package eu.eudat.swrldex.core;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.swrlapi.sqwrl.SQWRLResult;

import java.util.Map;

public class RuleEngine {

    public void event(JsonObject env) {
//        System.out.println("    json env: \n" + env);
        try {
            OntologyHelper oh = new OntologyHelper("test", "http://www.example.com/test");
            oh.addSubClass("Entity", "Environment");
            oh.addType("Environment", "global");

            String parent = "global";
            for (Map.Entry<String, JsonElement> e: env.entrySet()) {
                System.out.println(e);
                String prop = e.getKey();
                JsonElement obj = e.getValue();
                addJsonElementProp(oh, parent, prop, obj);
            }
            oh.print();

//            oh.addSubClass("Entity", "Event");
//            oh.addSubClass("Entity", "User");
//            oh.addSubClass("Entity", "Resource");
//            oh.addSubClass("Entity", "Environment");
//            String user = "#" + env.user.Email;
//            oh.addIndividual("User", user);


//            oh.addType("Male", "John");
//            oh.addType("Female", "Mary");
//            oh.addProp("John", "hasChild", "Bob");
//            oh.addProp("John", "hasWife", "Mary");
//            oh.print();

//            TODO: not working yet
//            oh.addRule("hasChild(?x, ?y) ^ hasSpouse(?x, ?z) -> hasChild(sqwrl:select(?x, ?y)");

//            oh.printSQWRL("q1", "Female(?name) -> sqwrl:select(?name)");
//            oh.printSQWRL("q2", "hasChild(?parent, ?child) -> sqwrl:select(?parent, ?child)");
//            oh.printSQWRL("q3", "hasWife(?x, ?wife) ^ hasChild(?x, ?child) -> sqwrl:select(?wife, ?child)");

            SQWRLResult result = oh.runSQWRL("q10", "swrlb:add(?x, 2, 20) -> sqwrl:select(?x)");
            if (result.next()) {
                System.out.println("x: " + result.getLiteral("x").getInteger());
            }

        } catch (Exception e) {
            System.err.println("Error in rule engine: " + e.getMessage());
            e.printStackTrace();
       }
    }

    static int counter = 1;

    private static void addJsonElementProp(OntologyHelper oh, String parent, String prop, JsonElement obj) {
        if (obj.isJsonNull()) {
            // ignore this
        } else if (obj.isJsonPrimitive()) {
            addJsonPrimitiveProp(oh, parent, prop, obj.getAsJsonPrimitive());
        } else if (obj.isJsonArray()) {
            JsonArray arr = obj.getAsJsonArray();
            for (JsonElement x: arr) {
                addJsonElementProp(oh, parent, prop, x);
            }
        } else if (obj.isJsonObject()) {
            String temp = prop + "_" + counter++;
            oh.addType("Entity", temp);
            oh.addProp(parent, prop, temp);
            for (Map.Entry<String, JsonElement> e: obj.getAsJsonObject().entrySet()) {
                System.out.println(e);
                addJsonElementProp(oh, temp, e.getKey(), e.getValue());
            }
        } else {
            throw new IllegalStateException("should never get here");
        }
    }

    private static void addJsonPrimitiveProp(OntologyHelper oh, String parent, String prop, JsonPrimitive p) {
        if (p.isNumber()) {
            oh.addDataProp(parent, prop, p.getAsNumber().doubleValue());
        } else if (p.isBoolean()) {
            oh.addDataProp(parent, prop, p.getAsBoolean());
        } else {
            oh.addDataProp(parent, prop, p.getAsString());
        }
    }
}
