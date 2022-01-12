/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 *
 * @author Brian McCloud
 */
public class TextureFactory {
    
    private TextureFactory() { }
    
    private static final ZipFile TEXTURE_PACK = getTexturePackZip();
    
    // paths within the texture pack zip to check for the texture images.
    private static final String[] ASSET_PATHS = {
        "",
        "assets/ic2/textures/items/",
        "assets/ic2/textures/items/reactor/",
        "assets/ic2/textures/items/reactor/fuel_rod/",
        "assets/gregtech/textures/items/",
        "assets/fm/textures/items/",
        "assets/gtnh/textures/items/",
        "assets/goodgenerator/textures/items/",
    };
    
    public static Image getImage(String... imageNames) {
        BufferedImage result = null;
        if (TEXTURE_PACK != null) {
            for (String imageName : imageNames) {
                for (String asset_path : ASSET_PATHS) {
                    if (result == null) {
                        ZipEntry entry = TEXTURE_PACK.getEntry(asset_path + imageName);
                        if (entry != null) {
                            try (InputStream entryStream = TEXTURE_PACK.getInputStream(entry)) {
                                result = ImageIO.read(entryStream);
                            } catch (IOException ex) {
                                // ignore, fall back to default texture.
                            }
                        }
                    }
                }
            }
        }
                
        for (String asset_path : ASSET_PATHS) {
            if (result == null && TextureFactory.class.getResource("/" + asset_path + imageNames[0]) != null) {
                try (InputStream stream = TextureFactory.class.getResourceAsStream("/" + asset_path + imageNames[0])) {
                    result = ImageIO.read(stream);
                } catch (IOException ex) {
                    ExceptionDialogDisplay.showExceptionDialog(ex);
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }
    
    private static ZipFile getTexturePackZip() {
        try (FileInputStream configStream = new FileInputStream("erpprefs.xml")) {
            Properties config = new Properties();
            config.loadFromXML(configStream);
            String texturePackName = config.getProperty("texturePack");
            if (texturePackName != null) {
                ZipFile result = new ZipFile(texturePackName);
                return result;
            }
        } catch (FileNotFoundException ex) {
            // ignore, this might just mean the file hasn't been created yet.
        } catch (IOException | NullPointerException ex) {
            // ignore, security settings or whatever preventing reading the xml file (or resource pack zip) should not stop the planner from running.
        }
        return null;
    }
    
}
