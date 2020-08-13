package com.example.lib;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageExecutor {
    public static ExecutorService executorService = Executors.newFixedThreadPool(4);
}
