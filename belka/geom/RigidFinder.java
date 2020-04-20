package belka.geom;

//--- Java imports ---
import java.lang.*;
import java.util.*;
import java.io.*;

//--- Application imports ---
import belka.mol.*;

/**
 * Objects of this class perform separation of a structure into several rigid
 * blocks.
 * 
 * @author Alexej Abyzov
 */

public class RigidFinder
{
    private static final int    N_TRACE      =  50;
    private static final double DEF_MAX_D    = 2.5;
    private static final int    MIN_FRG_SIZE =   4;

    // Number of seeds to trace
    int n_trace_ = N_TRACE;
    //public void setNTrace(int n) { n_trace_ = n; }

    // Chains
    Chain[] chains1_ = null, chains2_ = null;
    Pair    fPair_   = null, lPair_   = null;

    // Aligned assemblies
    Pair[] pairs_ = null;

    // Interresidue distance matrices
    double[][] delta_d_ = null, aver_d_ = null;

    // Delta distance flag matrix
    boolean[][] delta_bad_ = null;

    // Matrices for tracking  
    short[][]    ne_    = null;
    short[][][]  trace_ = null;

    /**
     * Object constructor. 
     */
    public RigidFinder() {}

    /**
     * Object constructor. Both submitted chains must have structure.
     * The resdiue correspondance is derived from alignment.
     */
    public RigidFinder(Chain chain1,Chain chain2)
    {
	this(new Chain[]{chain1},new Chain[]{chain2});
    }

    /**
     * Object constructor. Arrays must be of the same length.
     * Chains are considered to be correspoding if they have the same
     * indexes in arrays. All submitted chains must have structure.
     * The resdiue correspondance is derived from alignment.
     */
    public RigidFinder(Chain[] chains1,Chain[] chains2)
    {
	if (chains1 == null || chains2 == null) return;
	if (chains1.length  != chains2.length)   return;

	chains1_ = chains1;
	chains2_ = chains2;

	short n_aligned = 0;
	for (int i = 0;i < chains1.length;i++) {
	    Chain chain1 = chains1[i];
	    Chain chain2 = chains2[i];
	    if (chain1 == null || chain2 == null) continue;
	    for (Assembly a1 = chain1.assemblyList(),
		     a2 = chain2.assemblyList();a1 != null && a2 != null;
		 a1 = a1.next(), a2 = a2.next()) {
		boolean isGap = (a1.isGap() || a2.isGap());
		Pair newPair = new Pair(a1,a2,!isGap);
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
		if (!isGap) n_aligned++;
	    }
	}

	if (n_aligned <= 0) return;

	pairs_ = new Pair[n_aligned];
	n_aligned = 0;
	for (Pair p = fPair_;p != null;p = p.next())
	    if (p.isOfInterest()) {
		p.setShortValue(n_aligned);
		pairs_[n_aligned++] = p;
	    }

 	calcDistMatrix();
    }

    /**
     * Returns number of rigid blocks the structure can be split in.
     * Calculations are performed with the default value of the delta
     * distance parameter defining degree of interresidue conservation within
     * rigid block. Blocks are refined and small framgnets are clustered with
     * larger ones.
     *
     * See {@link #findRigids(double,boolean,boolean,File)}.
     *
     * @return number of rigid blocks the structure can be split in.
     */
    public int findRigids() { return findRigids(DEF_MAX_D,true,true,null); }

    /**
     * Returns number of rigid blocks the structure can be split in.
     * Calculations are performed with the given value of the delta
     * distance parameter defining degree of interresidue conservation within
     * rigid block. Blocks are refined and small framgnets are clustered with
     * larger ones.
     *
     * See {@link #findRigids(double,boolean,boolean,File)}.
     *
     * @return number of rigid blocks the structure can be split in.
     */
    public int findRigids(double deltaDist)
    {
	return findRigids(deltaDist,true,true,null);
    }

