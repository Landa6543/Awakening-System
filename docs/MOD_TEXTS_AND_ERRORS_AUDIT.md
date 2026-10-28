# AUDITORÍA GLOBAL MAESTRA: TEXTOS, COMENTARIOS DE CÓDIGO, ERRORES Y BUGS

> **Documento maestro exhaustivo y definitivo para la fase de Limpieza Final del Mod.**
> Contiene el inventario completo de comentarios técnicos del código Java, textos descriptivos, mensajes directos sin traducir, bloques de captura silenciosa y análisis de posibles bugs.

## 1. Resumen Ejecutivo de Métricas

| Categoría / Métrica | Cantidad Total | Estado Tras Limpieza |
| :--- | :--- | :--- |
| Claves de traducción en `en_us.json` | **7307** | **100% Sincronizado** (+5 claves añadidas) |
| Claves de traducción en `es_es.json` | **7307** | **100% Sincronizado** (+5 claves añadidas) |
| Tooltips y Lore de Objetos / Armas | **957** | **100% Traducido** |
| Habilidades, Afinidades y Talentos | **735** | **100% Traducido** |
| Diálogos y Citas de Compañeros / NPCs | **78** | **100% Traducido** |
| Libros de Historia y Guías | **205** | **100% Traducido** |
| Mensajes de Sistema y Notificaciones | **5324** | **100% Traducido** |
| **Comentarios técnicos descriptivos en el código Java** | **6749** | **ELIMINADOS AL 100%** (442 archivos limpiados) |
| **Mensajes hardcodeados al jugador (`Component.literal`)** | **648** | Documentados en Sección 4 |
| **Bloques de excepción silenciosa (`catch ignored`)** | **93** | Documentados en Sección 3 |
| **Compilación y Empaquetado (`./gradlew jar`)** | **EXITOSO** | `awakening_system-0.3.0.jar` generado (0 errores) |

---

## 2. Textos Descriptivos del Código Fuente (Comentarios Técnicos y Decisiones de Diseño)

A continuación se catalogan todos los comentarios descriptivos insertados directamente en el código Java que explican comportamientos, decisiones técnicas de renderizado (como evitar el blur de vanilla), heurísticas de físicas y protecciones:

### 2.1. Casino y Minijuegos de Azar (41 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/PhysicalRouletteManager.java` | 139 | `1. Efectos visuales de la Ruleta Física (Círculo Rúnico con 8 sectores)` |
| `com/soybe/awakeningsystem/PhysicalRouletteManager.java` | 216 | `Cascada de campanadas musicales y monedas mientras el ítem premiado flota y se entrega al jugador (ticks 76 a 92)` |
| `com/soybe/awakeningsystem/PhysicalRouletteManager.java` | 231 | `5. Finalización y limpieza (Tick 95)` |
| `com/soybe/awakeningsystem/block/CasinoBarStoolBlock.java` | 114 | `Altura elevada (0.85D) para sentarse frente a mostradores de barra de 1 bloque` |
| `com/soybe/awakeningsystem/block/CasinoChairBlock.java` | 114 | `Altura de 10 píxeles para sentarse erguido sobre el cojín de terciopelo` |
| `com/soybe/awakeningsystem/block/SlotMachineBlock.java` | 111 | `Si sostiene una Ficha de Casino: Inserción física y activación directa` |
| `com/soybe/awakeningsystem/block/entity/RouletteAltarBlockEntity.java` | 168 | `Curva cúbica de desaceleración física` |
| `com/soybe/awakeningsystem/block/entity/RouletteAltarBlockEntity.java` | 190 | `Partículas alrededor de la pista circular de la gran ruleta vertical (radio ~0.85D)` |
| `com/soybe/awakeningsystem/block/entity/RouletteAltarBlockEntity.java` | 247 | `Cascada de campanas de casino y monedas mientras se muestra el premio ganador` |
| `com/soybe/awakeningsystem/block/entity/RouletteTableBlockEntity.java` | 75 | `Calcular ángulo objetivo exacto para que la bola descanse suavemente en la casilla ganadora` |
| `com/soybe/awakeningsystem/block/entity/RouletteTableBlockEntity.java` | 82 | `La bola viaja 8 vueltas en contra-rotación (-2880°) y ajusta la fase para aterrizar en la casilla` |
| `com/soybe/awakeningsystem/block/entity/SlotMachineBlockEntity.java` | 193 | `Configurar ángulos de inicio y llegada física para los 3 tambores` |
| `com/soybe/awakeningsystem/block/entity/SlotMachineBlockEntity.java` | 451 | `1. Animación física de la palanca (Baja en 12 ticks y retorna en 16 ticks con muelle elástico)` |
| `com/soybe/awakeningsystem/block/entity/SlotMachineBlockEntity.java` | 483 | `3. Conclusión de la tirada y entrega física de premios en el mundo (Tick 0)` |
| `com/soybe/awakeningsystem/block/entity/SlotMachineBlockEntity.java` | 531 | `Acreditar Mora al jugador (solo si winMora > 0 para no duplicar si vino de apuesta de GUI)` |
| `com/soybe/awakeningsystem/block/entity/SlotMachineBlockEntity.java` | 554 | `Expulsión física de ítems desde la bandeja inferior hacia el frente` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 56 | `Comprobar si hay una partida PvP activa o en espera en esta mesa física` |
| `com/soybe/awakeningsystem/casino/CasinoJsonHelper.java` | 87 | `Para multibloques, solo exportar el bloque maestro` |
| `com/soybe/awakeningsystem/casino/RouletteServerHandler.java` | 88 | `Validar fichas físicas` |
| `com/soybe/awakeningsystem/casino/RouletteServerHandler.java` | 107 | `Iniciar giro físico 3D en el bloque del mundo en tiempo real` |
| `com/soybe/awakeningsystem/casino/SlotMachineServerHandler.java` | 70 | `Si es JACKPOT de 777: Premio especial adicional de 1,600 Protogemas` |
| `com/soybe/awakeningsystem/casino/SlotMachineServerHandler.java` | 80 | `Iniciar animación y física 1:1 en el bloque 3D del mundo` |
| `com/soybe/awakeningsystem/casino/blackjack/BlackjackGame.java` | 60 | `Usar un zapato de 4 barajas estándar para aleatoriedad de casino` |
| `com/soybe/awakeningsystem/casino/darts/DartboardServerHandler.java` | 133 | `Notificar a jugadores cercanos en un radio de 24 bloques` |
| `com/soybe/awakeningsystem/casino/darts/DartboardServerHandler.java` | 268 | `Locutor épico PDC para todos los jugadores en la taberna` |
| `com/soybe/awakeningsystem/casino/darts/DartboardServerHandler.java` | 397 | `Equipar directamente en la mano principal para comenzar a lanzar al instante` |
| `com/soybe/awakeningsystem/casino/darts/DartEntity.java` | 105 | `Rebota y cae al suelo como ítem recogible para erradicar dardos flotantes bugueados` |
| `com/soybe/awakeningsystem/casino/darts/DartEntity.java` | 170 | `Calcular coordenadas normalizadas (u, v) en la cara de la diana (0.0 a 1.0)` |
| `com/soybe/awakeningsystem/casino/darts/DartEntity.java` | 191 | `Ángulo trigonométrico en grados (0° = Derecha / Este, 90° = Arriba / 20)` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 58 | `6 Troneras en coordenadas de mesa: 4 esquinas y 2 laterales` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 108 | `Disposición oficial en triángulo a partir de Z = 0.32F` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 402 | `1. Mover bolas e integrar velocidad con fricción` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 409 | `Fricción constante del paño verde de billar` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 481 | `Separar bolas para evitar superposición` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 534 | `Pérdida inmediata por meter la 8 antes de tiempo o junto a la blanca` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 677 | `Para cada tronera, evaluar qué tan fácil es embocar` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 692 | `Calidad del tiro (distancia corta + ángulo alineado)` |
| `com/soybe/awakeningsystem/casino/roulette/RouletteBetEntry.java` | 11 | `-1 = Mora directa, 0..4 = Ficha física del inventario` |
| `com/soybe/awakeningsystem/casino/slot/SlotReels.java` | 50 | `Si faltan elementos para completar REEL_SIZE, rellenar con Slime` |
| `com/soybe/awakeningsystem/world/TeyvatCasinoBuilder.java` | 113 | `La huella del edificio mide 36 (x) x 61 (z), centrada en offset (-10, 0, -34)` |
| `com/soybe/awakeningsystem/world/TeyvatCasinoBuilder.java` | 140 | `Offset: (-10, 0, -34) centra exactamente el podio principal en (cx, cy + 3, cz)` |

### 2.2. Comandos de Administrador (13 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/SystemCommands.java` | 348 | `Atajo directo /sombras y /shadows para gestión del ejército del Monarca` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 360 | `Atajo directo /subespacio y /subspace para acceso al inventario dimensional` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 419 | `Comando /escape accesible para TODOS los jugadores sin permisos OP` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 423 | `Comando /dungeon con subcomando /dungeon exit y /dungeon escape para todos los jugadores` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 445 | `Comando /tower exit para todos los jugadores` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 479 | `Atajo directo para que las Constelaciones puedan renombrarse o regresar de proyección astral` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 487 | `Atajo directo /guide para todos los jugadores` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 495 | `Atajo directo /gacha, /wish, /deseos para todos los jugadores` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 594 | `Atajo directo /resetprogress para reiniciar progreso personal o de terceros` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 757 | `Comando directo para probar anuncios globales estilo ARK` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1251 | `Probar si es una estructura o arena de jefe de Teyvat (Genshin Impact)` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2195 | `Si estaba en la Torre, remover sesión de torre limpiamente` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2695 | `Si está en el vacío fuera del mundo o atascado en otra dimensión de prueba` |

### 2.3. Combate, Habilidades y Estadísticas (4 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/ClassSkillTree.java` | 44 | `Constructor para habilidades mayores` |
| `com/soybe/awakeningsystem/ClassSkillTree.java` | 49 | `Constructor para nodos de atributos` |
| `com/soybe/awakeningsystem/ClassSkillTree.java` | 575 | `Ya desbloqueado` |
| `com/soybe/awakeningsystem/ClassSkillTree.java` | 589 | `ya desbloqueada` |

### 2.4. Compañeros y NPCs (8 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 361 | `Desbloquear / Subir Constelación (C1... C∞)` |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 412 | `Actualizar interfaz en tiempo real` |
| `com/soybe/awakeningsystem/entity/CompanionEntity.java` | 583 | `Piroquinesis Carmesí: Llamas rosas curativas para amo y destructivas para hostiles` |
| `com/soybe/awakeningsystem/entity/CompanionEntity.java` | 650 | `Danza Pasional: Asalto veloz, acrobacia y acumulación de anomalía física` |
| `com/soybe/awakeningsystem/entity/CompanionEntity.java` | 659 | `Barrera del Coliseo: Escudo protector para amo y onda de choque 360°` |
| `com/soybe/awakeningsystem/entity/CompanionEntity.java` | 824 | `Vorágine del Caos Primordial: Desgarro dimensional gravitatorio que succiona y devasta` |
| `com/soybe/awakeningsystem/entity/CompanionEntity.java` | 888 | `Distorsión Telequinética: Singularidad gravitatoria que atrae, levita y aplasta` |
| `com/soybe/awakeningsystem/entity/SeriesNpcEntity.java` | 224 | `Los NPCs protagonistas son legendarios e inmortales frente a monstruos, fuego, asfixia y caídas` |

### 2.5. Hipódromo y Carreras de Uma Musume (44 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/block/entity/CustomRacetrackBlockEntity.java` | 43 | `null si es NPC` |
| `com/soybe/awakeningsystem/block/entity/CustomRacetrackBlockEntity.java` | 206 | `Evitar repetir la misma Uma si ya la tiene un jugador` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 65 | `60 segundos para apostar` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 116 | `para que los carriles se mantengan perfectamente paralelos y rectos hacia adelante,` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 117 | `evitando que se crucen o inviertan al llegar a la meta.` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 144 | `60 segundos para apostar` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 149 | `1. Asegurar carga de chunks de pista y compuertas para detección exhaustiva de entidades` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 156 | `Limpieza absoluta de CUALQUIER UmaRacerEntity residual del hipódromo en un radio de 300 bloques` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 187 | `Registrar las 9 corredoras actuales para que en la SIGUIENTE tanda ninguna de ellas repita` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 249 | `3 segundos para el pistoletazo de salida` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 292 | `Purgar duplicados cada 20 ticks (1 segundo) para que nunca coexistan dos Umas en el mismo carril` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 328 | `90 segundos de margen holgado para que todas terminen` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 365 | `Narración de carrera en tiempo real por Actionbar cada 35 ticks (~1.8 segundos)` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 388 | `Si todas terminaron o el tiempo expiró` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 410 | `Escanear entre X=140 y X=160 a nivel de suelo para detectar la línea de meta a cuadros construida` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 461 | `Dar tiempo generoso (35 segundos) para que todas las demás corredoras crucen la meta` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 474 | `Evitar llamadas dobles` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 256 | `En el cliente, asegurar orientación visual fija hacia la salida mientras no corra` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 271 | `Mantener orientación congelada fija hacia la recta de salida mientras espera o termina` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 289 | `Radio de proximidad de paso en tramos intermedios: 3.5 bloques (12.25 distSq)` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 305 | `Línea de meta (último waypoint): la corredora debe cruzar físicamente la línea` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 426 | `Cálculo de velocidad dinámica según arquetipo y tramo de carrera` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 517 | `Si está muy cerca (< 2.2 bloques), aplicar desplazamiento lateral para adelantar limpiamente` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 557 | `Dispersión por Sabiduría: entre -0.04F y +0.04F para escalonar y evitar activaciones sincronizadas` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 622 | `Bonificación por estadísticas entrenadas si es carrera custom` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 744 | `Si es una corredora antigua guardada en el chunk antes de esta corrección:` |
| `com/soybe/awakeningsystem/uma/UmaRacerEntity.java` | 746 | `Descartar de inmediato para purgar entidades duplicadas residuales del chunk` |
| `com/soybe/awakeningsystem/uma/gacha/UmaGachaManager.java` | 214 | `Probabilidades oficiales: 3% para 3★, 18% para 2★, 79% para 1★` |
| `com/soybe/awakeningsystem/uma/model/OwnedUma.java` | 16 | `Fragmentos para Talent Bloom` |
| `com/soybe/awakeningsystem/uma/model/OwnedUma.java` | 35 | `Estadísticas base escaladas según el perfil oficial y las estrellas` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 48 | `Marcadores de versiones anteriores y v8` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 56 | `Limpiar posibles cancelas huérfanas en la antigua posición de X=135` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 68 | `Limpieza de área de gradas previas para garantizar reemplazo perfecto` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 128 | `Ordenar por Y ascendente para asegurar que bases y suelos se colocan antes que partes superiores` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 162 | `Hojas persistentes para evitar que árboles ornamentales desaparezcan` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 189 | `Comprobación de persistencia global en SavedData del mundo:` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 202 | `Comprobación secundaria: marcador o bloques existentes en el mundo` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 269 | `4 pasos por bloque longitudinal para garantizar cobertura continua` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 291 | `2. Despeje vertical masivo, superficie continua de competición y barandillas seguras` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 320 | `Zona protegida de recta de salida y meta` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 331 | `Pista de carreras oficial de Césped de Competición (Turf):` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 356 | `Limpieza previa del volumen del arco de meta y cabinas` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 393 | `Marcador (Beacon) en el centro de la viga` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 453 | `Limpieza de estructura previa del pabellón` |

