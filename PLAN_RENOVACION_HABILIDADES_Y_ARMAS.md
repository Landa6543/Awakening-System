# ⚔️ PLAN MAESTRO: RENOVACIÓN ESTÉTICA TOTAL DE HABILIDADES, VFX/SFX Y SISTEMA DE ARMAS V2
> **Documento de Especificación y Hoja de Ruta para Awakening System**  
> **Fecha de actualización:** 3 de Octubre de 2026  
> **Directiva Principal del Usuario:** *"Es para TODAS las armas, quiero cosas mucho más estéticas y cinemáticas, no cosas simples o básicas como están ahora (efectos de pociones vanilla y partículas aleatorias)."*

---

## 📋 RESUMEN EJECUTIVO

Actualmente, las armas del mod operan con una lógica técnica elemental:
* Al hacer clic derecho, aplican un `MobEffectInstance` vanilla (Fuerza, Velocidad, Absorción).
* Detectan entidades mediante `getEntitiesOfClass` en una esfera/cono invisible instantáneo.
* Emiten unas cuantas partículas vanilla (`CRIT`, `SMOKE`, `PORTAL`) dispersadas al azar.
* Al subir de nivel, disparan el sonido estándar estridente de XP (`PLAYER_LEVELUP`) e inundan el chat de texto.

**El Nuevo Paradigma V2:**  
Inspirándonos en la arquitectura visual de **Iron's Spells 'n Spellbooks, Mahou Tsukai y Ars Nouveau**, crearemos un **Motor de Habilidades Cinematográficas** aplicable a **TODAS LAS ARMAS** del mod. Cada arma poseerá identidad visual propia, proyectiles y ondas de corte físicas modeladas en 3D, arcos de corte luminosos (slash arcs), sacudidas de cámara e impactos contundentes.

---

## 🎯 SECCIÓN 1: AJUSTES INMEDIATOS DE SUBIDA DE NIVEL DE ARMAS

### 1.1. Sonido de Nivel de Arma (`WeaponLevelManager.java`)
* **Problema actual:**  
  Usa `SoundEvents.PLAYER_LEVELUP` y `SoundEvents.UI_TOAST_CHALLENGE_COMPLETE`. Es el sonido de subir nivel de jugador de Minecraft vainilla, estridente y molesto al matar hordas.
* **Solución V2:**
  - **Niveles ordinarios:** Resonancia mística de acero templado (`SoundEvents.ENCHANTMENT_TABLE_USE` a tono agudo $1.3F$ + `SoundEvents.EXPERIENCE_ORB_PICKUP` a tono $1.8F$ con volumen suave $0.4F$).
  - **Hitos Legendarios (Lv. 10, 25, 50, 75, 100):** Sonido de forja divina `SoundEvents.ANVIL_USE` (tono $1.6F$) acompañado de un acorde celestial y destello visual.
  - Registro de sonido personalizado: `awakening_system:weapon_resonate`.

### 1.2. Notificación en Pantalla (`WeaponLevelManager.java`)
* **Problema actual:**  
  `player.displayClientMessage(..., false);` inunda el chat de texto con líneas doradas.
* **Solución V2:**
  - **Action Bar (`true`):** Toda subida de nivel ordinaria aparecerá sutilmente sobre la hotbar:  
    `§6§l✦ [Elucidator] §eNivel 24/100 §7(+0.5 ATK)`
  - **Banners de Sistema (Title / Subtitle):** Únicamente en los hitos decisivos (Lv. 10, 25, 50, 75, 100), la pantalla mostrará un banner estilo Solo Leveling:  
    `§6§l✦ HITO ALCANZADO ✦`  
    `§eEl arma ha despertado un nuevo rasgo divino`

---

## 🔮 SECCIÓN 2: PILARES DE DISEÑO ESTÉTICO V2 (REFERENCIA MODS DE MAGIA)