    /**
     * Returns number of rigid blocks the structure can be split in.
     * Calculations are performed with the given value of the delta
     * distance parameter defining degree of interresidue conservation within
     * rigid block. Blocks can be refined and small framgnets can be
     * clustered with larger ones.
     *
     * @param deltaDist maximal allowed deviation of interresidue distance
     * within rigid body.
     * @param ref  flag to specify whether blocks has to be refined.
     * @param clus flag to specify whether clustering of small fragmnet has to
     * be made.
     * @param file file to save output.
     *
     * @return number of rigid blocks the structure can be split in.
     */
    public int findRigids(double deltaDist,boolean ref,boolean clus,
			  File file)
    {
	if (pairs_ == null) return -1;
	if (deltaDist <  0) return -1;

	long start_time = System.currentTimeMillis();

	 // Cleaning group id
	resetGID();

	// Setting flag for delta distances
 	calcFlagMatrix(deltaDist);

	int n_res = pairs_.length;
	ne_    = new short[n_res][N_TRACE]; 
	trace_ = new short[n_res][N_TRACE][2]; // 0 - res, 1 - seed

	int n_found = 0;
	short gid = 0;
	do {
	    gid++;

	     // Initial finding of rigid blocks
	    Arrays.sort(pairs_,new IndexComparator());
	    n_found = findLargest(gid);
	    if (n_found < MIN_FRG_SIZE) break;

	    if (ref) { // Refining
		clearShortFragments(gid);
		n_found = refine(gid);
	    }

	    if (clus) { // Clustering
		for (int gap_size = 1;gap_size < MIN_FRG_SIZE;gap_size++) {
		    clusterFragments(gap_size,gid); // Cluster fragments
		    clusterFragments(gap_size,0);   // Cluster gaps
		}
		// We need to clearShortFragments since short fragments can be
		// left within rigid blocks found before
		clearShortFragments(gid);
		n_found = getNeFor(gid);
	    }
	    if (n_found < MIN_FRG_SIZE) break;

	    // Separation of only one compact block
	    // We have to do it after clustering since clustering removes
	    // a lot of noise, occasional pair far away from main block.
	    n_found = findCompact(gid);
	    if (n_found < MIN_FRG_SIZE) break;

	    if (clus) { // Clustering again after finding compact
		for (int gap_size = 1;gap_size < MIN_FRG_SIZE;gap_size++) {
		    clusterFragments(gap_size,gid); // Cluster fragments
		    clusterFragments(gap_size,0);   // Cluster gaps
		}
		// We need to clearShortFragments since short fragments can be
		// left within rigid blocks found before
		clearShortFragments(gid);
		n_found = getNeFor(gid);
	    }

	} while (n_found >= MIN_FRG_SIZE);

	n_rigids_ = gid - 1;

	// Clearing left-over ids
	for (Pair p = fPair_;p != null;p = p.next())
	    if (p.getIntValue() > n_rigids_) p.setIntValue(0);

	// Swapping ids to have block sizes in accending order
	for (int id1 = 1;id1 <= n_rigids_;id1++) {
	    int max_id = id1;
	    int max_n  = getNeFor(id1);
	    for (int id2 = id1 + 1;id2 <= n_rigids_;id2++) {
		int new_n = getNeFor(id2);
		if (max_n  < new_n) {
		    max_n  = new_n;
		    max_id = id2;
		}
	    }
	    if (id1 != max_id) swapGID(id1,max_id);
	}

//   	for (int i = 1;i <= n_rigids_;i++) {
//   	    System.out.println("Checking for id " + i + "...");
//   	    checkGroupId(i,deltaDist);
//   	    System.out.println("Done.");
//   	}

	// Projecting gid to assemblies
	for (Pair p = fPair_;p != null;p = p.next()) {
	    Assembly s1 = (Assembly)p.getObject1();
	    Assembly s2 = (Assembly)p.getObject2();
	    s1.setGroupId(p.getIntValue());
	    s2.setGroupId(p.getIntValue());
	}

	if (file != null) writeOutputFile(deltaDist,ref,clus,file);

	long stop_time = System.currentTimeMillis();
	time_ = (stop_time - start_time)/1000.;

	System.out.println("RF time: " + getSpentTime());

	return n_rigids_;
    }

    /**
     * Refinement of found rigid block. Atoms are sorted by proximity to
     * the rigid block (specifyed by gid) and then the method is run again to
     * find rigid block.
     * 
     * @param gid id of rigid block.
     * @return number of atoms in rigid block.
     */
    private int refine(int gid)
    {
	// Sorting atoms
	sortForBlock(gid);
	
	// Resetting group flag
	for (int i = 0;i < pairs_.length;i++)
	    if (pairs_[i].getIntValue() == gid)
		pairs_[i].setIntValue(0);

	// Do actuall refining
	return findLargest((short)gid);
    }

