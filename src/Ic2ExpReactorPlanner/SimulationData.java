/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

/**
 * Simple container for data from a simulation of an IC2 nuclear reactor, to allow comparison wtih another simulation.
 * @author Brian McCloud
 */
public class SimulationData {
    // Values should only be written to by the simulator class and read by other classes, but this is not yet strictly enforced.
    // Enforcement might require refactoring this to be an inner class of the simulator.
    
    // Times to temperature thresholds
    public int timeToBelow50 = Integer.MAX_VALUE;
    public int timeToBurn = Integer.MAX_VALUE;
    public int timeToEvaporate = Integer.MAX_VALUE;
    public int timeToHurt = Integer.MAX_VALUE;
    public int timeToLava = Integer.MAX_VALUE;
    public int timeToXplode = Integer.MAX_VALUE;
    
    // Special, for calculating efficiency
    public int totalRodCount = 0;
    
    // First component broken details
    public int firstComponentBrokenTime = Integer.MAX_VALUE;
    public int firstComponentBrokenRow = -1;
    public int firstComponentBrokenCol = -1;
    public String firstComponentBrokenDescription = "";
    public double prebreakTotalEUoutput = 0;
    public double prebreakAvgEUoutput = 0;
    public double prebreakMinEUoutput = Double.MAX_VALUE;
    public double prebreakMaxEUoutput = 0;
    public double prebreakTotalHUoutput = 0;
    public double prebreakAvgHUoutput = 0;
    public double prebreakMinHUoutput = Double.MAX_VALUE;
    public double prebreakMaxHUoutput = 0;
    
    // First rod depleted details
    public int firstRodDepletedTime = Integer.MAX_VALUE;
    public int firstRodDepletedRow = -1;
    public int firstRodDepletedCol = -1;
    public String firstRodDepletedDescription = "";
    public double predepleteTotalEUoutput = 0;
    public double predepleteAvgEUoutput = 0;
    public double predepleteMinEUoutput = Double.MAX_VALUE;
    public double predepleteMaxEUoutput = 0;
    public double predepleteTotalHUoutput = 0;
    public double predepleteAvgHUoutput = 0;
    public double predepleteMinHUoutput = Double.MAX_VALUE;
    public double predepleteMaxHUoutput = 0;
    public double predepleteMinTemp = Double.MAX_VALUE;
    public double predepleteMaxTemp = 0;
    
    // Completed-simulation details
    public double totalEUoutput = 0;
    public double avgEUoutput = 0;
    public double minEUoutput = Double.MAX_VALUE;
    public double maxEUoutput = 0;
    public double totalHUoutput = 0;
    public double avgHUoutput = 0;
    public double minHUoutput = Double.MAX_VALUE;
    public double maxHUoutput = 0;
    public double minTemp = Double.MAX_VALUE;
    public double maxTemp = 0;
    
}
