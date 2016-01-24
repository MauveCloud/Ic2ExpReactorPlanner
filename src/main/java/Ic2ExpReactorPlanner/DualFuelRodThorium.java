package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents a double thorium fuel rod.
 * @author Brian McCloud
 */
public class DualFuelRodThorium extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.Double_Thoriumcell.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(_("Iron"), 2, FuelRodThorium.MATERIALS);
    
    /**
     * Creates a new instance.
     */
    public DualFuelRodThorium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(50000);
        automationThreshold = 51000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return _("Double Fuel Rod (Thorium)");
    }
    
    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 2;
        int heat = pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 2;
        final Reactor parentReactor = getParent();
        double energy = 40 * pulses;
        parentReactor.addEUOutput(energy);
    }

    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public int getRodCount() {
        return 2;
    }
    
}