    // Sorting atoms in given block
    private void sortForBlock(int gid)
    {
	// Initial value assignemnt
	int n = pairs_.length;
	for (int i = 0;i < n;i++) pairs_[i].setFloatValue(0);

	// Assigning minimal distance to a block by setDoubleValue.
	// Assigning average delta distance within block by setFloatValue.
	for (int i1 = 0;i1 < n;i1++) {
	    short ind1 = pairs_[i1].getShortValue();
	    if (pairs_[i1].getIntValue() == gid) {
		pairs_[i1].setDoubleValue(0);
		for (int i2 = i1 + 1;i2 < n;i2++) {
		    if (pairs_[i2].getIntValue() != gid) continue;
		    short ind2 = pairs_[i2].getShortValue();
// 		    pairs_[i1].setFloatValue(pairs_[i1].getFloatValue() +
// 					     (float)delta_d_[ind1][ind2]);
// 		    pairs_[i2].setFloatValue(pairs_[i2].getFloatValue() +
// 					     (float)delta_d_[ind1][ind2]);
		    pairs_[i1].setFloatValue(pairs_[i1].getFloatValue() +
					     (float)aver_d_[ind1][ind2]);
		    pairs_[i2].setFloatValue(pairs_[i2].getFloatValue() +
					     (float)aver_d_[ind1][ind2]);
		}
	    } else {
		double min_dist = 1e+100;
		for (int i2 = 0;i2 < n;i2++) {
		    if (pairs_[i2].getIntValue() != gid) continue;
		    short ind2 = pairs_[i2].getShortValue();
		    double dist = aver_d_[ind1][ind2];
		    if (dist < min_dist) min_dist = dist;
		}
		pairs_[i1].setDoubleValue(min_dist);
	    }
	}
	
	// Sorting pairs by the distance
 	Arrays.sort(pairs_,new DistanceComparator());
    }


    /**
     * Finds the largest rigids block and assigns gid to it. All atoms with
     * smaller gid are considered as already part of some rigid block.
     *
     * @param gid id for block assignment.
     * @return number of atoms in the block.
     */
    private int findLargest(short gid)
    {
	return findLargest(gid,false);
    }

