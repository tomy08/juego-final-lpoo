package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class GameSettings extends JPanel {
    private GameWindow gameWindow;

    private int contentOffsetY = 0;
    private int scrollSpeed = 20;

    // Sección activa: "teclas", "pantalla", "sonido"
    private String seccionActiva = "teclas";

    public static Font Pixelart;

    // Botones
    private JButton btnTeclas, btnPantalla, btnSonido;
    
    private int opcionSeleccionada = 0;

    public GameSettings(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
        setLayout(null);

        // Key binding ESC
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "backToMenu");
        getActionMap().put("backToMenu", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                gameWindow.showMainMenu();
            }
        });
        
     // Key binding para cambiar sección a la izquierda (Q)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed Q"), "seccionAnterior");
        getActionMap().put("seccionAnterior", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cambiarSeccion(-1);
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
            }
        });

        // Key binding para cambiar sección a la derecha (E)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed E"), "seccionSiguiente");
        getActionMap().put("seccionSiguiente", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cambiarSeccion(1);
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
            }
        });


        // Key binding para mover selección hacia arriba
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("UP"), "seleccionArriba");
        getActionMap().put("seleccionArriba", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int maxIndex = getOptionsArray().length - 1;
                opcionSeleccionada = (opcionSeleccionada <= 0) ? maxIndex : opcionSeleccionada - 1;
                repaint();
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
            }
        });

        // Key binding para mover selección hacia abajo
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("DOWN"), "seleccionAbajo");
        getActionMap().put("seleccionAbajo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int maxIndex = getOptionsArray().length - 1;
                opcionSeleccionada = (opcionSeleccionada >= maxIndex) ? 0 : opcionSeleccionada + 1;
                repaint();
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
            }
        });
        
        

        // Crear botones
        btnTeclas = crearBoton("Teclas", e -> {
            seccionActiva = "teclas";
            opcionSeleccionada = 0;
            repaint();
        });

        btnPantalla = crearBoton("Pantalla", e -> {
            seccionActiva = "pantalla";
            opcionSeleccionada = 0;
            repaint();
        });

        btnSonido = crearBoton("Sonido", e -> {
            seccionActiva = "sonido";
            opcionSeleccionada = 0;
            repaint();
        });

        add(btnTeclas);
        add(btnPantalla);
        add(btnSonido);
    }

    private JButton crearBoton(String texto, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(texto);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(GameWindow.Pixelart.deriveFont(36f));
        btn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        btn.addActionListener(listener);
        return btn;
    }

    private String[] getOptionsArray() {
        switch (seccionActiva) {
            case "teclas":
                return new String[] {
                    "W - ARRIBA",
                    "S - ABAJO",
                    "A - IZQUIERDA",
                    "D - DERECHA",
                    "E - INTERACTUAR",
                    "ESC - PAUSA",
                    "ENTER - ADELANTAR TEXTO"
                };
            case "pantalla":
                return new String[] {
                    "Resolución: 1920x1080",
                    "Pantalla completa: Activado",
                    "Sincronización vertical: Desactivada"
                };
            case "sonido":
                return new String[] {
                    "Volumen general: 100%",
                    "Música: Activada",
                    "Efectos: Activados"
                };
            default:
                return new String[]{};
        }
    }
    
    private void cambiarSeccion(int direccion) {
        String[] secciones = {"teclas", "pantalla", "sonido"};
        int index = 0;
        for (int i = 0; i < secciones.length; i++) {
            if (seccionActiva.equals(secciones[i])) {
                index = i;
                break;
            }
        }
        index += direccion;
        if (index < 0) index = secciones.length - 1;
        if (index >= secciones.length) index = 0;

        seccionActiva = secciones[index];
        opcionSeleccionada = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Actualizar color de los botones según la sección activa
        btnTeclas.setForeground(seccionActiva.equals("teclas") ? Color.YELLOW : Color.WHITE);
        btnPantalla.setForeground(seccionActiva.equals("pantalla") ? Color.YELLOW : Color.WHITE);
        btnSonido.setForeground(seccionActiva.equals("sonido") ? Color.YELLOW : Color.WHITE);

        int width = getWidth();
        int height = getHeight();

        // Posiciones y medidas proporcionales
        int rectWidth = (int)(width * 0.75);
        int rectHeight = (int)(height * 0.6);
        int rectX = (width - rectWidth) / 2;
        int rectY = (height - rectHeight) / 2;

        // Dibujar título
        g2.setFont(GameWindow.Pixelart.deriveFont((float)(height * 0.06)));
        String title = "CONFIGURACIÓN";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.setColor(Color.WHITE);
        g2.drawString(title, (width - titleWidth) / 2, (int)(height * 0.08));

        // Dibujar marco
        g2.setStroke(new java.awt.BasicStroke(5));
        g2.drawRect(rectX, rectY, rectWidth, rectHeight);

        // Clip para scroll interno
        g2.setClip(rectX, rectY, rectWidth, rectHeight);

        int baseY = rectY + (int)(height * 0.05);
        int baseX = rectX + (int)(rectWidth * 0.1);
        int spacing = (int)(height * 0.07);

        // Subtítulo
        g2.setFont(GameWindow.Pixelart.deriveFont((float)(height * 0.045)));
        g2.setColor(Color.WHITE);
        g2.drawString("-  " + seccionActiva.toUpperCase() + "  -", baseX, baseY - contentOffsetY);

        // Contenido según la sección activa
        g2.setFont(GameWindow.Pixelart.deriveFont((float)(height * 0.04)));
        String[] options = getOptionsArray();

        for (int i = 0; i < options.length; i++) {
            int y = baseY + 60 + i * spacing - contentOffsetY;

            String texto = options[i];
            if (i == opcionSeleccionada) {
                texto = texto + " <";  // Agregamos los símbolos
                g2.setColor(Color.YELLOW);
            } else {
                g2.setColor(Color.WHITE);
            }

            g2.drawString(texto, baseX, y);
        }


        // Fin clip
        g2.setClip(null);

        // Footer fijo abajo
        g2.setFont(GameWindow.Pixelart.deriveFont((float)(height * 0.03)));
        String footer = "ESC - VOLVER AL MENU";
        int footerWidth = g2.getFontMetrics().stringWidth(footer);
        g2.setColor(Color.WHITE);
        g2.drawString(footer, (width - footerWidth) / 2, (int)(height * 0.95));

        // Posicionar botones de forma responsive
        int btnWidth = (int)(width * 0.18);
        int btnHeight = (int)(height * 0.07);
        int spacingX = (int)(width * 0.02);
        int totalWidth = btnWidth * 3 + spacingX * 2;
        int startX = (width - totalWidth) / 2;
        int btnY = (int)(height * 0.15);

        btnTeclas.setBounds(startX, btnY, btnWidth, btnHeight);
        btnPantalla.setBounds(startX + btnWidth + spacingX, btnY, btnWidth, btnHeight);
        btnSonido.setBounds(startX + (btnWidth + spacingX) * 2, btnY, btnWidth, btnHeight);
    }
}
