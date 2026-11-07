package com.soybe.awakeningsystem.entity;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import com.soybe.awakeningsystem.SystemEvents;
import com.soybe.awakeningsystem.SystemItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public enum NpcProfile {
    SUNG_JINWOO(
            "sung_jinwoo",
            "Sung Jin-Woo",
            "Monarca de las Sombras",
            "Solo Leveling",
            "§5§l",
            List.of(
                    "Surjan... Aquellos que caigan bajo mi mando servirán a las sombras por la eternidad.",
                    "¿Te estás esforzando en tus mazmorras? El Sistema no recompensa la debilidad.",
                    "Mi ejército nunca duerme. Mantén la calma y continúa subiendo de nivel.",
                    "Solo me arrodillo ante aquellos que protegen a quienes no pueden luchar."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 6000, 0, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.sung_jinwoo.blessing_title", "awakening_system.npc.sung_jinwoo.blessing_desc", 0xFF8A2BE2);
        }
    },

    CHA_HAEIN(
            "cha_haein",
            "Cha Hae-In",
            "Cazadora de Rango S",
            "Solo Leveling",
            "§e§l",
            List.of(
                    "Tu aroma no es como el de los otros cazadores... ¿Estás conectado al Sistema?",
                    "La postura de la espada exige concentración absoluta. Jamás dudes en el momento decisivo.",
                    "No descuides tu entrenamiento físico; la agilidad lo es todo cuando se abre una Puerta Roja.",
                    "Siempre que desenvaines tu espada, recuerda por quién estás luchando."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.cha_haein.blessing_title", "awakening_system.npc.cha_haein.blessing_desc", 0xFFFFD700);
        }
    },

    KIM_DOKJA(
            "kim_dokja",
            "Kim Dokja",
            "Rey Demonio de la Salvación",
            "Punto de Vista del Lector Omnisciente",
            "§f§l",
            List.of(
                    "¿Buscas una guía para el próximo escenario? Todo está escrito si sabes leer entre líneas.",
                    "No confíes ciegamente en las Constelaciones del Star Stream; cobran muy caro cada milagro.",
                    "Este mundo no es una tragedia si luchamos hasta el mismísimo último capítulo.",
                    "Toma algunas monedas del Sistema. En este escenario, la información y los recursos lo son todo."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 2, false, true));
            var data = SystemEvents.data(player);
            if (data != null) {
                data.addCoins(250);
                AwakeningSystemMod.sync(player);
            }
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.2D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.kim_dokja.blessing_title", "awakening_system.npc.kim_dokja.blessing_desc", 0xFF70E8FF);
        }
    },

    YOO_JOONGHYUK(
            "yoo_joonghyuk",
            "Yoo Joonghyuk",
            "El Regresor Supremo",
            "Punto de Vista del Lector Omnisciente",
            "§8§l",
            List.of(
                    "Si eres débil, morirás antes del décimo escenario. Apártate de mi camino.",
                    "He visto este mundo perecer cientos de veces. No repetiré los errores del pasado.",
                    "¿Crees que tu determinación basta? Demuéstralo con tu espada en el campo de batalla.",
                    "No mueras en una mazmorra cualquiera. Una muerte patética no merece recuerdo."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.yoo_joonghyuk.blessing_title", "awakening_system.npc.yoo_joonghyuk.blessing_desc", 0xFFFF3333);
        }
    },

    LLOYD_FRONTERA(
            "lloyd_frontera",
            "Lloyd Frontera",
            "El Ingeniero Supremo",
            "El Mejor Diseñador Inmobiliario",
            "§6§l",
            List.of(
                    "¡El dinero mueve el mundo, pero el cemento, el agua y las palas lo construyen!",
                    "¡Ondol caliente en invierno, acueductos limpios en verano! ¡Eso sí es calidad de vida!",
                    "¿Tienes monedas del Sistema? ¡Tengo planos, contratos y materiales de primera calidad!",
                    "¡Javier, factura a este aventurero! ¡Cada metro cuadrado de terreno tiene su precio!"
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 12000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 12000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 100, 1, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.lloyd_frontera.blessing_title", "awakening_system.npc.lloyd_frontera.blessing_desc", 0xFFFFD866);
        }
    },

    JAVIER_ASRAHAN(
            "javier_asrahan",
            "Javier Asrahan",
            "El Caballero Sagrado",
            "El Mejor Diseñador Inmobiliario",
            "§b§l",
            List.of(
                    "El joven amo Lloyd parece codicioso, pero sus proyectos siempre terminan salvando vidas.",
                    "La Espada de Asrahan protege a los inocentes y aniquila todo mal.",
                    "Mantén la guardia alta. Un verdadero guerrero jamás baja la vigilancia en tierras desconocidas.",
                    "Si necesitas ayuda contra monstruos que amenacen tus tierras, cuenta con mi espada."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 3, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 25, 0.4D, 0.7D, 0.4D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.javier_asrahan.blessing_title", "awakening_system.npc.javier_asrahan.blessing_desc", 0xFF55FFFF);
        }
    },

    CHEON_YEOWOON(
            "cheon_yeowoon",
            "Cheon Yeo-woon",
            "Líder del Culto Demoníaco",
            "Nano Machine",
            "§4§l",
            List.of(
                    "Nano, escanea los meridianos de este cazador... Estructura biológica y energética aceptable.",
                    "El Culto Celestial Demoníaco no inclina la cabeza ni ante dioses ni emperadores.",
                    "El poder absoluto no necesita excusas; se forja con precisión milimétrica y acero implacable.",
                    "Elimina a tu enemigo antes de que pueda formular su primer pensamiento de traición."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.7D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.cheon_yeowoon.blessing_title", "awakening_system.npc.cheon_yeowoon.blessing_desc", 0xFFFF2222);
        }
    },

    ARTHUR_LEYWIN(
            "arthur_leywin",
            "Arthur Leywin",
            "Gobernante del Éter",
            "The Beginning After The End",
            "§9§l",
            List.of(
                    "El maná llena el cuerpo, pero el éter gobierna las dimensiones mismas del espacio y la realidad.",
                    "No lucho por títulos ni gloria vana; lucho para asegurar que mis seres queridos tengan un mañana.",
                    "Purifica tu núcleo. La claridad de tu intención define la fuerza de tu despertar.",
                    "Aprende a fluir con tu entorno; la fuerza bruta sin control solo engendra ruina."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.arthur_leywin.blessing_title", "awakening_system.npc.arthur_leywin.blessing_desc", 0xFF3388FF);
        }
    },

    CHA_YEONWOO(
            "cha_yeonwoo",
            "Cha Yeon-woo",
            "El Cazador Caín",
            "Second Life Ranker",
            "§c§l",
            List.of(
                    "Mi hermano me dejó un reloj de bolsillo y un diario. Cada piso de esta Torre será conquistado y purgado.",
                    "Las alas del Dragón Negro devoran a los falsos dioses de los cielos.",
                    "Si asciendes por la Torre de los Deseos, prepárate para no mirar jamás atrás.",
                    "La debilidad es un pecado en un reino gobernado por Constelaciones."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.7D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.cha_yeonwoo.blessing_title", "awakening_system.npc.cha_yeonwoo.blessing_desc", 0xFFCC0000);
        }
    },

    CHUNG_MYUNG(
            "chung_myung",
            "Chung Myung",
            "Dragón Divino de Mount Hua",
            "El Retorno de la Secta Mount Hua",
            "§a§l",
            List.of(
                    "¿Dónde está el vino de flor de ciruelo? Y más importante aún, ¡¿dónde está el dinero de Shaolin?!",
                    "¡En Mount Hua no nos rendimos; golpeamos el doble de fuerte hasta que el enemigo lo entienda!",
                    "¿Crees que 10.000 estocadas al día es difícil? ¡En mis tiempos hacíamos 100.000 antes de desayunar!",
                    "Desenvaina tu espada. Si empuñas una hoja, corta las dudas como flores de ciruelo en pleno invierno."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CHERRY_LEAVES, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.15D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.chung_myung.blessing_title", "awakening_system.npc.chung_myung.blessing_desc", 0xFF55FF55);
        }
    },

    RYU_HANBIN(
            "ryu_hanbin",
            "Ryu Hanbin",
            "El Rey de la Espada Valter",
            "Historia de Supervivencia de un Rey de la Espada",
            "§c§l",
            List.of(
                    "Pasé más de veinte años atrapado en la Montaña Rocosa por culpa de una guía con bugs... Ya nada me asusta.",
                    "¿Mi nivel? Sigue diciendo 'Nivel 5', pero mi fuerza puede partir una montaña en dos.",
                    "No te compliques la vida. Si una bestia te ataca, agarra una espada más grande y aplástala.",
                    "A veces extraño el pollo frito y el ramen de la Tierra... Este mundo de fantasía es un caos total."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 3, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.5D, player.getZ(), 2, 0.3D, 0.3D, 0.3D, 0.0D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.ryu_hanbin.blessing_title", "awakening_system.npc.ryu_hanbin.blessing_desc", 0xFFFF3333);
        }
    },

    TWENTY_FIFTH_BAAM(
            "twenty_fifth_baam",
            "Vigesimoquinto Baam",
            "El Irregular de la Torre",
            "Tower of God",
            "§b§l",
            List.of(
                    "¿Qué buscas en la cima de la Torre? ¿Riquezas, gloria o a aquellos que amas?",
                    "El Shinsoo no es un arma que poseer; es el aliento del mundo que nos permite existir juntos.",
                    "No permitiré que nadie vuelva a herir a mis amigos. Si debo romper las reglas de la Torre, lo haré.",
                    "La Espina pulsa dentro de mí... Un poder capaz de desafiar el mismísimo destino de las Grandes Familias."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 6000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.15D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.twenty_fifth_baam.blessing_title", "awakening_system.npc.twenty_fifth_baam.blessing_desc", 0xFF00FFFF);
        }
    },

    ELENA_RECEPTIONIST(
            "elena_receptionist",
            "Elena",
            "Recepcionista del Gremio",
            "Gremio de Aventureros",
            "§6§l",
            List.of(
                    "¡Bienvenida al Gremio de Aventureros! Consulta el Tablón de Misiones en la pared para contratos de cacería.",
                    "¿Buscas gloria y recompensas? Completa contratos para ascender de rango hasta el mítico Rango S.",
                    "No olvides reparar y mejorar tu equipo en las mesas de herrería antes de aventurarte en tierras salvajes.",
                    "Cada bestia mágica eliminada a nombre del gremio te otorga Medallas de Aventurero y prestigio en el reino."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.elena_receptionist.blessing_title", "awakening_system.npc.elena_receptionist.blessing_desc", 0xFFFFD700);
        }
    },

    BLACK_MARKET_DEALER(
            "black_market_dealer",
            "Valerio el Silencioso",
            "Traficante del Mercado Negro",
            "Bajos Fondos Clandestinos",
            "§5§l",
            List.of(
                    "Las fichas negras no dejan rastro... y las Monedas no hacen preguntas.",
                    "¿Buscas maquinaria clandestina o un duelo de billar donde las apuestas no tienen techo?",
                    "Lo que ocurre en esta cripta se queda oculto en las sombras del bajo mundo.",
                    "Tus ojos no vieron nada; tus manos solo contaron fichas."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 3600, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 6000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.6D, 0.4D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.black_market_dealer.blessing_title", "awakening_system.npc.black_market_dealer.blessing_desc", 0xFFA855F7);
        }
    },

    CASINO_RECEPTIONIST(
            "casino_receptionist",
            "Madame Valerie",
            "Recepcionista y Anfitriona VIP",
            "Gran Casino Real",
            "§6§l",
            List.of(
                    "¡Bienvenido al Gran Casino Real de Teyvat! Aquí la fortuna y el glamour convergen bajo luces doradas.",
                    "¿Deseas probar tu suerte en las mesas de altas apuestas? Obtén tu Pase VIP para el salón exclusivo.",
                    "En nuestro mostrador puedes cambiar Monedas por Fichas Oficiales y solicitar tu acreditación VIP.",
                    "Recuerda que en el Blackjack VIP y la Ruleta VIP solo se juegan los tesoros más codiciados de todo el reino."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 12000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 12000, 0, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.casino_receptionist.blessing_title", "awakening_system.npc.casino_receptionist.blessing_desc", 0xFFFFD700);
        }
    },

    RAIDEN_SHOGUN(
            "raiden_shogun",
            "Raiden Shogun",
            "Arconte Electro",
            "Genshin Impact",
            "§5§l",
            List.of(
                    "El trueno ruge y las ilusiones se desvanecen. Solo la Eternidad permanece inmutable.",
                    "¿Buscas desafiar los decretos celestiales? Demuestra la firmeza de tu voluntad.",
                    "No temas al relámpago; teme a la vacilación en el instante del juicio supremo.",
                    "La Luz del Segador guiará tu camino a través de la tormenta."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.raiden_shogun.blessing_title", "awakening_system.npc.raiden_shogun.blessing_desc", 0xFF9933FF);
        }
    },

    ZHONGLI(
            "zhongli",
            "Zhongli",
            "Rex Lapis • Arconte Geo",
            "Genshin Impact",
            "§6§l",
            List.of(
                    "Los contratos se graban en piedra y oro. Quienes rompen sus promesas sufren la Ira de la Roca.",
                    "El vino de osmanthus sabe igual que en mis recuerdos... pero ¿dónde están aquellos que compartieron la memoria?",
                    "La firmeza de un escudo no reside en el mineral, sino en la calma inquebrantable del espíritu.",
                    "Camina con pasos firmes; la historia jamás olvida a quienes forjan su propio destino."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 8000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 8000, 4, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.zhongli.blessing_title", "awakening_system.npc.zhongli.blessing_desc", 0xFFFFD700);
        }
    },

    HUTAO(
            "hu_tao",
            "Hu Tao",
            "Directora de la Funeraria El Camino",
            "Genshin Impact",
            "§c§l",
            List.of(
                    "¡Aiya! ¡Cliente a la vista! ¿Necesitas cupones funerarios? ¡Compra dos y llévate el tercero gratis!",
                    "Hay una línea delgada entre la vida y la muerte... ¡así que asegúrate de disfrutar cada paso!",
                    "Las mariposas espirituales danzan en la noche; el Báculo de Homa purifica las almas inquietas.",
                    "¡No te preocupes por el peligro! Si caes en combate, en la Funeraria te cuidaremos muy bien, ¡jaja!"
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.4D, 0.7D, 0.4D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.hu_tao.blessing_title", "awakening_system.npc.hu_tao.blessing_desc", 0xFFFF4500);
        }
    },

    GANYU(
            "ganyu",
            "Ganyu",
            "Secretaria General del Qixing",
            "Genshin Impact",
            "§b§l",
            List.of(
                    "El rocío del amanecer calma el corazón... No debemos retrasar los deberes pendientes de Liyue.",
                    "El Arco de Amos susurra con el viento gélido; un disparo certero disipa cualquier amenaza.",
                    "¿Has descansado lo suficiente? El sobreesfuerzo nubla la puntería de un cazador.",
                    "Por la paz de los mortales y el pacto milenario de los Adeptus."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.5D, 0.8D, 0.5D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.ganyu.blessing_title", "awakening_system.npc.ganyu.blessing_desc", 0xFF80D8FF);
        }
    },

    NEUVILLETTE(
            "neuvillette",
            "Neuvillette",
            "Juez Supremo de Fontaine",
            "Genshin Impact",
            "§9§l",
            List.of(
                    "Las mareas de Fontaine limpian toda mancha de iniquidad. Que la justicia guíe tus juicios.",
                    "El agua guarda memorias, y los cielos lloran cuando la balanza de la verdad se quiebra.",
                    "¿Escuchas la lluvia? Es el juicio del dragón purificando la tierra.",
                    "Mantén la compostura ante el caos. El orden primordial prevalecerá."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 12000, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 6000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.6D, 0.8D, 0.6D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.neuvillette.blessing_title", "awakening_system.npc.neuvillette.blessing_desc", 0xFF00B0FF);
        }
    },

    MARCH_7TH(
            "march_7th",
            "Siete de Marzo",
            "Arquera del Expreso Astral",
            "Honkai: Star Rail",
            "§d§l",
            List.of(
                    "¡Mira qué pose! ¡Sonríe para la cámara del Expreso Astral antes de que comience el combate!",
                    "¡Una flecha de hielo hexagonal para cada monstruo que intente arruinar nuestro viaje!",
                    "Viajar entre las estrellas es emocionante, ¡pero siempre hay que estar preparados para la aventura!",
                    "¡No te quedes atrás! ¡El próximo mundo nos está esperando!"
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3600, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.march_7th.blessing_title", "awakening_system.npc.march_7th.blessing_desc", 0xFFFF80DF);
        }
    },

    ACHERON(
            "acheron",
            "Acheron",
            "Emanadora de la Nihilidad",
            "Honkai: Star Rail",
            "§5§l",
            List.of(
                    "He caminado junto a incontables ríos carmesíes... Pasado y futuro se desvanecen en la nada.",
                    "La lluvia nunca cesa en este sendero. Desenvaina tu hoja solo si estás dispuesto a cargar con el vacío.",
                    "Los sueños y la vigilia se entrelazan. Recuerda tu nombre antes de cruzar el horizonte.",
                    "No temas a la oscuridad; incluso en la más profunda nihilidad, una chispa fugaz persiste."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 45, 0.6D, 0.8D, 0.6D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.acheron.blessing_title", "awakening_system.npc.acheron.blessing_desc", 0xFF8A2BE2);
        }
    },

    HOSHIMI_MIYABI(
            "hoshimi_miyabi",
            "Hoshimi Miyabi",
            "Jefa de la Sección 6",
            "Zenless Zone Zero",
            "§3§l",
            List.of(
                    "Sección 6 en posición. Toda distorsión etérea dentro de los Huecos será erradicada.",
                    "La Hoja Shunguang no vacila. La velocidad de corte supera al propio pensamiento.",
                    "Mantén la serenidad ante los etéreos. La disciplina es el arma más afilada de un operativo.",
                    "Misión recibida. Procediendo con la neutralización inmediata del objetivo."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 2, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0D, player.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.0D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.hoshimi_miyabi.blessing_title", "awakening_system.npc.hoshimi_miyabi.blessing_desc", 0xFF00CED1);
        }
    },

    ELLEN_JOE(
            "ellen_joe",
            "Ellen Joe",
            "Limpiadora de Victoria Housekeeping",
            "Zenless Zone Zero",
            "§b§l",
            List.of(
                    "Uff... ¿Más horas extras? Solo quiero mis dulces de café y una buena siesta.",
                    "Si te interpones entre la salida y yo, te haré trizas antes de que termine mi turno.",
                    "Victoria Housekeeping no deja cabos sueltos ni manchas en la alfombra.",
                    "Terminemos esto rápido... Odio que el trabajo interrumpa mi descanso."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 8000, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 120, 1, false, false));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 1.0D, player.getZ(), 30, 0.5D, 0.6D, 0.5D, 0.1D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.ellen_joe.blessing_title", "awakening_system.npc.ellen_joe.blessing_desc", 0xFF1E90FF);
        }
    },

    DILUC(
            "diluc",
            "Diluc Ragnvindr",
            "Magnate del Amanecer",
            "Genshin Impact",
            "§c§l",
            List.of(
                    "Bienvenido al Viñedo del Amanecer. ¿Una copa de jugo de uva antes de seguir tu camino?",
                    "Los Caballeros de Favonius son demasiado lentos. Ciertas amenazas solo se neutralizan desde las sombras.",
                    "El fuego del amanecer nunca vacila. Jamás bajes la guardia cuando caiga la noche sobre Teyvat.",
                    "Si buscas información o descanso en estas tierras, las puertas de este viñedo permanecen abiertas."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0D, player.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.05D);
                sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.2D, player.getZ(), 15, 0.3D, 0.5D, 0.3D, 0.02D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.diluc.blessing_title", "awakening_system.npc.diluc.blessing_desc", 0xFFFF4500);
        }
    },

    HUNTER_EVALUATOR(
            "hunter_evaluator",
            "Inspector de la Asociación",
            "Evaluador Oficial",
            "Solo Leveling",
            "§b§l",
            List.of(
                    "Saludos, Cazador. Soy el Inspector de Evaluación de la Asociación de Cazadores.",
                    "El maná acumulado en tu núcleo determina el rango que el mundo te otorga.",
                    "Si has alcanzado tu límite de nivel, realiza la Evaluación Oficial para romper el sello.",
                    "El deber de un cazador es proteger a la humanidad de los horrores tras los Portales."
            )
    ) {
        @Override
        public void applyBlessing(ServerPlayer player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 3000, 1, false, true));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 35, 0.4D, 0.6D, 0.4D, 0.05D);
            }
            AwakeningSystemMod.notify(player, "awakening_system.npc.hunter_evaluator.blessing_title", "awakening_system.npc.hunter_evaluator.blessing_desc", 0xFF00E5FF);
        }
    };

    private final String id;
    private final String displayName;
    private final String title;
    private final String series;
    private final String titleColor;
    private final List<String> dialogues;

    NpcProfile(String id, String displayName, String title, String series, String titleColor, List<String> dialogues) {
        this.id = id;
        this.displayName = displayName;
        this.title = title;
        this.series = series;
        this.titleColor = titleColor;
        this.dialogues = dialogues;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String title() { return title; }
    public String series() { return series; }
    public String titleColor() { return titleColor; }
    public Component getTitleComponent() {
        return Component.translatable("awakening_system.npc." + id + ".title");
    }

    public Component getDisplayNameComponent() {
        return Component.translatable("entity.awakening_system." + id);
    }

    public Component getDialogueComponent(int index) {
        return Component.translatable("awakening_system.npc." + id + ".dialogue." + index);
    }

    public List<String> dialogues() { return dialogues; }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/npc/" + id + ".png");
    }

    public String getRandomDialogue() {
        if (dialogues.isEmpty()) return "Greetings, hunter.";
        int idx = (int) (Math.random() * dialogues.size());
        return dialogues.get(idx);
    }

    public abstract void applyBlessing(ServerPlayer player);

    public ItemStack getSignatureWeapon() {
        return switch (this) {
            case SUNG_JINWOO -> SystemItems.SHADOW_DAGGERS.isBound() ? new ItemStack(SystemItems.SHADOW_DAGGERS.get()) : ItemStack.EMPTY;
            case CHA_HAEIN -> SystemItems.RADIANT_LIGHT_SWORD.isBound() ? new ItemStack(SystemItems.RADIANT_LIGHT_SWORD.get()) : ItemStack.EMPTY;
            case JAVIER_ASRAHAN -> SystemItems.ASRAHAN_HOLY_SWORD.isBound() ? new ItemStack(SystemItems.ASRAHAN_HOLY_SWORD.get()) : ItemStack.EMPTY;
            case CHEON_YEOWOON -> SystemItems.SKY_DEMON_SWORD.isBound() ? new ItemStack(SystemItems.SKY_DEMON_SWORD.get()) : ItemStack.EMPTY;
            case CHA_YEONWOO -> SystemItems.BLACK_DRAGON_SCYTHE.isBound() ? new ItemStack(SystemItems.BLACK_DRAGON_SCYTHE.get()) : ItemStack.EMPTY;
            case CHUNG_MYUNG -> SystemItems.PLUM_BLOSSOM_SWORD.isBound() ? new ItemStack(SystemItems.PLUM_BLOSSOM_SWORD.get()) : ItemStack.EMPTY;
            case RYU_HANBIN -> SystemItems.GIGANTIC_GREATSWORD.isBound() ? new ItemStack(SystemItems.GIGANTIC_GREATSWORD.get()) : ItemStack.EMPTY;
            case TWENTY_FIFTH_BAAM -> SystemItems.TRANSCENDENT_THORN.isBound() ? new ItemStack(SystemItems.TRANSCENDENT_THORN.get()) : ItemStack.EMPTY;
            case RAIDEN_SHOGUN -> SystemItems.ENGULFING_LIGHTNING.isBound() ? new ItemStack(SystemItems.ENGULFING_LIGHTNING.get()) : ItemStack.EMPTY;
            case ZHONGLI -> SystemItems.VORTEX_VANQUISHER.isBound() ? new ItemStack(SystemItems.VORTEX_VANQUISHER.get()) : ItemStack.EMPTY;
            case HUTAO -> SystemItems.STAFF_OF_HOMA.isBound() ? new ItemStack(SystemItems.STAFF_OF_HOMA.get()) : ItemStack.EMPTY;
            case GANYU -> SystemItems.AMOS_BOW.isBound() ? new ItemStack(SystemItems.AMOS_BOW.get()) : ItemStack.EMPTY;
            case MARCH_7TH -> SystemItems.ASTRAL_ARCHERS_BOW.isBound() ? new ItemStack(SystemItems.ASTRAL_ARCHERS_BOW.get()) : ItemStack.EMPTY;
            case HOSHIMI_MIYABI -> SystemItems.SHUNGUANG_SWORD.isBound() ? new ItemStack(SystemItems.SHUNGUANG_SWORD.get()) : ItemStack.EMPTY;
            case DILUC -> SystemItems.WOLFS_GRAVESTONE.isBound() ? new ItemStack(SystemItems.WOLFS_GRAVESTONE.get()) : ItemStack.EMPTY;
            case BLACK_MARKET_DEALER -> SystemItems.CASINO_CHIP_BLACK.isBound() ? new ItemStack(SystemItems.CASINO_CHIP_BLACK.get()) : ItemStack.EMPTY;
            case CASINO_RECEPTIONIST -> SystemItems.CASINO_VIP_PASS.isBound() ? new ItemStack(SystemItems.CASINO_VIP_PASS.get()) : ItemStack.EMPTY;
            default -> ItemStack.EMPTY;
        };
    }

    public net.minecraft.sounds.SoundEvent getVoiceSound() {
        return switch (this) {
            case HUNTER_EVALUATOR -> null;
            default -> null;
        };
    }

    public static NpcProfile getById(String id) {
        if (id == null) return SUNG_JINWOO;
        String clean = id.toLowerCase(Locale.ROOT).trim();
        for (NpcProfile p : values()) {
            if (p.id.equals(clean)) return p;
        }
        return SUNG_JINWOO;
    }
}