    /**
     * Finds the largest rigids block and assigns gid to it. All atoms with
     * smaller gid are considered as already part of some rigid block.
     *
     * @param gid id for block assignment.
     * @param reverse flag to indicate that search should be take from C- to
     * N-terminal.
     * @return number of atoms in the block.
     */
    private int findLargest(short gid,boolean reverse)
    {
	short n_res = (short)pairs_.length;
	short start1 = 0, end1 = n_res, step1 =  1;
	if (reverse) {
	    start1 = (short)(n_res - 1);
	    end1   = -1;
	    step1  = -1;
	}

	for (short r1 = start1;r1 != end1;r1 += step1) {
	    if (pairs_[r1].getIntValue() > 0 &&
		pairs_[r1].getIntValue() < gid) continue;
	    for (int s = 0;s < n_trace_;s++) {
		trace_[r1][s][0] = trace_[r1][s][1] = -1;
		ne_[r1][s] = 0;
	    }
	    ne_[r1][0] = 1;
	    int min_ind = -1, n_traced = 1;
	    short ind1 = pairs_[r1].getShortValue();

	    short start2 = (short)(r1 - 1), end2 = -1, step2 = -1;
	    if (reverse) {
		start2 = (short)(r1 + 1);
		end2   = n_res;
		step2  =     1;
	    }
 	    for (short r2 = start2;r2 != end2;r2 += step2) {
		if (pairs_[r2].getIntValue() > 0 &&
		    pairs_[r2].getIntValue() < gid) continue;
		for (short s = 0;s < n_trace_;s++) {
		    if (ne_[r2][s] <= 0) break;
		    if (n_traced == n_trace_ &&
			ne_[r2][s] < ne_[r1][min_ind]) continue;
		    short ne_new = ne_[r2][s]; ne_new++;
		    short r_tr = r2;
		    short s_tr = s;
		    boolean bad = false;
		    do { // Tracing back to check distances
			short ind_tr = pairs_[r_tr].getShortValue();
			if (delta_bad_[ind_tr][ind1]) {
			    bad = true;
			    break;
			}
			short r_tr_new = trace_[r_tr][s_tr][0];
			short s_tr_new = trace_[r_tr][s_tr][1];
			r_tr = r_tr_new;
			s_tr = s_tr_new;
		    } while (r_tr >= 0 && s_tr >= 0);
		    if (bad) continue;
		    
		    // Storing new seed
		    int index_put = -1;
		    if (n_traced < n_trace_) {
			index_put = n_traced;
			n_traced++;
		    } else {
			if (ne_new > ne_[r1][min_ind]) index_put = min_ind;
		    }
		    if (index_put < 0) continue;
		    ne_[r1][index_put]       = ne_new;
		    trace_[r1][index_put][0] = r2;
		    trace_[r1][index_put][1] = s;
		    
		    // Updating index with minimal ne
		    int min = ne_[r1][1];
		    min_ind = 1;
		    for (int i = 2;i < n_traced;i++)
			if (ne_[r1][i] < min) {
			    min = ne_[r1][i];
			    min_ind = i;
			}
		}
	    }
	}
	// Finding best solution
	int r_best = -1, s_best = -1, ne_best = 0;
	for (int r = 0;r < n_res;r++) {
	    if (pairs_[r].getIntValue() > 0 &&
		pairs_[r].getIntValue() < gid) continue;
	    for (int s = 0;s < n_trace_;s++) {
		if (ne_[r][s] > ne_best) {
		    ne_best = ne_[r][s];
		    r_best = r;
		    s_best = s;
		}
	    }
	}
	if (r_best < 0 || s_best < 0) return ne_best;

	for (int r_tr = r_best, s_tr = s_best;r_tr >= 0 && s_tr >= 0;) {
	    pairs_[r_tr].setIntValue(gid);
	    int r_tr_new = trace_[r_tr][s_tr][0];
	    int s_tr_new = trace_[r_tr][s_tr][1];
	    r_tr = r_tr_new;
	    s_tr = s_tr_new;
	}
	
	return ne_best;
    }

    /**
     * Finds the compact rigid block within the block with gid.
     * Atoms are clustered by single linkage clustering with 
     * cut off of 10.0 A.
     *
     * @param gid id of rigid block.
     * @return number of atoms in the compact block.
     */
    private int findCompact(int gid)
    {
 	// Sorting atoms
 	sortForBlock(gid);

	// Checking centroid
	if (pairs_[0].getIntValue() != gid) return 0;

	// Setting distance to the first atom/centroid
	short ind0 = pairs_[0].getShortValue();
	pairs_[0].setDoubleValue(0);
	pairs_[0].setFloatValue(0);
	for (int i = 1;i < pairs_.length;i++) {
	    short ind = pairs_[i].getShortValue();
	    pairs_[i].setDoubleValue(aver_d_[ind0][ind]);
	    pairs_[i].setFloatValue(0);
	}

	// Sorting atoms by that distance
 	Arrays.sort(pairs_,new DistanceComparator());

	// Clustering
	int ret = 1;
	for (int i1 = 1;i1 < pairs_.length;i1++) {
	    if (pairs_[i1].getIntValue() != gid) continue;
	    pairs_[i1].setIntValue(0); // Setting by default zero
	    short ind1 = pairs_[i1].getShortValue();
	    for (int i2 = 0;i2 < i1;i2++) {
		if (pairs_[i2].getIntValue() != gid) continue;
		short ind2 = pairs_[i2].getShortValue();
		if (aver_d_[ind1][ind2] < 10.0) {
		    pairs_[i1].setIntValue(gid);
		    ret++;
		    break;
		}
	    }
	}
	
	return ret;
    }

    /**
     * Cluster fragments with particular gid and remove gaps defined
     * by gap_size.
     *
     * @param gap_size size of gaps that can be clustered.
     * @param gid id to be assigned to a rigid block.
     */
    private void clusterFragments(int gap_size,int gid)
    {
	for (Pair p = fPair_;p != null;p = p.next()) {
	    int len_frg = 0;
	    Pair p_start = null, p_end = null;
	    if (p.getIntValue() == gid) {
		len_frg++;
		p_start = p_end = p;
		p = p.next();
		while (p != null && p.getIntValue() == gid &&
		       p.isConnectedTo(p_end)) {
		    p_end = p;
		    p = p.next();
		    len_frg++;
		}
	    }
	    if (len_frg >= MIN_FRG_SIZE) {
		clusterOnSide(gap_size,gid,p_start,false);
		clusterOnSide(gap_size,gid,p_end,  true);
	    }

	    // Scroling up to the next gap
	    while (p != null && p.getIntValue() == gid) p = p.next();

	    if (p == null) break;
	}
    }

