package top.ncserver.chatimg.Tools.mixin;

import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.client.gui.screen.ChatScreen.class)
public abstract class ChatScreen extends Screen {
    @Shadow
    protected TextFieldWidget input;
    @Shadow
    private CommandSuggestionHelper commandSuggestions;
    @Shadow
    private String initial = "";

    protected ChatScreen(String p_i1024_1_) {
        super(NarratorChatListener.NO_TITLE);
        this.initial = p_i1024_1_;
    }
    /**
     * @author
     * @reason
     */
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
