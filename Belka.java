//--- Java imports ---
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

//--- Application imports ---
import belka.*;
import belka.mol.*;
import belka.draw.*;
import belka.menu.*;

/**
 * Class containing main function to start Belka.
 *
 * @author Alexej Abyzov
 */
public class Belka
{
    /**
     * Starts the Belka. The following actions are performed:
     * <ul>
     * <li> Set system specific settings
     * <li> Set look and feel
     * <li> Create menus
     * <li> Create main frame
     * <li> Create {@link BelkaManager} object
     * <li> Pass program control to {@link BelkaManager}
     * </ul>
     */
    public static void main(String[] args)
    {
	// To have apple menus in top bar and not in every window
	System.setProperty("apple.laf.useScreenMenuBar","true");

	// Processing input
	boolean useGraphics = true;
	ArrayList<String> arrListFileNames = new ArrayList<String>(4);
	for (int i = 0;i < args.length;i++) {
	    String word = args[i];
	    if (word.startsWith("-")) {
		if (word.equalsIgnoreCase("-nodisplay")) useGraphics = false;
	    } else {
		arrListFileNames.add(word);
	    }
	}

	// Saving filenames in an array
	int nFileNames = arrListFileNames.size();
	String[] fileNames = new String[nFileNames];
	for (int i = 0;i < nFileNames;i++)
	    fileNames[i] = arrListFileNames.get(i).toString();

	BelkaManager manager = null;

	// Trying open the application with graphics
	if (useGraphics) try {
	    // Setting look and feel
	    try {
		UIManager.
		    setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
// 		UIManager.
// 		    setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
	    } catch (Exception e) {}

	    // Creating main frame
	    JFrame frame = new JFrame();
	    frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent event)
		    {
			System.exit(0);
		    }
		});
	
	    // Creating manager
	    manager = new BelkaManager(frame.getContentPane(),fileNames);

	    // Creating menus
	    JMenuBar menuBar = new JMenuBar();
	    menuBar.add(new MenuFile(manager));
	    menuBar.add(new MenuDisplay(manager));
	    menuBar.add(new MenuColor(manager));
	    menuBar.add(new MenuSelect(manager));
	    frame.setJMenuBar(menuBar);
	    
	    // Making frame visible
	    frame.setBounds(new Rectangle(0,0,600,600));
	    frame.setVisible(true);
	} catch (Exception e) {
	    System.err.println("Can't open display.");
	    System.err.println("Switching to the text mode.");
	    useGraphics = false;
	}

	// Open the application without graphics
	if (!useGraphics) {
	    System.setProperty("java.awt.headless","true");
	    manager = new BelkaManager(null,fileNames);
	}
	
	// Processing user input commands
	manager.run();
    }
}
