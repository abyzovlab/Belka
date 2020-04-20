package belka.mol;

//--- Application imports ---
import belka.chem.*;

/**
 * Implementation of for class {@link Assembly}
 *
 * @author Alexej Abyzov
 */
class Assembly_imp extends Assembly
{
    // Constructor for natural assembly
    public Assembly_imp(Compound comp) { comp_ = comp; }

    // Constructor for gap assembly
    public Assembly_imp() { }

    // Disassembles assembly into null pointers
    public void disassemble()
    {
	for (Atom atom = atomList_;atom != null;atom = atom.next())
	    atom.disassemble();
	atomList_ = lastAtom_ = null;
	if (bonds_ != null)
	    for (int b = 0;b < bonds_.length;b++)
		if (bonds_[b] != null) {
		    bonds_[b].disassemble();
		    bonds_[b] = null;
		}
	chain_    = null;
	comp_     = null;
    }

    // Check if it represents gap.
    public boolean isGap() { return (comp_ == null); }

    // Next assemblys
    private Assembly next_ = null;
    public  Assembly next() { return next_; }

    // Previous assembly
    private Assembly prev_ = null;
    public  Assembly prev() { return prev_; }

    // Adding assembly after
    public boolean insertAfter(Assembly newAssembly)
    {
	// Check is object is good
	if (newAssembly == null) return false;
	if (newAssembly == this) return false;

	// Check if the call comes from insertBefore of inserted object
	if (newAssembly.prev() == this && next_ == null) {
	    next_ = newAssembly;
	    return true;
	}

	// Check if input is good
	if (newAssembly.next() != null) return false;
	if (newAssembly.prev() != null) return false;

	Assembly next = next_;
	next_ = newAssembly;
	if (next != null && next.prev() == this)
	    next.insertBefore(newAssembly);

	return newAssembly.insertBefore(this);
    }

    // Adding assembly before
    public boolean insertBefore(Assembly newAssembly) 
    {
	// Check is object is good
	if (newAssembly == null) return false;
	if (newAssembly == this) return false;

	// Check if the call comes from insertAfter of inserted object
	if (newAssembly.next() == this && prev_ == null) {
	    prev_ = newAssembly;
	    return true;
	}
	
	// Check if input is good
	if (newAssembly.next() != null) return false;
	if (newAssembly.prev() != null) return false;
	
	Assembly prev = prev_;
	prev_ = newAssembly;
	if (prev != null && prev.next() == this)
	    prev.insertAfter(newAssembly);

	return newAssembly.insertAfter(this);
    }

    // Extracting assembly after the current one
    public Assembly extractAfter()
    {
	// Check if next exists
	if (next_ == null) return null;

	// Check if the call comes from extractBefore
	if (next_ != null && next_.prev() != this) {
	    next_ = null;
	    return this;
	}
	
	Assembly ret = next_;
	next_ = next_.next();
	if (next_ != null && next_.prev() == ret) next_.extractBefore();

	ret.extractBefore();
	
	return ret;
    }

    // Extracting assembly before the current one
    public Assembly extractBefore()
    {
	// Check if next exists
	if (prev_ == null) return null;

	// Check if the call comes from extractAfter
	if (prev_ != null && prev_.next() != this) {
	    prev_ = null;
	    return this;
	}

	Assembly ret = prev_;
	prev_ = prev_.prev();
	if (prev_ != null && prev_.next() == ret) prev_.extractAfter();

	ret.extractAfter();
	
	return ret;
    }

    // Access to atoms in the assembly
    private Atom atomList_ = null;
    private Atom lastAtom_ = null;
    public  Atom atomList() { return atomList_; }
    public boolean addAtom(Atom atom)
    {
	if (isGap()) return false;

	// Check if input is correct
	if (atom == null) return false;

	// Check if it can be added
	if (atom.next() != null) return false;
	if (atom.prev() != null) return false;

	// Trying to set assembly for atom
	if (!atom.setAssembly(this)) return false;

	if (atomList_ == null) {
	    lastAtom_ = atomList_ = atom;
	} else {
	    if (!lastAtom_.addAfter(atom)) return false;
	    lastAtom_ = atom;
	}
	return true;
    }
    
    // Peptide/phosphodiester bonds
    private Bond[] bonds_ = null;
    public  int    countBonds()
    {
	if (bonds_ == null) return 0;
	else                return bonds_.length;
    }
    public Bond[]  bondArray() { return bonds_; }
    public boolean addBond(Bond bond)
    {
	if (bond == null) return false;
	if (bonds_ == null) {
	    bonds_ = new Bond[1];
	    bonds_[0] = bond;
	    return true;
	}
	int n_bonds = bonds_.length;
	Bond[] new_bonds = new Bond[n_bonds + 1];
	for (int i = 0;i < n_bonds;i++) {
	    Bond b = bonds_[i];
	    if (b == bond) return false;
	    new_bonds[i] = bonds_[i];
	}
	new_bonds[n_bonds] = bond;
	bonds_ = new_bonds;
	return true;
    }
    public boolean isConnectedTo(Assembly s)
    {
	if (s == null)      return false;
	if (bonds_ == null) return false;
	for (int i = 0;i < bonds_.length;i++) {
	    Bond b = bonds_[i];
	    if (b.getFAtom() != null &&
		b.getFAtom().assembly() == s) return true;
	    if (b.getSAtom() != null &&
		b.getSAtom().assembly() == s) return true;
	}
	return false;
    }

