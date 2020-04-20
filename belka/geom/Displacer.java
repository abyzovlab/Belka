package  belka.geom;

//--- Java imports ---
import java.io.*;

//--- Application imports ---
import belka.mol.*;
import Jama.*;

/**
 * The class calculates displacement vector between two given confomartions
 * of a protein. Protein sequences are supposed to be aligned. Rigid blocks
 * are supposed to be assigned.
 * 
 * @author Alexej Abyzov
 */
public class Displacer
{
    // Chains
    Chain chain1_ = null, chain2_ = null;
    Pair   fPair_ = null, lPair_  = null;

    // Local rotation matrices and translation vectors, i.e. for 
    // superimposing each rigid block. Element zero stores transformation for
    // global superposition.
    Matrix[] rotations_    = null;
    Matrix[] translations_ = null;

    // Array with indexes for relative movements
    int[] relative_index_ = null;
    
    // First molecule center of mass
    double center_x_ = 0, center_y_ = 0,center_z_ = 0;

    /**
     * Object constructor. 
     *
     * @param chain1 chain with first conformation.
     * @param chain2 chain with second conformation.
     */
    public Displacer(Chain chain1,Chain chain2)
    {
	if (chain1 == null || chain2 == null) return;

	short nAligned = 0;
	for (Assembly a1 = chain1.assemblyList(), a2 = chain2.assemblyList();
	     a1 != null && a2 != null;a1 = a1.next(), a2 = a2.next()) {

	    boolean isGap = (a1.isGap() || a2.isGap());
	    Pair newPair = new Pair(a1,a2,!isGap);
	    if (a1.getGroupId() == a2.getGroupId())
		newPair.setIntValue(a1.getGroupId());

	    if (fPair_ == null) fPair_ = lPair_ = newPair;
	    else if (lPair_.insertAfter(newPair)) {
		Assembly a1to1 = (Assembly)lPair_.getObject1();
		Assembly a2to2 = (Assembly)lPair_.getObject2();
		if (a1.isConnectedTo(a1to1) && a2.isConnectedTo(a2to2)) {
		    lPair_.addConnection(newPair);
		    newPair.addConnection(lPair_);
		}
		lPair_ = newPair;
	    }
	    if (!isGap) nAligned++;
	}

	if (nAligned < 3) {
	    System.err.println("Not enough aligned atoms in chains.");
	    return;
	}

	// Storing chains
	chain1_ = chain1;
	chain2_ = chain2;

	// Calculating number of rigid blocks
	int n_rigids = 0;
	for (Pair p = fPair_;p != null;p = p.next()) {
	    if (!p.isOfInterest()) continue;
	    if (p.getIntValue() > n_rigids) n_rigids = p.getIntValue();
	}

	// Allocating array for matrices
	rotations_      = new Matrix[n_rigids + 1];
	translations_   = new Matrix[n_rigids + 1];
	relative_index_ = new int[n_rigids + 1];

	// Rotation and translation for global superposition
	Atom[] atoms1 = new Atom[nAligned];
	Atom[] atoms2 = new Atom[nAligned];
	int index = 0;
	for (Pair p = fPair_;p != null;p = p.next()) {
	    if (!p.isOfInterest()) continue;
	    Assembly ass1 = (Assembly)p.getObject1();
	    Assembly ass2 = (Assembly)p.getObject2();
	    Atom a1 = ass1.getMainAtom();
	    Atom a2 = ass2.getMainAtom();
	    if (a1 == null || a2 == null) continue;
	    atoms1[index] = a1;
	    atoms2[index] = a2;
	    index++;
	    center_x_ += a1.getX();
	    center_y_ += a1.getY();
	    center_z_ += a1.getZ();
	}
	if (index == 0) return;
	center_x_ /= index;
	center_y_ /= index;
	center_z_ /= index;

	Kabscher kb = new Kabscher();
	kb.fit(atoms1,atoms2,center_x_,center_y_,center_z_);
	rotations_[0]    = new Matrix(kb.getRotation());
	translations_[0] = new Matrix(kb.getTranslation(),
				      kb.getTranslation().length);

	// Rotation and translation for local superpositions
	for (int r = 1;r <= n_rigids;r++) {
	    index = 0;
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
	    kb.fit(atoms1,atoms2,center_x_,center_y_,center_z_);
	    rotations_[r]    = new Matrix(kb.getRotationCopy());
	    translations_[r] = new Matrix(kb.getTranslationCopy(),
					  kb.getTranslationCopy().length);
	}
    }

