package belka.draw;

//--- Java imports ---
import java.awt.*;

/**
 * Objects of this class store the array of the color shade on a sphere
 * depending on intensity of the light.
 *
 * @author Alexej Abyzov
 */

class ShadeArray
{
    public ShadeArray() {}

    public void init(Color col,int max)
    {
	col_ = col;
	max_ = max;
	if (max_ < 0) max_ = 0;
	if (max_ >= MAX_MAX) max_ = MAX_MAX - 1;

// 	int addMax_r = 255 - max_r;
// 	int addMax_g = 255 - max_g;
// 	int addMax_b = 255 - max_b;
// 	int addMax = addMax_r;
// 	if (addMax_g < addMax) addMax = addMax_g;
// 	if (addMax_b < addMax) addMax = addMax_b;
// 	max_r += addMax;
// 	max_g += addMax;
// 	max_b += addMax;

	int r_col = 0, g_col = 0, b_col = 0;
	if (col_ != null) {
	    r_col = col_.getRed();
	    g_col = col_.getGreen();
	    b_col = col_.getBlue();
	}

	shadeForInten_[0] = ((255   << 24)   |
			     (r_col >> 1 << 16) |
			     (g_col >> 1 <<  8) |
			     (b_col >> 1 <<  0));

	for (int inten = 1;inten <= max_;inten++) {
	    int r = r_col>>1, r_add = r_col - r;
	    int g = g_col>>1, g_add = g_col - g;
	    int b = b_col>>1, b_add = b_col - b;
	    int inten_tmp = inten;
	    max = max_;
	    while (max > 1) {
		max = max >> 1;
		r_add = r_add >> 1;
		g_add = g_add >> 1;
		b_add = b_add >> 1;
		if (inten_tmp >= max) {
		    inten_tmp -= max;
		    r += r_add;
		    g += g_add;
		    b += b_add;
		}
	    }
	    shadeForInten_[inten] = ((255 << 24) |
				     (r   << 16)   |
				     (g   <<  8)   |
				     (b   <<  0));
	}
    }

    /**
     * Base color. The shade is equal to the color when the intensity is 0.
     */
    private Color col_ = null;
    public Color getColor() { return col_; }

    /**
     * Maximum of the intensity
     */
    private int max_ = 0;
    public  int getMax() { return max_; }

    /**
     * Array of shades for intensities.
     */
    final static int MAX_MAX = 256<<4;
    int shadeForInten_[] = new int[MAX_MAX];
    int shadeForInten(int inten)
    {
	if (inten < 0)    return shadeForInten_[0];
	if (inten > max_) return shadeForInten_[max_];
	return shadeForInten_[inten];
    }
}