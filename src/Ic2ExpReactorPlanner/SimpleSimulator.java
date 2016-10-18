package Ic2ExpReactorPlanner;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 *
 * @author Brian McCloud
 */
public class SimpleSimulator extends SwingWorker<Void, String> {

    private final Reactor reactor;
    
    private final JTextArea output;
    
    private final JPanel[][] reactorButtonPanels;
    
    private final boolean[][] alreadyBroken = new boolean[6][9];
    
    private final boolean[][] needsCooldown = new boolean[6][9];
    
    private final int initialHeat;
    
    private double minEUoutput = Double.MAX_VALUE;
    
    private double maxEUoutput = 0.0;
    
    private double minHeatOutput = Double.MAX_VALUE;
    
    private double maxHeatOutput = 0.0;
    
    public SimpleSimulator(final Reactor reactor, final JTextArea output, final JPanel[][] reactorButtonPanels, final int initialHeat) {
        this.reactor = reactor;
        this.output = output;
        this.reactorButtonPanels = reactorButtonPanels;
        this.initialHeat = initialHeat;
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        long startTime = System.nanoTime();
        int reactorTicks = 0;
        int cooldownTicks = 0;
        int totalRodCount = 0;
        try {
            publish(""); //NOI18N
            publish(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("SIMULATION_STARTED"));
            reactor.setCurrentHeat(initialHeat);
            reactor.clearVentedHeat();
            double minReactorHeat = initialHeat;
            double maxReactorHeat = initialHeat;
            boolean reachedBurn = initialHeat >= 0.4 * reactor.getMaxHeat();
            boolean reachedEvaporate = initialHeat >= 0.5 * reactor.getMaxHeat();
            boolean reachedHurt = initialHeat >= 0.7 * reactor.getMaxHeat();
            boolean reachedLava = initialHeat >= 0.85 * reactor.getMaxHeat();
            boolean reachedExplode = false;
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null) {
                        component.clearCurrentHeat();
                        component.clearDamage();
                        totalRodCount += component.getRodCount();
                    }
                    publish(String.format("R%dC%d:0xC0C0C0", row, col)); //NOI18N
                }
            }
            double lastEUoutput = 0.0;
            double totalEUoutput = 0.0;
            double lastHeatOutput = 0.0;
            double totalHeatOutput = 0.0;
            double maxGeneratedHeat = 0.0;
            double minHeatBuildup = Double.MAX_VALUE;
            double maxHeatBuildup = 0.0;
            boolean componentsIntact = true;
            int timeToFirstComponentBreak = 500000;
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
                double preTickReactorHeat = reactor.getCurrentHeat();
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
                        if (maxReactorHeat >= 0.4 * reactor.getMaxHeat() && !reachedBurn) {
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_BURN_TIME"), reactorTicks));
                            reachedBurn = true;
                        }
                        if (maxReactorHeat >= 0.5 * reactor.getMaxHeat() && !reachedEvaporate) {
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_EVAPORATE_TIME"), reactorTicks));
                            reachedEvaporate = true;
                        }
                        if (maxReactorHeat >= 0.7 * reactor.getMaxHeat() && !reachedHurt) {
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_HURT_TIME"), reactorTicks));
                            reachedHurt = true;
                        }
                        if (maxReactorHeat >= 0.85 * reactor.getMaxHeat() && !reachedLava) {
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_LAVA_TIME"), reactorTicks));
                            reachedLava = true;
                        }
                        if (maxReactorHeat >= reactor.getMaxHeat() && !reachedExplode) {
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_EXPLODE_TIME"), reactorTicks));
                            reachedExplode = true;
                        }
                    }
                }
                double postTickReactorHeat = reactor.getCurrentHeat();
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
                        if (component != null && component.isBroken() && !alreadyBroken[row][col] && !component.getClass().getName().contains("FuelRod")) { //NOI18N
                            publish(String.format("R%dC%d:0xFF0000", row, col)); //NOI18N
                            alreadyBroken[row][col] = true;
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("BROKE_TIME"), row, col, reactorTicks));
                            if (componentsIntact) {
                                componentsIntact = false;
                                timeToFirstComponentBreak = reactorTicks;
                            }
                        }
                    }
                }
                if (componentsIntact && postTickReactorHeat >= preTickReactorHeat) {
                    minHeatBuildup = Math.min(minHeatBuildup, postTickReactorHeat - preTickReactorHeat);
                    maxHeatBuildup = Math.max(maxHeatBuildup, postTickReactorHeat - preTickReactorHeat);
                }
            } while (reactor.getCurrentHeat() <= reactor.getMaxHeat() && lastEUoutput > 0.0);
            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("MIN_TEMP"), minReactorHeat));
            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("MAX_TEMP"), maxReactorHeat));
            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("FUEL_RODS_TIME"), reactorTicks));
            if (reactorTicks > 0) {
                if (reactor.isFluid()) {
                    publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("HEAT_OUTPUTS"), 2 * totalHeatOutput, 2 * totalHeatOutput / reactorTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
                    if (totalRodCount > 0) {
                        publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("EFFICIENCY"), totalHeatOutput / reactorTicks / 4 / totalRodCount, minHeatOutput / 4 / totalRodCount, maxHeatOutput / 4 / totalRodCount));
                    }
                } else {
                    publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("EU_OUTPUTS"), totalEUoutput, minEUoutput / 20.0, maxEUoutput / 20.0, totalEUoutput / (reactorTicks * 20)));
                    if (totalRodCount > 0) {
                        publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("EFFICIENCY"), totalEUoutput / reactorTicks / 100 / totalRodCount, minEUoutput / 100 / totalRodCount, maxEUoutput / 100 / totalRodCount));
                    }
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
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COMPONENT_REMAINING_HEAT"), row, col, component.getCurrentHeat()));
                            needsCooldown[row][col] = true;
                        }
                    }
                }
            }
            if (prevReactorHeat == 0.0 && prevTotalComponentHeat == 0.0) {
                output.append(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("NO_COOLDOWN_NEEDED"));
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
                                    publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COMPONENT_COOLDOWN_TIME"), row, col, cooldownTicks));
                                    needsCooldown[row][col] = false;
                                }
                            }
                        }
                    }
                } while (lastHeatOutput > 0 && cooldownTicks < 20000);
                if (reactor.getCurrentHeat() < reactor.getMaxHeat()) {
                    if (reactor.getCurrentHeat() == 0.0) {
                        publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_COOLDOWN_TIME"), reactorCooldownTime));
                    } else {
                        publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_RESIDUAL_HEAT"), reactor.getCurrentHeat(), reactorCooldownTime));
                    }
                    publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("TOTAL_COOLDOWN_TIME"), cooldownTicks));
                }
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            prevTotalComponentHeat += component.getCurrentHeat();
                            if (component.getCurrentHeat() > 0.0) {
                                publish(String.format("R%dC%d:0xFFA500", row, col)); //NOI18N
                                publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COMPONENT_RESIDUAL_HEAT"), row, col, component.getCurrentHeat()));
                            }
                        }
                    }
                }
            }
            if (reactor.getCurrentHeat() > reactor.getMaxHeat()) {
                publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REACTOR_OVERHEATED_TIME"), reactorTicks));
            }
            if (reactor.isFluid()) {
                publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("HEAT_OUTPUTS"), 2 * totalHeatOutput, 2 * totalHeatOutput / cooldownTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
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
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("USED_COOLING"), row, col, component.getEffectiveVentCooling(), component.getVentCoolingCapacity()));
                            totalEffectiveVentCooling += component.getEffectiveVentCooling();
                            totalVentCoolingCapacity += component.getVentCoolingCapacity();
                        } else if (component.getBestCellCooling() > 0) {
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("RECEIVED_HEAT"), row, col, component.getBestCellCooling()));
                            totalCellCooling += component.getBestCellCooling();
                        } else if (component.getBestCondensatorCooling() > 0) {
                            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("RECEIVED_HEAT"), row, col, component.getBestCondensatorCooling()));
                            totalCondensatorCooling += component.getBestCondensatorCooling();
                        }
                    }
                }
            }
                    
            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("TOTAL_VENT_COOLING"), totalEffectiveVentCooling, totalVentCoolingCapacity));
            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("TOTAL_CELL_COOLING"), totalCellCooling));
            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("TOTAL_CONDENSATOR_COOLING"), totalCondensatorCooling));
            publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("MAX_HEAT_GENERATED"), maxGeneratedHeat));
            double totalCooling = totalEffectiveVentCooling + totalCellCooling + totalCondensatorCooling;
            if (maxHeatBuildup > 0) {
                publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("HEAT_BUILDUP"), minHeatBuildup, maxHeatBuildup));
            }
            //return null;
        } catch (Throwable e) {
            if (cooldownTicks == 0) {
                publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("ERROR_AT_REACTOR_TICK"), reactorTicks));
            } else {
                publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("ERROR_AT_COOLDOWN_TICK"), cooldownTicks));
            }
            publish(e.toString(), " ", Arrays.toString(e.getStackTrace()));
        }
        long endTime = System.nanoTime();
        publish(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("SIMULATION_TIME"), (endTime - startTime) / 1e9));
        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        if (!isCancelled()) {
            for (String chunk : chunks) {
                if (chunk.isEmpty()) {
                    output.setText("");
                } else {
                    if (chunk.matches("R\\dC\\d:.*")) { //NOI18N
                        String temp = chunk.substring(5);
                        int row = chunk.charAt(1) - '0';
                        int col = chunk.charAt(3) - '0';
                        if (temp.startsWith("0x")) { //NOI18N
                            reactorButtonPanels[row][col].setBackground(Color.decode(temp));
                        } else if (temp.startsWith("+")) { //NOI18N
                            final ReactorComponent component = reactor.getComponentAt(row, col);
                            if (component != null) {
                                component.info += "\n" + temp.substring(1); //NOI18N
                            }
                        } else {
                            final ReactorComponent component = reactor.getComponentAt(row, col);
                            if (component != null) {
                                component.info = temp;
                            }
                        }
                    } else {
                        output.append(chunk);
                    }
                }
            }
        }
    }
    
}
