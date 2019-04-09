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
import java.io.IOException;

/**
 * Represents a clipboard selection of html text, to allow pasting into editors that support it, such as OpenOffice Writer.
 * Loosely based on StringSelection from the standard Java API.
 * @author Brian McCloud
 */
public class HtmlSelection implements Transferable, ClipboardOwner {

    private final DataFlavor htmlFlavor = new DataFlavor("text/html; charset=utf-8", null);
    private final DataFlavor plainFlavor = new DataFlavor("text/plain; charset=utf-8", null);
    
    private final String data;
    
    public HtmlSelection(final String data) {
        this.data = data;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {
                htmlFlavor,
                plainFlavor
            };
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return flavor.equals(htmlFlavor) || flavor.equals(plainFlavor);
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(htmlFlavor)) {
            return new ByteArrayInputStream(data.getBytes("utf-8"));
        } else if (flavor.equals(plainFlavor)) {
            return new ByteArrayInputStream(data.replace("<br>", "\n").replaceAll("<[^>]+>", "").getBytes("utf-8"));
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        // no-op
    }
    
}
