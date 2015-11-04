import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ShiftSubs {

	private JFrame frmShiftSubsTo;
	private int delay;
	private BufferedReader in;
	private BufferedWriter out;
	private JLabel lblDelay;
	private JFileChooser fileChooser;
	private File before, after;
	private JSpinner spinner;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ShiftSubs window = new ShiftSubs();
					window.frmShiftSubsTo.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ShiftSubs() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmShiftSubsTo = new JFrame();
		frmShiftSubsTo.setTitle("Shift subtitles");
		frmShiftSubsTo.setBounds(100, 100, 219, 215);
		frmShiftSubsTo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmShiftSubsTo.getContentPane().setLayout(null);
		
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Subtitle Files", "srt"));

		JButton btnMakeShift = new JButton("Make shift");
		btnMakeShift.setEnabled(false);
		btnMakeShift.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				delay = Integer.parseInt(spinner.getValue().toString());
				after = new File("new.srt").getAbsoluteFile();
				try {
					in = new BufferedReader(new FileReader(before));
					out = new BufferedWriter(new FileWriter(after));
					shift();
					in.close();
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				before.delete();
				after.renameTo(before);
				JOptionPane.showMessageDialog( frmShiftSubsTo, "Done!");
			}
		});
		btnMakeShift.setBounds(25, 127, 154, 39);
		frmShiftSubsTo.getContentPane().add(btnMakeShift);
		
		lblDelay = new JLabel("Delay:");
		lblDelay.setBounds(49, 75, 49, 41);
		frmShiftSubsTo.getContentPane().add(lblDelay);
		
		JButton btnSelectTargetsrt = new JButton("Select target *.srt file");
		btnSelectTargetsrt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int result = fileChooser.showOpenDialog(frmShiftSubsTo);
				if (result == JFileChooser.APPROVE_OPTION) {
					before = fileChooser.getSelectedFile();
					if(before.getAbsolutePath().endsWith(".srt")){
						btnMakeShift.setEnabled(true);
						spinner.setEnabled(true);
					} else{
						btnMakeShift.setEnabled(false);
						spinner.setEnabled(false);
						JOptionPane.showMessageDialog( frmShiftSubsTo, "Selected file is not *.srt!");
					}
				}
			}
		});
		btnSelectTargetsrt.setBounds(25, 25, 154, 39);
		frmShiftSubsTo.getContentPane().add(btnSelectTargetsrt);
		
		spinner = new JSpinner();
		spinner.setEnabled(false);
		spinner.setModel(new SpinnerNumberModel(0, -60, 60, 1));
		spinner.setBounds(98, 80, 51, 36);
		frmShiftSubsTo.getContentPane().add(spinner);
	}

	private void shift() throws IOException {
		String str;
		StringBuffer sb;
		int h = 0, m = 0, s = 0, timeOfShift = 0;
		while (true) {
			sb = new StringBuffer();
			str = in.readLine();
//			out.append("È");
//			out.flush();
			while (true) {
				if (str == null)
					return;
				if (!Pattern.compile(".*-->.*").matcher(String.valueOf(str))
						.matches()){
					out.append(new String(str + "\n"));
					out.flush();
				}
				else
					break;
				str = in.readLine();
			}
			shiftOnDelay(str, sb, h, m, s, timeOfShift);
		}
	}
	
	private void shiftOnDelay(String str, StringBuffer sb, int h, int m, int s, int timeOfShift) throws IOException{
		String[] result = str.split(",");
		String[] time = result[0].split(":");
		timeOfShift = Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
				* 60 + Integer.parseInt(time[2]) + delay;
		h = timeOfShift / 3600;
		m = timeOfShift % 3600 / 60;
		s = timeOfShift % 60;
		sb.append(String.format("%02d:%02d:%02d,", h, m, s));
		time = result[1].split(" ");
		sb.append(time[0] + " " + time[1] + " ");
		time = time[2].split(":");
		timeOfShift = Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
				* 60 + Integer.parseInt(time[2]) + delay;
		h = timeOfShift / 3600;
		m = timeOfShift % 3600 / 60;
		s = timeOfShift % 60;
		sb.append(String.format("%02d:%02d:%02d,%s\n", h, m, s, result[2]));
		out.append(sb);
		out.flush();
	}
}
