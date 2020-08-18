package test.java.com.github.monkeywie.proxyee;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.netty.util.concurrent.DefaultThreadFactory;

public class DownLoadExecutor  {

    public static Executor executor = Executors.newCachedThreadPool(new DefaultThreadFactory("download-thread"));
}
