import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;

public class Histogram {

	Window parent;

	int x, y, w, h;
	int[] amounts;
	int cursor;

	public Histogram(Window p, int x, int y, int w, int h) {
		this.parent = p;
		this.x = x;
		this.y = y;
		this.w = w - 1;
		this.h = h - 1;
		this.amounts = new int[h];
		this.cursor = h - 1;
	}

	public void compute() {
		// puts the sample values in the array
		int greaterValue = 0;
		for (int i = 0; i < amounts.length; i++) {
			amounts[i] = 0;
		}
		for (int i = 0; i < parent.getSampleLength(); i++) {
			int currentBar = (int) Math.floor(Math.abs(parent.streamSample(i,
					false))
					* (h - 1));
			amounts[currentBar] += 1;
			if (amounts[currentBar] > greaterValue) {
				greaterValue = amounts[currentBar];
			}
		}
		// normalize
		for (int i = 0; i < amounts.length; i++) {
			amounts[i] = (int) ((float) amounts[i] * w / greaterValue);
		}
	}

	public void display() {
		parent.g.setStroke(new BasicStroke(1.0f));
		parent.g.setColor(new Color(0x000000));
		parent.g.fillRect(x, y, w + 1, h + 1);
		parent.g.setColor(Color.white);
		parent.g.draw(new Rectangle(x, y, w + 1, h + 1));
		for (int yP = 0; yP < h; yP++) {
			if (yP > cursor) {
				parent.g.setColor(Color.red);
			} else {
				parent.g.setColor(Color.green);
			}
			parent.g.drawLine(x + w, y + h - yP, x + w - amounts[yP], y + h
					- yP);
		}
		parent.g.setColor(Color.red);
		parent.g.drawLine(x + 1, y + h - cursor, x + w, y + h - cursor);
	}

	public boolean click(int x2, int y2) {
		if (x2 >= x && x2 <= x + w && y2 >= y && y2 <= y + h) {
			cursor = h - y2 + y;
			return true;
		}
		return false;
	}

	public double getLimit() {
		return (double) cursor / h;
	}

	public void setPosition(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

}
