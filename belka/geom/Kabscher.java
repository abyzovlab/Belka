package  belka.geom;

//--- Application imports ---
import belka.mol.*;
import Jama.*;

/**
 * The implementation of the Kabsch algorith for least-square fitting of two
 * sets of points as describe in: <br>
 * 
 * W. Kabsch, A solution for the best rotation to relate two sets of vectors.
 * Acta Cryst. (1976). A32, 922-923. <br>
 * W. Kabsch, A discussion of the solution for the best rotation to relate two
 * sets of vectors.
 * Acta Cryst. (1978). A34, 827-828. <br>
 *
 * The code is adapted from the protein geometry library
 * http://geometry.molmovdb.org/
 *
 * @author Alexej Abyzov
 */
public class Kabscher
{
    public Kabscher()
    {
	rot_[0][0] = rot_[1][1] = rot_[2][2] = 1;
    }

    // Rotation matrix
    private double[][] rot_   = new double[3][3];
    /**
     * Returns rotation matrix.
     * 
     * @return rotation matrix.
     */
    public double[][] getRotation() { return rot_; }
    /**
     * Returns copy of rotation matrix.
     * 
     * @return copy of rotation matrix.
     */
    public double[][] getRotationCopy()
    {
	return new double[][]{{rot_[0][0],rot_[0][1],rot_[0][2]},
			      {rot_[1][0],rot_[1][1],rot_[1][2]},
			      {rot_[2][0],rot_[2][1],rot_[2][2]}};
    }

    // Translation
    private double[] trans_ = new double[3];
    /**
     * Returns translation vector.
     * 
     * @return translation vector.
     */
    public double[] getTranslation() { return trans_; }
    /**
     * Returns copy translation vector.
     * 
     * @return copy translation vector.
     */
    public double[] getTranslationCopy()
    {
	double[] ret = {trans_[0],trans_[1],trans_[2]};
	return ret;
    }

    // Rotation axis
    private double[] axis_ = new double[3];
    /**
     * Return the axis of rotation.
     *
     * @return the axis of rotation.
     */
    public double[] getAxis() { return axis_; }

    // Rotation angle
    private double angle_ = 0;
    /**
     * Return the angle of rotation.
     *
     * @return the angle of rotation.
     */
    public double getAngle() { return angle_; }

    // Ne
    private int ne_ = 0;
    /**
     * Returns number of fitted atoms.
     *
     * @return number of fitted atoms.
     */
    public int getNFitted() { return ne_; }

    // RMSD
    private double rmsd_ = -1;
    /**
     * Returns RMSD of fit.
     *
     * @return RMSD of fit.
     */
    public double getRMSD() { return rmsd_; }

    /**
     * The function performs least-square fit of C-alpha protein or
     * P-phosphate nucleotide atoms and returns RMSD of the fit. All aligned
     * atoms are used in the fit. Current alignment of the chains is used.
     *
     * @param c1 first chain.
     * @param c2 second chain.
     *
     * @return rmsd of the fit.
     */
    public double fitCA(Chain c1,Chain c2) { return fitCA(c1,c2,false); }

    /**
     * The function performs least-square fit of C-alpha protein or
     * P-phosphate nucleotide atoms and returns RMSD of the fit. All aligned
     * atoms are used in the fit. Current alignment of the chains is used.
     *
     * @param chains1 first set of chains.
     * @param chains2 second set of chains.
     *
     * @return rmsd of the fit.
     */
    public double fitCA(Chain[] chains1,Chain[] chains2)
    {
	return fitCA(chains1,chains2,false);
    }

    /**
     * The function performs least-square fit of C-alpha protein or
     * P-phosphate nucleotide atoms and returns RMSD of the fit. Selected or
     * all aligned atoms can be used in the fit. Current alignment of the
     * chains is used.
     *
     * @param chain1 first chain.
     * @param chain2 second chain.
     * @param selected flag to indicate that only selected atoms must be used.
     *
     * @return rmsd of the fit.
     */
    public double fitCA(Chain chain1,Chain chain2,boolean selected)
    {
	return fitCA(new Chain[]{chain1},new Chain[]{chain2},selected);
    }

