package belka.mol;

//--- Java import ---
import java.io.*;

//--- Application imports ---
import belka.chem.*;


/**
 * Abstract class describing behavior of an assembly. Assembly is the group
 * of atoms, for example residue or nucleotide.
 * 
 * @author Alexej Abyzov
 */
public abstract class Assembly implements Serializable
{
    // Gap symbol
    protected final static char GAP_CHAR = '-';

    /**
     * Static method to create a new assembly.
     *
     * @param comp assembly's chemical compound.
     * @return new created assembly.
     */
    public static belka.mol.Assembly create(Compound comp)
    {
	if (comp == null) return null;
	return new Assembly_imp(comp);
    }

    /**
     * Static method to create a new assembly representing gap.
     *
     * @return new created assembly repesenting gap.
     */
    public static belka.mol.Assembly createGap()
    {
	return new Assembly_imp();
    }

    /**
     * Disassembles assembly into null pointers to help garbage collector to 
     * free memory.
     */
    public abstract void disassemble();

    /**
     * Returns true if given character represents a gap.
     *
     * @return returns true if the character represents a gap
     */
    public static boolean isGap(char c) { return (c == GAP_CHAR); }

    /**
     * Returns true if the assembly represents gap. Return false if the 
     * assembly represent real residue/nucleotide.
     *
     * @return returns true if the assembly represents gap.
     */
    public abstract boolean isGap();

    /**
     * Returns next assembly in list.
     *
     * @return next assembly in list.
     */
    public abstract Assembly next();

    /**
     * Returns previous assembly in list.
     *
     * @return previous assembly in list.
     */
    public abstract Assembly prev();

    /**
     * Inserts an assembly after the current one. The function inserts the
     * assembly only if it is not linked to any other assembly.
     *
     * @param newAssembly assembly to insert.
     * @return true if newAssemlly has been inserted, false otherwise.
     */
    public abstract boolean insertAfter(Assembly newAssembly);

    /**
     * Inserts an assembly before the current one. The function inserts the
     * assembly only if it is not linked to any other assembly.
     *
     * @param newAssembly assembly to insert.
     * @return true if newAssembly has been inserted, false otherwise.
     */
    public abstract boolean insertBefore(Assembly newAssembly);

    /**
     * Extracts the assembly following the current one from the list.
     * Extracted assembly is not linked to any other assembly.
     *
     * @return next assembly in the list.
     */
    public abstract Assembly extractAfter();

    /**
     * Extracts the assembly preceeding the current one from the list.
     * Extracted assembly is not linked to any other assembly.
     *
     * @return previous assembly in the list.
     */
    public abstract Assembly extractBefore();

    /**
     * Returns list of atoms.
     * 
     * @return list of atoms.
     */
    public abstract Atom atomList();
    
    /**
     * Adds atom to the end of atom list of the current assembly. The atom
     * must be not linked to other atom. The function sets reference to
     * the current assembly for added atom.
     *
     * @param atom atom to add.
     * @return true if atom has been added, false otherwise.
     */
    public abstract boolean addAtom(Atom atom);

    /**
     * Adds peptide/phosphodiester bond.
     *
     * @param bond bond to add.
     * @return true if bond has been added, false otherwise.
     */
    public abstract boolean addBond(Bond bond);

    /**
     * Returns number of peptide/phosphodiester bonds for the assembly.
     *
     * @return number of peptide/phosphodiester bonds for the assembly.
     */
    public abstract int countBonds();

    /**
     * Returns array of peptide/phosphodiester bonds. There can be more than
     * two bonds since neighboring assemblies can have more than one
     * conformation.
     *
     * @return array of peptide/phosphodiester bonds.
     */
    public abstract Bond[] bondArray();

    /**
     * Returns true if assemblies are connected by peptide/phosphodiester
     * bonds, false otherwise.
     * 
     * @return true if assemblies are connected by peptide/phosphodiester
     * bonds, false otherwise.
     */
    public abstract boolean isConnectedTo(Assembly s);
    
    /**
     * Returns a chain the assembly belongs to.
     *
     * @return a chain the assembly belongs to.
     */
    public abstract Chain chain();

    /**
     * Sets a chain the assembly belongs to. The chain is set only if the
     * assembly does not belong to other chain.
     *
     * @param chain chain to set.
     * @return true if chain has been set, false otherwise.
     */
    public abstract boolean setChain(Chain chain);

    /**
     * Returns assembly's compound.
     *
     * @return assembly's compound.
     */
    public abstract Compound getCompound();

    /**
     * Returns assembly's name.
     *
     * @return assembly's name.
     */
    public abstract String getName();

    /**
     * Returns assembly's one letter name.
     *
     * @return assembly's one letter name.
     */
    public abstract char getLetterName();

