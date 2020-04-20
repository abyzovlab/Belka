package belka.mol;

/**
 * Implementation of for class {@link Molecule}
 *
 * @author Alexej Abyzov
 */
class Molecule_imp extends Molecule
{
    // Constructor
    public Molecule_imp(String molName,String pdbCode)
    {
	molName_ = molName;
	if (molName_ == null) molName_ = "";

	pdbCode_ = pdbCode;
	if (pdbCode_ == null) pdbCode_ = "    ";
	while (pdbCode_.length() < 4) pdbCode_ += ' ';
	if (pdbCode_.length() > 4) pdbCode_ = pdbCode_.substring(0,4);
    }

    // Disassembles molecule into null pointers
    public void disassemble()
    {
	for (Chain chain = chainList_;chain != null;chain = chain.next())
	    chain.disassemble();
	chainList_ = lastChain_ = null;
    }

    // Next molecule
    private Molecule next_ = null;
    public  Molecule next() { return next_; }

    // Previous molecule
    private Molecule prev_ = null;
    public  Molecule prev() { return prev_; }

    // Adding molecule before
    public boolean addBefore(Molecule newMol) 
    {
	// Check if input is good
	if (newMol == null) return false;

	// Check if same object
	if (newMol == this) return false;

	// Check if the call comes from addAfter
	if (newMol.next() == this && prev_ == null) {
	    prev_ = newMol;
	    return true;
	}
	
	// Chack if they are already paired
	if (newMol.prev() == this && next_ == newMol) return true;
	
	// Check if it can be added
	if (prev_ != null) return false;
	if (newMol.next() != null) return false;
	if (newMol.prev() != null) return false;

	prev_ = newMol; // Set previous 
	if (!newMol.addAfter(this)) { // Update next for newMol
	    prev_ = null;
	    return false;
	}
	return true;
    }

    // Adding molecule after
    public boolean addAfter(Molecule newMol)
    {
	// Check if not null
	if (newMol == null) return false;

	// Check if same object
	if (newMol == this) return true;

	// Check if the call comes from addAfter
	if (newMol.prev() == this && next_ == null) {
	    next_ = newMol;
	    return true;
	}
	
	// Chack if they are already paired
	if (newMol.prev() == this && next_ == newMol) return true;
	
	// Check if it can be added
	if (next_ != null) return false;
	if (newMol.next() != null) return false;
	if (newMol.prev() != null) return false;

	next_ = newMol; // Set next
	if (!newMol.addBefore(this)) { // Update previous for newMol
	    next_ = null;
	    return false;
	}
	return true;
    }
    
    // Extracting molecule after the current one
    public Molecule extractAfter()
    {
	// Check if next exists
	if (next_ == null) return null;

	// Check if the call comes from extractBefore
	if (next_ != null && next_.prev() != this) {
	    next_ = null;
	    return this;
	}
	
	Molecule ret = next_;
	next_ = next_.next();
	if (next_ != null && next_.prev() == ret) next_.extractBefore();

	ret.extractBefore();
	
	return ret;
    }

    // Extracting molecule before the current one
    public Molecule extractBefore()
    {
	// Check if next exists
	if (prev_ == null) return null;

	// Check if the call comes from extractAfter
	if (prev_ != null && prev_.next() != this) {
	    prev_ = null;
	    return this;
	}

	Molecule ret = prev_;
	prev_ = prev_.prev();
	if (prev_ != null && prev_.next() == ret) prev_.extractAfter();

	ret.extractAfter();
	
	return ret;
    }

    // Access to chains in a molecule
    private Chain chainList_ = null;
    private Chain lastChain_ = null;
    public  Chain chainList() { return chainList_; }
    public boolean addChain(Chain chain)
    {
	// Check if input is correct
	if (chain == null) return false;

	// Check if it can be added
	if (chain.next() != null) return false;
	if (chain.prev() != null) return false;

	// Trying to set molecule for chain
	if (!chain.setMolecule(this)) return false;

	if (chainList_ == null) {
	    lastChain_ = chainList_ = chain;
	} else {
	    if (!lastChain_.addAfter(chain)) return false;
	    lastChain_ = chain;
	}
	return true;
    }

    // Name
    String molName_ = null;
    public String getName() { return molName_; }

    // Name
    String pdbCode_ = null;
    public String getPDBCode() { return pdbCode_; }

    // Counting chain, assemblys and atoms
    public int countChains()
    {
	int ret = 0;
	for (Chain c = chainList();c != null;c = c.next()) ret++;
	return ret;
    }
    public int countAssemblies()
    {
	int ret = 0;
	for (Chain c = chainList();c != null;c = c.next())
	    ret += c.countAssemblies();
	return ret;
    }

    public int countModels()
    {
	int maxModel = -1;
	for (Chain c = chainList();c != null;c = c.next()) {
	    int m = c.getModel();
	    if (m > maxModel) maxModel = m;
	}
	if (maxModel < 0) return 0;

	boolean[] modelFlag = new boolean[maxModel + 1];
	for (int i = 0;i <= maxModel;i++) modelFlag[i] = false;
	for (Chain c = chainList();c != null;c = c.next()) {
	    int m = c.getModel();
	    if (m >= 0) modelFlag[m] = true;
	}

	int ret = 0;
	for (int i = 0;i <= maxModel;i++)
	    if (modelFlag[i]) ret++;
	
	return ret;
    }

