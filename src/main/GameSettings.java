package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class GameSettings extends JPanel implements KeyListener {
    private GameWindow gameWindow;

    private int contentOffsetY = 0;
    private int scrollSpeed = 20;

    // Sección activa: "teclas", "pantalla", "sonido"
    private String seccionActiva = "teclas";

    public static Font Pixelart;

    // Botones
    private JButton btnTeclas, btnPantalla, btnSonido;
    
    private int opcionSeleccionada = 0;
    
    // Estado de edición
    private boolean esperandoTecla = false;
    private String teclaEditando = "";
    
    // Configuraciones editables
    // Teclas
    private String teclaArriba = "W";
    private String teclaAbajo = "S";
    private String teclaIzquierda = "A";
    private String teclaDerecha = "D";
    private String teclaInteractuar = "E";
    private String teclaPausa = "ESC";
    private String teclaAdelantarTexto = "ENTER";
    
    // Pantalla
    private String[] resoluciones = {"1280x720", "1920x1080", "2560x1440", "3840x2160"};
    private int resolucionActual = 1;
    private boolean pantallaCompleta = true;
    private boolean vsync = false;
    
    // Sonido
    private int volumenGeneral = 100;
    private boolean musicaActivada = true;
    private boolean efectosActivados = true;

    public GameSettings(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();
        setLayout(null);
        
        // Añadir KeyListener
        addKeyListener(this);

        // Key binding ESC
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ESCAPE"), "backToMenu");
        getActionMap().put("backToMenu", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!esperandoTecla) {
                    gameWindow.showMainMenu();
                }
            }
        });
        
     // Key binding para cambiar sección a la izquierda (Q)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed Q"), "seccionAnterior");
        getActionMap().put("seccionAnterior", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!esperandoTecla) {
                    cambiarSeccion(-1);
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                }
            }
        });

        // Key binding para cambiar sección a la derecha (E)
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("pressed E"), "seccionSiguiente");
        getActionMap().put("seccionSiguiente", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!esperandoTecla && !seccionActiva.equals("teclas")) {
                    cambiarSeccion(1);
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                }
            }
        });


        // Key binding para mover selección hacia arriba
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("UP"), "seleccionArriba");
        getActionMap().put("seleccionArriba", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!esperandoTecla) {
                    int maxIndex = getOptionsArray().length - 1;
                    opcionSeleccionada = (opcionSeleccionada <= 0) ? maxIndex : opcionSeleccionada - 1;
                    repaint();
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                }
            }
        });

        // Key binding para mover selección hacia abajo
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("DOWN"), "seleccionAbajo");
        getActionMap().put("seleccionAbajo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!esperandoTecla) {
                    int maxIndex = getOptionsArray().length - 1;
                    opcionSeleccionada = (opcionSeleccionada >= maxIndex) ? 0 : opcionSeleccionada + 1;
                    repaint();
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                }
            }
        });
        
        // Key binding para LEFT
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("LEFT"), "modificarIzquierda");
        getActionMap().put("modificarIzquierda", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!esperandoTecla) {
                    modificarOpcion(-1);
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                }
            }
        });
        
        // Key binding para RIGHT
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("RIGHT"), "modificarDerecha");
        getActionMap().put("modificarDerecha", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!esperandoTecla) {
                    modificarOpcion(1);
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                }
            }
        });
        
        // Key binding para ENTER
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("ENTER"), "editarTecla");
        getActionMap().put("editarTecla", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (seccionActiva.equals("teclas") && opcionSeleccionada < 7) {
                    esperandoTecla = true;
                    teclaEditando = getTeclaEditandoNombre();
                    GameWindow.reproducirSonido("resources/sounds/interact.wav");
                    repaint();
                }
            }
        });
        

        // Crear botones
        btnTeclas = crearBoton("Teclas", e -> {
            seccionActiva = "teclas";
            opcionSeleccionada = 0;
            esperandoTecla = false;
            repaint();
        });

        btnPantalla = crearBoton("Pantalla", e -> {
            seccionActiva = "pantalla";
            opcionSeleccionada = 0;
            esperandoTecla = false;
            repaint();
        });

        btnSonido = crearBoton("Sonido", e -> {
            seccionActiva = "sonido";
            opcionSeleccionada = 0;
            esperandoTecla = false;
            repaint();
        });

        add(btnTeclas);
        add(btnPantalla);
        add(btnSonido);
    }
    
    private String getTeclaEditandoNombre() {
        switch (opcionSeleccionada) {
            case 0: return "ARRIBA";
            case 1: return "ABAJO";
            case 2: return "IZQUIERDA";
            case 3: return "DERECHA";
            case 4: return "INTERACTUAR";
            case 5: return "PAUSA";
            case 6: return "ADELANTAR TEXTO";
            default: return "";
        }
    }
    
    private void modificarOpcion(int direccion) {
        switch (seccionActiva) {
            case "pantalla":
                switch (opcionSeleccionada) {
                    case 0: // Resolución
                        resolucionActual += direccion;
                        if (resolucionActual < 0) resolucionActual = resoluciones.length - 1;
                        if (resolucionActual >= resoluciones.length) resolucionActual = 0;
                        break;
                    case 1: // Pantalla completa
                        pantallaCompleta = !pantallaCompleta;
                        break;
                    case 2: // VSync
                        vsync = !vsync;
                        break;
                }
                break;
            case "sonido":
                switch (opcionSeleccionada) {
                    case 0: // Volumen
                        volumenGeneral += direccion * 10;
                        if (volumenGeneral < 0) volumenGeneral = 0;
                        if (volumenGeneral > 100) volumenGeneral = 100;
                        break;
                    case 1: // Música
                        musicaActivada = !musicaActivada;
                        break;
                    case 2: // Efectos
                        efectosActivados = !efectosActivados;
                        break;
                }
                break;
        }
        repaint();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (esperandoTecla) {
            String nuevaTecla = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
            
            // Asignar la nueva tecla según la opción seleccionada
            switch (opcionSeleccionada) {
                case 0: teclaArriba = nuevaTecla; break;
                case 1: teclaAbajo = nuevaTecla; break;
                case 2: teclaIzquierda = nuevaTecla; break;
                case 3: teclaDerecha = nuevaTecla; break;
                case 4: teclaInteractuar = nuevaTecla; break;
                case 5: teclaPausa = nuevaTecla; break;
                case 6: teclaAdelantarTexto = nuevaTecla; break;
            }
            
            esperandoTecla = false;
            teclaEditando = "";
            GameWindow.reproducirSonido("resources/sounds/interact.wav");
            repaint();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}


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
                    "ARRIBA: " + teclaArriba,
                    "ABAJO: " + teclaAbajo,
                    "IZQUIERDA: " + teclaIzquierda,
                    "DERECHA: " + teclaDerecha,
                    "INTERACTUAR: " + teclaInteractuar,
                    "PAUSA: " + teclaPausa,
                    "ADELANTAR TEXTO: " + teclaAdelantarTexto
                };
            case "pantalla":
                return new String[] {
                    "Resolución: " + resoluciones[resolucionActual] + " < >",
                    "Pantalla completa: " + (pantallaCompleta ? "Activado" : "Desactivado") + " < >",
                    "Sincronización vertical: " + (vsync ? "Activada" : "Desactivada") + " < >"
                };
            case "sonido":
                return new String[] {
                    "Volumen general: " + volumenGeneral + "% < >",
                    "Música: " + (musicaActivada ? "Activada" : "Desactivada") + " < >",
                    "Efectos: " + (efectosActivados ? "Activados" : "Desactivados") + " < >"
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
            
            // Mostrar indicador de edición
            if (i == opcionSeleccionada && esperandoTecla) {
                texto = teclaEditando + ": [Presiona una tecla...]";
                g2.setColor(Color.GREEN);
            } else if (i == opcionSeleccionada) {
                texto = "> " + texto + " <";
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
        String footer;
        if (seccionActiva.equals("teclas")) {
            footer = "ENTER - CAMBIAR TECLA | ESC - VOLVER";
        } else {
            footer = "← → - MODIFICAR | ESC - VOLVER";
        }
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
