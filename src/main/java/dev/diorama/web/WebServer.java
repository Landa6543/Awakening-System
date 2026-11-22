package dev.diorama.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.diorama.Diorama;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import org.jspecify.annotations.Nullable;

/**
 * The web map's own small web server, as BlueMap and Dynmap have one: serves the folder the map
 * draws into, read-only, and nothing else. Java's built-in HTTP server, two threads.
 */
final class WebServer {
	private final HttpServer server;
	private final java.util.concurrent.ExecutorService workers;

	private WebServer(HttpServer server, java.util.concurrent.ExecutorService workers) {
		this.server = server;
		this.workers = workers;
	}

	/**
	 * Starts serving {@code folder}, or returns null (logged) if the port can't be had. Nothing is
	 * served from a folder in it whose name, in lower case, {@code closed} says yes to. Where players
	 * are comes from {@code players}, never from a file; with none, nothing answers at that address.
	 * Land claims likewise come from {@code claims}, which is given the version the page already has
	 * and answers null when that's still current. The markers' file is served only while
	 * {@code markers} says it holds what it should, and the server's own copy of the markers never,
	 * wherever the folder is.
	 */
	static @Nullable WebServer start(Path folder, String address, int port, java.util.function.Predicate<String> closed, java.util.function.@Nullable Supplier<String> players,
		java.util.function.@Nullable UnaryOperator<@Nullable String> claims, java.util.function.BooleanSupplier markers) {
		try {
			Path root = folder.toAbsolutePath().normalize();
			return start(address, port, exchange -> serve(root, closed, players, claims, markers, exchange));
		} catch (IOException | RuntimeException e) {
			Diorama.LOGGER.warn("Diorama's web map can't serve on port {} ({}); set web-map-port to a free one, or 0 to serve the folder yourself", port, e.toString());
			return null;
		} catch (NoClassDefFoundError e) {
			Diorama.LOGGER.warn("This Java has no built-in web server; serve {} with one of your own", folder);
			return null;
		}
	}

	/**
	 * Starts answering every request with {@code handler}, on {@code address} (empty for every
	 * address). Throws if the port can't be had, and {@link NoClassDefFoundError} on a Java without
	 * the built-in web server.
	 */
	static WebServer start(String address, int port, com.sun.net.httpserver.HttpHandler handler) throws IOException {
		InetSocketAddress bind = address.isEmpty() ? new InetSocketAddress(port) : new InetSocketAddress(address, port);
		HttpServer server = HttpServer.create(bind, 64);
		server.createContext("/", handler);
		java.util.concurrent.ExecutorService workers = Executors.newFixedThreadPool(2, task -> {
			Thread thread = new Thread(task, "Diorama web map server");
			thread.setDaemon(true);
			return thread;
		});
		server.setExecutor(workers);
		server.start();
		return new WebServer(server, workers);
	}

	/** The port it listens on. */
	int port() {
		return this.server.getAddress().getPort();
	}

	void stop() {
		this.server.stop(0);
		this.workers.shutdownNow();
	}

