package belka.menu;

//--- Java imports ---
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.util.*;

//--- Application imports ---
import belka.*;
import belka.parser.*;

/**
 * The class implements the memu 'File' with items to load and save molecules
 * to/from file.
 *
 * @author Alexej Abyzov
 */

public class MenuFile extends JMenu
{
    // Last used directory
    static String lastUsedDir_ = null;

    // Last used URL
    static String lastUsedURL_ = null;

    // Menu items
    JMenuItem openFileMI_    = null;
    JMenuItem openFileURLMI_ = null;
    JMenuItem openFilePDBMI_ = null;
    JMenuItem saveFileMI_    = null;
    JMenuItem openProjectMI_ = null;
    JMenuItem saveProjectMI_ = null;
    JMenuItem quitMI_        = null;

    // Reference to manager
    BelkaManager manager_ = null;

    /**
     * Object constructor
     *
     * @param manager manger which will execute commands comming from the menu
     */

    public MenuFile(BelkaManager manager)
    {
	super("File");

	manager_ = manager;

	MenuFileListener menuFileListener = new MenuFileListener();

	int actionEventMask = ActionEvent.CTRL_MASK;
	if (System.getProperty("os.name").startsWith("Mac"))
	    actionEventMask = ActionEvent.META_MASK;
	

	add( (openFileMI_ = new JMenuItem("Open file")) );
	openFileMI_.addActionListener(menuFileListener);
	openFileMI_.
	    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
						  ActionEvent.META_MASK));

	add( (openFileURLMI_ = new JMenuItem("Open file from URL")) );
	openFileURLMI_.addActionListener(menuFileListener);
	openFileURLMI_.
	    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
						  ActionEvent.META_MASK));

	add( (openFilePDBMI_ = new JMenuItem("Open structure from PDB")) );
	openFilePDBMI_.addActionListener(menuFileListener);
	openFilePDBMI_.
	    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
						  ActionEvent.META_MASK));

	add( (saveFileMI_ = new JMenuItem("Save file")) );
	saveFileMI_.addActionListener(menuFileListener);
	saveFileMI_.
	    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						  ActionEvent.META_MASK));

        addSeparator();

	add( (openProjectMI_ = new JMenuItem("Open project")) );
	openProjectMI_.addActionListener(menuFileListener);
	openProjectMI_.
	    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
						  ActionEvent.SHIFT_MASK |
						  ActionEvent.META_MASK));

	add( (saveProjectMI_ = new JMenuItem("Save project")) );
	saveProjectMI_.addActionListener(menuFileListener);
	saveProjectMI_.
	    setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
						  ActionEvent.SHIFT_MASK |
						  ActionEvent.META_MASK));

        addSeparator();

	add( (quitMI_ = new JMenuItem("Quit")) );
        quitMI_.addActionListener(menuFileListener);
        quitMI_.
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                  ActionEvent.META_MASK));
    }

    private class MenuFileListener implements ActionListener
    {
	public void actionPerformed(ActionEvent event)
	{
	    Object source = event.getSource();

	    if (source == openFileMI_) {
		String[] fileNames = getFileNamesToOpen();
		if (fileNames == null) return;
		String command = "load";
		for (int i = 0;i < fileNames.length;i++)
		    command += " " + fileNames[i];
		manager_.runScript(command);
	    } else if (source == openFileURLMI_) {
		String urlName = getURLNameToOpen();
		if (urlName == null) return;
		manager_.runScript("load " + urlName);
	    } else if (source == openFilePDBMI_) {
		String urlName = getPDBCodeToOpen();
		if (urlName == null) return;
		manager_.runScript("pdbload " + urlName);
	    } else if (source == saveFileMI_) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Belka: save file");
		fileChooser.setFileFilter(new PDBFileFilter());
		String fileName = getFileNameFromUser(fileChooser,true);
		if (fileName == null) return;
		String command = "save " + fileName;
		manager_.runScript(command);
	    } else if (source == openProjectMI_) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Belka: open project");
		fileChooser.setFileFilter(new ProjectFileFilter());
		String fileName = getFileNameFromUser(fileChooser,false);
		if (fileName == null) return;
		String command = "load project " + fileName;
		manager_.setUserInput(command);
	    } else if (source == saveProjectMI_) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Belka: save project");
		fileChooser.setFileFilter(new ProjectFileFilter());
		String fileName = getFileNameFromUser(fileChooser,true);
		if (fileName == null) return;
		String command = "save project " + fileName;
		manager_.runScript(command);
            } else if (source == quitMI_) System.exit(0);
	}
    }

    /**
     * The function gets file name from user.
     */
    private String getFileNameFromUser(JFileChooser fileChooser,boolean save)
    {
	if (lastUsedDir_ == null)
	    lastUsedDir_ = System.getProperty("user.dir");
	fileChooser.setCurrentDirectory(new File(lastUsedDir_));
	fileChooser.setMultiSelectionEnabled(false);
	Component base = null;
	if (manager_ != null) base = manager_.getMainComponent();
	int ret = 0;
	if (save) ret = fileChooser.showSaveDialog(base);
	else      ret = fileChooser.showOpenDialog(base);
	
	String inFileName = null;
	if (ret == JFileChooser.APPROVE_OPTION) {
	    lastUsedDir_ = fileChooser.getCurrentDirectory().toString();
	    File selFile = fileChooser.getSelectedFile();
	    inFileName = lastUsedDir_ + System.getProperty("file.separator") +
		selFile.getName();
	}
	fileChooser.setVisible(false);
	
	return inFileName;
    }

    /**
     * The function gets PDB code by using JOptionPane
     */
    private String getPDBCodeToOpen()
    {
	Component base = null;
	if (manager_ != null) base = manager_.getMainComponent();
	Object out = JOptionPane.showInputDialog(base,"Enter PDB codes " +
						 "(4 characters each) " +
						 "separated by spaces",
						 "Belka: open structure by " +
						 "PDB codes",
						 JOptionPane.PLAIN_MESSAGE,
						 null,null,"");

	String ret = null;
	if (out != null) ret = out.toString();

	return ret;
    }

    /**
     * The function gets URL name to open by using JOptionPane.
     */
    private String getURLNameToOpen()
    {
	Component base = null;
	if (manager_ != null) base = manager_.getMainComponent();
	Object out = JOptionPane.showInputDialog(base,"Enter URL",
						 "Belka: open file from URL",
						 JOptionPane.PLAIN_MESSAGE,
						 null,null,
						 lastUsedURL_);

	String ret = null;
	if (out != null) ret = out.toString();
	if (ret != null) lastUsedURL_ = ret;

	return ret;
    }

    /**
     * The function gets file names to open using JFileChooser.
     */
    private String[] getFileNamesToOpen()
    {
	JFileChooser fileChooser = new JFileChooser();
	fileChooser.setDialogTitle("Belka: open files");
	fileChooser.setFileFilter(new PDBFileFilter());
	if (lastUsedDir_ == null)
	    lastUsedDir_ = System.getProperty("user.dir");
	fileChooser.setCurrentDirectory(new File(lastUsedDir_));
	fileChooser.setMultiSelectionEnabled(true);
	Component base = null;
	if (manager_ != null) base = manager_.getMainComponent();
	int ret = fileChooser.showOpenDialog(base);
	
	String[] inFileNames = null;
	if (ret == JFileChooser.APPROVE_OPTION) {
	    lastUsedDir_ = fileChooser.getCurrentDirectory().toString();
	    File[] selFiles = fileChooser.getSelectedFiles();
	    inFileNames = new String[selFiles.length];
	    for (int i = 0;i < selFiles.length;i++)
		inFileNames[i] = lastUsedDir_ +
		    System.getProperty("file.separator") +
		    selFiles[i].getName();
	}
	fileChooser.setVisible(false);
	
	return inFileNames;
    }

