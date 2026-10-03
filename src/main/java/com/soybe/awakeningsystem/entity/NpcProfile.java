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
                    "Surge... Aquellos que caigan bajo mi mando servirán por la eternidad a las sombras.",
                    "¿Te estás esforzando en tus mazmorras? El Sistema no recompensa la debilidad.",
                    "Mi ejército nunca duerme. Mantén la calma y sigue subiendo de nivel.",
                    "Solo me arrodillo ante aquellos que protegen a los que no pueden luchar."
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
            "Cazadora Rango S",
            "Solo Leveling",
            "§e§l",
            List.of(
                    "Tu aroma no huele como el de los demás cazadores... ¿Acaso estás conectado al Sistema?",
                    "La postura de la espada requiere concentración absoluta. No vaciles en el momento decisivo.",
                    "No descuides tu entrenamiento físico; la agilidad lo es todo cuando se abre una Puerta Roja.",
                    "Siempre que desenvaines tu arma, recuerda por quién estás luchando."
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
            "Omniscient Reader's Viewpoint",
            "§f§l",
            List.of(
                    "¿Buscando una guía para el próximo escenario? Todo está escrito si sabes leer entre líneas.",
                    "No confíes ciegamente en las Constelaciones del Star Stream; cobran muy caro por cada milagro.",
                    "Este mundo no es una tragedia si luchamos hasta el último capítulo.",
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
            "Omniscient Reader's Viewpoint",
            "§8§l",
            List.of(
                    "Si eres débil, morirás antes del escenario 10. No te interpongas en mi camino.",
                    "He visto este mundo perecer cientos de veces. No repetiré los mismos errores en esta vida.",
                    "¿Crees que tu determinación es suficiente? Demuéstramelo con tu filo en el campo de batalla.",
                    "No mueras en una mazmorra cualquiera. Una muerte patética no merece ser recordada."
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
            "The Greatest Estate Developer",
            "§6§l",
            List.of(
                    "¡El dinero mueve el mundo, pero el cemento, el agua y la pala lo construyen!",
                    "¡Ondol caliente en invierno y acueductos limpios en verano! ¡Eso sí es verdadera calidad de vida!",
                    "¿Tienes monedas del Sistema? ¡Tengo planos, contratos y materiales de primera calidad!",
                    "¡Javier, anota la factura de este aventurero! ¡Cada metro de tierra tiene un precio!"
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
            "El Caballero Santo",
            "The Greatest Estate Developer",
            "§b§l",
            List.of(
                    "El joven maestro Lloyd parece codicioso, pero sus obras siempre terminan salvando a la gente.",
                    "La Espada de Asrahan protege a los inocentes y corta toda maldad.",
                    "Mantén la guardia alta. Un verdadero guerrero nunca baja los brazos en tierra desconocida.",
                    "Si necesitas ayuda con monstruos que amenazan tus tierras, cuenta con mi espada."
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
            "Señor del Culto Demoníaco",
            "Nano Machine",
            "§4§l",
            List.of(
                    "Nano, escanea los meridianos de este cazador... Estructura biológica y energética aceptable.",
                    "El Culto Demoníaco Celestial no doblega su cabeza ante ningún dios ni emperador.",
                    "El poder absoluto no necesita excusas; se forja con precisión milimétrica y acero despiadado.",
                    "Corta a tu enemigo antes de que formule su primer pensamiento de traición."
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
                    "El maná llena el cuerpo, pero el éter rige las dimensiones mismas del espacio y la realidad.",
                    "No lucho por títulos ni por vanagloria; peleo para asegurar que mis seres queridos tengan un mañana.",
                    "Purifica tu núcleo. La claridad de tu intención determina la fuerza de tu despertar.",
                    "Aprende a fluir con el entorno; la fuerza bruta sin control solo engendra destrucción."
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
                    "Mi hermano me dejó un diario. Cada escalón de esta Torre será conquistado y purgado.",
                    "Las alas del Dragón Negro devoran a los falsos dioses del firmamento.",
                    "Si vas a escalar la Torre de los Deseos, prepárate para no mirar hacia atrás jamás.",
                    "La debilidad es un pecado en un mundo regido por las Constelaciones."
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
            "Dragón Divino del Monte Hua",
            "Return of the Mount Hua Sect",
            "§a§l",
            List.of(
                    "¿Dónde está el licor de ciruelo? ¡Y más importante, dónde está el dinero de los ancianos de Shaolin!",
                    "En el Monte Hua no nos rendimos, ¡golpeamos el doble de fuerte hasta que el enemigo entienda!",
                    "¿Crees que entrenar 10,000 estocadas al día es duro? ¡En mis tiempos hacíamos 100,000 antes del desayuno!",
                    "Desenvaina tu espada. Si vas a blandir un filo, que corte a través de las dudas como el florecer de un ciruelo en invierno."
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
            "Survival Story of a Sword King",
            "§c§l",
            List.of(
                    "Pasé más de veinte años atrapado en la Dimensión Rocosa por culpa de una guía con error... Ya nada me asusta.",
                    "¿Mi nivel? Sigue diciendo 'Nivel 5', pero mi fuerza puede partir una montaña en dos.",
                    "No compliques las cosas. Si una bestia o un monstruo te ataca, usa una espada más grande y aplástalo.",
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
                    "¿Qué es lo que buscas en la cima de la Torre? ¿Riquezas, gloria, o a las personas que te importan?",
                    "El Shinsoo no es un arma que debamos poseer; es el aliento del mundo que nos permite respirar juntos.",
                    "No permitiré que nadie vuelva a lastimar a mis compañeros. Si debo romper las reglas de la Torre, lo haré.",
                    "La Espina late en mi interior... Un poder que desafía el destino mismo de las familias de la Torre."
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
                    "¡Bienvenido al Gremio de Aventureros! Consulta el Tablón de la pared para aceptar contratos de caza.",
                    "¿Buscas gloria y recompensas legítimas? Completa contratos para subir de Rango de Aventurero hasta el mítico Rango S.",
                    "No olvides mantener tu equipo en las mesas de herrería de la planta alta antes de adentrarte en el bosque.",
                    "Cada bestia que derrotas en nombre del gremio te otorga Medallas de Aventurero y prestigio en todo el reino."
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

    RAIDEN_SHOGUN(
            "raiden_shogun",
            "Raiden Shogun",
            "Arconte Electro",
            "Genshin Impact",
            "§5§l",
            List.of(
                    "El trueno ruge y las ilusiones se desvanecen. Solo la Eternidad permanece inmutable.",
                    "¿Buscas desafiar los designios celestiales? Demuestra la fuerza de tu voluntad.",
                    "No temas al rayo; teme a la vacilación en el instante del juicio supremo.",
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
                    "Los contratos se firman con roca y oro. Aquellos que rompan sus promesas sufrirán la ira de las piedras.",
                    "El vino de osmanthus sabe igual que en mis recuerdos... pero ¿dónde están aquellos con quienes compartirlo?",
                    "La solidez del escudo no reside en el mineral, sino en la calma inquebrantable del espíritu.",
                    "Recorre Teyvat con paso firme; la historia no olvida a quienes forjan su propio destino."
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
            "Directora de El Camino",
            "Genshin Impact",
            "§c§l",
            List.of(
                    "¡Aiya! ¡Cliente a la vista! ¿Necesitas cupones de entierro? ¡Llevas dos y el tercero es gratis!",
                    "Entre la vida y la muerte hay una delgada línea... ¡asegúrate de disfrutar cada paso!",
                    "Las mariposas espirituales bailan en la noche; el Báculo de Homa purifica las almas intranquilas.",
                    "¡No te preocupes por el peligro! Si caes en combate, la Funeraria El Camino cuidará de ti, ¡jaja!"
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
            "Vigía de las Siete Estrellas",
            "Genshin Impact",
            "§b§l",
            List.of(
                    "El rocío del amanecer calma el corazón... No debemos retrasar el trabajo pendiente de Liyue.",
                    "El Arco de Amos susurra con el viento helado; un tiro limpio y certero disipa cualquier amenaza.",
                    "¿Has descansado lo suficiente? El sobreesfuerzo nubla la precisión del cazador.",
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

    MARCH_7TH(
            "march_7th",
            "Siete de Marzo",
            "Arquera Astral",
            "Honkai: Star Rail",
            "§d§l",
            List.of(
                    "¡Mira esa pose! ¡Sonríe para la cámara del Expreso Astral antes de que empiece la batalla!",
                    "¡Una flecha de seis fases de hielo para cada monstruo que intente arruinar nuestro viaje!",
                    "Viajar entre las estrellas es emocionante, pero siempre hay que estar listos para la aventura.",
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
                    "He caminado por incontables ríos carmesí... El pasado y el futuro se desvanecen en la nada.",
                    "La lluvia no cesa en este sendero. Desenvaina solo cuando estés listo para cargar con el vacío.",
                    "El sueño y la vigilia se entrelazan. Recuerda tu nombre antes de cruzar el horizonte.",
                    "No temas a la oscuridad; incluso en la nihilidad más profunda, un destello persiste."
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
                    "Sección 6 en posición. Toda distorsión de éter en las Cavidades será erradicada.",
                    "La Espada Shunguang no vacila. La velocidad de corte supera al pensamiento.",
                    "Mantén la compostura ante los etéreos. La disciplina es el arma más afilada del agente.",
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
                    "Ugh... ¿Más trabajo de horas extra? Solo quiero mis caramelos de café y una siesta.",
                    "Si te interpones en mi camino de salida, te cortaré en rebanadas antes de que termine mi turno.",
                    "El servicio doméstico de Victoria no deja cabos sueltos ni manchas en la alfombra.",
                    "Terminemos esto rápido... Odio cuando el trabajo interrumpe mis horas de descanso."
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
            "El Magnate del Amanecer",
            "Genshin Impact",
            "§c§l",
            List.of(
                    "Bienvenido al Viñedo del Amanecer. ¿Una copa de jugo de uva antes de que continúes tu camino?",
                    "Los Caballeros de Favonius son demasiado lentos. Ciertas amenazas solo se disipan desde las sombras.",
                    "El fuego del amanecer nunca vacila. No bajes la guardia cuando la noche caiga sobre Teyvat.",
                    "Si buscas información o un respiro en estas tierras, las puertas de este viñedo siempre estarán abiertas."
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
    public List<String> dialogues() { return dialogues; }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "textures/entity/npc/" + id + ".png");
    }

    public String getRandomDialogue() {
        if (dialogues.isEmpty()) return "Saludos, cazador.";
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
            default -> ItemStack.EMPTY;
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
