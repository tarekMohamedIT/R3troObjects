package core;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;

public class ScriptObject {

    public static Object execute(String script) throws ScriptException {
        ScriptEngineManager mgr;
        ScriptEngine engine;

        mgr = new ScriptEngineManager();
        engine = mgr.getEngineByName("JavaScript");

        return engine.eval(script); // s is javascript code
    }

}
