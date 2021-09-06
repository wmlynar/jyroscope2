package com.inovatica.autofork.orchestrator.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.inovatica.orchestrator.json.OrcherstratorStatus;
import com.inovatica.orchestrator.json.OrchestratorStatusItem;

public class ApplicationFrame extends JFrame {

    public final static Color VERY_DARK_GREEN = new Color(0, 192, 0);
    public final static Color DARK_GREEN = new Color(0, 192, 0);
    public final static Color SELECTED = new Color(184, 207, 229);
    private static final int MAX_NUM_CONSOLE_ROWS = 200;
    private static final int RESTART_SLEEP_TIME = 5000;
    private static final long serialVersionUID = 1L;
    public DistributedInterface distributedInterface;
    LinkedList<String> list = new LinkedList<>();
    private JPanel contentPane;
    private JTextField textAddress;
    private JTable table;
    private JPanel panel;
    private JButton btnStartButton;
    private JButton btnStopButton;
    private JButton btnRestartButton;
    private JPanel panel_1;
    private JButton btnRestartAll;
    private JButton btnConnectDisconnect;
    private JSplitPane splitPane;
    private JLabel lblConsole;
    private JButton btnClear;
    private JScrollPane scrollPaneBottom;
    private JButton btnKillButton;
    private JButton btnScan;
    private JToggleButton tglbtnPause;
	private JButton button;
	private JToggleButton tglbtnFilter;

