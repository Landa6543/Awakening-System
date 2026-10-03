# Guía de Instalación: Awakening: Monarch's Ascension (~250+ Mods)
**Versión de Minecraft:** `1.21.1`  
**Cargador (ModLoader):** `NeoForge 21.1.65` o superior  
**RAM Recomendada:** `6 GB - 8 GB` (Con 6 GB funciona de maravilla gracias a FerriteCore y ModernFix)

---

## 🚀 Método 1: CurseForge App (El más fácil y rápido)

1. Abre la aplicación de **CurseForge**.
2. En la pestaña de **Minecraft**, haz clic en el botón superior **"Crear perfil personalizado"** (Create Custom Profile) o en los 3 puntos `...` arriba a la derecha.
3. Selecciona **"Importar"** (Import).
4. Elige el archivo comprimido **`Awakening_Monarchs_Ascension_v1.0.0.zip`** que hemos creado en esta carpeta.
5. CurseForge descargará automáticamente todos los mods del manifiesto y colocará tu mod `awakening_system-0.1.0.jar` junto con las configuraciones optimizadas en la carpeta del juego.
6. ¡Haz clic en **Jugar**!

---

## ⚡ Método 2: Prism Launcher / Modrinth App

1. Abre **Prism Launcher**.
2. Haz clic en **"Añadir Instancia"** (Add Instance).
3. En el menú izquierdo, selecciona **"Importar"** (Import from zip).
4. Selecciona **`Awakening_Monarchs_Ascension_v1.0.0.zip`**.
5. Prism detectará automáticamente NeoForge 1.21.1 y descargará los mods en segundos.
6. Ajusta la memoria en la configuración de la instancia a mínimo `6144 MB` (6 GB).
7. ¡Listo para jugar!

---

## 🛠️ Método 3: Minecraft Launcher Oficial / Instalación Manual

1. Descarga e instala el instalador oficial de **NeoForge 1.21.1** (versión `21.1.65` o más reciente) desde [neoforged.net](https://neoforged.net/).
2. Presiona `Windows + R`, escribe `%appdata%\.minecraft` y pulsa Enter.
3. Copia el contenido de la carpeta `overrides/`:
   - Copia la carpeta `overrides/mods/` en tu `.minecraft/mods/`.
   - Copia la carpeta `overrides/config/` en tu `.minecraft/config/`.
   - Copia `overrides/options.txt` en tu `.minecraft/options.txt` para tener los controles sin colisiones.
4. Si necesitas descargar los mods adicionales de una sola vez, ejecuta el archivo **`Descargar_Mods_Automatico.bat`** incluido en este paquete.
5. Abre el Launcher oficial, selecciona el perfil de NeoForge 1.21.1, asigna `-Xmx6G` o `-Xmx8G` en las opciones avanzadas de Java y lanza el juego.

---

## 🎮 Distribución de Controles (Preconfigurados sin conflictos)

Para garantizar que disfrutes del sistema de combate y habilidades de Solo Leveling sin interrupciones, las teclas principales han sido reservadas:

| Tecla | Función en Awakening System |
| :--- | :--- |
| **`K`** | **Menú de Estado del Cazador** (Atributos, Rangos, Casa de Subastas, Tienda Dokkaebi) |
| **`V`** | **Árbol de Habilidades de Cazador** (Subida de nivel de habilidades activas y pasivas) |
| **`R`** | **Extracción de Sombras ("Surge / Arise")** sobre cadáveres de monstruos derrotados |
| **`G`** | **Rueda Táctica de Sombras** (Comandos: Seguir, Atacar, Modo Sigilo, Guardar) |
| **`B`** | **Habilidad Definitiva / Despertar** |
| **`Z` / `X` / `C`** | **Ranuras de Habilidades Rápidas de Cazador** |
| **`;`** *(Punto y coma)* | Abrir Mochila Sofisticada (Reubicado para liberar `B`) |
| **`I`** | Abrir menú de Accesorios Curios / Reliquias (Reubicado para liberar `V`) |
| **`M`** | Mapa a pantalla completa de Xaero / FTB |
| **`U`** | Crear / gestionar Waypoints en el mapa |

---

## 🛡️ Sistema de Gremios, Misiones y Territorio (FTB Suite)

El modpack incluye la suite oficial de **FTB** integrada con Awakening System para juego individual o en cooperativo:

1. **Creación de Gremios (FTB Teams):**
   - Abre tu inventario y haz clic en el icono de miembros de equipo (arriba a la izquierda) o escribe en el chat `/ftbteams party create "NombreDeTuGremio"`.
   - Puedes invitar a otros cazadores con `/ftbteams party invite <Jugador>`.
   - Los miembros del gremio comparten el progreso del libro de misiones y acceso a las fortalezas protegidas.

2. **Reclamación de Territorio y Mapa (FTB Chunks):**
   - Haz clic en el icono de mapa en la esquina superior izquierda del inventario.
   - Haz clic izquierdo sobre los chunks para **reclamarlos (Claim)** a nombre de tu gremio. Esto activa la protección anti-griefing y anti-explosiones.
   - Presiona `Shift + Clic Izquierdo` sobre un chunk para **mantenerlo siempre cargado (Forceload)**, de forma que tus fábricas de Create y hornos sigan operando mientras exploras mazmorras lejanas.

3. **Libro de Misiones (FTB Quests):**
   - Accede a las misiones haciendo clic en el icono del libro en el inventario.
   - Guía la progresión de tu Cazador: despertar inicial, ascensión de rango E -> S, doma de bestias míticas de Mo' Creatures, caza de dragones de Ice and Fire y asalto a las ciudadelas de Cataclysm.

---

## 💡 Consejos de Rendimiento para 120+ FPS

1. **Shaders:** Si deseas usar Shaders cinemáticos, el modpack incluye **Iris Shaders** y **Sodium** nativos para NeoForge 1.21.1. Puedes activar shaders como *Complementary Reimagined*, *BSL* o *MakeUp UltraFast* directamente en `Opciones -> Gráficos -> Shaders`.
2. **Distancia de Renderizado:** Recomendamos entre `12` y `16` chunks. Con **ImmediatelyFast** y **Entity Culling**, el juego mantendrá 120+ FPS incluso dentro de mazmorras colosales de Cataclysm o fortalezas de YUNG's.
3. **Apilado con BigStacks:** Los minerales, materiales de crafteo y botín de mazmorras ahora se apilan hasta **1024**, permitiendo explorar durante horas sin llenar el inventario.
