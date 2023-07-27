import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.JOptionPane;

public class Slider {

	Window parent;

	int x, w, y, h;
	boolean vertical;
	Color c;
	float value;
	String dialogA;
	float[] range = new float[2];

	public float[] getRange() {
		return range;
	}

	public void setRange(float a, float b) {
		this.range = new float[] { a, b };
	}

	public String getDialogA() {
		return dialogA;
	}

	public void setDialogA(String dialogA) {
		this.dialogA = dialogA;
	}

	public float getValue() {
		return value * (range[1] - range[0]) + range[0];
	}

	public void setValue(float value) {
		this.value = value;
	}

	Slider(Window p, int x, int y, int w, int h, boolean vertical, Color c) {
		this.range = new float[] { 0, 1 };
		this.parent = p;
		this.x = x;
		this.w = w - 1;
		this.y = y;
		this.h = h - 1;
		this.c = c;
		this.vertical = vertical;
	}

	public void display() {
		parent.g.setStroke(new BasicStroke(1.0f));
		parent.g.setColor(new Color(0x000000));
		parent.g.fillRect(x, y, w + 1, h + 1);
		parent.g.setColor(Color.white);
		parent.g.draw(new Rectangle(x, y, w + 1, h + 1));
		parent.g.setColor(c);
		if (vertical) {
			parent.g.fillRect(x + 1, y + h - (int) (h * Math.min(value, 1)), w,
					(int) (h * Math.min(value, 1)) + 1);
		} else {
			parent.g.fillRect(x + w - (int) (w * Math.min(value, 1)), y + 1,
					(int) (w * Math.min(value, 1)) + 1, h);
		}
	}

	public void click(int x2, int y2) {
		if (x2 >= x && x2 <= x + w && y2 >= y && y2 <= y + h) {
			if (vertical) {
				value = (float) (y + h - y2) / h;
			} else {
				value = (float) (x + w - x2) / w;
			}
		}
	}

	public void rightClick(int x2, int y2) {
		if (x2 >= x && x2 <= x + w && y2 >= y && y2 <= y + h) {
			String userInput = JOptionPane.showInputDialog(dialogA);
			if (userInput != null) {
				if (!userInput.isEmpty()) {
					value = Float.parseFloat(userInput) / (range[1] - range[0])
							+ range[0];
				}
			}
		}
		parent.compute();
		parent.display();
	}

	public void setPosition(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
}
