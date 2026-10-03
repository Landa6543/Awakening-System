# ⚔️ PLAN MAESTRO: RENOVACIÓN DE HABILIDADES, VFX/SFX Y SISTEMA DE ARMAS V2
> **Documento de Especificación y Hoja de Ruta para Awakening System**  
> **Fecha de creación:** 3 de Octubre de 2026  
> **Objetivo:** Transformar las habilidades básicas de armas en experiencias dinámicas, visualmente espectaculares e inmersivas inspiradas en la arquitectura de los mejores mods de magia (*Iron's Spells 'n Spellbooks, Ars Nouveau, Mahou Tsukai*).

---

## 📋 RESUMEN EJECUTIVO

Las habilidades actuales de las armas del mod operan bajo mecánicas elementales: detección por radio `getEntitiesOfClass`, aplicación de `MobEffectInstance` vanilla y partículas de partículas simples.  
Este plan detalla:
1. **Solución inmediata al sonido y mensaje de subida de nivel de armas** (eliminar spam de chat y sonido estridente de XP).
2. **Análisis técnico y lecciones de arquitectura de mods de magia consagrados**.
3. **Catálogo de habilidades V2 para armas míticas y de SAO** (proyectiles de vacío, cortes cinemáticos, embestidas estelares, prisiones de espinas).
4. **Infraestructura de renderizado: VFX, Slash Arcs, Shakes de pantalla y Raycasting**.

---

## 🎯 SECCIÓN 1: AJUSTES INMEDIATOS DE SUBIDA DE NIVEL DE ARMAS

### 1.1. Sonido de Nivel de Arma (`WeaponLevelManager.java`)
* **Problema actual:**  
  Usa `SoundEvents.PLAYER_LEVELUP` y `SoundEvents.UI_TOAST_CHALLENGE_COMPLETE`. Es el sonido de nivel de jugador vainilla de Minecraft, fuerte y estridente cada vez que se derrota un mob.
* **Solución propuesta:**
  - Reemplazar por un sonido de **resonancia mística de acero**:
    - `SoundEvents.ENCHANTMENT_TABLE_USE` (tono $1.3F$) + `SoundEvents.EXPERIENCE_ORB_PICKUP` (tono $1.8F$, volumen bajo $0.4F$) para niveles ordinarios.
    - Para **Hitos legendarios** (niveles 10, 25, 50, 75, 100): Sonido de forja divina `SoundEvents.ANVIL_USE` (tono $1.6F$) + destello místico.
    - Opcionalmente registrar un evento sonoro personalizado: `awakening_system:weapon_resonate`.

### 1.2. Mensaje en el Chat (`WeaponLevelManager.java`)
* **Problema actual:**  
  `player.displayClientMessage(..., false);` imprime un mensaje dorado en el registro de chat cada vez que el arma sube de nivel, inundando el chat durante combates con hordas.
* **Solución propuesta:**
  - Cambiar el segundo argumento a `true`:  
    `player.displayClientMessage(Component.literal("§6§l✦ " + weaponName + " §eLv. " + currentLevel + "§7/100"), true);`  
    Esto envía la notificación de forma limpia a la **Action Bar** (sobre la hotbar), sin ensuciar el chat.
  - Para **Hitos Decisivos** (Lv. 50, 75, 100): Mostrar un banner de Sistema en pantalla con título grande (`Title / Subtitle`) de color dorado/amatista:  
    `[DESPERTAR DE ARMA: TRASCENDENCIA]`.

---

## 🔮 SECCIÓN 2: ANÁLISIS DE MODS DE MAGIA Y ARQUITECTURA TÉCNICA

| Mod de Referencia | Mecánicas Clave | Lo que debemos adaptar a Awakening System |
| :--- | :--- | :--- |
| **Iron's Spells 'n Spellbooks** | Proyectiles con entidades personalizadas (`AbstractSpellEntity`), `GeoEntityRenderer`, partículas con vectores de arrastre, tiempo de canalización (cast time). | Reemplazar daño instantáneo en cono por entidades de corte (`SlashWaveEntity`) y proyectiles físicos con trayectorias visibles. |
| **Mahou Tsukai** | Cortes dimensionales, rayos continuos por raycast (`ClipContext`), círculos mágicos en el plano de corte, retroceso cinemático. | Habilidades de estocada y cortes de vacío que atraviesan enemigos y cortan el espacio-tiempo. |
| **Ars Nouveau** | Encadenamiento de efectos, zonas de suelo telegrafiadas (runas en el suelo antes de la explosión). | Indicadores de área en el suelo para habilidades definitivas (como el Trono de Ceniza). |
| **Epic Fight / SlashBlade** | Mallas de corte en arco (Slash Arcs) con texturas translúcidas que siguen la curva del golpe. | Arcos de corte visibles en el aire al activar las habilidades de armas de espada. |

---

## ⚔️ SECCIÓN 3: CATÁLOGO DE HABILIDADES V2 PARA ARMAS