| Pilar Estético | Cómo lo implementan los mejores mods | Cómo se aplicará a TODO el arsenal |
| :--- | :--- | :--- |
| **Entidades de Corte Físicas (`SlashWaveEntity`)** | *Iron's Spells* genera proyectiles móviles (`GeoEntityRenderer`) con mallas orientadas y transparencias. | Reemplazar daño invisible en cono por tajos voladores de energía que viajan, tienen volumen 3D y cortan a los enemigos a su paso. |
| **Cortes Dimensionales y Raycast** | *Mahou Tsukai* traza líneas matemáticas de corte exacto con planos luminosos que rajan el espacio. | Habilidades de estocada y cortes de vacío que dejan grietas en el aire antes de detonar. |
| **Impacto y Sacudida (`CameraShake`)** | Shakes controlados de cámara según la fuerza del impacto. | Micro-sacudida de pantalla ($0.15\text{s}$) al asestar habilidades definitivas para dar peso real al golpe. |
| **Zonas Telegrafiadas en Suelo** | *Ars Nouveau* proyecta runas y sellos mágicos en el terreno antes de la detonación. | Sellos luminosos en el suelo al invocar tempestades, prisiones de espinas o pilares ígneos. |
| **Hit-Stop y Micro-Pausa** | Juegos de acción estilo *Devil May Cry* / *Genshin Impact*. | Pausa imperceptible de 2 ticks ($0.1\text{s}$) en el objetivo alcanzado por un golpe crítico definitivo. |

---

## ⚔️ SECCIÓN 3: CATÁLOGO DE RENOVACIÓN ESTÉTICA POR ARMA (TODO EL ARSENAL)

### 3.1. 🗡️ Armas de Sword Art Online (SAO)
* **Elucidator — *Starburst Stream V2*:**
  - *VFX:* Postura cinemática donde el jugador realiza 16 tajos rápidos automáticos. Cada golpe dispara una **cuchilla de vacío negra con filo plateado** (`SlashWaveEntity`) que vuela 5 bloques hacia adelante.
  - *Finalizador:* El 16º golpe cruza ambas espadas en una **X titánica de fuego oscuro** que estalla hacia adelante, empujando a los enemigos contra bloques con sonido de impacto sónico y sacudida de pantalla.
* **Dark Repulser — *Crystalline Resonance & Dual Eclipse*:**
  - *VFX:* Al clavar la espada en el suelo, una hilera de **púas de cristal de dragón aguamarina** brota del terreno en línea recta empalando y congelando a los objetivos.
  - *Sinergia Dual:* Al empuñar Elucidator + Dark Repulser, clic derecho desata **[Dual Eclipse]**: Un vórtice de 360° combinando fuego de sombra negro y cristales de hielo esmeralda que atrae enemigos al centro y los despedaza.
* **Lambent Light — *Linear / Flashing Penetrator V2*:**
  - *VFX:* El jugador se desvanece en un **haz de luz estelar dorada y turquesa**, teletransportándose o embistiendo 12 bloques hacia adelante.
  - *Detonación:* Los enemigos atravesados sufren daño perforante que ignora el 50% de armadura. Tras 0.4s, la estela de luz residual en el aire estalla en una cascada de estocadas sónicas.

---

### 3.2. 🌸 Armas Murim & Cultivo Ki
* **Plum Blossom Sword (Espada de la Flor de Ciruelo — Mount Hua / Chung Myung):**
  - *VFX:* En vez de buffs invisibles, desata **[Danza de los Veinticuatro Pétalos]**: Un torbellino huracanado de pétalos de flor de ciruelo rosados y cuchillas de Qi en 360°.
  - *Estética:* Los pétalos giran en espiral alrededor del jugador mientras cortes curvos rosados cortan el aire con ráfagas de viento cortante.
* **Sky Demon Sword (Espada del Demonio Celestial):**
  - *VFX:* Desata **[Tajo del Dragón Demoniaco Carmesí]**: La espada se envuelve en llamas carmesí y negras, liberando una cabeza de dragón de Qi oscuro que viaja por el suelo levantando bloques de tierra y dejando un rastro ardiente.
* **Soul Solid Sword (Espada de Alma Sólida):**
  - *VFX:* **[Desgarro del Espíritu]**: Corte horizontal que libera una onda de choque etérea translúcida que atraviesa escudos y separa momentáneamente el "alma" de los enemigos, inmovilizándolos durante 2 segundos.

---

### 3.3. 👥 Armas de Solo Leveling / Cazadores
* **Shadow Daggers (Dagas de Sombra de Sung Jin-woo / Kamish):**
  - *VFX:* **[Paso de las Sombras & Degüello]**: El jugador se disuelve en humo de sombras negras y relámpagos púrpuras, apareciendo instantáneamente a la espalda del objetivo más cercano en 8 bloques.
  - *Estética:* Corte en cruz sangriento con estelas de sombras oscuras y ojos brillantes azules/rojos que parpadean en la pantalla.