    /**
     * The function performs least-square fit of C-alpha protein or
     * P-phosphate nucleotide atoms and returns RMSD of the fit. Selected or
     * all aligned atoms can be used in the fit. Current alignment of the
     * chains is used.
     *
     * @param chains1 first set of chains.
     * @param chains2 second set of chains.
     * @param selected flag to indicate that only selected atoms must be used.
     *
     * @return rmsd of the fit.
     */
    public double fitCA(Chain[] chains1,Chain[] chains2,boolean selected)
    {
	ne_   =  0;
	rmsd_ = -1;
	if (chains1 == null || chains2 == null) return rmsd_;
	if (chains1.length  != chains2.length)  return rmsd_;

	int n_atoms = 0;
	for (int i = 0;i < chains1.length;i++) {
	    Chain c1 = chains1[i];
	    Chain c2 = chains2[i];
	    if (c1 == null || c2 == null) continue;
	    for (Assembly ass1 = c1.assemblyList(),ass2 = c2.assemblyList();
		 ass1 != null && ass2 != null;
		 ass1 = ass1.next(),ass2 = ass2.next()) {
		Atom a1 = ass1.getMainAtom();
		Atom a2 = ass2.getMainAtom();
		if (a1 == null || a2 == null) continue;
		if (selected &&
		    (!a1.isSelected() || !a2.isSelected())) continue;
		n_atoms++;
	    }
	}

	Atom[] arr1 = new Atom[n_atoms];
	Atom[] arr2 = new Atom[n_atoms];
	if (n_atoms < 3) return rmsd_;
	n_atoms = 0;

	for (int i = 0;i < chains1.length;i++) {
	    Chain c1 = chains1[i];
	    Chain c2 = chains2[i];
	    if (c1 == null || c2 == null) continue;
	    for (Assembly ass1 = c1.assemblyList(),ass2 = c2.assemblyList();
		 ass1 != null && ass2 != null;
		 ass1 = ass1.next(),ass2 = ass2.next()) {
		Atom a1 = ass1.getMainAtom();
		Atom a2 = ass2.getMainAtom();
		if (a1 == null || a2 == null) continue;
		if (selected &&
		    (!a1.isSelected() || !a2.isSelected())) continue;
		arr1[n_atoms] = a1;
		arr2[n_atoms] = a2;
		n_atoms++;
	    }
	}

	return fit(arr1,arr2);
    }

    /**
     * The function performs least-square fit of given atoms. 
     *
     * @param atoms1 first array with atoms.
     * @param atoms2 second array with atoms.
     *
     * @return rmsd of the fit.
     */
    public double fit(Atom[] atoms1,Atom[] atoms2)
    {
	return fit(atoms1,atoms2,0,0,0);
    }

