package main;

import io.*;
import java.io.*;

import huffmanDecoder.HuffmanDecoder;
import huffmanEncoder.HuffmanEncoder;

public class Main {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		// TODO Auto-generated method stub

		String fileToDecode = "/Users/michael_zhao/Desktop/2019 Spring Class/"
				+ "COMP 590-42 Data Compression/A1/comp590sp19-a1/data/compressed.dat";
		String decodedFile = "/Users/michael_zhao/Desktop/2019 Spring Class/"
				+ "COMP 590-42 Data Compression/A1/comp590sp19-a1/data/decodedFile.dat";
		String encodedFile= "/Users/michael_zhao/Desktop/2019 Spring Class/"
				+ "COMP 590-42 Data Compression/A1/comp590sp19-a1/data/encodedFile.dat";
		String decodedFileBasedOnEncoding= "/Users/michael_zhao/Desktop/2019 Spring Class/"
				+ "COMP 590-42 Data Compression/A1/comp590sp19-a1/data/decodedFile2.dat";
//		String fileToOutput = "/Users/michael_zhao/Desktop/2019 Spring Class/"
//				+ "COMP 590-42 Data Compression/A1/comp590sp19-a1/data/output.txt";
		
		// initiate decoder and decode
//		HuffmanDecoder decoder = new HuffmanDecoder(fileToDecode, decodedFile);
		HuffmanDecoder decoder = new HuffmanDecoder(encodedFile, decodedFileBasedOnEncoding);
		decoder.decode();
		int symbolNum = 574992;
//		HuffmanEncoder encoder = new HuffmanEncoder(decodedFile, encodedFile, symbolNum);
//		encoder.encode();
//		int symbolNum = 574992;
//		System.out.println(Integer.toBinaryString(symbolNum));
		

	}

}
