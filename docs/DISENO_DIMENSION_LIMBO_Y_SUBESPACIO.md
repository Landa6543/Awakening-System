# 🌑 Documento de Diseño: Dimensión del Limbo y Reforma del Subespacio

**Sistema de Muerte en Mazmorras, Cinemática de Despertar y Bloqueos de Seguridad Dimensional**  
*Awakening System — NeoForge 1.21.1*

---

## 📖 1. Visión General y Trasfondo (Lore)

En los mundos de cazadores y manhwas de despertar, las mazmorras y portales dimensionales no son simples cuevas: son **fracturas del espacio-tiempo**. Cuando un cazador cae abatido dentro de una mazmorra, puerta de cazador o en los pisos profundos del Abismo, la inestabilidad de la ruptura no permite que su alma regrese limpiamente al lecho del mundo mortal.

En lugar de reaparecer plácidamente en su cama o punto de reaparición del Overworld:
- **El cazador es arrastrado al Limbo:** Un plano dimensional intermedio, brumoso y silencioso, suspendido entre la existencia y el vacío absoluto.
- **La supervivencia es la única salida:** El cazador debe orientarse en este purgatorio etéreo y encontrar la fisura de retorno para reanudar su destino.

---

## 🎬 2. Cinemática Inmersiva de Despertar

Al materializarse en la dimensión del Limbo, el jugador no aparece de pie de forma instantánea. Se activa una breve cinemática de despertar en primera persona (3 a 4 segundos):

### A. Simulación de Párpados y Parpadeo (Eye Blinking Effect)
- **Fase Inicial (0.0s - 1.0s):** Pantalla en negro absoluto (`#000000`), con una ligera viñeta espectral azulada/ceniza.
- **Primer Parpadeo (1.0s - 1.8s):** Dos bandas horizontales oscuras que simulan párpados se abren un 30% con visión borrosa (desenfoque / efecto náusea leve) y vuelven a cerrarse con pesadez.
- **Segundo Parpadeo (1.8s - 2.8s):** Los párpados se abren un 70%, revelando la atmósfera desolada del Limbo, y parpadean rápidamente una vez más.
- **Apertura Definitiva (2.8s - 3.8s):** La visión se aclara al 100%, eliminando la viñeta y restaurando el HUD del Sistema.

### B. Movimiento Físico de Cámara ("Levantándose del Suelo")
- Al aparecer, la cámara del jugador inicia tumbada a ras de suelo (altura de ojos reducida a ~0.2 bloques, ángulo de inclinación lateral `Roll` de 20° a 30° y `Pitch` mirando ligeramente hacia arriba).
- Durante los 3.5 segundos de la cinemática:
  - El ángulo de roll se estabiliza suavemente a 0° mediante una curva cuadrática de interpolación (`easeInOutQuad`).
  - La altura de la cámara asciende de forma continua desde el suelo hasta la altura normal de los ojos (1.62 bloques), simulando el esfuerzo del cuerpo al apoyarse y ponerse de pie con dificultad.

### C. Paisaje Sonoro (Sound Design)
- **Exhalación de Agotamiento (`exhaustion_pant.ogg`):** Sonido de respiración pesada y entrecortada, una exhalación profunda de cansancio al recobrar el aliento.
- **Latido Cardíaco Atenuado (`heartbeat_slow.ogg`):** Latidos graves y lentos que aceleran gradualmente hasta recuperar el ritmo vital y extinguirse.
- **Susurro del Vacío:** Un viento sordo, etéreo y de baja frecuencia que ambienta el aislamiento del Limbo.

---

## 🔒 3. Restricción del Menú del Sistema: Candados Dimensionales

Al intentar escapar abriendo la interfaz del **Awakening System**:

```
+-------------------------------------------------------------------+
|  [AWAKENING SYSTEM]                        ESTADO DEL PORTADOR    |
+-------------------------------------------------------------------+
|  PORTALES DISPONIBLES:                                            |
|                                                                   |
|  [ 🔒 OVERWORLD (BLOQUEADO) ]      [ 🔒 NETHER (BLOQUEADO) ]      |
|  [ 🔒 TEYVAT (BLOQUEADO) ]         [ 🔒 ABYSS (BLOQUEADO) ]       |
|                                                                   |
|  ⚠ ALERTA DEL SISTEMA:                                            |
|  Frecuencia espacio-temporal colapsada en el Limbo.               |
|  Los conductos de teletransporte exterior están inhabilitados.    |
+-------------------------------------------------------------------+
```

