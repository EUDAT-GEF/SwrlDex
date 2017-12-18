package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;

public class RuleEngine {

    public void event(JsonObject eventObject) {
        try {
            OntologyHelper oh = new OntologyHelper("test", "http://www.example.com/test");
            oh.addSubClass("Entity", "Environment");
            oh.addIndividual("Environment", "global");
            oh.addIndividual("Entity", "temporary_entity");
            oh.addDataProp("temporary_entity", "allow", false);

            JsonLoader jsonLoader = new JsonLoader(oh);
            jsonLoader.load("global", eventObject);
//            oh.saveAsXML(Paths.get("event.xml"));

            oh.addRule("r1", "user(global, ?u) ^ Name(?u, \"John Doe\") -> allow(global, true)");
            oh.print();

            oh.printSQWRL("q1", "Entity(?entity) -> sqwrl:select(?entity)");
            oh.printSQWRL("q2", "allow(global, ?allowed) -> sqwrl:select(?allowed)");

//            SQWRLResult result = oh.runSQWRL("q10", "swrlb:add(?x, 2, 20) -> sqwrl:select(?x)");
//            if (result.next()) {
//                System.out.println("x: " + result.getLiteral("x").getInteger());
//            }
        } catch (Exception e) {
            System.err.println("Error in rule engine: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
