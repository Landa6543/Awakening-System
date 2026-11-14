package com.soybe.awakeningsystem.combat;

import com.soybe.awakeningsystem.AwakeningSystemMod;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code /weapon_audit}: lists every weapon of the mod (anything with real attack damage) and the move set Epic
 * Fight gives it, and whether it has a Better Combat file, so weapons without moves can be found.
 * Writes weapon_audit.txt in the server folder.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class WeaponAudit {
    private WeaponAudit() {}

    @SubscribeEvent
    public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("weapon_audit").requires(s -> s.hasPermission(2)).executes(ctx -> {
            List<String> lines = new ArrayList<>();
            int missing = 0;
            var server = ctx.getSource().getServer();
            var rm = server.getResourceManager();
            for (var item : BuiltInRegistries.ITEM) {
                var id = BuiltInRegistries.ITEM.getKey(item);
                if (!id.getNamespace().equals(AwakeningSystemMod.MOD_ID)) continue;
                ItemStack stack = new ItemStack(item);
                double dmg = 0;
                for (ItemAttributeModifiers.Entry e : stack.getAttributeModifiers().modifiers())
                    if (e.attribute().is(Attributes.ATTACK_DAMAGE)) dmg += e.modifier().amount();
                if (dmg < 2.0) continue;
                String ef = EpicFightBridge.loaded() ? Impl.category(stack) : "no-epicfight";
                boolean bc = rm.getResource(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "weapon_attributes/" + id.getPath() + ".json")).isPresent();
                boolean bad = ef.equals("NONE") || ef.equals("EMPTY") || !bc;
                if (bad) missing++;
                lines.add((bad ? "MISSING " : "ok      ") + id.getPath() + "  epicfight=" + ef + "  bettercombat=" + bc + "  class=" + item.getClass().getSimpleName());
            }
            lines.sort(null);
            try {
                Files.write(server.getServerDirectory().resolve("weapon_audit.txt"), lines);
            } catch (Exception ignored) {}
            int m = missing;
            ctx.getSource().sendSuccess(() -> Component.literal("WEAPON_AUDIT " + lines.size() + " weapons, " + m + " missing something"), false);
            return m;
        }));
    }

    private static final class Impl {
        static String category(ItemStack stack) {
            CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(stack);
            if (cap == null || cap.isEmpty()) return "EMPTY";
            return String.valueOf(cap.getWeaponCategory());
        }
    }
}
