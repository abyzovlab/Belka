package belka.draw;

//--- Java imports ---
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

//--- Application imports ---
import belka.mol.*;

/**
 *  Panel to draw structures of molecules.
 *  Molecule to draw can be added by function
 * {@link #addMoleculeToDraw(Molecule)} and removed by function
 * {@link #removeMoleculeToDraw(Molecule)}.
 *
 * @author Alexej Abyzov
 */
public class MolPanel extends JPanel
{
    /**
     * Object constructor.
     */
    public MolPanel()
    {
	super();
	MolMouseListener mouseListener = new MolMouseListener();
	addMouseListener(mouseListener);
	addMouseMotionListener(mouseListener);
	DrawConstants.init();
	setBackground(Color.black);
	arrayInitialize();
    }

    // Initializing arrays
    private void arrayInitialize()
    {
	// Making pointers null to allow garbage collector to clean memory
	pixImage_ = null;
	zDrawn_   = null;
	cleanPix_ = null;
	molImage_ = null;
	//System.gc();

	// Allocating arrays
	pixImage_ = new int[MAX_WIDTH*MAX_HEIGHT];
	zDrawn_   = new int[MAX_WIDTH*MAX_HEIGHT];
	nToClean_ = MAX_WIDTH*MAX_HEIGHT;         
	cleanPix_ = new int[MAX_WIDTH*MAX_HEIGHT];

	// Creating initial depth of  Z
	int index = 0;
	for (int y = 0;y < MAX_HEIGHT;y++)
	    for (int x = 0;x < MAX_WIDTH;x++) {
		zDrawn_[index]   = HIGH_Z;
		index++;
	    }
    }
    
    // Maximax width and height
    int MAX_WIDTH_POWER  = 100;
    int MAX_HEIGHT_POWER = 100;
    int MAX_WIDTH  = 1<<MAX_WIDTH_POWER;
    int MAX_HEIGHT = 1<<MAX_HEIGHT_POWER;

    // Array of last calculate color shades
    ShadeArray poolShadeArray_ = new ShadeArray();
    final static ShadeArray colShades_[] = { new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray(),
					     new ShadeArray()};
    /**
     * Function to get array of shades for give color. The array represnets
     * the variations of the original color depending of intensity.
     * The function does not calculate the array but finds it in the list of
     * 32 previously calculated ones. If not found one the 32 is initialized
     * with given color.
     * @param col original color
     * @param max max value of intensity
     *
     * @return object of {@link #ShadeArray} representing the array with
     * colors.
     */
    private ShadeArray calcColorShades(Color col,int max)
    {
	if (col == null || max < 0) return null;

	// Checking if it was already calculated
	for (int i = 0;i < colShades_.length;i++) {
	    ShadeArray curr_shade = colShades_[i];
	    if (col.equals(curr_shade.getColor()) &&
		max == curr_shade.getMax()) return curr_shade;
	}
	
	// Calculating new
	ShadeArray tmp = poolShadeArray_;
	int i = colShades_.length - 1;
	poolShadeArray_ = colShades_[i];
	while (i > 0) colShades_[i] = colShades_[--i];
	colShades_[0] = tmp;
	colShades_[0].init(col,max);
	return colShades_[0];
    }

    // Flag of what to draw
    final static int FLAG_REDRAW      = 0x0001;
    final static int FLAG_RESIZE      = 0x0002;
    final static int FLAG_SCREENCOORD = 0x0004;
    final static int FLAG_SCREENSCALE = 0x0008;
    int flagDraw = (FLAG_REDRAW | FLAG_RESIZE | FLAG_SCREENCOORD);

    /**
     * The function updates the view of molecules. It's not equivalent to
     * function {@link #paint(Graphics)}. The function sets nessesary flags for
     * {@link #paint(Graphics)} to know how to update the view.
     */
    public void updateMoleculeView()
    {
	flagDraw |= (FLAG_REDRAW | FLAG_SCREENCOORD | FLAG_SCREENSCALE);
	repaint();
    }

    // Image of molecules
    Image             molImage_    = null;
    MemoryImageSource imageSource_ = null;
    
