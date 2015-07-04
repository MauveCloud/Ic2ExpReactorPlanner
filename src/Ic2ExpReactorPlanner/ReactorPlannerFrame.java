/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * JFrame to display a gui for planning a Nuclear Reactor using IndustrialCraft2 Experimental.
 * @author Brian McCloud
 */
public class ReactorPlannerFrame extends javax.swing.JFrame {

    private final Reactor reactor = new Reactor();
    
    private final JButton[][] reactorButtons = new JButton[6][9];
    
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
                        temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int) (reactor.getMaxHeat() * 0.4), (int) (reactor.getMaxHeat() * 0.5), (int) (reactor.getMaxHeat() * 0.7), (int) (reactor.getMaxHeat() * 0.85), (int) (reactor.getMaxHeat() * 1.0)));
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
                        refreshSimulationButtonActionPerformed(null);
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
                            temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int) (reactor.getMaxHeat() * 0.4), (int) (reactor.getMaxHeat() * 0.5), (int) (reactor.getMaxHeat() * 0.7), (int) (reactor.getMaxHeat() * 0.85), (int) (reactor.getMaxHeat() * 1.0)));
                            SpinnerModel model = heatSpinner.getModel();
                            if (model instanceof SpinnerNumberModel) {
                                ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
                            }
                            reactorButtons[finalRow][finalCol].setIcon(null);
                            reactorButtons[finalRow][finalCol].setToolTipText(null);
                            reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                            refreshSimulationButtonActionPerformed(null);
                            changingCode = true;
                            codeField.setText(reactor.getCode());
                            changingCode = false;
                        }
                    }
                    
                });
                reactorButtons[row][col].setContentAreaFilled(false);
                reactorButtons[row][col].setOpaque(true);
                reactorButtons[row][col].setBackground(Color.LIGHT_GRAY);
                reactorPanel.add(reactorButtons[row][col]);
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
        refrfeshSimulationButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        heatSpinner = new javax.swing.JSpinner();
        maxHeatLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        codeField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        timeSpinner = new javax.swing.JSpinner();
        secondsLabel = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nuclearControlSpinner = new javax.swing.JSpinner();
        heatLabel = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        outputPane = new javax.swing.JScrollPane();
        outputArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        materialsArea = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        resourcePackItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IC2 Experimental Reactor Planner");
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

        temperatureEffectsLabel.setText("Burn: 4,000  Evaporate: 5,000  Hurt: 7,000  Lava: 8,500  Explode: 10,000");
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
        fuelRodUraniumButton.setToolTipText("Fuel Rod (Uranium)");
        fuelRodUraniumButton.setActionCommand("fuelRodUranium");
        componentsPanel.add(fuelRodUraniumButton);

        componentsGroup.add(dualFuelRodUraniumButton);
        dualFuelRodUraniumButton.setToolTipText("Dual Fuel Rod (Uranium)");
        dualFuelRodUraniumButton.setActionCommand("dualFuelRodUranium");
        componentsPanel.add(dualFuelRodUraniumButton);

        componentsGroup.add(quadFuelRodUraniumButton);
        quadFuelRodUraniumButton.setToolTipText("Quad Fuel Rod (Uranium)");
        quadFuelRodUraniumButton.setActionCommand("quadFuelRodUranium");
        componentsPanel.add(quadFuelRodUraniumButton);

        componentsGroup.add(fuelRodMoxButton);
        fuelRodMoxButton.setToolTipText("Fuel Rod (MOX)");
        fuelRodMoxButton.setActionCommand("fuelRodMox");
        componentsPanel.add(fuelRodMoxButton);

        componentsGroup.add(dualFuelRodMoxButton);
        dualFuelRodMoxButton.setToolTipText("Dual Fuel Rod (MOX)");
        dualFuelRodMoxButton.setActionCommand("dualFuelRodMox");
        componentsPanel.add(dualFuelRodMoxButton);

        componentsGroup.add(quadFuelRodMoxButton);
        quadFuelRodMoxButton.setToolTipText("Quad Fuel Rod (MOX)");
        quadFuelRodMoxButton.setActionCommand("quadFuelRodMox");
        componentsPanel.add(quadFuelRodMoxButton);

        componentsGroup.add(neutronReflectorButton);
        neutronReflectorButton.setToolTipText("Neutron Reflector");
        neutronReflectorButton.setActionCommand("neutronReflector");
        componentsPanel.add(neutronReflectorButton);

        componentsGroup.add(thickNeutronReflectorButton);
        thickNeutronReflectorButton.setToolTipText("Thick Neutron Reflector");
        thickNeutronReflectorButton.setActionCommand("thickNeutronReflector");
        componentsPanel.add(thickNeutronReflectorButton);

        componentsGroup.add(heatVentButton);
        heatVentButton.setToolTipText("Heat Vent");
        heatVentButton.setActionCommand("heatVent");
        componentsPanel.add(heatVentButton);

        componentsGroup.add(advancedHeatVentButton);
        advancedHeatVentButton.setToolTipText("Advanced Heat Vent");
        advancedHeatVentButton.setActionCommand("advancedHeatVent");
        componentsPanel.add(advancedHeatVentButton);

        componentsGroup.add(reactorHeatVentButton);
        reactorHeatVentButton.setToolTipText("Reactor Heat Vent");
        reactorHeatVentButton.setActionCommand("reactorHeatVent");
        componentsPanel.add(reactorHeatVentButton);

        componentsGroup.add(componentHeatVentButton);
        componentHeatVentButton.setToolTipText("Component Heat Vent");
        componentHeatVentButton.setActionCommand("componentHeatVent");
        componentsPanel.add(componentHeatVentButton);

        componentsGroup.add(overclockedHeatVentButton);
        overclockedHeatVentButton.setToolTipText("Overclocked Heat Vent");
        overclockedHeatVentButton.setActionCommand("overclockedHeatVent");
        componentsPanel.add(overclockedHeatVentButton);

        componentsGroup.add(coolantCell10kButton);
        coolantCell10kButton.setToolTipText("10k Coolant Cell");
        coolantCell10kButton.setActionCommand("coolantCell10k");
        componentsPanel.add(coolantCell10kButton);

        componentsGroup.add(coolantCell30kButton);
        coolantCell30kButton.setToolTipText("30k Coolant Cell");
        coolantCell30kButton.setActionCommand("coolantCell30k");
        componentsPanel.add(coolantCell30kButton);

        componentsGroup.add(coolantCell60kButton);
        coolantCell60kButton.setToolTipText("60k Coolant Cell");
        coolantCell60kButton.setActionCommand("coolantCell60k");
        componentsPanel.add(coolantCell60kButton);

        componentsGroup.add(heatExchangerButton);
        heatExchangerButton.setToolTipText("Heat Exchanger");
        heatExchangerButton.setActionCommand("heatExchanger");
        componentsPanel.add(heatExchangerButton);

        componentsGroup.add(advancedHeatExchangerButton);
        advancedHeatExchangerButton.setToolTipText("Advanced Heat Exchanger");
        advancedHeatExchangerButton.setActionCommand("advancedHeatExchanger");
        componentsPanel.add(advancedHeatExchangerButton);

        componentsGroup.add(coreHeatExchangerButton);
        coreHeatExchangerButton.setToolTipText("Core Heat Exchanger");
        coreHeatExchangerButton.setActionCommand("coreHeatExchanger");
        componentsPanel.add(coreHeatExchangerButton);

        componentsGroup.add(componentHeatExchangerButton);
        componentHeatExchangerButton.setToolTipText("Component Heat Exchanger");
        componentHeatExchangerButton.setActionCommand("componentHeatExchanger");
        componentsPanel.add(componentHeatExchangerButton);

        componentsGroup.add(reactorPlatingButton);
        reactorPlatingButton.setToolTipText("Reactor Plating");
        reactorPlatingButton.setActionCommand("reactorPlating");
        componentsPanel.add(reactorPlatingButton);

        componentsGroup.add(heatCapacityReactorPlatingButton);
        heatCapacityReactorPlatingButton.setToolTipText("Heat-Capacity Reactor Plating");
        heatCapacityReactorPlatingButton.setActionCommand("heatCapacityReactorPlating");
        componentsPanel.add(heatCapacityReactorPlatingButton);

        componentsGroup.add(containmentReactorPlatingButton);
        containmentReactorPlatingButton.setToolTipText("Containment Reactor Plating");
        containmentReactorPlatingButton.setActionCommand("containmentReactorPlating");
        componentsPanel.add(containmentReactorPlatingButton);

        componentsGroup.add(rshCondensatorButton);
        rshCondensatorButton.setToolTipText("RSH-Condensator");
        rshCondensatorButton.setActionCommand("rshCondensator");
        componentsPanel.add(rshCondensatorButton);

        componentsGroup.add(lzhCondensatorButton);
        lzhCondensatorButton.setToolTipText("LZH-Condensator");
        lzhCondensatorButton.setActionCommand("lzhCondensator");
        componentsPanel.add(lzhCondensatorButton);

        componentsGroup.add(fuelRodThoriumButton);
        fuelRodThoriumButton.setToolTipText("Fuel Rod (Thorium)");
        fuelRodThoriumButton.setActionCommand("fuelRodThorium");
        componentsPanel.add(fuelRodThoriumButton);

        componentsGroup.add(dualFuelRodThoriumButton);
        dualFuelRodThoriumButton.setToolTipText("Double Fuel Rod (Thorium)");
        dualFuelRodThoriumButton.setActionCommand("dualFuelRodThorium");
        componentsPanel.add(dualFuelRodThoriumButton);

        componentsGroup.add(quadFuelRodThoriumButton);
        quadFuelRodThoriumButton.setToolTipText("Quad Fuel Rod (Thorium)");
        quadFuelRodThoriumButton.setActionCommand("quadFuelRodThorium");
        componentsPanel.add(quadFuelRodThoriumButton);

        componentsGroup.add(coolantCellHelium60kButton);
        coolantCellHelium60kButton.setToolTipText("60k He Coolant Cell");
        coolantCellHelium60kButton.setActionCommand("coolantCellHelium60k");
        componentsPanel.add(coolantCellHelium60kButton);

        componentsGroup.add(coolantCellHelium180kButton);
        coolantCellHelium180kButton.setToolTipText("180k He Coolant Cell");
        coolantCellHelium180kButton.setActionCommand("coolantCellHelium180k");
        componentsPanel.add(coolantCellHelium180kButton);

        componentsGroup.add(coolantCellHelium360kButton);
        coolantCellHelium360kButton.setToolTipText("360k He Coolant Cell");
        coolantCellHelium360kButton.setActionCommand("coolantCellHelium360k");
        componentsPanel.add(coolantCellHelium360kButton);

        componentsGroup.add(coolantCellNak60kButton);
        coolantCellNak60kButton.setToolTipText("60k NaK Coolant Cell");
        coolantCellNak60kButton.setActionCommand("coolantCellNak60k");
        componentsPanel.add(coolantCellNak60kButton);

        componentsGroup.add(coolantCellNak180kButton);
        coolantCellNak180kButton.setToolTipText("180k NaK Coolant Cell");
        coolantCellNak180kButton.setActionCommand("coolantCellNak180k");
        componentsPanel.add(coolantCellNak180kButton);

        componentsGroup.add(coolantCellNak360kButton);
        coolantCellNak360kButton.setToolTipText("360k NaK Coolant Cell");
        coolantCellNak360kButton.setActionCommand("coolantCellNak360k");
        componentsPanel.add(coolantCellNak360kButton);

        componentsGroup.add(iridiumNeutronReflectorButton);
        iridiumNeutronReflectorButton.setToolTipText("Iridium Neutron Reflector");
        iridiumNeutronReflectorButton.setActionCommand("iridiumNeutronReflector");
        componentsPanel.add(iridiumNeutronReflectorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        temperatureAndComponentsPanel.add(componentsPanel, gridBagConstraints);

        jLabel5.setText("Initial Component Heat:");
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
        euReactorRadio.setText("EU Reactor");
        euReactorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                euReactorRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(euReactorRadio, gridBagConstraints);

        reactorStyleGroup.add(fluidReactorRadio);
        fluidReactorRadio.setText("Fluid Reactor");
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

        clearGridButton.setText("Clear Grid");
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

        refrfeshSimulationButton.setText("Refresh Simulation");
        refrfeshSimulationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshSimulationButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 6);
        jPanel1.add(refrfeshSimulationButton, gridBagConstraints);

        jLabel1.setText("Initial Reactor Heat:");
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

        jLabel2.setText("Code:");
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

        jLabel3.setText("Stop redstone signal after:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jLabel3, gridBagConstraints);

        timeSpinner.setModel(new javax.swing.SpinnerNumberModel(100000, 0, 100000, 1));
        timeSpinner.setMinimumSize(new java.awt.Dimension(70, 20));
        timeSpinner.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(timeSpinner, gridBagConstraints);

        secondsLabel.setText("seconds");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(secondsLabel, gridBagConstraints);

        jLabel4.setText("Or if reactor heat reaches:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(jLabel4, gridBagConstraints);

        nuclearControlSpinner.setModel(new javax.swing.SpinnerNumberModel(120000, 0, 120000, 1));
        nuclearControlSpinner.setMinimumSize(new java.awt.Dimension(70, 20));
        nuclearControlSpinner.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(nuclearControlSpinner, gridBagConstraints);

        heatLabel.setText("heat");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanel1.add(heatLabel, gridBagConstraints);

        jSplitPane3.setBottomComponent(jPanel1);

        jSplitPane2.setRightComponent(jSplitPane3);

        jSplitPane1.setLeftComponent(jSplitPane2);

        outputArea.setEditable(false);
        outputArea.setColumns(20);
        outputArea.setRows(5);
        outputPane.setViewportView(outputArea);

        jTabbedPane1.addTab("Simulation", outputPane);

        materialsArea.setEditable(false);
        materialsArea.setColumns(20);
        materialsArea.setRows(5);
        jScrollPane2.setViewportView(materialsArea);

        jTabbedPane1.addTab("Materials", jScrollPane2);

        jSplitPane1.setRightComponent(jTabbedPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(jSplitPane1, gridBagConstraints);

        fileMenu.setText("File");

        resourcePackItem.setText("Choose Resource Pack");
        resourcePackItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resourcePackItemActionPerformed(evt);
            }
        });
        fileMenu.add(resourcePackItem);

        jMenuBar1.add(fileMenu);

        setJMenuBar(jMenuBar1);

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
        String resourcePackPath = Preferences.userRoot().get("Ic2ExpReactorPlanner.ResourcePack", null);
        JFileChooser chooser = new JFileChooser(resourcePackPath);
        chooser.setFileFilter(new FileNameExtensionFilter("Resource Packs", "jar", "zip"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            resourcePackPath = chooser.getSelectedFile().getAbsolutePath();
            Preferences.userRoot().put("Ic2ExpReactorPlanner.ResourcePack", resourcePackPath);
            plannerResized(null);
        }
    }//GEN-LAST:event_resourcePackItemActionPerformed

    private void clearGridButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearGridButtonActionPerformed
        reactor.clearGrid();
        for (int i = 0; i < reactorButtons.length; i++) {
            for (int j = 0; j < reactorButtons[i].length; j++) {
                reactorButtons[i][j].setIcon(null);
                reactorButtons[i][j].setToolTipText(null);
                reactorButtons[i][j].setBackground(Color.LIGHT_GRAY);
            }
        }
        outputArea.setText(null);
        materialsArea.setText(reactor.getMaterials().toString());
        maxHeatLabel.setText(String.format("/%,.0f", reactor.getMaxHeat()));
        temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
        SpinnerModel model = heatSpinner.getModel();
        if (model instanceof SpinnerNumberModel) {
            ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
        }
        changingCode = true;
        codeField.setText(null);
        changingCode = false;
    }//GEN-LAST:event_clearGridButtonActionPerformed

    private void refreshSimulationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshSimulationButtonActionPerformed
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
        Reactor tempReactor = new Reactor();
        tempReactor.setCode(reactor.getCode());
        tempReactor.setFluid(reactor.isFluid());
        simulator = new Simulator(tempReactor, outputArea, reactorButtons, initialHeat, ((Number)timeSpinner.getValue()).intValue(), ((Number)nuclearControlSpinner.getValue()).intValue());
        simulator.execute();
    }//GEN-LAST:event_refreshSimulationButtonActionPerformed

    private void euReactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_euReactorRadioActionPerformed
        reactor.setFluid(false);
        refreshSimulationButtonActionPerformed(null);
    }//GEN-LAST:event_euReactorRadioActionPerformed

    private void fluidReactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fluidReactorRadioActionPerformed
        reactor.setFluid(true);
        refreshSimulationButtonActionPerformed(null);
    }//GEN-LAST:event_fluidReactorRadioActionPerformed
    
    private Simulator simulator;

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
        temperatureEffectsLabel.setText(String.format("Burn: %,d  Evaporate: %,d  Hurt: %,d  Lava: %,d  Explode: %,d", (int)(reactor.getMaxHeat() * 0.4), (int)(reactor.getMaxHeat() * 0.5), (int)(reactor.getMaxHeat() * 0.7), (int)(reactor.getMaxHeat() * 0.85), (int)(reactor.getMaxHeat() * 1.0)));
        SpinnerModel model = heatSpinner.getModel();
        if (model instanceof SpinnerNumberModel) {
            ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
        }
        refreshSimulationButtonActionPerformed(null);
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
    private javax.swing.JButton clearGridButton;
    private javax.swing.JTextField codeField;
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
    private javax.swing.JLabel heatLabel;
    private javax.swing.JSpinner heatSpinner;
    private javax.swing.JToggleButton heatVentButton;
    private javax.swing.JToggleButton iridiumNeutronReflectorButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton lzhCondensatorButton;
    private javax.swing.JTextArea materialsArea;
    private javax.swing.JLabel maxHeatLabel;
    private javax.swing.JToggleButton neutronReflectorButton;
    private javax.swing.JSpinner nuclearControlSpinner;
    private javax.swing.JTextArea outputArea;
    private javax.swing.JScrollPane outputPane;
    private javax.swing.JToggleButton overclockedHeatVentButton;
    private javax.swing.JToggleButton quadFuelRodMoxButton;
    private javax.swing.JToggleButton quadFuelRodThoriumButton;
    private javax.swing.JToggleButton quadFuelRodUraniumButton;
    private javax.swing.JToggleButton reactorHeatVentButton;
    private javax.swing.JPanel reactorPanel;
    private javax.swing.JToggleButton reactorPlatingButton;
    private javax.swing.ButtonGroup reactorStyleGroup;
    private javax.swing.JButton refrfeshSimulationButton;
    private javax.swing.JMenuItem resourcePackItem;
    private javax.swing.JToggleButton rshCondensatorButton;
    private javax.swing.JLabel secondsLabel;
    private javax.swing.JPanel temperatureAndComponentsPanel;
    private javax.swing.JLabel temperatureEffectsLabel;
    private javax.swing.JToggleButton thickNeutronReflectorButton;
    private javax.swing.JSpinner timeSpinner;
    // End of variables declaration//GEN-END:variables
}
