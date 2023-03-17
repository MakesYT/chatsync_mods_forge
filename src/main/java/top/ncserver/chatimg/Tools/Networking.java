package top.ncserver.chatimg.Tools;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    public static final String VERSION = "ABSENT \uD83E\uDD14";
    private static final int ID = 6969;



    public static void registerMessage() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("chatimg", "img"),
                () -> VERSION,
                (version) -> version.equals(VERSION),
                (version) -> version.equals(VERSION)
        );
        INSTANCE.messageBuilder(SendPack.class, ID)
                .encoder(SendPack::toBytes)
                .decoder(SendPack::new)
                .consumerMainThread(SendPack::handler)
                .add();
    }
}
