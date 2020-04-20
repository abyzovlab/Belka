package belka.mol;

//--- Java import ---
import java.io.*;
import java.awt.*;

//--- Application imports ---
import belka.chem.*;

/**
 * Abstract class describing behavior of an atom.
 * 
 * @author Alexej Abyzov
 */
public abstract class Atom implements Serializable
{
    /**
     * Static method to create new atom
     *
     * @param name atom's name.
     * @param x atom's X coordinate.
     * @param y atom's Y coordinate.
     * @param z atom's Z coordinate.
     * @param element atom's chemical element.
     * @return new created atom.
     */
    public static belka.mol.Atom create(String name,double x,double y,double z,
					Element element)
    {
	return new Atom_imp(name,x,y,z,element);
    }

    /**
     * Disassembles atom into null pointers to help garbage collector to 
     * free memory.
     */
    public abstract void disassemble();

    /**
     * Returns next atom in list.
     *
     * @return next atom in list.
     */
    public abstract Atom next();

    /**
     * Returns previous atom in list.
     *
     * @return previous atom in list.
     */
    public abstract Atom prev();

    /**
     * Adds an atom after the current one. The function adds the atom only if
     * it is not linked to any other atom and if the current atom does not
     * have linked next atom.
     *
     * @param newAtom atom to add.
     * @return 'true' if the newAtom has been added, 'false' otherwise.
     */
    public abstract boolean addAfter(Atom newAtom);

    /**
     * Adds an atom before the current one. The function adds the atom only
     * if it is not linked to any other atom and if the current atom
     * does not have linked previous atom.
     *
     * @param newAtom atom to add.
     * @return 'true' if the newAtom has been added, 'false' otherwise.
     */
    public abstract boolean addBefore(Atom newAtom);

    /**
     * Returns an assembly the atom belongs to.
     *
     * @return an assembly the atom belongs to.
     */
    public abstract Assembly assembly();

    /**
     * Sets assembly the atom belongs to. The assembly is set only if the
     * atom does not belong to other assembly.
     *
     * @param assembly assemlby to set. 
     * @return 'true' if the assembly has been set, 'false' otherwise.
     */
    public abstract boolean setAssembly(Assembly assembly);

    /**
     * Returns number of bonds for the atom.
     *
     * @return number of bonds for the atom.
     */
    public abstract int countBonds();

    /**
     * Returns array of covalent bonds.
     *
     * @return array of covalent bonds.
     */
    public abstract Bond[] bondArray();

    /**
     * Adds a covalent bond to the end of covalent bond array for the current
     * atom. Bond is added only if it is not already in the array of bonds 
     * for the current atom.
     *
     * @param bond bond to add.
     * @return 'true' if the bond has been added, 'false' otherwise.
     */
    public abstract boolean addBond(Bond bond);

    /**
     * Returns original (as in the loaded file) atom's X coordinate.
     *
     * @return original (as in the loaded file) atom's X coordinate.
     */
    public abstract double getX();

    /**
     * Returns original (as in the loaded file) atom's Y coordinate.
     *
     * @return original (as in the loaded file) atom's Y coordinate.
     */
    public abstract double getY();
    
    /**
     * Returns original (as in the loaded file) atom's Z coordinate.
     *
     * @return original (as in the loaded file) atom's Z coordinate.
     */
    public abstract double getZ();

    /**
     * Returns atom's X coordinate relative to current center of coordinates.
     *
     * @return atom's X coordinate relative to current center of coordinates.
     */
    public abstract double getDerivedX();

    /**
     * Returns atom's Y coordinate relative to current center of coordinates.
     *
     * @return atom's Y coordinate relative to current center of coordinates.
     */
    public abstract double getDerivedY();

    /**
     * Returns atom's Z coordinate relative to current center of coordinates.
     *
     * @return atom's Z coordinate relative to current center of coordinates.
     */
    public abstract double getDerivedZ();

    /**
     * Returns atom's X coordinate on a screen.
     *
     * @return atom's X coordinate on a screen.
     */
    public abstract int  getScreenX();

    /**
     * Returns atom's Y coordinate on a screen.
     *
     * @return atom's Y coordinate on a screen.
     */
    public abstract int  getScreenY();

    /**
     * Returns atom's Z coordinate on a screen.
     *
     * @return atom's Z coordinate on a screen.
     */
    public abstract int  getScreenZ();

    /**
     * Sets derived atom's X coordinate.
     *
     * @param x atom's derived X coordinate.
     */
    public abstract void setDerivedX(double x);

    /**
     * Sets derived atom's Y coordinate.
     *
     * @param y atom's derived Y coordinate.
     */
    public abstract void setDerivedY(double y);

    /**
     * Sets derived atom's Z coordinate.
     *
     * @param z atom's derived Z coordinate.
     */
    public abstract void setDerivedZ(double z);

    /**
     * Sets atom's X coordinate on a screen. Value is rescaled to int.
     *
     * @param x atom's X coordinate on a screen.
     */
    public abstract void setScreenX(double x);

    /**
     * Sets atom's Y coordinate on a screen. Value is rescaled to int.
     *
     * @param y atom's Y coordinate on a screen.
     */
    public abstract void setScreenY(double y);

