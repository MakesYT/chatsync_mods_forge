package top.ncserver.chatimg.Tools;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.codec.Decoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ncserver.chatimg.ChatImg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
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
    public void handler(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //LOGGER.info(this.message);
            String json = this.message.substring(this.message.indexOf("{"));
            json = json.substring(0, this.message.lastIndexOf("}")+1);
            //System.out.println(json);
            try {
                JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                int imgID=jsonObject.get("id").getAsInt();
                if (ChatImg.imgMap.containsKey(imgID)){
                    Img img= ChatImg.imgMap.get(imgID);
                    img.add(jsonObject.get("index").getAsInt(),jsonObject.get("data").getAsString());
                    ChatImg.imgMap.replace(imgID,img);
                }else {
                    Img img=new Img(jsonObject.get("packageNum").getAsInt(),jsonObject.get("index").getAsInt(),jsonObject.get("data").getAsString());
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
                    NativeImage nativeImage=NativeImage.read(new ByteArrayInputStream(b));
                    img.setWidthAndHeight(nativeImage.getWidth(),nativeImage.getHeight());
                    ChatImg.imgMap.replace(imgID,img);
                    Minecraft.getInstance().getTextureManager().loadTexture(F,new DynamicTexture(nativeImage));

                }

            }catch (Exception e) {}
        });
        ctx.get().setPacketHandled(true);
    }
}
