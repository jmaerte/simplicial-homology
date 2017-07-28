package com.jmaerte.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Julian on 01/07/2017.
 */
public class Logger {

    BufferedWriter bw;
    String runningProcess;
    long start;
    boolean file, console;

    public Logger(String path, boolean console, boolean file) {
        this.file = file;
        this.console = console;
        if(file) {
            File f = new File(path);
            try {
                if(!f.exists()) {
                    f.mkdirs();
                    f.createNewFile();
                }
                bw = new BufferedWriter(new FileWriter(f, false));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startProcess(String process) {
        runningProcess = process;
        start = System.currentTimeMillis();
        log("STARTED: " + process, true);
    }

    public void log(String toLog, boolean time) {

    }

    public void endProcess() {
        log("TERMINATED: " + runningProcess + " after " + (System.currentTimeMillis() - start), true);
        runningProcess = "";
        start = 0;
    }
}
