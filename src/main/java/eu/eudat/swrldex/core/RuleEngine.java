package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

public class RuleEngine {

    public void event(JsonObject env) {
        try {
            OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
//            JsonLdParserFactory.register(); // Really just needed once

//            IRI vcardIri = IRI.create("http://www.w3.org/2006/vcard/ns.jsonld");
//            OWLOntology ontology = ontologyManager.loadOntology(vcardIri);
            OWLOntology ontology = ontologyManager.createOntology();

            SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);
            SQWRLResult result = queryEngine.runSQWRLQuery("q1", "swrlb:add(?x, 2, 20) -> sqwrl:select(?x)");

//            ontologyManager.saveOntology(ontology, new JsonLdOntologyFormat(), System.out);

            // Process the SQWRL result
            if (result.next())
                System.out.println("x: " + result.getLiteral("x").getInteger());

        } catch (OWLOntologyCreationException e) {
            System.err.println("Error creating OWL ontology: " + e.getMessage());
            e.printStackTrace();
        } catch (SWRLParseException e) {
            System.err.println("Error parsing SWRL rule or SQWRL query: " + e.getMessage());
            e.printStackTrace();
        } catch (SQWRLException e) {
            System.err.println("Error running SWRL rule or SQWRL query: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            System.err.println("Error in rule engine: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
