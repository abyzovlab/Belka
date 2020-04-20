package belka.menu;

//--- Java imports ---
import java.awt.event.*;
import javax.swing.*;

//--- Application imports ---
import belka.*;

/**
 * The class implements the menu 'Select' with items to select
 * sets of atoms in loaded molecules.
 *
 * @author Alexej Abyzov
 */
public class MenuSelect extends JMenu
{
    // Menu items
    private JMenuItem selectAllMI_;
    private JMenuItem selectAlignedMI_;
    private JMenu     rigidsMenu_;
    private JMenuItem[] rigidsMI_ = new JMenuItem[0];
     
    // Reference to manager
    BelkaManager manager_ = null;

    /**
     * Object constructor
     *
     * @param manager manager which will execute commands comming from the menu
     */
    public MenuSelect(BelkaManager manager)
    {
	super("Select");

	manager_ = manager;

	MenuSelectListener menuSelectListener = new MenuSelectListener();

 	add( (selectAllMI_ = new JMenuItem("... all")) );
 	selectAllMI_.addActionListener(menuSelectListener);

 	add( (selectAlignedMI_ = new JMenuItem("... aligned")) );
 	selectAlignedMI_.addActionListener(menuSelectListener);

 	add( (rigidsMenu_ = new JMenu("Rigid #")) );
	rigidsMenu_.addMouseListener(new MenuRigidsMouseListener());

     }

    private class MenuSelectListener implements ActionListener
    {
	public void actionPerformed(ActionEvent event)
	{
	    if (manager_ == null) return;
	    Object source = event.getSource();

	    if (source == selectAllMI_)
		manager_.runScript("select all");
	    else if (source == selectAlignedMI_)
		manager_.runScript("select aligned");
	    else
		for (int i = 0;i < rigidsMI_.length;i++)
		    if (source == rigidsMI_[i])
			manager_.runScript("select group " +
					   Integer.parseInt(rigidsMI_[i].getText()));
	}
    }

    private class MenuRigidsMouseListener implements MouseListener
    {
	public void mouseClicked(MouseEvent e)
	{}
	public void mouseEntered(MouseEvent e)
	{
	    int n_rigids = manager_.getLargestGroup();
	    if (rigidsMI_.length == n_rigids) return;

	    rigidsMenu_.removeAll();
	    rigidsMI_ = new JMenuItem[n_rigids];
	    MenuSelectListener acl = new MenuSelectListener();
	    for (int i = 1;i <= n_rigids;i++) {
		int ind = i - 1;
		rigidsMI_[ind] = new JMenuItem(Integer.toString(i));
		rigidsMI_[ind].addActionListener(acl);
		rigidsMenu_.add(rigidsMI_[ind]);
	    }
	}

	public void mouseExited(MouseEvent e)
	{}
	public void mousePressed(MouseEvent e)
	{}
	public void mouseReleased(MouseEvent e)
	{}
    }
}