    /**
     * Returns assembly's serial number.
     *
     * @return assembly's serial number.
     */
    public abstract int getSerialNum();

    /**
     * Sets assembly's serial number.
     *
     * @param serial assembly's serial number.
     */
    public abstract void setSerialNum(int serial);

    /**
     * Returns assembly's icode.
     *
     * @return assembly's icode.
     */
    public abstract char getICode();

    /**
     * Sets assembly's icode.
     *
     * @param icode assembly's icode.
     */
    public abstract void setICode(char icode);

    /**
     * Returns C-alpha atom or null if it is not found.
     *
     * @return C-alpha atom or null if it is not found.
     */
    public abstract Atom getCAAtom();

    /**
     * Returns the main atom (CA for proteins and sugar P for nucleotides)
     * in assembly or null if such atom can't be found.
     *
     * @return the main atom in assembly.
     */
    public abstract Atom getMainAtom();

    /**
     * Returns number of atoms in the assembly.
     *
     * @return number of atoms in the assembly.
     */
    public abstract int countAtoms();

    /**
     * Returns number of selected atoms in the assembly.
     *
     * @return number of selected atoms in the assembly.
     */
    public abstract int countSelectedAtoms();

    /**
     * Select or deselect all atoms in a assembly.
     *
     * @param val if true -- select all atoms, if false -- deselect.
     */
    public abstract void selectAllAtoms(boolean val);

    /**
     * Returns flag attribute of aligned assemblies.
     *
     * @return flag attribute of aligned assemblies.
     */
    public static int getAlignedAttribute()
    {
	return Assembly_imp.getAlignedAttribute();
    }

    /**
     * Returns flag attribute for residues in helices. Attribute is for all
     * type of helices (i,i+3), (i,i+4), (i,i+5).
     *
     * @return flag attribute for residues in helices.
     */
    public static int getHelixAttribute()
    {
	return Assembly_imp.getHelixAttribute();
    }

    /**
     * Returns flag attribute for residues in 3,10-helices.
     *
     * @return flag attribute for residues in 3,10-helices.
     */
    public static int getHelix3Attribute()
    {
	return Assembly_imp.getHelix3Attribute();
    }

    /**
     * Returns flag attribute for residues in 4-helices.
     *
     * @return flag attribute for residues in 4-helices.
     */
    public static int getHelix4Attribute()
    {
	return Assembly_imp.getHelix4Attribute();
    }

    /**
     * Returns flag attribute for residues in 5-helices.
     *
     * @return flag attribute for residues in 5-helices.
     */
    public static int getHelix5Attribute()
    {
	return Assembly_imp.getHelix5Attribute();
    }

    /**
     * Returns flag attribute for residues in sheets.
     *
     * @return flag attribute for residues in sheets.
     */
    public static int getSheetAttribute()
    {
	return Assembly_imp.getSheetAttribute();
    }

    /**
     * Returns flag attribute for residues in turn.
     *
     * @return flag attribute for residues in turn.
     */
    public static int getTurnAttribute()
    {
	return Assembly_imp.getTurnAttribute();
    }

    /**
     * Returns flag attribute for residues in any secondary structure elements.
     *
     * @return flag attribute for residues in any secondary structure elements.
     */
    public static int getRegularAttribute()
    {
	return Assembly_imp.getRegularAttribute();
    }

    /**
     * Returns flag attribute for assemblies in particular group.
     *
     * @return flag attribute for assemblies in particular group.
     */
    public static int getGroupAttribute(int group_id)
    {
 	return Assembly_imp.getGroupAttribute(group_id);
    }

    /**
     * Returns true if assemblies is aligned, false otherwise.
     *
     * @return true if assemblies is aligned, false otherwise.
     */
    public abstract boolean isAligned();

    /**
     * Sets assembly's alignment attribute.
     *
     * @param val assembly's alignment attribute.
     */
    public abstract void setAligned(boolean val);

    /**
     * Returns group id or 0 if assembly is not assigned to any group.
     *
     * @return group id or 0 if assembly is not assigned to any group.
     */
    public abstract int getGroupId();

    /**
     * Sets group id for the assembly. Currently 3 bytes are allocated to
     * store the value.
     *
     * @return true is succesfull, false if the value is too large.
     */
    public abstract boolean setGroupId(int groupId);

    /**
     * Returns true if the provide attribute matches to the attributes of
     * the assembly.
     *
     * @return true if the provide attribute matches to the attributes of
     * the assembly.
     */
    public abstract boolean hasAttribute(int att);

    /**
     * Returns string representation of the assembly.
     *
     * @return string representation of the assembly.
     */ 
    public abstract String toString();
}
