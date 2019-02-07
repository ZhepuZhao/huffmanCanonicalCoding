package huffmanEncoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import huffmanDecoder.Cell;
import huffmanDecoder.Node;
import io.BitSink;
import io.BitSource;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;
import io.OutputStreamBitSink;

public class HuffmanEncoder {

	// input and output stream with coding file name
	private InputStream input;
	private BitSource source;
	private OutputStream output;
	private BitSink sink;
	private String encodingFile;
	
	// <symbol, frequency of symbol>
	private Map<Integer, Double> frequencyMap; 
	// <symbol, codeword(0, 1 string)> for original huffman tree
	private Map<Integer, String> codeMap; 
	// symbol array
	private Cell[] symbols;
	// symbol heap to sort Node based on frequency and tree height
	private Queue<Node> symbolHeap;
	// total number of symbol in the input file
	private int symbolNum;
	private Node root;

	public HuffmanEncoder(String encodingFile, String outputFile) throws FileNotFoundException {
//		this.input = new FileInputStream(encodingFile);
		// BufferedStream is much faster than FileStream
		this.input = new BufferedInputStream(new FileInputStream(encodingFile));
		this.source = new InputStreamBitSource(input);
//		this.output = new FileOutputStream(outputFile);
		this.output = new BufferedOutputStream(new FileOutputStream(outputFile));
		this.sink = new OutputStreamBitSink(output);
		this.encodingFile = encodingFile;

		this.frequencyMap = new HashMap<Integer, Double>();
		this.codeMap = new HashMap<Integer, String>();
		this.symbols = new Cell[256];
		this.symbolHeap = new PriorityQueue<Node>(new Comparator<Node>() {
			@Override
			public int compare(Node n1, Node n2) {
				int diff = Double.compare(n1.getFrequency(), n2.getFrequency());
				if (diff > 0) {
					return 1;
				} else if (diff < 0) {
					return -1;
				} else {
					int tmp = Double.compare(n1.getHeight(), n2.getHeight());
					if (tmp > 0) {
						return 1;
					} else if (tmp < 0) {
						return -1;
					} else {
						return 0;
					}
				}
			}
		});
		this.symbolNum = 0;
		this.root = null;
	}

	// <symbol, frequency>
	public void constructFrequencyMap() throws InsufficientBitsLeftException, IOException {
		while (input.available() > 0) {
			int currentSymbol = source.next(8);
			symbolNum++;
			frequencyMap.put(currentSymbol, frequencyMap.getOrDefault(currentSymbol, 0.0) + 1);
		}
//		System.out.println(symbolNum);
		for (int i = 0; i < 256; i++) {
			if (!frequencyMap.containsKey(i)) {
				frequencyMap.put(i, 0.0);
			}
		}
	}
	
	// frequency heap with nodes
	public void constructFrequencyHeap() {

		for (Map.Entry<Integer, Double> entry : frequencyMap.entrySet()) {
			entry.setValue((double) (entry.getValue() / this.symbolNum));
		}
//		// calculate the theoretical entropy here
//		double entropy = 0.0;
//		for (Map.Entry<Integer, Double> entry : frequencyMap.entrySet()) {
//			if (entry.getValue() > 0) {
//				entropy += entry.getValue() * Math.log(1 / entry.getValue());
//			}
//		}
//		System.out.println("theoretical entropy: " + entropy);
		
		for (Map.Entry<Integer, Double> entry : frequencyMap.entrySet()) {
			Node node = new Node(entry.getKey());
			node.setFrequency(entry.getValue());
			symbolHeap.add(node);
		}

	}

	public void constructOriginalTree() {

		while (symbolHeap.size() > 1) {
			Node root = new Node(-1);
			Node first = symbolHeap.poll();
//			System.out.println(first.getFrequency());
			Node second = symbolHeap.poll();
//			System.out.println(second.getFrequency());
			root.setLeft(first);
			root.setRight(second);
			root.setFrequency(first.getFrequency() + second.getFrequency());
			root.setHeight(1 + Math.max(first.getHeight(), second.getHeight()));
			symbolHeap.offer(root);
		}
		root = symbolHeap.peek();
	}

	// set string of sequence of 0 and 1 as codeword to each node
	// here node means all the nodes in the original tree rather than just leaf nodes
	public void setCodeString(Node root, boolean isLeft, String s) {
		if (root == null)
			return;

		if (isLeft) {
			s = s + "0";
			root.setCode(s);

		} else {
			s = s + "1";
			root.setCode(s);
		}
		// use parents' codeString to set the child nodes
		setCodeString(root.getLeft(), true, root.getCode());
		setCodeString(root.getRight(), false, root.getCode());
	}

