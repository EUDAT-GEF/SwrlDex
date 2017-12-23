package eu.eudat.swrldex.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.swrlapi.drools.owl.properties.P;
import org.swrlapi.sqwrl.SQWRLResult;

import java.nio.file.Paths;

public class DirectiveEngine {

    public JsonObject event(JsonObject jsonEvent) {
        try {
            // TODO: test 2 simple rules with outcome in the ontology
            // TODO: assertive rules: return allow/disallow, environment changes
            // TODO: generative rules: create new service invocations

//            OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#", );
            OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#",
                    Paths.get("eventOntology.xml"));

            OntologyHelper.OClass inputCls = oh.cls("INPUT");
            OntologyHelper.OClass outputCls = oh.cls("OUTPUT");

            // root individual for all incoming data
            OntologyHelper.OIndividual input = oh.ind("input");
            input.addType(inputCls);

            // root individual for all outgoing data
            OntologyHelper.OIndividual output = oh.ind("output");
            output.addType(outputCls);

            new JsonLoader(oh).load(input, jsonEvent, output);

//            oh.saveAsXML(Paths.get("event.out.xml"));
            oh.print();

            oh.execRulesFromDir(Paths.get("rules"));

//            oh.printAsXML();

//            // hardcoded policy: an event is allowed by default unless there is at least one rule against it
//            SQWRLResult result = oh.runSQWRL("is_allowed", "allow(output, ?allowed) -> sqwrl:select(?allowed)");
//            boolean allowExists = false;
//            boolean allow = true;
//            while (result.next()) {
//                allowExists = true;
//                if (result.getLiteral("allowed").getBoolean() == false) {
//                    allow = false;
//                    break;
//                }
//            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            System.out.println("dumped input:");
            System.out.println(gson.toJson(new JsonDumper(oh).dump(input)));
            System.out.println("");

            System.out.println("dumped output:");
            System.out.println(gson.toJson(new JsonDumper(oh).dump(output)));
            System.out.println("");

            JsonObject jsonEventOutput = new JsonDumper(oh).dump(output);
            return jsonEventOutput;
        } catch (Exception e) {
            System.err.println("Error in rule engine: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
