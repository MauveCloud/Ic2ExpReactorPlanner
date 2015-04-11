package Ic2ExpReactorPlanner;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author Brian McCloud
 */
public class Simulator extends SwingWorker<Void, String> {

    private final Reactor reactor;
    
    private final JTextArea output;
    
    private final JButton[][] reactorButtons;
    
    private final boolean[][] alreadyBroken = new boolean[6][9];
    
    private final boolean[][] needsCooldown = new boolean[6][9];
    
    private final int initialHeat;
    
    private double minEUoutput = Double.MAX_VALUE;
    
    private double maxEUoutput = 0.0;
    
    private double minHeatOutput = Double.MAX_VALUE;
    
    private double maxHeatOutput = 0.0;
    
    private double stopTime = 25000;
    
    private double stopTemp = 120000;
    
    public Simulator(final Reactor reactor, final JTextArea output, final JButton[][] reactorButtons, final int initialHeat, final int stopTime, final int stopTemp) {
        this.reactor = reactor;
        this.output = output;
        this.reactorButtons = reactorButtons;
        this.initialHeat = initialHeat;
        this.stopTime = stopTime;
        this.stopTemp = stopTemp;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        long startTime = System.nanoTime();
        int reactorTicks = 0;
        int cooldownTicks = 0;
        int totalRodCount = 0;
        try {
            publish("");
            publish("Simulation started.\n");
            // Checking for temperature thresholds only
            reactor.setCurrentHeat(initialHeat);
            reactor.clearVentedHeat();
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null) {
                        component.clearCurrentHeat();
                        component.clearDamage();
                    }
                }
            }
            double minReactorHeat = initialHeat;
            double maxReactorHeat = initialHeat;
            boolean reachedBurn = initialHeat >= 0.4 * reactor.getMaxHeat();
            boolean reachedEvaporate = initialHeat >= 0.5 * reactor.getMaxHeat();
            boolean reachedHurt = initialHeat >= 0.7 * reactor.getMaxHeat();
            boolean reachedLava = initialHeat >= 0.85 * reactor.getMaxHeat();
            boolean reachedExplode = false;
            do {
                reactor.clearEUOutput();
                reactor.clearVentedHeat();
                reactorTicks++;
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            component.generateHeat();
                            maxReactorHeat = Math.max(reactor.getCurrentHeat(), maxReactorHeat);
                            minReactorHeat = Math.min(reactor.getCurrentHeat(), minReactorHeat);
                            component.dissipate();
                            maxReactorHeat = Math.max(reactor.getCurrentHeat(), maxReactorHeat);
                            minReactorHeat = Math.min(reactor.getCurrentHeat(), minReactorHeat);
                            component.transfer();
                            maxReactorHeat = Math.max(reactor.getCurrentHeat(), maxReactorHeat);
                            minReactorHeat = Math.min(reactor.getCurrentHeat(), minReactorHeat);
                        }
                        if (maxReactorHeat >= 0.4 * reactor.getMaxHeat() && !reachedBurn) {
                            publish(String.format("Reactor will reach \"Burn\" temperature at %d seconds.\n", reactorTicks));
                            reachedBurn = true;
                        }
                        if (maxReactorHeat >= 0.5 * reactor.getMaxHeat() && !reachedEvaporate) {
                            publish(String.format("Reactor will reach \"Evaporate\" temperature at %d seconds.\n", reactorTicks));
                            reachedEvaporate = true;
                        }
                        if (maxReactorHeat >= 0.7 * reactor.getMaxHeat() && !reachedHurt) {
                            publish(String.format("Reactor will reach \"Hurt\" temperature at %d seconds.\n", reactorTicks));
                            reachedHurt = true;
                        }
                        if (maxReactorHeat >= 0.85 * reactor.getMaxHeat() && !reachedLava) {
                            publish(String.format("Reactor will reach \"Lava\" temperature at %d seconds.\n", reactorTicks));
                            reachedLava = true;
                        }
                        if (maxReactorHeat >= reactor.getMaxHeat() && !reachedExplode) {
                            publish(String.format("Reactor will explode at %d seconds.\n", reactorTicks));
                            reachedExplode = true;
                        }
                    }
                }
            } while (reactor.getCurrentHeat() <= reactor.getMaxHeat() && reactorTicks <= 20000);
            publish(String.format("Reactor minimum temperature: %,.2f\n", minReactorHeat));
            publish(String.format("Reactor maximum temperature (if unchecked): %,.2f\n", maxReactorHeat));
            // full simulation
            reactorTicks = 0;
            reactor.setCurrentHeat(initialHeat);
            reactor.clearVentedHeat();
            minReactorHeat = initialHeat;
            maxReactorHeat = initialHeat;
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null) {
                        component.clearCurrentHeat();
                        component.clearDamage();
                        publish(String.format("R%dC%d:%s", row, col, component.toString()));
                        totalRodCount += component.getRodCount();
                    } else {
                        publish(String.format("R%dC%d:", row, col));
                    }
                    publish(String.format("R%dC%d:0xC0C0C0", row, col));
                }
            }
            double lastEUoutput = 0.0;
            double totalEUoutput = 0.0;
            double lastHeatOutput = 0.0;
            double totalHeatOutput = 0.0;
            double maxGeneratedHeat = 0.0;
            do {
                reactor.clearEUOutput();
                reactor.clearVentedHeat();
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null) {
                            component.preReactorTick();
                        }
                    }
                }
                double generatedHeat = 0.0;
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            generatedHeat += component.generateHeat();
                            maxReactorHeat = Math.max(reactor.getCurrentHeat(), maxReactorHeat);
                            minReactorHeat = Math.min(reactor.getCurrentHeat(), minReactorHeat);
                            component.dissipate();
                            maxReactorHeat = Math.max(reactor.getCurrentHeat(), maxReactorHeat);
                            minReactorHeat = Math.min(reactor.getCurrentHeat(), minReactorHeat);
                            component.transfer();
                            maxReactorHeat = Math.max(reactor.getCurrentHeat(), maxReactorHeat);
                            minReactorHeat = Math.min(reactor.getCurrentHeat(), minReactorHeat);
                        }
                    }
                }
                maxGeneratedHeat = Math.max(generatedHeat, maxGeneratedHeat);
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
                lastHeatOutput = reactor.getVentedHeat();
                totalHeatOutput += lastHeatOutput;
                if (reactor.getCurrentHeat() <= reactor.getMaxHeat() && lastEUoutput > 0.0) {
                    reactorTicks++;
                    minEUoutput = Math.min(lastEUoutput, minEUoutput);
                    maxEUoutput = Math.max(lastEUoutput, maxEUoutput);
                    minHeatOutput = Math.min(lastHeatOutput, minHeatOutput);
                    maxHeatOutput = Math.max(lastHeatOutput, maxHeatOutput);
                }
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && component.isBroken() && !alreadyBroken[row][col] && !component.getClass().getName().contains("FuelRod")) {
                            publish(String.format("R%dC%d:0xFF0000", row, col));
                            alreadyBroken[row][col] = true;
                            publish(String.format("R%dC%d:%s, broke after %,d seconds", row, col, component.toString(), reactorTicks));
                        }
                    }
                }
            } while (reactor.getCurrentHeat() <= reactor.getMaxHeat() && lastEUoutput > 0.0 && reactorTicks < stopTime && reactor.getCurrentHeat() < stopTemp);
            if (reactor.getCurrentHeat() <= reactor.getMaxHeat()) {
                publish(String.format("Fuel rods (if any) stopped after %,d seconds.\n", reactorTicks));
                if (reactorTicks > 0) {
                    if (reactor.isFluid()) {
                        publish(String.format("Average heat output before fuel rods stopped: %.2f Hu/s\nMinimum heat output: %.2f Hu/s\nMaximum heat output: %.2f Hu/s\n", 2 * totalHeatOutput / reactorTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
                        publish(String.format("Efficiency: %.2f average, %.2f minimum, %.2f maximum\n", totalHeatOutput / reactorTicks / 4 / totalRodCount, minHeatOutput / 4 / totalRodCount, maxHeatOutput / 4 / totalRodCount));
                    } else {
                        publish(String.format("Total EU output: %,.0f (%.2f EU/t min, %.2f EU/t max, %.2f EU/t average)\n", totalEUoutput, minEUoutput / 20.0, maxEUoutput / 20.0, totalEUoutput / (reactorTicks * 20)));
                        publish(String.format("Efficiency: %.2f average, %.2f minimum, %.2f maximum\n", totalEUoutput / reactorTicks / 100 / totalRodCount, minEUoutput / 100 / totalRodCount, maxEUoutput / 100 / totalRodCount));
                    }
                }
                lastHeatOutput = 0.0;
                totalHeatOutput = 0.0;
                double prevReactorHeat = reactor.getCurrentHeat();
                double prevTotalComponentHeat = 0.0;
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            prevTotalComponentHeat += component.getCurrentHeat();
                            if (component.getCurrentHeat() > 0.0) {
                                publish(String.format("R%dC%d:0xFFFF00", row, col));
                                publish(String.format("R%dC%d:%s, had %,.2f heat left when reactor stopped", row, col, component.toString(), component.getCurrentHeat()));
                                needsCooldown[row][col] = true;
                            }
                        }
                    }
                }
                if (prevReactorHeat == 0.0 && prevTotalComponentHeat == 0.0) {
                    output.append("No cooldown needed.\n");
                } else {
                    double currentTotalComponentHeat = prevTotalComponentHeat;
                    int reactorCooldownTime = 0;
                    do {
                        reactor.clearVentedHeat();
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
                        lastHeatOutput = reactor.getVentedHeat();
                        totalHeatOutput += lastHeatOutput;
                        minEUoutput = Math.min(lastEUoutput, minEUoutput);
                        maxEUoutput = Math.max(lastEUoutput, maxEUoutput);
                        minHeatOutput = Math.min(lastHeatOutput, minHeatOutput);
                        maxHeatOutput = Math.max(lastHeatOutput, maxHeatOutput);
                        cooldownTicks++;
                        currentTotalComponentHeat = 0.0;
                        for (int row = 0; row < 6; row++) {
                            for (int col = 0; col < 9; col++) {
                                ReactorComponent component = reactor.getComponentAt(row, col);
                                if (component != null && !component.isBroken()) {
                                    currentTotalComponentHeat += component.getCurrentHeat();
                                    if (component.getCurrentHeat() == 0.0 && needsCooldown[row][col]) {
                                        publish(String.format("R%dC%d:+, took %,d seconds to cool down", row, col, cooldownTicks));
                                        needsCooldown[row][col] = false;
                                    }
                                }
                            }
                        }
                    } while (lastHeatOutput > 0 && cooldownTicks < 20000);
                    if (reactor.getCurrentHeat() == 0.0) {
                        publish(String.format("Reactor took %,d seconds to cool down.\n", reactorCooldownTime));
                    } else {
                        publish(String.format("Reactor remained at %,.2f heat even after cool down period of %,d seconds.\n", reactor.getCurrentHeat(), reactorCooldownTime));
                    }
                    publish(String.format("Other components took %,d seconds to cool down (as much as they would).\n", cooldownTicks));
                    for (int row = 0; row < 6; row++) {
                        for (int col = 0; col < 9; col++) {
                            ReactorComponent component = reactor.getComponentAt(row, col);
                            if (component != null && !component.isBroken()) {
                                prevTotalComponentHeat += component.getCurrentHeat();
                                if (component.getCurrentHeat() > 0.0) {
                                    publish(String.format("R%dC%d:0xFFA500", row, col));
                                    publish(String.format("R%dC%d:+, had %,.2f heat left after cooldown period", row, col, component.getCurrentHeat()));
                                }
                            }
                        }
                    }
                }
            } else {
                publish(String.format("Reactor overheated at %,d seconds.\n", reactorTicks));
            }
            if (reactor.isFluid() && reactor.getCurrentHeat() < reactor.getMaxHeat()) {
                publish(String.format("Average heat output after fuel rods stopped: %.2f Hu/s\nMinimum heat output: %.2f Hu/s\nMaximum heat output: %.2f Hu/s\n", 2 * totalHeatOutput / cooldownTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
            }
            double totalEffectiveVentCooling = 0.0;
            double totalVentCoolingCapacity = 0.0;
            double totalCellCooling = 0.0;
            double totalCondensatorCooling = 0.0;
            
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null) {
                        if (component.getVentCoolingCapacity() > 0) {
                            publish(String.format("R%dC%d:+ (%.2f of %.2f cooling)", row, col, component.getEffectiveVentCooling(), component.getVentCoolingCapacity()));
                            totalEffectiveVentCooling += component.getEffectiveVentCooling();
                            totalVentCoolingCapacity += component.getVentCoolingCapacity();
                        } else if (component.getBestCellCooling() > 0) {
                            publish(String.format("R%dC%d:+ (received at most %.2f heat per reactor tick)", row, col, component.getBestCellCooling()));
                            totalCellCooling += component.getBestCellCooling();
                        } else if (component.getBestCondensatorCooling() > 0) {
                            publish(String.format("R%dC%d:+ (received at most %.2f heat per reactor tick)", row, col, component.getBestCondensatorCooling()));
                            totalCondensatorCooling += component.getBestCondensatorCooling();
                        }
                    }
                }
            }
                    
            publish(String.format("Total Vent Cooling: %,.2f of %,.2f\n", totalEffectiveVentCooling, totalVentCoolingCapacity));
            publish(String.format("Total Cell Cooling: %,.2f\n", totalCellCooling));
            publish(String.format("Total Condensator Cooling: %,.2f\n", totalCondensatorCooling));
            publish(String.format("Max Heat Generated: %.2f\n", maxGeneratedHeat));
            publish(String.format("Reactor maximum temperature (when limited by settings): %,.2f\n", maxReactorHeat));
            double totalCooling = totalEffectiveVentCooling + totalCellCooling + totalCondensatorCooling;
            if (totalCooling >= maxGeneratedHeat) {
                publish(String.format("Excess cooling: %.2f\n", totalCooling - maxGeneratedHeat));
            } else {
                publish(String.format("Excess heating: %.2f\n", maxGeneratedHeat - totalCooling));
            }
            //return null;
        } catch (Throwable e) {
            if (cooldownTicks == 0) {
                publish(String.format("Error at reactor tick %d\n", reactorTicks));
            } else {
                publish(String.format("Error at cooldown tick %d\n", cooldownTicks));
            }
            publish(e.toString(), " ", Arrays.toString(e.getStackTrace()));
        }
        long endTime = System.nanoTime();
        publish(String.format("Simulation took %.2f seconds.\n", (endTime - startTime) / 1e9));
        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        if (!isCancelled()) {
            for (String chunk : chunks) {
                if (chunk.isEmpty()) {
                    output.setText("");
                } else {
                    if (chunk.matches("R\\dC\\d:.*")) {
                        String temp = chunk.substring(5);
                        int row = chunk.charAt(1) - '0';
                        int col = chunk.charAt(3) - '0';
                        if (temp.startsWith("0x")) {
                            reactorButtons[row][col].setBackground(Color.decode(temp));
                        } else if (temp.startsWith("+")) {
                            reactorButtons[row][col].setToolTipText(reactorButtons[row][col].getToolTipText() + temp.substring(1));
                        } else {
                            reactorButtons[row][col].setToolTipText(temp);
                        }
                    } else {
                        output.append(chunk);
                    }
                }
            }
        }
    }
    
}
