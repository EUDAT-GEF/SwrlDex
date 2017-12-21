package eu.eudat.swrldex.core;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFJsonLDDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.DefaultIRIResolver;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class OntologyHelper {
    OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    OWLDataFactory df = OWLManager.getOWLDataFactory();
    PrefixManager pm = new DefaultPrefixManager();

    OWLOntology ontology;

    SWRLRuleEngine ruleEngine;
    SQWRLQueryEngine queryEngine;

    public OntologyHelper(String prefix, String namespace) throws OWLOntologyCreationException {
        this(prefix, namespace, null);
    }

    public OntologyHelper(String prefix, String namespace, Path path) throws OWLOntologyCreationException {
            if (path == null) {
            ontology = ontologyManager.createOntology();
        } else {
            FileDocumentSource source = new FileDocumentSource(path.toFile());
            ontology = ontologyManager.loadOntologyFromOntologyDocument(source);
        }

        pm.setDefaultPrefix(namespace);
        pm.setPrefix(prefix, namespace);
        OWLDocumentFormat odf = ontologyManager.getOntologyFormat(ontology);
        if (odf.isPrefixOWLOntologyFormat()) {
            odf.asPrefixOWLOntologyFormat().setPrefixManager(pm);
        }
//        printPrefixMap(pm);

        DefaultIRIResolver reir = new DefaultIRIResolver();
        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology, reir);

        DefaultIRIResolver qeir = new DefaultIRIResolver();
        queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology, qeir);

        for (Map.Entry<String, String> e : pm.getPrefixName2PrefixMap().entrySet()) {
            reir.setPrefix(e.getKey(), e.getValue());
            qeir.setPrefix(e.getKey(), e.getValue());
        }

