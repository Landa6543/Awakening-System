# ⚡ Awakening System — Version 0.3.5
### *Deadlock Tombstone, Interactive NPC Dialogue Engine & Hunter Evaluation Overhaul*

* **Minecraft Version:** 1.21.1
* **Mod Loader:** NeoForge 21.1.249+
* **Java:** Java 21 LTS

---

## 💬 Motor de Diálogos Cinematográficos RPG (Inspirado en Saints & Dragons)
Implementación integral de un sistema de diálogos interactivos cinematográficos con diseño holográfico de *Solo Leveling*:
* **Giro Cinemático de Cámara (`beginCameraTurnToSpeaker`):** Al iniciar conversación con un NPC, la cámara del jugador rota suavemente hacia el rostro del interlocutor mediante interpolación cúbica fluida (`easeInOutCubic`, 260ms) sin pausar el juego ni distorsionar los shaders del mundo.
* **Aparición Deslizante de Interfaz (`getBoxY`):** El panel de diálogo surge suavemente desde el borde inferior de la pantalla (`easeOutCubic`, 250ms) con cristal obsidiana oscuro, marco cian resplandeciente (`#00e5ff`) y placa del hablante.
* **Efecto Máquina de Escribir y Blips de Voz (`playVoiceBlipForNewText`):**
  * El texto se revela carácter a carácter con velocidad fluida (24ms por letra).
  * Emisión de efectos de sonido sintetizados y modulados en tono aleatorio (1.82 - 2.08) por carácter ignorando espacios y puntuación, creando la sensación de una consola de RPG.
  * Posibilidad de saltar instantáneamente la animación presionando `Espacio`, `Enter` o haciendo clic.
* **Opciones y Ramificación Interactiva:**
  * Opciones flotantes con fondo dinámico, borde cian al pasar el ratón (`hover`), prefijo en flecha dorada (`▶`) y soporte para desplazamiento con rueda de ratón (`mouseScrolled`).
  * Protocolo de red nativo cliente/servidor (`OpenDialoguePayload`, `SelectDialogueChoicePayload`, `CloseDialoguePayload`).

---

## 🎖️ NPC Evaluador de la Asociación (Woo Jin-Chul) & Ascenso de Rango
* **Entidad `hunter_evaluator`:** Inspector oficial de la Asociación de Cazadores con modelo bípedo y traje formal.
* **Evaluación de Maná Oficial:**
  * Detecta si el jugador ha llegado al límite de nivel de su rango (`isEvaluationPending`).
  * Al iniciar la evaluación, el cristal reacciona con sonidos de fanfarria (`UI_TOAST_CHALLENGE_COMPLETE`, `PLAYER_LEVELUP`) y partículas místicas.
  * Otorga +5 Puntos de Atributo permanentes y libera toda la experiencia acumulada en reserva (`reserveExperience`).
* **Tabla de Rangos y Consulta:**
  * Menú categorizado en 5 Tiers existenciales desde Mortales hasta Dioses Absolutos (Nivel 2000).
  * Opciones visualmente depuradas sin paréntesis desbordantes ni duplicación de números.
* **Sonido Táctil de Tecleo:**
  * Restaurado el sonido rítmico de máquina de escribir / consola (`playVoiceBlipForNewText`) al revelarse el texto carácter por carácter.
  * Silenciado el sonido genérico de interacción repetitiva.
* **Venta del Rastreador de la Asociación:** Posibilidad de adquirir el `hunter_association_tracker` por 100 Monedas directamente desde el diálogo.
* **Comando y Huevo Generador:**
  * Comando `/system summon_evaluator` para invocar al Inspector Woo Jin-Chul en la posición actual.
  * Objeto `hunter_evaluator_spawn_egg` registrado en pestaña creativa.

---

## ☄️ Meteoritos de Siderita & Cráter de Impacto Tridimensional Real
* **Entidad Cinemática `FallingSideriteMeteorEntity`:**
  * Alerta celestial en chat al entrar el bólido en la atmósfera terrestre (`awakening_system.announcement.meteor_entry`).
  * Estela luminosa de fuego y humo denso cayendo a gran velocidad desde la estratosfera.
  * Mensaje de coordenadas exactas del impacto con sonido de choque sísmico masivo (`meteor_impact`).
* **Excavación de Cráter Real 3D (No Parche Plano):**
  * Cavidad semiesférica cóncava natural con radio de 6.5 bloques y 4 bloques de profundidad real.
  * **Núcleo y Corteza:** Núcleo de Bloques de Siderita (`siderite_block`) y Mineral de Siderita (`siderite_ore`), rodeado por una corteza fundida de obsidiana llorosa, bloques de magma incandescente, basalto liso y deepslate empedrado.
  * **Reborde Elevado (*Ejecta Lip Rim*):** Elevación de tierra y rocas expulsadas en los bordes con ascuas encendidas.

---

## 🗡️ Armamento de Siderita & Daga "Colmillo Envenenado de Kasaka"
* **Colmillo Envenenado de Kasaka (`kasakas_venom_fang`):**
  * Daga icónica de *Solo Leveling* implementada con proporciones auténticas de daga (no alargada como espada).
  * Posicionamiento y rotación sincronizados con la **Daga de Sombras** (`shadow_daggers` a 55°), apuntando naturalmente hacia adelante.
* **Hacha-Martillo de Siderita Meteórica (`siderite_hamaxe`):**
  * Textura reorientada con el filo del hacha cortando hacia el frente y la cabeza del martillo con ascuas hacia atrás.
  * Sostén ergonómico natural por el mango inferior alineado con la mano del jugador.
* **Pico de Siderita Meteórica (`siderite_pickaxe`):**
  * Eliminada la rotación errónea invertida de 145°. Ahora utiliza la orientación canónica de herramientas handheld de Minecraft picando hacia adelante.
* **Limpieza de Auras Visibles:**
  * Eliminada la columna invasiva de partículas de fuego celeste (`SOUL_FIRE_FLAME`) que tapaba la visión al empuñar armas comunes.

---

## ⚖️ Reescalado de Drops RPG a 2000 Niveles y Separación de Dimensiones
* **Separación Estricta por Dimensiones:**
  * Las criaturas del Overworld nunca soltarán materiales ni armaduras de reinos superiores (Mithril, Adamantita, Jade Espiritual, etc.).
* **Escala Balanceada de Botín:**
  * **Nv. 1 – 59:** Cuero, Malla y Hierro.
  * **Nv. 60 – 119:** Hierro, Oro y Diamante.
  * **Nv. 120 – 199:** Diamante, Netherite y Siderita Meteórica (armadura, espada y hacha-martillo).
  * **Nv. 200 – 299:** Netherite, Siderita y Forja de Almas / Soul Alloy.
  * **Nv. 300 – 399:** Mithril y Adamantita.
  * **Nv. 400 – 500+:** Adamantita, Jade Espiritual, Forja de Almas y Astralita.
  * Progresión extendida para Deidades y Creadores Supremos hasta Nivel 2000.
* **Compatibilidad con Criaturas y Dragones de Ice and Fire:**
  * Dragones y bestias de Ice and Fire (`iceandfire:`) ahora reciben nivelación RPG (`[Nv. X]`), escalado de atributos y recompensas.
  * Detección y rastreo en el Minimapa HUD (`HunterMinimapRenderer`).

---

## 🌐 100% Paridad de Traducciones y Corrección de Codificación UTF-8
* **Eliminación Total de Mojibake:**
  * Archivos de localización regenerados con UTF-8 puro sin BOM ni caracteres corruptos (`Ã³`, `Ã­`, `Ã¡`).
* **Traducciones Nuevas Completas:**
  * Casco, pechera, pantalones, botas, espada, hacha-martillo, pico, lingote, fragmento y mineral de siderita meteórica.
  * Bonus del conjunto de armadura de siderita meteórica (`awakening_system.armor_set.siderite.header` y `desc`).
  * Manzana de Sombras (`shadow_apple`), Zanahoria de Qi Espiritual (`spirit_qi_carrot`), Rastreador de la Asociación (`hunter_association_tracker`) y diálogos completos del Inspector.
  * Paridad en 14 archivos de idioma (Español de todas las regiones, Inglés, Francés, Alemán, Portugués y Ruso).

---

## 🪙 Corrección de Textura de Monedas en HUD
* **Restauración del Ícono de Moneda:** En el pie de la tarjeta del HUD (`SystemHudOverlay`), se reemplazó el carácter unicode genérico `✦` por el renderizado a escala del ítem real `awakening_coin` (`SystemItems.coin(1)` a escala 0.55x).
* **Visualización de Monedas Garantizada:** Se corrigió la condición que reemplazaba la visualización de monedas por protogemas en Sistema 4.0, garantizando que el dinero del cazador siempre se aprecie con su textura dorada oficial y formato numérico estilizado (`§e`).

---

## ⚰️ Sistema de Lápidas (Deadlock Tombstone)
Reemplazo total de la antigua entidad de muerte por un **bloque físico animado con GeckoLib 4**, solucionando desincronizaciones de colisión, problemas de traspaso y bugs visuales:
* **Colisión Física Monumental:** Implementado como bloque sólido (`TombstoneBlock`) con `VoxelShape` de 1.9 bloques de altura, impidiendo que el jugador u otras entidades lo atraviesen.
* **Textura del Sistema Limpia y 100% Sin Residuos:** Remapeo espectral completo de la textura original eliminando todos los tonos oliva/verdosos del musgo en la base y grietas (`Hue 44.0° - 55.9°`), transformándolos en cian cósmico y pizarra celeste armónica con la piedra.
* **Resguardo Absoluto del Jugador al Morir:**
  * **Inventario Completo (41 ranuras):** 36 ranuras principales/hotbar, 4 de armadura y 1 de segunda mano (`offhand`).
  * **Accesorios Curios API:** Serialización profunda de todos los talismanes y amuletos equipados en los slots de Curios.
  * **Experiencia Acumulada:** Preservación exacta del nivel de experiencia y los puntos porcentuales de progreso.
* **Seguridad y Propiedad:** Solo el legítimo propietario de la tumba (o administradores en creativo) pueden reclamarla, evitando saqueos por otros jugadores en multijugador.
* **Animaciones GeckoLib 4 Fluidas y Sincronizadas:**
  * `spawn` (2.25s / 45 ticks): Erupción violenta de la tumba desde el suelo al momento de la muerte.
  * `idle` (Cíclica): Resonancia espectral constante con almas flotantes estabilizadas.
  * `breakdown` (1.68s / 33 ticks): Desmoronamiento y colapso de la piedra al interactuar para reclamar las pertenencias.
* **Diseño Exclusivo del Evento de Muerte:** Retirado del menú creativo (`ModCreativeTabs`, `SystemItems`) para evitar la colocación manual descontextualizada.

---

## 👻 Modo Fantasma Espectral (Ghost Mode)
Al morir, el cazador entra en una fase incorpórea para regresar por sus pertenencias de forma inmersiva y segura:
* **Activación Fiable en Respawn:** Corrección del ciclo de vida en `PlayerEvent.Clone`, garantizando que las etiquetas de tumba y estado pasen del jugador muerto al clon reaparecido sin pérdida de datos.
* **Renderizado Translúcido en 3ra y 1ra Persona:**
  * **Tercera Persona (F5):** El modelo de la skin del jugador se procesa mediante `RenderType.entityTranslucent` con un resplandor etéreo cian-celeste (`0x7B8CDDFF`, ~48% de alfa), mostrando un cuerpo fantasmal semitransparente.
  * **Primera Persona:** Implementado `onRenderArm` (`RenderArmEvent`) para que los brazos y mangas del jugador se rendericen con la misma translucidez espectral en primera persona.
* **Inmunidad y Protección Física:** En este estado, los monstruos y peligros ambientales no pueden dañarte mientras viajas de vuelta a tu tumba.
* **Orientación y Guía del Sistema:** Notificaciones detalladas en el chat con las coordenadas exactas (X, Y, Z) y dimensión de la tumba, junto con un indicador en pantalla de distancia en bloques.

---

## 🔊 Paisaje Sonoro 3D Inmersivo (Audio Espacial)
Integración de 3 nuevos efectos de audio personalizados con motor OpenAL nativo:
* **Audio Mono Posicional (1 Canal Vorbis):** Todos los archivos de audio se optimizaron a formato MONO, permitiendo que Minecraft calcule la atenuación volumétrica por distancia y el balance estéreo direccional (izq/der).
* **`tombstone_spawn`:** Crujido y rugido telúrico al erigirse la lápida al morir (atenuación a 16 bloques, volumen moderado).
* **`tombstone_idle`:** Murmullo espectral en bucle suave que solo se activa cuando el jugador está a menos de 8 bloques de su tumba (atenuación a 10 bloques), sirviendo de faro sonoro sin contaminar el entorno.
* **`tombstone_breakdown`:** Resonancia y fractura de almas al momento de romper la lápida y recuperar las pertenencias.
* **Subtítulos Accesibles:** Registrados en inglés (`en_us`) y español (`es_es`, `es_cl`).

---

## 🗺️ Integración con Diorama Map
* **Marcador de Muerte en Español:** Generación automática del waypoint en el mapa voxel con nomenclatura localizada (`Muerte [Día]`).
* **Autolimpieza de Waypoints:** El marcador se elimina automáticamente de la memoria y del cliente en cuanto el jugador reclama su lápida, evitando acumulación de marcas viejas.

---

## 🛡️ Gremios y Alianzas: Inmunidad contra Fuego Amigo de Sombras
* **Inmunidad Estricta de Fuego Amigo en Ejército de Sombras:**
  * **Protección Cruzada Integral:** Los miembros de un mismo gremio y de hermandades aliadas ya no pueden dañarse entre sí, ni dañar a las sombras (`shadow_soldier`) invocadas por sus compañeros.
  * **Anulación Total de Ataques (`event.setCanceled(true)` & `newDamage = 0.0F`):** Golpes directos, flechas, habilidades mágicas, armas y ataques en área (AoE) infligidos accidentalmente a aliados o sus tropas son bloqueados por completo.
  * **Inteligencia Artificial Pacífica entre Tropas Aliadas:** Las sombras de cazadores del mismo gremio o alianzas nunca se seleccionarán como objetivos de ataque ni responderán a provocaciones entre sí.
  * **Diferenciación Visual en el Mundo:**
    * Las sombras de compañeros de gremio y aliados adquieren un contorno brillante (*glowing aura*) de tonalidad **Verde Esmeralda** (`ChatFormatting.GREEN`), distinguiéndose con facilidad del cian propio del Monarca (`AQUA`) y del rojo hostil (`DARK_RED`).
  * **Radar Táctico y Minimapa:**
    * En el minimapa del Sistema, las sombras aliadas se renderizan con un borde verde esmeralda de compañero (`0xFF50FA7B`), evitando que se confundan con monstruos hostiles o generen falsas alarmas de peligro élite.
  * **Protocolo de Red Sincronizado:** Incorporado el paquete `SyncShadowAlliancePayload` para sincronizar al instante el estado de alianza y propiedad de las sombras entre servidor y clientes cercanos.

---

## 🍎 Consumibles Sagrados de Clase y Raciones de Combate
* **Manzana de las Sombras / del Monarca (`shadow_apple`):**
  * Fruta sagrada de nigromancia imbuida con esencias de alma, metal abisal y la voluntad del Monarca.
  * Otorga **Regeneración V** (30s), **Absorción VI** (24 corazones extra durante 2 minutos), **Resistencia al Fuego** (2 min) y **Visión Nocturna Táctica del Cazador** (2 min).
  * **Regeneración Ultra-Rápida de Sombras Aliadas:** Durante 2 minutos, todas las sombras vivas del cazador en un radio de 48 bloques sanan pasivamente un 8% de su vida máxima + 2 puntos planos cada segundo, envueltas en llamas y partículas de alma.
  * Restablece instantáneamente un **35% del Maná Máximo** del jugador.
  * Receta de crafteo en mesa de trabajo combinando una manzana rodeada de Lingotes de Aleación de Almas (`soul_alloy_ingot`) y Lingotes Abisales (`abyssal_ingot`).
* **Zanahoria de Qi Espiritual (`spirit_qi_carrot`):**
  * Ración de combate suprema de los cazadores y cultivadores para expediciones en mazmorras prolongadas.
  * **Saciedad y Nutrición:** Llena 10 puntos de hambre y otorga **20 puntos de saturación instantánea** (equivalente a 2.0F de modificador).
  * **Restauración Espiritual Instantánea:** Regenera de inmediato un **25% de la reserva espiritual total** (Qi para cultivadores Murim y Maná para cazadores del Sistema).
  * Receta de forja en mesa de crafteo combinando una Zanahoria Dorada rodeada de Fragmentos de Jade Espiritual (`spirit_jade_shard`).
* **Integración en la Tienda del Sistema:**
  * Ambos consumibles han sido incorporados en la categoría de consumibles de la tienda (`ShopDefinitions`), disponibles para compra por monedas del Sistema.

---

## 🎁 Expansión de Cofres de la Tienda y Texturas Exclusivas del Sistema
Integración completa de la nueva jerarquía de cofres y cajas de botín directamente en la interfaz del Sistema, sin generar ítems físicos en el inventario ni registros innecesarios en el mundo:
* **Iconografía y Arte Pixel Art Exclusivo:**
  * Reemplazo de los antiguos placeholders vanilla (`chest`, `bundle`, `trapped_chest`, `ender_chest`, `shulker_box`) por 7 sprites personalizados de 16x16 creados para el mod.
  * Cada cofre cuenta con remates metálicos reforzados, cerrojos únicos y paletas alineadas con su rareza y minerales:
    * 🍀 **Caja de la Suerte (`lucky_box`):** Verde trébol de fortuna con cerrojo de oro premiado (Nivel 20+).
    * ⚪ **Cofre Común (`box_common`):** Hierro forjado y acero pulido (Nivel 10+).
    * 🔵 **Cofre Raro (`box_rare`):** Zafiro y resplandor de diamante (Nivel 35+).
    * 🟣 **Cofre Épico (`box_epic`):** Amatista mística y astralita profunda (Nivel 55+).
    * 🟡 **Cofre Legendario (`box_legendary`):** Oro solar imperial y ámbar (Nivel 75+).
    * 🟢 **Cofre Mítico (`box_mythic`):** Jade espiritual supremo y metales de Isekai/Murim (Nivel 100+).
    * 🌌 **Cofre Primordial (`box_primordial`):** Luz celestial y ébano divino del Monarca (Nivel 130+).
* **Nuevos Tiers Superiores de Recompensas:**
  * **Cofre Mítico:** Botín de alto nivel con lingotes de Mithril, Adamantita y Astral, manzanas de Mithril y Jade, píldoras de avance de reino marcial y el Martillo de Guerra de Adamantita.
  * **Cofre Primordial:** El botín cumbre del Sistema con lingotes Celestiales y Primordiales, manzanas primordiales, núcleos y perlas de dragón, la Píldora del Renacimiento Primordial y armas de rango divino (Cortador de Jade Primordial y Lanza Alada).
