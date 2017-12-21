package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;

import java.nio.file.Paths;

public class DirectiveEngine {

    public JsonObject event(JsonObject jsonEvent) {
        try {
            // TODO: integrate with real GEF events
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

            oh.reloadRulesFromDir(Paths.get("rules"));

//            oh.printAsXML();

            oh.printSQWRL("q1", "Name(?u, ?name) -> sqwrl:select(?u, ?name)");
            oh.printSQWRL("q2", "allow(global, ?allowed) -> sqwrl:select(?allowed)");

//            SQWRLResult result = oh.runSQWRL("q10", "swrlb:add(?x, 2, 20) -> sqwrl:select(?x)");
//            if (result.next()) {
//                System.out.println("x: " + result.getLiteral("x").getInteger());
//            }

            // TODO: here we must return a json object to the event source service
        } catch (Exception e) {
            System.err.println("Error in rule engine: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
