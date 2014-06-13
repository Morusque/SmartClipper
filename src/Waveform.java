import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;

public class Waveform {

	Window parent;

	int x, y, w, h;
	int[] average;
	int[][] max;
	boolean[] over;
	double limit = 1;

	int attack = 0;// in px
	int release = 0;// in px

	int compType = 0;

	int tokenA = -1;
	int tokenB = -1;

	public int[] getAverage() {
		return average;
	}

	public void setAverage(int[] average) {
		this.average = average;
	}

	public int[][] getMax() {
		return max;
	}

	public void setMax(int[][] max) {
		this.max = max;
	}

	public boolean[] getOver() {
		return over;
	}

	public void setOver(boolean[] over) {
		this.over = over;
	}

	public int getCompType() {
		return compType;
	}

	public void setCompType(int compType) {
		this.compType = compType;
	}

	public Waveform(Window p, int x, int y, int w, int h) {
		this.parent = p;
		this.x = x;
		this.y = y;
		this.w = w - 1;
		this.h = h - 1;
		this.average = new int[w];
		this.max = new int[w][2];
		this.over = new boolean[w];
	}

	public void displayGain() {
		parent.g.setStroke(new BasicStroke(1.0f));
		parent.g.setColor(new Color(0x000000));
		parent.g.fillRect(x, y, w + 1, h + 1);
		parent.g.setColor(Color.white);
		parent.g.draw(new Rectangle(x, y, w + 1, h + 1));
		for (int xP = 0; xP < w; xP++) {
			if (over[xP]) {
				parent.g.setColor(Color.red);
			} else {
				parent.g.setColor(Color.green);
			}
			parent.g.drawLine(x + xP + 1, y + h, x + xP + 1, y + h
					- average[xP]);
		}
	}

	public void displayTokens() {
		int tokenAPx = (int) ((float) tokenA * w / parent.getSampleLength());
		int tokenBPx = (int) ((float) tokenB * w / parent.getSampleLength());
		parent.g.setColor(Color.red);
		parent.g.drawLine(x + tokenAPx, y, x + tokenAPx, y + h);
		parent.g.drawLine(x + tokenBPx, y, x + tokenBPx, y + h);
	}

	public void displayMaxes() {
		parent.g.setStroke(new BasicStroke(1.0f));
		parent.g.setColor(new Color(0x000000));
		parent.g.fillRect(x, y, w + 1, h + 1);
		parent.g.setColor(Color.white);
		parent.g.draw(new Rectangle(x, y, w + 1, h + 1));
		for (int xP = 0; xP < w; xP++) {
			if (over[xP]) {
				parent.g.setColor(Color.red);
			} else {
				parent.g.setColor(Color.green);
			}
			parent.g.drawLine(x + xP + 1, y + (h / 2) - max[xP][1] / 2 + 1, x
					+ xP + 1, y + (h / 2) - max[xP][0] / 2 + 1);
		}
	}

	public void displayCompression() {
		parent.g.setStroke(new BasicStroke(1.0f));
		parent.g.setColor(Color.red);
		parent.g.draw(new Rectangle(x + 3, y + 3, attack, 5));
		parent.g.setColor(Color.green);
		parent.g.draw(new Rectangle(x + w - 3 - release, y + 3, release, 5));
	}

	public void setLimit(double limit) {
		this.limit = limit;
	}

	public void setTokens(int a, int b) {
		tokenA = a;
		tokenB = b;
	}

	public boolean click(int x2, int y2, int mouseButton) {
		// if inside the rectangle
		if (x2 >= x && x2 <= x + w && y2 >= y && y2 <= y + h) {
			if (mouseButton == 1) {
				tokenA = (int) ((float) (x2 - x));
				tokenA = (int) ((float) tokenA * parent.getSampleLength() / w);
				// sets it before tokenB
				tokenA = Math.min(tokenB - 1, tokenA);
			}
			if (mouseButton == 3) {
				tokenB = (int) ((float) (x2 - x));
				tokenB = (int) ((float) tokenB * parent.getSampleLength() / w);
				// sets it after tokenA
				tokenB = Math.max(tokenA + 1, tokenB);
			}
			return true;
		}
		return false;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int[] getTokenSamples() {
		int[] result = new int[2];
		result[0] = tokenA;
		result[1] = tokenB;
		return result;
	}

	public void setAverageFrom(float[] average2) {
		for (int i = 0; i < w; i++) {
			average[i] = (int) (average2[i] * (float) h);
		}
	}

	public void setOverFrom(boolean[] over2) {
		for (int i = 0; i < w; i++) {
			over[i] = over2[i];
		}
	}

	public void setMaxFrom(float[][] max2) {
		for (int i = 0; i < w; i++) {
			max[i][0] = (int) (max2[i][0] * (float) h);
			max[i][1] = (int) (max2[i][1] * (float) h);
		}
	}

	public void setPosition(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public void setEnvelope(int attack, int release, float oneBarDuration) {
		this.attack = (int) (attack / oneBarDuration);
		this.release = (int) (release / oneBarDuration);
		// don't exceed the window
		this.attack = Math.min(this.attack, w - 6);
		this.release = Math.min(this.release, w - 6);
	}

}
