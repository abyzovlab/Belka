package belka.chem;

//--- Java imports ---
import java.awt.*;
import java.util.*;
import java.io.*;

/**
 * Objects of this class represent chemical elements.
 * Data are take from Wikipedia: http://wikipedia.org.
 * For elements missing data van der Waals radii the doubled atomic radii were 
 * used.
 * 
 * @author Alexej Abyzov
 *
 */
public class Element implements Serializable
{
    // Hash by name
    private static Hashtable<String,Element> hash_by_name_ =
	new Hashtable<String,Element>(150);

    // Hash by sign
    private static Hashtable<String,Element> hash_by_sign_ =
	new Hashtable<String,Element>(150);

    /** Hydrogen */
    public final static Element H   =
	new Element("Hydrogen",  "H",  1, 1.20, 0.37, new Color(255,255,255));

    /** Helium */
    public final static Element He  =
	new Element("Helium",    "He", 2, 1.40, 0.32, new Color(255,192,203));

    /** Lithium */
    public final static Element Li  = 
	new Element("Lithium",   "Li", 3, 1.82, 1.34, new Color(178, 34, 34));

    /**
     * Beryllium, using double atomic radius for van der Waals radius.
     */
    public final static Element Be  =
	new Element("Beryllium", "Be", 4, 2.10, 0.90, new Color(255, 20,147));

    /**
     * Boron, using double atomic radius for van der Waals radius.
     */
    public final static Element B   =
	new Element("Boron",     "B",  5, 1.70, 0.82, new Color(  0,255,  0));

    /** Carbon */
    public final static Element C  =
	new Element("Carbon",    "C",  6, 1.70, 0.77, new Color(200,200,200));

    /** Nitrogen */
    public final static Element N  =
	new Element("Nitrogen",  "N",  7, 1.55, 0.75, new Color(143,143,255));

    /** Oxygen */
    public final static Element O  =
	new Element("Oxygen",    "O",  8, 1.52, 0.73, new Color(240,  0,  0));

    /** Fluorine */
    public final static Element F  =
	new Element("Fluorine",  "F",  9, 1.47, 0.72, new Color(218,165, 32));

    /** Neon */
    public final static Element Ne =
	new Element("Neon",      "Ne",10, 1.54, 0.69, new Color(255, 20,147));

    /** Sodium */
    public final static Element Na =
	new Element("Sodium",    "Na",11, 2.27, 1.54, new Color(  0,  0,255));

    /** Magnesium */
    public final static Element Mg =
	new Element("Magnesium", "Mg",12, 1.73, 1.30, new Color( 34,139, 34));

    /**
     * Aluminium, using double atomic radius for van der Waals radius.
     */
    public final static Element Al =
	new Element("Aluminium", "Al",13, 2.50, 1.18, new Color(128,128, 14));

    /** Silicon */
    public final static Element Si =
	new Element("Silicon",   "Si",14, 2.10, 1.11, new Color(218,165, 32));

    /** Phosphorus */
    public final static Element P  =
	new Element("Phosphorus","P", 15, 1.80, 1.06, new Color(255,165,  0));

    /** Sulphur */
    public final static Element S  =
	new Element("Sulphur",   "S", 16, 1.80, 1.02, new Color(255,200, 50));

    /** Chlorine */
    public final static Element Cl =
	new Element("Chlorine",  "Cl",17, 1.89, 0.99, new Color(  0,255,  0));

    /** Argon */
    public final static Element Ar = 
	new Element("Argon",     "Ar",18, 1.88, 0.97, new Color(255, 20,147));

    /** Potassium */
    public final static Element K  =
	new Element("Potassium",  "K",19, 2.75, 1.96, new Color(255, 20,147));

    /**
     * Calcium, using double atomic radius for van der Walls radius.
     */
    public final static Element Ca =
	new Element("Calcium",   "Ca",20, 3.60, 1.74, new Color(128,128,144));

    /**
     * Scandium, using double atomic radius for van der Walls radius.
     */
    public final static Element Sc =
	new Element("Scandium",  "Sc",21, 3.20, 1.44, new Color(255, 20,147));

    /**
     * Titanium, using double atomic radius for van der Waals radius.
     */
    public final static Element Ti =
	new Element("Titanium",  "Ti",22, 2.80, 1.36, new Color(128,128,144));

    /**
     * Vanadium, using double atomic radius for van der Waals radius.
     */
    public final static Element V  =
	new Element("Vanadium",   "V",23, 2.70, 1.25, new Color(255, 20,147));

    /**
     * Chromium, using double atomic radius for van der Waals radius.
     */
    public final static Element Cr =
	new Element("Chromium",  "Cr",24, 2.80, 1.27, new Color(128,128,144));

