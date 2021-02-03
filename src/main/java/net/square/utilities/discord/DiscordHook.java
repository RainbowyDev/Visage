package net.square.utilities.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.square.Visage;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.Instant;

public class DiscordHook {

    public static void sendMessageToDiscord(Visage visage, Player player, String moduleName, String comment, String violation) {

        visage.getService().execute(() -> {
            try {
                URL url = new URL(visage.getConfig().getString("logging.discord.link"));

                JsonObject object = new JsonObject();
                JsonArray array = new JsonArray();
                JsonArray fields = new JsonArray();
                JsonObject arrayObject = new JsonObject();
                JsonObject thumbObj = new JsonObject();
                JsonObject authorObj = new JsonObject();

                JsonObject object1 = new JsonObject();
                object1.addProperty("name", "Player information");
                object1.addProperty(
                    "value", "" + player.getName() + " / " + player.getAddress().getAddress().getHostAddress() + " / "
                        + player.getUniqueId());
                fields.add(object1);

                JsonObject object2 = new JsonObject();
                object2.addProperty("name", "Module");
                object2.addProperty("value", moduleName);
                fields.add(object2);

                JsonObject object3 = new JsonObject();
                object3.addProperty("name", "Comment");
                object3.addProperty("value", comment);
                fields.add(object3);

                JsonObject object7 = new JsonObject();
                object7.addProperty("name", "Violation");
                object7.addProperty("value", violation);
                fields.add(object7);

                JsonObject object4 = new JsonObject();
                object4.addProperty("name", "TPS");
                object4.addProperty("value", String.valueOf(MinecraftServer.getServer().recentTps[0]));
                object4.addProperty("inline", true);
                fields.add(object4);

                JsonObject object5 = new JsonObject();
                object5.addProperty("name", "World");
                object5.addProperty("value", player.getWorld().getName());
                object5.addProperty("inline", true);
                fields.add(object5);

                JsonObject object6 = new JsonObject();
                object6.addProperty("name", "Ping");
                object6.addProperty("value", ((CraftPlayer) player).getHandle().ping + "ms");
                object6.addProperty("inline", true);
                fields.add(object6);

                authorObj.addProperty("name", "Visage anticheat");

                thumbObj.addProperty("url", "https://visage.surgeplay.com/face/32/" + player.getUniqueId()
                    .toString()
                    .replace("-", ""));

                arrayObject.addProperty("title", "Verbose report");
                arrayObject.addProperty(
                    "description", "Advanced and fully automated cheat-protection to prevent cheating on the server");

                arrayObject.addProperty("url", "https://git.squarecode.de/SquareCode/Visage");
                arrayObject.addProperty("color", 16711680);
                arrayObject.add("thumbnail", thumbObj);
                arrayObject.add("author", authorObj);
                arrayObject.add("fields", fields);
                arrayObject.addProperty("timestamp", Instant.now().toString());

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("text", "Visage v"+ visage.getDescription().getVersion());
                arrayObject.add("footer", jsonObject);

                array.add(arrayObject);

                object.addProperty("content", "One report has been received");
                object.add("embeds", array);

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "Java-Discord-Webhook");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                try (OutputStream stream = connection.getOutputStream()) {
                    stream.write(object.toString().getBytes());
                    stream.flush();
                }
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                inputStream.close();
                connection.disconnect();

            } catch (Exception e) {
                System.out.printf("[Visage] Error: %s", e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
