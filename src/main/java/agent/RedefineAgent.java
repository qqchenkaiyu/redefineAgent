package agent;

import util.CompileUtil;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;

public class RedefineAgent {
    static String outputDir;

    public static void agentmain(String agentargs, Instrumentation instrumentation) {
        Class[] allLoadedClasses = instrumentation.getAllLoadedClasses();
        outputDir = agentargs.split(" ")[2];
        if (agentargs.startsWith("compile")) {
            String classname=agentargs.split(" ")[1];
            for (Class aclass : allLoadedClasses) {
                if (aclass.getName().equalsIgnoreCase(classname)){
                    String decompile = CompileUtil.decompile(aclass);
                    try {
                       String path= getJavaPath(aclass.getName());
                       ensureDir(path);
                        Files.write(Paths.get(path),decompile.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

        }else if (agentargs.startsWith("redefine")){
            File file = new File(agentargs.split(" ")[1]);
            if (!file.exists()){
                System.out.println(" file not found "+file.getAbsolutePath());
            }
            try {
                Class<?> compile = CompileUtil.compile(new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8),outputDir);
                String classPath = getClassPath(compile.getName());
                ensureDir(classPath);
                byte[] bytes = Files.readAllBytes(Paths.get(classPath));
                ClassDefinition classDefinition = new ClassDefinition(compile, bytes);
                instrumentation.redefineClasses(classDefinition);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private static String getJavaPath(String name) {
        Path path = Paths.get(outputDir, name.replaceAll("\\.", "/") + ".java");
        return path.toString();
    }
    private static String getClassPath(String name) {
        Path path = Paths.get(outputDir, name.replaceAll("\\.", "/") + ".class");
        return path.toString();
    }
    private static void ensureDir(String path) {
        File file = new File(path);
        file.getParentFile().mkdirs();
        file.deleteOnExit();
    }
}
