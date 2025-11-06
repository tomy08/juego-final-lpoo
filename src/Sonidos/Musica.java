package Sonidos;

import java.io.File;
import javax.sound.sampled.*;

import main.GameWindow;

public class Musica {
    private static Clip clip;
    private static String rutaActual = null;
    private static boolean loop = false;

    public static void reproducirMusica(String rutaArchivo) {
        try {
        	
            if (clip != null && clip.isRunning() && rutaArchivo.equals(rutaActual)) return;
            if (clip != null) {
                clip.stop();
                clip.close();
            }

            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(rutaArchivo));
            clip = AudioSystem.getClip();
            clip.open(audioInput);

            
            setVolumen(GameWindow.volumenGlobal);
            if(!GameWindow.musicaActivada) {
            	setVolumen(0);
            }

            clip.start();
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);

            rutaActual = rutaArchivo;
        } catch (Exception e) {
            System.out.println("Error al reproducir música: " + e.getMessage());
        }
    }

    public static void detenerMusica() {
        if (clip != null) {
            clip.stop();
        }
    }

    public static void pausarMusica() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public static void reanudarMusica() {
    	if(!GameWindow.musicaActivada) return;
        if (clip != null && !clip.isRunning()) {
            clip.start();
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void enableLoop() {
        loop = true;
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void disableLoop() {
        loop = false;
    }

    public static void setVolumen(float valor) {
        try {
            if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                
                FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (-60.0 + 60.0 * valor);
                if (dB < control.getMinimum()) dB = control.getMinimum();
                if (dB > control.getMaximum()) dB = control.getMaximum();
                control.setValue(dB);
            }
        } catch (Exception e) {
            System.out.println("Error al ajustar volumen: " + e.getMessage());
        }
    }

    public static boolean estaCorriendo() {
        return clip != null && clip.isRunning();
    }

    public static String getRutaActual() {
        return rutaActual;
    }
}
