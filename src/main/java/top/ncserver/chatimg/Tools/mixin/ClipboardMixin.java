package top.ncserver.chatimg.Tools.mixin;


import com.google.gson.Gson;
import net.coobird.thumbnailator.Thumbnails;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ncserver.chatimg.Tools.ImgJson;
import top.ncserver.chatimg.Tools.Networking;
import top.ncserver.chatimg.Tools.SendPack;
import top.ncserver.chatimg.Tools.dll.ClipboardImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.UUID;


/**
 * 注入修改剪切板,支持粘贴图片
 *
 * @author kitUIN
 */
@Mixin(KeyboardListener.class)
public class ClipboardMixin {

    private static boolean isWindows() {
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

    private static void copyFile(InputStream inputStream, File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] arrayOfByte = new byte[63];
            int i;
            while ((i = inputStream.read(arrayOfByte)) > 0) {
                fileOutputStream.write(arrayOfByte, 0, i);
            }
            fileOutputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(at = @At("RETURN"), method = "getClipboard", cancellable = true)
    public void getClipboard(CallbackInfoReturnable<String> cir) {
        if (isWindows()) {
            try {
                // Minecraft.getInstance().player.sendMessage(new StringTextComponent("获取图片"),UUID.randomUUID());
                ClipboardImage clipboardImage = new ClipboardImage();
                byte[] imageData = clipboardImage.getImageData();
                if (imageData != null) {

                    BufferedImage image = Thumbnails.of(ImageIO.read(new ByteArrayInputStream(imageData)))
                            .scale(1f) //按比例放大缩小 和size() 必须使用一个 不然会报错
                            .outputQuality(0.5f)    //输出的图片质量  0~1 之间,否则报错
                            .asBufferedImage();
                    cir.setReturnValue("");
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent("图片发送中...."), UUID.randomUUID());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", baos);
                    byte[] bytes = baos.toByteArray();
                    // Encode byte array to base64 string
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    // Print base64 string
                    ((Runnable) () -> {
                        int length = 4096;
                        int n = (base64.length() + length - 1) / length; //获取整个字符串可以被切割成字符子串的个数
                        ImgJson imgJson = new ImgJson(UUID.randomUUID().toString(), n, Minecraft.getInstance().player.getDisplayName().getString());
                        String[] split = new String[n];
                        for (int i = 0; i < n; i++) {
                            if (i < (n - 1)) {
                                split[i] = base64.substring(i * length, (i + 1) * length);
                            } else {
                                split[i] = base64.substring(i * length);
                            }
                        }
                        for (int i = 0; i < split.length; i++) {
                            imgJson.setData(i, split[i]);
                            String s = new Gson().toJson(imgJson);
                            System.out.println(s);
                            Networking.INSTANCE.sendToServer(new SendPack(s));
                        }

                    }).run();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
