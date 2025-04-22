import java.awt.*;

public class Bullet {
	private double x, y;
	private final double speed = 40;
	private final int size = 6;
	private final double dx, dy;

	// Constructor
	public Bullet(double x, double y, double angle) {
		this.x = x;
		this.y = y;

		// Calculate direction using angle
		dx = Math.cos(angle) * speed;
		dy = Math.sin(angle) * speed;
	}

	// Move the bullet
	public void update() {
		x += dx;
		y += dy;
	}

	// Draw the bullet
	public void draw(Graphics g) {
		g.setColor(Color.YELLOW);
		g.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
	}

	// Check if bullet goes off screen
	public boolean isOffScreen(int width, int height) {
		return x < 0 || x > width || y < 0 || y > height;
	}
}