    // Array with pixels of image
    int pixImage_[] = null; // Array of pixels
    int zDrawn_[]   = null; // Draw Z for pixel
    int nToClean_   = 0;    // # of pixels to clean
    int cleanPix_[] = null; // Pixel indexes to clean
    int width_ = 1,height_ = 1;                      // Width and height
    int center_x_ = 0,center_y_ = 0;                 // Center
    long timePixel = 0,timeColor = 0;;
    void fillPixels()
    {
	if ((flagDraw & FLAG_SCREENCOORD) > 0) {
	    flagDraw &= ~FLAG_SCREENCOORD;
	    calcScreenCoord();
	}

	if ((flagDraw & FLAG_SCREENSCALE) > 0) {
	    flagDraw &= ~FLAG_SCREENSCALE;
	    adjustToScreenScale();
	}

	if ((flagDraw & (FLAG_REDRAW | FLAG_RESIZE)) > 0) {
	    flagDraw &= ~FLAG_REDRAW;
	    if ((flagDraw & FLAG_RESIZE) > 0) {
		flagDraw &= ~FLAG_RESIZE;
		imageSource_ = null;
		molImage_ = null;
		imageSource_ = new MemoryImageSource(width_,
						     height_,
						     pixImage_,
						     0,
						     MAX_WIDTH);
		imageSource_.setAnimated(true);
		molImage_ = createImage(imageSource_);
		//molImage_ = g3d.getScreenImage();
	    }
	    clearImage();
	    drawMolecules();
 	    imageSource_.newPixels(0,0,width_,height_);
	}
    }
    
    // List of molelcules
    private Molecule[] molArray_ = null;

    /**
     * Returs molecules drawn in the panel. 
     *
     * @return copy of the array with molecules drawn in the panel.
     */
    public  Molecule[] getMoleculesToDraw()
    {
	if (molArray_ == null || molArray_.length == 0) return null;

	Molecule[] ret = new Molecule[molArray_.length];
	for (int i = 0;i < molArray_.length;i++)
	    ret[i] = molArray_[i];

	return ret;
    }

    /**
     * Adds a molecule to draw in the panel.
     */
    public  void addMoleculeToDraw(Molecule mol)
    {
	if (mol == null) return;

	mol.setScale(scale_);
		    
	if (molArray_ == null) {
	    molArray_ = new Molecule[1];
	    molArray_[0] = mol;
	    return;
	}
	Molecule[] molArrayNew = new Molecule[molArray_.length + 1];
	for (int i = 0;i < molArray_.length;i++)
	    molArrayNew[i] = molArray_[i];
	molArrayNew[molArray_.length] = mol;
	molArray_ = molArrayNew;
    }

    /**
     * Removes molecule from the panel.
     */
    public void removeMoleculeToDraw(Molecule mol)
    {
	if (mol == null)       return;
	if (molArray_ == null) return;

	int iRemove = 0;
	for (;iRemove < molArray_.length;iRemove++)
	    if (molArray_[iRemove] == mol) break;
	if (iRemove >= molArray_.length) return;

	Molecule[] molArrayNew = new Molecule[molArray_.length - 1];
	int index = 0;
	for (int i = 0;i < molArray_.length;i++) {
	    if (i != iRemove) molArrayNew[index++] = molArray_[i];
	}

	molArray_ = molArrayNew;
    }

    // Calculating coordinate of atoms on screen
    int[] rot_   = {0,0,0};   // Angle to rotate
    int[] shift_ = {0,0,0};   // Shift to displace
    double scale_ = 0.004, scaleStepRatio_ = 0.005;
    double scaleStep_ = scale_*scaleStepRatio_;
    /**
     * The function invokes calculation of screen coordianted for drawn
     * molecules upon transformation.
     */
    void calcScreenCoord()
    {
	if (molArray_ == null) return;

	int x_r = rot_[0]>>1;
	int y_r = rot_[1]>>1;
	int z_r = rot_[2]>>1;
	rot_[0] = rot_[1] = rot_[2] = 0;
	double x_s = shift_[0]/(double)Atom.scaleToPixels();
	double y_s = shift_[1]/(double)Atom.scaleToPixels();
	double z_s = shift_[2]/(double)Atom.scaleToPixels();
	shift_[0] = shift_[1] = shift_[2] = 0;

	if (x_s == 0 && y_s == 0 && z_s == 0 &&
	    x_r == 0 && y_r == 0 && z_r == 0) {
	    for (int i = 0;i < molArray_.length;i++) {
		Molecule mol = molArray_[i];
		mol.transform();
	    }
	} else {
	    for (int i = 0;i < molArray_.length;i++) {
		Molecule mol = molArray_[i];
		mol.addTransformation(-y_r,x_r,z_r,x_s,y_s,z_s);
	    }
	}
    }

