package test.java.com.github.monkeywie.proxyee;

import com.example.lib.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class FireWallTest {

    public static void main(String[] args) {
        ScriptEngine engine;
        ScriptEngineManager scriptEngineManager =new ScriptEngineManager();
        engine=scriptEngineManager.getEngineByName("javascript");
        Invocable invocable = (Invocable) engine;
        Object res = null;
        try {
            engine.eval((new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("gfwlist.pac"))));
            String href = "https://baidu.com/";
            res =invocable.invokeFunction("FindProxyForURL",new Object[]{"","www.google.com"});

        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        System.out.println(res);

//        List<String> fireWall;
//
//        long start = System.currentTimeMillis();
//        fireWall = FileUtils.readFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("firewall.txt"));
//        fireWall.removeIf(new Predicate<String>() {
//            @Override
//            public boolean test(String s) {
//                if (s.contains("||"))
//                    return false;
//                return true;
//            }
//        });
//
//        System.out.println("end time :" + (System.currentTimeMillis() - start));
//        System.out.println(fireWall.size());

    }
}
