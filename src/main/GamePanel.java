package main;
import javax.swing.*;

import Mapa.CollisionMap;
import Sonidos.Musica;
import entities.NPC;
import entities.Player;
import entities.NPCManager;
import entities.Inventory;
import entities.Item;
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
    private static int SCALE = GW.SX(34);
    
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
    
    private boolean martinBien = false;
    public boolean gennusoBien = true;
    
    // Zonas desbloqueadas:
    
    public boolean taller = true;

    // Tienda
    
    public int monedas = 1000000;
    private boolean EnTienda = true;
    private String[] itemsCantina = {
    		"Pancho",
    		"Jugo Placer",
    		"Cachafaz"
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
    private String[] opcionesPausa = {"CONTINUAR","SETTINGS", "VOLVER AL MENU"};

    // Inventario
    private boolean inventoryOpen = false;
    
    // Render
    ArrayList<Object> renderList = new ArrayList<>();

    
    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        
        Musica.reproducirMusica("resources/Music/Fondo.wav");
        Musica.enableLoop();
        
        // Cargar ambos mapas de colisiones
        plantaAltaMap = new CollisionMap("resources/Collision_Maps/PLANTAALTA.png");
        plantaBajaMap = new CollisionMap("resources/Collision_Maps/PLANTABAJA.png");
        
        pressedKeys = new HashSet<>();
        
        // Inicializar jugador
        player = new Player(130 * SCALE, 159 * SCALE, this);
        CargarZona(0);
        
        
        
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
                g2d.drawString(nombreNPC, GW.SX(340), GW.SY(700));
                
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
        
        // No está permitido taller en planta baja si no se desbloquea
        if (!taller && !enPlantaAlta) {
            if (player.getX() > 168 * SCALE) {  
                player.setX(168 * SCALE);
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

        // Tecla para abrir/cerrar inventario
        if (keyCode == GameSettings.KEY_INVENTORY) {
            inventoryOpen = !inventoryOpen;
            GameWindow.reproducirSonido("resources/sounds/menu.wav");
            repaint();
            return;
            
            
        }
        
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

        

        // Selección de hotbar con teclas 1-9
        if (keyCode >= KeyEvent.VK_1 && keyCode <= KeyEvent.VK_9) {
            int num = keyCode - KeyEvent.VK_1; // 0-based
            if (player != null && player.inventory != null) {
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
        
        // Cambios en otros NPCs
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
        
        if(nombreNPC.equals("Martin") && npc.Trigger == 3) {
        	if(playerHasItem("cachafaz", 1)) {
        		currentLine = 14;
        	}
        }
        
        if(nombreNPC.equals("Ciccaroni") && npc.Trigger == 1) {
        	if(playerHasItem("pastafrola", 1)) { // Tiene pastafrola para darle
        		currentLine = 8; // Recibe pastafrola
        		npc.Trigger = 2; // Pasa a comerla
        	}
        }
        
        if(nombreNPC.equals("Signorello")) {
        	npc.Trigger = 1;
        }
        
        // Findlay: verificar si tiene 8 marcadores
        if(nombreNPC.equals("Findlay") && npc.Trigger == 1 && countPlayerItem("marcador_findlay") >= 8) {
        	currentLine = 10; // Listo amigo gracias
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
                if (gameWindow != null) gameWindow.SWM("Has conseguido: " + displayName);
        	}
            
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

        // Cargar mapa destino
        if(enPlantaAlta) {
        	CargarZona(1);
        } else {
        	CargarZona(0);
        }
        
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
    private void CargarZona(int zona) { // 0 = Planta Alta, 1 = Planta Baja, 2 = Ascensor secreto niejejej
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
            NPCs.add(NPCManager.getOrCreateNPC("Pacheco", 130 * SCALE, 156 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Linzalata", 130 * SCALE, 135 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Ledesma", 14 * SCALE, 62 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Moya", 123 * SCALE, 128 * SCALE, GW.SX(45), this));
            NPCs.add(NPCManager.getOrCreateNPC("Gera", 43 * SCALE, 20 * SCALE, GW.SX(40), this));
            
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
            NPCs.add(NPCManager.getOrCreateNPC("Vagos", 56 * SCALE, 109 * SCALE, GW.SX(60), this));
            NPCs.add(NPCManager.getOrCreateNPC("Biblioteca", 17 * SCALE, 238 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Guerra", 15 * SCALE, 230 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Cantina", 85 * SCALE, 96 * SCALE, GW.SX(90), this));
            NPCs.add(NPCManager.getOrCreateNPC("Rita", 14 * SCALE, 245 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Pecile", 24 * SCALE, 219 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Kreimer", 115 * SCALE, 207 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Martin", 123 * SCALE, 196 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Casas", 209 * SCALE, 238 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Ciccaroni", 179 * SCALE, 196 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Ulises", 195 * SCALE, 173 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Gramajo", 236 * SCALE, 175 * SCALE, GW.SX(40), this));
            NPCs.add(NPCManager.getOrCreateNPC("Zambrana", 166 * SCALE, 206 * SCALE, GW.SX(40), this));
            
            for (NPC npc : NPCs) {
                renderList.add(npc);
            }
    		break;
    		
    	case 2: // ASCENSOR SECRETO
    		
    		// NPCs
            NPCs.clear();
    		renderList.clear();
            renderList.add(player);
            
            // Generar NPCs
    		NPCs.add(NPCManager.getOrCreateNPC("Ricky", 34 * SCALE, 47 * SCALE, GW.SX(50), this));
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
    		if (opcion.equals("SI")) {
            	gameWindow.startRitmo("Moya", 12, 153);
            }
            if (opcion.equals("NO")) System.out.println("Usuario dijo que no");
    		break;
    		
    	case "Linzalata":
    		if (opcion.equals("SI")) {
            	gameWindow.startRitmo("Linzalata", 13, 222);
            }
            if (opcion.equals("NO")) System.out.println("Usuario dijo que no");
    		break;
    		
    	case "Ricky":
    		if (opcion.equals("SI")) {
            	gameWindow.startRitmo("Moya", 13, 170);
            }
            if (opcion.equals("NO")) System.out.println("Usuario dijo que no");
    		break;
    		
    	case "Cantina":
    		if(opcion.equals("Comprar")) {
    			EnTienda = true;
    			OpcionTienda = 0;
    		}
    		if(opcion.equals("Hablar")) {
    			if(npc.Trigger == 0) { // Default
            		currentLine = 2; // 1 menos porque despues currentLine se suma
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
                givePlayerItem("marcador_findlay", "Marcador", "marcador_Rojo.png", 1, 1);
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
    			gameWindow.startRitmo("Martin", 10, 180);
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
    				givePlayerItem("marcador_findlay", "Marcador", "marcador_Azul.png", 1, 1);
    				ShowWinMessage("Conseguiste el Marcador azul");
    				return;
    			} else {
    				ShowWinMessage("No tenes ningún Instagram para dar.");
    				return;
    			}
    		}
    		break;
    		
    	case "Ledesma":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Ledesma", 10, -1);
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
    		
    	case "Pacheco":
    		if(opcion.equals("SI")) {
    			gameWindow.startRitmo("Pacheco", 5, -1);
    		}
    		if(opcion.equals("Agarrar boletin")) {
    			givePlayerItem("boletin", "Boletin Pacheco", "boletin_Pacheco.png", 1, 1);
    			npc.Trigger = 2;
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
    			givePlayerItem("marcador_findlay", "Marcador", "marcador_Violeta.png", 1, 1);
    			npc.Trigger = 2;
    		}
    		
    	}
        
        eligiendoOpcion = false;
        opciones = null;
        currentLine++;
        loadCurrentLine(currentNPC);
    }

}
