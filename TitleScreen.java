import java.awt.*;
import java.awt.event.KeyEvent;

public class TitleScreen {

    private Font titleFont, instructionFont;
    private Color titleColor, instructionColor;
    private int WIDTH, HEIGHT;
    
    private MenuScreen menuScreen;

    // Constructor that initializes the screen width and height
    public TitleScreen(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        // Initialize fonts and colors
        titleFont = new Font("Arial", Font.BOLD, 72);
        instructionFont = new Font("Arial", Font.PLAIN, 24);
        titleColor = new Color(255, 255, 255);  // White color for the title
        instructionColor = new Color(200, 200, 200);  // Light grey for instructions
    }

    // Render the title screen visuals
    public void render(Graphics g) {
        // Set background color to dark gray
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, WIDTH, HEIGHT);  // Use the instance variables for width and height

        // Draw the game title (e.g., "My Shooter Game")
        g.setColor(titleColor);
        g.setFont(titleFont);
        String title = "My Shooter Game";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (WIDTH - titleWidth) / 2, HEIGHT / 3);

        // Draw instructions (e.g., "Press Enter to Start")
        g.setColor(instructionColor);
        g.setFont(instructionFont);
        String instruction = "Press Enter to Start";
        int instructionWidth = g.getFontMetrics().stringWidth(instruction);
        g.drawString(instruction, (WIDTH - instructionWidth) / 2, HEIGHT / 2 + 100);
    }

    // Handle user input during the title screen
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        	
            Game.setGameState(GameState.MENU);
            
        }
    }
}
