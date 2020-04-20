package belka.mol;

//--- Java import ---
import java.awt.*;

/**
 * Implementation of for class {@link Bond}
 *
 * @author Alexej Abyzov
 */
class Bond_imp extends Bond
{
    // Constructor
    public Bond_imp(Atom a1,Atom a2) { a1_ = a1; a2_ = a2; }

    // Disassembles bond into null pointers
    public void disassemble()
    {
	a1_ = a2_ = null;
    }

    // Atoms of the bonds
    private Atom a1_ = null;
    private Atom a2_ = null;
    public  Atom getFAtom() { return a1_; }
    public  Atom getSAtom() { return a2_; }

    // Radius of the cylinder representing the bond
    private double r_ = -1;
    public  double getRadius()         { return r_; }
    public  void   setRadius(double r) { r_ = r; }
    private int  r_screen_ = -1;
    public  int  getScreenRadius()         { return r_screen_; }
    public  void setScreenRadius(double r) { r_screen_ =
	    (int)(Atom_imp.SCALE_COORD_TO_PIXELS*r); }

    // Color of the bond
    private Color color_ = null;
    public Color getColor()            { return color_;  }
    public void  setColor(Color color) { color_ = color; }
}