### Reglas de Bloqueo:
1. **Icono de Candado (`🔒`):** Todos los botones de viaje interdimensional y teletransporte a sede de gremio muestran un icono de candado superpuesto con tinte rojizo/dorado apagado.
2. **Deshabilitación de Clic:** Los botones no ejecutan ningún paquete de teletransporte (`PacketDistributor.sendToServer(...)`). Al pulsarlos, se reproduce un sonido metálico sordo de traba (`UI_BUTTON_CLICK` con tono grave 0.5F).
3. **Notificación de Emergencia del Sistema:** Aparece un aviso emergente:
   > *«[SISTEMA: La densidad espectral del Limbo anula las coordenadas del portal. Busca la Brecha del Destino para regresar.]»*

---

## 📦 4. Reglas del Subespacio (Pocket Dimension) en el Limbo

El cazador cuenta con un espacio de bolsillo personal (Subespacio) que suele albergar cofres, mesas de alquimia y refugio. Para evitar que el jugador evada el desafío del Limbo escondiéndose en su dimensión privada:

1. **Acceso al Almacén Remoto (Permitido):**
   - El cazador **SÍ** puede interactuar con el inventario o baúl de almacenamiento del subespacio desde el menú para extraer herramientas, antorchas, pociones o armas que necesite para sobrevivir en el Limbo.
2. **Entrada Física en Persona (Terminantemente Prohibida):**
   - El botón para "Entrar al Subespacio" físicamente se bloquea con el candado `🔒`.
   - Lore: *El alma debilitada del cazador no posee la cohesión dimensional necesaria para trasladar su cuerpo al espacio de bolsillo sin destruirlo.*

---

## 🌌 5. Reforma del Subespacio: Vacío Puro sin Bedrock

Para dar al Subespacio una estética y naturaleza puramente cósmica y de alto riesgo:

```
[ PLATAFORMA DE CONSTRUCCIÓN DEL CAZADOR ] (Y=64 a Y=120)
                  |
         [ AIRE / VACÍO CÓSMICO ]
                  |
     (Y=0) -- ¡SIN BEDROCK! -- (Piedra Base Eliminada)
                  |
    [ EL VACÍO INFERNAL / THE VOID (Y < -64) ]
   ☠ CAÍDA = MUERTE INSTANTÁNEA POR VACÍO ☠
```

### Características Técnicas:
- **Generador de Chunk Vacío (`EmptyVoidChunkGenerator`):**
  - La dimensión del subespacio no genera suelo natural de Bedrock a nivel Y=0 ni en Y=-64.
  - La dimensión es un espacio de bloques flotantes en un vacío estelar puro.
- **Peligro de Caída Letal:**
  - Si el jugador resbala o cae fuera de los bordes construidos de su subespacio, caerá directamente a través de `Y < -64` al vacío (`damageSources().fellOutOfWorld()`), muriendo sin posibilidad de salvación en el lecho rocoso.

---

## 🗺️ 6. Mecánica de Escape del Limbo

Para volver al mundo de los vivos, el jugador deberá interactuar con la dimensión del Limbo:

1. **Atmósfera y Paisaje:** Terreno espectral flotante con cenizas de almas, niebla espesa y pilares antiguos de piedra oscura.
2. **El Altar del Retorno (The Returning Rift):**
   - Una estructura central en el Limbo protegida por sombras errantes o que requiere encender 3 faros espectrales con fragmentos de alma.
   - Al activar el Altar, se estabiliza una grieta luminosa que transporta al cazador de vuelta a su punto de reaparición original en el Overworld, desbloqueando nuevamente todas las funciones del Sistema.

---

## 🛠️ 7. Plan de Implementación de Componentes en Código

| Componente | Paquete / Clase Propuesta | Responsabilidad |
|---|---|---|
| **Dimensión Limbo** | `com.soybe.awakeningsystem.world.limbo` | Registro de dimensión, bioma brumoso y generador de chunks flotantes. |
| **Generador Subespacio** | `com.soybe.awakeningsystem.world.subspace` | Generador de vacío puro sin capa de Bedrock. |
| **Listener de Muerte** | `com.soybe.awakeningsystem.event.LimboDeathHandler` | Intercepta muertes en mazmorras y redirige el respawn a la dimensión Limbo. |
| **Cinemática de Despertar** | `com.soybe.awakeningsystem.client.LimboCinematicRenderer` | Control de cámara (Roll/Pitch/Y), overlay de párpados y audio de cansancio. |
| **Bloqueo UI de Portales** | `com.soybe.awakeningsystem.SystemScreen` | Detección de dimensión Limbo, renderizado de candados `🔒` e inhibición de clics. |
| **Sonidos de Despertar** | `ModSounds` (`exhaustion_pant`, `heartbeat_slow`) | Archivos `.ogg` y registros de sonido para la cinemática. |
