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
import java.util.prefs.Preferences;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * JFrame to display a gui for planning a Nuclear Reactor using IndustrialCraft2 Experimental.
 * @author Brian McCloud
 */
public class ReactorPlannerFrame extends javax.swing.JFrame {

    private FuelRodUranium fuelRodUranium = new FuelRodUranium();
    private DualFuelRodUranium dualFuelRodUranium = new DualFuelRodUranium();
    private QuadFuelRodUranium quadFuelRodUranium = new QuadFuelRodUranium();
    private FuelRodMox fuelRodMox = new FuelRodMox();
    private DualFuelRodMox dualFuelRodMox = new DualFuelRodMox();
    private QuadFuelRodMox quadFuelRodMox = new QuadFuelRodMox();
    private NeutronReflector neutronReflector = new NeutronReflector();
    private ThickNeutronReflector thickNeutronReflector = new ThickNeutronReflector();
    private HeatVent heatVent = new HeatVent();
    private AdvancedHeatVent advancedHeatVent = new AdvancedHeatVent();
    private ReactorHeatVent reactorHeatVent = new ReactorHeatVent();
    private ComponentHeatVent componentHeatVent = new ComponentHeatVent();
    private OverclockedHeatVent overclockedHeatVent = new OverclockedHeatVent();
    private CoolantCell10k coolantCell10k = new CoolantCell10k();
    private CoolantCell30k coolantCell30k = new CoolantCell30k();
    private CoolantCell60k coolantCell60k = new CoolantCell60k();
    private HeatExchanger heatExchanger = new HeatExchanger();
    private AdvancedHeatExchanger advancedHeatExchanger = new AdvancedHeatExchanger();
    private CoreHeatExchanger coreHeatExchanger = new CoreHeatExchanger();
    private ComponentHeatExchanger componentHeatExchanger = new ComponentHeatExchanger();
    private ReactorPlating reactorPlating = new ReactorPlating();
    private HeatCapacityReactorPlating heatCapacityReactorPlating = new HeatCapacityReactorPlating();
    private ContainmentReactorPlating containmentReactorPlating = new ContainmentReactorPlating();
    private RshCondensator rshCondensator = new RshCondensator();
    private LzhCondensator lzhCondensator = new LzhCondensator();
    
    private final Reactor reactor = new Reactor();
    
