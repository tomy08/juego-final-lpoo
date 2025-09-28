package main;
public class GameThread extends Thread {
    private static final int FPS = 60;
    private static final long FRAME_TIME = 1000 / FPS; // Tiempo por frame en milisegundos
    
    private GamePanel gamePanel;
    private boolean running;
    
    public GameThread(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.running = false;
    }
    
    @Override
    public void run() {
        running = true;
        long lastTime = System.currentTimeMillis();
        
        while (running) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastTime;
            
            // Actualizar juego
            gamePanel.update();
            gamePanel.repaint();
            
            // Control de FPS
            long frameTime = System.currentTimeMillis() - currentTime;
            long sleepTime = FRAME_TIME - frameTime;
            
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    running = false;
                    break;
                }
            }
            
            lastTime = currentTime;
        }
    }
    
    public void stopGame() {
        running = false;
        try {
            // Esperar a que el hilo termine
            if (this.isAlive()) {
                this.interrupt();
                this.join(1000); // Esperar máximo 1 segundo
            }
        } catch (InterruptedException e) {
            // Thread interrumpido, continuar con el cierre
        }
    }
    
    public boolean isRunning() {
        return running;
    }
}