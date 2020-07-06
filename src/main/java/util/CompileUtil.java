package util;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/**
 * Created by Administrator on 2019/4/3.
 */

public class CompileUtil {
    /**
     * 装载字符串成为java可执行文件
     * @param className className
     * @param javaCodes javaCodes
     * @return Class
     */

    private static Class<?> compile(String className, String javaCodes,String outDir) throws Exception{
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if(compiler==null){
            throw new RuntimeException("ToolProvider.getSystemJavaCompiler() == null   java_home maybe not set!!");
        }
        // 用来获取编译错误时的错误信息
        /** START 以下代码在打包成web程序时必须开启，在编辑器里面时请屏蔽 */
        StringBuilder cp = new StringBuilder();
        URLClassLoader urlClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        for (URL url : urlClassLoader.getURLs()) {
            cp.append(url.getFile()).append(File.pathSeparator);
        }
        /**  END  以上代码在打包成web程序时必须开启，在编辑器里面时请屏蔽 */
        StrSrcJavaObject srcObject = new StrSrcJavaObject(className, javaCodes);
        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(srcObject);
        Iterable<String> options = Arrays.asList("-d", outDir,"-classpath",cp.toString());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream);
        JavaCompiler.CompilationTask task = compiler.getTask(writer, null, null, options, null, fileObjects);
        boolean result = task.call();
        if (result == true) {
             return Class.forName(className);
        }
        writer.flush();
        WriterLog.log("编译时添加的cp "+cp.toString());
        WriterLog.log(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
        return null;

    }

public static String decompile(Class clazz){
    PlainTextOutput plainTextOutput = new PlainTextOutput();
    DecompilerSettings decompilerSettings = new DecompilerSettings();
    Decompiler.decompile(clazz.getPackage().getName().replaceAll("\\.","/")+"/"+clazz.getSimpleName(),plainTextOutput,decompilerSettings);
    String unicodeStr2String = StringUnicodeUtil.unicodeStr2String(plainTextOutput.toString());
    return unicodeStr2String;
}
    public static Class<?> compile(String javaCodes,String outDir ) throws Exception {
      return compile(getClassName(javaCodes),javaCodes,outDir);
    }

    //从字符串中截取全类名


    private static String getClassName(String javaCodes) throws IOException {

        Pattern m = Pattern.compile("package\\s[\\w+.]*");
        Matcher matcher = m.matcher(javaCodes);
        String packgename = "";
if(matcher.find()){
    String group = matcher.group(0);
    packgename=group.split("package ")[1];
}
        m = Pattern.compile("class\\s[\\w+.$]*");
         matcher = m.matcher(javaCodes);
        String classname = "";
        if(matcher.find()){
            String group = matcher.group(0);
            classname=group.split("class ")[1];
        }
        return packgename+"."+classname;
    }

    private static class StrSrcJavaObject extends SimpleJavaFileObject {
        private String content;
        StrSrcJavaObject(String name, String content) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }

    public static void main(String[] args) throws Exception {
 
    }

}
