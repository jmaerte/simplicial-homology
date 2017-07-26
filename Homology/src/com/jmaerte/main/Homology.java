package com.jmaerte.main;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.jmaerte.IO.IO;
import com.jmaerte.IO.Logger;
import com.jmaerte.simplicial.Simplicial;
import com.jmaerte.util.Evaluable;
import com.jmaerte.util.Vector3D;

public class Homology {
	
	public enum Mode {
		INTEGER, LONG, BIGINTEGER;
	}
	
	public static Homology instance;
	public HashMap<String, Object> flags = new HashMap<>();
	public Simplicial simplicial;
	public int n;
	
	// Options:
	public boolean log = false;
	public String path;
	public Logger logger;
	public Mode mode = Mode.BIGINTEGER;
	public List<String> arguments = new ArrayList<>();
	private char setOpen = '{', setDelimiter = ',', setClose = '}';
	
	public Homology(String[] args) {
		/**
		 * Load arguments
		 */
		instance = this;
		for(int i = 0; i < args.length; i++) {
			if(args[i].length() > 0) {
				switch(args[i].charAt(0)) {
				case '-':
					if(args[i].length() < 2) throw new IllegalArgumentException("Not a valid argument: " + args[i]);
					if(args[i].charAt(1) == '-') {
						if(args[i].length() < 3) throw new IllegalArgumentException("Not a valid argument: " + args[i]);
						doubleOption(args[i].substring(2, args[i].length()));
					} else {
						if(i == args.length - 1) throw new IllegalArgumentException("Expected argument after: " + args[i]);
						option(args[i].substring(1, args[i].length()), args[i+1]);
						i++;
					}
					break;
				default:
					arguments.add(args[i]);
					break;
				}
			}
		}
		
		IO io = new IO();
		
		if(log) {
			try {
				logger.beginProcess("Reading Input");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long begin = System.currentTimeMillis();
		
		ArrayList<BigInteger> arr = null;
		try {
			 arr = io.getComplex(path,
					new Vector3D<Character, Character, Character>(setOpen,setDelimiter,setClose),
					new Evaluable<Character, Integer>() {
						@Override
						public Integer evaluate(Character k) {
							if(k >= 97 && k < 123) {
								return k-96;
							}
							return 0;
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(BigInteger b : arr) {
			if(b.compareTo(BigInteger.valueOf(0)) == 0) {
				System.out.println(0);
				continue;
			}
			int magn = b.bitCount();
			int count = 0;
			String s = "";
			int i = 1;
			while(count != magn) {
				int set = b.shiftRight(i).and(BigInteger.valueOf(1)).intValue();
				if(set == 1) count++;
				s += set; 
				i++;
			}
			System.out.println(s);
		}
		
		
		System.out.println("Reading input took: " + (System.currentTimeMillis() - begin) + "ms");
		if(log) {
			try {
				logger.endProcess();
				logger.beginProcess("Calculation");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		simplicial = new Simplicial(arr, n);
	}
	
	
	
	public void option(String qualifier, String opt) {
		switch(qualifier) {
		case "l":
			this.log = true;
			try {
				this.logger = new Logger(opt);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case "p":
			this.path = opt;
			break;
		case "o":
			this.setOpen = opt.charAt(0);
			break;
		case "c":
			this.setClose = opt.charAt(0);
			break;
		case "d":
			this.setDelimiter = opt.charAt(0);
			break;
		}
	}
	
	public void doubleOption(String opt) {
		switch(opt) {
		
		}
	}
	
	public static void main(String[] args) {
		new Homology(args);
	}
}
