package com.example.wee6_base;/*File Exercise1.java

 IAT455 - Workshop week 6

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

class Exercise1 extends Frame {
    // 1
    BufferedImage srcImage;
    BufferedImage twoBitImage;
    BufferedImage brightImage;

    // 2
    BufferedImage manImage;
    BufferedImage bestmenImage;
    BufferedImage suppressedImage;
    BufferedImage matteImage;
    BufferedImage newBackgroundImage;
    BufferedImage colorDiffImage;

    // 3
    BufferedImage coverImage;
    BufferedImage messageImage;
    BufferedImage secretImage;
    BufferedImage fourBitsImage;
    BufferedImage decodedImage;

    int width; // width of the image
    int height; // height of the image

    public Exercise1() {
        // constructor
        // Get an image from the specified file in the current directory on the
        // local hard disk.
        try {
            srcImage = ImageIO.read(new File("trees.png"));
            manImage = ImageIO.read(new File("man.jpg"));
            bestmenImage = ImageIO.read(new File("bestmen.jpg"));
            coverImage = ImageIO.read(new File("cover.jpg"));
            messageImage = ImageIO.read(new File("message.jpg"));
        } catch (Exception e) {
            System.out.println("Cannot load the provided image");
        }
        this.setTitle("Week 6 workshop");
        this.setVisible(true);

        width = srcImage.getWidth();
        height = srcImage.getHeight();

        // 1
        twoBitImage = reduceTo2Bits(srcImage); //WRITE YOUR OWN reduceTo2Bits(srcImage);
        brightImage = modifyBrightness(twoBitImage, 85); //WRITE YOUR OWN modifyBrightness(twoBitImage, 85);

        // 2
        // color difference method
        suppressedImage = suppressToBlack(manImage);
        matteImage = createInvertedMatte(manImage);
        newBackgroundImage = combineImages(matteImage, bestmenImage, Operations.multiply);
        colorDiffImage = combineImages(suppressedImage, newBackgroundImage, Operations.add);

        // 3
        secretImage = hideImage(coverImage, messageImage);
        fourBitsImage = reduceTo4Bits(secretImage);
        decodedImage = decodeImage(fourBitsImage);

        //keymixImage = keymixImages(birdImage, boardImage, matteImage);
        //premultipliedImage = combineImages(birdImage, matteImage, Operations.multiply);

        //Anonymous inner-class listener to terminate program
        this.addWindowListener(
                new WindowAdapter() {//anonymous class definition
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);//terminate the program
                    }//end windowClosing()
                }//end WindowAdapter
        );//end addWindowListener
    }// end constructor

    // 1
    private BufferedImage reduceTo2Bits(BufferedImage srcImage) {
        BufferedImage result = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), srcImage.getType());

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb = srcImage.getRGB(i, j);
                int newRGB = rgb & 0xFF030303;
                result.setRGB(i, j, newRGB);
            }
        }

        return result;
    }

    private BufferedImage modifyBrightness(BufferedImage srcImage, int value) {
        BufferedImage result = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), srcImage.getType());

        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb = srcImage.getRGB(i, j);
                int r = getRed(rgb);
                int g = getGreen(rgb);
                int b = getBlue(rgb);
                float hsb[] = Color.RGBtoHSB(r, g, b, null);
                float newBrightness = hsb[2] * value;
                int newRGB = Color.HSBtoRGB(hsb[0], hsb[1], newBrightness);
                result.setRGB(i, j, newRGB);
            }
        }

        return result;
    }

    // 2
    private BufferedImage suppressToBlack(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        // Write your code here
        for (int i = 0; i < result.getWidth(); i++) {
            for (int j = 0; j < result.getHeight(); j++) {
                int rgb = src.getRGB(i, j);
                int r = getRed(rgb);
                int g = getGreen(rgb);
                int b = getBlue(rgb);

                if(r > 50 && r < 60) {
                    r = 0;
                }

                int newG = g > b ? b : g;
                result.setRGB(i, j, new Color(r, newG, b).getRGB());
            }
        }
        return result;
    }

    private BufferedImage createInvertedMatte(BufferedImage src) {
        BufferedImage invertedMatte = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());

        // Write your code here
        for (int i = 0; i < invertedMatte.getWidth(); i++) {
            for (int j = 0; j < invertedMatte.getHeight(); j++) {
                int rgb = src.getRGB(i, j);
                int r = getRed(rgb);
                int g = getGreen(rgb);
                int b = getBlue(rgb);
                int matte = clip(g - Math.max(b, r));
                invertedMatte.setRGB(i, j, new Color(matte, matte, matte).getRGB());
            }
        }
        return invertedMatte;
    }

    private BufferedImage combineImages(BufferedImage src1, BufferedImage src2, Operations op) {
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
                }
            }
        }
        return result;
    }

    // 3
    private BufferedImage hideImage(BufferedImage srcImage1, BufferedImage srcImage2) {
        BufferedImage result = new BufferedImage(srcImage1.getWidth(), srcImage1.getHeight(), srcImage1.getType());

        for (int i = 0; i < srcImage1.getWidth(); i++) {
            for (int j = 0; j < srcImage1.getHeight(); j++) {
                int RGB1 = srcImage1.getRGB(i,j);
                int RGB2 = srcImage2.getRGB(i,j);
                int newRGB1 = RGB1 & 0XFFF0F0F0;
                int newRGB2 = RGB2 & 0XFFF0F0F0;
                String b1 = Integer.toBinaryString(newRGB1);
                String b2 = Integer.toBinaryString(newRGB2);
                int r = Integer.parseInt(b1.substring(8, 12) + b2.substring(8, 12), 2);
                int g = Integer.parseInt(b1.substring(16, 20) + b2.substring(16, 20), 2);
                int b = Integer.parseInt(b1.substring(24, 28) + b2.substring(24, 28), 2);

                result.setRGB(i, j, new Color(r, g, b).getRGB());
            }
        }

        return result;
    }

    private BufferedImage reduceTo4Bits(BufferedImage srcImage) {
        BufferedImage result = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), srcImage.getType());
        for (int i = 0; i < srcImage.getWidth(); i++) {
            for (int j = 0; j < srcImage.getHeight(); j++) {
                int rgb = srcImage.getRGB(i, j);
                int newRGB = rgb & 0xFF0F0F0F;
                result.setRGB(i, j, newRGB);
            }
        }
        return result;
    }

    private BufferedImage decodeImage(BufferedImage srcImage) {
        return modifyBrightness(srcImage, 15);
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

        this.setSize(w * 4 + 450, h * 4 + 200);

        // 1
        g.drawImage(srcImage, 25, 50, w, h, this);
        g.drawImage(twoBitImage, 25 + w + 45, 50, w, h, this);
        g.drawImage(brightImage, 25 + w * 2 + 85, 50, w, h, this);

        // 2
        g.drawImage(manImage, 25, 280, w, h, this);
        g.drawImage(bestmenImage, 25 + w + 45, 280, w, h, this);
        g.drawImage(suppressedImage, 25 + w * 2 + 85, 280, w, h, this);
        g.drawImage(matteImage, 25, 510, w, h, this);
        g.drawImage(newBackgroundImage, 25 + w + 45, 510, w, h, this);
        g.drawImage(colorDiffImage, 25 + w * 2 + 85, 510, w, h, this);

        // 3
        g.drawImage(coverImage, 25, 740, w, h, this);
        g.drawImage(messageImage, 25 + w + 45, 740, w, h, this);
        g.drawImage(secretImage, 25 + w * 2 + 85, 740, w, h, this);
        g.drawImage(fourBitsImage, 25 + w * 3 + 125, 740, w, h, this);
        g.drawImage(decodedImage, 25 + w * 4 + 165, 740, w, h, this);

        g.setColor(Color.BLACK);
        Font f1 = new Font("Verdana", Font.PLAIN, 13);
        g.setFont(f1);
        g.drawString("Original Image", 25, 45);
        g.drawString("2 bits per channel", 25 + w + 45, 45);
        g.drawString("Brightness * 85 = Result Image", 25 + w * 2 + 85, 45);
    }
// =======================================================//

    public static void main(String[] args) {

        Exercise1 img = new Exercise1();// instantiate this object
        img.repaint();// render the image

    }// end main
}
// =======================================================//
