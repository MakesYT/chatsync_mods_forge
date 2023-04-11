package top.ncserver.chatimg.Tools.PackTool;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.ncserver.chatimg.Tools.Img;
import top.ncserver.chatimg.Tools.ImgJsonSTC;
import top.ncserver.chatimg.Tools.Networking;
import top.ncserver.chatimg.Tools.SendPack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PackToolS {
    private final String message;

    public PackToolS(String message) {
        this.message = message;
    }

    public Map<String, Img> server(Map<String, Img> imgMap, Integer ImgID) {
        //System.out.println(("s " + this.message));
        String msg = this.message;
        msg = msg.substring(msg.indexOf("{"));
        String finalMsg = msg;
        //LogManager.getLogger().debug(finalMsg);

        if (finalMsg.contains("base64imgdata")) {
            JsonObject jsonObject = new JsonParser().parse(finalMsg).getAsJsonObject();
            try {
                String imgID = jsonObject.get("id").getAsString();
                if (imgMap.containsKey(imgID)) {
                    Img img = imgMap.get(imgID);
                    img.add(jsonObject.get("index").getAsInt(), jsonObject.get("data").getAsString());
                    imgMap.replace(imgID, img);
                } else {
                    Img img = new Img(jsonObject.get("packageNum").getAsInt(), jsonObject.get("index").getAsInt(), jsonObject.get("data").getAsString());
                    imgMap.put(imgID, img);
                }
                Img img = imgMap.get(imgID);
                if (img.allReceived()) {
                    //LogManager.getLogger().debug("AllRegOnServerPacket");
                    int imgId = ImgID;
                    int length = 1024 * 30;
                    String base64 = img.getData();
                    int n = (base64.length() + length - 1) / length;
                    ImgJsonSTC imgJson = new ImgJsonSTC(imgId, n, jsonObject.get("sender").getAsString());
                    String[] split = new String[n];
                    for (int i = 0; i < n; i++) {
                        if (i < (n - 1)) {
                            split[i] = base64.substring(i * length, (i + 1) * length);
                        } else {
                            split[i] = base64.substring(i * length);
                        }
                    }
                    List<ServerPlayer> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
                    for (int i = 0; i < split.length; i++) {
                        imgJson.setData(i, split[i]);
                        String s = new Gson().toJson(imgJson);
                        //System.out.println(s);
                        Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SendPack(s));


                    }
                    for (ServerPlayer player : players) {
                        player.sendMessage(new TextComponent("[" + jsonObject.get("sender").getAsString() + "]:" + "[ImgID=" + imgId + "]"), UUID.randomUUID());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imgMap;
    }
}
