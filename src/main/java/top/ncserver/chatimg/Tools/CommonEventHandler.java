package top.ncserver.chatimg.Tools;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import top.ncserver.chatimg.ChatImg;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "chatimg")
public class CommonEventHandler {
    public static FMLEventChannel channel;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent evt) {
        String message = evt.getPacket().payload().toString(StandardCharsets.UTF_8);

        String json = message.substring(message.indexOf("{"));
        LogManager.getLogger().debug(json);
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

                BufferedImage nativeImage = ImageIO.read(new ByteArrayInputStream(b));
                img.setWidthAndHeight(nativeImage.getWidth(), nativeImage.getHeight());
                ChatImg.imgMap.replace(imgID, img);

                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft.getMinecraft().renderEngine.loadTexture(F, new DynamicTexture(nativeImage));
                });


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Img> imgMap = new LinkedHashMap<String, Img>();

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public static void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent evt) {
        //LogManager.getLogger().debug("onServerPacket");
        String msg = evt.getPacket().payload().toString(StandardCharsets.UTF_8);
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

                    }


                    byte[] array = s.getBytes(StandardCharsets.UTF_8); // 你要发送的消息的 byte 数组
                    ByteBuf buf = Unpooled.wrappedBuffer(array);
                    FMLProxyPacket packet = new FMLProxyPacket(new PacketBuffer(buf), "chatimg:img"); // 数据包
                    List<EntityPlayerMP> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();

                    for (EntityPlayerMP player : players) {
                        CommonEventHandler.channel.sendTo(packet, player);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
