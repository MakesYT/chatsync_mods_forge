package top.ncserver.chatimg.Tools.PackTool;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import top.ncserver.chatimg.ChatImg;
import top.ncserver.chatimg.Tools.Img;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class PackToolC {
    private final String message;

    public PackToolC(String message) {
        this.message = message;
    }

    public void client() {
        //System.out.println(("c " + this.message));
        String json = this.message.substring(this.message.indexOf("{"));
        json = json.substring(0, json.lastIndexOf("}") + 1);
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
            e.printStackTrace();
        }
    }
}
