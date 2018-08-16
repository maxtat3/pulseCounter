import javax.swing.*;
import java.awt.*;

/**
 * General constants used in app.
 */
public class Const {

	public static final String[] COM_PORTS = {
		"COM1", "COM2", "COM3", "COM4", "COM5",
		"COM6", "COM7", "COM8", "COM9", "COM10",
		"/dev/ttyACM1", "/dev/ttyACM2", "/dev/ttyACM3", "/dev/ttyACM0",
		"/dev/ttyUSB0", "/dev/ttyUSB1", "/dev/ttyUSB2", "/dev/ttyUSB3",
	};

	/**
	 * Запрос инициализации устройства. Приложение посылает устройству эту строку.
	 * host(this app) -> device
	 */
	public static final byte REQUEST_INIT_DEVICE = 0x31;

	/**
	 * Ответ инициализации устройства. Устройство возвращает эту строку если
	 * запрос иницализации {@link #REQUEST_INIT_DEVICE} прошел успешно.
	 * host(this app) <- device
	 */
	public static final int RESPONSE_INIT_DEVICE = 0x70;

	/**
	 * Команда увеличения (инкркментирования) счетчика импульсов на 1.
	 */
	public static final int CMD_PULSE_INCREMENT = 0x55;


	public static final String TEXT_DEFAULT_STYLE = "Veranda";
	public static final int TEXT_DEFAULT_SIZE = 18;


	/**
	 * Вывод сообщения если устройство не найдено.
	 * Вызывается как модальное окно.
	 */
	public static void msgDeviceNotFound() {
		JOptionPane.showMessageDialog(null, "Устройство не обнаружено. Попробуйте указать COM порт выручную.",
			"Предупреждение", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Вывод сообщения если COM порт закрыт.
	 * Вызывается как модальное окно.
	 */
	public static void msgPortClosed(String port) {
		// https://stackoverflow.com/questions/26913923/change-size-e-font-joptionpane
		JLabel jlMsg = new JLabel("Порт " + port + " закрыт !");
		jlMsg.setFont(new Font(TEXT_DEFAULT_STYLE, Font.PLAIN, TEXT_DEFAULT_SIZE));
		JOptionPane.showMessageDialog(null, jlMsg, "Предупреждение", JOptionPane.WARNING_MESSAGE);

		// without change message text size
//		JOptionPane.showMessageDialog(null, "Порт " + port + " закрыт !", "Предупреждение", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Show confirm dialog with two variants - clear pulse counter.
	 * Show as modal window.
	 *
	 * @return true - Yes button pressed, otherwise false
	 */
	public static boolean dialogConfirm() {
		JLabel jlMsg = new JLabel("Действительно очистить счетчик ?");
		jlMsg.setFont(new Font(TEXT_DEFAULT_STYLE, Font.PLAIN, TEXT_DEFAULT_SIZE));

		// https://stackoverflow.com/questions/18286027/how-to-change-yes-no-option-in-confirmation-dialog
		String[] options = new String[2];
		options[0] = "<html><h1 style='font-family: " + TEXT_DEFAULT_STYLE + "; font-size: " + TEXT_DEFAULT_SIZE + "pt;'>Да";
		options[1] = "<html><h1 style='font-family: " + TEXT_DEFAULT_STYLE + "; font-size: " + TEXT_DEFAULT_SIZE + "pt;'>Нет";
		int res = JOptionPane.showOptionDialog(null, jlMsg, "Предупреждение", JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE, null, options, null);

		// Without change text size in buttons
//		int res = JOptionPane.showConfirmDialog (null, jlMsg, "Предупреждение", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

		return res == JOptionPane.YES_OPTION;
	}
}
