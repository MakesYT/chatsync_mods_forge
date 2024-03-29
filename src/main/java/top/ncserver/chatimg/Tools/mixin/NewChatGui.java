package top.ncserver.chatimg.Tools.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.ncserver.chatimg.ChatImg;
import top.ncserver.chatimg.Tools.Img;

import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
@Mixin(net.minecraft.client.gui.NewChatGui.class)
public abstract class NewChatGui extends AbstractGui {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Final
    @Shadow
    private List<ChatLine<IReorderingProcessor>> trimmedMessages;
    @Shadow
    private int chatScrollbarPos;
    @Shadow
    @Final
    private Deque<ITextComponent> chatQueue;
    @Shadow
    private boolean newMessageSinceScroll;
    @Shadow
    @Final
    private List<ChatLine<ITextComponent>> allMessages;

    /**
     * @author
     * @reason
     */
    @Overwrite
    private static double getTimeFactor(int counterIn) {
        double d0 = (double) counterIn / 200.0D;
        d0 = 1.0D - d0;
        d0 = d0 * 10.0D;
        d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
        return d0 * d0;
    }

    @Shadow
    public abstract double getScale();

    /**
     * @author
     * @reason
     */
    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    protected abstract boolean isChatHidden();

    @Shadow
    protected abstract void processPendingMessages();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getLinesPerPage() {
        return this.getHeight() / 9;
    }

    @Shadow
    public abstract int getHeight();

