package top.ncserver.chatimg.Tools;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
    public static SimpleChannel INSTANCE;
    public static final String VERSION = "ABSENT \uD83E\uDD14";
    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }


    public static void registerMessage() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("chatimg", "img"),
                () -> VERSION,
                (version) -> version.equals(VERSION),
                (version) -> version.equals(VERSION)
        );
        INSTANCE.messageBuilder(SendPack.class, 6969)
                .encoder(SendPack::toBytes)
                .decoder(SendPack::new)
                .consumerMainThread(SendPack::handler)
                .add();
    }
}