* **Enriquecimiento del Botín Progresivo:**
  * Incorporación de los nuevos consumibles y materiales del mod en las cajas existentes (Zanahorias de Qi, Manzanas de las Sombras, Lingotes de Hierro Blanco Refinado y Aleaciones de Almas).
  * Sistema de respaldo automático en memoria para mundos existentes (`CustomLootConfigManager`), garantizando la disponibilidad inmediata de los nuevos tiers sin requerir el reseteo manual de archivos JSON.
* **Apertura Cinemática en Pantalla (`ChestOpeningScreen`):**
  * Animación en vivo de la bóveda dimensional con vibración sísmica, auras de color por rareza, sello arcano original y ruleta interactiva con los ítems candidatos de alto nivel.

---

## 📖 Sistema de Adquisición de Habilidades por Hábito y Constancia (Progresión Procedural)
Retomando los pilares fundacionales de las series clásicas de Sistemas, los cazadores ya no dependen exclusivamente de los puntos de nivel para fortalecerse: el Sistema premia **la perseverancia, el hábito y las acciones repetidas en el mundo** desbloqueando y evolucionando habilidades orgánicas y títulos de maestría:
* **👁️ Ojo Perspicaz / Mirada del Cazador (`deadeye`):**
  * **Hábito Desencadenante:** Mantener el punto de mira enfocado continuamente sobre enemigos distantes o jefes en combate (`habitFocusTicks`).
  * **Desbloqueo y Evolución:** Se despierta a Nivel 1 tras 15 segundos de fijación y asciende hasta Nivel 6 (*Ojo de Halcón*).
  * **Barra de Vida Numérica a Larga Distancia:** Las etiquetas de nombre y vida de criaturas élite y jefes se vuelven legibles a distancias tácticas ampliadas de hasta 48 metros (`❤ HP actual / HP máximo`).
  * **Probabilidad de Golpe Crítico Observado:** Otorga un bono pasivo de impacto crítico (+5% por nivel, hasta +30%) al impactar presas y objetivos observados con proyectiles o tajos marciales.
  * **Título de Maestría:** `Mirada del Cazador` (+1.5 Daño, +1.5 Suerte Perceptiva).
* **🛡️ Cuerpo Templado / Resistencia al Dolor (`enduring_flesh`):**
  * **Hábito Desencadenante:** Sobrevivir a grandes cantidades de daño acumulado recibido en batallas difíciles (`habitDamageTaken`).
  * **Desbloqueo y Evolución:** Se adquiere tras sobrevivir a 150 puntos de daño acumulado y evoluciona hasta Nivel 6 (*Carne Inquebrantable*).
  * **Mitigación Pasiva de Daño Físico:** Reduce directamente entre un 5% y un 15% todo el daño físico entrante.
  * **Firmeza Monumental:** Otorga resistencia contra empuje pasiva (`Knockback Resistance` escalada hasta +0.24) y mantiene el pulso de regeneración celular al ser herido.
  * **Título de Maestría:** `Cuerpo de Hierro` (+0.15 Firmeza, +2.0 Armadura).
* **💨 Paso del Viento / Resistencia Pulmonar (`windrunner`):**
  * **Hábito Desencadenante:** Esprintar largas distancias explorando y descubriendo múltiples biomas a lo largo del mundo (`habitSprintDistance` y `habitBiomesExplored`).
  * **Desbloqueo y Evolución:** Se despierta al recorrer 500 bloques de esprint a través de al menos 2 biomas y escala hasta Nivel 6 (*Vendaval*).
  * **Resistencia Pulmonar y Ahorro de Estamina:** Reduce significativamente el agotamiento de hambre al correr, permitiendo esprints prolongados sin drenar la barra de comida.
  * **Ráfagas Espontáneas de Velocidad:** Posibilidad de activar ráfagas cinéticas de Velocidad II/III con estelas de viento y micro-impulsos en carrera libre, sumado a un aumento permanente de velocidad de movimiento en estadísticas base.
  * **Título de Maestría:** `Caminante del Viento` (+0.02 Velocidad de Movimiento, +0.05 Cadencia de Ataque).
* **⛏️ Instinto de la Tierra / Furia del Minero (`ore_sense`):**
  * **Hábito Desencadenante:** Picar miles de bloques en las profundidades subterráneas ($Y < 32$ y estratos de pizarra profunda) (`habitDeepBlocksMined`).
  * **Desbloqueo y Evolución:** Se despierta tras minar 150 bloques profundos y alcanza su cima a Nivel 6 (*Visión Telúrica*).
  * **Celeridad Minera Pasiva:** Confiere Prisa Minera (*Haste*) automática al empuñar picos bajo tierra.
  * **Duplicación Telúrica de Minerales:** Otorga una probabilidad del 4% al 24% de duplicar instantáneamente gemas y minerales extraídos (diamantes, esmeraldas, oro, hierro, etc.) con partículas de brillo y tañido de amatista.
  * **Título de Maestría:** `Instinto de la Tierra` (+1.5 Suerte Minera, +1.0 Dureza de Armadura).
* **⚔️ Maestría del Acero (`combat_instinct`):**
  * **Hábito Desencadenante:** Ejecutar miles de golpes certeros contra criaturas con espadas, hachas u otras armas cuerpo a cuerpo (`habitWeaponHits`).
  * **Desbloqueo y Evolución:** Se adquiere tras 80 impactos y progresa hasta Nivel 6 (*Instinto de Batalla*).
  * **Cadencia y Daño Marcial:** Incrementa pasivamente la velocidad de ataque (+0.04 a +0.24 de cadencia) y añade daño plano y porcentual a todos los impactos físicos con armamento marcial.
  * **Título de Maestría:** `Maestro del Acero` (+0.12 Velocidad de Ataque, +2.0 Daño de Arma).
* **📢 Notificaciones Festivas del Sistema y Audio Dedicado:**
  * Al desbloquear o evolucionar cualquier técnica por constancia, el Sistema despliega un banner dorado/esmeralda de celebración con efectos de partículas de resurrección y sonido de desafío completado (`SYSTEM_SKILL_UNLOCK`).

---

## 🗡️ Rediseño Visual y Estético de Habilidades de Armas (Fin del Círculo Genérico de Maná/Ki)
Erradicación total de los círculos planos genéricos de maná y ki que se proyectaban en el suelo al utilizar habilidades de armas, sustituyéndolos por un **motor cinemático de tajos y ondas de choque 3D** con texturas HD exclusivas, geometrías volumétricas multicapa, animación de swing del portador y vibración de pantalla dinámica:

* **Motor Gráfico de Tajos 3D Multicapa (`SlashWaveEntity` & `SlashWaveRenderer`):**
  * **Renderizado de Doble Cara con Cara Emisiva Central:** La malla geométrica proyecta caras superior e inferior para garantizar 100% de visibilidad volumétrica desde cualquier ángulo y cámara (F5 o primera persona), combinada con una arista interna hiperbrillante de luz pura (`LightTexture.FULL_BRIGHT`).
  * **Geometrías por Estilo de Impacto:**
    * *Doble Corte en Cruz (`STYLE_CROSS`):* Planos duales cruzados a $\pm 32^\circ$ con separación tridimensional.
    * *Estocada Espiral Perforante (`STYLE_THRUST`):* Planos ortogonales en ángulo recto ($0^\circ$ y $90^\circ$) simulando un haz de perforación supersónica.
    * *Barrido y Creciente Curva:* Geometría extendida con inclinación dinámica por *roll* e inercia direccional.
  * **Biblioteca de Nuevas Texturas VFX Exclusivas (256x256 ARGB HD):**
    * 🖌️ `slash_ink_wuxia.png`: Trazado de caligrafía en tinta china negra y filo de Qi dorado para técnicas marciales de Murim.
    * 🐾 `slash_shadow_claw.png`: Triple desgarro de garras abisales del Monarca de las Sombras.
    * 🪨 `slash_heavy_cleave.png`: Brecha sísmica de fractura colosal para mandobles y martillos titánicos.
    * ✨ `slash_celestial_blade.png`: Creciente sagrada de luz astral y filos de cristal divino.
    * 🌙 `slash_scythe_reap.png`: Arco de siega de almas alargado y afilado para guadañas de combate.
    * 💥 `slash_impact_burst.png`: Destello de impacto radial en estrella para colisiones físicas contundentes.
* **Armamento Rediseñado con Identidades Visuales Propias:**
  * **Espada de la Flor de Ciruelo (`PlumBlossomSwordItem`):** Despliega 3 tajos marciales de tinta Wuxia en abanico frontal con estelas de pétalos de cerezo flotantes, sonido de viento cortante y vibración de pantalla.
  * **Espada Sacude Cielos (`HeavenShakingSwordItem`):** Dispara un corte horizontal y un tajo descendente de Qi marcial y fractura pesada con chispas de relámpago e impacto sísmico.
  * **Espada del Demonio Celestial (`SkyDemonSwordItem`):** Emite una creciente carmesí Wuxia y un corte vertical abisal con partículas de fuego espectral y sonido de desgarro demoníaco.
  * **Guandao Astral (`AstralGuandaoItem`):** Ejecuta un colosal barrido de alabarda marcial de 5.4 metros con rugido de dragón y micro sacudida táctica.
  * **Guadaña del Dragón Negro (`BlackDragonScytheItem`):** Lanza un arco alargado de siega de almas de 5.6 metros con llamas de dragón abisal y corte etéreo.
  * **Guadaña del Pavor (`DreadScytheItem`):** Desata una siega frontal espectral y una siega carmesí de retorno posterior en vórtice de almas purificadoras.
  * **Mandoble del Creador (`CreatorClaymoreItem`):** Libera tajos cruzados de fractura colosal (horizontal y vertical) en energía de génesis primordial.
  * **Mandoble Gigantesco (`GiganticGreatswordItem`):** Proyecta un tajo sísmico ígneo de 5.6 metros con partículas de lava y fractura del suelo.
  * **Espada Sagrada de Asrahan (`AsrahanHolySwordItem`):** Lanza una onda radiante de luz estelar celestial de 4.8 metros con resonancia de campanas cósmicas.
  * **Báculo de Homa (`StaffOfHomaItem`):** Descarga un corte colosal de fuego carmesí seguido de una estocada ígnea perforante a alta velocidad.
  * **Dark Repulser (`DarkRepulserItem`):** Modo dual empuñado con Elucidator ejecuta ráfagas celestiales, ondas de vacío y corte en cruz; modo individual dispara olas de cristal sagrado.
  * **Elucidator (`ElucidatorItem`):** Habilidad *Starburst Stream* despliega 3 tajos sucesivos de garras de sombra y un corte final en cruz con partículas de dragón del vacío.
  * **Martillo de Guerra de Adamantita (`AdamantiteWarHammerItem`):** La habilidad sísmica proyecta 6 ondas de choque radiales de fractura telúrica (`TYPE_EARTH` y `STYLE_HEAVY_CLEAVE`) que emergen del suelo al impactar.
  * **La Doncella (`LaDoncellaSwordItem`):** Modo Trono de Ceniza proyecta un arco de siega carmesí y tajo ascendente con drenaje de vida; modo Fragancia del Silencio emite ondas celestiales de pétalos plateados.
  * **Luz Radiante / Rapier de Asuna (`LambentLightItem`):** Despliega una estocada de destello lineal supersónica con 50% de penetración de armadura y estela de luz solar.
  * **Espada Protogema (`PrimogemSwordItem`):** Lanza una creciente estelar de amatista cósmica restaurando vitalidad al portador.
  * **Espina Trascendente (`TranscendentThornItem`):** Emite una onda de choque sónica y fractura carmesí con explosión de fuego a su alrededor.
  * **Fe Inquebrantable (`UnbreakableFaithItem`):** Desata un tajo celestial bendito con auras de protección y velocidad.
  * **Balada del Amanecer (`DawnsBalladItem`):** Tras el teletransporte instantáneo, proyecta una onda de corte celestial amatista.
  * **Espada de la Luz Radiante (`RadiantLightSwordItem`):** Lanza un barrido celestial y una estocada solar perforante de alta concentración.
  * **Soul Solid (`SoulSolidSwordItem`):** Emite un haz de escarcha celestial congelante con fragmentación de hielo y fuego espectral.
  * **Gran Espada del Vacío (`VoidClaymoreItem`):** Habilidad activa *Corte Dimensional del Vacío* incorpora un proyectil masivo de fractura abisal que atrae a los enemigos a la línea de corte con sacudida de pantalla.

* **Sistema de Partículas de Impacto de Corte y Sonido Físico de Tajo (Golpes Básicos y Habilidades):**
  * **Erradicación de Efectos Vanilla Inanimados:** Se eliminó la dependencia de partículas y sonidos genéricos de Minecraft (`CRIT`, `SWEEP_ATTACK`, `PLAYER_ATTACK_CRIT`, `ANVIL_LAND`) en ataques cuerpo a cuerpo de armas únicas del mod.
  * **16 Nuevas Texturas de Partículas de Impacto (Animación 4-Frames por Categoría):**
    * 🌸 **Tajo de Qi Marcial y Tinta (`STRIKE_SLASH`):** Corte en arco curvo dinámico y tinta caligráfica para espadas ligeras, katanas y guandaos de cultivador.
    * 🐾 **Garras de Sombra del Monarca (`STRIKE_CLAW`):** Triple desgarro de garras abisales y cortes veloces para dagas, guadañas y armas demoníacas.
    * 🪨 **Fractura Sísmica y Hendidura Titánica (`STRIKE_CLEAVE`):** Explosión radial de fractura y estallido contundente para mandobles colosales, martillos y claymores.
    * ✨ **Destello de Filo Celestial y Diamante (`STRIKE_CELESTIAL`):** Corte sagrado en estrella de 4 puntas y destellos de luz pura para armas divinas, estoques y espadas sagradas.
  * **Renderizado Billboard en Pantalla Completa:** Partículas emisivas con iluminación completa (`0xF000F0`) de 1.55 a 1.75 bloques de tamaño que se orientan hacia la cámara directamente sobre el pecho del objetivo impactado.
  * **Paisaje Sonoro de Impacto Físico:** Cada golpe básico (clic izquierdo) y habilidad activa reproduce los efectos de audio de tajo marcial `CULTIVATOR_SLASH` y corte demoníaco `DOMAIN_OF_SANCTION_SLASH`, acompañados de micro sacudida sísmica de pantalla (`CameraShakePayload`).
  * **Planeo Dinámico de Proyectiles sobre el Terreno:** Solucionado el problema donde las ondas de corte colisionaban con el suelo bajo los pies del jugador al mirar hacia adelante; ahora el motor detecta el terreno vertical y desliza la onda a +0.25 bloques sobre la superficie, impactando y destruyéndose únicamente ante muros sólidos horizontales.
  * **Geometría Volumétrica Triplanar:** Las ondas de corte ahora proyectan 3 planos entrecruzados (plano de corte primario a $-16^\circ$, plano de cruce a $+22^\circ$ y espina vertical a $90^\circ$), otorgando un volumen 3D contundente visible desde cualquier perspectiva y distancia.

---

## ⚔️ Compatibilidad Total al 100% con Epic Fight y Better Combat
Integración nativa profunda y puente universal de eventos entre Awakening System, **Better Combat** y **Epic Fight** (Battle Mode):
* **Puente Universal de Daño y Ejecución de Habilidades (`LivingDamageEvent.Post`):**
  * **Resolución del Bypass de Epic Fight:** Epic Fight en modo de combate omite el llamado nativo de Minecraft a `Item#hurtEnemy()` al procesar los impactos por sus propios colliders y fuentes de daño. El nuevo puente del sistema intercepta todo golpe infligido por el jugador bajo fuentes de Epic Fight (`isEpicFightDamage`) y ejecuta inmediatamente el ciclo completo de combate del arma.
  * **Activación Garantizada de Partículas y Sonidos:** Todas las partículas visuales de corte (`STRIKE_SLASH`, `STRIKE_CLAW`, `STRIKE_CLEAVE`, `STRIKE_CELESTIAL`), efectos de audio físicos (`CULTIVATOR_SLASH`, `DOMAIN_OF_SANCTION_SLASH`), vibración de cámara (`CameraShakePayload`), combos pasivos y acumulación de EXP de arma (`WeaponLevelManager`) se ejecutan con 100% de fiabilidad en cada impacto de combo o animación de Epic Fight y Better Combat.
  * **Protección contra Golpes Dobles:** Sistema de detección con cero duplicaciones de durabilidad o efectos al alternar entre ataques estándar y ataques con mods de combate.
* **Sincronización y Corrección de Arquetipos en Better Combat:**
  * **Dominio de la Sanción (`domain_of_sanction`):** Corregido arquetipo de animación de mandoble lento a **Katana** (`bettercombat:katana`) con alcance de 3.8 bloques.
  * **Sin Cola / Samehada (`tailless`):** Corregido arquetipo de katana a **Mandoble Pesado** (`bettercombat:claymore`) con alcance de 3.6 bloques.
  * **Martillo de Herta y Martillo de Guerra de Adamantita:** Corregidos arquetipos de maza ligera a **Martillo Pesado a Dos Manos** (`bettercombat:hammer`) con alcance de 3.4 bloques y barridos colosales.
  * **Veredicto (`verdict`):** Configurado como **Gran Hacha de Batalla a Dos Manos** (`bettercombat:double_axe`) con alcance de 3.6 bloques.
  * **Incorporación de Espadas de Mineral:** Creados e integrados los perfiles de combate para `soul_alloy_sword` y `astralite_sword` (`bettercombat:sword`).
* **Capacidades y Atributos de Combate para Epic Fight:**
  * **Configuración de Atributos Auténticos:** Todas las capacidades de armas (`capabilities/weapons/`) ahora incluyen bloques de atributos completos de impacto (`impact`), penetración de armadura (`armor_negation`) y límite de objetivos por tajo (`max_strikes`):
    * *Katanas / Tachis:* 0.8 impacto, 15% penetración de armadura, 2 objetivos.
    * *Grandes Espadas y Mandobles:* 2.5 impacto, 30% penetración de armadura, 3 objetivos.
    * *Lanzas y Alabardas:* 1.2 impacto, 20% penetración de armadura, 2 objetivos.
    * *Dagas:* 0.3 impacto, 25% penetración de armadura, 1 objetivo.
    * *Espadas Ligeras:* 0.5 impacto, 5% penetración de armadura, 1 objetivo.
  * **Corrección de Clases:** `domain_of_sanction` configurado como `epicfight:tachi`, `tailless` como `epicfight:greatsword`, y los martillos pesados como `epicfight:greatsword`.
  * **Doble Compatibilidad de Rutas (Namespace Mirroring):** Los 75 archivos de capacidad se publican tanto en `data/awakening_system/capabilities/weapons/` como en `data/epicfight/capabilities/weapons/`, asegurando carga inmediata en cualquier versión o fork de Epic Fight.
* **Estandarización Completa de Etiquetas de Armas (`Item Tags`):**
  * Se agregaron todas las armas únicas, espadas de mineral, katanas, lanzas, dagas y mandobles a las etiquetas comunitarias e internas: `#c:melee_weapons`, `#c:tools/melee_weapon`, `#minecraft:swords`, `#c:tools/swords`, `#c:tools/spears`, `#c:tools/daggers` y `#c:tools/axes`.