    // Clustering short fragments to the right/after or left/before the given
    // atoms pair.
    private void clusterOnSide(int gap_size,int gid,
			       Pair p_start,boolean after)
    {
	Pair p = p_start.prev();
	if (after) p = p_start.next();

	int n_between = 0;
	while (p != null && p.getIntValue() != gid) {
	    if (p.isOfInterest()) n_between++;
	    if (after) p = p.next();
	    else       p = p.prev();
	}

 	if (p == null) { // Reach an end
 	    if (n_between > gap_size - 1) return;
	} else {
 	    if (n_between > gap_size) return;
	}

	// Setting gid for residues in gaps
	while (p_start != p) {
	    if (p_start.isOfInterest())	p_start.setIntValue(gid);
	    if (after) p_start = p_start.next();
	    else       p_start = p_start.prev();
	}
	
	// Scrolling up/down to next gap
	while (p != null && p.getIntValue() == gid) {
	    p_start = p;
	    if (after) p = p.next();
	    else       p = p.prev();
	}

	// If at the end
	if (p == null) return;

	// Cluster before
	clusterOnSide(gap_size,gid,p_start,after);
    }

    private void clearShortFragments(int gid)
    {
	for (Pair p = fPair_;p != null;p = p.next()) {
	    int len_frg = 0;
	    Pair p_start = null, p_end = null;
	    if (p.getIntValue() == gid) {
		len_frg++;
		p_start = p_end = p;
		p = p.next();
		while (p != null && p.getIntValue() == gid &&
		       p.isConnectedTo(p_end)) {
		    p_end = p;
		    p = p.next();
		    len_frg++;
		}
	    }

	    if (len_frg > 0 && len_frg < MIN_FRG_SIZE)
		while (p_start != p) {
		    p_start.setIntValue(0);
		    p_start = p_start.next();
		}

	    if (p == null) break;
	}
    }

    private double time_ = 0; // Time spent on last call of findRigids
    /**
     * Returns time in seconds spent on last call to function
     * {@link #findRigids(double,boolean,boolean,File)}.
     *
     * @return time in seconds spent on last call to function
     * {@link #findRigids(double,boolean,boolean,File)}.
     */
    public  double getSpentTime() { return time_; }