### 2.6. Núcleo del Sistema (SystemCore & Eventos) (345 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/ConstellationChannelHandler.java` | 74 | `Si está en solitario o no hay más jugadores, incluir encarnaciones narrativas para pruebas` |
| `com/soybe/awakeningsystem/ConstellationChannelHandler.java` | 132 | `Buscar si es un jugador real online` |
| `com/soybe/awakeningsystem/ConstellationChannelHandler.java` | 176 | `Actualizar la lista para el donante` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 134 | `Ruleta Física en el Mundo (Estilo Crate de Servidores P2W)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 184 | `Para llegar al Boss, se debe explorar y doblar obligatoriamente por alas y pasillos laberínticos en zigzag.` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 187 | `1. Callejón sin Salida Central (Tumba Falsa / Trampa para quien corre de frente en línea recta):` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 581 | `10 min (600s) - Para jefes supremos de ~25.000-33.000 HP (Absolute God, Singularity, etc.)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 595 | `+20% tiempo en Red Gates debido al +35% de vida` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 718 | `6. BossBar y Temporizador de Combate escalado por rango` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 980 | `Escalado de dificultad según rango del Gate` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 1219 | `Aumento de tiempo al matar enemigos (+8s esbirro, +20s élite)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 1305 | `En biomas de llanura, el techo exterior usa barreras invisibles para permitir la vista libre del cielo y entrada de luz solar` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 1329 | `Tumba Falsa / Emboscada Central (CAMINO SIN SALIDA para quien va de frente en línea recta)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 1496 | `Algoritmo de laberinto (DFS con retroceso) para árbol de expansión` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 1539 | `Laberinto trenzado moderado (12% de atajos adicionales) para mantener la sensación de laberinto real` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2027 | `Claraboyas naturales al cielo abierto para biomas de llanura con frondoso follaje` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2065 | `Cúpula cenital: si es borde, hojas ornamentales; si es centro, BARRERA INVISIBLE sellada para que no escapen` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2074 | `dy == h - 1: Aire interior para que se vea el cielo a través de las barreras` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2096 | `Si es claraboya plains: BARRERAS INVISIBLES (u hojas en el borde) para permitir el paso de luz solar sin hueco físico` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2292 | `Sella de forma hermética el corte transversal del túnel en cualquier ángulo (radio 5)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2469 | `Emboscada en la Tumba Central para los jugadores que corren directo de frente (+Z)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2903 | `1. Limpieza de aire en volumen 5x5 alrededor del núcleo (desde Y hasta Y+4)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2914 | `2. Base de sustentación profunda (Y-2 a Y-1) para garantizar que nunca flote sobre vacío o suelos irregulares` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2940 | `4. Borde perimetral 5x5 de losas lisas pulidas para transición perfecta con el suelo` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2982 | `Desbloquear portal de salida en Sala 1 si era Red Gate` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3072 | `Iniciar la Ruleta Física en el Mundo (Estilo Crate de Servidores P2W - Sin UI que tape la pantalla)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3081 | `Asegurar que NO corre ningún temporizador de salida mientras gira` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3472 | `Tiempo límite de combate para derrotar a los enemigos de la sala` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3513 | `1. Lógica de la Ruleta Física estilo P2W Server Crate` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3560 | `Fase 1: Revelación de recompensa inicial - Esperando decisión del jugador (Asegurar o Apostar)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3863 | `Interacción al cruzar el portal (< 1.0 bloque horizontal para no ser absorbido al pasar al lado)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3900 | `Desborde de Mazmorras (Dungeon Break) cuando expira el tiempo de una Falla sin conquistar` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3921 | `Sonido ambiental de vórtice dimensional (sonido custom profundo, sin pitidos)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3933 | `Entrada al vórtice: exactamente a 1 bloque de distancia para evitar ser absorbido al pasar al lado` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3947 | `NO removemos la falla de WORLD_RIFTS para que persista si el jugador sale` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3991 | `Si es un jugador cooperativo (no el host), solo sale él y la mazmorra continúa para el anfitrión` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4001 | `Si es el anfitrión quien sale, se retira a todos los jugadores cooperativos de la mazmorra` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4027 | `Gestión de persistencia del Portal en el Overworld` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4030 | `Calabozo conquistado (jefe derrotado): se cierra la falla dimensional definitivamente` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4041 | `Salió sin conquistar: el portal dimensional permanece activo en el exterior` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4084 | `2. Limpieza rpida y exhaustiva de bloques no aire mediante secciones de chunk cargadas` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4370 | `Estado BLOQUEADO (Imagen 2): Sello Carmesí Perimetral y vano abierto al cielo` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4383 | `Portal de Salida en la Sala 1 del Calabozo (tipo 1 para escala ajustada al vano del templo)` |
| `com/soybe/awakeningsystem/GachaManager.java` | 131 | `Enviar DIRECTAMENTE al Subespacio del Sistema (evita que el modo creativo de Minecraft destruya el objeto)` |
| `com/soybe/awakeningsystem/GachaManager.java` | 136 | `4. Si incluso el Subespacio de 54 casillas estuviera lleno, soltar al suelo para no perderlo jamás` |
| `com/soybe/awakeningsystem/GachaManager.java` | 398 | `Para los pulls restantes, pueden pagarse con Carats (150 cada uno) o Protogemas (160 cada una)` |
| `com/soybe/awakeningsystem/GachaManager.java` | 541 | `Si es la primera tirada de 10 del banner de principiantes, garantizar a Noelle en el slot 0` |
| `com/soybe/awakeningsystem/GachaManager.java` | 908 | `Senda divina para armas` |
| `com/soybe/awakeningsystem/ModCreativeTabs.java` | 466 | `--- LLAVES DIMENSIONALES ---` |
| `com/soybe/awakeningsystem/PagodaTrialManager.java` | 276 | `1. Limpieza y Construcción de la Gran Cámara Oriental` |
| `com/soybe/awakeningsystem/PagodaTrialManager.java` | 315 | `2. Grandes Columnas Imperiales Orientales (en etapa 9 se colocan al borde para no obstruir al Dragón)` |
| `com/soybe/awakeningsystem/PagodaTrialManager.java` | 378 | `Puerta de madera para regresar` |
| `com/soybe/awakeningsystem/PagodaTrialManager.java` | 598 | `Preservar IA de combate aéreo y ataque con garras/rayos del dragón` |
| `com/soybe/awakeningsystem/PagodaTrialManager.java` | 650 | `Base ornamental para la puerta` |
| `com/soybe/awakeningsystem/RpgAffixManager.java` | 24 | `6% robo de vida (balanceado para evitar inmortalidad)` |
| `com/soybe/awakeningsystem/RpgLevelZoneManager.java` | 85 | `Escalar vida y daño según el nivel de zona` |
| `com/soybe/awakeningsystem/RpgLevelZoneManager.java` | 117 | `Drops garantizados para el Wyvern` |
| `com/soybe/awakeningsystem/RpgLevelZoneManager.java` | 137 | `Detección de si estamos en la dimensión de Teyvat (Mora y Protos SOLO se consiguen en Teyvat)` |
| `com/soybe/awakeningsystem/ShopDefinitions.java` | 366 | `Tier 4.0 (Dominio Celestial) hereda acceso y compras de todas las dimensiones` |
| `com/soybe/awakeningsystem/SystemConfig.java` | 20 | `--- ESCALADO DE CRIATURAS POR DISTANCIA ---` |
| `com/soybe/awakeningsystem/SystemData.java` | 210 | `Puntos de Habilidad (SP) para el Árbol de Clases` |
| `com/soybe/awakeningsystem/SystemData.java` | 264 | `=== SUBESPACIO DIMENSIONAL / REINO INTERIOR ===` |
| `com/soybe/awakeningsystem/SystemData.java` | 1853 | `--- Dimensiones Descubiertas / Visitadas ---` |
| `com/soybe/awakeningsystem/SystemData.java` | 1968 | `1. Intentar apilar en casillas con el mismo objeto dentro del límite desbloqueado` |
| `com/soybe/awakeningsystem/SystemData.java` | 1983 | `2. Colocar en la primera casilla vacía dentro del límite desbloqueado` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 411 | `Conservar exactamente UNA Paimon y asegurar vinculación al jugador` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 469 | `> 60 bloques (antes 16 bloques) para dar libertad en el campo de batalla` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 559 | `Subespacio Dimensional: Detección en tiempo real de colocación de cofres/barriles` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 737 | `Subespacio Dimensional: Detección en tiempo real de remoción de cofres/barriles` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 994 | `Registro de Bajas y Ascensos de Rango para Soldados Sombra (Solo Leveling)` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1109 | `Progreso de caza para misiones del Dokkaebi` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1128 | `Experiencia de combate balanceada según salud y tipo de criatura (máx 35 para monstruos normales, máx 500 para jefes)` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1158 | `Balanceado: Requiere nivel 40+ y que la criatura sea jefe o monstruo de élite escalado` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1197 | `Runa del Monarca de las Sombras: Deja Resto de Alma de Sombra en el suelo para extracción ("¡SURGE!")` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1422 | `+100% Monedas para Apóstol del Star Stream` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1591 | `Protección de fuego amigo y sofocación en bloques para Soldados Sombra` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1638 | `para que no inflijan 800+ de daño de un solo golpe activando a su vez robos de vida descomunales.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1649 | `Escalado dinámico y protección anti-one-shot de Jefes de Mundo frente al jugador` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1657 | `Preservación Cósmica del Manto de la Belleza al recibir daño letal o crítico` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1694 | `=== RUNA PASIVA: LA CUARTA PARED (PREVIENE DAÑO LETAL CADA 5 MIN) ===` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1912 | `=== VULNERABILIDAD FÍSICA POR SUPERCONDUCTOR (-40% RESISTENCIA FÍSICA) ===` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2066 | `--- Pasivas de Árbol de Habilidades de Clase (Escaladas por Rango I, II y III) ---` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2102 | `Lector Omnisciente: Maestría de Marcadores (Probabilidad de buff aleatorio)` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2278 | `Si es una curación completa intencional como Píldora de la Tortuga Negra, Altar de la Diosa o Tótem` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2288 | `Esto previene que afijos desbalanceados de robo de vida (vampirismo / lifesteal) curen 80+ HP por golpe` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2426 | `Si el portal estaba bloqueado, activarlo automáticamente` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2768 | `Prevención de fuego amigo para sombras` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2910 | `=== MÓDULO UMA MUSUME PRETTY DERBY TICKING ===` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 3082 | `Cálculo de daño de Qi de la tribulación escalado hasta Reino 9` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 3214 | `Si está en fase de emergencia saliendo del suelo (primeros 60 ticks)` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 3256 | `Sincronizar puertas dimensionales` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 3283 | `1. Portal Visible Secreto: solo teletransporta a Murim al entrar físicamente al arco` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 3652 | `Seguimiento de racha de combo para misiones del Dokkaebi` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 5462 | `Ralentización leve mientras se canaliza poder` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8136 | `Se ancla al suelo con velocidad cero, debilidad y lentitud 255 mientras emerge gradualmente durante 3.5 segundos` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8143 | `Registro en el Equipo del Scoreboard con color AQUA (Cian/Azul eléctrico brillante) para el Glow distintivo` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8320 | `Escalado de aura azul según el rango jerárquico de la sombra` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8672 | `2. Verificación de tiempo de enfriamiento de resonancia (5 minutos = 6000 ticks)` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8681 | `3. Verificación de ofrenda de Sigilos Elementales (5x requeridos para cambio)` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8839 | `Si es activa y tiene ranura vacía, equipar automáticamente` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8929 | `Cooldown de 20s en subidas de nivel para evitar spam en subidas consecutivas` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8938 | `Cooldown de 35s en combate para evitar saturación de pantalla` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 9187 | `Comprobación de seguridad en cuevas / interiores para evitar spawn dentro de bloques sólidos` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 9215 | `Escalar criaturas según las zonas RPG o escalar Jefes de Mundo según los jugadores` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 9258 | `Integración dinámica de criaturas pasivas de Friends & Foes si está instalado` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 28 | `Escala del HUD y comportamiento adaptativo según resolución / GUI Scale` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 36 | `Cálculo adaptativo:` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 37 | `Pantallas compactas (427x240 o 512x288): escala suave a 0.72x - 0.85x` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 38 | `Pantallas medianas (640x360 a 854x480): escala 0.90x - 1.0x` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 46 | `Interpolación suave y daño/consumo rezagado (Damage & Resource Lag / Ghost Bar)` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 78 | `0. Banner de Actualización del Sistema 2.0 / 3.0 (si está en mantenimiento o ascensión)` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 92 | `2. Barra de Habilidades Activas (Abajo adaptativa y protegida contra solapamiento)` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 365 | `4. Barra de Qi con barra fantasma suave (si ha desbloqueado cultivo / Murim)` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 593 | `Margen adicional de separación para jefes principales` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 683 | `Nombre del objetivo en la línea superior (espacio completo sin colisión)` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 770 | `Margen garantizado para mazmorras y torres` |
| `com/soybe/awakeningsystem/SystemItems.java` | 1229 | `=== PIEDRAS DE RUNA FÍSICAS ===` |
| `com/soybe/awakeningsystem/SystemItems.java` | 1400 | `=== LLAVES DIMENSIONALES ===` |
| `com/soybe/awakeningsystem/SystemNotificationOverlay.java` | 87 | `Fatiga tras sostener demasiado tiempo` |
| `com/soybe/awakeningsystem/SystemNotificationOverlay.java` | 125 | `Barra de tensión debajo de la retícula si está apuntando` |
| `com/soybe/awakeningsystem/SystemNotificationOverlay.java` | 182 | `Cálculo de fade in/out alfa` |
| `com/soybe/awakeningsystem/TowerManager.java` | 331 | `6. Spawn de Enemigos con Escalado Estrictamente Monótono` |
| `com/soybe/awakeningsystem/TowerManager.java` | 451 | `2. Añadir meta prioritaria hacia el jugador escalador de la torre` |
| `com/soybe/awakeningsystem/TowerManager.java` | 457 | `3. Meta de respaldo para cualquier jugador` |
| `com/soybe/awakeningsystem/TowerManager.java` | 467 | `5. Enfocar inmediatamente al jugador desafiante si está presente` |
| `com/soybe/awakeningsystem/TowerManager.java` | 480 | `Fórmulas de escalado monótono garantizado (Pisos 1 al 100)` |
| `com/soybe/awakeningsystem/TowerManager.java` | 802 | `Despejar el área alrededor del Gran Portal Isekai para que flote limpiamente` |
| `com/soybe/awakeningsystem/TrilunarSanctuaryManager.java` | 26 | `1. Limpieza de aire sobre el santuario` |
| `com/soybe/awakeningsystem/VersionBannerManager.java` | 131 | `Cada fase de 14 días en el tiempo mundial posee un ciclo estrictamente incremental` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 27 | `Portal dimensional oculto entre las ramas maestras` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 44 | `4. Ramas mayores tridimensionales y cúpulas de follaje de cerezo` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 118 | `Vano interior con el bloque de portal dimensional` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 135 | `UPDATE_CLIENTS \| UPDATE_KNOWN_SHAPE (evita recálculos de forma redundantes)` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 168 | `1. Limpieza de aire en la meseta para despejar árboles vanilla que atraviesen las raíces` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 313 | `Si está bajo tierra, garantizar que la superficie a BASE_Y sea tierra enraizada con musgo` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 321 | `Si es un arco que sobresale del suelo, vaciar túnel transitable debajo y colgar raíces` |
| `com/soybe/awakeningsystem/YggdrasilTreeGenerator.java` | 492 | `Capa envolvente de follaje (hollow shell) para reducir bloques en un 80% con idéntico aspecto` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 663 | `Espacio completamente despejado en el centro para las entidades 3D (Llave Roja y Monolito)` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 671 | `Limpiar todo el espacio vertical superior (Y = centerY + 1 a 6) para que nada obstruya la Llave/Monolito` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 841 | `Retirar la Llave central y el marcador` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 853 | `Determinar si es sala de Defensa del Monolito (Sala 1 de cada piso en Pisos 1-4, y Sala 1 Primera Mitad en Pisos 5+)` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 923 | `para dar tiempo al jugador a interceptarlos antes de que lleguen al centro` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1214 | `Failsafe de seguridad y contención: Evitar que el jugador caiga al vacío abisal o sea expulsado del perímetro` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1227 | `Partículas de atmósfera mística abisal (niebla cósmica y esporas flotantes sin shaders)` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1291 | `Si es sala de monolito, orientar el aggro hacia el monolito` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1467 | `Cálculo canónico de estrellas: Monolito HP% en Sala 1, Tiempo restante en Salas 2 y 3` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1478 | `Limpiar el Monolito si estaba presente` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1545 | `2. Abrir Portal de Descenso Astral hacia la siguiente sala (o Salida si es sala 3)` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1639 | `Spawnear de nuevo la Llave del desafío para la siguiente sala` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1642 | `Teletransportar al balcón norte para contemplar la siguiente sala` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1766 | `Redirigir al menú para poder reintentar o elegir otro piso` |
| `com/soybe/awakeningsystem/block/AscensionPortalBlock.java` | 91 | `Evitar spam de teletransporte si el jugador ya está cambiando de dimensión` |
| `com/soybe/awakeningsystem/block/CampfireLogSeatBlock.java` | 96 | `Altura perfecta para quedar sentado sobre el tronco de 8 píxeles (0.50D)` |
| `com/soybe/awakeningsystem/block/CelestialDragonAltarBlock.java` | 61 | `Verificar si el dragón jefe ya existe en el área (radio 350 bloques)` |
| `com/soybe/awakeningsystem/block/DragonNestBlock.java` | 134 | `Radio de furia dracónica: 96 bloques` |
| `com/soybe/awakeningsystem/block/ElementalAltarBlock.java` | 98 | `Si el Señor Elemental de esta ala sigue con vida, protege el altar` |
| `com/soybe/awakeningsystem/block/ElementalAltarBlock.java` | 121 | `Buscar cuántos altares de las 4 alas están activados en un radio de 120 bloques` |
| `com/soybe/awakeningsystem/block/ElementalMonumentBlock.java` | 56 | `15x30x15: dimensiones auténticas de monumentos de Genshin` |
| `com/soybe/awakeningsystem/block/ElementalMonumentBlock.java` | 394 | `5. Spawnear Cofre del Tesoro desbloqueado frente al monumento` |
| `com/soybe/awakeningsystem/block/ElementalMonumentBlock.java` | 399 | `Buscar un bloque adyacente despejado para el cofre (preferentemente al frente)` |
| `com/soybe/awakeningsystem/block/ElementalMonumentBlock.java` | 408 | `Asegurar que el cofre no quede flotando en el vacío` |
| `com/soybe/awakeningsystem/block/ElementalMonumentBlock.java` | 423 | `Protogemas físicas` |
| `com/soybe/awakeningsystem/block/GoddessAltarBlock.java` | 70 | `90 segundos para completar` |
| `com/soybe/awakeningsystem/block/LockedTreasureChestBlock.java` | 163 | `Indestructible para un jugador que aún no ha desbloqueado su cofre` |
| `com/soybe/awakeningsystem/block/LockedTreasureChestBlock.java` | 184 | `Si el cofre ya está desbloqueado para este jugador: abrir su menú instanciado` |
| `com/soybe/awakeningsystem/block/LockedTreasureChestBlock.java` | 196 | `Si aún está cerrado para este jugador, verificar si sostiene la llave adecuada` |
| `com/soybe/awakeningsystem/block/LockedTreasureChestBlock.java` | 240 | `Desbloquear individualmente para este jugador (Lootr-style)` |
| `com/soybe/awakeningsystem/block/LockedTreasureChestBlock.java` | 304 | `Si el jugador está en modo creativo, desbloquear automáticamente` |
| `com/soybe/awakeningsystem/block/LockedTreasureChestBlock.java` | 328 | `Si no está desbloqueado para el jugador` |
| `com/soybe/awakeningsystem/block/OculusBlock.java` | 16 | `Hitbox centrado y flotante para que coincida con el modelo 3D` |
| `com/soybe/awakeningsystem/block/SubwayDoorBlock.java` | 97 | `Sincronizar con la puerta hermana si es una puerta doble de vagón` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 109 | `Ofrenda armónica de Protogema para Sonnet (Luna Creciente)` |
| `com/soybe/awakeningsystem/block/entity/AncientDragonEggBlockEntity.java` | 125 | `Calor: Fuego, fogata, lava o magma cerca en un radio de 3 bloques` |
| `com/soybe/awakeningsystem/block/entity/AncientDragonEggBlockEntity.java` | 145 | `Naturaleza: Hojas, flores, césped o musgo en un radio de 3 bloques` |
| `com/soybe/awakeningsystem/block/entity/AncientDragonEggBlockEntity.java` | 202 | `Generar cría de dragón (Escala 0.8x, Edad 0)` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapBlockEntity.java` | 420 | `Pequeñas burbujas periódicas cada 20 ticks (1 seg) en el agua para ambientar` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapMenu.java` | 34 | `Coordenadas idénticas a la interfaz original de Genshin Nature` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapMenu.java` | 120 | `Del inventario a la trampa: si es cebo a las ranuras 4..8` |
| `com/soybe/awakeningsystem/compat/CuriosCompatHandler.java` | 140 | `Fallback de emergencia: entregar en el inventario normal para que no se pierdan bajo ningún concepto` |
| `com/soybe/awakeningsystem/element/ElementalReactionManager.java` | 43 | `Para estados donde coexisten 2 auras (Electro-Charged o Quicken)` |
| `com/soybe/awakeningsystem/element/ElementalReactionManager.java` | 191 | `-40% Resistencia Física por 12 segundos (240 ticks)` |
| `com/soybe/awakeningsystem/element/ElementalReactionManager.java` | 443 | `Notificación directa en la barra de acción para jugadores cercanos en combate (16 bloques)` |
| `com/soybe/awakeningsystem/element/ElementalReactionManager.java` | 457 | `1. Limpieza de textos flotantes de reacciones` |
| `com/soybe/awakeningsystem/entity/AbstractAbyssMageEntity.java` | 156 | `5 segundos para canalizar la recuperación` |
| `com/soybe/awakeningsystem/entity/AbstractAbyssMageEntity.java` | 199 | `3. Flotación suave sobre el terreno mientras tiene escudo` |
| `com/soybe/awakeningsystem/entity/AbstractAbyssMageEntity.java` | 241 | `Daño aumentado mientras está pataleando indefenso en el suelo` |
| `com/soybe/awakeningsystem/entity/AbstractAbyssMageEntity.java` | 246 | `Si está cantando para recuperar el escudo, 22 de daño cancela el ritual` |
| `com/soybe/awakeningsystem/entity/AbyssalBeholderEntity.java` | 327 | `Colisión e impacto masivo` |
| `com/soybe/awakeningsystem/entity/AncientCelestialDragonBossEntity.java` | 367 | `Revisar cada 2 ticks para óptima respuesta sin sobrecarga` |
| `com/soybe/awakeningsystem/entity/AncientCelestialDragonBossEntity.java` | 620 | `Sigue cayendo al piso hacia el altar: proteger para no morir en el aire` |
| `com/soybe/awakeningsystem/entity/AncientCelestialDragonBossEntity.java` | 625 | `Si este golpe dejaría su vida en <= 30 HP, activar estado de caída directa al altar` |
| `com/soybe/awakeningsystem/entity/AncientCelestialDragonBossEntity.java` | 906 | `Ondulación senoidal en altura para serpentear en 3D` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 76 | `Constante de edad adulta: 144,000 ticks (~2 horas de tiempo real / 6 días de Minecraft)` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 79 | `Esqueleto Multipart (Hitboxes para golpear cabeza, cuello, torso y cola)` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 111 | `Inicialización de las 5 partes del esqueleto de colisión (cabeza, cuello, cuerpo y cola)` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 112 | `Las alas no tienen PartEntity separadas para evitar colisiones estáticas que bloqueen clics del jinete` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 261 | `Despegue furioso inmediato para perseguir y abalanzarse sobre el ladrón de huevos` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 345 | `Metas de Vuelo, Despegue y Vida en el Nido para Dragones Salvajes:` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 390 | `Actualización fluida de flyProgress para animaciones en cliente` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 555 | `Repulsión suave entre dragones (Boids separation) para evitar solapamientos y amontonamiento en nidos/vuelo` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 611 | `Atajo en Modo Creativo para depuración` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 641 | `B. Colocar Montura (EXCLUSIVO PARA DRAGONES ADULTOS)` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 687 | `F. Montar al dragón (EXCLUSIVO PARA DRAGONES ADULTOS CON MONTURA)` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 749 | `A escala 4.0x, la punta del hocico se proyecta ~15.8 bloques hacia adelante y 5.2 bloques de altura` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 787 | `A escala 4.0x, la punta del hocico se proyecta ~15.8 bloques hacia adelante y 5.2 bloques de altura` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 822 | `FÍSICAS DE VUELO DRAGÓN CELESTIAL ASIÁTICO` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 863 | `¡El ratón NO inclina el torso para permitir mirar libremente en 1ª persona sin desfasar la cámara!` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 926 | `Gravedad terrestre firme para adherencia continua a pendientes, colinas y desniveles` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 937 | `Modo Vuelo Autónomo para Dragones Salvajes / Libres (sin jinete)` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 990 | `Altura base calibrada exactamente sobre el cojín de la montura escalada` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 997 | `Posición longitudinal centrada en la montura escalada` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 1254 | `Diversificar radio, sentido de giro, velocidad y altitud segun el ID de la entidad` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 1328 | `Orientación de la entidad suavemente alineada al vector de velocidad tridimensional` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 1389 | `Evitar aterrizar pegado o encima de otro dragón` |
| `com/soybe/awakeningsystem/entity/AncientDragonEntity.java` | 1428 | `ÚNICA CONDICIÓN VÁLIDA PARA DESACTIVAR EL VUELO: TOCAR SUELO FIRME REAL` |
| `com/soybe/awakeningsystem/entity/AstralBreathProjectileEntity.java` | 35 | `No additional synced data needed` |
| `com/soybe/awakeningsystem/entity/AstralDragonEntity.java` | 138 | `Curar si está herido y se le ofrece comida` |
| `com/soybe/awakeningsystem/entity/AstralDragonEntity.java` | 177 | `Control de gravedad: si está sentado desciende y se sienta en tierra, si no, vuela suavemente` |
| `com/soybe/awakeningsystem/entity/AstralDragonEntity.java` | 190 | `Radio de 12 bloques` |
| `com/soybe/awakeningsystem/entity/AwakeningCoinEntity.java` | 74 | `Partículas de estela giratoria dorada mientras vuela` |
| `com/soybe/awakeningsystem/entity/AwakeningCoinEntity.java` | 174 | `Física de rebote de la ficha contra el suelo o muros` |
| `com/soybe/awakeningsystem/entity/AzhdahaBossEntity.java` | 225 | `Ondas de choque concéntricas en radio de 9 bloques` |
| `com/soybe/awakeningsystem/entity/BaranBossEntity.java` | 224 | `Spawn Shadow Extraction Soul (Exclusivo para Monarca de las Sombras)` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 228 | `Evitar que la cabeza dé tirones erráticos intentando mirar al jugador al estar montado o domesticado` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 282 | `Cada vértebra sigue progresivamente a la anterior para curvar el cuerpo entero en arcos y círculos` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 481 | `Domesticación rápida en modo creativo para pruebas` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 501 | `Solo la instancia que controla la montura ejecuta las físicas locales de vuelo;` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 502 | `previene tirones por doble simulación concurrente entre cliente y servidor.` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 508 | `Espacio presionado: Cabeza empinada hacia el cielo (-65°) para ascender en línea recta` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 527 | `Preservar yRotO, yBodyRotO y xRotO con los valores del inicio del tick` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 528 | `para permitir que rotLerp interpole con suavidad total a 144+ FPS sin tirones a 20 Hz` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 562 | `Soltar ambas teclas: Altitud bloqueada sin deriva (¡CERO vuelo infinito!)` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 594 | `Escala colosal 3.6x:` |
| `com/soybe/awakeningsystem/entity/CelestialDragonEntity.java` | 609 | `Proyección a espacio de mundo (MobRenderer aplica escala 3.6x y traslación -1.501)` |
| `com/soybe/awakeningsystem/entity/ChildeBossEntity.java` | 357 | `3. Destino Entrelazado (Intertwined Fate para el Gacha)` |
| `com/soybe/awakeningsystem/entity/CloudCraneEntity.java` | 155 | `Montar para volar` |
| `com/soybe/awakeningsystem/entity/CloudCraneEntity.java` | 230 | `Consumir el salto del jugador en este tick e INMEDIATAMENTE ponerlo a 0 para que no se quede subiendo infinito` |
| `com/soybe/awakeningsystem/entity/CloudCraneEntity.java` | 280 | `Caída suave de planeo hacia la tierra para aterrizar` |
| `com/soybe/awakeningsystem/entity/CloudCraneEntity.java` | 284 | `Si toca tierra mientras descendía: aterriza y pasa a caminata` |
| `com/soybe/awakeningsystem/entity/CloudCraneEntity.java` | 308 | `Si está en tierra, apagar estado de vuelo` |
| `com/soybe/awakeningsystem/entity/CloudCraneEntity.java` | 313 | `Si está en el aire salvaje (sin jinete)` |
| `com/soybe/awakeningsystem/entity/CloudCraneEntity.java` | 327 | `Planeo suave para aves salvajes sin jinete` |
| `com/soybe/awakeningsystem/entity/CryoRegisvineEntity.java` | 146 | `Si está aturdida, no realiza ataques` |
| `com/soybe/awakeningsystem/entity/CryoRegisvineEntity.java` | 332 | `Si está aturdida recibe daño crítico incrementado (1.8x)` |
| `com/soybe/awakeningsystem/entity/CryoRegisvineEntity.java` | 366 | `Drops de Núcleo de Escarcha y Jade Shivada con física nativa` |
| `com/soybe/awakeningsystem/entity/DokkaebiEntity.java` | 119 | `Si está dentro de un bloque sólido (por ejemplo en una cueva con techo bajo), reposicionar al aire` |
| `com/soybe/awakeningsystem/entity/DokkaebiEntity.java` | 136 | `Ajustar altura de seguimiento para no incrustarse en el techo de cuevas` |
| `com/soybe/awakeningsystem/entity/DokkaebiEntity.java` | 146 | `Teletransporte seguro si está muy lejos` |
| `com/soybe/awakeningsystem/entity/DokkaebiEntity.java` | 153 | `Despedida si expira el tiempo` |
| `com/soybe/awakeningsystem/entity/DokkaebiEntity.java` | 338 | `3 segundos de cooldown entre sonidos para evitar spam en cuevas` |
| `com/soybe/awakeningsystem/entity/DokkaebiEntity.java` | 374 | `El Dokkaebi está protegido por la barrera del Star Stream e inmune a asfixia en bloques` |
| `com/soybe/awakeningsystem/entity/ElementalLordBossEntity.java` | 167 | `Ráfaga cortante y levitación` |
| `com/soybe/awakeningsystem/entity/GenshinSlimeEntity.java` | 275 | `Fricción en el suelo para asentarse y lucir la animación squash antes de saltar de nuevo` |
| `com/soybe/awakeningsystem/entity/GoblinWarriorEntity.java` | 60 | `Probabilidad de bloquear con escudo de madera` |
| `com/soybe/awakeningsystem/entity/GoldenWolflordBossEntity.java` | 150 | `Si está activo el escudo, emitir partículas de Geo brillantes` |
| `com/soybe/awakeningsystem/entity/GoldenWolflordBossEntity.java` | 255 | `--- METAS DE IA PARA VUELO Y COMBATE ---` |
| `com/soybe/awakeningsystem/entity/HilichurlEntity.java` | 135 | `Ranged attack goal para Shooter` |
| `com/soybe/awakeningsystem/entity/HilichurlEntity.java` | 143 | `Melee attack goal para Berserker y Guard` |
| `com/soybe/awakeningsystem/entity/HilichurlEntity.java` | 167 | `Verificar si el atacante está al frente (ángulo frontal)` |
| `com/soybe/awakeningsystem/entity/IgrisBossEntity.java` | 226 | `Spawn Shadow Extraction Soul (Exclusivo para Monarca de las Sombras)` |
| `com/soybe/awakeningsystem/entity/IsekaiSlimeEntity.java` | 309 | `Slime Gigante salvaje: probabilidad de domesticar (35% o instantáneo para oro mítico)` |
| `com/soybe/awakeningsystem/entity/IsekaiSlimeEntity.java` | 355 | `Si es pequeño y domesticado: alimentarlo puede hacerlo crecer a Gigante` |
| `com/soybe/awakeningsystem/entity/IsekaiSlimeEntity.java` | 400 | `Acompaña el squash & stretch con amortiguación para evitar cualquier temblor de cámara` |
| `com/soybe/awakeningsystem/entity/IsekaiSlimeEntity.java` | 535 | `En el aire: mantener aceleración horizontal para que los saltos cubran largas distancias` |
| `com/soybe/awakeningsystem/entity/IsekaiSlimeEntity.java` | 677 | `airborneTicks >= 3 evita temblor/flickering en esquinas y bordes de bloques` |
| `com/soybe/awakeningsystem/entity/IsekaiSlimeEntity.java` | 878 | `Cuando tiene un jinete montado, las físicas las comanda travel()` |
| `com/soybe/awakeningsystem/entity/JadeArmoredBeastEntity.java` | 76 | `Partículas de jade mientras rueda/embiste` |
| `com/soybe/awakeningsystem/entity/JadeGuardianBossEntity.java` | 177 | `Soltar la Llave de Jade Ancestral para desbloquear el Sarcófago` |
| `com/soybe/awakeningsystem/entity/LeyLineMonolithEntity.java` | 58 | `Obelisco masivo anclado por magia antigua` |
| `com/soybe/awakeningsystem/entity/LeyLineMonolithEntity.java` | 98 | `Limitar daño por golpe individual (máximo 15.0F = 0.5% de vida) para evitar que lo destrocen en segundos` |
| `com/soybe/awakeningsystem/entity/LunarShadowEntity.java` | 85 | `Embestida Astral Lunar si está entre 4 y 14 bloques` |
| `com/soybe/awakeningsystem/entity/MitachurlEntity.java` | 131 | `Variante Escudo Colosal: bloquea el 85% de impactos frontales` |
| `com/soybe/awakeningsystem/entity/MitachurlEntity.java` | 180 | `Colisión con el objetivo o entidades cercanas` |
| `com/soybe/awakeningsystem/entity/ModEntities.java` | 319 | `Resto de Alma de Sombra (Para extracción con 'Surge')` |
| `com/soybe/awakeningsystem/entity/ModEntities.java` | 328 | `Asiento de Meditación de Loto para Cultivo de Qi` |
| `com/soybe/awakeningsystem/entity/MountHuaDiscipleEntity.java` | 71 | `Defenderse si es atacado por cualquier entidad` |
| `com/soybe/awakeningsystem/entity/PaimonEntity.java` | 83 | `Deduplicación activa periódica (evita clones en caso de recargas de chunks o reingresos)` |
| `com/soybe/awakeningsystem/entity/PaimonEntity.java` | 104 | `Teletransportarse si está demasiado lejos` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 164 | `Partículas divinas de levitación y electro bajo el mecha flotante` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 192 | `Mecánica de levitación / vuelo divino en Fase 2 (Marioneta del Juicio)` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 197 | `Asegurar que no tenga ítems de mano residuales en Fase 2` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 202 | `Desactivar navegación terrestre para que no intente correr en el suelo` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 216 | `Altura de levitación deseada: al menos 2.2 bloques sobre el suelo o jugador` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 223 | `Impulso vertical suave para mantenerse flotando en el aire` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 357 | `Onda expansiva de corte de trueno dimensional (Musou no Hitotachi)` |
| `com/soybe/awakeningsystem/entity/RhodeiaOceanidBossEntity.java` | 115 | `Limpieza de mímicos derrotados` |
| `com/soybe/awakeningsystem/entity/RhodeiaOceanidBossEntity.java` | 198 | `85% de reducción mientras haya mímicos con vida` |
| `com/soybe/awakeningsystem/entity/RuinGuardEntity.java` | 183 | `Drops de materiales de Khaenri'ah con física nativa` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 176 | `Los autómatas de levitación nunca sufren daño por caída` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 183 | `Partículas en cliente para los propulsores arcanos` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 218 | `Impulso vertical para volver a elevarse` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 245 | `Reducir tiempos de enfriamiento de habilidades` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 255 | `En Genshin Impact: Mientras está en el aire en Modo Bombardeo, impactar su ojo frontal` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 280 | `Recibe daño aumentado mientras está indefenso en el suelo` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 301 | `Drops de materiales de Khaenri'ah con física e interactividad nativa de Minecraft` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 386 | `Cada 22 ticks dispara una salva de misiles teledirigidos` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 513 | `Daño radial cada 8 ticks a enemigos en radio de 3.0 bloques` |
| `com/soybe/awakeningsystem/entity/RuinSentinelEntity.java` | 174 | `Defensor bloquea gran parte del daño frontal` |
| `com/soybe/awakeningsystem/entity/ShadowDirewolfEntity.java` | 139 | `A. Alimentar para curar` |
| `com/soybe/awakeningsystem/entity/SlashWaveEntity.java` | 145 | `Colisión con bloques (si impacta contra un bloque sólido grueso y no perfora bloques)` |
| `com/soybe/awakeningsystem/entity/SpiritFoxEntity.java` | 142 | `1. Efecto de Aura de Concentración de Qi para el maestro (Servidor)` |
| `com/soybe/awakeningsystem/entity/StonehideLawachurlEntity.java` | 157 | `Drops de cuernos y materiales de Lawachurl con física nativa` |
| `com/soybe/awakeningsystem/entity/SubwayTrainCarriageEntity.java` | 129 | `Fricción pasiva de rodadura` |
| `com/soybe/awakeningsystem/entity/WhiteThunderTigerEntity.java` | 138 | `Curar con comida si está herido` |
| `com/soybe/awakeningsystem/entity/WhiteThunderTigerEntity.java` | 159 | `Si está parado y no tiene jinete: Montar` |
| `com/soybe/awakeningsystem/entity/WildfireEntity.java` | 411 | `Retroceder un poco para mantener distancia` |
| `com/soybe/awakeningsystem/entity/WyvernEntity.java` | 65 | `~1 hora de juego para ser adulto` |
| `com/soybe/awakeningsystem/entity/WyvernEntity.java` | 268 | `Físicas de vuelo montado para el Wyvern` |
| `com/soybe/awakeningsystem/entity/WyvernEntity.java` | 459 | `Silenciado para evitar el sonido de madera o choque al descender o colisionar con bloques` |
| `com/soybe/awakeningsystem/entity/YggdrasilTreantBossEntity.java` | 237 | `Recompensas legendarias: Semilla del Corazón de Yggdrasil y Llave del Treant para el Cofre Encadenado` |
| `com/soybe/awakeningsystem/entity/hypostasis/AnemoHypostasisBossEntity.java` | 92 | `Dispara proyectiles en ráfagas (cada 15% de progreso)` |
| `com/soybe/awakeningsystem/entity/hypostasis/AnemoRevivalOrbEntity.java` | 51 | `Si un jugador la toca físicamente, se absorbe y destruye` |
| `com/soybe/awakeningsystem/entity/hypostasis/ElectroHypostasisBossEntity.java` | 155 | `Chequeo de colisión de láser` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 165 | `Fin del ataque -> entrar en Estado Vulnerable (Downtime para el jugador)` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 170 | `Descender hacia el suelo para exponer el núcleo` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 175 | `Reensamblar y volver al estado protegido` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 191 | `Tiempo agotado sin destruir prismas -> Se recupera 30% de HP` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 246 | `17 segundos para romper los prismas` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 301 | `Si está en fase de renacimiento o ya derrotado, es completamente inmune` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 309 | `Si está protegido, absorbe el 90% del daño y genera efecto de desvío` |
| `com/soybe/awakeningsystem/entity/hypostasis/HypostasisBaseBossEntity.java` | 319 | `Si está VULNERABLE, recibe el 100% del daño + golpe crítico garantizado (+25% extra)` |
| `com/soybe/awakeningsystem/furnace/CustomTierFurnaceBlockEntity.java` | 218 | `Tiempo total de cocción: normal según el tier, o 2x velocidad si tiene mejora de Alto Horno o Ahumador` |
| `com/soybe/awakeningsystem/furnace/CustomTierFurnaceBlockEntity.java` | 358 | `Combustible especial cósmico para el Horno Astral` |
| `com/soybe/awakeningsystem/furnace/FurnaceUpgradeItem.java` | 85 | `Evitar duplicación al romper` |
| `com/soybe/awakeningsystem/item/AwakeningCoinItem.java` | 58 | `Tiro vertical parabólico para Cara o Cruz` |
| `com/soybe/awakeningsystem/item/DreadScytheItem.java` | 88 | `Radio de atracción y daño necrótico moderado` |
| `com/soybe/awakeningsystem/item/DreadScytheItem.java` | 116 | `Tajo de Segado Espectral de Vacío (Ángulo de guadaña -30 grados)` |
| `com/soybe/awakeningsystem/item/ElucidatorItem.java` | 85 | `Ráfaga Física de Tajos de Vacío (Starburst Stream V2)` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 332 | `1. Intentar fusionar en stacks existentes si es apilable (excluyendo la mano activa)` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 633 | `Escalar con armadura del jugador` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 740 | `Bono de +20 Vida Máxima (10 corazones) mientras se empuña` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 753 | `Bono de +4 Vida Máxima (2 corazones) mientras se empuña` |
| `com/soybe/awakeningsystem/item/GiganticGreatswordItem.java` | 58 | `Seismic leap & slam: si está en el suelo, salta hacia adelante/arriba; si está en el aire, picado hacia el suelo` |
| `com/soybe/awakeningsystem/item/HeavenlyEnlightenmentPillItem.java` | 38 | `Abre o purifica un meridiano bloqueado si existe alguno pendiente` |
| `com/soybe/awakeningsystem/item/LambentLightItem.java` | 69 | `2. Propulsión física ultrarrápida del usuario` |
| `com/soybe/awakeningsystem/item/PlumBlossomSwordItem.java` | 76 | `2. Danza de los 24 Pétalos del Monte Hua (Cuchillas Físicas de Qi Floral)` |
| `com/soybe/awakeningsystem/item/StaffOfHomaItem.java` | 49 | `Sacrifica 15% de salud actual para activar Mariposa del Averno` |
| `com/soybe/awakeningsystem/item/WeaponLevelManager.java` | 250 | `Estadísticas escaladas` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 169 | `Si el jugador está físicamente dentro del Reino, sincronizar con los cofres de la plataforma` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 211 | `Guardar posición y dimensión actual para el retorno seguro` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 355 | `Generar suelo y plataforma (SOLO reemplaza aire para no destruir cofres ni construcciones del jugador)` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 387 | `Estructuras de bienvenida para Nivel 1 (Cofre Doble Inicial = 54 ranuras del Subespacio)` |
| `com/soybe/awakeningsystem/subspace/SubspaceMenu.java` | 39 | `1. Ranuras del Subespacio Dimensional (54 casillas dinámicas por página)` |
| `com/soybe/awakeningsystem/subspace/SubspaceMenu.java` | 107 | `Del inventario del jugador al Subespacio (solo casillas desbloqueadas de la página actual)` |
| `com/soybe/awakeningsystem/util/TrackPlacementHelper.java` | 56 | `Colocar la vía con flag 3 para actualizar conexiones y curvas automáticamente` |

### 2.7. Renderizado e Interfaces (GUI & Shaders) (333 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 336 | `Renderers de NPCs de Series Manhwa` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 788 | `2. Aura sutil y distintiva para todas las sombras activas (iterando solo sobre sombras registradas)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 815 | `Limpieza periódica de caché de entidades no sombras para evitar retención prolongada de IDs` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 820 | `2. Partículas ambientales místicas para portales dimensionales activos` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1082 | `Mientras esté en el aire, evitar que shift desmonte automáticamente al jugador` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1087 | `Mientras esté en el aire, evitar que shift desmonte automáticamente al jugador` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1152 | `Mini notificación independiente para monedas (evita sobreescribir el diálogo de patrocinador)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1163 | `Máxima prioridad para los mensajes de constelaciones / patrocinadores: no pueden ser sobreescritos` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1168 | `Preservar el mensaje de la constelación sin interrupción` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1194 | `Aumentar duración a 160 ticks (~8 segundos completos) para constelaciones y mensajes importantes` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1286 | `Notificación Star Stream: escalable y reubicable según GuiConfig` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1795 | `1. Cinemática de emergencia: saliendo físicamente desde las profundidades del suelo` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1838 | `Forzar glowing en el cliente para asegurar la silueta AQUA` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1874 | `Si es el jugador local y está en primera persona, NO renderizar para no obstruir la vista en pantalla` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1878 | `En tercera persona (F5) para el jugador local, forzar que la placa se procese y dibuje` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1883 | `Si el jugador es invisible para el cliente local, no mostrar las placas` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1888 | `Comprobar distancia de renderizado según si se agacha o no (32 bloques agachado, 64 normal)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1895 | `Asegurar que el nombre vanilla también se dibuje para otros jugadores` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1989 | `En Minecraft, el nombre vanilla del jugador se renderiza en Y = 0.` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 1990 | `Las líneas de títulos y rangos deben colocarse en coordenadas Y negativas para flotar ARRIBA del nombre:` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2082 | `Renderizado Volumétrico de Auras 3D (Ki de Cultivo, Modo Saiyan y Creador)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2108 | `Escala ampliada (hw = 5.0F) para cubrir completamente el vano circular (7 bloques)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2114 | `Arco Monumental Dimensional (Torii Isekai / Murim Pagoda):` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2125 | `Puerta de Mazmorra en el Overworld: colosal e imponente como en Solo Leveling (escalada por rango)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2150 | `2. Animación orgánica de respiración dimensional (sutil pulso de vida)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2170 | `Opacidad según estado: Activo (245, opaco/denso) vs Bloqueado (55, tenue/translúcido permitiendo ver el cielo)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2178 | `Vórtice Dimensional Limpio (Cara Frontal)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2188 | `Vórtice Dimensional Limpio (Cara Trasera)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2198 | `Capa secundaria de resonancia dimensional contra-rotativa para portales de dimensiones ("mezcla de colores" viva en 3D)` |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 2461 | `Sincronizar el jugador para que al salir no dé un salto brusco de vista` |
| `com/soybe/awakeningsystem/BountyBoardScreen.java` | 337 | `Caja de Recompensas compacta (evita solapamiento)` |
| `com/soybe/awakeningsystem/BountyBoardScreen.java` | 361 | `Indicación explicativa para misiones de recolección` |
| `com/soybe/awakeningsystem/ChestOpeningScreen.java` | 180 | `Lluvia de campanas de casino y monedas mientras se revela el premio` |
| `com/soybe/awakeningsystem/ChestOpeningScreen.java` | 191 | `Detención y revelación de la ruleta en tick 56 (o 46 para cajas comunes)` |
| `com/soybe/awakeningsystem/ChestOpeningScreen.java` | 290 | `=== FASE DE BÓVEDA DIMENSIONAL VIBRANDO ===` |
| `com/soybe/awakeningsystem/ChestOpeningScreen.java` | 421 | `3. Radios divisorios de los 8 sectores` |
| `com/soybe/awakeningsystem/ChestOpeningScreen.java` | 433 | `4. Renderizado de los 8 pods con sus ítems (Slot 0 es el ganador que frenará exactamente arriba)` |
| `com/soybe/awakeningsystem/ClassSelectionScreen.java` | 108 | `Evita el blur vanilla` |
| `com/soybe/awakeningsystem/ClassSelectionScreen.java` | 415 | `Renderizado de las 3 habilidades pasivas` |
| `com/soybe/awakeningsystem/ClassSelectionScreen.java` | 432 | `4. Botón de Confirmación / Despertar o Aviso de Clase Bloqueada` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 47 | `Evita el desenfoque vanilla` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 217 | `Tijera de recorte para el lienzo navegable` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 242 | `Banners de cabecera de rama en coordenadas del mundo` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 247 | `Transformación de ratón a coordenadas locales del mundo` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 271 | `Oro brillante para camino activo` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 275 | `Cian suave para camino disponible` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 360 | `Doble marco para raíz` |
| `com/soybe/awakeningsystem/ClassSkillTreeScreen.java` | 675 | `Límites para evitar perder el árbol` |
| `com/soybe/awakeningsystem/ConstellationChannelScreen.java` | 228 | `Renderizado 3D de la entidad` |
| `com/soybe/awakeningsystem/CultivationScreen.java` | 76 | `Evita el shader de desenfoque de Minecraft vanilla` |
| `com/soybe/awakeningsystem/CultivationScreen.java` | 417 | `Fila 2 derecha: Botón Avanzar Sub-nivel o Reino (Escalado hasta Reino 9)` |
| `com/soybe/awakeningsystem/CultivationScreen.java` | 470 | `Indicador de Calamidad Celestial si está activa` |
| `com/soybe/awakeningsystem/DungeonKeyScreen.java` | 67 | `Evita deliberadamente el desenfoque (blur shader) de Minecraft vanilla` |
| `com/soybe/awakeningsystem/DungeonKeyScreen.java` | 108 | `Fondo nítido translúcido sin blur` |
| `com/soybe/awakeningsystem/FishingStationScreen.java` | 92 | `Fondo translúcido nítido sin activar el shader de desenfoque (blur)` |
| `com/soybe/awakeningsystem/FishingStationScreen.java` | 155 | `Renderizado según la pestaña activa` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 138 | `Vista previa dinámica para los banners` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 143 | `Cache de entidades 3D ficticias para renderizado de personajes` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 264 | `Armas para la vista previa del Banner 0` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 278 | `Personajes & Multiverso para Banners Promocionales` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 330 | `Permanente para Banner 2` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 493 | `Vacío intencionalmente para evitar shaders de blur` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 971 | `Espacio para icono, número y [+]` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 993 | `Botón '+' para recargar Carats` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 1004 | `Espacio para icono, texto y [+]` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 1013 | `Botón '+' para recargar Jade Estelar` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 1608 | `Botón Subespacio Dimensional` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3463 | `Radios de la rueda` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3607 | `Renderizado de la entidad viva 3D siguiendo al cursor con escala 56` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3680 | `Con altura y separación vertical suficiente para evitar cualquier solapamiento` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3752 | `Efecto arcoíris holográfico oficial para 3★` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3779 | `Cinta roja "¡NUEVA!" en la esquina superior izquierda si es nueva` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3932 | `Renderizar la cara de la Uma` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3968 | `Renderizar la cara del personaje (base + capa externa/cabello)` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 3988 | `Estrellas y nombres generales para armas y compañeros (no Uma)` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 4047 | `Botón Subespacio Dimensional` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 4757 | `Clic en botón Subespacio Dimensional` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 4898 | `Clic en Cápsula de Carats superior derecha para abrir recargas de Carats` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 4915 | `Clic en Cápsula de Jade Estelar superior derecha para abrir recargas de Jade Estelar` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 4933 | `Clic en Botones de Tirada (Bloqueados durante mantenimiento)` |
| `com/soybe/awakeningsystem/MurimAlchemyScreen.java` | 56 | `1. Píldora de Limpieza de Médula` |
| `com/soybe/awakeningsystem/MurimAlchemyScreen.java` | 298 | `Detección de hover para tooltips de ítems` |
| `com/soybe/awakeningsystem/MurimAlchemyScreen.java` | 401 | `Renderizado diferido de Tooltips` |
| `com/soybe/awakeningsystem/SkillDetailScreen.java` | 36 | `Deliberately avoids vanilla blur` |
| `com/soybe/awakeningsystem/SponsorSelectionScreen.java` | 96 | `Evita deliberadamente el desenfoque (blur shader) de Minecraft vanilla` |
| `com/soybe/awakeningsystem/SponsorSelectionScreen.java` | 101 | `Fondo translúcido oscuro nítido sin desenfoque` |
| `com/soybe/awakeningsystem/SponsorSelectionScreen.java` | 204 | `Descripción del Estigma (envuelta si es larga)` |
| `com/soybe/awakeningsystem/SystemBestiaryScreen.java` | 681 | `Evita desenfoque vanilla` |
| `com/soybe/awakeningsystem/SystemBestiaryScreen.java` | 865 | `Renderizado de la Entidad 3D Viva` |
| `com/soybe/awakeningsystem/SystemBestiaryScreen.java` | 908 | `Delimitar renderizado al interior del panel táctico` |
| `com/soybe/awakeningsystem/SystemGuidebookScreen.java` | 45 | `Evita deliberadamente el desenfoque (blur shader) de Minecraft vanilla` |
| `com/soybe/awakeningsystem/SystemGuidebookScreen.java` | 71 | `Fondo nítido translúcido sin blur` |
| `com/soybe/awakeningsystem/SystemGuidebookScreen.java` | 144 | `Renderizado de pestaña activa` |
| `com/soybe/awakeningsystem/SystemMaintenanceScreen.java` | 116 | `No-op intencional: Desactiva por completo el efecto de blur y oscurecimiento del fondo` |
| `com/soybe/awakeningsystem/SystemMaintenanceScreen.java` | 227 | `Renderizado de descripciones con ajuste de línea exacto (cero desborde)` |
| `com/soybe/awakeningsystem/SystemMaintenanceScreen.java` | 276 | `Render widgets (button) on top` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 248 | `5. Agarrar esquina inferior derecha para redimensionar (Resize Grip)` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 257 | `6. Arrastrar cabecera para mover libremente la ventana con el ratón` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 268 | `Clic en pestañas dimensionales del lateral izquierdo` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 460 | `Botón Evolucionar si es nivel 5` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 526 | `Botón Evolucionar si es nivel 5` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 890 | `Clic en ranura de artefacto para desequipar o mejorar` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 917 | `Clic en Cono de Luz para desequipar` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 976 | `Clic en objeto de la página actual para equipar` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 1325 | `Pestañas dimensionales del lateral izquierdo` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 1343 | `Grip de redimensionamiento en la esquina inferior derecha` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 1382 | `Tooltips de pestañas dimensionales al final para máxima prioridad visual` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 1404 | `1. RENDERIZADO DE LAS 5 CASILLAS DE ARTEFACTOS` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 1789 | `Subespacio Dimensional siempre está disponible en todas las versiones (al final de la lista)` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 2199 | `Fondo y marco holográfico para el avatar del jugador` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 2208 | `Renderizado del modelo 3D del jugador o tarjeta simplificada según configuración` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 2446 | `Tecla bloqueada` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 3022 | `Botón rápido para ver Fábulas` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 3187 | `Contar monedas físicas en el inventario del jugador` |
| `com/soybe/awakeningsystem/SystemScreen.java` | 3446 | `Botón de acción Comprar / Abrir / Bloqueado` |
| `com/soybe/awakeningsystem/WelkinMoonScreen.java` | 159 | `4. Centro visual: Cálculo de dimensiones adaptativas` |
| `com/soybe/awakeningsystem/WelkinMoonScreen.java` | 170 | `7. Renderizado de la Doncella Lunar (Estándar o Columbina Ascendida)` |
| `com/soybe/awakeningsystem/WornBookScreen.java` | 102 | `Borde interior de tinta dorada antigua` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssScreen.java` | 96 | `Evitar el fondo gris borroso por defecto de Minecraft` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssScreen.java` | 123 | `Renderizado de la Vitrina de Puertas de Dominio` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssScreen.java` | 286 | `Indicador de triángulo activo debajo de la pestaña` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssScreen.java` | 325 | `Flechas de Paginación (solo para el Corredor del Abismo)` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssScreen.java` | 347 | `Renderizado individual de las 4 Puertas Sagradas` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssScreen.java` | 368 | `Elevación suave tridimensional si está seleccionada` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssScreen.java` | 838 | `Medidas de las puertas idénticas a render` |
| `com/soybe/awakeningsystem/blackmarket/client/BlackMarketScreen.java` | 176 | `Fondo oscuro semitransparente con viñeta para atenuar el mundo 3D y etiquetas` |
| `com/soybe/awakeningsystem/blackmarket/client/BlackMarketScreen.java` | 224 | `Renderizado de las 3 tarjetas de comercio según la pestaña` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapScreen.java` | 64 | `Tinte suave nítido sin activar el post-processing shader de desenfoque (blur)` |
| `com/soybe/awakeningsystem/casino/darts/client/DartboardScreen.java` | 355 | `Cerrar GUI para jugar en el mundo físico!` |
| `com/soybe/awakeningsystem/casino/darts/client/DartRenderer.java` | 52 | `Escalar modelo para tamaño reglamentario de torneo (aprox 20 cm)` |
| `com/soybe/awakeningsystem/casino/darts/client/DartRenderer.java` | 73 | `4. Plumas de Competición en Cruz (+) (Flights aerodinámicas rojas con ribete dorado)` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableBlockEntityRenderer.java` | 54 | `Nivel de luz vibrante para que las bolas no se vean oscuras ni opacas en 3D` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 137 | `1. Dimensiones de la mesa de pool en la UI` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 143 | `2. Renderizado del marco de madera y diamantes` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 162 | `5. Transformación de coordenadas de mesa a UI` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 177 | `7. Renderizado de las 16 bolas con números 1 al 15` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 203 | `10. Marcador superior HUD dividido en tarjetas independientes y sin solapamiento` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 207 | `11. Panel inferior: Barra de fuerza y botones (oculto mientras el modal esté abierto para evitar solapamientos)` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 212 | `Modal de configuración de partida si está activo` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 249 | `Sincronizar ángulo de puntería con espectadores cada 150 ms` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 336 | `Bola lisa: cuerpo completo de color y círculo blanco central para el número` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 412 | `Indicador de porcentaje arriba del medidor (evita solapamiento con controles inferiores)` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 635 | `Scrim oscuro que cubre toda la pantalla para enfoque modal completo` |
| `com/soybe/awakeningsystem/casino/pool/client/PoolTableScreen.java` | 656 | `Sombra exterior y fondo 100% opaco para que NADA se transluzca por debajo` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 36 | `Dimensiones de la mesa` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 136 | `Suprimir blur vanilla para mantener el mundo exterior completamente nítido` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 141 | `Suprimir blur shader` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 169 | `Oscurecido suave sin shader de desenfoque` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 257 | `RENDERIZADO: MODO SOLO (vs DEALER)` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 326 | `Fila 1: Botones de Fichas de Casino físicas (5 fichas amplias y centradas)` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 413 | `RENDERIZADO: MODO PVP (DUELO ENTRE JUGADORES)` |
| `com/soybe/awakeningsystem/casino/screen/BlackjackScreen.java` | 418 | `Caso 1: No hay reto creado en esta mesa física` |
| `com/soybe/awakeningsystem/casino/screen/CasinoCashierScreen.java` | 122 | `Suprimir blur vanilla para mantener el mundo exterior completamente nítido` |
| `com/soybe/awakeningsystem/casino/screen/CasinoCashierScreen.java` | 127 | `Suprimir blur shader` |
| `com/soybe/awakeningsystem/casino/screen/CasinoCashierScreen.java` | 132 | `Oscurecimiento nítido sin shader de blur` |
| `com/soybe/awakeningsystem/casino/screen/CasinoCashierScreen.java` | 194 | `Renderizado del ítem 3D de la ficha` |
| `com/soybe/awakeningsystem/casino/screen/ClandestineCashierScreen.java` | 121 | `Suprimir blur vanilla para mantener nitidez` |
| `com/soybe/awakeningsystem/casino/screen/ClandestineCashierScreen.java` | 126 | `Suprimir blur shader` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 59 | `Dimensiones ampliadas y cómodas de la mesa` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 107 | `La bola viaja 8 vueltas en contra-rotación (-2880°) y ajusta la fase para aterrizar suavemente en la casilla` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 125 | `Iniciar animación física de giro` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 134 | `Suprimir blur vanilla para mantener el mundo exterior completamente nítido` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 139 | `Suprimir blur shader` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 167 | `Fondo oscuro translúcido nítido (sin shader de blur)` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 319 | `La aguja está arriba (ángulo polar 270° o -90°)` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 335 | `Casilla de color con 3x3 para visibilidad` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 343 | `Bolita de marfil con física de descenso hacia la casilla` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 538 | `Marco dorado resplandeciente para indicar apuesta activa en la casilla` |
| `com/soybe/awakeningsystem/casino/screen/RouletteScreen.java` | 576 | `Fichas Sagradas físicas (si posee, excluyendo fichas clandestinas en mesas legales)` |
| `com/soybe/awakeningsystem/casino/screen/SlotMachineScreen.java` | 126 | `Cargar sprites de líneas con sus dimensiones exactas de textura (14px para horizontales, 112px para diagonales)` |
| `com/soybe/awakeningsystem/casino/screen/SlotMachineScreen.java` | 407 | `Vacío intencionalmente para evitar shaders de blur vanilla de Minecraft` |
| `com/soybe/awakeningsystem/casino/screen/SlotMachineScreen.java` | 412 | `Suprimir blur shader` |
| `com/soybe/awakeningsystem/casino/screen/SlotMachineScreen.java` | 417 | `Fondo oscuro semitransparente limpio sin shader de blur` |
| `com/soybe/awakeningsystem/casino/screen/SlotMachineScreen.java` | 452 | `Renderizar widgets/botones` |
| `com/soybe/awakeningsystem/casino/screen/SlotMachineScreen.java` | 481 | `Limitar el renderizado de símbolos estrictamente a la ventana visible de los rodillos` |
| `com/soybe/awakeningsystem/client/gui/CompanionAscensionScreen.java` | 194 | `1. SELECTOR Y RENDER 3D (Panel Izquierdo: ancho 120)` |
| `com/soybe/awakeningsystem/client/gui/CompanionAscensionScreen.java` | 220 | `Caja de render 3D` |
| `com/soybe/awakeningsystem/client/gui/CompanionScreen.java` | 142 | `1. RENDER 3D DEL COMPAÑERO (Panel Izquierdo)` |
| `com/soybe/awakeningsystem/client/gui/CompanionScreen.java` | 270 | `5. MINI INVENTARIO INFERIOR (Para arrastrar o equipar artefactos)` |
| `com/soybe/awakeningsystem/client/gui/GuiConfig.java` | 43 | `Sin desenfoque por defecto (estilo AR nítido)` |
| `com/soybe/awakeningsystem/client/gui/GuiConfigScreen.java` | 116 | `1. Escala de la ventana` |
| `com/soybe/awakeningsystem/client/gui/GuiConfigScreen.java` | 169 | `1. Escala del HUD` |
| `com/soybe/awakeningsystem/client/gui/GuiConfigScreen.java` | 194 | `2. Escala de Constelaciones (Pop-ups de donación)` |
| `com/soybe/awakeningsystem/client/gui/GuiConfigScreen.java` | 335 | `Escala: Menos, Más, 100%` |
| `com/soybe/awakeningsystem/client/gui/GuiConfigScreen.java` | 407 | `1. Escala HUD` |
| `com/soybe/awakeningsystem/client/gui/GuiConfigScreen.java` | 431 | `2. Escala Constelaciones` |
| `com/soybe/awakeningsystem/client/gui/GuiConfigScreen.java` | 479 | `5. Resetear Coordenadas del HUD` |
| `com/soybe/awakeningsystem/client/gui/HudEditorScreen.java` | 64 | `Tinte oscuro translúcido suave sin desenfoque para ver el mundo y el juego nítido` |
| `com/soybe/awakeningsystem/client/gui/HudEditorScreen.java` | 86 | `2. Renderizar previews de los elementos del HUD y sus cajas de interacción` |
| `com/soybe/awakeningsystem/client/gui/HudEditorScreen.java` | 204 | `Renderizado del contenido / preview real del elemento` |
| `com/soybe/awakeningsystem/client/gui/HudEditorScreen.java` | 372 | `Comprobar elementos en orden inverso para priorizar el superior` |
| `com/soybe/awakeningsystem/client/gui/HudEditorScreen.java` | 452 | `Verificar clic en elemento del HUD para arrastrar` |
| `com/soybe/awakeningsystem/client/gui/HudEditorScreen.java` | 558 | `Flechas del teclado para ajuste fino píxel a píxel del elemento seleccionado` |
| `com/soybe/awakeningsystem/client/gui/VisionSelectionScreen.java` | 195 | `Si es la actual, indicador brillante en el borde izquierdo` |
| `com/soybe/awakeningsystem/client/renderer/CustomPlayerPlushieRenderer.java` | 46 | `1. Si es el jugador cliente local, obtener directamente su skin activa en tiempo real` |
| `com/soybe/awakeningsystem/client/renderer/CustomPlayerPlushieRenderer.java` | 53 | `2. Si es uno de los colaboradores o amigos reconocidos, usar su textura local HD oficial` |
| `com/soybe/awakeningsystem/client/renderer/CustomPlayerPlushieRenderer.java` | 101 | `Proporción Chibi: escala de ~14 píxeles de alto en el bloque (0.88F * 16u = 14.08 píxeles)` |
| `com/soybe/awakeningsystem/client/renderer/DragonNestModel.java` | 85 | `4. Esquinas diagonales a 45 grados para redondear el cuenco del nido` |
| `com/soybe/awakeningsystem/client/renderer/DragonNestRenderer.java` | 28 | `1. Renderizar el Nido de ramas y paja apoyado firmemente en el suelo` |
| `com/soybe/awakeningsystem/client/renderer/DragonNestRenderer.java` | 29 | `Escala ampliada a 1.55x en planta para una cuna amplia, espaciosa y majestuosa` |
| `com/soybe/awakeningsystem/client/renderer/DragonNestRenderer.java` | 39 | `2. Renderizar 1 o 2 huevos 3D descansando naturalmente sobre el lecho de paja` |
| `com/soybe/awakeningsystem/client/renderer/DragonNestRenderer.java` | 69 | `A. Posicionar la base del huevo en las coordenadas del bloque` |
| `com/soybe/awakeningsystem/client/renderer/DragonNestRenderer.java` | 77 | `C. Escalar el huevo con su pivote exactamente en la base` |
| `com/soybe/awakeningsystem/client/renderer/DragonNestRenderer.java` | 80 | `D. Alinear con el sistema de coordenadas de AncientDragonEggModel (offset base Y=24, origen Y=1.5 con XP 180)` |
| `com/soybe/awakeningsystem/client/renderer/HeadPlushieLayer.java` | 62 | `Escala del peluche chibi para sentarse en la cabeza (~55% del tamaño de bloque)` |
| `com/soybe/awakeningsystem/client/renderer/HeadPlushieLayer.java` | 91 | `Soporte adicional por compatibilidad si está cargado Curios API` |
| `com/soybe/awakeningsystem/client/renderer/LockedTreasureChestModel.java` | 71 | `Cuerpo del candado (4x5x2) que sobresale físicamente del frontal` |
| `com/soybe/awakeningsystem/client/renderer/LockedTreasureChestRenderer.java` | 87 | `Si está cerrado con candado, renderizar cadenas 3D y el gran candado de oro con grillete` |
| `com/soybe/awakeningsystem/client/renderer/PlushieBlockEntityRenderer.java` | 81 | `RENDERIZADO CHIBI CON SKIN HUMANA / ANIME 64x64 (Capas 3D sólidas, sin transparencias)` |
| `com/soybe/awakeningsystem/client/renderer/PlushieBlockEntityRenderer.java` | 91 | `Escala Chibi exacta equivalente al resto de peluches (~13-14 px de alto en el bloque)` |
| `com/soybe/awakeningsystem/client/renderer/PlushieBlockEntityRenderer.java` | 98 | `RENDERIZADO ESTÁNDAR POR MODELO JSON (Peluches tradicionales de Genshin Impact)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteAltarRenderer.java` | 46 | `2. Renderizar soporte de caballete en A (ESTRICTAMENTE DETRÁS DE LA RUEDA)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteAltarRenderer.java` | 64 | `4. Renderizar la Gran Rueda Vertical Monumental (Diámetro 2.0 bloques = Radio 1.0D)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteAltarRenderer.java` | 70 | `Renderizar el buje central compacto y las 16 clavijas físicas 3D` |
| `com/soybe/awakeningsystem/client/renderer/RouletteAltarRenderer.java` | 73 | `Renderizar carátula circular de alta definición (Doble cara: Frontal y Trasera)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteAltarRenderer.java` | 74 | `1.0 bloque de radio = 2.0 bloques de diámetro` |
| `com/soybe/awakeningsystem/client/renderer/RouletteAltarRenderer.java` | 99 | `Renderizar los 8 ítems físicos en 3D en las 8 casillas (radio generoso r = 0.65D)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteTableBlockEntityRenderer.java` | 116 | `Renderizar bola de marfil en 3D (tamaño visible y luminoso)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteTableBlockEntityRenderer.java` | 278 | `Luminiscencia máxima para garantizar que resalte nítida contra el fieltro oscuro y el latón` |
| `com/soybe/awakeningsystem/client/renderer/RouletteWheelModel.java` | 57 | `Columna / Montante izquierdo en ángulo que asciende hacia el eje (detrás de la rueda, Z: -3.5)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteWheelModel.java` | 63 | `Columna / Montante derecho en ángulo hacia el eje` |
| `com/soybe/awakeningsystem/client/renderer/RouletteWheelModel.java` | 74 | `Arco superior en voladizo para sostener la aguja desde atrás` |
| `com/soybe/awakeningsystem/client/renderer/RouletteWheelModel.java` | 93 | `3. RUEDA VERTICAL 3D (Buje compacto y 16 clavijas físicas de latón en radio 15.2 unidades)` |
| `com/soybe/awakeningsystem/client/renderer/RouletteWheelModel.java` | 102 | `16 Clavijas físicas 3D de latón en el perímetro exterior (radio 15.2 unidades)` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineGeoModel.java` | 55 | `1. Palanca física interactiva (bone4): rota hacia abajo al pulsar con ficha y regresa con rebote` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineGeoRenderer.java` | 28 | `Desplazar 0.5 bloques hacia atrás en la dirección del bloque trasero para que el frente quede al ras` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineModelRenderer.java` | 151 | `Cube 8: Repisa horizontal para apoyo` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineModelRenderer.java` | 220 | `(RENDERIZADO POR LOTES - BATCHING PARA EVITAR BUFFERBUILDER EXCEPTION)` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineModelRenderer.java` | 240 | `Precalcular ángulos de rotación y visibilidad de cada símbolo por rodillo` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineModelRenderer.java` | 300 | `Solicitar buffer exclusivo para este símbolo una única vez` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineModelRenderer.java` | 377 | `MÉTODOS DE APOYO PARA VÉRTICES Y CARAS` |
| `com/soybe/awakeningsystem/client/renderer/SlotMachineRenderer.java` | 45 | `El bloque se renderiza nativamente como modelo de bloque Java (RenderShape.MODEL)` |
| `com/soybe/awakeningsystem/client/renderer/SubwayDoorRenderer.java` | 38 | `Curva SmoothStep para simular el émbolo de aire comprimido (amortiguación suave en los extremos)` |
| `com/soybe/awakeningsystem/client/renderer/SubwayDoorRenderer.java` | 51 | `Renderizamos el modelo de la puerta en su geometría cerrada, aplicándole la traslación calculada` |
| `com/soybe/awakeningsystem/client/renderer/SwordAuraRenderer.java` | 99 | `General para cualquier AwakeningWeaponItem` |
| `com/soybe/awakeningsystem/client/renderer/VolumetricAuraRenderer.java` | 58 | `En primera persona deshabilitamos el aura sobre uno mismo para no tapar la vista` |
| `com/soybe/awakeningsystem/client/renderer/VolumetricAuraRenderer.java` | 139 | `4. Círculo Mágico de Cultivo Murim en el suelo (elevado de forma segura para eliminar z-fighting y parpadeos)` |
| `com/soybe/awakeningsystem/client/renderer/VolumetricAuraRenderer.java` | 154 | `Elevación segura a 0.065 bloques sobre el suelo para evitar coplanaridad con bloques de césped` |
| `com/soybe/awakeningsystem/client/renderer/VolumetricAuraRenderer.java` | 164 | `Separación vertical de +0.02 bloques para evitar interferencia entre ambos sellos en rotación` |
| `com/soybe/awakeningsystem/client/renderer/VolumetricAuraRenderer.java` | 382 | `Definición de las 5 alturas y radios del manto corporal` |
| `com/soybe/awakeningsystem/client/renderer/VolumetricAuraRenderer.java` | 436 | `Doble cara para que el manto sea visible tanto desde el exterior como desde el interior` |
| `com/soybe/awakeningsystem/client/track/RacetrackWandPreview.java` | 78 | `1. Renderizar balizas y pilares holográficos en cada waypoint registrado` |
| `com/soybe/awakeningsystem/client/track/RacetrackWandPreview.java` | 113 | `Límites de carril (anchura de 8 metros simétrica para los 8 competidores)` |
| `com/soybe/awakeningsystem/client/track/RacetrackWandPreview.java` | 148 | `Haz dorado para indicar la vuelta completa al circuito` |
| `com/soybe/awakeningsystem/client/track/RacetrackWandPreview.java` | 152 | `4. Guía dinámica en tiempo real hacia donde está mirando el jugador en el suelo` |
| `com/soybe/awakeningsystem/client/track/RacetrackWandPreview.java` | 160 | `Marcador flotante de previsualización en el punto apuntado` |
| `com/soybe/awakeningsystem/client/track/TrackPlacementPreview.java` | 122 | `Enviar paquete al servidor para tender las vías` |
| `com/soybe/awakeningsystem/client/track/TrackPlacementPreview.java` | 222 | `Color holográfico estilo Create: verde neón / cian si es válido, rojo si excede distancia` |
| `com/soybe/awakeningsystem/client/track/TrackPlacementPreview.java` | 251 | `2. Marcadores holográficos en cada bloque de la trayectoria (ghost track markers)` |
| `com/soybe/awakeningsystem/entity/client/AbyssChallengeKeyModel.java` | 48 | `2. Espada Ceremonial Carmesí levitante` |
| `com/soybe/awakeningsystem/entity/client/AbyssChallengeKeyModel.java` | 93 | `1. Levitación sinusoidal suave de la espada y del dial` |
| `com/soybe/awakeningsystem/entity/client/AncientCelestialDragonBossModel.java` | 144 | `2. neck2 (offset 5.0F, z = -2 a +8 -> solapamiento permanente de 3 unidades con neck1)` |
| `com/soybe/awakeningsystem/entity/client/AncientCelestialDragonBossModel.java` | 229 | `Penacho de cola en llamas de Qi celestial tridimensional` |
| `com/soybe/awakeningsystem/entity/client/AncientDragonModel.java` | 258 | `10. Patas Traseras (Misma altura total que las delanteras para pisada equilibrada)` |
| `com/soybe/awakeningsystem/entity/client/AncientDragonModel.java` | 379 | `A. Frecuencias constantes basadas en el tiempo del mundo (cero saltos de fase / cero tiritera)` |
| `com/soybe/awakeningsystem/entity/client/AncientDragonModel.java` | 393 | `Ondulación vertical en Vuelo: sustentación y aleteo armónico a medio tiempo` |
| `com/soybe/awakeningsystem/entity/client/AncientDragonModel.java` | 430 | `E. Interpolación fluida entre Tierra y Vuelo (Mth.lerp con flyProgress)` |
| `com/soybe/awakeningsystem/entity/client/AncientDragonModel.java` | 454 | `Vuelo dinámico: si está descendiendo o volando a velocidad estable, transición a planeo majestuoso` |
| `com/soybe/awakeningsystem/entity/client/AncientDragonModel.java` | 561 | `Interpolación fluida entre pose de tierra y pose de vuelo` |
| `com/soybe/awakeningsystem/entity/client/AncientDragonRenderer.java` | 50 | `Compensación de elevación para que las patas toquen exactamente la superficie del suelo sin hundirse` |
| `com/soybe/awakeningsystem/entity/client/AstralDragonModel.java` | 253 | `Alas confortablemente plegadas hacia atrás protegiendo los flancos` |
| `com/soybe/awakeningsystem/entity/client/AstralDragonRenderer.java` | 31 | `Escala armoniosa para dragón bebé compañero (se ajusta al hitbox de 0.65x0.6)` |
| `com/soybe/awakeningsystem/entity/client/CelestialDragonBreathRenderer.java` | 41 | `Renderizar perla/orbe místico con brillo emisivo completo (15728880)` |
| `com/soybe/awakeningsystem/entity/client/CelestialDragonModel.java` | 144 | `2. neck2 (offset 5.0F, caja z = -2 a +8 -> solapamiento permanente de 3 unidades con neck1)` |
| `com/soybe/awakeningsystem/entity/client/CelestialDragonModel.java` | 307 | `Cinemática pura sin restricciones artificiales para permitir enroscar el dragón libremente` |
| `com/soybe/awakeningsystem/entity/client/ClassicMonarchBossModel.java` | 95 | `Levitación flotante continua` |
| `com/soybe/awakeningsystem/entity/client/CloudCraneRenderer.java` | 30 | `Escala esbelta y majestuosa de ave sagrada (1.2x)` |
| `com/soybe/awakeningsystem/entity/client/DokkaebiModel.java` | 105 | `Animación suave de levitación flotante` |
| `com/soybe/awakeningsystem/entity/client/GenshinLargeSlimeModel.java` | 65 | `Rostro frontal con ojos canónicos de Genshin para Slime Grande (UV 0, 0)` |
| `com/soybe/awakeningsystem/entity/client/GenshinLargeSlimeModel.java` | 170 | `Interpolación fluida de squash & stretch idéntica a la del slime Isekai` |
| `com/soybe/awakeningsystem/entity/client/GenshinSlimeModel.java` | 169 | `Interpolación fluida de squash & stretch idéntica a la del slime Isekai` |
| `com/soybe/awakeningsystem/entity/client/GoblinWarriorModel.java` | 127 | `Escudo alzado protegiendo el pecho` |
| `com/soybe/awakeningsystem/entity/client/HilichurlModel.java` | 175 | `Guardia con escudo alzado protegiendose` |
| `com/soybe/awakeningsystem/entity/client/IsekaiSlimeModel.java` | 115 | `Interpolación fluida de la deformación elástica entre ticks` |
| `com/soybe/awakeningsystem/entity/client/LeyLineMonolithModel.java` | 60 | `2. Obelisco levitante central (flota sobre el pedestal)` |
| `com/soybe/awakeningsystem/entity/client/LeyLineMonolithModel.java` | 115 | `1. Levitación sinusoidal suave del obelisco central` |
| `com/soybe/awakeningsystem/entity/client/LeyLineMonolithRenderer.java` | 37 | `Renderizado nítido sin culling para apreciar los anillos orbitales` |
| `com/soybe/awakeningsystem/entity/client/LunarGuardianRenderer.java` | 21 | `Escala imponente de jefe (1.45x)` |
| `com/soybe/awakeningsystem/entity/client/ManaPigModel.java` | 83 | `Cola 3D curvada hacia arriba en la grupa (Y=+6 en coordenadas locales del cuerpo)` |
| `com/soybe/awakeningsystem/entity/client/ManaPigModel.java` | 92 | `Patas traseras recolocadas hacia adelante (Z=5.0F) para que queden bajo la grupa y no sobresalgan por detrás` |
| `com/soybe/awakeningsystem/entity/client/ManaPixieModel.java` | 147 | `Rotación y levitación del Orbe de Maná` |
| `com/soybe/awakeningsystem/entity/client/RaidenPuppetModel.java` | 899 | `En Fase 2 (Marioneta del Juicio), no se renderizan ítems de mano para evitar espadas flotantes` |
| `com/soybe/awakeningsystem/entity/client/RuinHunterModel.java` | 130 | `7. Base de Levitación y Anillo Inferior (bb_main)` |
| `com/soybe/awakeningsystem/entity/client/RuinHunterModel.java` | 188 | `2. Levitación dinámica (Bobbing sinusoidal continuo)` |
| `com/soybe/awakeningsystem/entity/client/RuinHunterModel.java` | 210 | `Brazos alzados y replegados hacia los lados para despejar el campo de fuego` |
| `com/soybe/awakeningsystem/entity/client/RuinHunterModel.java` | 215 | `Propulsores abiertos al máximo para sostener el vuelo` |
| `com/soybe/awakeningsystem/entity/client/ShadowDirewolfModel.java` | 59 | `1. Cuerpo Principal: robusto y estilizado (offset en Y=13 para altura cuadrúpeda)` |
| `com/soybe/awakeningsystem/entity/client/ShadowDirewolfRenderer.java` | 30 | `Escala imponente de Lobo Alfa / Direwolf (1.3x tamaño respecto a modelo base)` |
| `com/soybe/awakeningsystem/entity/client/ShadowMonarchBossModel.java` | 95 | `Levitación flotante continua de Monarca de las Sombras` |
| `com/soybe/awakeningsystem/entity/client/ShadowMonarchBossRenderer.java` | 39 | `Escala imponente de jefe` |
| `com/soybe/awakeningsystem/entity/client/SpiritFoxModel.java` | 90 | `y apertura tridimensional (yaw, pitch ascendente y roll de expansión)` |
| `com/soybe/awakeningsystem/entity/client/StonehideLawachurlModel.java` | 775 | `Brazos alzados para el impacto sísmico hacia el suelo` |
| `com/soybe/awakeningsystem/entity/client/UmaRenderer.java` | 63 | `En las cabinas de salida: si está lejos muestra dorsal limpio (#1..#8) para evitar amontonamiento` |
| `com/soybe/awakeningsystem/entity/client/WhiteThunderTigerModel.java` | 72 | `Pelaje erizado de hombros y cruz (con deformación para evitar Z-fighting con el cuerpo)` |
| `com/soybe/awakeningsystem/entity/client/WhiteThunderTigerModel.java` | 115 | `4. Patas Delanteras poderosas (con ligera reducción para evitar Z-fighting con la melena)` |
| `com/soybe/awakeningsystem/entity/client/WhiteThunderTigerRenderer.java` | 30 | `Escala regia de bestia espiritual felina (1.3x)` |
| `com/soybe/awakeningsystem/entity/client/WildfireModel.java` | 110 | `Si está ejecutando la onda de choque, los escudos se expanden hacia afuera` |
| `com/soybe/awakeningsystem/entity/client/WyvernModel.java` | 152 | `Si está rugiendo o lanzando aliento: abrir mandíbula inferior` |
| `com/soybe/awakeningsystem/entity/client/YggdrasilTreantModel.java` | 43 | `Cabeza de corteza antigua y corona de hojas (texOffs 0, 0)` |
| `com/soybe/awakeningsystem/entity/client/YggdrasilTreantRenderer.java` | 25 | `Escala imponente del Treant Ancestral (1.7x)` |
| `com/soybe/awakeningsystem/entity/hypostasis/client/HypostasisModel.java` | 59 | `Escala normalizada de los 8 cubos exteriores (1.35x para porte imponente de jefe)` |
| `com/soybe/awakeningsystem/entity/hypostasis/client/HypostasisRenderer.java` | 24 | `Escala imponente de jefe de mundo (1.75x)` |
| `com/soybe/awakeningsystem/fable/FablesScreen.java` | 57 | `Evita desenfoque vanilla` |
| `com/soybe/awakeningsystem/furnace/CustomTierFurnaceScreen.java` | 69 | `Tooltips fantasma en las ranuras de mejoras si están vacías` |
| `com/soybe/awakeningsystem/guild/Guild.java` | 121 | `Rebalanceado: 500 Moras = 1 EXP (evita romper el nivel con dinero)` |
| `com/soybe/awakeningsystem/guild/Guild.java` | 286 | `Perks desbloqueados` |
| `com/soybe/awakeningsystem/guild/GuildChunkMapScreen.java` | 138 | `Renderizado de celdas de chunks` |
| `com/soybe/awakeningsystem/guild/GuildChunkMapScreen.java` | 198 | `Si es reclamable libre, dibujar un punto verde central de disponibilidad` |
| `com/soybe/awakeningsystem/guild/GuildChunkMapScreen.java` | 212 | `Marcador del jugador` |
| `com/soybe/awakeningsystem/guild/GuildEventHandler.java` | 83 | `1. Reducción de daño para miembros con bendición de Fortaleza Colectiva` |
| `com/soybe/awakeningsystem/guild/GuildEventHandler.java` | 91 | `2. Aumento de daño para miembros atacantes con Poder del Gremio o Resonancia` |
| `com/soybe/awakeningsystem/guild/GuildEventHandler.java` | 231 | `Territorio Protegido (Ajeno)` |
| `com/soybe/awakeningsystem/guild/GuildEventHandler.java` | 312 | `Proteger todos los bloques en chunks reclamados contra destrucción por explosiones (Creepers, TNT, etc.)` |
| `com/soybe/awakeningsystem/guild/GuildScreen.java` | 29 | `Componentes para creación de gremio` |
| `com/soybe/awakeningsystem/guild/GuildScreen.java` | 33 | `Componente para invitar` |
| `com/soybe/awakeningsystem/guild/GuildScreen.java` | 295 | `Botones de acción para el Líder` |
| `com/soybe/awakeningsystem/guild/GuildScreen.java` | 586 | `Panel inferior: Adquirir bloque de tablón de cacerías para colocar en la base` |
| `com/soybe/awakeningsystem/guild/GuildScreen.java` | 724 | `Desbloquear Perks` |
| `com/soybe/awakeningsystem/subspace/SubspaceScreen.java` | 169 | `Tooltip para ranuras bloqueadas` |
| `com/soybe/awakeningsystem/subspace/SubspaceScreen.java` | 230 | `5. Casillas iluminadas para cada slot del menú (con detección de bloqueo)` |
| `com/soybe/awakeningsystem/subspace/SubspaceScreen.java` | 237 | `tinte carmesí oscuro si está bloqueada` |
| `com/soybe/awakeningsystem/subspace/SubspaceScreen.java` | 278 | `Contador de capacidad en tiempo real basado en ranuras desbloqueadas` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 38 | `Cache de Umas ficticias para exhibición 3D` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 379 | `Garantizar modo de iluminación plano y shader limpio para toda la interfaz` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 409 | `Marcador de Monedas en cápsula esmerilada de alto contraste` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 419 | `Renderizado específico de la pestaña activa (primero fondos)` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 426 | `Renderizar widgets interactivos encima de los fondos` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 486 | `Evita que textos, fondos y barras posteriores se rendericen con iluminación sombreada y aspecto apagado/oscuro.` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 548 | `Insignia / Pill brillante para el número del stat con alto contraste` |
| `com/soybe/awakeningsystem/uma/screen/UmaRaceScreen.java` | 78 | `Tinte oscuro translúcido suave y nítido (SIN desenfoque / blur de pantalla)` |
| `com/soybe/awakeningsystem/uma/screen/UmaRaceScreen.java` | 258 | `3. Renderizar las tarjetas de las corredoras y botones` |
| `com/soybe/awakeningsystem/uma/screen/UmaRaceScreen.java` | 270 | `Textos organizados con margen seguro para que JAMÁS se solapen con el slot de la derecha` |
| `com/soybe/awakeningsystem/uma/screen/UmaRaceScreen.java` | 364 | `Fondo oscuro apagado para botón deshabilitado` |

### 2.8. Worldgen, Estructuras y Dimensiones (144 comentarios técnicos)

| Archivo | Línea | Comentario Técnico / Decisión de Diseño |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 48 | `Si estaba en la Torre, remover sesion` |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 53 | `Asegurar la generacion del Santuario y el Gran Arbol del Mundo solo si no existe` |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 59 | `Sincronizar puertas dimensionales (incluyendo el portal de retorno del santuario)` |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 140 | `Desbloquear habilidades de series Isekai y Sistema 2.0` |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 267 | `4. Despejar completamente el vano interior del Torii para el portal estático` |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 279 | `Desactivado: El generador de dimension de ruido ya aplica el bioma correctamente` |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 308 | `y genera laderas suaves de cesped hacia el exterior para integracion total` |
| `com/soybe/awakeningsystem/IsekaiWorldManager.java` | 337 | `Rellenar solidamente hacia abajo para anclar al lecho continental` |
| `com/soybe/awakeningsystem/MurimWorldManager.java` | 50 | `Si estaba en la Torre, remover sesion` |
| `com/soybe/awakeningsystem/MurimWorldManager.java` | 55 | `Asegurar la generación del complejo de la secta inmortal flotante` |
| `com/soybe/awakeningsystem/MurimWorldManager.java` | 182 | `Subsuelo de tierra y piedra flotante (cáscara optimizada para rendimiento)` |
| `com/soybe/awakeningsystem/MurimWorldManager.java` | 364 | `Despejar bloques residuales en el vano para que quede 1 solo portal animado` |
| `com/soybe/awakeningsystem/MurimWorldManager.java` | 410 | `4. Vano interior del portal: despejado con aire para renderizar 1 solo portal vórtice dimensional unificado (gate_murim.png)` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 44 | `Coordenadas canónicas de estructuras emblemáticas y jefes de Teyvat` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 426 | `Limpiar cualquier residuo de plataformas o cajas procedurales antiguas` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 464 | `Si estaba bloqueado, activarlo automáticamente` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 501 | `Solo sincronizar el tiempo del overworld a Teyvat si nadie está durmiendo` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 511 | `Construir Hipódromo de Teyvat y asegurar inicialización` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 516 | `Ticking de todas las instancias del Hipódromo activas` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 528 | `2. Comprobar colisión de jugadores en el nivel del portal` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 555 | `Corrección de mundos existentes si Golden House se había guardado con el Y del sótano (<= 77)` |
| `com/soybe/awakeningsystem/world/MobScalingHandler.java` | 48 | `Validar si el escalado está activado` |
| `com/soybe/awakeningsystem/world/MobScalingHandler.java` | 51 | `El escalado por distancia desde (0, 0) SOLO aplica al Overworld estándar` |
| `com/soybe/awakeningsystem/world/MobScalingHandler.java` | 54 | `No escalar NPCs ni animales pacíficos (solo hostiles / enemigos / combatientes)` |
| `com/soybe/awakeningsystem/world/MobScalingHandler.java` | 60 | `Excluir mobs de calabozos, torres de ranker, pagodas o con escalado propio` |
| `com/soybe/awakeningsystem/world/MobScalingHandler.java` | 69 | `Excluir jefes si está configurado (Ender Dragon, Wither o entidades con vida base de jefe >= 250)` |
| `com/soybe/awakeningsystem/world/MobScalingHandler.java` | 77 | `Ya fue escalado` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 83 | `Solo detener el cimiento si es terreno sólido real (no hojas, no troncos flotantes, no agua)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 144 | `Asegurar que el suelo bajo el NPC sea firme y s\u00f3lido` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 149 | `Comprobar si ya existe el NPC en un radio estrecho (6 bloques) de esta posici\u00f3n concreta` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 475 | `Vía férrea estándar para vagonetas en el túnel (x = 5)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 717 | `Gran lámpara central colgante` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 740 | `Estanterías con tratados de hidráulica y cálculo de estructuras` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 750 | `Reserva de lingotes de oro y hierro de Lloyd (¡los beneficios de la finca!)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 1338 | `1. Plataforma de Suspendium Levitante` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 1525 | `1. PRIMERA TERRAZA (Radio 11 en cy + 1)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 1553 | `2. SEGUNDA TERRAZA (Radio 7 en cy + 2)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 1576 | `3. TERCERA TERRAZA (Radio 4 en cy + 3)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 1718 | `Base sólida para las vallas (evita que queden flotando en el aire)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 1742 | `Cierre interior del tejado (solo para step >= 0 para no alterar las paredes a cy + 4)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 1775 | `5. LA RULETA FÍSICA 3D - Orientada hacia el NORTE (mirando de frente al jugador al entrar)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 2366 | `Piso de madera maciza para los niveles superiores (2 al 9)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 2444 | `Despejar altura para la cabeza` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 2779 | `Limpiamos los 6 espacios de la silueta (z: cz-1..cz+1, y: cy+2..cy+3) para que no haya bloques flotantes ni colisiones.` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 3373 | `Paredes del pozo de descenso (anillo en radio 3, de cy - 7 a cy - 1)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 3402 | `Farol de rana verde en el pilar central para iluminar la bajada` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 3453 | `Omitir el hueco central de la escalera para no tapar la entrada ni los peldaños` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 3506 | `Faroles de rana verdes empotrados en las columnas para iluminación mágica` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 3889 | `Cimientos profundos hacia abajo: rellenar con pizarra para que NUNCA flote en el aire` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4004 | `1. Limpieza del espacio aéreo para todo el complejo vertical (62 bloques de altura total)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4007 | `2. Nivel Inferior: Gran Plaza del Nexus Central (Radio 12)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4036 | `NIVEL INFERIOR: PLAZA DEL NEXUS CENTRAL (RADIO 12)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4042 | `Suelo de la Plaza (radio 12)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4082 | `4 Farolas monumentales en los ángulos diagonales (±7, ±7)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4096 | `para garantizar que no queden bloques flotantes ni residuales de la quilla del castillo` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4126 | `Dejar vano de paso abierto para los portales` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4218 | `Mirador Exterior Intermedio (Y = cy + 16, Radio 8 alrededor de la torre)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4286 | `Barandilla de seguridad en el atrio superior de llegada para proteger el hueco de la escalera` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4300 | `Dejar los 4 portales de entrada completamente abiertos y limpios con aire para permitir libre paso sin barreras invisibles` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4537 | `1. CIUDADELA DEL VIENTO (COLISEO DEL NORTE - RADIO 14, 29x29)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4543 | `Suelo del Coliseo (radio 14)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4567 | `Muralla perimetral almenada con arcos de viento (radio 13-14)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4597 | `Santuario central elevado del Altar (radio 3, altura +1 y +2)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4619 | `2. BASTIÓN GLACIAL (COLISEO DEL OESTE - RADIO 14, 29x29)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4673 | `Pedestal Glacial Central del Altar (radio 3)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4695 | `3. FORTALEZA DE MAGMA (COLISEO DEL SUR - RADIO 14, 29x29)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4750 | `Pedestal Central de Obsidiana y Netherite del Altar (radio 3)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4772 | `4. AGUJA DE LA TORMENTA (COLISEO DEL ESTE - RADIO 14, 29x29)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4826 | `Pedestal Central de Cobre Cincelado del Altar (radio 3)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 4944 | `BANCOS DE TRONCOS RÚSTICOS PARA SENTARSE (EXACTAMENTE 3 EN CADA LADO: NORTE, SUR, ESTE, OESTE)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5241 | `Suelos espaciosos con aperturas limpias para escaleras` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5386 | `Lámpara central colgante en Planta Baja` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5415 | `Lámpara central colgante en Planta Alta` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5508 | `Nivel 1 elevado (grada intermedia: cy + 1 para dist <= 5.5)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5517 | `Nivel 2 elevado (sanctum central: cy + 2 para dist <= 2.8)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5581 | `Núcleo Arcano Luminoso (Nexo dimensional cargado)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5695 | `3. Tejado pagoda nivel 1 (losetas de pizarra oscura con alero a radio 5)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5728 | `5. Tejado superior de la pagoda (cy + 8, radio 3 con cobre y losetas)` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 5815 | `Tejado (alero a radio 4 con escaleras orientadas al exterior)` |
| `com/soybe/awakeningsystem/world/SeriesStructureWorldGen.java` | 242 | `2. Cimientos para terrenos irregulares` |
| `com/soybe/awakeningsystem/world/SeriesStructureWorldGen.java` | 702 | `Generar como máximo 1 por ciclo para prevenir caídas de tick` |
| `com/soybe/awakeningsystem/world/SeriesStructureWorldGen.java` | 727 | `No cerca del árbol (radio mínimo de 350 bloques)` |
| `com/soybe/awakeningsystem/world/SeriesStructureWorldGen.java` | 805 | `No cerca de la isla central de la Secta (radio mínimo de 250 bloques)` |
| `com/soybe/awakeningsystem/world/SeriesStructureWorldGen.java` | 889 | `No cerca del Arrecife de Musk (radio mínimo de 250 bloques)` |
| `com/soybe/awakeningsystem/world/SeriesStructureWorldGen.java` | 1366 | `Estructura SUBTERRÁNEA en Murim: asegurar un techo macizo de roca de al menos 25-35 bloques` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 42 | `Apertura transparente abierta (para ver el cielo en bloqueado o vórtice en activo)` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 44 | `Bloques de portal dimensional del mod` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 104 | `1. Cuenca oceánica despejada de 38 bloques de radio` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 155 | `Terraza central nivelada para la plaza del portal` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 208 | `Registrar dinámicamente en el gestor mundial para sincronización de rifts` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 211 | `1. Cimientos sólidos bajo la plataforma para evitar cualquier bloque flotante` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 286 | `--- A. VANO INTERIOR CIRCULAR (Apertura de 7 bloques de diámetro, radio < 3.45) ---` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 293 | `--- B. ANILLO CELESTIAL DE BRONCE Y GEMAS (Radio 3.45 <= r <= 4.75) ---` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 337 | `--- E. MARCO EXTERIOR DE ARENISCA ANCESTRAL (Radio 4.75 < r <= 5.85) ---` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 361 | `Espacio libre exterior: despejar aire para garantizar silueta nítida` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 408 | `4. Asegurar que el vano interior en dz = -1 y dz = +1 permanezca COMPLETAMENTE LIBRE (sin túnel)` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 412 | `Radio < ~3.6` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 99 | `1. Cimientos y suelo de la plaza circular segura (radio 5, diámetro 11)` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 106 | `Despejar cúpula aérea interior (hasta 7 bloques arriba para evitar cualquier asfixia)` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 120 | `Proyectar cimiento sólido hacia abajo (hasta 14 bloques) para evitar huecos o cuevas abiertas` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 439 | `Cofre protegido` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 557 | `1. Cimentación circular adaptativa (radio 10)` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 578 | `2. Hoguera Central de Cocina Segura (Sin fuego activo dañino para que el Mitachurl no se autoinmole)` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 688 | `COORDENADAS CANÓNICAS DE LAS 5 CAPITALES Y SEDES DE TEYVAT` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 730 | `Cimientos adaptativos masivos para la capital` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 1320 | `1. Limpiar Ciudadela de Favonius antigua en (0, -320)` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 1354 | `2. Limpiar Plaza de Llegada antigua en (0, 0)` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 1418 | `3. Si es generación natural del mundo, comprobar espaciado para no apiñar troncos` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 1782 | `1. Enormes raíces tabulares (Buttress roots) escalonadas de 4 bloques de altura y 4 de radio` |
| `com/soybe/awakeningsystem/world/TeyvatRegionalBuilder.java` | 2286 | `Colgar algunas hojas hacia abajo para dar efecto de cerezo llorón` |
| `com/soybe/awakeningsystem/world/TeyvatStructureManager.java` | 82 | `Solo limpiar vegetación localizada si es arena de jefe` |
| `com/soybe/awakeningsystem/world/TeyvatStructureManager.java` | 95 | `Adaptación exclusiva para santuarios de las profundidades` |
| `com/soybe/awakeningsystem/world/TeyvatStructureManager.java` | 152 | `Soporte adaptativo natural únicamente bajo el tronco central del árbol (radio 14)` |
| `com/soybe/awakeningsystem/world/TeyvatStructureManager.java` | 172 | `Dimensiones del árbol gigante: 143x136x138 -> offset (-71, -2, -69)` |
| `com/soybe/awakeningsystem/world/TeyvatStructureManager.java` | 324 | `4. Hojas generadas naturalmente sin persistencia (las del NBT tienen persistent=true)` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 260 | `Evaluamos en un radio razonable alrededor del jugador (dx, dz de 2 a 6)` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 270 | `Solo procesar chunks que ya estén cargados por el servidor para evitar bloqueos` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 278 | `Presupuesto de 6 chunks por ciclo para evitar tirones` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 679 | `2. Hoguera Central Apagada (LIT = false) con Caldero y Asientos de Troncos (Inofensiva para los mobs)` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 709 | `Cofre protegido en el fondo de la choza` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1275 | `Asegurar que el bloque central sea el ElementalMonumentBlock del elemento` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1323 | `Terreno demasiado empinado, descartar para evitar santuario flotante` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 30 | `Excluir dimensiones de calabozos, torres y pagodas` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 129 | `Si ya está escalado a este nivel o superior, no degradar` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 143 | `Escalar vida: +6.5% de vida extra por cada nivel por encima del base` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 148 | `Escalar daño: +1.5% de daño extra por cada nivel` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 152 | `Escalar armadura y tenacidad` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 180 | `Actualizar etiqueta de nombre visible con el nivel escalado` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 227 | `1. Escalar dinámicamente si el atacante tiene nivel superior` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 231 | `Esto previene que armas rotas o combinaciones infinitas eliminen al jefe de 1-2 golpes.` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 245 | `Reducir daño de spam continuo para forzar interacción y tiempos de recarga` |
| `com/soybe/awakeningsystem/world/WuXingPuzzleManager.java` | 34 | `2. Comprobar si este pedestal ya está activado` |
| `com/soybe/awakeningsystem/world/WuXingPuzzleManager.java` | 42 | `3. Obtener todos los pedestales en la sala para sincronizar estados de forma infalible` |
| `com/soybe/awakeningsystem/world/WuXingPuzzleManager.java` | 146 | `1. Buscar la linterna marina del Taiji central (cx, cy, cz) en un radio de 10 bloques` |
| `com/soybe/awakeningsystem/world/WuXingPuzzleManager.java` | 158 | `2. Buscar en datos guardados de estructuras del servidor` |
| `com/soybe/awakeningsystem/world/WuXingPuzzleManager.java` | 170 | `3. Fallback: Agrupar por la posición del pedestal más al noroeste para que todos compartan el mismo centro` |
| `com/soybe/awakeningsystem/worldgen/DragonNestFeature.java` | 34 | `Centrar el nido en el trozo para que el radio 7 quede confinado estrictamente` |
| `com/soybe/awakeningsystem/worldgen/DragonNestFeature.java` | 59 | `1. Crear una gran caldera volcánica / santuario de nido colosal (Radio 7 bloques)` |
| `com/soybe/awakeningsystem/worldgen/DragonNestFeature.java` | 107 | `B. Despejar espacio aéreo vertical para que el nido y el dragón adulto quepan con total amplitud` |
| `com/soybe/awakeningsystem/worldgen/GiantFantasyTreeFeature.java` | 75 | `4. Despeje vertical de 16 bloques hacia arriba (para nunca generarse debajo ni dentro de otro árbol)` |
| `com/soybe/awakeningsystem/worldgen/GiantFantasyTreeFeature.java` | 83 | `5. Despeje horizontal para evitar que dos árboles colosales se solapen` |
| `com/soybe/awakeningsystem/worldgen/TeyvatCuihuaTreeFeature.java` | 31 | `1. Proteger el radio sagrado del Gran Roble de Levantaviento (Windrise)` |
| `com/soybe/awakeningsystem/worldgen/TeyvatHilichurlCampFeature.java` | 55 | `1. Evitar generar sobre agua, vacío o fuera de rango de superficie` |
| `com/soybe/awakeningsystem/worldgen/TeyvatHilichurlCampFeature.java` | 104 | `5. Si el terreno era demasiado accidentado para una aldea grande completa, fallback al campamento procedural adaptativo` |

---

## 3. Gran Auditoría de Bugs, Errores Potenciales y Edge Cases

Esta sección desglosa las posibles vulnerabilidades, desincronizaciones o errores lógicos identificados en el código para su revisión y blindaje durante la limpieza final.

### 3.1. Riesgos en Minijuegos y Casino

| Subsistema | Riesgo / Bug Potencial | Archivo / Componente | Severidad | Acción Recomendada |
| :--- | :--- | :--- | :--- | :--- |
| **Tragamonedas** | Destrucción del bloque mientras gira el tambor (`spinning = true`) | `SlotMachineBlock.java` / `SlotMachineBlockEntity.java` | **Media** | En `onRemove`, si la máquina está activa, expulsar inmediatamente las fichas apostadas para evitar pérdida de ítems del jugador. |
| **Tragamonedas** | Desconexión del jugador con tirada en marcha | `SlotMachineBlockEntity.java` | **Baja** | La máquina termina la tirada de forma autónoma y acumula el premio en su inventario interno o lo dropea al suelo. |
| **Blackjack PvP** | Desconexión de un jugador durante su turno de apostar/pedir carta | `BlackjackServerHandler.java` | **Alta** | Implementar temporizador de desconexión (timeout de 30s) que declare victoria por abandono al rival o reembolse las fichas. |
| **Mesa de Billar** | Desconexión o muerte mientras se apunta con el taco | `PoolTableBlockEntity.java` | **Media** | Liberar el bloqueo de turno del jugador si el UUID del jugador ya no está conectado al servidor. |
| **Diana de Dardos** | Proyectiles huérfanos si el dardo cae en chunk descargado | `DartEntity.java` | **Baja** | Failsafe de `tickCount > 600` para autodespawn si no impacta en ninguna superficie. |
| **Paquetes de Casino** | Falta de verificación de distancia máxima entre jugador y mesa en handlers | `BlackjackServerHandler.java`, `RouletteServerHandler.java` | **Media** | Validar `player.distanceToSqr(blockPos) <= 64.0` antes de procesar apuestas recibidas por red. |

### 3.2. Riesgos en el Hipódromo y Carreras de Uma Musume

| Subsistema | Riesgo / Bug Potencial | Archivo / Componente | Severidad | Acción Recomendada |
| :--- | :--- | :--- | :--- | :--- |
| **Navegación de Umas** | Descarga de chunks del circuito durante una carrera activa | `TeyvatRacetrackManager.java` | **Alta** | Mantener un ticket de chunk forzado (`FORCED` chunk ticket) en los chunks del circuito mientras `state == RACING`. |
| **Apuestas de Hipódromo** | Servidor reiniciado con apuestas activas en `BETTING_OPEN` | `TeyvatRacetrackManager.java` | **Media** | Guardar apuestas en `SavedData` o devolver las apuestas a los jugadores conectados al iniciar el servidor. |
| **Multi-Hipódromos** | Los nuevos hipódromos naturales del Overworld tienen taquilla pero no gestor local | `UmaBookmakerBlock.java` | **Media** | Permitir que la taquilla vincule o ejecute la carrera del hipódromo custom/local más cercano. |

### 3.3. Riesgos en Guardado y Persistencia (`SavedData`)

| Subsistema | Riesgo / Bug Potencial | Archivo / Componente | Severidad | Acción Recomendada |
| :--- | :--- | :--- | :--- | :--- |
| **Estructuras de Series** | Llamadas a `recordStructure` sin verificar si `level.getServer()` es nulo | `SeriesStructureWorldGen.java` | **Baja** | Ya cuenta con guardia `if (level.getServer() != null)`, pero verificar consistencia en llamadas auxiliares. |
| **Persistencia de Teyvat** | `setRacetrackBuilt(true)` llamado dentro de bloques try-catch que silencian excepciones | `TeyvatRacetrackBuilder.java` | **Baja** | Cambiar `catch (Exception ignored)` por log de nivel WARN o DEBUG para no ocultar fallas de I/O. |

### 3.4. Catálogo Completo de Bloques de Excepción Silenciosa (`catch (Exception ignored)`)

Los siguientes bloques suprimen excepciones sin registrar errores en el log, lo que puede ocultar problemas en tiempo de ejecución:

| Archivo | Línea | Código Silencioso |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/AwakeningPlayerRegistryData.java` | 43 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 624 | `} catch (NumberFormatException ignored) {}` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 1215 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 1221 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 1227 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 1766 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3434 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 4019 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/GachaManager.java` | 1191 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/GachaWishScreen.java` | 1980 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/RpgAffixManager.java` | 117 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/RpgAffixManager.java` | 127 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 914 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 943 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 990 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1606 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 3212 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8012 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8133 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8160 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8173 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8351 | `} catch (Exception ignored) {` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 750 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/SystemHudOverlay.java` | 761 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/SystemItems.java` | 3152 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 101 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 172 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 205 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 248 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/WelkinMoonScreen.java` | 436 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/WelkinMoonScreen.java` | 469 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/block/entity/CustomPlayerPlushieBlockEntity.java` | 124 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/block/entity/LockedTreasureChestBlockEntity.java` | 188 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/block/entity/LockedTreasureChestBlockEntity.java` | 201 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/client/gui/GuiConfig.java` | 169 | `} catch (Exception ignored) {` |
| `com/soybe/awakeningsystem/client/renderer/CustomPlayerPlushieRenderer.java` | 78 | `} catch (Exception ignored) {` |
| `com/soybe/awakeningsystem/client/renderer/HeadPlushieLayer.java` | 106 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/client/renderer/HeadPlushieLayer.java` | 149 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/compat/XaeroGuildMapIntegration.java` | 60 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/compat/XaeroGuildMapIntegration.java` | 198 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/compat/XaeroGuildMapIntegration.java` | 213 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/compat/XaeroGuildMapIntegration.java` | 242 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/config/CustomLootConfigManager.java` | 397 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/element/ElementalReactionManager.java` | 448 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/entity/AzhdahaBossEntity.java` | 373 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/entity/ChildeBossEntity.java` | 409 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/entity/GoldenWolflordBossEntity.java` | 233 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/entity/RaidenBossEntity.java` | 411 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/entity/client/UmaRenderer.java` | 41 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/furnace/CustomTierFurnaceMenu.java` | 244 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/furnace/CustomTierFurnaceMenu.java` | 263 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/furnace/CustomTierFurnaceMenu.java` | 282 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/guild/GuildEventHandler.java` | 246 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/guild/GuildMember.java` | 52 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/util/AttributeLimitFixer.java` | 65 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/util/AttributeLimitFixer.java` | 86 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/world/SeriesStructureBuilder.java` | 187 | `} catch (Throwable ignored) {}` |
| `com/soybe/awakeningsystem/world/SeriesStructureWorldGen.java` | 229 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 65 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 79 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatAbyssPortalBuilder.java` | 90 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatCasinoBuilder.java` | 62 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatCasinoBuilder.java` | 72 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatCasinoBuilder.java` | 83 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatCasinoBuilder.java` | 94 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatCasinoBuilder.java` | 101 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 46 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 199 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 209 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 219 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatRacetrackBuilder.java` | 230 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatStructureManager.java` | 71 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 953 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 970 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1004 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1122 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1133 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1241 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1252 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1285 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 1297 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/world/WorldBossScalingManager.java` | 117 | `} catch (Exception ignored) {` |
| `com/soybe/awakeningsystem/worldgen/DragonNestFeature.java` | 144 | `} catch (Exception ignored) {` |
| `com/soybe/awakeningsystem/worldgen/TeyvatHilichurlCampFeature.java` | 73 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatHilichurlCampFeature.java` | 118 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatRegionalStructureFeature.java` | 49 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatRegionalStructureFeature.java` | 113 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatShrineOfDepthsFeature.java` | 48 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatShrineOfDepthsFeature.java` | 60 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatStatueFeature.java` | 45 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatStatueFeature.java` | 55 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatWaypointFeature.java` | 44 | `} catch (Exception ignored) {}` |
| `com/soybe/awakeningsystem/worldgen/TeyvatWaypointFeature.java` | 54 | `} catch (Exception ignored) {}` |

