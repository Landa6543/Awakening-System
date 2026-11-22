package dev.diorama;

import dev.diorama.compat.ChunkyBorder;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
//? if fabric {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
*///?} else if neoforge {
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
//?} else {
/*import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
*///?}
//? if fabric && >=1.21 {
/*import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
*///?}
import net.minecraft.network.FriendlyByteBuf;
//? if >=1.21 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?}
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * The Chunky Border of the dimension the player is in, for the map to draw (see
 * {@link ChunkyBorder}). Two ways to know it:
 * <ul>
 *   <li>With the add-on installed here too, its client already keeps each dimension's border as its
 *       server sends it, and draws it in the world; Diorama reads that, so the map shows what the
 *       wall does. The channel is the add-on's then: a second listener would stop the game starting
 *       on Fabric, and is refused on NeoForge.</li>
 *   <li>Without it, Diorama listens on the channel itself. The add-on's server sends to every player
 *       on Fabric, and on NeoForge, Forge and Paper to players whose client says it listens there,
 *       which listening does.</li>
 * </ul>
 */
public final class ChunkyBorders {
	private static final String MOD = "chunkyborder";
	public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath("chunky", "border");

	/** What Diorama heard on the channel itself, by dimension ID. Only without the add-on here. */
	private static final Map<String, ChunkyBorder.Shape> HEARD = new ConcurrentHashMap<>();
	/** Borders are kept for this many dimensions of a server; one that names more is making them up. */
	private static final int MAX_DIMENSIONS = 256;

	private static boolean looked;
	private static @Nullable Method clientShape;
	private static @Nullable Method clientColor;
	/** The add-on's last shape object for a dimension and what it reads as, so it's read once. */
	private static @Nullable Object lastRaw;
	private static ChunkyBorder.@Nullable Shape lastShape;

	private ChunkyBorders() {
	}

