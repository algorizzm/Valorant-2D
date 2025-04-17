import java.awt.*;

public class Player {
	private int x, y;
	private final int SIZE = 40;
	private final int SPEED = 5;
	private Rectangle bounds;
	private Tile[][] map; // <-- store the tilemap here

	public Player(int x, int y, Tile[][] map) {
		this.x = x;
		this.y = y;
		this.map = map;
		bounds = new Rectangle(x, y, SIZE, SIZE);
	}

	public void move(boolean up, boolean down, boolean left, boolean right) {
		int dx = 0, dy = 0;
		if (up)
			dy -= SPEED;
		if (down)
			dy += SPEED;
		if (left)
			dx -= SPEED;
		if (right)
			dx += SPEED;

		// Check horizontal movement
		if (!collidesWithWall(x + dx, y)) {
			x += dx;
		}

		// Check vertical movement
		if (!collidesWithWall(x, y + dy)) {
			y += dy;
		}

		bounds.setLocation(x, y);
	}

	private boolean collidesWithWall(int nextX, int nextY) {
		Rectangle nextBounds = new Rectangle(nextX, nextY, SIZE, SIZE);

		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[0].length; col++) {
				Tile tile = map[row][col];
				if (tile.getType() == Tile.Type.WALL && nextBounds.intersects(tile.getBounds())) {
					return true;
				}
			}
		}
		return false;
	}

	public void draw(Graphics g) {
		g.setColor(Color.BLUE);
		g.fillRect(x, y, SIZE, SIZE);
	}

	public int getX() {
		return x + SIZE / 2;
	}

	public int getY() {
		return y + SIZE / 2;
	}
}
