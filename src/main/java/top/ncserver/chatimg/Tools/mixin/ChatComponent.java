package top.ncserver.chatimg.Tools.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.ncserver.chatimg.ChatImg;
import top.ncserver.chatimg.Tools.Img;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
@Mixin(net.minecraft.client.gui.components.ChatComponent.class)
public abstract class ChatComponent extends GuiComponent {
    @Shadow
    protected abstract boolean isChatHidden();

    @Shadow
    public abstract int getLinesPerPage();

    @Shadow
    @Final
    private List<GuiMessage.Line> trimmedMessages;
    @Shadow
    @Final
    private List<GuiMessage> allMessages;

    @Shadow
    protected abstract boolean isChatFocused();

    @Shadow
    public abstract double getScale();

    @Shadow
    public abstract int getWidth();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract int getLineHeight();

    @Shadow
    private int chatScrollbarPos;

    @Shadow
    private static double getTimeFactor(int pCounter) {
        double d0 = (double) pCounter / 200.0D;
        d0 = 1.0D - d0;
        d0 *= 10.0D;
        d0 = Mth.clamp(d0, 0.0D, 1.0D);
        return d0 * d0;
    }

    @Shadow
    protected abstract int getTagIconLeft(GuiMessage.Line pLine);

    @Shadow
    protected abstract void drawTagIcon(PoseStack pPoseStack, int pLeft, int pBottom, GuiMessageTag.Icon pTagIcon);

    @Shadow
    private boolean newMessageSinceScroll;

