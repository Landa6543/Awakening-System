package com.soybe.awakeningsystem.kaisel;

import com.mojang.brigadier.context.CommandContext;
import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * {@code /kaisel sombra} puts a shadow Kaisel bound to you in front of you, ready to ride (made the way "Arise"
 * makes shadows); {@code /kaisel vivo} a living, hostile one. For trying them out; needs operator rights.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class KaiselCommand {
    private KaiselCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("kaisel").requires(s -> s.hasPermission(2))
                .then(Commands.literal("sombra").executes(c -> summon(c, true)))
                .then(Commands.literal("shadow").executes(c -> summon(c, true)))
                .then(Commands.literal("vivo").executes(c -> summon(c, false)))
                .then(Commands.literal("living").executes(c -> summon(c, false))));
    }

    private static int summon(CommandContext<CommandSourceStack> c, boolean shadow) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = c.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        KaiselEntity k = KaiselContent.KAISEL.create(level);
        if (k == null) return 0;
        // a few blocks in front of the player, facing them
        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 at = player.position().add(look.scale(6));
        k.moveTo(at.x, player.getY(), at.z, player.getYRot() + 180F, 0);
        if (shadow) {
            k.addTag("shadow_soldier");
            k.addTag("shadow_master_" + player.getUUID());
            k.setCustomName(Component.translatable("awakening_system.msg.sombra", Component.translatable("entity.awakening_system.kaisel").getString()));
            k.setCustomNameVisible(true);
            com.soybe.awakeningsystem.SystemEvents.setupShadowMob(level, player, k);
            k.roar();
        } else {
            level.addFreshEntity(k);
        }
        c.getSource().sendSuccess(() -> Component.literal(shadow
                ? "Kaisel (sombra): clic derecho para montarlo. W vuela hacia donde miras, Espacio sube, Ctrl más rápido, Mayús para bajar."
                : "Kaisel (vivo) ha aparecido."), false);
        return 1;
    }
}
