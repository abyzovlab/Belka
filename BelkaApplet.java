//--- Java imports ---
import java.net.*;
import java.util.*;
import javax.swing.*;

//--- Application imports ---
import belka.*;

/**
 * Class called by browser to start Belka applet.
 *
 * @author Alexej Abyzov
 */
public class BelkaApplet extends JApplet
{
    // Manager executing commands
    BelkaManager manager_ = null;
 
    /**
     * Starts the Belka applet. Following actions are performed:
     * <ul>
     * <li> Set look and feel
     * <li> Obtain codebase of the applet
     * <li> Put input parameters into array of arguments
     * <li> Create {@link BelkaManager} object
     * </ul>
     */
    public void init()
    {
	// Setting look and fill
	try {
	    UIManager.
		setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception e) { }

	// Getting URL of applet
	URL codeBase = getCodeBase();

	// Parsing input parameters
        String args_string = getParameter("args");
        String[] args = null;
        if (args_string != null) {
            StringTokenizer all_args = new StringTokenizer(args_string,",");
	    if (all_args != null) {
		int n_args = all_args.countTokens();
		if (n_args > 0) {
		    args = new String[n_args];
		    for (int i = 0;all_args.hasMoreTokens();i++)
			args[i] = all_args.nextToken().trim();
		}
	    }
        }

	// Creating manager
	manager_ = new BelkaManager(getContentPane(),args,codeBase);

	// Executing initial script
        String script = getParameter("script");
	if (script != null) play(script);
    }

    /**
     * Destroys applet.
     */
    public void destroy()
    {
	// Clering graphical panel
	getContentPane().removeAll();

	// Clearing manager
	manager_.destroy();
	manager_ = null;

	// Calling garbage collector
	System.gc();
    }

    /**
     * The function executes set of commands inputed by 'script'.
     *
     * @param script script of commands.
     */
    public String play(String script)
    {
	if (manager_ == null) return "";
	return manager_.runScript(script);
    }

    /**
     * Returns the number of rigid block found in all molecules.
     *
     * @return number of rigid blocks.
     */
    public int getNRigids()
    {
 	if (manager_ != null) return manager_.getLargestGroup();
 	return 0;
    }

    /**
     * Returns information about applet
     *
     * @return information about applet
     */
    public String getAppletInfo()
    {
	return "Belka applet.";
    }

    
}
