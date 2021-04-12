//IAT455 - Workshop week 9

//**********************************************************/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.lang.String;

import javax.imageio.ImageIO;

class Week8 extends Frame {  //controlling class
    BufferedImage carImage;  //reference to an Image object
    BufferedImage paint_mask;  //mask for the painted areas
    BufferedImage reflection_layer;
    BufferedImage occlusion;

    int width; //width of the resized image
    int height; //height of the resized image

    BufferedImage color_corrected;
    BufferedImage paint_changed_mask;
    BufferedImage newColor_image;
    BufferedImage paint_mask_refl;
    BufferedImage shadows;
    BufferedImage refl;
    BufferedImage shiny;
    BufferedImage comp;

    public Week8() {
        // constructor
        // Get an image from the specified file in the current directory on the
        // local hard disk.
        try {
            carImage = ImageIO.read(new File("car_cg.jpg")); //1
            paint_mask = ImageIO.read(new File("painted_areas_mask.jpg")); //2
            reflection_layer = ImageIO.read(new File("reflections_layer.jpg")); //3
            occlusion = ImageIO.read(new File("ambient_occlusion_layer.jpg")); //4

        } catch (Exception e) {
            System.out.println("Cannot load the provided image");
        }
        this.setTitle("Week 8 workshop");
        this.setVisible(true);

        width = carImage.getWidth();//
        height = carImage.getHeight();//

        // For images 5 to 12 you should replace the carImage with the proper methods that perform the requested task

        // 5 Adding shadows to car
        shadows = combineImages(carImage, occlusion, Operations.multiply);

        // 6 Creating a new image of the desired color
        newColor_image = createUniformColor(height, width, carImage.getType(), 157, 19, 35);

        // 7 Creating the colored paint mask
        paint_changed_mask = combineImages(paint_mask, newColor_image, Operations.multiply);

        // 8 Performing over operation
        color_corrected = over(paint_mask, shadows, paint_changed_mask);

        //9 Reflection mask
        paint_mask_refl = combineImages(paint_changed_mask, reflection_layer, Operations.multiply);

        //10 Over operation like step 8, but with colored reflection mask
        refl = over(paint_mask, shadows, paint_mask_refl);

        //11 Over operation like step 8, but with reflection mask
        shiny = over(paint_mask, shadows, reflection_layer);

        //12 Over operation like step 8, but using a dark color
        BufferedImage dark = createUniformColor(height, width, carImage.getType(), 1, 0, 0);
        BufferedImage changedPaintMask2 = combineImages(dark, paint_mask, Operations.multiply);
        comp = over(paint_mask, shadows, changedPaintMask2);


        //Anonymous inner-class listener to terminate program
        this.addWindowListener(
                new WindowAdapter() {//anonymous class definition
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);//terminate the program
                    }//end windowClosing()
                }//end WindowAdapter
        );//end addWindowListener
    }// end constructor

    public BufferedImage combineImages(BufferedImage src1, BufferedImage src2, Operations op) {

        if (src1.getType() != src2.getType()) {
            System.out.println("Source Images should be of the same type");
            return null;
        }
        BufferedImage result = new BufferedImage(src1.getWidth(),
                src1.getHeight(), src1.getType());

        for (int i = 0; i < src1.getWidth(); i++) {
            for (int j = 0; j < src2.getHeight(); j++) {
                int rgb1 = src1.getRGB(i, j);
                int rgb2 = src2.getRGB(i, j);

                int r1 = getRed(rgb1);
                int r2 = getRed(rgb2);

                int g1 = getGreen(rgb1);
                int g2 = getGreen(rgb2);

                int b1 = getBlue(rgb1);
                int b2 = getBlue(rgb2);

                int newR, newG, newB;

                if (op == Operations.multiply) {
                    newR = clip(r1 * r2 / 255);
                    newG = clip(g1 * g2 / 255);
                    newB = clip(b1 * b2 / 255);
                    result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
                } else if (op == Operations.add) {
                    newR = clip(r1 + r2);
                    newG = clip(g1 + g2);
                    newB = clip(b1 + b2);
                    result.setRGB(i, j, new Color(newR, newG, newB).getRGB());
                }
            }
        }

        return result;
    }

    public BufferedImage createUniformColor(int height, int width, int imageType, int red, int green, int blue) {
        BufferedImage result = new BufferedImage(width, height, imageType);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                result.setRGB(i, j, new Color(red, green, blue).getRGB());
            }
        }
        return result;
    }

    public BufferedImage over(BufferedImage paintMatte, BufferedImage shadowsAdded, BufferedImage changedPaintMask) {
        BufferedImage invertedMatte = invert(paintMatte);
        BufferedImage bg = combineImages(shadowsAdded, invertedMatte, Operations.multiply);
        BufferedImage result = combineImages(bg, changedPaintMask, Operations.add);
        return result; // CHANGE THIS!
    }

    public BufferedImage invert(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(),
                src.getHeight(), src.getType());

        for (int i = 0; i < src.getWidth(); i++) {
            for (int j = 0; j < src.getHeight(); j++) {
                int rgb = src.getRGB(i, j);

                int red = getRed(rgb);
                int green = getGreen(rgb);
                int blue = getBlue(rgb);

                int new_red = 255 - red;
                int new_green = 255 - green;
                int new_blue = 255 - blue;

                result.setRGB(i, j, new Color(new_red, new_green, new_blue).getRGB());
            }
        }
        return result;
    }

    private int clip(int v) {
        v = v > 255 ? 255 : v;
        v = v < 0 ? 0 : v;
        return v;
    }

    protected int getRed(int pixel) {
        return (pixel >>> 16) & 0xFF;
    }

    protected int getGreen(int pixel) {
        return (pixel >>> 8) & 0xFF;
    }

    protected int getBlue(int pixel) {
        return pixel & 0xFF;
    }

    public void paint(Graphics g) {
        int w = width / 5;
        int h = height / 5;


        this.setSize(w * 5 + 100, h * 4 + 50);

        g.drawImage(carImage, 25, 50, w, h, this);
        g.drawImage(occlusion, 100 + w, 50, w, h, this);
        g.drawImage(paint_mask, 200 + w * 2, 50, w, h, this);
        g.drawImage(reflection_layer, 300 + w * 3, 50, w, h, this);

        g.drawImage(shadows, 25, 50 + h + 50, w, h, this);
        g.drawImage(newColor_image, 100 + w, 50 + h + 50, w, h, this);
        g.drawImage(paint_changed_mask, 200 + w * 2, 50 + h + 50, w, h, this);
        g.drawImage(color_corrected, 300 + w * 3, 50 + h + 50, w, h, this);

        g.drawImage(paint_mask_refl, 25, 50 + 2 * h + 100, w, h, this);
        g.drawImage(refl, 100 + w, 50 + 2 * h + 100, w, h, this);
        g.drawImage(shiny, 200 + w * 2, 50 + 2 * h + 100, w, h, this);
        g.drawImage(comp, 300 + w * 3, 50 + 2 * h + 100, w, h, this);

        g.setColor(Color.BLACK);
        Font f1 = new Font("Verdana", Font.PLAIN, 13);
        g.setFont(f1);

        g.drawString("1.Original CG image", 25, 40);
        g.drawString("2.Ambient occlusion mask", 125 + w, 40);
        g.drawString("3.Painted areas mask", 225 + w * 2, 40);
        g.drawString("4.Reflections layer", 325 + w * 3, 40);

        g.drawString("5.Shadows added", 25, 50 + h + 40);
        g.drawString("6.New color", 125 + w, 50 + h + 40);
        g.drawString("7.Mask - changed paint color", 225 + w * 2, 50 + h + 40);
        g.drawString("8.Changed color - shadows ok", 325 + w * 3, 50 + h + 40);

        g.drawString("9.Mask-changed color+reflections", 10, 50 + 2 * h + 90);
        g.drawString("10.Changed color-shadows,reflections", 85 + w, 50 + 2 * h + 90);
        g.drawString("11.Reflections", 200 + w * 2, 50 + 2 * h + 90);
        g.drawString("12.Another color-no reflections", 300 + w * 3, 50 + 2 * h + 90);
    }
    //=======================================================//

    public static void main(String[] args) {

        Week8 img = new Week8();//instantiate this object
        img.repaint();//render the image

    }//end main
}
//=======================================================//