import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
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
import java.sql.Time;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.JLabel;

public class ShiftSubs {

	private JFrame frmShiftSubsTo;
	private JTextField pathTF;
	private JTextField delayTF;
	private String absolutePath;
	private int delay;
	private BufferedReader in;
	private BufferedWriter out;
	private JLabel lblPath;
	private JLabel lblDelay;

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
		frmShiftSubsTo.setTitle("Shift subs to DELAY seconds");
		frmShiftSubsTo.setBounds(100, 100, 450, 215);
		frmShiftSubsTo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmShiftSubsTo.getContentPane().setLayout(null);

		pathTF = new JTextField();
		pathTF.setBounds(80, 50, 339, 20);
		frmShiftSubsTo.getContentPane().add(pathTF);
		pathTF.setColumns(10);

		delayTF = new JTextField();
		delayTF.setBounds(80, 97, 115, 20);
		frmShiftSubsTo.getContentPane().add(delayTF);
		delayTF.setColumns(10);

		JButton btnMakeShift = new JButton("Make shift");
		btnMakeShift.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				absolutePath = pathTF.getText();
				delay = Integer.parseInt(delayTF.getText());
				File before = new File(absolutePath).getAbsoluteFile(), after = new File("new.srt").getAbsoluteFile();
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
				delayTF.setText("OK");
			}
		});
		btnMakeShift.setBounds(80, 153, 115, 23);
		frmShiftSubsTo.getContentPane().add(btnMakeShift);
		
		lblPath = new JLabel("Path to *.srt:");
		lblPath.setBounds(10, 49, 72, 23);
		frmShiftSubsTo.getContentPane().add(lblPath);
		
		lblDelay = new JLabel("Delay:");
		lblDelay.setBounds(33, 92, 49, 30);
		frmShiftSubsTo.getContentPane().add(lblDelay);
	}

	private void shift() throws IOException {
		String str;
		StringBuffer sb;
		int h = 0, m = 0, s = 0, t = 0;
		while (true) {
			sb = new StringBuffer();
			str = in.readLine();
			while (true) {
				if (str == null)
					return;
				if("504".equals(str)){
					h=0;
					m=0;
					s=0;
				}
				if (!Pattern.compile(".*-->.*").matcher(String.valueOf(str))
						.matches()){
					out.append(str + "\n");
					out.flush();
				}
				else
					break;
				str = in.readLine();
			}
			shiftOnDelay(str, sb, h, m, s, t);
		}
	}
	
	private void shiftOnDelay(String str, StringBuffer sb, int h, int m, int s,int t) throws IOException{
		String[] result = str.split(",");
		String[] time = result[0].split(":");
		t = Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
				* 60 + Integer.parseInt(time[2]) + delay;
		h = t / 3600;
		m = t % 3600 / 60;
		s = t % 60;
		sb.append(String.format("%02d:%02d:%02d,", h, m, s));
		time = result[1].split(" ");
		sb.append(time[0] + " " + time[1] + " ");
		time = time[2].split(":");
		t = Integer.parseInt(time[0]) * 3600 + Integer.parseInt(time[1])
				* 60 + Integer.parseInt(time[2]) + delay;
		h = t / 3600;
		m = t % 3600 / 60;
		s = t % 60;
		sb.append(String.format("%02d:%02d:%02d,%s\n", h, m, s, result[2]));
		out.append(sb);
		out.flush();
	}
}
