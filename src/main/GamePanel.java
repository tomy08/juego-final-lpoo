package main;
import javax.swing.*;

import Mapa.CollisionMap;
import Sonidos.Musica;
import entities.NPC;
import entities.Player;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class GamePanel extends JPanel implements GameThread.Updatable {
    
    private GameWindow gameWindow;
    private Player player;
    private ArrayList<NPC> NPCs = new ArrayList<>();
    private Set<Integer> pressedKeys;
    private CollisionMap collisionMap; // Sistema de colisiones
    
    // Sistema de Teleport
    private CollisionMap plantaAltaMap;
    private CollisionMap plantaBajaMap;
    private boolean enPlantaAlta = true; // true = PLANTA_ALTA, false = PLANTA_BAJA
    private boolean estaEnZonaTeleport = false;
    private int currentTeleportId = -1; // ID del teleport actual
    
    // Posiciones en el Mapa
    private static int SCALE = 28; // Reducido en 20%
    
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
    
    
    //Menu de pausa
    
    private boolean paused = false;
    private int opcionPausa = 0;
    private String[] opcionesPausa = {"CONTINUAR","SETTINGS", "VOLVER AL MENU"};

    // Inventario
    private boolean inventoryOpen = false;
    
    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        
        Musica.reproducirMusica("resources/Music/Fondo.wav");
        Musica.enableLoop();
        
        pressedKeys = new HashSet<>();
        
        // Inicializar jugador en el centro de la pantalla
        player = new Player(130 * SCALE, 130 * SCALE, this);
        NPCs.add(new NPC(129 * SCALE, 135 * SCALE, GW.SX(40), "Mauro", this));
        NPCs.add(new NPC(125 * SCALE, 140 * SCALE, GW.SX(40), "random", this));
        
        // Cargar ambos mapas de colisiones
        plantaAltaMap = new CollisionMap("resources/Collision_Maps/PLANTA_ALTA.png");
        plantaBajaMap = new CollisionMap("resources/Collision_Maps/PLANTA_BAJA.png");
        
        // Establecer mapa inicial
        collisionMap = plantaAltaMap;
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
        

        Image img = collisionMap.getImage();
        if (img != null) {
            int newW = img.getWidth(null) * SCALE;
            int newH = img.getHeight(null) * SCALE;
            
            g2d.drawImage(img,
                -(int)CameraX, -(int)CameraY,
                newW, newH,
                null);
        }

        
        // Dibujar jugador
        player.draw(g2d);
        int drawX = (int)player.getX() - (int)CameraX;
        int drawY = (int)player.getY() - (int)CameraY - player.getSize();
        int drawW = player.getSize();
        int drawH = player.getSize() * 2;
        
        if (player.facingLeft) {
            g2d.drawImage(player.image, 
                          drawX + drawW, drawY, 
                          -drawW, drawH, 
                          this);
        } else {
            g2d.drawImage(player.image, 
                          drawX, drawY, 
                          drawW, drawH, 
                          this);
        }
        
        // Dibujar NPCs
        for(NPC npc : NPCs) {
             npc.drawNPC(g2d);
             npc.drawInteractive(g2d, GameSettings.teclaInteractuar);
        }
        // Dibujar UI
        drawUI(g2d);
        // Dibujar hotbar (si el jugador tiene uno)
        if (player != null && player.inventory != null) {
            player.inventory.drawHotbar(g2d, getWidth(), getHeight(), GW.SX(48), (int)CameraX, (int)CameraY);
            if (inventoryOpen) {
                player.inventory.drawFullInventory(g2d, getWidth(), getHeight());
            }
        }
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
        
        // Mostrar mapa actual
        g2d.setColor(Color.CYAN);
        String mapaActual = enPlantaAlta ? "PLANTA ALTA" : "PLANTA BAJA";
        g2d.drawString("Mapa: " + mapaActual, GW.SX(10), getHeight() - GW.SY(40));
        
        // Mostrar ID de teleport (debug)
        if (estaEnZonaTeleport && currentTeleportId != -1) {
            g2d.setColor(Color.MAGENTA);
            g2d.drawString("Teleport ID: " + currentTeleportId, GW.SX(10), getHeight() - GW.SY(60));
        }
        
        // Indicador de teleport disponible
        if (estaEnZonaTeleport && !interactuando) {
            g2d.setColor(new Color(255, 50, 50, 200));
            g2d.fillRect(getWidth() / 2 - GW.SX(150), GW.SY(100), GW.SX(300), GW.SY(60));
            
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(GW.SX(3)));
            g2d.drawRect(getWidth() / 2 - GW.SX(150), GW.SY(100), GW.SX(300), GW.SY(60));
            
            g2d.setFont(new Font("Arial", Font.BOLD, GW.SY(24)));
            g2d.drawString("Presiona E para teleport", getWidth() / 2 - GW.SX(135), GW.SY(140));
        }
        
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
            
            // Texto Ayuda
            g2d.setFont(GameWindow.Pixelart.deriveFont(25f));
            g2d.drawString(GameSettings.teclaAdelantarTexto + " >>", GW.SX(1350), GW.SY(925));
            
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
        
        if (paused) {
            drawPauseMenu(g2d);
        }

    }
    
    public void update() {
    	
    	if (paused) return; // No actualiza nada si está pausado

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
                if (pressedKeys.contains(GameSettings.KEY_UP)) {
                    deltaY = -1;
                    moving = true;
                }
                if (pressedKeys.contains(GameSettings.KEY_DOWN)) {
                    deltaY = 1;
                    moving = true;
                }
                if (pressedKeys.contains(GameSettings.KEY_LEFT)) {
                    deltaX = -1;
                    moving = true;
                }
                if (pressedKeys.contains(GameSettings.KEY_RIGHT)) {
                    deltaX = 1;
                    moving = true;
                }

                if (deltaX != 0 && deltaY != 0) {
                    deltaX *= 0.707;
                    deltaY *= 0.707;
                }
                
                if (moving) {
                    if (deltaY < 0) { // Prioridad Arriba
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-up.png").getImage();
                        player.facingLeft = false; // Resetear reflejo si se movía horizontalmente
                    } else if (deltaY > 0) { // Prioridad Abajo
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-down.png").getImage();
                        player.facingLeft = false;
                    } else if (deltaX < 0) { // Izquierda
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-side.png").getImage();
                        player.facingLeft = true; // Establecer para reflejar
                    } else if (deltaX > 0) { // Derecha
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-side.png").getImage();
                        player.facingLeft = false; // No reflejar
                    }
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
        
        // Verificar si el jugador está en zona de teleport
        estaEnZonaTeleport = collisionMap.isTeleportZoneRect(
            (int)player.getX(), 
            (int)player.getY(), 
            player.getSize(), 
            player.getSize()
        );
        
        // Obtener el ID del teleport si está en zona de teleport
        if (estaEnZonaTeleport) {
            currentTeleportId = collisionMap.getTeleportIdRect(
                (int)player.getX(), 
                (int)player.getY(), 
                player.getSize(), 
                player.getSize()
            );
        } else {
            currentTeleportId = -1;
        }
    }
    
    public void handleKeyPress(int keyCode) {
        pressedKeys.add(keyCode);
        
        
     
        if (keyCode == KeyEvent.VK_ESCAPE) {
            paused = !paused;
            repaint();
            return; // evita ejecutar otras acciones cuando estás pausado
        }
        
     // Controles del menu de pausa
        if (paused) {
            if (keyCode == KeyEvent.VK_UP) {
                opcionPausa = (opcionPausa - 1 + opcionesPausa.length) % opcionesPausa.length;
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
                repaint();
            } else if (keyCode == KeyEvent.VK_DOWN) {
                opcionPausa = (opcionPausa + 1) % opcionesPausa.length;
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
                repaint();
            } else if (keyCode == KeyEvent.VK_ENTER) {
                if (opcionPausa == 0) { // Continuar
                    paused = false;
                } else if (opcionPausa == 1) { // Ir al menú de configuración
                    gameWindow.settingsGame();
                }

                else if (opcionPausa == 2) { // Volver al menú
                    gameWindow.backToMenu();
                }
                GameWindow.reproducirSonido("resources/sounds/confirm.wav");
                repaint();
            }
            return; // Evita que se siga ejecutando lógica normal
        }


        if (keyCode == GameSettings.KEY_MENU) {
            gameWindow.backToMenu();
        }

        // Tecla para abrir/cerrar inventario
        if (keyCode == GameSettings.KEY_INVENTORY) {
            inventoryOpen = !inventoryOpen;
            GameWindow.reproducirSonido("resources/sounds/menu.wav");
            repaint();
            return;
        }

        // Selección de hotbar con teclas 1-9
        if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
            int num = keyCode - KeyEvent.VK_1; // 0-based
            if (player != null && player.inventory != null) {
                player.inventory.setSelectedHotbar(num);
                GameWindow.reproducirSonido("resources/sounds/menu.wav");
                repaint();
            }
        }

        // Tecla E para teleport
        if (keyCode == KeyEvent.VK_E && estaEnZonaTeleport && !interactuando) {
            realizarTeleport();
        }

        if (keyCode == GameSettings.KEY_INTERACT) {
            for (NPC npc : NPCs) {
                if (npc.interactive) {
                    interactNPC(npc);
                    break;
                }
            }
        }
        
        if(interactuando && eligiendoOpcion) {
        	switch(keyCode) {
        		case KeyEvent.VK_LEFT:
        			opcionSeleccionada = (opcionSeleccionada - 1 + opciones.length) % opciones.length;
        			GameWindow.reproducirSonido("resources/sounds/menu.wav");
        			repaint();
        			break;
        		case KeyEvent.VK_RIGHT:
        			opcionSeleccionada = (opcionSeleccionada + 1) % opciones.length;
        			GameWindow.reproducirSonido("resources/sounds/menu.wav");
        			repaint();
        			break;
        	}
        }

        // Avanzar texto o confirmar opciones
        if (interactuando && keyCode == GameSettings.KEY_CONFIRM) {
            if (eligiendoOpcion && textoCompleto.equals(textoActual)) {
                if (opcionSeleccionada >= 0 && opcionSeleccionada < opciones.length) {
                    procesarOpcion(opciones[opcionSeleccionada], currentNPC);
                }
            } else {
                if (textoActual.length() < textoCompleto.length()) {
                    textoActual = textoCompleto;
                } else {
                    currentLine++;
                    loadCurrentLine(currentNPC);
                }
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
                gameWindow.startRitmo("Ricky", 13, 170);
            }
            if (opcion.equals("NO")) System.out.println("Usuario dijo que no");
        }
        
        eligiendoOpcion = false;
        opciones = null;
        currentLine++;
        loadCurrentLine(currentNPC);
    }
    
    /**
     * Realiza el teleport entre PLANTA_ALTA y PLANTA_BAJA
     */
    private void realizarTeleport() {
        if (currentTeleportId == -1) {
            System.err.println("⚠ No se puede teleportar: ID de teleport inválido");
            return;
        }
        
        // Reproducir sonido de teleport
        GameWindow.reproducirSonido("resources/sounds/menu.wav");
        
        // Cambiar de mapa
        enPlantaAlta = !enPlantaAlta;
        
        // Actualizar el mapa de colisiones
        collisionMap = enPlantaAlta ? plantaAltaMap : plantaBajaMap;
        player.setCollisionMap(collisionMap);
        
        // Buscar la posición de destino en el nuevo mapa con el mismo ID
        Point destino = collisionMap.findTeleportDestination(currentTeleportId);
        
        if (destino != null) {
            player.setX(destino.x);
            player.setY(destino.y);
            System.out.println("✓ Teleport realizado (ID: " + currentTeleportId + ") a: " + destino.x + ", " + destino.y);
        } else {
            System.err.println("⚠ No se encontró destino de teleport con ID " + currentTeleportId + " en el nuevo mapa");
            // Revertir el cambio de mapa si no hay destino
            enPlantaAlta = !enPlantaAlta;
            collisionMap = enPlantaAlta ? plantaAltaMap : plantaBajaMap;
            player.setCollisionMap(collisionMap);
        }
        
        // Actualizar la cámara
        player.update();
        
        // Forzar repaint
        repaint();
    }
    
    private void drawPauseMenu(Graphics2D g2d) {
        // Fondo semitransparente oscuro
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Título
        g2d.setFont(GameWindow.Pixelart.deriveFont(90f));
        g2d.setColor(Color.WHITE);
        String titulo = "PAUSA";
        FontMetrics fmTitulo = g2d.getFontMetrics();
        int xTitulo = (getWidth() - fmTitulo.stringWidth(titulo)) / 2;
        int yTitulo = getHeight() / 2 - 250;
        g2d.drawString(titulo, xTitulo, yTitulo);

        // Opciones
        g2d.setFont(GameWindow.Pixelart.deriveFont(55f));
        FontMetrics fmOpciones = g2d.getFontMetrics();

        int espacioEntreOpciones = 90;
        int yInicial = getHeight() / 2 - 50;

        for (int i = 0; i < opcionesPausa.length; i++) {
            String opcion = opcionesPausa[i];
            int x = (getWidth() - fmOpciones.stringWidth(opcion)) / 2;
            int y = yInicial + i * espacioEntreOpciones;

            if (i == opcionPausa) {
                g2d.setColor(Color.YELLOW);
                g2d.drawString("> " + opcion  + " <", x - 50, y); // Agrega el "<" a la derecha
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString(opcion, x, y);
            }
        }
    }

    // Mostrar mensaje al ganar
    public void ShowWinMessage(String message) {
        currentNPC = null;
        interactuando = true;
        eligiendoOpcion = false;
        opciones = null;
        textoCompleto = message;     
        textoActual = "";
        currentLine = -1;
        nombreNPC = "";
        lastCharTime = System.currentTimeMillis() + 250;

        repaint();
    }


}
