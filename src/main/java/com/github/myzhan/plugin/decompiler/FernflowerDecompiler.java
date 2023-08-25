package com.github.myzhan.plugin.decompiler;

import org.apache.commons.collections.map.HashedMap;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.struct.ContextUnit;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Map;

public class FernflowerDecompiler {

    private static Field UNITS_FIELD;
    private static Field LOADER_FIELD;
    static {
        try {
            UNITS_FIELD = StructContext.class.getDeclaredField("units");
            UNITS_FIELD.setAccessible(true);
            LOADER_FIELD = StructContext.class.getDeclaredField("loader");
            LOADER_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not initialize Fernflower decompiler", e);
        }
    }

    private Map<String, Object> getOptions() {
        Map<String, Object> options = new HashedMap();
        options.put(IFernflowerPreferences.REMOVE_BRIDGE, "0");
        options.put(IFernflowerPreferences.LAMBDA_TO_ANONYMOUS_CLASS, "1");
        return options;
    }

    public String decompile(byte[] bytecode) throws Exception {
        InMemoryResultSaver inMemoryResultSaver = new InMemoryResultSaver();
        ByteCodeProvider byteCodeProvider = new ByteCodeProvider(bytecode);
        ByteArrayOutputStream log = new ByteArrayOutputStream();

        Fernflower fernflower = new Fernflower(byteCodeProvider, inMemoryResultSaver, getOptions(), new PrintStreamLogger(new PrintStream(log)));

            StructContext context = fernflower.getStructContext();
            Map<String, ContextUnit> units = (Map<String, ContextUnit>) UNITS_FIELD.get(context);
            LazyLoader loader = (LazyLoader) LOADER_FIELD.get(context);
            ContextUnit defaultUnit = units.get("");

            StructClass structClass = new StructClass(new DataInputFullStream(bytecode), true, loader);
            context.getClasses().put(structClass.qualifiedName, structClass);
            defaultUnit.addClass(structClass, "tmp.class");
            loader.addClassLink(structClass.qualifiedName, new LazyLoader.Link(1, "tmp", null));

            fernflower.decompileContext();

        return inMemoryResultSaver.javaCode;
    }
}
