import com.google.gson.*;
import software.bernie.geckolib.loading.json.typeadapter.BakedAnimationsAdapter;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Bakes each animation of a file with GeckoLib's own code and prints the real error when one fails. */
public class GeoAnimCheck {
    public static void main(String[] a) throws Exception {
        JsonObject root = JsonParser.parseString(Files.readString(Path.of(a[0]))).getAsJsonObject();
        BakedAnimationsAdapter adapter = new BakedAnimationsAdapter();
        Method bake = BakedAnimationsAdapter.class.getDeclaredMethod("bakeAnimation", String.class, JsonObject.class, JsonDeserializationContext.class);
        bake.setAccessible(true);
        JsonDeserializationContext ctx = new JsonDeserializationContext() {
            @Override
            public <T> T deserialize(JsonElement json, Type type) {
                return KeyFramesAdapter.GEO_GSON.fromJson(json, type);
            }
        };
        for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("animations").entrySet()) {
            try {
                Object anim = bake.invoke(adapter, e.getKey(), e.getValue().getAsJsonObject(), ctx);
                System.out.println("OK   " + e.getKey() + " -> " + anim.getClass().getSimpleName());
            } catch (java.lang.reflect.InvocationTargetException ex) {
                Throwable c = ex.getCause();
                StringBuilder sb = new StringBuilder(String.valueOf(c));
                while (c.getCause() != null) { c = c.getCause(); sb.append(" <- ").append(c); }
                System.out.println("FAIL " + e.getKey() + ": " + sb);
            }
        }
    }
}