    /**
     * The function performs least-square fit of given atoms. 
     *
     * @param atoms1 first array with atoms.
     * @param atoms2 second array with atoms.
     * @param center_x location new center of coordiantes.
     * @param center_y location new center of coordiantes.
     * @param center_z location new center of coordiantes.
     *
     * @return rmsd of the fit.
     */
    public double fit(Atom[] atoms1,Atom[] atoms2,
		      double center_x,double center_y,double center_z)
    {
	ne_   =  0;
	rmsd_ = -1;
	if (atoms1 == null || atoms2 == null) return rmsd_;
	if (atoms1.length  != atoms2.length)  return rmsd_;

	// Swapping
	Atom[] tmp = atoms2;
	atoms2 = atoms1;
	atoms1 = tmp;

	double[] sx = new double[3];
	double[] sy = new double[3];
 	double sx2 = 0, sy2 = 0;
	double[][] sxy = new double[3][3];

	for (int i = 0;i < atoms1.length; i++) {
	    Atom a1 = atoms1[i];
	    if (a1 == null) continue;
	    double x1 = a1.getX() - center_x;
	    double y1 = a1.getY() - center_y;
	    double z1 = a1.getZ() - center_z;
	    sx[0] += x1; sx[1] += y1; sx[2] += z1;
	    sx2   += x1*x1 + y1*y1 + z1*z1;
	    Atom a2 = atoms2[i];
	    if (a2 == null) continue;
	    ne_++;
	    double x2 = a2.getX() - center_x;
	    double y2 = a2.getY() - center_y;
	    double z2 = a2.getZ() - center_z;
	    sy[0] += x2; sy[1] += y2; sy[2] += z2;
	    sy2   += x2*x2 + y2*y2 + z2*z2;

	    sxy[0][0] += x1*x2; sxy[0][1] += x1*y2; sxy[0][2] += x1*z2;
	    sxy[1][0] += y1*x2; sxy[1][1] += y1*y2; sxy[1][2] += y1*z2;
	    sxy[2][0] += z1*x2; sxy[2][1] += z1*y2; sxy[2][2] += z1*z2;
	}

	double inpts = 1./ne_;
	double e0 = (sx2 - (sx[0]*sx[0] + sx[1]*sx[1] + sx[2]*sx[2])*inpts +
		     sy2 - (sy[0]*sy[0] + sy[1]*sy[1] + sy[2]*sy[2])*inpts)*inpts;

	double[][] r = new double[3][3];
	for (int i = 0; i < 3; i++)
	    for (int j = 0; j < 3; j++)
		r[i][j] = (sxy[i][j] - sx[i]*sy[j]*inpts)*inpts;

	double[] rr = new double[6];
	for (int m = 0, j = 0; j < 3; j++)
	    for (int i = 0; i <= j; i++,m++)
		rr[m] = r[i][0]*r[j][0] + r[i][1]*r[j][1] + r[i][2]*r[j][2];

	double det = r[0][0] * (r[1][1]*r[2][2] - r[2][1]*r[1][2])
	    - r[1][0] * (r[0][1]*r[2][2] - r[2][1]*r[0][2])
	    + r[2][0] * (r[0][1]*r[1][2] - r[1][1]*r[0][2]);

	double spur = (rr[0]+rr[2]+rr[5])/3.0;
	double cof  = (rr[2]*rr[5] - rr[4]*rr[4] + rr[0]*rr[5] -
		       rr[3]*rr[3] + rr[0]*rr[2] - rr[1]*rr[1])/3.0;

	double[] e = new double[3];
 	int sw = solve(e,e0,det,spur,cof);
	matrix(sw,e,rr,r,sx,sy,ne_);
	
 	double d = sqrtabs(e[2]);
 	if (det < 0) d = -d;
 	d += sqrtabs(e[1]) + sqrtabs(e[0]);
 	d = e0 - 2*d;
	rmsd_ = sqrtabs(d);

	if (rot_[0][0] <=  1 && rot_[0][1] <=  1 && rot_[0][2] <=  1 &&
	    rot_[1][0] <=  1 && rot_[1][1] <=  1 && rot_[1][2] <=  1 &&
	    rot_[2][0] <=  1 && rot_[2][1] <=  1 && rot_[2][2] <=  1 &&
	    rot_[0][0] >= -1 && rot_[0][1] >= -1 && rot_[0][2] >= -1 &&
	    rot_[1][0] >= -1 && rot_[1][1] >= -1 && rot_[1][2] >= -1 &&
	    rot_[2][0] >= -1 && rot_[2][1] >= -1 && rot_[2][2] >= -1) {

	    // Calculating axis
	    EigenvalueDecomposition eig = (new Matrix(rot_)).eig();
	    double[] eigen_values  = eig.getRealEigenvalues();
	    int index = 0;
	    double delta = Math.abs(1 - eigen_values[0]);
	    for (int i = 1;i < eigen_values.length;i++) {
		double new_delta = Math.abs(1 - eigen_values[i]);
		if (new_delta < delta) {
		    index = i;
		    delta = new_delta;
		}
	    }
	    axis_ = eig.getV().getMatrix(0,rot_.length - 1,index,index).
		getColumnPackedCopy();

	    // Calculating angle
	    double xs = rot_[2][1] - rot_[1][2];
	    double ys = rot_[0][2] - rot_[2][0];
	    double zs = rot_[1][0] - rot_[0][1];
	    double sin = 0.5*(axis_[0]*xs + axis_[1]*ys + axis_[2]*zs);
	    angle_ = Math.asin(sin);
	} else {
	    axis_[0] = axis_[1] = axis_[2] = 0;
	    angle_ = 0;
	}

 	return rmsd_;
    }

