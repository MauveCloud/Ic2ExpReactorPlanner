package Ic2ExpReactorPlanner.components;

import java.awt.*;

public class GGFuelRod extends FuelRod{

    private final int rodCount;
    private final double energyMult;
    private final int heatBonus;

    private static boolean GTNHbehavior = false;

    public static void setGTNHBehavior(boolean value) {
        GTNHbehavior = value;
    }

    public GGFuelRod(int id, String baseName, String name, Image image, double maxDamage, double maxHeat, String sourceMod, int energyMult, double heatMult, int rodCount, boolean moxStyle, int heatBonus) {
        super(id, baseName, name, image, maxDamage, maxHeat, sourceMod, energyMult, heatMult, rodCount, moxStyle);
        this.energyMult = energyMult;
        this.rodCount = rodCount;
        this.heatBonus = heatBonus;
    }

    public GGFuelRod(GGFuelRod other) {
        super(other);
        this.energyMult = other.energyMult;
        this.rodCount = other.rodCount;
        this.heatBonus = other.heatBonus;
    }

    private int countNeutronNeighbors() {
        int neutronNeighbors = 0;
        ReactorItem component = parent.getComponentAt(row + 1, col);
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        component = parent.getComponentAt(row - 1, col);
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        component = parent.getComponentAt(row, col - 1);
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        component = parent.getComponentAt(row, col + 1);
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        return neutronNeighbors;
    }

    @Override
    public double generateEnergy() {
        int pulses = countNeutronNeighbors() + 1 + rodCount / 2;
        double energy = energyMult * pulses *(1 + heatBonus * ((float) parent.getCurrentHeat() / (float) parent.getMaxHeat()));
        if (GTNHbehavior || "GTNH".equals(sourceMod)) {
            energy *= 5;//EUx5 if from GTNH or in GTNH mode, no gt bonus
        }
        minEUGenerated = Math.min(minEUGenerated, energy);
        maxEUGenerated = Math.max(maxEUGenerated, energy);
        currentEUGenerated = energy;
        parent.addEUOutput(energy);
        applyDamage(1.0);
        return energy;
    }

}
