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
 * Object of this class load annotation/group file either from file or URL.
 *
 * @author Alexej Abyzov
 */
public class GroupLoader
{
    /**
     * Saves molecules into a annotation/group file. 
     *
     * @param molsToSave list molecules to be save.
     * @param urlFileName name of file on the web to save.
     *
     * @return number of atoms saved. Negative if error happend.
     */
    public int saveToFile(URL urlFileName,Molecule molsToSave)
    {
	int ret = -1;
	OutputStream outStream = null;
	try {
 	    URLConnection conn = urlFileName.openConnection();
	    conn.setDoOutput(true);
	    outStream = new BufferedOutputStream(conn.getOutputStream());
	    ret = saveToFile(outStream,molsToSave);
	} catch (Exception e) {
	    System.err.println("Exception while writing group file to URL:");
	    System.err.println(urlFileName.toString());
	    System.err.println(e.toString());
	    return -1;
	}

	try { outStream.close(); } catch (Exception e) {}
	return ret;
    }

    /**
     * Saves molecules into a annotation/group file. 
     *
     * @param molsToSave list of molecules to be save.
     * @param file name of file to save.
     *
     * @return number of atoms saved. Negative if error happend.
     */
    public int saveToFile(File file,Molecule molsToSave)
    {
	int ret = -1;
	OutputStream outStream = null;
	try {
	    outStream =	new BufferedOutputStream(new FileOutputStream(file));
	    ret = saveToFile(outStream,molsToSave);
	} catch (Exception e) {
	    System.err.println("Exception while writing group file:");
	    System.err.println(file.getName());
	    System.err.println(e.toString());
	} 
	
	try { outStream.close(); } catch (Exception e) {}
	return ret;
    }

    // Saving annotation/group
    private int saveToFile(OutputStream outStream,Molecule mols)
    {
	int ret = 0;
	try {
	    GZIPOutputStream zos = new GZIPOutputStream(outStream);
	    PrintWriter      out = new PrintWriter(zos);
	    for (Molecule m = mols;m != null;m = m.next())
		for (Chain c = m.chainList();c != null;c = c.next())
		    for (Assembly a = c.assemblyList();a != null;a = a.next())
			if (!a.isGap())	out.println(a.getGroupId());
	    out.close();
	} catch (Exception e) {
	    System.err.println("Exception while writing to group file.");
	    System.err.println(e.toString());
	    return -1;
	}
	for (Molecule m = mols;m != null;m = m.next())
	    ret  += m.countAtoms();
	return ret;
    }

    /**
     * Loads annotation/group file and creates a molecule.
     *
     * @param file file to load.
     * @param mols list of molecules to be annotated.
     *
     * @return true if file was loaded and annotation assigned to molecule,
     * false otherwise.
     */
    public boolean loadFile(File file,Molecule mols)
    {
	if (mols == null) return false;

	boolean ret = false;
	InputStream inStream = null;
	try {
	    inStream = new BufferedInputStream(new FileInputStream(file));
	    ret = loadFile(inStream,mols);
	} catch (Exception e) {
	    System.err.println("Exception while loading local group file:");
	    System.err.println(file.getName());
	    System.err.println(e.toString());
	}
	try { inStream.close(); } catch (Exception e) {}
	return ret;
    }

    /**
     * Loads annotation/group file from URL.
     *
     * @param urlFileName name of file on URL.
     * @param mols list of molecules to be annotated.
     *
     * @return true if file was loaded and annotation assigned to molecule,
     * false otherwise.
     */
    public boolean loadFile(URL urlFileName,Molecule mols)
    {
	if (mols == null) return false;

	boolean ret = false;
	InputStream inStream = null;
	try {
 	    URLConnection conn = urlFileName.openConnection();
	    conn.setDoInput(true);
	    inStream = new BufferedInputStream(conn.getInputStream());
	    ret = loadFile(inStream,mols);
	} catch (Exception e) {
	    System.err.println("Exception while loading group file from " +
			       "URL:");
	    System.err.println(urlFileName.toString());
	    System.err.println(e.toString());
	}
	try { inStream.close(); } catch (Exception e) {}
	return ret;
    }

    // Loading annotation/group
    private boolean loadFile(InputStream inStream,Molecule mols)
    {
	boolean ret = false;
	inStream.mark(1024*1024*128);
	try {
	    GZIPInputStream zis = new GZIPInputStream(inStream);
	    BufferedReader in = new BufferedReader(new InputStreamReader(zis));
	    if (countAnnotations(in) != countAssemblies(mols))
		return false;
	    inStream.reset();
	    zis = new GZIPInputStream(inStream);
	    in  = new BufferedReader(new InputStreamReader(zis));
	    for (Molecule m = mols;m != null;m = m.next())
		for (Chain c = m.chainList();c != null;c = c.next())
		    for (Assembly a = c.assemblyList();a != null;
			 a = a.next())
			if (!a.isGap())	{
			    String line = in.readLine();
			    a.setGroupId(Integer.parseInt(line));
			}
	    ret = true;
	} catch (Exception e) {
	    System.err.println("Exception while loading group file.");
	    System.err.println(e.toString());
	}
	return ret;
    }

    // Count number of annotations in a file
    private int countAnnotations(BufferedReader in)
    {
	int ret = 0;
	String line = null;
	try {
	    while ((line = in.readLine()) != null) ret++;
	} catch (Exception e) {}
	return ret;
    }

    // Count number of residues/nucleotides in molecules
    private int countAssemblies(Molecule mols)
    {
	int ret = 0;
	for (Molecule m = mols;m != null;m = m.next())
	    ret += m.countAssemblies();
	return ret;
    }
}