    public int countAtoms()
    {
	int ret = 0;
	for (Chain c = chainList();c != null;c = c.next())
	    ret += c.countAtoms();
	return ret;
    }

    public int countSelectedAtoms()
    {
	int ret = 0;
	for (Chain c = chainList();c != null;c = c.next())
	    ret += c.countSelectedAtoms();
	return ret;
    }

    // Selecting all atoms
    public void selectAllAtoms(boolean val)
    {
	for (Chain c = chainList();c != null;c = c.next())
	    c.selectAllAtoms(val);
    }

    // Transformation of molecule. This is not related to screen rotation
    public boolean rotate(double [][] rot,double[] trans)
    {
	return rotate(rot,trans,null);
    }

    // Transformation of molecule. This is not related to screen rotation
    public boolean rotate(double [][] rot,double[] trans,double[] center)
    {
	double x_center = 0, y_center = 0,z_center = 0;
	if (center != null) {
	    x_center = center[0];
	    y_center = center[1];
	    z_center = center[2];
	}
	if (rot.length < 3 ||
	    rot[0].length < 3 ||
	    rot[1].length < 3 ||
	    rot[2].length < 3 ||
	    trans.length < 3) return false;

	for (Chain c = chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    double x = a.getX();
		    double y = a.getY();
		    double z = a.getZ();
		    a.setDerivedX(x*rot[0][0] + y*rot[0][1] + z*rot[0][2] +
				  trans[0] - x_center);
		    a.setDerivedY(x*rot[1][0] + y*rot[1][1] + z*rot[1][2] +
				  trans[1] - y_center);
		    a.setDerivedZ(x*rot[2][0] + y*rot[2][1] + z*rot[2][2] +
				  trans[2] - z_center);
		}
	return true;
    }

    // Methods of Transformable class
    public void transform()
    {
	for (Chain c = chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    double x = a.getDerivedX();
		    double y = a.getDerivedY();
		    double z = a.getDerivedZ();
		    a.setScreenX((x*rot_[0][0] +
				  y*rot_[0][1] +
				  z*rot_[0][2])*scale_ + trans_[0]);
		    a.setScreenY((x*rot_[1][0] +
				  y*rot_[1][1] +
				  z*rot_[1][2])*scale_ + trans_[1]);
		    a.setScreenZ((x*rot_[2][0] +
				  y*rot_[2][1] +
				  z*rot_[2][2])*scale_ + trans_[2]);
		}
    }

    public void transformXY()
    {
	for (Chain c = chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    double x = a.getDerivedX();
		    double y = a.getDerivedY();
		    double z = a.getDerivedZ();
		    a.setScreenX((x*rot_[0][0] +
				  y*rot_[0][1] +
				  z*rot_[0][2])*scale_ + trans_[0]);
		    a.setScreenY((x*rot_[1][0] +
				  y*rot_[1][1] +
				  z*rot_[1][2])*scale_ + trans_[1]);
		}
    }

    public void transformYZ()
    {
	for (Chain c = chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    double x = a.getDerivedX();
		    double y = a.getDerivedY();
		    double z = a.getDerivedZ();
		    a.setScreenY((x*rot_[1][0] +
				  y*rot_[1][1] +
				  z*rot_[1][2])*scale_ + trans_[1]);
		    a.setScreenZ((x*rot_[2][0] +
				  y*rot_[2][1] +
				  z*rot_[2][2])*scale_ + trans_[2]);
		}
    }

    public void transformZX()
    {
	for (Chain c = chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    double x = a.getDerivedX();
		    double y = a.getDerivedY();
		    double z = a.getDerivedZ();
		    a.setScreenX((x*rot_[0][0] +
				  y*rot_[0][1] +
				  z*rot_[0][2])*scale_ + trans_[0]);
		    a.setScreenZ((x*rot_[2][0] +
				  y*rot_[2][1] +
				  z*rot_[2][2])*scale_ + trans_[2]);
		}
    }

    public void transformSize()
    {
	// Setting for atoms and covalent bonds
	for (Chain c = chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    // Setting for atoms 
		    double inRadius  = a.getRadius();
		    double outRadius = scale_*inRadius;
		    if (inRadius < 0) outRadius = -1;
		    a.setScreenRadius(outRadius);
		    // Setting for bonds
		    Bond[] bonds = a.bondArray();
		    if (bonds == null) continue;
		    for (int b = 0;b < bonds.length;b++) {
			Bond bond = bonds[b];
			if (bond == null) continue;
			inRadius  = bond.getRadius();
			outRadius = scale_*inRadius;
			if (inRadius < 0) outRadius = -1;
			bond.setScreenRadius(outRadius);
		    }
		}
		// Setting for backbones
		Bond[] bonds = s.bondArray();
		if (bonds == null) continue;
		for (int b = 0;b < bonds.length;b++) {
		    Bond bond = bonds[b];
		    if (bond == null) continue;
		    double inRadius  = bond.getRadius();
		    double outRadius = scale_*inRadius;
		    if (inRadius < 0) outRadius = -1;
		    bond.setScreenRadius(outRadius);
		}
	    }
    }
}
