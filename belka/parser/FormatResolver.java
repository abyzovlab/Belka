package belka.parser;

/**
 * The class represents set of functions aimed to resolve format of file by
 * extension.
 *
 * @author Alexej Abyzov
 */ 
public class FormatResolver
{
    /**
     * Constant to define PDB-file format
     */ 
    public final static int FORMAT_UNKNOWN = -1;
    public final static int FORMAT_PDB     =  0;
    public final static int FORMAT_PROJECT =  1;
    public final static int FORMAT_GROUP   =  2;
    public final static int FORMAT_FASTA   =  3;

    /**
     * Standard extensions for file in PDB format
     */ 
    public final static String FORMAT_PDB_EXTENSIONS[] = { ".pdb",
							   ".ent",
							   ".noc"};

    /**
     * Standard extensions for annotation/group file format
     */ 
    public final static String FORMAT_FASTA_EXTENSIONS[] = { ".fa",
							     ".fasta"};

    /**
     * Standard extensions for project file format
     */ 
    public final static String FORMAT_PROJECT_EXTENSIONS[] = { ".bel"};

    /**
     * Standard extensions for annotation/group file format
     */ 
    public final static String FORMAT_GROUP_EXTENSIONS[] = { ".gr"};

    /**
     * Standard extensions for gzipped files.
     */ 
    public final static String FORMAT_GZIP_EXTENSIONS[] = { ".gz",
							    ".gzip"};

    /**
     * Standard extensions for gzipped files.
     */ 
    public final static String FORMAT_ZIP_EXTENSIONS[] = { ".zip"};

    /**
     * The function resolves file format by its extension.
     *
     * @param fileName name of the file.
     *
     * @return format index, or negative if format can no be resolved.
     */
    public static int resolveFileFormatByExtension(String fileName)
    {
	if (fileName == null) return FORMAT_UNKNOWN;

	String fn = fileName.toLowerCase().trim();

	if (gzippedByExtension(fn))     fn = stripGZIPExtension(fn);
	else if (zippedByExtension(fn)) fn = stripZIPExtension(fn);

	if (matchExtension(fn,FORMAT_PDB_EXTENSIONS))
	    return FORMAT_PDB;
	if (matchExtension(fn,FORMAT_FASTA_EXTENSIONS))
	    return FORMAT_FASTA;
	if (matchExtension(fn,FORMAT_PROJECT_EXTENSIONS))
	    return FORMAT_PROJECT;
	if (matchExtension(fn,FORMAT_GROUP_EXTENSIONS))
	    return FORMAT_GROUP;

	return FORMAT_UNKNOWN;
    }
    
    /**
     * The function tells from file extension whether it seems to be gzipped.
     *
     * @param fileName name of file.
     *
     * @return true or false.
     */
    public static boolean gzippedByExtension(String fileName)
    {
	if (fileName == null) return false;
	String fn = fileName.toLowerCase().trim();
	return matchExtension(fn,FORMAT_GZIP_EXTENSIONS);
    }

    /**
     * The function tells from file extension whether it seems to be zipped.
     *
     * @param fileName name of file.
     *
     * @return true or false.
     */
    public static boolean zippedByExtension(String fileName)
    {
	if (fileName == null) return false;
	String fn = fileName.toLowerCase().trim();
	return matchExtension(fn,FORMAT_ZIP_EXTENSIONS);
    }

    /**
     * Removes extension assisiated with gzip files.
     *
     * @param fileName input file name.
     *
     * @return file name after string extension. File name is the same if no
     * gzip extension is found.
     */
    public static String stripGZIPExtension(String fileName)
    {
	if (fileName == null) return fileName;
	String fn = fileName.toLowerCase().trim();
	for (int i = 0;i < FORMAT_GZIP_EXTENSIONS.length;i++)
	    if (fn.endsWith(FORMAT_GZIP_EXTENSIONS[i]))
		return fileName.substring(0,fileName.length() -
					  FORMAT_GZIP_EXTENSIONS[i].length());
	return fileName;
    }

    /**
     * Removes extension assisiated with zip files.
     *
     * @param fileName input file name.
     *
     * @return file name after string extension. File name is the same if no
     * zip extension is found.
     */
    public static String stripZIPExtension(String fileName)
    {
	if (fileName == null) return fileName;
	String fn = fileName.toLowerCase().trim();
	for (int i = 0;i < FORMAT_ZIP_EXTENSIONS.length;i++)
	    if (fn.endsWith(FORMAT_ZIP_EXTENSIONS[i]))
		return fileName.substring(0,fileName.length() - 1 -
					  FORMAT_ZIP_EXTENSIONS[i].length());
	return fileName;
    }

    private static boolean matchExtension(String fileName,String[] exs)
    {
	for (int i = 0;i < exs.length;i++)
	    if (fileName.endsWith(exs[i])) return true;
	return false;
    }
}