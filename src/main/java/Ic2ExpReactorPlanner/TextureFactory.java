/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Brian McCloud
 */
public class TextureFactory {
    
    private TextureFactory() { }
    
    public static Image getImage(String imageName) {
//        String resourcePackPath = Preferences.userRoot().get("Ic2ExpReactorPlanner.ResourcePack", null);
//        if (resourcePackPath != null && new File(resourcePackPath).exists()) {
//            try (ZipFile zip = new ZipFile(resourcePackPath)) {
//                ZipEntry entry = zip.getEntry("assets/ic2/textures/items/" + imageName);
//                if (entry != null) {
//                    InputStream entryStream = zip.getInputStream(entry);
//                    BufferedImage entryImage = ImageIO.read(entryStream);
//                    if (entryImage != null) {
//                        return entryImage;
//                    }
//                }
//                entry = zip.getEntry("assets/gregtech/textures/items/" + imageName);
//                if (entry != null) {
//                    InputStream entryStream = zip.getInputStream(entry);
//                    BufferedImage entryImage = ImageIO.read(entryStream);
//                    if (entryImage != null) {
//                        return entryImage;
//                    }
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(TextureFactory.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        
//        // Remove the resource pack preference if something caused the internal resources to be invoked.
//        Preferences.userRoot().remove("Ic2ExpReactorPlanner.ResourcePack");
        
        InputStream stream = TextureFactory.class.getResourceAsStream("/assets/ic2/textures/items/" + imageName);
        if (stream != null) {
            try {
                BufferedImage image = ImageIO.read(stream);
                return image;
            } catch (IOException ex) {
                Logger.getLogger(TextureFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        stream = TextureFactory.class.getResourceAsStream("/assets/gregtech/textures/items/" + imageName);
        if (stream != null) {
            try {
                BufferedImage image = ImageIO.read(stream);
                return image;
            } catch (IOException ex) {
                Logger.getLogger(TextureFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
}
