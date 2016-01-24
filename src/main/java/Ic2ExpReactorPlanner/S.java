package Ic2ExpReactorPlanner;

import com.gettextresourcebundle.GettextResourceBundle;
import com.gettextresourcebundle.GettextResourceBundleControl;

import java.util.ResourceBundle;

/**
 * Internationalization Support of this program.
 */
public class S {
    private static GettextResourceBundle rb = null;
    private static ResourceBundle resourceBundle = null;
    static {
        try {
            resourceBundle = ResourceBundle.getBundle("locale", GettextResourceBundleControl.getControl("ic2expreactorplanner"));
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
    public static String _(String s) {
        if(resourceBundle != null)
            return resourceBundle.getString(s);
        else
            return s;
    }
}
