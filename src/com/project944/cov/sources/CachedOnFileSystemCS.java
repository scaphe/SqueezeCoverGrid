package com.project944.cov.sources;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.project944.cov.CoverDetails;
import com.project944.cov.utils.MyLogger;
import com.project944.cov.utils.MyProgressTracker;
import com.project944.cov.utils.PropsUtils;

/**
 * Can be used to read/write covers to file system
 */
public class CachedOnFileSystemCS implements CoverSource {

    private CoverDetailsJsonSerializer ser;

    public CachedOnFileSystemCS() {
        ser = new CoverDetailsJsonSerializer();
    }

    private File getImageFile(int id) {
        File imagesDir = new File(PropsUtils.getCacheDir() + "/images");
        if ( !imagesDir.exists() ) {
            imagesDir.mkdir();
        }
        return new File(PropsUtils.getCacheDir() + "/images/" + id + ".png");
    }
    
    private File getCoversFile() {
        return new File(PropsUtils.getCacheDir() + "/covers.json");
    }

    public List<CoverDetails> getCovers(MyProgressTracker logger) {
        // Read back all tracks as saved
        // Read back all images as we go
        List<CoverDetails> covers = new LinkedList<CoverDetails>();
        
        try {
            // Read file with all json cover details in it
            File coversFile = getCoversFile();
            if ( coversFile.isFile() ) {
                // Read back image
                InputStreamReader is = new InputStreamReader(new FileInputStream(coversFile), "UTF-8");
                BufferedReader in = new BufferedReader(is); // new FileReader(coversFile));
                while ( true ) {
                    String line = in.readLine();
                    if ( line == null ) {
                        break;  
                    }
                    JSONObject json = new JSONObject(new JSONTokener(line));
                    CoverDetails cover = ser.deserialize(json);
                    covers.add(cover);
                    // System.out.println("Read cached cover of "+cover.getAlbum());
                }
                CoverDetails.fixMultiDiscAlbums(covers);

                logger.log("Loading images");
                long start = System.currentTimeMillis();
                boolean loadImages = true;
                if ( loadImages ) {
                    int count = 0;
                    logger.setMinMax(0, covers.size());
                    for (CoverDetails cover : covers) {
                        logger.setProgress(count);
                        if ( (count % 50) == 0 ) {
                            logger.log("Loaded "+count+" out of "+covers.size());
                        }
                        count++;
                        int id = cover.getId();
                        File imageFile = getImageFile(id);
                        BufferedImage image = null;
                        try {
                            image = ImageIO.read(imageFile);
                            cover.setImage(image);
                        } catch (IOException e) {
                            logger.log("Failed to read image from "+imageFile+" with "+e);
                            e.printStackTrace();
                        }
                    }
                }
                logger.log("Loaded "+covers.size()+" images in "+(System.currentTimeMillis()-start)+"ms");
            }
        } catch (Exception e) {
            System.out.println("Failed to read back file with "+e);
            e.printStackTrace();
        }
        return covers;
    }

    public void saveCovers(List<CoverDetails> covers, MyLogger logger) throws Exception {
        File coversFile = getCoversFile();
        FileOutputStream fos = new FileOutputStream(coversFile);
        if ( !coversFile.exists() || coversFile.isFile() ) {
            for (CoverDetails cover : covers) {
                JSONObject json = ser.serialize(cover);
                StringWriter sw = new StringWriter();
                json.write(sw);
                fos.write((sw+"\n").getBytes("UTF-8"));
                int id = cover.getId();
                Image image = cover.getImage();
                if (image != null) {
                    File imageFile = getImageFile(id);
                    final BufferedImage bufImage;
                    if ( image instanceof BufferedImage ) {
                        bufImage = (BufferedImage) image;
                    } else {
                        bufImage = imageToBufferedImage(image);
                    }
                    try {
                        ImageIO.write(bufImage, "png", imageFile);
                    } catch (IOException e) {
                        logger.log("Failed to write file "+imageFile+" with "+e);
                        e.printStackTrace();
                    }
                }
            }
        }
        fos.close();
    }

    public static BufferedImage imageToBufferedImage(Image im) {
        BufferedImage bi = new BufferedImage
           (im.getWidth(null),im.getHeight(null),BufferedImage.TYPE_INT_RGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
     }

    
    public List<CoverDetails> refreshFromServer(List<CoverDetails> prevCovers, boolean forceRefreshAttempt, MyProgressTracker logger) {
        throw new UnsupportedOperationException("no server to refresh from?");
    }

    public static void main(String[] args) {
        System.out.println(System.getenv("HOME"));
    }
}
