package top.ncserver.chatimg.Tools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ncserver.chatimg.ChatImg;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Supplier;


public class SendPack {
    private final String message;
    private static final Logger LOGGER = LogManager.getLogger();

    public SendPack(FriendlyByteBuf buffer) {
        message = buffer.toString(StandardCharsets.UTF_8);
    }

    public SendPack(String message) {
        this.message = message;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBytes(this.message.getBytes(StandardCharsets.UTF_8));
    }

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOGGER.debug(this.message);
            String json = this.message;
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
                    ChatImg.imgMap.put(imgID,img);
                }
                Img img= ChatImg.imgMap.get(imgID);
                if (img.allReceived()){
                    ResourceLocation F=new ResourceLocation("chatimg","imgs/"+imgID);
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
                    LOGGER.debug(String.valueOf(imgID));
                    Minecraft.getInstance().getTextureManager().register(F, new DynamicTexture(nativeImage));

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
