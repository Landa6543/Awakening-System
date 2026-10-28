# Diseno: Awakening System

## Concepto

Awakening System mezcla tres pilares:

- Progresion intensa inspirada en historias de cazadores y despertares.
- Equilibrio RPG basado en elecciones, requisitos y limites de equipamiento.
- Una narrativa en la que el Sistema observa, evalua y oculta sus intenciones.

El jugador no elige una clase al comenzar. El Sistema estudia su conducta y le ofrece habilidades, evoluciones y clases compatibles con su forma real de jugar.

## Bucle principal

1. El jugador realiza actividades normales de Minecraft.
2. El Sistema registra acciones, contexto, riesgo y constancia.
3. Las acciones aumentan afinidades ocultas.
4. Combinaciones significativas activan condiciones de habilidad.
5. El jugador adquiere, rechaza o transforma habilidades.
6. Las habilidades abren misiones, titulos, clases y eventos narrativos.
7. El crecimiento modifica la manera en que el jugador afronta el mundo.

## Datos observados

El Sistema puede registrar:

- Bloques minados, material, herramienta, profundidad y tiempo bajo tierra.
- Enemigos derrotados, arma, distancia, vida restante y diferencia de poder.
- Danio recibido, bloqueado, evitado y tipo de fuente.
- Distancia recorrida corriendo, nadando, escalando o a baja salud.
- Objetos fabricados, fundidos, encantados y reparados.
- Cultivos, animales, pociones, pesca y exploracion.
- Condiciones ambientales como oscuridad, lluvia, lava o dimensiones.
- Secuencias poco comunes que puedan representar una hazana.

Repetir una accion facil aumenta afinidad, pero las habilidades de rareza alta requieren riesgo, variedad o condiciones especiales. Esto evita premiar granjas AFK con poderes legendarios.

## Afinidades

Afinidades implementadas:

- Mineria
- Combate cuerpo a cuerpo
- Combate a distancia
- Supervivencia
- Movilidad
- Percepcion
- Profundidad
- Fuego
- Naturaleza
- Artesania
- Alquimia
- Proteccion

Normalmente el jugador vera descripciones narrativas en vez de los numeros exactos. El comando de depuracion del prototipo si muestra los valores.

## Generacion procedural controlada

Las habilidades no generan codigo arbitrario. Se construyen a partir de componentes programados y equilibrados:

```text
Habilidad = Arquetipo + Disparador + Efecto + Modificador + Coste + Limitacion
```

Ejemplo:

```text
Arquetipo: Mineria
Disparador: Romper un mineral
Efecto: Afectar bloques conectados
Modificador: Solo el mismo mineral
Coste: Resistencia por bloque
Limitacion: Numero maximo segun dominio
Resultado: Resonancia Mineral
```

Este sistema permite muchas combinaciones sin perder control sobre rendimiento, balance o compatibilidad multijugador.

## Ejemplos de habilidades

### Mineria

- Golpe de Cantero: celeridad de mineria al romper piedra.
- Instinto Mineral: posibilidad de percibir minerales cercanos.
- Resonancia Mineral: extrae parte de una veta conectada pagando resistencia.
- Pulmon de las Profundidades: reduce el agotamiento bajo tierra.
- Sentido del Magma: advierte sobre lava oculta a corta distancia.

### Combate

- Instinto de Combate: mejora del dano cuerpo a cuerpo al enfrentarse repetidamente al mismo tipo de enemigo.
- Corte Ascendente: ataque activo con control ligero.
- Ejecucion: bonificacion limitada contra enemigos con poca vida.
- Lectura del Adversario: recompensa bloquear o esquivar con buen tiempo.
- Ultimo Aliento: evita una muerte bajo condiciones estrictas y con gran enfriamiento.

### Hibridas

- Mineria + Percepcion = Eco del Prospector.
- Mineria + Fuego = Corazon de la Fragua.
- Espada + Movilidad = Destello Cortante.
- Arco + Sigilo + Noche = Cazador de la Luna Negra.
- Proteccion + Aldeas = Juramento del Guardian.

## Evolucion y fusion

Las habilidades ganan dominio al usarse correctamente, no solo al activarse. Pueden evolucionar:

```text
Instinto Mineral I -> Instinto Mineral V -> Eco del Prospector -> Dominio de las Profundidades
```

