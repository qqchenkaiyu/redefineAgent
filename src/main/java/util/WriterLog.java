/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 */

package util;

import org.apache.commons.lang3.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 功能描述
 *
 * @author c30000456
 * @since 2020-06-28
 */
public class WriterLog {
    public static void  log(String str){
        try {
            File userHome = SystemUtils.getUserHome();
            String logpath=userHome+"/redefine/error.log";
            File file = new File(logpath);
            if(!file.exists()){
                file.createNewFile();
            }
            Files.write(Paths.get(logpath), (str+System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static void  error(Throwable e){
        log(getErrInfo(Thread.currentThread(),e));
    }
    private static String getErrInfo(Thread t, Throwable e) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        e.printStackTrace(printStream);
        return String.format("线程%s发生异常,异常信息%s", t.getName(), byteArrayOutputStream.toString());
    }
}
