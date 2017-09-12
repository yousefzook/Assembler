package View;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;

import Assembler.Pass1;

public class MenuBarListener implements javax.swing.event.MenuListener {

	private JMenu open, assemble, exit;

	public MenuBarListener(JMenu open, JMenu assemble, JMenu exit) {
		// TODO Auto-generated constructor stub
		this.open = open;
		this.assemble = assemble;
		this.exit = exit;
	}

	@Override
	public void menuSelected(MenuEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource().equals(exit))
			System.exit(0);
		else if (e.getSource().equals(open)) {
			Pass1 pass1 = new Pass1();
			pass1.Start();
		}
	}

	@Override
	public void menuDeselected(MenuEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void menuCanceled(MenuEvent e) {
		// TODO Auto-generated method stub

	}

}
