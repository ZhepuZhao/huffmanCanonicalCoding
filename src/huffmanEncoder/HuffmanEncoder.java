package huffmanEncoder;

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

	private InputStream input;
	private BitSource source;
	private OutputStream output;
	private BitSink sink;
	private String encodingFile;
	private Map<Integer, Double> frequencyMap; // <symbol, frequency of symbol>
	private Map<Integer, String> codeMap; // <symbol, codeword> for original huffman tree
	private Cell[] symbols;
	private Queue<Node> symbolHeap;
	private int symbolNum;
	private Node root;

	public HuffmanEncoder(String encodingFile, String outputFile, int symbolNum) throws FileNotFoundException {
		this.input = new FileInputStream(encodingFile);
		this.source = new InputStreamBitSource(input);
		this.output = new FileOutputStream(outputFile);
		this.sink = new OutputStreamBitSink(output);
		this.frequencyMap = new HashMap<Integer, Double>();
		this.codeMap = new HashMap<Integer, String>();
		this.symbolNum = symbolNum;
		this.symbols = new Cell[256];
		this.encodingFile = encodingFile;
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
		this.root = null;
	}

	// <symbol, frequency>
	public void constructFrequencyMap() throws InsufficientBitsLeftException, IOException {
		for (int i = 0; i < this.symbolNum; i++) {
			int currentSymbol = source.next(8);
			frequencyMap.put(currentSymbol, frequencyMap.getOrDefault(currentSymbol, 0.0) + 1);
		}
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
		for (Map.Entry<Integer, Double> entry : frequencyMap.entrySet()) {
			Node node = new Node(entry.getKey());
			node.setFrequency(entry.getValue());
			symbolHeap.add(node);
		}

	}

	public void constructTreeAndGetLength() {

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

		setCodeString(root.getLeft(), true, root.getCode());

		setCodeString(root.getRight(), false, root.getCode());
	}

	public void addEntryToCodeMap(Node root) {
		if (root == null)
			return;
		if (root.getValue() != -1) {
			codeMap.put(root.getValue(), root.getCode().substring(1));
		}
		addEntryToCodeMap(root.getLeft());
		addEntryToCodeMap(root.getRight());
	}

	public void formCodewordSymbolMap(Node root) {
		if (root == null)
			return;
		if (root.getValue() != -1) {
//			System.out.println(root.getValue());
			codeMap.put(root.getValue(), root.getCode().substring(1));
		}
		formCodewordSymbolMap(root.getLeft());
		formCodewordSymbolMap(root.getRight());
	}
	
	// instance methods of decoder
	public void constructSymbols() throws InsufficientBitsLeftException, IOException {
		// for (Map.Entry<Integer, String> entry : codeMap.entrySet()) {
		// symbols.add(new Cell(entry.getValue().length(), entry.getKey()));
		// }
		for (int i = 0; i < symbols.length; i++) {
			// first parameter: length, second parameter: symbol
			symbols[i] = new Cell(codeMap.get(i).length(), i);
		}
	}

	public void sortSymbols() {
		// sort symbols
		Arrays.sort(symbols, new Comparator<Cell>() {
			@Override
			public int compare(Cell c1, Cell c2) {
				return c1.getLength() - c2.getLength();
			}
		});
	}

	public void pruneTree() {
		root = new Node(-1);
		for (Cell cell : symbols) {
			
			pruneBranch(this.root, cell, cell.getLength());
		}
	}

	public Node pruneBranch(Node root, Cell cell, int length) {
		if (length == 0) {
			Node tmp = new Node(cell.getSymbol());
			tmp.setLeftFull(true);
			tmp.setRightFull(true);
			System.out.println(cell.getSymbol());
			return tmp;
		}
		if (root == null)
			root = new Node(-1);
		if (!root.isLeftFull()) {
			root.setLeft(pruneBranch(root.getLeft(), cell, length - 1));
			root.setLeftFull(root.getLeft().isLeftFull() && root.getLeft().isRightFull());
		} else {
			root.setRight(pruneBranch(root.getRight(), cell, length - 1));
			root.setRightFull(root.getRight().isLeftFull() && root.getRight().isRightFull());
		}
		return root;
	}



	public void outputFile() throws InsufficientBitsLeftException, IOException {
		input.close();
		source = new InputStreamBitSource(new FileInputStream(encodingFile));

		for (int i = 0; i < symbolNum; i++) {
			int tmp = source.next(8);
			// System.out.println((char)tmp);
			if (codeMap.containsKey(tmp)) {
				sink.write(codeMap.get(tmp));
			}
		}
		sink.padToWord();
	}

	public void outputLength() throws IOException {
		// write the length of symbols
		for (int i = 0; i < 256; i++) {
			sink.write(codeMap.get(i).length(), 8);
		}
		// write total number of symbols
		sink.write(symbolNum, 32);
	}

	public void encode() throws InsufficientBitsLeftException, IOException {

		constructFrequencyMap();
		constructFrequencyHeap();
		constructTreeAndGetLength();
		setCodeString(root, true, "");
		addEntryToCodeMap(root);
		constructSymbols();
		sortSymbols();
		
		pruneTree();
		setCodeString(root, true, "");
		codeMap.clear();
		formCodewordSymbolMap(root);
		outputLength();
		// symbolNum = source.next(32);
		outputFile();
	}

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
