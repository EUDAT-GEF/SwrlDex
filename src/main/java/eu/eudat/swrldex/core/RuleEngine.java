package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;

import java.nio.file.Paths;

public class RuleEngine {

    public void event(JsonObject jsonEvent) {
        try {
            // TODO: put rules into files
            // TODO: integrate with GEF events
            // TODO: test 2 simple rules with outcome in the ontology
            // TODO: assertive rules: return allow/disallow, environment changes
            // TODO: generative rules: create new service invocations

            OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#");
            oh.addIndividual("ENTITY", "global");
            oh.addIndividual("OUTPUT", "output");
            oh.addDataProp("OUTPUT", "allow", false);

            JsonLoader jsonLoader = new JsonLoader(oh);
            jsonLoader.load("ENTITY", "global", jsonEvent);

            oh.saveAsXML(Paths.get("event.out.xml"));

//            OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#", Paths.get("event.out.xml"));

            oh.print();

            oh.setRule("r1", "dex:user(global, ?u) ^ dex:Name(?u, \"John Doe\") -> dex:allow(global, true)");
            oh.setRule("r1", "Name(?u, ?n) -> Name(?u, \"New Name\")");

//            oh.printAsXML();

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