    /**
     * Function prints information about rigid blocks into a string and returns
     * it. Each line contains information about fragments in one rigid block.
     * The inforamtion about rigid blocks is stored in the following format
     * (c1c2, start1,start2,length), where c1c2 -- chains id of aligned chains,
     * start1 -- is the residues serial number in the first chain,
     * start2 -- is the serial number of corresponding 
     * residue in the second chain, and length -- is the length of the
     * fragments.
     *
     * @param chains1 first set of chains.
     * @param chains2 second set of chains.
     *
     * @return string representation of rigid blocks.
     */
    public StringBuffer print(Chain[] chains1,Chain[] chains2)
    {
	if (chains1 == null || chains2 == null) return null;
	if (chains1.length  != chains2.length) return null;

	int n_rigids = 0;
	for (int i = 0;i < chains1.length;i++)
	    for (Assembly a = chains1[i].assemblyList();a != null;a = a.next())
		if (!a.isGap() && a.getGroupId() > n_rigids)
		    n_rigids = a.getGroupId();
	for (int i = 0;i < chains2.length;i++)
	    for (Assembly a = chains2[i].assemblyList();a != null;a = a.next())
		if (!a.isGap() && a.getGroupId() > n_rigids)
		    n_rigids = a.getGroupId();

	StringWriter ret = new StringWriter();
	if (n_rigids == 0) return ret.getBuffer();

	PrintWriter  pw  = new PrintWriter(ret);
	for (int r = 1;r <= n_rigids;r++) {

	    int n_res = 0;
	    for (int i = 0;i < chains2.length;i++)
		for (Assembly a1 = chains1[i].assemblyList(),
			 a2 = chains2[i].assemblyList();
		     a1 != null && a2 != null;a1 = a1.next(), a2 = a2.next())
		    if (a1.getGroupId() == r && a2.getGroupId() == r) n_res++;
	    pw.print(n_res + " ");

	    int start1 = 0, start2 = 0, length = 0;
	    for (int i = 0;i < chains2.length;i++) {
		for (Assembly a1 = chains1[i].assemblyList(),
			 a2 = chains2[i].assemblyList();
		     a1 != null && a2 != null;a1 = a1.next(), a2 = a2.next()) {
		    if (a1.getGroupId() == r && a2.getGroupId() == r) {
			if (length == 0) {
			    start1 = a1.getSerialNum();
			    start2 = a2.getSerialNum();
			    length = 1;
			} else {
			    if (a1.getSerialNum() == start1 + length &&
				a2.getSerialNum() == start2 + length) length++;
			    else {
				pw.print(" (" + chains1[i].getId() +
					 chains2[i].getId() + "," + start1 +
					 "," + start2 + "," + length + ")");
				start1 = a1.getSerialNum();
				start2 = a2.getSerialNum();
				length = 1;
			    }
			}
		    } else {
			if (length != 0) pw.print(" (" + chains1[i].getId() +
						  chains2[i].getId() + "," +
						  start1 + "," + start2 + "," +
						  length + ")");
			length = 0;
			continue;
		    }
		}
		if (length != 0)
		    pw.print(" (" + chains1[i].getId() + chains2[i].getId() +
			     "," + start1 + "," + start2 + "," + length +
			     ")");
		length = 0;
	    }
	    pw.println();
	}
	
	pw.flush();
	pw.close();

	return ret.getBuffer();
    }
    
    /**
     * The function writes fragments of rigid blocks into a file.
     */
    void writeOutputFile(double deltaDist,boolean ref,boolean clus,File file)
    {
	PrintWriter pw = null;
	try                  { pw = new PrintWriter(file); }
	catch (Exception ex) { return; }

	String line = "";
	Molecule mol1 = chains1_[0].molecule();
	if (mol1 != null) line += mol1.getName();
	else              line += chains1_[0].getId();
	line += " ";
	Molecule mol2 = chains2_[0].molecule();
	if (mol2 != null) line += mol2.getName();
	else              line += chains2_[0].getId();
	line += " ";
	line += deltaDist;
	line += " ";
	if (ref) line += "refine";
	else     line += "norefine";
	line += " ";
	if (clus) line += "cluster";
	else      line += "nocluster";

	pw.println(line);

	StringBuffer frags = print(chains1_,chains2_);
	if (frags != null) pw.println(frags.toString());

	pw.close();
    }

    private int n_rigids_ = -1; // Number of rigid blocks found
    /**
     * Returns number of found rigid blocks. Negative if no search was made.
     *
     * @return number of found rigid blocks. 
     */
    public  int getNRigids() { return n_rigids_; }

    // Calculated matrix with flag whether delta is below or about specified
    // threshould.
    private void calcFlagMatrix(double maxDelta)
    {
	int n = pairs_.length;
	delta_bad_ = new boolean[n][n];
	for (int i1 = 0;i1 < n;i1++)
	    for (int i2 = i1;i2 < n;i2++) {
		double delta = delta_d_[i1][i2];
		if (delta < 0) delta = -delta;
		boolean flag = true;
		if (delta <= maxDelta) flag = false;
		delta_bad_[i1][i2] = delta_bad_[i2][i1] = flag;
	    }
    }

