package com.github.jy2.sm.monitor.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;

import com.github.jy2.di.JyroscopeDi;
import com.github.jy2.di.annotations.Init;
import com.github.jy2.di.annotations.Parameter;
import com.github.jy2.sm.monitor.ros.RosHandler;
import com.github.jy2.sm.monitor.util.Holder;

public class ApplicationFrame extends JFrame {
	@Parameter("default_node")
	String defaultNode="/autofork_canbox_stateful";
	private static final long serialVersionUID = 1L;

	public static final Log LOG = JyroscopeDi.getLog();

	public RosHandler rosHandler;

	private JTextField textAddress;
	private JButton btnConnectDisconnect;
	private ImagePanel imagePanel;
	private JLabel labelRightTop;
	private JLabel labelRightMiddle;
	private JLabel labelRightBottom;
	private JTextField txtTime;
	private JTextField txtLife;
	private JTextField txtDuration;

	// for repaint
	JScrollPane jScrollPanelLeft;

	/**
	 * Launch the application.
	 *
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new ApplicationFrame().showFrame();
	}

	@Init
	public void init(){
		EventQueue.invokeLater(() -> textAddress.setText(defaultNode));
	}

	public void showFrame() throws Exception {
		final Holder<Exception> exceptionHolder = new Holder<>(null);
		CountDownLatch latch = new CountDownLatch(1);
		EventQueue.invokeLater(() -> {
			try {
				setVisible(true);
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
				exceptionHolder.value = e;
			}
			latch.countDown();
		});
		latch.await();
		if (exceptionHolder.value != null) {
			throw new RuntimeException("Failed to show application frame", exceptionHolder.value);
		}
	}

	/**
	 * Create the frame.
	 */
	public ApplicationFrame() {
		setTitle("State Machine Monitor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1100, 700);
		JPanel contentPane = new JPanel();
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
		textAddress.setText("/");
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

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.75);
		contentPane.add(splitPane);

		jScrollPanelLeft = new JScrollPane();
		splitPane.setLeftComponent(jScrollPanelLeft);

		imagePanel = new ImagePanel();
		jScrollPanelLeft.setViewportView(imagePanel);

		JSplitPane splitPaneRight = new JSplitPane();
		splitPaneRight.setResizeWeight(0.33);
		splitPaneRight.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setRightComponent(splitPaneRight);

		JScrollPane scrollPane = new JScrollPane();
		splitPaneRight.setLeftComponent(scrollPane);

		labelRightTop = new JLabel("");
		labelRightTop.setVerticalAlignment(SwingConstants.TOP);
		scrollPane.setViewportView(labelRightTop);

		JSplitPane splitPaneRightBottom = new JSplitPane();
		splitPaneRightBottom.setResizeWeight(0.5);
		splitPaneRightBottom.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPaneRight.setRightComponent(splitPaneRightBottom);

		JScrollPane scrollPane_1 = new JScrollPane();
		splitPaneRightBottom.setLeftComponent(scrollPane_1);

		labelRightMiddle = new JLabel("");
		labelRightMiddle.setVerticalAlignment(SwingConstants.TOP);
		scrollPane_1.setViewportView(labelRightMiddle);

		JScrollPane scrollPane_2 = new JScrollPane();
		splitPaneRightBottom.setRightComponent(scrollPane_2);

		labelRightBottom = new JLabel("");
		labelRightBottom.setVerticalAlignment(SwingConstants.TOP);
		scrollPane_2.setViewportView(labelRightBottom);

		JPanel panelBottomStatus = new JPanel();
		FlowLayout fl_panelBottomStatus = (FlowLayout) panelBottomStatus.getLayout();
		fl_panelBottomStatus.setHgap(10);
		fl_panelBottomStatus.setAlignment(FlowLayout.LEFT);
		contentPane.add(panelBottomStatus, BorderLayout.SOUTH);

		JLabel lblTime = new JLabel("Time");
		panelBottomStatus.add(lblTime);

		txtTime = new JTextField();
		txtTime.setEditable(false);
		lblTime.setLabelFor(txtTime);
		panelBottomStatus.add(txtTime);
		txtTime.setColumns(11);

		JLabel lblLife = new JLabel("Life");
		panelBottomStatus.add(lblLife);

		txtLife = new JTextField();
		txtLife.setEditable(false);
		lblLife.setLabelFor(txtLife);
		panelBottomStatus.add(txtLife);
		txtLife.setColumns(11);

		JLabel lblDuration = new JLabel("Duration of state");
		panelBottomStatus.add(lblDuration);

		txtDuration = new JTextField();
		txtDuration.setEditable(false);
		lblDuration.setLabelFor(txtDuration);
		panelBottomStatus.add(txtDuration);
		txtDuration.setColumns(11);

		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/smlib_ui.png")));
	}

	public void setImage(BufferedImage image) {
		imagePanel.setImage(image);
		jScrollPanelLeft.repaint();
		jScrollPanelLeft.doLayout();
	}

	public void setTopText(String text) {
		labelRightTop.setText(formatLabelText(text));
	}

	public void setMiddleText(String text) {
		labelRightMiddle.setText(formatLabelText(text));
	}

	public void setBottomText(String text) {
		labelRightBottom.setText(formatLabelText(text));
	}

	public void setTimeText(String text) {
		txtTime.setText(text);
	}

	public void setLifeText(String text) {
		txtLife.setText(text);
	}

	public void setDurationText(String text) {
		txtDuration.setText(text);
	}

	protected void btnConnectDisconnectPressed() {
		if ("Connect".equalsIgnoreCase(btnConnectDisconnect.getText())) {
			rosHandler.onConnect(textAddress.getText());
			btnConnectDisconnect.setText("Disconnect");
			textAddress.setEnabled(false);
		} else {
			rosHandler.onDisconnect();
			btnConnectDisconnect.setText("Connect");
			textAddress.setEnabled(true);
		}
	}

	private String formatLabelText(String text) {
		return "<html>" + text.replace("\n", "<br>").replace(" ", "&nbsp;") + "</html>";
	}
}