* **Internacionalización y Corrección de Localización en Pantalla de Clases (`ClassSelectionScreen`):**
  * **Erradicación de Textos Bilingües y Hardcodeados:** Los botones de categorías (`Guerreros`, `Arcano`, `Murim`, `Todas`), la insignia de selección (`ELEGIDO`) y los nombres de manhwas de origen (`getShortManhwa`, `getManhwa`) fueron desvinculados de valores fijos en Java e integrados al sistema nativo de traducción (`Component.translatable`).
  * **Sincronización Total de Idiomas Hispanohablantes:** Sincronizadas las 13,630 claves de traducción en todos los dialectos de español (`es_es.json`, `es_cl.json`, `es_mx.json`, `es_ar.json`, `es_co.json`, `es_ec.json`, `es_pe.json`, `es_uy.json`, `es_ve.json`), resolviendo claves faltantes de teletransporte de gremios (`guild.msg.tpa_*`), senderos HSR, manzanas y zanahorias nuevas, y cofres de la tienda.
  * **Configuración del Perfil de Ejecución:** Ajustado el entorno de pruebas local (`run/options.txt`) a `lang:es_es` para alinearse de forma inmediata con las opciones del cliente del usuario.

* **Reingeniería Total del Sistema de Habilidades por Hábito y Constancia:**
  * **Habilidades Innatas Corporales del Cazador:** Las 5 habilidades de hábito (`enduring_flesh`, `windrunner`, `combat_instinct`, `ore_sense`, `deadeye`) son ahora **Habilidades Innatas Permanentes**. Ya no ocupan ranuras de talentos pasivos del árbol de clases ni requieren equiparse manualmente: una vez despertadas por la perseverancia del cuerpo, sus efectos y bonificadores de estadísticas se aplican automáticamente y de por vida.
  * **Corrección del Error de Muestreo de Esprint en `Paso del Viento` (`windrunner`):** Se eliminó la compuerta de 20 ticks que provocaba que el esprint se muestreara solo 1 tick cada 20 ticks (haciendo que recorrer 500 metros requiriera en la práctica 10,000 metros de carrera). El esprint ahora se acumula fluidamente en cada tick de movimiento. Además, se ajustó el umbral de Nivel 1 a 300 metros sin exigir visitar múltiples biomas para el primer despertar.
  * **Calibración de Enfoque Visual y Proyectiles en `Mirada del Cazador` (`deadeye`):** Se redujo la distancia mínima de fijación de 8 bloques a 3 bloques, ampliando el área de escaneo de combate. Asimismo, disparar e impactar a cualquier enemigo con proyectiles (flechas, tridentes, etc.) otorga instantáneamente +25 ticks de hábito hacia `deadeye`. El Nivel 1 ahora despierta a los 120 ticks (~6 segundos de combate o 5 disparos).
  * **Reconocimiento Universal de Armas en `Maestría del Acero` (`combat_instinct`):** Ahora cualquier impacto con espadas, hachas, armas del mod, armas de Better Combat o combos de Epic Fight suma impactos de forma fiable. El Nivel 1 se alcanza a los 40 golpes.
  * **Ajuste de Supervivencia en `Cuerpo Templado` (`enduring_flesh`):** Se redujo el umbral del primer despertar de 150 puntos a 50 puntos de daño acumulado sobrevivido, permitiendo que cazadores novatos despierten la habilidad tras sobrevivir a sus primeros combates reñidos.
  * **Minería Subterránea Ampliada en `Instinto de la Tierra` (`ore_sense`):** Se reconoce la extracción de bloques y minerales en cualquier estrato subterráneo ($Y < 50$ o bajo el nivel del mar con pico), reduciendo el umbral de Nivel 1 a 80 bloques.
  * **Resolución del Conflicto con la Afinidad Clásica:** Se crearon contadores de nivel dedicados para el progreso de hábito en NBT, evitando que la asignación de afinidades bloquee la detección del primer despertar por constancia.
  * **Visualización en Vivo en la Interfaz del Sistema (`SystemScreen`):** Las habilidades de hábito ahora aparecen listadas en la pestaña de pasivas con la insignia `✦ INNATA`. Si aún no han sido despertadas, se muestran bloqueadas pero con una barra de progreso en tiempo real (ej. `Daño: 24/50`, `Esprint: 180/300m`), permitiendo al cazador ver con exactitud cuánto le falta para desbloquearlas.
  * **Comando de Consulta de Hábitos (`/system habit` o `/system habito`):** Se integró un comando informativo que despliega en el chat los niveles y estadísticas exactas de cada hábito en tiempo real.

---

## ⚔️ Integración Cruzada con el Mod 'Armageddon' (`armageddon_mod`)
Integración nativa profunda y modular entre Awakening System y el mod de progresión RPG **Armageddon** (desarrollado por choupicul):
* **Detección Condicional y Arquitectura Modular (`ArmageddonCompatHandler`):**
  * El módulo detecta automáticamente la presencia de `armageddon` o `armageddon_mod` sin imponer dependencias estrictas, permitiendo que ambos mods coexistan en armonía sin errores ni incompatibilidades.
* **Reconocimiento de los 11 Jefes de Progresión:**
  * Registro integral de los jefes pre y post-dragón en `BossCompatibilityManager`: *The Iron Colossus*, *The Goblin Lord*, *Arion (Tyrant of the Emerald Wrath)*, *Eldorath (the Ancient Builder)*, *Zoranth (the Forgotten One)*, *The Elvenite Paladin*, *Vaedric (the Fallen Wanderer)*, *The Bringer of Doom*, *The Calamities*, *Zoranth (Newborn of the Zenith)* y *Nyxaris (the Veil of Oblivion)*.
* **Recompensas del Sistema por Superar Bloqueos de Tier:**
  * Al derrotar por primera vez a cada uno de los jefes de tier de Armageddon, el Sistema despliega una notificación holográfica triunfal (`[ALERTA DEL SISTEMA: BLOQUEO SUPERADO]`), otorga **+5 Puntos de Atributo libres** para asignar en las estadísticas del personaje y entrega un bono masivo de Monedas del Sistema (+2,500 a +20,000 monedas según el rango).
* **Títulos Exclusivos de Cazador con Atributos Dedicados:**
  * `armageddon_colossus_breaker` (*Destructor del Coloso de Hierro*): +2.0 Armadura, +0.10 Resistencia al Empuje.
  * `armageddon_goblin_subjugator` (*Subyugador del Señor Duende*): +1.5 Suerte, +0.05 Cadencia de Ataque.
  * `armageddon_emerald_conqueror` (*Conquistador de la Furia Esmeralda*): +2.0 Daño de Ataque, +1.0 Suerte.
  * `armageddon_nether_gatekeeper` (*Vencedor del Constructor Antiguo*): +4.0 Vida Máxima, +2.0 Armadura.
  * `armageddon_void_reaper` (*Segador del Olvido*): +3.0 Daño de Ataque, +0.02 Velocidad de Movimiento.
  * `armageddon_elven_slayer` (*Flagelo de Elvenita*): +2.5 Daño de Ataque, +0.08 Cadencia de Ataque.
  * `armageddon_doom_bringer_slayer` (*Fin del Heraldo del Destino*): +6.0 Vida Máxima, +3.0 Armadura.
  * `armageddon_calamity_overlord` (*Soberano del Armagedón*): Rango Mítico (+10.0 Vida Máxima, +5.0 Daño de Ataque, +3.0 Dureza de Armadura, +0.20 Resistencia al Empuje).
* **Extracción de Sombras de Jefes y Criaturas de Armageddon:**
  * El Monarca de las Sombras ahora puede extraer las sombras de todas las bestias y jefes de Armageddon. Los jefes son promovidos de forma automática a rangos jerárquicos superiores (*Elite Knight*, *Commander* y *Grand Marshal*).
* **Habilidad Mítica Cruzada: Voluntad del Armagedón (`armageddon_breaker`):**
  * Técnica pasiva de combate desbloqueada automáticamente al superar el primer gran desafío de Armageddon. Otorga entre un **+15% y un +35% de daño perforante directo contra jefes**, penetra la armadura del objetivo y, en su nivel máximo, emite un pulso táctico que restaura maná y Qi a todos los aliados cercanos en combate.
* **Tienda Dokkaebi: Suministro de Invocaciones de Jefes:**
  * Incorporación de las 10 herramientas y artefactos de invocación remota de jefes de Armageddon en el catálogo de compra por monedas del Sistema, permitiendo a cazadores veteranos re-enfrentar a los bosses sin requerir crafteos tediosos.

---

## 🎰 Repotenciación Integral de la Ruleta Digital del Sistema
Revisión visual, acústica y de balance económico para la Ruleta del Destino del Sistema:
* **Lluvia de Confeti Dinámico 2D en Pantalla (`ConfettiParticle`):**
  * Implementación de un sistema de partículas de confeti en la interfaz (`ChestOpeningScreen`), con cintas multicolores (oro, cian, magenta, esmeralda y blanco) con física de gravedad, aceleración vertical, oscilación de vaivén y aleteo tridimensional al abrirse la ruleta o cajas de alta jerarquía.
* **Integración de Efectos de Audio Nativos (Fanfarria y Trinquete Arcade):**
  * Conexión de los eventos de audio dedicados `ModSounds.ROULETTE_SPIN`, `ModSounds.ROULETTE_WIN` y `ModSounds.CASINO_JACKPOT`.
  * La ruleta reproduce su zumbido de giro rúnico al comenzar y una fanfarria orquestal con campanas triunfales al detenerse en la casilla premiada.
* **Actualización del Botín y Gran Jackpot del Sistema:**
  * Modernización del carrete y de la tabla de probabilidades con materiales avanzados de progresión: Zanahorias de Qi Espiritual, Lingotes de Hierro Blanco Refinado, Lingotes Abisales, Aleaciones de Almas, Manzanas de las Sombras, Lingotes de Mithril y Lingotes Primordiales.
  * **¡¡Gran Jackpot del Sistema!! (Roll 99):** Casilla legendaria que otorga **25,000 Monedas del Sistema** directas más un **Lingote Celestial**, desatando una lluvia masiva de confeti dorado, sonido de jackpot arcade y un banner resplandeciente en pantalla.
* **Tirada Diaria Gratuita por Constancia:**
  * Cada nuevo día en el mundo de Minecraft, el jugador recibe **1 Tirada Gratuita** garantizada en la Ruleta.
  * En la interfaz de la Tienda se destaca con un botón esmeralda resplandeciente (`✦ ¡GIRAR GRATIS (DIARIA)! ✦`).
  * Sincronización persistente en NBT (`last_free_roulette_day`) evitando abusos en reconexiones.

---

## 🏛️ Renovación y Re-Roll Dinámico del Altar 3D de la Ruleta
Modernización integral del bloque físico interactivo `RouletteAltarBlock` en el mundo:
* **Re-Roll Garantizado al Romper y Colocar (Garantía de Botín Fresco):**
  * Al romper o minar el Altar de la Ruleta, se extrae como bloque limpio sin NBT residual.
  * Al volverse a colocar en el mundo (`setPlacedBy`), el bloque ejecuta automáticamente `populateSlotsFresh()`, re-sorteando por completo las 8 ranuras del disco físico tomando recompensas ponderadas desde la configuración de botines (`CustomLootConfigManager`).
  * Despliegue de partículas rúnicas y varas del end (`ParticleTypes.ENCHANT` y `END_ROD`) al ser colocado para confirmar visualmente el refresco de premios.
* **Requisito Estricto de Ficha de Ruleta del Sistema:**
  * El Altar 3D físico exige obligatoriamente insertar 1x **Ficha de Ruleta del Sistema** (`RouletteTokenItem`) para activar el giro.
  * Se eliminó el atajo de monedas directas: la ficha es un recurso prestigioso y de alto valor (10,000 monedas en la tienda del Sistema o botín élite), por lo que el altar físico protege su valor requiriendo la ficha física en mano.
  * Si el jugador interactúa con la mano vacía o con cualquier otro ítem, el altar emite un sonido sordo de escudo e indica en pantalla que requiere obligatoriamente la Ficha de Ruleta.
* **Resolución de Bloqueo / Crash por Serialización de Ítems (Codec Limit [1;99]):**
  * Solucionado el error crítico `java.lang.IllegalStateException: Value must be within range [1;99]` provocado por `ContainerHelper.saveAllItems` en Minecraft 1.21.1 al serializar pilas de ítems monetarios con recuentos altos.
  * En `CustomLootConfigManager#resolveStack`, el tamaño de la pila se limita estrictamente a `Math.min(64, defaultMaxStackSize)`.
  * La entrega masiva de Mora en la ranura 7 se sustituyó por los ítems físicos correspondientes: **Fardos Dorados de Mora** (`mora_bundle`, 50,000 Mora) y **Bolsas de Mora del Aventurero** (`mora_pouch`, 10,000 Mora), que el jugador puede sostener e interactuar para añadir la moneda a su monedero.
* **Holograma 3D Flotante y Exhibición Monumental del Premio:**
  * Durante la secuencia de victoria (`winDisplayTicks`), el ítem premiado asciende en un arco parabólico girando a 720° con iluminación radiante máxima (`LightTexture.FULL_BRIGHT`).
  * Se proyecta directamente sobre el ítem un **cartel holográfico tridimensional tipo nametag** orientado de cara al jugador (`cameraOrientation`), mostrando en letras doradas y blancas el nombre del objeto ganador (`★ [Nombre del Ítem] ★`) y el bono de monedas obtenido (`+XXX ⛃ Monedas`).
  * La posición del holograma se compensa matemáticamente respecto a la orientación cardinal del bloque (`FACING`), garantizando nitidez perfecta desde cualquier ángulo.
* **Diseño Acústico Arcade y Fanfarria en el Mundo:**
  * Conexión de audio ambiental posicional OpenAL: zumbido de giro rúnico al arrancar (`ModSounds.ROULETTE_SPIN`), clics de trinquete desacelerados y campanas metálicas en cada sector.
  * Fanfarria triunfal al detenerse la aguja (`ModSounds.ROULETTE_WIN`) y sonido de jackpot de casino (`ModSounds.CASINO_JACKPOT`) con fuegos artificiales y estallidos de tótem de la inmortalidad ante premios de alto calibre.
* **Sobrecarga y Modernización Total del Botín del Altar 3D (Versión 2):**
  * Auto-migración y actualización a la versión 2 de configuración (`roulette_rewards.json`) incorporando todo el contenido de progresión moderna del mod:
    * **Ranura 0 (Gran Premio Mítico y Divino):** Lingotes Celestiales x2, Cortador de Jade Primordial, Reflejo de las Tinieblas, Báculo de Homa, Elucidator, Dark Repulser, Espada de Flor de Ciruelo, Espada del Demonio Celestial, Espada de Asrahan y Guadaña del Dragón Negro.
    * **Ranura 1 (Reliquias Celestiales):** Lingotes Primordiales x2-x3, Núcleo del Dragón Celestial, Perla del Dragón Celestial, Píldora del Renacimiento Primordial, Reloj del Rey de la Muerte y Estrellas del Nether x2-x3.
    * **Ranura 2 (Metales Supremos):** Lingotes de Mithril, Astralita, Adamantita, Abisales y Aleaciones de Almas (en lotes de x3 a x8 unidades).
    * **Ranura 3 (Artefactos y Talismanes Divinos):** Fe Inquebrantable, Dagas de la Sombra, Espina Trascendente, Tótems de la Inmortalidad x2-x3 y Talismán de Ascensión Murim.
    * **Ranura 4 (Consumibles Sagrados del Sistema):** Manzanas de las Sombras x2-x4, Zanahorias de Qi Espiritual x8-x16, Manzanas Doradas Encantadas x2-x4, Píldoras de Transformación Ósea y Purificación de Qi.
    * **Ranura 5 (Metales Refinados y Riqueza):** Lingotes de Hierro Blanco Refinado x6-x12, Lingotes de Forja de Almas x4-x8, Bloques de Netherite x1-x2 y Bloques de Diamante x3-x6.
    * **Ranura 6 (Fichas y Suministros de Expedición):** Fichas de Ruleta x3-x6, Hadas de Maná Embotelladas x2-x4, Manzanas Doradas x6-x12 y Perlas de Ender x16-x32.
    * **Ranura 7 (Fortuna y Monedas del Sistema):** Monedas del Sistema x64-x128 y Mora Celestial (25,000 a 60,000).
  * **Escalado Dinámico de Monedas Ganadas:** Se sustituyó la antigua recompensa plana (75-225 monedas) por una bonificación proporcional al tier del premio:
    * *Premios de Suministros y Consumibles:* 500 a 1,800 monedas.
    * *Metales y Minerales Supremos:* 1,500 a 3,000 monedas.
    * *Reliquias Celestiales y Gran Premio Mítico:* 2,500 a 15,000 monedas.
    * *Gran Jackpot (Roll 0 en Ranura Mítica):* **25,000 Monedas del Sistema** directas.

---

## 🚪 Dimensión de Bolsillo y Santuario Personal (Roadmap Sección 29)
Implementación integral y nativa en NeoForge de la habilidad de invocación de la **Puerta a la Dimensión de Bolsillo** y el reino privado del cazador, operando de manera 100% autónoma y sin dependencias externas:
* **Entidad Tridimensional de Puerta Física (`PocketDoorEntity` y `PocketDoorModel`):**
  * Invocación de una puerta física tridimensional frente a la línea de visión del jugador al ejecutar la habilidad.
  * Modelo 3D detallado con marco de madera oscura pulida, bisagras de hierro forjado y pomo/picaporte dorado reluciente.
  * Al hacer clic derecho sobre la puerta, esta se abre o cierra interactivamente con efectos de sonido de puerta real.
* **Animación Cinemática de Cierre Suave (18 Ticks):**
  * Sistema de rotación angular fluida con interpolación de ticks parciales (`doorAngle`), cerrándose suavemente desde 90° hasta 0° a lo largo de 18 ticks de juego.
  * Al culminar el recorrido de cierre, reproduce el sonido acústico de encaje y la puerta se desvanece de forma limpia.
* **Teletransporte Instantáneo y Directo por Umbral:**
  * Al atravesar el vano de la puerta abierta, el cazador es transportado de inmediato entre el Overworld y su dimensión de Santuario sin pantallas de carga forzadas ni esperas.
  * Posicionamiento seguro garantizado: el punto de aparición ubica al jugador sobre el terreno firme frente a la puerta, evitando asfixia o caídas al vacío.
* **Ciclo Completo de Ocultamiento y Privacidad Interdimensional (Roadmap 29.1):**
  * **Invocación:** El cazador invoca la puerta en el mundo exterior y la abre con clic derecho para cruzar a su santuario.
  * **Cierre Interior (Ocultamiento Total):** Al entrar al Santuario y cerrar la puerta desde adentro, la puerta del Overworld se oculta y desvanece por completo. Ningún mob hostil ni jugador ajeno puede ver la entrada ni seguir al cazador.
  * **Reapertura de Retorno:** Al interactuar nuevamente con la puerta desde el interior para salir, la puerta exterior reaparece de forma idéntica en las coordenadas exactas de origen en el Overworld.
  * **Retorno y Desvanecimiento:** Al cruzar de vuelta al Overworld y cerrar la puerta desde afuera, esta completa su animación de giro suave de 18 ticks y se despawnea por completo.
