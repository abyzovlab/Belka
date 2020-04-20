package belka.geom;

//--- Java imports ---
import java.lang.*;
import java.util.*;
import java.io.*;

//--- Application imports ---
import belka.mol.*;
import Jama.*;

/**
 * Objects of this class perform operations on structures to classify motions.
 * 
 * @author Alexej Abyzov
 */

public class Motioner
{
    // Pairs of atoms
    Pair   fPair_ = null, lPair_  = null;

    // Global rotation matrices and translation vectors for 
    // superimposing each rigid block.
    Matrix[] rotations_    = null;
    Matrix[] translations_ = null;

    /**
     * Object constructor. 
     *
     * @param chains1 chains for first conformation.
     * @param chains2 chains for second conformation.
     */
    public Motioner(Chain[] chains1,Chain[] chains2)
    {
	if (chains1 == null || chains2 == null) {
	    System.err.println("No chains given.");
	    return;
	}
	if (chains1.length != chains2.length) {
	    System.err.println("Different number of chains.");
	    return;
	}
	for (int c = 0;c < chains1.length;c++)
	    if (chains1[c] == null || chains2[c] == null) {
		System.err.println("Null chain found.");
		return;
	    }

	int nAligned = 0;
	for (int c = 0;c < chains1.length;c++) {
	    Chain c1 = chains1[c],c2 = chains2[c];
	    for (Assembly a1 = c1.assemblyList(), a2 = c2.assemblyList();
		 a1 != null && a2 != null;a1 = a1.next(), a2 = a2.next()) {

		boolean isGap = (a1.isGap() || a2.isGap());
		Pair newPair = new Pair(a1,a2,!isGap);
		if (a1.getGroupId() == a2.getGroupId())
		    newPair.setIntValue(a1.getGroupId());

		if (fPair_ == null) fPair_ = lPair_ = newPair;
		else if (lPair_.insertAfter(newPair)) lPair_ = newPair;
		if (!isGap) nAligned++;
	    }
	}

	if (nAligned < 3) {
	    System.err.println("Not enough aligned atoms in chains.");
	    return;
	}

	// Calculating number of rigid blocks
	int n_rigids = 0;
	for (Pair p = fPair_;p != null;p = p.next()) {
	    if (!p.isOfInterest()) continue;
	    if (p.getIntValue() > n_rigids) n_rigids = p.getIntValue();
	}

	// Allocating array for matrices
	rotations_      = new Matrix[n_rigids];
	translations_   = new Matrix[n_rigids];

	// Rotation and translation for each rigid
	Atom[] atoms1 = new Atom[nAligned];
	Atom[] atoms2 = new Atom[nAligned];
	Kabscher kb = new Kabscher();
	for (int r = 1;r <= n_rigids;r++) {
	    int index = 0;
	    for (Pair p = fPair_;p != null;p = p.next()) {
		if (!p.isOfInterest()) continue;
		if (p.getIntValue() != r) continue;
		Assembly ass1 = (Assembly)p.getObject1();
		Assembly ass2 = (Assembly)p.getObject2();
		Atom a1 = ass1.getMainAtom();
		Atom a2 = ass2.getMainAtom();
		if (a1 == null || a2 == null) continue;
		atoms1[index] = a1;
		atoms2[index] = a2;
		index++;
	    }
	    if (nAligned < 3) {
		System.err.println("Not enough aligned atoms in rigid #" +
				   r + ".");
		continue;
	    }
	    while (index < nAligned) { // Clearing remaining cells
		atoms1[index] = atoms2[index] = null;
		index++;
	    }
	    kb = new Kabscher();
	    kb.fit(atoms1,atoms2);
	    rotations_[r - 1]    = new Matrix(kb.getRotationCopy());
	    translations_[r - 1] = new Matrix(kb.getTranslationCopy(),
					      kb.getTranslationCopy().length);
	}
    }

