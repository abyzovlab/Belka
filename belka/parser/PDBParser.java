package belka.parser;

//--- Java imports ---
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

//--- Application imports ---
import belka.*;
import belka.mol.*;
import belka.chem.*;

/**
 * Object of this class parses PDB-file either from file or from URL and 
 * creates a molecule. Filename is used as a name for new molecule.
 *
 * @author Alexej Abyzov
 */
public class PDBParser extends Parser
{
    // Default name
    private String MOLECULE_NAME_UNKNOWN = "Unknown";

    // File separator
    private String SYSTEM_FILE_SEPARATOR =
	System.getProperty("file.separator");
    private String URL_FILE_SEPARATOR =	"/";

    // Variable to keep track of current objects
    private Molecule current_mol   = null;
    private Chain    current_chain = null;
    private Assembly current_ass   = null;
    private Atom     current_atom  = null;
    private Bond[]   current_bonds = null;
    private int      current_model =   -1;

    protected int save(PrintWriter pw,Molecule mol,SelectExpression expr)
    {
	int ret = 0;
	
	StringBuffer hline = new StringBuffer("HEADER");
	while (hline.length() < 62) hline.append(' ');
	hline.append(mol.getPDBCode());
	pw.println(hline.toString());

	for (Chain c = mol.chainList();c != null;c = c.next()) {
	    int n_saved = 0;
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    if (expr != null && !expr.satisfy(mol,c,s,a)) continue;
		    if (!a.isSelected()) continue;
		    n_saved++;
		    pw.print("ATOM  ");
		    pw.format("%5d",a.getSerialNum());
		    pw.print(" ");
		    String name = a.getName();
		    if (name.length() < 4) name = " " + name;
		    pw.print(name);
		    for (int j = name.length();j < 4;j++)
			pw.print(" ");
		    pw.print(a.getAlternative());
		    pw.print(s.getName().toUpperCase());
		    pw.print(" ");
		    pw.print(c.getId());
		    pw.format("%4d",s.getSerialNum());
		    pw.print(s.getICode());
		    pw.print("   ");
		    pw.format("%8.3f",a.getDerivedX());
		    pw.format("%8.3f",a.getDerivedY());
		    pw.format("%8.3f",a.getDerivedZ());
		    pw.format("%6.2f",a.getOccupancy());
		    pw.format("%6.2f",a.getTemperature());
		    pw.print("          ");
		    if (a.getElement() != null) {
			name = a.getElement().getSign();
			for (int j = name.length();j < 2;j++)
			    pw.print(" ");
			pw.print(name);
		    }
		    pw.println();
		    ret++;
		}
	    if (n_saved > 0) pw.println("TER");
	}
	pw.println("END");
	return ret;
    }
    
    // Parsing stream
    protected ArrayList<Molecule> parseStream(InputStream is,
					      String molName) throws Exception
    {
	if (is == null) return null;
	if (molName == null) molName = MOLECULE_NAME_UNKNOWN;

	ArrayList<Molecule> ret = new ArrayList<Molecule>();

	current_mol   = Molecule.create(molName);
 	current_chain = null;
 	current_ass   = null;
	current_atom  = null;
	current_bonds = null;

	int n_atoms_added  = 0, n_a = 0;
	int n_chains_added = 0;
	int n_res_added    = 0;
	BufferedReader in = new BufferedReader(new InputStreamReader(is));
	String line = null;
	while ((line = in.readLine()) != null) {
	    char firstChar = line.charAt(0);
	    if (firstChar == 'A') {
		if (line.startsWith("ATOM"))
		    current_atom = parseAtomDescription(line);
	    } else if (firstChar == 'C') {
		if (line.startsWith("CONECT"))
		    current_bonds = parseConnectDescription(line);
	    } else if (firstChar == 'E') {
		if (line.equals("END")) {
		    if (n_a > 0) {
			ret.add(current_mol);
			n_a = 0;
		    }
		    current_mol = Molecule.create(molName);
		    current_chain = null;
		    current_ass   = null;
		    current_atom  = null;
		    current_bonds = null;
		} else if (line.startsWith("ENDMDL"))
		    current_chain = null;
	    } else if (firstChar == 'H') {
		if (line.startsWith("HETATM"))
		    current_atom = parseAtomDescription(line);
		else if (line.startsWith("HEADER")) {
		    if (n_a > 0) {
			ret.add(current_mol);
			n_a = 0;
		    }
		    if (line.length() >= 66) {
			String pdbCode = line.substring(62,66);
			current_mol    = Molecule.create(molName,pdbCode);
		    } else {
			current_mol    = Molecule.create(molName);
		    }
		    current_chain = null;
		    current_ass   = null;
		    current_atom  = null;
		    current_bonds = null;
		}
	    } else if (firstChar == 'M') {
		if (line.startsWith("MODEL"))
		    current_model = parseModelDescription(line);
	    } else if (firstChar == 'R') {
		    
	    } else if (firstChar == 'T') {
		if (line.startsWith("TER"))
		    current_chain = null;
	    }

	    if (current_atom != null) {
		if (needToParseChain(line)) {
		    current_chain = parseChainDescription(line);
		    current_mol.addChain(current_chain);
		    n_chains_added++;
		    current_ass = parseAssemblyDescription(line);
		    current_chain.addAssembly(current_ass);
		    n_res_added++;
		} else if (needToParseAssembly(line)) {
		    current_ass = parseAssemblyDescription(line);
		    current_chain.addAssembly(current_ass);
		    n_res_added++;
		}
		current_ass.addAtom(current_atom);
		current_atom = null;
		n_atoms_added++;
		n_a++;
	    }
	    if (current_bonds != null)
		for (int i = 0;i < 4;i++) { // Covalent bonds
		    Bond bond = current_bonds[i];
		    if (bond == null) continue;
		    Atom a1 = bond.getFAtom();
		    if (a1 != null) a1.addBond(bond);
		    Atom a2 = bond.getSAtom();
		    if (a2 != null) a2.addBond(bond);
		}
	}

	if (n_a > 0) {
	    ret.add(current_mol);
	    n_a = 0;
	}

	return ret;
    }
    
    /**
     * Parses description of an atom, creates a new atom and returns it.
     */
    private Atom parseAtomDescription(String line)
    {
	Atom ret = null;

	Element elem = null;
	try {
	    String elementSign = line.substring(76,78).trim();
	    elem = Element.getElementBySign(elementSign);
	} catch (Exception e) {}

	try {
	    String atom_name = line.substring(12,16).trim();
	    double x = Double.parseDouble(line.substring(30,38).trim());
	    double y = Double.parseDouble(line.substring(38,46).trim());
	    double z = Double.parseDouble(line.substring(46,54).trim());
	    if (elem == null) {
		char firstChar = atom_name.charAt(0);
		if (Character.isDigit(firstChar))
		    firstChar = atom_name.charAt(1);
		String elementSign = "";
		elementSign += firstChar;
		elem = Element.getElementBySign(elementSign);
	    }
	    ret = Atom.create(atom_name,x,y,z,elem);
	} catch(Exception e) {
	    System.err.println("Error in parsing line:");
	    System.err.println(line);
	    return null;
	}

	// Parsing and setting atom's serial number.
	try {
	    int serial = Integer.parseInt(line.substring( 6,11).trim());
	    ret.setSerialNum(serial);
	} catch (Exception e) {}

	char altern = line.charAt(16);
	if (!Character.isSpaceChar(altern))
	    ret.setAlternative(altern);

	// Parsing and setting atom's serial number.
	try {
	    double occup = Double.parseDouble(line.substring(54,60).trim());
	    ret.setOccupancy(occup);
	} catch (Exception e) {}

	try {
	    double temper = Double.parseDouble(line.substring(60,66).trim());
	    ret.setTemperature(temper);
	} catch (Exception e) {}

	return ret;
    }

    /**
     * Parses description of a assembly, creates a new chain and returns it.
     */
    private Assembly parseAssemblyDescription(String line)
    {
	String compoundName = line.substring(17,20).trim();
	Compound comp = Compound.getCompoundByShortName(compoundName);
	if (comp == null) comp = new Compound(compoundName);
	    
	Assembly ret = Assembly.create(comp);
	
	try {
	    int pdb_num = Integer.parseInt(line.substring(22,26).trim());
	    ret.setSerialNum(pdb_num);
	} catch (Exception e) { return null; }

	ret.setICode(line.charAt(26));
	
	return ret;
    }

    /**
     * Parses description of a chain, creates a new chain and returns it.
     */
    private Chain parseChainDescription(String line)
    {
	char newChainId = line.charAt(21);
	if (Character.isSpaceChar(newChainId)) newChainId = '_';

	Chain ret = Chain.create(newChainId);

	if (current_model >= 0) ret.setModel(current_model);

	return ret;
    }

    /**
     * Parses model number and returns it.
     */
    private int parseModelDescription(String line)
    {
	int ret = -1;
	try {
	    ret = Integer.parseInt(line.substring(10,15).trim());
	} catch (Exception e) {}
	return ret;
    }

    // Atom where the last bond was made
    private Molecule m_last = null;
    private Chain    c_last = null;
    private Assembly s_last = null;
    private Atom     a_last = null;
    /**
     * Parses conect record, creates described bonds and returns the in array.
     * Index of bonds in the array correspond to the index of described bonds
     * in PDB-file: #1-4 are convaent bonds, #5-6 and 8-9 are hydrogen bonds,
     * and #7 and 10 are salt bridges. The function creates bonds only if
     * serial number of the bonded atom is larger than the original.
     * For example, bond between 402  405 is created while bond between 405 402
     * is not created. Because PDB-file contatins two description of same bond
     * (see abouve), such trick saves time on parsing.
     */
    private Bond[] parseConnectDescription(String line)
    {
	if (line == null)        return null;
	if (current_mol == null) return null;

	if (current_mol != m_last) {
	    m_last = null;
	    c_last = null;
	    s_last = null;
	    a_last = null;
	}

	// Place to start search for second atom
	Molecule m_start = null;
	Chain    c_start = null;
	Assembly s_start = null;
	Atom     a_start = null;

	int  ser1 = 0,  ser2 = 0;
	Atom a1 = null, a2 = null;

	try { ser1 = Integer.parseInt(line.substring(6,11).trim()); }
	catch (Exception e) { return null; }

	// Finding first atom
	Molecule m_tmp = m_last;
	Chain    c_tmp = c_last;
	Assembly s_tmp = s_last;
	Atom     a_tmp = a_last;
	Molecule m = current_mol;
	if (m_tmp != null) { m = m_tmp; m_tmp = null; }
	Chain c = m.chainList();
	if (c_tmp != null) { c = c_tmp; c_tmp = null; }
	for (;c != null;c = c.next()) {
	    Assembly s = c.assemblyList();
	    if (s_tmp != null) { s = s_tmp; s_tmp = null; }
	    for (;s != null;s = s.next()) {
		Atom a = s.atomList();
		if (a_tmp != null) { a = a_tmp; a_tmp = null; }
		for (;a != null;a = a.next())
		    if (a.getSerialNum() == ser1) {
			a1 = a;
			m_start = m_last = m;
			c_start = c_last = c;
			s_start = s_last = s;
			a_start = a_last = a;
			break;
		    }
		if (a1 != null) break;
	    }
	    if (a1 != null) break;
	}

	if (a1 == null) return null;

	Bond[] ret = new Bond[10];

	int width = 5;
	for (int i = 0, start = 11;i < 10;i++, start += 5 ) {

	    String new_number = line.substring(start,start + width).trim();

	    try { ser2 = Integer.parseInt(new_number); }
	    catch (Exception e) { continue; }

	    if (ser2 < ser1) continue; // Bond was already created.

	    // Finding second atom
	    a2 = null;
	    m_tmp = m_start;
	    c_tmp = c_start;
	    s_tmp = s_start;
	    a_tmp = a_start;
	    m = current_mol;
	    if (m_tmp != null) { m = m_tmp; m_tmp = null; }
	    c = m.chainList();
	    if (c_tmp != null) { c = c_tmp; c_tmp = null; }
	    for (;c != null;c = c.next()) {
		Assembly s = c.assemblyList();
		if (s_tmp != null) { s = s_tmp; s_tmp = null; }
		for (;s != null;s = s.next()) {
		    Atom a = s.atomList();
		    if (a_tmp != null) { a = a_tmp; a_tmp = null; }
		    for (;a != null;a = a.next())
			if (a.getSerialNum() == ser2) {
			    a2 = a;
			    m_start = m;
			    c_start = c;
			    s_start = s;
			    a_start = a;
			    break;
			}
		    if (a2 != null) break;
		}
		if (a2 != null) break;
	    }

	    Bond new_bond = null;
	    if (a2 != null) new_bond = Bond.create(a1,a2);
	
	    ret[i] = new_bond;
	}

	return ret;
    }
    
    /**
     * Returns 'true' if chain description must be parsed. The reason can be 
     * that line has new chain description or there is no current chain
     * used.
     */
    private boolean needToParseChain(String line)
    {
	char newChainId = line.charAt(21);
	if (Character.isSpaceChar(newChainId))   newChainId = '_';
	if (current_chain == null)               return true;
	if (current_chain.getId() != newChainId) return true;
	return false;
    }

    /**
     * Returns 'true' if assembly description must be parsed. The reason can
     * be that line has new assembly description or there is no current
     * assembly used.
     */
    private boolean needToParseAssembly(String line)
    {
	if (current_ass == null) return true;
	String new_name = line.substring(17,20).trim();
	if (!new_name.equalsIgnoreCase(current_ass.getName())) return true;
 	if (line.charAt(26) != current_ass.getICode()) return true;
	int pdb_num = 0;
	try {
	    pdb_num = Integer.parseInt(line.substring(22,26).trim());
	} catch (Exception e) { return true; }
	if (pdb_num != current_ass.getSerialNum()) return true;
	return false;
    }
}