    @Shadow
    public abstract int getHeight();

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void scrollChat(int pPosInc) {
        this.chatScrollbarPos += pPosInc;
        int i = this.trimmedMessages.size();
        if (this.chatScrollbarPos > i - 3) {
            this.chatScrollbarPos = i - 3;
        }

        if (this.chatScrollbarPos <= 0) {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }

    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern patternP = Pattern.compile("\\[ImgID=(.+)\\]");

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void render(PoseStack pPoseStack, int pTickCount) {
        if (!this.isChatHidden()) {
            int i = this.getLinesPerPage();
            int j = this.trimmedMessages.size();
            if (j > 0) {
                boolean flag = this.isChatFocused();
                float f = (float) this.getScale();
                int k = Mth.ceil((float) this.getWidth() / f);
                pPoseStack.pushPose();
                pPoseStack.translate(4.0D, 8.0D, 0.0D);
                pPoseStack.scale(f, f, 1.0F);
                double d0 = this.minecraft.options.chatOpacity().get() * (double) 0.9F + (double) 0.1F;
                double d1 = this.minecraft.options.textBackgroundOpacity().get();
                double d2 = this.minecraft.options.chatLineSpacing().get();
                int l = this.getLineHeight();
                double d3 = -8.0D * (d2 + 1.0D) + 4.0D * d2;
                int i1 = 0;
                int indexY = (int) ((double) (-this.getLinesPerPage()) * getLineHeight()) + getHeight();
                for (int u = 0; ; u++) {
                    if (indexY <= -getHeight() || u > this.trimmedMessages.size() - 1 || u + this.chatScrollbarPos > this.allMessages.size() - 1) {
                        break;
                    }
                    GuiMessage.Line guimessage$line = this.trimmedMessages.get(u + this.chatScrollbarPos);
                    if (guimessage$line != null) {
                        int k1 = pTickCount - guimessage$line.addedTime();
                        if (k1 < 200 || flag) {
                            double d4 = flag ? 1.0D : getTimeFactor(k1);
                            int i2 = (int) (255.0D * d4 * d0);
                            int j2 = (int) (255.0D * d4 * d1);
                            ++i1;
                            if (i2 > 3) {
                                if (this.allMessages.get(u + this.chatScrollbarPos).content().getString().contains("[ImgID=")) {
                                    Matcher matcher = patternP.matcher(this.allMessages.get(u + this.chatScrollbarPos).content().getString());
                                    int imgID = -1;
                                    try {
                                        if (matcher.find()) {
                                            imgID = Integer.parseInt((matcher.group(0)).split("=")[1].replace("]", ""));
                                        }
                                        Img img = ChatImg.imgMap.get(imgID);
                                        if (img.allReceived()) {
                                            pPoseStack.pushPose();
                                            pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                            fill(pPoseStack, -4, indexY, k + 4 + 4, indexY - img.getHeight(), j2 << 24);
                                            RenderSystem.enableBlend();
                                            //pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                            //this.mc.fontRenderer.drawTextWithShadow(p_238492_1_, chatline.getLineString(), 0.0F, (float)((int)(d6 + d4)), 16777215 + (l1 << 24));
                                            //ChatImg.LOGGER.debug(String.valueOf(imgID));
                                            ResourceLocation F = new ResourceLocation("chatimg", "imgs/" + imgID);
                                            RenderSystem.setShaderTexture(0, F);
                                            RenderSystem.setShaderColor(0.7F, 0.7F, 0.7F, 0.7F);
                                            //this.minecraft.getTextureManager().bindForSetup(F);
                                            blit(pPoseStack, 0, indexY - img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), img.getHeight());
                                            //
                                            pPoseStack.popPose();
                                            RenderSystem.disableBlend();
                                            indexY -= img.getHeight();


                                            pPoseStack.pushPose();
                                            pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                            fill(pPoseStack, -4, indexY, k + 4 + 4, indexY - 9, j2 << 24);
                                            GuiMessageTag guimessagetag = guimessage$line.tag();

                                            if (guimessagetag != null) {
                                                int j3 = guimessagetag.indicatorColor() | i2 << 24;
                                                fill(pPoseStack, -4, indexY - 9, -2, indexY + img.getHeight(), j3);
                                                if (flag && guimessage$line.endOfEntry() && guimessagetag.icon() != null) {
                                                    int k3 = this.getTagIconLeft(guimessage$line);
                                                    this.drawTagIcon(pPoseStack, k3, indexY, guimessagetag.icon());
                                                }
                                            }

                                            RenderSystem.enableBlend();
                                            pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                            this.minecraft.font.drawShadow(pPoseStack, guimessage$line.content(), 0.0F, (float) indexY - 9, 16777215 + (i2 << 24));
                                            RenderSystem.disableBlend();
                                            pPoseStack.popPose();
                                            indexY -= 9;


                                        }
                                    } catch (Exception e) {
                                        pPoseStack.pushPose();
                                        pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                        fill(pPoseStack, -4, indexY, k + 4 + 4, indexY - 9, j2 << 24);
                                        GuiMessageTag guimessagetag = guimessage$line.tag();

                                        if (guimessagetag != null) {
                                            int j3 = guimessagetag.indicatorColor() | i2 << 24;
                                            fill(pPoseStack, -4, indexY, -2, indexY - 9, j3);
                                            if (flag && guimessage$line.endOfEntry() && guimessagetag.icon() != null) {
                                                int k3 = this.getTagIconLeft(guimessage$line);
                                                this.drawTagIcon(pPoseStack, k3, indexY, guimessagetag.icon());
                                            }
                                        }

                                        RenderSystem.enableBlend();
                                        pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                        this.minecraft.font.drawShadow(pPoseStack, guimessage$line.content(), 0.0F, (float) indexY - 9, 16777215 + (i2 << 24));
                                        RenderSystem.disableBlend();
                                        pPoseStack.popPose();
                                        indexY -= 9;
                                    }
                                } else {
                                    pPoseStack.pushPose();
                                    pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                    fill(pPoseStack, -4, indexY, k + 4 + 4, indexY - 9, j2 << 24);
                                    GuiMessageTag guimessagetag = guimessage$line.tag();

                                    if (guimessagetag != null) {
                                        int j3 = guimessagetag.indicatorColor() | i2 << 24;
                                        fill(pPoseStack, -4, indexY, -2, indexY - 9, j3);
                                        if (flag && guimessage$line.endOfEntry() && guimessagetag.icon() != null) {
                                            int k3 = this.getTagIconLeft(guimessage$line);
                                            this.drawTagIcon(pPoseStack, k3, indexY, guimessagetag.icon());
                                        }
                                    }

                                    RenderSystem.enableBlend();
                                    pPoseStack.translate(0.0D, 0.0D, 50.0D);
                                    this.minecraft.font.drawShadow(pPoseStack, guimessage$line.content(), 0.0F, (float) indexY - 9, 16777215 + (i2 << 24));
                                    RenderSystem.disableBlend();
                                    pPoseStack.popPose();
                                    indexY -= 9;
                                }


                            }
                        }
                    }
                }

                long i4 = this.minecraft.getChatListener().queueSize();
                if (i4 > 0L) {
                    int j4 = (int) (128.0D * d0);
                    int l4 = (int) (255.0D * d1);
                    pPoseStack.pushPose();
                    pPoseStack.translate(0.0D, 0.0D, 50.0D);
                    fill(pPoseStack, -2, 0, k + 4, 9, l4 << 24);
                    RenderSystem.enableBlend();
                    pPoseStack.translate(0.0D, 0.0D, 50.0D);
                    this.minecraft.font.drawShadow(pPoseStack, Component.translatable("chat.queue", i4), 0.0F, 1.0F, 16777215 + (j4 << 24));
                    pPoseStack.popPose();
                    RenderSystem.disableBlend();
                }

                if (flag) {
                    int k4 = this.getLineHeight();
                    int i5 = j * k4;
                    int l1 = i1 * k4;
                    int j5 = this.chatScrollbarPos * l1 / j;
                    int k5 = l1 * l1 / i5;
                    if (i5 != l1) {
                        int l5 = j5 > 0 ? 170 : 96;
                        int i6 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        int j6 = k + 4;
                        fill(pPoseStack, j6, -j5, j6 + 2, -j5 - k5, i6 + (l5 << 24));
                        fill(pPoseStack, j6 + 2, -j5, j6 + 1, -j5 - k5, 13421772 + (l5 << 24));
                    }
                }

                pPoseStack.popPose();
            }
        }
    }
}
