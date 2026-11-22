package dev.diorama.web;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * {@code /diorama-web status} and {@code /diorama-web redraw}, for operators: what the web map is
 * doing, and drawing every tile again; and {@code /diorama-web marker add|remove|list}, the named
 * places on the page, as BlueMap's and Dynmap's marker commands. Not under {@code /diorama}, which the client's own commands
 * take (waypoint sharing): an operator with Diorama typing a server command there would reach the
 * client's.
 */
public final class WebMapCommand {
	private WebMapCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("diorama-web")
			//? if >=1.21.11 {
			/*.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			*///?} else {
			.requires(source -> source.hasPermission(2))
			//?}
			.then(Commands.literal("status").executes(context -> {
					WebMap map = WebMap.get();
					context.getSource().sendSuccess(() -> Component.literal(map == null
						? "The web map is off (web-map in config/" + WebSettings.FILE + ")"
						: "Web map: " + map.status()), false);
					return 1;
				}))
				.then(Commands.literal("redraw").executes(context -> {
					WebMap map = WebMap.get();
					if (map == null) {
						context.getSource().sendFailure(Component.literal("The web map is off (web-map in config/" + WebSettings.FILE + ")"));
						return 0;
					}
					map.redrawAll();
					context.getSource().sendSuccess(() -> Component.literal("Drawing the whole web map again"), true);
					return 1;
				}))
				.then(Commands.literal("marker")
					.then(Commands.literal("add").then(Commands.argument("name", StringArgumentType.greedyString()).executes(WebMapCommand::addMarker)))
					.then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.greedyString())
						.suggests((context, builder) -> {
							WebMap map = WebMap.get();
							if (map != null) {
								String typed = builder.getRemaining().toLowerCase(java.util.Locale.ROOT);
								map.markers.all().stream().map(WebMarkers.Marker::name).filter(name -> name.toLowerCase(java.util.Locale.ROOT).startsWith(typed)).forEach(builder::suggest);
							}
							return builder.buildFuture();
						}).executes(WebMapCommand::removeMarker)))
					.then(Commands.literal("list").executes(WebMapCommand::listMarkers))));
	}

	/** The running web map, or null after telling the operator it's off or its markers can't be changed. */
	private static WebMap withMarkers(CommandContext<CommandSourceStack> context, boolean changing) {
		WebMap map = WebMap.get();
		if (map == null) {
			context.getSource().sendFailure(Component.literal("The web map is off (web-map in config/" + WebSettings.FILE + ")"));
			return null;
		}
		if (changing && !map.markers.writable()) {
			context.getSource().sendFailure(Component.literal("The web map's markers file (diorama-web-markers.json in the world folder) can't be read. Mend or delete it, then restart"));
			return null;
		}
		return map;
	}

	/** A marker where the operator stands, or where {@code execute positioned} puts the command. */
	private static int addMarker(CommandContext<CommandSourceStack> context) {
		WebMap map = withMarkers(context, true);
		if (map == null) {
			return 0;
		}
		String name = StringArgumentType.getString(context, "name").strip();
		String dimension = context.getSource().getLevel().dimension().location().toString();
		if (name.isEmpty() || name.length() > WebMarkers.LONGEST_NAME) {
			context.getSource().sendFailure(Component.literal("A marker's name is 1 to " + WebMarkers.LONGEST_NAME + " characters"));
			return 0;
		}
		if (map.leftOut(dimension)) {
			context.getSource().sendFailure(Component.literal("The web map doesn't show this dimension, so it can't have a marker"));
			return 0;
		}
		WebMarkers.Marker before = map.markers.named(name);
		if (before == null && map.markers.all().size() >= WebMarkers.MOST) {
			context.getSource().sendFailure(Component.literal("The web map has " + WebMarkers.MOST + " markers, which is the most it takes"));
			return 0;
		}
		BlockPos at = BlockPos.containing(context.getSource().getPosition());
		// Under its name as first typed: adding it again moves it.
		WebMarkers.Marker marker = new WebMarkers.Marker(before == null ? name : before.name(), dimension, at.getX(), at.getY(), at.getZ());
		if (!map.markers.put(marker, map::leftOut)) {
			context.getSource().sendFailure(Component.literal("Couldn't save the web map's markers; see the server log"));
			return 0;
		}
		if (!map.markers.published()) {
			context.getSource().sendFailure(Component.literal("Saved " + marker.name() + ", but the page's markers file couldn't be written, so the page shows no markers for now; see the server log"));
			return 0;
		}
		context.getSource().sendSuccess(() -> Component.literal((before == null ? "Marked " : "Moved ") + marker.name() + " on the web map at " + at.getX() + ", " + at.getY() + ", " + at.getZ()), true);
		return 1;
	}

	private static int removeMarker(CommandContext<CommandSourceStack> context) {
		WebMap map = withMarkers(context, true);
		if (map == null) {
			return 0;
		}
		String name = StringArgumentType.getString(context, "name").strip();
		WebMarkers.Marker marker = map.markers.named(name);
		if (marker == null) {
			context.getSource().sendFailure(Component.literal("The web map has no marker named " + name));
			return 0;
		}
		if (!map.markers.remove(marker, map::leftOut)) {
			context.getSource().sendFailure(Component.literal("Couldn't save the web map's markers; see the server log"));
			return 0;
		}
		if (!map.markers.published()) {
			context.getSource().sendFailure(Component.literal("Removed " + marker.name() + ", but the page's markers file couldn't be written, so the page shows no markers for now; see the server log"));
			return 0;
		}
		context.getSource().sendSuccess(() -> Component.literal("Took " + marker.name() + " off the web map"), true);
		return 1;
	}

	private static int listMarkers(CommandContext<CommandSourceStack> context) {
		WebMap map = withMarkers(context, false);
		if (map == null) {
			return 0;
		}
		java.util.List<WebMarkers.Marker> markers = map.markers.all();
		if (markers.isEmpty()) {
			context.getSource().sendSuccess(() -> Component.literal("The web map has no markers. /diorama-web marker add <name> places one where you stand"), false);
			return 1;
		}
		StringBuilder list = new StringBuilder("Web map markers:");
		for (WebMarkers.Marker marker : markers) {
			list.append("\n").append(marker.name()).append(": ").append(marker.x()).append(", ").append(marker.y()).append(", ").append(marker.z())
				.append(" in ").append(marker.dimension()).append(map.leftOut(marker.dimension()) ? " (not on the page: the map doesn't show that dimension)" : "");
		}
		context.getSource().sendSuccess(() -> Component.literal(list.toString()), false);
		return markers.size();
	}
}
