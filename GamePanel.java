import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

public class GamePanel extends Canvas implements Runnable, KeyListener, MouseListener, MouseMotionListener {

	private GameState gameState = GameState.MENU;
	private Thread thread;
	private boolean isRunning = false;

	private final int TILE_SIZE = 40;
	private Tile[][] map;

	private final int WIDTH = 800, HEIGHT = 600;

	private Player player;
	private ArrayList<Bullet> bullets = new ArrayList<>();

	private boolean up, down, left, right;
	private int mouseX, mouseY;
	private boolean isMouseDown = false;

	// Vandal mechanics
	private WeaponType currentWeapon = WeaponType.PRIMARY;

	private final int MAX_MAG_SIZE = 30;
	private final int MAX_RESERVE_AMMO = 60;
	private int bulletsInMag = MAX_MAG_SIZE;
	private int reserveAmmo = MAX_RESERVE_AMMO;
	private final int FIRE_RATE_MS = 114; // ~8.775 rounds/sec
	private final int RELOAD_TIME_MS = 2500;
	private long lastFiredTime = 0;
	private boolean isReloading = false;
	private long reloadStartTime = 0;

	// Classic mechanics

	private final int MAX_PISTOL_MAG = 12;
	private final int MAX_PISTOL_RESERVE = 36;
	private int pistolBulletsInMag = MAX_PISTOL_MAG;
	private int pistolReserveAmmo = MAX_PISTOL_RESERVE;
	private final int PISTOL_FIRE_RATE_MS = 300;

	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		int cols = WIDTH / TILE_SIZE;
		int rows = HEIGHT / TILE_SIZE;
		map = new Tile[rows][cols];

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				Tile.Type type = (row == 0 || row == rows - 1 || col == 0 || col == cols - 1) ? Tile.Type.WALL
						: Tile.Type.FLOOR;
				map[row][col] = new Tile(col * TILE_SIZE, row * TILE_SIZE, type);
			}
		}

		// Move this here after map is initialized
		player = new Player(2 * TILE_SIZE, 2 * TILE_SIZE, map);
	}

	public synchronized void start() {
		isRunning = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop() {
		isRunning = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;

		while (isRunning) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;

			while (delta >= 1) {
				tick();
				delta--;
			}

			render();
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				frames = 0;
			}
		}

		stop();
	}

	public void tick() {
		// Reload logic
		if (isReloading) {
			if (System.currentTimeMillis() - reloadStartTime >= RELOAD_TIME_MS) {
				if (currentWeapon == WeaponType.PRIMARY) {
					int needed = MAX_MAG_SIZE - bulletsInMag;
					int toReload = Math.min(needed, reserveAmmo);
					bulletsInMag += toReload;
					reserveAmmo -= toReload;
				} else if (currentWeapon == WeaponType.SECONDARY) {
					int needed = MAX_PISTOL_MAG - pistolBulletsInMag;
					int toReload = Math.min(needed, pistolReserveAmmo);
					pistolBulletsInMag += toReload;
					pistolReserveAmmo -= toReload;
				}
				isReloading = false;
			}
		}

		// Shooting logic
		if (isMouseDown && !isReloading) {
			long currentTime = System.currentTimeMillis();

			switch (currentWeapon) {
			case PRIMARY -> {
				if (currentTime - lastFiredTime >= FIRE_RATE_MS && bulletsInMag > 0) {
					shootBullet();
					bulletsInMag--;
					lastFiredTime = currentTime;
				}
			}
			case SECONDARY -> {
				if (currentTime - lastFiredTime >= PISTOL_FIRE_RATE_MS && pistolBulletsInMag > 0) {
					shootBullet();
					pistolBulletsInMag--;
					lastFiredTime = currentTime;
				}
			}
			case KNIFE -> {
				// We'll add melee logic later (like short-range slash)
			}
			}

			// Reload if mag is empty
			if (currentWeapon == WeaponType.PRIMARY && bulletsInMag <= 0 && reserveAmmo > 0)
				startReload();
			if (currentWeapon == WeaponType.SECONDARY && pistolBulletsInMag <= 0 && pistolReserveAmmo > 0)
				startReload();
		}

		player = new Player(100, 100, map); // Replace 100, 100 with your spawn point

		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			b.update();
			if (b.isOffScreen(WIDTH, HEIGHT)) {
				bullets.remove(i);
				i--;
			}
		}
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();

		// Draw map tiles
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map[0].length; col++) {
				map[row][col].draw(g);
			}
		}

		// Draw player and bullets
		player.draw(g);
		for (Bullet b : bullets) {
			b.draw(g);
		}

		// Draw HUD (weapon & ammo info)
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.PLAIN, 16));

		switch (currentWeapon) {
		case PRIMARY -> {
			g.drawString("Weapon: VANDAL", 10, 20);
			g.drawString("Ammo: " + bulletsInMag + " / " + reserveAmmo, 10, 40);
			if (isReloading) {
				g.drawString("Reloading...", 10, 60);
			}
		}
		case SECONDARY -> {
			g.drawString("Weapon: PISTOL", 10, 20);
			g.drawString("Ammo: " + pistolBulletsInMag + " / " + pistolReserveAmmo, 10, 40);
			if (isReloading) {
				g.drawString("Reloading...", 10, 60);
			}
		}
		case KNIFE -> {
			g.drawString("Weapon: KNIFE", 10, 20);
			// No ammo or reloading info for knife
		}
		}

		g.dispose();
		bs.show();
	}

	private void shootBullet() {
		double angle = Math.atan2(mouseY - player.getY(), mouseX - player.getX());
		bullets.add(new Bullet(player.getX(), player.getY(), angle));
	}

	private void startReload() {
		isReloading = true;
		reloadStartTime = System.currentTimeMillis();
	}

	// ===== KeyListener =====
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W -> up = true;
		case KeyEvent.VK_S -> down = true;
		case KeyEvent.VK_A -> left = true;
		case KeyEvent.VK_D -> right = true;
		case KeyEvent.VK_R -> {
			if (!isReloading && reserveAmmo > 0 && bulletsInMag < MAX_MAG_SIZE) {
				startReload();
			}
		}
		case KeyEvent.VK_1 -> currentWeapon = WeaponType.PRIMARY;
		case KeyEvent.VK_2 -> currentWeapon = WeaponType.SECONDARY;
		case KeyEvent.VK_3 -> currentWeapon = WeaponType.KNIFE;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W -> up = false;
		case KeyEvent.VK_S -> down = false;
		case KeyEvent.VK_A -> left = false;
		case KeyEvent.VK_D -> right = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	// ===== MouseListener =====
	@Override
	public void mousePressed(MouseEvent e) {
		isMouseDown = true;
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		isMouseDown = false;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
