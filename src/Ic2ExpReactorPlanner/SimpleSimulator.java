package Ic2ExpReactorPlanner;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
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
    
    private int redstoneUsed = 0;
    
    private int lapisUsed = 0;
    
    private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle");

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
            publish(BUNDLE.getString("Simulation.Started"));
            reactor.setCurrentHeat(initialHeat);
            reactor.clearVentedHeat();
            double minReactorHeat = initialHeat;
            double maxReactorHeat = initialHeat;
            boolean reachedBelow50 = false;
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
                            component.dissipate();
                            component.transfer();
                        }
                    }
                }
                maxReactorHeat = Math.max(reactor.getCurrentHeat(), maxReactorHeat);
                minReactorHeat = Math.min(reactor.getCurrentHeat(), minReactorHeat);
                if (reactor.getCurrentHeat() < 0.5 * reactor.getMaxHeat() && !reachedBelow50 && reachedEvaporate) {
                    publish(String.format(BUNDLE.getString("Simulation.TimeToBelow50"), reactorTicks));
                    reachedBelow50 = true;
                }
                if (reactor.getCurrentHeat() >= 0.4 * reactor.getMaxHeat() && !reachedBurn) {
                    publish(String.format(BUNDLE.getString("Simulation.TimeToBurn"), reactorTicks));
                    reachedBurn = true;
                }
                if (reactor.getCurrentHeat() >= 0.5 * reactor.getMaxHeat() && !reachedEvaporate) {
                    publish(String.format(BUNDLE.getString("Simulation.TimeToEvaporate"), reactorTicks));
                    reachedEvaporate = true;
                }
                if (reactor.getCurrentHeat() >= 0.7 * reactor.getMaxHeat() && !reachedHurt) {
                    publish(String.format(BUNDLE.getString("Simulation.TimeToHurt"), reactorTicks));
                    reachedHurt = true;
                }
                if (reactor.getCurrentHeat() >= 0.85 * reactor.getMaxHeat() && !reachedLava) {
                    publish(String.format(BUNDLE.getString("Simulation.TimeToLava"), reactorTicks));
                    reachedLava = true;
                }
                if (reactor.getCurrentHeat() >= reactor.getMaxHeat() && !reachedExplode) {
                    publish(String.format(BUNDLE.getString("Simulation.TimeToXplode"), reactorTicks));
                    reachedExplode = true;
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
                if (reactor.getCurrentHeat() <= reactor.getMaxHeat()) {
                    lastEUoutput = reactor.getCurrentEUoutput();
                    totalEUoutput += lastEUoutput;
                    lastHeatOutput = reactor.getVentedHeat();
                    totalHeatOutput += lastHeatOutput;
                    if (lastEUoutput > 0.0) {
                        reactorTicks++;
                        minEUoutput = Math.min(lastEUoutput, minEUoutput);
                        maxEUoutput = Math.max(lastEUoutput, maxEUoutput);
                        minHeatOutput = Math.min(lastHeatOutput, minHeatOutput);
                        maxHeatOutput = Math.max(lastHeatOutput, maxHeatOutput);
                    }
                }
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && component.isBroken() && !alreadyBroken[row][col] && component.getRodCount() == 0) {
                            publish(String.format("R%dC%d:0xFF0000", row, col)); //NOI18N
                            alreadyBroken[row][col] = true;
                            publish(String.format(BUNDLE.getString("ComponentInfo.BrokeTime"), row, col, reactorTicks));
                            if (componentsIntact) {
                                componentsIntact = false;
                                publish(String.format(BUNDLE.getString("Simulation.FirstComponentBrokenDetails"), component.toString(), row, col, reactorTicks));
                                if (reactor.isFluid()) {
                                    publish(String.format(BUNDLE.getString("Simulation.HeatOutputsBeforeBreak"), 2 * totalHeatOutput, 2 * totalHeatOutput / reactorTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
                                    if (totalRodCount > 0) {
                                        publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalHeatOutput / reactorTicks / 4 / totalRodCount, minHeatOutput / 4 / totalRodCount, maxHeatOutput / 4 / totalRodCount));
                                    }
                                } else {
                                    publish(String.format(BUNDLE.getString("Simulation.EUOutputsBeforeBreak"), totalEUoutput, minEUoutput / 20.0, maxEUoutput / 20.0, totalEUoutput / (reactorTicks * 20)));
                                    if (totalRodCount > 0) {
                                        publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalEUoutput / reactorTicks / 100 / totalRodCount, minEUoutput / 100 / totalRodCount, maxEUoutput / 100 / totalRodCount));
                                    }
                                }
                            }
                        }
                        if (reactor.isUsingReactorCoolantInjectors()) {
                            if (component instanceof RshCondensator && component.getCurrentHeat() > 17000 && !component.isBroken()) {
                                ((RshCondensator) component).injectCoolant();
                                redstoneUsed++;
                            } else if (component instanceof LzhCondensator && component.getCurrentHeat() > 85000 && !component.isBroken()) {
                                ((LzhCondensator) component).injectCoolant();
                                lapisUsed++;
                            }
                        }
                    }
                }
                if (componentsIntact && postTickReactorHeat >= preTickReactorHeat) {
                    minHeatBuildup = Math.min(minHeatBuildup, postTickReactorHeat - preTickReactorHeat);
                    maxHeatBuildup = Math.max(maxHeatBuildup, postTickReactorHeat - preTickReactorHeat);
                }
            } while (reactor.getCurrentHeat() < reactor.getMaxHeat() && lastEUoutput > 0.0);
            publish(String.format(BUNDLE.getString("Simulation.ReactorMinTemp"), minReactorHeat));
            publish(String.format(BUNDLE.getString("Simulation.ReactorMaxTemp"), maxReactorHeat));
            publish(String.format(BUNDLE.getString("Simulation.FuelRodsTime"), reactorTicks));
            if (reactorTicks > 0) {
                if (reactor.isFluid()) {
                    publish(String.format(BUNDLE.getString("Simulation.HeatOutputs"), 2 * totalHeatOutput, 2 * totalHeatOutput / reactorTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
                    if (totalRodCount > 0) {
                        publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalHeatOutput / reactorTicks / 4 / totalRodCount, minHeatOutput / 4 / totalRodCount, maxHeatOutput / 4 / totalRodCount));
                    }
                } else {
                    publish(String.format(BUNDLE.getString("Simulation.EUOutputs"), totalEUoutput, minEUoutput / 20.0, maxEUoutput / 20.0, totalEUoutput / (reactorTicks * 20)));
                    if (totalRodCount > 0) {
                        publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalEUoutput / reactorTicks / 100 / totalRodCount, minEUoutput / 100 / totalRodCount, maxEUoutput / 100 / totalRodCount));
                    }
                }
            }
            lastHeatOutput = 0.0;
            totalHeatOutput = 0.0;
            double prevReactorHeat = reactor.getCurrentHeat();
            double prevTotalComponentHeat = 0.0;
            if (reactor.getCurrentHeat() >= reactor.getMaxHeat()) {
                publish(String.format(BUNDLE.getString("Simulation.ReactorOverheatedTime"), reactorTicks));
                double explosionPower = 10.0;
                double explosionPowerMult = 1.0;
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null) {
                            explosionPower += component.getExplosionPowerOffset();
                            explosionPowerMult *= component.getExplosionPowerMultiplier();
                        }
                    }
                }
                explosionPower *= explosionPowerMult;
                publish(String.format(BUNDLE.getString("Simulation.ExplosionPower"), explosionPower));
            } else {
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            prevTotalComponentHeat += component.getCurrentHeat();
                            if (component.getCurrentHeat() > 0.0) {
                                publish(String.format("R%dC%d:0xFFFF00", row, col));
                                publish(String.format(BUNDLE.getString("ComponentInfo.RemainingHeat"), row, col, component.getCurrentHeat()));
                                needsCooldown[row][col] = true;
                            }
                        }
                    }
                }
            }
            if (prevReactorHeat == 0.0 && prevTotalComponentHeat == 0.0) {
                publish(BUNDLE.getString("Simulation.NoCooldown"));
            } else if (reactor.getCurrentHeat() < reactor.getMaxHeat()) {
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
                                    publish(String.format(BUNDLE.getString("ComponentInfo.CooldownTime"), row, col, cooldownTicks));
                                    needsCooldown[row][col] = false;
                                }
                            }
                        }
                    }
                } while (lastHeatOutput > 0 && cooldownTicks < 50000);
                if (reactor.getCurrentHeat() < reactor.getMaxHeat()) {
                    if (reactor.getCurrentHeat() == 0.0) {
                        publish(String.format(BUNDLE.getString("Simulation.ReactorCooldownTime"), reactorCooldownTime));
                    } else {
                        publish(String.format(BUNDLE.getString("Simulation.ReactorResidualHeat"), reactor.getCurrentHeat(), reactorCooldownTime));
                    }
                    publish(String.format(BUNDLE.getString("Simulation.TotalCooldownTime"), cooldownTicks));
                }
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            prevTotalComponentHeat += component.getCurrentHeat();
                            if (component.getCurrentHeat() > 0.0) {
                                publish(String.format("R%dC%d:0xFFA500", row, col)); //NOI18N
                                publish(String.format(BUNDLE.getString("ComponentInfo.ResidualHeat"), row, col, component.getCurrentHeat()));
                            }
                        }
                    }
                }
                if (reactor.isFluid()) {
                    publish(String.format(BUNDLE.getString("Simulation.HeatOutputsCooldown"), 2 * totalHeatOutput, 2 * totalHeatOutput / cooldownTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
                }
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
                            publish(String.format(BUNDLE.getString("ComponentInfo.UsedCooling"), row, col, component.getEffectiveVentCooling(), component.getVentCoolingCapacity()));
                            totalEffectiveVentCooling += component.getEffectiveVentCooling();
                            totalVentCoolingCapacity += component.getVentCoolingCapacity();
                        } else if (component.getBestCellCooling() > 0) {
                            publish(String.format(BUNDLE.getString("ComponentInfo.ReceivedHeat"), row, col, component.getBestCellCooling()));
                            totalCellCooling += component.getBestCellCooling();
                        } else if (component.getBestCondensatorCooling() > 0) {
                            publish(String.format(BUNDLE.getString("ComponentInfo.ReceivedHeat"), row, col, component.getBestCondensatorCooling()));
                            totalCondensatorCooling += component.getBestCondensatorCooling();
                        } else if (component.getMaxHeatGenerated() > 0) {
                            if (!reactor.isFluid() && component.getMaxEUGenerated() > 0) {
                                publish(String.format(BUNDLE.getString("ComponentInfo.GeneratedEU"), row, col, component.getMinEUGenerated(), component.getMaxEUGenerated()));
                            }
                            publish(String.format(BUNDLE.getString("ComponentInfo.GeneratedHeat"), row, col, component.getMinHeatGenerated(), component.getMaxHeatGenerated()));
                        }
                        if (component.getMaxReachedHeat() > 0) {
                            publish(String.format(BUNDLE.getString("ComponentInfo.ReachedHeat"), row, col, component.getMaxReachedHeat(), component.getMaxHeat()));
                        }
                    }
                }
            }
                    
            publish(String.format(BUNDLE.getString("Simulation.TotalVentCooling"), totalEffectiveVentCooling, totalVentCoolingCapacity));
            publish(String.format(BUNDLE.getString("Simulation.TotalCellCooling"), totalCellCooling));
            publish(String.format(BUNDLE.getString("Simulation.TotalCondensatorCooling"), totalCondensatorCooling));
            publish(String.format(BUNDLE.getString("Simulation.MaxHeatGenerated"), maxGeneratedHeat));
            if (redstoneUsed > 0) {
                publish(String.format(BUNDLE.getString("Simulation.RedstoneUsed"), redstoneUsed));
            }
            if (lapisUsed > 0) {
                publish(String.format(BUNDLE.getString("Simulation.LapisUsed"), lapisUsed));
            }
            double totalCooling = totalEffectiveVentCooling + totalCellCooling + totalCondensatorCooling;
            if (maxHeatBuildup > 0) {
                publish(String.format(BUNDLE.getString("Simulation.ReactorHeatBuildup"), minHeatBuildup, maxHeatBuildup));
            }
            //return null;
        } catch (Throwable e) {
            if (cooldownTicks == 0) {
                publish(String.format(BUNDLE.getString("Simulation.ErrorReactor"), reactorTicks));
            } else {
                publish(String.format(BUNDLE.getString("Simulation.ErrorCooldown"), cooldownTicks));
            }
            publish(e.toString(), " ", Arrays.toString(e.getStackTrace()));
        }
        long endTime = System.nanoTime();
        publish(String.format(BUNDLE.getString("Simulation.ElapsedTime"), (endTime - startTime) / 1e9));
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
