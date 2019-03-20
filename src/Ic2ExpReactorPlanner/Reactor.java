/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.HeadlessException;
import javax.swing.JOptionPane;

/**
 * Represents an IndustrialCraft2 Nuclear Reactor.
 * @author Brian McCloud
 */
public class Reactor {
    
    private final ReactorComponent[][] grid = new ReactorComponent[6][9];
    
    private double currentEUoutput = 0.0;
    
    private double currentHeat = 0.0;
    
    private double maxHeat = 10000.0;
    
    private double ventedHeat = 0.0;
    
    private boolean fluid = false;
    
    private boolean pulsed = false;
    
    private boolean automated = false;
    
    private boolean usingReactorCoolantInjectors = false;
    
    private static final int DEFAULT_ON_PULSE = (int)5e6;
    
    private int onPulse = DEFAULT_ON_PULSE;
    
    private static final int DEFAULT_OFF_PULSE = 0;
    
    private int offPulse = DEFAULT_OFF_PULSE;
    
    private static final int DEFAULT_SUSPEND_TEMP = (int)120e3;
    
    private int suspendTemp = DEFAULT_SUSPEND_TEMP;
    
    private static final int DEFAULT_RESUME_TEMP = (int)120e3;
    
    private int resumeTemp = DEFAULT_RESUME_TEMP;
    
    private int maxSimulationTicks = (int)5e6;
    
    // maximum paramatter types for a reactor component (current initial heat, automation threshold, reactor pause
    private static final int MAX_PARAM_TYPES = 3;
    
    public ReactorComponent getComponentAt(final int row, final int column) {
        if (row >= 0 && row < grid.length && column >= 0 && column < grid[row].length) {
            return grid[row][column];
        }
        return null;
    }
    
    public void setComponentAt(final int row, final int column, final ReactorComponent component) {
        if (row >= 0 && row < grid.length && column >= 0 && column < grid[row].length) {
            if (grid[row][column] != null) {
                grid[row][column].removeFromReactor();
            }
            grid[row][column] = component;
            if (component != null) {
                component.setRow(row);
                component.setColumn(column);
                component.setParent(this);
                component.addToReactor();
            }
        }
    }

