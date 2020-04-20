package belka.mol;

//--- Java import ---
import java.io.*;

/**
 * Abstract class describing behavior of a molecule. Molecule can be as simple
 * as single chemical compaund and as complex as protein bound to a DNA with
 * surrounding water.
 * 
 * @author Alexej Abyzov
 */
public abstract class Molecule extends Transformable implements Serializable
{
    /**
     * Static method to create a new molecule.
     *
     * @param name molecule's name.
     * @param pdbCode molecule's PDB-code.
     * @return new created molecule.
     */
    public static belka.mol.Molecule create(String name,String pdbCode)
    {
	return new Molecule_imp(name,pdbCode);
    }

    /**
     * Static method to create a new molecule.
     *
     * @param name molecule's name.
     * @return new created molecule.
     */
    public static belka.mol.Molecule create(String name)
    {
	return new Molecule_imp(name,null);
    }

    /**
     * Disassembles molecule into null pointers to help garbage collector to 
     * free memory.
     */
    public abstract void disassemble();

    /**
     * Returns next molecule in list.
     *
     * @return next molecule in list.
     */
    public abstract Molecule next();

    /**
     * Returns previous molecule in list.
     *
     * @return previous molecule in list.
     */
    public abstract Molecule prev();

    /**
     * Adds a molecule after the current one. The function adds the molecule
     * only if it is not linked to any other molecule and if the current
     * molecule does not have linked next molecule.
     *
     * @param newMol molecule to add.
     * @return 'true' if the newMol has been added, 'false' otherwise.
     */
    public abstract boolean addAfter(Molecule newMol);

    /**
     * Adds a molecule before the current one. The function adds the molecule
     * only if it is not linked to any other molecule and if the current
     * molecule does not have linked previous molecule.
     *
     * @param newMol molecule to add.
     * @return 'true' if the newMol has been added, 'false' otherwise.
     */
    public abstract boolean addBefore(Molecule newMol);

    /**
     * Extracts the molecule following the current one from the list.
     * Extracted molecule is not linked to any other molecule.
     *
     * @return next molecule in the list.
     */
    public abstract Molecule extractAfter();

    /**
     * Extracts the molecule preceeding the current one from the list.
     * Extracted molecule is not linked to any other molecule.
     *
     * @return previous molecule in the list.
     */
    public abstract Molecule extractBefore();

    /**
     * Returns list of chains.
     *
     * @return list of chains.
     */
    public abstract Chain chainList();

    /**
     * Adds a chain to the end of chain list for the current molecule. The
     * chain must be not linked to other chain. The function sets link of
     * added chain to the current molecule.
     *
     * @param chain chain to add.
     * @return 'true' if the chain has been added, 'false' otherwise.
     */
    public abstract boolean addChain(Chain chain);

    /**
     * Returns molecule's name. Always not null.
     *
     * @return molecule's name.
     */
    public abstract String getName();

    /**
     * Returns molecule's PDB-code. Always not null.
     *
     * @return molecule's PDB-code.
     */
    public abstract String getPDBCode();

    /**
     * Returns number of chains in the molecule.
     *
     * @return number of chains in the molecule.
     */
    public abstract int countChains();

    /**
     * Returns number of assemblies in the molecule.
     *
     * @return number of assemblies in the molecule.
     */
    public abstract int countAssemblies();

    /**
     * Returns number of atoms in the molecule.
     *
     * @return number of atoms in the molecule.
     */
    public abstract int countAtoms();

    /**
     * Returns number of selected atoms in the molecule.
     *
     * @return number of selected atoms in the molecule.
     */
    public abstract int countSelectedAtoms();

    /**
     * Returns number of different NMR models in the molecule.
     *
     * @return number of different NMR models in the molecule.
     */
    public abstract int countModels();

    /**
     * Select or deselect all atoms in a molecule.
     *
     * @param val if true -- select all atoms, if false -- deselect.
     */
    public abstract void selectAllAtoms(boolean val);

    /**
     * The function transforms a molecule.
     * The transformation is not related to screen rotation and affect 
     * atoms real (not screen) coordinates.
     *
     * @param rot rotation matrix.
     * @param trans translation vector.
     * @param center center of coordinates. If null then the center is (0,0,0).
     *
     * @return true is succesfull, false otherwise.
     */
    public abstract boolean rotate(double [][] rot,double[] trans,
				   double[] center);

    /**
     * The function transforms a molecule.
     * The transformation is not related to screen rotation and affect 
     * atoms real (not screen) coordinates.
     *
     * @param rot rotation matrix.
     * @param trans translation vector.
     *
     * @return true is succesfull, false otherwise.
     */
    public abstract boolean rotate(double [][] rot,double[] trans);

    /**
     * Returns string representation of the molecule.
     *
     * @return string representation of the molecule.
     */ 
    public String toString() { return "Molecule"; };
}
