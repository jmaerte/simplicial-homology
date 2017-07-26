package com.jmaerte.IO;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import com.jmaerte.main.Homology;
import com.jmaerte.util.Evaluable;
import com.jmaerte.util.Vector3D;

public class IO {
	
	public ArrayList<BigInteger> getComplex(String path, Vector3D<Character, Character, Character> delimiter, Evaluable<Character, Integer> charBijektion) throws IOException {
		if(Homology.instance.log) {
			Homology.instance.logger.log(Logger.INFO, "Input: " + path);
		}
		String contents = readFile(path);
		ArrayList<BigInteger> res = new ArrayList<>();
		char[] arr = contents.toCharArray();
		BigInteger curr = BigInteger.valueOf(0);
		for(char c : arr) {
			if(c == delimiter.i) {
				curr = BigInteger.valueOf(0);
				continue;
			}
			if(c == delimiter.k && curr.compareTo(BigInteger.valueOf(0)) != 0) {
				res.add(curr);
			}
			if(c != delimiter.j) {
				curr = curr.or(BigInteger.valueOf(1).shiftLeft(charBijektion.evaluate(c) - 1));
			}
		}
		return res;
	}
	
	public static String readFile(String path) throws IOException {
		byte[] content = Files.readAllBytes(Paths.get(path));
		return new String(content);
	}
}
