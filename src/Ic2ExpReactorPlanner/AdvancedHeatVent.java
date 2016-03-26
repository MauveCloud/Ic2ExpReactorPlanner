package Ic2ExpReactorPlanner;

/**
 * Represents an advanced heat vent.
 * @author Brian McCloud
 */
public class AdvancedHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentDiamond.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, HeatVent.MATERIALS, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("DIAMOND"), 4.5, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("IRON"));
    
    /**
     * Creates a new instance.
     */
    public AdvancedHeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
        automationThreshold = 900;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("ADVANCED HEAT VENT");
        if (getInitialHeat() > 0) {
            result += String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("INITIAL_HEAT_DISPLAY"), (int)getInitialHeat());
        }
        return result;
    }
    
    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }

    @Override
    public void dissipate() {
        final double currentDissipation = Math.min(12, getCurrentHeat());
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
        return 12;
    }

}
