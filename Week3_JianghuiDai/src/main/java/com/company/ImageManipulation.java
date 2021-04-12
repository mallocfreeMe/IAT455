package com.company;

/*File ImageBasics.java

 IAT455 - Workshop week 3
 Basic Image Manipulation
 
 Starter code
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
import javax.swing.plaf.BorderUIResource;

class ImageManipulation extends Frame {
    BufferedImage testImage;
    BufferedImage testImage1;

    BufferedImage brightnessImage;
    BufferedImage RGBmultiplyImage;
    BufferedImage invertImage;
    BufferedImage contrastImage;
    BufferedImage monochrome1Image;
    BufferedImage monochrome2Image;
    BufferedImage edgeDetectionImage;

    int width; // width of the image
    int height; // height of the image

    int width1; // width of the image
    int height1; // height of the image

    public ImageManipulation() {
        // constructor
        // Get an image from the specified file in the current directory on the
        // local hard disk.
        try {
            testImage = ImageIO.read(new File("bird1.jpg"));
            testImage1 = ImageIO.read(new File("church.jpg"));

        } catch (Exception e) {
            System.out.println("Cannot load the provided image");
        }
        this.setTitle("Week 3 workshop - Basic image manipulation");
        this.setVisible(true);

        width = testImage.getWidth();
        height = testImage.getHeight();

        width1 = testImage1.getWidth();
        height1 = testImage1.getHeight();

        brightnessImage = filterImage(testImage, Filters.brightness);
        RGBmultiplyImage = filterImage(testImage, Filters.RGBmultiply);
        invertImage = filterImage(testImage, Filters.invert);
        contrastImage = filterImage(testImage, Filters.contrast);
        monochrome1Image = filterImage(testImage, Filters.monochrome_average);
        monochrome2Image = filterImage(testImage, Filters.monochrome_perceptual);

        edgeDetectionImage = convolve(testImage1);

        //Anonymous inner-class listener to terminate program
        this.addWindowListener(
                new WindowAdapter() {//anonymous class definition
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);//terminate the program
                    }//end windowClosing()
                }//end WindowAdapter
        );//end addWindowListener
    }// end constructor

    public BufferedImage filterImage(BufferedImage img, Filters filt)
    //produce the result image for each operation
    {
        int width = img.getWidth();
        int height = img.getHeight();

        WritableRaster wRaster = img.copyData(null);
        BufferedImage copy = new BufferedImage(img.getColorModel(), wRaster, img.isAlphaPremultiplied(), null);

        //apply the operation to each pixel
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = img.getRGB(i, j);
                copy.setRGB(i, j, filterPixel(rgb, filt));
            }
        }
        return copy;
    }

    public int filterPixel(int rgb, Filters filt) { //operation to be applied to each pixel

        int alpha = (rgb >>> 24) & 0xff;
        int red = (rgb >>> 16) & 0xff;
        int green = (rgb >>> 8) & 0xff;
        int blue = rgb & 0xff;

        int nBlue, nRed, nGreen;
        switch (filt) {
            case brightness:  //O = I*2
                //write code
                if (red * 2 > 255)
                    nRed = 255;
                else
                    nRed = red * 2;
                nBlue = (blue * 2 > 255) ? 255 : (blue * 2);
                nGreen = (green * 2 > 255) ? 255 : (green * 2);
                return new Color(nRed, nGreen, nBlue, alpha).getRGB();
            case RGBmultiply: //R=R*0.1, G=G*1.25, B=B*1
                //write code
                nRed = (int) (red * 0.1);
                nGreen = (int) ((green * 1.25 > 255) ? 255 : green * 1.25);
                nBlue = blue;
                return new Color(nRed, nGreen, nBlue, alpha).getRGB();
            case invert: //O=1=I
                //write code
                return new Color(255 - red, 255 - green, 255 - blue, alpha).getRGB();
            case contrast: //O=(I-0.33)*3
                //write code
                nRed = increaseContrast(red);
                nBlue = increaseContrast(blue);
                nGreen = increaseContrast(green);
                return new Color(nRed, nGreen, nBlue, alpha).getRGB();
            case monochrome_average: //average R, G, B
                //write code
                int mono = (red + green + blue) / 3;
                return new Color(mono, mono, mono, alpha).getRGB();
            case monochrome_perceptual: //human eye perception values
                //write code
                mono = (int) (red * 0.309 + green * 0.609 + blue * 0.082);
                return new Color(mono, mono, mono, alpha).getRGB();
            case blank_image:
                return rgb | 0xFFFFFFFF;
            default:
                return rgb | 0xFFFFFFFF;
        }
    }

    // helper func for contrast filter
    private int increaseContrast(int value) {
        int val = (int) ((value - 0.33 * 255) * 3);
        if (val > 255) {
            val = 255;
        }
        if (val < 0) {
            val = 0;
        }
        return val;
    }

    // helper func for edge detection filter
    // apply edge detection filter [-1,-1,-1,-1,8,-1,-1,-1,-1]
    private int computerConvolve(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9) {
        int result = p1 * -1 + p2 * -1 + p3 * -1 + p4 * -1 + p5 * 8 + p6 * -1 + p7 * -1 + p8 * -1 + p9 * -1;
        if (result < 0) {
            result = 0;
        }
        if (result > 255) {
            result = 255;
        }
        return result;
    }

    // helper func for edge detection filter
    private int getPixel(int x, int y) {
        try {
            return testImage1.getRGB(x, y);
        } catch (Exception e) {
            return 0;
        }
    }

    // helper func for edge detection filter
    private int getRed(int rgb) {
        return new Color(rgb).getRed();
    }

    // helper func for edge detection filter
    private int getGreen(int rgb) {
        return new Color(rgb).getGreen();
    }

    // helper func for edge detection filter
    private int getBlue(int rgb) {
        return new Color(rgb).getBlue();
    }

    // helper func for edge detection filter
    private int computerConvolveChannel(int[] values) {
        return computerConvolve(values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8]);
    }

    // Edge detection algorithm - spatial filtering by implementing the moving window manually
    public BufferedImage convolve(BufferedImage image) {
        //write algorithm to perform edge detection based on spatial convolution, as described in lecture/textbook
        //return a Bufferedimage = edgeDetectionImage
        int width = image.getWidth();
        int height = image.getHeight();

        WritableRaster wRaster = image.copyData(null);
        BufferedImage copy = new BufferedImage(image.getColorModel(), wRaster, image.isAlphaPremultiplied(), null);

        int[] pixels = new int[9];
        int[] red = new int[9];
        int[] green = new int[9];
        int[] blue = new int[9];
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[0] = getPixel(i - 1, j - 1);
                pixels[1] = getPixel(i, j - 1);
                pixels[2] = getPixel(i + 1, j - 1);

                pixels[3] = getPixel(i - 1, j);
                pixels[4] = getPixel(i, j);
                pixels[5] = getPixel(i + 1, j);

                pixels[6] = getPixel(i - 1, j + 1);
                pixels[7] = getPixel(i, j + 1);
                pixels[8] = getPixel(i + 1, j + 1);

                for (int p = 0; p < pixels.length; p++) {
                    red[p] = getRed(pixels[p]);
                    green[p] = getGreen(pixels[p]);
                    blue[p] = getBlue(pixels[p]);
                }

                int cRed = computerConvolveChannel(red);
                int cBlue = computerConvolveChannel(blue);
                int cGreen = computerConvolveChannel(green);
                int cColor = new Color(cRed, cBlue, cGreen).getRGB();
                copy.setRGB(i, j, cColor);
            }
        }

        return copy;
    }

    public void paint(Graphics g) {

        //if working with different images, this may need to be adjusted
        int w = width / 3;
        int h = height / 3;

        this.setSize(w * 5 + 300, h * 3 + 150);

        g.drawImage(testImage, 25, 50, w, h, this);
        g.drawImage(brightnessImage, 25 + w + 25, 50, w, h, this);
        g.drawImage(RGBmultiplyImage, 25 + w * 2 + 50, 50, w, h, this);
        g.drawImage(invertImage, 25 + w * 3 + 75, 50, w, h, this);
        g.drawImage(contrastImage, w * 4 + 125, 50, w, h, this);

        g.drawImage(monochrome1Image, 25, h + 30 + 250, w, h, this);
        g.drawImage(monochrome2Image, 25 + w + 25, h + 30 + 250, w, h, this);

        g.setColor(Color.BLACK);
        Font f1 = new Font("Verdana", Font.PLAIN, 13);
        g.setFont(f1);
        g.drawString("Original image", 25, 45);
        g.drawString("Brightness x2.0", 50 + w, 45);
        g.drawString("RGB Multiply 0.1, 1.25, 1", 72 + 2 * w, 45);
        g.drawString("Invert", 100 + 3 * w, 45);
        g.drawString("Contrast", 125 + 4 * w, 45);

        g.drawString("Monochrome 1", 25, 45 + h + 220);
        g.drawString("Monochrome 2", 50 + w, 45 + h + 220);

        g.drawString("Monochrome 1 - based on averaging red, green, blue", 15, h + h / 2 + 60);
        g.drawString("Monochrome 2 - based on human perception of colors:", 15, h + h / 2 + 90);
        g.drawString("R*0.309+G*0.609+B*0.082", 15, h + h / 2 + 60 + 60);

        g.drawString("Edge detection - based on spatial convolution", w * 2 + 170, 20 + h + 100);

        g.drawImage(testImage1, w * 2 + 150, 50 + h + 100, width1 / 2, height1 / 2, this);
        g.drawImage(edgeDetectionImage, w * 2 + 180 + width1 / 2, 50 + h + 100, width1 / 2, height1 / 2, this);
    }
// =======================================================//

    public static void main(String[] args) {

        ImageManipulation img = new ImageManipulation();// instantiate this object
        img.repaint();// render the image

    }// end main
}
