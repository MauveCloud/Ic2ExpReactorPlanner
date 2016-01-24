/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Ic2ExpReactorPlanner.S._;

/**
 * JFrame to display a gui for planning a Nuclear Reactor using IndustrialCraft2 Experimental.
 * @author Brian McCloud
 */
public class ReactorPlannerFrame extends javax.swing.JFrame {

    private final Reactor reactor = new Reactor();
    
    private final JButton[][] reactorButtons = new JButton[6][9];
    
    private final JPanel[][] reactorButtonPanels = new JPanel[6][9];
    
    private int selectedRow = -1;
    
    private int selectedColumn = -1;
    
    private boolean changingCode = false;
    
    /**
     * Creates new form ReactorPlannerFrame
     */
    public ReactorPlannerFrame() {
        initComponents();
        for (int row = 0; row < reactorButtons.length; row++) {
            final int finalRow = row;
            for (int col = 0; col < reactorButtons[row].length; col++) {
                final int finalCol = col;
                reactorButtonPanels[row][col] = new JPanel();
                reactorButtonPanels[row][col].setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.lightGray, Color.darkGray));
                reactorButtonPanels[row][col].setLayout(new GridBagLayout());
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.weightx = 0;
                constraints.weighty = 0;
                JButton automationButton = new JButton("a");
                automationButton.setFont(Font.decode("Arial 10"));
                automationButton.setMargin(new Insets(-2, 0, -2, 0));
                automationButton.setToolTipText(_("Click to define automation rules for this component."));
                automationButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        final ReactorComponent component = reactor.getComponentAt(finalRow, finalCol);
                        selectedRow = finalRow;
                        selectedColumn = finalCol;
                        if (component == null) {
                            selectedComponentLabel.setText(String.format(_("No component at row %d column %d."), finalRow, finalCol));
                        } else {
                            selectedComponentLabel.setText(String.format(_("%s at row %d column %d\n"), component.toString(), finalRow, finalCol));
                            thresholdSpinner.setValue(component.automationThreshold);
                            pauseSpinner.setValue(component.reactorPause);
                        }
                        outputTabs.setSelectedIndex(4);
                    }
                });
                reactorButtonPanels[row][col].add(automationButton, constraints);
                reactorButtonPanels[row][col].add(new JLabel(), constraints);
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                constraints.anchor = GridBagConstraints.EAST;
                JButton infoButton = new JButton("i");
                infoButton.setFont(Font.decode("Arial 10"));
                infoButton.setMargin(new Insets(-2, 0, -2, 0));
                infoButton.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (simulatedReactor != null) {
                            final ReactorComponent component = simulatedReactor.getComponentAt(finalRow, finalCol);
                            if (component == null) {
                                componentArea.setText(String.format(_("No component at row %d column %d during last simulation."), finalRow, finalCol));
                            } else {
                                componentArea.setText(String.format(_("%s at row %d column %d\n%s"), component.toString(), finalRow, finalCol, component.info));
                            }
                        } else {
                            componentArea.setText(_("No simulation run yet."));
                        }
                        outputTabs.setSelectedIndex(2);
                    }
                });
                infoButton.setToolTipText(_("Click for information about this component"));
                reactorButtonPanels[row][col].add(infoButton, constraints);
                constraints.weightx = 1.0;
                constraints.weighty = 1.0;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                reactorButtons[row][col] = new JButton();
                reactorButtons[row][col].addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ReactorComponent componentToPlace = null;
                        final ButtonModel selection = componentsGroup.getSelection();
                        if (selection != null) {
                            componentToPlace = ComponentFactory.createComponent(selection.getActionCommand());
                            if (componentToPlace != null) {
                                componentToPlace.setInitialHeat(((Number)componentHeatSpinner.getValue()).intValue());
                            }
                        }
                        reactor.setComponentAt(finalRow, finalCol, componentToPlace);
                        materialsArea.setText(reactor.getMaterials().toString());
                        maxHeatLabel.setText(String.format("/%,.0f", reactor.getMaxHeat()));
                        temperatureEffectsLabel.setText(String.format(_("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d"), (int) (reactor.getMaxHeat() * 0.4), (int) (reactor.getMaxHeat() * 0.5), (int) (reactor.getMaxHeat() * 0.7), (int) (reactor.getMaxHeat() * 0.85), (int) (reactor.getMaxHeat() * 1.0)));
                        SpinnerModel model = heatSpinner.getModel();
                        if (model instanceof SpinnerNumberModel) {
                            ((SpinnerNumberModel)model).setMaximum(reactor.getMaxHeat());
                        }
                        int buttonSize = Math.min(reactorButtons[finalRow][finalCol].getWidth(), reactorButtons[finalRow][finalCol].getHeight());
                        if (buttonSize > 2 && componentToPlace != null) {
                            reactorButtons[finalRow][finalCol].setIcon(new ImageIcon(componentToPlace.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
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
                        changingCode = true;
                        codeField.setText(reactor.getCode());
                        changingCode = false;
                    }
                });

                reactorButtons[row][col].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() != MouseEvent.BUTTON1) {
                            reactor.setComponentAt(finalRow, finalCol, null);
                            materialsArea.setText(reactor.getMaterials().toString());
                            maxHeatLabel.setText(String.format("/%,.0f", reactor.getMaxHeat()));
                            temperatureEffectsLabel.setText(String.format(_("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d"), (int) (reactor.getMaxHeat() * 0.4), (int) (reactor.getMaxHeat() * 0.5), (int) (reactor.getMaxHeat() * 0.7), (int) (reactor.getMaxHeat() * 0.85), (int) (reactor.getMaxHeat() * 1.0)));
                            SpinnerModel model = heatSpinner.getModel();
                            if (model instanceof SpinnerNumberModel) {
                                ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
                            }
                            reactorButtons[finalRow][finalCol].setIcon(null);
                            reactorButtons[finalRow][finalCol].setToolTipText(null);
                            reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                            changingCode = true;
                            codeField.setText(reactor.getCode());
                            changingCode = false;
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
                if (!changingCode) {
                    reactor.setCode(codeField.getText());
                    updateReactorButtons();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!changingCode) {
                    reactor.setCode(codeField.getText());
                    updateReactorButtons();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!changingCode) {
                    reactor.setCode(codeField.getText());
                    updateReactorButtons();
                }
            }
        });
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
        pulseTypeGroup = new javax.swing.ButtonGroup();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        resourcePackItem = new javax.swing.JMenuItem();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        reactorPanel = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
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
        jLabel5 = new javax.swing.JLabel();
        componentHeatSpinner = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        euReactorRadio = new javax.swing.JRadioButton();
        fluidReactorRadio = new javax.swing.JRadioButton();
        clearGridButton = new javax.swing.JButton();
        simulateButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        heatSpinner = new javax.swing.JSpinner();
        maxHeatLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        codeField = new javax.swing.JTextField();
        copyCodeButton = new javax.swing.JButton();
        pasteCodeButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        simulationStyleCombo = new javax.swing.JComboBox<String>();
        outputTabs = new javax.swing.JTabbedPane();
        outputPane = new javax.swing.JScrollPane();
        outputArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        materialsArea = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        componentArea = new javax.swing.JTextArea();
        pulsePanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        onPulseSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        offPulseSpinner = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        suspendTempSpinner = new javax.swing.JSpinner();
        jLabel10 = new javax.swing.JLabel();
        resumeTempSpinner = new javax.swing.JSpinner();
        automationPanel = new javax.swing.JPanel();
        selectedComponentLabel = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        thresholdSpinner = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        pauseSpinner = new javax.swing.JSpinner();
        jLabel15 = new javax.swing.JLabel();

        fileMenu.setText(_("File"));

        resourcePackItem.setText(_("Choose Resource Pack"));
        resourcePackItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resourcePackItemActionPerformed(evt);
            }
        });
        fileMenu.add(resourcePackItem);

        jMenuBar1.add(fileMenu);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(_("IC2 Experimental Reactor Planner"));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                plannerResized(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.8);

        jSplitPane2.setResizeWeight(0.75);

        reactorPanel.setMinimumSize(new java.awt.Dimension(180, 120));
        reactorPanel.setPreferredSize(new java.awt.Dimension(180, 120));
        reactorPanel.setLayout(new java.awt.GridLayout(6, 9, 2, 2));
        jSplitPane2.setLeftComponent(reactorPanel);

        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setResizeWeight(0.7);

        temperatureAndComponentsPanel.setLayout(new java.awt.GridBagLayout());

        temperatureEffectsLabel.setText(_("Burn: 4,000  Evaporate: 5,000  Hurt: 7,000  Lava: 8,500  Explode: 10,000"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(temperatureEffectsLabel, gridBagConstraints);

        componentsPanel.setMinimumSize(new java.awt.Dimension(160, 80));
        componentsPanel.setPreferredSize(new java.awt.Dimension(160, 80));
        componentsPanel.setLayout(new java.awt.GridLayout(4, 9));

        componentsGroup.add(emptyButton);
        emptyButton.setActionCommand("empty");
        componentsPanel.add(emptyButton);

        componentsGroup.add(fuelRodUraniumButton);
        fuelRodUraniumButton.setToolTipText(_("Fuel Rod (Uranium)"));
        fuelRodUraniumButton.setActionCommand("fuelRodUranium");
        componentsPanel.add(fuelRodUraniumButton);

        componentsGroup.add(dualFuelRodUraniumButton);
        dualFuelRodUraniumButton.setToolTipText(_("Dual Fuel Rod (Uranium)"));
        dualFuelRodUraniumButton.setActionCommand("dualFuelRodUranium");
        componentsPanel.add(dualFuelRodUraniumButton);

        componentsGroup.add(quadFuelRodUraniumButton);
        quadFuelRodUraniumButton.setToolTipText(_("Quad Fuel Rod (Uranium)"));
        quadFuelRodUraniumButton.setActionCommand("quadFuelRodUranium");
        componentsPanel.add(quadFuelRodUraniumButton);

        componentsGroup.add(fuelRodMoxButton);
        fuelRodMoxButton.setToolTipText(_("Fuel Rod (MOX)"));
        fuelRodMoxButton.setActionCommand("fuelRodMox");
        componentsPanel.add(fuelRodMoxButton);

        componentsGroup.add(dualFuelRodMoxButton);
        dualFuelRodMoxButton.setToolTipText(_("Dual Fuel Rod (MOX)"));
        dualFuelRodMoxButton.setActionCommand("dualFuelRodMox");
        componentsPanel.add(dualFuelRodMoxButton);

        componentsGroup.add(quadFuelRodMoxButton);
        quadFuelRodMoxButton.setToolTipText(_("Quad Fuel Rod (MOX)"));
        quadFuelRodMoxButton.setActionCommand("quadFuelRodMox");
        componentsPanel.add(quadFuelRodMoxButton);

        componentsGroup.add(neutronReflectorButton);
        neutronReflectorButton.setToolTipText(_("Neutron Reflector"));
        neutronReflectorButton.setActionCommand("neutronReflector");
        componentsPanel.add(neutronReflectorButton);

        componentsGroup.add(thickNeutronReflectorButton);
        thickNeutronReflectorButton.setToolTipText(_("Thick Neutron Reflector"));
        thickNeutronReflectorButton.setActionCommand("thickNeutronReflector");
        componentsPanel.add(thickNeutronReflectorButton);

        componentsGroup.add(heatVentButton);
        heatVentButton.setToolTipText(_("Heat Vent"));
        heatVentButton.setActionCommand("heatVent");
        componentsPanel.add(heatVentButton);

        componentsGroup.add(advancedHeatVentButton);
        advancedHeatVentButton.setToolTipText(_("Advanced Heat Vent"));
        advancedHeatVentButton.setActionCommand("advancedHeatVent");
        componentsPanel.add(advancedHeatVentButton);

        componentsGroup.add(reactorHeatVentButton);
        reactorHeatVentButton.setToolTipText(_("Reactor Heat Vent"));
        reactorHeatVentButton.setActionCommand("reactorHeatVent");
        componentsPanel.add(reactorHeatVentButton);

        componentsGroup.add(componentHeatVentButton);
        componentHeatVentButton.setToolTipText(_("Component Heat Vent"));
        componentHeatVentButton.setActionCommand("componentHeatVent");
        componentsPanel.add(componentHeatVentButton);

        componentsGroup.add(overclockedHeatVentButton);
        overclockedHeatVentButton.setToolTipText(_("Overclocked Heat Vent"));
        overclockedHeatVentButton.setActionCommand("overclockedHeatVent");
        componentsPanel.add(overclockedHeatVentButton);

        componentsGroup.add(coolantCell10kButton);
        coolantCell10kButton.setToolTipText(_("10k Coolant Cell"));
        coolantCell10kButton.setActionCommand("coolantCell10k");
        componentsPanel.add(coolantCell10kButton);

        componentsGroup.add(coolantCell30kButton);
        coolantCell30kButton.setToolTipText(_("30k Coolant Cell"));
        coolantCell30kButton.setActionCommand("coolantCell30k");
        componentsPanel.add(coolantCell30kButton);

        componentsGroup.add(coolantCell60kButton);
        coolantCell60kButton.setToolTipText(_("60k Coolant Cell"));
        coolantCell60kButton.setActionCommand("coolantCell60k");
        componentsPanel.add(coolantCell60kButton);

        componentsGroup.add(heatExchangerButton);
        heatExchangerButton.setToolTipText(_("Heat Exchanger"));
        heatExchangerButton.setActionCommand("heatExchanger");
        componentsPanel.add(heatExchangerButton);

        componentsGroup.add(advancedHeatExchangerButton);
        advancedHeatExchangerButton.setToolTipText(_("Advanced Heat Exchanger"));
        advancedHeatExchangerButton.setActionCommand("advancedHeatExchanger");
        componentsPanel.add(advancedHeatExchangerButton);

        componentsGroup.add(coreHeatExchangerButton);
        coreHeatExchangerButton.setToolTipText(_("Core Heat Exchanger"));
        coreHeatExchangerButton.setActionCommand("coreHeatExchanger");
        componentsPanel.add(coreHeatExchangerButton);

        componentsGroup.add(componentHeatExchangerButton);
        componentHeatExchangerButton.setToolTipText(_("Component Heat Exchanger"));
        componentHeatExchangerButton.setActionCommand("componentHeatExchanger");
        componentsPanel.add(componentHeatExchangerButton);

        componentsGroup.add(reactorPlatingButton);
        reactorPlatingButton.setToolTipText(_("Reactor Plating"));
        reactorPlatingButton.setActionCommand("reactorPlating");
        componentsPanel.add(reactorPlatingButton);

        componentsGroup.add(heatCapacityReactorPlatingButton);
        heatCapacityReactorPlatingButton.setToolTipText(_("Heat-Capacity Reactor Plating"));
        heatCapacityReactorPlatingButton.setActionCommand("heatCapacityReactorPlating");
        componentsPanel.add(heatCapacityReactorPlatingButton);

        componentsGroup.add(containmentReactorPlatingButton);
        containmentReactorPlatingButton.setToolTipText(_("Containment Reactor Plating"));
        containmentReactorPlatingButton.setActionCommand("containmentReactorPlating");
        componentsPanel.add(containmentReactorPlatingButton);

        componentsGroup.add(rshCondensatorButton);
        rshCondensatorButton.setToolTipText(_("RSH-Condensator"));
        rshCondensatorButton.setActionCommand("rshCondensator");
        componentsPanel.add(rshCondensatorButton);

        componentsGroup.add(lzhCondensatorButton);
        lzhCondensatorButton.setToolTipText(_("LZH-Condensator"));
        lzhCondensatorButton.setActionCommand("lzhCondensator");
        componentsPanel.add(lzhCondensatorButton);

        componentsGroup.add(fuelRodThoriumButton);
        fuelRodThoriumButton.setToolTipText(_("Fuel Rod (Thorium)"));
        fuelRodThoriumButton.setActionCommand("fuelRodThorium");
        componentsPanel.add(fuelRodThoriumButton);

        componentsGroup.add(dualFuelRodThoriumButton);
        dualFuelRodThoriumButton.setToolTipText(_("Double Fuel Rod (Thorium)"));
        dualFuelRodThoriumButton.setActionCommand("dualFuelRodThorium");
        componentsPanel.add(dualFuelRodThoriumButton);

        componentsGroup.add(quadFuelRodThoriumButton);
        quadFuelRodThoriumButton.setToolTipText(_("Quad Fuel Rod (Thorium)"));
        quadFuelRodThoriumButton.setActionCommand("quadFuelRodThorium");
        componentsPanel.add(quadFuelRodThoriumButton);

        componentsGroup.add(coolantCellHelium60kButton);
        coolantCellHelium60kButton.setToolTipText(_("60k He Coolant Cell"));
        coolantCellHelium60kButton.setActionCommand("coolantCellHelium60k");
        componentsPanel.add(coolantCellHelium60kButton);

        componentsGroup.add(coolantCellHelium180kButton);
        coolantCellHelium180kButton.setToolTipText(_("180k He Coolant Cell"));
        coolantCellHelium180kButton.setActionCommand("coolantCellHelium180k");
        componentsPanel.add(coolantCellHelium180kButton);

        componentsGroup.add(coolantCellHelium360kButton);
        coolantCellHelium360kButton.setToolTipText(_("360k He Coolant Cell"));
        coolantCellHelium360kButton.setActionCommand("coolantCellHelium360k");
        componentsPanel.add(coolantCellHelium360kButton);

        componentsGroup.add(coolantCellNak60kButton);
        coolantCellNak60kButton.setToolTipText(_("60k NaK Coolant Cell"));
        coolantCellNak60kButton.setActionCommand("coolantCellNak60k");
        componentsPanel.add(coolantCellNak60kButton);

        componentsGroup.add(coolantCellNak180kButton);
        coolantCellNak180kButton.setToolTipText(_("180k NaK Coolant Cell"));
        coolantCellNak180kButton.setActionCommand("coolantCellNak180k");
        componentsPanel.add(coolantCellNak180kButton);

        componentsGroup.add(coolantCellNak360kButton);
        coolantCellNak360kButton.setToolTipText(_("360k NaK Coolant Cell"));
        coolantCellNak360kButton.setActionCommand("coolantCellNak360k");
        componentsPanel.add(coolantCellNak360kButton);

        componentsGroup.add(iridiumNeutronReflectorButton);
        iridiumNeutronReflectorButton.setToolTipText(_("Iridium Neutron Reflector"));
        iridiumNeutronReflectorButton.setActionCommand("iridiumNeutronReflector");
        componentsPanel.add(iridiumNeutronReflectorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(componentsPanel, gridBagConstraints);

        jLabel5.setText(_("Initial Component Heat:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(jLabel5, gridBagConstraints);

        componentHeatSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 360000, 1));
        componentHeatSpinner.setMinimumSize(new java.awt.Dimension(70, 20));
        componentHeatSpinner.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(componentHeatSpinner, gridBagConstraints);

        jSplitPane3.setTopComponent(temperatureAndComponentsPanel);

        jPanel1.setMinimumSize(new java.awt.Dimension(392, 151));
        jPanel1.setPreferredSize(new java.awt.Dimension(392, 151));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        reactorStyleGroup.add(euReactorRadio);
        euReactorRadio.setSelected(true);
        euReactorRadio.setText(_("EU Reactor"));
        euReactorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                euReactorRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(euReactorRadio, gridBagConstraints);

        reactorStyleGroup.add(fluidReactorRadio);
        fluidReactorRadio.setText(_("Fluid Reactor"));
        fluidReactorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fluidReactorRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(fluidReactorRadio, gridBagConstraints);

        clearGridButton.setText(_("Clear Grid"));
        clearGridButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearGridButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel1.add(clearGridButton, gridBagConstraints);

        simulateButton.setText(_("Simulate"));
        simulateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel1.add(simulateButton, gridBagConstraints);

        jLabel1.setText(_("Initial Reactor Heat:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jLabel1, gridBagConstraints);

        heatSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 10000.0d, 1.0d));
        heatSpinner.setMinimumSize(new java.awt.Dimension(70, 20));
        heatSpinner.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(heatSpinner, gridBagConstraints);

        maxHeatLabel.setText("/10,000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(maxHeatLabel, gridBagConstraints);

        jLabel2.setText(_("Code:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jLabel2, gridBagConstraints);

        codeField.setMaximumSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(codeField, gridBagConstraints);

        copyCodeButton.setText(_("Copy Code"));
        copyCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyCodeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(copyCodeButton, gridBagConstraints);

        pasteCodeButton.setText(_("Paste Code"));
        pasteCodeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteCodeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(pasteCodeButton, gridBagConstraints);

        jLabel6.setText(_("Simulation Style:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 0.5;
        jPanel1.add(jLabel6, gridBagConstraints);

        simulationStyleCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { _("Simple Cycle"), _("Pulsed Cycle"), _("Pulsed Automation") }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(simulationStyleCombo, gridBagConstraints);

        jSplitPane3.setBottomComponent(jPanel1);

        jSplitPane2.setRightComponent(jSplitPane3);

        jSplitPane1.setLeftComponent(jSplitPane2);

        outputArea.setEditable(false);
        outputArea.setColumns(20);
        outputArea.setRows(5);
        outputPane.setViewportView(outputArea);

        outputTabs.addTab(_("Simulation"), outputPane);

        materialsArea.setEditable(false);
        materialsArea.setColumns(20);
        materialsArea.setRows(5);
        jScrollPane2.setViewportView(materialsArea);

        outputTabs.addTab(_("Materials"), jScrollPane2);

        componentArea.setColumns(20);
        componentArea.setRows(5);
        jScrollPane1.setViewportView(componentArea);

        outputTabs.addTab(_("Component"), jScrollPane1);

        pulsePanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText(_("On-pulse duration:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel3, gridBagConstraints);

        onPulseSpinner.setModel(new javax.swing.SpinnerNumberModel(30, 0, 5000000, 1));
        onPulseSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        onPulseSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(onPulseSpinner, gridBagConstraints);

        jLabel4.setText(_("seconds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel4, gridBagConstraints);

        jLabel7.setText(_("Off-pulse duration:"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 2, 2);
        pulsePanel.add(jLabel7, gridBagConstraints);

        offPulseSpinner.setModel(new javax.swing.SpinnerNumberModel(30, 0, 5000000, 1));
        offPulseSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        offPulseSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(offPulseSpinner, gridBagConstraints);

        jLabel8.setText(_("seconds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel8, gridBagConstraints);

        jLabel11.setText(_("(on-pulse can be set to 5 million to mimic having no redstone timing)"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel11, gridBagConstraints);

        jLabel9.setText(_("Suspend when reactor temp >="));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(jLabel9, gridBagConstraints);

        suspendTempSpinner.setModel(new javax.swing.SpinnerNumberModel(8400, 0, 100000, 1));
        suspendTempSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        suspendTempSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(suspendTempSpinner, gridBagConstraints);

        jLabel10.setText(_("Resume when reactor temp <="));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 2, 2);
        pulsePanel.add(jLabel10, gridBagConstraints);

        resumeTempSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100000, 1));
        resumeTempSpinner.setMinimumSize(new java.awt.Dimension(80, 20));
        resumeTempSpinner.setPreferredSize(new java.awt.Dimension(80, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        pulsePanel.add(resumeTempSpinner, gridBagConstraints);

        outputTabs.addTab(_("Pulse Configuration"), pulsePanel);

        automationPanel.setLayout(new java.awt.GridBagLayout());

        selectedComponentLabel.setText(_("No component selected"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(selectedComponentLabel, gridBagConstraints);

        jLabel12.setText(_("Replacement Threshold:"));
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

        jLabel13.setText(_("<html>(Set higher than component's initial heat/damage to replace the component when it gets this hot/damaged, or lower to replace a component that has cooled; broken components will be replaced during automation runs regardless of this setting)</html>"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(jLabel13, gridBagConstraints);

        jLabel14.setText(_("Reactor Pause (seconds):"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(jLabel14, gridBagConstraints);

        pauseSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 0, 10000, 1));
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

        jLabel15.setText(_("<html>(This is how long the reactor will pause while replacing this component)</html>"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        automationPanel.add(jLabel15, gridBagConstraints);

        outputTabs.addTab(_("Component Automation"), automationPanel);

        jSplitPane1.setRightComponent(outputTabs);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(jSplitPane1, gridBagConstraints);

        setSize(new java.awt.Dimension(1109, 760));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void plannerResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_plannerResized
        Enumeration<AbstractButton> elements = componentsGroup.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = elements.nextElement();
            int buttonSize = Math.min(button.getWidth(), button.getHeight());
            if (buttonSize > 2) {
                final ReactorComponent component = ComponentFactory.getDefaultComponent(button.getActionCommand());
                if (component != null && component.getImage() != null) {
                    button.setIcon(new ImageIcon(component.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
                } else {
                    button.setIcon(null);
                }
            }
        }
        for (int row = 0; row < reactorButtons.length; row++) {
            for (int col = 0; col < reactorButtons[row].length; col++) {
                int buttonSize = Math.min(reactorButtons[row][col].getWidth(), reactorButtons[row][col].getHeight());
                if (buttonSize > 2) {
                    final ReactorComponent component = reactor.getComponentAt(row, col);
                    if (component != null) {
                        reactorButtons[row][col].setIcon(new ImageIcon(component.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
                    } else {
                        reactorButtons[row][col].setIcon(null);
                    }
                }
            }
        }
    }//GEN-LAST:event_plannerResized

    private void resourcePackItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resourcePackItemActionPerformed
//        String resourcePackPath = Preferences.userRoot().get("Ic2ExpReactorPlanner.ResourcePack", null);
//        JFileChooser chooser = new JFileChooser(resourcePackPath);
//        chooser.setFileFilter(new FileNameExtensionFilter("Resource Packs", "jar", "zip"));
//        int result = chooser.showOpenDialog(this);
//        if (result == JFileChooser.APPROVE_OPTION) {
//            resourcePackPath = chooser.getSelectedFile().getAbsolutePath();
//            Preferences.userRoot().put("Ic2ExpReactorPlanner.ResourcePack", resourcePackPath);
//            plannerResized(null);
//        }
    }//GEN-LAST:event_resourcePackItemActionPerformed

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
        maxHeatLabel.setText(String.format("/%,.0f", reactor.getMaxHeat()));
        temperatureEffectsLabel.setText(String.format(_("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d"), (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
        SpinnerModel model = heatSpinner.getModel();
        if (model instanceof SpinnerNumberModel) {
            ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
        }
        changingCode = true;
        codeField.setText(null);
        changingCode = false;
    }//GEN-LAST:event_clearGridButtonActionPerformed

    private void simulateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulateButtonActionPerformed
        SpinnerModel model = heatSpinner.getModel();
        if (model instanceof SpinnerNumberModel) {
            ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
        }
        if (simulator != null) {
            simulator.cancel(true);
        }
        int initialHeat = 0;
        Object value = heatSpinner.getValue();
        if (value instanceof Number) {
            initialHeat = ((Number) value).intValue();
        }
        simulatedReactor = new Reactor();
        simulatedReactor.setCode(reactor.getCode());
        simulatedReactor.setFluid(reactor.isFluid());
        outputTabs.setSelectedIndex(0);
        if (_("Simple Cycle").equals(simulationStyleCombo.getSelectedItem().toString())) {
            simulator = new SimpleSimulator(simulatedReactor, outputArea, reactorButtonPanels, initialHeat);
            simulator.execute();
        } else if (_("Pulsed Cycle").equals(simulationStyleCombo.getSelectedItem().toString())) {
            int onPulseDuration = 30;
            int offPulseDuration = 30;
            int suspendTemp = 8400;
            int resumeTemp = 0;
            value = onPulseSpinner.getValue();
            if (value instanceof Number) {
                onPulseDuration = ((Number) value).intValue();
            }
            value = offPulseSpinner.getValue();
            if (value instanceof Number) {
                offPulseDuration = ((Number) value).intValue();
            }
            value = suspendTempSpinner.getValue();
            if (value instanceof Number) {
                suspendTemp = ((Number) value).intValue();
            }
            value = resumeTempSpinner.getValue();
            if (value instanceof Number) {
                resumeTemp = ((Number) value).intValue();
            }
            simulator = new PulsedSimulator(simulatedReactor, outputArea, reactorButtonPanels, initialHeat, onPulseDuration, offPulseDuration, suspendTemp, resumeTemp);
            simulator.execute();
        } else if (_("Pulsed Automation").equals(simulationStyleCombo.getSelectedItem().toString())) {
            int onPulseDuration = 30;
            int offPulseDuration = 30;
            int suspendTemp = 8400;
            int resumeTemp = 0;
            value = onPulseSpinner.getValue();
            if (value instanceof Number) {
                onPulseDuration = ((Number) value).intValue();
            }
            value = offPulseSpinner.getValue();
            if (value instanceof Number) {
                offPulseDuration = ((Number) value).intValue();
            }
            value = suspendTempSpinner.getValue();
            if (value instanceof Number) {
                suspendTemp = ((Number) value).intValue();
            }
            value = resumeTempSpinner.getValue();
            if (value instanceof Number) {
                resumeTemp = ((Number) value).intValue();
            }
            simulator = new AutomationSimulator(simulatedReactor, outputArea, reactorButtonPanels, initialHeat, onPulseDuration, offPulseDuration, suspendTemp, resumeTemp);
            simulator.execute();
        }
        
    }//GEN-LAST:event_simulateButtonActionPerformed
    
    /**
     * The reactor that was last simulated.
     */
    private Reactor simulatedReactor = null;

    private void euReactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_euReactorRadioActionPerformed
        reactor.setFluid(false);
    }//GEN-LAST:event_euReactorRadioActionPerformed

    private void fluidReactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fluidReactorRadioActionPerformed
        reactor.setFluid(true);
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
            ReactorComponent component = reactor.getComponentAt(selectedRow, selectedColumn);
            component.automationThreshold = ((Number)thresholdSpinner.getValue()).intValue();
        }
    }//GEN-LAST:event_thresholdSpinnerStateChanged

    private void pauseSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pauseSpinnerStateChanged
        if (selectedColumn >= 0 && selectedRow >= 0 && reactor.getComponentAt(selectedRow, selectedColumn) != null) {
            ReactorComponent component = reactor.getComponentAt(selectedRow, selectedColumn);
            component.reactorPause = ((Number)pauseSpinner.getValue()).intValue();
        }
    }//GEN-LAST:event_pauseSpinnerStateChanged
    
    private SwingWorker<Void, String> simulator;

    private void updateReactorButtons() {
        for (int row = 0; row < reactorButtons.length; row++) {
            final int finalRow = row;
            for (int col = 0; col < reactorButtons[row].length; col++) {
                final int finalCol = col;
                ReactorComponent componentToPlace = reactor.getComponentAt(row, col);
                int buttonSize = Math.min(reactorButtons[finalRow][finalCol].getWidth(), reactorButtons[finalRow][finalCol].getHeight());
                if (buttonSize > 2 && componentToPlace != null) {
                    reactorButtons[finalRow][finalCol].setIcon(new ImageIcon(componentToPlace.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
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
        maxHeatLabel.setText(String.format("/%,.0f", reactor.getMaxHeat()));
        temperatureEffectsLabel.setText(String.format(_("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d"), (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
        SpinnerModel model = heatSpinner.getModel();
        if (model instanceof SpinnerNumberModel) {
            ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
        }
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
    private javax.swing.JPanel automationPanel;
    private javax.swing.JButton clearGridButton;
    private javax.swing.JTextField codeField;
    private javax.swing.JTextArea componentArea;
    private javax.swing.JToggleButton componentHeatExchangerButton;
    private javax.swing.JSpinner componentHeatSpinner;
    private javax.swing.JToggleButton componentHeatVentButton;
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
    private javax.swing.JToggleButton dualFuelRodMoxButton;
    private javax.swing.JToggleButton dualFuelRodThoriumButton;
    private javax.swing.JToggleButton dualFuelRodUraniumButton;
    private javax.swing.JToggleButton emptyButton;
    private javax.swing.JRadioButton euReactorRadio;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JRadioButton fluidReactorRadio;
    private javax.swing.JToggleButton fuelRodMoxButton;
    private javax.swing.JToggleButton fuelRodThoriumButton;
    private javax.swing.JToggleButton fuelRodUraniumButton;
    private javax.swing.JToggleButton heatCapacityReactorPlatingButton;
    private javax.swing.JToggleButton heatExchangerButton;
    private javax.swing.JSpinner heatSpinner;
    private javax.swing.JToggleButton heatVentButton;
    private javax.swing.JToggleButton iridiumNeutronReflectorButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JToggleButton lzhCondensatorButton;
    private javax.swing.JTextArea materialsArea;
    private javax.swing.JLabel maxHeatLabel;
    private javax.swing.JToggleButton neutronReflectorButton;
    private javax.swing.JSpinner offPulseSpinner;
    private javax.swing.JSpinner onPulseSpinner;
    private javax.swing.JTextArea outputArea;
    private javax.swing.JScrollPane outputPane;
    private javax.swing.JTabbedPane outputTabs;
    private javax.swing.JToggleButton overclockedHeatVentButton;
    private javax.swing.JButton pasteCodeButton;
    private javax.swing.JSpinner pauseSpinner;
    private javax.swing.JPanel pulsePanel;
    private javax.swing.ButtonGroup pulseTypeGroup;
    private javax.swing.JToggleButton quadFuelRodMoxButton;
    private javax.swing.JToggleButton quadFuelRodThoriumButton;
    private javax.swing.JToggleButton quadFuelRodUraniumButton;
    private javax.swing.JToggleButton reactorHeatVentButton;
    private javax.swing.JPanel reactorPanel;
    private javax.swing.JToggleButton reactorPlatingButton;
    private javax.swing.ButtonGroup reactorStyleGroup;
    private javax.swing.JMenuItem resourcePackItem;
    private javax.swing.JSpinner resumeTempSpinner;
    private javax.swing.JToggleButton rshCondensatorButton;
    private javax.swing.JLabel selectedComponentLabel;
    private javax.swing.JButton simulateButton;
    private javax.swing.JComboBox<String> simulationStyleCombo;
    private javax.swing.JSpinner suspendTempSpinner;
    private javax.swing.JPanel temperatureAndComponentsPanel;
    private javax.swing.JLabel temperatureEffectsLabel;
    private javax.swing.JToggleButton thickNeutronReflectorButton;
    private javax.swing.JSpinner thresholdSpinner;
    // End of variables declaration//GEN-END:variables
}