    public void clearGrid() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] != null) {
                    grid[row][col].removeFromReactor();
                }
                grid[row][col] = null;
            }
        }
    }
    
    /**
     * @return the amount of EU output in the reactor tick just simulated.
     */
    public double getCurrentEUoutput() {
        return currentEUoutput;
    }

    /**
     * @return the current heat level of the reactor.
     */
    public double getCurrentHeat() {
        return currentHeat;
    }

    /**
     * @return the maximum heat of the reactor.
     */
    public double getMaxHeat() {
        return maxHeat;
    }
    
    /**
     * Adjust the maximum heat
     * @param adjustment the adjustment amount (negative values decrease the max heat).
     */
    public void adjustMaxHeat(final double adjustment) {
        maxHeat += adjustment;
    }

    /**
     * Set the current heat of the reactor.  Mainly to be used for simulating a pre-heated reactor, or for resetting to 0 for a new simulation.
     * @param currentHeat the heat to set
     */
    public void setCurrentHeat(final double currentHeat) {
        this.currentHeat = currentHeat;
    }
    
    /**
     * Adjusts the reactor's current heat by a specified amount
     * @param adjustment the adjustment amount.
     */
    public void adjustCurrentHeat(final double adjustment) {
        currentHeat += adjustment;
        if (currentHeat < 0.0) {
            currentHeat = 0.0;
        }
    }
    
    /**
     * add some EU output.
     * @param amount the amount of EU to output over 1 reactor tick (20 game ticks).
     */
    public void addEUOutput(final double amount) {
        currentEUoutput += amount;
    }
    
    /**
     * clears the EU output (presumably to start simulating a new reactor tick).
     */
    public void clearEUOutput() {
        currentEUoutput = 0.0;
    }
    
    /**
     * Gets a list of the materials needed to build the components.
     * @return a list of the materials needed to build the components.
     */
    public MaterialsList getMaterials() {
        MaterialsList result = new MaterialsList();
        for (int col = 0; col < grid[0].length; col++) {
            for (int row = 0; row < grid.length; row++) {
                if (grid[row][col] != null) {
                    result.add(grid[row][col].getMaterials());
                }
            }
        }
        return result;
    }

    public MaterialsList getComponentList() {
        MaterialsList result = new MaterialsList();
        for (int col = 0; col < grid[0].length; col++) {
            for (int row = 0; row < grid.length; row++) {
                if (grid[row][col] != null) {
                    result.add(ComponentFactory.getDisplayName(grid[row][col]));
                }
            }
        }
        return result;
    }
    
    /**
     * @return the amount of heat vented this reactor tick.
     */
    public double getVentedHeat() {
        return ventedHeat;
    }
    
    /**
     * Adds to the amount of heat vented this reactor tick, in case it is a new-style reactor with a pressure vessel and outputting heat to fluid instead of EU.
     * @param amount the amount to add.
     */
    public void ventHeat(final double amount) {
        ventedHeat += amount;
    }
    
    /**
     * Clears the amount of vented heat, in case a new reactor tick is starting.
     */
    public void clearVentedHeat() {
        ventedHeat = 0;
    }
    
    /**
     * Get a code that represents the component set, which can be passed between forum users, etc.
     * @return a code representing some ids for the components and arrangement.  Passing the same code to setCode() should re-create an identical reactor setup, even if other changes have happened in the meantime.
     */
    public String getCode() {
        return "erpA=" + buildCodeString();
    }
    
    /**
     * Sets a code to configure the entire grid all at once.  Expects the code to have originally been output by getCode().
     * @param code the code of the reactor setup to use.
     */
    public void setCode(final String code) {
        int pos = 0;
        int[][] ids = new int[grid.length][grid[0].length];
        char[][][] paramTypes = new char[grid.length][grid[0].length][MAX_PARAM_TYPES];
        int[][][] params = new int[grid.length][grid[0].length][MAX_PARAM_TYPES];
        if (code.startsWith("erpA=")) {
            readCodeString(0, code.substring(5));
        } else if (code.length() >= 108 && code.matches("[0-9A-Za-z(),|]+")) { //NOI18N
            try {
                for (int row = 0; row < grid.length; row++) {
                    for (int col = 0; col < grid[row].length; col++) {
                        ids[row][col] = Integer.parseInt(code.substring(pos, pos + 2), 16);
                        pos += 2;
                        int paramNum = 0;
                        if (pos + 1 < code.length() && code.charAt(pos) == '(') {
                            paramTypes[row][col][paramNum] = code.charAt(pos + 1);
                            int tempPos = pos + 2;
                            StringBuilder param = new StringBuilder(10);
                            while (tempPos < code.length() && code.charAt(tempPos) != ')') {
                                if (code.charAt(tempPos) == ',') {
                                    params[row][col][paramNum] = Integer.parseInt(param.toString(), 36);
                                    paramNum++;
                                    if (tempPos + 1 < code.length()) {
                                        tempPos++;
                                        paramTypes[row][col][paramNum] = code.charAt(tempPos);
                                    }
                                    param.setLength(0);
                                } else {
                                    param.append(code.charAt(tempPos));
                                }
                                tempPos++;
                            }
                            params[row][col][paramNum] = Integer.parseInt(param.toString(), 36);
                            pos = tempPos + 1;
                        }
                    }
                }
                for (int row = 0; row < grid.length; row++) {
                    for (int col = 0; col < grid[row].length; col++) {
                        final ReactorComponent component = ComponentFactory.createComponent(ids[row][col]);
                        for (int paramNum = 0; paramNum < MAX_PARAM_TYPES; paramNum++) {
                            switch (paramTypes[row][col][paramNum]) {
                                case 'h':
                                    component.setInitialHeat(params[row][col][paramNum]);
                                    break;
                                case 'a':
                                    component.automationThreshold = params[row][col][paramNum];
                                    break;
                                case 'p':
                                    component.reactorPause = params[row][col][paramNum];
                                    break;
                                default:
                                    break;
                            }
                        }
                        setComponentAt(row, col, component);
                    }
                }
                if (code.split("\\|").length > 1) {
                    String extraCode = code.split("\\|")[1];
                    switch (extraCode.charAt(0)) {
                        case 'f':
                            fluid = true;
                            break;
                        case 'e':
                            fluid = false;
                            break;
                        default:
                            break;
                    }
                    switch (extraCode.charAt(1)) {
                        case 's':
                            pulsed = false;
                            automated = false;
                            break;
                        case 'p':
                            pulsed = true;
                            automated = false;
                            break;
                        case 'a':
                            pulsed = true;
                            automated = false;
                            break;
                        default:
                            break;
                    }
                    switch (extraCode.charAt(2)) {
                        case 'i':
                            usingReactorCoolantInjectors = true;
                            break;
                        case 'n':
                            usingReactorCoolantInjectors = false;
                            break;
                        default:
                            break;
                    }
                    if (extraCode.length() > 3) {
                        currentHeat = Integer.parseInt(extraCode.substring(3), 36);
                    } else {
                        currentHeat = 0;
                    }
                }
                if (code.split("\\|").length > 2) {
                    String[] moreCodes = code.split("\\|");
                    for (int i = 2; i < moreCodes.length; i++) {
                        switch (moreCodes[i].charAt(0)) {
                            case 'n':
                                onPulse = Integer.parseInt(moreCodes[i].substring(1), 36);
                                break;
                            case 'f':
                                offPulse = Integer.parseInt(moreCodes[i].substring(1), 36);
                                break;
                            case 's':
                                suspendTemp = Integer.parseInt(moreCodes[i].substring(1), 36);
                                break;
                            case 'r':
                                resumeTemp = Integer.parseInt(moreCodes[i].substring(1), 36);
                                break;
                            default:
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        } else {
            String tempCode = code;
            if (code.startsWith("http://www.talonfiremage.pwp.blueyonder.co.uk/v3/reactorplanner.html?")) { //NOI18N
                tempCode = code.replace("http://www.talonfiremage.pwp.blueyonder.co.uk/v3/reactorplanner.html?", ""); //NOI18N
            }
            if (tempCode.matches("[0-9a-z]+")) { //NOI18N
                // Possibly a code from Talonius's old planner
                handleTaloniusCode(tempCode);
            }
        }
    }

    private void handleTaloniusCode(String tempCode) throws HeadlessException {
        StringBuilder warnings = new StringBuilder(500);
        TaloniusDecoder decoder = new TaloniusDecoder(tempCode);
        // initial heat, ignored by new planner.
        decoder.readInt(10);
        // reactor grid
        for (int x = 8; x >= 0; x--) {
            for (int y = 5; y >= 0; y--) {
                int nextValue = decoder.readInt(7);
                
                // items are no longer stackable in IC2 reactors, but stack sizes from the planner code still need to be handled
                if (nextValue > 64) {
                    nextValue = decoder.readInt(7);
                }
                
                switch (nextValue) {
                    case 0:
                        setComponentAt(y, x, null);
                        break;
                    case 1:
                        setComponentAt(y, x, new FuelRodUranium());
                        break;
                    case 2:
                        setComponentAt(y, x, new DualFuelRodUranium());
                        break;
                    case 3:
                        setComponentAt(y, x, new QuadFuelRodUranium());
                        break;
                    case 4:
                        warnings.append(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("Warning.DepletedIsotope"), y, x));
                        break;
                    case 5:
                        setComponentAt(y, x, new NeutronReflector());
                        break;
                    case 6:
                        setComponentAt(y, x, new ThickNeutronReflector());
                        break;
                    case 7:
                        setComponentAt(y, x, new HeatVent());
                        break;
                    case 8:
                        setComponentAt(y, x, new ReactorHeatVent());
                        break;
                    case 9:
                        setComponentAt(y, x, new OverclockedHeatVent());
                        break;
                    case 10:
                        setComponentAt(y, x, new AdvancedHeatVent());
                        break;
                    case 11:
                        setComponentAt(y, x, new ComponentHeatVent());
                        break;
                    case 12:
                        setComponentAt(y, x, new RshCondensator());
                        break;
                    case 13:
                        setComponentAt(y, x, new LzhCondensator());
                        break;
                    case 14:
                        setComponentAt(y, x, new HeatExchanger());
                        break;
                    case 15:
                        setComponentAt(y, x, new ReactorHeatExchanger());
                        break;
                    case 16:
                        setComponentAt(y, x, new ComponentHeatExchanger());
                        break;
                    case 17:
                        setComponentAt(y, x, new AdvancedHeatExchanger());
                        break;
                    case 18:
                        setComponentAt(y, x, new ReactorPlating());
                        break;
                    case 19:
                        setComponentAt(y, x, new HeatCapacityReactorPlating());
                        break;
                    case 20:
                        setComponentAt(y, x, new ContainmentReactorPlating());
                        break;
                    case 21:
                        setComponentAt(y, x, new CoolantCell10k());
                        break;
                    case 22:
                        setComponentAt(y, x, new CoolantCell30k());
                        break;
                    case 23:
                        setComponentAt(y, x, new CoolantCell60k());
                        break;
                    case 24:
                        warnings.append(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("Warning.Heating"), y, x));
                        break;
                    case 32:
                        setComponentAt(y, x, new FuelRodThorium());
                        break;
                    case 33:
                        setComponentAt(y, x, new DualFuelRodThorium());
                        break;
                    case 34:
                        setComponentAt(y, x, new QuadFuelRodThorium());
                        break;
                    case 35:
                        warnings.append(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("Warning.Plutonium"), y, x));
                        break;
                    case 36:
                        warnings.append(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("Warning.DualPlutonium"), y, x));
                        break;
                    case 37:
                        warnings.append(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("Warning.QuadPlutonium"), y, x));
                        break;
                    case 38:
                        setComponentAt(y, x, new IridiumNeutronReflector());
                        break;
                    case 39:
                        setComponentAt(y, x, new CoolantCell60kHelium());
                        break;
                    case 40:
                        setComponentAt(y, x, new CoolantCell180kHelium());
                        break;
                    case 41:
                        setComponentAt(y, x, new CoolantCell360kHelium());
                        break;
                    case 42:
                        setComponentAt(y, x, new CoolantCell60kNak());
                        break;
                    case 43:
                        setComponentAt(y, x, new CoolantCell180kNak());
                        break;
                    case 44:
                        setComponentAt(y, x, new CoolantCell360kNak());
                        break;
                    default:
                        warnings.append(String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("Warning.Unrecognized"), nextValue, y, x));
                        break;
                }
            }
        }
        if (warnings.length() > 0) {
            warnings.setLength(warnings.length() - 1);  // to remove last newline character
            JOptionPane.showMessageDialog(null, warnings, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("Warning.Title"), JOptionPane.WARNING_MESSAGE);
        }
    }

    // reads a Base64 code string for the reactor, after stripping the prefix.
    private void readCodeString(final int codeRevision, final String code) {
        BigintStorage storage = BigintStorage.inputBase64(code);
        // read the grid first
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                int componentId = 0;
                // Changes may be coming to the number of components available, so make sure to check the code revision number.
                if (codeRevision >= 0) {
                    componentId = storage.extract(38);
                }
                if (componentId != 0) {
                    ReactorComponent component = ComponentFactory.createComponent(componentId);
                    int hasSpecialAutomationConfig = storage.extract(1);
                    if (hasSpecialAutomationConfig > 0) {
                        component.setInitialHeat(storage.extract((int)360e3));
                        component.automationThreshold = storage.extract((int)360e3);
                        component.reactorPause = storage.extract((int)10e3);
                    }
                    setComponentAt(row, col, component);
                } else {
                    setComponentAt(row, col, null);
                }
            }
        }
        // next, read the inital temperature and other details.
        currentHeat = storage.extract((int)120e3);
        onPulse = storage.extract((int)5e6);
        offPulse = storage.extract((int)5e6);
        suspendTemp = storage.extract((int)120e3);
        resumeTemp = storage.extract((int)120e3);
        fluid = storage.extract(1) > 0;
        usingReactorCoolantInjectors = storage.extract(1) > 0;
        pulsed = storage.extract(1) > 0;
        automated = storage.extract(1) > 0;
        maxSimulationTicks = storage.extract((int)5e6);
    }
    
    // builds a Base64 code string, not including the prefix that indicates the code revision.
    private String buildCodeString() {
        BigintStorage storage = new BigintStorage();
        // first, store the extra details, in reverse order of expected reading.
        storage.store(maxSimulationTicks, (int)5e6);
        storage.store(automated ? 1 : 0, 1);
        storage.store(pulsed ? 1 : 0, 1);
        storage.store(usingReactorCoolantInjectors ? 1 : 0, 1);
        storage.store(fluid ? 1 : 0, 1);
        storage.store(resumeTemp, (int)120e3);
        storage.store(suspendTemp, (int)120e3);
        storage.store(offPulse, (int)5e6);
        storage.store(onPulse, (int)5e6);
        storage.store((int)currentHeat, (int)120e3);
        // grid is read first, so written last, and in reverse order
        for (int row = grid.length - 1; row >= 0; row--) {
            for (int col = grid[row].length - 1; col >= 0; col--) {
                ReactorComponent component = grid[row][col];
                if (component != null) {
                    int id = ComponentFactory.getID(grid[row][col]);
                    // only store automation details for a component if non-default, and add a flag bit to indicate their presence.  null components don't even need the flag bit.
                    if (component.getInitialHeat() > 0 || component.automationThreshold != ComponentFactory.getDefaultComponent(id).automationThreshold || component.reactorPause != ComponentFactory.getDefaultComponent(id).reactorPause) {
                        storage.store(component.reactorPause, (int)10e3);
                        storage.store(component.automationThreshold, (int)360e3);
                        storage.store((int)component.getInitialHeat(), (int)360e3);
                        storage.store(1, 1);
                    } else {
                        storage.store(0, 1);
                    }
                }
                storage.store(ComponentFactory.getID(grid[row][col]), 38);
            }
        }
        return storage.outputBase64();
    }
    
    /**
     * Checks whether the reactor is to simulate a fluid-style reactor, rather than a direct EU-output reactor.
     * @return true if this was set to be a fluid-style reactor, false if this was set to be direct EU-output reactor.
     */
    public boolean isFluid() {
        return fluid;
    }

    /**
     * Sets whether the reactor is to simulate a fluid-style reactor, rather than a direct EU-output reactor.
     * @param fluid true if this is to be a fluid-style reactor, false if this is to be direct EU-output reactor.
     */
    public void setFluid(final boolean fluid) {
        this.fluid = fluid;
    }
    
    /**
     * Checks whether the reactor is using Reactor Coolant Injectors (RCIs)
     * @return true if this reactor was set to use RCIs, false otherwise.
     */
    public boolean isUsingReactorCoolantInjectors() {
        return usingReactorCoolantInjectors;
    }
    
    /**
     * Sets whether the reactor is to use Reactor Coolant Injectors (RCIs)
     * @param usingReactorCoolantInjectors true if this reactor should use RCIs, false otherwise.
     */
    public void setUsingReactorCoolantInjectors(final boolean usingReactorCoolantInjectors) {
        this.usingReactorCoolantInjectors = usingReactorCoolantInjectors;
    }
    
    public int getOnPulse() {
        return onPulse;
    }
    
    public void setOnPulse(final int onPulse) {
        this.onPulse = onPulse;
    }
    
    public int getOffPulse() {
        return offPulse;
    }
    
    public void setOffPulse(final int offPulse) {
        this.offPulse = offPulse;
    }
    
    public int getSuspendTemp() {
        return suspendTemp;
    }
    
    public void setSuspendTemp(final int suspendTemp) {
        this.suspendTemp = suspendTemp;
    }
    
    public int getResumeTemp() {
        return resumeTemp;
    }
    
    public void setResumeTemp(final int resumeTemp) {
        this.resumeTemp = resumeTemp;
    }

    public boolean isPulsed() {
        return pulsed;
    }

    public void setPulsed(boolean pulsed) {
        this.pulsed = pulsed;
    }

    public boolean isAutomated() {
        return automated;
    }

    public void setAutomated(boolean automated) {
        this.automated = automated;
    }

    public int getMaxSimulationTicks() {
        return maxSimulationTicks;
    }

    public void setMaxSimulationTicks(int maxSimulationTicks) {
        this.maxSimulationTicks = maxSimulationTicks;
    }
    
}
