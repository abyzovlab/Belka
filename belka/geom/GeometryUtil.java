package  belka.geom;

//--- Java imports ---
import java.io.*;

//--- Application imports ---
import belka.mol.*;
import Jama.*;

/**
 * The class does simple geometric calculations.
 * 
 * @author Alexej Abyzov
 */
public class GeometryUtil
{
    /**
     * Function transforms vector by given rotation matrix and translation
     * vectory.
     *
     * @param rot rotation matrix.
     * @param trans translation vector.
     * @param vect vector to be transformed.
     * @return resulting vector.
     */ 
    public static double[] transform(double[][] rot,double[] trans,
				     double[] vect)
    {
	if (vect == null) return null;

	double[] ret = vect;

	if (rot != null) ret = multiply(rot,vect);
	if (ret == null) return null;
	if (trans != null) ret = summ(ret,trans);

	return ret;
    }

    /**
     * Function substracts two given vectors.
     *
     * @param vect1 first vector.
     * @param vect2 second vector.
     * @return resulting vector.
     */ 
    public static double[] substract(double[] vect1,double[] vect2)
    {
	if (vect1 == null || vect2 == null) return null;
	int n = vect1.length;
	if (n != vect2.length) {
	    System.err.println("Vectors have incompartible dimentions.");
	    return null;
	}
	double[] ret = new double[n];
	for (int i = 0;i < n;i++) ret[i] = vect1[i] - vect2[i];
	return ret;
    }

    /**
     * Function substracts two given matrices.
     *
     * @param matrix1 first matrix.
     * @param matrix2 second matrix.
     * @return resulting matrix.
     */ 
    public static double[][] substract(double[][] matrix1,double[][] matrix2)
    {
	if (matrix1 == null || matrix2 == null) return null;
	int n1 = matrix1.length;
	int n2 = matrix1[0].length;
	if (n1 != matrix2.length || n2 != matrix2[0].length) {
	    System.err.println("Matrices have incompartible dimentions.");
	    return null;
	}
	double[][] ret = new double[n1][n2];
	for (int i1 = 0;i1 < n1;i1++)
	    for (int i2 = 0;i2 < n2;i2++)
		ret[i1][i2] = matrix1[i1][i2] - matrix2[i1][i2];
	return ret;
    }

    /**
     * Function summs two given vectors.
     *
     * @param vect1 first vector.
     * @param vect2 second vector.
     * @return resulting vector.
     */ 
    public static double[] summ(double[] vect1,double[] vect2)
    {
	if (vect1 == null || vect2 == null) return null;
	int n = vect1.length;
	if (n != vect2.length) {
	    System.err.println("Vectors have incompartible dimentions.");
	    return null;
	}
	double[] ret = new double[n];
	for (int i = 0;i < n;i++) ret[i] = vect1[i] + vect2[i];
	return ret;
    }

    /**
     * Function multiplies two given matrices.
     *
     * @param matrix1 first matrix.
     * @param matrix2 second matrix.
     * @return product of the two matrices.
     */ 
    public static double[][] multiply(double[][] matrix1,double[][] matrix2)
    {
	if (matrix1 == null || matrix2 == null) return null;
	int n1 = matrix1.length;
	int n2 = matrix1[0].length;
	if (n1 != matrix2[0].length || n2 != matrix2.length) {
	    System.err.println("Matrices have incompartible dimentions.");
	    return null;
	}
	double[][] ret = new double[n1][n1];
	for (int i1 = 0;i1 < n1;i1++)
	    for (int i2 = 0;i2 < n1;i2++)
		for (int j = 0;j < n2;j++)
		    ret[i1][i2] += matrix1[i1][j]*matrix2[j][i2];
	return ret;
    }

    /**
     * Function multiplies given matrix and vector.
     *
     * @param matrix matrix.
     * @param vect vector.
     * @return product of the matrix and vector.
     */ 
    public static double[] multiply(double[][] matrix,double[] vect)
    {
	if (matrix == null || vect == null) return null;
	int n1 = matrix.length;
	int n2 = matrix[0].length;
	if (n2 != vect.length) {
	    System.err.println("Matrix and vector have incompartible " +
			       "dimentions.");
	    return null;
	}
	double[] ret = new double[n1];
	for (int i = 0;i < n1;i++)
	    for (int j = 0;j < n2;j++)
		ret[i] += matrix[i][j]*vect[j];
	return ret;
    }

