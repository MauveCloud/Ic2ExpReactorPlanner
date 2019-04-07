/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Semi-utility class to handle showing a dialog for an exception, with a scrollable
 * area to view the stack trace.
 * @author Brian McCloud
 */
public class ExceptionDialogDisplay implements Thread.UncaughtExceptionHandler {
    
    public static void showExceptionDialog(final Throwable e) {
        // Mostly copied from https://stackoverflow.com/questions/14011492/text-wrap-in-joptionpane/14011536#14011536
        StringBuilder sb = new StringBuilder("Error: "); //NOI18N
        sb.append(e.toString());
        sb.append("\n"); //NOI18N
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n"); //NOI18N
        }
        JTextArea jta = new JTextArea(sb.toString());
        jta.setEditable(false);
        JScrollPane jsp = new JScrollPane(jta) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(480, 320);
            }
        };
        JOptionPane.showMessageDialog(null, jsp, "Error", JOptionPane.ERROR_MESSAGE); //NOI18N
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        showExceptionDialog(e);
    }
    
}
