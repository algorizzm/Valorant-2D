import java.awt.*;

public class Tile {
	public enum Type {
		FLOOR, WALL
	}

	private int x, y;
	private Type type;
	private final int size = 40;
	private Rectangle bounds;

	public Tile(int x, int y, Type type) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.bounds = new Rectangle(x, y, size, size);
	}

	public void draw(Graphics g) {
		switch (type) {
		case FLOOR -> g.setColor(Color.DARK_GRAY);
		case WALL -> g.setColor(Color.GRAY);
		}
		g.fillRect(x, y, size, size);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, size, size); // grid lines
	}

	public Type getType() {
		return type;
	}

	public Rectangle getBounds() {
		return bounds;
	}
}