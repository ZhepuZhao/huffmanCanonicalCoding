package huffmanDecoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import io.BitSink;
import io.BitSource;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;
import io.OutputStreamBitSink;

public class HuffmanDecoder {

	private Cell[] symbols;
	private Node root;
	// <huffman code,symbol> pair
	private Map<String, Integer> map;
	private InputStream input;
	private BitSource source;
	private OutputStream output;
	private BitSink sink;
	private String decodingFile;
	private int symbolNum;

	public HuffmanDecoder(String decodingFile, String outputFile) throws FileNotFoundException {
		this.symbols = new Cell[256];
		// this.symbols = new Cell[83];
//		this.root = null;
		this.root = new Node(-1);
		this.map = new HashMap<String, Integer>(); // key: Huffman code; value: symbol value
		this.input = new FileInputStream(decodingFile);
		this.source = new InputStreamBitSource(input);
		this.output = new FileOutputStream(outputFile);
		this.sink = new OutputStreamBitSink(output);
		this.symbolNum = 0;
	}

	// getters below
	public Cell[] getSymbols() {
		return symbols;
	}

	public int getMaxLength() {
		return symbols[symbols.length - 1].getLength();
	}

	public Node getRoot() {
		return root;
	}

	public Map<String, Integer> getMap() {
		return map;
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

	public String getDecodingFile() {
		return decodingFile;
	}

	public int getSymbolNum() {
		return symbolNum;
	}

	// instance methods of decoder
	public void constructSymbols() throws InsufficientBitsLeftException, IOException {
		for (int i = 0; i < symbols.length; i++) {
			// first parameter: length, second parameter: symbol
			int tmp = source.next(8);
//			System.out.println(tmp);
			symbols[i] = new Cell(tmp, i);
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

	public void decode() throws InsufficientBitsLeftException, IOException {
		constructSymbols();
		sortSymbols();
		symbolNum = source.next(32);
//		symbolNum = 574992;
		System.out.println(symbolNum);
		// constructSymbolHeap();
//		root = constructTree(new Node(-1), getMaxLength(), true, "");
//		root = constructTree(new Node(-1), getMaxLength(), "");
		pruneTree();
		setCodeString(root, true, "");
		formCodewordSymbolMap(this.root);
		// symbolNum = source.next(32);
		outputFile();
	}

	public void pruneTree() {
		for (Cell cell : symbols) {
			pruneBranch(this.root, cell, cell.getLength());
		}
	}

	public Node pruneBranch(Node root, Cell cell, int length) {
		if (length == 0) {
			Node tmp = new Node(cell.getSymbol());
			tmp.setLeftFull(true);
			tmp.setRightFull(true);
			return tmp;
		}
		if (root == null) root = new Node(-1);
		if (!root.isLeftFull()) {
			root.setLeft(pruneBranch(root.getLeft(), cell, length - 1));
			root.setLeftFull(root.getLeft().isLeftFull() && root.getLeft().isRightFull());
		} else {
			root.setRight(pruneBranch(root.getRight(), cell, length - 1));
			root.setRightFull(root.getRight().isLeftFull() && root.getRight().isRightFull());
		}
		return root;
	}
	public void setCodeString(Node root, boolean isLeft, String s) {
		if (root == null) return;

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
	
//	public Node constructTree(Node root, int length, String s) {
//		Queue<Node> queue = new LinkedList<Node>();		
//		queue.offer(root);
//		while (!queue.isEmpty()) {
//			Node temp = queue.poll();
//			if (temp.getHeight() + 1 <= length) {
//				Node left = new Node(-1);
//				left.setCode(temp.getCode() + "0");
//				left.setHeight(temp.getHeight() + 1);
//				Node right = new Node(-1);
//				right.setCode(temp.getCode() + "1");
//				right.setHeight(temp.getHeight() + 1);
//				temp.setLeft(left);
//				temp.setRight(right);
//				queue.offer(left);
//				queue.offer(right);
//			} 
//		}
//		return root;
//	}
//
//	public void pruneTree() {
//		for (Cell cell : symbols) {
//			pruneBranch(this.root, cell, cell.getLength());
//		}
//	}
//
//	public boolean pruneBranch(Node root, Cell cell, int length) {
//		if (root == null || root.getValue() != -1)
//			return false;
//		if (length == 0) {
//			if (root.getValue() == -1) {
//				root.setValue(cell.getSymbol());
//				return true;
//			} else {
//				return false;
//			}
//		} else {
//			if (!pruneBranch(root.getLeft(), cell, length - 1)) {
//				return pruneBranch(root.getRight(), cell, length - 1);
//			} else {
//				return true;
//			}
//		}
//	}

	public void formCodewordSymbolMap(Node root) {
		if (root == null)
			return;
		if (root.getValue() != -1) {
			map.put(root.getCode().substring(1), root.getValue());
//			map.put(root.getCode(), root.getValue());
			return;
		}
		formCodewordSymbolMap(root.getLeft());
		formCodewordSymbolMap(root.getRight());
	}

	public void outputFile() throws InsufficientBitsLeftException, IOException {
		boolean decoded = false;
		for (int i = 0; i < symbolNum; i++) {
			StringBuilder sb = new StringBuilder();
			while (!decoded) {
				
				sb.append(source.next(1));
				if (map.containsKey(sb.toString())) {
					decoded = true;
					System.out.println((char)map.get(sb.toString()).intValue());
					sink.write(map.get(sb.toString()), 8);
				}
			}
			decoded = false;
		}
	}

}