//        printPrefixMap(getPrefixManager(reir));
//        printPrefixMap(getPrefixManager(qeir));
    }

    public void saveAsXML(Path path) throws OWLOntologyStorageException {
        FileDocumentTarget target = new FileDocumentTarget(path.toFile());
        ontologyManager.saveOntology(ontology, target);
    }

    public void saveAsJsonLD(Path path) throws OWLOntologyStorageException {
        OWLDocumentFormat jsonFormat = new RDFJsonLDDocumentFormat();
        FileDocumentTarget target = new FileDocumentTarget(path.toFile());
        ontologyManager.saveOntology(ontology, jsonFormat, target);
    }

    public void addSubClass(String parent, String child) {
        add(subClassAx(toClass(parent), toClass(child)));
    }

    public void addIndividual(String type, String individual) {
        add(typeAx(toClass(type), toInd(individual)));
    }

    public void addProp(String parent, String prop, String child) {
        add(propAx(toProp(prop), toInd(parent), toInd(child)));
    }

    public void addDataProp(String parent, String prop, String child) {
        add(dataPropAx(toDataProp(prop), toInd(parent), child));
    }

    public void addDataProp(String parent, String prop, double child) {
        add(dataPropAx(toDataProp(prop), toInd(parent), child));
    }

    public void addDataProp(String parent, String prop, boolean child) {
        add(dataPropAx(toDataProp(prop), toInd(parent), child));
    }

    public void setRule(String id, String ruleText) throws SWRLBuiltInException, SWRLParseException {
        ruleEngine.replaceSWRLRule(id, id, ruleText, "", true);
    }

    public void deleteRule(String id){
        ruleEngine.deleteSWRLRule(id);
    }

    OWLClass toClass(String name) {
        return df.getOWLClass(name, pm);
    };

    OWLNamedIndividual toInd(String name) {
        return df.getOWLNamedIndividual(name, pm);
    };

    OWLObjectProperty toProp(String name) {
        return df.getOWLObjectProperty(name, pm);
    };

    OWLDataProperty toDataProp(String name) {
        return df.getOWLDataProperty(name, pm);
    };

    OWLAxiom subClassAx(OWLClass parent, OWLClass child) {
        return df.getOWLSubClassOfAxiom(child, parent);
    }

    OWLAxiom typeAx(OWLClass class_, OWLNamedIndividual individual) {
        return df.getOWLClassAssertionAxiom(class_, individual);
    }

    OWLAxiom propAx(OWLObjectProperty p, OWLNamedIndividual parent, OWLNamedIndividual child) {
        return df.getOWLObjectPropertyAssertionAxiom(p, parent, child);
    }

    OWLAxiom dataPropAx(OWLDataProperty p, OWLNamedIndividual parent, double d) {
        return df.getOWLDataPropertyAssertionAxiom(p, parent, d);
    }

    OWLAxiom dataPropAx(OWLDataProperty p, OWLNamedIndividual parent, boolean b) {
        return df.getOWLDataPropertyAssertionAxiom(p, parent, b);
    }

    OWLAxiom dataPropAx(OWLDataProperty p, OWLNamedIndividual parent, String s) {
        return df.getOWLDataPropertyAssertionAxiom(p, parent, s);
    }

    SWRLRule ruleAx(Set<SWRLAtom> body, Set<SWRLAtom> head) {
        return df.getSWRLRule(body, head);
    }

    void add(OWLAxiom axiom) {
        AddAxiom addAxiom = new AddAxiom(ontology, axiom);
        ontologyManager.applyChange(addAxiom);
    }

    public SQWRLResult runSQWRL(String id, String queryText) throws SQWRLException, SWRLParseException {
        return queryEngine.runSQWRLQuery(id, queryText);
    }

    public void printSQWRL(String id, String queryText) throws SQWRLException, SWRLParseException {
        SQWRLResult result = runSQWRL(id, queryText);
        System.out.println(result.getColumnNames() + " " + id);
        System.out.println("---- ");
        while (result.next()) {
            System.out.println(result.getRow());
        }
        System.out.println(".");
    }

    public void printAsXML() throws OWLException {
        StringDocumentTarget target = new StringDocumentTarget();
        ontologyManager.saveOntology(ontology, target);
        System.out.println("---- ontology " + pm.getDefaultPrefix());
        System.out.println(target.toString());
        System.out.println("----");
    }

    void printPrefixes() {
        System.out.println("---- ontology prefixes:");
        OWLOntologyManager owlOntologyManager = ontology.getOWLOntologyManager();
        OWLDocumentFormat ontologyFormat = owlOntologyManager.getOntologyFormat(ontology);

        if (ontologyFormat != null && ontologyFormat.isPrefixOWLOntologyFormat()) {
            Map<String, String> map = ontologyFormat.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap();
            for (String prefix : map.keySet())
                System.out.println(prefix + " -> " + map.get(prefix));
        }
    }

    private static PrefixManager getPrefixManager(DefaultIRIResolver obj) {
        try {
            Field f = obj.getClass().getDeclaredField("prefixManager");
            f.setAccessible(true);
            return (PrefixManager) f.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void print() {
        System.out.println("---- ontology structure:");
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        try {
            printHierarchy(reasoner, df.getOWLThing(), 0, new HashSet<OWLClass>());
        } catch (OWLException e) {
            e.printStackTrace();
        }
    }

    private static void printPrefixMap(PrefixManager pm) {
        System.out.println("---- prefixManager prefix map:");
        if (pm == null) {
            System.out.println("Null prefixManager");
        } else {
            for (Map.Entry<String, String> e: pm.getPrefixName2PrefixMap().entrySet()) {
                System.out.println(e.getKey() + " -> " + e.getValue());
            }
        }
    }

    private static void printHierarchy(OWLReasoner r, OWLClass clazz, int level, Set<OWLClass> visited) throws OWLException {
        if (!visited.contains(clazz) && r.isSatisfiable(clazz)) {
            visited.add(clazz);
            for (int i = 0; i < level * 4; i++) {
                System.out.print(" ");
            }
            System.out.println(clazz.getIRI().getShortForm());

            NodeSet<OWLClass> classes = r.getSubClasses(clazz, true);
            for (OWLClass child :  classes.getFlattened()) {
                printHierarchy(r, child, level + 1, visited);
            }

            NodeSet<OWLNamedIndividual> instances = r.getInstances(clazz, true);
            for (OWLNamedIndividual ind :  instances.getFlattened()) {
                for (int i = 0; i < (level+1) * 4; i++) {
                    System.out.print(" ");
                }
                System.out.println("- " + ind.getIRI());
            }
        }
    }
}