    /**
     * The function invokes calculation of screen coordianted for drawn
     * molecules upon rescaling.
     */
    void adjustToScreenScale()
    {
	if (molArray_ == null) return;

	for (int i = 0;i < molArray_.length;i++) {
	    Molecule mol = molArray_[i];
	    mol.setScale(scale_);
	}
    }

    // Highest z-coordinate
    final static int HIGH_Z = 9999999;
    /**
     * The function fills the image with background pixels.
     */
    void clearImage()
    {
	for (int i = 0;i < nToClean_;i++) {
	    int index = cleanPix_[i];
	    pixImage_[index] = 0;
	    zDrawn_[index]   = HIGH_Z;
	}
	nToClean_ = 0;
    }

    /**
     * The function renders the drawing of molecules.
     */
    void drawMolecules()
    {
	if (molArray_ == null) return;

	for (int i = 0;i < molArray_.length;i++) {
	    Molecule mol = molArray_[i];

	    // Drawing atoms
	    for (Chain chain = mol.chainList();chain != null;
		 chain = chain.next())
		for (Assembly ass = chain.assemblyList();ass != null;
		     ass = ass.next()) {
		    for (Atom a = ass.atomList();a != null;a = a.next()) {

			// Draw atoms
			int rad = a.getScreenRadius();
			if (rad >= 0) {
			    int x     = a.getScreenX() + center_x_;
			    int y     = a.getScreenY() + center_y_;
			    int z     = a.getScreenZ();
			    Color col = a.getColor();
			    if ((x + rad) >=  0    &&
				(x - rad) < width_ &&
				(y + rad) >=  0    &&
				(y - rad) < height_) drawSphere(x,y,z,rad,col);
			}

			// Draw bonds
			Bond[] bonds = a.bondArray();
			if (bonds == null) continue;
			for (int b = 0;b < bonds.length;b++) {
			    Bond bond = bonds[b];
			    if (bond.getFAtom() != a) continue;
			    drawBond(bond);
			}
		    }
		    // Draw backbone
		    Bond[] bonds = ass.bondArray();
		    if (bonds == null) continue;
		    for (int b = 0;b < bonds.length;b++) {
			Bond bond = bonds[b];
			if (bond != null && bond.getFAtom() != null &&
			    bond.getFAtom().assembly() == ass) drawBond(bond);
		    }
		}
 	}
    }
	

    /**
     * The function to set individual pixel into a pixel map.
     * @param pixel index of the pixel on pixel map
     * @param z Z-coordianted of the pixel
     * @param col color to set for the pixel
     *
     * @return 'true' if it was put into pixel map (that is visible),
     * 'false' otherwise.
     */
    boolean setPixel(int pixel,int z,int col)
    {
 	if (z > zDrawn_[pixel]) return false;

	if (zDrawn_[pixel] == HIGH_Z)
	    cleanPix_[nToClean_++] = pixel;
	pixImage_[pixel] = col;
	zDrawn_[pixel]   = z;
	return true;
    }

