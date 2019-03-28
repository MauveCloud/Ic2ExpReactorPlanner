/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * JFrame to display a gui for planning a Nuclear Reactor using IndustrialCraft2 Experimental.
 * @author Brian McCloud
 */
public class ReactorPlannerFrame extends javax.swing.JFrame {

    private static final String VERSION = "2.3.3";
    
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
    
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle");
    
    private static final Properties advancedConfig = new Properties();
    
    /**
     * Creates new form ReactorPlannerFrame
     */
    public ReactorPlannerFrame() {
        initComponents();
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
                        placingLabel.setText(BUNDLE.getString("UI.ComponentPlacingDefault"));
                        paletteComponentId = 0;
                    } else if (button.getActionCommand() != null) {
                        ReactorItem tempComponent = ComponentFactory.getDefaultComponent(button.getActionCommand());
                        if (tempComponent != null) {
                            paletteComponentId = tempComponent.id;
                            if (paletteComponents[paletteComponentId] == null) {
                                paletteComponents[paletteComponentId] = ComponentFactory.createComponent(paletteComponentId);
                            }
                            placingLabel.setText(String.format(BUNDLE.getString("UI.ComponentPlacingSpecific"),
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
                JButton automationButton = new JButton(BUNDLE.getString("UI.AutomateButton"));
                automationButton.setFont(Font.decode(BUNDLE.getString("UI.AutomateButtonFont")));
                automationButton.setMargin(new Insets(-2, 0, -2, 0));
                automationButton.setToolTipText(BUNDLE.getString("UI.AutomationTooltip"));
                automationButton.setFocusable(false);
                automationButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final ReactorItem component = reactor.getComponentAt(finalRow, finalCol);
                        selectedRow = finalRow;
                        selectedColumn = finalCol;
                        if (component == null) {
                            selectedComponentLabel.setText(String.format(BUNDLE.getString("UI.NoComponentRowCol"), finalRow, finalCol));
                        } else {
                            selectedComponentLabel.setText(String.format(BUNDLE.getString("UI.ChosenComponentRowCol"), component.toString(), finalRow, finalCol));
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
                JButton infoButton = new JButton(BUNDLE.getString("UI.ComponentInfoButton"));
                infoButton.setFont(Font.decode(BUNDLE.getString("UI.ComponentInfoButtonFont")));
                infoButton.setMargin(new Insets(-2, 0, -2, 0));
                infoButton.setFocusable(false);
                infoButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (simulatedReactor != null) {
                            final ReactorItem component = simulatedReactor.getComponentAt(finalRow, finalCol);
                            if (component == null) {
                                componentArea.setText(String.format(BUNDLE.getString("UI.NoComponentLastSimRowCol"), finalRow, finalCol));
                            } else {
                                componentArea.setText(String.format(BUNDLE.getString("UI.ComponentInfoLastSimRowCol"), component.toString(), finalRow, finalCol, component.info));
                            }
                        } else {
                            componentArea.setText(BUNDLE.getString("UI.NoSimulationRun"));
                        }
                        outputTabs.setSelectedComponent(componentPane);
                    }
                });
                infoButton.setToolTipText(BUNDLE.getString("UI.ComponentInfoButtonTooltip"));
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
                        maxHeatLabel.setText(String.format(BUNDLE.getString("UI.MaxHeatSpecific"), reactor.getMaxHeat()));
                        heatSpinnerModel.setMaximum(reactor.getMaxHeat() - 1);
                        heatSpinnerModel.setValue(Math.min(((Number)heatSpinnerModel.getValue()).intValue(), reactor.getMaxHeat() - 1));
                        temperatureEffectsLabel.setText(String.format(BUNDLE.getString("UI.TemperatureEffectsSpecific"), (int) (reactor.getMaxHeat() * 0.4), (int) (reactor.getMaxHeat() * 0.5), (int) (reactor.getMaxHeat() * 0.7), (int) (reactor.getMaxHeat() * 0.85), (int) (reactor.getMaxHeat() * 1.0)));
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
                            maxHeatLabel.setText(String.format(BUNDLE.getString("UI.MaxHeatSpecific"), reactor.getMaxHeat()));
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
            public void changedUpdate(DocumentEvent e) {
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
        });
        loadAdvancedConfig();
    }