* **Generación Procedural Natural del Santuario (Roadmap 29.2):**
  * **Biomas Exclusivos de Paisaje Sereno:** Generación de un mundo pacífico de colinas suaves compuesto por praderas floridas (`sanctuary_peaceful_meadow`) y bosques susurrantes de robles y cerezos en flor (`sanctuary_whispering_forest`).
  * **Erradicación de Estructuras Vanilla y Cajas Artificiales:** Eliminación total de aldeas vanilla, campamentos, fortalezas y estructuras no deseadas.
  * **Spawn Directo en la Naturaleza:** Se eliminaron las plataformas artificiales de adoquín o spawn en el punto de aparición; la puerta se asienta orgánicamente sobre la hierba natural.
  * **Clima y Atmósfera Zen:** Cielo diurno perpetuo con luz solar dorada, ausencia total de mobs hostiles y entorno zen optimizado para la meditación, cultivo y construcción de la base del cazador.
  * **Domo Celeste Protector:** Frontera perimetral con barrera etérea que previene caídas al vacío y mantiene al cazador dentro de los límites territoriales de su santuario.
* **Árbol de Habilidades y Progresión Territorial (Roadmap 29.3):**
  * **Integración en Árbol de Habilidades (`SkillDetailScreen`):** Desbloqueo y gestión con icono pixel art personalizado de la puerta dimensional (`pocket_dimension_doorway.png`).
  * **Niveles de Expansión Territorial:**
    * *Nivel 1 (Puerta Inicial):* Radio territorial de 48 bloques alrededor de la puerta; acceso a los biomas pacíficos base.
    * *Nivel 2 (Expansión Territorial):* Ampliación a un radio de 96 bloques de construcción libre y expansión del horizonte natural.
    * *Nivel 3 (Dominio Soberano):* Radio extendido a 160 bloques, regeneración ambiental pasiva y control absoluto del espacio.
* **Diferenciación Estricta con el Subespacio (Roadmap 29.4):**
  * El **Subespacio** permanece dedicado exclusivamente como bóveda de almacenamiento y bodega dimensional de cofres/ítems.
  * El **Santuario Personal** es un plano habitable, físico y editable donde el jugador puede construir su hogar, entrenar y resguardarse con total privacidad.
* **Purga y Erradicación Total de Immersive Portals (100% Nativo en NeoForge):**
  * Eliminación completa de mixins, accesstransformers, compatibilidades externas y jars de Immersive Portals (`imm_ptl`, `qouteall`).
  * Operación 100% nativa y optimizada para NeoForge 1.21.1, garantizando compatibilidad absoluta con Iris, Sodium, Lodestone, Epic Fight, Better Combat y paquetes de shaders sin crasheos de VertexConsumer ni incompatibilidades de red.

---

## 🎃 Festividades Estacionales y Colección de Dulces Mágicos (Halloween y Navidad)
Sistema dinámico de eventos de temporada integrando consumibles gastronómicos temáticos, efectos mágicos únicos y texturas exclusivas de alta resolución (32x32):
* **Gestor de Eventos Estacionales (`SeasonalEventManager`):**
  * Activación automática según el calendario del sistema: **Halloween** (15 de Octubre al 5 de Noviembre) y **Navidad** (1 de Diciembre al 7 de Enero).
  * Los dulces y golosinas conservan sus valores nutricionales en cualquier época del año, pero sus efectos mágicos especiales solo despiertan durante su festividad correspondiente (mostrando un aviso elegante en el tooltip fuera de temporada).
* **🍬 Colección de Noche de Brujas (Halloween Sweets):**
  * 🍬 **Caramelo Embrujado (`haunted_candy`):** Dulce envuelto en papel festivo violeta y naranja. Desata el efecto de *Truco o Trato*: 50% de probabilidad de *Truco* (Levitación por 5s + Suerte II por 60s) o 50% de probabilidad de *Trato* (Invisibilidad por 20s + Hipervelocidad IV por 20s con partículas espectrales).
  * 👻 **Caramelo de Alma (`soul_candy`):** Golosina etérea brillante. Otorga Resplandor (*Glowing* por 30s), Velocidad III (20s) y Visión Nocturna clara (30s) para cacerías en mazmorras oscuras.
  * 🫐 **Gomitas de Maná (`mana_gummy`):** Gomitas azules masticables infusionadas con néctar de maná. Otorgan Regeneración II y restauran instantáneamente **+50 puntos de Maná** a la reserva del cazador.
  * 🍫 **Chocolatín Berserker (`berserker_chocolate`):** Tableta de chocolate oscuro amargo con runa carmesí. Otorga Fuerza II (*Damage Boost* por 30s) y Resistencia I (20s) para asaltos directos en combate.
  * 🧺 **Canasta de Dulces de Halloween (`candy_basket`):** Contenedor de mimbre con lazo de calabaza y rareza Rara. Al abrirse durante Halloween, reproduce sonidos espeluznantes de bruja/ghast, desata partículas de llamas de alma, humo y portal, entrega de 2 a 4 golosinas de Halloween aleatorias y otorga entre **+40 y +160 Monedas del Sistema** (Cosecha de Almas).
* **🎄 Colección de Festividades Invernales (Christmas Sweets):**
  * 🦯 **Bastón de Caramelo Mágico (`candy_cane`):** Bastón clásico de caramelo a rayas rojas y blancas con esencia de menta. Otorga Velocidad II y Prisa Minera II (*Haste* por 30s) para desplazamientos ágiles.
  * 🍪 **Galleta de Jengibre Real (`gingerbread_cookie`):** Hombrecillo de jengibre horneado y glaseado. Ración de alta nutrición que otorga Regeneración II (15s) y Saturación instantánea prolongada.
  * ☕ **Chocolate Caliente Gourmet (`hot_chocolate`):** Taza humeante de chocolate espeso con malvaviscos. Disipa inmediatamente el fuego o congelación, confiere Absorción II (4 corazones extra por 45s) y Resistencia al Fuego total por 45s.
  * 🍬 **Turrón Imperial de Almendra (`almond_nougat`):** Barra de turrón duro tradicional de almendras seleccionadas. Proporciona una firmeza colosal otorgando Resistencia II (30s) y Fuerza I (30s).
  * ❄️ **Caramelo de Menta Polar (`polar_peppermint`):** Caramelo blanco glacial con copos de nieve. Otorga Velocidad I por 30s y proyecta un aura de copos de nieve flotantes alrededor del cazador.
  * 🎁 **Caja de Regalo Navideña (`christmas_gift_box`):** Paquete festivo envuelto con cintas rojas y doradas. Al interactuar durante la Navidad, desata tintineos de amatista y fuegos artificiales, recompensando al jugador con dulces navideños, monedas del Sistema y sorpresas festivas.
* **Diseño Artístico y Balance Nutricional:**
  * 9 texturas pixel art dedicadas en 32x32 píxeles con paletas armonizadas y legibilidad perfecta tanto en el inventario como en la mano del jugador.
  * Propiedades de comida configuradas con `alwaysEdible()`, permitiendo a los cazadores consumir los dulces en cualquier momento del combate para recibir sus beneficios mágicos incluso con la barra de hambre al máximo.



---

## 🔨 Forja del Cazador (`hunter_forge`)
* **Nueva estación de trabajo** con modelo 3D de yunque de acero azulado sobre un pedestal de brasas y pantalla propia del Sistema.
* **Templado por golpe:** una barra de precisión se detiene al golpear y define la calidad del forjado; el resultado se muestra en pantalla.
* **Bonificaciones reales:** el arma o armadura forjada gana daño o armadura según la calidad, más un extra según el rango del núcleo de monstruo usado.
* **Solo acepta lo correcto:** equipo, núcleos de monstruo, catalizadores y gemas en su ranura (etiquetas `forge_catalysts` y `forge_gems`).

---

## 🛡️ Escudos del Sistema con Modelo 3D (Roadmap Sección 58)
* **Escudo Cometa de Siderita (`siderite_kite_shield`):** al bloquear empuja a los atacantes cuerpo a cuerpo. +2 de armadura en la mano secundaria.
* **Escudo Torre del Baluarte (`bastion_tower_shield`):** cada bloqueo da Resistencia por un momento; con la clase Baluarte de Hierro además devuelve el 30% del daño al atacante. +4 de armadura y resistencia al empuje.
* **Escudo Redondo de Jade Espiritual (`spirit_jade_round_shield`):** al bloquear cura con el Qi del golpe detenido. +3 de armadura y +2 corazones.
* Se sostienen y bloquean igual que el escudo de Minecraft, también con Epic Fight. Recetas con un escudo y su material; se reparan en el yunque con ese material.

---

## 🏰 Clase Tanque: Baluarte de Hierro y Pasivas de Combate
* **Nueva clase Baluarte de Hierro (`iron_bastion`):** atrae a los enemigos, bloquea solo golpes frontales que un escudo puede parar y tiene su propio árbol de habilidades.
* **Pasivas de combate** nuevas para seis clases existentes (cadena de rayos limitada a 3 enemigos hostiles, robo de vida, daño extra contra enemigos heridos, entre otras).

---

## ☄️ Meteoritos: Caída, Impacto y Objetos Nuevos
* **Caída en diagonal** desde lejos, con explosión a nivel del suelo, onda expansiva, cúpula de fuego, humo que sube y un estruendo retrasado en capas.
* **Cráter humeante y restos del meteorito** tras el impacto.
* **Brújula de Meteoritos (`meteor_compass`):** apunta al último meteorito caído.
* **Fragmento de meteorito** y texturas nuevas: espadón de siderita más grande, bloque de siderita con placas, fragmento de metal y piedras rúnicas talladas.
* **Corregido:** el cierre del juego al chocar el meteorito contra el suelo y la estela que seguía a la cámara.

---

## ⌚ Reloj de Kim Gong-ja
* Nueva textura dorada.
* **Clic derecho:** muestra cuándo puede volver a salvarte.

---

## 🧑‍🤝‍🧑 NPCs, Gremios y Rangos
* **Corregido:** las estructuras ya no vuelven a generar NPCs que fueron reclutados por un gremio.
* **Corregido:** los NPCs expulsados de un gremio vuelven a su estructura.
* **Corregido:** sobre los jugadores se muestra el rango de cazador evaluado y no el rango por nivel.
* Se quitaron las voces genéricas de los NPCs; solo queda la voz del inspector de la Asociación.

---

## 🧹 Limpieza y Correcciones Varias
* Se quitaron la arena de prueba y el observatorio.
* Se quitó el aviso permanente de la pantalla de clase y se subió el botón de confirmar.
* **Epic Fight:** se quitaron 75 archivos de armas duplicados que generaban avisos al entrar al mundo; las armas siguen con sus movimientos.

---

## 🏘️ Pueblo Medieval del Piso 11 (Pradera Serena)
* **Pueblo grande con interiores:** taberna, herrería, biblioteca, panadería, capilla con campanario, casa señorial, granero, torres de vigía, puestos en la plaza, pozo, campos de cultivo y casas por todas las calles. Se construye solo al llegar al piso; `/medieval_village` lo pone donde estés.
* **Casas variadas de verdad:** 10 paletas de materiales, formas distintas (pequeñas, altas, alargadas, en L), porches, chimeneas, torrecillas y escalones en la puerta. Techos cerrados por dentro, puertas que abren hacia fuera, vallas unidas, calles lisas, campos planos y agua a un solo nivel.
* **Gente propia en lugar de aldeanos:** cada vecino es una persona distinta con su skin y su nombre (Borin el herrero, Greta la tabernera, Anselm el sacerdote…). Nadie se repite y hay un solo herrero. Los que no tienen oficio toman el de su ropa.
* **Conversaciones de RPG:** al hablar con ellos se abre un diálogo con su retrato y el título "Nombre · Oficio"; cuentan a qué se dedican, dan consejos sobre la Torre y explican el pueblo. Los vecinos tienen cuatro juegos de frases distintos (en español, inglés, portugués y ruso).
* **Brazos finos:** las skins de brazos finos (estilo Alex) se dibujan con su cuerpo correcto.

---

## 🌌 Armería Celestial (28 Armas Nuevas)
* **28 armas celestiales** con habilidad de clic derecho propia: rayos, lluvia de estrellas, guadaña giratoria, jabalinas, nube de nebulosa, golpe al suelo y embestida. Galaxia cambia de forma al agacharse y hacer clic derecho. Tempestad Aurora es el antiguo "Weapon 1".
* **Bien sujetas:** el filo mira hacia fuera, se agarran por el mango (también en el modo combate de Epic Fight) y su tamaño va según el tipo: espada, mandoble, guadaña o lanza.
* **Movimientos de Epic Fight** para cada una. Las guadañas atacan como guadañas con Better Combat y, si el jugador tiene el addon **Ascended Arts**, con sus combos de guadaña (paquete opcional: sin el addon todo sigue igual).
* Sin abuso de habilidades: el tiempo de recarga se respeta y ya no se puede salir volando pulsando clic derecho sin parar.

---

## 🎮 Estilo SAO (Full Dive)
* **Menú propio en el estilo SAO** para Estado, Habilidades, Títulos, Misiones, Artefactos, Ranking, Progreso y Tienda, con venta, banco, ruleta, mercado de cazadores y Star Stream.
* **Menú flotando en 3D** delante del jugador, con entrada y salida animadas y sonidos de menú.
* **Barras de vida estilo SAO** sobre las criaturas, **HUD del jugador** estilo SAO, **muerte en cristales** que estallan, **cursor sobre el objetivo** y **cursor rojo** para los jugadores asesinos (5 o más muertes de jugadores).
* **Viajar a los pisos de la Torre** ya alcanzados desde Mundos.

---

## 👹 Criaturas Rehechas y Sombras
* Rehechos desde cero: **Hombre Bestia** (león-lobo), **Rey Demonio de los Elementos** (monarca acorazado), **Nigromante Abisal**, **Elfo de Escarcha** y **Soldado Hormiga** (caballero-hormiga).
* **Goblins** repintados con orejas caídas, colmillos y ojos que brillan.
* **Animales de maná** (vaca, cerdo, oveja, gallina) con cristales brillantes en la piel.
* **Pieles de sombra:** los soldados de sombra del Monarca usan una versión negra y violeta de su textura.
* **Mutant Warden** como élite de los pisos 16 al 19.
* Sonidos nuevos para el Hombre Bestia, el Contemplador, la hormiga, el lobo de sombra, el tigre y los goblins.

---

## 🗼 Gran Torre
* La torre se levanta al crear el mundo, en vez de aparecer de golpe mientras cargan los chunks.
* **Pisos 16 al 20 con terreno propio:** el Bastión de Obsidiana del Vacío (mesetas de piedra del End, agujas de obsidiana, chorus e islotes flotantes), las Cumbres Celestiales del Dao (torres de roca con musgo y bosques de bambú), el Templo Dorado de la Calamidad Solar (cañones de arenisca roja con franjas de colores y columnas doradas caídas), la Antecámara de las Telarañas (bosque muerto de noche con costillas gigantes) y la Cámara Real de la Reina, una cueva de seda con la arena-nido colgando de hilos hasta el techo, suelo de telaraña, huevos que brillan y una calzada con faroles hacia los portales.
* **Puertas de Ruinas fijas:** las Minas Oscuras y el Templo de la Jungla ya no aparecen al azar cerca del jugador; son lugares fijos del mundo (unas 6 de cada 10 zonas de 576 bloques tienen una), se quedan para siempre y llevan su nombre y su rango flotando encima, visibles solo de cerca. `/ruingate locate` dice dónde está la más cercana.
* **Enemigos de la torre con nivel por piso:** ya no copian el nivel del jugador; cada piso suma 5 niveles (piso 12 ≈ 60, piso 20 ≈ 100) y los jefes 10 más. Nombres de los biomas de los pisos 16 al 20 traducidos. El Soldado Hormiga tiene textura nueva (quitina rojiza, sin morado), antenas acodadas, alas translúcidas y ya no hace ruido al caminar. El Cazador de las Ruinas vuela estable en vez de subir y bajar sin parar. Los nombres de los enemigos ya no se ven grises ni a medias en la oscuridad.
* **Piso 20 rehecho:** los portales están en las esquinas de la cueva y hay que cruzar el nido entre arañas, arañas de cueva y crías de la araña madre, y subir por una rampa en espiral alrededor del pilar de la arena. La jefa (la Reina de los Nidos) ya no desaparece antes de que llegues. El mensaje de "cima de la torre" ya no dice "Piso 10". Los cristales de maná de la torre salen del rango de su piso (antes el piso 10 daba B y A). Ya no suena el sonido de puerta al viajar entre mundos.
* **Minerales nuevos de la Gran Torre:** Ignita, Acero Sepulcral, Oricalco, Gelidita, Cristal de Escarcha Antigua, Verdanita, Perenita, Dracoplomo, Xenio, Eridanio, Exodita y Lumenita, cada uno con su rango (D a S) y su franja de pisos (la Ignita del 10 al 20, el Acero Sepulcral del 15 al 25, el Oricalco del 20 al 30…). Cada metal tiene mena, material en bruto, lingote y bloque; los cristales, mena, gema y bloque. Aleaciones Hongarita y Espectrita, y Chatarra Fundida que suelta a veces la Ignita. Texturas propias.
* **Rarezas de las armas:** ahora salen en el idioma del juego (Común, Raro, Épico, Legendario, Mítico) y hay una nueva por encima de Mítico: **Divino**. Los prefijos y sufijos también se traducen. El equipo con rareza cae mucho menos (de hasta 1 de cada 6 enemigos a como mucho 1 de cada 40) y las rarezas altas piden enemigos fuertes y suerte.
* **Herrero del pueblo (piso 11):** Borin ahora vende armas sencillas (hierro, diamante y netherita) y forja por encargo armas del mod (Espada de Siderita, Durandal, Galaxia…) si le llevas lingotes de los minerales de la torre y monedas. Tarda un día de Minecraft (20 minutos reales) y en el piso 11 una barra arriba muestra el tiempo que falta. Un encargo a la vez.
* **Retoques:** las menas de la torre ahora parecen de Minecraft (pepitas redondeadas sobre la piedra del juego) y los bloques de mineral tienen marco y colores más limpios; los minerales salen juntos y ordenados al principio de la pestaña de Materiales. Ya no suena el "Link Start" al viajar a otro piso. Los árboles del piso 2 (y las demás copas de la torre) ya no tienen enredaderas acostadas.
* **Pisos 21 al 25 de la Gran Torre:** la Aldea del Valle Otoñal (segundo pueblo, pacífico, entre robles naranjas), las Marismas Secas de las Raíces Ahogadas (barro y manglares), las Salinas Blancas del Cementerio de Titanes (costillas y cráneos gigantes), las Cumbres de Cobre de la Tormenta Eterna (vetas de cobre y pararrayos) y la Caverna de los Cien Filones (minería, sin monstruos). Los portales de los pisos de jefe (10 y 20) quedan sellados hasta vencer al jefe, y ya se recuerda cuando cae la Reina de los Nidos. **Nuevo enemigo:** el Esqueleto Wither Mutado, de tres metros y medio, con el pecho abierto y un núcleo que estalla en una onda que marchita (pisos 23 y 24).
* **Nuevas criaturas del bestiario:** el **Oso de Retazos**, un osito de peluche remendado con un botón por ojo que, al acercarte, revienta las costuras del vientre en unas fauces de dientes de aguja, saca garras negras y le brilla un ojo rojo; su mordida rompe armadura (pisos 19, 22 y 24). Y **Sonrisa**, una roca con cara que duerme quieta como un peñasco y, al despertar, abre una sonrisa de dientes de piedra, le brillan los ojos y rueda contra ti dejándote aturdido; las flechas casi no le hacen daño (pisos 5 y 24).
* **Piso 4:** los hongos gigantes brillantes ya no tienen el sombrero flotando separado del tallo.

