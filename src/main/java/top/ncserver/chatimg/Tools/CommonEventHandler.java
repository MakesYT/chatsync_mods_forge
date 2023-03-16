package top.ncserver.chatimg.Tools;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import top.ncserver.chatimg.ChatImg;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Mod.EventBusSubscriber(modid = "chatimg")
public class CommonEventHandler {
    public static FMLEventChannel channel;



    @SubscribeEvent
    public static void onClientPacket(FMLNetworkEvent.ClientCustomPacketEvent evt) {
        String message = evt.getPacket().payload().toString(StandardCharsets.UTF_8);

        String json = message.substring(message.indexOf("{"));

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

                Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        Minecraft.getMinecraft().renderEngine.loadTexture(F, new DynamicTexture(nativeImage));
                    }
                });


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