    private void loadAdvancedConfig() {
        try (FileInputStream configStream = new FileInputStream("erpprefs.xml")) {
            advancedConfig.loadFromXML(configStream);
            showComponentDetailButtonsCheck.setSelected(Boolean.valueOf(advancedConfig.getProperty("showComponentDetailButtons", "true")));
            enableGT508ComponentsCheck.setSelected(Boolean.valueOf(advancedConfig.getProperty("enableGT508Components", "true")));
            enableGT509ComponentsCheck.setSelected(Boolean.valueOf(advancedConfig.getProperty("enableGT509Components", "true")));
            showOldStyleReactorCodeCheck.setSelected(Boolean.valueOf(advancedConfig.getProperty("showOldStyleReactorCode", "false")));
            showComponentPreconfigCheck.setSelected(Boolean.valueOf(advancedConfig.getProperty("showComponentPreconfigControls", "true")));
            String texturePackName = advancedConfig.getProperty("texturePack");
            if (texturePackName != null) {
                File texturePackFile = new File(texturePackName);
                if (texturePackFile.isFile()) {
                    textureChooser.setSelectedFile(texturePackFile);
                    texturePackLabel.setText(String.format(BUNDLE.getString("UI.TexturePackSpecific"), texturePackName));
                }
            }
            String csvFileName = advancedConfig.getProperty("csvFile");
            if (csvFileName != null) {
                csvChooser.setSelectedFile(new File (csvFileName));
                csvFileLabel.setText(csvFileName);
            }
            showComponentDetailButtonsCheckActionPerformed(null);
            enableGT508ComponentsCheckActionPerformed(null);
            enableGT509ComponentsCheckActionPerformed(null);
            showComponentPreconfigCheckActionPerformed(null);
        } catch (FileNotFoundException ex) {
            // ignore, this might just mean the file hasn't been created yet.
        } catch (IOException | NullPointerException ex) {
            // ignore, security settings or whatever preventing reading the xml file should not stop the planner from running.
        }
    }
    