	/** The border of the dimension {@code level} is, or null if it has none. Client thread. */
	public static ChunkyBorder.@Nullable Shape shape(ClientLevel level) {
		ResourceLocation dimension = level.dimension().location();
		if (!addOnInstalled()) {
			return HEARD.get(dimension.toString());
		}
		Method method = clientShape;
		if (method == null) {
			return null;
		}
		try {
			Object raw = method.invoke(null, dimension);
			if (raw != lastRaw) {
				lastRaw = raw;
				lastShape = ChunkyBorder.ofClientShape(raw);
			}
			return lastShape;
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

	/**
	 * The wall's colour: the one the player gave the add-on's client, which is what the world shows,
	 * or the game's blue, which the add-on's client uses unless told otherwise.
	 */
	public static int color() {
		Method method = clientColor;
		if (method != null) {
			try {
				return ((Number) method.invoke(null)).intValue() & 0xFFFFFF;
			} catch (ReflectiveOperationException | RuntimeException e) {
				// The blue, then.
			}
		}
		return ChunkyBorder.BLUE;
	}

	/** Left the server, or a network moved the player to another of its servers: its borders go. */
	public static void clear() {
		HEARD.clear();
	}

	/** For tests: what Diorama heard itself on the channel, by dimension ID. */
	public static Map<String, ChunkyBorder.Shape> heardForTests() {
		return Map.copyOf(HEARD);
	}

	/** For tests: whether the add-on is installed here, so its client is read rather than the channel. */
	public static boolean addOnInstalled() {
		if (!looked) {
			looked = true;
			if (modLoaded()) {
				findClient();
			}
		}
		return modLoaded();
	}

	private static void findClient() {
		//? if fabric {
		/*String main = "org.popcraft.chunkyborder.ChunkyBorderFabricClient";
		*///?} else if neoforge {
		String main = "org.popcraft.chunkyborder.ChunkyBorderNeoForge";
		//?} else {
		/*String main = "org.popcraft.chunkyborder.ChunkyBorderForge";
		*///?}
		try {
			ClassLoader loader = ChunkyBorders.class.getClassLoader();
			clientShape = Class.forName(main, false, loader).getMethod("getBorderShape", ResourceLocation.class);
			try {
				clientColor = Class.forName("org.popcraft.chunkyborder.util.BorderColor", false, loader).getMethod("getColor");
			} catch (ReflectiveOperationException e) {
				// The blue, then.
			}
			Diorama.LOGGER.info("Chunky Border is installed: the map shows the border its client keeps");
		} catch (ReflectiveOperationException | LinkageError e) {
			Diorama.LOGGER.warn("Chunky Border is installed, but not as Diorama knows it, so the map can't show its border", e);
		}
	}

	private static boolean modLoaded() {
		//? if fabric {
		/*return FabricLoader.getInstance().isModLoaded(MOD);
		*///?} else {
		return ModList.get().isLoaded(MOD);
		//?}
	}

	/** A message from the server on the channel. Any thread. */
	static void receive(byte[] bytes) {
		ChunkyBorder.Message message = ChunkyBorder.read(bytes);
		if (message == null) {
			return;
		}
		if (message.shape() == null) {
			HEARD.remove(message.dimension());
		} else if (HEARD.size() < MAX_DIMENSIONS || HEARD.containsKey(message.dimension())) {
			HEARD.put(message.dimension(), message.shape());
		}
	}

	//? if >=1.21 {
	/** The channel's message as the game carries it: its bytes, all of them. */
	public record Payload(byte[] bytes) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Payload> TYPE = new CustomPacketPayload.Type<>(CHANNEL);
		public static final StreamCodec<FriendlyByteBuf, Payload> CODEC = StreamCodec.of((buf, payload) -> buf.writeBytes(payload.bytes()), buf -> {
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			return new Payload(bytes);
		});

		@Override
		public CustomPacketPayload.Type<Payload> type() {
			return TYPE;
		}
	}
	//?}

	//? if fabric && >=1.21 {
	/*/^* Listens on the channel, unless the add-on is installed here. Call once, at startup, after every mod has. ^/
	public static void register() {
		if (addOnInstalled()) {
			return;
		}
		try {
			PayloadTypeRegistry.playS2C().register(Payload.TYPE, Payload.CODEC);
		} catch (IllegalArgumentException e) {
			Diorama.LOGGER.info("Another mod has {}, so Diorama doesn't show Chunky Border's border", CHANNEL);
			return;
		}
		// Registering a handler also tells the server Diorama listens here, which Paper needs.
		ClientPlayNetworking.registerGlobalReceiver(Payload.TYPE, (payload, context) -> receive(payload.bytes()));
		Diorama.LOGGER.info("Diorama listens on {} for Chunky Border's border", CHANNEL);
	}
	*///?} else if fabric {
	/*// Listens on the channel, unless the add-on is installed here. Call once, at startup, after every mod has.
	public static void register() {
		if (addOnInstalled()) {
			return;
		}
		// Registering a handler also tells the server Diorama listens here, which Paper needs.
		boolean registered = ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, sender) -> {
			// Read here: the buffer is only good until this returns.
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			receive(bytes);
		});
		if (!registered) {
			Diorama.LOGGER.info("Another mod has {}, so Diorama doesn't show Chunky Border's border", CHANNEL);
			return;
		}
		Diorama.LOGGER.info("Diorama listens on {} for Chunky Border's border", CHANNEL);
	}
	*///?} else if neoforge {
	// Listens on the channel, unless the add-on is installed here. NeoForge calls it once, at startup.
	public static void register(RegisterPayloadHandlersEvent event) {
		if (addOnInstalled()) {
			return;
		}
		// Registered as the add-on registers it, version "0", to the client only: a NeoForge server
		// with the add-on refuses a client whose channel has another version or direction. Optional,
		// so servers without it still let the player in; NeoForge tells servers that aren't NeoForge
		// about optional channels as the player joins, which Paper needs.
		try {
			event.registrar("0").optional().playToClient(Payload.TYPE, Payload.CODEC, (payload, context) -> receive(payload.bytes()));
		} catch (UnsupportedOperationException e) {
			Diorama.LOGGER.info("Another mod has {}, so Diorama doesn't show Chunky Border's border", CHANNEL);
			return;
		}
		Diorama.LOGGER.info("Diorama listens on {} for Chunky Border's border", CHANNEL);
	}
	//?} else {
	/*// Listens on the channel, unless the add-on is installed here. Call once, after every mod has loaded.
	public static void register() {
		if (addOnInstalled()) {
			return;
		}
		// Any version accepted either way: Forge only lets a player in when both sides accept the
		// other's version of every channel they share. Forge tells servers that aren't Forge about its
		// channels as the player joins, which Paper needs.
		EventNetworkChannel channel;
		try {
			channel = NetworkRegistry.newEventChannel(CHANNEL, () -> "0", version -> true, version -> true);
		} catch (IllegalArgumentException e) {
			Diorama.LOGGER.info("Another mod has {}, so Diorama doesn't show Chunky Border's border", CHANNEL);
			return;
		}
		channel.<NetworkEvent.ServerCustomPayloadEvent>addListener(event -> {
			if (event instanceof NetworkEvent.ServerCustomPayloadLoginEvent) {
				return;
			}
			// Read here: the buffer is only good until this returns.
			FriendlyByteBuf buf = event.getPayload();
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			receive(bytes);
			event.getSource().get().setPacketHandled(true);
		});
		Diorama.LOGGER.info("Diorama listens on {} for Chunky Border's border", CHANNEL);
	}
	*///?}
}