    /**
     * Returns string with values of displacement for each CA atom. The 
     * displacement is the vector between initial and final comformations.
     *
     * @return string with values of displacement for each CA atom.
     */
    public StringBuffer print()
    {
	if (rotations_ == null || translations_ == null)
	    return new StringBuffer();

	Matrix rot   = rotations_[0];
	Matrix trans = translations_[0];
	if (rot == null || trans == null) {
	    System.err.println("No global tranformation found.");
	    return new StringBuffer();
	}
	
	// Calculating displacements
	double r00 = rot.get(0,0),r01 = rot.get(0,1),r02 = rot.get(0,2);
	double r10 = rot.get(1,0),r11 = rot.get(1,1),r12 = rot.get(1,2);
	double r20 = rot.get(2,0),r21 = rot.get(2,1),r22 = rot.get(2,2);
	double tr0 = trans.get(0,0),tr1 = trans.get(1,0),tr2 = trans.get(2,0);
	StringWriter ret = new StringWriter();
	PrintWriter  pw  = new PrintWriter(ret);
	for (Pair p = fPair_;p != null;p = p.next()) {
	    if (!p.isOfInterest()) continue;
	    
	    Assembly ass1 = (Assembly)p.getObject1();
	    Assembly ass2 = (Assembly)p.getObject2();
	    Atom a1 = ass1.getMainAtom();
	    Atom a2 = ass2.getMainAtom();
	    if (a1 == null || a2 == null) continue;
	    double x  = a1.getX() - center_x_, x2 = a2.getX() - center_x_;
	    double y  = a1.getY() - center_y_, y2 = a2.getY() - center_y_;
	    double z  = a1.getZ() - center_z_, z2 = a2.getZ() - center_z_;
	    
	    double x_displ = r00*x2 + r01*y2 + r02*z2 +	tr0 - x;
	    double y_displ = r10*x2 + r11*y2 + r12*z2 +	tr1 - y;
	    double z_displ = r20*x2 + r21*y2 + r22*z2 +	tr2 - z;
	    pw.format("%8.4f ",x_displ);
	    pw.format("%8.4f ",y_displ);
	    pw.format("%8.4f ",z_displ);
	}
	pw.println();
	pw.flush();
	pw.close();
	return ret.getBuffer();
    } 
   
    /**
     * Returns a string with values of displacement for each CA atom. The 
     * diplacement is a vector between initial and interpolated 
     * by angle structures. Amount of interpolation is controlled by parameter
     * fraction. The interpolation is done for each rigid block.
     *
     * @param staticBlock id of static (not moving) block.
     * @param fraction value of interpolation [0,1].
     *
     * @return string with values of displacement for each CA atom.
     */
    public StringBuffer printInfinitesimal(int staticBlock,double fraction,
					   boolean screw)
    {
	StringWriter ret = new StringWriter();
	if (rotations_ == null || translations_ == null)
	    return ret.getBuffer();

	if (fraction < 0 || fraction > 1) {
	    System.err.println("Interpolation fraction should be between " +
			       "0 and 1.");
	    return ret.getBuffer();
	}
	
	int n_rigids = rotations_.length - 1;
	if (staticBlock < 0 || staticBlock > n_rigids) {
	    System.err.println("Wrong static block '" + staticBlock + "'.");
	    return ret.getBuffer();
	}

	// Array with global fractional transformation for each rigid block
	Matrix[] gf_rotations    = new Matrix[n_rigids + 1];
	Matrix[] gf_translations = new Matrix[n_rigids + 1];
	Matrix[] gf_shift        = new Matrix[n_rigids + 1];
	gf_rotations[0]    = rotations_[0];
	gf_translations[0] = translations_[0];
	gf_shift[0]        = new Matrix(new double[][]{{0},
						       {0},
						       {0}});

	for (int r = 1;r <= n_rigids;r++) {

	    Matrix r_rot    = rotations_[staticBlock];
	    Matrix r_trans  = translations_[staticBlock];
	    if (r_rot == null || r_trans == null) {
		System.err.println("No reference transformation found.");
		return ret.getBuffer();
	    }

	    Matrix b_rot   = rotations_[r];
	    Matrix b_trans = translations_[r];
	    if (b_rot == null || b_trans == null) {
		System.err.println("No body specific transformation found.");
		return ret.getBuffer();
	    }

 	    // Transformation from reference to body specific,
	    // i.e. local transformation
	    Matrix l_rot   = b_rot.times(r_rot.transpose());
	    Matrix l_trans = b_trans.minus(l_rot.times(r_trans));

	    Matrix shift = new Matrix(new double[][]{{0,0,0},
						     {0,0,0},
						     {0,0,0}});

	    // Calculating fraction of local transformation
	    // Sign in front of fraction should be negative since
	    // l_rot and l_trans are calculated for second molecule
	    // and we have to apply fractional transformation to the first one
	    Matrix f_rot   = rotationFraction(l_rot,-fraction);
	    Matrix f_trans = l_trans.times(-fraction);

	    if (screw) {
		Matrix rot_axis    = GeometryUtil.getRotationAxis(l_rot);
		double magn = l_trans.transpose().times(rot_axis).trace();
		Matrix l_trans_par = rot_axis.times(magn);
		Matrix l_trans_per = l_trans.minus(l_trans_par);
		Matrix I = new Matrix(new double[][]{{1,0,0},
						     {0,1,0},
						     {0,0,1}});
		shift   = I.minus(l_rot.transpose()).times(l_trans_per);
		f_trans = l_trans_par.times(-fraction);
	    }

	    // Global fractional transformation
	    gf_rotations[r]    = f_rot;
	    gf_translations[r] = f_trans;
	    gf_shift[r]        = shift;
	}

	return print(gf_rotations,gf_translations,gf_shift,fraction);
    }