    /**
     * Manganese, using double atomic radius for van der Waals radius.
     */
    public final static Element Mn =
	new Element("Manganese", "Mn",25, 2.80, 1.39, new Color(128,128,144));

    /**
     * Iron, using double atomic radius for van der Waals radius.
     */
    public final static Element Fe =
	new Element("Iron",      "Fe",26, 2.80, 1.25, new Color(255,165,  0));

    /**
     * Cobalt, using double atomic radius for van der Waals radius.
     */
    public final static Element Co =
	new Element("Cobalt",    "Co",27, 2.70, 1.26, new Color(255, 20,147));

    /** Nickel */
    public final static Element Ni =
	new Element("Nickel",    "Ni",28, 1.63, 1.21, new Color(165, 42, 42));

    /** Copper */
    public final static Element Cu =
	new Element("Copper",    "Cu",29, 1.40, 1.38, new Color(165, 42, 42));

    /** Zinc */
    public final static Element Zn =
	new Element("Zinc",      "Zn",30, 1.39, 1.31, new Color(165, 42, 42));

    /** Gallium */
    public final static Element Ga =
	new Element("Gallium",   "Ga",31, 1.87, 1.26, new Color(255, 20,147));

    /**
     * Germanium, using double atomic radius for van der Waals radius.
     */
    public final static Element Ge =
	new Element("Germanium", "Ge",32, 2.50, 1.22, new Color(255, 20,147));

    /** Arsenic */
    public final static Element As =
	new Element("Arsenic",   "As",33, 1.85, 1.19, new Color(255, 20,147));

    /** Selenium */
    public final static Element Se =
	new Element("Selenium",  "Se",34, 1.90, 1.16, new Color(255, 20,147));

    /** Bromine */
    public final static Element Br =
	new Element("Bromine",   "Br",35, 1.85, 1.14, new Color(165, 42, 42));

    /** Krypton */
    public final static Element Kr =
	new Element("Krypton",   "Kr",36, 2.02, 1.10, new Color(255, 20,147));

    // Constructor
    private Element(String name,String sign,int atomic_number,
		    double vdw_radius,double coval_radius,
		    Color col)
    {
	name_          = name;
	sign_          = sign;
	atomic_number_ = atomic_number;
	vdw_radius_    = vdw_radius;
	coval_radius_  = coval_radius;
	col_           = col;

	if (name != null && name.length() > 0) {
	    if (hash_by_name_.get(name) == null)
		hash_by_name_.put(name,this);
	    else
		System.err.println("WARNING: Element '" + name + "' " +
				   "already exists.");
	}

	if (sign != null && sign.length() > 0)
	    if (hash_by_sign_.get(sign) == null)
		hash_by_sign_.put(sign,this);
	    else
		System.err.println("WARNING: Element '" + sign + "' " +
				   "already exists.");
    }

    // Element name
    private String name_ = "";
    /**
     * Returns element's full name, for example "Iron".
     *
     * @return element's full name.
     */
    public String getName() { return name_; }

    // Sign name
    private String sign_ = "";
    /**
     * Returns element's chemical sign, for example "Fe".
     *
     * @return element's chemical sign.
     */
    public String getSign() { return sign_; }

    // Table number
    private int atomic_number_ = 0;
    /**
     * Returns element's number in Mendeleev's periodic table,
     * for example 26 for iron.
     *
     * @return element's number in Mendeleev's periodic table.
     */
    public int getAtomicNumber() { return atomic_number_; }

    // VDW radius
    private double vdw_radius_ = 0;
    /**
     * Returns element's van der Waals radius. 
     * In case the radius is not know the double atomic radius is returned.
     *
     * @return element's van der Waals radius. 
     */
    public double getVDWRadius() { return vdw_radius_; }

    // Covalent radius
    private double coval_radius_ = 0;
    /**
     * Returns element's covalent radius.
     *
     * @return element's covalent radius.
     */
    public double getCovalentRadius() { return coval_radius_; }

    // Color
    private Color col_ = null;
    /**
     * Returns element's color to be used to draw it.
     *
     * @return element's color to be used to draw it.
     */
    public Color getColor() { return col_; }

    /**
     * Returns element with given chemical name.
     * 
     * @param name element's chemical name
     * @return element with inputed name. Null if there is not such element.
     */
    public static Element getElementByName(String name)
    {
	if (name == null) return null;
	return hash_by_name_.get(name);
    }

    /**
     * Returns element with given chemical sign.
     * 
     * @param sign element's chemical sign
     * @return element with inputed sign. Null if there is not such element.
     */
    public static Element getElementBySign(String sign)
    {
	if (sign == null) return null;
	return hash_by_sign_.get(sign);
    }
}