    void matrix(int sw,double[] e,double[] rr,double[][] r,
		double[] sx,double[] sy,int npts)
    {
	//double d, h, p;
	double[][] a = new double[3][3];
	double[][] b = new double[3][3];

	if (sw == 4) { // Case of three identical roots
	    for (int i = 0; i < 3; i++)
		for (int j = 0; j < 3; j++)
		    if (i == j) a[i][j] = 1.0;
		    else        a[i][j] = 0.0;
	} else {
	    int m, m1, m2, m3;
	    if (sw == 1) { // Case of three distinct roots
		for (int l = 0;l < 2;l++) {
		    double d = e[l];
		    double[] ss = new double[6];
		    ss[0] = (d - rr[2])*(d - rr[5]) - rr[4]*rr[4];
		    ss[1] = (d - rr[5])*rr[1] + rr[3]*rr[4];
		    ss[2] = (d - rr[0])*(d - rr[5]) - rr[3]*rr[3];
		    ss[3] = (d - rr[2])*rr[3] + rr[1]*rr[4];
		    ss[4] = (d - rr[0])*rr[4] + rr[1]*rr[3];
		    ss[5] = (d - rr[0])*(d - rr[2]) - rr[1]*rr[1];
		    if (Math.abs(ss[0]) >= Math.abs(ss[2])) {
			if (Math.abs(ss[0]) >= Math.abs(ss[5])) {
			    a[0][l] = ss[0]; a[1][l] = ss[1]; a[2][l] = ss[3];
			} else {
			    a[0][l] = ss[3]; a[1][l] = ss[4]; a[2][l] = ss[5];
			}
		    } else if (Math.abs(ss[2]) >= Math.abs(ss[5])) {
			a[0][l] = ss[1]; a[1][l] = ss[2]; a[2][l] = ss[4];
		    } else {
			a[0][l] = ss[3]; a[1][l] = ss[4]; a[2][l] = ss[5];
		    }
		    d = Math.sqrt(a[0][l]*a[0][l] + a[1][l]*a[1][l] +
				  a[2][l]*a[2][l]);
		    a[0][l] /= d; a[1][l] /= d; a[2][l] /= d;
		}
		m1 = 2; m2 = 0; m3 = 1;
	    } else { // Cases of two distinct roots
		if (sw == 2) {
		    m = 0; m1 = 2; m2 = 0; m3 = 1;
		} else {
		    m = 2; m1 = 0; m2 = 1; m3 = 2;
		}
		double h = e[2];
		a[0][1] = 1.0; a[1][1] = 1.0; a[2][1] = 1.0;
		if (Math.abs(rr[0] - h) > Math.abs(rr[2] - h)) {
		    if (Math.abs(rr[0] - h) > Math.abs(rr[5] - h)) {
			a[0][m] = rr[0] - h; a[1][m] = rr[1]; a[2][m] = rr[3];
			double p = -(rr[0] + rr[1] + rr[3] - h);
			a[0][1] = p/a[0][m];
		    } else {
			a[0][m] = rr[3]; a[1][m] = rr[4]; a[2][m] = rr[5] - h;
			double p = -(rr[3] + rr[4] + rr[5] - h);
			a[2][1] = p/a[2][m];
		    }
		} else {
		    if (Math.abs(rr[2] - h) > Math.abs(rr[5] - h)) {
			a[0][m] = rr[1]; a[1][m] = rr[2] - h; a[2][m] = rr[4];
			double p = -(rr[1] + rr[2] + rr[4] - h);
			a[1][1] = p/a[1][m];
		    } else {
			a[0][m] = rr[3]; a[1][m] = rr[4]; a[2][m] = rr[5] - h;
			double p = -(rr[3] + rr[4] + rr[5] - h);
			a[2][1] = p/a[2][m];
		    }
		}
		double d = Math.sqrt(a[0][m]*a[0][m] + a[1][m]*a[1][m] +
				     a[2][m]*a[2][m]);
		double p = Math.sqrt(a[0][1]*a[0][1] + a[1][1]*a[1][1] +
				     a[2][1]*a[2][1]);
		for (int i = 0; i < 3;i++) {
		    a[i][1] /= p;
		    a[i][m] /= d;
		}
	    }
	    // Common for either two or three distinct roots
	    a[0][m1] = a[1][m2]*a[2][m3] - a[1][m3]*a[2][m2];
	    a[1][m1] = a[2][m2]*a[0][m3] - a[2][m3]*a[0][m2];
	    a[2][m1] = a[0][m2]*a[1][m3] - a[0][m3]*a[1][m2];
	}

	for (int l = 0;l < 2;l++) {
	    double d = 0;
	    for (int i = 0; i < 3; i++) {
		b[i][l] = r[0][i]*a[0][l] + r[1][i]*a[1][l] + r[2][i]*a[2][l];
		d += b[i][l]*b[i][l];
	    }
	    d = Math.sqrt(d);
	    if (d == 0) {
		System.err.println("WARNING: Degenerate rotation matrix.");
		for (int i = 0;i < 3;i++) {
		    trans_[i] = 0;
		    for (int k = 0;k < 3;k++) {
			if (i == k) rot_[i][k] = 1.0;
			else        rot_[i][k] = 0.0;
		    }
		}
		return;
	    }


	    for (int i = 0;i < 3;i++) b[i][l] /= d;
	}
	b[0][2] = b[1][0]*b[2][1] - b[1][1]*b[2][0];
	b[1][2] = b[2][0]*b[0][1] - b[2][1]*b[0][0];
	b[2][2] = b[0][0]*b[1][1] - b[0][1]*b[1][0];

	// Calculate rotation matrix
	for (int i = 0; i < 3;i++)
	    for (int j = 0; j < 3; j++)
		rot_[i][j] = b[i][0]*a[j][0] + b[i][1]*a[j][1] +
		    b[i][2]*a[j][2];
	// Calculate translation vector
	for (int i = 0; i < 3;i++)
	    trans_[i] = (sy[i] - rot_[i][0]*sx[0] - rot_[i][1]*sx[1] -
			 rot_[i][2]*sx[2])/npts;
    }

