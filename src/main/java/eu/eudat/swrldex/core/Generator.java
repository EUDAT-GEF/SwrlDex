package eu.eudat.swrldex.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Generator {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DirectiveEngine.class);
    String script = null;
    List<String> parameters = new ArrayList<String>();

    public void handle() throws IOException {
        if (script == null || script.isEmpty()) {
            return;
        }

        List<String> cmd = new ArrayList<String>();
        cmd.add(Paths.get(script).toAbsolutePath().toString());
        cmd.addAll(parameters);

        log.info("starting process: " + cmd);
        Runtime.getRuntime().exec(cmd.toArray(new String[]{}));
    }

    public static Generator fromJson(JsonObject json) {
        Generator generator = new Generator();

        {
            JsonElement jsonScript = json.get("script");
            if (jsonScript == null) {
                return generator;
            }
            if (!jsonScript.isJsonPrimitive()) {
                log.warn("script is not JsonPrimitive");
                return generator;
            }
            JsonPrimitive jsonScriptPrimitive = jsonScript.getAsJsonPrimitive();
            if (!jsonScriptPrimitive.isString()) {
                log.warn("script is not a string");
                return generator;
            }
            generator.script = jsonScriptPrimitive.getAsString();
        }

        {
            JsonElement jsonParameter = json.get("parameter");
            if (jsonParameter == null) {
                return generator;
            }
            if (jsonParameter.isJsonPrimitive()) {
                JsonPrimitive jsonParameterPrimitive = jsonParameter.getAsJsonPrimitive();
                generator.parameters.add(jsonParameterPrimitive.getAsString());
            } else if (jsonParameter.isJsonArray()) {
                JsonArray jsonParameterArray = jsonParameter.getAsJsonArray();
                for (int i = 0; i < jsonParameterArray.size(); ++i) {
                    JsonElement jp = jsonParameterArray.get(i);
                    if (!jp.isJsonPrimitive()) {
                        log.warn("parameter array element is not a primitive");
                        return generator;
                    }
                    generator.parameters.add(jp.getAsJsonPrimitive().getAsString());
                }
            } else {
                log.warn("parameter is neither primitive nor array");
                return generator;
            }
        }

        return generator;
    }
}
