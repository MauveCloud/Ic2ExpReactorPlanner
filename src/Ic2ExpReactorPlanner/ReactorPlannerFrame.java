/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.BundleHelper.formatI18n;
import static Ic2ExpReactorPlanner.BundleHelper.getI18n;
import Ic2ExpReactorPlanner.components.FuelRod;
import Ic2ExpReactorPlanner.components.ReactorItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * JFrame to display a gui for planning a Nuclear Reactor using IndustrialCraft2 Experimental.
 * @author Brian McCloud
 */
public class ReactorPlannerFrame extends javax.swing.JFrame {

    private static final String VERSION = "@VERSION@";
    
    private final Reactor reactor = new Reactor();
    
    private final JButton[][] reactorButtons = new JButton[6][9];
    
    private final JPanel[][] reactorButtonPanels = new JPanel[6][9];
    
    private int selectedRow = -1;
    
    private int selectedColumn = -1;
    
    // Lock variable to (hopefully) prevent recursive modification of the reactor code, while allowing design changes to affect the code and pasting the code to change the design.
    private boolean lockCode = false;
    
    private final JFileChooser csvChooser = new JFileChooser();
    
    private final JFileChooser textureChooser = new JFileChooser();
    
    private final ReactorItem[] paletteComponents = new ReactorItem[ComponentFactory.getComponentCount()];
    
    private int paletteComponentId = 0;
    
    private SpinnerNumberModel heatSpinnerModel = new SpinnerNumberModel();
    
    private final LinkedList<JButton> componentDetailButtons = new LinkedList<>();
    
    private static final Properties ADVANCED_CONFIG = new Properties();
    
    /**
     * The reactor that was last simulated.
     */
    private Reactor simulatedReactor = null;

    private AutomationSimulator simulator = null;

    private String currentReactorCode = null;
    
    private String currentReactorOldCode = null;
    
    private AutomationSimulator prevSimulator = null;
    
    private String prevReactorCode = null;
    
    private String prevReactorOldCode = null;
    
