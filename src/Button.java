import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;

public class Button {

	Window parent;

	int x, w, y, h;
	String label;

	Button(Window p, int x, int y, int w, int h, String label) {
		this.parent = p;
		this.x = x;
		this.w = w;
		this.y = y;
		this.h = h;
		this.label = label;
	}

	public void display() {
		parent.g.setStroke(new BasicStroke(1.0f));
		parent.g.setColor(new Color(0x000000));
		parent.g.fillRect(x, y, w, h);
		parent.g.setColor(Color.white);
		parent.g.drawString(label, x + 2, y + h - 2);
		parent.g.draw(new Rectangle(x, y, w, h));
	}

	public boolean isAt(int x2, int y2) {
		if (x2 >= x && x2 <= x + w && y2 >= y && y2 <= y + h) {
			return true;
		}
		return false;
	}

	public void setPosition(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
}
