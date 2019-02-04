package main;

import io.*;
import java.io.*;
import java.util.Map;

import huffmanDecoder.Cell;
import huffmanDecoder.HuffmanDecoder;
import huffmanEncoder.HuffmanEncoder;

public class Main {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		// TODO Auto-generated method stub

		String fileToDecode = "/Users/michael_zhao/eclipse-workspace/HuffmanCode/data/compressed.dat";
		String decodedFile = "/Users/michael_zhao/eclipse-workspace/HuffmanCode/data/decodedFile.dat";
		String encodedFile= "/Users/michael_zhao/eclipse-workspace/HuffmanCode/data/encodedFile.dat";
		String decodedFileBasedOnEncoding= "/Users/michael_zhao/eclipse-workspace/HuffmanCode/data/decodedFile2.dat";
		
		// record decoding and encoding time
		long firstDecoderStarts = 0;
		long encoderStarts = 0;
		long lastDecoderStarts = 0;
		long lastDecoderEnds = 0;
		
		// first decoder
		firstDecoderStarts = System.currentTimeMillis();
		HuffmanDecoder decoder = new HuffmanDecoder(fileToDecode, decodedFile);
		decoder.decode();

		// encoder
		encoderStarts = System.currentTimeMillis();
		System.out.println("The first decoder costs: " + (encoderStarts - 
				 firstDecoderStarts) / 1000 + " seconds");
		HuffmanEncoder encoder = new HuffmanEncoder(decodedFile, encodedFile);
		encoder.encode();
		
		// last decoder
		lastDecoderStarts = System.currentTimeMillis();
		System.out.println("The encoder costs: " + (lastDecoderStarts - 
				encoderStarts) / 1000 + " seconds");

		HuffmanDecoder decoder2 = new HuffmanDecoder(encodedFile, decodedFileBasedOnEncoding);
		decoder2.decode();
		lastDecoderEnds = System.currentTimeMillis();
		System.out.println("The last decoder costs: " + (lastDecoderEnds - 
				lastDecoderStarts) / 1000 + " seconds");
		
		/*
		 * calculate original compressed file entropy
		 */
//		HuffmanDecoder decoder = new HuffmanDecoder(fileToDecode, decodedFile);
//		decoder.decode();
//		HuffmanEncoder encoder = new HuffmanEncoder(decodedFile, encodedFile);
//		encoder.encode();
//		Map<Integer, Double> frequencyMap = encoder.getFrequencyMap();
//		Cell[] symbols = decoder.getSymbols();
//		double entropy = 0.0;
//		for (Map.Entry<Integer, Double> entry : frequencyMap.entrySet()) {
//			if (entry.getValue() > 0) {
//				
//				entropy += entry.getValue() * symbols[entry.getKey()].getLength();
//				System.out.println(entry.getValue() + " " + entry.getKey() + " " + 
//						symbols[entry.getKey()].getLength() + " " + entropy);
//			}
//		}
//		System.out.println("compressed entropy: " + entropy);

	}

}
