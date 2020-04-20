package  belka.geom;

//--- Java imports ---
import java.io.*;

//--- Application imports ---
import belka.mol.*;
import Jama.*;

/**
 * The class calculates normal model for given structure from Elastic Network
 * Model (ENM), as described in 
 *
 * Atilgan AR, Durell SR, Jernigan RL, Demirel MC, Keskin O, Bahar I,
 * Anisotropy of fluctuation dynamics of proteins with an elastic network
 * model. Biophys J. 2001 Jan;80(1):505-15.
 * 
 * @author Alexej Abyzov
 */
public class NormalModeCalculator
{
    public NormalModeCalculator()
    {}

    // Hessian matrix
    private Matrix hessian_ = null;
    public  void printHessian(File file)
    {
	if (hessian_ == null) return;
	printMatrix(hessian_,file);
    }

    // Mode/eigenvalue matrix and values
    EigenvalueDecomposition evd_ = null;
    public  void printModes(File file)
    {
	if (evd_ == null) return;
	printMatrix(evd_.getV(),file);
    }

    // Array with residues used in calculations
    private Assembly[] res_ = null;

    private  void printMatrix(Matrix matr,File file)
    {
	if (file == null) { // Print to a screen
	    for (int i1 = 0;i1 < n_modes_;i1++) {
		StringWriter ret = new StringWriter();
		PrintWriter  wr  = wr  = new PrintWriter(ret);
		for (int i2 = 0;i2 < n_modes_;i2++) 
		    wr.printf("%10.5f",matr.get(i1,i2));
		wr.println();
		wr.close();
		System.out.print(ret.toString());
	    }
	} else { // Print to a screen
	    PrintWriter wr = null;
	    try                  { wr = new PrintWriter(file); }
	    catch (Exception ex) { 
		System.err.println("Cann't write to file '" + file.getName() +
				   "'.");
		return;
	    }
	    for (int i1 = 0;i1 < n_modes_;i1++) {
		for (int i2 = 0;i2 < n_modes_;i2++) 
		    wr.printf("%10.5f",matr.get(i1,i2));
		wr.println();
	    }
	    wr.close();
	}
    }

    // Number of modes
    private int n_modes_ = 0;
    public  int getNModes() { return n_modes_; }

    /**
     * The functions generates a hessian matrix for provided molecule and 
     * calculates its eigen values and vectors. Resides C-alpha carbons are
     * used as a representative points.
     *
     * @param mol molecule to calculate normal modes.
     * @param r_gamma value of gamma for rigid blocks (in unit of gamma for
     * between blocks)
     */
    public int calculateModes(Molecule mol,double r_gamma)
    {
	return calculateModes(mol,r_gamma,false);
    }