    /**
     * Create the frame.
     */
    public ApplicationFrame() {
        setIconImage(new ImageIcon(this.getClass().getResource("/orchestrator.png")).getImage());
        setTitle("Orchestrator UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 655);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JToolBar toolBar = new JToolBar();
        contentPane.add(toolBar, BorderLayout.NORTH);

        textAddress = new JTextField();
		textAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnConnectDisconnectPressed();
			}
		});
        textAddress.setText("/orchestrator_navbox");
        toolBar.add(textAddress);
        textAddress.setColumns(10);

        btnConnectDisconnect = new JButton("Connect");
        btnConnectDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnConnectDisconnectPressed();
            }
        });
        toolBar.add(btnConnectDisconnect);

        splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        contentPane.add(splitPane, BorderLayout.CENTER);

        JScrollPane scrollPaneTop = new JScrollPane();
        splitPane.setLeftComponent(scrollPaneTop);

        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(String.class, new ProxyCellRenderer(table.getDefaultRenderer(String.class)));
        scrollPaneTop.setViewportView(table);

        scrollPaneBottom = new JScrollPane();
        splitPane.setRightComponent(scrollPaneBottom);

        lblConsole = new JLabel(" ");
        scrollPaneBottom.setViewportView(lblConsole);
        lblConsole.setVerticalAlignment(SwingConstants.TOP);

        panel = new JPanel();
        contentPane.add(panel, BorderLayout.EAST);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{117, 0};
        gbl_panel.rowHeights = new int[]{57, 57, 57, 57, 57, 57, 57, 57, 57, 0};
        gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        panel.setLayout(gbl_panel);

        btnStartButton = new JButton("Start");
        btnStartButton.setEnabled(false);
        btnStartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        startButtonPressed();
                    }
                });
            }
        });
        GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
        gbc_btnNewButton_1.fill = GridBagConstraints.BOTH;
        gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
        gbc_btnNewButton_1.gridx = 0;
        gbc_btnNewButton_1.gridy = 0;
        panel.add(btnStartButton, gbc_btnNewButton_1);

        btnStopButton = new JButton("Stop");
        btnStopButton.setEnabled(false);
        btnStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        stopButtonPressed();
                    }
                });
            }
        });
        GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
        gbc_btnNewButton_2.fill = GridBagConstraints.BOTH;
        gbc_btnNewButton_2.insets = new Insets(0, 0, 5, 0);
        gbc_btnNewButton_2.gridx = 0;
        gbc_btnNewButton_2.gridy = 1;
        panel.add(btnStopButton, gbc_btnNewButton_2);

        btnRestartButton = new JButton("Restart");
        btnRestartButton.setEnabled(false);
        btnRestartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        restartButtonPressed();
                    }
                });
            }
        });

        btnKillButton = new JButton("Kill");
        btnKillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                killButtonPressed();
            }
        });
        btnKillButton.setEnabled(false);
        GridBagConstraints gbc_btnKillButton = new GridBagConstraints();
        gbc_btnKillButton.fill = GridBagConstraints.BOTH;
        gbc_btnKillButton.insets = new Insets(0, 0, 5, 0);
        gbc_btnKillButton.gridx = 0;
        gbc_btnKillButton.gridy = 2;
        panel.add(btnKillButton, gbc_btnKillButton);

        btnScan = new JButton("Scan");
        btnScan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                scanButtonPressed();
            }
        });
        btnScan.setEnabled(false);
        GridBagConstraints gbc_btnScan = new GridBagConstraints();
        gbc_btnScan.fill = GridBagConstraints.BOTH;
        gbc_btnScan.insets = new Insets(0, 0, 5, 0);
        gbc_btnScan.gridx = 0;
        gbc_btnScan.gridy = 3;
        panel.add(btnScan, gbc_btnScan);

        GridBagConstraints gbc_btnNewButton_3 = new GridBagConstraints();
        gbc_btnNewButton_3.fill = GridBagConstraints.BOTH;
        gbc_btnNewButton_3.insets = new Insets(0, 0, 5, 0);
        gbc_btnNewButton_3.gridx = 0;
        gbc_btnNewButton_3.gridy = 4;
        panel.add(btnRestartButton, gbc_btnNewButton_3);

        btnRestartAll = new JButton("Restart All");
        btnRestartAll.setEnabled(false);
        btnRestartAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        restartAllButtonPressed();
                    }
                });
            }
        });

        GridBagConstraints gbc_btnRestartAll = new GridBagConstraints();
        gbc_btnRestartAll.fill = GridBagConstraints.BOTH;
        gbc_btnRestartAll.insets = new Insets(0, 0, 5, 0);
        gbc_btnRestartAll.gridx = 0;
        gbc_btnRestartAll.gridy = 5;
        panel.add(btnRestartAll, gbc_btnRestartAll);

        btnClear = new JButton("Clear");
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearButtonPressed();
            }
        });
        btnClear.setEnabled(false);
        GridBagConstraints gbc_btnClear = new GridBagConstraints();
        gbc_btnClear.fill = GridBagConstraints.BOTH;
        gbc_btnClear.insets = new Insets(0, 0, 5, 0);
        gbc_btnClear.gridx = 0;
        gbc_btnClear.gridy = 6;
        panel.add(btnClear, gbc_btnClear);

		tglbtnFilter = new JToggleButton("Filter");
		tglbtnFilter.setEnabled(false);
		GridBagConstraints gbc_tglbtnFilter = new GridBagConstraints();
		gbc_tglbtnFilter.fill = GridBagConstraints.BOTH;
		gbc_tglbtnFilter.insets = new Insets(0, 0, 5, 0);
		gbc_tglbtnFilter.gridx = 0;
		gbc_tglbtnFilter.gridy = 7;
		panel.add(tglbtnFilter, gbc_tglbtnFilter);

        tglbtnPause = new JToggleButton("Pause");
        tglbtnPause.setEnabled(false);
        GridBagConstraints gbc_tglbtnPause = new GridBagConstraints();
        gbc_tglbtnPause.fill = GridBagConstraints.BOTH;
        gbc_tglbtnPause.insets = new Insets(0, 0, 5, 0);
        gbc_tglbtnPause.gridx = 0;
		gbc_tglbtnPause.gridy = 8;
        panel.add(tglbtnPause, gbc_tglbtnPause);

        panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
        gbc_panel_1.weighty = 1.0;
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 9;
        panel.add(panel_1, gbc_panel_1);
    }

    private static Color makeDarker(Color color) {
        return new Color((int) (color.getRed() * 0.9), (int) (color.getGreen() * 0.9), (int) (color.getBlue() * 0.9));
    }

    public void showFrame() throws Exception {
        final Holder<Exception> exceptionHolder = new Holder<>(null);
        CountDownLatch latch = new CountDownLatch(1);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    setVisible(true);
                } catch (Exception e) {
                    exceptionHolder.value = e;
                }
                latch.countDown();
            }
        });
        latch.await();
        if (exceptionHolder.value != null) {
            throw new RuntimeException("Failed to show application frame", exceptionHolder.value);
        }
    }

    public void setStatus(OrcherstratorStatus status) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                int row = table.getSelectedRow();
                table.setModel(new MyTableModel(status));
                if (row >= 0 && row < table.getRowCount()) {
                    table.setRowSelectionInterval(0, row);
                }
                table.getColumnModel().getColumn(0).setPreferredWidth(1000);
                table.getColumnModel().getColumn(0).setMinWidth(80);
				table.getColumnModel().getColumn(1).setPreferredWidth(120);
				table.getColumnModel().getColumn(1).setMinWidth(120);
                table.getColumnModel().getColumn(2).setPreferredWidth(80);
                table.getColumnModel().getColumn(2).setMinWidth(80);
                table.getColumnModel().getColumn(3).setPreferredWidth(80);
                table.getColumnModel().getColumn(3).setMinWidth(80);
                table.getColumnModel().getColumn(4).setPreferredWidth(80);
                table.getColumnModel().getColumn(4).setMinWidth(80);
                table.getColumnModel().getColumn(5).setPreferredWidth(80);
                table.getColumnModel().getColumn(5).setMinWidth(80);
            }

        });

    }

    public void onStartup(String connectTopicOnStartup) {
        if (connectTopicOnStartup.trim().isEmpty()) {
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                textAddress.setText(connectTopicOnStartup);
                btnConnectDisconnectPressed();
            }
        });
    }

    public void addText(String text) {

        if (tglbtnPause.isSelected()) {
            return;
        }

        synchronized (this) {
            if (list.size() >= MAX_NUM_CONSOLE_ROWS) {
                list.removeFirst();
            }
            list.addLast(text);
        }
    }

    public void updateConsole() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        synchronized (this) {
            for (String s : list) {
                sb.append(s);
                sb.append("<br>");
            }
        }
        sb.append("</html>");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                lblConsole.setText(sb.toString());
            }
        });
    }

    private void startButtonPressed() {
        if (distributedInterface == null) {
            return;
        }
        TableModel model = table.getModel();
        int row = table.getSelectedRow();
        if (row < 0 || row >= model.getRowCount()) {
            return;
        }
        distributedInterface.onStartItem((String) model.getValueAt(row, 0));
    }

    private void stopButtonPressed() {
        if (distributedInterface == null) {
            return;
        }
        TableModel model = table.getModel();
        int row = table.getSelectedRow();
        if (row < 0 || row >= model.getRowCount()) {
            return;
        }
        distributedInterface.onStopItem((String) model.getValueAt(row, 0));
    }

    protected void killButtonPressed() {
        if (distributedInterface == null) {
            return;
        }
        TableModel model = table.getModel();
        int row = table.getSelectedRow();
        if (row < 0 || row >= model.getRowCount()) {
            return;
        }
        distributedInterface.onKillItem((String) model.getValueAt(row, 0));
    }

    private void restartButtonPressed() {
        if (distributedInterface == null) {
            return;
        }
        TableModel model = table.getModel();
        int row = table.getSelectedRow();
        if (row < 0 || row >= model.getRowCount()) {
            return;
        }
        distributedInterface.onStopItem((String) model.getValueAt(row, 0));
        try {
            Thread.sleep(RESTART_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        distributedInterface.onStartItem((String) model.getValueAt(row, 0));
    }

    private void restartAllButtonPressed() {
        if (distributedInterface == null) {
            return;
        }
        TableModel model = table.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            int row = table.getSelectedRow();
            boolean wasStarted = "started".equalsIgnoreCase((String) model.getValueAt(row, 5));
            if (wasStarted) {
                distributedInterface.onStopItem((String) model.getValueAt(row, 0));
            }
        }
        try {
            Thread.sleep(RESTART_SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            int row = table.getSelectedRow();
            boolean wasStarted = "true".equalsIgnoreCase((String) model.getValueAt(row, 5));
            if (wasStarted) {
                distributedInterface.onStartItem((String) model.getValueAt(row, 0));
            }
        }
    }

    protected void btnConnectDisconnectPressed() {
        if ("Connect".equalsIgnoreCase(btnConnectDisconnect.getText())) {
            distributedInterface.onConnect(textAddress.getText());
            btnConnectDisconnect.setText("Disconnect");
            textAddress.setEnabled(false);
            btnStartButton.setEnabled(true);
            btnStopButton.setEnabled(true);
            btnKillButton.setEnabled(true);
            btnScan.setEnabled(true);
            btnRestartButton.setEnabled(true);
            btnRestartAll.setEnabled(true);
            btnClear.setEnabled(true);
			tglbtnFilter.setEnabled(true);
            tglbtnPause.setEnabled(true);
        } else {
            distributedInterface.onDisconnect();
            btnConnectDisconnect.setText("Connect");
            textAddress.setEnabled(true);
            btnStartButton.setEnabled(false);
            btnStopButton.setEnabled(false);
            btnStopButton.setEnabled(false);
            btnKillButton.setEnabled(false);
            btnScan.setEnabled(false);
            btnRestartButton.setEnabled(false);
            btnRestartAll.setEnabled(false);
            table.setModel(new DefaultTableModel());
            btnClear.setEnabled(false);
			tglbtnFilter.setEnabled(false);
            tglbtnPause.setEnabled(false);
        }
    }

    protected void clearButtonPressed() {
        list.clear();
        lblConsole.setText("");
    }

    private void scanButtonPressed() {
        if (distributedInterface == null) {
            return;
        }
        distributedInterface.onScanItems();
    }

    private static class ProxyCellRenderer implements TableCellRenderer {

        protected static final Border DEFAULT_BORDER = new EmptyBorder(1, 1, 1, 1);
        private TableCellRenderer renderer;

        public ProxyCellRenderer(TableCellRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component comp = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (comp instanceof JComponent) {
                ((JComponent) comp).setBorder(DEFAULT_BORDER);
            }
            MyTableModel model = (MyTableModel) table.getModel();
            boolean isStarted = "started".equalsIgnoreCase((String) model.getValueAt(row, 5));
            boolean isStartup = "true".equalsIgnoreCase((String) model.getValueAt(row, 2));
            if (isStarted) {
                if (isSelected) {
                    comp.setBackground(DARK_GREEN); // GRAY
                } else {
                    comp.setBackground(Color.GREEN);
                }
            } else {
                if (isSelected) {
                    comp.setBackground(SELECTED); // LIGHT_GRAY
                } else {
                    comp.setBackground(Color.WHITE);
                }
            }
            if (column == 2) {
                if (isStartup) {
                    comp.setBackground(makeDarker(comp.getBackground()));
                }
            }
            return comp;
        }
    }

    private final class MyTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;
        String[] columnNames = new String[]{"Name", "Type", "On Startup", "Debug Port", "JMX/RMI Port", "Status"};
        private Class<?>[] columnTypes = new Class[]{String.class, String.class, String.class, String.class,
                String.class, String.class};
        private List<OrchestratorStatusItem> status;

        private MyTableModel(OrcherstratorStatus status) {
            this.status = status.items.stream()
                    .sorted((a, b) -> a.name.compareTo(b.name))
                    .collect(Collectors.toList());
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public int getRowCount() {
            if (status == null) {
                return 0;
            } else {
                return status.size();
            }
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (row == -1 || column == -1 || status == null) {
                return "";
            }
            OrchestratorStatusItem item = status.get(row);
            switch (column) {
                case 0:
                    return item.name;
                case 1:
                    return item.type;
                case 2:
                    return Boolean.toString(item.onStart);
                case 3:
                    return Integer.toString(item.debugPort);
                case 4:
                    return "" + item.jmxPort + "," + (item.jmxPort + 1);
                case 5:
                    return item.isStarted ? "started" : "stopped";
                default:
            }
            throw new RuntimeException("should not happen!");
        }
    }

	public boolean isFilteringEnabled() {
		return tglbtnFilter.isSelected();
	}

	public String getSelectedItemName() {
		TableModel model = table.getModel();
		int row = table.getSelectedRow();
		if (row < 0 || row >= model.getRowCount()) {
			return null;
		}
		return (String) model.getValueAt(row, 0);
	}
}
