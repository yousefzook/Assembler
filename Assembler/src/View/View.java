package View;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.sun.glass.events.KeyEvent;

public class View {

	private JFrame frame;
	private JPanel panel;
	private JTextArea outArea;
	private JScrollPane scrollPane;
	private JMenuBar menuBar;
	private JMenu openMenu, assembleMenu, exitMenu;
	private MenuBarListener menuListener;

	public View() {
		initialize();
		setSize();
		setVisibility();
		addComponentes();
	}

	private void initialize() {
		frame = new JFrame();
		panel = new JPanel();
		outArea = new JTextArea("Here");
		scrollPane = new JScrollPane(outArea);
		menuBar = new JMenuBar();
		openMenu = new JMenu("Open");
		assembleMenu = new JMenu("Assemble");
		exitMenu = new JMenu("Exit");
		menuListener = new MenuBarListener(openMenu , assembleMenu , exitMenu);
	}

	private void setSize() {
		openMenu.setMnemonic(KeyEvent.VK_O);
		assembleMenu.setMnemonic(KeyEvent.VK_A);
		exitMenu.setMnemonic(KeyEvent.VK_E);
		outArea.setPreferredSize(new Dimension(1000, 1000));
		scrollPane.setPreferredSize(new Dimension(890, 510));
		panel.setSize(890, 520);
		frame.setSize(900, 600);
		frame.setResizable(false);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2,
				dim.height / 2 - frame.getSize().height / 2);
	}

	private void setVisibility() {
		scrollPane.setVisible(true);
		outArea.setVisible(true);
		outArea.setEditable(true);
		panel.setVisible(true);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void addComponentes() {
		menuBar.add(openMenu);
		menuBar.add(assembleMenu);
		menuBar.add(exitMenu);
		openMenu.addMenuListener(menuListener);
		assembleMenu.addMenuListener(menuListener);
		exitMenu.addMenuListener(menuListener);
		panel.add(scrollPane);
		frame.add(panel);
		frame.setJMenuBar(menuBar);
	}
}
