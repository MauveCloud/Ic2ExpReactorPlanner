package Ic2ExpReactorPlanner;

/**
 * Represents a heat vent.
 * @author Brian McCloud
 */
public class HeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVent.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(4, "Iron Bars", 4, "Iron Plate", 3, "Iron Ingot", 1, "Tin Plate", 16, "Copper Cable");
    
    /**
     * Creates a new instance.
     */
    public HeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = "Heat Vent";
        if (getInitialHeat() > 0) {
            result += String.format(" (initial heat: %,d)", (int)getInitialHeat());
        }
        return result;
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }

    @Override
    public void dissipate() {
        final double currentDissipation = Math.min(6, getCurrentHeat());
        getParent().ventHeat(currentDissipation);
        adjustCurrentHeat(-currentDissipation);
        effectiveVentCooling = Math.max(effectiveVentCooling, currentDissipation);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }

    @Override
    public double getVentCoolingCapacity() {
        return 6;
    }

}
