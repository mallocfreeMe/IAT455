
/*File BasicImageCompositing.java
 IAT455 - Workshop week 4
 Basic Image Compositing
 **********************************************************/

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

class BasicImageCompositing extends Frame {
    BufferedImage birdImage;
    BufferedImage boardImage;
    BufferedImage matteImage;
    BufferedImage placeholderImage;

    // ====== [Lab 4] result images ====== //
    BufferedImage addImage;
    BufferedImage subtractImage;
    BufferedImage keymixImage;
    BufferedImage premultipliedImage;
    BufferedImage P1, P2, P3, P4, P5;
    // =================================== //

    int width; // width of the image
    int height; // height of the image

    public BasicImageCompositing() {
        // constructor
        // Get an image from the specified file in the current directory on the
        // local hard disk.
        try {
            birdImage = ImageIO.read(new File("bird2.jpg"));
            boardImage = ImageIO.read(new File("board.jpg"));
            matteImage = ImageIO.read(new File("matte.jpg"));
            placeholderImage = ImageIO.read(new File("placeholderImg.jpg"));

        } catch (Exception e) {
            System.out.println("Cannot load the provided image");
        }
        this.setTitle("Week 4 workshop - Basic image compositing");
        this.setVisible(true);

        width = birdImage.getWidth();
        height = birdImage.getHeight();

        // ============= Lab 4 ============= //
        addImage = combineImages(birdImage, boardImage, Operations.add);
        subtractImage = combineImages(birdImage, boardImage, Operations.subtract);
        keymixImage = combineImages(birdImage, boardImage, matteImage, Operations.keymix);
        premultipliedImage = combineImages(birdImage, matteImage, Operations.premultiplied);
        P1 = combineImages(birdImage, boardImage, 0.9, Operations.multiply);
        P2 = combineImages(birdImage, boardImage, 0.7, Operations.multiply);
        P3 = combineImages(birdImage, boardImage, 0.5, Operations.multiply);
        P4 = combineImages(birdImage, boardImage, 0.3, Operations.multiply);
        P5 = combineImages(birdImage, boardImage, 0.1, Operations.multiply);
        // ================================= //

        // Anonymous inner-class listener to terminate program
        this.addWindowListener(new WindowAdapter() {// anonymous class definition
                                   public void windowClosing(WindowEvent e) {
                                       System.exit(0);// terminate the program
                                   }// end windowClosing()
                               }// end WindowAdapter
        );// end addWindowListener
    }// end constructor

