package eu.eudat.swrldex.core;

import org.checkerframework.checker.nullness.qual.NonNull;
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
import org.swrlapi.core.IRIResolver;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

class OntologyHelper {
    PrefixManager pm = new DefaultPrefixManager();
    IRIResolver defaultIriResolver = SWRLAPIFactory.createIRIResolver();
    OWLOntologyManager ontologyManager;
    OWLDataFactory df;
    OWLOntology ontology;
    SWRLRuleEngine ruleEngine;
    SQWRLQueryEngine queryEngine;
    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

    IRIResolver printerIriResolver = new IRIResolver() {
        @Override
        public void reset() {
            System.out.println("iri reset");
            defaultIriResolver.reset();
        }

        @Override
        public @NonNull Optional<IRI> prefixedName2IRI(@NonNull String prefixedName) {
            System.out.println("iri prefixedName2IRI ... " + prefixedName);
            Optional<IRI> ret = defaultIriResolver.prefixedName2IRI(prefixedName);
            System.out.println("iri prefixedName2IRI " + prefixedName + " -> " + ret);
            return ret;
        }

        @Override
        public @NonNull Optional<IRI> variableName2IRI(@NonNull String variableName) {
            Optional<IRI> ret = defaultIriResolver.variableName2IRI(variableName);
            System.out.println("iri variableName2IRI " + variableName + " -> " + ret);
            return ret;
        }

        @Override
        public @NonNull Optional<String> iri2PrefixedName(@NonNull IRI iri) {
            Optional<String> ret = defaultIriResolver.iri2PrefixedName(iri);
            System.out.println("iri iri2prefixedname " + iri + " -> " + ret);
            return ret;
        }

        @Override
        public @NonNull Optional<String> iri2VariableName(@NonNull IRI iri) {
            Optional<String> ret = defaultIriResolver.iri2VariableName(iri);
            System.out.println("iri iri2varname " + iri + " -> " + ret);
            return ret;
        }

        @Override
        public @NonNull Optional<String> iri2ShortForm(@NonNull IRI iri) {
            Optional<String> ret = defaultIriResolver.iri2ShortForm(iri);
            System.out.println("iri iri2shortform " + iri + " -> " + ret);
            return ret;
        }

        @Override
        public void updatePrefixes(@NonNull OWLOntology ontology) {
            System.out.println("iri updateprefixes ");
            defaultIriResolver.updatePrefixes(ontology);
        }

        @Override
        public void setPrefix(@NonNull String prefix, @NonNull String namespace) {
            System.out.println("iri setprefix " + prefix + " : " + namespace);
            defaultIriResolver.setPrefix(prefix, namespace);
        }

        @Override
        public @NonNull IRI generateIRI() {
            IRI ret = defaultIriResolver.generateIRI();
            System.out.println("iri generateIRI " + " -> " + ret);
            return ret;
        }

        @Override
        public @NonNull String render(@Nonnull OWLObject owlObject) {
            String ret = defaultIriResolver.render(owlObject);
            System.out.println("iri render " + owlObject + " -> " + ret);
            return ret;
        }
    };


    public OntologyHelper(String prefix, String namespaceIRI) throws OWLOntologyCreationException {
        pm.setPrefix(prefix, namespaceIRI);
        pm.setDefaultPrefix(namespaceIRI);

        //IRIResolver iriResolver = printerIriResolver;
        IRIResolver iriResolver = defaultIriResolver;
        iriResolver.setPrefix(prefix, namespaceIRI);

        df = OWLManager.getOWLDataFactory();

        ontologyManager = OWLManager.createOWLOntologyManager();
        ontology = ontologyManager.createOntology();
        ontologyManager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setPrefixManager(pm);

        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology, iriResolver);

        queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology, iriResolver);
//        List<String> builtins = queryEngine.getSWRLBuiltInIRIs()
//                .stream()
//                .map(iri -> iri.toString())
//                .sorted()
//                .distinct()
//                .collect(Collectors.toList());
//        for (String s: builtins) {
//            System.out.println("    builtin: " + s);
//        }
//        for (String s: queryEngine.getSQWRLQueryNames()) {
//            System.out.println("    query name: " + s);
//        }
//        System.out.println("-- ");
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
        defaultIriResolver.updatePrefixes(ontology);
        ruleEngine.replaceSWRLRule(id, id, ruleText, "", true);
    }

    public void deleteRule(String id){
        ruleEngine.deleteSWRLRule(id);
    }

    public SQWRLResult runSQWRL(String id, String queryText) throws SQWRLException, SWRLParseException {
        defaultIriResolver.updatePrefixes(ontology);
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

    public void print() throws OWLException {
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        printHierarchy(reasoner, df.getOWLThing(), 0, new HashSet<OWLClass>());
    }

    public void printAsXML() throws OWLException {
        StringDocumentTarget target = new StringDocumentTarget();
        ontologyManager.saveOntology(ontology, target);
        System.out.println("---- " + pm.getDefaultPrefix());
        System.out.println(target.toString());
        System.out.println("----");
    }

    private void printHierarchy(OWLReasoner r, OWLClass clazz, int level, Set<OWLClass> visited) throws OWLException {
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
                System.out.println("- " + ind.getIRI().getShortForm());
            }
        }
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


    public void load(Path path) throws OWLOntologyCreationException {
        FileDocumentSource source = new FileDocumentSource(path.toFile());
        ontology = ontologyManager.loadOntologyFromOntologyDocument(source);
    }

    OWLClass toClass(String name) {
//        System.out.println(df.getOWLClass(name, pm));
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
}