    private final JButton[][] reactorButtons = new JButton[6][9];
    
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
                            switch (selection.getActionCommand()) {
                                case "fuelRodUranium":
                                    componentToPlace = new FuelRodUranium();
                                    break;
                                case "dualFuelRodUranium":
                                    componentToPlace = new DualFuelRodUranium();
                                    break;
                                case "quadFuelRodUranium":
                                    componentToPlace = new QuadFuelRodUranium();
                                    break;
                                case "fuelRodMox":
                                    componentToPlace = new FuelRodMox();
                                    break;
                                case "dualFuelRodMox":
                                    componentToPlace = new DualFuelRodMox();
                                    break;
                                case "quadFuelRodMox":
                                    componentToPlace = new QuadFuelRodMox();
                                    break;
                                case "neutronReflector":
                                    componentToPlace = new NeutronReflector();
                                    break;
                                case "thickNeutronReflector":
                                    componentToPlace = new ThickNeutronReflector();
                                    break;
                                case "heatVent":
                                    componentToPlace = new HeatVent();
                                    break;
                                case "advancedHeatVent":
                                    componentToPlace = new AdvancedHeatVent();
                                    break;
                                case "reactorHeatVent":
                                    componentToPlace = new ReactorHeatVent();
                                    break;
                                case "componentHeatVent":
                                    componentToPlace = new ComponentHeatVent();
                                    break;
                                case "overclockedHeatVent":
                                    componentToPlace = new OverclockedHeatVent();
                                    break;
                                case "coolantCell10k":
                                    componentToPlace = new CoolantCell10k();
                                    break;
                                case "coolantCell30k":
                                    componentToPlace = new CoolantCell30k();
                                    break;
                                case "coolantCell60k":
                                    componentToPlace = new CoolantCell60k();
                                    break;
                                case "heatExchanger":
                                    componentToPlace = new HeatExchanger();
                                    break;
                                case "advancedHeatExchanger":
                                    componentToPlace = new AdvancedHeatExchanger();
                                    break;
                                case "coreHeatExchanger":
                                    componentToPlace = new CoreHeatExchanger();
                                    break;
                                case "componentHeatExchanger":
                                    componentToPlace = new ComponentHeatExchanger();
                                    break;
                                case "reactorPlating":
                                    componentToPlace = new ReactorPlating();
                                    break;
                                case "heatCapacityReactorPlating":
                                    componentToPlace = new HeatCapacityReactorPlating();
                                    break;
                                case "containmentReactorPlating":
                                    componentToPlace = new ContainmentReactorPlating();
                                    break;
                                case "rshCondensator":
                                    componentToPlace = new RshCondensator();
                                    break;
                                case "lzhCondensator":
                                    componentToPlace = new LzhCondensator();
                                    break;
                                default:
                                    throw new AssertionError("Unrecognized component: " + selection.getActionCommand());
                            }
                        }
                        reactor.setComponentAt(finalRow, finalCol, componentToPlace);
                        maxHeatLabel.setText(String.format("/%,.0f", reactor.getMaxHeat()));
                        SpinnerModel model = heatSpinner.getModel();
                        if (model instanceof SpinnerNumberModel) {
                            ((SpinnerNumberModel)model).setMaximum(reactor.getMaxHeat());
                        }
                        int buttonSize = Math.min(reactorButtons[finalRow][finalCol].getWidth(), reactorButtons[finalRow][finalCol].getHeight());
                        if (buttonSize > 2 && componentToPlace != null) {
                            reactorButtons[finalRow][finalCol].setIcon(new ImageIcon(componentToPlace.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
                            reactorButtons[finalRow][finalCol].setToolTipText(componentToPlace.toString());
                            reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                        }
                    }
                });

                reactorButtons[row][col].addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getButton() != MouseEvent.BUTTON1) {
                            reactor.setComponentAt(finalRow, finalCol, null);
                            maxHeatLabel.setText(String.format("/%,.0f", reactor.getMaxHeat()));
                            SpinnerModel model = heatSpinner.getModel();
                            if (model instanceof SpinnerNumberModel) {
                                ((SpinnerNumberModel) model).setMaximum(reactor.getMaxHeat());
                            }
                            reactorButtons[finalRow][finalCol].setIcon(null);
                            reactorButtons[finalRow][finalCol].setToolTipText(null);
                            reactorButtons[finalRow][finalCol].setBackground(Color.LIGHT_GRAY);
                        }
                    }
                    
                });
                reactorButtons[row][col].setContentAreaFilled(false);
                reactorButtons[row][col].setOpaque(true);
                reactorButtons[row][col].setBackground(Color.LIGHT_GRAY);
                reactorPanel.add(reactorButtons[row][col]);
            }
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
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        reactorPanel = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
        componentsPanel = new javax.swing.JPanel();
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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        heatSpinner = new javax.swing.JSpinner();
        maxHeatLabel = new javax.swing.JLabel();
        simulateButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        outputArea = new javax.swing.JTextArea();
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
        jSplitPane3.setResizeWeight(0.8);

        componentsPanel.setMinimumSize(new java.awt.Dimension(160, 80));
        componentsPanel.setPreferredSize(new java.awt.Dimension(160, 80));
        componentsPanel.setLayout(new java.awt.GridLayout(5, 5));

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

        jSplitPane3.setLeftComponent(componentsPanel);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Initial Reactor Heat:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jLabel1, gridBagConstraints);

        heatSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 10000.0d, 1.0d));
        heatSpinner.setMinimumSize(new java.awt.Dimension(70, 20));
        heatSpinner.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(heatSpinner, gridBagConstraints);

        maxHeatLabel.setText("/10,000");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(maxHeatLabel, gridBagConstraints);

        simulateButton.setText("Simulate");
        simulateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulateButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(simulateButton, gridBagConstraints);

        jSplitPane3.setRightComponent(jPanel1);

        jSplitPane2.setRightComponent(jSplitPane3);

        jSplitPane1.setLeftComponent(jSplitPane2);

        outputArea.setEditable(false);
        outputArea.setColumns(20);
        outputArea.setRows(5);
        jScrollPane1.setViewportView(outputArea);

        jTabbedPane1.addTab("Simulation", jScrollPane1);

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
        int buttonSize = Math.min(fuelRodUraniumButton.getWidth(), fuelRodUraniumButton.getHeight());
        if (buttonSize > 2) {
            fuelRodUraniumButton.setIcon(new ImageIcon(fuelRodUranium.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(dualFuelRodUraniumButton.getWidth(), dualFuelRodUraniumButton.getHeight());
        if (buttonSize > 2) {
            dualFuelRodUraniumButton.setIcon(new ImageIcon(dualFuelRodUranium.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(quadFuelRodUraniumButton.getWidth(), quadFuelRodUraniumButton.getHeight());
        if (buttonSize > 2) {
            quadFuelRodUraniumButton.setIcon(new ImageIcon(quadFuelRodUranium.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(fuelRodMoxButton.getWidth(), fuelRodMoxButton.getHeight());
        if (buttonSize > 2) {
            fuelRodMoxButton.setIcon(new ImageIcon(fuelRodMox.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(dualFuelRodMoxButton.getWidth(), dualFuelRodMoxButton.getHeight());
        if (buttonSize > 2) {
            dualFuelRodMoxButton.setIcon(new ImageIcon(dualFuelRodMox.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(quadFuelRodMoxButton.getWidth(), quadFuelRodMoxButton.getHeight());
        if (buttonSize > 2) {
            quadFuelRodMoxButton.setIcon(new ImageIcon(quadFuelRodMox.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(neutronReflectorButton.getWidth(), neutronReflectorButton.getHeight());
        if (buttonSize > 2) {
            neutronReflectorButton.setIcon(new ImageIcon(neutronReflector.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(thickNeutronReflectorButton.getWidth(), thickNeutronReflectorButton.getHeight());
        if (buttonSize > 2) {
            thickNeutronReflectorButton.setIcon(new ImageIcon(thickNeutronReflector.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(heatVentButton.getWidth(), heatVentButton.getHeight());
        if (buttonSize > 2) {
            heatVentButton.setIcon(new ImageIcon(heatVent.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(advancedHeatVentButton.getWidth(), advancedHeatVentButton.getHeight());
        if (buttonSize > 2) {
            advancedHeatVentButton.setIcon(new ImageIcon(advancedHeatVent.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(reactorHeatVentButton.getWidth(), reactorHeatVentButton.getHeight());
        if (buttonSize > 2) {
            reactorHeatVentButton.setIcon(new ImageIcon(reactorHeatVent.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(componentHeatVentButton.getWidth(), componentHeatVentButton.getHeight());
        if (buttonSize > 2) {
            componentHeatVentButton.setIcon(new ImageIcon(componentHeatVent.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(overclockedHeatVentButton.getWidth(), overclockedHeatVentButton.getHeight());
        if (buttonSize > 2) {
            overclockedHeatVentButton.setIcon(new ImageIcon(overclockedHeatVent.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(coolantCell10kButton.getWidth(), coolantCell10kButton.getHeight());
        if (buttonSize > 2) {
            coolantCell10kButton.setIcon(new ImageIcon(coolantCell10k.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(coolantCell30kButton.getWidth(), coolantCell30kButton.getHeight());
        if (buttonSize > 2) {
            coolantCell30kButton.setIcon(new ImageIcon(coolantCell30k.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(coolantCell60kButton.getWidth(), coolantCell60kButton.getHeight());
        if (buttonSize > 2) {
            coolantCell60kButton.setIcon(new ImageIcon(coolantCell60k.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(heatExchangerButton.getWidth(), heatExchangerButton.getHeight());
        if (buttonSize > 2) {
            heatExchangerButton.setIcon(new ImageIcon(heatExchanger.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(advancedHeatExchangerButton.getWidth(), advancedHeatExchangerButton.getHeight());
        if (buttonSize > 2) {
            advancedHeatExchangerButton.setIcon(new ImageIcon(advancedHeatExchanger.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(coreHeatExchangerButton.getWidth(), coreHeatExchangerButton.getHeight());
        if (buttonSize > 2) {
            coreHeatExchangerButton.setIcon(new ImageIcon(coreHeatExchanger.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(componentHeatExchangerButton.getWidth(), componentHeatExchangerButton.getHeight());
        if (buttonSize > 2) {
            componentHeatExchangerButton.setIcon(new ImageIcon(componentHeatExchanger.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(reactorPlatingButton.getWidth(), reactorPlatingButton.getHeight());
        if (buttonSize > 2) {
            reactorPlatingButton.setIcon(new ImageIcon(reactorPlating.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(heatCapacityReactorPlatingButton.getWidth(), heatCapacityReactorPlatingButton.getHeight());
        if (buttonSize > 2) {
            heatCapacityReactorPlatingButton.setIcon(new ImageIcon(heatCapacityReactorPlating.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(containmentReactorPlatingButton.getWidth(), containmentReactorPlatingButton.getHeight());
        if (buttonSize > 2) {
            containmentReactorPlatingButton.setIcon(new ImageIcon(containmentReactorPlating.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(rshCondensatorButton.getWidth(), rshCondensatorButton.getHeight());
        if (buttonSize > 2) {
            rshCondensatorButton.setIcon(new ImageIcon(rshCondensator.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        buttonSize = Math.min(lzhCondensatorButton.getWidth(), lzhCondensatorButton.getHeight());
        if (buttonSize > 2) {
            lzhCondensatorButton.setIcon(new ImageIcon(lzhCondensator.getImage().getScaledInstance(buttonSize * 8 / 10, buttonSize * 8 / 10, Image.SCALE_FAST)));
        }
        
        for (int row = 0; row < reactorButtons.length; row++) {
            for (int col = 0; col < reactorButtons[row].length; col++) {
                buttonSize = Math.min(reactorButtons[row][col].getWidth(), reactorButtons[row][col].getHeight());
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
            fuelRodUranium = new FuelRodUranium();
            dualFuelRodUranium = new DualFuelRodUranium();
            quadFuelRodUranium = new QuadFuelRodUranium();
            fuelRodMox = new FuelRodMox();
            dualFuelRodMox = new DualFuelRodMox();
            quadFuelRodMox = new QuadFuelRodMox();
            neutronReflector = new NeutronReflector();
            thickNeutronReflector = new ThickNeutronReflector();
            heatVent = new HeatVent();
            advancedHeatVent = new AdvancedHeatVent();
            reactorHeatVent = new ReactorHeatVent();
            componentHeatVent = new ComponentHeatVent();
            overclockedHeatVent = new OverclockedHeatVent();
            coolantCell10k = new CoolantCell10k();
            coolantCell30k = new CoolantCell30k();
            coolantCell60k = new CoolantCell60k();
            heatExchanger = new HeatExchanger();
            advancedHeatExchanger = new AdvancedHeatExchanger();
            coreHeatExchanger = new CoreHeatExchanger();
            componentHeatExchanger = new ComponentHeatExchanger();
            reactorPlating = new ReactorPlating();
            heatCapacityReactorPlating = new HeatCapacityReactorPlating();
            containmentReactorPlating = new ContainmentReactorPlating();
            rshCondensator = new RshCondensator();
            lzhCondensator = new LzhCondensator();
            plannerResized(null);
        }
    }//GEN-LAST:event_resourcePackItemActionPerformed

    private void simulateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulateButtonActionPerformed
        int initialHeat = 0;
        Object value = heatSpinner.getValue();
        if (value instanceof Number) {
            initialHeat = ((Number)value).intValue();
        }
        new Simulator(reactor, outputArea, reactorButtons, initialHeat).execute();
    }//GEN-LAST:event_simulateButtonActionPerformed

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
    private javax.swing.JToggleButton componentHeatExchangerButton;
    private javax.swing.JToggleButton componentHeatVentButton;
    private javax.swing.ButtonGroup componentsGroup;
    private javax.swing.JPanel componentsPanel;
    private javax.swing.JToggleButton containmentReactorPlatingButton;
    private javax.swing.JToggleButton coolantCell10kButton;
    private javax.swing.JToggleButton coolantCell30kButton;
    private javax.swing.JToggleButton coolantCell60kButton;
    private javax.swing.JToggleButton coreHeatExchangerButton;
    private javax.swing.JToggleButton dualFuelRodMoxButton;
    private javax.swing.JToggleButton dualFuelRodUraniumButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JToggleButton fuelRodMoxButton;
    private javax.swing.JToggleButton fuelRodUraniumButton;
    private javax.swing.JToggleButton heatCapacityReactorPlatingButton;
    private javax.swing.JToggleButton heatExchangerButton;
    private javax.swing.JSpinner heatSpinner;
    private javax.swing.JToggleButton heatVentButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToggleButton lzhCondensatorButton;
    private javax.swing.JLabel maxHeatLabel;
    private javax.swing.JToggleButton neutronReflectorButton;
    private javax.swing.JTextArea outputArea;
    private javax.swing.JToggleButton overclockedHeatVentButton;
    private javax.swing.JToggleButton quadFuelRodMoxButton;
    private javax.swing.JToggleButton quadFuelRodUraniumButton;
    private javax.swing.JToggleButton reactorHeatVentButton;
    private javax.swing.JPanel reactorPanel;
    private javax.swing.JToggleButton reactorPlatingButton;
    private javax.swing.JMenuItem resourcePackItem;
    private javax.swing.JToggleButton rshCondensatorButton;
    private javax.swing.JButton simulateButton;
    private javax.swing.JToggleButton thickNeutronReflectorButton;
    // End of variables declaration//GEN-END:variables
}
