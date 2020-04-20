package belka.mol;

/**
 * Abstract class describing transformable object. The transformation is
 * assumend to be made in 3D by rotation matrix, translation shift and scale.
 *
 * @author Alexej Abyzov
 */
abstract class Transformable
{
    final static double ANGLE_CONVERSION_COEFF = Math.PI/180.;

    // Rotation matrix 
    protected double[][] rot_ = {{1,0,0},{0,1,0},{0,0,1}};
    /**
     * Returns current rotation matrix.
     *
     * @return current rotation matrix
     */
    public double[][] getRotation() { return rot_; }

    // Translation vector
    protected double[] trans_ = {0,0,0};
    /**
     * Returns current translation vector.
     *
     * @return current translation vector
     */
    public double[] getTranslation() { return trans_; }

    // Scale factor 
    protected double scale_ = 1;
    /**
     * Returns current scale.
     *
     * @return current scale.
     */
    public double getScale() { return scale_; }

    // Freezing status 
    boolean freez = false;
    /**
     * Returns status of freezing.
     * 
     * @return status of freezing.
     */
    public boolean isFrozen() { return freez; }

    /**
     * Freez or unfreez the object. If object is frozen no new transformation
     * is added {@link #addTransformation} but scaling and setting
     * transfromation work as usuall: {@link #transformSize()} and
     * {@link #setTransformation}.
     * 
     */
    public void setFrozen(boolean val) { freez = val; }

    /**
     * Sets transforamtion (rotation and translation) for the object.
     * Once values are set the function to perform transformation
     * {@link #transform()} is called.
     *
     * @param rot rotation matrix.
     * @param trans translation vector.
     */
    public void setTransformation(double[][] rot, double[] trans)
    {
	if (rot == null || trans == null ||
	    rot.length != 3 || rot[0].length != 3 || rot[1].length != 3 ||
	    rot[2].length != 3 || trans.length != 3) {
	    System.err.println("ERROR: unacceptable transformation in " +
			       "transformable.");
	    return;
	}

	for (int i1 = 0;i1 < 3;i1++) {
	    trans_[i1] = trans[i1];
	    for (int i2 = 0;i2 < 3;i2++)
		rot_[i1][i2] = rot[i1][i2];
	}

	transform();
    }
    /**
     * Adds transformation (rotation and translation) to the current
     * transformation. Rotation is specified by three rotation angles
     * around axis. Translation is specified by three shifts along axis.
     * After values for rotation and translation are updated the one of the
     * functions for transformation is called: {@link #transformXY()}, 
     * {@link #transformYZ()}, {@link #transformZX()}, {@link #transform()}.
     *
     * @param angle_x anlge of rotation around X-axis.
     * @param angle_y anlge of rotation around Y-axis.
     * @param angle_z anlge of rotation around Z-axis.
     * @param shift_x shift long X-axis.
     * @param shift_y shift long Y-axis.
     * @param shift_z shift long Z-axis.
     */
    public void addTransformation(int angle_x,int angle_y,int angle_z,
				  double shift_x,
				  double shift_y,
				  double shift_z)
    {
	if (freez) return;

	if (angle_x != 0) addRotX(angle_x);
	if (angle_y != 0) addRotY(angle_y);
	if (angle_z != 0) addRotZ(angle_z);
	if (shift_x != 0) addShiftX(shift_x);
	if (shift_y != 0) addShiftY(shift_y);
	if (shift_z != 0) addShiftZ(shift_z);
	if (angle_x == 0 && angle_y == 0 && shift_z == 0) {
	    transformXY();
	} else if (shift_x == 0 && angle_y == 0 && angle_z == 0) {
	    transformYZ();
	} else if (angle_x == 0 && shift_y == 0 && angle_z == 0) {
	    transformZX();
	} else {
	    transform();
	}
    }

    void addRotX(int angle)
    {
	if (freez) return;
	
	double angle_rad = angle*ANGLE_CONVERSION_COEFF;
	double cos_angle = Math.cos(angle_rad);
	double sin_angle = Math.sin(angle_rad);

	for (int i = 0;i < 3;i++) {
	    double y = rot_[1][i];
	    double z = rot_[2][i];
	    rot_[1][i] = cos_angle*y - sin_angle*z;
	    rot_[2][i] = sin_angle*y + cos_angle*z;
	}
    }

    void addRotY(int angle)
    {
	if (freez) return;

	double angle_rad = angle*ANGLE_CONVERSION_COEFF;
	double cos_angle = Math.cos(angle_rad);
	double sin_angle = Math.sin(angle_rad);

	for (int i = 0;i < 3;i++) {
	    double z = rot_[2][i];
	    double x = rot_[0][i];
	    rot_[2][i] = cos_angle*z - sin_angle*x;
	    rot_[0][i] = sin_angle*z + cos_angle*x;
	}
    }

    void addRotZ(int angle)
    {
	if (freez) return;

	double angle_rad = angle*ANGLE_CONVERSION_COEFF;
	double cos_angle = Math.cos(angle_rad);
	double sin_angle = Math.sin(angle_rad);

	for (int i = 0;i < 3;i++) {
	    double x = rot_[0][i];
	    double y = rot_[1][i];
	    rot_[0][i] = cos_angle*x - sin_angle*y;
	    rot_[1][i] = sin_angle*x + cos_angle*y;
	}
    }

    void addShiftX(double shift)
    {
	if (freez) return;
	trans_[0] += shift;
    }

    void addShiftY(double shift)
    {
	if (freez) return;
	trans_[1] += shift;
    }

    void addShiftZ(double shift)
    {
	if (freez) return;
	trans_[2] += shift;
    }

    /**
     * Sets scale to use. After updating value function
     * {@link #transformSize()} is called.
     */
    public void setScale(double scale)
    {
	scale_ = scale;
	transform();
	transformSize();
    }

    // Methods for transformation (must be implemented in descendants)
    /**
     * Performs transformation of the object using current values for
     * transformation. The method has to be implemented by child class.
     */
    public void transform() {}
    /**
     * Performs transformation of X- and Y-coordinates of the object using
     * current values for transformation. The method has to be
     * implemented by child class.
     */
    public void transformXY() {}
    /**
     * Performs transformation of Y- and Z-coordinates of the object using
     * current values for transformation. The method has to be
     * implemented by child class.
     */
    public void transformYZ() {}
    /**
     * Performs transformation of Z- and X-coordinates of the object using
     * current values for transformation. The method has to be
     * implemented by child class.
     */
    public void transformZX() {}
    /**
     * Performs transformation of object size and coordinates using current
     * scale. The method has to be implemented by child class.
     */
    public void transformSize() {}
}
