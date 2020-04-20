package belka.geom;

//--- Java imports ---
import java.util.Vector;

//--- Application imports ---
import belka.mol.*;

/**
 * Objects of class Pair hold pair of objects (like Atom, Assembly, etc.).
 * Usage of this class is usefull when dealing with pairwise alignments.
 *
 * @author Alexej Abyzov
 */
class Pair
{
    // Constructors
    public Pair(Object obj1,Object obj2) { this(obj1,obj2,true); }

    public Pair(Object obj1,Object obj2,boolean isOfInterest)
    {
	obj1_ = obj1;
	obj2_ = obj2;
	isOfInterest_ = isOfInterest;
    }

    // Pair of objects
    private Object obj1_ = null;
    private Object obj2_ = null;
    public  Object getObject1() { return obj1_; }
    public  Object getObject2() { return obj2_; }

    // Flag to mark a pair. For example, pair of residues with gap can be
    // marked a of no interest.
    private boolean isOfInterest_ = true;
    public  boolean isOfInterest() { return isOfInterest_; }

    // Next pair
    private Pair next_ = null;
    public  Pair next() { return next_; }

    // Previous pair
    private Pair prev_ = null;
    public  Pair prev() { return prev_; }

    // Connections
    private Vector<Pair> conn_ = new Vector<Pair>(2);
    public  void    addConnection(Pair p) { conn_.add(p); }
    public  boolean isConnectedTo(Pair p) { return (conn_.indexOf(p) >= 0); }

    // Associated int value
    private int int_val_ = 0;
    public  int getIntValue()        { return int_val_; }
    public  int setIntValue(int val) { return int_val_ = val; }

    // Second associated int value
    private short short_val_ = 0;
    public  short getShortValue()          { return short_val_; }
    public  short setShortValue(short val) { return short_val_ = val; }

    // Associated double value
    private double double_val_ = 0;
    public  double getDoubleValue()           { return double_val_; }
    public  double setDoubleValue(double val) { return double_val_ = val; }

    // Associated float value
    private float float_val_ = 0;
    public  float getFloatValue()          { return float_val_; }
    public  float setFloatValue(float val) { return float_val_ = val; }

    // Adding pair after
    public boolean insertAfter(Pair newPair)
    {
	// Check is object is good
	if (newPair == null) return false;
	if (newPair == this) return false;

	// Check if the call comes from insertBefore of inserted object
	if (newPair.prev() == this && next_ == null) {
	    next_ = newPair;
	    return true;
	}

	// Check if input is good
	if (newPair.next() != null) return false;
	if (newPair.prev() != null) return false;

	Pair next = next_;
	next_ = newPair;
	if (next != null && next.prev() == this)
	    next.insertBefore(newPair);

	return newPair.insertBefore(this);
    }

    // Adding pair before
    public boolean insertBefore(Pair newPair) 
    {
	// Check is object is good
	if (newPair == null) return false;
	if (newPair == this) return false;

	// Check if the call comes from insertAfter of inserted object
	if (newPair.next() == this && prev_ == null) {
	    prev_ = newPair;
	    return true;
	}
	
	// Check if input is good
	if (newPair.next() != null) return false;
	if (newPair.prev() != null) return false;
	
	Pair prev = prev_;
	prev_ = newPair;
	if (prev != null && prev.next() == this)
	    prev.insertAfter(newPair);

	return newPair.insertAfter(this);
    }

    // Extracting pair after the current one
    public Pair extractAfter()
    {
	// Check if next exists
	if (next_ == null) return null;

	// Check if the call comes from extractBefore
	if (next_ != null && next_.prev() != this) {
	    next_ = null;
	    return this;
	}
	
	Pair ret = next_;
	next_ = next_.next();
	if (next_ != null && next_.prev() == ret) next_.extractBefore();

	ret.extractBefore();
	
	return ret;
    }

    // Extracting pair before the current one
    public Pair extractBefore()
    {
	// Check if next exists
	if (prev_ == null) return null;

	// Check if the call comes from extractAfter
	if (prev_ != null && prev_.next() != this) {
	    prev_ = null;
	    return this;
	}

	Pair ret = prev_;
	prev_ = prev_.prev();
	if (prev_ != null && prev_.next() == ret) prev_.extractAfter();

	ret.extractAfter();
	
	return ret;
    }

    public String toString()
    {
	String ret = "";
	if (obj1_ != null) ret += obj1_.toString();
	else               ret += "null";
	ret += " <=> ";
	if (obj2_ != null) ret += obj2_.toString();
	else               ret += "null";
	return ret;
    }
}
