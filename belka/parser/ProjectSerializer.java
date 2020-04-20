package belka.parser;

//--- Java imports ---
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

//--- Application imports ---
import belka.mol.*;
import belka.chem.*;

/**
 * Object of this class load project-file either from file or URL.
 *
 * @author Alexej Abyzov
 */
public class ProjectSerializer
{
    /**
     * Saves molecules into a project-file. 
     *
     * @param molsToSave list molecules to be save.
     * @param urlFileName name of file on the web to save.
     *
     * @return number of atoms saved. Negative if error happend.
     */
    public int saveToFile(Molecule molsToSave,URL urlFileName)
    {
	if (molsToSave == null) return -1;

	int ret = -1;
	OutputStream outStream = null;
	try {
 	    URLConnection conn = urlFileName.openConnection();
	    conn.setDoOutput(true);
	    outStream = new BufferedOutputStream(conn.getOutputStream());
	    ret = saveToFile(molsToSave,outStream);
	} catch (Exception e) {
	    System.err.println("Exception while writing project-file to URL:");
	    System.err.println(urlFileName.toString());
	    System.err.println(e.toString());
	    return -1;
	}

	try { outStream.close(); } catch (Exception e) {}
	return ret;
    }

    /**
     * Saves molecules into a project-file. 
     *
     * @param molsToSave list of molecules to be save.
     * @param file name of file to save.
     *
     * @return number of atoms saved. Negative if error happend.
     */
    public int saveToFile(Molecule molsToSave,File file)
    {
	if (molsToSave == null) return -1;

	int ret = -1;
	OutputStream outStream = null;
	try {
	    outStream =	new BufferedOutputStream(new FileOutputStream(file));
	    ret = saveToFile(molsToSave,outStream);
	} catch (Exception e) {
	    System.err.println("Exception while writing project-file:");
	    System.err.println(file.getName());
	    System.err.println(e.toString());
	} 
	
	try { outStream.close(); } catch (Exception e) {}
	return ret;
    }

    // Saving project
    private int saveToFile(Molecule molsToSave,OutputStream outStream)
    {
	int ret = 0;
	try {
	    GZIPOutputStream zos   = new GZIPOutputStream(outStream);
	    ObjectOutputStream out = new ObjectOutputStream(zos);
	    out.writeObject(molsToSave);
	    out.close();
	} catch (Exception e) {
	    System.err.println("Exception while writing to project-file.");
	    System.err.println(e.toString());
	    return -1;
	}
	for (Molecule m = molsToSave;m != null;m = m.next())
	    ret  += m.countAtoms();
	return ret;
    }

    /**
     * Loads project-file and creates a molecule.
     *
     * @param file file to load.
     * @return list of loaded molecules.
     */
    public Molecule loadFromFile(File file)
    {
	Molecule ret = null;
	InputStream inStream = null;
	try {
	    inStream = new BufferedInputStream(new FileInputStream(file));
	    ret = loadFromFile(inStream);
	} catch (Exception e) {
	    System.err.println("Exception while loading local project-file:");
	    System.err.println(file.getName());
	    System.err.println(e.toString());
	}
	try { inStream.close(); } catch (Exception e) {}
	return ret;
    }

    /**
     * Loads project-file from URL.
     *
     * @param urlFileName name of file on URL.
     * @return list of loaded molecules.
     */
    public Molecule loadFromFile(URL urlFileName)
    {
	Molecule ret = null;
	InputStream inStream = null;
	try {
 	    URLConnection conn = urlFileName.openConnection();
	    conn.setDoInput(true);
	    inStream = new BufferedInputStream(conn.getInputStream());
	    ret = loadFromFile(inStream);
	} catch (Exception e) {
	    System.err.println("Exception while loading project-file from " +
			       "URL:");
	    System.err.println(urlFileName.toString());
	    System.err.println(e.toString());
	}
	try { inStream.close(); } catch (Exception e) {}
	return ret;
    }

    // Loading project
    private Molecule loadFromFile(InputStream inStream)
    {
	Molecule mols = null;
	try {
	    GZIPInputStream zis = new GZIPInputStream(inStream);
	    ObjectInputStream in = new ObjectInputStream(zis);
	    mols = (Molecule)in.readObject();
	    in.close();
	} catch (Exception e) {
	    System.err.println("Exception while loading project-file.");
	    System.err.println(e.toString());
	}
	return mols;
    }
}
