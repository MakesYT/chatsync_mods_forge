package top.ncserver.chatimg.Tools;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    public static final String VERSION = "ABSENT \uD83E\uDD14";
    private static final int ID = 6969;



    public static void registerMessage() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("chatimg", "img"),
                () -> VERSION,
                (version) -> true,
                (version) -> true
        );
        INSTANCE.messageBuilder(SendPack.class,ID)
                .encoder(SendPack::toBytes)
                .decoder(SendPack::new)
                .consumer(SendPack::handler)
                .add();
    }
}
