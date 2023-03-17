package top.ncserver.chatimg.Tools.mixin;


import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.client.gui.screens.ChatScreen.class)
public abstract class ChatScreen extends Screen {
    @Shadow
    CommandSuggestions commandSuggestions;
    @Shadow
    private String initial;

    public ChatScreen(String p_95579_) {
        super(Component.translatable("chat_screen.title"));
        this.initial = p_95579_;
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean mouseScrolled(double p_95581_, double p_95582_, double p_95583_) {
        p_95583_ = Mth.clamp(p_95583_, -1.0, 1.0);
        if (!this.commandSuggestions.mouseScrolled(p_95583_)) {

            this.minecraft.gui.getChat().scrollChat((int) p_95583_);
        }
        return true;
    }


}
