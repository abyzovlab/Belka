package belka.mol;

//--- Java import ---
import java.io.*;

/**
 * Abstract class describing behavior of a chain. Chain is a set of molecule
 * building blocks. It can be, for example, polypetide chain, nucleotide
 * chain, or set of water molecules. 
 * 
 * @author Alexej Abyzov
 */
public abstract class Chain implements Serializable
{
    /**
     * Static method to create a new chain.
     *
     * @param id chain's id.
     * @return new created chain.
     */
    public static belka.mol.Chain create(char id)
    {
	return new Chain_imp(id);
    }

    /**
     * Disassembles chain into null pointers to help garbage collector to 
     * free memory.
     */
    public abstract void disassemble();

    /**
     * Returns next chain in list.
     *
     * @return next chain in list.
     */
    public abstract Chain next();

    /**
     * Returns previous chain in list.
     *
     * @return previous chain in list.
     */
    public abstract Chain prev();

    /**
     * Adds a chain after the current one. The function adds the chain only
     * if it is not linked to any other chain and if the current chain does
     * not have linked next chain.
     *
     * @param newChain chain to add.
     * @return 'true' if the newChain has been added, 'false' otherwise.
     */
    public abstract boolean addAfter(Chain newChain);

    /**
     * Adds a chain before the current one. The function adds the chain only
     * if it is not linked to any other chain and if the current chain does
     * not have linked previous chain.
     *
     * @param newChain chain to add.
     * @return 'true' if the newChain has been added, 'false' otherwise.
     */
    public abstract boolean addBefore(Chain newChain);

    /**
     * Returns list of assemblies.
     *
     * @return list of assemblies.
     */
    public abstract Assembly assemblyList();

    /**
     * Adds an assembly to the end of assembly list for the current chain. The
     * assembly must be not linked to other assembly. The function sets
     * reference to the current chain for added assembly.
     *
     * @param assembly assembly to add.
     * @return 'true' if the assembly has been added, 'false' otherwise.
     */
    public abstract boolean addAssembly(Assembly assembly);

    /**
     * The function updates pointers to the first and last assemblies belonging
     * to the chain. It assumes that the assemblies where it currently has
     * pointers set are not deleted from the chain at the time of function
     * call. Therefore, it travels up and down the assembly list to find
     * the beginning and end. Note that you must call this function is
     * you modify the list of assemblies externaly. Internal methods of the
     * class update the pointer automatically.
     */
    public abstract void updateAssemblyPointers();

    /**
     * Removes assemblies that are gaps from the list of assemblies.
     * Returns number of removed assemblies.
     *
     * @return number of removed gaps.
     */
    public abstract int removeGaps();

    /**
     * Returns a molecule the chain belongs to.
     *
     * @return a molecule the chain belongs to.
     */
    public abstract Molecule molecule();
    
    /**
     * Sets a molecule the chain belongs to. The molecule is set only if the
     * chain does not belong to other molecule.
     *
     * @param mol molecule to set.
     * @return 'true' if the mol has been set, 'false' otherwise.
     */
    public abstract boolean setMolecule(Molecule mol);

    /**
     * Returns chain's id.
     *
     * @return chain's id.
     */
    public abstract char getId();

    /**
     * Returns chain's model number.
     *
     * @return chain's model number.
     */ 
    public abstract int getModel();

    /**
     * Sets chain's model number.
     *
     * @param model chain's model number.
     */ 
    public abstract void setModel(int model);

    /**
     * Returns number of assemblies in the chain.
     *
     * @return number of assemblies in the chain.
     */
    public abstract int countAssemblies();

    /**
     * Returns number of atoms in the chain.
     *
     * @return number of atoms in the chain.
     */
    public abstract int countAtoms();

    /**
     * Returns number of selected atoms in the chain.
     *
     * @return number of selected atoms in the chain.
     */
    public abstract int countSelectedAtoms();

    /**
     * Select or deselect all atoms in a chain.
     *
     * @param val if true -- select all atoms, if false -- deselect.
     */
    public abstract void selectAllAtoms(boolean val);

    /**
     * Returns sequence of the chain.
     *
     * @return sequence of the chain.
     */ 
    public abstract String getSequence();

    /**
     * Resets group id for assemblies in the chain to the specified value.
     */ 
    public abstract void resetGroupId(int val);

    /**
     * Returns string representation of the chain.
     *
     * @return string representation of the chain.
     */ 
    public String toString() { return "Chain"; }

}
