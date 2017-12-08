package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;
import org.swrlapi.sqwrl.SQWRLResult;

public class RuleEngine {

    public void event(JsonObject env) {
        try {
            OntologyHelper oh = new OntologyHelper("test", "http://www.example.com/test");
            oh.addSubClass("Human", "Male");
            oh.addSubClass("Human", "Female");
            oh.addType("Male", "John");
            oh.addType("Female", "Mary");
            oh.addProp("hasChild", "John", "Bob");
            oh.addProp("hasWife", "John", "Mary");
            oh.print();

//            TODO: not working yet
//            oh.addRule("hasChild(?x, ?y) ^ hasSpouse(?x, ?z) -> hasChild(sqwrl:select(?x, ?y)");

            oh.printSQWRL("q1", "Female(?name) -> sqwrl:select(?name)");
            oh.printSQWRL("q2", "hasChild(?parent, ?child) -> sqwrl:select(?parent, ?child)");
            oh.printSQWRL("q3", "hasWife(?x, ?wife) ^ hasChild(?x, ?child) -> sqwrl:select(?wife, ?child)");

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

