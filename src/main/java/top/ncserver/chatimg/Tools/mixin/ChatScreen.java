package top.ncserver.chatimg.Tools.mixin;


@Mixin(net.minecraft.client.gui.screen.ChatScreen.class)
public abstract class ChatScreen extends Screen {
    @Shadow
    protected TextFieldWidget input;
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
