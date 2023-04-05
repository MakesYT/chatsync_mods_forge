package top.ncserver.chatimg.Tools.mixin;


import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ITabCompleter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.IOException;

@SideOnly(Side.CLIENT)
@Mixin(GuiChat.class)
public abstract class ChatScreen extends GuiScreen implements ITabCompleter {


    /**
     * @author
     * @reason
     */
    @Overwrite
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0) {
            if (i > 1) {
                i = 1;
            }

            if (i < -1) {
                i = -1;
            }
            this.mc.ingameGUI.getChatGUI().scroll(i);
        }
    }
}
