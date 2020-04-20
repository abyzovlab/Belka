package belka;

//--- Java import ---
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.util.zip.*;

//--- Application imports ---
import belka.mol.*;
import belka.draw.*;
import belka.chem.*;
import belka.parser.*;
import belka.align.*;
import belka.geom.*;

/**
 * The object of this class manages the life of Belka as a program or applet.
 * It performes two main functions:
 * <ul>
 * <li> Distribute molecules between drawing panels
 * <li> Process input commands
 * </ul>
 *
 * @author Alexej Abyzov
 */
public class BelkaManager implements ActionListener
{
    static final String INVITATION_LINE        = "Belka> ";
    static final double RASMOL_INT_TO_ANGSTROM = 0.004;
    static final String PDB_URL =
	"ftp://ftp.wwpdb.org/pub/pdb/data/structures/divided/pdb";

    /**
     * Object constructor meant to create object when Belka is used as an
     * applet.
     * 
     * @param drawContentPane main content pane for drawing. If it's null,
     * no graphical display is created.
     * @param args input files to load upon start up.
     * @param codeBase url where the input files are located. If null,
     * files are searched in current directory.
     */
    public BelkaManager(Container drawContentPane,String[] args,URL codeBase)
    {
	if (drawContentPane == null) return;

	// Do not allow to exit java VM
	allowExit_ = false;

	// Saving main component
	mainComponent_ = drawContentPane;
	
	// Saving graphics flag
	useGUI_ = false;

	// Saving code base
	codeBase_ = codeBase;

	// Creating molecule panel
	molPanels_    = new MolPanel[1];
	molPanels_[0] = new MolPanel();
	drawContentPane.add(molPanels_[0]);

	// Output panel
	inOutPanel_ = new InOutPanel("Belka>",false);
	inOutPanel_.addActionListener(this);
	drawContentPane.add(inOutPanel_,"South");

	// Loading input files
	if (args != null) {
	    String fileNames = "";
	    for (int i = 0;i < args.length;i++)
		fileNames += args[i] + " ";
	    CommandParser comm0 = new CommandParser(fileNames);
	    proceedToLoadCommand(molPanels_[0],comm0);
	}

	// Setting default representation
	centerMolecules();
	molPanels_[0].updateMoleculeView();
    }

    /**
     * Object constructor.
     * 
     * @param drawContentPane main content pane for drawing. If it's null,
     * no graphical display is created. This allows to use Belka in batch
     * mode.
     * @param args input files to load upon start up. Files are searched in
     * current directory.
     */
    public BelkaManager(Container drawContentPane,String[] args)
    {
	// Saving main component
	mainComponent_ = drawContentPane;
	
	// Saving graphics flag
	if (drawContentPane == null) useGUI_ = false;

	// Creating molecule panel
	molPanels_ = new MolPanel[1];
	molPanels_[0] = new MolPanel();

	// Creating input/output panel text field
	outTextArea_ = new JTextArea("Welcome to Belka\n");
	inOutPanel_ = new InOutPanel("Belka>");
	inOutPanel_.addActionListener(this);
	
	if (useGUI_) { // Creating output text area
	    OutputStream outStr = new BelkaOutputStream();
	    try {
		System.setOut(new PrintStream(outStr));
		System.setErr(new PrintStream(outStr));
	    } catch (Exception e) {}
	}

	// Adding component to the content pane
	JScrollPane scrollPane = new JScrollPane(outTextArea_);
	sPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,molPanels_[0],
			       scrollPane);

	sPane.setOneTouchExpandable(true);
	sPane.setResizeWeight(0.95);
	if (drawContentPane != null) {
	    drawContentPane.add(sPane,"Center");
	    drawContentPane.add(inOutPanel_,"South");
	}

	// Loading input files
	if (args != null) {
	    String fileNames = "";
	    for (int i = 0;i < args.length;i++)
		fileNames += args[i] + " ";
	    CommandParser comm0 = new CommandParser(fileNames);
	    proceedToLoadCommand(molPanels_[0],comm0);
	}

