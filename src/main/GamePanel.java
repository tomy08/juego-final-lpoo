package main;
import javax.swing.*;

import Levels.LevelPanel;
import Mapa.CollisionMap;
import Sonidos.Musica;
import entities.NPC;
import entities.Player;
import entities.NPCManager;
import entities.Item;
import entities.Inventory;
import entities.ItemStack;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class GamePanel extends JPanel implements GameThread.Updatable {
    
    private GameWindow gameWindow;
    public Player player;
    private ArrayList<NPC> NPCs = new ArrayList<>();
    private Set<Integer> pressedKeys;
    private CollisionMap collisionMap; // Sistema de colisiones
    private Image[] fondo = {
    		new ImageIcon("resources/Sprites/fondos/FONDOARRIBA.png").getImage(),
    		new ImageIcon("resources/Sprites/fondos/FONDOABAJO.png").getImage()
    };
    
    // Sistema de Teleport
    private CollisionMap plantaAltaMap;
    private CollisionMap plantaBajaMap;
    public boolean enPlantaAlta = true; // true = PLANTA_ALTA, false = PLANTA_BAJA
    private boolean estaEnZonaTeleport = false;
    private int currentTeleportId = -1; // ID del teleport actual
    
    // Posiciones en el Mapa
    public static int SCALE = GW.SX(34);
    
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
    
    // Final bueno
    
    public boolean martinBien = false;
    public boolean gennusoBien = true;
    
    // Zonas para desbloquear:
    
    public boolean taller = true;
    public boolean ascensorTaller = true;

    // Tienda
    
    public int monedas = 0;
    private boolean EnTienda = false;
    private String[] itemsCantina = {
    		"Pancho",
    		"Jugo Placer",
    		"Cachafaz"
    };
    private Image[] imagenesCantina = {
    		new ImageIcon("resources/Sprites/items/pancho.png").getImage(),
    		new ImageIcon("resources/Sprites/items/jugo placer.png").getImage(),
    		new ImageIcon("resources/Sprites/items/cachafaz.png").getImage()
    };
    private int[] precioCantina = {
    	18000,
    	25000,
    	300000,
    };
    private int[] StockItem = {
    		3,
    		3,
    		1
    };
    private String[] infoItem = {
    	"Más vida máxima",
    	"Multiplicador de $",
    	"???",
    };
    private int OpcionTienda = 0;
    private String textoTienda = "";
    private Color tiendaColor;
    
    
    // Seguimiento de Camara
    
    public double CameraX = 0;
    public double CameraY = 0;
    
    
    //Menu de pausa
    
    private boolean paused = false;
    private int opcionPausa = 0;
    private String[] opcionesPausa = {"CONTINUAR", "GUARDAR", "CONFIGURACION", "GUARDAR Y SALIR"};

    // Inventario
    private boolean inventoryOpen = false;
    
    // Render
    private final CopyOnWriteArrayList<Object> renderList = new CopyOnWriteArrayList<>();

    // Musica
    public boolean musicaParada = false;
    
    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.BLACK);
        setFocusable(true);
        
        Musica.reproducirMusica("resources/Music/Fondo.wav");
        Musica.enableLoop();
        
        // Cargar ambos mapas de colisiones
        plantaAltaMap = new CollisionMap("resources/Collision_Maps/PLANTAALTA.png");
        plantaBajaMap = new CollisionMap("resources/Collision_Maps/PLANTABAJA.png");
        
        // Cargar teclas
        pressedKeys = new HashSet<>();
        
        // Inicializar jugador
        player = new Player(285 * SCALE, 55 * SCALE, this);
        
        // Precargar ambos mapas
        CargarZona(1);
        CargarZona(0);
        
        //test
        givePlayerItem("llave_reja", "Llave de la reja", "llave_Reja.png", 1, 1);
        givePlayerItem("llave_sum", "Llave de la reja", "llave_Reja.png", 1, 1);
        
        // Cargar Dialogos de los NPC
        dialogos = new Properties();
        InputStream input = getClass().getResourceAsStream("dialogos.properties");
        try {
            if (input == null) {
                // Intentar fallback a archivo en disco (modo desarrollo)
                File f = new File("src/main/dialogos.properties");
                if (f.exists()) {
                    input = new FileInputStream(f);
                }
            }
            if (input != null) {
                try (InputStream in = input) {
                    dialogos.load(in);
                }
            } else {
                System.err.println("Aviso: dialogos.properties no encontrado en classpath ni en src/main/");
            }
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
        
        
        if(enPlantaAlta) {
        	img = fondo[0];
        } else {
        	img = fondo[1];
        }
        
        // Dibujar fondo con diseños
        int newW = (int) (img.getWidth(null) * SCALE / 3.3);
        int newH = (int) (img.getHeight(null) * SCALE / 3.3);
        
        g2d.drawImage(img,
            -(int)CameraX, -(int)CameraY,
            newW, newH,
            null);

        
        
        
        // Dibujar NPCs
        
        if(renderList.size() > 0) {
        	 for (Object o : renderList) {
                 if (o instanceof Player p) {
                 	
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
                 }
                 else if (o instanceof NPC n) n.drawNPC(g2d);
             }
        }

        for(NPC npc : NPCs) {
             npc.drawInteractive(g2d, GameSettings.teclaInteractuar);
        }
        
        // Dibujar UI
        drawUI(g2d);
        if (player != null && player.inventory != null) {
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
        g2d.setColor(Color.BLACK);
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
        
        // Abrir la tienda/Cantina
        if(EnTienda) {
    		
    		g2d.setColor(new Color(0,0,0,100));
            g2d.fillRect(GW.SX(520), GW.SY(200), GW.SX(880), GW.SY(700));
            
            // Dibujar borde
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(GW.SX(4)));
            g2d.drawRect(GW.SX(520), GW.SY(200), GW.SX(880), GW.SY(700));
            
            // Escribir Nombre
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(65f)));
            g2d.drawString("Cantina", GW.SX(560), GW.SY(300));
            
            // Ayuda
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(35f)));
            g2d.drawString("ESC - Cerrar", GW.SX(1120), GW.SY(300));
            
            // Plata
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(45f)));
            g2d.drawString(monedas + "$", GW.SX(560), GW.SY(870));
            
            // Texto al Comprar
            g2d.setColor(tiendaColor);
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(45f)));
            g2d.drawString(textoTienda, GW.SX(560), GW.SY(800));
            
            // Imagen del objeto
            g2d.drawImage(imagenesCantina[OpcionTienda], 
            		GW.SX(1150), GW.SY(350), 
                    GW.SX(200), GW.SY(200), 
                    this);
            
            int posY = 500;
            int i = 0;
            for(String itemCantina : itemsCantina) {
            	
            	g2d.setColor(Color.WHITE);
            	g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(45f)));
            	if(StockItem[i] == 0) { // No queda Stock del item
            		g2d.setColor(new Color(120,120,120));
            	}
            	if(i == OpcionTienda) { // Seleccionando un Item
            		if(StockItem[OpcionTienda] > 0) {
            			g2d.setColor(Color.YELLOW);
            		}
            		g2d.drawString(itemCantina + " - " + precioCantina[i] + "$ <", GW.SX(560), GW.SY(posY));
            	} else {
            		g2d.drawString(itemCantina + " - " + precioCantina[i] + "$", GW.SX(560), GW.SY(posY));
            	}
            	posY += 100;
            	i++;
            }
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(35f)));
            g2d.drawString(infoItem[OpcionTienda], GW.SX(1100), GW.SY(600));
    		
    	}
        
        if(interactuando) {
        		
        		g2d.setColor(new Color(0,0,0,100));
                g2d.fillRect(GW.SX(320), GW.SY(650), GW.SX(1280), GW.SY(300));
                
                // Dibujar borde
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(GW.SX(4)));
                g2d.drawRect(GW.SX(320), GW.SY(650), GW.SX(1280), GW.SY(300));
                
                // Escribir Nombre
                g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(55f)));
                g2d.drawString(nombreNPC.toUpperCase(), GW.SX(340), GW.SY(700));
                
                // Escribir Texto
                g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(45f)));
                drawDialogue(g2d, textoActual, GW.SX(340), GW.SY(750), GW.SX(1200)); // 300px es el ancho máximo del cuadro
                
                // Texto Ayuda
                g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(35f)));
                g2d.drawString(GameSettings.teclaAdelantarTexto + " >>", GW.SX(1350), GW.SY(925));
                
                // Dibujar Opciones
                g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(46f)));
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
        
        if(!interactuando && !EnTienda) {
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
                	
                	// Reordenar Lista
                	renderList.sort((a, b) -> {
                	    int yA = 0;
                	    int yB = 0;

                	    if (a instanceof Player p) yA = (int)p.getY();
                	    if (a instanceof NPC n) yA = (int)n.y;

                	    if (b instanceof Player p) yB = (int)p.getY();
                	    if (b instanceof NPC n) yB = (int)n.y;

                	    return Integer.compare(yA, yB);
                	});

                	
                    if (deltaY < 0) { // Prioridad Arriba
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-up.png").getImage();
                        player.facingLeft = false; // Resetear reflejo si se movía horizontalmente
                    } else if (deltaY > 0) { // Prioridad Abajo
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-down.png").getImage();
                        player.facingLeft = false;
                    } else if (deltaX < 0) { // Izquierda
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-side.png").getImage();
                        player.facingLeft = true;
                    } else if (deltaX > 0) { // Derecha
                        player.image = new ImageIcon("resources/Sprites/Jugador/pj-side.png").getImage();
                        player.facingLeft = false;
                    }
                }
        }
        
        // Zonas a desbloquear plantaBaja
        if (!enPlantaAlta) {
        	// Taller
            if (!taller && player.getX() > 168 * SCALE) {  
                player.setX(168 * SCALE);
            }
            
            // Laboratorio
            if (!playerHasItem("llave_laboratorio", 1) && player.getX() < 22 * SCALE && player.getY() > 240 * SCALE) {
            	player.setX(22 * SCALE);
            }
            
            // Ascensor taller
            if (!ascensorTaller && player.getX() >= 219 * SCALE && player.getX() < 225 * SCALE && player.getY() < 204 * SCALE) {
            	player.setY(204 * SCALE);
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
    
    // Desarmar Texto
    
    public ArrayList<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        for (String word : text.split(" ")) {
            String testLine = currentLine + word + " ";
            if (fm.stringWidth(testLine) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word + " ");
            } else {
                currentLine.append(word).append(" ");
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
    
    // Dibujar Texto separado
    public void drawDialogue(Graphics2D g, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        ArrayList<String> lines = wrapText(text, fm, maxWidth);
        int lineHeight = fm.getHeight();

        for (int i = 0; i < lines.size(); i++) {
            g.drawString(lines.get(i), x, y + (i * lineHeight));
        }
    }


    
    public void handleKeyPress(int keyCode) {
        pressedKeys.add(keyCode);
        
        
     
        if (keyCode == GameSettings.KEY_MENU) {
        	if(inventoryOpen) {
        		inventoryOpen = false;
        		repaint();
        		return;
        	}
        	if(EnTienda) {
        		EnTienda = false;
        		repaint();
        		return;
        	}
            paused = !paused;
            repaint();
            return;
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
                } else if (opcionPausa == 1) { // Guardar partida
                    boolean guardadoExitoso = GameSaveManager.guardarPartida(this, player);
                    ProfileManager.guardarPerfil();
                    if (guardadoExitoso) {
                        ShowWinMessage("Partida guardada exitosamente");
                    } else {
                        ShowWinMessage("Error al guardar la partida");
                    }
                    paused = false;
                } else if (opcionPausa == 2) { // Ir al menú de configuración
                    gameWindow.settingsGame();
                } else if (opcionPausa == 3) { // Volver al menú y guardar
                	
                	GameSaveManager.guardarPartida(this, player);
                	ProfileManager.guardarPerfil();
                    gameWindow.backToMenu();
                    
                }
                GameWindow.reproducirSonido("resources/sounds/confirm.wav");
                repaint();
            }
            return; // Evita que se siga ejecutando lógica normal
        }

        // Tecla para abrir/cerrar inventario
        if (keyCode == GameSettings.KEY_INVENTORY) {
            inventoryOpen = !inventoryOpen;
            GameWindow.reproducirSonido("resources/sounds/menu.wav");
            repaint();
            return;
        }
        
        // Moverse y usar el inventario
        if (inventoryOpen && player != null && player.inventory != null) {
            Inventory inv = player.inventory;
            switch(keyCode) {
                case KeyEvent.VK_UP:
                    inv.selectSlot((inv.getSelectedRow() - 1 + inv.getRows()) % inv.getRows(), inv.getSelectedColumn());
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    inv.selectSlot((inv.getSelectedRow() + 1) % inv.getRows(), inv.getSelectedColumn());
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                    repaint();
                    break;
                case KeyEvent.VK_LEFT:
                    inv.selectSlot(inv.getSelectedRow(), (inv.getSelectedColumn() - 1 + inv.getColumns()) % inv.getColumns());
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                    repaint();
                    break;
                case KeyEvent.VK_RIGHT:
                    inv.selectSlot(inv.getSelectedRow(), (inv.getSelectedColumn() + 1) % inv.getColumns());
                    GameWindow.reproducirSonido("resources/sounds/menu.wav");
                    repaint();
                    break;
                case KeyEvent.VK_ENTER:
                    ItemStack selected = inv.getSelectedSlot();
                    if (selected != null && !selected.isEmpty() && selected.getItem().isConsumable()) {
                        System.out.println("Usaste " + selected.getItem().getName());
                        
                        switch(selected.getItem().getId()) {
                        case "pancho":
                        	LevelPanel.Max_vida += 10;
                        	System.out.print(LevelPanel.Max_vida);
                        	break;
                        	
                        case "jugo_placer":
                        	LevelPanel.multiplicador_puntos += 0.1;
                        	System.out.print(LevelPanel.multiplicador_puntos);
                        	break;
                        	
                        case "chocolate_dubai":
                        	LevelPanel.plusSuma = 1;
                        	System.out.print(LevelPanel.plusSuma);
                        	break;
                        }
                        
                        // Resta 1 unidad del stack
                        selected.remove(1);
                        
                        // Sonido opcional
                        GameWindow.reproducirSonido("resources/sounds/use_item.wav");
                        
                        // Redibuja el inventario
                        repaint();
                    }
                    break;

            }
            return; // Evita que otras teclas interfieran mientras el inventario está abierto
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
        
        if(EnTienda) {
        	switch(keyCode) {
    		case KeyEvent.VK_UP:
    			OpcionTienda = (OpcionTienda - 1 + itemsCantina.length) % itemsCantina.length;
    			GameWindow.reproducirSonido("resources/sounds/menu.wav");
    			repaint();
    			break;
    		case KeyEvent.VK_DOWN:
    			OpcionTienda = (OpcionTienda + 1) % itemsCantina.length;
    			GameWindow.reproducirSonido("resources/sounds/menu.wav");
    			repaint();
    			break;
    		case KeyEvent.VK_ENTER:
    			
    			if(monedas >= precioCantina[OpcionTienda] && StockItem[OpcionTienda] > 0) { // Puede comprar
    				
    				// Agregar Item Al inventario
    				switch(itemsCantina[OpcionTienda]) {
    				case "Pancho":
    					givePlayerItem("pancho", "Pancho", "pancho.png", 1, 3);
    					break;
    				case "Jugo Placer":
    					givePlayerItem("jugo_placer", "Jugo Placer", "jugo placer.png", 1, 3);
    					break;
    					
    				case "Cachafaz":
    					givePlayerItem("cachafaz", "Cachafaz", "cachafaz.png", 1, 1);
    					break;
    				}
    				monedas -= precioCantina[OpcionTienda];
    				precioCantina[OpcionTienda] *= 2; // Subir el precio en cada compra
    				StockItem[OpcionTienda]--; // restar 1 stock en el item
    				
    				// Cambiar Texto
    				tiendaColor = new Color(255,255,255);
    				textoTienda = "Has comprado el item";
    				
    				// GameWindow.reproducirSonido("Compra");
    				
    			} else { // No puede comprar
    				
    				// GameWindow.reproducirSonido("Rechazo");
    				tiendaColor = new Color(200,0,0);
    				textoTienda = "No se puede comprar este Item.";
    				System.out.println("No se pudo comprar el item");
    				
    			}
    			
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
        
        // ------ Cambios en otros NPCs
        
        // Si hablas con zambrana por primera vez active a Melody
        if(nombreNPC.equals("Zambrana") && triggeredNPC("Melody") == 0) {
        	triggerNPC("Melody", 1);
        }
        
        // Kreimer: verificar si tiene pan sin tacc
        if(nombreNPC.equals("Kreimer")) {
        	if(npc.Trigger == 1 && playerHasItem("pan_sin_tacc", 1)) {
        		// Si tiene pan sin tacc, pasar a la linea donde agradece
        		removePlayerItem("pan_sin_tacc", 1);
        		currentLine = 6; // Linea antes de "Fua wacho..."
        	}
        }
        
        // Si ya le ganaste a martin y le traes un cachafaz
        if(nombreNPC.equals("Martin") && npc.Trigger == 3) {
        	if(playerHasItem("cachafaz", 1)) {
        		currentLine = 14;
        	}
        }
        
     // Si ya le ganaste a martin y le traes un cachafaz
        if(nombreNPC.equals("Interino") && npc.Trigger == 0) {
        	npc.Trigger = 1;
        }
        
        // Si hablaste con ciccaroni y tenes la pastafrola
        if(nombreNPC.equals("Ciccaroni") && npc.Trigger == 1) {
        	if(playerHasItem("pastafrola", 1)) { // Tiene pastafrola para darle
        		currentLine = 8; // Recibe pastafrola
        		npc.Trigger = 2; // Pasa a comerla
        	}
        }
        
        // Si hablas con seba
        if(nombreNPC.equals("Signorello")) {
        	npc.Trigger = 1;
        }
        
        // Findlay: verificar si tiene 8 marcadores
        if(nombreNPC.equals("Findlay") && npc.Trigger == 1 && countPlayerItem("marcador") >= 8) {
        	currentLine = 10; // Listo amigo gracias
        }
        
        // Si tenes el tornillo se lo das a la de lo biblioteca
        if(nombreNPC.equals("Biblioteca") && playerHasItem("tornillo", 1)) {
        	currentLine = 10; 
        }
        
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
    
    // Darle trigger a un npc
    public void triggerNPC(String targetTipo, int trigger) {
        NPC npc = NPCManager.getNPCByTipo(targetTipo);
        if (npc != null) {
            npc.Trigger = trigger;
        }
    }

    /**
     * Entrega un item al inventario del jugador. Si no cabe, devuelve la cantidad que no entró.
     * spriteFilename debe ser el nombre de archivo dentro de resources/Sprites/Items
     */
    public int givePlayerItem(String id, String displayName, String spriteFilename, int amount, int maxStack) {
        if (player == null) return amount;
        java.awt.Image img = null;
        try {
            img = new javax.swing.ImageIcon("resources/Sprites/Items/" + spriteFilename).getImage();
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen del item: " + spriteFilename);
        }
        Item item = new Item(id, displayName, img, maxStack);
        int leftover = player.pickupItem(item, amount);
        if (leftover > 0) {
            System.out.println("Inventario lleno: no cupo " + leftover + "x " + displayName);
            if (gameWindow != null) gameWindow.SWM("No cupo " + leftover + "x " + displayName + " en tu inventario.");
        } else {
        	if(!EnTienda) {
        		System.out.println("Obtuviste: " + amount + "x " + displayName);
                //ShowWinMessage("Has conseguido: " + displayName);
        	}
            
        }
        
        if(playerHasItem("lapicera", 1) && playerHasItem("vinilo", 1) && playerHasItem("fusible", 1) && playerHasItem("procesador", 1)) {
        	triggerNPC("Moya", 1);
        }
        return leftover;
    }
    
    // Verificar qué trigger tiene un npc
    public int triggeredNPC(String targetTipo) {
    	NPC npc = NPCManager.getNPCByTipo(targetTipo);
        return npc.Trigger;
    }
    
    /**
     * Verifica si el jugador tiene al menos 'amount' unidades de un item.
     */
    public boolean playerHasItem(String itemId, int amount) {
        if (player == null || player.inventory == null) return false;
        return player.inventory.hasItem(itemId, amount);
    }
    
    /**
     * Cuenta cuántas unidades de un item tiene el jugador.
     */
    public int countPlayerItem(String itemId) {
        if (player == null || player.inventory == null) return 0;
        return player.inventory.countItem(itemId);
    }
    
    /**
     * Remueve items del inventario del jugador. Devuelve la cantidad realmente removida.
     */
    public int removePlayerItem(String itemId, int amount) {
        if (player == null || player.inventory == null) return 0;
        return player.inventory.removeItem(itemId, amount);
    }
    
    private void realizarTeleport() {
        if (currentTeleportId == -1) {
            System.err.println("⚠ No se puede teleportar: ID inválido");
            return;
        }
        if (!player.canTeleport()) return;

        GameWindow.reproducirSonido("resources/sounds/menu.wav");

        // Determinar si hay cambio de piso
        Color tpColor = collisionMap.getPixelColor((int) player.getX(), (int) player.getY());
        boolean cambiaDePiso = collisionMap.isTeleportChangeFloor(tpColor);

        if (cambiaDePiso) {
            if (enPlantaAlta) {
                CargarZona(1); // carga planta baja
                enPlantaAlta = false;
            } else {
                CargarZona(0); // carga planta alta
                enPlantaAlta = true;
            }
        }

        boolean ignoreExclusion = cambiaDePiso; // si cambió de piso, ignoramos exclusión
        CollisionMap destinoMap = collisionMap;

        // Buscar destino
        Point destino = destinoMap.findTeleportDestination(currentTeleportId,
                                                          (int) player.getX(),
                                                          (int) player.getY(),
                                                          ignoreExclusion);
        if (destino != null) {
        	
        	if(enPlantaAlta) { // LLaves planta Alta
        		
        		if(currentTeleportId == 235 && !playerHasItem("llave_sum", 1)) { // Si no tiene llave del SUM
            		ShowWinMessage("Necesitas llave del SUM para pasar.");
            		return;
            	}
        		
        	} else { // Llaves planta baja
        		
        		if(currentTeleportId == 245 && !playerHasItem("llave_reja", 1)) { // Si no tiene llave de la reja
            		ShowWinMessage("Necesitas llave de la reja para pasar.");
            		return;
            	}
        		
        	}
        	
        	
        	if(currentTeleportId == 245 && !enPlantaAlta) { // Parar musica con el tp de ricky
        		
        		musicaParada = !musicaParada;
        		if(musicaParada) {
        			Musica.detenerMusica();
        		} else {
        			Musica.reproducirMusica("resources/Music/Fondo.wav");
        		}
        		
        	}
        	
            player.setX(destino.x);
            player.setY(destino.y);
            System.out.println("Teleport realizado (ID: " + currentTeleportId + ") a: " + destino.x + ", " + destino.y);
        } else {
            System.err.println("No se encontró destino de teleport con ID " + currentTeleportId);
        }

        player.update();
        player.setTeleportCooldown();
        repaint();
    }
    
    private void drawPauseMenu(Graphics2D g2d) {
        // Fondo semitransparente oscuro
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Título
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(110f)));
        g2d.setColor(Color.WHITE);
        String titulo = "PAUSA";
        FontMetrics fmTitulo = g2d.getFontMetrics();
        int xTitulo = (getWidth() - fmTitulo.stringWidth(titulo)) / 2;
        int yTitulo = getHeight() / 2 - 250;
        g2d.drawString(titulo, xTitulo, yTitulo);

        // Opciones
        g2d.setFont(GameWindow.Pixelart.deriveFont(GW.SF(65f)));
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
    
    // Cargar zona y npcs
    public void CargarZona(int zona) { // 0 = Planta Alta, 1 = Planta Baja, 2 = Ascensor secreto niejejej
    	switch(zona) {
    	
    	case 0: // PLANTA ALTA
    		
    		// Mapa
            collisionMap = plantaAltaMap;
            player.setCollisionMap(collisionMap);
            enPlantaAlta = true;
            
            // NPCs
            
            // Borrar npcs anteriores
            NPCs.clear();
            renderList.clear();
            renderList.add(player);
            
            // Generar NPCs
            NPCs.add(NPCManager.getOrCreateNPC("Pacheco", 285 * SCALE, 52 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Linzalata", 285 * SCALE, 30 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Ledesma", 13 * SCALE, 11 * SCALE, GW.SX(50), this));
            NPCs.add(NPCManager.getOrCreateNPC("Moya", 277 * SCALE, 14 * SCALE, GW.SX(45), this));
            NPCs.add(NPCManager.getOrCreateNPC("Gera", 99 * SCALE, 14 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("ASCENSOR", 174 * SCALE, 122 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Ascensor", 172 * SCALE, 43 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Estufa", 261 * SCALE, 191 * SCALE, GW.SX(60), this));
            NPCs.add(NPCManager.getOrCreateNPC("Caja", 23 * SCALE, 133 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Tacho", 128 * SCALE, 208 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("TAcho", 268 * SCALE, 164 * SCALE, GW.SX(40), this));
            
            for (NPC npc : NPCs) {
                renderList.add(npc);
            }
            
    		break;
    		
    	case 1: // PLANTA BAJA
    		
    		// Mapa
            collisionMap = plantaBajaMap;
            player.setCollisionMap(collisionMap);
            enPlantaAlta = false;
            
            // NPCs
            NPCs.clear();
            renderList.clear();
            renderList.add(player);
       
            // Generar NPCs
            NPCs.add(NPCManager.getOrCreateNPC("Findlay", 63 * SCALE, 167 * SCALE, GW.SX(45), this));
            NPCs.add(NPCManager.getOrCreateNPC("Lavega", 67 * SCALE, 167 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Melody", 49 * SCALE, 105 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Gennuso", 72 * SCALE, 237 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Signorello", 75 * SCALE, 110 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Vagos", 56 * SCALE, 109 * SCALE, GW.SX(110), this));
            NPCs.add(NPCManager.getOrCreateNPC("Biblioteca", 17 * SCALE, 238 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Guerra", 15 * SCALE, 230 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Cantina", 84 * SCALE, 97 * SCALE, GW.SX(45), this));
            NPCs.add(NPCManager.getOrCreateNPC("Rita", 14 * SCALE, 245 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Pecile", 24 * SCALE, 219 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Kreimer", 115 * SCALE, 207 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Martin", 123 * SCALE, 196 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Casas", 209 * SCALE, 238 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Ciccaroni", 179 * SCALE, 196 * SCALE, GW.SX(46), this));
            NPCs.add(NPCManager.getOrCreateNPC("Ulises", 195 * SCALE, 173 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Gramajo", 236 * SCALE, 175 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Zambrana", 166 * SCALE, 206 * SCALE, GW.SX(45), this));
            NPCs.add(NPCManager.getOrCreateNPC("Interino", 218 * SCALE, 203 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("TACho", 58 * SCALE, 215 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("TACHo", 70 * SCALE, 101 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("TACHO", 190 * SCALE, 195 * SCALE, GW.SX(40), this));
            
            NPCs.add(NPCManager.getOrCreateNPC("Ricky", 46 * SCALE, 36 * SCALE, GW.SX(200), this));
            
            for (NPC npc : NPCs) {
                renderList.add(npc);
            }
            
    		break;
    		
    	default:
    		System.out.print("No se ha cargado ningún mapa");
    	}
    }
    
    // Procesar Opciones NPCs
    private void procesarOpcion(String opcion, NPC npc) {
    	
    	// Npcs
    	switch(npc.Tipo) {
    		
    	case "Melody":
    		if (opcion.equals("SI")) {
            	gameWindow.startRitmo("Melody", 7, 135);
            }
            if (opcion.equals("NO")) System.out.println("Usuario dijo que no");
    		break;
    		
    	case "Moya":
    		if(opcion.equals("Dar objetos")) {
    			Musica.reproducirMusica("resources/Music/juzgar.wav");
    			if(martinBien && gennusoBien) { // Final Bueno
    				currentLine = 15;
    				npc.Trigger = 3;
    				triggerNPC("Linzalata", 1);
    			} else { // Final neutral
    				currentLine = 6;
    				npc.Trigger = 2;
    			}
    		}
    		if (opcion.equals("SI")) {
            	gameWindow.startRitmo("Moya", 12, 153);
            }
            if (opcion.equals("NO")) {
            	npc.Trigger = 2;
            }
            if(opcion.equals("Volver")) {
            	currentLine = -1; // Dejar de hablar con moya
            }
    		break;
    		
    	case "Linzalata":
    		if (opcion.equals("SI")) {
            	gameWindow.startRitmo("Linzalata", 13, 222);
            }
            if (opcion.equals("Ya vengo peleando así")) {
            	npc.Trigger = 2;
            };
    		break;
    		
    	case "Ricky":
    		if (opcion.equals("SI")) {
            	gameWindow.startRitmo("Ricky", 13, 170);
            }
            if (opcion.equals("NO")) System.out.println("Usuario dijo que no");
    		break;
    		
    	case "Biblioteca":
    		if(opcion.equals("Agarrar vinilo")) {
    			givePlayerItem("vinilo", "Vinilo de La Renga", "vinilo_LaRenga.png", 1, 1);
    		}
    		if(opcion.equals("Dar tornillo")) {
    			removePlayerItem("tornillo", 1);
    		}
    		if(opcion.equals("Ok")) {
    			npc.Trigger = 1;
    			triggerNPC("Casas", 1);
    		}
    		break;
    		
    	case "Casas":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Casas", 10, 162);
    		}
    		if(opcion.equals("Las necesito ahora")) {
    			npc.Trigger = 2;
    		}
    		break;
    		
    	case "Interino":
    		if(opcion.equals("Dar chocolate")) {
    			if(playerHasItem("chocolate_dubai", 1)) { // Tiene el chocolate
    				removePlayerItem("chocolate_dubai", 1);
    				ascensorTaller = true;
    				npc.Trigger = 2;
    			} else { // No tiene el chocolate
    				currentLine = -1;
    				ShowWinMessage("No tenés ningún chocolate para dar");
    				return;
    			}
    		}
    		break;
    		
    	case "Cantina":
    		if(opcion.equals("Comprar")) {
    			EnTienda = true;
    			OpcionTienda = 0;
    		}
    		if(opcion.equals("Hablar")) {
    			if(npc.Trigger == 0) { // Default
            		currentLine = 2; // 1 menos porque despues currentLine se suma
            		triggerNPC("Vagos", 1);
            	} else if(npc.Trigger == 1) { // Recompensa por echar a los vagos
            		currentLine = 5;
            		triggerNPC("Cantina", 2);
            	} else if(npc.Trigger == 2) { // Nada
            		if(playerHasItem("renaa_gm", 1)) {
            			currentLine = 9; // Tiene el IG de renaa
            			npc.Trigger = 3;
            		} else {
            			currentLine = 16; // No tiene nada
            		}
            	} else if(npc.Trigger == 3) { // Ten�s el ig de rena
            		currentLine = 9;
            	} else if(npc.Trigger == 4) { // Ya le diste todo
            		currentLine = 16;
            	}
    		}
    		if(opcion.equals("SI")) {
    			// Dar el IG de renaa_gm y recibir pan sin tacc
    			if(playerHasItem("renaa_gm", 1)) {
    				removePlayerItem("renaa_gm", 1);
    				givePlayerItem("pan_sin_tacc", "Pan sin TACC", "pan sin tacc.png", 1, 1);
    				GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    			}
    		}
    		if(opcion.equals("Ok")) {
    			npc.Trigger = 4;
    		}
    		if(opcion.equals("NO")) {
    			// No le da el IG
    			currentLine = 14;
    		}
    		if(opcion.equals("Dale")) {
    			// Recibe el chocolate dubai (cuando gana a Los Vagos)
    			givePlayerItem("chocolate_dubai", "Chocolate Dubai", "chocolate dubai.png", 1, 1);
    			GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		if (opcion.equals("Salir")) System.out.println("Usuario dijo que no");
    		break;
    		
    	case "Gera":
    		if(opcion.equals("Aceptar")) {
    			// Dar libro de automotor
    			givePlayerItem("libro_automotor", "Libro de Automotor", "libro_Automotor.png", 1, 1);
    			npc.Trigger = 1;
    			GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		break;
    		
    	case "Findlay":
    		if(opcion.equals("Agarrar lapicera")) {
                    givePlayerItem("lapicera", "Lapicera", "lapicera.png", 1, 1);
                    npc.Trigger = 2; // marcar como tomado
                    GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		if(opcion.equals("Qué haces en un tacho?")) {
    			npc.Trigger = 1;
    			triggerNPC("Lavega", 1);
    		}
    		break;
    		
    	case "Lavega":
    		if(opcion.equals("Agarrar marcador")) {
                // Darle marcador al jugador
                givePlayerItem("marcador_findlay", "Marcador rojo", "marcador_Rojo.png", 1, 1);
                npc.Trigger = 2;
                GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		break;
    		
    	case "Ulises":
    		if(opcion.equals("Agarrar")) {
                // Darle Zancos al jugador
                givePlayerItem("zancos", "Zancos", "zancos.png", 1, 1);
                npc.Trigger = 1;
                GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		break;
    		
    	case "Martin":
    		if(opcion.equals("Me lo das?")) {
    			// Verificar si tiene al menos 7 marcadores
    			if(countPlayerItem("marcador_findlay") >= 7) {
    				npc.Trigger = 2; // Tiene los 7, puede pasar a la pelea
    			} else {
    				npc.Trigger = 1; // No tiene todos, mostrar mensaje
    			}
    		}
    		// SI Para pelear
    		if(opcion.equals("SI")) {
    			// Pelear con martin
    			gameWindow.startRitmo("Martin", 10, 130);
    		}
    		// Si para darle cachafaz
    		if(opcion.equals("Si")) {
    			removePlayerItem("cachafaz", 1);
    			martinBien = true;
    		}
    		break;
    		
    	case "Kreimer":
    		if(opcion.equals("Agarrar fusible")) {
                // Darle fusible al jugador
                givePlayerItem("fusible", "Fusible", "Fusible.png", 1, 3);
                npc.Trigger = 2; // Ya le dio el fusible
                GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		break;
    		
    	case "Ascensor":
    		if(opcion.equals("SI")) {
                // Darle fusible al jugador
                givePlayerItem("fusible", "Fusible", "Fusible.png", 1, 3);
                npc.Trigger = 1; // Ya le dio el fusible
                GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		break;
    		
    	case "ASCENSOR":
    		if(opcion.equals("SI")) {
                // Darle fusible al jugador
    			if(playerHasItem("fusible", 3)) {
    				ShowWinMessage("Escuchas un ascensor abriéndose en el piso de abajo");
    				npc.Trigger = 1;
    				triggerNPC("pecile", 1);
    				return;
    			} else {
    				ShowWinMessage("no tenes los fusibles suficientes.");
    				return;
    			}
    		}
    		break;
    		
    	case "Pecile":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Pecile", 11, 200);
    		}
    		break;
    		
    	case "Ciccaroni":
    		if(opcion.equals("Un fusible")) {
    			npc.Trigger = 1;
    		}
    		if(opcion.equals("Agarrar fusible")) {
                givePlayerItem("fusible", "Fusible", "Fusible.png", 1, 3);
                GameWindow.reproducirSonido("resources/sounds/confirm.wav");
    		}
    		break;
    		
    	case "Signorello":
    		
    		if(opcion.equals("Pelear")) {
    			gameWindow.startRitmo("Signorello", 8, 178);
    		}
    		if(opcion.equals("Dar Instagram")) {
    			if(playerHasItem("renaa_gm", 1)) {
    				
    				removePlayerItem("renaa_gm", 1);
    				givePlayerItem("marcador_findlay", "Marcador azul", "marcador_Azul.png", 1, 1);
    				
    				ShowWinMessage("Conseguiste el Marcador azul");
    				npc.Trigger = 2;
    				return;
    			} else {
    				ShowWinMessage("No tenes ningún Instagram para dar.");
    				return;
    			}
    		}
    		break;
    		
    	case "Ledesma":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Ledesma", 10, 102);
    		}
    		if(opcion.equals("Dar boletín")) {
    			if(playerHasItem("boletin", 1)) {
    				removePlayerItem("boletin", 1);
    				npc.Trigger = 1;
    			} else {
    				ShowWinMessage("No tenes ningún boletín para dar.");
    				return;
    			}
    		}
    		if(opcion.equals("Agarrar resumen")) {
    			givePlayerItem("resumen", "Resumen BBDD", "resumen_Prueba.png", 1, 1);
    			npc.Trigger = 2;
    		}
    		break;
    		
    	case "Gramajo":
    		if(opcion.equals("Dar libro")) {
    			if(playerHasItem("libro_automotor", 1)) {
    				removePlayerItem("libro_automotor", 1);
    				npc.Trigger = 1;
    			} else {
    				ShowWinMessage("No tenes ningún libro para dar");
    				return;
    			}
    		}
    		if(opcion.equals("Agarrar llave")) {
    			givePlayerItem("llave_laboratorio", "llave del laboratorio", "llave_Laboratorio.png", 1, 1);
    			npc.Trigger = 2;
    		}
    		break;
    		
    	case "Pacheco":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Pacheco", 5, -1);
    		}
    		if(opcion.equals("Agarrar boletin")) {
    			givePlayerItem("boletin", "Boletin Pacheco", "boletin_Pacheco.png", 1, 1);
    			npc.Trigger = 2;
    		}
    		break;
    		
    	case "Gennuso":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Gennuso", 5, -1);
    		}
    		if(opcion.equals("Nada")) {
    			currentLine = -1;
    		}
    		break;
    		
    	case "Rita":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Rita", 10, 184);
    		}
    		break;
    		
    	case "Vagos":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Vagos", 5, 172);
    		}
    		break;
    		
    	case "Guerra":
    		
    		if(opcion.equals("Dar resumen")) {
    			if(playerHasItem("resumen", 1)) {
    				removePlayerItem("resumen", 1);
    				npc.Trigger = 1;
    			} else {
    				ShowWinMessage("No tenes ningún resumen para dar.");
    				return;
    			}
    		}
    		if(opcion.equals("Agarrar Marcador")) {
    			givePlayerItem("marcador_findlay", "Marcador violeta", "marcador_Violeta.png", 1, 1);
    			npc.Trigger = 2;
    		}
    		break;
    		
    	case "TAcho":
    		if(opcion.equals("SI")) {
    			npc.Trigger = 1;
    			givePlayerItem("marcador_findlay", "Marcador amarillo", "marcador_Amarillo.png", 1, 1);
    		}
    		break;
    		
    	case "TACho":
    		if(opcion.equals("SI")) {
    			npc.Trigger = 1;
    			givePlayerItem("marcador_findlay", "Marcador naranja", "marcador_Naranja.png", 1, 1);
    		}
    		break;
    		
    	case "TACHO":
    		if(opcion.equals("SI")) {
    			npc.Trigger = 1;
    			givePlayerItem("marcador_findlay", "Marcador rosa", "marcador_Rosa.png", 1, 1);
    		}
    		break;
    		
    	case "Estufa":
    		if(opcion.equals("Agarrar marcador")) {
    			if(playerHasItem("zancos", 1)) {
    				npc.Trigger = 1;
        			givePlayerItem("marcador_findlay", "Marcador celeste", "marcador_Celeste.png", 1, 1);
        			ShowWinMessage("Conseguiste el Marcador celeste");
        			return;
    			} else {
    				ShowWinMessage("Está muy alto para alcanzarlo");
    				return;
    			}
    		}
    		break;
    		
    	case "Caja":
    		if(opcion.equals("Agarrar tornillo")) {
    			npc.Trigger = 1;
    			givePlayerItem("tornillo", "tornillo especifico", "tornillo especifico.png", 1, 1);
    			ShowWinMessage("Conseguiste el tornillo convenientemente especifico... y también muchas dudas existenciales");
    			return;
    		}
    		break;
    		
    		
    	}
        
        eligiendoOpcion = false;
        opciones = null;
        currentLine++;
        loadCurrentLine(currentNPC);
    }
    
    // ===== MÉTODOS PARA SISTEMA DE GUARDADO =====
    
    /**
     * Obtiene el stock actual de la tienda
     */
    public int[] getStockTienda() {
        return StockItem;
    }
    
    /**
     * Establece el stock de la tienda
     */
    public void setStockTienda(int[] stock) {
        if (stock != null && stock.length == StockItem.length) {
            this.StockItem = stock;
        }
    }
    
    /**
     * Crea un Item con su imagen cargada (para sistema de carga)
     */
    public static Item createItemWithImage(String id, String displayName, int maxStack) {
        // Mapeo de IDs a nombres de archivos de sprites
        String spriteFilename = getSpriteName(id, displayName);
        
        java.awt.Image img = null;
        if (spriteFilename != null) {
            try {
                img = new javax.swing.ImageIcon("resources/Sprites/Items/" + spriteFilename).getImage();
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen del item: " + spriteFilename);
            }
        }
        
        return new Item(id, displayName, img, maxStack);
    }
    
    /**
     * Obtiene el nombre del archivo de sprite según el ID del item
     */
    private static String getSpriteName(String id, String nombre) {
        // Mapeo de todos los items del juego
        switch (id) {
            case "pancho": return "pancho.png";
            case "procesador": return "procesador.png";
            case "jugo_placer": return "jugo placer.png";
            case "cachafaz": return "cachafaz.png";
            case "vinilo": return "vinilo_LaRenga.png";
            case "pan_sin_tacc": return "pan sin tacc.png";
            case "chocolate_dubai": return "chocolate dubai.png";
            case "libro_automotor": return "libro_Automotor.png";
            case "lapicera": return "lapicera.png";
            
            case "marcador_findlay": // Marcadores findlay
            	switch(nombre) {
            	
            	case "Marcador rojo": return "marcador_Rojo.png";
            	case "Marcador rosa": return "marcador_Rosa.png";
            	case "Marcador amarillo": return "marcador_Amarillo.png";
            	case "Marcador naranja": return "marcador_Naranja.png";
            	case "Marcador verde": return "marcador_Verde.png";
            	case "Marcador violeta": return "marcador_Violeta.png";
            	case "Marcador azul": return "marcador_Azul.png";
            	case "Marcador celeste": return "marcador_Celeste.png";
            	default: return null;
            	
            	}
            	
            case "zancos": return "zancos.png";
            case "fusible": return "Fusible.png";
            case "resumen": return "resumen_Prueba.png";
            case "llave_laboratorio": return "llave_Laboratorio.png";
            case "boletin": return "boletin_Pacheco.png";
            case "llave_sum": return "llave_SUM.png";
            case "llave_reja": return "llave_Reja.png";
            case "instagram": return "@renaa_gm.png";
            case "tornillo": return "tornillo especifico.png";
            default: return null;
        }
    }

}