    // Function that draws bonds
    void drawBond(Bond bond)
    {
	int rad = bond.getScreenRadius();
	if (rad < 0) return;

	Atom a1 = bond.getFAtom();
	Atom a2 = bond.getSAtom();
	if (a1 == null || a2 == null) return;

	int x1     = a1.getScreenX() + center_x_;
	int y1     = a1.getScreenY() + center_y_;
	int z1     = a1.getScreenZ();
	Color col1 = a1.getColor();
	int x2     = a2.getScreenX() + center_x_;
	int y2     = a2.getScreenY() + center_y_;
	int z2     = a2.getScreenZ();
	Color col2 = a2.getColor();

	if (x1 < 0 && x2 < 0)               return;
	if (x1 >= width_ && x2 >= width_)   return;
	if (y1 < 0 && y2 < 0)               return;
	if (y1 >= height_ && y2 >= height_) return;
	drawCylinder(x1,y1,z1,x2,y2,z2,col1,col2,rad);
    }

    // Function drawing cylinder by pixels 
    void drawCylinder(int x1,int y1,int z1,int x2,int y2,int z2,
		      Color col1,Color col2,int rad)
    {
	if (rad > DrawConstants.MAXIMAL_RADIUS)
	    rad = DrawConstants.MAXIMAL_RADIUS;
	if (rad < 0) return;
	if (col1 == null || col2 == null) return;

	int delta_x = x2 - x1, delta_y = y2 - y1, delta_z = z2 - z1;
	int nx = delta_x; if (nx > 0) nx++; else if (nx < 0) nx--;
	int ny = delta_y; if (ny > 0) ny++; else if (ny < 0) ny--;
	int nz = delta_z; if (nz > 0) nz++; else if (nz < 0) nz--;

	// Arrayx for arc arc projected on the sphere.
	int index_arc = 0;
	int[] arcx = DrawConstants.arcx;
	int[] arcy = DrawConstants.arcy;
	int[] arcz = DrawConstants.arcz;

	// Arrays of shades for two colors
	ShadeArray shadeArray1 =
	    calcColorShades(col1,DrawConstants.radMaxInten[rad]);
	ShadeArray shadeArray2 =
	    calcColorShades(col2,DrawConstants.radMaxInten[rad]);

	// Drawing cylinder caps and saving arc
	int pixel_index1 = ((y1 - rad)<<MAX_WIDTH_POWER) + x1;
	int pixel_index2 = ((y2 - rad)<<MAX_WIDTH_POWER) + x2;
	int y_chord_index = DrawConstants.radChordIndex[rad];
	for (int dy = -rad, scalar_y = dy*ny, scalar_x = dy*nx;dy <= rad;
	     dy++,scalar_y += ny, scalar_x += nx) {
	    int width = DrawConstants.precalcChords[y_chord_index++];
	    int z_chord_index = DrawConstants.radChordIndex[width];
	    pixel_index1 -= width;
	    pixel_index2 -= width;
	    int prevVal_x = 0,prevVal_y = 0;
	    int max_z = (width<<1)*Math.abs(nz);
	    int max_x = max_z + Math.abs(nx);
	    int max_y = max_z + Math.abs(ny);
	    for (int dx = -width, scalar_yx = scalar_y + dx*nx,
		     scalar_xy = scalar_x + dx*ny;dx <= width;
		 dx++, scalar_yx += nx, scalar_xy += ny) {
		int dz = DrawConstants.precalcChords[z_chord_index++];
		int tmp = -dz*nz;
		int scalar_yxz = scalar_yx + tmp;
		int scalar_xyz = scalar_xy + tmp;
		int sign = scalar_yxz;
		int inten = dx + dy + dz;

		// Choosing which cap to draw
		if (sign < 0) {
		    int xToPut = x1 + dx;
		    int yToPut = y1 + dy;
		    if (inBounds(xToPut,yToPut)) {
			int zToPut     = z1 - dz;
			int colorToPut = shadeArray1.shadeForInten(inten);
			setPixel(pixel_index1,zToPut,colorToPut);
		    }
		} else if (sign > 0) {
		    int xToPut = x2 + dx;
		    int yToPut = y2 + dy;
		    if (inBounds(xToPut,yToPut)) {
			int zToPut     = z2 - dz;
			int colorToPut = shadeArray2.shadeForInten(inten);
			setPixel(pixel_index2,zToPut,colorToPut);
		    }
		} else {
		    int xToPut = x1 + dx;
		    int yToPut = y1 + dy;
		    if (inBounds(xToPut,yToPut)) {
			int zToPut     = z1 - dz;
			int colorToPut = shadeArray1.shadeForInten(inten);
			setPixel(pixel_index1,zToPut,colorToPut);
		    }
		    xToPut = x2 + dx;
		    yToPut = y2 + dy;
		    if (inBounds(xToPut,yToPut)) {
			int zToPut     = z2 - dz;
			int colorToPut = shadeArray2.shadeForInten(inten);
			setPixel(pixel_index2,zToPut,colorToPut);
		    }
		}

		// Setting flag which coordinate to save
		boolean save_x = false, save_prev_x = false;
		boolean save_y = false, save_prev_y = false;
		if (dx > -width && dx < width) {
		    if ((prevVal_x >= 0 && scalar_xyz <= 0) ||
			(prevVal_x <= 0 && scalar_xyz >= 0))
			save_prev_x = save_x = true;
		    if ((prevVal_y >= 0 && scalar_yxz <= 0) ||
			(prevVal_y <= 0 && scalar_yxz >= 0))
 			save_prev_y = save_y = true;
		} else { // On edge of a sphere
		    if (Math.abs(scalar_xyz) < max_x) save_x = true;
		    if (Math.abs(scalar_yxz) < max_y) save_y = true;
		}
		if (scalar_xyz == 0) save_x = true;
		if (scalar_yxz == 0) save_y = true;
		if (rad <= 4) { // For small radius save all pixels
		    save_x = false;
		    save_y = true;
		    save_prev_y = save_prev_x = false;
		}

		// Saving coordinate of arc
		if (save_x) {
		    arcx[index_arc]   = dy;
		    arcy[index_arc]   = dx;
		    arcz[index_arc++] = dz;
		}
// 		if (save_prev_x) {
// 		    arcx[index_arc]   = dy - 1;
// 		    arcy[index_arc]   = dx;
// 		    arcz[index_arc++] = dz;
// 		}
		if (save_y) {
		    arcx[index_arc]   = dx;
		    arcy[index_arc]   = dy;
		    arcz[index_arc++] = dz;
		}
// 		if (save_prev_y) {
// 		    arcx[index_arc]   = dx;
// 		    arcy[index_arc]   = dy - 1;
// 		    arcz[index_arc++] = dz;
// 		}
		pixel_index1++;
		pixel_index2++;
		prevVal_y = scalar_yxz;
		prevVal_x = scalar_xyz;
	    }
	    pixel_index1--;
	    pixel_index1 += (MAX_WIDTH - width);
	    pixel_index2--;
	    pixel_index2 += (MAX_WIDTH - width);
	}

	// Saving color for pixels on arc
	for (int i = 0;i < index_arc;i++) {
	    int inten = arcx[i] + arcy[i] + arcz[i];
	    DrawConstants.arcc1[i] = shadeArray1.shadeForInten(inten);
	    DrawConstants.arcc2[i] = shadeArray2.shadeForInten(inten);
	}

	// Drawing cylinder
	if (nx == 0) {
	    double ratioy = (double)(delta_z)/(double)delta_y;
	    int y_step = 1;
	    int y_mid  = Math.abs(ny)>>1;
	    if (ny < 0) {
		y_step = -1;
	        y_mid  = -y_mid;
		ratioy = -ratioy;
	    }
	    double z_by_y = z1;
	    int arcc[] = DrawConstants.arcc1;
	    for (int y = 0;y != ny;y += y_step,z_by_y += ratioy) {
		int xAdd = x1;
		int yAdd = y1 + y;
		int zAdd = roundOff(z_by_y);
		for (int i = 0;i < index_arc;i++) {
		    int xToPut = xAdd + arcx[i];
		    int yToPut = yAdd + arcy[i];
		    if (inBounds(xToPut,yToPut)) {
			int zToPut = zAdd - arcz[i];
			int pixel_index = (yToPut<<MAX_WIDTH_POWER) + xToPut;
			setPixel(pixel_index,zToPut,arcc[i]);
		    }
		}
		if (y == y_mid) arcc = DrawConstants.arcc2;
	    }
	} else if (ny == 0) {
	    double ratiox = (double)(delta_z)/(double)delta_x;
	    int x_step = 1, x_mid  = Math.abs(nx)>>1;
	    if (nx < 0) {
		x_step = -1;
	        x_mid  = -x_mid;
		ratiox = -ratiox;
	    }
	    int arcc[] = DrawConstants.arcc1;
	    double z_by_x = z1;
 	    for (int x = 0;x != nx;x += x_step,z_by_x += ratiox) {
		int xAdd = x1 + x;
		int yAdd = y1;
		int zAdd = roundOff(z_by_x);
		for (int i = 0;i < index_arc;i++) {
		    int xToPut = xAdd + arcx[i];
		    int yToPut = yAdd + arcy[i];
		    if (inBounds(xToPut,yToPut)) {
			int zToPut = zAdd - arcz[i];
			int pixel_index = (yToPut<<MAX_WIDTH_POWER) + xToPut;
			setPixel(pixel_index,zToPut,arcc[i]);
		    }
		}
		if (x == x_mid) arcc = DrawConstants.arcc2;
	    }
	} else {
	    double ratiox = (double)(delta_z)/(double)delta_x;
	    double ratioy = (double)(delta_z)/(double)delta_y;
	    double tan = Math.abs((double)(ny)/(double)nx);
	    int    x_mid  = Math.abs(nx)>>1, y_mid  = Math.abs(ny)>>1;
	    double max = tan, max_x = Math.abs(nx), max_y = Math.abs(ny);
	    if (nx < 0) ratiox = -ratiox;
	    if (ny < 0) ratioy = -ratioy;
	    double z_by_x = z1, z_by_y = z1;
	    int arcc[] = DrawConstants.arcc1;
	    for (int x = 0, y = 0;x < max_x;
		 x++, max += tan, z_by_x += ratiox) {
		for (;y < max && y < max_y;y++,z_by_y += ratioy) {
		    int xAdd = x; if (nx < 0) xAdd = -x; xAdd += x1;
		    int yAdd = y; if (ny < 0) yAdd = -y; yAdd += y1;
		    int zAdd = roundOff(z_by_y);
		    if (y > y_mid || x > x_mid) arcc = DrawConstants.arcc2;
		    else                        arcc = DrawConstants.arcc1;
		    for (int i = 0;i < index_arc;i++) {
			int xToPut = xAdd + arcx[i];
			int yToPut = yAdd + arcy[i];
			if (inBounds(xToPut,yToPut)) {
			    int zToPut = zAdd - arcz[i];
			    int pixel_index = (yToPut<<MAX_WIDTH_POWER) +
				xToPut;
			    setPixel(pixel_index,zToPut,arcc[i]);
			}
		    }
		}
		y--; z_by_y -= ratioy;
		int xAdd = x; if (nx < 0) xAdd = -x; xAdd += x1;
		int yAdd = y; if (ny < 0) yAdd = -y; yAdd += y1;
		int zAdd = roundOff(z_by_x);
		if (y > y_mid || x > x_mid) arcc = DrawConstants.arcc2;
		else                        arcc = DrawConstants.arcc1;
		for (int i = 0;i < index_arc;i++) {
		    int xToPut = xAdd + arcx[i];
		    int yToPut = yAdd + arcy[i];
		    if (inBounds(xToPut,yToPut)) {
			int zToPut = zAdd - arcz[i];
			int pixel_index = (yToPut<<MAX_WIDTH_POWER) + xToPut;
			setPixel(pixel_index,zToPut,arcc[i]);
		    }
		}
	    }
	}
    }