---

## 4. Inventario de Mensajes Hardcodeados en Java (`Component.literal`)

Catálogo de los 648 mensajes directos enviados al jugador vía chat o actionbar que deben internacionalizarse en `lang`:

### Módulo: Casino y Minijuegos de Azar (34 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/block/CasinoBarStoolBlock.java` | 107 | `§cEste taburete de bar ya está ocupado.` |
| `com/soybe/awakeningsystem/block/CasinoChairBlock.java` | 107 | `§cEste asiento de casino ya está ocupado.` |
| `com/soybe/awakeningsystem/block/CasinoTrophyBlock.java` | 155 | `§6§l✦==============================✦` |
| `com/soybe/awakeningsystem/block/CasinoTrophyBlock.java` | 160 | `§6§l✦==============================✦` |
| `com/soybe/awakeningsystem/block/SlotMachineBlock.java` | 115 | `§5🎰 [Casino Clandestino] §cEsta máquina del bajo mundo solo acepta §5§lFichas Negras Clandestinas§c.` |
| `com/soybe/awakeningsystem/block/SlotMachineBlock.java` | 125 | `§e🎰 [Casino Legal] §cEsta máquina pública rechaza fichas del mercado negro. ¡Llévala al Casino Clandestino!` |
| `com/soybe/awakeningsystem/block/SlotMachineBlock.java` | 135 | `§e🎰 ¡La máquina tragamonedas ya está en marcha! Espera a que termine la tirada.` |
| `com/soybe/awakeningsystem/block/SlotMachineBlock.java` | 166 | `§5🎰 [Casino Clandestino] §cInserta una Ficha Negra Clandestina en la ranura para apostar en el bajo mundo.` |
| `com/soybe/awakeningsystem/block/SlotMachineBlock.java` | 168 | `§c🎰 Inserta una Ficha de Casino (Bronce, Plata, Oro o Platino) en la ranura para jugar.` |
| `com/soybe/awakeningsystem/block/SlotMachineBlock.java` | 195 | `§e🎰 ¡La máquina tragamonedas ya está en marcha!` |
| `com/soybe/awakeningsystem/block/entity/SlotMachineBlockEntity.java` | 547 | `§6🏆 ¡Has recibido el §eTrofeo Gran Apostador §6por tu Jackpot 777!` |
| `com/soybe/awakeningsystem/block/entity/SlotMachineBlockEntity.java` | 573 | `§7🎰 No hubo suerte esta vez... ¡Prueba otra tirada!` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 83 | `§c🎰 La mesa de Blackjack legal rechaza fichas clandestinas. ¡Apuesta en la mesa clandestina!` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 87 | `§c🎰 No tienes fichas suficientes de esa categoría.` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 93 | `§c🎰 Saldo insuficiente de Mora para esa apuesta.` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 126 | `§c🎰 Saldo de Mora insuficiente para doblar la apuesta.` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 190 | `§c🎰 Ya hay un duelo en curso o en espera en esta mesa.` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 208 | `§c🎰 No hay ningún reto activo esperando en esta mesa.` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 212 | `§c🎰 No puedes aceptar tu propio reto.` |
| `com/soybe/awakeningsystem/casino/BlackjackServerHandler.java` | 250 | `§e🎰 Duelo cancelado. Apuesta devuelta.` |
| `com/soybe/awakeningsystem/casino/CasinoCashierServerHandler.java` | 34 | `§c[Casa de Cambio] Esta categoría de ficha no se negocia aquí. Las Fichas Clandestinas solo se canjean en el Cambiador Clandestino o Mercado Negro.` |
| `com/soybe/awakeningsystem/casino/CasinoCashierServerHandler.java` | 151 | `§5[Bajo Mundo] §cNo tienes Fichas Clandestinas en el inventario para canjear.` |
| `com/soybe/awakeningsystem/casino/RouletteServerHandler.java` | 59 | `§c🎰 Debes colocar al menos una apuesta en el tapete.` |
| `com/soybe/awakeningsystem/casino/RouletteServerHandler.java` | 73 | `§c🎰 La mesa de Ruleta legal rechaza fichas clandestinas. ¡Apuesta en la ruleta del bajo mundo!` |
| `com/soybe/awakeningsystem/casino/RouletteServerHandler.java` | 84 | `§c🎰 Saldo de Mora insuficiente para cubrir las apuestas.` |
| `com/soybe/awakeningsystem/casino/SlotMachineServerHandler.java` | 40 | `§cSaldo insuficiente de Mora para realizar esta tirada.` |
| `com/soybe/awakeningsystem/casino/darts/DartboardBlock.java` | 93 | `§6🎯 ¡Has tomado 3 Dardos de competición! §eAléjate a 3–8 bloques y lánzalos apuntando a la diana.` |
| `com/soybe/awakeningsystem/casino/darts/DartboardServerHandler.java` | 115 | `§6§l✦ SESIÓN PRÁCTICA DE DARDOS INICIADA ✦\n§eTienes 3 dardos en la mano. Aléjate unos 3 a 8 bloques y lánzalos hacia la Diana.` |
| `com/soybe/awakeningsystem/casino/darts/DartboardServerHandler.java` | 145 | `§cNo hay ningún reto PvP activo esperando en esta diana.` |
| `com/soybe/awakeningsystem/casino/darts/DartboardServerHandler.java` | 149 | `§cNo puedes unirte a tu propio reto. Usa Cancelar si deseas retirar tu apuesta.` |
| `com/soybe/awakeningsystem/casino/darts/DartboardServerHandler.java` | 282 | `§6🏆 ¡Has recibido el §eTrofeo Maestro de Sisal §6por tu récord perfecto de 180 puntos!` |
| `com/soybe/awakeningsystem/casino/darts/DartEntity.java` | 251 | `§6§l✦ PDC ANNOUNCER: §c§l¡¡DOBLE BULLSEYE (50 PTS)!! §6(+5,000 🪙) ✦` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 633 | `§6🏆 ¡Has recibido el §eTrofeo Leyenda del Tapete §6por tu victoria clandestina!` |
| `com/soybe/awakeningsystem/casino/pool/PoolTableBlockEntity.java` | 646 | `§6🏆 ¡Has recibido el §eTrofeo Leyenda del Tapete §6por tu gran victoria!` |

