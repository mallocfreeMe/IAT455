
/*File MatteManipulations.java

 IAT455 - Workshop week 5
 Matte Creation and Manipulations

 **********************************************************/

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;

class MatteManipulations extends Frame {
    BufferedImage skullImage;
    BufferedImage boardImage;
    BufferedImage carvingImage;
    BufferedImage redImage;

    BufferedImage supressedImage;
    BufferedImage invertedMatteImage;
    BufferedImage newBackgroundImage;
    BufferedImage colorDiffResultImage;

    BufferedImage subtractedImage;
    BufferedImage improvedMatteImage;
    BufferedImage overImage;
    BufferedImage backgroundImage;

    int width; // width of the image
    int height; // height of the image

    public MatteManipulations() {
        // constructor
        // Get an image from the specified file in the current directory on the
        // local hard disk.
        try {
            skullImage = ImageIO.read(new File("skull.jpg"));
            boardImage = ImageIO.read(new File("board0.jpg"));
            redImage = ImageIO.read(new File("red.jpg"));
            carvingImage = ImageIO.read(new File("carving.jpg"));
            backgroundImage = ImageIO.read(new File("backgroundImage.jpg"));

        } catch (Exception e) {
            System.out.println("Cannot load the provided image");
        }
        this.setTitle("Week 5 workshop - Matte Creation and Manipulations");
        this.setVisible(true);

        width = skullImage.getWidth();
        height = skullImage.getHeight();

        //Color Difference Method
        supressedImage = suppressToBlack(skullImage);
        invertedMatteImage = createInvertedMatte(skullImage);
        newBackgroundImage = combineImages(invertedMatteImage, boardImage, Operations.multiply);
        colorDiffResultImage = combineImages(supressedImage, newBackgroundImage, Operations.add);

        //Difference Matting
        subtractedImage = combineImages(carvingImage, redImage, Operations.subtract);
        improvedMatteImage = improveMatte(subtractedImage);
        overImage = over(carvingImage, improvedMatteImage, backgroundImage);

        //Anonymous inner-class listener to terminate program
        this.addWindowListener(
                new WindowAdapter() {//anonymous class definition
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);//terminate the program
                    }//end windowClosing()
                }//end WindowAdapter
        );//end addWindowListener
    }// end constructor

    public BufferedImage suppressToBlack(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        // Write your code here
        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb = src.getRGB(i, j);
                int r = getRed(rgb);
                int g = getGreen(rgb);
                int b = getBlue(rgb);

                int newB = b > g ? g : b;
                result.setRGB(i, j, new Color(r, g, newB).getRGB());
            }
        }
        return result;
    }

    public BufferedImage createInvertedMatte(BufferedImage src) {
        BufferedImage invertedMatte = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        // Write your code here
        for (int i = 0; i < invertedMatte.getWidth(); i++) {
            for (int j = 0; j < invertedMatte.getHeight(); j++) {
                int rgb = src.getRGB(i, j);
                int r = getRed(rgb);
                int g = getGreen(rgb);
                int b = getBlue(rgb);
                int matte = clip(b - Math.max(g, r));
                invertedMatte.setRGB(i, j, new Color(matte, matte, matte).getRGB());
            }
        }
        return invertedMatte;
    }

    public BufferedImage combineImages(BufferedImage src1, BufferedImage src2, Operations op) {
        BufferedImage result = new BufferedImage(src1.getWidth(), src1.getHeight(), src1.getType());

        // Write your code here
        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb1 = src1.getRGB(i, j);
                int rgb2 = src2.getRGB(i, j);

                int r1 = getRed(rgb1);
                int g1 = getGreen(rgb1);
                int b1 = getBlue(rgb1);

                int r2 = getRed(rgb2);
                int g2 = getGreen(rgb2);
                int b2 = getBlue(rgb2);

                if (op == Operations.multiply) {
                    int newR = clip(r1 * r2 / 255);
                    int newG = clip(g1 * g2 / 255);
                    int newB = clip(b1 * b2 / 255);
                    result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
                } else if (op == Operations.add) {
                    int newR = clip(r1 + r2);
                    int newG = clip(g1 + g2);
                    int newB = clip(b1 + b2);
                    result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
                } else if (op == Operations.subtract) {
                    int newR = Math.abs(r1 - r2);
                    int newG = Math.abs(g1 - g2);
                    int newB = Math.abs(b1 - b2);
                    result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
                }
            }
        }
        return result;
    }

    public BufferedImage improveMatte(BufferedImage src) {
        BufferedImage matte = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        // Write your code here
        // hsb[0] -> h, hsb[1] -> s, hsb[2] -> brightness
        for (int i = 0; i < matte.getWidth(); i++) {
            for (int j = 0; j < matte.getHeight(); j++) {
                int rgb = src.getRGB(i, j);
                int r = getRed(rgb);
                int g = getGreen(rgb);
                int b = getBlue(rgb);

                float[] hsb = Color.RGBtoHSB(r, g, b, null);
                float newBrightness = hsb[2] > 0.05 ? 1 : hsb[2];
                matte.setRGB(i, j, Color.HSBtoRGB(hsb[0], 0, newBrightness));
            }
        }
        return matte;
    }

    public BufferedImage over(BufferedImage foreground, BufferedImage matte, BufferedImage background) {

        // Write your code here
        BufferedImage result = new BufferedImage(foreground.getWidth(), foreground.getHeight(), foreground.getType());

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgbF = foreground.getRGB(i, j);
                int rF = getRed(rgbF);
                int gF = getGreen(rgbF);
                int bF = getBlue(rgbF);

                int rgbM = matte.getRGB(i, j);
                int rM = getRed(rgbM);
                int gM = getGreen(rgbM);
                int bM = getBlue(rgbM);

                int rgbB = background.getRGB(i, j);
                int rB = getRed(rgbB);
                int gB = getGreen(rgbB);
                int bB = getBlue(rgbB);

                int newR = clip(rF * rM / 255 + rB * (1 - rM / 255));
                int newG = clip(gF * gM / 255 + gB * (1 - gM / 255));
                int newB = clip(bF * bM / 255 + bB * (1 - bM / 255));

                result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
            }
        }

        // NOTE: You should change the return statement below to the actual result
        return result;
    }

    public BufferedImage invert(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        // Write your code here

        return result;
    }

    private int clip(int v) {
        v = v > 255 ? 255 : v;
        v = v < 0 ? 0 : v;
        return v;
    }

    protected int getRed(int pixel) {
        return (new Color(pixel)).getRed();
    }

    protected int getGreen(int pixel) {
        return (new Color(pixel)).getGreen();
    }

    protected int getBlue(int pixel) {
        return (new Color(pixel)).getBlue();
    }

    public void paint(Graphics g) {

        //if working with different images, this may need to be adjusted
        int w = width;
        int h = height;

        this.setSize(w * 7 + 300, h * 4 + 150);

        g.drawImage(skullImage, 25, 50, w, h, this);
        g.drawImage(supressedImage, 25 + w + 25, 50, w, h, this);
        g.drawImage(invertedMatteImage, 25 + w * 2 + 50, 50, w, h, this);
        g.drawImage(newBackgroundImage, 25 + w * 3 + 75, 50, w, h, this);
        g.drawImage(colorDiffResultImage, w * 4 + 125, 50, w, h, this);


        g.setColor(Color.BLACK);
        Font f1 = new Font("Verdana", Font.PLAIN, 13);
        g.setFont(f1);
        g.drawString("Skull image", 25, 45);
        g.drawString("Step 1: suppressed image", 50 + w, 45);
        g.drawString("Step 2: inverted matte image", 72 + 2 * w, 45);
        g.drawString("Step 3: new background image", 100 + 3 * w, 45);
        g.drawString("Step 4: result image", 125 + 4 * w, 45);


        g.drawImage(carvingImage, 25, 130 + h, w, h, this);
        g.drawImage(redImage, 25 + w + 25, 130 + h, w, h, this);
        g.drawImage(subtractedImage, 25 + w * 2 + 50, 130 + h, w, h, this);
        g.drawImage(improvedMatteImage, 25 + w * 3 + 75, 130 + h, w, h, this);
        g.drawImage(overImage, w * 4 + 125, 130 + h, w, h, this);


        g.drawString("Carving Image", 25, 120 + h);
        g.drawString("Red background", 60 + w, 120 + h);
        g.drawString("Subtracted", 82 + 2 * w, 120 + h);
        g.drawString("Improved matte", 110 + 3 * w, 120 + h);
        g.drawString("Over", 135 + 4 * w, 120 + h);


        Font f2 = new Font("Verdana", Font.BOLD, 10);
        g.setFont(f2);

        g.drawString("Difference Matting", w * 5 + 130, 250 + h);
        g.drawString("Color Difference Method", w * 5 + 130, 130);
    }
// =======================================================//

    public static void main(String[] args) {

        MatteManipulations img = new MatteManipulations();// instantiate this object
        img.repaint();// render the image

    }// end main
}
// =======================================================//
