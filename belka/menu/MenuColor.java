package belka.menu;

//--- Java imports ---
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//--- Application imports ---
import belka.*;

/**
 * The class implements the menu 'Color' with items to change
 * color of molecule structure on a screen.
 *
 * @author Alexej Abyzov
 */
public class MenuColor extends JMenu
{
    // Menu items
    JMenuItem moleculeMI_;
    JMenuItem nmrModelMI_;
    JMenuItem chainMI_;
    JMenuItem secStrMI_;
    JMenuItem residueMI_;
    JMenuItem atomMI_;
    JMenuItem temperatureMI_;
    JMenuItem seChainMI_;
    JMenuItem seMolMI_;
    JMenu     resPropMenu; // Submenu 'By residue properties'
    JMenuItem propZappoMI_;
    JMenuItem propTaylorMI_;
    JMenuItem propHydrophMI_;
    JMenuItem propHelixMI_;
    JMenuItem propSheetMI_;
    JMenuItem propTurnMI_;
    JMenuItem propSurfMI_;
    JMenu     chooseMenu;  // Submenu 'Choose color'
    JMenuItem colorAtomsMI_;
    JMenuItem colorBondsMI_;
    JMenuItem colorBackboneMI_;
    JMenuItem colorBackgroundMI_;
    
    // Reference to manager
    BelkaManager manager_ = null;

    /**
     * Object constructor
     *
     * @param manager manager which will execute commands comming from the menu
     */
    public MenuColor(BelkaManager manager)
    {
	super("Color");
        
	manager_ = manager;

	MenuColorsListener menuColorsListener = new MenuColorsListener();
	
	add( (moleculeMI_ = new JMenuItem("By molecule")) );
	moleculeMI_.addActionListener(menuColorsListener);
	
	add( (nmrModelMI_ = new JMenuItem("By NMR model")) );
	nmrModelMI_.addActionListener(menuColorsListener);   
            
	add( (chainMI_ = new JMenuItem("By chain")) );
	chainMI_.addActionListener(menuColorsListener);
       
	add( (secStrMI_ = new JMenuItem("By secondary structure")) );
	secStrMI_.addActionListener(menuColorsListener);
	secStrMI_.setEnabled(false);

	add( (residueMI_ = new JMenuItem("By residue type")) );
	residueMI_.addActionListener(menuColorsListener);

	add( (atomMI_ = new JMenuItem("By atom type")) );
	atomMI_.addActionListener(menuColorsListener);

	add( (temperatureMI_ = new JMenuItem("By atom temperature")) );
	temperatureMI_.addActionListener(menuColorsListener);

	add( (seMolMI_ = new JMenuItem("Molecules start-to-end")) );
	seMolMI_.addActionListener(menuColorsListener);

	add( (seChainMI_ = new JMenuItem("Chains start-to-end")) );
	seChainMI_.addActionListener(menuColorsListener);

	addSeparator();

	resPropMenu = new JMenu("By residue properites");
	add(resPropMenu);

	resPropMenu.add( (propZappoMI_ = new JMenuItem("Zappo")) );
	propZappoMI_.addActionListener(menuColorsListener);
	propZappoMI_.setEnabled(false);

	resPropMenu.add( (propTaylorMI_ = new JMenuItem("Taylor")) );
	propTaylorMI_.addActionListener(menuColorsListener);
	propTaylorMI_.setEnabled(false);

	resPropMenu.add( (propHydrophMI_ = new JMenuItem("Hydrophobicity")) );
	propHydrophMI_.addActionListener(menuColorsListener);
	propHydrophMI_.setEnabled(false);

	resPropMenu.add( (propHelixMI_ = new JMenuItem("Helix propensity")) );
	propHelixMI_.addActionListener(menuColorsListener);
	propHelixMI_.setEnabled(false);

	resPropMenu.add( (propSheetMI_ = new JMenuItem("Sheet propensity")) );
	propSheetMI_.addActionListener(menuColorsListener);
	propSheetMI_.setEnabled(false);

	resPropMenu.add( (propTurnMI_ = new JMenuItem("Turn propensity")) );
	propTurnMI_.addActionListener(menuColorsListener);
	propTurnMI_.setEnabled(false);

	resPropMenu.add( (propSurfMI_ = new JMenuItem("Surface propensity")) );
	propSurfMI_.addActionListener(menuColorsListener);
	propSurfMI_.setEnabled(false);

	addSeparator();

	chooseMenu = new JMenu("Choose color");
	add(chooseMenu);

	chooseMenu.add( (colorAtomsMI_ = new JMenuItem("For atoms")) );
	colorAtomsMI_.addActionListener(menuColorsListener);
		
	chooseMenu.add( (colorBondsMI_ = new JMenuItem("For bonds")) );
	colorBondsMI_.addActionListener(menuColorsListener);
	colorBondsMI_.setEnabled(false);
		
	chooseMenu.add( (colorBackboneMI_ = new JMenuItem("For backbone")) );
	colorBackboneMI_.addActionListener(menuColorsListener);
	colorBackboneMI_.setEnabled(false);
		
	chooseMenu.add( (colorBackgroundMI_ =
			 new JMenuItem("For background")) );
	colorBackgroundMI_.addActionListener(menuColorsListener);
    }

    private class MenuColorsListener implements ActionListener
    {
	public void actionPerformed(ActionEvent event)
	{
	    Object source = event.getSource();
	    
	    if (source == chainMI_)
		manager_.runScript("color chain");
	    else if (source == moleculeMI_)
		manager_.runScript("color molecule");
	    else if (source == secStrMI_)
		manager_.runScript("color structure");
	    else if (source == residueMI_)
		manager_.runScript("color shapely");
	    else if (source == atomMI_)
		manager_.runScript("color cpk");
	    else if (source == seMolMI_)
		manager_.runScript("color groupmol");
	    else if (source == seChainMI_)
		manager_.runScript("color group");
	    else if (source == temperatureMI_)
		manager_.runScript("color temperature");
	    else if (source == nmrModelMI_)
		manager_.runScript("color model");
	    else if (source == colorAtomsMI_ ||
		     source == colorBondsMI_ ||
		     source == colorBackboneMI_ ||
		     source == colorBackgroundMI_) {

		String title = "Choose color for ";
		if (source == colorAtomsMI_)           title += "atoms";
		else if (source == colorBondsMI_)      title += "bonds";
		else if (source == colorBackboneMI_)   title += "backbone";
		else if (source == colorBackgroundMI_) title += "background";
		title += " ...";

		Color col = JColorChooser.showDialog(null,title,null);
		if (col == null) return;

		String command = "";
		if (source == colorAtomsMI_)
		    command += "color atom [";
		else if (source == colorBondsMI_)
		    command += "color bond [";
		else if (source == colorBackboneMI_)
		    command += "color backbone [";
		else if (source == colorBackgroundMI_)
		    command += "color background [";
		command += col.getRed() + ",";
		command += col.getGreen() + ",";
		command += col.getBlue() + "]";
		manager_.runScript(command);
	    }
	}
    }
}
