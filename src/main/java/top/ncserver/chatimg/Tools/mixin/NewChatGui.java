package top.ncserver.chatimg.Tools.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import top.ncserver.chatimg.ChatImg;
import top.ncserver.chatimg.Tools.Img;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiNewChat.class)
public abstract class NewChatGui extends Gui {
    @Final
    @Shadow
    private static Logger LOGGER;
    @Final
    @Shadow
    private Minecraft mc;
    @Final
    @Shadow
    private List<String> sentMessages;
    @Final
    @Shadow
    private List<ChatLine> chatLines;
    @Final
    @Shadow
    private List<ChatLine> drawnChatLines;
    @Shadow
    private int scrollPos;
    @Shadow
    private boolean isScrolled;

    @Shadow
    public abstract int getLineCount();

    @Shadow
    public abstract boolean getChatOpen();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract int getChatWidth();

    @Shadow
    public abstract int getChatHeight();

    private static final String pattern = "\\[ImgID=(.+)\\]";
    private static final Pattern patternP = Pattern.compile("\\[ImgID=(.+)\\]");

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void scroll(int amount) {
        this.scrollPos += amount;
        if (this.scrollPos > this.drawnChatLines.size() - 3) {
            this.scrollPos = this.drawnChatLines.size() - 3;
        }

        if (this.scrollPos <= 0) {
            this.scrollPos = 0;
            this.isScrolled = false;
        }

    }

    /**
     * @author MakesYT
     * @reason 添加图片显示
     */
    @Overwrite
    public void drawChat(int updateCounter) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            int j = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (j > 0) {
                boolean flag = this.getChatOpen();

                float f1 = this.getChatScale();
                int k = MathHelper.ceil((float) this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 8.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);
                int l = 0;
                int indexY = (int) ((double) (-this.getLineCount()) * 9) + getChatHeight();
                for (int u = 0; ; u++) {
                    if (indexY <= -getChatHeight() || u > this.drawnChatLines.size() - 1 || u + this.scrollPos > this.chatLines.size() - 1) {
                        break;
                    }
                    ++l;
                    ChatLine chatline = this.drawnChatLines.get(u + this.scrollPos);
                    int j1 = updateCounter - chatline.getUpdatedCounter();
                    if (j1 < 200 || flag) {
                        double d0 = (double) j1 / 200.0D;
                        d0 = 1.0D - d0;
                        d0 = d0 * 10.0D;
                        d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
                        d0 = d0 * d0;
                        int l1 = (int) (255.0D * d0);

                        if (flag) {
                            l1 = 255;
                        }

                        l1 = (int) ((float) l1 * f);
                        ++l;
                        String json = chatline.getChatComponent().getUnformattedText();
                        if (json.contains("[ImgID=")) {
                            Matcher matcher = patternP.matcher(json);
                            int imgID = -1;
                            try {
                                if (matcher.find()) {
                                    imgID = Integer.parseInt((matcher.group(0)).split("=")[1].replace("]", ""));
                                }
                                Img img = ChatImg.imgMap.get(imgID);
                                if (img.allReceived()) {
                                    drawRect(-2, indexY + 9, k + 4, indexY - img.getHeight() + 9, l1 / 2 << 24);

                                    GlStateManager.enableBlend();

                                    //this.mc.fontRenderer.drawTextWithShadow(p_238492_1_, chatline.getLineString(), 0.0F, (float)((int)(d6 + d4)), 16777215 + (l1 << 24));
                                    ResourceLocation F = new ResourceLocation("chatimg", "imgs/" + imgID);
                                    GlStateManager.color(0.7F, 0.7F, 0.7F, 0.7F);
                                    this.mc.getTextureManager().bindTexture(F);
                                    drawModalRectWithCustomSizedTexture(0, indexY - img.getHeight() + 9, 0, 0, img.getWidth(), img.getHeight(), img.getWidth(), img.getHeight());


                                    GlStateManager.disableAlpha();
                                    GlStateManager.disableBlend();
                                    indexY -= img.getHeight();
                                    drawRect(-2, indexY, k + 4, indexY + 9, l1 / 2 << 24);
                                    String s = chatline.getChatComponent().getFormattedText();
                                    GlStateManager.enableBlend();
                                    this.mc.fontRenderer.drawStringWithShadow(s, 0.0F, (float) (indexY), 16777215 + (l1 << 24));
                                    GlStateManager.disableAlpha();
                                    GlStateManager.disableBlend();
                                    indexY -= 9;


                                }
                            } catch (Exception e) {

                                drawRect(-2, indexY, k + 4, indexY + 9, l1 / 2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                this.mc.fontRenderer.drawStringWithShadow(s, 0.0F, (float) (indexY), 16777215 + (l1 << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                                indexY -= 9;
                            }
                        } else {

                            drawRect(-2, indexY, k + 4, indexY + 9, l1 / 2 << 24);
                            String s = chatline.getChatComponent().getFormattedText();
                            GlStateManager.enableBlend();
                            this.mc.fontRenderer.drawStringWithShadow(s, 0.0F, (float) (indexY), 16777215 + (l1 << 24));
                            GlStateManager.disableAlpha();
                            GlStateManager.disableBlend();
                            indexY -= 9;
                        }


                    }
                }


                //////////////////////////////////////

                /////////////////////////////////////

                if (flag) {
                    int k2 = this.mc.fontRenderer.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = j * k2 + j;
                    int i3 = l * k2 + l;
                    int j3 = this.scrollPos * i3 / j;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }
}
