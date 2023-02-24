package top.ncserver.chatimg.Tools.mixin;

import com.google.gson.JsonObject;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ncserver.chatimg.Tools.Networking;
import top.ncserver.chatimg.Tools.SendPack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;


@Mixin(KeyboardListener.class)
public class Clipboard {
    private static boolean isWindows() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

    @Inject(at = @At("RETURN"), method = "getClipboard", cancellable = true)
    public void getClipboard(CallbackInfoReturnable<String> cir) {
        if (isWindows()) {
            try {
                Minecraft.getInstance().player.chat("获取剪贴板");
                if (false) {
                    cir.setReturnValue("");
                    Minecraft.getInstance().player.chat("图片发送中....");
                    // Cast content to image
                    Image image = null;
                    // Convert image to byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write((RenderedImage) image, "png", baos);
                    byte[] bytes = baos.toByteArray();
                    // Encode byte array to base64 string
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    // Print base64 string
                    ((Runnable) () -> {
                        String imgId = UUID.randomUUID().toString().replace("-", "");

                        JsonObject json = new JsonObject();
                        json.addProperty("id", imgId);
                        json.addProperty("base64imgdata", "base64imgdata");
                        int length = 4096;
                        int n = (base64.length() + length - 1) / length; //获取整个字符串可以被切割成字符子串的个数
                        json.addProperty("packageNum", n);
                        String[] split = new String[n];
                        for (int i = 0; i < n; i++) {
                            if (i < (n - 1)) {
                                split[i] = base64.substring(i * length, (i + 1) * length);
                            } else {
                                split[i] = base64.substring(i * length);
                            }
                        }
                        for (int i = 0; i < split.length; i++) {
                            JsonObject temp = json;
                            temp.addProperty("index", i);
                            temp.addProperty("data", split[i]);
                            Networking.INSTANCE.sendToServer(new SendPack(temp.getAsString()));
                        }

                    }).run();

                }
            } catch (IOException | IllegalStateException e) {
                // e.printStackTrace();
            }
        }
    }
}