    // Access to chain the assembly belongs to
    private Chain  chain_ = null;
    public  Chain  chain() { return chain_; }
    public boolean setChain(Chain chain)
    {
	if (chain_ != null)
	    for (Assembly s = chain_.assemblyList();s != null;s = s.next())
		if (s == this) return false;
	chain_ = chain;
	return true;
    }

    // Chemical compound
    private Compound comp_ = null;
    public  Compound getCompound() { return comp_; }

    // Residue's name == compound's short name
    public String getName()
    {
	if (comp_ == null) return "";
	return comp_.getShortName();
    }

    // Residue's letter name == compound's letter name
    public char getLetterName()
    {
	if (comp_ == null) return GAP_CHAR;
	return comp_.getLetterName();
    }

    // Serial number
    private int serial_num_ = 0;
    public  int  getSerialNum()           { return serial_num_; }
    public  void setSerialNum(int serial) { serial_num_ = serial; }

    // iCode
    private char icode_ = ' ';
    public  char getICode()           { return icode_; }
    public  void setICode(char icode) { icode_ = icode; }


    // Main atoms
    public Atom getCAAtom()
    {
	for (Atom a = atomList();a != null;a = a.next())
	    if (a.isCA()) return a;
	return null;
    }
    public Atom getMainAtom() { return getCAAtom(); }

    // Counting atoms
    public int countAtoms()
    {
	int ret = 0;
	for (Atom a = atomList();a != null;a = a.next()) ret++;
	return ret;
    }

    // Counting atoms
    public int countSelectedAtoms()
    {
	int ret = 0;
	for (Atom a = atomList();a != null;a = a.next())
	    if (a.isSelected()) ret++;
	return ret;
    }

    // Selecting all atoms
    public void selectAllAtoms(boolean val)
    {
	for (Atom a = atomList();a != null;a = a.next())
	    a.setSelected(val);
    }

    // Attributes. First 8 bits are used for attributes the rest for group_id.
    private int att_ = 0;
    private static final int ALIGNED_ATTRIBUTE = 0x01; // Aligned residues
    private static final int HELIX3_ATTRIBUTE  = 0x02; // 3,10-helix
    private static final int HELIX4_ATTRIBUTE  = 0x04; // 4-helix
    private static final int HELIX5_ATTRIBUTE  = 0x08; // 5-helix
    private static final int SHEET_ATTRIBUTE   = 0x10; // Sheet
    private static final int TURN_ATTRIBUTE    = 0x20; // Turn
    private static final int SOME7_ATTRIBUTE   = 0x40; 
    private static final int SOME8_ATTRIBUTE   = 0x80;

    private static final int HELIX_ATTRIBUTE   = // Any helix
	HELIX3_ATTRIBUTE | HELIX4_ATTRIBUTE | HELIX5_ATTRIBUTE;
    private static final int REGULAR_ATTRIBUTE = // Any secondary structure
	HELIX_ATTRIBUTE | TURN_ATTRIBUTE | SHEET_ATTRIBUTE;

    private static final int SINGLE_ATTRIBUTES = 0xFF;
    private static final int GROUP_ATTRIBUTES  = 0xFFFFFF00;

    // Getters for attributers
    public static int getAlignedAttribute() { return ALIGNED_ATTRIBUTE; }
    public static int getHelixAttribute()   { return HELIX_ATTRIBUTE;   }
    public static int getHelix3Attribute()  { return HELIX3_ATTRIBUTE;  }
    public static int getHelix4Attribute()  { return HELIX4_ATTRIBUTE;  }
    public static int getHelix5Attribute()  { return HELIX5_ATTRIBUTE;  }
    public static int getSheetAttribute()   { return SHEET_ATTRIBUTE;   }
    public static int getTurnAttribute()    { return TURN_ATTRIBUTE;    }
    public static int getRegularAttribute() { return REGULAR_ATTRIBUTE; }
    public static int getGroupAttribute(int group_id)
    {
	int ret = group_id<<8;
	if ((ret>>8) != group_id) return 0; // Too large
	return ret;
    }

    // Alignment attributes
    public boolean isAligned() { return ((att_ & ALIGNED_ATTRIBUTE) != 0); }
    public void    setAligned(boolean val)
    {
	if (val) att_ |=  ALIGNED_ATTRIBUTE;
	else     att_ &= ~ALIGNED_ATTRIBUTE;
    }

    // Group id
    public  int     getGroupId() { return (att_>>8); }
    public  boolean setGroupId(int group_id)
    {
	int tmp = group_id<<8;
	if ((tmp>>8) != group_id) return false; // Too large
	att_ &= ~GROUP_ATTRIBUTES;
	att_ |= tmp;
	return true;
    }

    // Attributes 
    public boolean hasAttribute(int att)
    {
	int val_in = (att & SINGLE_ATTRIBUTES);
	if (val_in != 0)
	    return ((val_in & (att_ & SINGLE_ATTRIBUTES)) != 0);

 	if ((att  & GROUP_ATTRIBUTES) ==
 	    (att_ & GROUP_ATTRIBUTES)) return true;
 	return false;
    }

    // String representation
    public String toString()
    {
	Chain c      = chain_;
	Molecule mol = null;
	if (c != null) mol = c.molecule();
	StringBuffer ret = new StringBuffer();
	if (mol != null) {
	    ret.append("@[");
	    ret.append(mol.getName());
	    ret.append("].");
	}
	ret.append(getName());
	ret.append(getSerialNum());
	if (c != null) 
	    ret.append(c.getId());
	
	return ret.toString();
    }

}