    // Function drawing sphere by pixels
    private void drawSphere(int x,int y,int z,int rad,Color col)
    {
	if (rad > DrawConstants.MAXIMAL_RADIUS)
	    rad = DrawConstants.MAXIMAL_RADIUS;
	if (rad < 0) return;
	if (col == null) return;

	if (rad == 0) { // Put single pixel and exit
	    if (x < 0 || x > width_)  return;
	    if (y < 0 || y > height_) return;
	    int pixel_index = (y<<MAX_WIDTH_POWER) + x;
	    setPixel(pixel_index,z,col.getRGB());
	    return;
	}

	ShadeArray shadeArray =
	    calcColorShades(col,DrawConstants.radMaxInten[rad]);

	int xToLowEdge  = -x;
	int xToHighEdge = width_ - x - 1;
	int yToLowEdge  = 0 - y;
	int yToHighEdge = height_ - y - 1;

	int start_y = -rad;
	if (start_y < yToLowEdge) start_y = yToLowEdge;
	int end_y = rad;
	if (end_y > yToHighEdge) end_y = yToHighEdge;
	int pixel_index = ((y + start_y)<<MAX_WIDTH_POWER) + x;
	int y_chord_index = DrawConstants.radChordIndex[rad] + rad + start_y;
	for (int dy = start_y;dy <= end_y;dy++) {
	    int width = DrawConstants.precalcChords[y_chord_index++];
	    int start_x = -width;
	    if (start_x < xToLowEdge) start_x = xToLowEdge;
	    int end_x =  width;
	    if (end_x > xToHighEdge) end_x = xToHighEdge;
	    if (start_x > end_x) {
		pixel_index += MAX_WIDTH;
		continue;
	    }
	    pixel_index += start_x;
	    int z_chord_index = DrawConstants.radChordIndex[width] + width +
		start_x;
	    for (int dx = start_x;dx <= end_x;dx++) {
		int dz = DrawConstants.precalcChords[z_chord_index++];
 		int zToPut = z - dz;
		int colorToPut = shadeArray.shadeForInten(dx + dy + dz);
		setPixel(pixel_index,zToPut,colorToPut);
		pixel_index++;
	    }
	    pixel_index--;
	    pixel_index += (MAX_WIDTH - end_x);
	}
    }

