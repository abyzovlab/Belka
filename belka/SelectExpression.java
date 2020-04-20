package belka;

//--- Application imports ---
import belka.mol.*;

/**
 * Object of this class represents an expression stating set of atoms.
 * Object represents only part of the expression not including boolean logic.
 * The boolean logic is implement by linking with next object and by knowing
 * boolean operation 'and' or 'or' applied to sum values returned by current
 * and next expressions. <p>
 * For example expression: <p>
 * <code> val28 and *.CA </code> <p>
 * will be represented inside of the program by two objects of this class
 * linked by boolean operation 'and'<p>
 * <code> obj1--and-->obj2 </code> <p>
 *
 * @author Alexej Abyzov
 */

public class SelectExpression
{
    // Checking expression for molecule
    private String molName_       = null; boolean useMolName_       = false;
    private String chainIds_      = null; boolean useChainIds_      = false;
    private String assemblyName_  = null; boolean useAssemblyName_  = false;
    private int    assemblyNum_   = 0;    boolean useAssemblyNum_   = false;
    private int    assemblyAtt_   = 0;    boolean useAssemblyAtt_   = false;
    private String atomName_      = null; boolean useAtomName_      = false;
    private int    atomAtt_       = 0;    boolean useAtomAtt_       = false;
    private SelectExpression subExpression_ = null;
    private boolean notOnlyMol_      = false;
    private boolean notOnlyChain_    = false;
    private boolean notOnlyAssembly_ = false;
    private boolean notOnlyAtom_     = false;

    /**
     * Object constructor
     *
     * @param notExpr flag for indicating that expression must be reversed.
     */
    public SelectExpression(boolean notExpr)
    {
	this(null,null,null,null,null,notExpr);
    }

    /**
     * Object constructor
     *
     * @param molName molecule name. Ignored in selection if null.
     * @param chainIds chain identifie. Ignored in selection if null or length
     * is not one.
     * @param assemblyName assembly name. Ignored in selection if null.
     * @param assemblyNum assembly number. Ignored in selection if not number.
     * @param atomName atom name. Ignored in selection if null.
     * @param notExpr flag for indicating that expression must be reversed.
     */
    public SelectExpression(String  molName,
			    String  chainIds,
			    String  assemblyName,
			    String  assemblyNum,
			    String  atomName,
			    boolean notExpr)
    {
	// Molecule name
	if (molName != null && molName.length() > 0) {
	    molName_ = molName;
	    useMolName_ = true;
	    notOnlyChain_ = notOnlyAssembly_ = notOnlyAtom_ = true;
	} else useMolName_ = false;


	// Chain Ids
	if (chainIds != null && chainIds.length() > 0) {
	    chainIds_    = chainIds;
	    useChainIds_ = true;
	    notOnlyMol_  = notOnlyAssembly_ = notOnlyAtom_ = true;
	} else useChainIds_ = false;

	// Assembly name
	if (assemblyName != null && assemblyName.length() > 0) {
	    assemblyName_ = assemblyName;
	    useAssemblyName_ = true;
	    notOnlyMol_ = notOnlyChain_ = notOnlyAtom_ = true;
	} else useAssemblyName_ = false;

	
	// Assembly number
	if (assemblyNum != null && assemblyNum.length() > 0) {
	    try {
		assemblyNum_ = Integer.parseInt(assemblyNum);
		useAssemblyNum_ = true;
	    } catch (Exception e) {
		useAssemblyNum_ = false;
		notOnlyMol_ = notOnlyChain_ = notOnlyAtom_ = true;
	    }
	} else useAssemblyNum_ = false;

	// Atom name
	if (atomName != null && atomName.length() > 0) {
	    atomName_ = atomName;
	    useAtomName_ = true;
	    notOnlyMol_ = notOnlyChain_ = notOnlyAssembly_ = true;
	} else useAtomName_ = false;

	notExpr_ = notExpr;
    }

    /**
     * Object constructor
     *
     * @param subExpression sub-expression, i.e. (...) or 1-20.
     * @param notExpr flag to indicate that expression must be reversed.
     */
    public SelectExpression(SelectExpression subExpression,
			    boolean notExpr)
    {
	// Sub-expression
	subExpression_ = subExpression;
	notOnlyMol_ = notOnlyChain_ = notOnlyAssembly_ = notOnlyAtom_ = true;

	notExpr_ = notExpr;
    }