### Módulo: Comandos de Administrador (160 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/SystemCommands.java` | 600 | `Especifica un jugador: /resetprogress <jugador>` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 812 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 826 | `Uso: /coins <cantidad> <jugador>` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 840 | `Debes especificar un jugador objetivo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 857 | `Uso: /carats <cantidad> [jugador]` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 871 | `Debes especificar un jugador objetivo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 885 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 896 | `§a§l[SISTEMA]§r ¡Desbloqueo total completado! Nivel 490, 47 habilidades desbloqueadas a Nivel VI (activas, pasivas y manhwas), títulos míticos, afinidades al máximo, 16 llaves de mazmorra y 1.000.000 de monedas.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 914 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 918 | `Atributo invalido o no tienes puntos disponibles.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 947 | `=== CLASIFICACION DEL SISTEMA ===` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 969 | `§c§l[MODO TRANCE]§r Activado por 10 segundos. (Daño +3.0 y ataques de barrido activados).` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 995 | `ID de constelación inválida o nivel menor a 25. Constelaciones disponibles: abyssal_black_dragon, demon_like_judge_of_fire, secretive_plotter, prisoner_of_the_golden_headband, master_of_the_underworld` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1023 | `§6§l[ESCENARIO]§r ¡Escenario completado! Abre el menú (Misiones) para pulsar [★ RECLAMAR].` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1084 | `§6§l[DESPERTAR]§r ¡Misión del Despertar del Sistema completada!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1091 | `§6§l[PATROCINIO]§r Abriendo pantalla de selección de Constelación.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1098 | `§6=== TÍTULOS DE RANGO ÚNICO (1 POR SERVIDOR) ===` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1104 | `§d=== HABILIDADES DE RANGO ÚNICO (1 POR SERVIDOR) ===` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1152 | `§aAbriendo la Guía del Cazador...` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1161 | `§aAbriendo el Bestiario de Cazadores...` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1173 | `Especifica un jugador objetivo: /system constelacion ascender <jugador> [nombre]` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1189 | `Especifica un jugador objetivo: /system constelacion degradar <jugador>` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1204 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1209 | `§cNo eres una Constelación. Debes ascender primero mediante eventos míticos o hazañas trascendentes.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1221 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1225 | `§cNo te encuentras en proyección astral.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1234 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1266 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1275 | `§cEstructura de Teyvat no reconocida. Opciones: golden_house (Childe), tenshukaku (Raiden Shogun), wolflord (Rey Acechador), azhdaha, anemo_hypostasis, electro_hypostasis, geo_hypostasis, cryo_hypostasis, pyro_hypostasis, hydro_hypostasis, cryo_regisvine, anemo_shrine, geo_shrine, electro_shrine, dendro_shrine, statue, waypoint, camp, windrise, fix_windrise, fix_arenas, clean_arenas, jade_chamber, beidou_ship, primal_temple, chasm_mine, karst, updraft, abyss.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1306 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1312 | `§cLa dimensión de Teyvat no está cargada. Viaja primero con /teyvat.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1483 | `§7Usa §b/teyvat list§7 para ver el catálogo interactivo o viaja directo a los jefes:` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1484 | `§6✦ [Tartaglia / Childe] §7-> §e/teyvat golden_house` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1485 | `§d✦ [Raiden Shogun]      §7-> §e/teyvat tenshukaku` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1486 | `§6✦ [Rey Acechador]      §7-> §e/teyvat wolflord` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1487 | `§e✦ [Dragón Azhdaha]     §7-> §e/teyvat azhdaha` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1493 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1500 | `§b§l================ CATÁLOGO DE ESTRUCTURAS DE TEYVAT ================` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1503 | `§6§l👑 JEFES SEMANALES Y PALACIOS AUTÉNTICOS:` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1510 | `§b§l💎 HIPÓSTASIS ELEMENTALES Y REGISVID:` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1520 | `§a§l🌳 MONUMENTOS Y SEDES DE NACIONES:` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1528 | `§e§l⛩ EXPLORACIÓN Y RECURSOS:` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1533 | `§7Usa §b/teyvat <nombre>§7 para viajar directamente a cualquier estructura o jefe.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1534 | `§b§l==================================================================` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1560 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1595 | `Solo los jugadores pueden teletransportarse a la Espiral del Abismo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1602 | `§b✦ [Espiral del Abismo] Abriendo interfaz...` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1605 | `Solo los jugadores pueden abrir la interfaz de la Espiral del Abismo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1614 | `Solo los jugadores pueden avanzar en la Espiral del Abismo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1623 | `Solo los jugadores pueden salir de la Espiral del Abismo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1644 | `§b§l✦ [ESPIRAL DEL ABISMO] §aIsla del Arrecife de Musk y portal reconstruidos con éxito.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1647 | `§cLa dimensión de Teyvat no está cargada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1653 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1716 | `§cNo se encontraron coordenadas en el rango actual.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1722 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1728 | `§e§l[SISTEMA]§r Aún no se han generado estructuras en este mundo. Explora nuevas regiones o usa §b/structure <nombre>§r.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1744 | `§cComando solo disponible en el servidor.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1761 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1768 | `§cNo tienes soldados de sombra cercanos en el campo para guardar.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1775 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1782 | `§cNo tienes soldados de sombra en la reserva de tu dominio.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1789 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1802 | `§8================ §9§lDOMINIO DE SOMBRAS §8================` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1806 | `§7Controles rápidos: §e[V] §7para Extraer/Sacar, §e[Shift + V] §7para Guardar en dominio.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1807 | `§8=======================================================` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1813 | `Este comando debe ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1817 | `§8[§9Ejército de Sombras§8] §eTodas tus sombras activas han sido disipadas.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1823 | `Este comando solo puede ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1827 | `§e§l[Star Stream]§r Dokkaebi invocado con éxito junto a ti.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1833 | `Este comando solo puede ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1840 | `§cNo hay ningún Dokkaebi cerca transmitiendo. Espera a que aparezca o usa /dokkaebi spawn.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1845 | `§e§l[Star Stream]§r El Dokkaebi aún no te ha asignado ningún Sub-Escenario. Hazle clic derecho para recibir uno.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1856 | `Este comando solo puede ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1863 | `§cNo hay ningún Dokkaebi cercano para despedir.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1869 | `§e§l[Star Stream]§r El Dokkaebi ha cerrado la transmisión.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1875 | `Este comando solo puede ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1884 | `§e§l[Star Stream]§r Escenario Forzado del Dokkaebi iniciado con éxito.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1890 | `Este comando solo puede ser ejecutado por un jugador.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1899 | `§c§l[Jefe Invocado]§r ¡Igris, Comandante Carmesí ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1907 | `§b§l[Jefe Invocado]§r ¡Baran, Monarca de las Llamas Blancas ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1915 | `§4§l[Jefe Invocado]§r ¡Gran Patriarca de la Secta de Sangre ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1923 | `§2§l[Jefe Invocado]§r ¡Treant Ancestral de Yggdrasil ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1931 | `§b§l[Jefe Invocado]§r ¡Nobile / Tartaglia (Childe), Undécimo Heraldo de Fatui ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1939 | `§d§l[Jefe Invocado]§r ¡Shogun Raiden / Baal, Diosa de la Eternidad ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1947 | `§6§l[Jefe Invocado]§r ¡Rey Acechador Dorado, Soberano del Can Menor de la Grieta ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1955 | `§6§l[Jefe Invocado]§r ¡Azhdaha, Rey Dragón Primigenio Sellado ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1963 | `§a§l[Jefe Invocado]§r ¡Hipóstasis Anemo (Beth) ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1971 | `§d§l[Jefe Invocado]§r ¡Hipóstasis Electro (Aleph) ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1979 | `§e§l[Jefe Invocado]§r ¡Hipóstasis Geo (Gimel) ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1987 | `§b§l[Jefe Invocado]§r ¡Hipóstasis Cryo (Daleth) ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 1995 | `§c§l[Jefe Invocado]§r ¡Hipóstasis Pyro (Ayin) ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2003 | `§9§l[Jefe Invocado]§r ¡Hipóstasis Hydro (He) ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2011 | `§b§l[Jefe Invocado]§r ¡Regisvid Criogénica ha aparecido!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2015 | `§cJefe desconocido. Usa: /boss <igris \| baran \| patriarch \| treant \| childe \| raiden \| wolflord \| azhdaha \| anemo_hypostasis \| electro_hypostasis \| geo_hypostasis \| cryo_hypostasis \| pyro_hypostasis \| hydro_hypostasis \| cryo_regisvine>` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2026 | `Solo los jugadores pueden ingresar a la Torre.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2035 | `Solo los jugadores pueden cambiar de piso en la Torre.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2044 | `Solo los jugadores pueden cultivar Qi.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2054 | `Solo los jugadores pueden viajar al mundo Isekai.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2063 | `§a✔ Actualización a Sistema 4.0 completada instantáneamente sin esperas.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2067 | `§a✔ Actualización a Sistema 3.0 completada instantáneamente sin esperas.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2074 | `§cNo hay ninguna actualización en curso. Puedes forzar la versión con: /system version <1\|2\|3\|4>` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2078 | `Solo los jugadores pueden completar la actualización del Sistema.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2131 | `§e[SISTEMA] §aVersión cambiada instantáneamente a: §b§lSistema 1.0 (Awakening Base)` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2141 | `§e[SISTEMA] §aVersión cambiada instantáneamente a: §d§lSistema 2.0 (Isekai & Constelaciones)` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2151 | `§e[SISTEMA] §aVersión cambiada instantáneamente a: §a§lSistema 3.0 (Murim & Cultivo Xianxia)` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2161 | `§e[SISTEMA] §aVersión cambiada instantáneamente a: §6§lSistema 4.0 (Teyvat & Impacto Celestial)` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2164 | `§cVersión no válida. Usa: /system version <1 \| 2 \| 3 \| 4>` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2209 | `Solo los jugadores pueden viajar al Árbol del Mundo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2219 | `Solo los jugadores pueden ascender al Reino Murim.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2226 | `§b[Teyvat] Viajando a la Plaza Ceremonial de Teyvat...` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2229 | `Solo los jugadores pueden viajar a Teyvat.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2235 | `Solo los jugadores pueden viajar por Teyvat.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2286 | `Solo los jugadores pueden viajar al Gran Casino de Teyvat.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2292 | `§cLa dimensión de Teyvat no está cargada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2297 | `§6✦ [Gran Casino de Teyvat] §a¡Te has teletransportado a la explanada del Gran Casino Real de Teyvat!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2305 | `§cLa dimensión de Teyvat no está cargada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2309 | `§6✦ [Gran Casino de Teyvat] §a¡Estructura monumental del Casino de Monte Carlo reconstruida con éxito!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2329 | `§cError al exportar el archivo de configuración del casino.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2351 | `§6✦ [Casino] §a¡Mobiliario oficial reglamentario (Tragamonedas, Blackjack 2x2, Ruleta 2x3, Pool 3x4, Sillas y Trofeos) desplegado con éxito!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2357 | `Solo los jugadores pueden reclamar el Jackpot del Casino.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2389 | `§a§l✔ [Casino Awakening] §e¡Has reclamado con éxito todas las recompensas completas del Gran Jackpot 777 Platino!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2401 | `Solo los jugadores pueden viajar al Hipódromo de Teyvat.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2407 | `§cLa dimensión de Teyvat no está cargada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2416 | `§6🏇 [Hipódromo de Teyvat] §a¡Te has teletransportado al Pabellón Oficial de Apuestas de Tracen!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2424 | `§cLa dimensión de Teyvat no está cargada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2432 | `§6🏇 [Hipódromo de Teyvat] §a¡Pista reconstruida por completo sin huecos, con despeje de árboles y cabinas de salida listas!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2440 | `§cLa dimensión de Teyvat no está cargada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2446 | `§6🏇 [Hipódromo de Teyvat] §e¡La cuenta regresiva de la carrera ha sido forzada e iniciada!` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2449 | `§cNo se pudo acceder al gestor del hipódromo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2523 | `§b§l✦ [TEYVAT] §aEstructuras procedurales eliminadas y terreno restaurado a su estado natural.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2526 | `§cLa dimensión de Teyvat no está cargada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2531 | `§e[Teyvat] La inyección de regiones externas está desactivada. La dimensión genera su terreno natural de forma limpia.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2538 | `§6[Gacha] Abriendo interfaz del Sistema de Deseos de Teyvat...` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2541 | `Solo los jugadores pueden abrir el Sistema de Deseos.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2562 | `Solo los jugadores pueden entrar a la Pagoda de las Nueve Tribulaciones.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2571 | `Solo los jugadores pueden cambiar de etapa en la Pagoda.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2580 | `Solo los jugadores pueden salir de la Pagoda.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2587 | `§6========================================` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2592 | `§8----------------------------------------` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2599 | `§7Abre el menú del Sistema (§eH§7) y haz clic en §6[FÁBULAS]§7 para la interfaz completa.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2600 | `§6========================================` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2621 | `Solo los jugadores pueden abrir el subespacio dimensional.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2630 | `§cNo tienes ninguna recompensa de mazmorra pendiente de asegurar.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2641 | `§cNo tienes ninguna recompensa de mazmorra pendiente de apostar.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2685 | `§a✔ Regresaste exitosamente de tu Subespacio Dimensional.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2691 | `§a✔ Regresaste exitosamente de la Espiral del Abismo.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2734 | `§7✦ Bendición Lunar restablecida a estándar (90 protos/día).` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2751 | `§d✦ Misión de Ascensión de Columbina completada.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2763 | `§e✦ Progreso de Misión de Columbina reiniciado a 0.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2784 | `§d✦ Santuario de las Tres Diosas de la Luna (Aria, Sonnet, Canon) manifestado con éxito.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2790 | `§cComando solo disponible para jugadores.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2808 | `§cNo se encontró ningún Núcleo de Hipódromo Custom en un radio de 32 bloques.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2814 | `§cComando solo disponible para jugadores.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2836 | `§cNo se encontró ningún Núcleo de Hipódromo Custom en un radio de 32 bloques.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2875 | `Solo los jugadores pueden abrir la interfaz del Mercado Negro.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2881 | `Solo los jugadores pueden buscar el Mercado Negro.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2905 | `§5☠ [Mercado Negro] §eAún no se ha registrado ninguna guarida en este mundo. Se genera en el Overworld subterráneo (Y=10 a 20) bajo bosques, pantanos o junglas, o ejecuta §d/black_market tp §epara encontrar o invocar una de inmediato.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2912 | `Solo los jugadores pueden viajar al Mercado Negro.` |
| `com/soybe/awakeningsystem/SystemCommands.java` | 2951 | `Solo los jugadores pueden construir una guarida de Mercado Negro.` |
| `com/soybe/awakeningsystem/item/CommandSpellItem.java` | 52 | `§c§l✦ Sellos de Comando agotados. (Se recargan automáticamente al iniciar un nuevo día)` |

