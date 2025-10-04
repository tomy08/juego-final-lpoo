package main;
import javax.swing.*;

import entities.NPC;
import entities.Player;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class GamePanel extends JPanel {
    
    private GameWindow gameWindow;
    private Player player;
    private ArrayList<NPC> NPCs = new ArrayList<>();
    private Set<Integer> pressedKeys;
    private CollisionMap collisionMap; // Sistema de colisiones
    
    // Interactuar con NPC
    public boolean interactuando = false;
    private Properties dialogos;
    private String nombreNPC;
    private NPC currentNPC;
    private int currentLine;
   
    // Opciones de NPC
    private boolean eligiendoOpcion = false;
    private String[] opciones = null;
    private int opcionSeleccionada = 0;
    
    // Speak del NPC
    private String textoCompleto = "";
    private String textoActual = "";
    private long lastCharTime = 0;
    private int charDelay = 25;

    
    // Seguimiento de Camara
    public double CameraX = 0;
    public double CameraY = 0;
    
    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        
        pressedKeys = new HashSet<>();
        
        // Inicializar jugador en el centro de la pantalla
        player = new Player(GW.SX(400), GW.SY(400), this);
        NPCs.add(new NPC(GW.SX(200), GW.SY(200), GW.SX(40), "Mauro", this));
        NPCs.add(new NPC(GW.SX(500), GW.SY(200), GW.SX(40), "random", this));
        
        // Cargar el mapa de colisiones
        collisionMap = new CollisionMap("resources/collision_map.png");
        player.setCollisionMap(collisionMap);
        
        // Cargar Dialogos de los NPC
        dialogos = new Properties();
        try (InputStream input = getClass().getResourceAsStream("dialogos.properties")) {
            dialogos.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibujar mapa de colisiones de fondo
        if (collisionMap != null && collisionMap.isLoaded()) {
            // Dibujar la imagen del mapa ajustada a la cámara
            g2d.drawImage(collisionMap.getImage(), 
                         -(int)CameraX, 
                         -(int)CameraY, 
                         null);
        }
        
        // Dibujar jugador
        player.draw(g2d);
        
        // Dibujar NPCs
        for(NPC npc : NPCs) {
             npc.drawNPC(g2d);
             npc.drawInteractive(g2d);
        }
        // Dibujar UI
        drawUI(g2d);
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        Font font = new Font("Arial", Font.PLAIN, GW.SY(16));
        g2d.setFont(font);
        g2d.drawString("WASD o flechas para mover", GW.SX(10), GW.SY(25));
        g2d.drawString("ESC para volver al menú", GW.SX(10), GW.SY(45));
        
        // Mostrar posición del jugador (para debug)
        g2d.setColor(Color.YELLOW);
        g2d.drawString("Posición: (" + (int)player.getX() + ", " + (int)player.getY() + ")", GW.SX(10), getHeight() - GW.SY(20));
        
        // Texto interactuar con NPC
        if(interactuando) {
            g2d.setColor(new Color(0,0,0,100));
            g2d.fillRect(GW.SX(320), GW.SY(650), GW.SX(1280), GW.SY(300));
            
            // Dibujar borde
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(GW.SX(4)));
            g2d.drawRect(GW.SX(320), GW.SY(650), GW.SX(1280), GW.SY(300));
            
            // Escribir Nombre
            g2d.setFont(GameWindow.Pixelart.deriveFont(40f));
            g2d.drawString(nombreNPC, GW.SX(340), GW.SY(700));
            
            // Escribir Texto
            g2d.setFont(GameWindow.Pixelart.deriveFont(30f));
            g2d.drawString(textoActual, GW.SX(340), GW.SY(750));
            
            // Dibujar Opciones
            g2d.setFont(GameWindow.Pixelart.deriveFont(36f));
            if (eligiendoOpcion) {
                for (int i = 0; i < opciones.length; i++) {
                    int x = GW.SX(480) + i * GW.SX(384);
                    int y = GW.SY(900);
                    if (i == opcionSeleccionada) {
                        g2d.setColor(Color.YELLOW);
                        g2d.drawString(opciones[i] + " <", x, y);
                        continue;
                    }
                    else g2d.setColor(Color.WHITE);

                    g2d.drawString(opciones[i], x, y);
                }
            }
        }
    }
    
    public void update() {
        // Interactuar con NPCs
        if (interactuando && textoActual.length() < textoCompleto.length()) {
            long now = System.currentTimeMillis();
            if (now - lastCharTime >= charDelay) {
                textoActual += textoCompleto.charAt(textoActual.length());
                GameWindow.reproducirSonido("resources/sounds/tipeo.wav");
                lastCharTime = now;
            }
        }
        
        // Actualizar movimiento del jugador basado en teclas presionadas
        boolean moving = false;
        double deltaX = 0, deltaY = 0;
        
        if(!interactuando) {
            if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) {
                deltaY = -1;
                moving = true;
            }
            if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) {
                deltaY = 1;
                moving = true;
            }
            if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) {
                deltaX = -1;
                moving = true;
            }
            if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) {
                deltaX = 1;
                moving = true;
            }
        
            // Normalizar movimiento diagonal
            if (deltaX != 0 && deltaY != 0) {
                deltaX *= 0.707; // 1/sqrt(2) para mantener velocidad constante
                deltaY *= 0.707;
            }
        }
        
        // Interactuar con NPCs
        for (NPC npc : NPCs) {
            // Entrar al área para interactuar
            if (player.getBounds().intersects(npc.getArea())) {
                npc.interactive = true;
            } else {
                npc.interactive = false;
            }
            
            // Colisión con el NPC
            if (player.getBounds().intersects(npc.getBounds())) {
                Rectangle p = player.getBounds();
                Rectangle n = npc.getBounds();

                int overlapLeft   = p.x + p.width - n.x;
                int overlapRight  = n.x + n.width - p.x;
                int overlapTop    = p.y + p.height - n.y;
                int overlapBottom = n.y + n.height - p.y;

                int minOverlapX = Math.min(overlapLeft, overlapRight);
                int minOverlapY = Math.min(overlapTop, overlapBottom);

                if (minOverlapX < minOverlapY) {
                    if (overlapLeft < overlapRight) {
                        player.setX(player.getX() - minOverlapX);
                    } else {
                        player.setX(player.getX() + minOverlapX);
                    }
                } else {
                    if (overlapTop < overlapBottom) {
                        player.setY(player.getY() - minOverlapY);
                    } else {
                        player.setY(player.getY() + minOverlapY);
                    }
                }

                moving = false;
            }
        }

        if (moving) {
            player.move(deltaX, deltaY, getWidth(), getHeight());
        }
        player.update();
    }
    
    public void handleKeyPress(int keyCode) {
        pressedKeys.add(keyCode);
        
        if (keyCode == KeyEvent.VK_ESCAPE) {
            gameWindow.backToMenu();
        }
        
        if (keyCode == KeyEvent.VK_E) {
            for (NPC npc : NPCs) {
                if (npc.interactive) {
                    interactNPC(npc);
                    break;
                }
            }
        }
        
        // Seleccionar opciones al interactuar
        if (interactuando) {
            if (eligiendoOpcion) {
                if (keyCode == KeyEvent.VK_LEFT) opcionSeleccionada = Math.max(0, opcionSeleccionada - 1);
                if (keyCode == KeyEvent.VK_RIGHT) opcionSeleccionada = Math.min(opciones.length - 1, opcionSeleccionada + 1);

                if (keyCode == KeyEvent.VK_ENTER) {
                    procesarOpcion(opciones[opcionSeleccionada], currentNPC);
                }
                return;
            }

            if (keyCode == KeyEvent.VK_ENTER) {
                if (textoActual.length() < textoCompleto.length()) {
                    textoActual = textoCompleto;
                } else {
                    currentLine++;
                    loadCurrentLine(currentNPC);
                }
                return;
            }
        }
    }
    
    public void handleKeyRelease(int keyCode) {
        pressedKeys.remove(keyCode);
    }
    
    // Interactuar con los NPC
    public void interactNPC(NPC npc) {
        currentNPC = npc;
        interactuando = true;
        GameWindow.reproducirSonido("resources/sounds/interact.wav");

        currentNPC = npc;
        currentLine = npc.npcLine();
        nombreNPC = npc.Tipo;

        loadCurrentLine(npc);
    }
    
    private void loadCurrentLine(NPC npc) {
        if (currentNPC == null) {
            interactuando = false;
            return;
        }

        String key = currentNPC.Tipo + "." + currentLine;
        String raw = dialogos.getProperty(key);

        if (raw == null) {
            interactuando = false;
            eligiendoOpcion = false;
            opciones = null;
            currentNPC = null;
            return;
        }
        
        if(currentLine == npc.npcFinalLine() + 1) {
        	interactuando = false;
            eligiendoOpcion = false;
            opciones = null;
            currentNPC = null;
            return;
        }

        if (raw.contains("|")) {
            String[] partes = raw.split("\\|");
            textoCompleto = partes[0];
            opciones = partes[1].split(":");
            eligiendoOpcion = true;
            opcionSeleccionada = 0;
        } else {
            textoCompleto = raw;
            opciones = null;
            eligiendoOpcion = false;
        }

        textoActual = "";
        lastCharTime = System.currentTimeMillis();

        repaint();
    }
    
    public void triggerNPC(String targetTipo) {
        for (NPC npc : NPCs) {
            if (npc.Tipo.equals(targetTipo)) {
                npc.Trigger = true;
                return;
            }
        }
    }
    
    private void procesarOpcion(String opcion, NPC npc) {
        if(npc.Tipo.equals("random")) {
            if (opcion.equals("SI")) {
                triggerNPC("Mauro");
                triggerNPC("random");
            }
            if (opcion.equals("NO")) System.out.println("Usuario dijo que no");
        }
        
        eligiendoOpcion = false;
        opciones = null;
        currentLine++;
        loadCurrentLine(currentNPC);
    }

}
