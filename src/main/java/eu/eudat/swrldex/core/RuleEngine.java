package eu.eudat.swrldex.core;


import com.google.gson.JsonObject;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.jena.PelletReasonerFactory;

public class RuleEngine{

    public void event(JsonObject env) {
    }

    public void handleEvent(JsonObject env) {
        String ont = "http://owldl.com/ontologies/dl-safe.owl";

        OntModel model = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC, null );
        model.read( ont );

        ObjectProperty sibling = model.getObjectProperty( ont + "#sibling" );

        OntClass BadChild = model.getOntClass( ont + "#BadChild" );
        OntClass Child = model.getOntClass( ont + "#Child" );

        Individual Abel = model.getIndividual( ont + "#Abel" );
        Individual Cain = model.getIndividual( ont + "#Cain" );
        Individual Remus = model.getIndividual( ont + "#Remus" );
        Individual Romulus = model.getIndividual( ont + "#Romulus" );

        model.prepare();

        // Cain has sibling Abel due to SiblingRule
        println(Cain, sibling);
        // Abel has sibling Cain due to SiblingRule and rule works symmetric
        println(Abel, sibling);
        // Remus is not inferred to have a sibling because his father is not
        // known
        println(Remus, sibling);
        // No siblings for Romulus for same reasons
        println(Romulus, sibling);

        // Cain is a BadChild due to BadChildRule
        println(BadChild);
        // Cain is a Child due to BadChildRule and ChildRule2
        // Oedipus is a Child due to ChildRule1 and ChildRule2 combined with the
        // unionOf type
        println(Child);
    }

    void println(Individual i, ObjectProperty op) {
        System.out.println(i.toString() + " " + (op != null ? op : ""));
    }
    void println(OntClass oc) {
        System.out.println(oc.toString());
    }
}
