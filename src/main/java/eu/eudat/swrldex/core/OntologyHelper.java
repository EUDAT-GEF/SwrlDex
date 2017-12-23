package eu.eudat.swrldex.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
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
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.DefaultIRIResolver;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class OntologyHelper {
    OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
    OWLDataFactory df = OWLManager.getOWLDataFactory();
    PrefixManager pm = new DefaultPrefixManager();

    OWLOntology ontology;

    SWRLRuleEngine ruleEngine;
    SQWRLQueryEngine queryEngine;

    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    OWLReasoner reasoner;


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

        reasoner = reasonerFactory.createReasoner(ontology);

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

    class OClass {
        OWLClass c;

        OClass(String name) {
            c = df.getOWLClass(name, pm);
            OWLAxiom ax = df.getOWLDeclarationAxiom(c);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        public void addParent(OClass parent) {
            OWLAxiom ax = df.getOWLSubClassOfAxiom(c, parent.c);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }
    }

    public OClass cls(String name) { return new OClass(name); }
    public OIndividual ind(String name) { return new OIndividual(name); }

    class OIndividual {
        OWLNamedIndividual i;
        OIndividual(String name) {
            i = df.getOWLNamedIndividual(name, pm);
            OWLAxiom ax = df.getOWLDeclarationAxiom(i);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        OIndividual(OWLNamedIndividual ind) {
            i = ind;
        }

        public void addType(OClass parent) {
            OWLAxiom ax = df.getOWLClassAssertionAxiom(parent.c, i);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        public void addProp(String prop, OIndividual obj) {
            OWLAxiom ax = df.getOWLObjectPropertyAssertionAxiom(getProp(prop), i, obj.i);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        public void addProp(String prop, boolean v) {
            OWLAxiom ax = df.getOWLDataPropertyAssertionAxiom(getDataProp(prop), i, v);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        public void addProp(String prop, int v) {
            OWLAxiom ax = df.getOWLDataPropertyAssertionAxiom(getDataProp(prop), i, v);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        public void addProp(String prop, double v) {
            OWLAxiom ax = df.getOWLDataPropertyAssertionAxiom(getDataProp(prop), i, v);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        public void addProp(String prop, String v) {
            OWLAxiom ax = df.getOWLDataPropertyAssertionAxiom(getDataProp(prop), i, v);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }

        public Map<String, OIndividual> getProps() {
            Map<String, OIndividual> ret = new HashMap<>();
            for (OWLObjectProperty p: ontology.getObjectPropertiesInSignature()) {
                for (OWLNamedIndividual v: reasoner.getObjectPropertyValues(i, p).getFlattened())
                    ret.put(p.getIRI().getShortForm(), new OIndividual(v));
            }
            return ret;
        }

        public Map<String, JsonPrimitive> getDataProps() {
            Map<String, JsonPrimitive> ret = new HashMap<>();
            for (OWLDataProperty p: ontology.getDataPropertiesInSignature()) {
                for (OWLLiteral v: reasoner.getDataPropertyValues(i, p))
                    if (v.isBoolean()) {
                        ret.put(p.getIRI().getShortForm(), new JsonPrimitive(v.parseBoolean()));
                    } else if (v.isInteger()) {
                        ret.put(p.getIRI().getShortForm(), new JsonPrimitive(v.parseInteger()));
                    } else if (v.isDouble() || v.isFloat()) {
                        ret.put(p.getIRI().getShortForm(), new JsonPrimitive(v.parseDouble()));
                    } else {
                        ret.put(p.getIRI().getShortForm(), new JsonPrimitive(v.getLiteral()));
                    }
            }
            return ret;
        }
    }

    private Map<String, OWLObjectProperty> propMap = new HashMap<>();
    private OWLObjectProperty getProp(String name) {
        if (propMap.containsKey(name)){
            return propMap.get(name);
        }
        OWLObjectProperty p = df.getOWLObjectProperty(name, pm);
        OWLAxiom ax = df.getOWLDeclarationAxiom(p);
        ontologyManager.applyChange(new AddAxiom(ontology, ax));
        propMap.put(name, p);
        return p;
    }

    private Map<String, OWLDataProperty> dataPropMap = new HashMap<>();
    private OWLDataProperty getDataProp(String name) {
        if (dataPropMap.containsKey(name)){
            return dataPropMap.get(name);
        }
        OWLDataProperty p = df.getOWLDataProperty(name, pm);
        OWLAxiom ax = df.getOWLDeclarationAxiom(p);
        ontologyManager.applyChange(new AddAxiom(ontology, ax));
        dataPropMap.put(name, p);
        return p;
    }

    class ODataProp {
        OWLDataProperty p;
        ODataProp(String name) {
            p = df.getOWLDataProperty(name, pm);
            OWLAxiom ax = df.getOWLDeclarationAxiom(p);
            ontologyManager.applyChange(new AddAxiom(ontology, ax));
        }
    }

    public ODataProp dataProp(String name) { return new ODataProp(name); }

    public void setRule(String id, String ruleText) throws SWRLBuiltInException, SWRLParseException {
        ruleEngine.replaceSWRLRule(id, id, ruleText, "", true);
    }

    public void deleteRule(String id){
        ruleEngine.deleteSWRLRule(id);
    }

    public void execRulesFromDir(Path ruleDir) throws IOException, SWRLBuiltInException, SWRLParseException {
        Set<String> ruleNames = new HashSet<>();
        File[] files = ruleDir.toFile().listFiles();
        for (File f : files) {
            String name = f.getName();
            ruleNames.add(name);
            try {
                ruleEngine.replaceSWRLRule(name, name,
                        new String(Files.readAllBytes(f.toPath())), "", true);
            } catch (SWRLParseException xc) {
                System.err.println("Error in rule: " + name + ":\n\t" + xc.getMessage());
                throw xc;
            }
        }
        for (SWRLAPIRule r: ruleEngine.getSWRLRules()) {
            if (!ruleNames.contains(r.getRuleName())) {
                ruleEngine.deleteSWRLRule(r.getRuleName());
            }
        }

        ruleEngine.infer();
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