    //    =========1.Adding / subtracting 2 source image
//    =========3.Premultiplied image
    public BufferedImage combineImages(BufferedImage src1, BufferedImage src2, Operations op) {
        BufferedImage result = new BufferedImage(src1.getWidth(), src1.getHeight(), src1.getType());

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb1 = src1.getRGB(i, j);
                int rgb2 = src2.getRGB(i, j);
                int newR = 0, newB = 0, newG = 0;
                if (op == Operations.add) {
                    newR = getRed(rgb1) + getRed(rgb2);
                    newG = getGreen(rgb1) + getGreen(rgb2);
                    newB = getBlue(rgb1) + getBlue(rgb2);
                } else if (op == Operations.subtract) {
                    newR = Math.abs(getRed(rgb1) - getRed(rgb2));
                    newG = Math.abs(getGreen(rgb1) - getGreen(rgb2));
                    newB = Math.abs(getBlue(rgb1) - getBlue(rgb2));
                } else if (op == Operations.premultiplied) {
                    newR = getRed(rgb1) * getRed(rgb2) / 255;
                    newG = getGreen(rgb1) * getGreen(rgb2) / 255;
                    newB = getBlue(rgb1) * getBlue(rgb2) / 255;
                }
                newR = clipChannelValue(newR);
                newG = clipChannelValue(newG);
                newB = clipChannelValue(newB);
                result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
            }
        }

        return result;
    }

    //    =========2.Keymix
    public BufferedImage combineImages(BufferedImage src1, BufferedImage src2, BufferedImage src3, Operations op) {
        BufferedImage result = new BufferedImage(src1.getWidth(), src1.getHeight(), src1.getType());

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb1 = src1.getRGB(i, j);
                int rgb2 = src2.getRGB(i, j);
                int rgb3 = src3.getRGB(i, j);
                int newR = 0, newB = 0, newG = 0;
                if (op == Operations.keymix) {
                    int m = getRed(rgb3) / 255;
                    newR = getRed(rgb1) * m + (getRed(rgb2) - getRed(rgb2) * m);
                    newG = getGreen(rgb1) * m + (getGreen(rgb2) - getGreen(rgb2) * m);
                    newB = getBlue(rgb1) * m + (getBlue(rgb2) - getBlue(rgb2) * m);
                }
                newR = clipChannelValue(newR);
                newG = clipChannelValue(newG);
                newB = clipChannelValue(newB);
                result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
            }
        }

        return result;
    }

    //    =========4.Dissolve
    public BufferedImage combineImages(BufferedImage src1, BufferedImage src2, double mv, Operations op) {
        BufferedImage result = new BufferedImage(src1.getWidth(), src1.getHeight(), src1.getType());

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb1 = src1.getRGB(i, j);
                int rgb2 = src2.getRGB(i, j);
                int newR = 0, newB = 0, newG = 0;
                if (op == Operations.multiply) {
                    newR = (int) (mv * getRed(rgb1) + (1 - mv) * getRed(rgb2));
                    newG = (int) (mv * getGreen(rgb1) + (1 - mv) * getGreen(rgb2));
                    newB = (int) (mv * getBlue(rgb1) + (1 - mv) * getBlue(rgb2));
                }
                newR = clipChannelValue(newR);
                newG = clipChannelValue(newG);
                newB = clipChannelValue(newB);
                result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
            }
        }

        return result;
    }

    // ========== helpers ========== //
    private int getRed(int rgb) {
        return new Color(rgb).getRed();
    }

    private int getGreen(int rgb) {
        return new Color(rgb).getGreen();
    }

    private int getBlue(int rgb) {
        return new Color(rgb).getBlue();
    }

    private int clipChannelValue(int v) {
        v = v > 255 ? 255 : v;
        v = v < 0 ? 0 : v;
        return v;
    }

    // ============================= //
    public void paint(Graphics g) {

        // if working with different images, this may need to be adjusted
        int w = width / 2;
        int h = height / 2;

        this.setSize(w * 7 + 300, h * 4 + 150);

        g.drawImage(birdImage, 25, 50, w, h, this);
        g.drawImage(boardImage, 25 + w + 25, 50, w, h, this);
        g.drawImage(matteImage, 25 + w * 2 + 50, 50, w, h, this);
        g.drawImage(addImage, 25 + w * 3 + 75, 50, w, h, this);
        g.drawImage(subtractImage, w * 4 + 125, 50, w, h, this);
        g.drawImage(keymixImage, w * 5 + 150, 50, w, h, this);
        g.drawImage(premultipliedImage, w * 6 + 175, 50, w, h, this);

        g.setColor(Color.BLACK);
        Font f1 = new Font("Verdana", Font.PLAIN, 13);
        g.setFont(f1);
        g.drawString("Source 1", 25, 45);
        g.drawString("Source 2", 50 + w, 45);
        g.drawString("Matte", 72 + 2 * w, 45);
        g.drawString("Add Src1 +Src2", 100 + 3 * w, 45);
        g.drawString("Subtract Src1 -Src2", 125 + 4 * w, 45);
        g.drawString("Keymix", 150 + 5 * w, 45);
        g.drawString("Premultiplied", 175 + 6 * w, 45);

        g.drawImage(birdImage, 25, 180 + h, w, h, this);
        g.drawImage(P1, 25 + w + 25, 180 + h, w, h, this);
        g.drawImage(P2, 25 + w * 2 + 50, 180 + h, w, h, this);
        g.drawImage(P3, 25 + w * 3 + 75, 180 + h, w, h, this);
        g.drawImage(P4, w * 4 + 125, 180 + h, w, h, this);
        g.drawImage(P5, w * 5 + 150, 180 + h, w, h, this);
        g.drawImage(boardImage, w * 6 + 175, 180 + h, w, h, this);

        g.drawString("Initial Image A", 25, 180 + h + h + 20);
        g.drawString("0.9A + 0.1B", 60 + w, 180 + h + h + 20);
        g.drawString("0.7A + 0.3B", 82 + 2 * w, 180 + h + h + 20);
        g.drawString("0.5A + 0.5B", 110 + 3 * w, 180 + h + h + 20);
        g.drawString("0.3A + 0.7B", 135 + 4 * w, 180 + h + h + 20);
        g.drawString("0.1A + 0.9B", 160 + 5 * w, 180 + h + h + 20);
        g.drawString("Final Image B", 185 + 6 * w, 180 + h + h + 20);

        Font f2 = new Font("Verdana", Font.BOLD, 15);
        g.setFont(f2);

        g.drawString("DISSOLVE between images", 170 + h, 150 + h);
    }

    // =======================================================//
    public static void main(String[] args) {

        BasicImageCompositing img = new BasicImageCompositing();// instantiate this object
        img.repaint();// render the image

    }// end main
}
// =======================================================//