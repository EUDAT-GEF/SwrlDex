package eu.eudat.swrldex.core;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: performance: reuse ontology: recursively remove input+output individuals after event

public class DirectiveEngine {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DirectiveEngine.class);

    // 64 is very large, but needed to run a WebLicht chain without crashes
    public static int ONTOLOGY_POOL_SIZE = 64;
    public static int ONTOLOGY_THREAD_SIZE = 8;

    private Gson gson = new GsonBuilder().create();

    public DirectiveEngine() {
        startCreatingOntologies();
    }

    public JsonObject event(JsonObject jsonEvent) {
        log.debug("--- original json input:");
        log.debug(gson.toJson(jsonEvent));

        // disable handling post-ceding events, otherwise WebLicht consumes the ontologies too fast
        try {
            if (jsonEvent.get("event").getAsJsonObject().get("preceding").getAsBoolean() == false) {
                return new JsonObject();
            }
        } catch (Exception e) {
            // ignore
        }

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

    private Queue<OntologyHelper> pool = new ConcurrentLinkedQueue();
    private AtomicInteger tasks = new AtomicInteger(0);
    private ExecutorService executor = Executors.newFixedThreadPool(ONTOLOGY_THREAD_SIZE );

    private OntologyHelper newOntologyHelper() throws OWLOntologyCreationException {
        startCreatingOntologies();
        OntologyHelper oh = pool.poll();
        if (oh == null) {
            oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#",
                    Paths.get("eventOntology.xml"));
        }
        return oh;
    }

    private void startCreatingOntologies() {
        while (pool.size() + tasks.get() <= ONTOLOGY_POOL_SIZE) {
            tasks.incrementAndGet();
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        OntologyHelper oh = new OntologyHelper("dex:", "http://eudat.eu/ns/dex#",
                                Paths.get("eventOntology.xml"));
                        pool.add(oh);
                        tasks.decrementAndGet();
                        log.debug("pool: new ontology added, now " + pool.size());
                    } catch (OWLOntologyCreationException e) {
                        log.error("pool: ontology creation error", e);
                    }
                }
            });
        }
    }
}
