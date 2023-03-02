import top.ncserver.chatimg.Tools.dll.ClipboardImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class ClipboardImageTest {
    public static void main(String[] args) throws IOException {
        System.load("D:\\WPF.net\\get_clipboard_image\\x64\\Debug\\get_clipboard_image.dll");
        ClipboardImage clipboardImage = new ClipboardImage();
        byte[] imageData = clipboardImage.getImageData();
        if (imageData == null) {
            System.out.println("No image data on clipboard.");
        } else {
            System.out.println("Image data found on clipboard.");
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            ImageIO.write(image, "png", new File("clipboard.png"));
            System.out.println();

        }
    }
}