### Módulo: Combate, Habilidades y Estadísticas (12 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 189 | `§6§l[GREMIO DE PESCADORES] §f¡Has obtenido el arma legendaria §b✦ La Captura ✦§f!` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 191 | `§c[Gremio de Pescadores] No tienes suficientes peces para forjar La Captura. (Requiere 6 Pez Raimei, 20 Cristalino, 20 Espina Venenosa).` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 207 | `§6§l[GREMIO DE PESCADORES] §f¡Has obtenido la espada legendaria §b✦ Vado del Río Ceniciento ✦§f!` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 209 | `§c[Gremio de Pescadores] No tienes suficientes peces para el Vado del Río Ceniciento. (Requiere 4 Pez Raimei, 12 Cristalino, 12 Medaka Aizen).` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 223 | `§6§l[GREMIO DE PESCADORES] §f¡Has recibido el accesorio §a✦ Estabilizador de Pesca ✦§f!` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 225 | `§c[Gremio de Pescadores] Faltan peces para el Estabilizador. (Requiere 3 Medaka y 3 Medaka Flor Dulce).` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 247 | `§6§l[GREMIO DE PESCADORES] §f¡Has obtenido la §bCaña del Maestro Pescador§f con Suerte y Señuelo III!` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 249 | `§c[Gremio de Pescadores] Faltan peces para la Caña del Maestro. (Requiere 5 Medaka, 5 Aizen, 5 Esmaltado).` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 295 | `§c[Pescadería] No tienes ese tipo de pez en el inventario.` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 320 | `§a§l[GREMIO DE PESCADORES] §f¡Has cocinado un delicioso §6Pescado Asado de Tigre§f!` |
| `com/soybe/awakeningsystem/block/FishingStationBlock.java` | 322 | `§c[Gremio de Pescadores] Necesitas al menos 1 pez (Medaka, Bacalao o Salmón) para cocinar.` |
| `com/soybe/awakeningsystem/block/StatueOfTheSevenBlock.java` | 147 | `§6✦ [ESTATUA DE LOS SIETE] §eBendición de los Arcontes concedida. §fSalud y Energía restauradas al 100%.` |