//     public static String getFileName(JFileChooser fileChooser,boolean save)
//     {
// 	if (fileChooser == null) return null;

// 	if (currDir == null) currDir = System.getProperty("user.dir");
// 	fileChooser.setCurrentDirectory(new File(currDir));
// 	fileChooser.setMultiSelectionEnabled(false);
// 	int ret = 0;
// 	if (save) ret = fileChooser.showSaveDialog(null);
// 	else      ret = fileChooser.showOpenDialog(null);

// 	String outFile = null;
// 	if (ret == JFileChooser.APPROVE_OPTION) {
// 	    currDir = fileChooser.getCurrentDirectory().toString();
// 	    outFile = currDir +
// 		System.getProperty("file.separator") +
// 		fileChooser.getSelectedFile().getName();
// 	}
// 	fileChooser.hide();

// 	return outFile;
//     }
}

class ProjectFileFilter extends javax.swing.filechooser.FileFilter
{
    public boolean accept(File file)
    {
	if (file.isDirectory()) return true;

	String fileName = file.getName().trim().toLowerCase();

	String extensions[] = FormatResolver.FORMAT_PROJECT_EXTENSIONS;
	if (extensions == null) return true;

	for (int i = 0;i < extensions.length;i++) {
	    if (extensions[i] == null) continue;
	    if (fileName.endsWith(extensions[i])) return true;
	}

	return false;
    }

