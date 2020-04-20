package belka.parser;

//--- Java imports ---
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.util.zip.*;

//--- Application imports ---
import belka.*;
import belka.mol.*;

/**
 * Abstract class describing behaviour of a file parser.
 *
 * @author Alexej Abyzov
 */
public abstract class Parser
{
    // Default name
    protected String MOLECULE_NAME_UNKNOWN = "Unknown";

    /**
     * Parses a file and creates a molecule.
     *
     * @param file file to parse.
     *
     * @return new molecules.
     */
    public Molecule[] parseFile(File file)
    {
	Molecule[] ret = null;
	InputStream inStream = null;
	try {
	    inStream = new BufferedInputStream(new FileInputStream(file));
	    ret = parseFile(inStream,file.getName());
	} catch (Exception e) {
	    System.err.print("Exception while parsing local file: ");
	    System.err.println(file.getName());
	    System.err.println(e.toString());
	}
	try { inStream.close(); } catch (Exception e) {}
	return ret;
    }

    /**
     * Parses a file from URL and creates molecules.
     *
     * @param urlFileName name of file on URL.
     * @return new molecules.
     */
    public Molecule[] parseFile(URL urlFileName)
    {
	Molecule[] ret = null;
	InputStream inStream = null;
	try {
 	    URLConnection conn = urlFileName.openConnection();
	    conn.setDoInput(true);
	    inStream = new BufferedInputStream(conn.getInputStream());
	    ret =  parseFile(inStream,
			     new File(urlFileName.getFile()).getName());
	} catch (Exception e) {
	    System.err.print("Exception while parsing file from URL: ");
	    System.err.println(urlFileName.toString());
	    System.err.println(e.toString());
	}
	try { inStream.close(); } catch (Exception e) {}
	return ret;
    }

    // Dispatch to a proper parsing function
    private Molecule[] parseFile(InputStream inStream,String name)
    {
	ArrayList<Molecule> mols = null;
	if (FormatResolver.zippedByExtension(name)) { // ZIPped file
	    mols = parseZippedFile(inStream);
	} else if (FormatResolver.gzippedByExtension(name)) { // GZipped file
	    mols = parseGZippedFile(inStream,
				    FormatResolver.stripGZIPExtension(name));
	} else { // Regular file
	    mols = parseRegularFile(inStream,name);
	}

	if (mols == null) return null;

	int size = mols.size();
	if (size == 0) return null;

	Molecule[] ret = new Molecule[size];
	for (int i = 0;i < size;i++) ret[i] = (Molecule)mols.remove(0);

	return ret;
    }

    // Wraping zip stream
    private ArrayList<Molecule> parseZippedFile(InputStream inStream)
    {
	ArrayList<Molecule> mols = new ArrayList<Molecule>();
	try {
	    ZipInputStream is = new ZipInputStream(inStream);
	    ZipEntry ze = null;
	    while ((ze = is.getNextEntry()) != null) {
		if (!ze.isDirectory()) {
		    String molName = new File(ze.getName()).getName();
		    mols.addAll(parseStream(is,molName));
		}
		is.closeEntry();
	    }
	    is.close();
	} catch (Exception e) {
	    System.err.println("Exception while parsing zip-file.");
	    System.err.println(e.toString());
	}
	return mols;
    }

    // Wraping gzip stream
    private ArrayList<Molecule> parseGZippedFile(InputStream inStream,
						 String molName)
    {
	ArrayList<Molecule> mols = null;
	try {
	    InputStream is = new GZIPInputStream(inStream);
	    mols = parseStream(is,molName);
	    is.close();
	} catch (Exception e) {
	    System.err.println("Exception while parsing gzip-file.");
	    System.err.println(e.toString());
	}
	return mols;
    }

    // Just parsing file
    private ArrayList<Molecule> parseRegularFile(InputStream inStream,
						   String molName)
    {
	try {
	    return parseStream(inStream,molName);
	} catch (Exception e) {
	    System.err.println("Exception while parsing gzip-file.");
	    System.err.println(e.toString());
	    return null;
	}
    }

    // Implementation specific function
    protected abstract
	ArrayList<Molecule> parseStream(InputStream is,
					String molName) throws Exception;

    /**
     * Saves molecules into a file. If selection expression is provided,
     * then it is used to determine which
     * molecules/chains/residues/atoms to save. If not, then
     * selected atoms are saved.
     *
     * @param molsToSave list of molecules to be saved.
     * @param urlFileName name of file on the web to save.
     * @param expr selection expression for atoms.
     *
     * @return number of atoms saved. Negative if error happend.
     */
    public int saveToFile(Molecule molsToSave,URL urlFileName,
			  SelectExpression expr)
    {
	if (molsToSave == null) return -1;

	int ret = -1;
	OutputStream outStream = null;
	try {
 	    URLConnection conn = urlFileName.openConnection();
	    conn.setDoOutput(true);
	    outStream = new BufferedOutputStream(conn.getOutputStream());
	    ret = saveToFile(molsToSave,outStream,urlFileName.getFile(),expr);
	} catch (Exception e) {
	    System.err.println("Exception while writing file to URL:");
	    System.err.println(urlFileName.toString());
	    System.err.println(e.toString());
	    return -1;
	}

	try { outStream.close(); } catch (Exception e) {}
	return ret;
    }

