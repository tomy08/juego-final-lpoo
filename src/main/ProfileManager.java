package main;

import java.io.*;

public class ProfileManager {

    private static final String PROFILE_FILE = "profile.dat";

    // === Variables globales ===
    public static boolean nivel1Pasado = false;
    public static boolean nivel2Pasado = false;
    public static boolean nivel3Pasado = false;
    public static boolean nivel4Pasado = false;
    public static boolean nivel5Pasado = false;
    public static boolean nivel6Pasado = false;
    public static boolean nivel7Pasado = false;
    public static boolean nivel8Pasado = false;
    public static boolean nivel9Pasado = false;
    public static boolean nivel10Pasado = false;
    public static boolean nivel11Pasado = false;
    public static boolean nivel12Pasado = false;
    public static boolean nivel13Pasado = false;

    /**
     * Guarda las configuraciones y desbloqueos permanentes
     */
    public static boolean guardarPerfil() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(PROFILE_FILE))) {

            // === 1. Controles configurados ===
            dos.writeUTF(GameSettings.teclaArriba);
            dos.writeUTF(GameSettings.teclaAbajo);
            dos.writeUTF(GameSettings.teclaIzquierda);
            dos.writeUTF(GameSettings.teclaDerecha);
            dos.writeUTF(GameSettings.teclaInteractuar);
            dos.writeUTF(GameSettings.teclaInventario);
            dos.writeUTF(GameSettings.teclaPausa);
            dos.writeUTF(GameSettings.teclaAdelantarTexto);
            dos.writeUTF(GameSettings.teclaNotaIzquierda);
            dos.writeUTF(GameSettings.teclaNotaAbajo);
            dos.writeUTF(GameSettings.teclaNotaArriba);
            dos.writeUTF(GameSettings.teclaNotaDerecha);

            dos.writeInt(GameSettings.KEY_UP);
            dos.writeInt(GameSettings.KEY_DOWN);
            dos.writeInt(GameSettings.KEY_LEFT);
            dos.writeInt(GameSettings.KEY_RIGHT);
            dos.writeInt(GameSettings.KEY_INTERACT);
            dos.writeInt(GameSettings.KEY_INVENTORY);
            dos.writeInt(GameSettings.KEY_MENU);
            dos.writeInt(GameSettings.KEY_CONFIRM);
            dos.writeInt(GameSettings.KEY_NLEFT);
            dos.writeInt(GameSettings.KEY_NDOWN);
            dos.writeInt(GameSettings.KEY_NUP);
            dos.writeInt(GameSettings.KEY_NRIGHT);

            // === 2. Datos de progreso global ===
            dos.writeBoolean(nivel1Pasado);
            dos.writeBoolean(nivel2Pasado);
            dos.writeBoolean(nivel3Pasado);
            dos.writeBoolean(nivel4Pasado);
            dos.writeBoolean(nivel5Pasado);
            dos.writeBoolean(nivel6Pasado);
            dos.writeBoolean(nivel7Pasado);
            dos.writeBoolean(nivel8Pasado);
            dos.writeBoolean(nivel9Pasado);
            dos.writeBoolean(nivel10Pasado);
            dos.writeBoolean(nivel11Pasado);
            dos.writeBoolean(nivel12Pasado);
            dos.writeBoolean(nivel13Pasado);

            System.out.println("Perfil guardado exitosamente en " + PROFILE_FILE);
            return true;

        } catch (IOException e) {
            System.err.println("Error al guardar el perfil: " + e.getMessage());
            return false;
        }
    }

    /**
     * Carga las configuraciones y desbloqueos permanentes
     */
    public static boolean cargarPerfil() {
        File file = new File(PROFILE_FILE);
        if (!file.exists()) {
            System.out.println("No se encontró archivo de perfil. Se crearán valores por defecto.");
            actualizarDesbloqueos(); // Inicializa desbloqueo básico
            return false;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(PROFILE_FILE))) {

            // === 1. Controles configurados ===
            GameSettings.teclaArriba = dis.readUTF();
            GameSettings.teclaAbajo = dis.readUTF();
            GameSettings.teclaIzquierda = dis.readUTF();
            GameSettings.teclaDerecha = dis.readUTF();
            GameSettings.teclaInteractuar = dis.readUTF();
            GameSettings.teclaInventario = dis.readUTF();
            GameSettings.teclaPausa = dis.readUTF();
            GameSettings.teclaAdelantarTexto = dis.readUTF();
            GameSettings.teclaNotaIzquierda = dis.readUTF();
            GameSettings.teclaNotaAbajo = dis.readUTF();
            GameSettings.teclaNotaArriba = dis.readUTF();
            GameSettings.teclaNotaDerecha = dis.readUTF();

            GameSettings.KEY_UP = dis.readInt();
            GameSettings.KEY_DOWN = dis.readInt();
            GameSettings.KEY_LEFT = dis.readInt();
            GameSettings.KEY_RIGHT = dis.readInt();
            GameSettings.KEY_INTERACT = dis.readInt();
            GameSettings.KEY_INVENTORY = dis.readInt();
            GameSettings.KEY_MENU = dis.readInt();
            GameSettings.KEY_CONFIRM = dis.readInt();
            GameSettings.KEY_NLEFT = dis.readInt();
            GameSettings.KEY_NDOWN = dis.readInt();
            GameSettings.KEY_NUP = dis.readInt();
            GameSettings.KEY_NRIGHT = dis.readInt();

            // === 2. Datos de progreso global ===
            nivel1Pasado = dis.readBoolean();
            nivel2Pasado = dis.readBoolean();
            nivel3Pasado = dis.readBoolean();
            nivel4Pasado = dis.readBoolean();
            nivel5Pasado = dis.readBoolean();
            nivel6Pasado = dis.readBoolean();
            nivel7Pasado = dis.readBoolean();
            nivel8Pasado = dis.readBoolean();
            nivel9Pasado = dis.readBoolean();
            nivel10Pasado = dis.readBoolean();
            nivel11Pasado = dis.readBoolean();
            nivel12Pasado = dis.readBoolean();
            nivel13Pasado = dis.readBoolean();

            // === Actualizar niveles desbloqueados ===
            actualizarDesbloqueos();

            System.out.println("Perfil cargado exitosamente desde " + PROFILE_FILE);
            return true;

        } catch (IOException e) {
            System.err.println("Error al cargar el perfil: " + e.getMessage());
            actualizarDesbloqueos();
            return false;
        }
    }

    /**
     * Sincroniza el progreso con Niveles.nivelesDesbloqueados[]
     */
    private static void actualizarDesbloqueos() {
        if (Niveles.nivelesDesbloqueados == null || Niveles.nivelesDesbloqueados.length < 13)
            return;

        Niveles.nivelesDesbloqueados[0] = true; // Nivel 1 siempre desbloqueado
        Niveles.nivelesDesbloqueados[1] = nivel2Pasado;
        Niveles.nivelesDesbloqueados[2] = nivel3Pasado;
        Niveles.nivelesDesbloqueados[3] = nivel4Pasado;
        Niveles.nivelesDesbloqueados[4] = nivel5Pasado;
        Niveles.nivelesDesbloqueados[5] = nivel6Pasado;
        Niveles.nivelesDesbloqueados[6] = nivel7Pasado;
        Niveles.nivelesDesbloqueados[7] = nivel8Pasado;
        Niveles.nivelesDesbloqueados[8] = nivel9Pasado;
        Niveles.nivelesDesbloqueados[9] = nivel10Pasado;
        Niveles.nivelesDesbloqueados[10] = nivel11Pasado;
        Niveles.nivelesDesbloqueados[11] = nivel12Pasado;
        Niveles.nivelesDesbloqueados[12] = nivel13Pasado;
    }

    /**
     * Verifica si el archivo de perfil existe
     */
    public static boolean existePerfil() {
        return new File(PROFILE_FILE).exists();
    }
}
