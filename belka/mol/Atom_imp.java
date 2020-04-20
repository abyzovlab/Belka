package belka.mol;

//--- Java import ---
import java.awt.*;

//--- Application imports ---
import belka.chem.*;

/**
 * Implementation for class {@link Atom}
 *
 * @author Alexej Abyzov
 */
class Atom_imp extends Atom
{
    // Constructor
    public Atom_imp(String name,double x,double y,double z,
		    Element element)
    {
	name_   = name;
	if (name_ == null) name_ = "";
	x_ = x; x_der_ = x;
	y_ = y; y_der_ = y;
	z_ = z; z_der_ = z;
	element_ = element;
    }

    // Disassembles atom into null pointers
    public void disassemble()
    {
	if (bonds_ != null)
	    for (int b = 0;b < bonds_.length;b++)
		if (bonds_[b] != null) {
		    bonds_[b].disassemble();
		    bonds_[b] = null;
		}
	ass_     = null;
	element_ = null;
    }

    // Next atom
    private Atom next_ = null;
    public  Atom next() { return next_; }

    // Previous atom
    private Atom prev_ = null;
    public  Atom prev() { return prev_; }

    // Adding atom before
    public boolean addBefore(Atom newAtom) 
    {
	// Check if input is good
	if (newAtom == null) return false;

	// Check if same object
	if (newAtom == this) return false;

	// Check if the call comes from addAfter
	if (newAtom.next() == this && prev_ == null) {
	    prev_ = newAtom;
	    return true;
	}
	
	// Chack if they are already paired
	if (newAtom.prev() == this && next_ == newAtom) return true;
	
	// Check if it can be added
	if (prev_ != null) return false;
	if (newAtom.next() != null) return false;
	if (newAtom.prev() != null) return false;

	prev_ = newAtom; // Set previous 
	if (!newAtom.addAfter(this)) { // Update next for newAtom
	    prev_ = null;
	    return false;
	}
	return true;
    }

    // Adding atom after
    public boolean addAfter(Atom newAtom)
    {
	// Check if not null
	if (newAtom == null) return false;

	// Check if same object
	if (newAtom == this) return true;

	// Check if the call comes from addAfter
	if (newAtom.prev() == this && next_ == null) {
	    next_ = newAtom;
	    return true;
	}
	
	// Chack if they are already paired
	if (newAtom.prev() == this && next_ == newAtom) return true;
	
	// Check if it can be added
	if (next_ != null) return false;
	if (newAtom.next() != null) return false;
	if (newAtom.prev() != null) return false;

	next_ = newAtom; // Set next
	if (!newAtom.addBefore(this)) { // Update previous for newAtom
	    next_ = null;
	    return false;
	}
	return true;
    }

    // Access to assembly the atom belongs to
    private Assembly ass_ = null;
    public  Assembly assembly() { return ass_; }
    public  boolean setAssembly(Assembly res)
    {
	if (ass_ != null)
	    for (Atom a = ass_.atomList();a != null;a = a.next())
		if (a == this) return false;
	ass_ = res;
	return true;
    }

    // Access to the covalent bonds for the atoms
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

    // Original coordinates (double)
    double x_ = 0,y_ = 0,z_ = 0;
    public double getX() { return x_; }
    public double getY() { return y_; }
    public double getZ() { return z_; }

    // Derived coordiantes if the center of coordinates is moved
    double x_der_ = 0,y_der_ = 0,z_der_ = 0;
    public double getDerivedX() { return x_der_; }
    public double getDerivedY() { return y_der_; }
    public double getDerivedZ() { return z_der_; }
    public void setDerivedX(double x) { x_der_ = x; }
    public void setDerivedY(double y) { y_der_ = y; }
    public void setDerivedZ(double z) { z_der_ = z; }

    // Screen coordinates (int)
    public final static double SCALE_COORD_TO_PIXELS = 1000;
    int x_screen_ = 0,y_screen_ = 0,z_screen_ = 0;
    public int  getScreenX() { return x_screen_; }
    public int  getScreenY() { return y_screen_; }
    public int  getScreenZ() { return z_screen_; }
    public void setScreenX(double x) { x_screen_ =
	    (int)(SCALE_COORD_TO_PIXELS*x); }
    public void setScreenY(double y) { y_screen_ =
	    (int)(SCALE_COORD_TO_PIXELS*y); }
    public void setScreenZ(double z) { z_screen_ =
	    (int)(SCALE_COORD_TO_PIXELS*z); }

    // Radius of the sphere representing the atom
    double r_ = -1;
    public double getRadius()         { return r_; }
    public void   setRadius(double r) { r_ = r; }
    private int  r_screen_ = -1;
    public  int  getScreenRadius()      { return r_screen_; }
    public  void setScreenRadius(double r)
    {
	double val = SCALE_COORD_TO_PIXELS*r;
	if (val > 0)      r_screen_ = (int)(val + 0.5);
	else if (val < 0) r_screen_ = (int)(val - 0.5);
	else              r_screen_ = 0;
    }

    // Color of the atom
    private Color color_ = null;
    public Color getColor()            { return color_;  }
    public void  setColor(Color color) { color_ = color; }

    // Atom name
    String name_ = "";
    public String getName() { return name_; }

    // Atom serial number
    int serial_ = 0;
    public int  getSerialNum() { return serial_; }
    public void setSerialNum(int serial) { serial_ = serial; }

    // Alternative conformation
    char altern_ = ' ';
    public char getAlternative()            { return altern_; }
    public void setAlternative(char altern) { altern_ = altern; }

    // Occupancy
    double occup_ = 0;
    public double getOccupancy()             { return occup_; }
    public void   setOccupancy(double occup) { occup_ = occup; }

    // Temperature
    double temper_ = 0;
    public double getTemperature()              { return temper_; }
    public void   setTemperature(double temper) { temper_ = temper; }

    // Chemical element
    Element element_ = null;
    public Element getElement() { return element_; }

    // Attribute tracking
    static final int SELECTED_ATTRIBUTE = 0x001; // Selected atom
    int att_ = SELECTED_ATTRIBUTE;

    public static int getSelectedAttribute() { return SELECTED_ATTRIBUTE; }

    // Selection attribute
    public boolean isSelected() { return ((att_ & SELECTED_ATTRIBUTE) > 0); }
    public void    setSelected(boolean val)
    {
	if (val) att_ |=  SELECTED_ATTRIBUTE;
	else     att_ &= ~SELECTED_ATTRIBUTE;
    }

    // Attributes 
    public boolean hasAttribute(int att)
    {
	return ((att_ & att) > 0);
    }


    public boolean isCA() { return (name_ != null && name_.equals("CA")); }
    public boolean isC()  { return (name_ != null && name_.equals("C"));  }
    public boolean isN()  { return (name_ != null && name_.equals("N"));  }
    public boolean isO()  { return (name_ != null && name_.equals("O"));  }
    public boolean isCB() { return (name_ != null && name_.equals("CB")); }

    // Returns distance to the given atom.
    public double distanceTo(Atom atom)
    {
	return Math.sqrt(distance2To(atom));
    }

    // Returns squared distance to the given atom.
    public double distance2To(Atom atom)
    {
	if (atom == null) return 0;
	double dx = x_der_ - atom.getDerivedX();
	double dy = y_der_ - atom.getDerivedY();
	double dz = z_der_ - atom.getDerivedZ();
	return dx*dx + dy*dy + dz*dz;
    }

}