    // Calculated distance matrices
    private void calcDistMatrix()
    {
	int n = pairs_.length;
	delta_d_ = new double[n][n];
	aver_d_  = new double[n][n];
	for (int i1 = 0;i1 < n;i1++) {
	    Assembly s1 = (Assembly)pairs_[i1].getObject1();
	    Assembly s2 = (Assembly)pairs_[i1].getObject2();
	    Atom a1 = s1.getMainAtom();
	    Atom a2 = s2.getMainAtom();
	    if (a1 == null)
		System.err.println("WARNING: Missing CA atom.");
	    if (a2 == null)
		System.err.println("WARNING: Missing CA atom.");
	    for (int i2 = i1;i2 < n;i2++) {
		Assembly s1to1 = (Assembly)pairs_[i2].getObject1();
		Assembly s2to2 = (Assembly)pairs_[i2].getObject2();
		Atom a1to1 = s1to1.getMainAtom();
		Atom a2to2 = s2to2.getMainAtom();
		double d1 = 0;
		if (a1 != null && a1to1 != null) {
		    double dx = a1.getX() - a1to1.getX();
		    double dy = a1.getY() - a1to1.getY();
		    double dz = a1.getZ() - a1to1.getZ();
		    d1 = Math.sqrt(dx*dx + dy*dy + dz*dz);
		}
		double d2 = 0;
		if (a2 != null && a2to2 != null) {
		    double dx = a2.getX() - a2to2.getX();
		    double dy = a2.getY() - a2to2.getY();
		    double dz = a2.getZ() - a2to2.getZ();
		    d2 = Math.sqrt(dx*dx + dy*dy + dz*dz);
		}
		double delta = d2 - d1;
		if (delta < 0) delta = -delta;
		delta_d_[i1][i2] = delta_d_[i2][i1] = delta;
		double average = (d2 + d1)/2.;
		aver_d_[i1][i2] = aver_d_[i2][i1] = average;
	    }
	}
    }

    // Returns number of pairs with given gid.
    private int getNeFor(int gid)
    {
	int ret = 0;
	for (Pair p = fPair_;p != null;p = p.next())
	    if (p.getIntValue() == gid) ret++;
	return ret;
    }

    // Swaps assigned gid for all pairs. That is, if pair had gid1 it will
    // have gid2 and vice-versa.
    private void swapGID(int gid1,int gid2)
    {
	for (Pair p = fPair_;p != null;p = p.next())
	    if (p.getIntValue() == gid1)      p.setIntValue(gid2);
	    else if (p.getIntValue() == gid2) p.setIntValue(gid1);
    }

    // Reset int value for all pairs to zero.
    private void resetGID()
    {
	for (Pair p = fPair_;p != null;p = p.next()) p.setIntValue(0);
    }

    // Function to check the final rigid body definition
    void checkGroupId(int gid,double max_delta)
    {
	for (Pair p1 = fPair_;p1 != null;p1 = p1.next()) {
	    if (p1.getIntValue() != gid) continue;
	    short ind1 = p1.getShortValue();
	    for (Pair p2 = p1.next();p2 != null;p2 = p2.next()) {
		if (p2.getIntValue() != gid) continue;
		short ind2 = p2.getShortValue();
		double del = delta_d_[ind1][ind2];
		if (del > max_delta)
		    System.err.println("Ooops: delta " + del + 
				       " flag " + delta_bad_[ind1][ind2] +
				       " for gid " + gid);
	    }
	}

	
    }

    // The class implements comparator for pairs.
    // Comparison is made by assisiated double values.
    private class DistanceComparator implements Comparator<Pair>
    {
	public int compare(Pair obj1,Pair obj2)
	{
	    Pair p1 = (Pair) obj1;
	    Pair p2 = (Pair) obj2;
	    if (p1 == null || p2 == null) {
		System.err.println("WARNING: null object in pair.");
		return 0;
	    }
	    double d1 = p1.getDoubleValue();
	    double d2 = p2.getDoubleValue();
	    if (d1 < d2)      return -1;
	    else if (d1 > d2) return  1;
	    else {
 		float f1 = p1.getFloatValue();
 		float f2 = p2.getFloatValue();
 		if (f1 < f2)      return -1;
 		else if (f1 > f2) return  1;
	    }
	    return 0;
	}
    }

    // The class implements comparator for pairs.
    // Comparison is made by assisiated double values.
    private class IndexComparator implements Comparator<Pair>
    {
	public int compare(Pair obj1,Pair obj2)
	{
	    Pair p1 = (Pair) obj1;
	    Pair p2 = (Pair) obj2;
	    if (p1 == null || p2 == null) {
		System.err.println("WARNING: null object in pair.");
		return 0;
	    }
	    short ind1 = p1.getShortValue();
	    short ind2 = p2.getShortValue();
	    if (ind1 < ind2)      return -1;
	    else if (ind1 > ind2) return  1;
	    else                  return  0;
	}
    }
}