	public void constructCodewordSymbolMap(Node root) {
		if (root == null)
			return;
		if (root.getValue() != -1) {
//			System.out.println(root.getValue());
			codeMap.put(root.getValue(), root.getCode().substring(1));
		}
		constructCodewordSymbolMap(root.getLeft());
		constructCodewordSymbolMap(root.getRight());
	}
	
	// array of cell containing symbol value and the length of symbol codeword
	public void constructSymbolArray() throws InsufficientBitsLeftException, IOException {
		for (int i = 0; i < symbols.length; i++) {
			// first parameter: length, second parameter: symbol
			symbols[i] = new Cell(codeMap.get(i).length(), i);
		}
	}
	
	// sort symbols in ascending codeword length order
	public void sortSymbolArray() {
		// sort symbols
		Arrays.sort(symbols, new Comparator<Cell>() {
			@Override
			public int compare(Cell c1, Cell c2) {
				return c1.getLength() - c2.getLength();
			}
		});
	}
	
	// construct the canonical tree
	public void constructCanonicalTree() {
		// we need to reset the root, otherwise, the tree cannot be formed correctly
		// there could be duplicate nodes in the tree if don't reset the root.
		root = new Node(-1);
		for (Cell cell : symbols) {
			insertSymbol(this.root, cell, cell.getLength());
		}
	}

	// insert the specified node to the Huffman canonical tree
	public Node insertSymbol(Node root, Cell cell, int length) {
		if (length == 0) {
			Node tmp = new Node(cell.getSymbol());
			tmp.setLeftFull(true);
			tmp.setRightFull(true);
//			System.out.println(cell.getSymbol());
			return tmp;
		}
		if (root == null)
			root = new Node(-1);
		if (!root.isLeftFull()) {
			root.setLeft(insertSymbol(root.getLeft(), cell, length - 1));
			// if both left and right sub-tree are full, set the root leftFull as true
			root.setLeftFull(root.getLeft().isLeftFull() && root.getLeft().isRightFull());
		} else {
			root.setRight(insertSymbol(root.getRight(), cell, length - 1));
			// if both left and right sub-tree are full, set the root rightFull as true
			root.setRightFull(root.getRight().isLeftFull() && root.getRight().isRightFull());
		}
		return root;
	}

	public void outputFile() throws InsufficientBitsLeftException, IOException {
		// write the length of symbols
		for (int i = 0; i < 256; i++) {
			sink.write(codeMap.get(i).length(), 8);
		}
		// write total number of symbols
		sink.write(symbolNum, 32);
		/*
		 * we need to iterate the symbol from the input symbol again
		 * 1. close the previous inputStream
		 * 2. open a new inputStream which takes the encodingFile
		 */
		
		input.close();
		source = new InputStreamBitSource(new BufferedInputStream(new FileInputStream(encodingFile)));
		// write the encoded codeword of symbol to the outputStream
		for (int i = 0; i < symbolNum; i++) {
			int tmp = source.next(8);
			// System.out.println((char)tmp);
			if (codeMap.containsKey(tmp)) {
				sink.write(codeMap.get(tmp));
			}
		}
		sink.padToWord();
		output.close();
	}

	public void encode() throws InsufficientBitsLeftException, IOException {
		//
		constructFrequencyMap();
		constructFrequencyHeap();
		constructOriginalTree();
		setCodeString(root, true, "");
		constructCodewordSymbolMap(root);
		constructSymbolArray();
		sortSymbolArray();
		
		constructCanonicalTree();
		setCodeString(root, true, "");
		codeMap.clear();
		constructCodewordSymbolMap(root);
//		// calculate entropy based on my compressed solution
//		double entropy = 0.0;
//		for (Map.Entry<Integer, Double> entry : frequencyMap.entrySet()) {
//			if (entry.getValue() > 0) {
//				entropy += entry.getValue() * this.codeMap.get(entry.getKey()).length();
//			}
//		}
//		System.out.println("mine entropy: " + entropy);
		outputFile();
	}

	// getter and setters
	public InputStream getInput() {
		return input;
	}

	public BitSource getSource() {
		return source;
	}

	public OutputStream getOutput() {
		return output;
	}

	public BitSink getSink() {
		return sink;
	}

	public String getEncodingFile() {
		return encodingFile;
	}

	public int getSymbolNum() {
		return symbolNum;
	}

	public Queue<Node> getSymbolHeap() {
		return symbolHeap;
	}

	public Node getRoot() {
		return root;
	}

	public Map<Integer, Double> getFrequencyMap() {
		return frequencyMap;
	}

	public Map<Integer, String> getCodeMap() {
		return codeMap;
	}

	public Cell[] getSymbols() {
		return symbols;
	}
}