    private int solve(double[] e,double e0,double det,double spur,double cof)
    {
	final double epsilon = 1e-20;
	final double sqrt3   = 1.7320508075688772;

	det *= det;
	double d = spur*spur;
	double h = d - cof;
	double g = spur*(cof*1.5 - d) - det*0.5;
	if (h > d*epsilon) {
	    double sqrth = sqrtabs(h);
	    d = -g/(h*sqrth);
	    if (d > 1 - epsilon) {// Two identical roots
		e[0] = spur + 2*sqrth;
		e[2] = e[1] = spur - sqrth;
		return 2;
	    } else if (d < -1 + epsilon) { // Two identical roots
		e[1] = e[0] = spur + sqrth;
		e[2] = spur - 2*sqrth;
		if (e[2] < 0) e[2] = 0;
		return 3;
	    } else { // Three distinct roots
		d = Math.acos(d)/3.0;
		double cth = sqrth*Math.cos(d), sth = sqrth*sqrt3*Math.sin(d);
		e[0] = spur + 2*cth;
		e[1] = spur - cth + sth;
		e[2] = spur - cth - sth;
		if (e[2] < 0) e[2] = 0;
		return 1;
	    }
	} else { // Three identical roots
	    e[0] = e[1] = e[2] = spur;
	    return 4;
	}
    }

    private double sqrtabs(double val)
    {
	if (val > 0) return Math.sqrt( val);
	else         return Math.sqrt(-val);
    }
}