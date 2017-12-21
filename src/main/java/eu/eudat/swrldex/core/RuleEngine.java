package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;

public class RuleEngine {

    public void event(JsonObject jsonEvent) {
        try {
            OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#");
            oh.addSubClass("Entity", "Environment");
            oh.addIndividual("Environment", "global");
            oh.addIndividual("Entity", "temporary_entity");
            oh.addDataProp("temporary_entity", "allow", false);

            JsonLoader jsonLoader = new JsonLoader(oh);
            jsonLoader.load("global", jsonEvent);

//            oh.saveAsXML(Paths.get("event.out.xml"));
//            oh.saveAsJsonLD(Paths.get("event.out.json"));
//            oh.printAsXML();

//            OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#", Paths.get("event.out.xml"));

            oh.setRule("r1", "dex:user(global, ?u) ^ dex:Name(?u, \"John Doe\") -> dex:allow(global, true)");
            oh.setRule("r1", "Name(?u, ?n) -> Name(?u, \"New Name\")");

            oh.print();

            oh.printSQWRL("q1", "Name(?u, ?name) -> sqwrl:select(?u, ?name)");
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