    /**
     * Function multiplies given matrix and vector.
     *
     * @param vect vector.
     * @param matrix matrix.
     * @return product of the matrix and vector.
     */ 
    public static double[] multiply(double[] vect,double[][] matrix)
    {
	if (vect == null || matrix == null) return null;
	int n1 = matrix.length;
	int n2 = matrix[0].length;
	if (n1 != vect.length) {
	    System.err.println("Matrix and vector have incompartible " +
			       "dimentions.");
	    return null;
	}
	double[] ret = new double[n2];
	for (int i = 0;i < n2;i++)
	    for (int j = 0;j < n1;j++)
		ret[i] += vect[j]*matrix[j][i];
	return ret;
    }

    /**
     * Function inverts given matrix. Return inverted matrix.
     *
     * @param matrix matrix to be inverted
     * @return inverted matrix
     */ 
    public static double[][] invert(double[][] matrix)
    {
	if (matrix == null) return null;

	double[][] ret = null;	
	if (matrix.length == 1 && matrix[0].length == 1) { // 1x1
	    if (matrix[0][0] == 0)
		System.err.println("Can't invert zero value.");
	    else {
		ret = new double[1][1];
		ret[0][0] = 1./matrix[0][0];
	    }
	} else if (matrix.length == 2 && matrix[0].length == 2) { // 2x2
	    double det = matrix[0][0]*matrix[1][1] - matrix[0][1]*matrix[1][0];
	    if (det == 0)
		System.err.println("Can't invert matrix with zero " +
				   "determinant.");
	    else {
		double one_det = 1./det;
		ret = new double[2][2];
		ret[0][0] =  matrix[1][1]*one_det;
		ret[1][1] =  matrix[0][0]*one_det;
		ret[0][1] = -matrix[0][1]*one_det;
		ret[1][0] = -matrix[1][0]*one_det;
	    }
	} else if (matrix.length == 3 && matrix[0].length == 3) { // 3x3
	    double det = matrix[0][0]*matrix[1][1]*matrix[2][2] -
		matrix[0][0]*matrix[1][2]*matrix[2][1] -
		matrix[0][1]*matrix[1][0]*matrix[2][2] +
		matrix[0][1]*matrix[1][2]*matrix[2][0] +
		matrix[0][2]*matrix[1][0]*matrix[2][1] -
		matrix[0][2]*matrix[1][1]*matrix[2][0];
	    if (det == 0)
		System.err.println("Can't invert matrix with zero " +
				   "determinant.");
	    else {
		double one_det = 1./det;
		ret = new double[3][3];
		ret[0][0] = (matrix[1][1]*matrix[2][2] -
			     matrix[1][2]*matrix[2][1])*one_det;
		ret[0][1] = (matrix[0][2]*matrix[2][1] -
			     matrix[0][1]*matrix[2][2])*one_det;
		ret[0][2] = (matrix[0][1]*matrix[1][2] -
			     matrix[0][2]*matrix[1][1])*one_det;
		ret[1][0] = (matrix[1][2]*matrix[2][0] -
			     matrix[1][0]*matrix[2][2])*one_det;
		ret[1][1] = (matrix[0][0]*matrix[2][2] -
			     matrix[0][2]*matrix[2][0])*one_det;
		ret[1][2] = (matrix[0][2]*matrix[1][0] -
			     matrix[0][0]*matrix[1][2])*one_det;
		ret[2][0] = (matrix[1][0]*matrix[2][1] -
			     matrix[1][1]*matrix[2][0])*one_det;
		ret[2][1] = (matrix[0][1]*matrix[2][0] -
			     matrix[0][0]*matrix[2][1])*one_det;
		ret[2][2] = (matrix[0][0]*matrix[1][1] -
			     matrix[0][1]*matrix[1][0])*one_det;
	    }
	} else {
	    System.err.println("Can't invert matrix with dimention " +
			       matrix.length + "x" + matrix[0].length + ".");
	}
	return ret;
    }

    // Calculating rotation axis
    public static Matrix getRotationAxis(Matrix rot)
    {
	EigenvalueDecomposition eig = rot.eig();
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
	return eig.getV().getMatrix(0,rot.getRowDimension() - 1,
				    index,index);
    }

    // Calculating rotation angle
    public static double getRotationAngle(Matrix rot,Matrix axis)
    {
	double xs = rot.get(2,1) - rot.get(1,2);
	double ys = rot.get(0,2) - rot.get(2,0);
	double zs = rot.get(1,0) - rot.get(0,1);
	double sin = 0.5*(axis.get(0,0)*xs + axis.get(1,0)*ys +
			  axis.get(2,0)*zs);
	return Math.asin(sin);
    }
}