    /**
     * Sets atom's Z coordinate on a screen. Value is rescaled to int.
     *
     * @param z atom's Z coordinate on a screen.
     */
    public abstract void setScreenZ(double z);

    /**
     * Returns radius of the sphere representing the atom.
     *
     * @return radius of the sphere representing the atom.
     */
    public abstract double getRadius();

    /**
     * Sets radius of the sphere representing the atom.
     *
     * @param r of the sphere representing the atom.
     */
    public abstract void   setRadius(double r);

    /**
     * Returns radius of the sphere representing the atom on a screen.
     *
     * @return radius of the sphere representing the atom on a screen.
     */
    public abstract int    getScreenRadius();

    /**
     * Sets radius of the sphere representing the atom on a screen.
     * Value is rescaled to int.
     *
     * @param r radius of the sphere representing the atom on a screen.
     */
    public abstract void   setScreenRadius(double r);

    /**
     * Returns color of the sphere representing the atom on a screen.
     *
     * @return color of the sphere representing the atom on a screen.
     */
    public abstract Color getColor();

    /**
     * Sets atom's color on a screen.
     *
     * @param color atom's color on a screen.
     */
    public abstract void  setColor(Color color);

    /**
     * Returns atom's name. Always not null;
     *
     * @return atom's name.
     */
    public abstract String getName();

    /**
     * Returns atom's serial number.
     *
     * @return atom's serial number.
     */
    public abstract int getSerialNum();

    /**
     * Sets atom's serial number.
     *
     * @param serial atom's serial number.
     */
    public abstract void setSerialNum(int serial);

    /**
     * Returns atom's alternate location indicator.
     *
     * @return atom's alternate location indicator.
     */
    public abstract char getAlternative();

    /**
     * Sets atom's alternate location indicator.
     *
     * @param altern atom's alternate location indicator.
     */
    public abstract void setAlternative(char altern);

    /**
     * Returns atom's occupancy.
     *
     * @return atom's occupancy.
     */
    public abstract double getOccupancy();

    /**
     * Sets atom's occupancy.
     *
     * @param occup atom's occupancy.
     */
    public abstract void setOccupancy(double occup);

    /**
     * Returns atom's temperature factor.
     *
     * @return atom's temperature factor.
     */
    public abstract double getTemperature();

    /**
     * Sets atom's temperature factor.
     *
     * @param temper atom's temperature factor.
     */
    public abstract void   setTemperature(double temper);

    /**
     * Returns atom's chemical element.
     *
     * @return atom's chemical element.
     */
    public abstract Element getElement();

    /**
     * Returns atom's selection status.
     *
     * @return atom's selection status.
     */
    public abstract boolean isSelected();

    /**
     * Sets atom's selection status.
     *
     * @param val atom's selection status.
     */
    public abstract void setSelected(boolean val);

    /**
     * Returns 'true' if atom is alpha carbon, 'false' otherwise.
     *
     * @return 'true' if atom is alpha carbon, 'false' otherwise.
     */
    public abstract boolean isCA();

    /**
     * Returns 'true' if atom is carbon of mainchain, 'false' otherwise.
     *
     * @return 'true' if atom is carbon of mainchain, 'false' otherwise.
     */
    public abstract boolean isC();

    /**
     * Returns 'true' if atom is nitrogen of mainchain, 'false' otherwise.
     *
     * @return 'true' if atom is nitrogen of mainchain, 'false' otherwise.
     */
    public abstract boolean isN();

    /**
     * Returns 'true' if atom is oxygen of mainchain, 'false' otherwise.
     *
     * @return 'true' if atom is oxygen of mainchain, 'false' otherwise.
     */
    public abstract boolean isO();

    /**
     * Returns 'true' if atom is CB carbon, 'false' otherwise.
     *
     * @return 'true' if atom is CB carbon, 'false' otherwise.
     */
    public abstract boolean isCB();

    /**
     * Returns flag attribute for selected atom.
     *
     * @return flag attribute for selected atom.
     */
    public static int getSelectedAttribute()
    {
	return Atom_imp.getSelectedAttribute();
    }

    /**
     * Returns true if the provide attribute matches to the attributes of
     * the atom.
     *
     * @return true if the provide attribute matches to the attributes of
     * the atom.
     */
    public abstract boolean hasAttribute(int att);

    /**
     * Returns distance to the given atom.
     *
     * @return distance to the given atom.
     */
    public abstract double distanceTo(Atom atom);

    /**
     * Returns squared distance to the given atom.
     *
     * @return squared distance to the given atom.
     */
    public abstract double distance2To(Atom atom);

    /**
     * Returns string representation of the atom.
     *
     * @return string representation of the atom.
     */ 
    public String toString() { return "Atom"; }

    /**
     * Returns scale used to transform atomic coordinates to pixel
     * coordiantes.
     *
     * @return scale used to transform atomic coordinates to pixel
     * coordiantes.
     */
    public static double scaleToPixels()
    {
	return Atom_imp.SCALE_COORD_TO_PIXELS;
    }
}