    @Shadow
    public abstract int getWidth();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void scrollChat(double posInc) {
        this.chatScrollbarPos = (int) ((double) this.chatScrollbarPos + posInc);
        if (this.chatScrollbarPos > this.trimmedMessages.size() - 3)
            this.chatScrollbarPos = this.trimmedMessages.size() - 3;
        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }

    }

    private static final String pattern = "\\[ImgID=(.+)\\]";
    private static final Pattern patternP = Pattern.compile("\\[ImgID=(.+)\\]");
    /**
     * @author
     * @reason
     */

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void render(MatrixStack p_238492_1_, int p_238492_2_) {
        if (!this.isChatHidden()) {
            this.processPendingMessages();
            int i = this.getLinesPerPage();
            int j = this.trimmedMessages.size();
            if (j > 0) {
                boolean flag = this.isChatFocused();

                double d0 = this.getScale();
                int k = MathHelper.ceil((double) this.getWidth() / d0);
                RenderSystem.pushMatrix();
                RenderSystem.translatef(2.0F, 8.0F, 0.0F);
                RenderSystem.scaled(d0, d0, 1.0D);
                double chatOpacity = this.minecraft.options.chatOpacity * (double) 0.9F + (double) 0.1F;
                double accessibilityTextBackgroundOpacity = this.minecraft.options.textBackgroundOpacity;

                double d3 = 9.0D * (this.minecraft.options.chatLineSpacing + 1.0D);
                int l = 0;
                int indexY = (int) ((double) (-this.getLinesPerPage()) * d3) + getHeight();
                for (int u = 0; ; u++) {
                    // System.out.println(this.scrollPos);
                    if (indexY <= -getHeight() || u > this.trimmedMessages.size() - 1 || u + this.chatScrollbarPos > this.allMessages.size() - 1) {
                        break;
                    }

                    ++l;
                    ChatLine<IReorderingProcessor> chatline = this.trimmedMessages.get(u + this.chatScrollbarPos);
                    if (chatline != null) {
                        int j1 = p_238492_2_ - chatline.getAddedTime();
                        if (j1 < 200 || flag) {
                            double d5 = flag ? 1.0D : getTimeFactor(j1);
                            int l1 = (int) (255.0D * d5 * chatOpacity);
                            int i2 = (int) (255.0D * d5 * accessibilityTextBackgroundOpacity);
                            String json = allMessages.get(u + this.chatScrollbarPos).getMessage().getString();

                            if (json.contains("[ImgID=")) {
                                Matcher matcher = patternP.matcher(json);
                                int imgID = -1;
                                try {
                                    if (matcher.find()) {
                                        imgID = Integer.parseInt((matcher.group(0)).split("=")[1].replace("]", ""));
                                    }
                                    Img img = ChatImg.imgMap.get(imgID);
                                    if (img.allReceived()) {
                                        p_238492_1_.pushPose();
                                        p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                        fill(p_238492_1_, -2, indexY + 9, k + 4, indexY - img.getHeight() + 9, i2 << 24);
                                        RenderSystem.enableBlend();
                                        p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                        //this.mc.fontRenderer.drawTextWithShadow(p_238492_1_, chatline.getLineString(), 0.0F, (float)((int)(d6 + d4)), 16777215 + (l1 << 24));
                                        ResourceLocation F = new ResourceLocation("chatimg", "imgs/" + imgID);
                                        RenderSystem.color4f(0.7F, 0.7F, 0.7F, 0.7F);
                                        this.minecraft.getTextureManager().bind(F);
                                        blit(p_238492_1_, 0, indexY - img.getHeight() + 9, 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), img.getHeight());
                                        //
                                        p_238492_1_.popPose();
                                        RenderSystem.disableAlphaTest();
                                        RenderSystem.disableBlend();
                                        indexY -= img.getHeight();
                                        int i1 = MathHelper.floor((double) this.getWidth() / this.getScale());
                                        List<IReorderingProcessor> list = RenderComponentsUtil.wrapComponents(allMessages.get(u + this.chatScrollbarPos).getMessage(), i1, this.minecraft.font);
                                        //System.out.println(list.size());
                                        for (IReorderingProcessor iReorderingProcessor : list) {
                                            p_238492_1_.pushPose();
                                            p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                            fill(p_238492_1_, -2, indexY, k + 4, indexY + 9, i2 << 24);
                                            RenderSystem.enableBlend();
                                            p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                            this.minecraft.font.drawShadow(p_238492_1_, iReorderingProcessor, 0.0F, indexY, 16777215 + (l1 << 24));
                                            p_238492_1_.popPose();
                                            RenderSystem.disableAlphaTest();
                                            RenderSystem.disableBlend();
                                            indexY -= 9;
                                        }


                                    }
                                }catch (Exception e) {
                                    p_238492_1_.pushPose();
                                    p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                    fill(p_238492_1_, -2, indexY, k + 4, indexY + 9, i2 << 24);
                                    RenderSystem.enableBlend();
                                    p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                    this.minecraft.font.drawShadow(p_238492_1_, chatline.getMessage(), 0.0F, indexY, 16777215 + (l1 << 24));
                                    p_238492_1_.popPose();
                                    RenderSystem.disableAlphaTest();
                                    RenderSystem.disableBlend();
                                    indexY -= 9;
                                }


                            } else {
                                int j2 = 0;
                                p_238492_1_.pushPose();
                                p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                fill(p_238492_1_, -2, indexY, k + 4, indexY + 9, i2 << 24);
                                RenderSystem.enableBlend();
                                p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                                this.minecraft.font.drawShadow(p_238492_1_, chatline.getMessage(), 0.0F, indexY, 16777215 + (l1 << 24));
                                p_238492_1_.popPose();
                                RenderSystem.disableAlphaTest();
                                RenderSystem.disableBlend();
                                indexY -= 9;
                            }
                        }

                    }

                }

                if (!this.chatQueue.isEmpty()) {
                    int k2 = (int) (128.0D * chatOpacity);
                    int i3 = (int) (255.0D * accessibilityTextBackgroundOpacity);
                    p_238492_1_.pushPose();
                    p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                    fill(p_238492_1_, -2, 0, k + 4, 9, i3 << 24);
                    RenderSystem.enableBlend();
                    p_238492_1_.translate(0.0D, 0.0D, 50.0D);
                    this.minecraft.font.drawShadow(p_238492_1_, new TranslationTextComponent("chat.queue", this.chatQueue.size()), 0.0F, 1.0F, 16777215 + (k2 << 24));
                    p_238492_1_.popPose();
                    RenderSystem.disableAlphaTest();
                    RenderSystem.disableBlend();
                }

                if (flag) {
                    int l2 = 9;
                    RenderSystem.translatef(-3.0F, 0.0F, 0.0F);
                    int j3 = j * l2 + j;
                    int k3 = l * l2 + l;
                    int l3 = this.chatScrollbarPos * k3 / j;
                    int k1 = k3 * k3 / j3;
                    if (j3 != k3) {
                        int i4 = l3 > 0 ? 170 : 96;
                        int j4 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        fill(p_238492_1_, 0, -l3, 2, -l3 - k1, j4 + (i4 << 24));
                        fill(p_238492_1_, 2, -l3, 1, -l3 - k1, 13421772 + (i4 << 24));
                    }
                }

                RenderSystem.popMatrix();
            }
        }
    }
}
