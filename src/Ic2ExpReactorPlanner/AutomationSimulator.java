package Ic2ExpReactorPlanner;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
public class AutomationSimulator extends SwingWorker<Void, String> {

    private final Reactor reactor;
    
    private final JTextArea output;
    
    private final JPanel[][] reactorButtonPanels;
    
    private final boolean[][] alreadyBroken = new boolean[6][9];
    
    private final boolean[][] needsCooldown = new boolean[6][9];
    
    private final int initialHeat;
    
    private final File csvFile;
    
    private final int csvLimit;
    
    private double minEUoutput = Double.MAX_VALUE;
    
    private double maxEUoutput = 0.0;
    
    private double minHeatOutput = Double.MAX_VALUE;
    
    private double maxHeatOutput = 0.0;

    private final int onPulseDuration;
    
    private final int offPulseDuration;
    
    private final int clockPeriod;
    
    private final int suspendTemp;
    
    private final int resumeTemp;
    
    private final int maxSimulationTicks;
    
    private boolean active = true;
    
    private int pauseTimer = 0;
    
    private int redstoneUsed = 0;
    
    private int lapisUsed = 0;
    
    private MaterialsList replacedItems = new MaterialsList();
    
    private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle");

    public AutomationSimulator(final Reactor reactor, final JTextArea output, final JPanel[][] reactorButtonPanels, final File csvFile, final int csvLimit) {
        this.reactor = reactor;
        this.output = output;
        this.reactorButtonPanels = reactorButtonPanels;
        this.initialHeat = (int)reactor.getCurrentHeat();
        this.onPulseDuration = reactor.getOnPulse();
        this.offPulseDuration = reactor.getOffPulse();
        this.clockPeriod = onPulseDuration + offPulseDuration;
        this.suspendTemp = reactor.getSuspendTemp();
        this.resumeTemp = reactor.getResumeTemp();
        this.csvFile = csvFile;
        this.csvLimit = csvLimit;
        this.maxSimulationTicks = reactor.getMaxSimulationTicks();
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        PrintWriter csvOut = null;
        if (csvFile != null) {
            try {
                csvOut = new PrintWriter(csvFile);
            } catch (IOException ex) {
                publish(BUNDLE.getString("Simulation.CSVOpenFailure"));
            }
        }
        long startTime = System.nanoTime();
        int reactorTicks = 0;
        int activeTime = 0;
        int inactiveTime = 0;
        int currentActiveTime = 0;
        int minActiveTime = Integer.MAX_VALUE;
        int maxActiveTime = 0;
        int cooldownTicks = 0;
        int totalRodCount = 0;
        try {
            if (csvOut != null) {
                csvOut.print(BUNDLE.getString("CSVData.HeaderReactorTick"));
                csvOut.print(BUNDLE.getString("CSVData.HeaderCoreHeat"));
                if (reactor.isFluid()) {
                    csvOut.print(BUNDLE.getString("CSVData.HeaderHUOutput"));
                } else {
                    csvOut.print(BUNDLE.getString("CSVData.HeaderEUOutput"));
                }
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && (component.getMaxHeat() > 1 || component.getMaxDamage() > 1)) {
                            csvOut.printf(BUNDLE.getString("CSVData.HeaderComponentName"), ComponentFactory.getDisplayName(component), row, col);
                        }
                        if (component != null && component.producesOutput()) {
                            csvOut.printf(BUNDLE.getString("CSVData.HeaderComponentOutput"), ComponentFactory.getDisplayName(component), row, col);
                        }
                    }
                }
                csvOut.println();
            }
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
            boolean allFuelRodsDepleted = false;
            boolean componentsIntact = true;
            boolean anyRodsDepleted = false;
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
                if (active) {
                    allFuelRodsDepleted = true; // assume rods depleted until one is found that isn't.
                }
                double generatedHeat = 0.0;
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            if (allFuelRodsDepleted && component.getRodCount() > 0) {
                                allFuelRodsDepleted = false;
                            }
                            if (active) {
                                generatedHeat += component.generateHeat();
                            }
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
                maxGeneratedHeat = Math.max(generatedHeat, maxGeneratedHeat);
                if (active) {
                    for (int row = 0; row < 6; row++) {
                        for (int col = 0; col < 9; col++) {
                            ReactorComponent component = reactor.getComponentAt(row, col);
                            if (component != null && !component.isBroken()) {
                                component.generateEnergy();
                            }
                        }
                    }
                }
                lastEUoutput = reactor.getCurrentEUoutput();
                totalEUoutput += lastEUoutput;
                lastHeatOutput = reactor.getVentedHeat();
                totalHeatOutput += lastHeatOutput;
                if (reactor.getCurrentHeat() <= reactor.getMaxHeat()) {
                    reactorTicks++;
                    if (reactor.isPulsed()) {
                        if (active) {
                            activeTime++;
                            currentActiveTime++;
                            if (reactor.getCurrentHeat() >= suspendTemp || (reactorTicks % clockPeriod) >= onPulseDuration) {
                                active = false;
                                minActiveTime = Math.min(currentActiveTime, minActiveTime);
                                maxActiveTime = Math.max(currentActiveTime, maxActiveTime);
                                currentActiveTime = 0;
                            }
                        } else {
                            inactiveTime++;
                            if (reactor.isAutomated() && pauseTimer > 0) {
                                pauseTimer--;
                            } else if (reactor.getCurrentHeat() <= resumeTemp && (reactorTicks % clockPeriod) < onPulseDuration) {
                                active = true;
                            }
                        }
                    }
                    minEUoutput = Math.min(lastEUoutput, minEUoutput);
                    maxEUoutput = Math.max(lastEUoutput, maxEUoutput);
                    minHeatOutput = Math.min(lastHeatOutput, minHeatOutput);
                    maxHeatOutput = Math.max(lastHeatOutput, maxHeatOutput);
                }
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && reactor.isAutomated()) {
                            if (component.getMaxHeat() > 1) {
                                if (component.getAutomationThreshold() > component.getInitialHeat() && component.getCurrentHeat() >= component.getAutomationThreshold()) {
                                    component.clearCurrentHeat();
                                    replacedItems.add(ComponentFactory.getDisplayName(component));
                                    component.info.append(String.format(BUNDLE.getString("ComponentInfo.ReplacedTime"), reactorTicks));
                                    if (component.getReactorPause() > 0) {
                                        active = false;
                                        pauseTimer = Math.max(pauseTimer, component.getReactorPause());
                                        minActiveTime = Math.min(currentActiveTime, minActiveTime);
                                        maxActiveTime = Math.max(currentActiveTime, maxActiveTime);
                                    }
                                } else if (component.getAutomationThreshold() < component.getInitialHeat() && component.getCurrentHeat() <= component.getAutomationThreshold()) {
                                    component.clearCurrentHeat();
                                    replacedItems.add(ComponentFactory.getDisplayName(component));
                                    component.info.append(String.format(BUNDLE.getString("ComponentInfo.ReplacedTime"), reactorTicks));
                                    if (component.getReactorPause() > 0) {
                                        active = false;
                                        pauseTimer = Math.max(pauseTimer, component.getReactorPause());
                                        minActiveTime = Math.min(currentActiveTime, minActiveTime);
                                        maxActiveTime = Math.max(currentActiveTime, maxActiveTime);
                                    }
                                }
                            } else if (component.isBroken() || (component.getMaxDamage() > 1 && component.getCurrentDamage() >= component.getAutomationThreshold())) {
                                component.clearDamage();
                                replacedItems.add(ComponentFactory.getDisplayName(component));
                                component.info.append(String.format(BUNDLE.getString("ComponentInfo.ReplacedTime"), reactorTicks));
                                if (component.getReactorPause() > 0) {
                                    active = false;
                                    pauseTimer = Math.max(pauseTimer, component.getReactorPause());
                                    minActiveTime = Math.min(currentActiveTime, minActiveTime);
                                    maxActiveTime = Math.max(currentActiveTime, maxActiveTime);
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
                if (!active) {
                    currentActiveTime = 0;
                }
                if (csvOut != null && reactorTicks <= csvLimit) {
                    csvOut.printf(BUNDLE.getString("CSVData.EntryReactorTick"), reactorTicks);
                    csvOut.printf(BUNDLE.getString("CSVData.EntryCoreHeat"), reactor.getCurrentHeat());
                    if (reactor.isFluid()) {
                        csvOut.printf(BUNDLE.getString("CSVData.EntryHUOutput"), reactor.getVentedHeat() * 40);
                    } else {
                        csvOut.printf(BUNDLE.getString("CSVData.EntryEUOutput"), reactor.getCurrentEUoutput());
                    }
                    for (int row = 0; row < 6; row++) {
                        for (int col = 0; col < 9; col++) {
                            ReactorComponent component = reactor.getComponentAt(row, col);
                            if (component != null && (component.getMaxHeat() > 1 || component.getMaxDamage() > 1)) {
                                double componentValue = component.getCurrentDamage();
                                if (component.getMaxHeat() > 1) {
                                    componentValue = component.getCurrentHeat();
                                }
                                csvOut.printf(BUNDLE.getString("CSVData.EntryComponentValue"), componentValue);
                            }
                            if (component != null && component.producesOutput()) {
                                csvOut.printf(BUNDLE.getString("CSVData.EntryComponentOutput"), component.getCurrentOutput());
                            }
                        }
                    }
                    csvOut.println();
                }
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && component.isBroken() && !alreadyBroken[row][col]) {
                            alreadyBroken[row][col] = true;
                            if (component.getRodCount() == 0) {
                                publish(String.format("R%dC%d:0xFF0000", row, col)); //NOI18N
                                component.info.append(String.format(BUNDLE.getString("ComponentInfo.BrokeTime"), reactorTicks));
                                if (componentsIntact) {
                                    componentsIntact = false;
                                    publish(String.format(BUNDLE.getString("Simulation.FirstComponentBrokenDetails"), component.toString(), row, col, reactorTicks));
                                    if (reactor.isFluid()) {
                                        publish(String.format(BUNDLE.getString("Simulation.HeatOutputsBeforeBreak"), 40 * totalHeatOutput, 2 * totalHeatOutput / reactorTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
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
                            } else if (!anyRodsDepleted) {
                                anyRodsDepleted = true;
                                publish(String.format(BUNDLE.getString("Simulation.FirstRodDepletedDetails"), component.toString(), row, col, reactorTicks));
                                if (reactor.isFluid()) {
                                    publish(String.format(BUNDLE.getString("Simulation.HeatOutputsBeforeDepleted"), 40 * totalHeatOutput, 2 * totalHeatOutput / reactorTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
                                    if (totalRodCount > 0) {
                                        publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalHeatOutput / reactorTicks / 4 / totalRodCount, minHeatOutput / 4 / totalRodCount, maxHeatOutput / 4 / totalRodCount));
                                    }
                                } else {
                                    publish(String.format(BUNDLE.getString("Simulation.EUOutputsBeforeDepleted"), totalEUoutput, minEUoutput / 20.0, maxEUoutput / 20.0, totalEUoutput / (reactorTicks * 20)));
                                    if (totalRodCount > 0) {
                                        publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalEUoutput / reactorTicks / 100 / totalRodCount, minEUoutput / 100 / totalRodCount, maxEUoutput / 100 / totalRodCount));
                                    }
                                }
                                publish(String.format(BUNDLE.getString("Simulation.ReactorMinTempBeforeDepleted"), minReactorHeat));
                                publish(String.format(BUNDLE.getString("Simulation.ReactorMaxTempBeforeDepleted"), maxReactorHeat));
                            }
                        }
                    }
                }
            } while (reactor.getCurrentHeat() < reactor.getMaxHeat() && (!allFuelRodsDepleted || lastEUoutput > 0 || lastHeatOutput > 0) && reactorTicks < maxSimulationTicks && !isCancelled());
            if (csvOut != null) {
                csvOut.close();
            }
            if (isCancelled()) {
                publish(String.format(BUNDLE.getString("Simulation.CancelledAtTick"), reactorTicks));
                return null;
            }
            publish(String.format(BUNDLE.getString("Simulation.ReactorMinTemp"), minReactorHeat));
            publish(String.format(BUNDLE.getString("Simulation.ReactorMaxTemp"), maxReactorHeat));
            if (reactor.getCurrentHeat() < reactor.getMaxHeat()) {
                publish(String.format(BUNDLE.getString("Simulation.TimeWithoutExploding"), reactorTicks));
                if (reactor.isPulsed()) {
                    String rangeString = "";
                    if (maxActiveTime > minActiveTime) {
                        rangeString = String.format(BUNDLE.getString("Simulation.ActiveTimeRange"), minActiveTime, maxActiveTime);
                    }
                    publish(String.format(BUNDLE.getString("Simulation.ActiveTime"), activeTime, rangeString));
                    publish(String.format(BUNDLE.getString("Simulation.InactiveTime"), inactiveTime));
                }
                final String replacedItemsString = replacedItems.toString();
                if (!replacedItemsString.isEmpty()) {
                    publish(String.format(BUNDLE.getString("Simulation.ComponentsReplaced"), replacedItemsString));
                }
                
                if (reactorTicks > 0) {
                    if (reactor.isFluid()) {
                        if (totalHeatOutput > 0) {
                            publish(String.format(BUNDLE.getString("Simulation.HeatOutputs"), 40 * totalHeatOutput, 2 * totalHeatOutput / reactorTicks, 2 * minHeatOutput, 2 * maxHeatOutput));
                            if (totalRodCount > 0) {
                                publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalHeatOutput / reactorTicks / 4 / totalRodCount, minHeatOutput / 4 / totalRodCount, maxHeatOutput / 4 / totalRodCount));
                            }
                        }
                    } else {
                        if (totalEUoutput > 0) {
                            publish(String.format(BUNDLE.getString("Simulation.EUOutputs"), totalEUoutput, minEUoutput / 20.0, maxEUoutput / 20.0, totalEUoutput / (reactorTicks * 20)));
                            if (totalRodCount > 0) {
                                publish(String.format(BUNDLE.getString("Simulation.Efficiency"), totalEUoutput / reactorTicks / 100 / totalRodCount, minEUoutput / 100 / totalRodCount, maxEUoutput / 100 / totalRodCount));
                            }
                        }
                    }
                }

                if (reactor.getCurrentHeat() > 0.0) {
                    publish(String.format(BUNDLE.getString("Simulation.ReactorRemainingHeat"), reactor.getCurrentHeat()));
                }
                double prevReactorHeat = reactor.getCurrentHeat();
                double prevTotalComponentHeat = 0.0;
                for (int row = 0; row < 6; row++) {
                    for (int col = 0; col < 9; col++) {
                        ReactorComponent component = reactor.getComponentAt(row, col);
                        if (component != null && !component.isBroken()) {
                            if (component.getCurrentHeat() > 0.0) {
                                prevTotalComponentHeat += component.getCurrentHeat();
                                publish(String.format("R%dC%d:0xFFA500", row, col)); // NOI18N
                                component.info.append(String.format(BUNDLE.getString("ComponentInfo.RemainingHeat"), component.getCurrentHeat()));
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
                                        component.info.append(String.format(BUNDLE.getString("ComponentInfo.CooldownTime"), cooldownTicks));
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
                }
            } else {
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
                            component.info.append(String.format(BUNDLE.getString("ComponentInfo.UsedCooling"), component.getEffectiveVentCooling(), component.getVentCoolingCapacity()));
                            totalEffectiveVentCooling += component.getEffectiveVentCooling();
                            totalVentCoolingCapacity += component.getVentCoolingCapacity();
                        } else if (component.getBestCellCooling() > 0) {
                            component.info.append(String.format(BUNDLE.getString("ComponentInfo.ReceivedHeat"), component.getBestCellCooling()));
                            totalCellCooling += component.getBestCellCooling();
                        } else if (component.getBestCondensatorCooling() > 0) {
                            component.info.append(String.format(BUNDLE.getString("ComponentInfo.ReceivedHeat"), component.getBestCondensatorCooling()));
                            totalCondensatorCooling += component.getBestCondensatorCooling();
                        } else if (component.getMaxHeatGenerated() > 0) {
                            if (!reactor.isFluid() && component.getMaxEUGenerated() > 0) {
                                component.info.append(String.format(BUNDLE.getString("ComponentInfo.GeneratedEU"), component.getMinEUGenerated(), component.getMaxEUGenerated()));
                            }
                            component.info.append(String.format(BUNDLE.getString("ComponentInfo.GeneratedHeat"), component.getMinHeatGenerated(), component.getMaxHeatGenerated()));
                        }
                        if (component.getMaxReachedHeat() > 0) {
                            component.info.append(String.format(BUNDLE.getString("ComponentInfo.ReachedHeat"), component.getMaxReachedHeat(), component.getMaxHeat()));
                        }
                    }
                }
            }
                    
            if (totalVentCoolingCapacity > 0) {
                publish(String.format(BUNDLE.getString("Simulation.TotalVentCooling"), totalEffectiveVentCooling, totalVentCoolingCapacity));
            }
            if (totalCellCooling > 0) {
                publish(String.format(BUNDLE.getString("Simulation.TotalCellCooling"), totalCellCooling));
            }
            if (totalCondensatorCooling > 0) {
                publish(String.format(BUNDLE.getString("Simulation.TotalCondensatorCooling"), totalCondensatorCooling));
            }
            if (maxGeneratedHeat > 0) {
                publish(String.format(BUNDLE.getString("Simulation.MaxHeatGenerated"), maxGeneratedHeat));
            }
            if (redstoneUsed > 0) {
                publish(String.format(BUNDLE.getString("Simulation.RedstoneUsed"), redstoneUsed));
            }
            if (lapisUsed > 0) {
                publish(String.format(BUNDLE.getString("Simulation.LapisUsed"), lapisUsed));
            }
            double totalCooling = totalEffectiveVentCooling + totalCellCooling + totalCondensatorCooling;
            if (totalCooling >= maxGeneratedHeat) {
                publish(String.format(BUNDLE.getString("Simulation.ExcessCooling"), totalCooling - maxGeneratedHeat));
            } else {
                publish(String.format(BUNDLE.getString("Simulation.ExcessHeating"), maxGeneratedHeat - totalCooling));
            }
            //return null;
        } catch (Throwable e) {
            if (cooldownTicks == 0) {
                publish(String.format(BUNDLE.getString("Simulation.ErrorReactor"), reactorTicks));
            } else {
                publish(String.format(BUNDLE.getString("Simulation.ErrorCooldown"), cooldownTicks));
            }
            publish(e.toString(), " ", Arrays.toString(e.getStackTrace())); // NO18N
            if (csvOut != null) {
                csvOut.close();
            }
        }
        long endTime = System.nanoTime();
        publish(String.format(BUNDLE.getString("Simulation.ElapsedTime"), (endTime - startTime) / 1e9));
        return null;
    }

    @Override
    protected void process(List<String> chunks) {
        for (String chunk : chunks) {
            if (chunk.isEmpty()) {
                output.setText(""); //NO18N
            } else {
                if (chunk.matches("R\\dC\\d:.*")) { //NO18N
                    String temp = chunk.substring(5);
                    int row = chunk.charAt(1) - '0';
                    int col = chunk.charAt(3) - '0';
                    if (temp.startsWith("0x")) { //NO18N
                        reactorButtonPanels[row][col].setBackground(Color.decode(temp));
                    }
                } else {
                    output.append(chunk);
                }
            }
        }
    }
    
}
