package belka.mol;

/**
 * Implementation of for class {@link Chain}
 *
 * @author Alexej Abyzov
 */
class Chain_imp extends Chain
{
    // Constractors
    public Chain_imp(char id) { id_ = id; }

    // Disassembles chain into null pointers
    public void disassemble()
    {
	for (Assembly ass = assemblyList_;ass != null;ass = ass.next())
	    ass.disassemble();
	mol_ = null;
	assemblyList_ = lastAssembly_ = null;
    }

    // Next chain
    private Chain next_ = null;
    public  Chain next() { return next_; }

    // Previous chain
    private Chain prev_ = null;
    public  Chain prev() { return prev_; }

    // Adding chain before
    public boolean addBefore(Chain newChain) 
    {
	// Check if input is good
	if (newChain == null) return false;

	// Check if same object
	if (newChain == this) return false;

	// Check if the call comes from addAfter
	if (newChain.next() == this && prev_ == null) {
	    prev_ = newChain;
	    return true;
	}
	
	// Chack if they are already paired
	if (newChain.prev() == this && next_ == newChain) return true;
	
	// Check if it can be added
	if (prev_ != null) return false;
	if (newChain.next() != null) return false;
	if (newChain.prev() != null) return false;

	prev_ = newChain; // Set previous 
	if (!newChain.addAfter(this)) { // Update next for newChain
	    prev_ = null;
	    return false;
	}
	return true;
    }

    // Adding chain after
    public boolean addAfter(Chain newChain)
    {
	// Check if not null
	if (newChain == null) return false;

	// Check if same object
	if (newChain == this) return true;

	// Check if the call comes from addAfter
	if (newChain.prev() == this && next_ == null) {
	    next_ = newChain;
	    return true;
	}
	
	// Chack if they are already paired
	if (newChain.prev() == this && next_ == newChain) return true;
	
	// Check if it can be added
	if (next_ != null) return false;
	if (newChain.next() != null) return false;
	if (newChain.prev() != null) return false;

	next_ = newChain; // Set next
	if (!newChain.addBefore(this)) { // Update previous for newChain
	    next_ = null;
	    return false;
	}
	return true;
    }

    // Access to assemblies in the chain
    private Assembly assemblyList_ = null;
    private Assembly lastAssembly_ = null;
    public  Assembly assemblyList() { return assemblyList_; }
    public boolean addAssembly(Assembly assembly)
    {
	// Check if input is correct
	if (assembly == null) return false;

	// Check if it can be added
	if (assembly.next() != null) return false;
	if (assembly.prev() != null) return false;

	// Trying to set chain for assembly
	if (!assembly.setChain(this)) return false;

	if (assemblyList_ == null) {
	    lastAssembly_ = assemblyList_ = assembly;
	} else {
	    if (!lastAssembly_.insertAfter(assembly)) return false;
	    lastAssembly_ = assembly;
	}
	return true;
    }
    
    // Updating pointers
    public void updateAssemblyPointers()
    {
	lastAssembly_ = assemblyList_;
	if (assemblyList_ == null) return;

	while (assemblyList_.prev() != null)
	    assemblyList_ = assemblyList_.prev();

	while (lastAssembly_.next() != null)
	    lastAssembly_ = lastAssembly_.next();
    }

    // Remove assemblies that are gaps from the list of assemblies
    public int removeGaps()
    {
	int ret = 0;
	// Removing gaps at the beginning
	while (assemblyList_ != null && assemblyList_.isGap()) {
	    if (assemblyList_.next() == null) assemblyList_ = null;
	    else {
		assemblyList_ = assemblyList_.next();
		assemblyList_.extractBefore();
		ret++;
	    }
	}

	// Removing gaps in the rest of the chain
	lastAssembly_ = assemblyList_;
	for (Assembly a = assemblyList_;a != null;a = a.next()) {
	    lastAssembly_ = a;
	    Assembly next_a = a.next();
	    while (next_a != null && next_a.isGap()) {
		a.extractAfter();
		ret++;
		next_a = a.next();
	    }
	}
	return ret;
    }

    // Access to molecule the chain belongs to
    private Molecule mol_ = null;
    public  Molecule molecule() { return mol_; }
    public  boolean  setMolecule(Molecule mol)
    {
	if (mol_ != null)
	    for (Chain c = mol_.chainList();c != null;c = c.next())
		if (c == this) return false;
	mol_ = mol;
	return true;
    }

    // Chain id
    char id_ = '_';
    public char getId() { return id_; }

    // Model
    int model_ = 0;
    public int  getModel() { return model_; }
    public void setModel(int model) { model_ = model; }

    // Counting assemblies and atoms
    public int countAssemblies()
    {
	int ret = 0;
	for (Assembly s = assemblyList();s != null;s = s.next())
	    if (!s.isGap()) ret++;
	return ret;
    }

    // Counting number of atoms in a chain
    public int countAtoms()
    {
	int ret = 0;
	for (Assembly s = assemblyList();s != null;s = s.next())
	    ret += s.countAtoms();
	return ret;
    }

    // Counting number of selected atoms in a chain
    public int countSelectedAtoms()
    {
	int ret = 0;
	for (Assembly s = assemblyList();s != null;s = s.next())
	    ret += s.countSelectedAtoms();
	return ret;
    }

    // Selecting all atoms
    public void selectAllAtoms(boolean val)
    {
	for (Assembly s = assemblyList();s != null;s = s.next())
	    s.selectAllAtoms(val);
    }

    // Sequence of the chain.
    public String getSequence()
    {
	StringBuffer ret = new StringBuffer(200);
	for (Assembly a = assemblyList_;a != null;a = a.next())
	    ret.append(a.getLetterName());
	return ret.toString();
    }

    // Resets group id for assemblies in the chain to the specified value.
    public void resetGroupId(int val)
    {
	for (Assembly a = assemblyList_;a != null;a = a.next())
	    a.setGroupId(val);
    }
}
