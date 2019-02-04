package huffmanDecoder;

public class Cell {

	private int length;
	private int symbol;
	private double frequency;

	public Cell(int length, int symbol) {
		// TODO Auto-generated constructor stub
		this.length = length;
		this.symbol = symbol;
		this.frequency = 0;

	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getSymbol() {
		return symbol;
	}

	public void setSymbol(int symbol) {
		this.symbol = symbol;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	

}
