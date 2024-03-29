package top.ncserver.chatimg.Tools.mixin;

import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@OnlyIn(Dist.CLIENT)
@Mixin(net.minecraft.client.gui.screen.ChatScreen.class)
public abstract class ChatScreen extends Screen {
    @Shadow
    private CommandSuggestionHelper commandSuggestions;
    @Shadow
    private String initial;

    public ChatScreen(String p_i1024_1_) {
        super(NarratorChatListener.NO_TITLE);
        this.initial = p_i1024_1_;
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta > 1.0D) {
            delta = 1.0D;
        }

        if (delta < -1.0D) {
            delta = -1.0D;
        }

        if (!this.commandSuggestions.mouseScrolled(delta)) {
            this.minecraft.gui.getChat().scrollChat(delta);
        }
        return true;
    }
}
