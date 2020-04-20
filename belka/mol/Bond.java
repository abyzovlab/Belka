package belka.mol;

//--- Java import ---
import java.awt.*;
import java.io.*;

/**
 * Abstract class describing behavior of a bond. Bond is a relation between
 * two atoms. It can be, for example, covalent bond, hydrogen bond, edge of 
 * DT tessellation, etc.
 * 
 * @author Alexej Abyzov
 */
public abstract class Bond implements Serializable
{
    /**
     * Static method to create a new bond
     *
     * @param a1 bond's first atom.
     * @param a2 bond's second atom.
     * @return new created bond.
     */
    public static belka.mol.Bond create(belka.mol.Atom a1,belka.mol.Atom a2)
    {
	return new Bond_imp(a1,a2);
    }

    /**
     * Disassembles bond into null pointers to help garbage collector to 
     * free memory.
     */
    public abstract void disassemble();

    /**
     * Returns first atom of the bond.
     *
     * @return first atom of the bond.
     */
    public abstract Atom getFAtom();

    /**
     * Returns second atom of the bond.
     *
     * @return second atom of the bond.
     */
    public abstract Atom getSAtom();

    /**
     * Returns color of the cylinder representing the bond on a screen.
     *
     * @return color of the cylinder representing the bond on a screen.
     */
    public abstract Color getColor();

    /**
     * Returns radius of the cylinder representing the bond.
     *
     * @return radius of the cylinder representing the bond.
     */
    public abstract double getRadius();

    /**
     * Sets radius of the cylinder representing the bond.
     *
     * @param r of the cylinder representing the bond.
     */
    public abstract void   setRadius(double r);

    /**
     * Returns radius of the cylinder representing the bond on a screen.
     *
     * @return radius of the cylinder representing the bond on a screen.
     */
    public abstract int    getScreenRadius();

    /**
     * Sets radius of the cylinder representing the bond on a screen.
     * Value is rescaled to int.
     *
     * @param r radius of the cylinder representing the bond on a screen.
     */
    public abstract void   setScreenRadius(double r);

    /**
     * Sets bond's color on a screen.
     *
     * @param color bond's color on a screen.
     */
    public abstract void  setColor(Color color);

    /**
     * Returns string representation of the bond.
     *
     * @return string representation of the bond.
     */ 
    public String toString() { return "Bond"; }
}


