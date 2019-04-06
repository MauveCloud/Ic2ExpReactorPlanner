/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.util.ResourceBundle;

/**
 * Utility class to handle handle access to the planner's resource bundle.
 * Methods may be static-imported, and hopefully this will make the code more
 * concise, but not to the point of obfuscation.
 * @author Brian McCloud
 */
public class BundleHelper {
    
    private BundleHelper() {
        // private no-op constructor to prevent instantiation.
    }
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle");
    
    /**
     * Looks up a key in the planner's resource bundle.
     * @param key the key to look up in the resource bundle.
     * @return the value from the resource bundle for the default locale of the system the planner is being run on.
     */
    public static String getI18n(String key) {
        return BUNDLE.getString(key);
    }
    
    /**
     * Uses a resource bundle entry as a format specifier.
     * @param key the key to look up in the resource bundle.
     * @param args the arguments to use in String.format.
     * @return the formatted string.
     */
    public static String formatI18n(String key, Object... args) {
        return String.format(getI18n(key), args);
    }
    
}
