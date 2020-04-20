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
import belka.chem.*;

/**
 * Object of this class parses FASTA-file either from file or from URL and 
 * creates a molecule. Molecule name is read from the header line.
 *
 * @author Alexej Abyzov
 */
public class FASTAParser extends Parser
{
    // Parsing stream
    protected ArrayList<Molecule> parseStream(InputStream is,
					      String molName) throws Exception
    {
	if (is == null) return null;
	ArrayList<Molecule> ret = new ArrayList<Molecule>();

	BufferedReader in = new BufferedReader(new InputStreamReader(is));
	String line = null;
	while ((line = in.readLine()) != null) { 
	    if (line.startsWith(">")) break;
	    if (line.length() > 0) {
		System.err.println("Skipping line:");
		System.err.println(line);
		continue;
	    }
	}
		
	while (line != null && line.startsWith(">")) {
	    Molecule mol = Molecule.create(line.substring(1));
	    String seq = "";
	    while ((line = in.readLine()) != null) { 
		if (line.startsWith(">")) break;
		seq += line.trim();
	    }
	    if (seq.endsWith("*")) seq = seq.substring(0,seq.length() - 1);
	    Chain c = Chain.create('_');
	    mol.addChain(c);
	    boolean isProtein = (getACGTUCounts(seq) < seq.length()>>1);
	    for (int i = 0;i < seq.length();i++) {
		char letter = seq.charAt(i);
		if (Assembly.isGap(letter)) {
		    c.addAssembly(Assembly.createGap());
		    continue;
		}
		Compound comp = null;
		if (isProtein) comp = Compound.getAminoAcid(letter);
		else           comp = Compound.getNucleicAcid(letter);
		if (comp == null) comp = new Compound("seq");
		Assembly a = Assembly.create(comp);
		a.setSerialNum(i + 1);
		c.addAssembly(a);
	    }
	    ret.add(mol);
	}

	return ret;
    }

    protected int save(PrintWriter pw,Molecule mol,SelectExpression expr)
    {
	return 0;
    }

    private int getACGTUCounts(String seq)
    {
	String s = seq.toLowerCase();
	int ret = 0;
	for (int i = 0;i < s.length();i++) {
	    char c = seq.charAt(i);
	    if (c == 'a' || c == 'c' || c == 't' || c == 'u' || c == 'g')
		ret++;
	}
	return ret;
    }
}