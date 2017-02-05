package com.stardust.scriptdroid.droid.script;

import com.efurture.script.JSTransformer;
import com.furture.react.DuktapeEngine;
import com.stardust.scriptdroid.App;
import com.stardust.scriptdroid.R;
import com.stardust.scriptdroid.droid.runtime.api.IDroidRuntime;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Stardust on 2017/1/27.
 */

public class DuktapeJavaScriptEngine implements JavaScriptEngine {


    private final Map<Thread, DuktapeEngine> mThreadDuktapeEngineMap = new Hashtable<>();
    private static final String INIT_SCRIPT;
    private Map<String, Object> mVariableMap = new HashMap<>();

    static {
        try {
            INIT_SCRIPT = JSTransformer.parse(new StringReader(Init.INIT_SCRIPT));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public DuktapeJavaScriptEngine(IDroidRuntime runtime) {
        setRuntime(runtime);

    }

    private void setRuntime(IDroidRuntime runtime) {
        set("droid", IDroidRuntime.class, runtime);
    }

    @Override
    public Object execute(String script) throws IOException {
        DuktapeEngine duktapeEngine = new DuktapeEngine();
        init(duktapeEngine);
        add(duktapeEngine, Thread.currentThread());
        Object result;
        try {
            result = duktapeEngine.execute(JSTransformer.parse(new StringReader(script)));
        } catch (IOException e) {
            remove(Thread.currentThread());
            throw e;
        }
        remove(Thread.currentThread());
        return result;
    }

    private void init(DuktapeEngine duktapeEngine) {
        duktapeEngine.put("context", App.getApp());
        duktapeEngine.execute(INIT_SCRIPT);
        for (Map.Entry<String, Object> variable : mVariableMap.entrySet()) {
            duktapeEngine.put(variable.getKey(), variable.getValue());
        }

    }

    private void remove(Thread thread) {
        synchronized (mThreadDuktapeEngineMap) {
            DuktapeEngine engine = mThreadDuktapeEngineMap.remove(thread);
            stop(engine, thread);
        }
    }

    private void stop(DuktapeEngine engine, Thread thread) {
        if (engine != null)
            engine.destory();
        try {
            thread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void add(DuktapeEngine duktapeEngine, Thread thread) {
        synchronized (mThreadDuktapeEngineMap) {
            mThreadDuktapeEngineMap.put(thread, duktapeEngine);
        }
    }

    @Override
    public <T> void set(String varName, Class<T> c, T value) {
        mVariableMap.put(varName, value);
    }

    @Override
    public int stopAll() {
        int n;
        synchronized (mThreadDuktapeEngineMap) {
            for (Map.Entry<Thread, DuktapeEngine> entry : mThreadDuktapeEngineMap.entrySet()) {
                stop(entry.getValue(), entry.getKey());
            }
            n = mThreadDuktapeEngineMap.size();
            mThreadDuktapeEngineMap.clear();
        }
        return n;
    }

    @Override
    public void ensureNotStopped() {
        if (!mThreadDuktapeEngineMap.containsKey(Thread.currentThread())) {
            throw new RuntimeException(App.getApp().getString(R.string.text_script_stopped));
        }
    }
}