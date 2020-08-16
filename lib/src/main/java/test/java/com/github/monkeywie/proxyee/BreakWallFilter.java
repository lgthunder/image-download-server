package test.java.com.github.monkeywie.proxyee;

import com.example.lib.FileUtils;

import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Predicate;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class BreakWallFilter {
    static ScriptEngine engine;

    static {

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        engine = scriptEngineManager.getEngineByName("javascript");
        Invocable invocable = (Invocable) engine;
        Object res = null;
        try {
            engine.eval((new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("gfwlist.pac"))));
//            String href = "https://baidu.com/";
//            res = invocable.invokeFunction("FindProxyForURL", new Object[]{href, "baidu.com"});

        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public static String getProxyByUrl(String url) {
        Invocable invocable = (Invocable) engine;
//        String href = "https://baidu.com/";
        try {
            Object o = invocable.invokeFunction("FindProxyForURL", new Object[]{"", url});
            if (o == null) {
                return "";
            }
            return o.toString();
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean isWallBlock(String host) {
        String proxy = getProxyByUrl(host);
        if (!"DIRECT".equals(proxy)) {
            return true;
        }
        return false;
    }
}
