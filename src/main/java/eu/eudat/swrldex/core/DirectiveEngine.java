package eu.eudat.swrldex.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// TODO: performance: reuse ontology: recursively remove input+output individuals after event

public class DirectiveEngine {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DirectiveEngine.class);

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DirectiveEngine() {
        startOntologyPoolThread();
    }

    public JsonObject event(JsonObject jsonEvent) {
        log.debug("--- original json input:");
        log.debug(gson.toJson(jsonEvent));
        try {
            OntologyHelper oh = newOntologyHelper();
            OntologyHelper.OClass inputCls = oh.cls("INPUT");
            OntologyHelper.OClass acceptCls = oh.cls("ACCEPT");
            OntologyHelper.OClass generateCls = oh.cls("GENERATE");

            // root individual for all incoming data
            OntologyHelper.OIndividual input = oh.ind("input");
            input.addType(inputCls);

            // root individual for all accept assertions
            OntologyHelper.OIndividual accept = oh.ind("accept");
            accept.addType(acceptCls);

            // root individual for all generated calls
            OntologyHelper.OIndividual generate = oh.ind("generate");
            generate.addType(generateCls);

            new JsonLoader(oh).load(input, jsonEvent, accept);

            // oh.print();
            // oh.printAsXML();
            // oh.saveAsXML(Paths.get("event.out.xml"));

            // log.debug("--- dumped input:");
            // log.debug(gson.toJson(new JsonDumper(oh).dump(input)));
            // log.debug("");

            oh.execRulesFromDir(Paths.get("rules"));

            log.debug("--- dumped accept and generate:");
            log.debug(gson.toJson(new JsonDumper(oh).dump(accept)));
            log.debug(gson.toJson(new JsonDumper(oh).dump(generate)));
            log.debug("");

            JsonObject jsonEventGenerate = new JsonDumper(oh).dump(generate);
            Generator.fromJson(jsonEventGenerate).handle();

            JsonObject jsonEventOutput = new JsonDumper(oh).dump(accept);
            return jsonEventOutput;
        } catch (OWLOntologyCreationException e) {
            log.error("Ontology creation error: ", e);
        } catch (org.swrlapi.parser.SWRLParseException e) {
            log.error("Parse error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error: ", e);
        }
        return null;
    }

    public static int ONTOLOGY_POOL_SIZE = 10;
    private Queue<OntologyHelper> pool = new ConcurrentLinkedQueue();
    private Object poolMon = new Object();

    private OntologyHelper newOntologyHelper() throws OWLOntologyCreationException {
        OntologyHelper oh = pool.poll();
        if (oh != null) {
            synchronized (poolMon) { poolMon.notify(); }
            return oh;
        } else {
            return new OntologyHelper("dex:", "http://eudat.eu/ns/dex#",
                    Paths.get("eventOntology.xml"));
        }
    }

    private void startOntologyPoolThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (pool.size() >= ONTOLOGY_POOL_SIZE) {
                            synchronized (poolMon) {
                                poolMon.wait();
                            }
                        } else {
                            OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#",
                                    Paths.get("eventOntology.xml"));
                            pool.add(oh);
                            log.debug("pool: new ontology added, now " + pool.size());
                        }
                    } catch (OWLOntologyCreationException e) {
                        log.error("pool: ontology creation error", e);
                    } catch (InterruptedException e) {
                        // ignore it
                    }
                }
            }
        }).start();
    }
}
