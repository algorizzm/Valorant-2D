import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GamePanel extends Canvas implements Runnable, KeyListener, MouseListener, MouseMotionListener {

    private Thread thread;
    private boolean isRunning = false;

    private Tile[][] map;  // 2D array of tiles representing the game world

    private final int TILE_SIZE = 40;
    private int WIDTH, HEIGHT;  // Game window dimensions

    private Player player;  // The player object
    private ArrayList<Bullet> bullets = new ArrayList<>();  // List of active bullets in the game

    private boolean up, down, left, right;  // Direction flags for player movement
    private int mouseX, mouseY;  // Mouse position
    private boolean isMouse1Down = false;  // Mouse1 button state
    private boolean isMouse3Down = false; // Mouse2 button state

    private HashMap<WeaponType, Weapon> weapons = new HashMap<>();  // Weapon collection

    private WeaponType currentWeapon = WeaponType.PRIMARY;  // Current weapon being used by the player
    
    // Switching settings
    private boolean isSwitchingWeapon = false;
    private long switchStartTime = 0;
    private long switchDuration = 0;

    private TitleScreen titleScreen;  // Title screen object
    private MenuScreen menuScreen; // Menu screen object
    
    // Movement and Recoil Settings
    private boolean isPlayerMoving = false;
    private boolean isPlayerSpraying = false;
    private long lastMoveTime = 0;
    private final long movementCooldown = 1000;  // Time in milliseconds before recoil factor lowers
    private float currentRecoilFactor = 3f;
    private final float defaultRecoilFactor = 1f;  // The minimum recoil factor when stationary

    public GamePanel() {
        // Set the preferred size for the game window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = (int) screenSize.getWidth();
        HEIGHT = (int) screenSize.getHeight();
        setPreferredSize(screenSize);

        // Add listeners for key and mouse input
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        // Initialize the game map with walls and floors
        int cols = (int) Math.ceil((double) WIDTH / TILE_SIZE);
        int rows = (int) Math.ceil((double) HEIGHT / TILE_SIZE);
        
        map = new Tile[rows][cols];

        // Initialize map tiles
        for (int row = 0; row < rows; row++) {
        	for (int col = 0; col < cols; col++) {
        		Tile.Type type = (row == 0 || row == rows - 1 || col == 0 || col == cols - 1)
        			? Tile.Type.WALL
        			: Tile.Type.FLOOR;
        		map[row][col] = new Tile(col * TILE_SIZE, row * TILE_SIZE, type);
        	}
        }

        // Initialize the player object after the map is set up
        player = new Player(2 * TILE_SIZE, 2 * TILE_SIZE, map);

        // Initialize weapons with their respective properties
        weapons.put(WeaponType.PRIMARY, new Weapon(WeaponType.PRIMARY, 30, 60, 114, 1000, 2500, 0));
        weapons.put(WeaponType.SECONDARY, new Weapon(WeaponType.SECONDARY, 12, 36, 300, 750, 1750, 300));
        weapons.put(WeaponType.KNIFE, new Weapon(WeaponType.KNIFE, 1, 0, 1000, 750, 0, 0));  // Placeholder weapon (knife)

        // Initialize Screen objects
        titleScreen = new TitleScreen(WIDTH, HEIGHT);
        menuScreen = new MenuScreen(this);

    }

    // Starts the game loop
    public synchronized void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    // Stop the game loop
    public synchronized void stop() {
        isRunning = false;
        try {
            thread.join();  // Wait for the thread to finish before stopping
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        requestFocus();  // Focus on the game panel for input events
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0; // Can be used to track frames if wanted

        // Main game loop
        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                tick();  // Update game logic
                delta--;
            }

            render();  // Render game visuals
            frames++;

            // Limit frame rate to 60 FPS
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frames = 0;
            }
        }

        stop();
    }

    // Update game state (player movement, weapon logic, etc.)
    public void tick() {
        if (Game.getGameState() == GameState.PLAYING) {
            // Update game logic when the game state is PLAYING
            player.tick(up, down, left, right);

            // Always update bullets
            for (int i = 0; i < bullets.size(); i++) {
                Bullet b = bullets.get(i);
                b.update();
                if (b.isOffScreen(WIDTH, HEIGHT)) {
                    bullets.remove(i);
                    i--;
                }
            }

            // Weapon switching block
            if (isSwitchingWeapon) {
                long elapsed = System.currentTimeMillis() - switchStartTime;
                if (elapsed >= switchDuration) {
                    isSwitchingWeapon = false;
                } else {
                    // Still switching – return early or prevent actions like firing
                    return;
                }
            }


            // After switching is complete, resume full weapon logic
            Weapon weapon = weapons.get(currentWeapon);
            weapon.updateReload();

            if (isMouse1Down && weapon.canShoot()) {
                shootBullet();
                weapon.shoot();
            }
            
            if (isMouse3Down && weapon.canSpray()) {
            	if(currentWeapon == WeaponType.SECONDARY) {
                	sprayBullet();
                	weapon.spray();
            	}
            }

            if (weapon.shouldAutoReload()) {
                weapon.startReload();
            }
            
         // Update player movement state
            if (player.isMoving()) {
                isPlayerMoving = true;
                lastMoveTime = System.currentTimeMillis();  // Reset the timer when moving
            } else {
                // If the player hasn't moved for a while, start lowering recoil
                if (System.currentTimeMillis() - lastMoveTime >= movementCooldown) {
                    isPlayerMoving = false;
                }
            }

            // Check if the player is spraying
            if (isMouse3Down && weapon.canShoot()) {
                isPlayerSpraying = true;
            } else {
                isPlayerSpraying = false;
            }
            
         // Adjust recoil factor based on movement and spray state
            if (!isPlayerMoving && !isPlayerSpraying) {
                // Gradually lower recoil factor to the default if the player is stationary
                currentRecoilFactor = Math.max(defaultRecoilFactor, currentRecoilFactor - 0.1f);
            } else {
                // Reset recoil factor to the higher value if the player is moving or spraying
                currentRecoilFactor = 3f;
            }


        }
    }

    // Render the game world, player, bullets, and HUD
    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);  // Create buffer strategy if it doesn't exist
            return;
        }

        Graphics g = bs.getDrawGraphics();

        // Check game state and render accordingly
        if (Game.getGameState() == GameState.TITLE) {
            titleScreen.render(g);  // Render the TitleScreen
        } else if (Game.getGameState() == GameState.MENU) {
        	menuScreen.render(g); // Render the MenuScreen
        } else if (Game.getGameState() == GameState.PLAYING) {
        	
            // Draw map tiles
            renderGameWorld(g);

            // Draw player and bullets
            player.draw(g);
            for (Bullet b : bullets) {
                b.draw(g);
            }

            // Draw HUD (weapon & ammo information)
            Weapon weapon = weapons.get(currentWeapon);
            g.drawString("Weapon: " + currentWeapon.name(), 10, 20);
            if (currentWeapon != WeaponType.KNIFE) {
                g.drawString("Ammo: " + weapon.getBulletsInMag() + " / " + weapon.getReserveAmmo(), 10, 40);
                if (weapon.isReloading()) {
                    g.drawString("Reloading...", 10, 1000);
                }
            }
            
            if (isSwitchingWeapon) {
                g.drawString("Switching weapon...", 10, 80);
            }
            
            
        }

        // Finalize rendering
        g.dispose();
        bs.show();
        
        
    }

    private void renderGameWorld(Graphics g) {
    	for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                map[row][col].draw(g);
            }
        }
	}
	
    // Shoot a bullet
    private void shootBullet() {
        double angle = Math.atan2(mouseY - player.getY(), mouseX - player.getX());

        double recoilMultiplier = player.isMoving() ? 1.0 : 0.0; // No recoil when not moving
        double recoil = Math.toRadians(1 + Math.random() * 2) * recoilMultiplier; // +1° to +3° when moving

        double recoilAngle = angle + recoil;

        bullets.add(new Bullet(player.getX(), player.getY(), recoilAngle));
    }




    // Spray Bullets with Classic
    private void sprayBullet() {
        double angle = Math.atan2(mouseY - player.getY(), mouseX - player.getX());
        double[] offsets = { Math.toRadians(-10), Math.toRadians(-5), 0, Math.toRadians(5), Math.toRadians(10) };

        for (double offset : offsets) {
            double recoilMultiplier = 1.0;  // Default recoil factor for no movement
            if (player.isMoving() || isMouse3Down) {  // Check if the player is moving or spraying
                recoilMultiplier = 3.0;  // Increase recoil when moving or spraying
            }

            // Add random recoil based on movement
            double sprayAngle = angle + offset + (Math.random() - 0.5) * recoilMultiplier * Math.toRadians(10);
            bullets.add(new Bullet(player.getX(), player.getY(), sprayAngle));
        }
    }



    // ===== KeyListener =====
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Handle TITLE state input
        if (Game.getGameState() == GameState.TITLE) {
            if (key == KeyEvent.VK_ENTER) {
                Game.setGameState(GameState.MENU);
                return;
            }
            titleScreen.keyPressed(e);
            return;
        }

        // Handle other game states (like PLAYING)
        switch (key) {
            case KeyEvent.VK_W -> up = true;
            case KeyEvent.VK_S -> down = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_R -> weapons.get(currentWeapon).startReload();  // Reload

            // Weapon switching with prevention of redundant switching
            case KeyEvent.VK_1 -> {
                if (currentWeapon != WeaponType.PRIMARY) {
                    switchWeapon(WeaponType.PRIMARY);
                }
            }
            case KeyEvent.VK_2 -> {
                if (currentWeapon != WeaponType.SECONDARY) {
                    switchWeapon(WeaponType.SECONDARY);
                }
            }
            case KeyEvent.VK_3 -> {
                if (currentWeapon != WeaponType.KNIFE) {
                    switchWeapon(WeaponType.KNIFE);
                }
            }

            case KeyEvent.VK_F11 -> System.exit(0); // Exit game
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
    public void keyTyped(KeyEvent e) {}

    // ===== MouseListener =====
    @Override
    public void mousePressed(MouseEvent e) {
        if (Game.getGameState() == GameState.PLAYING) {
            int button = e.getButton(); // Get the mouse button

            if (button == MouseEvent.BUTTON1) {  // Left mouse button (Primary fire)
                isMouse1Down = true;
                
            } else if (button == MouseEvent.BUTTON3 || currentWeapon == WeaponType.SECONDARY) {  // Right mouse button (Secondary fire for Classic)
                isMouse3Down = true;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isMouse1Down = false;
        isMouse3Down = false;
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

    // ===== MouseMotionListener =====
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

    // Switch weapons based on key presses (1, 2, 3 keys)
    private void switchWeapon(WeaponType newWeapon) {
        if (isSwitchingWeapon) {
            return;  // Don't allow switching while a switch is already in progress
        }
        
        isSwitchingWeapon = true;
        switchStartTime = System.currentTimeMillis();
        currentWeapon = newWeapon;

        // Use the reload time of the selected weapon as the switching duration
        Weapon weapon = weapons.get(newWeapon);
        switchDuration = weapon.getEquipTime();  // dynamically set based on weapon
        System.out.println(switchDuration);
    }

}