    /**
     * Sets assembly attribute.
     *
     * @param assemblyAtt assembly attribute.
     */
    public void setAssemblyAttribute(int assemblyAtt)
    {
	assemblyAtt_    = assemblyAtt;
	useAssemblyAtt_ = true;
	notOnlyMol_ = notOnlyChain_ = notOnlyAtom_ = true;
    }

    /**
     * Sets atom attribute.
     *
     * @param atomAtt atom attribute.
     */
    public void setAtomAttribute(int atomAtt)
    {
	atomAtt_    = atomAtt;
	useAtomAtt_ = true;
	notOnlyMol_ = notOnlyChain_ = notOnlyAssembly_ = true;
    }


    // Flag to indicate that it is 'not' expression
    boolean notExpr_ = false;
    boolean isNot() { return notExpr_; }

    // Flag to indicating type of boolean selection
    static final int SELECTION_OR  = 0x001;
    static final int SELECTION_AND = 0x002;
    int bool_ = 0;
    /**
     * Returns true if boolean operation with next expression is 'or',
     * false otherwise.
     *
     * @return true if boolean operation with next expression is 'or',
     * false otherwise.
     */
    public boolean isOrBoolean() 
    {
	if ((bool_ & SELECTION_OR) > 0) return true;
	else                            return false;
    }

    /**
     * Returns true if boolean operation with next expression is 'and',
     * false otherwise.
     *
     * @return true if boolean operation with next expression is 'and',
     * false otherwise.
     */
    public boolean isAndBoolean() 
    {
	if ((bool_ & SELECTION_AND) > 0) return true;
	else                             return false;
    }

    // Next expression
    private SelectExpression next_ = null;

    /**
     * Returns next expression.
     *
     * @return next expression.
     */

    public  SelectExpression next() { return next_; }
    /**
     * Sets next expression and boolean operation 'or' with it.
     *
     * @param expr expression to set.
     * @return true if the expression was set, false if expressin is null or if
     * current expression already has next expression.
     */
    public boolean setNextOr(SelectExpression expr)
    {
	if (next_ != null || expr == null) return false;
	next_  = expr;
	bool_ |= SELECTION_OR;
	return true;
    }

    /**
     * Sets next expression and boolean operation 'and' with it.
     *
     * @param expr expression to set.
     * @return true if the expression was set, false if expressin is null or if
     * current expression already has next expression.
     */
    public boolean setNextAnd(SelectExpression expr)
    {
	if (next_ != null || expr == null) return false;
	next_  = expr;
	bool_ |= SELECTION_AND;
	return true;
    }

    /**
     * Returns true if any atom in molecule may satisfy the expression,
     * false otherwise. All the boolean values from linked expressions are
     * summed. Decision is made based on select specification for molecule
     * only, no chains, assamblies, and atoms are checked. The funcion is fast.
     *
     * @param mol molecule to check.
     * @return true if any atom in molecule may satisfy the expression,
     * false otherwise.
     */
    public boolean moleculeMaySatisfy(Molecule mol)
    {
	if (mol == null) return false;
	
	boolean self_ret = true; // By default molecule satisfies

	if (useMolName_)
	    if (!molName_.equalsIgnoreCase(mol.getName()) &&
		!molName_.equalsIgnoreCase(mol.getPDBCode()))
		self_ret = false;

	// Sub-expression
	if (subExpression_ != null) {
	    boolean sub_ret = subExpression_.moleculeMaySatisfy(mol);
	    self_ret &= sub_ret;
	}

	// Handling 'not' expressions
	if (isNot()) self_ret = !self_ret; // If not and 
	if (isNot()     && // If not and 
	    !self_ret   && // false and 
	    notOnlyMol_)   // not only molecule is used
	    self_ret = true;
	
	// Next expression
	if (next_ != null) {
	    boolean next_ret = next_.moleculeMaySatisfy(mol);
	    if (isOrBoolean())  self_ret |= next_ret;
	    if (isAndBoolean()) self_ret &= next_ret;
	}

	return self_ret;
    }
    