* **Radiant Light Sword (Espada de Luz Radiante — Cha Hae-In):**
  - *VFX:* **[Danza de la Espada de Luz V2]**: El jugador ejecuta 3 estocadas de esgrima a la velocidad de la luz; cada estocada genera un haz de luz solar dorada que perfora enemigos y finaliza con un loto de luz divina en el suelo que cura a aliados y ciega a los enemigos.

---

### 3.4. 🌹 Armas Celestiales y Míticas
* **La Doncella que Contempló el Fin de las Rosas:**
  - *Forma Pura [Jardín del Silencio]:* Invoca una **corona de raíces y rosas plateadas** que brotan del suelo en un radio de 6 bloques, inmovilizando (root) a todos los enemigos y purificando cualquier estado negativo del portador.
  - *Forma Despierta [Trono de Ceniza / Luna Carmesí]:*
    - Tajo colosal de sangre carmesí de 10 bloques de longitud que rasga el aire.
    - Lluvia de pétalos negros y carmesíes ardientes que caen del cielo sobre el área afectada, drenando la sangre de los enemigos hacia el portador (robo de vida del 15%).
    - Neblina de cenizas y pétalos oscuros flotantes en el ambiente.
* **Creator Claymore (Mandoble del Creador):**
  - *VFX:* **[Fisura del Génesis Cósmico]**: Al blandir el mandoble, se abre una grieta dimensional cósmica en el aire con fondo de constelaciones y amatista cósmica. De la grieta caen 3 meteoros estelares que explotan en ondas de choque divinas.

---

### 3.5. 🔱 Armas de Genshin Impact & Fantasía
* **Staff of Homa (Báculo de Homa — Hu Tao):**
  - *VFX:* Desata un vuelo de **mariposas de fuego espectral carmesí** que orbitan al jugador. Al atacar, las mariposas se lanzan contra los enemigos explotando en pilares de fuego espiritual.
* **Wolf's Gravestone (Lápida del Lobo):**
  - *VFX:* **[Aullido del Viento Helado]**: Al golpear a un enemigo con menos del 30% de vida, se escucha el aullido del Lobo del Norte y se despliega una tormenta de ventisca y sangre en 8 bloques, otorgando +40% de ataque a todo el equipo.
* **Verdict (Veredicto):**
  - *VFX:* **[Juicio Geotécnico]**: Impacto pesado contra el suelo que resquebraja el terreno con esquirlas de roca y oro flotante, liberando una onda de choque sísmica.
* **Vortex Vanquisher (Perforanubes):**
  - *VFX:* Invoca una lanza de roca dorada que cae del cielo clavándose en el suelo y emitiendo pulsos de resonancia Geo que dañan y atraen a los monstruos.

---

## 🛠️ SECCIÓN 4: HOJA DE RUTA TÉCNICA PASO A PASO

```
[FASE 1: Audio & Action Bar]
  ├── Cambiar chat spam -> Action Bar (true) en WeaponLevelManager
  ├── Reemplazar sonido de XP -> Resonancia de acero templado
  └── Títulos épicos en pantalla en hitos (Lv. 10, 25, 50, 75, 100)
       │
[FASE 2: Infraestructura VFX & Proyectiles Físicos]
  ├── Crear entidad base de corte: SlashWaveEntity (3D render, velocidad, penetración)
  ├── Sistema de sacudida de cámara (CameraShakePacket)
  └── Motor de indicadores y sellos mágicos en el suelo (GroundDecal / MagicSeal)
       │
[FASE 3: Implementación por Bloques de Armas]
  ├── Bloque A: Armas de SAO (Elucidator, Dark Repulser, Lambent Light)
  ├── Bloque B: La Doncella (Jardín de Espinas & Trono de Ceniza)
  ├── Bloque C: Armas Murim & Cazadores (Flor de Ciruelo, Dagas de Sombra, Luz Radiante)
  └── Bloque D: Armas Míticas y de Genshin (Mandoble del Creador, Homa, Wolf's Gravestone)
```

---

> 📌 **Estado:** Plan consolidado para **TODAS LAS ARMAS** del mod. Todo preparado y documentado para comenzar la implementación cuando enciendas la PC.