    /**
     * Sets bounds of the panel.
     *
     * @param x coordinate of the left edge
     * @param y coordinate of the upper edge
     * @param width width of the panel
     * @param height height of the panel
     */
    public void setBounds(int x,int y,int width,int height)
    {
	flagDraw |= FLAG_RESIZE;
	int old_max_width  = MAX_WIDTH;
	int old_max_height = MAX_HEIGHT;

	width_  = width;
	while (MAX_WIDTH > width_) {
	    MAX_WIDTH_POWER--;
	    MAX_WIDTH  = 1<<MAX_WIDTH_POWER;
	}
	while (MAX_WIDTH < width_) {
	    MAX_WIDTH_POWER++;
	    MAX_WIDTH  = 1<<MAX_WIDTH_POWER;
	}

	height_ = height;
	while (MAX_HEIGHT > height_) {
	    MAX_HEIGHT_POWER--;
	    MAX_HEIGHT  = 1<<MAX_HEIGHT_POWER;
	}
	while (MAX_HEIGHT < height_) {
	    MAX_HEIGHT_POWER++;
	    MAX_HEIGHT  = 1<<MAX_HEIGHT_POWER;
	}


	if (MAX_WIDTH  != old_max_width ||
	    MAX_HEIGHT != old_max_height) {
	    arrayInitialize();
	}

	center_x_ = width_>>1;
	center_y_ = height_>>1;

	super.setBounds(x,y,width_,height_);
    }