    /**
     * The functions generates a hessian matrix for provided molecule and 
     * calculates its eigen values and vectors. Resides C-alpha carbons are
     * used as a representative points.
     *
     * @param mol molecule to calculate normal modes.
     * @param r_gamma value of gamma for rigid blocks (in unit of gamma for
     * between blocks)
     * @param selected flag to indicate that only selected atoms must be used.
     */
    public int calculateModes(Molecule mol,double r_gamma,boolean selected)
    {
	if (mol == null) {
	    System.err.println("No molecule found.");
	    return 0;
	}

	// Calculating number of atoms/residues
	int n_res = 0;
	for (Chain c = mol.chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		Atom a = s.getMainAtom();
		if (a == null) continue;
		if (selected && !a.isSelected()) continue;
		n_res++;
	    }

	// Storing atoms
	res_ = new Assembly[n_res];
	n_res = 0;
	for (Chain c = mol.chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		Atom a = s.getMainAtom();
		if (a == null) continue;
		if (selected && !a.isSelected()) continue;
		res_[n_res++] = s;
	    }

	if (n_res <= 0) {
	    if (selected) System.err.println("No selected residues found.");
	    else          System.err.println("No residues found.");
	    return 0;
	}

	// Allocating hessian matrix
	n_modes_ = 3*n_res;
	hessian_ = new Matrix(n_modes_,n_modes_);

	// Filling hessian matrix
	double[][] hess = hessian_.getArray();
	for (int i1 = 0;i1 < n_res;i1++) {
	    Atom a1 = res_[i1].getMainAtom();
	    double x1 = a1.getX();
	    double y1 = a1.getY();
	    double z1 = a1.getZ();
	    int ind1_1 = 3*i1;
	    int ind1_2 = ind1_1 + 1;
	    int ind1_3 = ind1_2 + 1;
	    for (int i2 = i1 + 1;i2 < n_res;i2++) {
		Atom a2 = res_[i2].getMainAtom();
		double dx = x1 - a2.getX(), dx2 = dx*dx;
		double dy = y1 - a2.getY(), dy2 = dy*dy;
		double dz = z1 - a2.getZ(), dz2 = dz*dz;
		double dxy = dx*dy, dxz = dx*dz, dyz = dy*dz;
		double d2 = dx2 + dy2 + dz2;
		if (d2 > 225) continue;

		double gamma = 1.0;
		if (res_[i1].getGroupId() == res_[i2].getGroupId())
		    gamma = r_gamma;
		double inv_d = 1./d2;
		int ind2_1 = 3*i2;
		int ind2_2 = ind2_1 + 1;
		int ind2_3 = ind2_2 + 1;

		// First line
		hess[ind1_1][ind2_1] = hess[ind2_1][ind1_1] = -gamma*dx2*inv_d;
		hess[ind1_1][ind2_2] = hess[ind2_1][ind1_2] = -gamma*dxy*inv_d;
		hess[ind1_1][ind2_3] = hess[ind2_1][ind1_3] = -gamma*dxz*inv_d;

		// Second line
		hess[ind1_2][ind2_1] = hess[ind2_2][ind1_1] = -gamma*dxy*inv_d;
		hess[ind1_2][ind2_2] = hess[ind2_2][ind1_2] = -gamma*dy2*inv_d;
		hess[ind1_2][ind2_3] = hess[ind2_2][ind1_3] = -gamma*dyz*inv_d;

		// Third line
		hess[ind1_3][ind2_1] = hess[ind2_3][ind1_1] = -gamma*dxz*inv_d;
		hess[ind1_3][ind2_2] = hess[ind2_3][ind1_2] = -gamma*dyz*inv_d;
		hess[ind1_3][ind2_3] = hess[ind2_3][ind1_3] = -gamma*dz2*inv_d;

		// First line
		hess[ind1_1][ind1_1] -= hess[ind1_1][ind2_1];
		hess[ind1_1][ind1_2] -= hess[ind1_1][ind2_2];
		hess[ind1_1][ind1_3] -= hess[ind1_1][ind2_3];

		// Second line
		hess[ind1_2][ind1_1] -= hess[ind1_2][ind2_1];
		hess[ind1_2][ind1_2] -= hess[ind1_2][ind2_2];
		hess[ind1_2][ind1_3] -= hess[ind1_2][ind2_3];

		// Third line
		hess[ind1_3][ind1_1] -= hess[ind1_3][ind2_1];
		hess[ind1_3][ind1_2] -= hess[ind1_3][ind2_2];
		hess[ind1_3][ind1_3] -= hess[ind1_3][ind2_3];

		// First line
		hess[ind2_1][ind2_1] -= hess[ind2_1][ind1_1];
		hess[ind2_1][ind2_2] -= hess[ind2_1][ind1_2];
		hess[ind2_1][ind2_3] -= hess[ind2_1][ind1_3];

		// Second line
		hess[ind2_2][ind2_1] -= hess[ind2_2][ind1_1];
		hess[ind2_2][ind2_2] -= hess[ind2_2][ind1_2];
		hess[ind2_2][ind2_3] -= hess[ind2_2][ind1_3];

		// Third line
		hess[ind2_3][ind2_1] -= hess[ind2_3][ind1_1];
		hess[ind2_3][ind2_2] -= hess[ind2_3][ind1_2];
		hess[ind2_3][ind2_3] -= hess[ind2_3][ind1_3];

	    }
	}

	evd_ = hessian_.eig();

	return n_modes_;
    }
}