	private static void serve(Path root, java.util.function.Predicate<String> closed, java.util.function.@Nullable Supplier<String> players,
		java.util.function.@Nullable UnaryOperator<@Nullable String> claims, java.util.function.BooleanSupplier markers, HttpExchange exchange) throws IOException {
		try {
			String method = exchange.getRequestMethod();
			if (!method.equals("GET") && !method.equals("HEAD")) {
				send(exchange, 405, "text/plain", null);
				return;
			}
			String path = exchange.getRequestURI().getPath();
			if (path.equals("/") || path.isEmpty()) {
				path = "/index.html";
			}
			if (path.equals(WebPlayers.PATH)) {
				// Before any file: a file of that name in the folder is never read.
				if (players == null) {
					send(exchange, 404, "text/plain", null);
				} else {
					exchange.getResponseHeaders().set("Cache-Control", "no-store");
					send(exchange, 200, "application/json", method.equals("HEAD") ? null : players.get().getBytes(java.nio.charset.StandardCharsets.UTF_8));
				}
				return;
			}
			if (path.equals(WebClaims.PATH)) {
				// Before any file, as players: claims are never a file, and one of that name is never read.
				if (claims == null) {
					send(exchange, 404, "text/plain", null);
				} else {
					exchange.getResponseHeaders().set("Cache-Control", "no-store");
					// The page says which answer it has (?since=<version>): nothing to send when that's still it.
					String query = exchange.getRequestURI().getRawQuery();
					String text = claims.apply(query != null && query.startsWith("since=") ? query.substring(6) : null);
					if (text == null) {
						exchange.sendResponseHeaders(204, -1);
					} else {
						send(exchange, 200, "application/json", method.equals("HEAD") ? null : text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
					}
				}
				return;
			}
			Path file = root.resolve(path.substring(1)).normalize();
			// Only what's in the folder, and only the page, its map file, the pictures and their chunks files.
			String name = file.getFileName() == null ? "" : file.getFileName().toString();
			if (name.equalsIgnoreCase(WebMarkers.STORE) || name.equalsIgnoreCase(WebMarkers.FILE) && !markers.getAsBoolean()) {
				// The markers of dimensions the map leaves out are in the first, and may be in the second until it's written again.
				send(exchange, 404, "text/plain", null);
				return;
			}
			if (("/" + name).equalsIgnoreCase(WebPlayers.PATH) || ("/" + name).equalsIgnoreCase(WebClaims.PATH)) {
				// Where players are and land claims are never files: one of those names, however it's asked for, isn't served.
				send(exchange, 404, "text/plain", null);
				return;
			}
			String type = name.endsWith(".jpg") ? "image/jpeg" : name.endsWith(".png") ? "image/png" : name.endsWith(".json") ? "application/json" : name.endsWith(".html") ? "text/html; charset=utf-8" : null;
			if (!file.startsWith(root) || type == null
				|| file.getNameCount() > root.getNameCount() + 1 && closed.test(file.getName(root.getNameCount()).toString().toLowerCase(java.util.Locale.ROOT))
				|| !Files.isRegularFile(file) || !file.toRealPath().startsWith(root.toRealPath())) {
				// Outside the folder, also through a link inside it; or a dimension the settings keep off the map.
				send(exchange, 404, "text/plain", null);
				return;
			}
			Instant modified = Files.getLastModifiedTime(file).toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
			exchange.getResponseHeaders().set("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(modified, ZoneOffset.UTC)));
			// Pictures change when their land does: the browser asks again, and gets a 304 if not.
			// A picture asked for with its version (the page's way) never changes under that address: the
			// browser keeps it. Anything else may change, and is checked each time (a 304 if not).
			String query = exchange.getRequestURI().getRawQuery();
			exchange.getResponseHeaders().set("Cache-Control", type.startsWith("image/") && query != null && query.startsWith("v=")
				? "public, max-age=31536000, immutable" : "no-cache");
			String since = exchange.getRequestHeaders().getFirst("If-Modified-Since");
			if (since != null) {
				try {
					if (!modified.isAfter(ZonedDateTime.parse(since, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant())) {
						exchange.sendResponseHeaders(304, -1);
						return;
					}
				} catch (DateTimeParseException ignored) {
				}
			}
			byte[] body;
			try {
				body = Files.readAllBytes(file);
			} catch (IOException e) {
				// Replaced as it was read: the browser asks again.
				send(exchange, 404, "text/plain", null);
				return;
			}
			send(exchange, 200, type, method.equals("HEAD") ? null : body);
		} catch (IOException | RuntimeException e) {
			// The browser went away.
		} finally {
			exchange.close();
		}
	}

	static void send(HttpExchange exchange, int code, String type, byte @Nullable [] body) throws IOException {
		exchange.getResponseHeaders().set("Content-Type", type);
		exchange.sendResponseHeaders(code, body == null ? -1 : body.length);
		if (body != null) {
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		}
	}
}