### 3.1. 🗡️ Elucidator (Kirito) — *Starburst Stream V2*
* **Mecánica Antigua:** Otorga Fuerza III temporal y empuje simple.
* **Mecánica V2:**
  1. **Secuencia de 16 Cortes:** Al hacer clic derecho, el jugador entra en postura de asalto. Durante 1.8 segundos, el arma ejecuta ráfagas de cortes rápidos automáticos.
  2. **Proyectiles de Vacío (Shadow Blades):** Cada corte emite una cuchilla de energía negra y plata (`SlashWaveEntity`) que viaja 5 bloques hacia adelante, dañando y ralentizando enemigos.
  3. **16º Golpe Cruzado (Cross Burst):** El golpe final desata una onda expansiva en cruz (forma de **X**) de fuego oscuro que inflige daño crítico masivo y empuja a los enemigos contra las paredes.
  4. **SFX:** Sonido de zumbido de acero sónico por cada golpe y explosión de corte final.

### 3.2. 💎 Dark Repulser (Kirito) — *Crystalline Resonance & Ice Dragon Slash*
* **Mecánica Antigua:** Buffs de velocidad y absorción con sinergia pasiva estática.
* **Mecánica V2:**
  1. **Púas de Cristal del Dragón:** El jugador clava o barre la espada hacia adelante; una hilera de cristales de aguamarina (`CrystalSpikeEntity`) brota del suelo en línea recta, empalando y congelando a los objetivos durante 2 segundos.
  2. **Sinergia Activa con Elucidator (Dual Wielding):**
     - Si se empuña Elucidator en mano principal y Dark Repulser en secundaria (o viceversa):
     - La habilidad se transforma en **[Dual Eclipse]**: Una danza torbellino de 360° combinando fuego de sombra negro y cristales de hielo esmeralda, atrayendo a los enemigos hacia el centro y despedazándolos.

### 3.3. ✨ Lambent Light (Asuna) — *Linear / Flashing Penetrator V2*
* **Mecánica Antigua:** Daño en línea y salto básico.
* **Mecánica V2:**
  1. **Embestida a la Velocidad de la Luz (Light Dash):** El jugador se desvanece en un haz de luz dorada y turquesa, teletransportándose o avanzando 12 bloques instantáneamente.
  2. **Línea de Estocada Perforante:** Todos los enemigos en la trayectoria son atravesados (daño que ignora el 50% de la armadura).
  3. **Detonación de Brillo:** Tras 0.5s de la embestida, la estela de luz que quedó en el aire detona en múltiples explosiones de estocada sonora.

### 3.4. 🌹 La Doncella que Contempló el Fin de las Rosas — *Trono de Ceniza V2*
* **Mecánica Antigua:** Potion effects con buffs de stats y empuje genérico.
* **Mecánica V2:**
  1. **Forma Pura — [Jardín del Silencio]:** Al hacer clic derecho, invoca una corona de raíces y rosas plateadas en el suelo que enraízan a todos los enemigos cercanos y purifica al portador.
  2. **Forma Despierta — [Trono de Ceniza / Luna Carmesí]:**
     - La espada genera un tajo horizontal titánico de sangre carmesí de 10 bloques de alcance.
     - Lluvia de pétalos negros ardientes que caen del cielo sobre el área afectada, provocando marchitamiento y drenando vida hacia el jugador (robo de vida del 15% del daño infligido).
  3. **Efecto de Niebla de Ceniza:** La zona se cubre momentáneamente de una neblina oscura con partículas de ceniza y pétalos carmesíes flotantes.

---

## 🛠️ SECCIÓN 4: HOJA DE RUTA DE IMPLEMENTACIÓN TÉCNICA

### Fase 1: Limpieza de HUD y Audio (Prioridad Inmediata)
- [ ] Modificar `WeaponLevelManager.java`:
  - Cambiar notificación de chat a Action Bar (`true`).
  - Cambiar sonido de `SoundEvents.PLAYER_LEVELUP` a resonancia de acero templado (`ENCHANTMENT_TABLE_USE` + tono agudo).
  - Agregar banner en pantalla para hitos importantes (Lv. 10, 25, 50, 75, 100).

### Fase 2: Motor de Proyectiles y Cortes Físicos
- [ ] Crear clase base de entidad de corte: `com.soybe.awakeningsystem.entity.projectile.SlashWaveEntity`:
  - Entidad móvil orientable con hitbox rectangular (caja de colisión OBB/AABB).
  - Renderizado 3D de quad con textura brillante y aditiva (hoja de vacío, cristal, tajo de sangre).
  - Detección de impacto que penetra o se detiene al impactar contra bloques.

### Fase 3: Animaciones de Habilidad y Shakes de Cámara
- [ ] Implementar `CameraShakePacket` para dar impacto físico a los golpes definitivos (estilo Solo Leveling / Manhwa).
- [ ] Conectar partículas direccionales generadas mediante raycast exacto en vez de dispersión aleatoria.

### Fase 4: Habilidades Específicas de SAO y La Doncella
- [ ] Programar *Starburst Stream V2* en `ElucidatorItem.java`.
- [ ] Programar *Crystalline Resonance* y combo dual en `DarkRepulserItem.java`.
- [ ] Programar *Flashing Penetrator* en `LambentLightItem.java`.
- [ ] Programar *Trono de Ceniza V2* en `LaDoncellaSwordItem.java`.

---

> 📌 **Estado:** Lista consolidada y lista para ejecución cuando reanudes tus sesiones de trabajo.