    // Printing (infinitesimal) displacement from interpolation 
    private StringBuffer print(Matrix[] rs,Matrix ts[],Matrix ss[],
			       double fraction)
    {
	StringWriter ret = new StringWriter();
	PrintWriter  pw  = new PrintWriter(ret);
	for (Pair p = fPair_;p != null;p = p.next()) {
	    if (!p.isOfInterest()) continue;
	    Assembly ass1 = (Assembly)p.getObject1();
	    Assembly ass2 = (Assembly)p.getObject2();
	    Atom a1 = ass1.getMainAtom();
	    Atom a2 = ass2.getMainAtom();
	    if (a1 == null || a2 == null) continue;
	    double x = a1.getX() - center_x_;
	    double y = a1.getY() - center_y_;
	    double z = a1.getZ() - center_z_;
	    double x_displ, y_displ, z_displ;
	    int r = p.getIntValue();
	    if (r <= 0 || rs[r] == null || ts[r] == null) {
		Matrix rot = rs[0], trans = ts[0];
		double x2 = a2.getX() - center_x_ ;
		double y2 = a2.getY() - center_y_;
		double z2 = a2.getZ() - center_z_;
		x_displ = rot.get(0,0)*x2 + rot.get(0,1)*y2 + rot.get(0,2)*z2 +
		    trans.get(0,0) - x;
		y_displ = rot.get(1,0)*x2 + rot.get(1,1)*y2 + rot.get(1,2)*z2 +
		    trans.get(1,0) - y;
		z_displ = rot.get(2,0)*x2 + rot.get(2,1)*y2 + rot.get(2,2)*z2 +
		    trans.get(2,0) - z;
		x_displ *= fraction;
		y_displ *= fraction;
		z_displ *= fraction;
	    } else {
		Matrix rot = rs[r], trans = ts[r], shift = ts[r];
		x -= shift.get(0,0);
		y -= shift.get(1,0);
		z -= shift.get(2,0);
		x_displ = rot.get(0,0)*x + rot.get(0,1)*y + rot.get(0,2)*z +
		    trans.get(0,0) - x;
		y_displ = rot.get(1,0)*x + rot.get(1,1)*y + rot.get(1,2)*z +
		    trans.get(1,0) - y;
		z_displ = rot.get(2,0)*x + rot.get(2,1)*y + rot.get(2,2)*z +
		    trans.get(2,0) - z;
	    }
	    pw.format("%8.4f ",x_displ);
	    pw.format("%8.4f ",y_displ);
	    pw.format("%8.4f ",z_displ);
	}
	pw.println();
	pw.flush();
	pw.close();
	return ret.getBuffer();
    }

    // The function calculates only fraction of transformaiton
    private Matrix rotationFraction(Matrix rot,double fraction)
    {
	Matrix axis  = GeometryUtil.getRotationAxis(rot);
	double angle = GeometryUtil.getRotationAngle(rot,axis);

	// Fractional local interpolation
	double f_angle = fraction*angle;
	return calcRotationMatrix(axis,f_angle);
    }
    
    private Matrix calcRotationMatrix(Matrix vector,double angle)
    {
	double x   = vector.get(0,0);
	double y   = vector.get(1,0);
	double z   = vector.get(2,0);
	double cos = Math.cos(angle), sin = Math.sin(angle), C = 1 - cos;
	double xs  = x*sin, ys  = y*sin, zs  = z*sin;
	double xC  = x*C,   yC  = y*C,   zC  = z*C;
	double xyC = x*yC,  yzC = y*zC,  zxC = z*xC;
	return new Matrix(new double[][]{{x*xC + cos,xyC  - zs, zxC + ys},
					 {xyC  + zs, y*yC + cos,yzC - xs},
					 {zxC  - ys, yzC  + xs, z*zC + cos}});
    }
}