Tambien pueden fusionarse. Una fusion requiere dos habilidades compatibles, dominio minimo y una mision o hazana. Algunas decisiones seran irreversibles para que las configuraciones de personajes sean distintas.

En el prototipo, cada habilidad puede evolucionar de nivel V a nivel VI con 250 de afinidad, 3 puntos de atributo y un enfriamiento de 30 segundos. El nivel VI activa un efecto ampliado y cambia el nombre y la descripcion de la ficha.

## Rarezas

- Comun
- Inusual
- Rara
- Epica
- Legendaria
- Mitica
- Unica

La rareza depende de dificultad, contexto, creatividad, riesgo y consecuencias narrativas. Una cantidad enorme de acciones comunes no garantiza una habilidad mitica.

## Atributos

- Fuerza: dano fisico y potencia.
- Vitalidad: vida, resistencia y recuperacion.
- Agilidad: movilidad, esquiva y velocidad de ataque.
- Percepcion: precision, deteccion y lectura del entorno.
- Inteligencia: mana, conocimiento y habilidades complejas.
- Voluntad: control, resistencia mental y recuperacion de recursos.

El jugador recibe tres puntos al subir de nivel. Cada atributo tiene rendimientos decrecientes: cada punto adicional vale un 92 % del anterior. El servidor aplica los modificadores mediante UUID estables y los reaplica al iniciar sesion y al revivir.

## Limites de habilidades

El personaje puede descubrir muchas habilidades, pero equipa una cantidad reducida:

- 3 ranuras activas iniciales.
- 4 ranuras pasivas iniciales.
- 1 ranura unica.

Los rangos altos pueden ampliar estas cantidades con moderacion. Cambiar una configuracion solo sera posible fuera de combate o en puntos seguros. El prototipo actual no limita ranuras y todas las habilidades pasivas estan activas.

## Clases emergentes

Las clases son conclusiones del Sistema, no elecciones iniciales. Ejemplo:

```text
Afinidades dominantes:
- Mineria
- Supervivencia
- Combate pesado

Clases compatibles:
- Excavador de Guerra
- Guardian de las Profundidades
- Forjador Runico
```

El jugador puede aceptar, rechazar o esperar. Esperar abre otras posibilidades, pero retrasa bonificaciones de clase.

El prototipo muestra una clase emergente segun la afinidad dominante: Excavador, Luchador, Sabio, Caminante o En Observacion.

## Misiones

Tipos previstos:

- Diarias
- Crecimiento
- Emergencia
- Evolucion
- Clase
- Secreta
- Narrativa
- Penalizacion
- Rango
- Mazmorra

Las misiones se adaptan a las afinidades. Las penalizaciones obligatorias se reservan para momentos narrativos; no deben convertir cada sesion en una lista frustrante de tareas.

Implementadas en el prototipo:

- Despertar: minar 16 bloques y derrotar 5 criaturas; recompensa 3 puntos de atributo.
- Diaria: extraer 32 bloques en un dia de Minecraft; recompensa 30 EXP.
- Emergencia: sobrevivir 3 veces por debajo del 25 % de vida y recuperarse por encima del 50 %; recompensa 1 punto de atributo.
- Evolucion: llevar una habilidad a nivel VI; recompensa 2 puntos de atributo y primer mensaje narrativo.
- Secreta: descubrir mineral de diamante tras el despertar; recompensa 75 EXP.

## Narrativa

El Sistema funciona como personaje. Al principio parece una interfaz neutral, pero gradualmente muestra prioridades, errores y restricciones.

Preguntas centrales:

- Quien creo el Sistema.
- Por que eligio al jugador.
- Que representan los rangos.
- Por que aparecen mazmorras.
- Que obtiene el Sistema del crecimiento del usuario.
- Que ocurre al desobedecer o romper sus reglas.

Mensajes de ejemplo:

```text
[OBSERVACION]
El comportamiento del usuario contradice el patron previsto.
Generando una recompensa alternativa...
```

```text
[INFORMACION RESTRINGIDA]
Alcanza el Rango C para solicitar autorizacion.
```

```text
[EVENTO EXCEPCIONAL]
Has protegido una aldea pese a una probabilidad minima de supervivencia.
Nueva afinidad detectada: Guardian.
```

El prototipo incluye el primer latido narrativo: al completar la mision de evolucion, el Sistema anuncia que desbloquea la ruta evolutiva.

## Balance