    /**
     * Creates new form ReactorPlannerFrame
     */
    public ReactorPlannerFrame() {
        initComponents();
        advancedScroll.getVerticalScrollBar().setUnitIncrement(16);
        ToolTipManager.sharedInstance().setDismissDelay((int)30e3);
        Enumeration<AbstractButton> buttons = componentsGroup.getElements();
        if (heatSpinner.getModel() instanceof SpinnerNumberModel) {
            heatSpinnerModel = (SpinnerNumberModel)heatSpinner.getModel();
        }
        while (buttons.hasMoreElements()) {
            final AbstractButton button = buttons.nextElement();
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("empty".equals(button.getActionCommand())) {
                        placingLabel.setText(getI18n("UI.ComponentPlacingDefault"));
                        paletteComponentId = 0;
                    } else if (button.getActionCommand() != null) {
                        ReactorItem tempComponent = ComponentFactory.getDefaultComponent(button.getActionCommand());
                        if (tempComponent != null) {
                            paletteComponentId = tempComponent.id;
                            if (paletteComponents[paletteComponentId] == null) {
                                paletteComponents[paletteComponentId] = ComponentFactory.createComponent(paletteComponentId);
                            }
                            placingLabel.setText(formatI18n("UI.ComponentPlacingSpecific",
                                    paletteComponents[paletteComponentId].toString()));
                            componentHeatSpinner.setValue(paletteComponents[paletteComponentId].getInitialHeat());
                            placingThresholdSpinner.setValue(paletteComponents[paletteComponentId].getAutomationThreshold());
                            placingReactorPauseSpinner.setValue(paletteComponents[paletteComponentId].getReactorPause());
                        }
                    }
                }
            });
        }
        for (int row = 0; row < reactorButtons.length; row++) {
            final int finalRow = row;
            for (int col = 0; col < reactorButtons[row].length; col++) {
                final int finalCol = col;
                reactorButtonPanels[row][col] = new JPanel();
                reactorButtonPanels[row][col].setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.darkGray));
                reactorButtonPanels[row][col].setLayout(new GridBagLayout());
                reactorButtonPanels[row][col].setBackground(Color.LIGHT_GRAY);
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.weightx = 0;
                constraints.weighty = 0;
                JButton automationButton = new JButton(getI18n("UI.AutomateButton"));
                automationButton.setFont(Font.decode(getI18n("UI.AutomateButtonFont")));
                automationButton.setMargin(new Insets(-2, 0, -2, 0));
                automationButton.setToolTipText(getI18n("UI.AutomationTooltip"));
                automationButton.setFocusable(false);
                automationButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final ReactorItem component = reactor.getComponentAt(finalRow, finalCol);
                        selectedRow = finalRow;
                        selectedColumn = finalCol;
                        if (component == null) {
                            selectedComponentLabel.setText(formatI18n("UI.NoComponentRowCol", finalRow, finalCol));
                        } else {
                            selectedComponentLabel.setText(formatI18n("UI.ChosenComponentRowCol", component.toString(), finalRow, finalCol));
                            thresholdSpinner.setValue(component.getAutomationThreshold());
                            pauseSpinner.setValue(component.getReactorPause());
                        }
                        outputTabs.setSelectedComponent(automationPanel);
                    }
                });
                reactorButtonPanels[row][col].add(automationButton, constraints);
                componentDetailButtons.add(automationButton);
                reactorButtonPanels[row][col].add(new JLabel(), constraints);
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                constraints.anchor = GridBagConstraints.EAST;
                JButton infoButton = new JButton(getI18n("UI.ComponentInfoButton"));
                infoButton.setFont(Font.decode(getI18n("UI.ComponentInfoButtonFont")));
                infoButton.setMargin(new Insets(-2, 0, -2, 0));
                infoButton.setFocusable(false);
                infoButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (simulatedReactor != null) {
                            final ReactorItem component = simulatedReactor.getComponentAt(finalRow, finalCol);
                            if (component == null) {
                                componentArea.setText(formatI18n("UI.NoComponentLastSimRowCol", finalRow, finalCol));
                            } else {
                                componentArea.setText(formatI18n("UI.ComponentInfoLastSimRowCol", component.toString(), finalRow, finalCol, component.info));
                            }
                        } else {
                            componentArea.setText(getI18n("UI.NoSimulationRun"));
                        }
                        outputTabs.setSelectedComponent(componentPane);
                    }
                });
                infoButton.setToolTipText(getI18n("UI.ComponentInfoButtonTooltip"));
                reactorButtonPanels[row][col].add(infoButton, constraints);
                componentDetailButtons.add(infoButton);
                constraints.weightx = 1.0;
                constraints.weighty = 1.0;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                reactorButtons[row][col] = new JButton();
                reactorButtons[row][col].addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if ((e.getModifiers() & ActionEvent.ALT_MASK) != 0) {
                            ReactorItem component = reactor.getComponentAt(finalRow, finalCol);
                            if (component != null) {
                                paletteComponentId = component.id;
                                if (paletteComponents[paletteComponentId] == null) {
                                    paletteComponents[paletteComponentId] = ComponentFactory.createComponent(paletteComponentId);
                                }
                                paletteComponents[paletteComponentId].setInitialHeat(component.getInitialHeat());
                                paletteComponents[paletteComponentId].setAutomationThreshold(component.getAutomationThreshold());
                                paletteComponents[paletteComponentId].setReactorPause(component.getReactorPause());
                                Enumeration<AbstractButton> buttons = componentsGroup.getElements();
                                while (buttons.hasMoreElements()) {
                                    AbstractButton button = buttons.nextElement();
                                    if (component.baseName.equals(button.getActionCommand())) {
                                        button.doClick();
                                    }
                                }
                            }
                            return;
                        }
                        ReactorItem componentToPlace = null;
                        final ButtonModel selection = componentsGroup.getSelection();
                        if (selection != null) {
                            componentToPlace = ComponentFactory.createComponent(selection.getActionCommand());
                            if (componentToPlace != null) {
                                componentToPlace.setInitialHeat(((Number)componentHeatSpinner.getValue()).intValue());
                                componentToPlace.setAutomationThreshold(((Number)placingThresholdSpinner.getValue()).intValue());
                                componentToPlace.setReactorPause(((Number)placingReactorPauseSpinner.getValue()).intValue());
                            }
                        }
                        reactor.setComponentAt(finalRow, finalCol, componentToPlace);
                        materialsArea.setText(reactor.getMaterials().toString());
                        componentListArea.setText(reactor.getComponentList().toString());
                        maxHeatLabel.setText(formatI18n("UI.MaxHeatSpecific", reactor.getMaxHeat()));
                        heatSpinnerModel.setMaximum(reactor.getMaxHeat() - 1);
                        heatSpinnerModel.setValue(Math.min(((Number)heatSpinnerModel.getValue()).intValue(), reactor.getMaxHeat() - 1));
                        temperatureEffectsLabel.setText(formatI18n("UI.TemperatureEffectsSpecific", (int) (reactor.getMaxHeat() * 0.4), (int) (reactor.getMaxHeat() * 0.5), (int) (reactor.getMaxHeat() * 0.7), (int) (reactor.getMaxHeat() * 0.85), (int) (reactor.getMaxHeat() * 1.0)));
                        int buttonSize = Math.min(reactorButtons[finalRow][finalCol].getWidth(), reactorButtons[finalRow][finalCol].getHeight());
                        if (buttonSize > 2 && componentToPlace != null && componentToPlace.image != null) {
                            reactorButtons[finalRow][finalCol].setIcon(new ImageIcon(componentToPlace.image.getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
                            reactorButtons[finalRow][finalCol].setToolTipText(componentToPlace.toString());
                            reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                        } else {
                            reactorButtons[finalRow][finalCol].setIcon(null);
                            if (componentToPlace != null) {
                                reactorButtons[finalRow][finalCol].setToolTipText(componentToPlace.toString());
                            } else {
                                reactorButtons[finalRow][finalCol].setToolTipText(null);
                            }
                            reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                        }
                        lockCode = true;
                        updateCodeField();
                        lockCode = false;
                    }
                });

                reactorButtons[row][col].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            reactor.setComponentAt(finalRow, finalCol, null);
                            materialsArea.setText(reactor.getMaterials().toString());
                            componentListArea.setText(reactor.getComponentList().toString());
                            maxHeatLabel.setText(formatI18n("UI.MaxHeatSpecific", reactor.getMaxHeat()));
                            heatSpinnerModel.setMaximum(reactor.getMaxHeat() - 1);
                            heatSpinnerModel.setValue(Math.min(((Number) heatSpinnerModel.getValue()).intValue(), reactor.getMaxHeat() - 1));
                            temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int) (reactor.getMaxHeat() * 0.4), (int) (reactor.getMaxHeat() * 0.5), (int) (reactor.getMaxHeat() * 0.7), (int) (reactor.getMaxHeat() * 0.85), (int) (reactor.getMaxHeat() * 1.0)));
                            reactorButtons[finalRow][finalCol].setIcon(null);
                            reactorButtons[finalRow][finalCol].setToolTipText(null);
                            reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                            lockCode = true;
                            updateCodeField();
                            lockCode = false;
                        } else if (e.getButton() == MouseEvent.BUTTON2) {
                            ReactorItem component = reactor.getComponentAt(finalRow, finalCol);
                            if (component != null) {
                                paletteComponentId = component.id;
                                if (paletteComponents[paletteComponentId] == null) {
                                    paletteComponents[paletteComponentId] = ComponentFactory.createComponent(paletteComponentId);
                                }
                                paletteComponents[paletteComponentId].setInitialHeat(component.getInitialHeat());
                                paletteComponents[paletteComponentId].setAutomationThreshold(component.getAutomationThreshold());
                                paletteComponents[paletteComponentId].setReactorPause(component.getReactorPause());
                                Enumeration<AbstractButton> buttons = componentsGroup.getElements();
                                while (buttons.hasMoreElements()) {
                                    AbstractButton button = buttons.nextElement();
                                    if (component.baseName.equals(button.getActionCommand())) {
                                        button.doClick();
                                    }
                                }
                            }
                        }
                    }
                    
                });
                reactorButtons[row][col].setContentAreaFilled(false);
                reactorButtons[row][col].setOpaque(true);
                reactorButtons[row][col].setBackground(Color.LIGHT_GRAY);
                reactorButtonPanels[row][col].add(reactorButtons[row][col], constraints);
                reactorPanel.add(reactorButtonPanels[row][col]);
            }
        }
        codeField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!lockCode) {
                    lockCode = true;
                    reactor.setCode(codeField.getText());
                    updateReactorButtons();
                    if (reactor.isFluid()) {
                        fluidReactorRadio.setSelected(true);
                    } else {
                        euReactorRadio.setSelected(true);
                    }
                    automatedReactorCheck.setSelected(reactor.isAutomated());
                    pulsedReactorCheck.setSelected(reactor.isPulsed());
                    togglePulseConfigTab();
                    reactorCoolantInjectorCheckbox.setSelected(reactor.isUsingReactorCoolantInjectors());
                    heatSpinner.setValue(reactor.getCurrentHeat());
                    onPulseSpinner.setValue(reactor.getOnPulse());
                    offPulseSpinner.setValue(reactor.getOffPulse());
                    suspendTempSpinner.setValue(reactor.getSuspendTemp());
                    resumeTempSpinner.setValue(reactor.getResumeTemp());
                    maxSimulationTicksSpinner.setValue(reactor.getMaxSimulationTicks());
                    lockCode = false;
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(null);
            }
        });
        loadAdvancedConfig();
    }

    private void loadAdvancedConfig() {
        try (FileInputStream configStream = new FileInputStream("erpprefs.xml")) {
            ADVANCED_CONFIG.loadFromXML(configStream);
            showComponentDetailButtonsCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("showComponentDetailButtons", "true")));
            enableGT508ComponentsCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("enableGT508Components", "true")));
            enableGT509ComponentsCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("enableGT509Components", "true")));
            showOldStyleReactorCodeCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("showOldStyleReactorCode", "false")));
            showComponentPreconfigCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("showComponentPreconfigControls", "true")));
            gt509BehaviorCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("gt509ReactorBehavior", "false")));
            useGTRecipesCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("useGTrecipes", "false")));
            useUfcForCoolantCellsCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("useUfcForCoolant", "false")));
            expandAdvancedAlloyCheck.setSelected(Boolean.valueOf(ADVANCED_CONFIG.getProperty("expandAdvancedAlloy", "false")));
            FuelRod.setGT509Behavior(gt509BehaviorCheck.isSelected());
            String texturePackName = ADVANCED_CONFIG.getProperty("texturePack");
            if (texturePackName != null) {
                File texturePackFile = new File(texturePackName);
                if (texturePackFile.isFile()) {
                    textureChooser.setSelectedFile(texturePackFile);
                    texturePackLabel.setText(formatI18n("UI.TexturePackSpecific", texturePackName));
                }
            }
            String csvFileName = ADVANCED_CONFIG.getProperty("csvFile");
            if (csvFileName != null) {
                csvChooser.setSelectedFile(new File (csvFileName));
                csvFileLabel.setText(csvFileName);
            }
            showComponentDetailButtonsCheckActionPerformed(null);
            enableGT508ComponentsCheckActionPerformed(null);
            enableGT509ComponentsCheckActionPerformed(null);
            showComponentPreconfigCheckActionPerformed(null);
            useUfcForCoolantCellsCheckActionPerformed(null);
            expandAdvancedAlloyCheckActionPerformed(null);
            useGTRecipesCheckActionPerformed(null);
        } catch (FileNotFoundException ex) {
            // ignore, this might just mean the file hasn't been created yet.
        } catch (IOException | NullPointerException ex) {
            // ignore, security settings or whatever preventing reading the xml file should not stop the planner from running.
        }
    }
    
    private void saveAdvancedConfig() {
        try (FileOutputStream configStream = new FileOutputStream("erpprefs.xml")) {
            ADVANCED_CONFIG.setProperty("showComponentDetailButtons", Boolean.toString(showComponentDetailButtonsCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("enableGT508Components", Boolean.toString(enableGT508ComponentsCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("enableGT509Components", Boolean.toString(enableGT509ComponentsCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("showOldStyleReactorCode", Boolean.toString(showOldStyleReactorCodeCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("showComponentPreconfigControls", Boolean.toString(showComponentPreconfigCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("gt509ReactorBehavior", Boolean.toString(gt509BehaviorCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("useGTrecipes", Boolean.toString(useGTRecipesCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("useUfcForCoolant", Boolean.toString(useUfcForCoolantCellsCheck.isSelected()));
            ADVANCED_CONFIG.setProperty("expandAdvancedAlloy", Boolean.toString(expandAdvancedAlloyCheck.isSelected()));
            if (csvChooser.getSelectedFile() != null) {
                ADVANCED_CONFIG.setProperty("csvFile", csvChooser.getSelectedFile().getAbsolutePath());
            }
            ADVANCED_CONFIG.storeToXML(configStream, null);
        } catch (IOException | NullPointerException | ClassCastException ex) {
            // ignore and keep running anyway
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        componentsGroup = new javax.swing.ButtonGroup();
        reactorStyleGroup = new javax.swing.ButtonGroup();
        pulsePanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        onPulseSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        offPulseSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        suspendTempSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel10 = new javax.swing.JLabel();
        resumeTempSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel16 = new javax.swing.JLabel();
        resetPulseConfigButton = new javax.swing.JButton();
        javax.swing.JSplitPane jSplitPane1 = new javax.swing.JSplitPane();
        javax.swing.JPanel jPanel2 = new javax.swing.JPanel();
        javax.swing.JSplitPane jSplitPane2 = new javax.swing.JSplitPane();
        reactorPanel = new javax.swing.JPanel();
        javax.swing.JSplitPane jSplitPane3 = new javax.swing.JSplitPane();
        temperatureAndComponentsPanel = new javax.swing.JPanel();
        temperatureEffectsLabel = new javax.swing.JLabel();
        componentsPanel = new javax.swing.JPanel();
        emptyButton = new javax.swing.JToggleButton();
        fuelRodUraniumButton = new javax.swing.JToggleButton();
        dualFuelRodUraniumButton = new javax.swing.JToggleButton();
        quadFuelRodUraniumButton = new javax.swing.JToggleButton();
        fuelRodMoxButton = new javax.swing.JToggleButton();
        dualFuelRodMoxButton = new javax.swing.JToggleButton();
        quadFuelRodMoxButton = new javax.swing.JToggleButton();
        neutronReflectorButton = new javax.swing.JToggleButton();
        thickNeutronReflectorButton = new javax.swing.JToggleButton();
        heatVentButton = new javax.swing.JToggleButton();
        advancedHeatVentButton = new javax.swing.JToggleButton();
        reactorHeatVentButton = new javax.swing.JToggleButton();
        componentHeatVentButton = new javax.swing.JToggleButton();
        overclockedHeatVentButton = new javax.swing.JToggleButton();
        coolantCell10kButton = new javax.swing.JToggleButton();
        coolantCell30kButton = new javax.swing.JToggleButton();
        coolantCell60kButton = new javax.swing.JToggleButton();
        heatExchangerButton = new javax.swing.JToggleButton();
        advancedHeatExchangerButton = new javax.swing.JToggleButton();
        coreHeatExchangerButton = new javax.swing.JToggleButton();
        componentHeatExchangerButton = new javax.swing.JToggleButton();
        reactorPlatingButton = new javax.swing.JToggleButton();
        heatCapacityReactorPlatingButton = new javax.swing.JToggleButton();
        containmentReactorPlatingButton = new javax.swing.JToggleButton();
        rshCondensatorButton = new javax.swing.JToggleButton();
        lzhCondensatorButton = new javax.swing.JToggleButton();
        fuelRodThoriumButton = new javax.swing.JToggleButton();
        dualFuelRodThoriumButton = new javax.swing.JToggleButton();
        quadFuelRodThoriumButton = new javax.swing.JToggleButton();
        coolantCellHelium60kButton = new javax.swing.JToggleButton();
        coolantCellHelium180kButton = new javax.swing.JToggleButton();
        coolantCellHelium360kButton = new javax.swing.JToggleButton();
        coolantCellNak60kButton = new javax.swing.JToggleButton();
        coolantCellNak180kButton = new javax.swing.JToggleButton();
        coolantCellNak360kButton = new javax.swing.JToggleButton();
        iridiumNeutronReflectorButton = new javax.swing.JToggleButton();
        fuelRodNaquadahButton = new javax.swing.JToggleButton();
        dualFuelRodNaquadahButton = new javax.swing.JToggleButton();
        quadFuelRodNaquadahButton = new javax.swing.JToggleButton();
        javax.swing.JPanel jPanel8 = new javax.swing.JPanel();
        placingLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        componentHeatLabel = new javax.swing.JLabel();
        componentHeatSpinner = new javax.swing.JSpinner();
        placingThresholdLabel = new javax.swing.JLabel();
        placingThresholdSpinner = new javax.swing.JSpinner();
        placingReactorPauseLabel = new javax.swing.JLabel();
        placingReactorPauseSpinner = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        javax.swing.JPanel jPanel3 = new javax.swing.JPanel();
        euReactorRadio = new javax.swing.JRadioButton();
        fluidReactorRadio = new javax.swing.JRadioButton();
        javax.swing.JPanel jPanel4 = new javax.swing.JPanel();
        clearGridButton = new javax.swing.JButton();
        simulateButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel5 = new javax.swing.JPanel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        heatSpinner = new javax.swing.JSpinner();
        maxHeatLabel = new javax.swing.JLabel();
        javax.swing.JPanel jPanel6 = new javax.swing.JPanel();
        pulsedReactorCheck = new javax.swing.JCheckBox();
        automatedReactorCheck = new javax.swing.JCheckBox();
        javax.swing.JPanel jPanel7 = new javax.swing.JPanel();
        maxSimulationTicksLabel = new javax.swing.JLabel();
        maxSimulationTicksSpinner = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        reactorCoolantInjectorCheckbox = new javax.swing.JCheckBox();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        codeField = new javax.swing.JTextField();
        copyCodeButton = new javax.swing.JButton();
        pasteCodeButton = new javax.swing.JButton();
        outputTabs = new javax.swing.JTabbedPane();
        outputPane = new javax.swing.JScrollPane();
        outputArea = new javax.swing.JTextArea();
        componentPane = new javax.swing.JScrollPane();
        componentArea = new javax.swing.JTextArea();
        automationPanel = new javax.swing.JPanel();
        selectedComponentLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel12 = new javax.swing.JLabel();
        thresholdSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel13 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel14 = new javax.swing.JLabel();
        pauseSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel15 = new javax.swing.JLabel();
        componentListPane = new javax.swing.JScrollPane();
        componentListArea = new javax.swing.JTextArea();
        materialsListPane = new javax.swing.JScrollPane();
        materialsArea = new javax.swing.JTextArea();
        csvPanel = new javax.swing.JPanel();
        csvOutputCheck = new javax.swing.JCheckBox();
        javax.swing.JLabel jLabel19 = new javax.swing.JLabel();
        csvLimitSpinner = new javax.swing.JSpinner();
        csvFileLabel = new javax.swing.JLabel();
        csvBrowseButton = new javax.swing.JButton();
        javax.swing.JLabel csvHelpLabel = new javax.swing.JLabel();
        advancedScroll = new javax.swing.JScrollPane();
        advancedPanel = new javax.swing.JPanel();
        showComponentDetailButtonsCheck = new javax.swing.JCheckBox();
        enableGT508ComponentsCheck = new javax.swing.JCheckBox();
        enableGT509ComponentsCheck = new javax.swing.JCheckBox();
        showOldStyleReactorCodeCheck = new javax.swing.JCheckBox();
        showComponentPreconfigCheck = new javax.swing.JCheckBox();
        texturePackLabel = new javax.swing.JLabel();
        texturePackBrowseButton = new javax.swing.JButton();
        texturePackClearButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        gt509BehaviorCheck = new javax.swing.JCheckBox();
        useGTRecipesCheck = new javax.swing.JCheckBox();
        useUfcForCoolantCellsCheck = new javax.swing.JCheckBox();
        expandAdvancedAlloyCheck = new javax.swing.JCheckBox();
        comparisonScroll = new javax.swing.JScrollPane();
        javax.swing.JPanel comparisonPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel17 = new javax.swing.JLabel();
        comparisonCodeField = new javax.swing.JTextField();
        comparisonCopyCodeButton = new javax.swing.JButton();
        javax.swing.JPanel jPanel9 = new javax.swing.JPanel();
        lockPrevCodeCheck = new javax.swing.JCheckBox();
        onlyShowDiffCheck = new javax.swing.JCheckBox();
        copyComparisonButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel18 = new javax.swing.JLabel();
        comparisonLabel = new javax.swing.JLabel();

        pulsePanel.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle"); // NOI18N
        jLabel3.setText(bundle.getString("Config.OnPulse")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel3, gridBagConstraints);

        onPulseSpinner.setModel(new javax.swing.SpinnerNumberModel(5000000, 0, 5000000, 1));
        onPulseSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        onPulseSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        onPulseSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                onPulseSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(onPulseSpinner, gridBagConstraints);

        jLabel4.setText(bundle.getString("Config.Seconds")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel4, gridBagConstraints);

        jLabel7.setText(bundle.getString("Config.OffPulse")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 2, 2);
        pulsePanel.add(jLabel7, gridBagConstraints);

        offPulseSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 5000000, 1));
        offPulseSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        offPulseSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        offPulseSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                offPulseSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(offPulseSpinner, gridBagConstraints);

        jLabel8.setText(bundle.getString("Config.Seconds")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel8, gridBagConstraints);

        jLabel11.setText(bundle.getString("Config.PulseHelp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel11, gridBagConstraints);

        jLabel9.setText(bundle.getString("Config.SuspendTemp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel9, gridBagConstraints);

        suspendTempSpinner.setModel(new javax.swing.SpinnerNumberModel(120000, 0, 120000, 1));
        suspendTempSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        suspendTempSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        suspendTempSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                suspendTempSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(suspendTempSpinner, gridBagConstraints);

        jLabel10.setText(bundle.getString("Config.ResumeTemp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 2, 2);
        pulsePanel.add(jLabel10, gridBagConstraints);

        resumeTempSpinner.setModel(new javax.swing.SpinnerNumberModel(120000, 0, 120000, 1));
        resumeTempSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        resumeTempSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        resumeTempSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                resumeTempSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(resumeTempSpinner, gridBagConstraints);

        jLabel16.setText(bundle.getString("Config.SuspendTempHelp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel16, gridBagConstraints);

        resetPulseConfigButton.setText(bundle.getString("UI.ResetPulseConfig")); // NOI18N
        resetPulseConfigButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetPulseConfigButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(resetPulseConfigButton, gridBagConstraints);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("UI.MainTitle")); // NOI18N
        setMinimumSize(new java.awt.Dimension(915, 700));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                plannerResized(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jSplitPane2.setResizeWeight(1.0);

        reactorPanel.setMinimumSize(new java.awt.Dimension(450, 300));
        reactorPanel.setPreferredSize(new java.awt.Dimension(450, 300));
        reactorPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                reactorPanelComponentResized(evt);
            }
        });
        reactorPanel.setLayout(new java.awt.GridLayout(6, 9, 2, 2));
        jSplitPane2.setLeftComponent(reactorPanel);

        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setResizeWeight(1.0);

        temperatureAndComponentsPanel.setMinimumSize(new java.awt.Dimension(475, 240));
        temperatureAndComponentsPanel.setPreferredSize(new java.awt.Dimension(475, 240));
        temperatureAndComponentsPanel.setLayout(new java.awt.GridBagLayout());

        temperatureEffectsLabel.setText(bundle.getString("UI.TemperatureEffectsDefault")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(temperatureEffectsLabel, gridBagConstraints);

        componentsPanel.setMinimumSize(new java.awt.Dimension(160, 80));
        componentsPanel.setPreferredSize(new java.awt.Dimension(160, 80));
        componentsPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                componentsPanelComponentResized(evt);
            }
        });
        componentsPanel.setLayout(new java.awt.GridLayout(5, 8));

        componentsGroup.add(emptyButton);
        emptyButton.setActionCommand("empty"); // NOI18N
        emptyButton.setMaximumSize(new java.awt.Dimension(50, 50));
        emptyButton.setMinimumSize(new java.awt.Dimension(50, 50));
        emptyButton.setPreferredSize(new java.awt.Dimension(50, 50));
        componentsPanel.add(emptyButton);

        componentsGroup.add(fuelRodUraniumButton);
        fuelRodUraniumButton.setToolTipText(buildTooltipInfo("FuelRodUranium"));
        fuelRodUraniumButton.setActionCommand("fuelRodUranium"); // NOI18N
        componentsPanel.add(fuelRodUraniumButton);

        componentsGroup.add(dualFuelRodUraniumButton);
        dualFuelRodUraniumButton.setToolTipText(buildTooltipInfo("DualFuelRodUranium"));
        dualFuelRodUraniumButton.setActionCommand("dualFuelRodUranium"); // NOI18N
        componentsPanel.add(dualFuelRodUraniumButton);

        componentsGroup.add(quadFuelRodUraniumButton);
        quadFuelRodUraniumButton.setToolTipText(buildTooltipInfo("QuadFuelRodUranium"));
        quadFuelRodUraniumButton.setActionCommand("quadFuelRodUranium"); // NOI18N
        componentsPanel.add(quadFuelRodUraniumButton);

        componentsGroup.add(fuelRodMoxButton);
        fuelRodMoxButton.setToolTipText(buildTooltipInfo("FuelRodMox"));
        fuelRodMoxButton.setActionCommand("fuelRodMox"); // NOI18N
        componentsPanel.add(fuelRodMoxButton);

        componentsGroup.add(dualFuelRodMoxButton);
        dualFuelRodMoxButton.setToolTipText(buildTooltipInfo("DualFuelRodMox"));
        dualFuelRodMoxButton.setActionCommand("dualFuelRodMox"); // NOI18N
        componentsPanel.add(dualFuelRodMoxButton);

        componentsGroup.add(quadFuelRodMoxButton);
        quadFuelRodMoxButton.setToolTipText(buildTooltipInfo("QuadFuelRodMox"));
        quadFuelRodMoxButton.setActionCommand("quadFuelRodMox"); // NOI18N
        componentsPanel.add(quadFuelRodMoxButton);

        componentsGroup.add(neutronReflectorButton);
        neutronReflectorButton.setToolTipText(buildTooltipInfo("NeutronReflector"));
        neutronReflectorButton.setActionCommand("neutronReflector"); // NOI18N
        componentsPanel.add(neutronReflectorButton);

        componentsGroup.add(thickNeutronReflectorButton);
        thickNeutronReflectorButton.setToolTipText(buildTooltipInfo("ThickNeutronReflector"));
        thickNeutronReflectorButton.setActionCommand("thickNeutronReflector"); // NOI18N
        componentsPanel.add(thickNeutronReflectorButton);

        componentsGroup.add(heatVentButton);
        heatVentButton.setToolTipText(buildTooltipInfo("HeatVent"));
        heatVentButton.setActionCommand("heatVent"); // NOI18N
        componentsPanel.add(heatVentButton);

        componentsGroup.add(advancedHeatVentButton);
        advancedHeatVentButton.setToolTipText(buildTooltipInfo("AdvancedHeatVent"));
        advancedHeatVentButton.setActionCommand("advancedHeatVent"); // NOI18N
        componentsPanel.add(advancedHeatVentButton);

        componentsGroup.add(reactorHeatVentButton);
        reactorHeatVentButton.setToolTipText(buildTooltipInfo("ReactorHeatVent"));
        reactorHeatVentButton.setActionCommand("reactorHeatVent"); // NOI18N
        componentsPanel.add(reactorHeatVentButton);

        componentsGroup.add(componentHeatVentButton);
        componentHeatVentButton.setToolTipText(buildTooltipInfo("ComponentHeatVent"));
        componentHeatVentButton.setActionCommand("componentHeatVent"); // NOI18N
        componentsPanel.add(componentHeatVentButton);

        componentsGroup.add(overclockedHeatVentButton);
        overclockedHeatVentButton.setToolTipText(buildTooltipInfo("OverclockedHeatVent"));
        overclockedHeatVentButton.setActionCommand("overclockedHeatVent"); // NOI18N
        componentsPanel.add(overclockedHeatVentButton);

        componentsGroup.add(coolantCell10kButton);
        coolantCell10kButton.setToolTipText(buildTooltipInfo("CoolantCell10k"));
        coolantCell10kButton.setActionCommand("coolantCell10k"); // NOI18N
        componentsPanel.add(coolantCell10kButton);

        componentsGroup.add(coolantCell30kButton);
        coolantCell30kButton.setToolTipText(buildTooltipInfo("CoolantCell30k"));
        coolantCell30kButton.setActionCommand("coolantCell30k"); // NOI18N
        componentsPanel.add(coolantCell30kButton);

        componentsGroup.add(coolantCell60kButton);
        coolantCell60kButton.setToolTipText(buildTooltipInfo("CoolantCell60k"));
        coolantCell60kButton.setActionCommand("coolantCell60k"); // NOI18N
        componentsPanel.add(coolantCell60kButton);

        componentsGroup.add(heatExchangerButton);
        heatExchangerButton.setToolTipText(buildTooltipInfo("HeatExchanger"));
        heatExchangerButton.setActionCommand("heatExchanger"); // NOI18N
        componentsPanel.add(heatExchangerButton);

        componentsGroup.add(advancedHeatExchangerButton);
        advancedHeatExchangerButton.setToolTipText(buildTooltipInfo("AdvancedHeatExchanger"));
        advancedHeatExchangerButton.setActionCommand("advancedHeatExchanger"); // NOI18N
        componentsPanel.add(advancedHeatExchangerButton);

        componentsGroup.add(coreHeatExchangerButton);
        coreHeatExchangerButton.setToolTipText(buildTooltipInfo("ReactorHeatExchanger"));
        coreHeatExchangerButton.setActionCommand("coreHeatExchanger"); // NOI18N
        componentsPanel.add(coreHeatExchangerButton);

        componentsGroup.add(componentHeatExchangerButton);
        componentHeatExchangerButton.setToolTipText(buildTooltipInfo("ComponentHeatExchanger"));
        componentHeatExchangerButton.setActionCommand("componentHeatExchanger"); // NOI18N
        componentsPanel.add(componentHeatExchangerButton);

        componentsGroup.add(reactorPlatingButton);
        reactorPlatingButton.setToolTipText(buildTooltipInfo("ReactorPlating"));
        reactorPlatingButton.setActionCommand("reactorPlating"); // NOI18N
        componentsPanel.add(reactorPlatingButton);

        componentsGroup.add(heatCapacityReactorPlatingButton);
        heatCapacityReactorPlatingButton.setToolTipText(buildTooltipInfo("HeatCapacityReactorPlating"));
        heatCapacityReactorPlatingButton.setActionCommand("heatCapacityReactorPlating"); // NOI18N
        componentsPanel.add(heatCapacityReactorPlatingButton);

        componentsGroup.add(containmentReactorPlatingButton);
        containmentReactorPlatingButton.setToolTipText(buildTooltipInfo("ContainmentReactorPlating"));
        containmentReactorPlatingButton.setActionCommand("containmentReactorPlating"); // NOI18N
        componentsPanel.add(containmentReactorPlatingButton);

        componentsGroup.add(rshCondensatorButton);
        rshCondensatorButton.setToolTipText(buildTooltipInfo("RshCondensator"));
        rshCondensatorButton.setActionCommand("rshCondensator"); // NOI18N
        componentsPanel.add(rshCondensatorButton);

        componentsGroup.add(lzhCondensatorButton);
        lzhCondensatorButton.setToolTipText(buildTooltipInfo("LzhCondensator"));
        lzhCondensatorButton.setActionCommand("lzhCondensator"); // NOI18N
        componentsPanel.add(lzhCondensatorButton);

        componentsGroup.add(fuelRodThoriumButton);
        fuelRodThoriumButton.setToolTipText(buildTooltipInfo("FuelRodThorium"));
        fuelRodThoriumButton.setActionCommand("fuelRodThorium"); // NOI18N
        componentsPanel.add(fuelRodThoriumButton);

        componentsGroup.add(dualFuelRodThoriumButton);
        dualFuelRodThoriumButton.setToolTipText(buildTooltipInfo("DualFuelRodThorium"));
        dualFuelRodThoriumButton.setActionCommand("dualFuelRodThorium"); // NOI18N
        componentsPanel.add(dualFuelRodThoriumButton);

        componentsGroup.add(quadFuelRodThoriumButton);
        quadFuelRodThoriumButton.setToolTipText(buildTooltipInfo("QuadFuelRodThorium"));
        quadFuelRodThoriumButton.setActionCommand("quadFuelRodThorium"); // NOI18N
        componentsPanel.add(quadFuelRodThoriumButton);

        componentsGroup.add(coolantCellHelium60kButton);
        coolantCellHelium60kButton.setToolTipText(buildTooltipInfo("CoolantCell60kHelium"));
        coolantCellHelium60kButton.setActionCommand("coolantCellHelium60k"); // NOI18N
        componentsPanel.add(coolantCellHelium60kButton);

        componentsGroup.add(coolantCellHelium180kButton);
        coolantCellHelium180kButton.setToolTipText(buildTooltipInfo("CoolantCell180kHelium"));
        coolantCellHelium180kButton.setActionCommand("coolantCellHelium180k"); // NOI18N
        componentsPanel.add(coolantCellHelium180kButton);

        componentsGroup.add(coolantCellHelium360kButton);
        coolantCellHelium360kButton.setToolTipText(buildTooltipInfo("CoolantCell360kHelium"));
        coolantCellHelium360kButton.setActionCommand("coolantCellHelium360k"); // NOI18N
        componentsPanel.add(coolantCellHelium360kButton);

        componentsGroup.add(coolantCellNak60kButton);
        coolantCellNak60kButton.setToolTipText(buildTooltipInfo("CoolantCell60kNak"));
        coolantCellNak60kButton.setActionCommand("coolantCellNak60k"); // NOI18N
        componentsPanel.add(coolantCellNak60kButton);

        componentsGroup.add(coolantCellNak180kButton);
        coolantCellNak180kButton.setToolTipText(buildTooltipInfo("CoolantCell180kNak"));
        coolantCellNak180kButton.setActionCommand("coolantCellNak180k"); // NOI18N
        componentsPanel.add(coolantCellNak180kButton);

        componentsGroup.add(coolantCellNak360kButton);
        coolantCellNak360kButton.setToolTipText(buildTooltipInfo("CoolantCell360kNak"));
        coolantCellNak360kButton.setActionCommand("coolantCellNak360k"); // NOI18N
        componentsPanel.add(coolantCellNak360kButton);

        componentsGroup.add(iridiumNeutronReflectorButton);
        iridiumNeutronReflectorButton.setToolTipText(buildTooltipInfo("IridiumNeutronReflector"));
        iridiumNeutronReflectorButton.setActionCommand("iridiumNeutronReflector"); // NOI18N
        componentsPanel.add(iridiumNeutronReflectorButton);

        componentsGroup.add(fuelRodNaquadahButton);
        fuelRodNaquadahButton.setToolTipText(buildTooltipInfo("FuelRodNaquadah"));
        fuelRodNaquadahButton.setActionCommand("fuelRodNaquadah"); // NOI18N
        componentsPanel.add(fuelRodNaquadahButton);

        componentsGroup.add(dualFuelRodNaquadahButton);
        dualFuelRodNaquadahButton.setToolTipText(buildTooltipInfo("DualFuelRodNaquadah"));
        dualFuelRodNaquadahButton.setActionCommand("dualFuelRodNaquadah"); // NOI18N
        componentsPanel.add(dualFuelRodNaquadahButton);

        componentsGroup.add(quadFuelRodNaquadahButton);
        quadFuelRodNaquadahButton.setToolTipText(buildTooltipInfo("QuadFuelRodNaquadah"));
        quadFuelRodNaquadahButton.setActionCommand("quadFuelRodNaquadah"); // NOI18N
        componentsPanel.add(quadFuelRodNaquadahButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(componentsPanel, gridBagConstraints);

        jPanel8.setLayout(new java.awt.GridBagLayout());

        placingLabel.setText(bundle.getString("UI.ComponentPlacingDefault")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel8.add(placingLabel, gridBagConstraints);

        versionLabel.setText(formatI18n("UI.VersionNumber", VERSION));
        versionLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel8.add(versionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        temperatureAndComponentsPanel.add(jPanel8, gridBagConstraints);

        componentHeatLabel.setText(bundle.getString("Config.InitialComponentHeat")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(componentHeatLabel, gridBagConstraints);

        componentHeatSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 360000, 1));
        componentHeatSpinner.setMinimumSize(new java.awt.Dimension(70, 20));
        componentHeatSpinner.setPreferredSize(new java.awt.Dimension(70, 20));
        componentHeatSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                componentHeatSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(componentHeatSpinner, gridBagConstraints);

        placingThresholdLabel.setText(bundle.getString("Config.PlacingReplacementThreshold")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(placingThresholdLabel, gridBagConstraints);

        placingThresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(9000, 0, 360000, 1));
        placingThresholdSpinner.setMinimumSize(new java.awt.Dimension(100, 20));
        placingThresholdSpinner.setPreferredSize(new java.awt.Dimension(100, 20));
        placingThresholdSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                placingThresholdSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(placingThresholdSpinner, gridBagConstraints);

        placingReactorPauseLabel.setText(bundle.getString("Config.PlacingReactorPause")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(placingReactorPauseLabel, gridBagConstraints);

        placingReactorPauseSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10000, 1));
        placingReactorPauseSpinner.setMinimumSize(new java.awt.Dimension(100, 20));
        placingReactorPauseSpinner.setPreferredSize(new java.awt.Dimension(100, 20));
        placingReactorPauseSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                placingReactorPauseSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(placingReactorPauseSpinner, gridBagConstraints);

        jSplitPane3.setTopComponent(temperatureAndComponentsPanel);

        jPanel1.setMinimumSize(new java.awt.Dimension(392, 170));
        jPanel1.setPreferredSize(new java.awt.Dimension(392, 160));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel3.setOpaque(false);

        reactorStyleGroup.add(euReactorRadio);
        euReactorRadio.setSelected(true);
        euReactorRadio.setText(bundle.getString("Config.EUReactor")); // NOI18N
        euReactorRadio.setOpaque(false);
        euReactorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                euReactorRadioActionPerformed(evt);
            }
        });
        jPanel3.add(euReactorRadio);

        reactorStyleGroup.add(fluidReactorRadio);
        fluidReactorRadio.setText(bundle.getString("Config.FluidReactor")); // NOI18N
        fluidReactorRadio.setOpaque(false);
        fluidReactorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fluidReactorRadioActionPerformed(evt);
            }
        });
        jPanel3.add(fluidReactorRadio);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(-6, -6, -6, -6);
        jPanel1.add(jPanel3, gridBagConstraints);

        jPanel4.setOpaque(false);

        clearGridButton.setText(bundle.getString("UI.ClearGridButton")); // NOI18N
        clearGridButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearGridButtonActionPerformed(evt);
            }
        });
        jPanel4.add(clearGridButton);

        simulateButton.setText(bundle.getString("UI.SimulateButton")); // NOI18N
        simulateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulateButtonActionPerformed(evt);
            }
        });
        jPanel4.add(simulateButton);

        cancelButton.setText(bundle.getString("UI.CancelButton")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        jPanel4.add(cancelButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(-2, -2, -2, -2);
        jPanel1.add(jPanel4, gridBagConstraints);

        jPanel5.setOpaque(false);

        jLabel1.setText(bundle.getString("UI.InitialReactorHeat")); // NOI18N
        jPanel5.add(jLabel1);

        heatSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 9999.0d, 1.0d));
        heatSpinner.setMinimumSize(new java.awt.Dimension(70, 20));
        heatSpinner.setPreferredSize(new java.awt.Dimension(70, 20));
        heatSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heatSpinnerStateChanged(evt);
            }
        });
        jPanel5.add(heatSpinner);

        maxHeatLabel.setText(bundle.getString("UI.MaxHeatDefault")); // NOI18N
        jPanel5.add(maxHeatLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(-2, -2, -2, -2);
        jPanel1.add(jPanel5, gridBagConstraints);

        jPanel6.setOpaque(false);

        pulsedReactorCheck.setText(bundle.getString("UI.PulsedReactor")); // NOI18N
        pulsedReactorCheck.setOpaque(false);
        pulsedReactorCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pulsedReactorCheckActionPerformed(evt);
            }
        });
        jPanel6.add(pulsedReactorCheck);

        automatedReactorCheck.setText(bundle.getString("UI.AutomatedReactor")); // NOI18N
        automatedReactorCheck.setOpaque(false);
        automatedReactorCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                automatedReactorCheckActionPerformed(evt);
            }
        });
        jPanel6.add(automatedReactorCheck);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(-4, -4, -4, -4);
        jPanel1.add(jPanel6, gridBagConstraints);

        jPanel7.setOpaque(false);

        maxSimulationTicksLabel.setText(bundle.getString("UI.MaxSimulationTicks")); // NOI18N
        jPanel7.add(maxSimulationTicksLabel);

        maxSimulationTicksSpinner.setModel(new javax.swing.SpinnerNumberModel(5000000, null, 5000000, 1));
        maxSimulationTicksSpinner.setToolTipText(bundle.getString("UI.MaxSimulationTicksTooltip")); // NOI18N
        maxSimulationTicksSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxSimulationTicksSpinnerStateChanged(evt);
            }
        });
        jPanel7.add(maxSimulationTicksSpinner);

        jLabel6.setText(bundle.getString("Config.Seconds")); // NOI18N
        jPanel7.add(jLabel6);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(-2, -2, -2, -2);
        jPanel1.add(jPanel7, gridBagConstraints);

        reactorCoolantInjectorCheckbox.setText(bundle.getString("Config.ReactorCoolantInjectors")); // NOI18N
        reactorCoolantInjectorCheckbox.setOpaque(false);
        reactorCoolantInjectorCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reactorCoolantInjectorCheckboxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(reactorCoolantInjectorCheckbox, gridBagConstraints);

        jSplitPane3.setBottomComponent(jPanel1);

        jSplitPane2.setRightComponent(jSplitPane3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jSplitPane2, gridBagConstraints);

        jLabel2.setText(bundle.getString("UI.CodeLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel2.add(jLabel2, gridBagConstraints);

        codeField.setEditable(false);
        codeField.setMaximumSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(codeField, gridBagConstraints);

        copyCodeButton.setText(bundle.getString("UI.CopyCodeButton")); // NOI18N
        copyCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyCodeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(copyCodeButton, gridBagConstraints);

        pasteCodeButton.setText(bundle.getString("UI.PasteCodeButton")); // NOI18N
        pasteCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteCodeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel2.add(pasteCodeButton, gridBagConstraints);

        jSplitPane1.setTopComponent(jPanel2);

        outputTabs.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        outputArea.setEditable(false);
        outputArea.setColumns(20);
        outputArea.setRows(5);
        outputPane.setViewportView(outputArea);

        outputTabs.addTab(bundle.getString("UI.SimulationTab"), outputPane); // NOI18N

        componentArea.setColumns(20);
        componentArea.setRows(5);
        componentArea.setText(bundle.getString("UI.ComponentInfoDefault")); // NOI18N
        componentPane.setViewportView(componentArea);

        outputTabs.addTab(bundle.getString("UI.ComponentTab"), componentPane); // NOI18N

        automationPanel.setLayout(new java.awt.GridBagLayout());

        selectedComponentLabel.setText(bundle.getString("Config.NoComponentSelected")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(selectedComponentLabel, gridBagConstraints);

        jLabel12.setText(bundle.getString("Config.ReplacementThreshold")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(jLabel12, gridBagConstraints);

        thresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(9000, 0, 360000, 1));
        thresholdSpinner.setMinimumSize(new java.awt.Dimension(100, 20));
        thresholdSpinner.setPreferredSize(new java.awt.Dimension(100, 20));
        thresholdSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                thresholdSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(thresholdSpinner, gridBagConstraints);

        jLabel13.setText(bundle.getString("Config.ReplacementThresholdHelp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(jLabel13, gridBagConstraints);

        jLabel14.setText(bundle.getString("Config.ReactorPause")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(jLabel14, gridBagConstraints);

        pauseSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 10000, 1));
        pauseSpinner.setMinimumSize(new java.awt.Dimension(100, 20));
        pauseSpinner.setPreferredSize(new java.awt.Dimension(100, 20));
        pauseSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pauseSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(pauseSpinner, gridBagConstraints);

        jLabel15.setText(bundle.getString("Config.ReactorPauseHelp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(jLabel15, gridBagConstraints);

        outputTabs.addTab(bundle.getString("UI.ComponentAutomationTab"), automationPanel); // NOI18N

        componentListArea.setColumns(20);
        componentListArea.setRows(5);
        componentListPane.setViewportView(componentListArea);

        outputTabs.addTab(bundle.getString("UI.ComponentListTab"), componentListPane); // NOI18N

        materialsArea.setEditable(false);
        materialsArea.setColumns(20);
        materialsArea.setRows(5);
        materialsListPane.setViewportView(materialsArea);

        outputTabs.addTab(bundle.getString("UI.MaterialsTab"), materialsListPane); // NOI18N

        csvPanel.setLayout(new java.awt.GridBagLayout());

        csvOutputCheck.setText(bundle.getString("Config.CSVCheckbox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        csvPanel.add(csvOutputCheck, gridBagConstraints);

        jLabel19.setText(bundle.getString("Config.CSVLimit")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        csvPanel.add(jLabel19, gridBagConstraints);

        csvLimitSpinner.setModel(new javax.swing.SpinnerNumberModel(5000, 0, 5000000, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        csvPanel.add(csvLimitSpinner, gridBagConstraints);

        csvFileLabel.setText(bundle.getString("UI.CSVFileDefault")); // NOI18N
        csvFileLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        csvPanel.add(csvFileLabel, gridBagConstraints);

        csvBrowseButton.setText(bundle.getString("UI.CSVBrowseButton")); // NOI18N
        csvBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        csvPanel.add(csvBrowseButton, gridBagConstraints);

        csvHelpLabel.setText(bundle.getString("UI.CSVHelp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        csvPanel.add(csvHelpLabel, gridBagConstraints);

        outputTabs.addTab(bundle.getString("UI.CSVTab"), csvPanel); // NOI18N

        advancedPanel.setLayout(new java.awt.GridBagLayout());

        showComponentDetailButtonsCheck.setSelected(true);
        showComponentDetailButtonsCheck.setText(bundle.getString("UI.ShowComponentDetailButtons")); // NOI18N
        showComponentDetailButtonsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showComponentDetailButtonsCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        advancedPanel.add(showComponentDetailButtonsCheck, gridBagConstraints);

        enableGT508ComponentsCheck.setSelected(true);
        enableGT508ComponentsCheck.setText(bundle.getString("UI.EnableGT508Components")); // NOI18N
        enableGT508ComponentsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableGT508ComponentsCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        advancedPanel.add(enableGT508ComponentsCheck, gridBagConstraints);

        enableGT509ComponentsCheck.setSelected(true);
        enableGT509ComponentsCheck.setText(bundle.getString("UI.EnableGT509Components")); // NOI18N
        enableGT509ComponentsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableGT509ComponentsCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        advancedPanel.add(enableGT509ComponentsCheck, gridBagConstraints);

        showOldStyleReactorCodeCheck.setText(bundle.getString("UI.ShowOldStyleReactorCode")); // NOI18N
        showOldStyleReactorCodeCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showOldStyleReactorCodeCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        advancedPanel.add(showOldStyleReactorCodeCheck, gridBagConstraints);

        showComponentPreconfigCheck.setSelected(true);
        showComponentPreconfigCheck.setText(bundle.getString("UI.ShowComponentPreconfigControls")); // NOI18N
        showComponentPreconfigCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showComponentPreconfigCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        advancedPanel.add(showComponentPreconfigCheck, gridBagConstraints);

        texturePackLabel.setText(bundle.getString("UI.TexturePackDefault")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 2, 2);
        advancedPanel.add(texturePackLabel, gridBagConstraints);

        texturePackBrowseButton.setText(bundle.getString("UI.TexturePackBrowseButton")); // NOI18N
        texturePackBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texturePackBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        advancedPanel.add(texturePackBrowseButton, gridBagConstraints);

        texturePackClearButton.setText(bundle.getString("UI.TexturePackClearButton")); // NOI18N
        texturePackClearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                texturePackClearButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        advancedPanel.add(texturePackClearButton, gridBagConstraints);

        jLabel5.setText(bundle.getString("UI.TexturePackHelp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 2, 2);
        advancedPanel.add(jLabel5, gridBagConstraints);

        gt509BehaviorCheck.setText(bundle.getString("UI.GT509ReactorBehavior")); // NOI18N
        gt509BehaviorCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gt509BehaviorCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        advancedPanel.add(gt509BehaviorCheck, gridBagConstraints);

        useGTRecipesCheck.setText(bundle.getString("UI.UseGTRecipes")); // NOI18N
        useGTRecipesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useGTRecipesCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        advancedPanel.add(useGTRecipesCheck, gridBagConstraints);

        useUfcForCoolantCellsCheck.setText(bundle.getString("UI.UseUfcForCoolantCells")); // NOI18N
        useUfcForCoolantCellsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useUfcForCoolantCellsCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        advancedPanel.add(useUfcForCoolantCellsCheck, gridBagConstraints);

        expandAdvancedAlloyCheck.setText(bundle.getString("UI.ExpandAdvancedAlloy")); // NOI18N
        expandAdvancedAlloyCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandAdvancedAlloyCheckActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        advancedPanel.add(expandAdvancedAlloyCheck, gridBagConstraints);

        advancedScroll.setViewportView(advancedPanel);

        outputTabs.addTab(bundle.getString("UI.AdvancedTab"), advancedScroll); // NOI18N

        comparisonPanel.setLayout(new java.awt.GridBagLayout());

        jLabel17.setText(bundle.getString("UI.CodeLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        comparisonPanel.add(jLabel17, gridBagConstraints);

        comparisonCodeField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        comparisonPanel.add(comparisonCodeField, gridBagConstraints);

        comparisonCopyCodeButton.setText(bundle.getString("UI.CopyCodeButton")); // NOI18N
        comparisonCopyCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comparisonCopyCodeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        comparisonPanel.add(comparisonCopyCodeButton, gridBagConstraints);

        lockPrevCodeCheck.setText(bundle.getString("UI.LockInTabCode")); // NOI18N
        jPanel9.add(lockPrevCodeCheck);

        onlyShowDiffCheck.setText(bundle.getString("UI.OnlyShowDiffData")); // NOI18N
        onlyShowDiffCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlyShowDiffCheckActionPerformed(evt);
            }
        });
        jPanel9.add(onlyShowDiffCheck);

        copyComparisonButton.setText(bundle.getString("UI.CopyComparisonData")); // NOI18N
        copyComparisonButton.setEnabled(false);
        copyComparisonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyComparisonButtonActionPerformed(evt);
            }
        });
        jPanel9.add(copyComparisonButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        comparisonPanel.add(jPanel9, gridBagConstraints);

        jLabel18.setText(bundle.getString("Comparison.Header")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        comparisonPanel.add(jLabel18, gridBagConstraints);

        comparisonLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        comparisonLabel.setText(bundle.getString("Comparison.Default")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        comparisonPanel.add(comparisonLabel, gridBagConstraints);

        comparisonScroll.setViewportView(comparisonPanel);

        outputTabs.addTab(bundle.getString("UI.ComparisonTab"), comparisonScroll); // NOI18N

        jSplitPane1.setRightComponent(outputTabs);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(jSplitPane1, gridBagConstraints);

        setSize(new java.awt.Dimension(1109, 777));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void plannerResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_plannerResized
        // Force minimum dimensions to be honored, since Swing apparently doesn't handle that automatically.
        Dimension dim = this.getSize();
        Dimension minDim = this.getMinimumSize();
        if (dim.width < minDim.width) {
            dim.width = minDim.width;
        }
        if (dim.height < minDim.height) {
            dim.height = minDim.height;
        }
        setSize(dim);
        Enumeration<AbstractButton> elements = componentsGroup.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = elements.nextElement();
            int buttonSize = Math.min(button.getWidth(), button.getHeight());
            if (buttonSize > 2) {
                final ReactorItem component = ComponentFactory.getDefaultComponent(button.getActionCommand());
                if (component != null && component.image != null) {
                    button.setIcon(new ImageIcon(component.image.getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
                } else {
                    button.setIcon(null);
                }
            }
        }
        for (int row = 0; row < reactorButtons.length; row++) {
            for (int col = 0; col < reactorButtons[row].length; col++) {
                int buttonSize = Math.min(reactorButtons[row][col].getWidth(), reactorButtons[row][col].getHeight());
                if (buttonSize > 2) {
                    final ReactorItem component = reactor.getComponentAt(row, col);
                    if (component != null && component.image != null) {
                        reactorButtons[row][col].setIcon(new ImageIcon(component.image.getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
                    } else {
                        reactorButtons[row][col].setIcon(null);
                    }
                }
            }
        }
    }//GEN-LAST:event_plannerResized

    private void clearGridButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearGridButtonActionPerformed
        reactor.clearGrid();
        for (int i = 0; i < reactorButtons.length; i++) {
            for (int j = 0; j < reactorButtons[i].length; j++) {
                reactorButtons[i][j].setIcon(null);
                reactorButtons[i][j].setToolTipText(null);
                reactorButtonPanels[i][j].setBackground(Color.LIGHT_GRAY);
            }
        }
        outputArea.setText(null);
        materialsArea.setText(reactor.getMaterials().toString());
        componentListArea.setText(reactor.getComponentList().toString());
        maxHeatLabel.setText(formatI18n("UI.MaxHeatSpecific", reactor.getMaxHeat()));
        heatSpinnerModel.setMaximum(reactor.getMaxHeat() - 1);
        heatSpinnerModel.setValue(Math.min(((Number) heatSpinnerModel.getValue()).intValue(), reactor.getMaxHeat() - 1));
        temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
        lockCode = true;
        codeField.setText(null);
        lockCode = false;
    }//GEN-LAST:event_clearGridButtonActionPerformed

    private void simulateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulateButtonActionPerformed
        if (simulator != null) {
            if (simulator.getData() != null && !lockPrevCodeCheck.isSelected()) {
                prevSimulator = simulator;
                prevReactorCode = currentReactorCode;
                prevReactorOldCode = currentReactorOldCode;
            }
            simulator.cancel(true);
        }
        currentReactorCode = reactor.getCode();
        currentReactorOldCode = reactor.getOldCode();
        File csvFile = null;
        int csvLimit = -1;
        if (csvOutputCheck.isSelected()) {
            csvFile = csvChooser.getSelectedFile();
            Object value = csvLimitSpinner.getModel().getValue();
            if (value instanceof Number) {
                csvLimit = ((Number)value).intValue();
            }
        }
        outputArea = new javax.swing.JTextArea(5, 20);
        outputArea.setEditable(false);
        outputPane.setViewportView(outputArea);
        simulatedReactor = new Reactor();
        simulatedReactor.setCode(reactor.getCode());
        outputTabs.setSelectedComponent(outputPane);
        simulator = new AutomationSimulator(simulatedReactor, outputArea, reactorButtonPanels, csvFile, csvLimit);
        if (prevSimulator != null) {
            simulator.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("completed".equals(evt.getPropertyName()) && Boolean.TRUE.equals(evt.getNewValue())) {
                        updateComparison();
                    }
                }
            });
        }
        simulator.execute();        
    }//GEN-LAST:event_simulateButtonActionPerformed
    
    private void euReactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_euReactorRadioActionPerformed
        reactor.setFluid(false);
        updateCodeField();
    }//GEN-LAST:event_euReactorRadioActionPerformed

    private void updateCodeField() {
        lockCode = true;
        if (showOldStyleReactorCodeCheck.isSelected()) {
            codeField.setText(reactor.getOldCode());
        } else {
            codeField.setText(reactor.getCode());
        }
        lockCode = false;
    }

    private void fluidReactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fluidReactorRadioActionPerformed
        reactor.setFluid(true);
        updateCodeField();
    }//GEN-LAST:event_fluidReactorRadioActionPerformed

    private void copyCodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyCodeButtonActionPerformed
        StringSelection selection = new StringSelection(codeField.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }//GEN-LAST:event_copyCodeButtonActionPerformed

    private void pasteCodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pasteCodeButtonActionPerformed
        try {
            String code = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
            codeField.setText(code.replaceAll("[^0-9A-Za-z(),.?|:/+=]+", ""));
        } catch (UnsupportedFlavorException | IOException ex) {
            ExceptionDialogDisplay.showExceptionDialog(ex);
        }
    }//GEN-LAST:event_pasteCodeButtonActionPerformed

    private void thresholdSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_thresholdSpinnerStateChanged
        if (selectedColumn >= 0 && selectedRow >= 0 && reactor.getComponentAt(selectedRow, selectedColumn) != null) {
            ReactorItem component = reactor.getComponentAt(selectedRow, selectedColumn);
            component.setAutomationThreshold(((Number)thresholdSpinner.getValue()).intValue());
            if (!lockCode) {
                updateCodeField();
            }
        }
    }//GEN-LAST:event_thresholdSpinnerStateChanged

    private void pauseSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pauseSpinnerStateChanged
        if (selectedColumn >= 0 && selectedRow >= 0 && reactor.getComponentAt(selectedRow, selectedColumn) != null) {
            ReactorItem component = reactor.getComponentAt(selectedRow, selectedColumn);
            component.setReactorPause(((Number)pauseSpinner.getValue()).intValue());
            if (!lockCode) {
                updateCodeField();
            }
        }
    }//GEN-LAST:event_pauseSpinnerStateChanged

    private void reactorCoolantInjectorCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reactorCoolantInjectorCheckboxActionPerformed
        reactor.setUsingReactorCoolantInjectors(reactorCoolantInjectorCheckbox.isSelected());
        updateCodeField();
    }//GEN-LAST:event_reactorCoolantInjectorCheckboxActionPerformed

    private void heatSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heatSpinnerStateChanged
        reactor.setCurrentHeat(((Number)heatSpinner.getValue()).doubleValue());
        if (!lockCode) {
            updateCodeField();
        }
    }//GEN-LAST:event_heatSpinnerStateChanged

    private void onPulseSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_onPulseSpinnerStateChanged
        reactor.setOnPulse(((Number)onPulseSpinner.getValue()).intValue());
        if (!lockCode) {
            updateCodeField();
        }
    }//GEN-LAST:event_onPulseSpinnerStateChanged

    private void offPulseSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_offPulseSpinnerStateChanged
        reactor.setOffPulse(((Number)offPulseSpinner.getValue()).intValue());
        if (!lockCode) {
            updateCodeField();
        }
    }//GEN-LAST:event_offPulseSpinnerStateChanged

    private void suspendTempSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_suspendTempSpinnerStateChanged
        reactor.setSuspendTemp(((Number)suspendTempSpinner.getValue()).intValue());
        if (!lockCode) {
            updateCodeField();
        }
    }//GEN-LAST:event_suspendTempSpinnerStateChanged

    private void resumeTempSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_resumeTempSpinnerStateChanged
        reactor.setResumeTemp(((Number)resumeTempSpinner.getValue()).intValue());
        if (!lockCode) {
            updateCodeField();
        }
    }//GEN-LAST:event_resumeTempSpinnerStateChanged

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (simulator != null && !simulator.isDone() && !simulator.isCancelled()) {
            simulator.cancel(false);
        }
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void csvBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvBrowseButtonActionPerformed
        csvChooser.showSaveDialog(this);
        if (csvChooser.getSelectedFile() != null) {
            csvFileLabel.setText(csvChooser.getSelectedFile().getAbsolutePath());
        }
        saveAdvancedConfig();
    }//GEN-LAST:event_csvBrowseButtonActionPerformed

    private void pulsedReactorCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pulsedReactorCheckActionPerformed
        reactor.setPulsed(pulsedReactorCheck.isSelected());
        togglePulseConfigTab();
        updateCodeField();
    }//GEN-LAST:event_pulsedReactorCheckActionPerformed

    private void togglePulseConfigTab() {
        if (pulsedReactorCheck.isSelected()) {
            if (outputTabs.indexOfComponent(pulsePanel) < 0) {
                outputTabs.insertTab(getI18n("UI.PulseConfigurationTab"), null, pulsePanel, null, outputTabs.indexOfComponent(outputPane) + 1);
            }
        } else {
            outputTabs.remove(pulsePanel);
        }
    }

    private void automatedReactorCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automatedReactorCheckActionPerformed
        reactor.setAutomated(automatedReactorCheck.isSelected());
        updateCodeField();
    }//GEN-LAST:event_automatedReactorCheckActionPerformed

    private void maxSimulationTicksSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxSimulationTicksSpinnerStateChanged
        reactor.setMaxSimulationTicks(((Number)maxSimulationTicksSpinner.getValue()).intValue());
        if (!lockCode) {
            updateCodeField();
        }
    }//GEN-LAST:event_maxSimulationTicksSpinnerStateChanged

    private void componentHeatSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_componentHeatSpinnerStateChanged
        if (paletteComponentId > 0 && paletteComponents[paletteComponentId] != null) {
            paletteComponents[paletteComponentId].setInitialHeat(((Number)componentHeatSpinner.getValue()).intValue());
        }
    }//GEN-LAST:event_componentHeatSpinnerStateChanged

    private void placingThresholdSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_placingThresholdSpinnerStateChanged
        if (paletteComponentId > 0 && paletteComponents[paletteComponentId] != null) {
            paletteComponents[paletteComponentId].setAutomationThreshold(((Number)placingThresholdSpinner.getValue()).intValue());
        }
    }//GEN-LAST:event_placingThresholdSpinnerStateChanged

    private void placingReactorPauseSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_placingReactorPauseSpinnerStateChanged
        if (paletteComponentId > 0 && paletteComponents[paletteComponentId] != null) {
            paletteComponents[paletteComponentId].setReactorPause(((Number)placingReactorPauseSpinner.getValue()).intValue());
        }
    }//GEN-LAST:event_placingReactorPauseSpinnerStateChanged

    private void showComponentDetailButtonsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showComponentDetailButtonsCheckActionPerformed
        for (JButton componentDetailButton : componentDetailButtons) {
            componentDetailButton.setVisible(showComponentDetailButtonsCheck.isSelected());
        }
        saveAdvancedConfig();
    }//GEN-LAST:event_showComponentDetailButtonsCheckActionPerformed

    private void enableGT508ComponentsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableGT508ComponentsCheckActionPerformed
        Enumeration<AbstractButton> elements = componentsGroup.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton element = elements.nextElement();
            ReactorItem component = ComponentFactory.getDefaultComponent(element.getActionCommand());
            if (component != null && "GT5.08".equals(component.sourceMod)) {
                element.setEnabled(enableGT508ComponentsCheck.isSelected());
            }
        }
        if (!enableGT508ComponentsCheck.isSelected()) {
            boolean reactorHasGT508Components = false;
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorItem component = reactor.getComponentAt(row, col);
                    if (component != null && "GT5.08".equals(component.sourceMod)) {
                        reactorHasGT508Components = true;
                    }
                }
            }
            if (reactorHasGT508Components) {
                int result = JOptionPane.showConfirmDialog(this, getI18n("UI.RemoveGT508ComponentsText"), getI18n("UI.RemoveAddonComponentsTitle"), JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    for (int row = 0; row < 6; row++) {
                        for (int col = 0; col < 9; col++) {
                            ReactorItem component = reactor.getComponentAt(row, col);
                            if (component != null && "GT5.08".equals(component.sourceMod)) {
                                reactor.setComponentAt(row, col, null);
                            }
                        }
                    }
                    updateReactorButtons();
                    updateCodeField();
                }
            }
        }
        saveAdvancedConfig();
    }//GEN-LAST:event_enableGT508ComponentsCheckActionPerformed

    private void enableGT509ComponentsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableGT509ComponentsCheckActionPerformed
        Enumeration<AbstractButton> elements = componentsGroup.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton element = elements.nextElement();
            ReactorItem component = ComponentFactory.getDefaultComponent(element.getActionCommand());
            if (component != null && "GT5.09".equals(component.sourceMod)) {
                element.setEnabled(enableGT509ComponentsCheck.isSelected());
            }
        }
        if (!enableGT509ComponentsCheck.isSelected()) {
            boolean reactorHasGT509Components = false;
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 9; col++) {
                    ReactorItem component = reactor.getComponentAt(row, col);
                    if (component != null && "GT5.09".equals(component.sourceMod)) {
                        reactorHasGT509Components = true;
                    }
                }
            }
            if (reactorHasGT509Components) {
                int result = JOptionPane.showConfirmDialog(this, getI18n("UI.RemoveGT509ComponentsText"), getI18n("UI.RemoveAddonComponentsTitle"), JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    for (int row = 0; row < 6; row++) {
                        for (int col = 0; col < 9; col++) {
                            ReactorItem component = reactor.getComponentAt(row, col);
                            if (component != null && "GT5.09".equals(component.sourceMod)) {
                                reactor.setComponentAt(row, col, null);
                            }
                        }
                    }
                    updateReactorButtons();
                    updateCodeField();
                }
            }
        }
        saveAdvancedConfig();
    }//GEN-LAST:event_enableGT509ComponentsCheckActionPerformed

    private void showOldStyleReactorCodeCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showOldStyleReactorCodeCheckActionPerformed
        updateCodeField();
        saveAdvancedConfig();
    }//GEN-LAST:event_showOldStyleReactorCodeCheckActionPerformed

    private void reactorPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_reactorPanelComponentResized
        // planner resizing event handler already takes care of resizing images, so just call that.
        plannerResized(null);
    }//GEN-LAST:event_reactorPanelComponentResized

    private void componentsPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_componentsPanelComponentResized
        // planner resizing event handler already takes care of resizing images, so just call that.
        plannerResized(null);
    }//GEN-LAST:event_componentsPanelComponentResized

    private void showComponentPreconfigCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showComponentPreconfigCheckActionPerformed
        boolean selected = showComponentPreconfigCheck.isSelected();
        componentHeatLabel.setVisible(selected);
        componentHeatSpinner.setVisible(selected);
        placingThresholdLabel.setVisible(selected);
        placingThresholdSpinner.setVisible(selected);
        placingReactorPauseLabel.setVisible(selected);
        placingReactorPauseSpinner.setVisible(selected);
        saveAdvancedConfig();
    }//GEN-LAST:event_showComponentPreconfigCheckActionPerformed

    private void resetPulseConfigButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetPulseConfigButtonActionPerformed
        reactor.resetPulseConfig();
        updateCodeField();
    }//GEN-LAST:event_resetPulseConfigButtonActionPerformed

    private void texturePackBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texturePackBrowseButtonActionPerformed
        textureChooser.showOpenDialog(this);
        if (textureChooser.getSelectedFile() != null) {
            ADVANCED_CONFIG.setProperty("texturePack", textureChooser.getSelectedFile().getAbsolutePath());
            texturePackLabel.setText(formatI18n("UI.TexturePackSpecific", textureChooser.getSelectedFile().getAbsolutePath()));
            saveAdvancedConfig();
        }
    }//GEN-LAST:event_texturePackBrowseButtonActionPerformed

    private void texturePackClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texturePackClearButtonActionPerformed
        ADVANCED_CONFIG.remove("texturePack");
        texturePackLabel.setText(getI18n("UI.TexturePackDefault"));
        saveAdvancedConfig();
    }//GEN-LAST:event_texturePackClearButtonActionPerformed

    private void comparisonCopyCodeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comparisonCopyCodeButtonActionPerformed
        StringSelection selection = new StringSelection(comparisonCodeField.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }//GEN-LAST:event_comparisonCopyCodeButtonActionPerformed

    private void gt509BehaviorCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gt509BehaviorCheckActionPerformed
        FuelRod.setGT509Behavior(gt509BehaviorCheck.isSelected());
        saveAdvancedConfig();
    }//GEN-LAST:event_gt509BehaviorCheckActionPerformed

    private void useGTRecipesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useGTRecipesCheckActionPerformed
        useUfcForCoolantCellsCheck.setEnabled(!useGTRecipesCheck.isSelected());
        expandAdvancedAlloyCheck.setEnabled(!useGTRecipesCheck.isSelected());
        MaterialsList.setUseGTRecipes(useGTRecipesCheck.isSelected());
        materialsArea.setText(reactor.getMaterials().toString());
        saveAdvancedConfig();
    }//GEN-LAST:event_useGTRecipesCheckActionPerformed

    private void useUfcForCoolantCellsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useUfcForCoolantCellsCheckActionPerformed
        MaterialsList.setUseUfcForCoolantCells(useUfcForCoolantCellsCheck.isSelected());
        materialsArea.setText(reactor.getMaterials().toString());
        saveAdvancedConfig();
    }//GEN-LAST:event_useUfcForCoolantCellsCheckActionPerformed

    private void expandAdvancedAlloyCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandAdvancedAlloyCheckActionPerformed
        MaterialsList.setExpandAdvancedAlloy(expandAdvancedAlloyCheck.isSelected());
        materialsArea.setText(reactor.getMaterials().toString());
        saveAdvancedConfig();
    }//GEN-LAST:event_expandAdvancedAlloyCheckActionPerformed

    private void copyComparisonButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyComparisonButtonActionPerformed
        StringSelection selection = new StringSelection(comparisonLabel.getText().replace("<br>", "\n").replaceAll("<[^>]+>", ""));
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }//GEN-LAST:event_copyComparisonButtonActionPerformed

    private void onlyShowDiffCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlyShowDiffCheckActionPerformed
        updateComparison();
    }//GEN-LAST:event_onlyShowDiffCheckActionPerformed
    
    private void updateReactorButtons() {
        for (int row = 0; row < reactorButtons.length; row++) {
            final int finalRow = row;
            for (int col = 0; col < reactorButtons[row].length; col++) {
                final int finalCol = col;
                ReactorItem componentToPlace = reactor.getComponentAt(row, col);
                int buttonSize = Math.min(reactorButtons[finalRow][finalCol].getWidth(), reactorButtons[finalRow][finalCol].getHeight());
                if (buttonSize > 2 && componentToPlace != null && componentToPlace.image != null) {
                    reactorButtons[finalRow][finalCol].setIcon(new ImageIcon(componentToPlace.image.getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
                    reactorButtons[finalRow][finalCol].setToolTipText(componentToPlace.toString());
                    reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                } else {
                    reactorButtons[finalRow][finalCol].setIcon(null);
                    if (componentToPlace != null) {
                        reactorButtons[finalRow][finalCol].setToolTipText(componentToPlace.toString());
                    } else {
                        reactorButtons[finalRow][finalCol].setToolTipText(null);
                    }
                    reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                }
            }
        }
        materialsArea.setText(reactor.getMaterials().toString());
        componentListArea.setText(reactor.getComponentList().toString());
        maxHeatLabel.setText(formatI18n("UI.MaxHeatSpecific", reactor.getMaxHeat()));
        heatSpinnerModel.setMaximum(reactor.getMaxHeat() - 1);
        heatSpinnerModel.setValue(Math.min(((Number) heatSpinnerModel.getValue()).intValue(), reactor.getMaxHeat() - 1));
        temperatureEffectsLabel.setText(formatI18n("UI.TemperatureEffectsSpecific", (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
    }
    
    /**
     * Builds an integer comparison string using the resource bundle, based on whether both left and right values are non-default, or just one.
     * @param comparison the comparison type, for looking up the appropriate keys in the resource bundle, between "Comparison." and ".Both", ".LeftOnly", or ".RightOnly"
     * @param left the left-side value of the comparison
     * @param right the right-side value of the comparison
     * @param defaultValue the default value for the relevant entry, and if either left or right is equal to it, that value can be omitted as not applicable.
     * If both are equal to this, the method should not even be called.
     * @return a string to show the comparison between the two values.
     */
    private String buildIntComparisonString(String comparisonType, int left, int right, int defaultValue) {
        if (right == defaultValue) {
            return String.format(getI18n("Comparison." + comparisonType + ".LeftOnly"), left);
        } else if (left == defaultValue) {
            return String.format(getI18n("Comparison." + comparisonType + ".RightOnly"), right);
        } else {
            return String.format(getI18n("Comparison." + comparisonType + ".Both"), left - right, left, right);
        }
    }
    
    /**
     * Builds an integer comparison string using the resource bundle, based on whether both left and right values are non-default, or just one.
     * @param comparison the comparison type, for looking up the appropriate keys in the resource bundle, between "Comparison." and ".BothColored", ".LeftOnly", or ".RightOnly"
     * @param left the left-side value of the comparison
     * @param right the right-side value of the comparison
     * @param defaultValue the default value for the relevant entry, and if either left or right is equal to it, that value can be omitted as not applicable.
     * @param threshold the minimum value to consider good or bad - if this is negative, then negative differences with greater magnitude will be shown in green (good), 
     * otherwise positive differences will be green; differences in the other direction will be shown in red (bad).  Differences closer to zero than this (in either direction) 
     * will be shown in orange.
     * If both are equal to this, the method should not even be called.
     * @return a string to show the comparison between the two values.
     */
    private String buildColoredIntComparisonString(String comparisonType, int left, int right, int defaultValue, int threshold) {
        if (right == defaultValue) {
            return String.format(getI18n("Comparison." + comparisonType + ".LeftOnly"), left);
        } else if (left == defaultValue) {
            return String.format(getI18n("Comparison." + comparisonType + ".RightOnly"), right);
        } else {
            String color = "orange";
            if (Math.abs(left - right) > Math.abs(threshold)) {
                if (Math.signum(left - right) == Math.signum(threshold)) {
                    color = "green";
                } else {
                    color = "red";
                }
            }
            return String.format(getI18n("Comparison." + comparisonType + ".BothColored"), color, left - right, left, right);
        }
    }
    
    private final DecimalFormat comparisonFormat = new DecimalFormat(getI18n("Comparison.CompareDecimalFormat"));
    private final DecimalFormat simpleFormat = new DecimalFormat(getI18n("Comparison.SimpleDecimalFormat"));
    
    private String colorDecimal(double value, double threshold) {
        String color = "orange";
        if (Math.abs(value) > Math.abs(threshold)) {
            if (Math.signum(value) == Math.signum(threshold)) {
                color = "green";
            } else {
                color = "red";
            }
        }
        return String.format("<font color=\"%s\">%s</font>", color, comparisonFormat.format(value));
    }
    
    private String simpleDecimal(double value) {
        return simpleFormat.format(value);
    }
    
    private void updateComparison() {
        if (simulator == null || simulator.getData() == null || prevSimulator == null || prevSimulator.getData() == null) {
            return;
        }
        if (showOldStyleReactorCodeCheck.isSelected()) {
            comparisonCodeField.setText(prevReactorOldCode);
        } else {
            comparisonCodeField.setText(prevReactorCode);
        }
        comparisonScroll.getVerticalScrollBar().setUnitIncrement(16);
        StringBuilder text = new StringBuilder(1000);
        boolean alwaysDiff = !onlyShowDiffCheck.isSelected();
        text.append("<html>");
        SimulationData leftData = simulator.getData();
        SimulationData rightData = prevSimulator.getData();
        if ((leftData.timeToBelow50 != Integer.MAX_VALUE || rightData.timeToBelow50 != Integer.MAX_VALUE) && (alwaysDiff || leftData.timeToBelow50 != rightData.timeToBelow50)) {
            text.append(getI18n("Comparison.Prefix.TimeToBelow50"));
            text.append(buildColoredIntComparisonString("Time", leftData.timeToBelow50, rightData.timeToBelow50, Integer.MAX_VALUE, 1));
        }
        if ((leftData.timeToBurn != Integer.MAX_VALUE || rightData.timeToBurn != Integer.MAX_VALUE) && (alwaysDiff || leftData.timeToBurn != rightData.timeToBurn)) {
            text.append(getI18n("Comparison.Prefix.TimeToBurn"));
            text.append(buildColoredIntComparisonString("Time", leftData.timeToBurn, rightData.timeToBurn, Integer.MAX_VALUE, 1));
        }
        if ((leftData.timeToEvaporate != Integer.MAX_VALUE || rightData.timeToEvaporate != Integer.MAX_VALUE) && (alwaysDiff || leftData.timeToEvaporate != rightData.timeToEvaporate)) {
            text.append(getI18n("Comparison.Prefix.TimeToEvaporate"));
            text.append(buildColoredIntComparisonString("Time", leftData.timeToEvaporate, rightData.timeToEvaporate, Integer.MAX_VALUE, 1));
        }
        if ((leftData.timeToHurt != Integer.MAX_VALUE || rightData.timeToHurt != Integer.MAX_VALUE) && (alwaysDiff || leftData.timeToHurt != rightData.timeToHurt)) {
            text.append(getI18n("Comparison.Prefix.TimeToHurt"));
            text.append(buildColoredIntComparisonString("Time", leftData.timeToHurt, rightData.timeToHurt, Integer.MAX_VALUE, 1));
        }
        if ((leftData.timeToLava != Integer.MAX_VALUE || rightData.timeToLava != Integer.MAX_VALUE) && (alwaysDiff || leftData.timeToLava != rightData.timeToLava)) {
            text.append(getI18n("Comparison.Prefix.TimeToLava"));
            text.append(buildColoredIntComparisonString("Time", leftData.timeToLava, rightData.timeToLava, Integer.MAX_VALUE, 1));
        }
        if ((leftData.timeToXplode != Integer.MAX_VALUE || rightData.timeToXplode != Integer.MAX_VALUE) && (alwaysDiff || leftData.timeToXplode != rightData.timeToXplode)) {
            text.append(getI18n("Comparison.Prefix.TimeToXplode"));
            text.append(buildColoredIntComparisonString("Time", leftData.timeToXplode, rightData.timeToXplode, Integer.MAX_VALUE, 1));
        }
        if (leftData.firstComponentBrokenTime != Integer.MAX_VALUE || rightData.firstComponentBrokenTime != Integer.MAX_VALUE) {
            text.append(getI18n("Comparison.Prefix.PrebreakTime"));
            text.append(buildIntComparisonString("Time", leftData.firstComponentBrokenTime, rightData.firstComponentBrokenTime, Integer.MAX_VALUE));

            if (leftData.firstComponentBrokenTime != Integer.MAX_VALUE && rightData.firstComponentBrokenTime != Integer.MAX_VALUE) {
                text.append(getI18n("Comparison.Prefix.Prebreak"));
                if (leftData.prebreakTotalEUoutput > 0) {
                    if (rightData.prebreakTotalEUoutput > 0) {
                        if (alwaysDiff || Math.abs(leftData.prebreakTotalEUoutput - rightData.prebreakTotalEUoutput) > 1000
                                || Math.abs(leftData.prebreakAvgEUoutput - rightData.prebreakAvgEUoutput) > 0.1
                                || Math.abs(leftData.prebreakMinEUoutput - rightData.prebreakMinEUoutput) > 0.1
                                || Math.abs(leftData.prebreakMaxEUoutput - rightData.prebreakMaxEUoutput) > 0.1) {
                            text.append(formatI18n("Comparison.EUEUoutput",
                                    colorDecimal(leftData.prebreakTotalEUoutput - rightData.prebreakTotalEUoutput, 1000),
                                    simpleDecimal(leftData.prebreakTotalEUoutput),
                                    simpleDecimal(rightData.prebreakTotalEUoutput),
                                    colorDecimal(leftData.prebreakAvgEUoutput - rightData.prebreakAvgEUoutput, 0.1),
                                    simpleDecimal(leftData.prebreakAvgEUoutput),
                                    simpleDecimal(rightData.prebreakAvgEUoutput),
                                    colorDecimal(leftData.prebreakMinEUoutput - rightData.prebreakMinEUoutput, 0.1),
                                    simpleDecimal(leftData.prebreakMinEUoutput),
                                    simpleDecimal(rightData.prebreakMinEUoutput),
                                    colorDecimal(leftData.prebreakMaxEUoutput - rightData.prebreakMaxEUoutput, 0.1),
                                    simpleDecimal(leftData.prebreakMaxEUoutput),
                                    simpleDecimal(rightData.prebreakMaxEUoutput)));
                        }
                    } else {
                        text.append(formatI18n("Comparison.EUHUoutput",
                                simpleDecimal(leftData.prebreakTotalEUoutput),
                                simpleDecimal(rightData.prebreakTotalHUoutput),
                                simpleDecimal(leftData.prebreakAvgEUoutput),
                                simpleDecimal(rightData.prebreakAvgHUoutput),
                                simpleDecimal(leftData.prebreakMinEUoutput),
                                simpleDecimal(rightData.prebreakMinHUoutput),
                                simpleDecimal(leftData.prebreakMaxEUoutput),
                                simpleDecimal(rightData.prebreakMaxHUoutput)));
                    }
                } else {
                    if (rightData.prebreakTotalEUoutput > 0) {
                        text.append(formatI18n("Comparison.HUEUoutput",
                                simpleDecimal(leftData.prebreakTotalHUoutput),
                                simpleDecimal(rightData.prebreakTotalEUoutput),
                                simpleDecimal(leftData.prebreakAvgHUoutput),
                                simpleDecimal(rightData.prebreakAvgEUoutput),
                                simpleDecimal(leftData.prebreakMinHUoutput),
                                simpleDecimal(rightData.prebreakMinEUoutput),
                                simpleDecimal(leftData.prebreakMaxHUoutput),
                                simpleDecimal(rightData.prebreakMaxEUoutput)));
                    } else {
                        if (alwaysDiff || Math.abs(leftData.prebreakTotalHUoutput - rightData.prebreakTotalHUoutput) > 1000
                                || Math.abs(leftData.prebreakAvgHUoutput - rightData.prebreakAvgHUoutput) > 0.1
                                || Math.abs(leftData.prebreakMinHUoutput - rightData.prebreakMinHUoutput) > 0.1
                                || Math.abs(leftData.prebreakMaxHUoutput - rightData.prebreakMaxHUoutput) > 0.1) {
                            text.append(formatI18n("Comparison.HUHUoutput",
                                    colorDecimal(leftData.prebreakTotalHUoutput - rightData.prebreakTotalHUoutput, 1000),
                                    simpleDecimal(leftData.prebreakTotalHUoutput),
                                    simpleDecimal(rightData.prebreakTotalHUoutput),
                                    colorDecimal(leftData.prebreakAvgHUoutput - rightData.prebreakAvgHUoutput, 0.1),
                                    simpleDecimal(leftData.prebreakAvgHUoutput),
                                    simpleDecimal(rightData.prebreakAvgHUoutput),
                                    colorDecimal(leftData.prebreakMinHUoutput - rightData.prebreakMinHUoutput, 0.1),
                                    simpleDecimal(leftData.prebreakMinHUoutput),
                                    simpleDecimal(rightData.prebreakMinHUoutput),
                                    colorDecimal(leftData.prebreakMaxHUoutput - rightData.prebreakMaxHUoutput, 0.1),
                                    simpleDecimal(leftData.prebreakMaxHUoutput),
                                    simpleDecimal(rightData.prebreakMaxHUoutput)));
                        }
                    }
                }
            }
            text.append("<br>");
        }
        Reactor tempReactor = new Reactor();
        tempReactor.setCode(currentReactorCode);
        Reactor prevReactor = new Reactor();
        prevReactor.setCode(prevReactorCode);
        boolean moxStyleReactor = false;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                ReactorItem component = tempReactor.getComponentAt(row, col);
                if (component != null) {
                    String baseName = component.baseName;
                    if ("fuelRodMox".equals(baseName) || "dualFuelRodMox".equals(baseName) || "quadFuelRodMox".equals(baseName)
                            || "fuelRodNaquadah".equals(baseName) || "dualFuelRodNaquadah".equals(baseName) || "quadFuelRodNaquadah".equals(baseName)) {
                        moxStyleReactor = true;
                    }
                }
            }
        }
        if (leftData.firstRodDepletedTime != Integer.MAX_VALUE || rightData.firstRodDepletedTime != Integer.MAX_VALUE) {
            text.append(getI18n("Comparison.Prefix.PredepleteTime"));
            text.append(buildIntComparisonString("Time", leftData.firstRodDepletedTime, rightData.firstRodDepletedTime, Integer.MAX_VALUE));

            if (leftData.totalRodCount > 0 && rightData.totalRodCount > 0) {
                text.append(getI18n("Comparison.Prefix.Predeplete"));
                if (leftData.predepleteTotalEUoutput > 0) {
                    if (rightData.predepleteTotalEUoutput > 0) {
                        if (alwaysDiff || Math.abs(leftData.predepleteTotalEUoutput - rightData.predepleteTotalEUoutput) > 1000
                                || Math.abs(leftData.predepleteAvgEUoutput - rightData.predepleteAvgEUoutput) > 0.1
                                || Math.abs(leftData.predepleteMinEUoutput - rightData.predepleteMinEUoutput) > 0.1
                                || Math.abs(leftData.predepleteMaxEUoutput - rightData.predepleteMaxEUoutput) > 0.1) {
                            text.append(formatI18n("Comparison.EUEUoutput",
                                    colorDecimal(leftData.predepleteTotalEUoutput - rightData.predepleteTotalEUoutput, 1000),
                                    simpleDecimal(leftData.predepleteTotalEUoutput),
                                    simpleDecimal(rightData.predepleteTotalEUoutput),
                                    colorDecimal(leftData.predepleteAvgEUoutput - rightData.predepleteAvgEUoutput, 0.1),
                                    simpleDecimal(leftData.predepleteAvgEUoutput),
                                    simpleDecimal(rightData.predepleteAvgEUoutput),
                                    colorDecimal(leftData.predepleteMinEUoutput - rightData.predepleteMinEUoutput, 0.1),
                                    simpleDecimal(leftData.predepleteMinEUoutput),
                                    simpleDecimal(rightData.predepleteMinEUoutput),
                                    colorDecimal(leftData.predepleteMaxEUoutput - rightData.predepleteMaxEUoutput, 0.1),
                                    simpleDecimal(leftData.predepleteMaxEUoutput),
                                    simpleDecimal(rightData.predepleteMaxEUoutput)));
                        }
                    } else {
                        text.append(formatI18n("Comparison.EUHUoutput",
                                simpleDecimal(leftData.predepleteTotalEUoutput),
                                simpleDecimal(rightData.predepleteTotalHUoutput),
                                simpleDecimal(leftData.predepleteAvgEUoutput),
                                simpleDecimal(rightData.predepleteAvgHUoutput),
                                simpleDecimal(leftData.predepleteMinEUoutput),
                                simpleDecimal(rightData.predepleteMinHUoutput),
                                simpleDecimal(leftData.predepleteMaxEUoutput),
                                simpleDecimal(rightData.predepleteMaxHUoutput)));
                    }
                } else {
                    if (rightData.predepleteTotalEUoutput > 0) {
                        text.append(formatI18n("Comparison.HUEUoutput",
                                simpleDecimal(leftData.predepleteTotalHUoutput),
                                simpleDecimal(rightData.predepleteTotalEUoutput),
                                simpleDecimal(leftData.predepleteAvgHUoutput),
                                simpleDecimal(rightData.predepleteAvgEUoutput),
                                simpleDecimal(leftData.predepleteMinHUoutput),
                                simpleDecimal(rightData.predepleteMinEUoutput),
                                simpleDecimal(leftData.predepleteMaxHUoutput),
                                simpleDecimal(rightData.predepleteMaxEUoutput)));
                    } else {
                        if (alwaysDiff || Math.abs(leftData.predepleteTotalHUoutput - rightData.predepleteTotalHUoutput) > 1000
                                || Math.abs(leftData.predepleteAvgHUoutput - rightData.predepleteAvgHUoutput) > 0.1
                                || Math.abs(leftData.predepleteMinHUoutput - rightData.predepleteMinHUoutput) > 0.1
                                || Math.abs(leftData.predepleteMaxHUoutput - rightData.predepleteMaxHUoutput) > 0.1) {
                            text.append(formatI18n("Comparison.HUHUoutput",
                                    colorDecimal(leftData.predepleteTotalHUoutput - rightData.predepleteTotalHUoutput, 1000),
                                    simpleDecimal(leftData.predepleteTotalHUoutput),
                                    simpleDecimal(rightData.predepleteTotalHUoutput),
                                    colorDecimal(leftData.predepleteAvgHUoutput - rightData.predepleteAvgHUoutput, 0.1),
                                    simpleDecimal(leftData.predepleteAvgHUoutput),
                                    simpleDecimal(rightData.predepleteAvgHUoutput),
                                    colorDecimal(leftData.predepleteMinHUoutput - rightData.predepleteMinHUoutput, 0.1),
                                    simpleDecimal(leftData.predepleteMinHUoutput),
                                    simpleDecimal(rightData.predepleteMinHUoutput),
                                    colorDecimal(leftData.predepleteMaxHUoutput - rightData.predepleteMaxHUoutput, 0.1),
                                    simpleDecimal(leftData.predepleteMaxHUoutput),
                                    simpleDecimal(rightData.predepleteMaxHUoutput)));
                        }
                    }
                }
                if (alwaysDiff || Math.abs(leftData.predepleteMinTemp - rightData.predepleteMinTemp) > 10) {
                    text.append(formatI18n("Comparison.PredepleteMinTemp",
                            colorDecimal(leftData.predepleteMinTemp - rightData.predepleteMinTemp, moxStyleReactor ? 10 : -10),
                            simpleDecimal(leftData.predepleteMinTemp),
                            simpleDecimal(rightData.predepleteMinTemp)));
                }
                if (alwaysDiff || Math.abs(leftData.predepleteMaxTemp - rightData.predepleteMaxTemp) > 10) {
                    text.append(formatI18n("Comparison.PredepleteMaxTemp",
                            colorDecimal(leftData.predepleteMaxTemp - rightData.predepleteMaxTemp, moxStyleReactor ? 10 : -10),
                            simpleDecimal(leftData.predepleteMaxTemp),
                            simpleDecimal(rightData.predepleteMaxTemp)));
                }
            }
            text.append("<br>");
        }
        
        text.append(getI18n("Comparison.Prefix.PostSimulationTime"));
        text.append(buildIntComparisonString("Time", leftData.totalReactorTicks, rightData.totalReactorTicks, Integer.MAX_VALUE));
        text.append(getI18n("Comparison.Prefix.PostSimulation"));
        if (leftData.totalEUoutput > 0) {
            if (rightData.totalEUoutput > 0) {
                if (alwaysDiff || Math.abs(leftData.totalEUoutput - rightData.totalEUoutput) > 1000
                        || Math.abs(leftData.avgEUoutput - rightData.avgEUoutput) > 0.1
                        || Math.abs(leftData.minEUoutput - rightData.minEUoutput) > 0.1
                        || Math.abs(leftData.maxEUoutput - rightData.maxEUoutput) > 0.1) {
                    text.append(formatI18n("Comparison.EUEUoutput",
                            colorDecimal(leftData.totalEUoutput - rightData.totalEUoutput, 1000),
                            simpleDecimal(leftData.totalEUoutput),
                            simpleDecimal(rightData.totalEUoutput),
                            colorDecimal(leftData.avgEUoutput - rightData.avgEUoutput, 0.1),
                            simpleDecimal(leftData.avgEUoutput),
                            simpleDecimal(rightData.avgEUoutput),
                            colorDecimal(leftData.minEUoutput - rightData.minEUoutput, 0.1),
                            simpleDecimal(leftData.minEUoutput),
                            simpleDecimal(rightData.minEUoutput),
                            colorDecimal(leftData.maxEUoutput - rightData.maxEUoutput, 0.1),
                            simpleDecimal(leftData.maxEUoutput),
                            simpleDecimal(rightData.maxEUoutput)));
                }
            } else {
                text.append(formatI18n("Comparison.EUHUoutput",
                        simpleDecimal(leftData.totalEUoutput),
                        simpleDecimal(rightData.totalHUoutput),
                        simpleDecimal(leftData.avgEUoutput),
                        simpleDecimal(rightData.avgHUoutput),
                        simpleDecimal(leftData.minEUoutput),
                        simpleDecimal(rightData.minHUoutput),
                        simpleDecimal(leftData.maxEUoutput),
                        simpleDecimal(rightData.maxHUoutput)));
            }
        } else {
            if (rightData.totalEUoutput > 0) {
                text.append(formatI18n("Comparison.HUEUoutput",
                        simpleDecimal(leftData.totalHUoutput),
                        simpleDecimal(rightData.totalEUoutput),
                        simpleDecimal(leftData.avgHUoutput),
                        simpleDecimal(rightData.avgEUoutput),
                        simpleDecimal(leftData.minHUoutput),
                        simpleDecimal(rightData.minEUoutput),
                        simpleDecimal(leftData.maxHUoutput),
                        simpleDecimal(rightData.maxEUoutput)));
            } else {
                if (alwaysDiff || Math.abs(leftData.totalHUoutput - rightData.totalHUoutput) > 1000
                        || Math.abs(leftData.avgHUoutput - rightData.avgHUoutput) > 0.1
                        || Math.abs(leftData.minHUoutput - rightData.minHUoutput) > 0.1
                        || Math.abs(leftData.maxHUoutput - rightData.maxHUoutput) > 0.1) {
                    text.append(formatI18n("Comparison.HUHUoutput",
                            colorDecimal(leftData.totalHUoutput - rightData.totalHUoutput, 1000),
                            simpleDecimal(leftData.totalHUoutput),
                            simpleDecimal(rightData.totalHUoutput),
                            colorDecimal(leftData.avgHUoutput - rightData.avgHUoutput, 0.1),
                            simpleDecimal(leftData.avgHUoutput),
                            simpleDecimal(rightData.avgHUoutput),
                            colorDecimal(leftData.minHUoutput - rightData.minHUoutput, 0.1),
                            simpleDecimal(leftData.minHUoutput),
                            simpleDecimal(rightData.minHUoutput),
                            colorDecimal(leftData.maxHUoutput - rightData.maxHUoutput, 0.1),
                            simpleDecimal(leftData.maxHUoutput),
                            simpleDecimal(rightData.maxHUoutput)));
                }
            }
        }
        if (alwaysDiff || Math.abs(leftData.minTemp - rightData.minTemp) > 10) {
            text.append(formatI18n("Comparison.PostsimMinTemp",
                    colorDecimal(leftData.minTemp - rightData.minTemp, moxStyleReactor ? 10 : -10),
                    simpleDecimal(leftData.minTemp),
                    simpleDecimal(rightData.minTemp)));
        }
        if (alwaysDiff || Math.abs(leftData.maxTemp - rightData.maxTemp) > 10) {
            text.append(formatI18n("Comparison.PostsimMaxTemp",
                    colorDecimal(leftData.maxTemp - rightData.maxTemp, moxStyleReactor ? 10 : -10),
                    simpleDecimal(leftData.maxTemp),
                    simpleDecimal(rightData.maxTemp)));
        }
        text.append("<br>");
        
        if ((leftData.hullHeating != 0 || rightData.hullHeating != 0) && (alwaysDiff || Math.abs(leftData.hullHeating - rightData.hullHeating) > 1)) {
            text.append(formatI18n("Comparison.HullHeating",
                    colorDecimal(leftData.hullHeating - rightData.hullHeating, -1),
                    simpleDecimal(leftData.hullHeating),
                    simpleDecimal(rightData.hullHeating)));
        }
        if ((leftData.componentHeating != 0 || rightData.componentHeating != 0) && (alwaysDiff || Math.abs(leftData.componentHeating - rightData.componentHeating) > 1)) {
            text.append(formatI18n("Comparison.ComponentHeating",
                    colorDecimal(leftData.componentHeating - rightData.componentHeating, -1),
                    simpleDecimal(leftData.componentHeating),
                    simpleDecimal(rightData.componentHeating)));
        }
        if (leftData.hullCooling != 0 || rightData.hullCooling != 0) {
            if (alwaysDiff || Math.abs(leftData.hullCooling - rightData.hullCooling) > 1) {
                text.append(formatI18n("Comparison.HullCooling",
                        colorDecimal(leftData.hullCooling - rightData.hullCooling, 1),
                        simpleDecimal(leftData.hullCooling),
                        simpleDecimal(rightData.hullCooling)));
            }
            if (alwaysDiff || Math.abs(leftData.hullCoolingCapacity - rightData.hullCoolingCapacity) > 1) {
                text.append(formatI18n("Comparison.HullCoolingPossible",
                        colorDecimal(leftData.hullCoolingCapacity - rightData.hullCoolingCapacity, 1),
                        simpleDecimal(leftData.hullCoolingCapacity),
                        simpleDecimal(rightData.hullCoolingCapacity)));
            }
        }
        if (leftData.ventCooling != 0 || rightData.ventCooling != 0) {
            if (alwaysDiff || Math.abs(leftData.ventCooling - rightData.ventCooling) > 1) {
                text.append(formatI18n("Comparison.VentCooling",
                        colorDecimal(leftData.ventCooling - rightData.ventCooling, 1),
                        simpleDecimal(leftData.ventCooling),
                        simpleDecimal(rightData.ventCooling)));
            }
            if (alwaysDiff || Math.abs(leftData.ventCoolingCapacity - rightData.ventCoolingCapacity) > 1) {
                text.append(formatI18n("Comparison.VentCoolingPossible",
                        colorDecimal(leftData.ventCoolingCapacity - rightData.ventCoolingCapacity, 1),
                        simpleDecimal(leftData.ventCoolingCapacity),
                        simpleDecimal(rightData.ventCoolingCapacity)));
            }
        }
        text.append("<br>");
        
        final String materialDiff = tempReactor.getMaterials().buildComparisonString(prevReactor.getMaterials(), alwaysDiff);
        if (!materialDiff.isEmpty()) {
            text.append(getI18n("Comparison.MaterialsHeading"));
            text.append(materialDiff);
            text.append("<br>");
        }
        
        final String componentDiff = tempReactor.getComponentList().buildComparisonString(prevReactor.getComponentList(), alwaysDiff);
        if (!componentDiff.isEmpty()) {
            text.append(getI18n("Comparison.ComponentsHeading"));
            text.append(componentDiff);
            text.append("<br>");
        }
        
        final String replacedDiff = leftData.replacedItems.buildComparisonString(rightData.replacedItems, alwaysDiff);
        if (!replacedDiff.isEmpty()) {
            text.append(getI18n("Comparison.ComponentsReplacedHeading"));
            text.append(replacedDiff);
        }
        
        text.append("</html>");
        comparisonLabel.setText(text.toString());
        copyComparisonButton.setEnabled(true);
    }

    /**
     * @param compType Component type to read from the bundle object
     * @return Assembled tooltip string
     */
    private String buildTooltipInfo(final String compType) {
        // modified from Pull Request by kekzdealer: https://github.com/MauveCloud/Ic2ExpReactorPlanner/pull/67
    	final StringBuilder result = new StringBuilder(500);
    	
    	result.append("<html>");
    	result.append(getI18n("ComponentName." +compType));
    	result.append("<br>");
    	result.append(getI18n("ComponentData." +compType));
    	result.append("</html>");
    	
    	return result.toString();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionDialogDisplay());
        System.setProperty("sun.awt.exception.handler", ExceptionDialogDisplay.class.getName());
        
        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
            new ReactorPlannerFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton advancedHeatExchangerButton;
    private javax.swing.JToggleButton advancedHeatVentButton;
    private javax.swing.JPanel advancedPanel;
    private javax.swing.JScrollPane advancedScroll;
    private javax.swing.JCheckBox automatedReactorCheck;
    private javax.swing.JPanel automationPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearGridButton;
    private javax.swing.JTextField codeField;
    private javax.swing.JTextField comparisonCodeField;
    private javax.swing.JButton comparisonCopyCodeButton;
    private javax.swing.JLabel comparisonLabel;
    private javax.swing.JScrollPane comparisonScroll;
    private javax.swing.JTextArea componentArea;
    private javax.swing.JToggleButton componentHeatExchangerButton;
    private javax.swing.JLabel componentHeatLabel;
    private javax.swing.JSpinner componentHeatSpinner;
    private javax.swing.JToggleButton componentHeatVentButton;
    private javax.swing.JTextArea componentListArea;
    private javax.swing.JScrollPane componentListPane;
    private javax.swing.JScrollPane componentPane;
    private javax.swing.ButtonGroup componentsGroup;
    private javax.swing.JPanel componentsPanel;
    private javax.swing.JToggleButton containmentReactorPlatingButton;
    private javax.swing.JToggleButton coolantCell10kButton;
    private javax.swing.JToggleButton coolantCell30kButton;
    private javax.swing.JToggleButton coolantCell60kButton;
    private javax.swing.JToggleButton coolantCellHelium180kButton;
    private javax.swing.JToggleButton coolantCellHelium360kButton;
    private javax.swing.JToggleButton coolantCellHelium60kButton;
    private javax.swing.JToggleButton coolantCellNak180kButton;
    private javax.swing.JToggleButton coolantCellNak360kButton;
    private javax.swing.JToggleButton coolantCellNak60kButton;
    private javax.swing.JButton copyCodeButton;
    private javax.swing.JButton copyComparisonButton;
    private javax.swing.JToggleButton coreHeatExchangerButton;
    private javax.swing.JButton csvBrowseButton;
    private javax.swing.JLabel csvFileLabel;
    private javax.swing.JSpinner csvLimitSpinner;
    private javax.swing.JCheckBox csvOutputCheck;
    private javax.swing.JPanel csvPanel;
    private javax.swing.JToggleButton dualFuelRodMoxButton;
    private javax.swing.JToggleButton dualFuelRodNaquadahButton;
    private javax.swing.JToggleButton dualFuelRodThoriumButton;
    private javax.swing.JToggleButton dualFuelRodUraniumButton;
    private javax.swing.JToggleButton emptyButton;
    private javax.swing.JCheckBox enableGT508ComponentsCheck;
    private javax.swing.JCheckBox enableGT509ComponentsCheck;
    private javax.swing.JRadioButton euReactorRadio;
    private javax.swing.JCheckBox expandAdvancedAlloyCheck;
    private javax.swing.JRadioButton fluidReactorRadio;
    private javax.swing.JToggleButton fuelRodMoxButton;
    private javax.swing.JToggleButton fuelRodNaquadahButton;
    private javax.swing.JToggleButton fuelRodThoriumButton;
    private javax.swing.JToggleButton fuelRodUraniumButton;
    private javax.swing.JCheckBox gt509BehaviorCheck;
    private javax.swing.JToggleButton heatCapacityReactorPlatingButton;
    private javax.swing.JToggleButton heatExchangerButton;
    private javax.swing.JSpinner heatSpinner;
    private javax.swing.JToggleButton heatVentButton;
    private javax.swing.JToggleButton iridiumNeutronReflectorButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox lockPrevCodeCheck;
    private javax.swing.JToggleButton lzhCondensatorButton;
    private javax.swing.JTextArea materialsArea;
    private javax.swing.JScrollPane materialsListPane;
    private javax.swing.JLabel maxHeatLabel;
    private javax.swing.JLabel maxSimulationTicksLabel;
    private javax.swing.JSpinner maxSimulationTicksSpinner;
    private javax.swing.JToggleButton neutronReflectorButton;
    private javax.swing.JSpinner offPulseSpinner;
    private javax.swing.JSpinner onPulseSpinner;
    private javax.swing.JCheckBox onlyShowDiffCheck;
    private javax.swing.JTextArea outputArea;
    private javax.swing.JScrollPane outputPane;
    private javax.swing.JTabbedPane outputTabs;
    private javax.swing.JToggleButton overclockedHeatVentButton;
    private javax.swing.JButton pasteCodeButton;
    private javax.swing.JSpinner pauseSpinner;
    private javax.swing.JLabel placingLabel;
    private javax.swing.JLabel placingReactorPauseLabel;
    private javax.swing.JSpinner placingReactorPauseSpinner;
    private javax.swing.JLabel placingThresholdLabel;
    private javax.swing.JSpinner placingThresholdSpinner;
    private javax.swing.JPanel pulsePanel;
    private javax.swing.JCheckBox pulsedReactorCheck;
    private javax.swing.JToggleButton quadFuelRodMoxButton;
    private javax.swing.JToggleButton quadFuelRodNaquadahButton;
    private javax.swing.JToggleButton quadFuelRodThoriumButton;
    private javax.swing.JToggleButton quadFuelRodUraniumButton;
    private javax.swing.JCheckBox reactorCoolantInjectorCheckbox;
    private javax.swing.JToggleButton reactorHeatVentButton;
    private javax.swing.JPanel reactorPanel;
    private javax.swing.JToggleButton reactorPlatingButton;
    private javax.swing.ButtonGroup reactorStyleGroup;
    private javax.swing.JButton resetPulseConfigButton;
    private javax.swing.JSpinner resumeTempSpinner;
    private javax.swing.JToggleButton rshCondensatorButton;
    private javax.swing.JLabel selectedComponentLabel;
    private javax.swing.JCheckBox showComponentDetailButtonsCheck;
    private javax.swing.JCheckBox showComponentPreconfigCheck;
    private javax.swing.JCheckBox showOldStyleReactorCodeCheck;
    private javax.swing.JButton simulateButton;
    private javax.swing.JSpinner suspendTempSpinner;
    private javax.swing.JPanel temperatureAndComponentsPanel;
    private javax.swing.JLabel temperatureEffectsLabel;
    private javax.swing.JButton texturePackBrowseButton;
    private javax.swing.JButton texturePackClearButton;
    private javax.swing.JLabel texturePackLabel;
    private javax.swing.JToggleButton thickNeutronReflectorButton;
    private javax.swing.JSpinner thresholdSpinner;
    private javax.swing.JCheckBox useGTRecipesCheck;
    private javax.swing.JCheckBox useUfcForCoolantCellsCheck;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
