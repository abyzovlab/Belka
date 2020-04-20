package belka.draw;

/**
 * Constants for drawings.
 *
 * @author Alexej Abyzov
 */

class DrawConstants
{
    // Maximal visible radius of an atom
    public final static int MAXIMAL_RADIUS = 255;

    // Array with width for sphere radii
    public static final int[] radChordIndex = new int[MAXIMAL_RADIUS + 1];
    public static final int[] precalcChords =
	new int[(MAXIMAL_RADIUS + 1)*(MAXIMAL_RADIUS + 2)];

    // Array with values for maximal light intensity for radii
    public static final int[] radMaxInten = new int[MAXIMAL_RADIUS + 1];

    // Array with values of color for particular intensity and radius
    public static final int[] intenColor = new int[(MAXIMAL_RADIUS + 1)<<3];

    // Cordinates of the sphere arc
    public final static int MAX_N_ARC_PIXELS = MAXIMAL_RADIUS<<4;
    public static final int arcx[] = new int[MAX_N_ARC_PIXELS];
    public static final int arcy[] = new int[MAX_N_ARC_PIXELS];
    public static final int arcz[] = new int[MAX_N_ARC_PIXELS];

    // Color of the sphere arc
    public static final int arcc1[] = new int[MAX_N_ARC_PIXELS];
    public static final int arcc2[] = new int[MAX_N_ARC_PIXELS];


    static boolean init = false;
    public static void init()
    {
	if (init) return;

	int index     = 0;
	int rad_index = 0;
	for (int r = 0;r <= MAXIMAL_RADIUS;r++) {
	    int r2 = r*r;
	    radChordIndex[rad_index++] = index;
	    for (int x = -r;x <= r;x++)
		precalcChords[index++] = (int)(Math.sqrt(r2 - x*x) + 0.5);
	}
	
	calcMaxInten(1,1,1);
	init = true;
    }

    public static void calcMaxInten(int x_coeff,int y_coeff,int z_coeff)
    {
	for (int i = 0;i <= MAXIMAL_RADIUS;i++)
	    radMaxInten[i] = (int)(i*Math.sqrt(x_coeff*x_coeff +
					       y_coeff*y_coeff +
					       z_coeff*z_coeff));

    }
}