### Módulo: Compañeros y NPCs (8 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 149 | `§cCompañero no encontrado.` |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 260 | `§cRecursos insuficientes o nivel máximo alcanzado.` |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 293 | `§cRequiere §d1x Polvo Estelar §co §b20 Protogemas §cpara la Ascensión.` |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 313 | `§cTalento al límite para la fase actual. ¡Asciende de rango primero!` |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 330 | `§cTalento al límite para la fase actual. ¡Asciende de rango primero!` |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 347 | `§cTalento al límite para la fase actual. ¡Asciende de rango primero!` |
| `com/soybe/awakeningsystem/entity/CompanionAscensionManager.java` | 386 | `§cRequiere §62x Brillo Estelar §co §b160 Protogemas §cpara resonar con la Constelación.` |
| `com/soybe/awakeningsystem/entity/CompanionEntity.java` | 341 | `§d§l✦ [COLUMBINA] §f«¡Que la luz de las Tres Lunas te proteja!»` |

### Módulo: Hipódromo y Carreras de Uma Musume (11 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/block/RacetrackBleachersBlock.java` | 155 | `§7Esta grada ya está ocupada.` |
| `com/soybe/awakeningsystem/block/RacetrackBleachersBlock.java` | 165 | `§a✦ Viendo la carrera desde la tribuna. Presiona Shift para levantarte.` |
| `com/soybe/awakeningsystem/item/uma/BitterJuiceItem.java` | 67 | `§a🧪 [JUGO AMARGO] ¡Fuerza II y Doble Experiencia activados por 5 minutos! Cuidado con la resaca al expirar.` |
| `com/soybe/awakeningsystem/item/uma/DarkStaminaElixirItem.java` | 77 | `§5☠ [ELIXIR DE ESTAMINA OSCURA] §d¡Potencial ilícito desatado! Velocidad III y Salto II durante 3 minutos. Cuidado con el rebote de fatiga.` |
| `com/soybe/awakeningsystem/item/uma/PleasingParfaitItem.java` | 62 | `§d🍨 [PARFAIT DE LA FELICIDAD] ¡Tu cuerpo ha sido purificado y tu vitalidad rebosa al máximo!` |
| `com/soybe/awakeningsystem/item/uma/TrainingWhistleItem.java` | 72 | `§7📣 [Silbato] No se encontraron aldeanos en un radio de 10 bloques.` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 217 | `§c🚫 Las apuestas están cerradas mientras la carrera esté en marcha o finalizando.` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 222 | `§cYa tienes una apuesta registrada para este derby. Espera al siguiente para apostar de nuevo.` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 227 | `§cEsa corredora no participa en esta tanda.` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 232 | `§cDebes colocar un ítem o botín para apostar.` |
| `com/soybe/awakeningsystem/uma/TeyvatRacetrackManager.java` | 542 | `§6🏆 ¡Has recibido la §eCopa Derby de Teyvat §6por predecir a la campeona!` |

