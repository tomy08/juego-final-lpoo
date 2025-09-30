package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class GameSettings extends JPanel {
    private GameWindow gameWindow;

    // Acciones y teclas
    private String[] actions = {
        "Arriba", "Abajo", "Izquierda", "Derecha", "Interactuar", "Pausa", "Adelantar texto"
    };
    private int[] keyCodes = { 
        KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, 
        KeyEvent.VK_E, KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER 
    };

    private int contentOffsetY = 0; // desplazamiento vertical del contenido
    private int scrollSpeed = 20;   // velocidad del scroll

    public static Font Pixelart;

    public GameSettings(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);      
        setFocusable(true);              
        requestFocusInWindow();          
        setPreferredSize(new Dimension(1600, 1200)); // tamaño total que se puede recorrer

        // Key Binding para ESC
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "backToMenu");
        getActionMap().put("backToMenu", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                gameWindow.showMainMenu();
            }
        });

        // Scroll con rueda del mouse
        addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            contentOffsetY += notches * scrollSpeed;

            // Limitar desplazamiento
            if (contentOffsetY < 0) contentOffsetY = 0;
            if (contentOffsetY > 400) contentOffsetY = 400; // ajustar según contenido

            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Rectángulo fijo como marco
        g2.setColor(Color.WHITE);
        g2.setStroke(new java.awt.BasicStroke(5));
        g2.drawRect(420, 200, 1200, 700);

        // Título principal fuera del rectángulo
        g2.setFont(GameWindow.Pixelart.deriveFont(72f));
        g2.drawString("CONFIGURACIÓN", 100, 100);

        // Clipping para limitar contenido dentro del rectángulo
        g2.setClip(420, 200, 1200, 700);

        // Contenido dentro del rectángulo (desplazable)
        int scrollY = contentOffsetY; // scroll vertical
        int baseX = 500;       // posición horizontal inicial
        int baseY = 250;       // posición vertical inicial

        // Subtítulo dentro del rectángulo (se mueve con scroll)
        g2.setFont(GameWindow.Pixelart.deriveFont(56f));
        g2.drawString("-TECLAS-", baseX, baseY - scrollY);

        // Opciones
        g2.setFont(GameWindow.Pixelart.deriveFont(48f));
        String[] options = {
            "W - ARRIBA",
            "S - ABAJO",
            "A - IZQUIERDA",
            "D - DERECHA",
            "E - INTERACTUAR",
            "ESC - PAUSA",
            "ENTER - ADELANTAR TEXTO"
        };
        int spacing = 100; // separación vertical
        for (int i = 0; i < options.length; i++) {
            int y = baseY + 60 + i * spacing - scrollY; // 60 píxeles debajo del subtítulo
            g2.setColor(Color.WHITE);
            g2.drawString(options[i], baseX, y);
        }

        // Fin clipping
        g2.setClip(null);

        // Indicaciones fijas fuera del rectángulo
        g2.setFont(GameWindow.Pixelart.deriveFont(36f));
        g2.drawString("ESC - VOLVER AL MENU", 100, 950);
    }


}
