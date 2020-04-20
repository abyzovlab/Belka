package belka.menu;

//--- Java imports ---
import java.awt.event.*;
import javax.swing.*;

//--- Application imports ---
import belka.*;

/**
 * The class implements the menu 'Display' with items to change
 * representation of molecule structure on a screen.
 *
 * @author Alexej Abyzov
 */
public class MenuDisplay extends JMenu
{
    // Menu items
    private JMenuItem wireframeMI_;
    private JMenuItem backboneMI_;
    private JMenuItem traceMI_;
    private JMenuItem sticksMI_;
    private JMenuItem spacefillMI_;
    private JMenuItem ballsticksMI_;
    private JMenuItem ribbonsMI_;
    private JMenuItem strandsMI_;
    private JMenuItem cartoonsMI_;
     
    // Reference to manager
    BelkaManager manager_ = null;

    /**
     * Object constructor
     *
     * @param manager manager which will execute commands comming from the menu
     */
    public MenuDisplay(BelkaManager manager)
    {
	super("Display");

	manager_ = manager;

	MenuDisplayListener menuDisplayListener = new MenuDisplayListener();

 	add( (spacefillMI_ = new JMenuItem("Spacefill")) );
 	spacefillMI_.addActionListener(menuDisplayListener);

	add( (wireframeMI_ = new JMenuItem("Wireframe")) );
	wireframeMI_.addActionListener(menuDisplayListener);
	
 	add( (sticksMI_ = new JMenuItem("Sticks")) );
 	sticksMI_.addActionListener(menuDisplayListener);

 	add( (ballsticksMI_ = new JMenuItem("Ball & sticks")) );
 	ballsticksMI_.addActionListener(menuDisplayListener);

 	add( (backboneMI_ = new JMenuItem("Backbone")) );
 	backboneMI_.addActionListener(menuDisplayListener);
	
 	add( (traceMI_ = new JMenuItem("Trace")) );
 	traceMI_.addActionListener(menuDisplayListener);
	traceMI_.setEnabled(false);
	
 	add( (ribbonsMI_ = new JMenuItem("Ribbons")) );
 	ribbonsMI_.addActionListener(menuDisplayListener);
	ribbonsMI_.setEnabled(false);

 	add( (strandsMI_ = new JMenuItem("Strands")) );
 	strandsMI_.addActionListener(menuDisplayListener);
	strandsMI_.setEnabled(false);

 	add( (cartoonsMI_ = new JMenuItem("Cartoons")) );
 	cartoonsMI_.addActionListener(menuDisplayListener);
	cartoonsMI_.setEnabled(false);
     }

    private class MenuDisplayListener implements ActionListener, ItemListener
    {
	public void actionPerformed(ActionEvent event)
	{
	    if (manager_ == null) return;
	    Object source = event.getSource();

	    if (source == spacefillMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons  off]" +
				   "[ribbons   off]" +
				   "[spacefill  on]" +
				   "[strands   off]" +
				   "[trace     off]" +
				   "[wireframe off]");
	    else if (source == wireframeMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons  off]" +
				   "[ribbons   off]" +
				   "[spacefill off]" +
				   "[strands   off]" +
				   "[trace     off]" +
				   "[wireframe   0]");
	    else if (source == sticksMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons  off]" +
				   "[ribbons   off]" +
				   "[spacefill off]" +
				   "[strands   off]" +
				   "[trace     off]" +
				   "[wireframe 0.1]");
	    else if (source == ballsticksMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons  off]" +
				   "[ribbons   off]" +
				   "[spacefill 0.2]" +
				   "[strands   off]" +
				   "[trace     off]" +
				   "[wireframe 0.05]");
	    else if (source == backboneMI_)
		manager_.runScript("[backbone  0.1]" +
				   "[cartoons  off]" +
				   "[ribbons   off]" +
				   "[spacefill off]" +
				   "[strands   off]" +
				   "[trace     off]" +
				   "[wireframe off]");
	    else if (source == traceMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons  off]" +
				   "[ribbons   off]" +
				   "[spacefill off]" +
				   "[strands   off]" +
				   "[trace      on]" +
				   "[wireframe off]");
	    else if (source == ribbonsMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons  off]" +
				   "[ribbons    on]" +
				   "[spacefill off]" +
				   "[strands   off]" +
				   "[trace     off]" +
				   "[wireframe off]");
	    else if (source == strandsMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons  off]" +
				   "[ribbons   off]" +
				   "[spacefill off]" +
				   "[strands    on]" +
				   "[trace     off]" +
				   "[wireframe off]");
	    else if (source == cartoonsMI_)
		manager_.runScript("[backbone  off]" +
				   "[cartoons   on]" +
				   "[ribbons   off]" +
				   "[spacefill off]" +
				   "[strands   off]" +
				   "[trace     off]" +
				   "[wireframe off]");
	}

	public void itemStateChanged(ItemEvent event)
	{
	    if (manager_ == null) return;
	    Object source = event.getSource();
	    
// 	    if (source == hbondMI_)
// 		if (hbondMI_.getState())  manager_.runCommand("hbond on");
// 		else                     manager_.runCommand("hbond off");
// 	    else if (source == ssbondMI_)
// 		if (ssbondMI_.getState()) manager_.runCommand("ssbond on");
// 		else                     manager_.runCommand("ssbond off");
	}
    }
}