### Módulo: Núcleo del Sistema (SystemCore & Eventos) (349 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 681 | `§c🚫 Las apuestas están cerradas mientras la carrera esté en marcha o finalizando.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 686 | `§cYa tienes una apuesta registrada para este derby.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 705 | `§cNo tienes un ítem válido para apostar.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 709 | `§cLa carrera ya está iniciada o en curso.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 713 | `§e¡Señal de largada enviada a la pista de carreras!` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 740 | `§c🥕 No tienes suficientes Carats (Requiere 150 🥕).` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 757 | `§c🥕 No tienes suficientes Carats (Requiere 1,500 🥕).` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 785 | `§6★ ¡Este atributo ya ha alcanzado su límite de potencial máximo (1,200)!` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 797 | `§cNo tienes suficientes recursos (Requiere 50 🥕 y 200 🪙).` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 808 | `§6✨ ¡TALENT BLOOM! Tu corredora ha subido de estrellas y aumentado todos sus atributos.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 810 | `§cNo tienes suficientes fragmentos (Shards) para ascender a esta Uma.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 844 | `§cRequiere 5 Estatuas de la Diosa (🗽) en tu inventario.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 855 | `§a✔ Corredora principal asignada.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 864 | `§a¡Inscrito en la carrera con tu Uma activa!` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 866 | `§cNo se pudo inscribir (Carrera llena o ya estás inscrito).` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 869 | `§cNo hay ningún Núcleo de Hipódromo vinculado cerca.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 880 | `§cNo hay ningún Núcleo de Hipódromo vinculado cerca.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 890 | `§cNo hay ningún Núcleo de Hipódromo vinculado cerca.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 897 | `§6🚩 ¡Comienza la cuenta regresiva del derby custom!` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 899 | `§cNo se puede iniciar (Se requieren al menos 2 waypoints y participantes).` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 902 | `§cNo hay ningún Núcleo de Hipódromo vinculado cerca.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 940 | `§cNo tienes suficientes monedas para realizar este canje.` |
| `com/soybe/awakeningsystem/AwakeningSystemMod.java` | 1206 | `§cEl jugador no está en línea.` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2568 | `§6[Sistema] §c¡Fase 2: El Laberinto de Sombras!` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2569 | `§eTropas enemigas acechan dispersas en los pasillos y alas del laberinto. ¡Rastrea y elimínalas a todas!` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2657 | `§6[Sistema] §c¡Fase 3: Salón de Juicio y Criptas del Laberinto!` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2658 | `§eEl Comandante y su guardia de élite custodian el corazón y las alas del laberinto...` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2729 | `§6[Sistema] §c¡Fase 4: La Guardia Pretoriana del Trono!` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 2730 | `§e¡Destruye a los últimos defensores del santuario para abrir las puertas del Jefe!` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3287 | `§a§l✔ [SISTEMA] §f¡Has asegurado tu recompensa con éxito! Tu botín ha sido transferido a tu inventario.` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3288 | `§a§l✔ Recompensa Asegurada` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3321 | `§6§l🎲 [SISTEMA] §e¡Has decidido apostar tu botín al Doble o Mejor! La Ruleta del Destino gira con furia...` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3322 | `§6§l🎲 ¡APUESTA ACTIVADA! Girando...` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3579 | `§8§m` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3580 | `§6§l           ✦ RECOMPENSA DE MAZMORRA ✦` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3582 | `§7  ¿Deseas asegurar tu botín o apostarlo con el Sistema para duplicarlo?` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3596 | `§7  (Clic en el chat, Clic en el Núcleo para asegurar, o Shift+Clic para apostar)` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3597 | `§8§m` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3599 | `§e¡Recompensa lista! §fReclama o apuesta en el Núcleo` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3646 | `§6§l🎉 ¡¡APUESTA GANADA!! §e¡El Sistema ha duplicado y bendecido tu botín al máximo!` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3648 | `§6§l🎲 ¡APUESTA GANADA! §eBotín Supremo Recibido` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3672 | `§c§l💥 ¡APUESTA PERDIDA! §7La vorágine del Sistema devoró tu botín mayor.` |
| `com/soybe/awakeningsystem/DungeonManager.java` | 3674 | `§c§l💥 ¡APUESTA PERDIDA! §7Recibes consolación` |
| `com/soybe/awakeningsystem/GachaManager.java` | 405 | `§c🥕 [Academia Tracen] ¡No tienes suficientes Tickets, Deseos, Carats ni Protogemas para invocar!` |
| `com/soybe/awakeningsystem/GachaManager.java` | 489 | `§c[Gacha] ¡Has completado el límite máximo de 20 tiradas del Deseo de Principiantes!` |
| `com/soybe/awakeningsystem/GachaManager.java` | 913 | `§6[Senda Divina] §fHas fijado el arma objetivo para la Encarnación Divina.` |
| `com/soybe/awakeningsystem/GachaManager.java` | 1325 | `§cNo tienes Cristales Génesis para convertir.` |
| `com/soybe/awakeningsystem/GachaManager.java` | 1384 | `§cNo tienes Cristales Génesis para convertir.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 441 | `§d§l[Paimon] §f¡Bienvenido a Teyvat, Viajero! ¡Paimon te acompañará en todo tu viaje!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 878 | `§6⏰ [RELOJ DE ALARMA] ¡Ha sonado la alarma! Evitaste la muerte y tu inventario está a salvo.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1017 | `§d§l✦ [ALMA NACIENTE INTERCEPTÓ LA MUERTE] §f¡Tu Alma Naciente absorbió el golpe fatal y te devolvió a la vida!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1683 | `§6§l✦ [CUERPO DE DIAMANTE INMORTAL] §e¡Tu poder de Prestigio ha evitado tu muerte! (Recarga: 10 min)` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1708 | `§f§l✦ [LA CUARTA PARED] §b¡La barrera conceptual de Kim Dokja ha prevenido tu muerte!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 1751 | `§6§l[BARRERA ÁUREA DE MORA] §e¡El Dios del Comercio absorbió el golpe letal a cambio de tus riquezas!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2012 | `§0§l[§4§l⚡ DESTELLO NEGRO ⚡§0§l] §c§l¡IMPACTO ESPACIAL x2.5!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2430 | `§6§l✦ [ESPIRAL DEL ABISMO] §f¡Has roto el Sello Celestial! El vórtice hacia el Abismo se ha abierto.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2887 | `§7[Metro] Has soltado el pasamanos.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2920 | `§c[Jugo Amargo] ¡El efecto estimulante ha expirado! Sufres agotamiento extremo y debilidad.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 2942 | `§a⚡ [PESAS DE TOBILLO] ¡Te has quitado el lastre! Estallido de Velocidad IV por 15 segundos.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 3115 | `§6§l✦ [ESCUDO CELESTIAL] §f¡La Píldora dispersó el rayo fatal del Dao!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 4848 | `§e✦ ¡Canalizando Qi ancestral! Aguarda un instante...` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 5506 | `§c§l✖ Canalización cancelada` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 5528 | `§6§l✦✦✦ ¡LIBERACIÓN MÁXIMA (SOBRECARGA +50%)! ✦✦✦` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 5801 | `§e✦ ¡Canalizando poder! Aguarda un instante...` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 6647 | `§4§l✦ §0§l¡GETSUGA TENSHO! §4§l✦` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 6678 | `§c§l[§0§l⚡ HAKI DEL CONQUISTADOR ⚡§c§l] §4§l¡VOLUNTAD SUPREMA!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 6758 | `§b§l✦ §f§l¡FUTON: RASENSHURIKEN! §b§l✦` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7327 | `§3§l✦ §b§l[DILATACIÓN TEMPORAL] §f¡El flujo del tiempo se congela para tus enemigos!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7445 | `§a§l✦ §2§l[BENDICIÓN DEL CUIDADOR] §f¡Sanación milagrosa aplicada a ti y a tus aliados!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7465 | `§a§l✦ §f¡Tacto Sagrado! Purificación completa y vida restaurada.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7483 | `§e§l✦ §6§l[GRACIA DIVINA] §f¡Aura celestial de regeneración suprema activa!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7501 | `§a§l✦ §fSantuario Sagrado desplegado. No-muertos repelidos.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7516 | `§e§l✦ §f¡Resplandor Purificador! Estallido de luz sagrada.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7525 | `§b§l✦ §f¡Égida Celestial! Invulnerabilidad absoluta por 6 segundos.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7540 | `§6§l✦ §fVínculo de Bestias activado para todas tus mascotas.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7556 | `§a§l✦ §2§l[INSPIRACIÓN DEL MENTOR] §f¡+25% Potencia de combate a ti y tu equipo!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7574 | `§b§l✦ §3§l[ELECTRIFICACIÓN] §f¡Cuerpo imbuido en plasma celestial!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7591 | `§b§l✦ §f¡Lanza del Trueno disparada con éxito!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7603 | `§3§l✦ §b§l[ESTALLIDO DE PLASMA] §f¡Descarga circular omnidireccional!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7615 | `§e§l✦ §6§l[IRA DEL DIOS DEL TRUENO] §f¡Tormenta eléctrica celestial desatada!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7628 | `§b§l✦ §fPaso Relampagueante ejecutado.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7633 | `§e§l✦ §f¡Sobrecarga de Voltaje activa!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7638 | `§3§l✦ §fArmadura Electrificada activa.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7647 | `§6§l✦ §f¡Cuerpo Vajra Dorado activado!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7666 | `§c§l✦ §4§l[SED DE SANGRE] §f¡Vida drenada a tus enemigos!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7682 | `§c§l✦ §f¡Lanza de Sangre perforante disparada!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7694 | `§4§l✦ §c§l[ERUPCIÓN CARMESÍ] §f¡Púas de sangre empalaron a los enemigos!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7707 | `§4§l✦ §c§l[APOCALIPSIS DE SANGRE] §f¡Salud restaurada al máximo!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7713 | `§c§l✦ §fArremetida Sanguinaria ejecutada.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7719 | `§4§l✦ §f¡Sangre Hirviente desatada!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7723 | `§c§l✦ §fBarrera de Sangre Cristalina activa.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7730 | `§4§l✦ §c§l[AVATAR DEL DRAGÓN CARMESÍ] §fForma dracónica activa.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7750 | `§6§l✦ §f¡Rugido Sísmico de la Bestia liberado!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7759 | `§6§l✦ §fSalto Depredador ejecutado.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7771 | `§c§l✦ §f¡Desgarro Feroz cuádruple aplicado!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7783 | `§f§l✦ §6§l[TIGRE BLANCO] §f¡Transformación bestial activa!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7793 | `§6§l✦ §fRastreo de Presas activo. Enemigos revelados.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7799 | `§6§l✦ §fFuria de la Manada activa.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7803 | `§7§l✦ §fPelaje de Hierro endurecido.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7818 | `§c§l✦ §6§l[RUGIDO DEL TITÁN] §f¡Onda expansiva monstruosa!` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7826 | `§c§l✦ §fInstinto Valteriano desatado.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7830 | `§7§l✦ §fSupresión de dolor activa.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7835 | `§c§l✦ §fSed de sangre saciada.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7840 | `§5§l✦ §fSobrecarga de nanocuchilla activa.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7845 | `§8§l✦ §fAcecho sigiloso iniciado.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7849 | `§b§l✦ §fEmboscada fantasmal lista.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7853 | `§4§l✦ §fMarca de muerte fijada.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 7861 | `§6§l✦ §fCaída dracónica de impacto.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8659 | `§7[Visión] Has retirado tu resonancia elemental.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 8666 | `§c✦ Debes estar junto a una Estatua de los Siete o Monumento Elemental para sintonizar tu Visión. ✦` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 9376 | `§e§l✦ [TEYVAT] §fHas descansado plácidamente. Un nuevo amanecer ilumina las tierras celestiales.` |
| `com/soybe/awakeningsystem/SystemEvents.java` | 9431 | `§9§l¡VÓRTICE ABISAL DESATADO!` |
| `com/soybe/awakeningsystem/SystemItems.java` | 2013 | `§6¡Ha nacido una cría de Wyvern! ¡Aliméntala con Carne de Maná para domesticarla!` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 388 | `§e✦ Acércate al centro e interactúa con la §c§lLlave del Desafío§e para comenzar el combate.` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1224 | `§5§l✦ [BARRERA DEL ABISMO] §fHas sido contenido dentro de la plataforma del desafío.` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1319 | `§c§l✦ [DEFENSA FALLIDA] §7El Monolito de las Líneas Ley ha sido destruido.` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1458 | `§6§l✦ [¡PRIMERA MITAD COMPLETADA!] §aObjetivo cumplido.\n§b§l⚔ INICIANDO SEGUNDA MITAD (2ª ETAPA) ⚔ §e¡Derrota a los refuerzos de élite!` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1571 | `§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1588 | `§d§l✦ ¡HAS CONQUISTADO LA ESPIRAL DEL ABISMO POR COMPLETO! ✦` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1602 | `§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1603 | `§e✦ ¡Reclama el §6Cofre del Dominio§e y cruza la §dSalida Astral§e para abrir el menú!` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1605 | `§e✦ ¡Cofre del Dominio disponible! §fReclámalo con clic derecho y luego atraviesa el §b§lPortal de Descenso§f.` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1692 | `§6§l✦ [COFRE DEL DOMINIO RECLAMADO] §fHas obtenido:` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1728 | `§b✦ Redirigiendo al Menú de la Espiral del Abismo para elegir nivel o reclamar recompensas...` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1749 | `§c§l✦ [DESAFÍO FALLIDO] §7No se ha podido completar el objetivo de la cámara.` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1790 | `§d✦ ¡Has conquistado el último piso del Abismo! Redirigiendo al menú...` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1796 | `§c✦ Necesitas al menos 6 estrellas en este piso para avanzar al siguiente. Redirigiendo al menú...` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1803 | `§e✦ Debes completar todas las salas del piso actual antes de avanzar.` |
| `com/soybe/awakeningsystem/abyss/SpiralAbyssManager.java` | 1856 | `§7✦ Has salido de la Espiral del Abismo.` |
| `com/soybe/awakeningsystem/block/DungeonCoreBlock.java` | 85 | `§7El Núcleo Dimensional ha consumido su poder de mazmorra y reposa en calma.` |
| `com/soybe/awakeningsystem/block/DungeonCoreBlock.java` | 92 | `§5✦ [Núcleo de Mazmorra] §7Resuena con el aura de los Monarcas y las puertas dimensionales.` |
| `com/soybe/awakeningsystem/block/ElementalMonumentBlock.java` | 251 | `§7✦ La energía elemental de este monumento ya descansa en paz. ✦` |
| `com/soybe/awakeningsystem/block/ElementalMonumentBlock.java` | 269 | `§7✦ La energía elemental de este monumento ya descansa en paz. ✦` |
| `com/soybe/awakeningsystem/block/GachaMachineBlock.java` | 121 | `§e🪙 ¡5,000 Mora insertadas! Girando el mecanismo del Gachapon...` |
| `com/soybe/awakeningsystem/block/GachaMachineBlock.java` | 127 | `§cSaldo insuficiente. Necesitas §65,000 Mora §cpara girar el Gachapon de Teyvat.` |
| `com/soybe/awakeningsystem/block/ShrineOfDepthsBlock.java` | 126 | `§6§l✦ ¡SELLO DEL SANTUARIO DE LAS PROFUNDIDADES ROTO! ✦` |
| `com/soybe/awakeningsystem/block/ShrineOfDepthsBlock.java` | 132 | `§c[Sello Arcano] El santuario elemental está protegido por un sello inquebrantable.` |
| `com/soybe/awakeningsystem/block/SubwayGrabHandleBlock.java` | 70 | `§7[Metro] Has soltado el pasamanos.` |
| `com/soybe/awakeningsystem/block/SubwayGrabHandleBlock.java` | 79 | `§b✦ [Metro de Seúl] §7Sujeto al pasamanos (de pie, inmune a inercias). Presiona Shift para soltar.` |
| `com/soybe/awakeningsystem/block/SubwayPassengerSeatBlock.java` | 153 | `§c[Metro] Este asiento ya está ocupado.` |
| `com/soybe/awakeningsystem/block/SubwayPassengerSeatBlock.java` | 171 | `§b[Metro de Seúl] §7Te has acomodado en el asiento del vagón.` |
| `com/soybe/awakeningsystem/block/TeyvatJarBlock.java` | 92 | `§b§l[GRAN LOTERÍA GALÁCTICA] §e¡PREMIO MAYOR! La vasija desata una cascada de tesoros estelares.` |
| `com/soybe/awakeningsystem/block/TeyvatJarBlock.java` | 100 | `§c§l[LOTERÍA ROTA] §4¡El artefacto se ha hecho añicos! Pierdes casi toda tu salud.` |
| `com/soybe/awakeningsystem/block/TeyvatStoolBlock.java` | 66 | `§7Este taburete ya está ocupado.` |
| `com/soybe/awakeningsystem/block/TeyvatTableBlock.java` | 63 | `§7Ya has disfrutado de una taza de té recientemente.` |
| `com/soybe/awakeningsystem/block/TeyvatTableBlock.java` | 76 | `§a§l✦ [MESA DE TEYVAT] §fHas degustado una reconfortante taza de té aromático.` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 114 | `§d[Prueba de Sonnet] §fLa melodía de la Luna Creciente ya resuena plenamente en ti.` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 134 | `§d✦ ¡Has obtenido la Autoridad Lunar de Sonnet (Luna Creciente)!` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 187 | `§d§m==================================================` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 188 | `§d§l✦ [EL DESPERTAR DE LA DIOSA TRILUNAR] ✦` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 190 | `§6§l✦ RECOMPENSAS SAGRADAS OBTENIDAS:` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 191 | `§e✔ Bendición Trilunar de Columbina: §d+135 Protogemas diarias (+50% Permanente)` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 192 | `§b✔ +450 Cristales Génesis §fy §d+300 Protogemas directas` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 193 | `§5✔ Contrato de Invocación 5★: Columbina (Diosa Trilunar de Teyvat)` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 194 | `§6✔ Título Honorífico: §eElegido de la Diosa Trilunar` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 195 | `§d§m==================================================` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 219 | `§d[Prueba de Sonnet - Luna Creciente] §fSostén 1 Protogema en tu mano y haz clic derecho en el Altar para armonizar el Canto Lunar.` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 220 | `§d✦ Ofrece 1 Protogema en el altar para resonar con Sonnet.` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 263 | `§b✦ [Prueba de Aria] ¡Purifica a las 6 Sombras Astrales!` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 291 | `§5✦ [Prueba de Canon] ¡Derrota al Guardián del Silencio Lunar!` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 311 | `§b✦ ¡Prueba de Aria completada! Recoge la Autoridad Lunar de Aria.` |
| `com/soybe/awakeningsystem/block/TrilunarAltarBlock.java` | 318 | `§5✦ ¡Prueba de Canon completada! Recoge la Autoridad Lunar de Canon.` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapBlockEntity.java` | 227 | `§c[Trampa de Pesca] Sólo funciona sumergida o con agua directamente encima/adyacente.` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapBlockEntity.java` | 239 | `§c[Trampa de Pesca] No hay cebos válidos en las ranuras superiores.` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapBlockEntity.java` | 357 | `§a[Trampa de Pesca] Pesca automática ACTIVADA.` |
| `com/soybe/awakeningsystem/block/entity/FishingTrapBlockEntity.java` | 362 | `§c[Trampa de Pesca] Pesca automática DESACTIVADA.` |
| `com/soybe/awakeningsystem/entity/AzhdahaBossEntity.java` | 256 | `§6§l¡Cuidado! §eEl techo de la caverna se está derrumbando...` |
| `com/soybe/awakeningsystem/entity/CryoRegisvineEntity.java` | 326 | `§b§l✦ ¡COROLA CRYO DESTRUIDA! §fLa Regisvid Criogénica yace desplomada.` |
| `com/soybe/awakeningsystem/entity/ElectroRegisvineEntity.java` | 279 | `§d§l✦ [¡COROLA ELECTRO DESTRUIDA!] §f¡La Regisvid está aturdida y vulnerable a ataques críticos!` |
| `com/soybe/awakeningsystem/entity/ElectroRegisvineEntity.java` | 306 | `§d§l✦ ¡REGISVID ELECTRO DERROTADA! §eHas ganado §6+60,000 Mora §ey §b+35 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/ElectroRegisvineEntity.java` | 311 | `§d§l✦ ¡REGISVID ELECTRO DERROTADA! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/GreatSnowboarKingBossEntity.java` | 199 | `§b§l✦ ¡REY JABALÍ DE LAS NIEVES DERROTADO! §eHas ganado §6+55,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/GreatSnowboarKingBossEntity.java` | 204 | `§b§l✦ ¡REY JABALÍ DE LAS NIEVES DERROTADO! §6+20 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/PaimonEntity.java` | 177 | `§d§l[Paimon] §c¡Cuidado Viajero! ¡Tu salud está muy baja! ¡Cúrate rápido!` |
| `com/soybe/awakeningsystem/entity/PaimonEntity.java` | 181 | `§d§l[Paimon] §b¡Está lloviendo! ¡A Paimon no le gusta mojarse las alas!` |
| `com/soybe/awakeningsystem/entity/PaimonEntity.java` | 205 | `§d§l[Paimon] §f¡Hola! ¡Paimon será tu guía y mejor compañera en este mundo!` |
| `com/soybe/awakeningsystem/entity/PaimonEntity.java` | 222 | `§d§l[Paimon] §c¡Oye! ¡Paimon NO es comida de emergencia! ¡Paimon es una compañera respetable!` |
| `com/soybe/awakeningsystem/entity/PaimonEntity.java` | 245 | `§d§l[Paimon] §a¡Mmm, qué delicia! ¡La comida que me diste estaba increíble! ¡Paimon te da su bendición estelar (Suerte II y Maná)!` |
| `com/soybe/awakeningsystem/entity/PerpetualMechanicalArrayBossEntity.java` | 139 | `§6§l✦ [COLAPSO DE MATRIZ] §f¡La Matriz Mecánica ha entrado en estado de colapso y es vulnerable!` |
| `com/soybe/awakeningsystem/entity/PerpetualMechanicalArrayBossEntity.java` | 181 | `§6§l✦ [MODO DEFENSIVO] §f¡La Matriz se ha fragmentado! ¡Destruye a sus Centinelas Ruina!` |
| `com/soybe/awakeningsystem/entity/PerpetualMechanicalArrayBossEntity.java` | 254 | `§6§l✦ ¡MATRIZ MECÁNICA PERPETUA DERROTADA! §eHas ganado §6+65,000 Mora §ey §b+35 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/PerpetualMechanicalArrayBossEntity.java` | 259 | `§6§l✦ ¡MATRIZ MECÁNICA PERPETUA DERROTADA! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/PyroRegisvineEntity.java` | 324 | `§c§l✦ ¡COROLA PYRO DESTRUIDA! §fLa Regisvid Pirotécnica yace noqueada.` |
| `com/soybe/awakeningsystem/entity/RhodeiaOceanidBossEntity.java` | 217 | `§9§l✦ ¡RHODEIA DERROTADA! §eHas ganado §6+60,000 Mora §ey §b+35 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/RhodeiaOceanidBossEntity.java` | 222 | `§9§l✦ ¡RHODEIA DERROTADA! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/RuinGuardEntity.java` | 154 | `§6§l¡PUNTO DÉBIL IMPACTADO! §eEl Guardián de las Ruinas ha quedado desactivado.` |
| `com/soybe/awakeningsystem/entity/RuinHunterEntity.java` | 273 | `§c§l✦ ¡NÚCLEO OCULAR SOBRECARGADO! §eEl Cazador de las Ruinas ha sido derribado y desactivado.` |
| `com/soybe/awakeningsystem/entity/RuinSerpentBossEntity.java` | 208 | `§8§l✦ ¡SERPIENTE DE LAS RUINAS DERROTADA! §eHas ganado §6+60,000 Mora §ey §b+35 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/RuinSerpentBossEntity.java` | 213 | `§8§l✦ ¡SERPIENTE DE LAS RUINAS DERROTADA! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/SubwayTrainCarriageEntity.java` | 113 | `§c⚠ [FIN DE VÍA / TIERRA] El tren solo puede circular sobre rieles.` |
| `com/soybe/awakeningsystem/entity/SubwayTrainCarriageEntity.java` | 141 | `§6§l✦ [Bocina del Metro de Seúl] ✦` |
| `com/soybe/awakeningsystem/entity/SubwayTrainCarriageEntity.java` | 250 | `§6§l✦ [TREN ESTACIONADO EN EL ANDÉN] ✦\n§aLa estructura se ha fijado como bloques sólidos en la estación.` |
| `com/soybe/awakeningsystem/entity/SubwayTrainCarriageEntity.java` | 272 | `§6§l[CABINA DE CONDUCCIÓN - LÍNEA 3] §a¡Al mando! Usa §fW §apara acelerar y §fS §apara frenar.\n§7Shift + Clic cuando estés detenido para estacionar en el andén.` |
| `com/soybe/awakeningsystem/entity/SubwayTrainCarriageEntity.java` | 274 | `§b[METRO DE SEÚL - COCHE 3707] §7Has subido como pasajero al vagón.` |
| `com/soybe/awakeningsystem/entity/WyvernEntity.java` | 355 | `§6§l✦ ¡Has domado con éxito a este feroz Wyvern! ✦` |
| `com/soybe/awakeningsystem/entity/WyvernEntity.java` | 380 | `§aWyvern equipado con montura. ¡Listo para alzar el vuelo!` |
| `com/soybe/awakeningsystem/entity/hypostasis/AnemoHypostasisBossEntity.java` | 181 | `§b§l✦ [BETH EN CRISIS] §f¡Recoge o destruye las §b3 Esferas de Viento §fantes de que regenere su núcleo!` |
| `com/soybe/awakeningsystem/entity/hypostasis/AnemoHypostasisBossEntity.java` | 211 | `§b§l✦ ¡HIPÓSTASIS ANEMO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/AnemoHypostasisBossEntity.java` | 216 | `§b§l✦ ¡HIPÓSTASIS ANEMO DERROTADO! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/CryoHypostasisBossEntity.java` | 124 | `§b§l✦ [DALETH EN CRISIS] §f¡Aplica ataques de Pyro para derretir la coraza helada antes de que restaure su vitalidad!` |
| `com/soybe/awakeningsystem/entity/hypostasis/CryoHypostasisBossEntity.java` | 146 | `§b§l✦ ¡HIPÓSTASIS CRYO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/CryoHypostasisBossEntity.java` | 151 | `§b§l✦ ¡HIPÓSTASIS CRYO DERROTADO! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/DendroHypostasisBossEntity.java` | 152 | `§a§l✦ [ZAYIN EN CRISIS] §f¡Purifica los §a3 Núcleos Restauradores Tri-Lakshana §fantes de que Zayin reviva!` |
| `com/soybe/awakeningsystem/entity/hypostasis/DendroHypostasisBossEntity.java` | 182 | `§a§l✦ ¡HIPÓSTASIS DENDRO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/DendroHypostasisBossEntity.java` | 187 | `§a§l✦ ¡HIPÓSTASIS DENDRO DERROTADO! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/ElectroHypostasisBossEntity.java` | 187 | `§d§l✦ [ALEPH EN CRISIS] §f¡Destruye los §d3 Prismas Electro §fantes de que Aleph recupere su energía!` |
| `com/soybe/awakeningsystem/entity/hypostasis/ElectroHypostasisBossEntity.java` | 217 | `§d§l✦ ¡HIPÓSTASIS ELECTRO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/ElectroHypostasisBossEntity.java` | 222 | `§d§l✦ ¡HIPÓSTASIS ELECTRO DERROTADO! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/GeoHypostasisBossEntity.java` | 125 | `§6§l✦ [GIMEL EN CRISIS] §f¡Ataca rápidamente el núcleo de Geo expuesto antes de que canalice su defensa terrestre!` |
| `com/soybe/awakeningsystem/entity/hypostasis/GeoHypostasisBossEntity.java` | 147 | `§6§l✦ ¡HIPÓSTASIS GEO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/GeoHypostasisBossEntity.java` | 152 | `§6§l✦ ¡HIPÓSTASIS GEO DERROTADO! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/HydroHypostasisBossEntity.java` | 120 | `§9§l✦ [HE EN CRISIS] §f¡Interrumpe las gotas de agua con ataques elementales antes de que curen al núcleo!` |
| `com/soybe/awakeningsystem/entity/hypostasis/HydroHypostasisBossEntity.java` | 142 | `§9§l✦ ¡HIPÓSTASIS HYDRO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/HydroHypostasisBossEntity.java` | 147 | `§9§l✦ ¡HIPÓSTASIS HYDRO DERROTADO! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/PyroHypostasisBossEntity.java` | 124 | `§c§l✦ [AYIN EN CRISIS] §f¡Aplica ataques de Hydro para extinguir las ascuas ígneas antes de que se reavive!` |
| `com/soybe/awakeningsystem/entity/hypostasis/PyroHypostasisBossEntity.java` | 146 | `§c§l✦ ¡HIPÓSTASIS PYRO DERROTADO! §eHas ganado §6+60,000 Mora §ey §b+30 Protogemas§e.` |
| `com/soybe/awakeningsystem/entity/hypostasis/PyroHypostasisBossEntity.java` | 151 | `§c§l✦ ¡HIPÓSTASIS PYRO DERROTADO! §6+24 Monedas de Despertar§e.` |
| `com/soybe/awakeningsystem/item/AsrahanHolySwordItem.java` | 89 | `§b§l✦ [MAREMOTO SAGRADO] §f¡Marea purificadora de Asrahan desatada con bendición acuática!` |
| `com/soybe/awakeningsystem/item/AstralGuandaoItem.java` | 90 | `§d§l✦ [CRECIENTE DRACÓNICO] §5¡Corte cósmico del Dragón Celestial desatado!` |
| `com/soybe/awakeningsystem/item/BlackDragonScytheItem.java` | 93 | `§5§l✦ [COSECHA DEL DRAGÓN NEGRO] §4¡Tajo devastador de guadaña y fuego de dragón desatado!` |
| `com/soybe/awakeningsystem/item/CelestialPineNutItem.java` | 60 | `§2§l🌲 [PIÑÓN DEL DAO] §fLa esencia pétrea del pino milenario fortalece tu físico (+45 Qi).` |
| `com/soybe/awakeningsystem/item/CondensedResinItem.java` | 58 | `§6✦ [RESINA CONCENTRADA] §e¡Bendición de Dominio activada! EXP y botín multiplicados por 15 minutos.` |
| `com/soybe/awakeningsystem/item/CosmicFragmentItem.java` | 56 | `§6✦ [SÍNTESIS CÓSMICA] §e¡160 Fragmentos Cósmicos se han condensado en 1 Destino Entrelazado!` |
| `com/soybe/awakeningsystem/item/CosmicFragmentItem.java` | 79 | `§b✦ [REPARACIÓN CÓSMICA] §f¡Durabilidad restaurada en un +25%! (-16 Fragmentos Cósmicos).` |
| `com/soybe/awakeningsystem/item/CosmicFragmentItem.java` | 82 | `§c✦ Necesitas al menos 16 Fragmentos Cósmicos para reparar tu objeto.` |
| `com/soybe/awakeningsystem/item/CosmicFragmentItem.java` | 86 | `§7✦ Coloca un arma o armadura dañada en tu mano secundaria para repararla (-16), o presiona Shift + Clic Derecho para sintetizar 1 Destino Entrelazado (-160).` |
| `com/soybe/awakeningsystem/item/CreatorClaymoreItem.java` | 187 | `§6§l✦ ¡EJECUCIÓN DEL CREADOR! §eLa existencia del enemigo fue aniquilada en polvo estelar (Fuerza V activada).` |
| `com/soybe/awakeningsystem/item/CrimsonPhoenixPillItem.java` | 46 | `§c✦ Has refinado la §6Píldora del Fénix Carmesí §c(+6,000 Qi).` |
| `com/soybe/awakeningsystem/item/CrownOfInsightItem.java` | 66 | `§6§l✦ [CORONACIÓN CELESTIAL] §e¡Has coronado tus talentos! +1 Nivel a todas tus habilidades activas y 20m de Bendición Celestial.` |
| `com/soybe/awakeningsystem/item/DarkRepulserItem.java` | 139 | `§b§l✦ [DUAL ECLIPSE] §f¡Vórtice binario de Cristal y Fuego Oscuro desatado con Doble Empuñadura!` |
| `com/soybe/awakeningsystem/item/DarkRepulserItem.java` | 171 | `§a§l✦ [RESONANCIA CRISTALINA] §f¡Cortes de cristal de dragón aguamarina desatados!` |
| `com/soybe/awakeningsystem/item/DawnsBalladItem.java` | 94 | `§d§l✦ [BALADA DEL ALBA] §5¡Finta dimensional y corte crepuscular desatados!` |
| `com/soybe/awakeningsystem/item/DoctorsRobeItem.java` | 59 | `§a§l[MANTO DEL DOCTOR] §2¡Canalización cósmica! Maná y Qi restaurados al 100% con escudo vital.` |
| `com/soybe/awakeningsystem/item/DreadScytheItem.java` | 158 | `§5§l✦ ¡ALMA COSECHADA! §dEl terror potencia tu fuerza y velocidad.` |
| `com/soybe/awakeningsystem/item/EldoriaFruitItem.java` | 60 | `§e§l⭐ [FRUTA DE ELDORIA] §fLa esencia astral agudiza tu visión y restaura +50 de Maná.` |
| `com/soybe/awakeningsystem/item/ElucidatorItem.java` | 109 | `§9§l✦ [STARBURST STREAM V2] §f¡Flurry de 16 cortes de vacío y Cruz Titánica desatados!` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 82 | `§6⭐⭐⭐⭐⭐ [CONO DE LUZ 5★] §e¡Atributos Legendarios Sintonizados!` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 95 | `§c✦ Ese objeto no es un Cono de Luz válido. ✦` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 107 | `§7✦ [CONO DE LUZ DESEQUIPADO] §fRetirado a tu inventario.` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 136 | `§c✦ Ese objeto no es una Piedra de Runa válida. ✦` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 241 | `§5§l✦ [PASO SOMBRÍO] §d¡Te has desvanecido tras la espalda del enemigo!` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 250 | `§5§l✦ [PASO SOMBRÍO] §d¡Desplazamiento espectral!` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 263 | `§4§l✦ [OJOS DRACÓNICOS] §c¡Visión omnisciente del dragón activada!` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 272 | `§f§l✦ [LA CUARTA PARED] §b¡Muro conceptual absoluto activado! Efectos negativos purificados.` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 280 | `§7§l✦ [CUERPO DE HIERRO] §f¡Piel de adamantita forjada! +60% resistencia física.` |
| `com/soybe/awakeningsystem/item/EquipmentSystemManager.java` | 288 | `§c§l✦ [SED DE SANGRE] §4¡Furia berserker desatada! Fuerza y velocidad aumentadas.` |
| `com/soybe/awakeningsystem/item/FableScrollItem.java` | 44 | `§d§l✦ [Star Stream] §e¡Has desplegado una Fábula Central! §a+5,000 EXP de Constelación §ey §6+10,000 Mora.` |
| `com/soybe/awakeningsystem/item/FoundationBuildingPillItem.java` | 46 | `§a✦ Has refinado la §2Píldora de Construcción de Cimientos §a(+2,500 Qi).` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 214 | `§6§l¡✦✦✦✦✦ JACKPOT LEGENDARIO! §e¡Has obtenido el §dCono de Luz 3D: Noche en la Vía Láctea§e!` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 218 | `§6§l¡✦✦✦✦✦ JACKPOT LEGENDARIO! §e¡Has obtenido el §dCono de Luz 3D: A lo Largo de la Costa Pasajera§e!` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 222 | `§6§l¡✦✦✦✦✦ JACKPOT LEGENDARIO! §e¡Has obtenido el §dCono de Luz 3D: La Paciencia es Todo lo que Necesitas§e!` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 226 | `§6§l¡✦✦✦✦✦ JACKPOT LEGENDARIO! §e¡Has obtenido el §dCono de Luz 3D: Donde los Sueños Deberían Descansar§e!` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 230 | `§6§l¡✦✦✦✦✦ JACKPOT LEGENDARIO! §e¡Has obtenido el §dCono de Luz 3D: Antes del Amanecer§e!` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 234 | `§6§l¡✦✦✦✦✦ RECOMPENSA DORADA! §e¡Has obtenido §d1x Destino Entrelazado§e!` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 240 | `§6§l¡✦✦✦✦✦ BOLSA DEL DRAGÓN! §e¡Has ganado §6+100,000 Mora§e directa a tu monedero!` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 270 | `§d§l¡✦✦✦✦ PREMIO RARO! §fHas obtenido §d4x Brillo Estelar§f.` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 274 | `§d§l¡✦✦✦✦ PREMIO RARO! §fHas obtenido §b2x Píldora del Dragón Naciente§f.` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 278 | `§d§l¡✦✦✦✦ PREMIO RARO! §fHas obtenido §b2x Insignia del Guardián§f.` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 284 | `§d§l¡✦✦✦✦ PREMIO RARO! §fHas obtenido §6+25,000 Mora§f.` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 300 | `§b✦✦✦ Recompensa: §fHas obtenido §93x Ingenio del Héroe§f.` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 304 | `§b✦✦✦ Recompensa: §fHas obtenido §b15x Polvo Estelar§f.` |
| `com/soybe/awakeningsystem/item/GachaCapsuleItem.java` | 320 | `§b✦✦✦ Recompensa: §fHas obtenido §6+7,500 Mora§f.` |
| `com/soybe/awakeningsystem/item/GalacticBigLottoItem.java` | 51 | `§c§l✦ [GRAN LOTERÍA INTERGALÁCTICA] §4¡CRACK! ¡La Lotería se ha roto en mil pedazos! Tu salud cae a 1 HP...` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 139 | `§e✦ ¡Esta arma ya ha alcanzado el Rango Máximo de Refinamiento (R5)!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 209 | `§d§l✦ [LUZ DEL SEGADOR] §e¡SUEÑO ETERNO DE MUSOU ISSHIN ACTIVADO!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 226 | `§a§l✦ [HALCÓN DE JADE] §f¡RUINA DE TODO MAL DESATADA!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 244 | `§4§l✦ [SEMBLANZA DE LA LUNA CARMESÍ] §c¡CORTE DEL PACTO CARMESÍ!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 262 | `§6§l✦ [ROMPE ROCAS] §e¡ORDEN CELESTIAL Y DOMINIO TELÚRICO!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 280 | `§c§l✦ [LÁPIDA DEL LOBO] §f¡AULLIDO DEL REY LOBO! (+40% ATQ ALIADOS)` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 292 | `§6§l✦ [ESPADÓN CORNIRROJO] §e¡FURIA DEL REY ONI DORADO ACTIVADA!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 307 | `§e§l✦ [SENTENCIA] §6¡DISPARO DE CAÑÓN CEREMONIAL EJECUTADO!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 329 | `§9§l✦ [REFLEJO DE LAS TINIEBLAS] §b¡CORTE DE NIEBLA TRASCENDENTE!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 342 | `§a§l✦ [CORTADOR DE JADE PRIMORDIAL] §f¡PURIFICACIÓN ESMERALDA COMPLETA!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 362 | `§b§l✦ [JURAMENTO POR LA LIBERTAD] §f¡VÓRTICE CÉFIRO DE LA REBELIÓN!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 374 | `§3§l✦ [LLAVE DE LA CORONACIÓN] §b¡AGUAS CURATIVAS DEL OASIS DESATADAS!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 391 | `§d§l✦ [EXPIACIÓN] §f¡ESTOCADA FULGURANTE DE LOS DUELISTAS!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 402 | `§b§l✦ [LA CAPTURA] §f¡ENERGÍA ELEMENTAL RECARGADA (+35 MANÁ)!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 414 | `§7§l✦ [VADO DEL RÍO CENICIENTO] §f¡REGENERACIÓN Y VELOCIDAD CLANDESTINA!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 434 | `§b§l✦ [HOJA AFILADA CELESTIAL] §f¡PODER PERFORACIELOS DESATADO!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 453 | `§e§l✦ [LA ESPADA DE LA FLAUTA] §6¡OBERTURA MELÓDICA RESONANTE!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 464 | `§4§l✦ [LA ESPADA NEGRA] §c¡SED DE SANGRE Y COMBATE DESPERTADA!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 482 | `§7§l✦ [BORLA NEGRA] §f¡ESTOCADA CAZADORA DE ALIMAÑAS!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 508 | `§d§l✦ [MARTILLO DE HERTA] §b¡KURU KURU! ~ ¡KURURIN! (Torbellino Gélido)` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 527 | `§b§l✦ [ESPADA QINGMING] §3¡FLORACIÓN DE ÉTER Y ESCARCHA DESATADA!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 543 | `§6§l✦ [TAILLESS] §c¡RUPTURA DE ANOMALÍA CIBERNÉTICA EJECUTADA!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 561 | `§e§l✦ [ESPADA SHUNGUANG] §f¡DESTELLO RELÁMPAGO FUGAZ!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 574 | `§a§l✦ [ESPADA SHIYUAN] §f¡PULSO DEL ORIGEN ACTIVADO (+30 MANÁ)!` |
| `com/soybe/awakeningsystem/item/GenshinWeaponItem.java` | 597 | `§d§l✦ [DOMINIO DE LA SANCIÓN] §e¡JUICIO DE LA HERRSCHER DEL TRUENO!` |
| `com/soybe/awakeningsystem/item/GiftCardItem.java` | 45 | `§c✦ Las Tarjetas de Regalo requieren la Versión 4.0 (Teyvat) del Sistema.` |
| `com/soybe/awakeningsystem/item/HeavenlyEnlightenmentPillItem.java` | 55 | `§d✦ Has refinado la §5Píldora de la Iluminación Celestial §d(+4,500 Qi).` |
| `com/soybe/awakeningsystem/item/HeavenlyLongevityPeachItem.java` | 68 | `§6§l🍑 [MELOCOTÓN DE LA INMORTALIDAD] §e¡La fruta divina regenera tu cuerpo y meridianos (+150 Qi Supremo)!` |
| `com/soybe/awakeningsystem/item/HeavenShakingSwordItem.java` | 85 | `§6§l✦ [ESPADA QUE SACUDE LOS CIELOS] §e¡Tajo destructor de los nueve cielos desatado!` |
| `com/soybe/awakeningsystem/item/LaDoncellaSwordItem.java` | 96 | `§4§l🌹 [LA DONCELLA] §c¡El Trono de Ceniza despierta bajo la Luna Carmesí! Las espinas han brotado.` |
| `com/soybe/awakeningsystem/item/LaDoncellaSwordItem.java` | 106 | `§f§l✦ [LA DONCELLA] §7Las espinas se repliegan. El acero noble y las rosas puras resplandecen.` |
| `com/soybe/awakeningsystem/item/LaDoncellaSwordItem.java` | 167 | `§4§l🌹 [TRONO DE CENIZA] §c¡Creciente colosal carmesí y tajo ascendente desatados con robo de vida!` |
| `com/soybe/awakeningsystem/item/LaDoncellaSwordItem.java` | 215 | `§d§l✦ [FRAGANCIA DEL SILENCIO] §fLa fragancia purifica tu espíritu y bendice tus sentidos (65 daño en cono).` |
| `com/soybe/awakeningsystem/item/LambentLightItem.java` | 90 | `§e§l✦ [LINEAR / FLASHING PENETRATOR] §f¡Haz de estocada a la velocidad de la luz disparado!` |
| `com/soybe/awakeningsystem/item/LightConeAlongThePassingShoreItem.java` | 34 | `§c✦ [CONO DE LUZ] §fEquipa este Cono de Luz en el Menú del Sistema [K] para activar sus atributos estelares y bonificaciones de combate pasivas.` |
| `com/soybe/awakeningsystem/item/LightConeBeforeDawnItem.java` | 34 | `§e✦ [CONO DE LUZ] §fEquipa este Cono de Luz en el Menú del Sistema [K] para activar sus atributos estelares y bonificaciones de combate pasivas.` |
| `com/soybe/awakeningsystem/item/LightConeNightOnTheMilkyWayItem.java` | 34 | `§d✦ [CONO DE LUZ] §fEquipa este Cono de Luz en el Menú del Sistema [K] para activar sus atributos estelares y bonificaciones de combate pasivas.` |
| `com/soybe/awakeningsystem/item/LightConePatienceIsAllYouNeedItem.java` | 35 | `§d✦ [CONO DE LUZ] §fEquipa este Cono de Luz en el Menú del Sistema [K] para activar sus atributos estelares y bonificaciones de combate pasivas.` |
| `com/soybe/awakeningsystem/item/LightConeWhereDreamsShouldRestItem.java` | 35 | `§6✦ [CONO DE LUZ] §fEquipa este Cono de Luz en el Menú del Sistema [K] para activar sus atributos estelares y bonificaciones de combate pasivas.` |
| `com/soybe/awakeningsystem/item/NineRevolutionsPillItem.java` | 46 | `§6✦ Has refinado la §ePíldora de las Nueve Revoluciones §6(+10,000 Qi).` |
| `com/soybe/awakeningsystem/item/OliveElixirItem.java` | 36 | `§c[Sistema] No tienes atributos asignados para restablecer.` |
| `com/soybe/awakeningsystem/item/OliveElixirItem.java` | 51 | `§a§l✦ ¡Elixir de Olivo consumido! §fTus atributos han sido restablecidos y los puntos devueltos.` |
| `com/soybe/awakeningsystem/item/OriginalResinItem.java` | 53 | `§b✦ [RESINA ORIGINAL] §7¡Energía primordial de las Líneas Ley absorbida! (+20 Energía)` |
| `com/soybe/awakeningsystem/item/PillOfPrimordialRebirthItem.java` | 49 | `§6§m==================================================` |
| `com/soybe/awakeningsystem/item/PillOfPrimordialRebirthItem.java` | 50 | `§6§l✦ [RENACIMIENTO PRIMORDIAL DEL DESTINO] ✦` |
| `com/soybe/awakeningsystem/item/PillOfPrimordialRebirthItem.java` | 52 | `§e✔ Elige tu nuevo sendero marcial y trascendente.` |
| `com/soybe/awakeningsystem/item/PillOfPrimordialRebirthItem.java` | 53 | `§6§m==================================================` |
| `com/soybe/awakeningsystem/item/PlumBlossomSwordItem.java` | 93 | `§d§l✦ [DANZA DE LOS 24 PÉTALOS] §f¡Cuchillas de Qi de Flor de Ciruelo desatadas!` |
| `com/soybe/awakeningsystem/item/PraiseOfLoftyMoralityItem.java` | 40 | `§e§l✦ [ELOGIO A LA ALTA MORAL] §f¡El cosmos bendice tus virtuosas acciones! (+Suerte II y Regeneración II)` |
| `com/soybe/awakeningsystem/item/PrimogemSwordItem.java` | 91 | `§5§l✦ [ESPADA DE PROTOGEMAS] §d¡Deseo Astral del Destino liberado! (+Salud restaurada)` |
| `com/soybe/awakeningsystem/item/RadiantLightSwordItem.java` | 104 | `§e§l✦ [DANZA DE LA ESPADA DE LUZ] §6¡Tajo solar radiante y haz purificador desatados!` |
| `com/soybe/awakeningsystem/item/RobeOfTheBeautyItem.java` | 90 | `§d✦ [MANTO DE LA BELLEZA] §f¡Preservación Cósmica activada! (-30 Fragmentos Cósmicos) Daño mitigado.` |
| `com/soybe/awakeningsystem/item/RuneStoneItem.java` | 86 | `§6✦ [ENGARCE RÚNICO] §fEngarza esta Piedra de Runa en el Menú del Sistema [K] para activar su poder mediante teclas asignadas (Runas Activas) o bonificaciones permanentes (Runas Pasivas).` |
| `com/soybe/awakeningsystem/item/ShadowDaggersItem.java` | 138 | `§8§l✦ [PASO DE SOMBRAS] §7¡Parpadeo sombrío ejecutado con cuchilla de vacío!` |
| `com/soybe/awakeningsystem/item/SkyDemonSwordItem.java` | 89 | `§4§l✦ [TAJO DEL DRAGÓN DEMONIACO] §c¡Qi carmesí devastador desatado con perforación de armadura!` |
| `com/soybe/awakeningsystem/item/SoulSolidSwordItem.java` | 100 | `§b✦ [Soul Solid] §f¡Aubade Coup Droit! Estocada de Hielo del Inframundo desatada.` |
| `com/soybe/awakeningsystem/item/SpiritPlumFruitItem.java` | 60 | `§d§l🌸 [CIRUELA ESPIRITUAL] §fEl néctar de Monte Hua purifica tus meridianos (+30 Qi).` |
| `com/soybe/awakeningsystem/item/StaffOfHomaItem.java` | 89 | `§6§l✦ [BÁCULO DE HOMA] §c§l¡MARIPOSA DEL AVERNO DESATADA! §e(Tajo ígneo espectral +45% ATQ)` |
| `com/soybe/awakeningsystem/item/TeyvatDelusionItem.java` | 172 | `§4[Engaño Fatui] §c¡Poder liberado! La fuerza elemental corroe tu vitalidad...` |
| `com/soybe/awakeningsystem/item/TeyvatFoodItem.java` | 92 | `§6§l✦ [TENTACIÓN DE ADEPTUS] §f¡Poder celestial imbuido! (+30% Daño de Ataque, Absorción y Regeneración durante 5 min).` |
| `com/soybe/awakeningsystem/item/TeyvatFoodItem.java` | 98 | `§a§l✦ [POLLO DULCE] §f¡Salud restaurada (+16 HP) y efectos negativos purificados!` |
| `com/soybe/awakeningsystem/item/TeyvatFoodItem.java` | 104 | `§b§l✦ [TOFU DE ALMENDRAS] §f¡La agilidad de Xiao fluye en ti! (+Velocidad y Suerte aumentadas durante 5 min).` |
| `com/soybe/awakeningsystem/item/TeyvatFoodItem.java` | 108 | `§e§l✦ [FRUTA DEL OCASO] §f¡Dulce y jugosa! (+4 HP restaurados).` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 66 | `§b✦ [VISIÓN ELEMENTAL] §fEquipa o sintoniza esta Visión en el Menú del Sistema [K] o junto a una Estatua de los Siete para canalizar su resonancia elemental y habilidades.` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 75 | `§cManá insuficiente para Habilidad Definitiva Elemental (Requiere 30 MP)` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 109 | `§c§l✦ [VISIÓN PYRO] §6¡ESTALLIDO ÍGNEO DE TEYVAT!` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 121 | `§d§l✦ [VISIÓN ELECTRO] §5¡SOBRECARGA ELÉCTRICA DE INAZUMA!` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 134 | `§b§l✦ [VISIÓN CRYO] §3¡DOMINIO CONGELADO DE SNEZHNAYA!` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 149 | `§a§l✦ [VISIÓN ANEMO] §2¡VÓRTICE ELEMENTAL DE MONDSTADT!` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 164 | `§9§l✦ [VISIÓN HYDRO] §b¡MAREMOTO PURIFICADOR Y MAREA SANADORA!` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 179 | `§6§l✦ [VISIÓN GEO] §e¡ORDEN INMUTABLE Y ESCUDO DE JADE!` |
| `com/soybe/awakeningsystem/item/TeyvatVisionItem.java` | 193 | `§a§l✦ [VISIÓN DENDRO] §2¡FLORECIMIENTO PRIMORDIAL Y ESPORAS VIVIENTES!` |
| `com/soybe/awakeningsystem/item/TrackMarkerWandItem.java` | 77 | `§7No hay waypoints para deshacer.` |
| `com/soybe/awakeningsystem/item/TrackMarkerWandItem.java` | 89 | `§c⚠ La varita no tiene ningún waypoint registrado. Marca puntos en el suelo primero.` |
| `com/soybe/awakeningsystem/item/TrackMarkerWandItem.java` | 117 | `§c⚠ Demasiado cerca del waypoint anterior. Separa los puntos al menos 2 bloques.` |
| `com/soybe/awakeningsystem/item/TrackMarkerWandItem.java` | 139 | `§6🚩 Waypoint #1 (SALIDA / LÍNEA DE META) §fregistrado. ¡Traza el recorrido!` |
| `com/soybe/awakeningsystem/item/TransientResinItem.java` | 51 | `§d✦ [RESINA EFÍMERA] §a¡Has liberado 60 unidades de Resina Original!` |
| `com/soybe/awakeningsystem/item/VigorPotionItem.java` | 50 | `§6§l✦ ¡Poción de Vigor consumida! §e+100 Energía y vigor revitalizante.` |
| `com/soybe/awakeningsystem/item/WhiteTigerPillItem.java` | 46 | `§b✦ Has refinado la §fPíldora del Tigre Blanco del Trueno §b(+7,500 Qi).` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 206 | `§e✦ Ya te encuentras dentro de tu Subespacio Dimensional.` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 220 | `§c[ERROR] La dimensión del Subespacio no está disponible en este momento.` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 242 | `§e✦ Haz clic en el Waypoint Central o usa §a/escape §7o §a/system subspace exit §7para regresar.` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 276 | `§a✔ Regresaste exitosamente de tu Subespacio Dimensional.` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 283 | `§a✔ Tu Subespacio Dimensional ya ha alcanzado el Nivel Máximo (Nivel 5: Pequeño Mundo del Monarca).` |
| `com/soybe/awakeningsystem/subspace/SubspaceManager.java` | 342 | `§7✦ El territorio se ha expandido significativamente. ¡Construye tu almacén y base sagrada!` |
| `com/soybe/awakeningsystem/util/TrackPlacementHelper.java` | 33 | `§c⚠ [Tendido Create] La distancia máxima es de 48 bloques.` |

