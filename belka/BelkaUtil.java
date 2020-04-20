package belka;

//--- Java imports ---
import java.util.*;

//--- Application imports ---
import belka.mol.*;

/**
 * The class contains set of static functions to perform some operations with
 * molecules.
 *
 * @author Alexej Abyzov
 */
class BelkaUtil
{
    /**
     * The function creates bonds representing backbone connectivity between
     * CA atoms.
     *
     * @param mol molecule to do the action on
     */
    public static void connectCA(Molecule mol)
    {
	if (mol == null) return;
	
	final double MAX_CA_DIST = 4.2*4.2;
	Atom prev_a = null;
	Assembly prev_s = null;
	int ret = 0;
	for (Chain c = mol.chainList();c != null;c = c.next())
	    for (Assembly s = c.assemblyList();s != null;s = s.next()) {
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    if (!a.isCA()) continue;
		    Atom a2 = prev_a;
		    prev_a = a;
		    if (a2 == null) continue;
		    char alt1 = a.getAlternative();
		    char alt2 = a2.getAlternative();
		    if (alt1 != ' ' && alt2 != ' ' && alt1 != alt2) continue;
		    double dx = a2.getX() - a.getX();
		    double dist = dx*dx;
		    if (dist > MAX_CA_DIST) continue;
		    double dy = a2.getY() - a.getY();
		    dist += dy*dy;
		    if (dist > MAX_CA_DIST) continue;
		    double dz = a2.getZ() - a.getZ();
		    dist += dz*dz;
		    if (dist > MAX_CA_DIST) continue;
		    Bond newBond = Bond.create(a2,a);
		    s.addBond(newBond);
		    if (prev_s != null) {
			prev_s.addBond(newBond);
			ret++;
		    }
		}
		prev_s = s;
	    }
    }

    private final static int GRID_SIZE = 32;
    private final static ArrayList<Atom>[][][] grid =
	new ArrayList[GRID_SIZE][GRID_SIZE][GRID_SIZE];
    /**
     * The function creates bonds representing covalent bonds between atoms.
     *
     * @param mol molecule to do the action on
     */
    public static void connect(Molecule mol)
    {
	final double MAX_BOND_LENGTH = 2.4;

	for (Chain c = mol.chainList();c != null;c = c.next()) {
	    // Initialize min and max
	    double minX = 0, minY = 0, minZ = 0;
	    double maxX = 0, maxY = 0, maxZ = 0;
	    for (Assembly s = c.assemblyList();s != null;s = null)
		for (Atom a = s.atomList();a != null;a = null) {
		    minX = maxX = a.getX();
		    minY = maxY = a.getY();
		    minZ = maxZ = a.getZ();
		}
	    // Find min and max
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a = s.atomList();a != null;a = a.next()) {
		    double x = a.getX(),y = a.getY(), z = a.getZ();
		    if (x < minX) minX = x; if (x > maxX) maxX = x;
		    if (y < minY) minY = y; if (y > maxY) maxY = y;
		    if (z < minZ) minZ = z; if (z > maxZ) maxZ = z;
		}

	    // Calulate range on every axis
	    int rangeX = (int)(maxX - minX + 1);
	    int rangeY = (int)(maxY - minY + 1);
	    int rangeZ = (int)(maxZ - minZ + 1);

	    // Clear the grid 
	    for (int i1 = 0;i1 < GRID_SIZE;i1++)
		for (int i2 = 0;i2 < GRID_SIZE;i2++)
		    for (int i3 = 0;i3 < GRID_SIZE;i3++)
			grid[i1][i2][i3] = null;

	    // Calculate bonds
	    for (Assembly s = c.assemblyList();s != null;s = s.next())
		for (Atom a1 = s.atomList();a1 != null;a1 = a1.next()) {
		    
		    // Get covalent radius
		    double covRad1 = 0.3;
		    if (a1.getElement() != null)
			covRad1 = a1.getElement().getCovalentRadius();

		    // Alternative
		    char alt1 = a1.getAlternative();
		    
		    // Get coordinates
		    double x = a1.getX(),y = a1.getY(), z = a1.getZ();

		    // Calculate start on grid
		    double shiftX = x - minX - MAX_BOND_LENGTH;
		    double shiftY = y - minY - MAX_BOND_LENGTH;
		    double shiftZ = z - minZ - MAX_BOND_LENGTH;
		    int startX = 0,startY = 0, startZ = 0;
		    if (shiftX > 0) startX = (int)(GRID_SIZE*shiftX/rangeX);
		    if (shiftY > 0) startY = (int)(GRID_SIZE*shiftY/rangeY);
		    if (shiftZ > 0) startZ = (int)(GRID_SIZE*shiftZ/rangeZ);

		    // Calculate start on grid
		    shiftX += MAX_BOND_LENGTH + MAX_BOND_LENGTH;
		    shiftY += MAX_BOND_LENGTH + MAX_BOND_LENGTH;
		    shiftZ += MAX_BOND_LENGTH + MAX_BOND_LENGTH;
		    int last = GRID_SIZE - 1;
		    int endX = last,endY = last, endZ = last;
		    if (shiftX < rangeX) endX = (int)(GRID_SIZE*shiftX/rangeX);
		    if (shiftY < rangeY) endY = (int)(GRID_SIZE*shiftY/rangeY);
		    if (shiftZ < rangeZ) endZ = (int)(GRID_SIZE*shiftZ/rangeZ);

		    for (int ix = startX;ix <= endX;ix++)
			for (int iy = startY;iy <= endY;iy++)
			    for (int iz = startZ;iz <= endZ;iz++) {
				ArrayList<Atom> atoms = grid[ix][iy][iz];
				if (atoms == null) continue;
				int n = atoms.size();
				for (int i = 0;i < n;i++) {
				    Atom a2 = (Atom)atoms.get(i);
				    if (a2 == null) continue;
				    char alt2 = a2.getAlternative();
				    if (alt1 != ' ' && alt2 != ' ' &&
					alt1 != alt2) continue;
				    double covRad2 = 0.3;
				    if (a2.getElement() != null)
					covRad2 = a2.getElement().
					    getCovalentRadius();
				    double max  = covRad1 + covRad2 + 0.5;
				    double max2 = max*max;
				    double dx = x - a2.getX();
				    double dy = y - a2.getY();
				    double dz = z - a2.getZ();
				    double dist2 = dx*dx;
				    if (dist2 > max2) continue;
				    dist2 += dy*dy;
				    if (dist2 > max2) continue;
				    dist2 += dz*dz;
				    if (dist2 > max2) continue;
				    Bond b = Bond.create(a1,a2);
				    a1.addBond(b);
				    a2.addBond(b);
				}
			    }
		    shiftX -= MAX_BOND_LENGTH;
		    shiftY -= MAX_BOND_LENGTH;
		    shiftZ -= MAX_BOND_LENGTH;
		    int gridX = (int)(GRID_SIZE*shiftX/rangeX);
		    int gridY = (int)(GRID_SIZE*shiftY/rangeY);
		    int gridZ = (int)(GRID_SIZE*shiftZ/rangeZ);
		    ArrayList<Atom> atoms = grid[gridX][gridY][gridZ];
		    if (atoms == null) {
			atoms = new ArrayList<Atom>(2);
			grid[gridX][gridY][gridZ] = atoms;
		    }
		    atoms.add(a1);
 		}
	}
    }
}