    private void saveAdvancedConfig() {
        try (FileOutputStream configStream = new FileOutputStream("erpprefs.xml")) {
            advancedConfig.setProperty("showComponentDetailButtons", Boolean.toString(showComponentDetailButtonsCheck.isSelected()));
            advancedConfig.setProperty("enableGT508Components", Boolean.toString(enableGT508ComponentsCheck.isSelected()));
            advancedConfig.setProperty("enableGT509Components", Boolean.toString(enableGT509ComponentsCheck.isSelected()));
            advancedConfig.setProperty("showOldStyleReactorCode", Boolean.toString(showOldStyleReactorCodeCheck.isSelected()));
            advancedConfig.setProperty("showComponentPreconfigControls", Boolean.toString(showComponentPreconfigCheck.isSelected()));
            if (csvChooser.getSelectedFile() != null) {
                advancedConfig.setProperty("csvFile", csvChooser.getSelectedFile().getAbsolutePath());
            }
            advancedConfig.storeToXML(configStream, null);
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
        fuelRodUraniumButton.setToolTipText(bundle.getString("ComponentName.FuelRodUranium")); // NOI18N
        fuelRodUraniumButton.setActionCommand("fuelRodUranium"); // NOI18N
        componentsPanel.add(fuelRodUraniumButton);

        componentsGroup.add(dualFuelRodUraniumButton);
        dualFuelRodUraniumButton.setToolTipText(bundle.getString("ComponentName.DualFuelRodUranium")); // NOI18N
        dualFuelRodUraniumButton.setActionCommand("dualFuelRodUranium"); // NOI18N
        componentsPanel.add(dualFuelRodUraniumButton);

        componentsGroup.add(quadFuelRodUraniumButton);
        quadFuelRodUraniumButton.setToolTipText(bundle.getString("ComponentName.QuadFuelRodUranium")); // NOI18N
        quadFuelRodUraniumButton.setActionCommand("quadFuelRodUranium"); // NOI18N
        componentsPanel.add(quadFuelRodUraniumButton);

        componentsGroup.add(fuelRodMoxButton);
        fuelRodMoxButton.setToolTipText(bundle.getString("ComponentName.FuelRodMox")); // NOI18N
        fuelRodMoxButton.setActionCommand("fuelRodMox"); // NOI18N
        componentsPanel.add(fuelRodMoxButton);

        componentsGroup.add(dualFuelRodMoxButton);
        dualFuelRodMoxButton.setToolTipText(bundle.getString("ComponentName.DualFuelRodMox")); // NOI18N
        dualFuelRodMoxButton.setActionCommand("dualFuelRodMox"); // NOI18N
        componentsPanel.add(dualFuelRodMoxButton);

        componentsGroup.add(quadFuelRodMoxButton);
        quadFuelRodMoxButton.setToolTipText(bundle.getString("ComponentName.QuadFuelRodMox")); // NOI18N
        quadFuelRodMoxButton.setActionCommand("quadFuelRodMox"); // NOI18N
        componentsPanel.add(quadFuelRodMoxButton);

        componentsGroup.add(neutronReflectorButton);
        neutronReflectorButton.setToolTipText(bundle.getString("ComponentName.NeutronReflector")); // NOI18N
        neutronReflectorButton.setActionCommand("neutronReflector"); // NOI18N
        componentsPanel.add(neutronReflectorButton);

        componentsGroup.add(thickNeutronReflectorButton);
        thickNeutronReflectorButton.setToolTipText(bundle.getString("ComponentName.ThickNeutronReflector")); // NOI18N
        thickNeutronReflectorButton.setActionCommand("thickNeutronReflector"); // NOI18N
        componentsPanel.add(thickNeutronReflectorButton);

        componentsGroup.add(heatVentButton);
        heatVentButton.setToolTipText(bundle.getString("ComponentName.HeatVent")); // NOI18N
        heatVentButton.setActionCommand("heatVent"); // NOI18N
        componentsPanel.add(heatVentButton);

        componentsGroup.add(advancedHeatVentButton);
        advancedHeatVentButton.setToolTipText(bundle.getString("ComponentName.AdvancedHeatVent")); // NOI18N
        advancedHeatVentButton.setActionCommand("advancedHeatVent"); // NOI18N
        componentsPanel.add(advancedHeatVentButton);

        componentsGroup.add(reactorHeatVentButton);
        reactorHeatVentButton.setToolTipText(bundle.getString("ComponentName.ReactorHeatVent")); // NOI18N
        reactorHeatVentButton.setActionCommand("reactorHeatVent"); // NOI18N
        componentsPanel.add(reactorHeatVentButton);

        componentsGroup.add(componentHeatVentButton);
        componentHeatVentButton.setToolTipText(bundle.getString("ComponentName.ComponentHeatVent")); // NOI18N
        componentHeatVentButton.setActionCommand("componentHeatVent"); // NOI18N
        componentsPanel.add(componentHeatVentButton);

        componentsGroup.add(overclockedHeatVentButton);
        overclockedHeatVentButton.setToolTipText(bundle.getString("ComponentName.OverclockedHeatVent")); // NOI18N
        overclockedHeatVentButton.setActionCommand("overclockedHeatVent"); // NOI18N
        componentsPanel.add(overclockedHeatVentButton);

        componentsGroup.add(coolantCell10kButton);
        coolantCell10kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell10k")); // NOI18N
        coolantCell10kButton.setActionCommand("coolantCell10k"); // NOI18N
        componentsPanel.add(coolantCell10kButton);

        componentsGroup.add(coolantCell30kButton);
        coolantCell30kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell30k")); // NOI18N
        coolantCell30kButton.setActionCommand("coolantCell30k"); // NOI18N
        componentsPanel.add(coolantCell30kButton);

        componentsGroup.add(coolantCell60kButton);
        coolantCell60kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell60k")); // NOI18N
        coolantCell60kButton.setActionCommand("coolantCell60k"); // NOI18N
        componentsPanel.add(coolantCell60kButton);

        componentsGroup.add(heatExchangerButton);
        heatExchangerButton.setToolTipText(bundle.getString("ComponentName.HeatExchanger")); // NOI18N
        heatExchangerButton.setActionCommand("heatExchanger"); // NOI18N
        componentsPanel.add(heatExchangerButton);

        componentsGroup.add(advancedHeatExchangerButton);
        advancedHeatExchangerButton.setToolTipText(bundle.getString("ComponentName.AdvancedHeatExchanger")); // NOI18N
        advancedHeatExchangerButton.setActionCommand("advancedHeatExchanger"); // NOI18N
        componentsPanel.add(advancedHeatExchangerButton);

        componentsGroup.add(coreHeatExchangerButton);
        coreHeatExchangerButton.setToolTipText(bundle.getString("ComponentName.ReactorHeatExchanger")); // NOI18N
        coreHeatExchangerButton.setActionCommand("coreHeatExchanger"); // NOI18N
        componentsPanel.add(coreHeatExchangerButton);

        componentsGroup.add(componentHeatExchangerButton);
        componentHeatExchangerButton.setToolTipText(bundle.getString("ComponentName.ComponentHeatExchanger")); // NOI18N
        componentHeatExchangerButton.setActionCommand("componentHeatExchanger"); // NOI18N
        componentsPanel.add(componentHeatExchangerButton);

        componentsGroup.add(reactorPlatingButton);
        reactorPlatingButton.setToolTipText(bundle.getString("ComponentName.ReactorPlating")); // NOI18N
        reactorPlatingButton.setActionCommand("reactorPlating"); // NOI18N
        componentsPanel.add(reactorPlatingButton);

        componentsGroup.add(heatCapacityReactorPlatingButton);
        heatCapacityReactorPlatingButton.setToolTipText(bundle.getString("ComponentName.HeatCapacityReactorPlating")); // NOI18N
        heatCapacityReactorPlatingButton.setActionCommand("heatCapacityReactorPlating"); // NOI18N
        componentsPanel.add(heatCapacityReactorPlatingButton);

        componentsGroup.add(containmentReactorPlatingButton);
        containmentReactorPlatingButton.setToolTipText(bundle.getString("ComponentName.ContainmentReactorPlating")); // NOI18N
        containmentReactorPlatingButton.setActionCommand("containmentReactorPlating"); // NOI18N
        componentsPanel.add(containmentReactorPlatingButton);

        componentsGroup.add(rshCondensatorButton);
        rshCondensatorButton.setToolTipText(bundle.getString("ComponentName.RshCondensator")); // NOI18N
        rshCondensatorButton.setActionCommand("rshCondensator"); // NOI18N
        componentsPanel.add(rshCondensatorButton);

        componentsGroup.add(lzhCondensatorButton);
        lzhCondensatorButton.setToolTipText(bundle.getString("ComponentName.LzhCondensator")); // NOI18N
        lzhCondensatorButton.setActionCommand("lzhCondensator"); // NOI18N
        componentsPanel.add(lzhCondensatorButton);

        componentsGroup.add(fuelRodThoriumButton);
        fuelRodThoriumButton.setToolTipText(bundle.getString("ComponentName.FuelRodThorium")); // NOI18N
        fuelRodThoriumButton.setActionCommand("fuelRodThorium"); // NOI18N
        componentsPanel.add(fuelRodThoriumButton);

        componentsGroup.add(dualFuelRodThoriumButton);
        dualFuelRodThoriumButton.setToolTipText(bundle.getString("ComponentName.DualFuelRodThorium")); // NOI18N
        dualFuelRodThoriumButton.setActionCommand("dualFuelRodThorium"); // NOI18N
        componentsPanel.add(dualFuelRodThoriumButton);

        componentsGroup.add(quadFuelRodThoriumButton);
        quadFuelRodThoriumButton.setToolTipText(bundle.getString("ComponentName.QuadFuelRodThorium")); // NOI18N
        quadFuelRodThoriumButton.setActionCommand("quadFuelRodThorium"); // NOI18N
        componentsPanel.add(quadFuelRodThoriumButton);

        componentsGroup.add(coolantCellHelium60kButton);
        coolantCellHelium60kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell60kHelium")); // NOI18N
        coolantCellHelium60kButton.setActionCommand("coolantCellHelium60k"); // NOI18N
        componentsPanel.add(coolantCellHelium60kButton);

        componentsGroup.add(coolantCellHelium180kButton);
        coolantCellHelium180kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell180kHelium")); // NOI18N
        coolantCellHelium180kButton.setActionCommand("coolantCellHelium180k"); // NOI18N
        componentsPanel.add(coolantCellHelium180kButton);

        componentsGroup.add(coolantCellHelium360kButton);
        coolantCellHelium360kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell360kHelium")); // NOI18N
        coolantCellHelium360kButton.setActionCommand("coolantCellHelium360k"); // NOI18N
        componentsPanel.add(coolantCellHelium360kButton);

        componentsGroup.add(coolantCellNak60kButton);
        coolantCellNak60kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell60kNak")); // NOI18N
        coolantCellNak60kButton.setActionCommand("coolantCellNak60k"); // NOI18N
        componentsPanel.add(coolantCellNak60kButton);

        componentsGroup.add(coolantCellNak180kButton);
        coolantCellNak180kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell180kNak")); // NOI18N
        coolantCellNak180kButton.setActionCommand("coolantCellNak180k"); // NOI18N
        componentsPanel.add(coolantCellNak180kButton);

        componentsGroup.add(coolantCellNak360kButton);
        coolantCellNak360kButton.setToolTipText(bundle.getString("ComponentName.CoolantCell360kNak")); // NOI18N
        coolantCellNak360kButton.setActionCommand("coolantCellNak360k"); // NOI18N
        componentsPanel.add(coolantCellNak360kButton);

        componentsGroup.add(iridiumNeutronReflectorButton);
        iridiumNeutronReflectorButton.setToolTipText(bundle.getString("ComponentName.IridiumNeutronReflector")); // NOI18N
        iridiumNeutronReflectorButton.setActionCommand("iridiumNeutronReflector"); // NOI18N
        componentsPanel.add(iridiumNeutronReflectorButton);

        componentsGroup.add(fuelRodNaquadahButton);
        fuelRodNaquadahButton.setToolTipText(bundle.getString("ComponentName.FuelRodNaquadah")); // NOI18N
        fuelRodNaquadahButton.setActionCommand("fuelRodNaquadah"); // NOI18N
        componentsPanel.add(fuelRodNaquadahButton);

        componentsGroup.add(dualFuelRodNaquadahButton);
        dualFuelRodNaquadahButton.setToolTipText(bundle.getString("ComponentName.DualFuelRodNaquadah")); // NOI18N
        dualFuelRodNaquadahButton.setActionCommand("dualFuelRodNaquadah"); // NOI18N
        componentsPanel.add(dualFuelRodNaquadahButton);

        componentsGroup.add(quadFuelRodNaquadahButton);
        quadFuelRodNaquadahButton.setToolTipText(bundle.getString("ComponentName.QuadFuelRodNaquadah")); // NOI18N
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

        versionLabel.setText(String.format(BUNDLE.getString("UI.VersionNumber"), VERSION));
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
        gridBagConstraints.weighty = 1.0;
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
        gridBagConstraints.weighty = 1.0;
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 2, 2);
        advancedPanel.add(jLabel5, gridBagConstraints);

        advancedScroll.setViewportView(advancedPanel);

        outputTabs.addTab(bundle.getString("UI.AdvancedTab"), advancedScroll); // NOI18N

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
        maxHeatLabel.setText(String.format(BUNDLE.getString("UI.MaxHeatSpecific"), reactor.getMaxHeat()));
        heatSpinnerModel.setMaximum(reactor.getMaxHeat() - 1);
        heatSpinnerModel.setValue(Math.min(((Number) heatSpinnerModel.getValue()).intValue(), reactor.getMaxHeat() - 1));
        temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
        lockCode = true;
        codeField.setText(null);
        lockCode = false;
    }//GEN-LAST:event_clearGridButtonActionPerformed

