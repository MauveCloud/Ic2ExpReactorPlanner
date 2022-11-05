package Ic2ExpReactorPlanner.components;

import java.awt.*;

public class BreederCell extends ReactorItem{

    private final int mHeatBonusStep;
    private final int mHeatBonusMultiplier;

    public BreederCell(int id, String baseName, String name, Image image, double maxDamage, double maxHeat, String sourceMod, int aHeatBonusStep, int aHeatBonusMultiplier) {
        super(id, baseName, name, image, maxDamage, maxHeat, sourceMod);
        this.mHeatBonusStep = aHeatBonusStep;
        this.mHeatBonusMultiplier = aHeatBonusMultiplier;
    }

    public BreederCell(BreederCell other) {
        super(other);
        this.mHeatBonusStep = other.mHeatBonusStep;
        this.mHeatBonusMultiplier = other.mHeatBonusMultiplier;
    }

    @Override
    public double generateHeat() {
        double targetDamage = 1 + parent.getCurrentHeat() / mHeatBonusStep * mHeatBonusMultiplier;
        ReactorItem component = parent.getComponentAt(row - 1, col);
        if (component != null) {
            applyDamage(targetDamage);
        }
        component = parent.getComponentAt(row, col + 1);
        if (component != null) {
            applyDamage(targetDamage);
        }
        component = parent.getComponentAt(row + 1, col);
        if (component != null) {
            applyDamage(targetDamage);
        }
        component = parent.getComponentAt(row, col - 1);
        if (component != null) {
            applyDamage(targetDamage);
        }
        return 0;
    }
}
