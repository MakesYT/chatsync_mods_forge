package top.ncserver.chatimg.Tools;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.ncserver.chatimg.Tools.PackTool.PackToolC;
import top.ncserver.chatimg.Tools.PackTool.PackToolS;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;


public class SendPack {
    private final String message;
    private static final Logger LOGGER = LogManager.getLogger();

    public SendPack(FriendlyByteBuf buffer) {
        message = buffer.readUtf(Short.MAX_VALUE);
    }

    public SendPack(String message) {
        this.message = message;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.message);
    }

    public static Map<String, Img> imgMap = new LinkedHashMap<String, Img>();
    public static int ImgID = 0;

    public void handler(Supplier<NetworkEvent.Context> ctx) {
        // System.out.println(ImgID+" "+ imgMap.size());

        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                PackToolC packTool = new PackToolC(this.message);
                packTool.client();
            } else if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
                PackToolS packTool = new PackToolS(this.message);
                imgMap = packTool.server(imgMap, ImgID);
                if (ImgID <= packTool.getImgID())
                    ImgID = packTool.getImgID();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
