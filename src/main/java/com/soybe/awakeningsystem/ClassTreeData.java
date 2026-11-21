package com.soybe.awakeningsystem;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads the class webs from {@code data/<namespace>/class_trees/*.json} with the server's data (and on /reload),
 * and sends them to players when they join or the data is reloaded.
 */
@EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID)
public final class ClassTreeData {
    private ClassTreeData() {}

    private static final Gson GSON = new Gson();

    private static final class Loader extends SimpleJsonResourceReloadListener {
        Loader() {
            super(GSON, "class_trees");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, String> json = new LinkedHashMap<>();
            files.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .forEach(e -> json.put(e.getKey().getPath(), GSON.toJson(e.getValue())));   // compact text, also what is sent
            ClassSkillTree.install(json);
        }
    }

    @SubscribeEvent
    public static void addLoader(AddReloadListenerEvent event) {
        event.addListener(new Loader());
    }

    @SubscribeEvent
    public static void sync(OnDatapackSyncEvent event) {
        Payload p = new Payload(new HashMap<>(ClassSkillTree.json()));
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, p));
    }

    /** The webs' JSON texts, class id → JSON. */
    public record Payload(Map<String, String> json) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AwakeningSystemMod.MOD_ID, "class_trees"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.stringUtf8(1 << 18)), Payload::json, Payload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @EventBusSubscriber(modid = AwakeningSystemMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private Registration() {}

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").optional().playToClient(Payload.TYPE, Payload.STREAM_CODEC, (payload, context) -> context.enqueueWork(() -> {
                // in single player the server already installed the same webs
                if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient() && isLocalServer()) return;
                ClassSkillTree.install(payload.json());
            }));
        }

        private static boolean isLocalServer() {
            return net.minecraft.client.Minecraft.getInstance().isLocalServer();
        }
    }
}