    private void simulateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulateButtonActionPerformed
        if (simulator != null) {
            simulator.cancel(true);
        }
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
        simulator.execute();        
    }//GEN-LAST:event_simulateButtonActionPerformed
    
    /**
     * The reactor that was last simulated.
     */
    private Reactor simulatedReactor = null;

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
            codeField.setText(code);
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(ReactorPlannerFrame.class.getName()).log(Level.SEVERE, null, ex);
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
                outputTabs.insertTab(BUNDLE.getString("UI.PulseConfigurationTab"), null, pulsePanel, null, outputTabs.indexOfComponent(outputPane) + 1);
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
                int result = JOptionPane.showConfirmDialog(this, BUNDLE.getString("UI.RemoveGT508ComponentsText"), BUNDLE.getString("UI.RemoveAddonComponentsTitle"), JOptionPane.YES_NO_OPTION);
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
                int result = JOptionPane.showConfirmDialog(this, BUNDLE.getString("UI.RemoveGT509ComponentsText"), BUNDLE.getString("UI.RemoveAddonComponentsTitle"), JOptionPane.YES_NO_OPTION);
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
            advancedConfig.setProperty("texturePack", textureChooser.getSelectedFile().getAbsolutePath());
            texturePackLabel.setText(String.format(BUNDLE.getString("UI.TexturePackSpecific"), textureChooser.getSelectedFile().getAbsolutePath()));
            saveAdvancedConfig();
        }
    }//GEN-LAST:event_texturePackBrowseButtonActionPerformed

    private void texturePackClearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_texturePackClearButtonActionPerformed
        advancedConfig.remove("texturePack");
        texturePackLabel.setText(BUNDLE.getString("UI.TexturePackDefault"));
        saveAdvancedConfig();
    }//GEN-LAST:event_texturePackClearButtonActionPerformed
    
    private SwingWorker<Void, String> simulator;

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
        maxHeatLabel.setText(String.format(BUNDLE.getString("UI.MaxHeatSpecific"), reactor.getMaxHeat()));
        heatSpinnerModel.setMaximum(reactor.getMaxHeat() - 1);
        heatSpinnerModel.setValue(Math.min(((Number) heatSpinnerModel.getValue()).intValue(), reactor.getMaxHeat() - 1));
        temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {

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
    private javax.swing.JRadioButton fluidReactorRadio;
    private javax.swing.JToggleButton fuelRodMoxButton;
    private javax.swing.JToggleButton fuelRodNaquadahButton;
    private javax.swing.JToggleButton fuelRodThoriumButton;
    private javax.swing.JToggleButton fuelRodUraniumButton;
    private javax.swing.JToggleButton heatCapacityReactorPlatingButton;
    private javax.swing.JToggleButton heatExchangerButton;
    private javax.swing.JSpinner heatSpinner;
    private javax.swing.JToggleButton heatVentButton;
    private javax.swing.JToggleButton iridiumNeutronReflectorButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToggleButton lzhCondensatorButton;
    private javax.swing.JTextArea materialsArea;
    private javax.swing.JScrollPane materialsListPane;
    private javax.swing.JLabel maxHeatLabel;
    private javax.swing.JLabel maxSimulationTicksLabel;
    private javax.swing.JSpinner maxSimulationTicksSpinner;
    private javax.swing.JToggleButton neutronReflectorButton;
    private javax.swing.JSpinner offPulseSpinner;
    private javax.swing.JSpinner onPulseSpinner;
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
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