    public void splitMotions()
    {
	// Calculating relative motions
	int n_rigids      = rotations_.length;
	double[][] angles = new double[n_rigids][n_rigids];
	double[][] norms  = new double[n_rigids][n_rigids];
	Matrix I = new Matrix(new double[][]{{1,0,0},{0,1,0},{0,0,1}});
	for (int r1 = 0;r1 < n_rigids;r1++) {
	    for (int r2 = 0;r2 < n_rigids;r2++) {
		if (r1 == r2) continue;
		double dx = 0,dy = 0,dz = 0; int n = 0;
		for (Pair p = fPair_;p != null;p = p.next()) {
		    if (!p.isOfInterest()) continue;
		    if (p.getIntValue() != r2 + 1) continue;
		    Assembly ass1 = (Assembly)p.getObject1();
		    Assembly ass2 = (Assembly)p.getObject2();
		    Atom a1 = ass1.getMainAtom();
		    Atom a2 = ass2.getMainAtom();
		    if (a1 == null || a2 == null) continue;
		    Matrix coor = new Matrix(new double[][]{{a2.getX()},
							    {a2.getY()},
							    {a2.getZ()}});
		    Matrix res =
			rotations_[r1].times(coor).plus(translations_[r1]);
		    dx += a1.getX() - res.get(0,0);
		    dy += a1.getY() - res.get(1,0);
		    dz += a1.getZ() - res.get(2,0);
		    n++;
		}
		Matrix rot   =
		    rotations_[r2].times(rotations_[r1].inverse());
		Matrix axis  = GeometryUtil.getRotationAxis(rot);
		double angle = GeometryUtil.getRotationAngle(rot,axis);
// 		Matrix trans =
// 		    translations_[r2].minus(rot.times(translations_[r1]));
		angles[r1][r2] = Math.abs(angle);
		norms[r1][r2]  = Math.sqrt(dx*dx + dy*dy + dz*dz)/n;
// 		System.out.println((r1 + 1) + " " + (r2 + 1) + " = " +
// 				   angles[r1][r2] + " " + norms[r1][r2]);
	    }
	}

	// Calculating possible relative motions
	boolean[][] possible = new boolean[n_rigids][n_rigids];
	for (int r = 0;r < n_rigids;r++) {
	    for (Pair p = fPair_;p != null;p = p.next()) {
		if (!p.isOfInterest()) continue;
		if (p.getIntValue() != r + 1) continue;
		Assembly ass1 = (Assembly)p.getObject1();
		Assembly ass2 = (Assembly)p.getObject2();
		while (ass1 != null) {
		    int id = ass1.getGroupId();
		    if (id != 0 && id != r + 1) {
			possible[r][id - 1] = possible[id - 1][r] = true;
			break;
		    }
		    ass1 = ass1.next();
		}
		while (ass2 != null) {
		    int id = ass2.getGroupId();
		    if (id != 0 && id != r) {
			possible[r][id - 1] = possible[id - 1][r] = true;
			break;
		    }
		    ass2 = ass2.next();
		}
	    }
	}

	// Assigning relaive motions
	int[] relative = new int[n_rigids];
	for (int r = 0;r < n_rigids;r++) relative[r] = -1;
	relative[0] = 0;
	int n_resolved = 1;
	while (n_resolved < n_rigids) {
	    int ind_move = -1,ind_fix = -1;
	    double min_displ = 1e+10;
	    for (int r1 = 0;r1 < n_rigids;r1++) {
		if (relative[r1] < 0) continue; // Already assigned
		for (int r2 = 0;r2 < n_rigids;r2++) {
		    if (relative[r2] >= 0) continue; // Not yet assigned
		    if (!possible[r1][r2]) continue;
		    if (norms[r1][r2] < min_displ) {
			min_displ = norms[r1][r2];
			ind_fix   = r1;
			ind_move  = r2;
		    }
		}
	    }
	    relative[ind_move] = ind_fix;
	    System.out.println((ind_fix + 1) + " " + (ind_move + 1));
	    n_resolved++;
	}
	
	if (1 > 0) return;
	
	boolean[] resolved = new boolean[n_rigids];
	resolved[0] = true;
	n_resolved = 1;
	while (n_resolved < n_rigids) {
	    double min_angle = 1e+10,min_norm = 1e+10;
	    int static_rid = -1,movable_rid = -1;
	    boolean ambiguous = false;
	    for (int r1 = 0;r1 < n_rigids;r1++) {
		if (!resolved[r1]) continue;
		for (int r2 = 0;r2 < n_rigids;r2++) {
		    if (resolved[r2]) continue;
		    if (angles[r1][r2] < min_angle &&
			norms[r1][r2]  < min_norm) {
			min_angle = angles[r1][r2];
			min_norm  = norms[r1][r2];
			static_rid  = r1;
			movable_rid = r2;
			ambiguous = false;
			System.out.println("Min " + (r1 + 1) + " " + (r2 + 1));
		    } else if (!(angles[r1][r2] > min_angle &&
				 norms[r1][r2]  > min_norm)) {
			ambiguous = true;
			System.out.println("In " + (r1 + 1) + " " + (r2 + 1));
			System.out.println(min_angle + " " + min_norm);
			System.out.println(angles[r1][r2] + " " + norms[r1][r2]);
		    }
		}
	    }
	    if (ambiguous) {
		System.out.println("Ambiguous");
		return;
	    }
	    if (static_rid >= 0 && movable_rid >= 0) {
		n_resolved++;
		resolved[movable_rid] = true;
		System.out.println((static_rid + 1) + " " + (movable_rid + 1));
	    }
	}
    }
    
    private class Move
    {
	private double angle_ = 0, displacement_ = 0;

	public Move(double angle,double displacement)
	{
	    angle_        = angle;
	    displacement_ = displacement;
	}

	public double getAngle()        { return angle_; }
	public double getDisplacement() { return displacement_; }

	public boolean isSmaller(Move other_move)
	{
	    if (angle_        < other_move.getAngle() &&
		displacement_ < other_move.getAngle()) return true;
	    return false;
	}

	public boolean isLarger(Move other_move)
	{
	    if (angle_        > other_move.getAngle() &&
		displacement_ > other_move.getAngle()) return true;
	    return false;
	}
    }
}