    public String getDescription()
    {
	String ret = "Belka project (";

	String extensions[] = FormatResolver.FORMAT_PROJECT_EXTENSIONS;
	if (extensions != null)
	    for (int i = 0;i < extensions.length;i++) {
		if (extensions[i] == null) continue;
		if (i != 0) ret += " ";
		ret += extensions[i];
	    }
	ret += ")";

	return ret;
    }
}

class PDBFileFilter extends javax.swing.filechooser.FileFilter
{
    public boolean accept(File file)
    {
	if (file.isDirectory()) return true;

	String fileName = file.getName().trim().toLowerCase();

	String extensions[] = FormatResolver.FORMAT_PDB_EXTENSIONS;
	if (extensions == null) return true;

	for (int i = 0;i < extensions.length;i++) {
	    if (extensions[i] == null) continue;
	    if (fileName.endsWith(extensions[i])) return true;
	}

	return false;
    }

    public String getDescription()
    {
	String ret = "PDB (";

	String extensions[] = FormatResolver.FORMAT_PDB_EXTENSIONS;
	if (extensions != null)
	    for (int i = 0;i < extensions.length;i++) {
		if (extensions[i] == null) continue;
		if (i != 0) ret += " ";
		ret += extensions[i];
	    }
	ret += ")";

	return ret;
    }
}

class PNGFileFilter extends javax.swing.filechooser.FileFilter
{
    public boolean accept(File file)
    {
	if (file.isDirectory()) return true;
	
	String fileName = file.getName().trim().toLowerCase();
	
	if (fileName.endsWith(".png")) return true;
	
	return false;
    }

    public String getDescription() { return "PNG ( .png)"; }

    public String defaultExtension() { return ".png"; }
}

class JPEGFileFilter extends javax.swing.filechooser.FileFilter
{
    public boolean accept(File file)
    {
	if (file.isDirectory()) return true;
	
	String fileName = file.getName().trim().toLowerCase();
	
	if (fileName.endsWith(".jpeg")) return true;
	if (fileName.endsWith(".jpg")) return true;
	
	return false;
    }
    public String getDescription() { return "JPEG ( .jpeg, .jpg)"; }
}

class FastaFileFilter extends javax.swing.filechooser.FileFilter
{
    public boolean accept(File file)
    {
	if (file.isDirectory()) return true;
	
	String fileName = file.getName().trim().toLowerCase();
	
	if (fileName.endsWith(".fa"))    return true;
	if (fileName.endsWith(".fst"))   return true;
	if (fileName.endsWith(".fasta")) return true;

	return false;
    }

    public String getDescription() { return "FASTA ( .fa, .fst, .fasta)"; }
}

class PIRFileFilter extends javax.swing.filechooser.FileFilter
{
    public boolean accept(File file)
    {
	if (file.isDirectory()) return true;
	
	String fileName = file.getName().trim().toLowerCase();

	if (fileName.endsWith(".pir"))   return true;
	if (fileName.endsWith(".ali"))   return true;
	
	return false;
    }
		
    public String getDescription() { return "PIR ( .pir, .ali)"; }
}

class ClustalFileFilter extends javax.swing.filechooser.FileFilter
{
    public boolean accept(File file)
    {
	if (file.isDirectory()) return true;
	
	String fileName = file.getName().trim().toLowerCase();
	
	if (fileName.endsWith(".aln"))   return true;
	
	return false;
    }

    public String getDescription() { return "Clustal ( .aln)"; }
}
