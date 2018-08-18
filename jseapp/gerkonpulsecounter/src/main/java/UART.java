import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Управление COM портом
 */
public class UART {

	private static final boolean isLog = true;

	private CallbackToUI callbackUART;
	private SerialPort serialPort;
	private UI uiEntry;

	/**
	 * Временный буфер при приеме данных с COM порта
	 */
	private byte[] rxDataBuff;


	/**
	 * COM port position in {@link Const#COM_PORTS} array
	 */
	private int portIndex = 0;

	/**
	 * COM port name in {@link Const#COM_PORTS} array
	 */
	private String portName;


	/**
	 * Флаг обнаружения устройства
	 * true - устройство опознано
	 */
	private boolean isDeviceFound = false;

	/**
	 * Счетчик правильно полученных символов одной команды от устройства.
	 * Команда состоит из нескольких символов.  Тут работет принцип транзакции.
	 * Если все символы одной команды совпадают - команда
	 * считаеться выполненной. Этот счетчик
	 * применен для определения команды иницализации.
	 */
	private int responseCount = 0;



	public UART(UI uiEntry) {
		this.uiEntry = uiEntry;
		callbackUART = uiEntry;
	}



	/**
	 * Инициализация COM порта.
	 * @param portName имя COM порта (например com1, ttyACM0)
	 * @return true - порт открыт
	 */
	public boolean uartInit(String portName){
		log("#uartInit");
//		System.out.print("portName = " + portName + " ");
		serialPort = new SerialPort(portName);
		try {
			serialPort.openPort();
			serialPort.setParams(SerialPort.BAUDRATE_9600,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE);
			//Включаем аппаратное управление потоком (для FT232 нжуно отключать)
//            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
//                                          SerialPort.FLOWCONTROL_RTSCTS_OUT);
			serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);
			log("\tport " + portName + " open !");
			return true;
		}
		catch (SerialPortException ex) {
//			log(ex);
//			JOptionPane.showMessageDialog(null, "Port is close !", "Warning", JOptionPane.WARNING_MESSAGE);
			log("\tport " + portName + " close" + "\n");
		}
		return false;
	}


	/**
	 * Thread pending response some time from device when request command {@link Const#REQUEST_INIT_DEVICE} is sent.
	 * The waiting time is determined {@link DevicePendingResponseTimer#PENDING_RESPONSE_TIME_MS} .
	 * If in this time device is not response or response does not match
	 * from {@link Const#RESPONSE_INIT_DEVICE} command - continue scanning next COM ports.
	 *
	 * This thread intended for wait only if not any response from COM port (not call {@link #tryInitDeviceResponse}) !
	 */
	private class DevicePendingResponseTimer extends Thread {
		private static final int PENDING_RESPONSE_TIME_MS = 500;

		@Override
		public void run() {
			log("C DevicePendingResponseTimer#run");
			try {
				Thread.sleep(PENDING_RESPONSE_TIME_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!isDeviceFound) {
				log("C DevicePendingResponseTimer->call #autoConnect ");
				autoConnect();
			}
		}
	}

	/**
	 * Автоподключение к ком порту устройства.
	 * Выполняеться полный перебор всех портов в ОС. Далее каждый открытый порт
	 * проверяеться на пренадлежность к устройству, т.к. к ОС может быть подключено
	 * несколько устройств.
	 */
	public void autoConnect() {
		log("#autoConnect");
		if (isDeviceFound) return;

		for (; portIndex < Const.COM_PORTS.length; portIndex++) {
			portName = Const.COM_PORTS[portIndex];
			if (uartInit(portName)) {	// if port is open
				tryInitDeviceRequest();
				break;
			}
		}
	}

	/**
	 * Выполнение попытки нахождения устройства.
	 * Запрос к устройству.
	 */
	public void tryInitDeviceRequest() {
		log("#tryInitDeviceRequest");

		// Задержка нужна после иниц. порта и перед отправкой Байта в этот открытый порт.
		// Важно. Для обнаружения устройств на основе arduino для открытия порта задержка должна быть 1000...2000 ms.
		// Если задержка меньше, arduino не успевает отреагировать и не отправляет ответ.
		// Проверено на arduino nano, uno.
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		DevicePendingResponseTimer pendingRespTimer = new DevicePendingResponseTimer();
		pendingRespTimer.start();

		try {
			serialPort.writeByte(Const.REQUEST_INIT_DEVICE);
//			Thread.sleep(75);
		} catch (SerialPortException e) {
//		} catch (SerialPortException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Выполнение попытки нахождения устройства.
	 * Проверка ответа от устройства.
	 */
	private void tryInitDeviceResponse() {
		log("#tryInitDeviceResponse");

		int rxResponse = 0;
		try {
			rxResponse = serialPort.readIntArray()[0];
			log("\tresponse data = " + rxResponse);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

		if (rxResponse == Const.RESPONSE_INIT_DEVICE) {
			isDeviceFound = true;
			callbackUART.deviceConnected(portIndex, portName);
			log("\tisDeviceFound = " + isDeviceFound);
		}
	}

	/**
	 * Обработчик данных получаемых данных от COM порта.
	 * Метод {@link PortReader#serialEvent(SerialPortEvent)}
	 * срабатывает при получении данных от COM порта.
	 */
	private class PortReader implements SerialPortEventListener {
		@Override
		public void serialEvent(SerialPortEvent event) {
			synchronized(event){    // TODO: 06.08.18 may be remove sync this if statement
				if(event.isRXCHAR() && event.getEventValue() > 0){
					log("#serialEvent");
					if (isDeviceFound) {
						decoder();
					} else {
						tryInitDeviceResponse();
					}
//					log("@ rx = " + rxDataBuff[0]);
				}
			}
		}
	}

	/**
	 * Расшифровка потока байт полученных от устройства на команды .
	 */
	private void decoder() {
		log("#decoder");
		try {
			rxDataBuff = serialPort.readBytes();
			log("\t rxDataBuff[0]=" + rxDataBuff[0]);
			if (rxDataBuff == null) return;

			if (rxDataBuff[0] == Const.CMD_PULSE_INCREMENT) {
				callbackUART.incrementCounter();
				// clean buffer
				for (int i = 0; i < rxDataBuff.length; i++) {
					rxDataBuff[0] = 0;
				}
			}

		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}



	public SerialPort getSerialPort() {
		return serialPort;
	}

	public boolean isDeviceFound() {
		return isDeviceFound;
	}


	/**
	 * Этот интерфейс должен реализовать тот класс, который
	 * будет обрабатвыать получаемые результаты от COM порта устройства.
	 */
	public interface CallbackToUI {
		/**
		 * Метод должен вызываться при получении с COM порта данных.
		 */
		void incrementCounter();

		/**
		 * Callback for pass COM port number and name which correspond {@link Const#COM_PORTS} array.
		 *
		 * @param num COM port number found on which device is connected.
		 *            This number is index of array of {@link Const#COM_PORTS}.
		 * @param name name of this COM port
		 */
		void deviceConnected(int num, String name);
	}

	private static void log(String str) {
		if (isLog) System.out.println("UART:\t" + str);
	}
}
