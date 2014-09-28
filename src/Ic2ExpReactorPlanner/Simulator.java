package Ic2ExpReactorPlanner;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author Brian McCloud
 */
public class Simulator extends SwingWorker<Void, Void> {

    private final Reactor reactor;
    
    private final JTextArea output;
    
    private final JButton[][] reactorButtons;
    
    private final int initialHeat;
    
    public Simulator(final Reactor reactor, final JTextArea output, final JButton[][] reactorButtons, final int initialHeat) {
        this.reactor = reactor;
        this.output = output;
        this.reactorButtons = reactorButtons;
        this.initialHeat = initialHeat;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        long startTime = System.nanoTime();
        output.setText("Simulation started.\n");
        reactor.setCurrentHeat(initialHeat);
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                ReactorComponent component = reactor.getComponentAt(row, col);
                if (component != null) {
                    component.clearCurrentHeat();
                    component.clearDamage();
                    reactorButtons[row][col].setToolTipText(component.toString());
                } else {
                    reactorButtons[row][col].setToolTipText(null);
                }
                reactorButtons[row][col].setBackground(Color.LIGHT_GRAY);
            }
        }
        double lastEUoutput = 0.0;
        double totalEUoutput = 0.0;
        int reactorTicks = 0;
        do {
            reactor.clearEUOutput();
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null && !component.isBroken()) {
                        component.generateHeat();
                        component.dissipate();
                        component.transfer();
                    }
                }
            }
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null && !component.isBroken()) {
                        component.generateEnergy();
                    }
                }
            }
            lastEUoutput = reactor.getCurrentEUoutput();
            totalEUoutput += lastEUoutput;
            reactorTicks++;
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null && component.isBroken() && !Color.RED.equals(reactorButtons[row][col].getBackground())  && !component.getClass().getName().contains("FuelRod")) {
                        reactorButtons[row][col].setBackground(Color.RED);
                        reactorButtons[row][col].setToolTipText(String.format("%s, broke after %,d seconds", component.toString(), reactorTicks));
                    }
                }
            }
        } while (reactor.getCurrentHeat() <= reactor.getMaxHeat() && lastEUoutput > 0.0);
        if (lastEUoutput == 0.0) {
            output.append(String.format("Reactor stopped outputting after %,d seconds.\n", reactorTicks));
            double prevReactorHeat = reactor.getCurrentHeat();
            double prevTotalComponentHeat = 0.0;
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null && !component.isBroken()) {
                        prevTotalComponentHeat += component.getCurrentHeat();
                        if (component.getCurrentHeat() > 0.0) {
                            reactorButtons[row][col].setBackground(Color.YELLOW);
                            reactorButtons[row][col].setToolTipText(String.format("%s, had %,.2f heat left when reactor stopped", component.toString(), component.getCurrentHeat()));
                        }
                    }
                }
            }
            if (prevReactorHeat == 0.0 && prevTotalComponentHeat == 0.0) {
                output.append("No cooldown needed.\n");
            } else {
                double currentTotalComponentHeat = prevTotalComponentHeat;
                int reactorCooldownTime = 0;
                int cooldownTicks = 0;
                do {
                    prevReactorHeat = reactor.getCurrentHeat();
                    if (prevReactorHeat == 0.0) {
                        reactorCooldownTime = cooldownTicks;
                    }
                    prevTotalComponentHeat = currentTotalComponentHeat;
                    for (int row = 0; row < 6; row++) {
                        for (int col = 0; col < 9; col++) {
                            ReactorComponent component = reactor.getComponentAt(row, col);
                            if (component != null && !component.isBroken()) {
                                component.dissipate();
                                component.transfer();
                            }
                        }
                    }
                    cooldownTicks++;
                    currentTotalComponentHeat = 0.0;
                    for (int row = 0; row < 6; row++) {
                        for (int col = 0; col < 9; col++) {
                            ReactorComponent component = reactor.getComponentAt(row, col);
                            if (component != null && !component.isBroken()) {
                                currentTotalComponentHeat += component.getCurrentHeat();
                                if (component.getCurrentHeat() == 0.0 && Color.YELLOW.equals(reactorButtons[row][col].getBackground()) && !reactorButtons[row][col].getToolTipText().contains("cool down")) {
                                    reactorButtons[row][col].setToolTipText(String.format("%s, took %,d seconds to cool down", reactorButtons[row][col].getToolTipText(), cooldownTicks));
                                }
                            }
                        }
                    }
                } while (prevReactorHeat != reactor.getCurrentHeat() || prevTotalComponentHeat != currentTotalComponentHeat);
                if (reactor.getCurrentHeat() == 0.0) {
                    output.append(String.format("Reactor took %,d seconds to cool down.\n", reactorCooldownTime));
                } else {
                    output.append(String.format("Reactor remained at %,.2f heat after cool down period.\n", reactor.getCurrentHeat()));
                }                
                output.append(String.format("Other components took %,d seconds to cool down (as much as they would).\n", cooldownTicks));
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            prevTotalComponentHeat += component.getCurrentHeat();
                            if (component.getCurrentHeat() > 0.0) {
                                reactorButtons[row][col].setBackground(Color.ORANGE);
                                reactorButtons[row][col].setToolTipText(String.format("%s, had %,.2f heat left after cooldown period", reactorButtons[row][col].getToolTipText(), component.getCurrentHeat()));
                            }
                        }
                    }
                }
            }
        } else if (reactor.getCurrentHeat() >= reactor.getMaxHeat()) {
            output.append(String.format("Reactor overheated at %,d seconds.\n", reactorTicks));
        } else {
            output.append(String.format("Reactor stopped for an unknown reason at %d seconds.\n", reactorTicks));
        }
        output.append(String.format("Total EU output: %,.0f (%.2f EU/t average)\n", totalEUoutput, totalEUoutput / (reactorTicks * 20)));
        long endTime = System.nanoTime();
        output.append(String.format("Simulation took %.2f seconds.\n", (endTime - startTime) / 1e9));
        return null;
    }
    
}
