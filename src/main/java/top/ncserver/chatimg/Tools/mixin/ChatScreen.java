package top.ncserver.chatimg.Tools.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.CommandSuggestionHelper;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.client.gui.screen.ChatScreen.class)
public abstract class ChatScreen extends Screen {
    @Shadow
    protected TextFieldWidget inputField;
    @Shadow
    private CommandSuggestionHelper commandSuggestionHelper;
    @Shadow
    private String defaultInputFieldText = "";

    protected ChatScreen(ITextComponent titleIn) {
        super(titleIn);
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

        if (!this.commandSuggestionHelper.onScroll(delta)) {
            this.minecraft.ingameGUI.getChatGUI().addScrollPos(delta);
        }
        return true;
    }
}
