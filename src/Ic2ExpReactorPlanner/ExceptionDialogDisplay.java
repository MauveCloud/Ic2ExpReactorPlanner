/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        // Modified from https://stackoverflow.com/questions/14011492/text-wrap-in-joptionpane/14011536#14011536
        // also used https://stackoverflow.com/questions/4812570/how-to-store-printstacktrace-into-a-string/4812589#4812589
        StringWriter errors = new StringWriter(5000);
        e.printStackTrace(new PrintWriter(errors));
        JTextArea jta = new JTextArea(errors.toString());
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