	// Setting default representation
	centerMolecules();
	molPanels_[0].updateMoleculeView();
    }

    /**
     * The method nulls internal pointers and disassembles molecules to help 
     * garbage collector to clear memory.
     */
    public void destroy()
    {
	for (int i = 0;i < molPanels_.length;i++)
	    molPanels_[i] = null;

	disassembleMolecules();
    }

    private void disassembleMolecules()
    {
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
	    mol.disassemble();
	moleculeList_ = lastMolecule_ = null;
    }

    // Flags
    private boolean connectFlag_ = true;


    // User input
    private String userInput_ = null;
    /**
     * To set user input to the manager.
     *
     * @param input user input.
     */ 
    public void setUserInput(String input) { userInput_ = input; }

    // List of molecules
    private Molecule moleculeList_ = null;
    private Molecule lastMolecule_ = null;
    public  Molecule moleculeList() { return moleculeList_; }
    public boolean addMolecule(Molecule molecule)
    {
	// Check if input is correct
	if (molecule == null) return false;

	// Check if it can be added
	if (molecule.next() != null) return false;
	if (molecule.prev() != null) return false;

	if (moleculeList_ == null) {
	    lastMolecule_ = moleculeList_ = molecule;
	} else {
	    if (!lastMolecule_.addAfter(molecule)) return false;
	    lastMolecule_ = molecule;
	}
	return true;
    }
    public boolean removeMolecule(Molecule molecule)
    {
	return true;
    }

    // Counting molecule
    public int countMolecules()
    {
	int ret = 0;
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next()) ret++;
	return ret;
    }

    // Flag to indicate whether program is allowed to exit java VM
    private boolean allowExit_ = true;

    // Main componenet to draw on
    private Container mainComponent_ = null;
    /**
     * Returns the main component to draw on
     *
     * @return the main component to draw on
     */
    public Component getMainComponent() { return mainComponent_; }

    // Place where to retrive files from
    private URL codeBase_ = null;
    
    // Flag to show if the program has to use graphics
    private boolean useGUI_ = true;

    // Molecule panel where to draw molecule
    private JSplitPane sPane      = null;
    private MolPanel[] molPanels_ = null;

    // Input/Output panel
    private InOutPanel inOutPanel_ = null;

    // Output text area
    private JTextArea outTextArea_ = null;

    /**
     * The function waits for user input and processes command. Function exits
     * only if the input command equals to 'end' or java exits.
     */
    public void run()
    {
	try {
	    while (System.in.available() != 0) {
		String input = "";
		int readInt = -1;
		while ((readInt = System.in.read()) != -1) {
		    char readChar = (char)readInt;
		    if (readChar == '\n') break;
		    input += readChar;
		}
		input = input.trim();
		if (input.length() != 0 && !input.startsWith("#")) {
		    // Print command use graphics or batch
		    System.out.println(INVITATION_LINE + input);
		    inOutPanel_.setText(input + "\n");
		    // Run command
		    runScript(input.trim());
		}
	    }
	} catch (Exception e) {
	    System.err.println(e.toString());
	}

	if (useGUI_) {
	    // I do this loop in order to avoid executing user typed
	    // commands from external thread, since it cases stack
	    // overflow in serialization. User typed commands are
	    // stored in variable userInput_ and this loop monitors for its
	    // value and executes it when there is any input.
	    while (hold(20)) 
		if (userInput_ != null) {
		    runScript(userInput_);
		    userInput_ = null;
		}
	    return;
	}

	// Text mode. Input from command line.
	while (true) {
	    try {
		// Ivitation in case no graphics and no batch mode
		System.out.print(INVITATION_LINE);

		String input = "";
		int readInt = -1;
		while ((readInt = System.in.read()) != -1) {
		    char readChar = (char)readInt;
		    if (readChar == '\n') break;
		    input += readChar;
		}
		if (input.length() != 0) {
		    // Run command
		    runScript(input.trim());
		}
	    } catch (Exception e) {
		System.err.println(e.toString());
	    }
	}
    }

    /**
     * Runs the script of command and updates screen view if necesary.
     * Commands are assumed to be put in square brackets [].
     * For example [backbone off][spacefill on].
     *
     * @param script string with script.
     */
    public String runScript(String script)
    {
	if (script == null) return "";

	StringBuffer mess = new StringBuffer();

	if (script.startsWith("[")) {
	    int begin = 0;
	    int count = 0;
	    int len = script.length();
	    for (int i = 0;i < len;i++) {
		char c = script.charAt(i);
		if (c == '[') {
		    if (count == 0) begin = i;
		    count++;
		} else if (c == ']') {
		    count--;
		    if (count == 0)
			try {
			    mess.append(runCommand(script.substring(begin + 1,
								    i),
						   false));
			} catch (Exception e) {
			    System.err.println(e.toString());
			}
		} else {
		    if (count == 0 && !Character.isWhitespace(c)) break;
		}
	    }
	} else
	    try {
		mess.append(runCommand(script,true));
	    } catch (Exception e) {
		System.err.println(e.toString());
	    }

	for (int m = 0;m < molPanels_.length;m++)
	    molPanels_[m].updateMoleculeView();

	String ret = mess.toString();
	System.out.print(ret);
	if (codeBase_ != null) inOutPanel_.setText(ret);
	return ret;
    }

    /**
     * Runs the given command and updates screen view if necesary.
     *
     * @param command command to execute.
     *
     * @return message to be printed.
     */
    public String runCommand(String command) throws Exception
    {
	return runCommand(command,true);
    }

    /**
     * Runs the given command and updates screen view depending on value of
     * update.
     *
     * @param command command to execute.
     * @param update  flag specifying whether screen must be updated.
     *
     * @return message to be printed.
     */
    public String runCommand(String command,boolean update) throws Exception
    {
	if (command == null) return "";

	StringWriter ret = new StringWriter();
	PrintWriter  wr  = new PrintWriter(ret);

	CommandParser commParser = new CommandParser(command.trim());
	int commandVal = commParser.parseCommand();

	// Finding command to execute.
	// If found the value of commandVal is set depending
	// on succes of executing the command.
	if (commandVal == CommandParser.EXIT_VAL) {
	    if (allowExit_) System.exit(0);
	} else if (commandVal == CommandParser.LOAD_VAL) {
	    MolPanel mp = molPanels_[molPanels_.length - 1];
	    int n_load = proceedToLoadCommand(mp,commParser);
	    if (n_load >= 0) {
		if (update) mp.updateMoleculeView();
		if (n_load == 1) wr.println(n_load + " molecule loaded.");
		else             wr.println(n_load + " molecules loaded.");
	    } else commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.PDBLOAD_VAL) {
	    MolPanel mp = molPanels_[molPanels_.length - 1];
	    int n_load = proceedToPDBLoadCommand(mp,commParser);
	    if (n_load >= 0) {
		if (update) mp.updateMoleculeView();
		if (n_load == 1) wr.println(n_load + " molecule loaded.");
		else             wr.println(n_load + " molecules loaded.");
	    } else commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.SAVE_VAL) {
	    int n_saved = proceedToSaveCommand(commParser);
	    if (n_saved >= 0)
		if (n_saved == 1) wr.println(n_saved + " atom saved.");
		else              wr.println(n_saved + " atoms saved.");
	    else commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.UNLOAD_VAL ||
		   commandVal == CommandParser.ZAP_VAL) {
	    int n_set = proceedToUnloadCommand(commParser);
	    if (n_set > 0) {
		if (n_set == 1) wr.println(n_set + " molecule unloaded.");
		else            wr.println(n_set + " molecules unloaded.");
		if (update) molPanels_[0].updateMoleculeView();
	    } else if (n_set < 0) {
		commandVal = CommandParser._ERROR_VAL;
	    }
	} else if (commandVal == CommandParser.CENTER_VAL) {
	    centerMolecules();
	    if (update) molPanels_[0].updateMoleculeView();
	} else if (commandVal == CommandParser.BACKBONE_VAL) {
	    int n_set = proceedToBackboneCommand(commParser);
	    if (n_set > 0) {
		if (update) molPanels_[0].updateMoleculeView();
	    } else if (n_set < 0) {
		commandVal = CommandParser._ERROR_VAL;
	    }
	} else if (commandVal == CommandParser.SPACEFILL_VAL) {
	    int n_set = proceedToSpacefillCommand(commParser);
	    if (n_set > 0) {
		if (update) molPanels_[0].updateMoleculeView();
	    } else if (n_set < 0) {
		commandVal = CommandParser._ERROR_VAL;
	    }
	} else if (commandVal == CommandParser.WIREFRAME_VAL) {
	    int n_set = proceedToWireframeCommand(commParser);
	    if (n_set > 0) {
		if (update) molPanels_[0].updateMoleculeView();
	    } else if (n_set < 0) {
		commandVal = CommandParser._ERROR_VAL;
	    }
	} else if (commandVal == CommandParser.CARTOONS_VAL) {
	    // Not emplemented
	    wr.println("The command is not implemented yet.");
	    commParser = new CommandParser("");
	} else if (commandVal == CommandParser.STRANDS_VAL) {
	    // Not emplemented
	    wr.println("The command is not implemented yet.");
	    commParser = new CommandParser("");
	} else if (commandVal == CommandParser.TRACE_VAL) {
	    // Not emplemented
	    wr.println("The command is not implemented yet.");
	    commParser = new CommandParser("");
	} else if (commandVal == CommandParser.RIBBONS_VAL) {
	    // Not emplemented
	    wr.println("The command is not implemented yet.");
	    commParser = new CommandParser("");
	} else if (commandVal == CommandParser.COLOR_VAL) {
	    int n_set = proceedToColorCommand(commParser);
	    if (n_set > 0) {
		if (update) molPanels_[0].updateMoleculeView();
	    } else if (n_set < 0) {
		commandVal = CommandParser._ERROR_VAL;
	    }
	} else if (commandVal == CommandParser.SET_VAL) {
	    if (!proceedToSetCommand(commParser))
		commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.SELECT_VAL) {
	    int n_sel = proceedToSelectCommand(commParser);
	    if (n_sel >= 0) {
		if (n_sel == 1) wr.println(n_sel + " atom selected.");
		else            wr.println(n_sel + " atoms selected.");
	    } else commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.ASSIGN_VAL) {
	    int n_assigned = proceedToAssignCommand(commParser);
	    if (n_assigned >= 0) {
	    } else commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.JAR_VAL) {
	    Object obj = proceedToJarCommand(commParser);
	    if (obj == null) commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.SPLIT_VAL) {
	    boolean res = proceedToSplitCommand(sPane,commParser);
	    if (!res) commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.DRAG_VAL) {
	    boolean res = proceedToDragCommand(commParser);
	    if (!res) commandVal = CommandParser._ERROR_VAL;
	} else if (commandVal == CommandParser.PRINT_VAL) {
	    String res = proceedToPrintCommand(commParser);
	    if (res == null) commandVal = CommandParser._ERROR_VAL;
	    else {
		String fileName = commParser.getOutputFileName();
		if (fileName != null && fileName.length() > 0)
		    printToFile(fileName,res,commParser.outputFileAppend());
		else wr.print(res);
	    }
	} else if (commandVal == CommandParser.ECHO_VAL) {
	    wr.println(commParser.getRemainingContent().trim());
	    // Parsing all command availabe in parser just to make it empty
	    while (commParser.hasMoreInput()) commParser.parseCommand();
	} else if (commandVal == CommandParser.ALIGN_SW_VAL ||
		   commandVal == CommandParser.ALIGN_NW_VAL) {
	    String res = proceedToAlignCommand(commandVal,commParser);
	    if (res == null) commandVal = CommandParser._ERROR_VAL;
	    else if (!res.equals("")) wr.print(res);
	} else if (commandVal == CommandParser.RIGIDS_VAL) {
	    RigidFinder rf = proceedToRigidsCommand(commParser);
	    if (rf == null) {
		commandVal = CommandParser._ERROR_VAL;
	    } else if (rf.getNRigids() >= 0) {
		wr.print("Found " + rf.getNRigids() + " rigid ");
		if (rf.getNRigids() == 1) wr.println("block");
		else                      wr.println("blocks");
	    }
	} else if (commandVal == CommandParser.FIT_VAL) {
	    Kabscher kb = proceedToFitCommand(commParser);
	    if (kb == null) {
		commandVal = CommandParser._ERROR_VAL;
	    } else {
		if (kb.getNFitted() > 0) {
		    double[] axis = kb.getAxis();
		    if (axis != null)
			wr.printf("Rotation vector: (%6.3f,%6.3f,%6.3f)\n",
				  axis[0],axis[1],axis[2]);
		    wr.printf("Rotation angle: %6.3f\n",kb.getAngle());
		    double[] trans = kb.getTranslation();
		    if (trans != null)
			wr.printf("Translation vector: (%6.3f,%6.3f,%6.3f)\n",
				  trans[0],trans[1],trans[2]);
		}
		wr.print("ne = " + kb.getNFitted());
		double rmsd = kb.getRMSD();
		if (rmsd < 10) wr.format(" rmsd = %4.2f\n",rmsd);
		else           wr.format(" rmsd = %5.2f\n",rmsd);
	    }
	} else if (commandVal == CommandParser.MOTION_VAL) {
	    Motioner mov = proceedToMotionCommand(commParser);
	    if (mov == null) {
		commandVal = CommandParser._ERROR_VAL;
// 	    } else if (rf.getNRigids() >= 0) {
// 		wr.print("Found " + rf.getNRigids() + " rigid ");
// 		if (rf.getNRigids() == 1) wr.println("block");
// 		else                      wr.println("blocks");
	    }
	} else if (commandVal == CommandParser.NMA_VAL) {
	    NormalModeCalculator nmc = proceedToNMACommand(commParser);
	    if (nmc == null) {
		commandVal = CommandParser._ERROR_VAL;
	    } else if (nmc.getNModes() > 0) {
		wr.println("Calculated " + nmc.getNModes() + " modes.");
	    }
	} else if (commandVal == CommandParser.COMPARE_VAL) {
	    String res = proceedToCompareCommand(commParser);
	    if (res == null) commandVal = CommandParser._ERROR_VAL;
	    else if (!res.equals("")) wr.print(res);
	} else if (commandVal == CommandParser._NONE_VAL) {
	    // Do nothing.
	} else { // Unknown command
	    commandVal = CommandParser._ERROR_VAL;
	}

	if (commandVal == CommandParser._ERROR_VAL) {
 	    String parsed = commParser.getParsedContent();
	    wr.println("Error after parsing:");
	    wr.println(parsed + " <<<");
	} else if (commParser.hasMoreInput()) {
 	    String left = commParser.getRemainingContent();
	    wr.println("Too long line skiped: \"" + left + "\".");
	}

	return ret.toString();
    }

    /**
     * Moves coordinated to the center of mass for all molecules.
     */
    private double[] center_ = new double[3];
    private void centerMolecules()
    {
	double x_center = 0, y_center = 0, z_center = 0;
	int n_atoms = 0;
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
	    for (Chain c = mol.chainList();c != null;c = c.next())
		for (Assembly s = c.assemblyList();s != null;s = s.next())
		    for (Atom a = s.atomList();a != null;a = a.next()) {
			x_center += a.getDerivedX();
			y_center += a.getDerivedY();
			z_center += a.getDerivedZ();
			n_atoms++;
		    }
	if (n_atoms == 0) return;
	double inv = 1./n_atoms;
	x_center *= inv;
	y_center *= inv;
	z_center *= inv;
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
	    for (Chain c = mol.chainList();c != null;c = c.next())
		for (Assembly s = c.assemblyList();s != null;s = s.next())
		    for (Atom a = s.atomList();a != null;a = a.next()) {
			a.setDerivedX(a.getDerivedX() - x_center);
			a.setDerivedY(a.getDerivedY() - y_center);
			a.setDerivedZ(a.getDerivedZ() - z_center);
		    }
	center_[0] += x_center;
	center_[1] += y_center;
	center_[2] += z_center;
    }

    /**
     * Returns number of atoms radius changed. Negative if error happens.
     *
     * @return number of atoms radius changed.
     */
    int proceedToSpacefillCommand(CommandParser commParser)
    {
	// Parsing radius
	int n_set = 0;
	int commandVal = commParser.parseCommand();
	double rad = 0;
	if (commandVal == CommandParser.OFF_VAL   ||
	    commandVal == CommandParser.FALSE_VAL ||
	    commandVal == CommandParser.NO_VAL) {
	    rad = -1;
	} else if (commandVal == CommandParser.ON_VAL   ||
		   commandVal == CommandParser.TRUE_VAL ||
		   commandVal == CommandParser.YES_VAL  ||
		   commandVal == CommandParser.VDW_VAL) {
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    Element elem = a.getElement();
			    if (elem == null) continue;
			    a.setRadius(elem.getVDWRadius());
			    n_set++;
			}
	    return n_set;
	} else if (commandVal == CommandParser._INTEGER_NUM_VAL) {
	    rad = commParser.getParsedInt()*RASMOL_INT_TO_ANGSTROM;
	    if (rad < 0) rad = 0;
	} else if (commandVal == CommandParser._DOUBLE_NUM_VAL) {
	    rad = commParser.getParsedDouble(); 
	    if (rad < 0) rad = 0;
	} else return -1;

	// Setting radius
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
	    for (Chain c = mol.chainList();c != null;c = c.next())
		for (Assembly s = c.assemblyList();s != null;s = s.next())
		    for (Atom a = s.atomList();a != null;a = a.next()) {
			if (!a.isSelected()) continue;
			a.setRadius(rad);
			n_set++;
		    }
	return n_set;
    }

    /**
     * Returns number of bonds radius changed. Negative if error happens.
     *
     * @return number of bonds radius changed.
     */
    int proceedToWireframeCommand(CommandParser commParser)
    {
	// Parsing radius
	int n_set = 0;
	int commandVal = commParser.parseCommand();
	double rad = 0;
	if (commandVal == CommandParser.OFF_VAL   ||
	    commandVal == CommandParser.FALSE_VAL ||
	    commandVal == CommandParser.NO_VAL) {
	    rad = -1;
	} else if (commandVal == CommandParser.ON_VAL   ||
		   commandVal == CommandParser.TRUE_VAL ||
		   commandVal == CommandParser.YES_VAL) {
	    rad = 0;
	} else if (commandVal == CommandParser._INTEGER_NUM_VAL) {
	    rad = commParser.getParsedInt()*RASMOL_INT_TO_ANGSTROM;
	    if (rad < 0) rad = 0;
	} else if (commandVal == CommandParser._DOUBLE_NUM_VAL) {
	    rad = commParser.getParsedDouble(); 
	    if (rad < 0) rad = 0;
	} else return -1;

	// Setting radius
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
	    for (Chain c = mol.chainList();c != null;c = c.next())
		for (Assembly s = c.assemblyList();s != null;s = s.next())
		    for (Atom a = s.atomList();a != null;a = a.next()) {
			Bond[] bonds = a.bondArray();
			if (bonds == null) continue;
			for (int b = 0;b < bonds.length;b++) {
			    Bond bond = bonds[b];
			    Atom a1 = bond.getFAtom();
			    if (a1 == null) continue;
			    Atom a2 = bond.getSAtom();
			    if (a2 == null) continue;
			    if (!a1.isSelected() || !a2.isSelected()) continue;
			    bond.setRadius(rad);
			    n_set++;
			}
		    }
	return n_set;
    }
    
    /**
     * Returns number of bonds radius changed. Negative if error happens.
     *
     * @return number of bonds radius changed.
     */
    int proceedToBackboneCommand(CommandParser commParser)
    {
	// Parsing radius
	int n_set = 0;
	int commandVal = commParser.parseCommand();
	double rad = 0;
	if (commandVal == CommandParser.OFF_VAL   ||
	    commandVal == CommandParser.FALSE_VAL ||
	    commandVal == CommandParser.NO_VAL) {
	    rad = -1;
	} else if (commandVal == CommandParser.ON_VAL   ||
		   commandVal == CommandParser.TRUE_VAL ||
		   commandVal == CommandParser.YES_VAL) {
	    rad = 0;
	} else if (commandVal == CommandParser._INTEGER_NUM_VAL) {
	    rad = commParser.getParsedInt()*RASMOL_INT_TO_ANGSTROM;
	    if (rad < 0) rad = 0;
	} else if (commandVal == CommandParser._DOUBLE_NUM_VAL) {
	    rad = commParser.getParsedDouble(); 
	    if (rad < 0) rad = 0;
	} else return -1;

	// Setting radius
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
	    for (Chain c = mol.chainList();c != null;c = c.next())
		for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		    Bond[] bonds = s.bondArray();
		    if (bonds == null) continue;
		    for (int b = 0;b < bonds.length;b++) {
			Bond bond = bonds[b];
			Atom a1 = bond.getFAtom();
			if (a1 == null) continue;
			Atom a2 = bond.getSAtom();
			if (a2 == null) continue;
			if (!a1.isSelected() || !a2.isSelected()) continue;
			bond.setRadius(rad);
			n_set++;
		    }
		}
	return n_set;
    }
    
    /**
     * Returns number of atoms color changed. Negative if error happend.
     */
    int proceedToColorCommand(CommandParser commParser)
    {
	int n_set = 0;
	int commandVal = commParser.parseCommand();
	if (commandVal == CommandParser.BACKGROUND_VAL) { // Background
	    commandVal = commParser.parseCommand();
	    Color col = null;
	    if (commandVal == CommandParser._USERCOLOR_VAL) {
		col =  commParser.getParsedColor();
	    } else {
		col = getColorByName(commandVal);
	    }
	    if (col == null) return -1;
	    for (int i = 0;i < molPanels_.length;i++) {
		molPanels_[i].setBackground(col);
	    }
	// Parsing color for atoms
	} else if (commandVal == CommandParser.CPK_VAL) {
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    Element elem = a.getElement();
			    if (elem == null) continue;
			    a.setColor(elem.getColor());
			    n_set++;
			}
	} else if (commandVal == CommandParser.SHAPELY_VAL) {
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;
			 s = s.next()) {
			Compound comp = s.getCompound();
			if (comp == null) continue;
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    a.setColor(comp.getColor());
			    n_set++;
			}
		    }
	} else if (commandVal == CommandParser.MOLECULE_VAL) {
	    int n_mol = countMolecules();
	    if (n_mol == 1) n_mol = 2;
	    int color_ind = 0;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next()) {
		int fr = (int)(1023*color_ind/(n_mol - 1));
		int r = 0, g = 0, b = 0;
		if (fr < 256)      { r = 0;        g = fr;  b = 255; }
		else if (fr < 512) { r = 0;        g = 255; b = 511 - fr; }
		else if (fr < 768) { r = fr - 512; g = 255; b = 0; }
		else               { r = 255;      g = 1023 - fr; b = 0; }
		Color colToSet = new Color(r,g,b);
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    Element elem = a.getElement();
			    if (elem == null) continue;
			    a.setColor(colToSet);
			    n_set++;
			}
		color_ind++;
	    }
	} else if (commandVal == CommandParser.CHAIN_VAL) {
	    int n_chains = 0;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		n_chains += mol.countChains();
	    if (n_chains == 1) n_chains = 2;
	    int color_ind = 0;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next()) {
		    int fr = (int)(1023*color_ind/(n_chains - 1));
		    int r = 0, g = 0, b = 0;
		    if (fr < 256)      { r = 0;        g = fr;  b = 255; }
		    else if (fr < 512) { r = 0;        g = 255; b = 511 - fr; }
		    else if (fr < 768) { r = fr - 512; g = 255; b = 0; }
		    else               { r = 255;      g = 1023 - fr; b = 0; }
		    Color colToSet = new Color(r,g,b);
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    Element elem = a.getElement();
			    if (elem == null) continue;
			    a.setColor(colToSet);
			    n_set++;
			}
		    color_ind++;
		}
	} else if (commandVal == CommandParser.MODEL_VAL) {
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next()) {
		int n_models = mol.countModels();
		if (n_models <= 0) continue;
		for (Chain c = mol.chainList();c != null;c = c.next()) {
		    int fr = (int)(1023*c.getModel()/n_models);
		    int r = 0, g = 0, b = 0;
		    if (fr < 256)      { r = 0;        g = fr;  b = 255; }
		    else if (fr < 512) { r = 0;        g = 255; b = 511 - fr; }
		    else if (fr < 768) { r = fr - 512; g = 255; b = 0; }
		    else               { r = 255;      g = 1023 - fr; b = 0; }
		    Color colToSet = new Color(r,g,b);
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    Element elem = a.getElement();
			    if (elem == null) continue;
			    a.setColor(colToSet);
			    n_set++;
			}
		}
	    }
	} else if (commandVal == CommandParser.GROUPMOL_VAL) {
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next()) {
		int n_ass = mol.countAssemblies();
		if (n_ass == 1) n_ass = 2;
		int color_ind = 0;
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;
			 s = s.next()) {
			int fr = (int)(1023*color_ind/(n_ass - 1));
			int r = 0, g = 0, b = 0;
			if (fr < 256)      { r = 0; g = fr;  b = 255; }
			else if (fr < 512) { r = 0; g = 255; b = 511 - fr; }
			else if (fr < 768) { r = fr - 512; g = 255; b = 0; }
			else               { r = 255; g = 1023 - fr; b = 0; }
			Color colToSet = new Color(r,g,b);
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    Element elem = a.getElement();
			    if (elem == null) continue;
			    a.setColor(colToSet);
			    n_set++;
			}
			color_ind++;
		    }
	    }
	} else if (commandVal == CommandParser.GROUP_VAL) {
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next()) {
		    int n_ass = c.countAssemblies();
		    if (n_ass == 1) n_ass = 2;
		    int color_ind = 0;
		    for (Assembly s = c.assemblyList();s != null;
			 s = s.next()) {
			int fr = (int)(1023*color_ind/(n_ass - 1));
			int r = 0, g = 0, b = 0;
			if (fr < 256)      { r = 0; g = fr;  b = 255; }
			else if (fr < 512) { r = 0; g = 255; b = 511 - fr; }
			else if (fr < 768) { r = fr - 512; g = 255; b = 0; }
			else               { r = 255; g = 1023 - fr; b = 0; }
			Color colToSet = new Color(r,g,b);
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    Element elem = a.getElement();
			    if (elem == null) continue;
			    a.setColor(colToSet);
			    n_set++;
			}
			color_ind++;
		    }
		}
	} else if (commandVal == CommandParser.TEMPERATURE_VAL) {
	    double minTemp = 1E+255,maxTemp = -1E+255;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			 for (Atom a = s.atomList();a != null;a = a.next()) {
			     double temp = a.getTemperature();
			     if (temp > maxTemp) maxTemp = temp;
			     if (temp < minTemp) minTemp = temp;
			 }
	    double scale = maxTemp - minTemp;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			 for (Atom a = s.atomList();a != null;a = a.next()) {
			     if (!a.isSelected()) continue;
			     double temp = a.getTemperature();
			     int fr = (int)(1023*(temp - minTemp)/scale);
			     int r = 0, g = 0, b = 0;
			     if (fr < 256)      { r = 0; g = fr;  b = 255; }
			     else if (fr < 512) {
				 r = 0; g = 255; b = 511 - fr;
			     } else if (fr < 768) {
				 r = fr - 512; g = 255; b = 0;
			     } else { r = 255; g = 1023 - fr; b = 0; }
			     Color colToSet = new Color(r,g,b);
			     a.setColor(colToSet);
			     n_set++;
			 }
	} else if (commandVal == CommandParser.RIGIDS_VAL) {
	    double maxGroup = -1;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			if (s.getGroupId() > maxGroup)
			    maxGroup = s.getGroupId();
	    
	    double scale = maxGroup - 1;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;
			 s = s.next()) {
			double gr = s.getGroupId();
			if (gr == 0) continue;
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    int fr = 0;
			    if ((gr%2 == 0 && maxGroup%2 == 0) ||
				(gr%2 == 1 && maxGroup%2 == 1))
				fr = (int)(1023*(gr - 1)/scale);
			    else fr = (int)(1023*(maxGroup - gr)/scale);
			    int r = 0, g = 0, b = 0;
			    if (fr < 256)      { r = fr; g = 255;  b = 0; }
			    else if (fr < 512) {
				r = 255; g = 511 - fr; b = 0;
			    } else if (fr < 768) {
				r = 255; g = 0; b = fr - 512;
			    } else { r = 1023 - fr; g = 0; b = 255; }
			    Color colToSet = new Color(r,g,b);
			    a.setColor(colToSet);
			    n_set++;
			}
		    }
	} else if (commandVal == CommandParser.ATOM_VAL) {
	    return proceedToColorCommand(commParser);
	} else {
	    Color col = null;
	    if (commandVal == CommandParser._USERCOLOR_VAL) {
		col =  commParser.getParsedColor();
	    } else {
		col = getColorByName(commandVal);
	    }
	    if (col == null) return -1;

	    // Setting color
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    a.setColor(col);
			    n_set++;
			}
		
	}
	return n_set;
    }

    /**
     * Returns number of atoms selected. Negative if error happend.
     */
    int proceedToSelectCommand(CommandParser commParser)
    {
	if (!commParser.hasMoreInput()) return -1;

	SelectExpression selExpr = parseSelection(commParser);
	if (selExpr == null) return 0;

	// Selecting atoms
	int n_sel = 0;
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next()) {
	    if (!selExpr.moleculeMaySatisfy(mol)) {
		mol.selectAllAtoms(false);
		continue;
	    }
	    for (Chain c = mol.chainList();c != null;c = c.next()) {
		if (!selExpr.chainMaySatisfy(c)) {
		    c.selectAllAtoms(false);
		    continue;
		}
		for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		    if (!selExpr.assemblyMaySatisfy(s)) {
			s.selectAllAtoms(false);
			continue;
		    }
		    for (Atom a = s.atomList();a != null;a = a.next()) {
			if (!selExpr.atomMaySatisfy(a) || 
			    !selExpr.satisfy(mol,c,s,a)) {
			    a.setSelected(false);
			    continue;
			}
			a.setSelected(true);
			n_sel++;
		    }
		}
	    }
	}
	return n_sel;
    }

    /**
     * Parse complex selection expressions. Returns null if error happends.
     *
     * @param commParser parser of command line.
      *
     * @return selection expression.
     */
    SelectExpression parseSelection(CommandParser commParser)
    {
	// Parsing expressions
	boolean isNot = false, andBool = false,orBool = false;
	SelectExpression selExpr = null,newExpr = null;
	int commandVal = commParser.parseCommand();
	for (;commandVal != CommandParser._NONE_VAL;
	     commandVal = commParser.parseCommand()) {

	    if (commandVal == CommandParser._ERROR_VAL) return null;
					     
	    if (commandVal == CommandParser.NOT_VAL) {
		if (isNot) return null;
		isNot = true;
	    } else if (commandVal == CommandParser.AND_VAL) {
		if (orBool || isNot) return null;
		andBool = true;
	    } else if (commandVal == CommandParser.OR_VAL) {
		if (andBool || isNot) return null;
		orBool = true;
	    } else if (commandVal == CommandParser.ALL_VAL) {
		newExpr =
		    new SelectExpression(null,null,null,null,null,isNot);
	    } else if (commandVal == CommandParser._EXPRESSION_VAL) {
		newExpr =
		    new SelectExpression(commParser.getParsedMolName(),
					 commParser.getParsedChainIds(),
					 commParser.getParsedAssemblyName(),
					 commParser.getParsedAssemblyNum(),
					 commParser.getParsedAtomName(),
					 isNot);
	    } else if (commandVal == CommandParser._INTEGER_NUM_VAL) {
		int assNum = commParser.getParsedInt();
		newExpr =
		    new SelectExpression(null,null,null,
					 Integer.toString(assNum),
					 null,isNot);
	    } else if (commandVal == CommandParser._INTEGER_RANGE_VAL) {
		int start = commParser.getParsedIntRangeStart();
		int end   = commParser.getParsedIntRangeEnd();
		if (end < start) { // Swap numbers if end < start
		    int tmp = end;
		    end = start;
		    start = tmp;
		}
		// Creating all as sub-expression
		SelectExpression subExpr = null;
		for (int i = start;i <= end;i++) {
		    newExpr = new SelectExpression(null,null,null,
						   Integer.toString(i),
						   null,false);
		    if (subExpr == null) subExpr = newExpr;
		    else {
			newExpr.setNextOr(subExpr);
			subExpr = newExpr;
		    }
		}

		// Creating new expression with sub-expression
		newExpr = new SelectExpression(subExpr,isNot);
	    } else if (commandVal == CommandParser.SELECTED_VAL) {
 		int att = Atom.getSelectedAttribute();
 		newExpr = new SelectExpression(isNot);
 		newExpr.setAtomAttribute(att);
	    } else if (commandVal == CommandParser.ALIGNED_VAL) {
		int att = Assembly.getAlignedAttribute();
		newExpr = new SelectExpression(isNot);
		newExpr.setAssemblyAttribute(att);
	    } else if (commandVal == CommandParser.GROUP_VAL) {
		commandVal = commParser.parseCommand();
		if (commandVal != CommandParser._INTEGER_NUM_VAL) return null;
		int att =
		    Assembly.getGroupAttribute(commParser.getParsedInt());
		newExpr = new SelectExpression(isNot);
		newExpr.setAssemblyAttribute(att);
	    } else return selExpr;

	    if (newExpr != null) {
		if (selExpr == null) selExpr = newExpr;
		else {
		    if (andBool)     newExpr.setNextAnd(selExpr);
		    else if (orBool) newExpr.setNextOr(selExpr);
		    else return null;
		    selExpr = newExpr;
		}
		newExpr = null;
		isNot   = false;
		andBool = false;
		orBool  = false;
	    }
	}

	return selExpr;
    }

    /**
     * The function sets default view of molecule
     */ 
    void setMoleculeDefaultView(Molecule mol)
    {
	if (mol == null) return;

	// Setting col
	for (Chain c = mol.chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    if (!a.isSelected()) continue;
		    Element elem = a.getElement();
		    if (elem == null) continue;
		    a.setColor(elem.getColor());
		}

	// Setting backbone 
	double rad = 0.1;
	for (Chain c = mol.chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		Bond[] bonds = s.bondArray();
		if (bonds == null) continue;
		for (int b = 0;b < bonds.length;b++) {
		    Bond bond = bonds[b];
		    Atom a1 = bond.getFAtom();
		    if (a1 == null) continue;
		    Atom a2 = bond.getSAtom();
		    if (a2 == null) continue;
		    if (!a1.isSelected() || !a2.isSelected()) continue;
		    bond.setRadius(rad);
		}
	    }
	
	if (moleculeList_ != null) {
	    Molecule fm = moleculeList_;  
	    if (fm != mol)
		mol.setTransformation(fm.getRotation(),fm.getTranslation());
	}
    }

    /**
     * The function reads provided pdb-codes and parses files from PDB
     * database.
     * Returns number of molecules loaded.
     */
    int proceedToPDBLoadCommand(MolPanel panel,CommandParser commParser)
    {
        ArrayList<String> codes = new ArrayList<String>(4);

	commParser.parseCommand();
	String word = commParser.getParsedWord();
	while (word.length() > 0) {
	    codes.add(word);
	    commParser.parseCommand();
	    word = commParser.getParsedWord();
	}
	
	// Parsing input files
	int n_loaded = 0;
	int n_codes = codes.size();
	for (int c = 0;c < n_codes;c++) {
	    String code = codes.get(c).toString();
	    if (code.length() != 4) {
		System.err.println(code + " is not 4 characters long. " +
				   "Skipping.");
		continue;
	    }
	    String name = PDB_URL + "/" + code.substring(1,3) + "/pdb" + code +
		".ent.gz";
	    Molecule mols[] = parseFile(name,new PDBParser());
	    if (mols == null) {
		System.err.println("Structure with code '" + code +
				   "' not loaded.");
		continue;
	    }
	    
	    for (int i = 0;i < mols.length;i++) {
		Molecule m = mols[i];
		if (connectFlag_) {
		    BelkaUtil.connectCA(m);
		    BelkaUtil.connect(m);
		}
		if (panel != null) panel.addMoleculeToDraw(m);
		addMolecule(m);
		setMoleculeDefaultView(m);
		n_loaded++;
	    }
	}

	// Parsing all command availabe in parser just to make it empty
	while (commParser.hasMoreInput()) commParser.parseCommand();

	return n_loaded;
    }

    /**
     * The function reads provided filenames and parses files.
     * Returns number of molecules loaded.
     */
    int proceedToLoadCommand(MolPanel panel, CommandParser commParser)
    {
        ArrayList<String> fileNames = new ArrayList<String>(4);

	int userFormat = FormatResolver.FORMAT_UNKNOWN;
	int commandVal = commParser.parseCommand();
	if (commandVal == CommandParser.PDB_VAL) {
	    userFormat = FormatResolver.FORMAT_PDB;
	} else if (commandVal == CommandParser.FASTA_VAL) {
	    userFormat = FormatResolver.FORMAT_FASTA;
	} else if (commandVal == CommandParser.PROJECT_VAL) {
	    userFormat = FormatResolver.FORMAT_PROJECT;
	} else if (commandVal == CommandParser.GROUP_VAL) {
	    userFormat = FormatResolver.FORMAT_GROUP;
	}

	// File format is recognized => next token is file name
	if (userFormat != FormatResolver.FORMAT_UNKNOWN)
	    commParser.parseCommand();
	String word = commParser.getParsedWord();
	while (word.length() > 0) {
	    fileNames.add(word);
	    commParser.parseCommand();
	    word = commParser.getParsedWord();
	}

	// Parsing input files
	int n_loaded = 0;
	int n_files = fileNames.size();
	for (int f = 0;f < n_files;f++) {
	    String fileName = fileNames.get(f).toString();
	    int format = userFormat;
	    if (format == FormatResolver.FORMAT_UNKNOWN)
		format = FormatResolver.resolveFileFormatByExtension(fileName);
	    if (format == FormatResolver.FORMAT_PDB ||
		format == FormatResolver.FORMAT_FASTA) {
		Molecule mols[] = null;
		if (format == FormatResolver.FORMAT_PDB)
		    mols = parseFile(fileName,new PDBParser());
		else if (format == FormatResolver.FORMAT_FASTA)
		    mols = parseFile(fileName,new FASTAParser());
		if (mols == null) continue;
		
		for (int i = 0;i < mols.length;i++) {
		    Molecule m = mols[i];
		    if (connectFlag_) {
			BelkaUtil.connectCA(m);
			BelkaUtil.connect(m);
		    }
		    if (panel != null) panel.addMoleculeToDraw(m);
		    addMolecule(m);
		    setMoleculeDefaultView(m);
		    n_loaded++;
		}
	    } else if (format == FormatResolver.FORMAT_PROJECT) {
		if (moleculeList_ != null)
		    System.err.println("Replacing existing molecules!!!");
		moleculeList_ = loadProject(fileName);
		for (int p = 0;p < molPanels_.length;p++) {
		    MolPanel pan = molPanels_[p];
		    if (pan == null) continue;
		    Molecule[] mols = pan.getMoleculesToDraw();
		    if (mols == null) continue;
		    for (int m = 0;m < mols.length;m++) 
			pan.removeMoleculeToDraw(mols[m]);
		}
		if (sPane != null) {
		    molPanels_ = new MolPanel[1];
		    molPanels_[0] = new MolPanel();
		    sPane.setLeftComponent(molPanels_[0]);
		}
		for (Molecule m = moleculeList_;m != null;m = m.next()) {
		    molPanels_[0].addMoleculeToDraw(m);
		    n_loaded++;
		}
	    } else if (format == FormatResolver.FORMAT_GROUP) {
		loadGroups(fileName);
	    } else if (format == FormatResolver.FORMAT_FASTA) {
		loadGroups(fileName);
	    } else {
		System.err.println("Cannot resolve format for file '" +
				   fileName + "'.");
	    }
	}
	return n_loaded;
    }

    /**
     * Returns number of atoms written.
     */
    int saveProjectFile(String fileName,Molecule mols)
    {
	ProjectSerializer projectSerializer = new ProjectSerializer();
	URL fileURL = tryWriteURL(fileName);
	if (fileURL != null) return projectSerializer.saveToFile(mols,fileURL);
	else {
	    File file = tryWriteFile(fileName);
	    if (file != null) return projectSerializer.saveToFile(mols,file);
	}
	return 0;
    }

    /**
     * Returns number of atoms written.
     */
    int saveFile(String fileName,Parser parser,SelectExpression expr)
    {
	URL fileURL = tryWriteURL(fileName);
	if (fileURL != null) return parser.saveToFile(moleculeList_,
						      fileURL,
						      expr);
	else {
	    File file = tryWriteFile(fileName);
	    if (file != null) return parser.saveToFile(moleculeList_,
						       file,
						       expr);
	}
	return 0;
    }

    /**
     * Returns URL if file looks like URL or in codebase (in case of applet)
     * and can be written.
     */
    URL tryWriteURL(String fileName)
    {
	try { // Try it as URL
	    URL fileURL = new URL(fileName);
	    try {
		URLConnection conn = fileURL.openConnection();
		conn.setDoOutput(true);
		OutputStream outStream = conn.getOutputStream();
		outStream.close();
	    } catch (Exception e) {
		System.err.println("URL '" + fileName + "' can't be created " +
				   " or written.");
		return null;
	    }
	    return fileURL;
	} catch (Exception e) {}

	if (codeBase_ != null) { // Try it on codebase
	    try {
		URL fileURL = new URL(codeBase_,fileName);
		try {
		    URLConnection conn = fileURL.openConnection();
		    conn.setDoOutput(true);
		    OutputStream outStream = conn.getOutputStream();
		    outStream.close();
		} catch (Exception e) {
		    System.err.println("URL '" + fileName + "' can't be " +
				       "created or written.");
		    return null;
		}
		return fileURL;
	    } catch (Exception e) {}
	}
	return null;
    }

    /**
     * Returns file handle if file can be created and can be written.
     */
    File tryWriteFile(String fileName)
    {
	File file = new File(fileName);
	try { file.createNewFile(); }
	catch (Exception ex) {
	    System.err.println("Can't write to file '" + fileName + "'.");
	    return null;
	}
	if (!file.canWrite()) {
	    System.err.println("Can't write to file '" + fileName + "'.");
	    return null;
	}
	return file;
    }

    /**
     * Returns true if loading annotations/groups was successful.
     */
    boolean loadGroups(String fileName)
    {
	GroupLoader groupLoader = new GroupLoader();
	URL fileURL = tryReadURL(fileName);
	if (fileURL != null)
	    return groupLoader.loadFile(fileURL,moleculeList_);
	else {
	    File file = tryReadFile(fileName);
	    if (file != null)
		return groupLoader.loadFile(file,moleculeList_);
	}
	return false;
    }

    /**
     * Returns list of loaded molecule
     */
    Molecule loadProject(String fileName)
    {
	ProjectSerializer projectSerializer = new ProjectSerializer();
	URL fileURL = tryReadURL(fileName);
	if (fileURL != null) return projectSerializer.loadFromFile(fileURL);
	else {
	    File file = tryReadFile(fileName);
	    if (file != null) return projectSerializer.loadFromFile(file);
	}
	return null;
    }

    /**
     * Returns number of molecules loaded.
     */
    Molecule[] parseFile(String fileName,Parser parser)
    {
	URL fileURL = tryReadURL(fileName);
	if (fileURL != null) return parser.parseFile(fileURL);
	else {
	    File file = tryReadFile(fileName);
	    if (file != null) return parser.parseFile(file);
	}
	return null;
    }
    
    /**
     * Returns URL if file looks like URL or is in codebase, in case of applet.
     */
    URL tryReadURL(String fileName)
    {
	try { // Try it as URL
	    URL fileURL = new URL(fileName);
	    try {
		URLConnection conn = fileURL.openConnection();
		conn.setDoInput(true);
		InputStream inStream = conn.getInputStream();
		inStream.close();
	    } catch (Exception e) {
		System.err.println("URL '" + fileName + "' does not exists" +
				   " or not readable.");
		return null;
	    }
	    return fileURL;
	} catch (Exception e) {}

	if (codeBase_ != null) { // Try it on codebase
	    try {
		URL fileURL = new URL(codeBase_,fileName);
		try {
		    URLConnection conn = fileURL.openConnection();
		    conn.setDoInput(true);
		    InputStream inStream = conn.getInputStream();
		    inStream.close();
		} catch (Exception e) {
		    System.err.println("URL '" + fileName +
				       "' does not exists or not readable.");
		    return null;
		}
		return fileURL;
	    } catch (Exception e) {}
	}
	return null;
    }

    /**
     * Returns file handle if file exists and can be read.
     */
    File tryReadFile(String fileName)
    {
	File file = new File(fileName);
	if (!file.exists()) {
	    System.err.println("File '" + fileName + "' does not exists.");
	    return null;
	}
	if (!file.canRead()) {
	    System.err.println("Can't read file '" + fileName + "'.");
	    return null;
	}
	return file;
    }

    /**
     * The function saves structure(s) into a file.
     * 
     * Returns number of atoms written to a file. Negative if error happend.
     */
    int proceedToSaveCommand(CommandParser commParser)
    {
	int userFormat = FormatResolver.FORMAT_UNKNOWN;
	int commandVal = commParser.parseCommand();
	if (commandVal == CommandParser.PDB_VAL) {
	    userFormat = FormatResolver.FORMAT_PDB;
	} else if (commandVal == CommandParser.FASTA_VAL) {
	    userFormat = FormatResolver.FORMAT_FASTA;
	} else if (commandVal == CommandParser.PROJECT_VAL) {
	    userFormat = FormatResolver.FORMAT_PROJECT;
	} else if (commandVal == CommandParser.GROUP_VAL) {
	    userFormat = FormatResolver.FORMAT_GROUP;
	}

	// File format is recognized => next token is file name
	if (userFormat != FormatResolver.FORMAT_UNKNOWN)
	    commParser.parseCommand();
	String fileName = commParser.getParsedWord();
	if (fileName.length() <= 0) {
	    System.err.println("No file name given!");
	    return -1;
	}

	if (userFormat == FormatResolver.FORMAT_UNKNOWN)
	    userFormat = FormatResolver.resolveFileFormatByExtension(fileName);
	if (userFormat < 0) { // Still don't know format
	    System.err.println("Cannot resolve format for file '" +
			       fileName + "'.");
	    return -1;
	}

	int n_saved = 0;
	if (userFormat == FormatResolver.FORMAT_PDB) {
	    // Preparing for saving
	    SelectExpression selExpr = parseSelection(commParser);
	    n_saved = saveFile(fileName,new PDBParser(),selExpr);
	} else if (userFormat == FormatResolver.FORMAT_FASTA) {
	    
	} else if (userFormat == FormatResolver.FORMAT_PROJECT) {
	    if (moleculeList_ != null)
                System.err.println("Replacing existing molecules!!!");
	    n_saved = saveProjectFile(fileName,moleculeList_);
	} else if (userFormat == FormatResolver.FORMAT_GROUP) {
	    GroupLoader groupLoader = new GroupLoader();
	    File file = tryWriteFile(fileName);
	    if (file != null) groupLoader.saveToFile(file,moleculeList_);
	}
	return n_saved;
    }

    /**
     * Unloads specified structures.
     *
     * @return number of unloaded structures. Negative is error happend.
     */
    int proceedToUnloadCommand(CommandParser commParser)
    {
	int commandVal = commParser.parseCommand();
	if (commandVal == CommandParser.ALL_VAL) {
	    for (Molecule m = moleculeList_;m != null;m = m.next())
		for (int i = 0;i < molPanels_.length;i++)
		    molPanels_[i].removeMoleculeToDraw(m);
	    int ret = countMolecules();
	    disassembleMolecules();
	    return ret;
	} else if (commandVal == CommandParser._EXPRESSION_VAL) {
	    SelectExpression expr =
		new SelectExpression(commParser.getParsedMolName(),
				     commParser.getParsedChainIds(),
				     commParser.getParsedAssemblyName(),
				     commParser.getParsedAssemblyNum(),
				     commParser.getParsedAtomName(),
				     false);
	    int ret = 0;
	    Molecule m = moleculeList_;
	    while (m != null)
		if (!expr.specifiesMolecule(m)) m = m.next();
		else {
		    ret++;
		    for (int i = 0;i < molPanels_.length;i++)
			molPanels_[i].removeMoleculeToDraw(m);
		    if (m.prev() == null && m.next() == null) { // Only one
			moleculeList_ = lastMolecule_ = null;
			m.disassemble();
			m = null;
		    } else if (m.prev() == null) { // The molecule is first
			moleculeList_ = m.next();
			moleculeList_.extractBefore();
			m.disassemble();
			m = moleculeList_;
		    } else if (m.next() == null) { // The molecule is last
			lastMolecule_ = m.prev();
			lastMolecule_.extractAfter();
			m.disassemble();
			m = null;
		    } else { // Molecule somewhere in the middle
			Molecule prev_mol = m.prev();
			prev_mol.extractAfter();
			m.disassemble();
			m = prev_mol.next();
		    }
		}
	    return ret;
	} else return -1;
    }

    /**
     * Returns true number of atoms with assignment.
     */
    int proceedToAssignCommand(CommandParser commParser)
    {
	int ret = 0;

	int commVal1 = commParser.parseCommand();
	if (commVal1 == CommandParser.TEMPERATURE_VAL ||
	    commVal1 == CommandParser.OCCUPANCY_VAL) {
	    if (commParser.parseCommand() != CommandParser.FROM_VAL)
		return -1;
	    int commVal2 = commParser.parseCommand();
	    if (commVal2 != CommandParser.TEMPERATURE_VAL &&
		commVal2 != CommandParser.OCCUPANCY_VAL   &&
		commVal2 != CommandParser.GROUP_VAL) return -1;
	    for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
		for (Chain c = mol.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			for (Atom a = s.atomList();a != null;a = a.next()) {
			    if (!a.isSelected()) continue;
			    ret++;

			    // Getting values
			    double val = 0;
			    if (commVal2 == CommandParser.TEMPERATURE_VAL)
				val = a.getTemperature();
			    else if (commVal2 == CommandParser.OCCUPANCY_VAL)
				val = a.getOccupancy();
			    else if (commVal2 == CommandParser.GROUP_VAL)
				val = s.getGroupId();

			    // Setting value
			    if (commVal1 == CommandParser.TEMPERATURE_VAL)
				a.setTemperature(val);
			    else if (commVal1 == CommandParser.OCCUPANCY_VAL)
				a.setOccupancy(val);
			}
	} else {
	    return -1;
	}
	return ret;
    }

    /**
     * Returns true if execution of command was succesful false if error
     * happend.
     */
    boolean proceedToSetCommand(CommandParser commParser)
    {
	// Handling background command
	String content = commParser.getRemainingContent();
	CommandParser tmpParser = new CommandParser(content);
	int commandVal = tmpParser.parseCommand();
	if (commandVal == CommandParser.BACKGROUND_VAL) {
	    int n_set = proceedToColorCommand(commParser);
	    if (n_set < 0) return false;
	    return true;
	}

	commandVal = commParser.parseCommand();
	if (commandVal == CommandParser.CONNECT_VAL) {
	    commandVal = commParser.parseCommand();
	    if (commandVal == CommandParser.ON_VAL   ||
		commandVal == CommandParser.TRUE_VAL ||
		commandVal == CommandParser.YES_VAL) {
		connectFlag_ = true;
	    } else if (commandVal == CommandParser.OFF_VAL   ||
		       commandVal == CommandParser.FALSE_VAL ||
		       commandVal == CommandParser.NO_VAL) {
		connectFlag_ = false;
	    } else return false;
	} else return false;

	return true;
    }


    /**
     * Loads classes from jar file and executes specifyed function in specifyed
     * class. The syntaxis of the command is:
     * <code> jar class_name function_name </code>
     * The name of jar file must be <code> class_name.jar </code>
     *
     * Returns true if jar is found, class resolved and function run. Returns
     * false otherwise
     *
     * @return true if jar is found, class resolved and function run. Returns
     * false otherwise
     */
    Object proceedToJarCommand(CommandParser commParser)
    {
	commParser.parseCommand();
	String class_name = commParser.getParsedWord();
	if (class_name == "") {
	    System.err.println("No class name found.");
	    return false;
	}

	commParser.parseCommand();
	String func_name  = commParser.getParsedWord();
	if (func_name == "") {
	    System.err.println("No function name found.");
	    return false;
	}

	String jar_file = class_name + ".jar";

	try {
	    URL[] urls = {new URL("jar","","file:./" + jar_file + "!/")};
	    ClassLoader ucl = new URLClassLoader(urls);
	    Class c = ucl.loadClass(class_name + "." + class_name);
	    Object obj = c.newInstance();
	    Class[] param = {
		moleculeList_.getClass().forName("belka.mol.Molecule"),
		commParser.getClass()};
	    Method meth = c.getDeclaredMethod(func_name,param);
	    Object[] args = { moleculeList_, commParser };
	    return meth.invoke(obj,args);
	} catch (Exception e) {
	    System.err.println(e.toString());
	    return false;
	}
    }

    /**
     * Splits window into grid views. Number of view is defined by two
     * parameters.
     *
     * @return false if parameters are not accepted. Returns true otherwise.
     */
    boolean proceedToSplitCommand(JSplitPane sPane,CommandParser commParser)
    {
	int commandVal = CommandParser._NONE_VAL;
	if (commParser != null)
	    commandVal = commParser.parseCommand();

	int n_hor = 1, n_ver = 1;
	if (commandVal == CommandParser._INTEGER_NUM_VAL) {
	    n_hor = commParser.getParsedInt();
	    commandVal = commParser.parseCommand();
	    if (commandVal == CommandParser._INTEGER_NUM_VAL)
		n_ver = commParser.getParsedInt();
	    else return false;
	}

	// Check that numbers are positive
	if (n_hor <= 0 || n_ver <= 0)     return false;

	// Check that 
	MolPanel[] oldPanels = molPanels_;
	int n_new = n_hor*n_ver, n_old = oldPanels.length;
	molPanels_ = new MolPanel[n_new];
	
	if (n_hor == 1 && n_ver == 1) { // Make one panel
	    molPanels_[0] = oldPanels[0];
	    sPane.setLeftComponent(molPanels_[0]);
	} else if ((n_hor == 1 && n_ver == 2) ||
		   (n_hor == 2 && n_ver == 1)) { // Two panels
	    for (int i = 0;i < n_old && i < n_new;i++)
		molPanels_[i] = oldPanels[i];
	    for (int i = n_old;i < n_new;i++)
		molPanels_[i] = new MolPanel();
	    int option = JSplitPane.HORIZONTAL_SPLIT;
	    if (n_hor == 2) option = JSplitPane.VERTICAL_SPLIT;
 	    JSplitPane sp = new JSplitPane(option,molPanels_[0],molPanels_[1]);
	    sp.setResizeWeight(0.5);
	    sp.setOneTouchExpandable(false);
	    sPane.setLeftComponent(sp);
	} else { // More panels
	    JPanel pan = new JPanel(new GridLayout(n_hor,n_ver,3,3));
	    for (int i = 0;i < n_old && i < n_new;i++) {
		molPanels_[i] = oldPanels[i];
		pan.add(molPanels_[i]);
	    }
	    for (int i = n_old;i < n_new;i++) {
		molPanels_[i] = new MolPanel();
		pan.add(molPanels_[i]);
	    }
	    sPane.setLeftComponent(pan);
	}

	for (int i = n_new;i < n_old;i++) {
	    MolPanel mp     = oldPanels[i];
	    Molecule[] mols = mp.getMoleculesToDraw();
	    if (mols == null) continue;
	    for (int j = 0;j < mols.length;j++)
		molPanels_[n_new - 1].addMoleculeToDraw(mols[j]);
	}

	return true;
    }

    /**
     * The function relocates molecules between different view panels.
     *
     * @return false if parameters are not accepted, true otherwise
     */
    boolean proceedToDragCommand(CommandParser commParser)
    {
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return false;
	SelectExpression expr =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	if (commParser.parseCommand() != CommandParser._INTEGER_NUM_VAL)
	    return false;
	int panelIndex = commParser.getParsedInt();
	if (panelIndex <= 0 || panelIndex > molPanels_.length) return false;

	for (Molecule m = moleculeList_;m != null;m = m.next()) {
	    if (!expr.specifiesMolecule(m)) continue;
	    for (int i = 0;i < molPanels_.length;i++)
		molPanels_[i].removeMoleculeToDraw(m);
	    molPanels_[panelIndex - 1].addMoleculeToDraw(m);
	}

	return true;
    }

    /**
     * Prints according to user specification. 
     *
     * @return string to print, null if error happen.
     */
    String proceedToPrintCommand(CommandParser commParser)
    {
	StringBuffer ret = new StringBuffer();

	int commVal = commParser.parseCommand();
	if (commVal == CommandParser.SEQUENCE_VAL) { // Sequence
	    if (!commParser.hasMoreInput()) return null;
	    SelectExpression selExpr = parseSelection(commParser);
	    if (selExpr == null) return null;
	    boolean found = false;
	    for (Molecule m = moleculeList_;m != null;m = m.next())
		for (Chain c = m.chainList();c != null;c = c.next())
		    if (selExpr.satisfy(m,c,null,null)) {
			ret.append(">");
			if (m.getPDBCode().trim().length() > 0)
			    ret.append(m.getPDBCode());
			else ret.append(m.getName());
			ret.append(" chain " + c.getId() + "\n");
			ret.append(c.getSequence() + "\n");
			found = true;
		    }
	    if (!found) System.err.println("Chain(s) is/are not found.");
	} else if (commVal == CommandParser.PDB_VAL) { // PDB file
	    SelectExpression selExpr = parseSelection(commParser);
	    PDBParser parser = new PDBParser();
	    ret = parser.print(moleculeList_,selExpr);
	} else if (commVal == CommandParser.RIGIDS_VAL) { // PDB file
	    ret = printRigids(commParser);
	} else if (commVal == CommandParser.DISPLACEMENT_VAL) { // 
	    ret = printDisplacement(commParser);
	} else {
	    return null;
	}

	if (ret == null) return null;

	return ret.toString();
    }

    private void printToFile(String fileName,String str,boolean append)
    {
	// Printing to a file
	File file = new File(fileName);
	try { file.createNewFile(); }
	catch (Exception ex) {
	    System.err.println("Can't write to file '" + fileName + "'.");
	    return;
	}
	try {
	    FileWriter fw = new FileWriter(file,append);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(str);
	    bw.close();
	    fw.close();
	} catch (Exception ex) { 
	    System.err.println("Can't write to file '" + fileName + "'.");
	    return;
	}
    }

    /**
     * Alignes residue/nucleotide sequences by Needleman-Wunsch or
     * Smith-Waterman algorithms.
     *
     * @return false if alignment can't be performed. Returns true otherwise.
     */
    String proceedToAlignCommand(int commandVal,CommandParser commParser)
    {
	// Parsing descrition of first chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr1 =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	// Parsing descrition of second chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr2 = 
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	Chain[] chains1 = findSpecifiedChains(expr1);
	Chain[] chains2 = findSpecifiedChains(expr2);
	if (chains1 == null)
	    System.err.println("No spcified chains found in first molecule.");
	if (chains2 == null)
	    System.err.println("No spcified chains found in second molecule.");

	if (chains1 == null || chains2 == null) return "";
	if (chains1.length != chains2.length) {
	    System.err.println("Found different number of chains for " +
			       "two complexes.");
	    return null;
	}

	// Making alignment
	SeqAligner master = new SeqAligner();
	StringWriter ret = new StringWriter();
	PrintWriter  wr  = new PrintWriter(ret);

	for (int i = 0;i < chains1.length;i++) {
	    if (commandVal == CommandParser.ALIGN_NW_VAL)
		master.align_nw(chains1[i],chains2[i]);
	    else if (commandVal == CommandParser.ALIGN_SW_VAL)
		master.align_sw(chains1[i],chains2[i]);

	    wr.println("len = " +    master.getAliLength()    + ", " +
		       "#ident = " + master.getNumIdentical() + ", " +
		       "#pos = " +   master.getNumPositive()  + ", " +
		       "#gaps = " +  master.getNumGaps());
	    master.applyToChains();
// 		Molecule mol1 = moleculeList_;
// 		Molecule mol2 = mol1.next();
// 		wr.println(mol1.chainList().getSequence());
// 		wr.println(mol2.chainList().getSequence());
	}

	return ret.toString();
    }

    /**
     * Identifies rigid bodies from two structures with know reisue
     * correspondence that is derived from current alignment.
     *
     * @return if succesfull -- the object that performed an alignment,
     * null -- otherwise.
     */
    RigidFinder proceedToRigidsCommand(CommandParser commParser)
    {
	// Parsing descrition of first chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr1 =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	// Parsing descrition of second chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr2 = 
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	Chain[] chains1 = findSpecifiedChains(expr1);
	Chain[] chains2 = findSpecifiedChains(expr2);

	RigidFinder rf = new RigidFinder(chains1,chains2);
	if (chains1 == null || chains2 == null ||
	    chains1.length != chains2.length) {
	    if (chains1 == null)
		System.err.println("No spcified chains found in first " +
				   "molecule.");
	    if (chains2 == null)
		System.err.println("No spcified chains found in second " +
				   "molecule.");
	    if (chains1 != null && chains2 != null &&
		chains1.length != chains2.length) 
		System.err.println("Found different number of chains for " +
				   "the two complexes.");
	    while (commParser.hasMoreInput()) commParser.parseCommand();
	    return rf;
	}

	// Parsing maximal distance tollerance
	double max_d = -1;
	int val = commParser.parseCommand();
	if (val == CommandParser._NONE_VAL)
	    rf.findRigids();
	else if (val == CommandParser._DOUBLE_NUM_VAL ||
		 val == CommandParser._INTEGER_NUM_VAL) {
	    if (val == CommandParser._DOUBLE_NUM_VAL)
		max_d = commParser.getParsedDouble();
	    else if (val == CommandParser._INTEGER_NUM_VAL)
		max_d = commParser.getParsedInt();
	    if (max_d < 0) return null;

	    // Parsing options
	    boolean refine = true, cluster = true;
	    String fileName = null;
	    val = commParser.parseCommand();
	    if (val == CommandParser._NONE_VAL) ;
	    else if (val == CommandParser.REFINE_VAL ||
		     val == CommandParser.NOREFINE_VAL) {
		if (val == CommandParser.REFINE_VAL)        refine = true;
		else if (val == CommandParser.NOREFINE_VAL) refine = false;
		val = commParser.parseCommand();
		if (val == CommandParser._NONE_VAL) ;
		else if (val == CommandParser.CLUSTER_VAL ||
			 val == CommandParser.NOCLUSTER_VAL) {
		    if (val == CommandParser.CLUSTER_VAL)   cluster = true;
		    if (val == CommandParser.NOCLUSTER_VAL) cluster = false;
		    commParser.parseCommand();
		    fileName = commParser.getParsedWord();
		} else return null;
	    } else return null;
	    File file = null;
	    if (fileName != null && fileName.length() > 0) {
		file = new File(fileName);
		try { file.createNewFile(); }
		catch (Exception ex) {
		    System.err.println("Can't write to file '" + fileName
				       + "'.");
		    file = null;
		} 
	    }
	    rf.findRigids(max_d,refine,cluster,file);
	}

	return rf;
    }

    /**
     * Motion analyis
     *
     * @return if succesfull -- the object that analyses motions,
     * null -- otherwise.
     */
    Motioner proceedToMotionCommand(CommandParser commParser)
    {
	// Parsing descrition of first chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr1 =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	// Parsing descrition of second chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr2 = 
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	Chain[] chains1 = findSpecifiedChains(expr1);
	Chain[] chains2 = findSpecifiedChains(expr2);

	Motioner mov = new Motioner(chains1,chains2);
	mov.splitMotions();
	return mov;
    }

    /**
     * Fits aligned atoms by least square fit using Kabsch algorithm.
     *
     * @return if succesfull -- object that performed fitting
     * null otherwise.
     */
    Kabscher proceedToFitCommand(CommandParser commParser)
    {
	// Parsing descrition of first chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr1 =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	// Parsing descrition of second chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr2 = 
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	boolean selected = false;
	if (commParser.hasMoreInput())
	    if (commParser.parseCommand() == CommandParser.SELECTED_VAL) {
		selected = true;
	    } else {
		return null;
	    }

	Chain[] chains1 = findSpecifiedChains(expr1);
	Chain[] chains2 = findSpecifiedChains(expr2);
	if (chains1 == null)
	    System.err.println("No spcified chains found in first molecule.");
	if (chains2 == null)
	    System.err.println("No spcified chains found in second molecule.");

	Kabscher kb = new Kabscher();
	if (chains1 == null || chains2 == null) return kb;

	if (chains1.length != chains2.length) {
	    System.err.println("Found different number of chains for the " +
			       "two complexes.");
	    return kb;
	}

	if (kb.fitCA(chains1,chains2,selected) >= 0 &&
	    chains2[0].molecule() != null)
	    chains2[0].molecule().rotate(kb.getRotation(),
					 kb.getTranslation(),
					 center_);

	return kb;
    }

    /**
     * The function calculates normal modes for a protein structure
     *
     * @return if succesfull -- object that performed fitting,
     * null otherwise.
     */
    NormalModeCalculator proceedToNMACommand(CommandParser commParser)
    {
	// Parsing chain descrition
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	// Parsing value of gamma
	double gamma = 1.0;
	int commVal = commParser.parseCommand();
	if (commVal == CommandParser._NONE_VAL) ;
	else if (commVal == CommandParser._INTEGER_NUM_VAL)
	    gamma = commParser.getParsedInt();
	else if (commVal == CommandParser._DOUBLE_NUM_VAL) 
	    gamma = commParser.getParsedDouble();

	// Parsing file name to save output
	commParser.parseCommand();
	String fileName = commParser.getParsedWord();

	// Creating nma calculator
	NormalModeCalculator nmc = new NormalModeCalculator();

	// Getting first chain
	Chain chain = findSpecifiedChain(expr);
	if (chain == null) {
	    System.err.println("Chain is not found.");
	    return nmc;
	}

	// Calculating modes
	nmc.calculateModes(chain.molecule(),gamma,true);

	// Printing modes
	File file = null;
	if (fileName != null && fileName.length() > 0) {
	    file = new File(fileName);
	    try { file.createNewFile(); }
	    catch (Exception ex) {
		System.err.println("Can't write to file '" + fileName + "'.");
		file = null;
	    }
	    if (file != null) nmc.printModes(file);
	}

	return nmc;
    }

    /**
     * The function does various comparisons
     *
     * @return if succesfull -- string describing result of comparison.
     */
    String proceedToCompareCommand(CommandParser commParser)
    {
	StringWriter ret = new StringWriter();
	PrintWriter  wr  = new PrintWriter(ret);
	if (commParser.parseCommand() == CommandParser.GROUP_VAL) {
	    // Parsing descrition of chains
	    if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
		return null;
	    SelectExpression expr =
		new SelectExpression(commParser.getParsedMolName(),
				     commParser.getParsedChainIds(),
				     commParser.getParsedAssemblyName(),
				     commParser.getParsedAssemblyNum(),
				     commParser.getParsedAtomName(),
				     false);
	    
	    Chain[] chains = findSpecifiedChains(expr);
	    if (chains == null) {
		System.err.println("No spcified chains found.");
		return null;
	    }

	    // Getting file name
	    commParser.parseCommand();
	    String fileName = commParser.getParsedWord();
	    if (fileName == null || fileName.length() == 0) return null;

	    // Saving # of current blocks
	    int nr_cur = getLargestGroup();

	    // Calculating number of assemblies
	    int n_ass = 0;
	    for (Molecule m = moleculeList_;m != null;m = m.next())
		n_ass += m.countAssemblies();
	    // Storing current group assignments
	    int[] save_group = new int[n_ass];
	    int index = 0;
	    for (Molecule m = moleculeList_;m != null;m = m.next())
		for (Chain c = m.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			if (!s.isGap()) save_group[index++] = s.getGroupId();

	    if (loadGroups(fileName)) { // Doing comparison
		int nr_new = getLargestGroup();
		int[][] overlap = new int[nr_cur][nr_new];
		index = 0;
		for (Molecule m = moleculeList_;m != null;m = m.next())
		    for (Chain c = m.chainList();c != null;c = c.next()) {
			boolean selected = false;
			for (int i = 0;i < chains.length;i++)
			    if (chains[i] == c) {
				selected = true;
				break;
			    }
			for (Assembly s = c.assemblyList();s != null;
			     s = s.next()) {
			    if (s.isGap()) continue;
			    if (!selected) {
				index++;
				continue;
			    }
			    int ind_cur = save_group[index++];
			    int ind_new = s.getGroupId();
			    if (ind_cur == 0 || ind_new == 0) continue;
			    overlap[ind_cur - 1][ind_new - 1]++;
			}
		    }
		int[] best_for_cur = new int[nr_cur];
		for (int i_cur = 0;i_cur < nr_cur;i_cur++) {
		    int max = 0;
		    for (int i_new = 0;i_new < nr_new;i_new++)
			if (overlap[i_cur][i_new] > max) {
			    max = overlap[i_cur][i_new];
			    best_for_cur[i_cur] = i_new + 1;
			}
		}
		int[] best_for_new = new int[nr_new];
		for (int i_new = 0;i_new < nr_new;i_new++) {
		    int max = 0;
		    for (int i_cur = 0;i_cur < nr_cur;i_cur++)
			if (overlap[i_cur][i_new] > max) {
			    max = overlap[i_cur][i_new];
			    best_for_new[i_new] = i_cur + 1;
			}
		}

		int same = 0,diff = 0;
		int new_new = 0,new_cur = 0,split_new = 0,split_cur = 0;
		index = 0;
		for (Molecule m = moleculeList_;m != null;m = m.next())
		    for (Chain c = m.chainList();c != null;c = c.next()) {
			boolean selected = false;
			for (int i = 0;i < chains.length;i++)
			    if (chains[i] == c) {
				selected = true;
				break;
			    }
			for (Assembly s = c.assemblyList();s != null;
			     s = s.next()) {
			    if (s.isGap()) continue;
			    if (!selected) {
				index++;
				continue;
			    }
			    int ind_cur = save_group[index++];
			    int ind_new = s.getGroupId();
			    if (ind_cur == 0 && ind_new == 0) continue;
			    if      (ind_cur == 0 && ind_new != 0)
				new_new++;
			    else if (ind_cur != 0 && ind_new == 0)
				new_cur++;
			    else {
				int best_cur = best_for_cur[ind_cur - 1];
				int best_new = best_for_new[ind_new - 1];
				if      (best_cur == ind_new &&
					 best_new == ind_cur) same++;
				else if (best_cur == ind_new &&
					 best_new != ind_cur) split_cur++;
				else if (best_cur != ind_new &&
					 best_new == ind_cur) split_new++;
				else diff++;
			    }
			}
		    }
		int total_cur = same + split_cur + split_new + diff + new_cur;
		int total_new = same + split_cur + split_new + diff + new_new;
		wr.println("Annotation\tsame\tsplit\tdiff.\tnew\ttotal");
		wr.println("Current\t\t" + same + "\t" + split_cur + "\t" +
			   diff + "\t" + new_cur + "\t" + total_cur);
		wr.println("From file\t" + same + "\t" + split_new + "\t" +
			   diff + "\t" + new_new + "\t" + total_new);
	    }

	    // Restoring current group assignments
	    index = 0;
	    for (Molecule m = moleculeList_;m != null;m = m.next())
		for (Chain c = m.chainList();c != null;c = c.next())
		    for (Assembly s = c.assemblyList();s != null;s = s.next())
			if (!s.isGap()) s.setGroupId(save_group[index++]);

	}


	return ret.toString();
    }

    /**
     * Return string representation of rigid blocks for specified chains.
     */
    StringBuffer printRigids(CommandParser commParser)
    {
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr1 =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);
	
	// Parsing descrition of second chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr2 = 
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	Chain[] chains1 = findSpecifiedChains(expr1);
	Chain[] chains2 = findSpecifiedChains(expr2);
	if (chains1 == null || chains2 == null) {
	    if (chains1 == null)
		System.err.println("No spcified chains found in first " +
				   "molecule.");
	    if (chains2 == null)
		System.err.println("No spcified chains found in second " +
				   "molecule.");
	    return new StringBuffer("");
	}

	if (chains1.length != chains2.length) {
	    System.err.println("Found different number of chains for the " +
			       "two complexes.");
	    return new StringBuffer("");
	}

	RigidFinder rf = new RigidFinder();
	return rf.print(chains1,chains2);
    }

    /**
     * Returns string representation of displacement calculated from 
     * two conformations of s structure.
     */
    StringBuffer printDisplacement(CommandParser commParser)
    {
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr1 =
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);
	
	// Parsing descrition of second chain
	if (commParser.parseCommand() != CommandParser._EXPRESSION_VAL)
	    return null;
	SelectExpression expr2 = 
	    new SelectExpression(commParser.getParsedMolName(),
				 commParser.getParsedChainIds(),
				 commParser.getParsedAssemblyName(),
				 commParser.getParsedAssemblyNum(),
				 commParser.getParsedAtomName(),
				 false);

	// Finding chains
	Chain chain1 = findSpecifiedChain(expr1);
	Chain chain2 = findSpecifiedChain(expr2);
	if (chain1 == null || chain2 == null) {
	    if (chain1 == null)
		System.err.println("First chain is not found.");
	    if (chain2 == null)
		System.err.println("Second chain is not found.");
	    return new StringBuffer("");
	}

	Displacer displ = new Displacer(chain1,chain2);
	StringBuffer ret = displ.print();
	ret.append(displ.printInfinitesimal(0,0.01,false));
	ret.append(displ.printInfinitesimal(0,0.01,true));

	return ret;
    }

    /**
     * Returns the chain wich matches the given selection expression.
     * Both molecule and chain names must be specified explicitly in
     * selection expression. Molecule name must also be unique.
     * If several chains will match selection, only the first one in the
     * selected molecule will be return.
     *
     * @return the chain matching the given selection expression.
     */
    Chain findSpecifiedChain(SelectExpression expr)
    {
	// Finding molecule
	Molecule mol = null;
	for (Molecule m = moleculeList_;m != null;m = m.next())
	    if (expr.specifiesMolecule(m))
		if (mol == null) mol = m;
		else {
		    System.err.println("Molecule is ambiguous.");
		    return null;
		}
	if (mol == null) {
	    System.err.println("Molecule is not found.");
	    return null;
	}

	// Finding chain
	for (Chain c = mol.chainList();c != null;c = c.next())
	    if (expr.specifiesChain(c)) return c;

	return null;
    }

    /**
     * Returns chains wich match the given selection expression.
     * Both molecule and chain names must be specified explicitly in
     * selection expression. Molecule name must also be unique.
     * If several chains will match selection, only the first one in the
     * selected molecule will be return.
     *
     * @return chains matching the given selection expression.
     */
    Chain[] findSpecifiedChains(SelectExpression expr)
    {
	// Finding molecule
	Molecule mol = null;
	for (Molecule m = moleculeList_;m != null;m = m.next())
	    if (expr.specifiesMolecule(m))
		if (mol == null) mol = m;
		else {
		    System.err.println("Molecule is ambiguous.");
		    return null;
		}
	if (mol == null) {
	    System.err.println("Molecule is not found.");
	    return null;
	}

	// Counting number of chains to return
	Chain[] tmp = new Chain[expr.numChainsSpecified()];
	
	int index = 0;
	for (int i = 0;i < tmp.length;i++)
	    for (Chain c = mol.chainList();c != null;c = c.next())
		if (expr.specifiesChainAs(c,i)) {
		    tmp[index++] = c;
		    break;
		}
	if (index == 0) return null;

	Chain[] ret = new Chain[index];
	for (int i = 0;i < index;i++)
	    ret[i] = tmp[i];
	
	return ret;
    }

    /**
     * The function returns the largest group id assigned to any assembly.
     *
     * @return the largest group id assigned to any assembly.
     */
    public int getLargestGroup()
    {
	int ret = 0;
	for (Molecule mol = moleculeList_;mol != null;mol = mol.next())
	    for (Chain c = mol.chainList();c != null;c = c.next())
		for (Assembly a = c.assemblyList();a != null;a = a.next())
		    if (a.getGroupId() > ret)
			ret = a.getGroupId();
	return ret;
    }

    Color getColorByName(int commandVal)
    {
	if (commandVal == CommandParser.ALICEBLUE_VAL)
	    return new Color(0xF0,0xF8,0xFF);
	if (commandVal == CommandParser.ANTIQUEWHITE_VAL)
	    return new Color(0xFA,0xEB,0xD7);
	if (commandVal == CommandParser.AQUA_VAL)
	    return new Color(0x00,0xFF,0xFF);
	if (commandVal == CommandParser.AQUAMARINE_VAL)
	    return new Color(0x7F,0xFF,0xD4);
	if (commandVal == CommandParser.AZURE_VAL)
	    return new Color(0xF0,0xFF,0xFF);
	if (commandVal == CommandParser.BEIGE_VAL)
	    return new Color(0xF5,0xF5,0xDC);
	if (commandVal == CommandParser.BISQUE_VAL)
	    return new Color(0xFF,0xE4,0xC4);
	if (commandVal == CommandParser.BLACK_VAL)
	    return new Color(0x00,0x00,0x00);
	if (commandVal == CommandParser.BLANCHEDALMOND_VAL)
	    return new Color(0xFF,0xEB,0xCD);
	if (commandVal == CommandParser.BLUE_VAL)
	    return new Color(0x00,0x00,0xFF);
	if (commandVal == CommandParser.BLUEVIOLET_VAL)
	    return new Color(0x8A,0x2B,0xE2);
	if (commandVal == CommandParser.BROWN_VAL)
	    return new Color(0xA5,0x2A,0x2A);
	if (commandVal == CommandParser.BURLYWOOD_VAL)
	    return new Color(0xDE,0xB8,0x87);
	if (commandVal == CommandParser.CADETBLUE_VAL)
	    return new Color(0x5F,0x9E,0xA0);
	if (commandVal == CommandParser.CHARTREUSE_VAL)
	    return new Color(0x7F,0xFF,0x00);
	if (commandVal == CommandParser.CHOCOLATE_VAL)
	    return new Color(0xD2,0x69,0x1E);
	if (commandVal == CommandParser.CORAL_VAL)
	    return new Color(0xFF,0x7F,0x50);
	if (commandVal == CommandParser.CORNFLOWERBLUE_VAL)
	    return new Color(0x64,0x95,0xED);
	if (commandVal == CommandParser.CORNSILK_VAL)
	    return new Color(0xFF,0xF8,0xDC);
	if (commandVal == CommandParser.CRIMSON_VAL)
	    return new Color(0xDC,0x14,0x3C);
	if (commandVal == CommandParser.CYAN_VAL)
	    return new Color(0x00,0xFF,0xFF);
	if (commandVal == CommandParser.DARKBLUE_VAL)
	    return new Color(0x00,0x00,0x8B);
	if (commandVal == CommandParser.DARKCYAN_VAL)
	    return new Color(0x00,0x8B,0x8B);
	if (commandVal == CommandParser.DARKGOLDENROD_VAL)
	    return new Color(0xB8,0x86,0x0B);
	if (commandVal == CommandParser.DARKGRAY_VAL)
	    return new Color(0xA9,0xA9,0xA9);
	if (commandVal == CommandParser.DARKGREEN_VAL)
	    return new Color(0x00,0x64,0x00);
	if (commandVal == CommandParser.DARKKHAKI_VAL)
	    return new Color(0xBD,0xB7,0x6B);
	if (commandVal == CommandParser.DARKMAGENTA_VAL)
	    return new Color(0x8B,0x00,0x8B);
	if (commandVal == CommandParser.DARKOLIVEGREEN_VAL)
	    return new Color(0x55,0x6B,0x2F);
	if (commandVal == CommandParser.DARKORANGE_VAL)
	    return new Color(0xFF,0x8C,0x00);
	if (commandVal == CommandParser.DARKORCHID_VAL)
	    return new Color(0x99,0x32,0xCC);
	if (commandVal == CommandParser.DARKRED_VAL)
	    return new Color(0x8B,0x00,0x00);
	if (commandVal == CommandParser.DARKSALMON_VAL)
	    return new Color(0xE9,0x96,0x7A);
	if (commandVal == CommandParser.DARKSEAGREEN_VAL)
	    return new Color(0x8F,0xBC,0x8F);
	if (commandVal == CommandParser.DARKSLATEBLUE_VAL)
	    return new Color(0x48,0x3D,0x8B);
	if (commandVal == CommandParser.DARKSLATEGRAY_VAL)
	    return new Color(0x2F,0x4F,0x4F);
	if (commandVal == CommandParser.DARKTURQUOISE_VAL)
	    return new Color(0x00,0xCE,0xD1);
	if (commandVal == CommandParser.DARKVIOLET_VAL)
	    return new Color(0x94,0x00,0xD3);
	if (commandVal == CommandParser.DEEPPINK_VAL)
	    return new Color(0xFF,0x14,0x93);
	if (commandVal == CommandParser.DEEPSKYBLUE_VAL)
	    return new Color(0x00,0xBF,0xFF);
	if (commandVal == CommandParser.DIMGRAY_VAL)
	    return new Color(0x69,0x69,0x69);
	if (commandVal == CommandParser.DODGERBLUE_VAL)
	    return new Color(0x1E,0x90,0xFF);
	if (commandVal == CommandParser.FIREBRICK_VAL)
	    return new Color(0xB2,0x22,0x22);
	if (commandVal == CommandParser.FLORALWHITE_VAL)
	    return new Color(0xFF,0xFA,0xF0);
	if (commandVal == CommandParser.FORESTGREEN_VAL)
	    return new Color(0x22,0x8B,0x22);
	if (commandVal == CommandParser.FUCHSIA_VAL)
	    return new Color(0xFF,0x00,0xFF);
	if (commandVal == CommandParser.GAINSBORO_VAL)
	    return new Color(0xDC,0xDC,0xDC);
	if (commandVal == CommandParser.GHOSTWHITE_VAL)
	    return new Color(0xF8,0xF8,0xFF);
	if (commandVal == CommandParser.GOLD_VAL)
	    return new Color(0xFF,0xD7,0x00);
	if (commandVal == CommandParser.GOLDENROD_VAL)
	    return new Color(0xDA,0xA5,0x20);
	if (commandVal == CommandParser.GRAY_VAL)
	    return new Color(0x80,0x80,0x80);
	if (commandVal == CommandParser.GREEN_VAL)
	    return new Color(0x00,0x80,0x00);
	if (commandVal == CommandParser.GREENYELLOW_VAL)
	    return new Color(0xAD,0xFF,0x2F);
	if (commandVal == CommandParser.HONEYDEW_VAL)
	    return new Color(0xF0,0xFF,0xF0);
	if (commandVal == CommandParser.HOTPINK_VAL)
	    return new Color(0xFF,0x69,0xB4);
	if (commandVal == CommandParser.INDIANRED_VAL)
	    return new Color(0xCD,0x5C,0x5C);
	if (commandVal == CommandParser.INDIGO_VAL)
	    return new Color(0x4B,0x00,0x82);
	if (commandVal == CommandParser.IVORY_VAL)
	    return new Color(0xFF,0xFF,0xF0);
	if (commandVal == CommandParser.KHAKI_VAL)
	    return new Color(0xF0,0xE6,0x8C);
	if (commandVal == CommandParser.LAVENDER_VAL)
	    return new Color(0xE6,0xE6,0xFA);
	if (commandVal == CommandParser.LAVENDERBLUSH_VAL)
	    return new Color(0xFF,0xF0,0xF5);
	if (commandVal == CommandParser.LAWNGREEN_VAL)
	    return new Color(0x7C,0xFC,0x00);
	if (commandVal == CommandParser.LEMONCHIFFON_VAL)
	    return new Color(0xFF,0xFA,0xCD);
	if (commandVal == CommandParser.LIGHTBLUE_VAL)
	    return new Color(0xAD,0xD8,0xE6);
	if (commandVal == CommandParser.LIGHTCORAL_VAL)
	    return new Color(0xF0,0x80,0x80);
	if (commandVal == CommandParser.LIGHTCYAN_VAL)
	    return new Color(0xE0,0xFF,0xFF);
	if (commandVal == CommandParser.LIGHTGOLDENRODYELLOW_VAL)
	    return new Color(0xFA,0xFA,0xD2);
	if (commandVal == CommandParser.LIGHTGREEN_VAL)
	    return new Color(0x90,0xEE,0x90);
	if (commandVal == CommandParser.LIGHTGRAY_VAL)
	    return new Color(0xD3,0xD3,0xD3);
	if (commandVal == CommandParser.LIGHTPINK_VAL)
	    return new Color(0xFF,0xB6,0xC1);
	if (commandVal == CommandParser.LIGHTSALMON_VAL)
	    return new Color(0xFF,0xA0,0x7A);
	if (commandVal == CommandParser.LIGHTSEAGREEN_VAL)
	    return new Color(0x20,0xB2,0xAA);
	if (commandVal == CommandParser.LIGHTSKYBLUE_VAL)
	    return new Color(0x87,0xCE,0xFA);
	if (commandVal == CommandParser.LIGHTSLATEGRAY_VAL)
	    return new Color(0x77,0x88,0x99);
	if (commandVal == CommandParser.LIGHTSTEELBLUE_VAL)
	    return new Color(0xB0,0xC4,0xDE);
	if (commandVal == CommandParser.LIGHTYELLOW_VAL)
	    return new Color(0xFF,0xFF,0xE0);
	if (commandVal == CommandParser.LIME_VAL)
	    return new Color(0x00,0xFF,0x00);
	if (commandVal == CommandParser.LIMEGREEN_VAL)
	    return new Color(0x32,0xCD,0x32);
	if (commandVal == CommandParser.LINEN_VAL)
	    return new Color(0xFA,0xF0,0xE6);
	if (commandVal == CommandParser.MAGENTA_VAL)
	    return new Color(0xFF,0x00,0xFF);
	if (commandVal == CommandParser.MAROON_VAL)
	    return new Color(0x80,0x00,0x00);
	if (commandVal == CommandParser.MEDIUMAQUAMARINE_VAL)
	    return new Color(0x66,0xCD,0xAA);
	if (commandVal == CommandParser.MEDIUMBLUE_VAL)
	    return new Color(0x00,0x00,0xCD);
	if (commandVal == CommandParser.MEDIUMORCHID_VAL)
	    return new Color(0xBA,0x55,0xD3);
	if (commandVal == CommandParser.MEDIUMPURPLE_VAL)
	    return new Color(0x93,0x70,0xDB);
	if (commandVal == CommandParser.MEDIUMSEAGREEN_VAL)
	    return new Color(0x3C,0xB3,0x71);
	if (commandVal == CommandParser.MEDIUMSLATEBLUE_VAL)
	    return new Color(0x7B,0x68,0xEE);
	if (commandVal == CommandParser.MEDIUMSPRINGGREEN_VAL)
	    return new Color(0x00,0xFA,0x9A);
	if (commandVal == CommandParser.MEDIUMTURQUOISE_VAL)
	    return new Color(0x48,0xD1,0xCC);
	if (commandVal == CommandParser.MEDIUMVIOLETRED_VAL)
	    return new Color(0xC7,0x15,0x85);
	if (commandVal == CommandParser.MIDNIGHTBLUE_VAL)
	    return new Color(0x19,0x19,0x70);
	if (commandVal == CommandParser.MINTCREAM_VAL)
	    return new Color(0xF5,0xFF,0xFA);
	if (commandVal == CommandParser.MISTYROSE_VAL)
	    return new Color(0xFF,0xE4,0xE1);
	if (commandVal == CommandParser.MOCCASIN_VAL)
	    return new Color(0xFF,0xE4,0xB5);
	if (commandVal == CommandParser.NAVAJOWHITE_VAL)
	    return new Color(0xFF,0xDE,0xAD);
	if (commandVal == CommandParser.NAVY_VAL)
	    return new Color(0x00,0x00,0x80);
	if (commandVal == CommandParser.OLDLACE_VAL)
	    return new Color(0xFD,0xF5,0xE6);
	if (commandVal == CommandParser.OLIVE_VAL)
	    return new Color(0x80,0x80,0x00);
	if (commandVal == CommandParser.OLIVEDRAB_VAL)
	    return new Color(0x6B,0x8E,0x23);
	if (commandVal == CommandParser.ORANGE_VAL)
	    return new Color(0xFF,0xA5,0x00);
	if (commandVal == CommandParser.ORANGERED_VAL)
	    return new Color(0xFF,0x45,0x00);
	if (commandVal == CommandParser.ORCHID_VAL)
	    return new Color(0xDA,0x70,0xD6);
	if (commandVal == CommandParser.PALEGOLDENROD_VAL)
	    return new Color(0xEE,0xE8,0xAA);
	if (commandVal == CommandParser.PALEGREEN_VAL)
	    return new Color(0x98,0xFB,0x98);
	if (commandVal == CommandParser.PALETURQUOISE_VAL)
	    return new Color(0xAF,0xEE,0xEE);
	if (commandVal == CommandParser.PALEVIOLETRED_VAL)
	    return new Color(0xDB,0x70,0x93);
	if (commandVal == CommandParser.PAPAYAWHIP_VAL)
	    return new Color(0xFF,0xEF,0xD5);
	if (commandVal == CommandParser.PEACHPUFF_VAL)
	    return new Color(0xFF,0xDA,0xB9);
	if (commandVal == CommandParser.PERU_VAL)
	    return new Color(0xCD,0x85,0x3F);
	if (commandVal == CommandParser.PINK_VAL)
	    return new Color(0xFF,0xC0,0xCB);
	if (commandVal == CommandParser.PLUM_VAL)
	    return new Color(0xDD,0xA0,0xDD);
	if (commandVal == CommandParser.POWDERBLUE_VAL)
	    return new Color(0xB0,0xE0,0xE6);
	if (commandVal == CommandParser.PURPLE_VAL)
	    return new Color(0x80,0x00,0x80);
	if (commandVal == CommandParser.RED_VAL)
	    return new Color(0xFF,0x00,0x00);
	if (commandVal == CommandParser.ROSYBROWN_VAL)
	    return new Color(0xBC,0x8F,0x8F);
	if (commandVal == CommandParser.ROYALBLUE_VAL)
	    return new Color(0x41,0x69,0xE1);
	if (commandVal == CommandParser.SADDLEBROWN_VAL)
	    return new Color(0x8B,0x45,0x13);
	if (commandVal == CommandParser.SALMON_VAL)
	    return new Color(0xFA,0x80,0x72);
	if (commandVal == CommandParser.SANDYBROWN_VAL)
	    return new Color(0xF4,0xA4,0x60);
	if (commandVal == CommandParser.SEAGREEN_VAL)
	    return new Color(0x2E,0x8B,0x57);
	if (commandVal == CommandParser.SEASHELL_VAL)
	    return new Color(0xFF,0xF5,0xEE);
	if (commandVal == CommandParser.SIENNA_VAL)
	    return new Color(0xA0,0x52,0x2D);
	if (commandVal == CommandParser.SILVER_VAL)
	    return new Color(0xC0,0xC0,0xC0);
	if (commandVal == CommandParser.SKYBLUE_VAL)
	    return new Color(0x87,0xCE,0xEB);
	if (commandVal == CommandParser.SLATEBLUE_VAL)
	    return new Color(0x6A,0x5A,0xCD);
	if (commandVal == CommandParser.SLATEGRAY_VAL)
	    return new Color(0x70,0x80,0x90);
	if (commandVal == CommandParser.SNOW_VAL)
	    return new Color(0xFF,0xFA,0xFA);
	if (commandVal == CommandParser.SPRINGGREEN_VAL)
	    return new Color(0x00,0xFF,0x7F);
	if (commandVal == CommandParser.STEELBLUE_VAL)
	    return new Color(0x46,0x82,0xB4);
	if (commandVal == CommandParser.TAN_VAL)
	    return new Color(0xD2,0xB4,0x8C);
	if (commandVal == CommandParser.TEAL_VAL)
	    return new Color(0x00,0x80,0x80);
	if (commandVal == CommandParser.THISTLE_VAL)
	    return new Color(0xD8,0xBF,0xD8);
	if (commandVal == CommandParser.TOMATO_VAL)
	    return new Color(0xFF,0x63,0x47);
	if (commandVal == CommandParser.TURQUOISE_VAL)
	    return new Color(0x40,0xE0,0xD0);
	if (commandVal == CommandParser.VIOLET_VAL)
	    return new Color(0xEE,0x82,0xEE);
	if (commandVal == CommandParser.WHEAT_VAL)
	    return new Color(0xF5,0xDE,0xB3);
	if (commandVal == CommandParser.WHITE_VAL)
	    return new Color(0xFF,0xFF,0xFF);
	if (commandVal == CommandParser.WHITESMOKE_VAL)
	    return new Color(0xF5,0xF5,0xF5);
	if (commandVal == CommandParser.YELLOW_VAL)
	    return new Color(0xFF,0xFF,0x00);
	if (commandVal == CommandParser.YELLOWGREEN_VAL)
	    return new Color(0x9A,0xCD,0x32);
	return null;
    }

    // This method is needed in run() function
    private boolean hold(int time)
    {
	try { Thread.currentThread().sleep(time); }
	catch (InterruptedException e) { e.printStackTrace(); }
	return true;
    }  

    public void actionPerformed(ActionEvent evt)
    {
	Object source = evt.getSource();
	String input = inOutPanel_.getInput().trim();
	if (input == null) return;
	String text = input.trim();
	if (text.length() <= 0) return;
	System.out.println(INVITATION_LINE + text);
	inOutPanel_.setText(text + "\n");
	userInput_ = new String(text.trim());
	while (userInput_ != null) hold(50); // Hold to empty input line
	// untill command is executed. See function run().
    }

    // Object of this class print stream into output text fields and areas of 
    // the application
    class BelkaOutputStream extends OutputStream
    {
	public void close() {}
	public void flush() {}
	public void write(byte[] b) 
	{
	    String out = new String(b);
	    if (outTextArea_ != null) outTextArea_.append(out);
	    if (inOutPanel_  != null) inOutPanel_.setText(out);
	}
	public void write(byte[] b,int off,int len) 
	{
	    String out = new String(b,off,len);
	    if (outTextArea_ != null) outTextArea_.append(out);
	    if (inOutPanel_  != null) inOutPanel_.setText(out);
	}
	public void write(int b)
	{
	    String out = null;
	    try {
		out = Integer.toString(b);
	    } catch (Exception e) {
		return;
	    }
	    if (outTextArea_ == null) outTextArea_.append(out);
	    if (inOutPanel_  != null) inOutPanel_.setText(out);
	}
    }
}

