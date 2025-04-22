import javax.swing.*;
import java.awt.*;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class Game {

    private static GameState currentState = GameState.TITLE; // Default state

    public static GameState getGameState() {
        return currentState;
    }

    public static void setGameState(GameState newState) {
        currentState = newState;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Valorant 2D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true); // Removes title bar
        frame.setResizable(false);

        // Get the screen dimensions
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        gd.setFullScreenWindow(frame); // Makes frame fullscreen

        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        frame.setVisible(true);  // Ensure this is before gamePanel.start()

        gamePanel.start();  // Start the game after frame is visible
    }
}