    /**
     * Returns true if any atom in chain may satisfy the expression, false
     * otherwise. All the boolean values from linked expressions are summed.
     * Decision is made based on select specification for chain only, no
     * molecules, assamblies, and atoms are checked. The funcion is fast.
     * 
     * @param chain chain to check.
     * @return true if any atom in chain may satisfy the expression, false
     * otherwise.
     */
    public boolean chainMaySatisfy(Chain chain)
    {
	if (chain == null) return false;

	boolean self_ret = true; // By default chain satisfies

	if (useChainIds_ && chainIds_.indexOf(chain.getId()) < 0)
	    self_ret = false;

	// Sub-expression
	if (subExpression_ != null) {
	    boolean sub_ret = subExpression_.chainMaySatisfy(chain);
	    self_ret &= sub_ret;
	}

	// Handling 'not' expressions
	if (isNot()) self_ret = !self_ret; // If not and 
	if (isNot()       && // If not and 
	    !self_ret     && // false and 
	    notOnlyChain_)   // not only chain is used
	    self_ret = true;
	
	// Next expression
	if (next_ != null) {
	    boolean next_ret = next_.chainMaySatisfy(chain);
	    if (isOrBoolean())  self_ret |= next_ret;
	    if (isAndBoolean()) self_ret &= next_ret;
	}

	return self_ret;
    }

    /**
     * Returns true if any atom in assembly may satisfy the expression,
     * false otherwise. All the boolean values from linked expressions are
     * summed. Decision is made based on select specification for assembly
     * only, no molecules, chains and atoms are checked. The funcion is fast.
     * 
     * @param assembly assembly to check.
     * @return true if any atom in assembly may satisfy the expression,
     * false otherwise.
     */
    public boolean assemblyMaySatisfy(Assembly assembly)
    {
	if (assembly == null) return false;

	boolean self_ret = true; // By default assembly satisfies

	if (useAssemblyName_ && 
	    !assemblyName_.equalsIgnoreCase(assembly.getName()))
	    self_ret = false;

	if (useAssemblyNum_ &&
	    assemblyNum_ != assembly.getSerialNum())
	    self_ret = false;

 	if (useAssemblyAtt_ && 
 	    !assembly.hasAttribute(assemblyAtt_))
 	    self_ret = false;
	
	// Sub-expression
	if (subExpression_ != null) {
	    boolean sub_ret = subExpression_.assemblyMaySatisfy(assembly);
	    self_ret &= sub_ret;
	}

	// Handling 'not' expressions
	if (isNot()) self_ret = !self_ret; // If not
	if (isNot()   &&      // If not and
	    !self_ret &&      // and false and 
	    notOnlyAssembly_) // not only assembly is used
	    self_ret = true; 

	// Next expression
	if (next_ != null) {
	    boolean next_ret = next_.assemblyMaySatisfy(assembly);
	    if (isOrBoolean())  self_ret |= next_ret;
	    if (isAndBoolean()) self_ret &= next_ret;
	}

	return self_ret;
    }
    
    /**
     * Returns true if the atom may satisfy the expression, false
     * otherwise. All the boolean values from linked expressions are summed.
     * Decision is made based on select specification for atom, no molecules,
     * chains, and assemblys are checked. The funcion is fast.
     * 
     * @param atom atom to check.
     * @return true if the atom may satisfy the expression, false
     * otherwise.
     */
    public boolean atomMaySatisfy(Atom atom)
    {
	if (atom == null) return false;

	boolean self_ret = true; // By default assembly satisfies

	if (useAtomName_ &&
	    !atomName_.equalsIgnoreCase(atom.getName()))
	    self_ret = false;

 	if (useAtomAtt_ && 
 	    !atom.hasAttribute(atomAtt_))
	    self_ret = false;

	// Sub-expression
	if (subExpression_ != null) {
	    boolean sub_ret = subExpression_.atomMaySatisfy(atom);
	    self_ret &= sub_ret;
	}

	// Handling 'not' expressions
	if (isNot()) self_ret = !self_ret; // If not
	if (isNot()   &&  // If not and 
	    !self_ret &&  // and false and 
	    notOnlyAtom_) // not only atom is used
	    self_ret = true;

	// Next expression 
	if (next_ != null) {
	    boolean next_ret = next_.atomMaySatisfy(atom);
	    if (isOrBoolean())  self_ret |= next_ret;
	    if (isAndBoolean()) self_ret &= next_ret;
	}

	return self_ret;
    }

