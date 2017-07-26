package com.jmaerte.IO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
	
	public static String 	WARNING = " *** WARNING *** ",
							INFO 	= "        -        ",
							BEGIN 	= " *    BEGAN    * ",
							END 	= " *     END     * ";
	private File f;
	private BufferedWriter bw;
	private SimpleDateFormat sdf;
	
	private boolean processRunning = false;
	private String currentProcessName;
	private long processBegan = 0l;
	
	public Logger(String s) throws IOException {
		sdf = new SimpleDateFormat("YYYY-MM-dd hh:mm:ss.S");
		f = new File(s);
		if(f.isDirectory()) {
			f = new File(s + File.separator + "log_" + (new SimpleDateFormat("YYYY-MM-dd_hh mm ss")).format(new Date()) + ".txt");
		}
		if(f.exists()) {
			f.mkdirs();
			f.createNewFile();
		}
		bw = new BufferedWriter(new FileWriter(f, true));
	}
	
	/**Log Message
	 * 
	 * @param level Type of severeness of the message
	 * @param msg Message Content
	 * @throws IOException
	 */
	public void log(String level, String msg) throws IOException {
		bw.write(sdf.format(new Date()) + "" + level + "" + msg);
		bw.newLine();
		bw.flush();
	}
	
	public void beginProcess(String processName) throws IOException {
		if(processRunning) {
			endProcess();
		}
		log(BEGIN, "Process: " + processName);
		processRunning = true;
		currentProcessName = processName;
		processBegan = System.currentTimeMillis();
	}
	
	public void endProcess() throws IOException {
		log(END, "Process: " + currentProcessName + " - Ran: " + (System.currentTimeMillis() - processBegan) + "ms");
		processRunning = false;
		currentProcessName = "";
		processBegan = 0L;
	}
	
	public void logHomology() {
		
	}
}
