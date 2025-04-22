import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class MenuScreen implements MouseListener {

    private final ArrayList<MenuButton> buttons = new ArrayList<>();
    private final GamePanel gamePanel;

    public MenuScreen(GamePanel gamePanel) {
        this.gamePanel = gamePanel;

        buttons.add(new MenuButton("Play", () -> Game.setGameState(GameState.PLAYING)));
        buttons.add(new MenuButton("Options", () -> {}));
        buttons.add(new MenuButton("Exit", () -> System.exit(0)));

        gamePanel.addMouseListener(this);
    }

    public void render(Graphics g) {
        int width = gamePanel.getWidth();
        int height = gamePanel.getHeight();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Main Menu", width / 2 - 110, 100);

        int btnWidth = 200;
        int btnHeight = 40;
        int startX = width / 2 - btnWidth / 2;
        int startY = height / 2 - 60;

        for (int i = 0; i < buttons.size(); i++) {
            MenuButton btn = buttons.get(i);
            int y = startY + i * 60;
            btn.setBounds(startX, y, btnWidth, btnHeight);
            btn.render(g);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (Game.getGameState() == GameState.MENU) {
            for (MenuButton btn : buttons) {
                if (btn.isHovered(e.getX(), e.getY())) {
                    btn.click();
                }
            }
        }
    }

    // Unused
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    // ========== Inner Class ==========
    private static class MenuButton {
        private final String label;
        private final Runnable onClick;
        private int x, y, width, height;

        public MenuButton(String label, Runnable onClick) {
            this.label = label;
            this.onClick = onClick;
        }

        public void setBounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void render(Graphics g) {
            g.setColor(Color.GRAY);
            g.fillRect(x, y, width, height);

            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);

            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString(label, x + 20, y + 28);
        }

        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                   mouseY >= y && mouseY <= y + height;
        }

        public void click() {
            onClick.run();
        }
    }
}