---

## 🔗 Anillo de Enlace, Tarro San Mortis y Jureles con Patas
* **Anillo de Enlace:** se lleva en el cuello o como pulsera y llama a los portadores cercanos.
* **Tarro San Mortis** (`/sanmortis`) con sus **jureles con patas**, que hacen un ritual de baile cada pocos minutos y dan patadas voladoras.

---

## 🧹 Otras Correcciones
* La comisión diaria del Gremio da una **bolsa de botín** que se vacía en el inventario, en vez de un cofre con llave.
* Sonidos de misión completada más suaves.
* Créditos: se añadieron los autores de las 14 skins de los vecinos del pueblo (ver `CREDITS.md`).

---

## ⚔️ Clases Base y Evolución
* **Seis clases base** al nivel 50: Guerrero, Asesino, Tanque, Mago, Sanador y Cultivador, cada una con una habilidad activa y una pasiva.
* **Evolución al nivel 150:** una prueba invoca a un jefe; al vencerlo evolucionas a una de las clases avanzadas de tu clase base y conservas su árbol. Quien ya tenía una clase avanzada la mantiene como evolucionada.
* **Árboles de habilidades rehechos** como grandes redes de unos 100 nodos por clase (nodos de estadísticas pequeños y grandes, y las habilidades de la clase); las clases base tienen redes de unos 60. Se cargan desde archivos de datos. Un punto cada 3 niveles desde el nivel 30; a los jugadores se les devuelven una vez todos los puntos gastados.
* Pestañas Base / Evolución, botón Evolucionar y ventana de evolución, en estilo clásico y Full Dive.

---

## 🛠️ Herramientas que Suben de Nivel por su Trabajo
* Picos, palas y azadas ya no suben de nivel matando: ganan experiencia minando, cavando y cultivando, y desbloquean habilidades a los niveles 10, 25, 50, 75 y 100 (las hachas siguen siendo armas y también las ganan talando).
* **Pico:** minado más rápido, fortuna en menas, minado de vetas, menas fundidas. Su nivel suma a la Fortuna (+1 desde el nivel 10, +2 desde el 75, +3 al 100): Fortuna III en un pico de nivel 75 mina como Fortuna V.
* **Hacha:** talado más rápido, árbol entero de un golpe, replanta el brote, troncos extra.
* **Pala:** cavado más rápido, 3x3 y luego 5x5, hallazgos en la tierra.
* **Azada:** arado 3x3 y luego 5x5, cosecha 3x3 que replanta, cultivos extra.
* Agachado se rompe un solo bloque. La descripción del objeto lista las habilidades.

---

## 🎮 Full Dive (SAO): Más Ventanas
* Versiones Full Dive de: mando de sombras (escuadrones, rango, nivel, vida y bajas de cada sombra), fábulas, ajustes de interfaz, elección de clase, árbol de clase (nodos redondos, arrastrar y hacer zoom), cultivo (reino, Qi, meridianos, habilidades del Dao), gremio (miembros, bendiciones, tesorería) e inventario normal.
* Las ventanas grandes se encogen para caber en pantallas pequeñas (854x480).
* El menú de pausa suena como el menú principal. La tarjeta y el panel muestran bien el nivel y el rango, y las monedas con su símbolo.

---

## 🗼 Gran Torre y Puertas
* **Menas en todos los pisos:** las del juego (carbón, hierro y cobre desde el piso 1; oro, redstone, lapislázuli, diamante y esmeralda más arriba) y las de la torre en más vetas y más grandes, a la altura donde se cava.
* **Cada mundo tiene su propia tierra de la torre:** los pisos mantienen su estilo pero cambian con la semilla del mundo (los mundos con pisos ya generados no cambian).
* **Columna de luz azul** que marca la subida de cada piso, visible aunque esté lejos. El aviso "Algo te llama" se repite cada 30 segundos.
* Los jefes de la torre ya no son World Boss.
* Cada piso tiene nombre traducido (en el chat al llegar, en la lista de pisos y en el mapa).
* **Puertas:** vuelve el gran mapa clásico de mazmorra (27 salas), distinto en cada puerta: reflejado, salas desplazadas, atajos opcionales y el jefe en una de tres salas (las otras dos son santuarios de tesoro). Ya no aparecen acechadores del Limbo dentro de las puertas.
* Las criaturas hostiles sueltan Monedas del Despertar y a veces un Núcleo de Monstruo de su rango.

---

## 💰 Economía
* Una Moneda del Despertar vale 1000 en el saldo del Sistema. Las criaturas sueltan menos monedas pero valen mucho más; los cofres de tesoro, una décima parte de antes.
* La comisión diaria del Gremio da una bolsa de botín.

---

## 🧑‍🤝‍🧑 NPCs y Gremios
* **Rutina diaria para los NPC del gremio:** hasta 12 paradas, cada una con lugar y hora del día; el NPC camina a la parada que toca y se queda cerca, todos los días.
* Los NPC del gremio luchan contra monstruos hostiles (siguiendo, de guardia o en su rutina), nunca contra creepers ni criaturas neutrales tranquilas.
* Los vecinos con lugar de trabajo (herrero, taberneros, sacerdote, bibliotecario, comerciante) se quedan cerca de él.
* Los diálogos de los NPC de las series ya están traducidos.
* El inspector de la Asociación no se puede matar por accidente y sus tablas de rangos muestran los niveles reales de cada rango.

---

## 🗡️ Armas de Solo Leveling
* Nueve armas nuevas: **Daga de Baruka, Monzón Azul, Sombra Carmesí, Dagas del Rey Demonio, Rugido de Ennio, Asesina de Caballeros, Sombra Lunar, Hoja Fantasma y Colmillo Venenoso Verdadero de Kasaka**, cada una con su pasiva (escarcha, ola, fuego, rayo en cadena, rompearmaduras, eclipse, purificación, neurotoxina; las dagas hacen más daño por la espalda). Traducidas en todos los idiomas.
* Las armas celestiales pegan más fuerte (espada 25-33, mandoble 33-41, guadaña 29-37, lanza 27-35).
* Nuevo Anillo de Enlace: banda redonda con una línea blanca y una gema pequeña.
* **Deseos (gacha):** animación nueva del meteoro: cielo estrellado, una estela que se afina, el color que se descubre a mitad de la caída (morado o dorado), y al aterrizar un destello, un anillo de luz y chispas.

---

## 🐉 Kaisel
* **Kaisel vivo y Kaisel de sombra**, montables, con su modelo y sus animaciones (quieto, caminar, volar, planear, vuelo rápido y dos ataques), del mod Solo Craft: Reawakening (licencia Apache 2.0).
* En vuelo montado sube y baja según hacia dónde mires; espacio para despegar.
* Ya no se desliza al caminar: los pasos son más amplios y siguen su velocidad; quieto respira y mueve cuello, alas y cola.

---

## 🏪 Tienda, Misiones y Despertar
* **Tienda del Sistema rediseñada** en dos paneles: catálogo con rango requerido y precio, e inspección del objeto (vista grande, historia, estadísticas, cantidad y botón de compra). Las compras piden el **Rango Evaluado** del cazador, no solo el nivel.
* **Misiones en dos pestañas:** Diarias (Entrenamiento del Monarca: flexiones, abdominales, sentadillas y carrera, con reloj de medianoche y aviso de penalización; y la Comisión de la Asociación según tu rango) y Escenario (Canal Dokkaebi de Omniscient Reader).
* **Despertar en la Gran Torre:** el Sistema se despierta en el Altar de Origen tocando el Cristal de Potencial (se quitó el método de picar 16 bloques y matar 5 monstruos). Mientras duermes, la pestaña Misiones indica dónde está la Gran Torre más cercana.
* **Escuadrones de sombras a tu gusto:** cada sombra se puede mover a cualquier escuadrón (I a IV) con un clic, y mezclar voladoras y terrestres.
* **Cinco físicos marciales nuevos** para el cultivo Murim: Cuerpo Indestructible de Vajra, Fénix Inmortal del Nirvana, Vacío Abisal, Cuerpo Sagrado de los Cinco Elementos y Rayo Celestial (11 en total).

---

## 👹 Criaturas
* **Baran:** textura nueva hecha para su modelo (antes salía con bloques cian rotos), también en su versión de sombra.
* **Bestia Acorazada de Jade Espiritual** rehecha desde cero: un qilin acorazado con cuatro placas de jade sobre el lomo, cresta de púas, cuernos, melena y barba, mandíbula que muerde, cola con maza de púas, y ojos y vetas que brillan.
* **Sonrisa (Grin):** dormido es una roca sin boca y despierto muestra la sonrisa (ya no tiene una losa tapándole la boca); rueda hacia delante al moverse y ataca rodando hacia delante.
* **Lawachurls y Guardián de las Ruinas** corregidos: texturas completas, piernas y brazos que se mueven bien, salto con golpe en los tres Lawachurls, ataque y misiles del Guardián, y su tamaño ajustado a su caja de golpe.
* El huevo del jefe de pelo rojo ahora se llama **Huevo generador del Monarca del Abismo**.
* Caras de los orcos: ojos más arriba y más separados.

---

## 🐟 San Mortis
* Los jureles bailan con música: un minuto de canción, y el baile termina con ella. `/sanmortis dance` empieza uno ya.
* Ahora viven y bailan **abajo, en círculo alrededor de la lata**, y el primer baile llega dos minutos después de construirla.
* **La lata es irrompible** (ni a mano ni con explosiones); `/sanmortis remove` la libera.
* Los jureles sueltan **Jurel**, que se puede cocinar (horno, ahumador o fogata) en **Jurel cocinado**.
* **Las latas aparecen solas en la costa** del Overworld (playas o tierra junto al mar, también con mods de terreno), como mucho una cada 1024 bloques.
* La barandilla del techo queda unida, sin postes sueltos.

---

## 🖥️ Interfaz
* Interruptor del minimapa en los ajustes de interfaz.
* Interruptor de **fantasma al morir**: apagado, vuelves como tú mismo y la lápida sigue guardando tus cosas.
* Las ventanas clásicas dibujan su marco por piezas, así las esquinas y el adorno no se deforman en pantallas anchas. El árbol de clase llena la pantalla sin solaparse.
* La notificación del Sistema se encoge en pantallas pequeñas.
* Las animaciones de las puertas, el minimapa, el mapa y las auras ya no van a saltos en mundos viejos.
* El rastreador de la Asociación nombra su punto de ruta en tu idioma.
* Descripciones de 18 títulos traducidas; las criaturas de otros mods sin traducción muestran un nombre limpio en vez de la clave.
* Paimon y las corredoras Uma ya no salen como amenaza ni suben de nivel como enemigos.

---