    /**
     * Checks if specified atom from specified assembly from specified chain 
     * from specified molecule satisfies the selection. All the boolean
     * values from linked expressions are summed. It's responsibility of the
     * calling function to make sure that input atom belongs to input
     * assembly that belongs to input chain that belongs to input molecule.
     * 
     * @param mol      molecule to check.
     * @param chain    chain to check.
     * @param assembly assembly to check.
     * @param atom     atom to check.
     * @return true or false value.
     */
    public boolean satisfy(Molecule mol,
			   Chain chain,
			   Assembly assembly,
			   Atom atom)
    {
	boolean self_ret = true;

	// Checking molecule
	if (useMolName_ && mol != null)
	    if (!molName_.equalsIgnoreCase(mol.getName()) &&
		!molName_.equalsIgnoreCase(mol.getPDBCode()))
		self_ret = false;

	// Checking chain
	if (self_ret && useChainIds_ && chain != null)
	    if (chainIds_.indexOf(chain.getId()) < 0)
	    //if (chainIds_ != chain.getId())
		self_ret = false;

	// Checking assembly name
	if (self_ret && useAssemblyName_ && assembly != null)
	    if (!assemblyName_.equalsIgnoreCase(assembly.getName()))
		self_ret = false;

	// Checking assembly number
	if (self_ret && useAssemblyNum_ && assembly != null)
	    if (assemblyNum_ != assembly.getSerialNum())
		self_ret = false;

	// Checking group for assembly
	if (self_ret && useAssemblyAtt_ && assembly != null)
	    if (!assembly.hasAttribute(assemblyAtt_))
		self_ret = false;
	
	// Checking atom name
	if (self_ret && useAtomName_ && atom != null)
	    if (!atomName_.equalsIgnoreCase(atom.getName()))
		self_ret = false;

	// Checking atom attribute
 	if (self_ret && useAtomAtt_ && atom != null)
 	    if (!atom.hasAttribute(atomAtt_))
		self_ret = false;

	// Sub-expression
	if (subExpression_ != null) {
	    boolean sub_ret = subExpression_.satisfy(mol,chain,assembly,atom);
	    self_ret &= sub_ret;
	}

	// Reverse if the expresion isNot
	if (isNot()) self_ret = !self_ret;

	// Next expression
	if (next_ != null) {
	    boolean next_ret = next_.satisfy(mol,chain,assembly,atom);
	    if (isOrBoolean())  self_ret |= next_ret;
	    if (isAndBoolean()) self_ret &= next_ret;
	}

	return self_ret;
    }


    /**
     * Checks if given molecule is explicitly specified in selection.
     * 
     * @param mol molecule to check.
     * @return true or false value.
     */
    public boolean specifiesMolecule(Molecule mol)
    {
	if (useMolName_)
	    if (molName_.equalsIgnoreCase(mol.getName()) ||
		molName_.equalsIgnoreCase(mol.getPDBCode()))
	    return true;
	return false;
    }

    /**
     * The function returns the number of chains specified in selection.
     *
     * @return the number of chains specified in selection.
     */
    public int numChainsSpecified()
    {
	if (!useChainIds_ || chainIds_ == null) return 0;
	return chainIds_.length();
    }

    /**
     * Checks if given chain is explicitly specified in selection.
     * 
     * @param chain chain to check.
     * @return true or false value.
     */
    public boolean specifiesChain(Chain chain)
    {
	if (useChainIds_ && chainIds_.indexOf(chain.getId()) >= 0)
	    return true;
	return false;
    }

    /**
     * Checks if given chain is explicitly specified in selection in 
     * specific order.
     * 
     * @param chain chain to check.
     * @param index chain index in selection.
     * @return true or false value.
     */
    public boolean specifiesChainAs(Chain chain,int index)
    {
	if (index < 0 || index >= chainIds_.length()) return false;
	if (useChainIds_ && chainIds_.charAt(index) == chain.getId())
	    return true;
	return false;
    }
}
