package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class StoryScreen extends JPanel implements KeyListener {
    private String[] paragraphs = {
        "En un mundo donde las sombras han consumido la esperanza...",
        "Solo un héroe puede devolver la luz: Moya, el elegido del destino.",
        "Su viaje no será fácil. Los dioses antiguos observan cada paso...",
        "Y el eco de sus decisiones resonará por toda la eternidad."
    };

    private int currentParagraph = 0;
    private String displayedText = "";
    private int charIndex = 0;
    private Timer typingTimer;
    private GameWindow gameWindow;
    private boolean typing = false;
    private boolean finished = false;

    public StoryScreen(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        startTyping();
    }

    private void startTyping() {
        typing = true;
        displayedText = "";
        charIndex = 0;

        typingTimer = new Timer(40, e -> {
            if (charIndex < paragraphs[currentParagraph].length()) {
                displayedText += paragraphs[currentParagraph].charAt(charIndex);
                charIndex++;
                repaint();
            } else {
                typingTimer.stop();
                typing = false;
            }
        });
        typingTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(gameWindow.Pixelart != null ? gameWindow.Pixelart.deriveFont(gameWindow.scaleFont(36f)) : new Font("Monospaced", Font.PLAIN, 28));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(displayedText)) / 2;
        int y = getHeight() / 2;
        g.drawString(displayedText, Math.max(30, x), y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (typing) {
                // Terminar texto instantáneamente
                typingTimer.stop();
                displayedText = paragraphs[currentParagraph];
                typing = false;
                repaint();
            } else {
                // Pasar al siguiente párrafo o terminar historia
                currentParagraph++;
                if (currentParagraph < paragraphs.length) {
                    startTyping();
                } else {
                    finished = true;
                    gameWindow.startRealGame();
                }
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
