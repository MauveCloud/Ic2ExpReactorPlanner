/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

/**
 * Represents a clipboard selection of html text, to allow pasting into editors that support it, such as OpenOffice Writer.
 * Loosely based on StringSelection from the standard Java API.
 * @author Brian McCloud
 */
public class HtmlSelection implements Transferable, ClipboardOwner {

    private final DataFlavor htmlFlavor = new DataFlavor("text/html; charset=utf-8", null);
    private final DataFlavor plainFlavor = new DataFlavor("text/plain; charset=utf-8", null);
    private final DataFlavor rtfFlavor = new DataFlavor("text/rtf", null);
    
    private final String data;
    
    public HtmlSelection(final String data) {
        this.data = data;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {
                htmlFlavor,
                plainFlavor,
                rtfFlavor
            };
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return flavor.equals(htmlFlavor) || flavor.equals(plainFlavor) || flavor.equals(rtfFlavor);
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(htmlFlavor)) {
            return new ByteArrayInputStream(data.getBytes("utf-8"));
        } else if (flavor.equals(plainFlavor)) {
            return new ByteArrayInputStream(data.replace("<br>", "\n").replaceAll("<[^>]+>", "").getBytes("utf-8"));
        } else if (flavor.equals(rtfFlavor)) {
            return new ByteArrayInputStream(convertToRTF(data).getBytes("us-ascii"));
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        // no-op
    }
    
    // modified from https://stackoverflow.com/questions/2091803/how-to-convert-html-to-rtf-in-java
    // only tested to handle html tags expected to be output by the planner's comparison feature.
    private static String convertToRTF(final String htmlStr) {

        OutputStream os = new ByteArrayOutputStream();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        RTFEditorKit rtfEditorKit = new RTFEditorKit();
        String rtfStr = null;

        String tempStr = htmlStr.replace("</font>", "#END_FONT#").replace("<br>", "#NEW_LINE#");
        InputStream is = new ByteArrayInputStream(tempStr.getBytes());
        try {
            Document doc = htmlEditorKit.createDefaultDocument();
            htmlEditorKit.read(is, doc, 0);
            rtfEditorKit.write(os, doc, 0, doc.getLength());
            rtfStr = os.toString();
            rtfStr = rtfStr.replace("#NEW_LINE#", "\\line ");
            rtfStr = rtfStr.replace("#END_FONT#", "\\cf0 ");
        } catch (IOException | BadLocationException e) {
            e.printStackTrace();
        }
        return rtfStr;
    }
    
}