    /**
     * Return preferred size of a panel.
     *
     * @return preferred size of a panel
     */
    public Dimension getPreferredSize()
    {
	return new Dimension(width_,height_);
    }

    /**
     * The function painting molecules.
     *
     * @param g graphics to paint on.
     */
    public void paint(Graphics g)
    {
	super.paint(g);
	if (g == null) {
	    System.err.println("Graphics is null.");
	    return;
	}

	Rectangle drawRec = g.getClipBounds();
	if (drawRec == null) return;
	
	int x      = (int)drawRec.getX();
	int y      = (int)drawRec.getY();
	int width  = (int)drawRec.getWidth();
	int height = (int)drawRec.getHeight();

	fillPixels();

	Image imageToDraw = molImage_;
	if (width  != width_  ||
	    height != height_ ||
	    x      != 0       ||
	    y      != 0) {
	    int offset = x + y*MAX_WIDTH;
	    imageToDraw = createImage(new MemoryImageSource(width,
							    height,
							    pixImage_,
							    offset,
							    MAX_WIDTH));
	}

	try {
	    g.drawImage(imageToDraw,x,y,null);
	} catch (Exception e) {
	    System.err.println(e.toString());
	}
    }

    /**
     * Function to check if the give coodinates are inside of the drawn
     * rectangle.
     * @param x X-coordinate
     * @param y X-coordinate
     *
     * @return 'true' if point is in rectangle, 'false' otherwise
     */
    private boolean inBounds(int x, int y)
    {
	if (x > 0 && x < width_ &&
	    y > 0 && y < height_) return true;
	return false;
    }

