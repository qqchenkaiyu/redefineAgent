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
static Instrumentation mInstrumentation;
static MyClassFileTransformer classFileTransformer=new MyClassFileTransformer();
    public static void agentmain(String agentargs, Instrumentation instrumentation) {
mInstrumentation=instrumentation;
        mInstrumentation.addTransformer(classFileTransformer,true);
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
   static class MyClassFileTransformer implements ClassFileTransformer{
        Class classToChange;
      public   void getClassFile(Class clazz){
            classToChange=clazz;
            try {
                mInstrumentation.retransformClasses(classToChange);
            } catch (UnmodifiableClassException e) {
                e.printStackTrace();
            }
        }
       @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            try {
                if(classBeingRedefined==null||!classBeingRedefined.getName().equals(classToChange.getName()))return null;
                String classPath = getClassPath(classToChange.getName());
                System.out.println("输出class文件 "+ classPath);
                new File(classPath).deleteOnExit();
                new File(classPath).createNewFile();
                Files.write(Paths.get(classPath),classfileBuffer, StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return classfileBuffer;
        }
    }
}
