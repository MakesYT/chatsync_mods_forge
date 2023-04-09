package top.ncserver.chatimg.Tools;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ncserver.chatimg.ChatImg;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;


public class SendPack {
    private final String message;
    private static final Logger LOGGER = LogManager.getLogger();

    public SendPack(PacketBuffer buffer) {
        message = buffer.toString(StandardCharsets.UTF_8);
    }

    public SendPack(String message) {
        this.message = message;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBytes(this.message.getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, Img> imgMap = new LinkedHashMap<String, Img>();
    public static int ImgID = 0;

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
                //LOGGER.info(this.message);
                String json = this.message.substring(this.message.indexOf("{"));
                json = json.substring(0, this.message.lastIndexOf("}") + 1);
                //System.out.println(json);
                try {
                    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                    int imgID = jsonObject.get("id").getAsInt();
                    if (ChatImg.imgMap.containsKey(imgID)) {
                        Img img = ChatImg.imgMap.get(imgID);
                        img.add(jsonObject.get("index").getAsInt(), jsonObject.get("data").getAsString());
                        ChatImg.imgMap.replace(imgID, img);
                    } else {
                        Img img = new Img(jsonObject.get("packageNum").getAsInt(), jsonObject.get("index").getAsInt(), jsonObject.get("data").getAsString());
                        ChatImg.imgMap.put(imgID, img);
                    }
                    Img img = ChatImg.imgMap.get(imgID);
                    if (img.allReceived()) {
                        ResourceLocation F = new ResourceLocation("chatimg", "imgs/" + imgID);
                        Base64.Decoder decoder = Base64.getDecoder();
                        byte[] b = decoder.decode(img.getData());
                        // 处理数据
                        for (int i = 0; i < b.length; ++i) {
                            if (b[i] < 0) {
                                b[i] += 256;
                            }
                        }
                        NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(b));
                        img.setWidthAndHeight(nativeImage.getWidth(), nativeImage.getHeight());
                        ChatImg.imgMap.replace(imgID, img);
                        Minecraft.getInstance().getTextureManager().register(F, new DynamicTexture(nativeImage));

                    }

                } catch (Exception e) {
                }
            });
            DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> {
                String msg = this.message;
                msg = msg.substring(msg.indexOf("{"));
                String finalMsg = msg;
                //LogManager.getLogger().debug(finalMsg);
                new Thread(() -> {
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
                                int imgId = ImgID++;
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
                                List<ServerPlayerEntity> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
                                for (int i = 0; i < split.length; i++) {
                                    imgJson.setData(i, split[i]);
                                    String s = new Gson().toJson(imgJson);
                                    //System.out.println(s);
                                    Networking.INSTANCE.send(PacketDistributor.ALL.noArg(), new SendPack(s));


                                }
                                for (ServerPlayerEntity player : players) {
                                    player.sendMessage(new StringTextComponent("[" + jsonObject.get("sender").getAsString() + "]:" + "[ImgID=" + imgId + "]"), UUID.randomUUID());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            });
        });
        ctx.get().setPacketHandled(true);
    }
}