    /**
     * Saves molecules into a file. If selection expression is provide,
     * then it is used to determine which
     * molecules/chains/residues/atoms to save. If not, then
     * selected atoms are saved.
     *
     * @param molsToSave array of molecules to be saved.
     * @param file name of file to save.
     * @param expr selection expression for atoms.
     *
     * @return number of atoms saved. Negative if error happend.
     */
    public int saveToFile(Molecule molsToSave,File file,SelectExpression expr)
    {
	if (molsToSave == null) return -1;

	int ret = -1;
	OutputStream outStream = null;
	try {
	    outStream =	new BufferedOutputStream(new FileOutputStream(file));
	    ret = saveToFile(molsToSave,outStream,file.getName(),expr);
	} catch (Exception e) {
	    System.err.println("Exception while writing file:");
	    System.err.println(file.getName());
	    System.err.println(e.toString());
	} 
	
	try { outStream.close(); } catch (Exception e) {}
	return ret;
    }

    // Dispatch to a proper parsing function
    private int saveToFile(Molecule molsToSave,OutputStream outStream,
			   String name,SelectExpression expr)
    {
	if (FormatResolver.gzippedByExtension(name))
	    return saveToGZippedFile(molsToSave,outStream,expr);
	else
	    return saveToRegularFile(molsToSave,outStream,expr);
    }

    // Wraping gzip stream
    private int saveToGZippedFile(Molecule molsToSave,OutputStream outStream,
				  SelectExpression expr)
    {
	int ret = 0;
	try {
	    outStream = new GZIPOutputStream(outStream);
	    saveToRegularFile(molsToSave,outStream,expr);
	    outStream.close();
	} catch (Exception e) {
	    System.err.println("Exception while writing to a gzip file.");
	    System.err.println(e.toString());
	    return -1;
	}
	return ret;
    }

    // Saving to regular file
    private int saveToRegularFile(Molecule molsToSave,OutputStream outStream,
				  SelectExpression expr)
    {
	int ret = 0;
	try {
	    for (Molecule m = molsToSave;m != null;m = m.next())
		if (expr != null) {
		    if (countSelectedAtoms(m,expr) > 0)
			ret += saveToStream(outStream,m,expr);
		} else if (m.countSelectedAtoms() > 0)
		    ret += saveToStream(outStream,m,null);
	} catch (Exception e) {
	    System.err.println("Exception while writing to a file.");
	    System.err.println(e.toString());
	    return -1;
	}
	return ret;
    }

    private int saveToStream(OutputStream os,Molecule mol,
			     SelectExpression expr) throws Exception
    {
	PrintWriter pwriter = new PrintWriter(os);
	int ret = save(pwriter,mol,expr);
	pwriter.flush();
	return ret;
    }

    // Implementation specific function
    protected abstract int save(PrintWriter pw,Molecule mol,
				SelectExpression expr);

    /**
     * Prints a file to a string buffer. If selection expression is
     * provided, then it is used to determine which atoms to save. If not,
     * then selected atoms are saved.
     *
     * @param molsToSave list of molecules to be printed.
     * @param expr selection expression for atoms.
     *
     * @return string buffer with output.
     */
    public StringBuffer print(Molecule molsToSave,SelectExpression expr)
    {
	if (molsToSave == null) return new StringBuffer("");

	StringWriter ret = new StringWriter();
	PrintWriter  pw  = new PrintWriter(ret);
	for (Molecule m = molsToSave;m != null;m = m.next())
	    if (expr != null) {
		if (countSelectedAtoms(m,expr) > 0)
		    save(pw,m,expr);
	    } else if (m.countSelectedAtoms() > 0)
		save(pw,m,null);
	pw.flush();
	pw.close();
	return ret.getBuffer();
    }

    // Return nubmer of atoms in a molecule that satisfy the selection
    private int countSelectedAtoms(Molecule mol,SelectExpression expr)
    {
	int ret = 0;
	if (!expr.moleculeMaySatisfy(mol)) return ret;
	for (Chain c = mol.chainList();c != null;c = c.next()) {
	    if (!expr.chainMaySatisfy(c)) continue;
	    for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		if (!expr.assemblyMaySatisfy(s)) continue;
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    if (!expr.atomMaySatisfy(a) || 
			!expr.satisfy(mol,c,s,a)) continue;
		    ret++;
		}
	    }
	}
	return ret;
    }

}