### Módulo: Renderizado e Interfaces (GUI & Shaders) (71 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/AwakeningSystemClient.java` | 898 | `§c✦ El Sistema de Deseos (Gacha) se desbloquea en la Versión 4.0 (Teyvat).` |
| `com/soybe/awakeningsystem/client/track/TrackPlacementPreview.java` | 60 | `§c✦ [Trazado Create] Anclaje de vía cancelado.` |
| `com/soybe/awakeningsystem/client/track/TrackPlacementPreview.java` | 117 | `§c⚠ [Trazado Create] La distancia máxima es de 48 bloques.` |
| `com/soybe/awakeningsystem/guild/GuildChunkMapScreen.java` | 425 | `§cNo tienes rango de Oficial o Líder para gestionar territorio.` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 170 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 182 | `§cNo se encontró ningún gremio con ese nombre.` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 193 | `§7No hay gremios fundados en este mundo aún. ¡Usa §e/guild create §7para fundar uno!` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 196 | `§6═══════ §f[LISTA DE GREMIOS DE CAZADORES] §6═══════` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 217 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 242 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 267 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildCommands.java` | 305 | `§6══════════════════════════════════` |
| `com/soybe/awakeningsystem/guild/GuildEventHandler.java` | 62 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildEventHandler.java` | 337 | `§6⚔️ ¡Tu gremio ha recibido §e+300 EXP §6por derrotar un Jefe!` |
| `com/soybe/awakeningsystem/guild/GuildHuntingBoardBlock.java` | 59 | `§c[Gremio] Debes ser miembro de un gremio para consultar las misiones de cacería. Usa §e/guild create §co únete a uno.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 104 | `§cYa eres miembro de un gremio. Debes abandonarlo primero.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 110 | `§cEl nombre del gremio debe tener entre 3 y 16 caracteres.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 116 | `§cEl TAG del gremio debe tener entre 2 y 5 caracteres (ej. AHJ).` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 121 | `§cYa existe un gremio con ese nombre.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 129 | `§cNo tienes suficientes Monedas del Sistema para fundar un gremio (Necesitas 1,000 Monedas).` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 159 | `§cSolo el Líder supremo puede disolver el gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 184 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 190 | `§cNo tienes permisos de Oficial para invitar nuevos miembros.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 195 | `§cEse cazador ya pertenece a un gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 222 | `§cNo tienes ninguna invitación pendiente o ha expirado.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 228 | `§cEl gremio ya no existe.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 245 | `§7Has rechazado la invitación del gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 255 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 260 | `§cEres el Líder del gremio. Transfiere el liderazgo o usa §e/guild disband §cpara disolverlo.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 268 | `§eHas abandonado el gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 281 | `§cNo tienes permisos para expulsar miembros.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 287 | `§cEse miembro no está en el gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 292 | `§cNo puedes expulsar a alguien con tu mismo rango o superior.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 304 | `§cHas sido expulsado del gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 314 | `§cSolo el Líder puede ascender miembros.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 329 | `§cEse miembro ya tiene el rango máximo antes de Líder.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 344 | `§cSolo el Líder puede degradar miembros.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 359 | `§cEse miembro ya tiene el rango más bajo.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 374 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 406 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 417 | `§cError al procesar las monedas.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 441 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 447 | `§c[Gremio] Solo los Altos Mandos (Líder u Oficiales) están autorizados a retirar fondos de la Tesorería.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 476 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 482 | `§c[Gremio] Solo los Altos Mandos (Líder u Oficiales) están autorizados a retirar fondos de la Tesorería.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 525 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 531 | `§cSolo los Altos Mandos (Líder u Oficiales) pueden reclamar territorio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 540 | `§eEste chunk ya pertenece a tu gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 573 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 579 | `§cSolo los Altos Mandos (Líder u Oficiales) pueden liberar territorio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 592 | `§cEste chunk no estaba reclamado por tu gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 601 | `§cSolo el Líder supremo puede liberar todo el territorio del gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 607 | `§e✔ Se han liberado todos los chunks reclamados del gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 620 | `§cSolo el Líder puede desbloquear bendiciones del gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 626 | `§cBendición no reconocida.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 631 | `§cEsa bendición ya está desbloqueada.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 658 | `§cSolo el Líder supremo puede establecer la Sede del gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 666 | `§a✔ Sede del gremio establecida en tus coordenadas actuales.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 667 | `§b🌀 La Sede del gremio ha sido actualizada por el Líder. Usa §e/guild home §bpara viajar.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 676 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 681 | `§cEl gremio aún no tiene una Sede establecida o faro de teletransporte.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 688 | `§cNo se pudo acceder a la dimensión de la Sede.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 695 | `§a🌀 Has sido transportado a la Sede del Gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 703 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 797 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 814 | `§a✔ [Gremio] Solicitud de retiro enviada a los Altos Mandos del gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 821 | `§cNo perteneces a ningún gremio.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 827 | `§cSolo el Líder y Sublíderes pueden adquirir el Tablón de Cacerías oficial.` |
| `com/soybe/awakeningsystem/guild/GuildSavedData.java` | 837 | `§cEl gremio no dispone de 500 Monedas (o 5,000 Moras) en la Bóveda para el Tablón.` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 346 | `§cColoca un Núcleo de Hipódromo y haz clic derecho en él para iniciar la carrera.` |
| `com/soybe/awakeningsystem/uma/screen/UmaHubScreen.java` | 348 | `§cSe requieren al menos 2 waypoints creados con la Vara Marcadora de Pista.` |

### Módulo: Worldgen, Estructuras y Dimensiones (3 mensajes)

| Archivo | Línea | Mensaje Hardcodeado |
| :--- | :--- | :--- |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 352 | `§b§l✦ [DIMENSIÓN DE TEYVAT] §f¡Bienvenido a las tierras naturales de Teyvat!` |
| `com/soybe/awakeningsystem/TeyvatWorldManager.java` | 420 | `§b§l✦ [ESPIRAL DEL ABISMO] §fHas llegado al solitario Arrecife de Musk.` |
| `com/soybe/awakeningsystem/world/TeyvatWorldGenManager.java` | 227 | `§b§l✦ [ESTATUA DE LOS SIETE] §fHas recibido la Bendición y Sanación de los Arcontes.` |

---

## 5. Muestra de Textos de Idioma (`lang`)

### 5.1. Diálogos y Frases de Compañeros y NPCs (78 entradas)

| Clave de Idioma | Español (`es_es`) | Inglés (`en_us`) |
| :--- | :--- | :--- |
| `awakening_system.chat.box_opened` | §6§l[SISTEMA]§r ¡Has abierto §e%s§r! Recompensa: §a%s§r | §6§l[SYSTEM]§r Opened §e%s§r! Reward: §a%s§r |
| `awakening_system.chat.sell_success` | §a[Sistema]§r Has vendido §e%sx %s§r por §6+%s monedas§r. | §a[System]§r Sold §e%sx %s§r for §6+%s coins§r. |
| `awakening_system.entity.mount_hua_disciple.greeting` | §d[Discípulo del Monte Hua] §f«Saludos, compañero cultivador. Las flores de ciruelo del Monte Hua florecen firmes contra la corrupción demoníaca.» | §d[Mount Hua Disciple] §f«Greetings, fellow cultivator. Mount Hua's plum blossoms bloom firm against demonic corruption.» |
| `awakening_system.guild.chat_mode_off` | §6[Gremio] §eModo de chat de gremio desactivado. Ahora hablas en el chat global. | §6[Guild] §eGuild chat mode disabled. You are now speaking in global chat. |
| `awakening_system.guild.chat_mode_on` | §6[Gremio] §aModo de chat de gremio activado. Todo lo que escribas irá solo a tu gremio. (Usa /gc para volver al global o usa #mensaje). | §6[Guild] §aGuild chat mode enabled. All your messages will be sent to your guild only. (Use /gc to return to global or use #message). |
| `awakening_system.item.celestial_dragon_pearl.chat` | §6§l✦ [PERLA CELESTIAL] §fHas asimilado el núcleo dracónico (+1,000 Qi / Salud y Energía al 100%). | §6§l✦ [CELESTIAL PEARL] §fYou assimilated the draconic core (+1,000 Qi / Health & Energy 100%). |
| `awakening_system.item.chaos_mana_core.chat` | §5§l[NÚCLEO DEL CAOS] §fHas asimilado la energía de los Cuatro Reyes Elementales (+3500 Qi / +2000 EXP). | §5§l[CHAOS CORE] §fYou assimilated the energy of the Four Elemental Sovereigns (+3500 Qi / +2000 EXP). |
| `awakening_system.npc.arthur_leywin.dialogue.0` | El maná llena el cuerpo, pero el éter rige las dimensiones mismas del espacio y la realidad. | Mana fills the body, but aether governs the very dimensions of space and reality. |
| `awakening_system.npc.arthur_leywin.dialogue.1` | No lucho por títulos ni por vanagloria; peleo para asegurar que mis seres queridos tengan un mañana. | I fight not for titles or vain glory; I fight to ensure my loved ones have a tomorrow. |
| `awakening_system.npc.arthur_leywin.dialogue.2` | Purifica tu núcleo. La claridad de tu intención determina la fuerza de tu despertar. | Purify your core. The clarity of your intent defines the strength of your awakening. |
| `awakening_system.npc.arthur_leywin.dialogue.3` | Aprende a fluir con el entorno; la fuerza bruta sin control solo engendra destrucción. | Learn to flow with your surroundings; brute force without control only breeds ruin. |
| `awakening_system.npc.arthur_leywin.dialogue_title` | §9§l✦ Arthur Leywin ✦ | §9§l✦ Arthur Leywin ✦ |
| `awakening_system.npc.black_market_dealer.dialogue.0` | Las fichas negras no dejan rastro... y la Mora no hace preguntas. | Black chips leave no trace... and Mora asks no questions. |
| `awakening_system.npc.black_market_dealer.dialogue.1` | ¿Buscas máquinas clandestinas o una partida de billar donde las apuestas no tienen techo? | Looking for clandestine machinery or a pool duel where stakes have no ceiling? |
| `awakening_system.npc.black_market_dealer.dialogue.2` | Lo que pasa en esta cripta se queda en la oscuridad del inframundo. | What happens in this crypt stays hidden in underworld shadows. |
| `awakening_system.npc.black_market_dealer.dialogue.3` | Tus ojos no vieron nada, tus manos solo contaron fichas. | Your eyes saw nothing, your hands only counted chips. |
| `awakening_system.npc.black_market_dealer.dialogue_title` | Valerius el Silencioso | Valerius the Silent |
| `awakening_system.npc.cha_haein.dialogue.0` | Tu aroma no huele como el de los demás cazadores... ¿Acaso estás conectado al Sistema? | Your scent isn't like the other hunters'... Are you connected to the System? |
| `awakening_system.npc.cha_haein.dialogue.1` | La postura de la espada requiere concentración absoluta. No vaciles en el momento decisivo. | The sword stance requires absolute focus. Never hesitate at the decisive moment. |
| `awakening_system.npc.cha_haein.dialogue.2` | No descuides tu entrenamiento físico; la agilidad lo es todo cuando se abre una Puerta Roja. | Don't neglect your physical training; agility is everything when a Red Gate opens. |
| `awakening_system.npc.cha_haein.dialogue.3` | Siempre que desenvaines tu arma, recuerda por quién estás luchando. | Whenever you draw your blade, remember who you are fighting for. |
| `awakening_system.npc.cha_haein.dialogue_title` | §e§l✦ Cha Hae-In ✦ | §e§l✦ Cha Hae-In ✦ |
| `awakening_system.npc.cha_yeonwoo.dialogue.0` | Mi hermano me dejó un diario. Cada escalón de esta Torre será conquistado y purgado. | My brother left me a pocket watch and a diary. Every floor of this Tower will be conquered and purged. |
| `awakening_system.npc.cha_yeonwoo.dialogue.1` | Las alas del Dragón Negro devoran a los falsos dioses del firmamento. | The Black Dragon's wings devour false gods of the heavens. |
| `awakening_system.npc.cha_yeonwoo.dialogue.2` | Si vas a escalar la Torre de los Deseos, prepárate para no mirar hacia atrás jamás. | If you scale the Tower of Desires, prepare never to look back. |
| `awakening_system.npc.cha_yeonwoo.dialogue.3` | La debilidad es un pecado en un mundo regido por las Constelaciones. | Weakness is a sin in a realm governed by Constellations. |
| `awakening_system.npc.cha_yeonwoo.dialogue_title` | §c§l✦ Cha Yeon-woo ✦ | §c§l✦ Cha Yeon-woo ✦ |
| `awakening_system.npc.cheon_yeowoon.dialogue.0` | Nano, escanea los meridianos de este cazador... Estructura biológica y energética aceptable. | Nano, scan this hunter's meridians... Biological and energetic structure acceptable. |
| `awakening_system.npc.cheon_yeowoon.dialogue.1` | El Culto Demoníaco Celestial no doblega su cabeza ante ningún dios ni emperador. | The Heavenly Demonic Cult bows its head to neither god nor emperor. |
| `awakening_system.npc.cheon_yeowoon.dialogue.2` | El poder absoluto no necesita excusas; se forja con precisión milimétrica y acero despiadado. | Absolute power needs no excuses; it is forged with millimeter precision and ruthless steel. |
| `awakening_system.npc.cheon_yeowoon.dialogue.3` | Corta a tu enemigo antes de que formule su primer pensamiento de traición. | Cut down your enemy before their first thought of betrayal can form. |
| `awakening_system.npc.cheon_yeowoon.dialogue_title` | §4§l✦ Cheon Yeo-woon ✦ | §4§l✦ Cheon Yeo-woon ✦ |
| `awakening_system.npc.chung_myung.dialogue.0` | ¿Dónde está el licor de ciruelo? ¡Y más importante, dónde está el dinero de los ancianos de Shaolin! | Where's the plum blossom wine? And more importantly, where's Shaolin's money?! |
| `awakening_system.npc.chung_myung.dialogue.1` | En el Monte Hua no nos rendimos, ¡golpeamos el doble de fuerte hasta que el enemigo entienda! | At Mount Hua we don't surrender; we strike twice as hard until the enemy gets it! |
| `awakening_system.npc.chung_myung.dialogue.2` | ¿Crees que entrenar 10,000 estocadas al día es duro? ¡En mis tiempos hacíamos 100,000 antes del desayuno! | Think 10,000 thrusts a day is hard? Back in my day we did 100,000 before breakfast! |
| `awakening_system.npc.chung_myung.dialogue.3` | Desenvaina tu espada. Si vas a blandir un filo, que corte a través de las dudas como el florecer de un ciruelo en invierno. | Draw your sword. If you wield a blade, cut through doubts like plum blossoms in deep winter. |
| `awakening_system.npc.chung_myung.dialogue_title` | §a§l✦ Chung Myung ✦ | §a§l✦ Chung Myung ✦ |
| `awakening_system.npc.elena_receptionist.dialogue.0` | ¡Bienvenido al Gremio de Aventureros! Consulta el Tablón de la pared para aceptar contratos de caza. | Welcome to the Adventurers Guild! Check the Bounty Board on the wall for hunting contracts. |
| `awakening_system.npc.elena_receptionist.dialogue.1` | ¿Buscas gloria y recompensas legítimas? Completa contratos para subir de Rango de Aventurero hasta el mítico Rango S. | Seeking glory and rightful rewards? Complete contracts to rank up all the way to mythical Rank S. |
| `awakening_system.npc.elena_receptionist.dialogue.2` | No olvides mantener tu equipo en las mesas de herrería de la planta alta antes de adentrarte en el bosque. | Don't forget to upkeep your gear at the upstairs smithing tables before heading into the wild. |
| `awakening_system.npc.elena_receptionist.dialogue.3` | Cada bestia que derrotas en nombre del gremio te otorga Medallas de Aventurero y prestigio en todo el reino. | Every beast slain in the guild's name earns you Adventurer Medals and realm-wide prestige. |
| `awakening_system.npc.elena_receptionist.dialogue_title` | §6§l✦ Elena ✦ | §6§l✦ Elena ✦ |
| `awakening_system.npc.javier_asrahan.dialogue.0` | El joven maestro Lloyd parece codicioso, pero sus obras siempre terminan salvando a la gente. | Young Master Lloyd looks greedy, but his projects always save people in the end. |
| `awakening_system.npc.javier_asrahan.dialogue.1` | La Espada de Asrahan protege a los inocentes y corta toda maldad. | The Sword of Asrahan protects the innocent and strikes down all evil. |
| `awakening_system.npc.javier_asrahan.dialogue.2` | Mantén la guardia alta. Un verdadero guerrero nunca baja los brazos en tierra desconocida. | Keep your guard up. A true warrior never lets down their vigilance in unknown lands. |
| `awakening_system.npc.javier_asrahan.dialogue.3` | Si necesitas ayuda con monstruos que amenazan tus tierras, cuenta con mi espada. | If you need help with monsters threatening your lands, call upon my sword. |
| `awakening_system.npc.javier_asrahan.dialogue_title` | §b§l✦ Javier Asrahan ✦ | §b§l✦ Javier Asrahan ✦ |
| `awakening_system.npc.kim_dokja.dialogue.0` | ¿Buscando una guía para el próximo escenario? Todo está escrito si sabes leer entre líneas. | Looking for a guide to the next scenario? It's all written if you read between the lines. |
| `awakening_system.npc.kim_dokja.dialogue.1` | No confíes ciegamente en las Constelaciones del Star Stream; cobran muy caro por cada milagro. | Don't blindly trust Star Stream Constellations; they charge heavily for every miracle. |
| `awakening_system.npc.kim_dokja.dialogue.2` | Este mundo no es una tragedia si luchamos hasta el último capítulo. | This world is not a tragedy if we fight to the very last chapter. |
| `awakening_system.npc.kim_dokja.dialogue.3` | Toma algunas monedas del Sistema. En este escenario, la información y los recursos lo son todo. | Take some System coins. In this scenario, information and resources are everything. |
| `awakening_system.npc.kim_dokja.dialogue_title` | §f§l✦ Kim Dokja ✦ | §f§l✦ Kim Dokja ✦ |
| `awakening_system.npc.lloyd_frontera.dialogue.0` | ¡El dinero mueve el mundo, pero el cemento, el agua y la pala lo construyen! | Money moves the world, but cement, water, and shovels build it! |
| `awakening_system.npc.lloyd_frontera.dialogue.1` | ¡Ondol caliente en invierno y acueductos limpios en verano! ¡Eso sí es verdadera calidad de vida! | Warm ondol in winter, clean aqueducts in summer! That's real quality of life! |
| `awakening_system.npc.lloyd_frontera.dialogue.2` | ¿Tienes monedas del Sistema? ¡Tengo planos, contratos y materiales de primera calidad! | Got System coins? I've got blueprints, contracts, and top-tier materials! |
| `awakening_system.npc.lloyd_frontera.dialogue.3` | ¡Javier, anota la factura de este aventurero! ¡Cada metro de tierra tiene un precio! | Javier, invoice this adventurer! Every square meter of land has its price! |
| `awakening_system.npc.lloyd_frontera.dialogue_title` | §6§l✦ Lloyd Frontera ✦ | §6§l✦ Lloyd Frontera ✦ |
| `awakening_system.npc.mount_hua.greeting` | §d[Discípulo del Monte Hua] §f«Saludos, compañero cultivador. Las flores de ciruelo del Monte Hua florecen firmes contra la corrupción demoníaca.» | §d[Mount Hua Disciple] §f«Greetings, fellow cultivator. Mount Hua's plum blossoms bloom resolute against demonic corruption.» |
| `awakening_system.npc.ryu_hanbin.dialogue.0` | Pasé más de veinte años atrapado en la Dimensión Rocosa por culpa de una guía con error... Ya nada me asusta. | I spent over twenty years trapped in the Rocky Mountain because of a glitched guideline... Nothing scares me anymore. |
| `awakening_system.npc.ryu_hanbin.dialogue.1` | ¿Mi nivel? Sigue diciendo 'Nivel 5', pero mi fuerza puede partir una montaña en dos. | My level? Still says 'Level 5', but my strength can split a mountain in two. |
| `awakening_system.npc.ryu_hanbin.dialogue.2` | No compliques las cosas. Si una bestia o un monstruo te ataca, usa una espada más grande y aplástalo. | Don't overcomplicate things. If a beast attacks you, grab a bigger sword and smash it. |
| `awakening_system.npc.ryu_hanbin.dialogue.3` | A veces extraño el pollo frito y el ramen de la Tierra... Este mundo de fantasía es un caos total. | Sometimes I miss fried chicken and ramen from Earth... This fantasy world is pure chaos. |
| `awakening_system.npc.ryu_hanbin.dialogue_title` | §c§l✦ Ryu Hanbin ✦ | §c§l✦ Ryu Hanbin ✦ |
| `awakening_system.npc.sung_jinwoo.dialogue.0` | Surge... Aquellos que caigan bajo mi mando servirán por la eternidad a las sombras. | Arise... Those who fall under my command will serve the shadows for eternity. |
| `awakening_system.npc.sung_jinwoo.dialogue.1` | ¿Te estás esforzando en tus mazmorras? El Sistema no recompensa la debilidad. | Are you pushing yourself in your dungeons? The System does not reward weakness. |
| `awakening_system.npc.sung_jinwoo.dialogue.2` | Mi ejército nunca duerme. Mantén la calma y sigue subiendo de nivel. | My army never sleeps. Stay calm and keep leveling up. |
| `awakening_system.npc.sung_jinwoo.dialogue.3` | Solo me arrodillo ante aquellos que protegen a los que no pueden luchar. | I only kneel before those who protect the ones who cannot fight. |
| `awakening_system.npc.sung_jinwoo.dialogue_title` | §5§l✦ Sung Jin-Woo ✦ | §5§l✦ Sung Jin-Woo ✦ |
| `awakening_system.npc.twenty_fifth_baam.dialogue.0` | ¿Qué es lo que buscas en la cima de la Torre? ¿Riquezas, gloria, o a las personas que te importan? | What are you seeking at the top of the Tower? Wealth, glory, or the ones you cherish? |
| `awakening_system.npc.twenty_fifth_baam.dialogue.1` | El Shinsoo no es un arma que debamos poseer; es el aliento del mundo que nos permite respirar juntos. | Shinsoo isn't a weapon to possess; it is the world's breath that allows us to live together. |
| `awakening_system.npc.twenty_fifth_baam.dialogue.2` | No permitiré que nadie vuelva a lastimar a mis compañeros. Si debo romper las reglas de la Torre, lo haré. | I won't let anyone hurt my friends again. If I must break the Tower's rules, I will. |
| `awakening_system.npc.twenty_fifth_baam.dialogue.3` | La Espina late en mi interior... Un poder que desafía el destino mismo de las familias de la Torre. | The Thorn beats within me... A power that defies the very fate of the Great Families. |
| `awakening_system.npc.twenty_fifth_baam.dialogue_title` | §b§l✦ Vigesimoquinto Baam ✦ | §b§l✦ Twenty-Fifth Baam ✦ |
| `awakening_system.npc.yoo_joonghyuk.dialogue.0` | Si eres débil, morirás antes del escenario 10. No te interpongas en mi camino. | If you are weak, you will die before scenario 10. Stay out of my path. |
| `awakening_system.npc.yoo_joonghyuk.dialogue.1` | He visto este mundo perecer cientos de veces. No repetiré los mismos errores en esta vida. | I have seen this world perish hundreds of times. I will not repeat past mistakes. |
| `awakening_system.npc.yoo_joonghyuk.dialogue.2` | ¿Crees que tu determinación es suficiente? Demuéstramelo con tu filo en el campo de batalla. | Think your resolve is enough? Prove it with your blade on the battlefield. |
| `awakening_system.npc.yoo_joonghyuk.dialogue.3` | No mueras en una mazmorra cualquiera. Una muerte patética no merece ser recordada. | Do not die in some random dungeon. A pathetic death deserves no remembrance. |
| `awakening_system.npc.yoo_joonghyuk.dialogue_title` | §8§l✦ Yoo Joonghyuk ✦ | §8§l✦ Yoo Joonghyuk ✦ |

---

## 6. Lista de Verificación y Estado de la Limpieza

- [x] **Limpieza Total de Comentarios en Java:** 6.749 líneas de comentarios técnicos en español y notas de desarrollo eliminadas en 442 archivos de `src/main/java`. Código limpio y profesional.
- [x] **Auditoría y Paridad de Traducción:** 7.307 claves en `en_us.json` y `es_es.json`. Agregadas las claves de entidades y comandos faltantes (`slash_wave`, `subway_pilot_seat`, `dart`, `only_players`, `delusion.blank_info`). 0 claves faltantes y 0 vacías.
- [x] **Seguridad de Red en Minijuegos:** Validación de distancia física (`<= 64.0D`) implementada en Blackjack, Ruleta y Billar para prevenir exploits remotos.
- [x] **Protección contra Pérdida de Fichas/Drops:** Implementado `onBlockBroken` en `SlotMachineBlockEntity` y rescate en `playerWillDestroy`.
- [x] **Protección de Desconexión:** Implementado abandono/reembolso en Blackjack PvP y reclamo pendiente en carreras Uma Musume al reconectar.
- [x] **Compilación y Empaquetado:** `./gradlew compileJava` y `./gradlew jar` ejecutados exitosamente con 0 errores, generando `awakening_system-0.3.0.jar` (105 MB).
- [ ] **Migración futura opcional de literales:** Migración de textos hardcodeados remanentes (`Component.literal`) a `Component.translatable` cuando se requieran nuevas localizaciones.