    /**
     * Function to round off double to closes integer 
     * @param x value to round
     *
     * @return rounded value
     */
    private int roundOff(double x)
    {
	if (x > 0)      return (int)(x + 0.5);
	else if (x < 0) return (int)(x - 0.5);
	else            return 0;
    }


    /**
     * Function to process events. Dispatches event to the childes of the
     * parent component which are of the same class as the current object.
     * 
     * @param e event
     */
    public void processEvent(AWTEvent e)
    {
	// If events comes from different componenet simple process it
	if (e.getSource() != this) {
	    super.processEvent(e);
	    return;
	}

	// If there is no parent container simply process it 
	Container cont = getParent();
	if (cont == null) {
	    super.processEvent(e);
	    return;
	}

	// Go to container find same class object and process event
 	Class thisClass = this.getClass();
	Component[] comps = cont.getComponents();
	for (int i = 0;i < comps.length;i++)
	    if (comps[i] != this &&
		comps[i].getClass() == thisClass)
		comps[i].dispatchEvent(e);

	// Finall process event by itself
	super.processEvent(e);
    }

    /**
     * Listener to handle mouse events.
     */
    class MolMouseListener implements MouseListener, MouseMotionListener
    {
	int mouse_pressed_x_ = 0; // Coordinates where mouse is pressed
	int mouse_pressed_y_ = 0;

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
	    mouse_pressed_x_ = e.getX();
	    mouse_pressed_y_ = e.getY();
	}

	public void mouseReleased(MouseEvent e)
	{
	    rot_[0]   = rot_[1]   = rot_[2]   = 0;
	    shift_[0] = shift_[1] = shift_[2] = 0;
	}

	public void mouseDragged(MouseEvent e)
	{
	    int x = e.getX();
	    int y = e.getY();
	    int modifier = e.getModifiersEx();
	    if ((modifier & InputEvent.BUTTON1_DOWN_MASK) != 0 &&
		(modifier & InputEvent.SHIFT_DOWN_MASK)   != 0) {
		int move = y - mouse_pressed_y_;
		int n_step = Math.abs(move);
		for (int i = 0;i < n_step;i++) {
		    if (move > 0) scale_ += scaleStep_;
		    else          scale_ -= scaleStep_;
		    scaleStep_ = scaleStepRatio_*scale_;
		}
		flagDraw |= FLAG_SCREENSCALE;
	    } else if ((modifier & InputEvent.BUTTON1_DOWN_MASK) != 0) {
		rot_[0] -= ((x - mouse_pressed_x_)<<1);
		rot_[1] -= ((y - mouse_pressed_y_)<<1);
// 		rot_[0] -= (x - mouse_pressed_x_);
// 		rot_[1] -= (y - mouse_pressed_y_);
	    } else if ((modifier & InputEvent.BUTTON2_DOWN_MASK) != 0) {
		rot_[2] = ((x - mouse_pressed_x_)<<1);
// 		rot_[2] = (x - mouse_pressed_x_);
	    } else if ((modifier & InputEvent.BUTTON3_DOWN_MASK) != 0) {
		shift_[0] += (x - mouse_pressed_x_);
		shift_[1] += (y - mouse_pressed_y_);
	    }
	    mouse_pressed_x_ = x;
	    mouse_pressed_y_ = y;
	    flagDraw |= (FLAG_SCREENCOORD | FLAG_REDRAW);
	    repaint();
	}

	public void mouseMoved(MouseEvent e)
	{
	}
    }
}
