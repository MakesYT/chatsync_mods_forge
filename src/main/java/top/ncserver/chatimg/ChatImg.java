package top.ncserver.chatimg;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ncserver.chatimg.Tools.CommonEventHandler;
import top.ncserver.chatimg.Tools.Img;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;


// The value here should match an entry in the META-INF/mods.toml file
@Mod(modid = "chatimg")
public class ChatImg {
    private static final Logger LOGGER = LogManager.getLogger();
    public static Map<Integer, Img> imgMap = new LinkedHashMap<Integer, Img>();

    public ChatImg() {
        MinecraftForge.EVENT_BUS.register(this);

    }

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

    @Mod.EventHandler
    private void setup(final FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        // 注册通道
        CommonEventHandler.channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("chatimg:img");
        CommonEventHandler.channel.register(CommonEventHandler.class);
        if (isWindows()) {
            File dllF = new File("get_clipboard_image.dll");
            if (!dllF.exists()) {
                try {
                    URL url = this.getClass().getClassLoader().getResource("get_clipboard_image.dll");
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        connection.setUseCaches(false);
                        copyFile(connection.getInputStream(), dllF);
                    }
                } catch (IOException var4) {

                }

            }
            System.load(dllF.getAbsolutePath());
        }

    }



}
