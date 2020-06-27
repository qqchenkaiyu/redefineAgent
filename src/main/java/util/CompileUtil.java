package util;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static Class<?> compile(String className, String javaCodes,String outDir) {
        System.out.println(className);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StrSrcJavaObject srcObject = new StrSrcJavaObject(className, javaCodes);
        Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(srcObject);
        String flag = "-d";
        Iterable<String> options = Arrays.asList(flag, outDir);
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, fileObjects);
        boolean result = task.call();
        if (result == true) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;

    }

public static String decompile(Class clazz){
    PlainTextOutput plainTextOutput = new PlainTextOutput();
    DecompilerSettings decompilerSettings = new DecompilerSettings();
    Decompiler.decompile(clazz.getPackage().getName().replaceAll("\\.","/")+"/"+clazz.getSimpleName(),plainTextOutput,decompilerSettings);
    String unicodeStr2String = StringUnicodeUtil.unicodeStr2String(plainTextOutput.toString());
    return unicodeStr2String;
}
    public static Class<?> compile(String javaCodes,String outDir ) throws IOException {
      return compile(getClassName(javaCodes),javaCodes,outDir);
    }

    //从字符串中截取全类名


    private static String getClassName(String javaCodes) throws IOException {

        Pattern m = Pattern.compile("^package\\s[\\w+.]*");
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

    public static void main(String[] args) throws IOException {
        Class<?> compile = compile("package com.example.demo;\n" +
                "\n" +
                "public class BBking\n" +
                "{\n" +
                "    public void bb() {\n" +
                "        System.out.println(\"我就爱b--------b\");\n" +
                "    }\n" +
                "}\n", "F://");
    }
}