package agent;

import util.CompileUtil;
import util.WriterLog;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;

public class RedefineAgent {
    static String outputDir;

    static Instrumentation mInstrumentation;

    static MyClassFileTransformer classFileTransformer = new MyClassFileTransformer();

    public static void agentmain(String agentargs, Instrumentation instrumentation) {
        try {
            mInstrumentation = instrumentation;
            mInstrumentation.addTransformer(classFileTransformer,true);
            String javahome = System.getProperty("java.home");
            WriterLog.log("javahome = " + javahome);
            Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
            outputDir = agentargs.split(" ")[2];
            if (agentargs.startsWith("compile")) {
                String classname = agentargs.split(" ")[1];
                boolean found = false;
                for (Class aclass : allLoadedClasses) {
                    if (aclass.getName().equalsIgnoreCase(classname)) {
                        found = true;
                        classFileTransformer.getClassFile(aclass);
                    }
                }
                if (!found) {
                    WriterLog.log("classname not match " + classname);
                    for (Class allLoadedClass : allLoadedClasses) {
                        WriterLog.log(allLoadedClass.getName());
                    }
                }

            } else if (agentargs.startsWith("redefine")) {
                File file = new File(agentargs.split(" ")[1]);
                if (!file.exists()) {
                    System.out.println(" file not found " + file.getAbsolutePath());
                }
                Class<?> compile = CompileUtil.compile(
                    new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), outputDir);
                String classPath = getClassPath(compile.getName());
                ensureDir(classPath);
                byte[] bytes = Files.readAllBytes(Paths.get(classPath));
                ClassDefinition classDefinition = new ClassDefinition(compile, bytes);
                instrumentation.redefineClasses(classDefinition);

            }
        } catch (Exception e) {
            WriterLog.error(e);
        }
    }

    private static String getJavaPath(String name) {
        Path path = Paths.get(outputDir, name.replaceAll("\\.", "/") + ".java");
        ensureDir(path.toString());
        return path.toString();
    }

    private static String getClassPath(String name) {
        Path path = Paths.get(outputDir, name.replaceAll("\\.", "/") + ".class");
        ensureDir(path.toString());
        return path.toString();
    }

    private static void ensureDir(String path) {
        File file = new File(path);
        file.getParentFile().mkdirs();
        file.deleteOnExit();
    }

    static class MyClassFileTransformer implements ClassFileTransformer {
        Class classToChange;

        public void getClassFile(Class clazz) {
            classToChange = clazz;
            try {
                mInstrumentation.retransformClasses(classToChange);
            } catch (UnmodifiableClassException e) {
                e.printStackTrace();
            }
        }

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                if (classBeingRedefined == null || !classBeingRedefined.getName().equals(classToChange.getName())) {
                    return null;
                }
                String classPath = getClassPath(classToChange.getName());
                System.out.println("输出class文件 " + classPath);
                new File(classPath).deleteOnExit();
                Files.write(Paths.get(classPath), classfileBuffer, StandardOpenOption.CREATE);
            } catch (IOException e) {
                WriterLog.error(e);
            }
            return classfileBuffer;
        }
    }
}