- El servidor es la autoridad de todos los datos y desbloqueos.
- Las acciones repetitivas tienen rendimientos decrecientes.
- Las acciones automatizadas no cuentan o aportan progreso minimo.
- Las habilidades fuertes tienen coste, enfriamiento o condiciones.
- Los efectos se definen mediante datos cuando sea viable.
- Las habilidades deben ser utiles sin invalidar encantamientos, pociones o exploracion vanilla.
- El escalado de enemigos se limita a contenido del Sistema y eventos concretos, evitando arruinar mundos compartidos.

## Arquitectura tecnica

Base actual:

- NeoForge 21.1.249 para Minecraft 1.21.1.
- Java 21 mediante Gradle toolchains.
- `SystemData` persistido como attachment por jugador.
- Eventos de servidor para mineria, muertes, dano, crafteo, pociones, profundidad y movilidad.
- Paquetes de red para sincronizar la vista segura de datos y mostrar notificaciones por clave de traduccion.
- Comandos para consultar progresion y gastar puntos.

Arquitectura prevista:

- Registro de definiciones de habilidad mediante JSON/datapacks.
- Motor de condiciones que evalua afinidades y contexto.
- Gestor de misiones y recompensas del lado servidor.
- Pruebas para formulas de experiencia, condiciones y serializacion.

## Estado implementado

La version actual incluye la base jugable de las fases 1 a 3:

- Los atributos se aplican al jugador: Fuerza aumenta dano, Vitalidad vida maxima, Agilidad velocidad, Percepcion suerte, Inteligencia velocidad de ataque y Voluntad resistencia al retroceso, con rendimientos decrecientes.
- Las afinidades permanecen ocultas y tienen rendimiento reducido tras repetir mucho la misma actividad.
- Hay doce habilidades desbloqueables, con dominio persistente, evolucion hasta nivel VI y efectos funcionales.
- Las fichas de habilidad muestran nombre, descripcion, efecto y progreso de dominio.
- La pantalla es bilingue en espanol e ingles; el servidor conserva la autoridad de datos y efectos.
- Las misiones de despertar, diaria, emergencia, evolucion y secreta estan activas.
- La pestana de Clases muestra titulo dinamico y clase emergente.

### Fase 1: nucleo jugable

- Datos persistentes.
- Nivel, experiencia y atributos.
- Seguimiento basico de acciones.
- Primeros desbloqueos.
- Comandos de depuracion.

### Fase 2: interfaz

- Tecla configurable para abrir el Sistema.
- Pantalla translucida implementada en el prototipo.
- Panel de estado con estetica azul oscura y cian.
- Barra de experiencia, atributos y habilidades.
- Sincronizacion servidor-cliente para mostrar datos reales en multijugador.
- Notificaciones animadas con claves de traduccion.
- Gasto seguro de puntos desde la interfaz.

### Fase 3: habilidades

- Definiciones externas.
- Ranuras activas y pasivas.
- Costes, enfriamientos y dominio.
- Veinte componentes combinables.
- Entre quince y veinte habilidades iniciales.

### Fase 4: contenido narrativo

- Despertar inicial.
- Misiones dinamicas.
- Rangos de cazador.
- Primera mazmorra y jefe.
- Titulos y condiciones ocultas.

### Fase 5: especializacion

- Evoluciones y fusiones.
- Clases emergentes.
- Equilibrio multijugador.
- Configuracion para servidores y modpacks.

## Prototipo actual

En la version 0.1.0:

- Minar suma afinidad de Mineria y experiencia.
- Los minerales otorgan mas experiencia y desarrollan Percepcion.
- Derrotar criaturas suma Combate y experiencia segun su vida.
- Recibir dano suma Supervivencia.
- Permanecer bajo Y=32 suma Profundidad.
- Correr suma Movilidad.
- Disparar suma Combate a distancia.
- Recibir dano de fuego suma Fuego.
- Bloquear dano suma Proteccion.
- Craftear suma Artesania.
- Beber pociones suma Alquimia.
- Cosechar cultivos suma Naturaleza.
- Alcanzar 100 de afinidad desbloquea la habilidad asociada.
- `/system` muestra nivel, atributos, afinidades y habilidades.
- `/system attribute <nombre>` gasta un punto de atributo.

Los doce desbloqueos son funcionales y tienen efectos reales en el juego, con versiones evolucionadas a nivel VI.
