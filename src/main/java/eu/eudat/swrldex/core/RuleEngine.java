package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;
import org.swrlapi.sqwrl.SQWRLResult;

import java.nio.file.Paths;

public class RuleEngine {

    public void event(JsonObject env) {
//        System.out.println("    json env: \n" + env);
        try {
            OntologyHelper oh = new OntologyHelper("test", "http://www.example.com/test");
            oh.addSubClass("Entity", "Environment");
            oh.addType("Environment", "global");

//            JsonLoader jsonLoader = new JsonLoader(oh);
//            jsonLoader.load("global", env);
//            oh.print();

            oh.addType("Male", "John");
            oh.addType("Female", "Mary");
            oh.addProp("John", "hasChild", "Bob");
            oh.addProp("John", "hasWife", "Mary");


            oh.addRule("r1", "hasChild(?x, ?y) ^ hasWife(?x, ?z) -> hasChild(?z, ?y)");

//            oh.print();
            oh.saveAsXML(Paths.get("event.xml"));

//            oh.printSQWRL("q1", "Female(?name) -> sqwrl:select(?name)");
            oh.printSQWRL("q2", "hasChild(?parent, ?child) -> sqwrl:select(?parent, ?child)");

            SQWRLResult result = oh.runSQWRL("q10", "swrlb:add(?x, 2, 20) -> sqwrl:select(?x)");
            if (result.next()) {
                System.out.println("x: " + result.getLiteral("x").getInteger());
            }

        } catch (Exception e) {
            System.err.println("Error in rule engine: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

