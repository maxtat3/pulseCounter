import jssc.SerialPortException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class UI implements UART.CallbackToUI {

	private JTextField jtfPulseCounter;
	private JButton jbtnReset = null;
	private JComboBox jcmboxComPort;
	private JFrame mainFrame;

	private UART uart;

	/**
	 * Main counter to calculate pulses from gerkon sensor.
	 */
	private int pulseCounter = 0;


	public UI() {
		buildUI();
		setUIActions();

		uart = new UART(this);
		// при запуске приложение автоматчески пытается найти порт устройства и подключиться к нему.
		uart.autoConnect();

//		if (port != null) {
//			jcmboxComPort.setSelectedItem(port);
//		} else {
//			msgDeviceNotFound();
//		}
	}


	public void buildUI() {
		//==================================================
		// 		Pulse counter panel and jtf component  
		//==================================================
		JPanel jpPulseCounter = new JPanel();
		jpPulseCounter.setLayout(new BoxLayout(jpPulseCounter, BoxLayout.X_AXIS));
		jtfPulseCounter = new JTextField("0");
		jtfPulseCounter.setFont(new Font(Const.TEXT_DEFAULT_STYLE, Font.PLAIN, 70));
		jtfPulseCounter.setEditable(false);
		jpPulseCounter.add(jtfPulseCounter);

		//===============================
		// 		Direction panel   
		//===============================
		jcmboxComPort = new JComboBox();
		jcmboxComPort.setMaximumSize(new Dimension(180, Short.MAX_VALUE));
		jcmboxComPort.setModel(new DefaultComboBoxModel(Const.COM_PORTS));
		jcmboxComPort.setFont(new Font(Const.TEXT_DEFAULT_STYLE, Font.PLAIN, Const.TEXT_DEFAULT_SIZE));

		jbtnReset = new JButton("Сброс счетчика");
		jbtnReset.setFont(new Font(Const.TEXT_DEFAULT_STYLE, Font.PLAIN, Const.TEXT_DEFAULT_SIZE));


		JPanel jpDir = new JPanel();
		jpDir.setLayout(new BoxLayout(jpDir, BoxLayout.X_AXIS));
		jpDir.add(jcmboxComPort);
		jpDir.add(jbtnReset);

		//==========================================
		// 		Add all panels to main panel   
		//==========================================
		mainFrame = new JFrame();
		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.add(jpPulseCounter, BorderLayout.CENTER);
		mainFrame.add(jpDir, BorderLayout.NORTH);
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setSize(400, 300);
		mainFrame.setVisible(true);
	}

	/**
	 * Установка действий к компонентам UI
	 */
	private void setUIActions() {
		jbtnReset.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Const.dialogConfirm()) {
					System.out.println("Yes answer");
					pulseCounter = 0;    // TODO: 06.08.18 added confirm dialog
					jtfPulseCounter.setText(String.valueOf(pulseCounter));
				}

			}
		});

		jcmboxComPort.setSelectedIndex(0); //  устанавливаем порт по умолчанию
		jcmboxComPort.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				System.out.println("jcmboxComPort#itemStateChanged");
				String portName = jcmboxComPort.getSelectedItem().toString();
				if (uart.getSerialPort() != null && uart.getSerialPort().isOpened()) {
					try {
						uart.getSerialPort().closePort();
						if (uart.uartInit(portName)) {
//							uart.attemptConnectToDevice();
						} else {
							System.out.println("\t 0");
							Const.msgPortClosed(portName); // TODO: 07.08.18 when device not fount - call 0
						}
					} catch (SerialPortException e1) {
						e1.printStackTrace();
					}
				} else {
					if (!uart.uartInit(portName) ) {
						System.out.println("\t 1");
						Const.msgPortClosed(portName); // TODO: 07.08.18 when device not fount - call 1
					}
				}
			}
		});
	}




	@Override
	public void incrementCounter() {
		System.out.println("@incrementCounter");
		pulseCounter++;
		jtfPulseCounter.setText(String.valueOf(pulseCounter));
	}

	@Override
	public void deviceConnected(int num, String name) {
		jcmboxComPort.setSelectedItem(name);
	}

}
