package huffmanDecoder;

public class Node {

	private Node left;
	private Node right;
	private int value;
	private String code;
	private double frequency;
	private int height;
	private boolean leftFull;
	private boolean rightFull;

	public Node(int value) {
		// TODO Auto-generated constructor stub
		this.value = value;
		this.left = null;
		this.right = null;
		this.height = 0;
		this.code = "";
		this.leftFull = false;
		this.rightFull = false;
	}

	public void setLeftFull(boolean leftFull) {
		this.leftFull = leftFull;
	}

	public void setRightFull(boolean rightFull) {
		this.rightFull = rightFull;
	}

	public boolean isLeftFull() {
		return leftFull;
	}

	public boolean isRightFull() {
		return rightFull;
	}

	public Node getLeft() {
		return left;
	}

	public void setLeft(Node left) {
		this.left = left;
	}

	public Node getRight() {
		return right;
	}

	public void setRight(Node right) {
		this.right = right;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
}
