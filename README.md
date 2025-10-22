# La Odisea de Moya

Un juego de aventura RPG con elementos de juego de ritmo desarrollado en Java como proyecto final para la materia de Laboratorio y Programación Orientada a Objetos (LPOO).

## 📋 Descripción

"La Odisea de Moya" es un juego que combina exploración RPG con mecánicas de juego de ritmo. El jugador controla a un personaje que debe navegar por diferentes áreas, interactuar con NPCs, y enfrentarse a desafíos musicales para progresar en la historia.

## ✨ Características

- **Sistema de Exploración**: Navega por múltiples áreas del juego (planta alta, planta baja, taller, etc.)
- **Sistema de Interacción con NPCs**: Dialoga con varios personajes incluyendo:
  - Melody
  - Zambrana
  - Kreimer
  - Ricky
  - Linzalata
  - Y más...
- **Batallas de Ritmo**: Enfrenta a los NPCs en desafíos musicales donde debes presionar las teclas correctas al ritmo de la música
- **Sistema de Inventario**: Gestiona items similar al sistema de Minecraft (9 slots de hotbar + grid de 9x3)
- **Sistema de Colisiones**: Mapa de colisiones para navegación realista
- **Sistema de Teleportación**: Viaja entre diferentes plantas y áreas del juego
- **Música y Efectos de Sonido**: Banda sonora completa con temas para cada batalla
- **Diálogos Interactivos**: Sistema de conversaciones con opciones y respuestas

## 🎮 Controles

### Menú Principal
- **↑/↓**: Navegar entre opciones
- **Enter**: Seleccionar opción

### Exploración
- **W/↑**: Mover arriba
- **S/↓**: Mover abajo
- **A/←**: Mover izquierda
- **D/→**: Mover derecha
- **E**: Interactuar con NPCs/objetos
- **Q**: Cambiar de planta (cuando estés en zona de teleport)

### Batallas de Ritmo
- **D, F, J, K**: Presiona las teclas correspondientes al ritmo de la música cuando las notas lleguen a la línea de golpe

### Diálogos
- **Enter**: Avanzar diálogo / Confirmar selección
- **↑/↓**: Seleccionar opciones en diálogos con elecciones

## 🛠️ Requisitos

- **Java Development Kit (JDK)**: 8 o superior
- **IDE recomendado**: Eclipse (el proyecto incluye configuración de Eclipse)
- **Sistema Operativo**: Windows, Linux o macOS

## 📦 Instalación

1. Clona este repositorio:
```bash
git clone https://github.com/tomy08/juego-final-lpoo.git
cd juego-final-lpoo
```

2. Abre el proyecto en Eclipse:
   - File → Open Projects from File System
   - Selecciona la carpeta del proyecto
   - Click en "Finish"

3. Alternativamente, compila desde línea de comandos:
```bash
javac -d bin src/**/*.java
```

## 🚀 Cómo Ejecutar

### Desde Eclipse:
1. Abre el archivo `src/main/Main.java`
2. Click derecho → Run As → Java Application

### Desde línea de comandos:
```bash
java -cp bin main.Main
```

## 📁 Estructura del Proyecto

```
juego-final-lpoo/
├── src/
│   ├── main/              # Clases principales del juego
│   │   ├── Main.java
│   │   ├── GameWindow.java
│   │   ├── GamePanel.java
│   │   ├── MainMenu.java
│   │   └── ...
│   ├── entities/          # Entidades del juego (Player, NPCs, Items)
│   ├── Levels/            # Sistema de niveles y ritmo
│   ├── Mapa/              # Sistema de mapas y colisiones
│   └── Sonidos/           # Gestión de audio
├── resources/
│   ├── Sprites/           # Sprites del jugador, NPCs e items
│   ├── Music/             # Archivos de música (.wav)
│   ├── sounds/            # Efectos de sonido
│   ├── Levels/            # Archivos de configuración de niveles
│   ├── Collision_Maps/    # Mapas de colisión (.png)
│   └── font/              # Fuentes del juego
└── bin/                   # Archivos compilados (.class)
```

## 🎯 Objetivo del Juego

El objetivo principal es navegar por el edificio, interactuar con diferentes NPCs (profesores y compañeros), y completar desafíos musicales para desbloquear nuevas áreas. Deberás:

1. Hablar con Melody para que te ayude a convencer a su padre (Zambrana)
2. Ganar la batalla de ritmo contra Melody
3. Obtener acceso al Taller
4. Completar misiones adicionales (como conseguir comida sin TACC para Kreimer)
5. Explorar todas las áreas disponibles

## 🎵 Música

El juego incluye temas musicales originales para cada batalla:
- Tema de Melody
- Tema de Ricky
- Tema de Linzalata
- Tema de Moya
- Música de fondo

## 🏗️ Tecnologías Utilizadas

- **Java**: Lenguaje principal
- **Swing**: Para la interfaz gráfica
- **AWT**: Para renderizado de gráficos y manejo de eventos
- **Java Sound API**: Para reproducción de audio

## 👥 Desarrollado por

Proyecto final para la materia Laboratorio y Programación Orientada a Objetos (LPOO)

## 📄 Licencia

Este es un proyecto académico desarrollado con fines educativos.

---

**¡Disfruta jugando La Odisea de Moya!** 🎮🎵