## 🛠️ Correcciones
* **World Boss:** criaturas comunes salían como "WORLD BOSS" porque su nombre contenía una palabra de jefe (hi**lich**url contiene "lich", man**drake** contiene "drake"). Ahora se comparan palabras enteras, y las criaturas que ya tenían el título lo pierden al cargar y recuperan su vida normal. Los World Boss de otros mods tienen al menos la vida de su nivel y su nombre traducido.
* **Hipódromo Uma:** las corredoras salen dentro de los cajones de salida, la carrera se gana en la línea de meta real, no se quedan atascadas en el bordillo y las vallas blancas quedan unidas. Las estructuras ya no colocan su bloque de estructura.
* **Abismo:** las gradas de acceso a la plataforma están bien orientadas (en estructuras nuevas).
* **Abismo:** las gradas de acceso a la plataforma están bien orientadas, también en los portales ya construidos. La ventana del Abismo, rehecha al estilo del juego: los 8 pisos del Corredor en fila con estandartes detallados (su sala, una escalera hacia una puerta iluminada, el cofre en un rombo, sus estrellas) y una grieta morada hacia la Espiral; luz que respira, motas que flotan y el vórtice que gira. La vista de cada piso muestra el trastorno de las líneas ley, las tres salas con sus estrellas, **los enemigos de cada mitad en 3D** y la ventana "Tesoro de la sala" con las recompensas.
* **Abismo, más parecido al juego:** el fondo del Corredor redibujado (gran arco trenzado, ventana redonda de luz, arcos laterales) y la grieta hacia la Espiral con chispas en espiral y un latido de luz. Dentro ya no suena el ambiente inquietante del bosque deformado. La Llave del Desafío está derecha y se puede golpear bien. Los enemigos tienen un **nivel fijo por piso y sala** (el mismo que muestra la ventana), no el del jugador. Botón **"Salir del Abismo"** en el menú de pausa: se guarda el progreso y se reinicia el intento. El portal para bajar al siguiente piso es ahora un anillo de piedra negra con obsidiana llorosa y luz girando dentro.
* **Espiral Lunar (pisos 9-12):** se reinicia cada 14 días (estrellas y recompensas de esos pisos), con la cuenta atrás en la ventana. Cada piso tiene un **Registro** de quién lo superó, cuándo, con cuántas estrellas y con qué equipo (sus sombras e invocaciones). En la última sala de esos pisos puede aparecer un **jefe de otro mod instalado** (Cataclysm, Twilight Forest, Ice and Fire...); se desactiva con `abyss_modded_bosses` en `custom_bosses.json`.
* **Arreglos:** el juego ya no se cierra al terminar el último piso; volver del Abismo o viajar a Teyvat ya no te deja enterrado, y la plaza de llegada antigua que flotaba tiene columnas; las armas celestiales (como el Mandoble del Juicio) ya no caen de los monstruos; el mensaje de Protogemas depositadas ya no muestra "%s"; el panel de misiones SAO muestra la misión del escenario.
* Se quitó el motor de combate ACE abandonado.
* **Zona de Penalización:** al acabar el reloj el juego ya no se cierra. Los gusanos de arena mueren de verdad: estallan en arena a lo largo de todo el cuerpo, y su barra de vida ya no queda vacía en pantalla hasta reiniciar el servidor.
* **Tienda:** los objetos que piden una versión del Sistema (por ejemplo, la Biblioteca de Encantamientos de Apotheosis, que pide Sistema 4.0) lo dicen, en vez de mostrar solo el rango.
* **Tarro San Mortis:** ahora sí es irrompible, también las latas construidas antes de la protección (las reconocen sus jureles). `/sanmortis remove` sigue liberando una.
* **Jurel y Jurel cocinado** con texturas nuevas al estilo de los peces de Minecraft (en diagonal, con borde, aletas, cola en V, línea lateral y marcas de parrilla en el cocinado). El jurel con patas tiene la piel con el moteado del juego.
* **Abismo:** el fondo ya no "respira" con franjas cuadradas; ahora gira de verdad, como en el juego: un gran anillo con su esfera de marcas y runas, y una estrella dentro que gira al revés (violeta en la Espiral, aguamarina en el Corredor).
* **Mapa Diorama:** si tienes otro mod de mapa (JourneyMap, Xaero's Minimap o World Map, FTB Chunks, VoxelMap, Antique Atlas, Map Atlases…), el mapa incluido se apaga solo, para que no haya dos mapas a la vez.
* **Abismo, más cambios:**
  * Se quitaron los pitidos raros que sonaban todo el rato dentro: eran los sonidos del bosque deformado, que seguía en arenas de mundos viejos.
  * La grieta hacia la Espiral es un remolino que gira de verdad.
  * Los enemigos tienen un nivel fijo por piso y sala, alto, porque es la última dimensión.
  * La Llave del Desafío tiene textura nueva: pedestal de piedra con runas, espada carmesí con guarda dorada y anillo de cristales. Ya no se gira al activarla.
  * El Monolito es sólido como un bloque: los enemigos chocan con él y lo golpean, y ya no gira ni se tambalea con cada golpe.
  * Al superar una sala sale la ventana **"Desafío completado"** con sus estrellas, el tiempo restante y los botones Siguiente sala / Siguiente piso, Salir del Abismo y Quedarse; el cofre se abre solo. Entrar en el anillo de descenso también sigue funcionando.
  * En mundos nuevos, la isla del Abismo aparece **en el mar**, lejos del inicio, en vez de en tierra rompiendo lo que haya alrededor.
* **Mitachurl arreglado:** vuelve el diseño original de Genshin Nature Revived, ahora con el modelo exacto de cada tipo (hacha ardiente y muro de escudos de madera), así que la textura encaja en su sitio; el hacha brilla en la oscuridad. Antes se mezclaban piezas de los dos y se veía roto.
* **Abismo, más arreglos:** ya no suena la campana extraña al entrar (ni el eco de cueva); el Monolito sale de pie y en su sitio (antes estaba al revés y medio hundido), con textura nueva, sólido, y sin partículas alrededor, igual que la Llave. Los premios del cofre van directo al inventario y la ventana "Desafío completado" sale después de abrirlo; su texto y botones se ven más grandes. Los enemigos del Abismo van del nivel 1000 al 1880.
* **Todas las criaturas del mod sueltan algo:** si una muere a manos de un jugador sin soltar nada, deja lo que le corresponde (cuero, hueso y carne las bestias como el tigre o el lobo; pepitas los orcos y goblins; ojos y perlas el contemplador; hierro y redstone las máquinas de las ruinas; etc.).
* **Trébol de Tracen y su tienda:** su descripción ya no promete cosas que no existían. Se gana en cada tirada de Umas (1, 3 o 20 según la rareza) y se usa en dos sitios: en el menú de Umas del hipódromo (5 tréboles = 5 fragmentos de la Uma elegida) y en la nueva **Tienda de Tréboles** del banner de Umas: Boleto de Tracen (10), 150 Quilates (8), un premio 4★ del banner de Genshin (40) y un arma 5★ de Genshin garantizada (200).
* **Jureles en el mar:** pescando en el océano o en la playa, un tercio de los peces que pican son Jureles. Además, los jureles con patas ahora nadan y aparecen solos en los océanos; a la hora del ritual salen del agua, van a bailar alrededor del Tarro San Mortis más cercano y luego vuelven al mar.
* **Subespacio, duplicación corregida:** lo que sacabas desde el menú del Subespacio fuera de tu parcela seguía en los cofres de la parcela, y al entrar o al comprar una Expansión volvía a aparecer. Ahora los cofres de la parcela y el menú siempre tienen lo mismo, también con los premios del gacha que van al Subespacio.
* **Subespacio con mods de almacenamiento (Sophisticated Storage/Backpacks):** un clic en un montón de más de 64 ya no se lleva el montón entero, solo uno normal; y al cerrar el menú, cualquier montón gigante que tengas en el inventario se separa en montones normales.
* **Nueva clase base: Nigromante.** El Monarca de las Sombras ahora sale del Nigromante, como en Solo Leveling, y no del Asesino. Su prueba de evolución es contra Igris. Tiene su propia red de habilidades:
  * **Extracción de Sombra** (activa): levanta como sombra el alma que estás mirando, de una en una (hasta tres intentos por alma). Las almas aparecen al matar enemigos siendo Nigromante.
  * **Cosecha de Almas** (pasiva): cada alma extraída te cura 2 corazones y te devuelve 10 de maná.
  * Con el Nigromante también se pueden extraer con clic derecho los cadáveres de Ice and Fire.
* **Asesino:** ya no evoluciona a Monarca de las Sombras, solo a Asesino del Vacío. Los Monarcas que ya venían del Asesino conservan su red de Asesino y lo que aprendieron en ella.
* **Velocidad por Agilidad:** al pasar el ratón por el interruptor ahora se ve cuánta velocidad te da en ese momento. Con la Agilidad en su valor inicial (5) es +0%: hay que subir puntos de Agilidad para notarla.
* **Título "Conquistador Supremo del Sistema":** ya no se da al matar el primer jefe ni al llegar al nivel 40. Ahora pide conquistar la Torre de Rankers, la Pagoda de las Nueve Tribulaciones, la Secta de Sangre y el Laberinto de Yggdrasil; quien lo tenía sin cumplirlo lo pierde al entrar.
* **Comando `/setclass <clase> [jugador]`** (admins y creador): cambia de clase al momento, a una clase base o a una evolución, para probar las clases.
* **Jefes:** el nombre ya no dice "WORLD BOSS" (el marco de jefe ya lo indica); queda solo el nivel y el nombre, como "[Lv. 150] Beru, the Ant King".
* **Beru, el Rey Hormiga:** suena como sus hormigas pero más grave, con chillido al alzar el vuelo y al golpear, en vez de los sonidos genéricos de antes.
* **Escala de daño de las armas por mundo:** las armas especiales siguen el orden de los mundos.
  * Overworld (las de Solo Leveling y las básicas): 10 a 18.
  * Torre de Rankers (Lambent Light, Dark Repulser, Elucidator, Espina Trascendente): 21 a 26.
  * Isekai (Martillo de Adamantita, Mandoble de Valter, Espada Sagrada de Asrahan, Guadaña del Dragón Negro): 30 a 38.
  * Murim (Feijian de Jade, Espada Voladora, Guandao del Dragón Astral, Espada del Demonio Celestial, Espada que Sacude el Cielo, Espada de Flor de Ciruelo): 42 a 52.
  * Teyvat (armas de Genshin y del gacha según sus estrellas, Báculo de Homa, Espada de Protogema): 55 a 68.
  * Únicas: Guadaña del Caballero del Terror 72, Mandoble del Vacío 75, La Doncella 80.
  * **Mandoble del Creador: 100**, siempre el arma más fuerte del juego.
* **Abismo, Monolito y Llave:** ya no salen volando con habilidades que lanzan todo al aire (como la del Mandoble del Creador), y esa habilidad ya no golpea al Monolito, la Llave, tus sombras ni tus mascotas.
* **Abismo, Monolito y Llave rehechos al estilo de la Espiral:**
  * **Monolito** (salas de defender): obelisco de cristal turquesa que se angosta hacia arriba, con líneas doradas, sobre una base escalonada de pizarra rodeada de rocas afiladas. Ahora es él quien empieza la sala: aparece apagado, y al tocarlo o golpearlo sus líneas doradas se encienden, el cristal se eleva un poco y empieza el combate para defenderlo.
  * **Llave** (salas de derrotar enemigos): bastón rojo flotante con un marco en el centro, cabeza con cuernos que suben y se curvan hacia dentro, y un cubo azul de alambre que gira encima y brilla. Al activarla desaparece.
* **Casa del Árbol (Miyabi y Ellen) rehecha:** antes era una caja de tierra con el piso cuatro bloques arriba; Miyabi y Ellen salían enterradas y el cofre con premio no era el que existía. Ahora es un gran árbol de jungla con una plataforma de madera con baranda, una casita alrededor del tronco (puerta, ventanas, faroles, macetas y el cofre con su premio), una copa frondosa con luces y lianas, y una escalera desde el suelo. Miyabi espera dentro de la casa y Ellen al pie de la escalera.
* **Corregido:** la vista de las gafas del título de Creador se quedaba puesta después de quitarte el título; ahora solo funciona con el título equipado.
* **Gacha de las Umas, como en la serie:** el cajón de salida se pinta de plata, oro o arcoíris según la rareza antes de abrirse, tiembla mientras espera y a veces sube de color con un destello justo antes de abrir. En la tienda de Paimon, el precio de los destinos muestra la Protogema de verdad.
* **Niveles mínimos por mundo:** de los 2000 niveles, las criaturas de Isekai nunca bajan de nivel 300, las de Murim de 600 y las de Teyvat de 1000 (siguen subiendo con el jugador por encima de eso). Los slimes de Teyvat ya no pierden el nivel del nombre al recibir su elemento.
* **Deseos:** si no tienes 10 destinos, la tirada x10 usa los que tengas y paga el resto con Protogemas (160 cada uno), como en Genshin; el botón muestra la mezcla. La tienda de Paimon tiene una fila para comprar destinos (1 o 10, entrelazados o conocidos) con Protogemas. El banner de Star Rail tiene su propio fondo espacial (galaxia, nebulosas, planetas y las vías del Expreso Astral) y todas las cartas de banner tienen ahora brillo, anillos, rayos de luz y polvo en vez de un color plano.
* **Gacha:** el fondo de los deseos es un cielo nocturno pintado, con nebulosa, Vía Láctea, constelaciones, montañas y un mar de nubes, y de vez en cuando una estrella fugaz. El gacha de las Umas es ahora un **cajón de salida** de carreras: ocho puestos numerados con sus colores, puertas de rejilla que se abren de golpe, polvo, y la luz del puesto central que dice la rareza.
* **Anillo gigante, más fluido:** la vista del otro lado se construye con un tiempo fijo por fotograma y ya no rehace lo que llega sin cambios (eso causaba los bajones de FPS cerca del anillo). Al cruzar, lo que mostraba el anillo se queda como el terreno alrededor hasta que el juego dibuja el suyo, así el otro mundo ya está ahí al llegar en vez de cargarse a tu alrededor.

---

## 🚪 Puertas de Ruinas y Catacumbas Olvidadas
* **Puertas de Ruinas:** mazmorras nuevas hechas con piezas de DungeonZ (ver `CREDITS.md`), con rango, un portal de verdad dentro de su ruina de entrada y botín según el rango.
* **Catacumbas Olvidadas:** un tipo de Puerta que aparece sola por el mundo, con su propio portal. Su jefe es el **Señor Esqueleto**, con bastón, hechizos y cristales que brillan.
* **Corregido:** el juego se cerraba al entrar en el portal de una Puerta; salir de una Puerta ya no te mete en otra partida nueva; las salidas se abren como bloques de portal; no quedan postes de muro en el foso del portal de la mina.
* **Colmillos en Picado:** habilidad nueva de dagas dobles con las animaciones de Epic Fight; el salto apunta al enemigo más cercano.

---

## 🕳️ Grutas Abisales
* Cuevas que se encuentran explorando el Overworld: una red de túneles cerrada, con caminos anchos y pendientes naturales.

---

## 🗼 Gran Torre: el Comienzo
* **Una sola Gran Torre cerca del punto de aparición.** A su lado está la agencia de la Asociación de Cazadores.
* **Tablón de misiones** en una repisa y **Cristal de medición** grande con su propio pedestal.
* Santuarios de llegada en cada piso, un vórtice de portal que se queda dentro de su marco y un altar abierto sencillo.
* **Pisos 1 al 15 con terreno propio** (el piso 9 con los cerezos de Minecraft), y las salidas de los pisos ya no quedan escondidas entre los árboles.
* **Piso 10, la Cámara del Titán Colosal de Sculk:** una cueva para explorar, con el titán como jefe (modelo propio, piel verde azulada, corazón brillante en la caja torácica y siete ataques que se ven venir).
* Arreglados el bucle del portal de la torre, el Guardián de Jade y las recompensas del tablón.

---

## 🏰 Estructuras Nuevas
* **Castillo del Rey Demonio**, **Gruta del Inmortal**, **campamento de guerra de Valter**, **santuarios de las islas elementales** y una **ciudadela en el cielo**.
* Los cofres del campamento de expedición de cazadores, del puesto en ruinas del observador de estrellas y de la estela del saber antiguo tienen botín propio.

---

## ☄️ Meteoritos y Zona de Penalización
* Los meteoritos caen de formas distintas, dejan una estela propia y se oyen desde la dirección del impacto.
* **Los cráteres se curan solos a los 5 días**, salvo que alguien haya construido en ellos.
* **Zona de Penalización:** solo llega después de 5 días de misiones diarias fallidas. Los gusanos de arena cazan como gusanos de arena gigantes. Arreglada la salida del desierto.

---

## 👹 Orcos de Sangre
* Rediseñados como brutos enormes en estilo manhwa oscuro, en variantes roja y verde (los verdes con su propio nombre). Tienen codos y rodillas, ya no parpadean y se mueven mejor.

---

## ☯️ Cultivo Murim: 14 Reinos
* El cultivo pasa de 10 a **14 reinos**, con un botón **"Ver reinos"** que abre la jerarquía completa.
* Cada reino pide su píldora (Construcción de Cimientos, Ruptura de Reino, Transformación del Alma, Nueve Revoluciones y Renacimiento Primordial).
* La ventana muestra la densidad de Qi del lugar.
* `/dantian reinos` lista los reinos.

---

## 🏅 Títulos y Comunidad
* **Títulos exclusivos:** *El Sombrerero Loco* y *Orthax*, con sus bonificaciones y un aviso en pantalla al obtenerlos.
* Cinco títulos que todos los jugadores tenían desde el principio (uno de ellos Mítico) ahora piden su condición: gastar monedas, vencer jefes, conquistar Puertas, el evento de temporada o el nivel 900.
* **Discord:** botón en la pantalla de bienvenida y comando `/discord`.

---

## 🧹 Más Cambios y Correcciones
* **Sombras:** `/shadows deploy <escuadrón>` y `/shadows recall <escuadrón>` despliegan o retiran un escuadrón entero.
* **Ejercicios del Monarca más rápidos:** la Fuerza acelera las flexiones y sentadillas, y la Vitalidad los abdominales.
* Se quitaron las partículas de aura de rango alrededor de los jugadores.
* Las armaduras del mod se pueden encantar (Protección, Irrompibilidad, Reparación, Espinas y los encantamientos propios de cada pieza).
* La bonificación de velocidad de la Agilidad ahora se nota.
* **Pantalla de elección de patrocinador** rediseñada. El **Archivo de Mundos** solo aparece cuando las pestañas de mundos no caben, y está rehecho.
* La ruleta ya no da armas del gacha.
* **Santuario de bolsillo:** se coloca en tierra firme y no en el océano; la vuelta a la puerta funciona en mundos planos; se puede mejorar y las puertas de otros jugadores están protegidas; al mejorarlo ya no se duplican objetos.
* Al salir del Limbo apareces sobre el suelo del Overworld.
* Apuestas del casino, comprobación del creador y viajes entre dimensiones más seguros.
* Recetas de madera que faltaban y nombres manhwa de las clases.
* **Traducciones** completas en inglés, español, ruso y portugués.

---

## 🗺️ Mapa Diorama Actualizado
* **Mapas guardados y exportación** del mapa a imagen. Lista de dimensiones. Mapa nocturno con su propia luz. Cortes de cuevas.
* **Radar con categorías** de criaturas configurables, y posición del minimapa a elegir.
* **Puntos de ruta** con iconos, columnas de luz, orden y carpetas; teletransporte por comando.
* Borde del mundo y zonas reclamadas (FTB Chunks, Open Parties and Claims, Chunky) dibujados en el mapa.
* Compatibilidad con Cobblemon, Pixelmon, MineColonies y los trenes de Create.
* Nuevos idiomas del mapa: bielorruso, japonés y chino tradicional.

---

## 🌍 En Preparación: Aethelgard (no aparece todavía en los mundos)
* Base de la futura dimensión RPG **Aethelgard** (llanuras de Oakhaven, Elderwood, Tierras Altas, Mar Plateado y Niflheim) y del **anillo gigante** que lleva a ella, con vista en vivo del otro mundo a través del anillo y cruce sin pantalla de carga. De momento solo se coloca con `/giantportal`, mientras se termina.

---

# ⚡ Awakening System — Version 0.3.4
### *Tiered Apples, Penalty Zone, UI Overhaul & Community Credits*

* **Minecraft Version:** 1.21.1
* **Mod Loader:** NeoForge 21.1.249+
* **Java:** Java 21 LTS

---

## 🍎 Manzanas Escalonadas de Progresión
* **Tiered Apples:** Integración de manzanas de poder místico (Soul Alloy Apple, Spirit Jade Apple) con buffs ascendentes y balanceados.

---

## 🏜️ Zona de Penalización (Penalty Zone)
* **Biomas Desérticos Hostiles:** `penalty_desert`, `penalty_dunes` y `penalty_canyon`.
* **Jefe de Penalización — Giant Sandworm:** Bestia colosal segmentada de arena con modelo y animaciones dinámicas (`GiantSandwormHeadModel`, `GiantSandwormBodyModel`).

---

## 🖥️ Modernización de Interfaz (UI Overhaul)
* **ModernGuiHelper:** Estética pulida y unificada a través de los menús del Sistema.
* **SystemWelcomeScreen:** Pantalla de bienvenida inmersiva para nuevos cazadores.
* **Compatibilidad:** Manejadores de compatibilidad para Apotheosis y encantamientos Manhwa.

---

# ⚡ Awakening System — Version 0.3.3
### *The Monarchs, Limbo & Economy Overhaul*

* **Minecraft Version:** 1.21.1
* **Mod Loader:** NeoForge 21.1.249+
* **Java:** Java 21 LTS

---

## 👑 Boss Redesigns & The Ant King: Baran, Igris & Beru
* **Demon King Baran Redesign:** Complete visual and combat overhaul for Baran featuring brand-new 3D GeckoLib models, glowing demonic horns, magma-infused lightning attacks, and dynamic battle animations.
* **Blood-Red Commander Igris Redesign:** Reworked from the ground up with an imposing 3D armor silhouette, glowing visor effects, fluid greatsword slash combos, and his iconic crimson plume cape.
* **New Boss Integration — The Ant King, Beru:** Introduced Beru as a catastrophic apex predator boss! Features lightning-fast dash strikes, aerial swoops, paralyzing screech sound effects, and multi-phase combat.
* **Shadow Extraction Support:** Defeating these legendary monarchs allows Shadow Monarchs to extract them as elite Shadow Soldiers, preserving their custom models, animations, and battlefield abilities.

---

## 🏰 Dungeons & Red Gates Overhaul
* **Themed Multi-Chamber Dungeons:** Dungeons are no longer single static rooms. They now generate as continuous procedural gauntlets featuring progressive enemy waves, branching corridors, and grand boss chambers:
  * **Baran's Magma Domain:** Volcanic nether-brick bastions guarded by flame spirits and demon legionnaires.
  * **Igris's Throne Citadel:** Majestic chivalric halls and royal throne rooms surrounded by blood-red knights.
  * **Beru's Insect Colony:** Subterranean infested ant hives swarming with deadly Ant Soldiers.
* **Red Gate Peril:** High-rank dimensional rifts and Red Gates seal behind entering hunters, locking all exit portals until the dungeon boss is conquered.

---

## 🌌 New Dimension: The Limbo
Death inside a dungeon is no longer a simple respawn.
* **The Realm of Lost Souls:** Dying within any Dungeon Rift banishes your soul to **The Limbo**, a desolate, pitch-black wasteland shrouded in cosmic silence and dense fog.
* **Find the Exit Gates:** To return to the living realm with your equipment, you must navigate obsidian ruins, evade hunting horrors, and locate one of the cardinal Exit Portals.
* **New Horrors — Stalkers & Ophanim:** Beware of the entities dwelling in the mist. Featuring extended 64-block acoustic detection, you will hear their eerie calls and hovering wings long before they strike.

---

## 💰 Complete Hunter Shop & Economy Rebalance
The System Shop has been thoroughly overhauled to provide a balanced, challenging, and rewarding progression curve:
* **Strict Level-Gating on Mystery Boxes:** Mystery loot boxes are no longer available for early-game gambling. Players must earn their ranks to unlock them:
  * **Common Box:** Level 10 (350 Coins)
  * **Lucky Box:** Level 20 (850 Coins)
  * **Rare Box:** Level 35 (2,200 Coins)
  * **Epic Box:** Level 55 (6,500 Coins)
  * **Legendary Box:** Level 75 (20,000 Coins)
  * **Roulette Tokens & Altar:** Level 20 (3,500 Coins) & Level 25 (30,000 Coins)
* **Tiered Equipment & Weapon Level Caps:** All weapons, tools, and armors now have minimum level requirements corresponding to their power tier (Iron -> Diamond -> Netherite -> Soul Alloy -> Astralite -> Mithril -> Adamantite -> Spirit Jade -> Astral -> Celestial -> Primordial), eliminating overpowered skips.
* **Dimensional Progression Locks:** Advanced upgrade smithing templates, dimensional furnaces, and key relics are strictly locked behind dimension visits and System tiers (System 1.0 through System 4.0), preventing out-of-sequence gear acquisition.
* **Adjusted Coin Economy:** Item costs, pouch upgrades, and furnace investments have been scaled realistically to match high-level dungeon hunting yields.

---

## 🛡️ New 3D Armors & Legendary Equipment
* **Custom 3D Animated Armor Sets (GeckoLib):**
  * **Soulforge Plate:** Imbued with pulsating blue spectral fire and spiritual warding.
  * **Astralite Regalia:** Sleek starlight mail that amplifies mana capacity and critical strikes.
  * **Abyssal Vanguard Armor:** Heavy shadowy plate forged from dark rift metal for frontline survivability.
* **Matching Weapons & Tools:** Soul-imbued swords, heavy waraxes, katanas, pickaxes, and daggers with tailored attack animations and impact sound effects.

---

## 💎 New Minerals & Metallurgy: Soul Alloy & Astralite
* **Soul Alloy Progression:** Discover dormant Soul Alloy ingots and smelt them inside high-temperature Blast Furnaces to forge radiant **Ignited Soul Alloy Ingots**.
* **Astralite:** Rare celestial ore harvested across dimensional rifts, used for crafting high-tier magical catalysts and astralite gear.

---

## 🏛️ World Structure Redesigns
* **Nine Tribulations Pagoda Redesign:** Complete architectural overhaul of the sacred Overworld Pagoda with deepslate-crimson temple aesthetics, seamless entrance gate detection, and stability improvements.
* **Roulette Pavilion Redesign:** The Overworld casino pavilion has been visually redesigned with a grander oriental pavilion roof, decorative pillars, and improved roulette table layout.
* **Creative Pagoda Sandbox:** Build and customize your own trial arenas outside the dimension using `/pagoda arena [stage]` or `/structure pagoda_arena [stage]`, complete with an auto-placed Structure Block configured to save your custom arena NBTs.

---

## 📦 Visual Improvements & Polish
* **Dungeon Chest Visual Upgrade:** Enhanced textures across all 7 tiers of locked dungeon treasure chests, offering crisp high-definition visuals in hand, inventories, and placed worlds.
* **New Interactive Blocks:** Functional Roulette tables, Soulforge Altars, and ornate decorative masonry.

---

## ⚔️ Combat Stability, Shadow Scaling & Anti-Crash Guard
* **Server Anti-Crash Protection:** Added re-entrant combat cascade protection preventing `StackOverflowError` crashes during chain cleave attacks or damage reflection loops.
* **True Shadow Stat Retention:** Extracted Shadow Soldiers now preserve 100% of their fallen target's scaled maximum health, attack damage, and high-tier RPG levels.
* **Weapon Progression Filter:** Stackable utility items (bowls, food, ingots, blocks) are now strictly excluded from weapon leveling and attribute progression.

---

## 🎨 Open-Source & Asset Acknowledgments
* **Diorama Mod Integration:** Real-time ray-traced voxel minimap and tactical map engine created by **Dara Adedeji** (`SunkenInTime`) under the MIT License ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/diorama) | [GitHub](https://github.com/SunkenInTime/minecraft-orthogonal-map)).
* **Limbo Dimension Aberrations:** 3D entity models for the *Spider Head Monster*, *Tall Three Eyed Stalker*, and *One Eyed Stilt Walker* created by **`@ziiwplay`** under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
* **The Entity of Limbo:** 3D entity model for *THE_ENTITY* created by **`@nori`** under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
* **Skulk Golem:** 3D guardian model created by **`@elma_taburlas`** under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
* *(Complete open-source licensing documentation: [CREDITS.md on GitHub](https://github.com/Landa6543/Awakening-System/blob/main/CREDITS.md)).*

---


# 📜 Awakening System — Changelog v0.3.2

> **Version Transition:** `v0.3.1` ➔ `v0.3.2`  
> **Minecraft Version:** `1.21.1`  
> **Mod Loader:** NeoForge (`21.1.249+`)  
> **Java Version:** Java 21 LTS  
> **Release Target:** `awakening_system-0.3.2.jar`  

---

## 🛡️ 1. Critical Fix: Dungeon Dimension Isolation & External Mod Mob Infiltration Fix

A critical bug was resolved where players in Rank D dungeons encountered external mod mobs (e.g., wolves and ants from *Mo' Creatures*, Alex's Mobs, or creature mods) scaled up to Level 70–80, while native dungeon mobs properly stayed around Level 20:

* **Preservation of Overworld Distance Scaling (Core Feature):**
  * The core RPG system of **distance-based mob level scaling** (*"the further away from spawn, the higher the mob level"*) remains **100% active and preserved** throughout the Overworld and standard dimensions (Lv. 1–20 near spawn up to Lv. 80–100+ in distant zones).
* **Root Cause: Foreign Mod Mob Infiltration in Dungeon Realm:**
  * Dungeon instances generate inside their own dimension (`DUNGEON_REALM`) at distant coordinates (X >= 10,000, Z >= 10,000).
  * External creature mods were naturally spawning in the dark chambers of the dungeon dimension. Because those external mobs did not belong to the dungeon system, they were erroneously subjected to coordinate-based distance scaling, artificially inflating their level to 75–80 with extreme damage and health.
* **Triple-Layer Strict Dungeon Entity Isolation Filter:**
  * **Layer 1 (Natural Spawn Suppression):** `FinalizeSpawnEvent` cancels any natural or chunk-based mob spawn inside `DUNGEON_REALM`.
  * **Layer 2 (Highest-Priority Join Interception):** In `EntityJoinLevelEvent` (`HIGHEST` priority), any living entity entering `DUNGEON_REALM` that is NOT a player, NOT a player shadow soldier (`shadow_soldier`), NOT a tamed pet, and NOT a registered dungeon minion/boss (`dungeon_mob`) is immediately cancelled and discarded.
  * **Layer 3 (Active Session Tick Purge):** Active dungeon sessions continuously sweep a 150-block radius around the chamber every second (20 ticks), instantly purging and discarding any non-whitelisted foreign entities or leaked ambient creatures.
  * Dungeon instances are now strictly isolated: **ONLY** Awakening System dungeon minions, bosses, shadow soldiers, players, and pets can ever exist inside dungeons.
* **Baran & Igris Theme Minion and Boss Stat Scaling:**
  * Minions and mini-bosses in Baran and Igris themed chambers previously had hardcoded Rank S baseline stats (180–450 HP and 12–20 base DMG) across all ranks.
  * Health, attack damage, and armor now scale proportionally with dungeon rank tier (`tierRatio = Math.max(0.30D, (double) tier / 7.0D)`). In a Rank D dungeon, minions now appropriately possess 50–75 HP and 4–5 base attack damage.
  * Baran and Igris boss armor and knockback resistance now scale smoothly according to rank tier instead of having max armor in low-tier portals.

---

## 🌐 2. System Versioning & Dimension Side Tabs Progression Fix

* **Dimension-Based System Progression (Not Level-Based):**
  * System updates are tied directly to dimensional travel rather than player level. Passing into each realm unlocks its corresponding System tier:
    * **Default / Overworld:** Base System 1.0 (standard system screen without external dimension tabs).
    * **System 2.0 (Isekai Arrival):** Unlocked upon entering the Isekai Realm (`isekai_world`), enabling Overworld, Isekai, and Subspace navigation tabs.
    * **System 3.0 (Murim Arrival):** Unlocked upon entering the Murim Realm (`murim_world`), enabling the Murim navigation tab.
    * **System 4.0 (Teyvat Arrival):** Unlocked upon entering Teyvat (`teyvat_world`), enabling the Teyvat navigation tab.
    * When players are in any other realm, return navigation tabs remain accessible to allow easy transit back.
* **Fix for LAN / OP Privilege Bypass:**
  * Removed `isDev` check (`hasPermissions(2)`) from `getActiveSideTabs()`. Previously, any LAN host or player with operator status was flagged as a developer, bypassing dimensional progression checks and rendering all 5 dimension tabs at Level 1.

---

## 📦 3. Shop Mystery Boxes Level Lock Requirements

Minimum level requirements were introduced for purchasing loot boxes in the Hunter Point Shop:

* **Common Mystery Box (`box_common`):** Requires **Level 10** (Cost: 350 Coins).
* **Lucky Mystery Box (`lucky_box`):** Requires **Level 20** (Cost: 850 Coins).
* **Rare Mystery Box (`box_rare`):** Requires **Level 35** (Cost: 2,200 Coins).
* **Epic Mystery Box (`box_epic`):** Requires **Level 55** (Cost: 6,500 Coins).
* **Legendary Mystery Box (`box_legendary`):** Requires **Level 75** (Cost: 20,000 Coins).
* Purchase attempts below the required level display a clear notification stating the minimum level needed.

---

## ⚡ 4. Dungeon Break Event Fixes & Hardening

* **Chunk Pre-loading:** The event now pre-loads a 3x3 chunk area surrounding the rift prior to collapse, guaranteeing that terrain is fully generated and ready before monster emergence.
* **Persistence & Daylight Protection:** Monsters breaking through gain persistence (preventing despawn when players distance themselves) and sunlight immunity so the horde does not burn during the day.
* **Break Leader:** Every dungeon break spawns a designated Break Leader with buffed combat statistics and special naming.
* **Full Localization:** Translatable broadcast announcements and alerts configured for all languages (`es_es`, `es_cl`, `en_us`, `ru_ru`).
* **Cooldown Protection:** Added a 10-minute cooldown and 180-block radius restriction between consecutive breaks to prevent overlapping crises.

---

## 🗺️ 5. Biome & Dimension Titles Integration (Traveler's Titles)

* Implemented full compatibility with the **Traveler's Titles** mod format across all 40 mod biomes and 28 dimensions:
  * Dimension titles: `"travelerstitles.awakening_system.<dim>"` with matching hex color overrides `"travelerstitles.awakening_system.<dim>.color"`.
  * Biome titles: `"biome.awakening_system.<biome>"` and `"travelerstitles.biome.awakening_system.<biome>"` with custom hex colors `"travelerstitles.biome.awakening_system.<biome>.color"`.
  * Vanilla biome title overrides for Spanish locales (`travelerstitles.biome.minecraft.<id>`) ensuring that players in Spanish games see translated names rather than raw English terms.

---

## ⚔️ 6. Dokkaebi Server-Wide Timed Sub-Scenarios & Progressive Overkill Overhaul

* **Synchronized Global Server Event:**
  * Replaced individual, out-of-sync player tick triggers with a centralized server event (`onServerTick` every ~20 minutes / 24,000 ticks).
  * Dokkaebi Bihyung initiates a Star Stream emergency broadcast to all active Overworld hunters simultaneously with celestial chime acoustics.
  * Dokkaebi spawns near active players, and each hunter receives their own individual HUD tracking card and percentage progress bar.
* **Progressive Difficulty Tiers & Generous Timers:**
  * Scenario targets dynamically adapt according to highest hunter level on the server, with significantly extended hunt timers:
    * **Tier 1 (Basic Hunt):** 8–10 kills across **6:00 minutes** (increased to eliminate scarcity stress in quiet biomes). Base reward: **250 Coins, 400 EXP**.
    * **Tier 2 (Advanced Hunt):** 15–18 kills across **8:00 minutes**. Base reward: **400 Coins, 700 EXP**.
    * **Tier 3 (Elite Hunt):** 20–24 kills across **10:00 minutes**. Base reward: **600 Coins, 1,100 EXP**.
    * **Tier 4 (Star Stream Calamity):** 25–32 kills across **12:00 minutes**. Base reward: **850 Coins, 1,600 EXP**.
* **Universal Kill Detection:**
  * Fixed an issue where indirect kills failed to register. The engine now recognizes melee attacks, bow/trident projectiles, lingering Fire Aspect/poison damage, and kills performed by summoned Shadow Soldiers or tamed allies.
  * Properly tracks kills across all hostile entities, creature mods, and custom RPG mobs.
* **Escalating Overkill Rates & Constellation Gift Milestones:**
  * Exceeding the quota transforms the HUD card into a radiant gold frame and grants scaling rewards per extra kill:
    * **1–5 Extra Kills:** +50 Coins & +80 EXP each (Rank: *Combat Overload*).
    * **6–10 Extra Kills:** +75 Coins & +120 EXP each (Rank: *Destructive Frenzy*).
    * **11–20 Extra Kills:** +100 Coins & +160 EXP each (Rank: *Stellar Massacre*).
    * **21+ Extra Kills:** +150 Coins & +250 EXP each (Rank: *Absolute Calamity*).
  * Achieving Overkill milestones triggers real-time fanfares and awards physical Constellation Gifts deposited directly into player inventories upon completion:
    * **+5 Extra Kills:** +150 Coins donation bonus + **1x Mana Crystal Shard (Rank E)**.
    * **+10 Extra Kills:** +350 Coins donation bonus + **1x Mana Crystal Shard (Rank D)**.
    * **+15 Extra Kills:** +700 Coins donation bonus + **1x Mana Crystal Shard (Rank C)**.
    * **+20 Extra Kills:** +1,200 Coins donation bonus + **1x Mana Crystal Shard (Rank B)**.
    * **+30 Extra Kills:** +2,500 Coins donation bonus + **1x Mana Crystal Shard (Rank A)**.
* **Safe NPC Interaction & Automatic Resolution:**
  * Talking to Dokkaebi after reaching the quota will no longer forfeit remaining hunt time; Bihyung displays remaining time and encourages continuous Overkill hunting.
  * Hunters may Sneak + Right-Click to cash out early if desired.
  * Timer expiration automatically resolves all base rewards, Overkill coin/EXP bonuses, level-up calculations (`awardExperience`), and fireworks.
* **Full Trilingual Localization:**
  * All announcements, actionbar overlays, dialog prompts, rank tags, and completion breakdowns are fully localized with dedicated translatable keys in **Spanish (`es_es`, `es_cl`)**, **English (`en_us`)**, and **Russian (`ru_ru`)**.

---


# 📜 Awakening System — Changelog v0.3.1

> **Version Transition:** `v0.3.0` ➔ `v0.3.1`  
> **Minecraft Version:** `1.21.1`  
> **Mod Loader:** NeoForge (`21.1.249+`)  
> **Java Version:** Java 21 LTS  
> **Release Target:** `awakening_system-0.3.1.jar`  

---

## 🌲 1. Custom Woods, Logs & Worldgen Crafting Polish

A comprehensive registration and drop audit was conducted across all custom realm woods (**Cuihua, Adhigama, Sandbearer, Spirit Plum, Celestial Pine, Mana Wood, Eldoria, Aralia, Yumemiru, Otogi, and Maple**):

* **Axe Mining Speed Parity (`#minecraft:mineable/axe`)**:
  * Corrected tool tag assignments. All axe tiers (wooden, stone, iron, golden, diamond, and netherite) now harvest all custom logs, wood blocks, and planks at normal mining speeds with full efficiency.
* **Minecraft & NeoForge Tag Integration**:
  * Integrated all custom woods into standard tags: `#minecraft:logs`, `#minecraft:logs_that_burn`, `#minecraft:planks`, `#minecraft:fences`, `#minecraft:fence_gates`, and `#minecraft:wooden_fences`.
  * Guarantees full out-of-the-box compatibility with furnace fuels, recipe viewers (JEI/REI/EMI), and external storage mod filters.
* **Plank Crafting Recipes**:
  * Added recipes converting **1 Log ➔ 4 Planks** across all custom wood families, craftable in both crafting tables and the 2×2 player inventory grid.
* **Drop Tables & Harvesting**:
  * Fixed loot tables so that logs, stripped variants, leaves, stairs, slabs, fences, gates, doors, and trapdoors drop their intended item forms when mined.
* **Axe Stripping & Campfire Log Seats**:
  * Implemented right-click axe stripping behavior on custom logs.
  * Added the functional campfire log seat block (`CampfireLogSeatBlock`).

---

## 🗺️ 2. Standalone Tactical Minimap & Fullscreen World Map

A complete, built-in in-house map engine operating natively without requiring JourneyMap, Xaero's, or any other external map mod:

* **In-Game HUD Minimap (`HunterMinimapRenderer`)**:
  * Clean, border-accented tactical HUD minimap rendered in the corner of the screen.
  * Real-time coordinate HUD ($X, Y, Z$), active biome indicator, and smooth player heading pointer.
  * Position, scale, opacity, and visibility can be freely adjusted through the System HUD Editor (`GuiConfigScreen`).
* **Fullscreen Tactical World Map (`[M]`, `HunterWorldMapScreen`)**:
  * Accessible at any moment via the default **`[M]`** keybinding.
  * Fluid click-and-drag mouse panning and mouse-wheel zoom scaling.
  * Dimension switcher supporting the Overworld, Nether, End, Subspace, Abyss, and custom realms.
  * In-game time display, real-time cursor coordinates, and Day/Night surface lighting toggle.
* **High-Performance Binary Chunk Cache (`WorldMapChunkCache`)**:
  * Multi-threaded asynchronous chunk rasterization with zero tick lag or FPS drops during high-speed exploration.
  * Per-world binary cache files (`.bin`) stored in `run/awakening_map/` for instantaneous map reload.

---

## 🎨 3. JourneyMap / Xaero's Visual Parity & Shading

* **Natural Earthy Terrain Tints**:
  * Eradicated oversaturated, garish, and fluorescent coloring across biomes.
  * Applied JourneyMap's exact grass calibration formula: a $0.74\times$ luminance multiplier applied to vanilla biome grass tints to produce natural, soothing pasture greens.
  * Foliage and leaves calibrated with a $0.78\times$ factor for authentic forest canopies.
* **Northwest Hill-Shading Relief (3D Topography)**:
  * Dynamic hill-shading relief where northwest-facing slopes receive sunlight illumination and southeast-facing slopes receive topographic shadowing.
  * Accurately reveals cliffs, ravines, rolling hills, and mountain ridges with authentic depth.
* **Realistic Water Bathymetry**:
  * Depth-based alpha blending calculates water depth relative to the seabed or riverbed.
  * Renders shallow beaches and coastlines in translucent turquoise/aquamarine, transitioning into deep navy blue for ocean trenches.
* **Comprehensive Block Palette Mapping**:
  * Accurate pixel color rendering for stone, deepslate, gravel, sand, dirt, clay, snow, ice, terracotta, and realm ores.

---

## 📡 4. Mob Radar Dynamic Vertical Slicing & Density Control

* **Surface vs. Cave Vertical Culling**:
  * **Surface Exploration**: Subterranean entities dwelling in deep caverns below ground (`ly < surfaceY - 5.0`) are automatically culled using `Heightmap.Types.MOTION_BLOCKING`, keeping the map clear of cave clutter while on the surface.
  * **Cave / Underground Exploration**: When below ground, the radar automatically restricts mob display to a vertical slice of $\pm 8.5$ blocks relative to the player's current $Y$ level, showing only entities on the active floor or gallery.
* **Passive Herd Animal Proximity Limiter**:
  * Added density culling for clustered farm animals (cows, sheep, pigs, chickens, rabbits): clusters are capped at a maximum of 2 icons per 7-block radius.
  * Hard limits on rendered passive icons: maximum 10 on the minimap HUD, maximum 24 on the fullscreen world map.
* **Tactical Entity Radar Indicators (`EntityIconRenderer`)**:
  * Color-coded radar markers: Bosses (crimson skulls), Hostile Monsters (red markers), Passive/Neutral Creatures (green/yellow markers), Friendly NPCs & Players (distinct cyan/white pointers).

---

## 📍 5. Per-World Waypoint Isolation & GUI Brightness Fix

* **Per-World Waypoint Storage**:
  * Waypoints are now stored independently in `config/awakening_waypoints/<world_id>.json`, keyed by the world folder name (`sp_<folder>`) for singleplayer or server address (`mp_<ip>`) for multiplayer.
  * **Multi-World Bleed Bug Resolved**: Newly generated worlds start completely clean with **0 waypoints**, preventing waypoints from leaking across different saves.
  * Cache is automatically purged upon leaving a world (`ClientPlayerNetworkEvent.LoggingOut`).
* **Waypoint Editor GUI Brightness Fix (`WaypointEditScreen`, `WaypointListScreen`)**:
  * Fixed an issue where the RGB color picker wheel, sliders, and preview swatches appeared dark and washed out. The duplicate background gradient call (`super.render`) was removed, restoring 100% RGB brightness and color vibrancy.
  * In-game management: create, rename, coordinate edit, toggle visibility, and assign custom RGB colors.
* **In-World 3D Beacons (`WaypointBeaconRenderer`)**:
  * Renders customizable vertical light beams and floating distance tags in the 3D game world pointing toward marked waypoints.

---

## 🧘 6. Meridian System & Dantian Cultivation Engine

* **Interactive Acupuncture & Acupoint Interface (`MeridianScreen`)**:
  * Dedicated interactive body cultivation screen accessible from the hunter menu.
  * Renders human meridian channels and major extraordinary vessels (*Ren Mai* - Conception, *Du Mai* - Governing).
  * Major acupoints (*Baihui*, *Yintang*, *Tanzhong*, *Qihai*, *Mingmen*, *Yongquan*, Lower Dantian) featuring distinct visual states: Locked, Breakthrough Ready (pulsing glow), and Opened (radiant golden/crimson energy).
* **Alchemical Breakthroughs & Stat Progression**:
  * Opening acupoints consumes alchemical breakthrough pills (*Marrow Cleansing Pill*, *Realm Breaking Pill*) and cultivation Qi.
  * Unlocking complete meridian circuits grants permanent passive boons: amplified Qi regeneration, reduced skill cooldowns, increased physical damage, and natural regeneration.

---

## ⚔️ 7. Epic Fight Official Integration & Native ACE Engine Retirement

Based on player testing and community preference, the native ACE prototype has been cleanly retired in favor of **official out-of-the-box integration with Epic Fight (1.21.1)**:

* **Clean Decoupling & Keybinding Liberation**:
  * Deactivated client attack interception mixins (`AceMinecraftClientMixin`), restoring standard attack event flow so Epic Fight controls combat inputs.
  * Completely freed up the **`[R]`** keybinding (previously reserved for ACE stance toggles), allowing Epic Fight to use its default key for toggling Mining/Combat mode without key collisions.
  * Retired the custom ACE HUD overlay to eliminate visual clutter and avoid overlaps with Epic Fight's UI.
* **72 Custom Weapon Capabilities Configured for Epic Fight**:
  * Audited and optimized weapon capability definitions across all custom weapons in `src/main/resources/data/awakening_system/capabilities/weapons/`:
    * **Swords & Rapiers** (e.g. *Absolution, Soul Solid, Primordial Sword, Celestial Sword, Astral Sword*): Configured to `epicfight:sword` (fluid one-handed and dual-wield combos).
    * **Greatswords & Scythes** (e.g. *Black Dragon Scythe, Crimson Moon's Semblance, Creator Claymore*): Configured to `epicfight:greatsword` (heavy two-handed cleaves, momentum swings, and sweeps).
    * **Katanas & Feijian / Murim Blades** (e.g. *Plum Blossom Sword, Sky Demon Sword, Heaven Shaking Sword, Tailless, Shunguang Sword*): Updated to `epicfight:tachi` (elegant two-handed slashing moveset with authentic posture).
    * **Spears & Polearms** (e.g. *Astral Guandao, Black Tassel*): Configured to `epicfight:spear` (long-reach linear thrusts and guard stances).
    * **Daggers** (e.g. *Hunter Dagger*): Configured to `epicfight:dagger` (rapid alternating slashes and dual-dagger combinations).
    * **Axes & Warhammers**: Configured to `epicfight:axe`.
    * **Bows** (e.g. *Amos Bow, Aqua Simulacra, Thundering Pulse, Skyward Harp, Polar Star*): Configured to `epicfight:bow`.
* **Dual Combat Mod Ecosystem Support (Better Combat & Epic Fight)**:
  * **Better Combat Integration**: All 63 melee weapons have custom attributes configured in `data/awakening_system/weapon_attributes/` (katanas with `bettercombat:katana`, scythes with `bettercombat:scythe`, claymores with `bettercombat:claymore`, spears with `bettercombat:spear`, daggers with `bettercombat:dagger`, swords, and axes).
  * **Epic Fight Integration**: All 72 weapons and bows have capabilities configured in `data/awakening_system/capabilities/weapons/` (`epicfight:sword`, `epicfight:tachi`, `epicfight:greatsword`, `epicfight:spear`, `epicfight:dagger`, `epicfight:axe`, `epicfight:bow`).
* **System Active Skills & Spells Preservation**:
  * All right-click weapon abilities and custom hotkey System abilities (Cultivation Qi `[C]`, Stigmas `[Y]`, Spell Slots `[V]`, `[G]`, `[B]`, `[X]`, Martial Arts, Armor Skills `[Z]`, and Hunter World Map `[M]`) remain fully accessible and responsive alongside both Better Combat and Epic Fight.

---

## 🐉 8. Celestial Dragon Serpentine Animation & Wyvern Kinematics

* **Oriental Serpentine Wave Kinematics (`CelestialDragonModel.java`)**:
  * Eliminated rigid linear poses during flight and hovering.
  * Implemented an authentic sinusoidal wave propagation function across all 31 articulated vertebrae:
    $$\theta_{\text{yaw}}(i) = A_{\text{yaw}} \times \sin(\omega t - i \cdot k) + \Delta \text{yaw}_{\text{turn}}$$
    $$\theta_{\text{pitch}}(i) = A_{\text{pitch}} \times \cos(\omega t - i \cdot k) + \Delta \text{pitch}_{\text{turn}}$$
  * Produces continuous, majestic 3D helical swimming curves through clouds and sky.
  * **Jitter & Phase Shearing Elimination**: Resolved the issue where the dragon shuddered violently upon spawning:
    * **Constant-Frequency Monotonic Time**: Fixed phase shears caused by multiplying dynamic speed by elapsed ticks ($t \cdot \Delta \omega$). The wave now uses a strictly continuous, monotonic frequency ($\omega = 0.085\,\text{rad/tick}$), smoothly scaling only the amplitude with movement speed.
    * **Stable Kinematic Root**: Kept `neck1.yRot = 0.0F` so downstream body joints do not whip the world coordinate anchor of the 31-joint chain.
    * **Vertebral Differential Clamping**: Clamped inter-segment rotational differentials to $\pm 14^\circ$ yaw and $\pm 10^\circ$ pitch with cervical ease-in dampening, eliminating snapping during sharp turns.
    * **Smooth Sitting Damping**: Replaced boolean pose popping with continuous amplitude damping ($0.35\times$) during sitting.
  * **Fluid Whiskers, Tail Tuft & Claws**: Silk-like trailing inertia on dragon whiskers, whip-like tail tuft swaying, and coordinated ethereal paddling gestures on divine claws.
* **Ancient Celestial Dragon Boss Colossal Coiling (`AncientCelestialDragonBossModel.java`)**:
  * Calibrated monotonic wave propagation across all 63 chain vertebrae (spanning $+100$ blocks in the world), rendering up to 1.8 full serpentine coils sweeping across the Murim sky at $\omega = 0.055\,\text{rad/tick}$.
  * Completely stabilized against jitter with clamped vertebral angles and root yaw locking.
  * Dynamic body tension and whip lash during roar and Qi breath attacks.
* **Wyvern 3D Flight & Banking Roll (`WyvernModel.java`)**:
  * Upgraded flat wing flapping to true 3D wing kinematics combining vertical flap, aerodynamic sweep, and feathering angle of attack.
  * Decoupled flight and ground frequencies to prevent phase jumping during takeoff and landing transitions.
  * Added realistic body banking roll when turning, tucked flight legs, and sinusoidal tail stinger whip.

---


# 📜 Changelog — Awakening System v0.2.1 (Hotfix)

## 🛠️ v0.2.1 Hotfix Highlights
- **Responsive Top-Up & Gift Card Store UI:**
  - Resolved window boundary overflow and layout clipping where modal borders, right columns, and titles were pushed off-screen on smaller resolutions or higher GUI scales (GUI Scale 3/4).
  - Implemented dynamic auto-fitting and scaling for all headers, balances, gift cards, and crystal packages.
  - Buttons and mouse click detection perfectly synchronized with dynamic layout.
- **100% Multilingual Localization Parity (11 Supported Languages):**
  - Completed exact 5,093 translation keys across English (`en_us`), Russian (`ru_ru`), Spanish (`es_es`), and all Latin American Spanish regional dialects (`es_mx`, `es_ar`, `es_cl`, `es_co`, `es_ec`, `es_pe`, `es_uy`, `es_ve`).
  - Added missing Russian keys for Teyvat dimensions, Pagoda trials, ores, and sound subtitles.
  - Added missing English keys for Wyvern entities, items, and Paimon sound subtitles.
  - Fully localized Blessing of the Welkin Moon screen, claim notices, and Gift Card tooltips.
- **Modal Persistence on Purchases & Conversions:**
  - Players are no longer abruptly kicked back to the banner screen when buying Gift Cards or converting Genesis Crystals / Stellar Jade; balances now update live in place.
- **Ranker Tower Ticket Progression Softlock Fixed:**
  - Resolved a progression gating error in the System Shop where the **Ranker Tower Ticket** was mistakenly locked behind *System 2.0 (Isekai)*. Since conquering the Ranker Tower in System 1.0 is the canonical prerequisite to activate the portal and ascend to the Isekai world, the ticket is now properly available for purchase in **System 1.0**.
- **Gacha Wish Soundscape Polish:**
  - Replaced an unimmersive wooden chest opening sound (`CHEST_OPEN`) when wishing with a full inventory with an authentic celestial magical chime (`AMETHYST_BLOCK_CHIME`) alongside instant tactile button click audio feedback.
- **Genshin-Style 2X Double First Top-Up Bonus:**
  - The first purchase of any Genesis Crystal package (60, 330, 1090, 2240, 3880, or 8080) now awards **double crystals (2X)**, mirroring Genshin Impact!
  - Unclaimed bonuses are marked in the store UI with an authentic gold `[2X]` tag and detailed preview tooltips.
- **Creative Mode Inventory Safety:**
  - Gift Cards are no longer consumed from the player's hand when right-clicked in Creative mode (`instabuild`).

---

# 📜 Changelog — Awakening System v0.2.0 (Beta)

> [!NOTE]
> ### 📢 Why is v0.2.0 Releasing Early as a Public Beta?
> Version **0.2.0** is being published ahead of schedule as a **Public Beta** build to prioritize immediate gameplay stability, solve critical modpack conflicts, and gather player feedback while remaining late-game content continues active development:
> 
> 1. **Immediate Modpack Anti-Stuck & Boundary Failsafes:**
>    - Many players experienced game-breaking boundary entrapment or void drops when using random teleportation items and spells from popular mods (such as the *Chorus Totem* from the *Artifacts* mod, *Iron's Spells 'n Spellbooks*, *Wizards*, vanilla Chorus Fruits, and Ender Pearls).
>    - Rather than withholding these crucial bugfixes until the entirety of v0.2.0 is finalized, this Beta delivers instant spatial containment and auto-rescue protection for servers and singleplayer worlds.
> 2. **Early Community Testing & Balancing:**
>    - Players can immediately experience and test the new **Spiral Abyss (Espiral del Abismo)** dimension, complete with authentic **Genshin Impact monsters (Slimes, Hilichurls, Mitachurls)** featuring 100% original studio soundscapes and 3D models.
> 3. **Active Iterative Development:**
>    - Advanced late-game features (higher Spiral Abyss trial chambers, expanded Murim Dan alchemy recipes, and additional cultivator weapons) are actively being developed and will roll out smoothly in upcoming beta patches leading to the final stable release.

---

## 🛡️ Modpack Compatibility & Failsafe System (Anti-Stuck Protection)
- **Dungeon Boundary Teleport Containment:**
  - Added spatial boundary event interception (`EntityTeleportEvent`) to fully support accessory, magic, and combat mods (such as the **Chorus Totem** from the *Artifacts* mod, *Iron's Spells 'n Spellbooks*, *Wizards*, vanilla Chorus Fruits, and Ender Pearls).
  - Any random or spell-based teleportation attempt that would land outside the bedrock bunker or inside solid walls is automatically **contained and redirected to safe ground in the active chamber center**.
- **Bedrock & Block Breaking in Creative Mode:**
  - Players in **Creative Mode** (`isCreative()` / `instabuild`) can now break Bedrock, barriers, and any blocks inside Dungeons, Ranker Towers, and Pagoda Trials for inspection, construction, and rescue purposes.
- **Continuous Out-of-Bounds Watchdog:**
  - Continuous real-time positioning watchdog running every tick. If a player glitches through geometry due to lag, knockback, or high-speed dashes, they are instantly relocated back onto the active chamber floor with zero velocity.
- **Stranded Player Auto-Rescue:**
  - Players who disconnect or have corrupted saves stranded outside active dungeon instances (e.g. from previous teleport bugs) are automatically detected upon login and **safely teleported back to their Overworld spawn point or bed**.
- **Universal Escape Commands (No OP Required):**
  - Emergency escape commands available to all players without cheats or operator permissions: `/escape`, `/dungeon exit`, `/dungeon escape`, `/tower exit`, and `/pagoda exit`.

## 🎵 Authentic Genshin Impact Monster Soundscapes (100% Original Audio)
- **Elemental Slimes:**
  - Replaced vanilla sounds with official, original studio sound effects from Genshin Impact (48,000 Hz Vorbis).
  - Authentic cute vocalizations, bloops, squishes, water chirps, springy jumps, and death pops.
- **Hilichurls:**
  - Iconic native Hilichurlian battle chants and vocalizations ("*Ya!*", "*Olah!*", "*Yaika!*").
  - Authentic hurt vocal grunts, combat shouts, and distinct death cries.
- **Mitachurls:**
  - Deep, resonant guttural roars and menacing battle growls.
  - Custom sound events for **Blazing Axe Earth Slam** and **Tower Shield Charge Dash**.
  - Authentic impact grunts and thunderous defeat roars.

---

## 🍮 Volumetric Squash & Stretch 2.0 & Ultra-Round 3D Mesh
- **Canonical Non-Overlapping Faceted Mesh (Zero Z-Fighting / Anti-Flicker):**
  - Completely eradicated polygon flickering and texture jittering ("tiritan") by restoring the canonical disjoint 26-box Blockbench structure where no quads overlap or share identical spatial planes.
  - Symmetrical mirrored UV mapping for Anemo gliding wings ensuring crisp matching white-feathered textures on both sides.
- **Elemental Texture Palette Purification (100% Cyan Artifact Removal):**
  - Thoroughly cleaned all non-Hydro elemental texture atlases (*Pyro, Electro, Dendro, Cryo*), replacing over 13,000 unpainted leftover cyan template pixels with authentic warm flame gradients, deep lightning purples, lush forest greens, and frost crystal whites.
  - Pyro, Electro, and Dendro slimes will never display miscolored cyan/blue domes or flanks.
- **Mathematical Volume Preservation:**
  - Slimes now deform with accurate volumetric elasticity ($\text{width} = \sqrt{1 / \text{height}}$).
  - Launching into the air causes snappy vertical elongation (stretch).
  - Hitting the ground triggers deep horizontal pancake expansion (squash).
  - Added organic dynamic tilt angles based on velocity when airborne.
  - Fully synchronized on both client and server ticks.

---

## 🌀 Spiral Abyss: 3D Ley Line Monolith, Red Challenge Key & Arena Boundaries
- **3D Ley Line Monolith (Monolito de las Líneas Ley):**
  - Replaced temporary vanilla blocks and invisible Iron Golems with the authentic floating 3D Monolith from Genshin Impact for defense chambers (Floor X - Chamber 1).
  - Features an ancient stone domain pedestal, a levitating central obelisk with glowing Ley Line conduits, an inner glowing cyan core, two counter-rotating orbital runic rings, and apex crystal shards.
  - Emits periodic taunt/aggro pulses that draw all Abyss monsters (slimes, hilichurls, mitachurls) to prioritize attacking it.
  - Slashes/impacts trigger crystal resonance sound effects and cyan sparks. Real-time health percentage displayed on both its holographic name tag and the chamber BossBar.
- **3D Red Challenge Key (Llave Roja de Desafío):**
  - Replaced terracotta and armorstand placeholders with the iconic Genshin Impact floating crimson challenge sword/key and orbital dial.
  - Hovers in the center of combat chambers during preparation (`WAITING_FOR_TRIGGER`).
  - Right-clicking or hitting the mechanism triggers authentic Genshin challenge fanfare (`SoundEvents.UI_TOAST_CHALLENGE_COMPLETE` + beacon bell), bursts into crimson flame shockwaves, and initiates the chamber battle.
- **High-Altitude Barrier Wall:**
  - Added an impenetrable 16-block-high wall of invisible barrier blocks (`Blocks.BARRIER`) surrounding the entire circular arena perimeter ($r = 18$).
  - Prevents players, mobs, and projectiles from falling into the deep dark chasm or leaping out of bounds.
- **Active Containment Watchdog:**
  - Real-time tick monitor catches any entity or player forced outside the platform by extreme knockback, spells, or clipping bugs, immediately teleporting them safely back onto the arena floor.

---

## 👾 Authentic Genshin Impact Monster Lineup
- **Elemental Slimes (Teyvat):**
  - Completely overhauled with ultra-round 3D dumpling geometry and exact UV mapping.
  - Authentic high-resolution textures for all 7 elements: *Pyro, Hydro, Anemo, Electro, Cryo, Geo, and Dendro*.
  - Full 3D canonical accessories: flapping glide wings (Anemo), frost spikes (Cryo), lightning rod horn (Electro), crystal horns (Geo), and blooming flower foliage (Dendro).
- **Hilichurls:**
  - Canonical Blockbench humanoid model with 3D sculpted bone mask relief and angled ear tufts.
  - 4 specialized variants with dedicated weapons and equipment:
    - **Fighter:** Wooden tribal club.
    - **Berserker:** Flaming torch club.
    - **Guard:** Reinforced wooden tribal shield.
    - **Shooter:** Functional wooden crossbow.
- **Mitachurls:**
  - High-definition (256x256) brute model featuring layered horns, thick neck mane, shoulder guards, and battle armor.
  - Dynamic equipment sets: **Blazing Magma Battleaxe** and **Massive Wooden Tower Shield**.

---

## 🌌 New Dimensions, Cultivation & Combat Progression
- **Spiral Abyss Dimension (Espiral del Abismo):**
  - 12 tiered trial floors with timed combat encounters, Abyssal Moon blessings, star progression, and reward vaults.
- **Murim Qi Cultivation & Cauldron Alchemy:**
  - Interactive **Murim Alchemy Cauldron** block that ignites over spiritual fires (fire, soul fire, campfire, magma, or lava) to refine immortal Dao pills (*Dan*).
  - Marrow Cleansing Pills (permanently unlock blocked meridians), Rising Dragon Pills, and Realm Breakthrough Elixirs.
- **Exotic Cultivator Weaponry:**
  - **Jade Flying Sword (*Feijian*):** Homing telekinetic projectile attack that automatically returns to the cultivator's hand.
  - **Astral Dragon Guandao:** Piercing celestial crescent blade wave.
  - **Adamantite War Hammer:** Ground slam and seismic shockwave fissure.
- **Sacred Yggdrasil World Tree Protection:**
  - Complete immunity against fire, lava griefing, explosions, and destructive lightning bolts.
  - Native Mana Pixie entities and spiritual herbs.
- **Physical 3D Roulette Crate:**
  - Boss altar now spins loot physically in mid-air before awarding the conqueror's prize.

---

## 📦 File Information
- **Target File:** `awakening_system-0.2.0.jar`
- **Mod Loader:** NeoForge 1.21.1
- **Release Channel:** Beta