package belka.align;

//--- Java imports ---
import java.awt.*;
import java.util.*;

//--- Application imports ---
import belka.mol.*;

/**
 * Objects of this class perform sequence alignemnts by Needleman-Wunsch and 
 * Smith-Waterman algorithms.
 * 
 * @author Alexej Abyzov
 */
public class SeqAligner
{
    private static Hashtable<Character,Integer> index_hash =
	new Hashtable<Character,Integer>(25);

    /**
     * Contructor of SeqAligner object with default values:
     * scoring matrix: BLOSUM62
     * gap open penalty: -10
     * gap extend penalty: -1
     */
    public SeqAligner()
    {
	index_hash.put(new Character('A'),new Integer( 0));
	index_hash.put(new Character('R'),new Integer( 1));
	index_hash.put(new Character('N'),new Integer( 2));
	index_hash.put(new Character('D'),new Integer( 3));
	index_hash.put(new Character('C'),new Integer( 4));
	index_hash.put(new Character('Q'),new Integer( 5));
	index_hash.put(new Character('E'),new Integer( 6));
	index_hash.put(new Character('G'),new Integer( 7));
	index_hash.put(new Character('H'),new Integer( 8));
	index_hash.put(new Character('I'),new Integer( 9));
	index_hash.put(new Character('L'),new Integer(10));
	index_hash.put(new Character('K'),new Integer(11));
	index_hash.put(new Character('M'),new Integer(12));
	index_hash.put(new Character('F'),new Integer(13));
	index_hash.put(new Character('P'),new Integer(14));
	index_hash.put(new Character('S'),new Integer(15));
	index_hash.put(new Character('T'),new Integer(16));
	index_hash.put(new Character('W'),new Integer(17));
	index_hash.put(new Character('Y'),new Integer(18));
	index_hash.put(new Character('V'),new Integer(19));
	index_hash.put(new Character('B'),new Integer(20));
	index_hash.put(new Character('J'),new Integer(21));
	index_hash.put(new Character('Z'),new Integer(22));
	index_hash.put(new Character('X'),new Integer(23));
	index_hash.put(new Character('*'),new Integer(24));
    }

    /**
     * Contructor of SeqAligner object with specified scoring matrix,
     * gap open penalty, and gap extend penalty.
     *
     * @param matrix name of scoring matrix.
     * @param gap_open gap open penalty.
     * @param gap_extend gap extend penalty.
     */
    public SeqAligner(String matrix,int gap_open,int gap_extend)
    {
	this();
	if (!setScoringMatrix(matrix))
	    System.err.println("Matrix " + matrix + " is not accepted. " +
			       "Using default " + matrix_ + ".");

	if (!setGapOpen(gap_open)) 
	    System.err.println("Value " + gap_open + " is not accepted. " +
			       "Using default " + gap_open_ + ".");

	if (!setGapExtend(gap_extend))
	    System.err.println("Value " + gap_extend + " is not accepted. " +
			       "Using default " + gap_extend_ + ".");
    }

    // Flag to indicate that alignment was produced
    private boolean hasAli_ = false;
    /**
     * Returns true if the object has alignment, false otherwise.
     *
     * @return true if the object has alignment, false otherwise.
     */
    public boolean hasAlignment() { return hasAli_; }


    // Length of first aligned sequence
    private int len1_ = 0;
    /**
     * Returns lengths of first aligned sequence.
     *
     * @return lengths of first aligned sequence.
     */
    public int getLength1() { return len1_; }

    // Length of second aligned sequence
    private int len2_ = 0;
    /**
     * Returns lengths of second aligned sequence.
     *
     * @return lengths of second aligned sequence.
     */
    public int getLength2() { return len2_; }

    // Aligned part of first sequence
    private String ali_seq1_ = "";
    /**
     * Returns aligned part of first sequence.
     *
     * @return aligned part of first sequence.
     */
    public String getAliSequence1() { return ali_seq1_; }

    // Aligned part of second sequence
    private String ali_seq2_ = "";
    /**
     * Returns aligned part of second sequence.
     *
     * @return aligned part of second sequence.
     */
    public String getAliSequence2() { return ali_seq2_; }

    // Length of alignment
    private int n_ali_ = 0;
    /**
     * Returns lengths of alignment;
     *
     * @return lengths of alignment;
     */
    public int getAliLength() { return n_ali_; }

    // Number of identical residues in alignment
    private int n_ident_ = 0;
    /**
     * Returns number of identical residues in alignment.
     *
     * @return number of identical residues in alignment.
     */
    public int getNumIdentical() { return n_ident_; }

    // Number of aligned residues with positive score.
    private int n_pos_ = 0;
    /**
     * Returns number of aligned residues with positive score.
     *
     * @return number of aligned residues with positive score.
     */
    public int getNumPositive() { return n_pos_; }

    // Number of gaps
    private int n_gaps_ = 0;
    /**
     * Returns number of gaps in alignment.
     *
     * @return number of gaps in alignment.
     */
    public int getNumGaps() { return n_gaps_; }

    // Score of the alignments.
    private int score_ = 0;
    /**
     * Returns score of the alignment.
     *
     * @return score of the alignment.
     */
    public int getScore() { return score_; }

    // Aligned chains
    private Chain chain1_ = null;
    private Chain chain2_ = null;
    // Trace of alignment
    private int[][] trace_ = null;
    /**
     * The function introduces gaps in chains to reflect the calculated
     * alignment.
     */
    public void applyToChains()
    {
	if (chain1_ == null || chain2_ == null) return;
	if (trace_  == null)                    return;
	if (trace_.length == 0)                 return;

	int rem1 = chain1_.removeGaps();
	int rem2 = chain2_.removeGaps();

	Assembly a1 = null, a2 = null;
	int i = 0;
	int ind1 = -1, ind2 = -1;
	for (;i < trace_.length;i++) { // Initiating alignment
	    ind1 = trace_[i][0];
	    ind2 = trace_[i][1];
	    if (ind1 >= 0 || ind2 >= 0) {
		a1 = chain1_.assemblyList();
		a2 = chain2_.assemblyList();
		if (ind1 < 0) {
		    a1.insertBefore(Assembly.createGap());
		    a1 = a1.prev();
		} else if (ind2 < 0) {
		    a2.insertBefore(Assembly.createGap());
		    a2 = a2.prev();
		}
		break;
	    }
	}
	i++;
	for (int l_ind1 = ind1, l_ind2 = ind2;i < trace_.length;
	     i++,l_ind1 = ind1,l_ind2 = ind2) { // Continuing alignment
	    ind1 = trace_[i][0];
	    ind2 = trace_[i][1];
	    if (ind1 < 0 && ind2 < 0) continue;
	    else if (ind1 < 0) 	      a1.insertAfter(Assembly.createGap());
	    else if (ind2 < 0) 	      a2.insertAfter(Assembly.createGap());
	    else if (ind1 == l_ind1)  a1.insertAfter(Assembly.createGap());
	    else if (ind2 == l_ind2)  a2.insertAfter(Assembly.createGap());
	    a1 = a1.next();
	    a2 = a2.next();

	    if (a1 == null || a2 == null) {
		System.err.println("WARNING: premature termination.");
		break;
	    }

	}
	chain1_.updateAssemblyPointers();
	chain2_.updateAssemblyPointers();
	
	// Setting aligned flag
	a1 = chain1_.assemblyList();
	a2 = chain2_.assemblyList();
	while (a1 != null && a2 != null) {
	    a1.setAligned(!a2.isGap());
	    a2.setAligned(!a1.isGap());
	    a1 = a1.next();
	    a2 = a2.next();
	}
	while (a1 != null) { // Case when chain length is different
	    a1.setAligned(false);
	    a1 = a1.next();
	}
	while (a2 != null) { // Case when chain length is different
	    a2.setAligned(false);
	    a2 = a2.next();
	}
    }

    // Scoring matrix
    private final static String DEF_MATRIX = "BLOSUM62";
    private              String matrix_    = DEF_MATRIX;
    private byte[][] score_matrix_ = BLOSUM62;
    /**
     * Sets new scoring matrix.
     *
     * @param matrix name of scoring matrix.
     * @return true if value is accepted, false otherwise.
     */
    public boolean setScoringMatrix(String matrix)
    {
	if (matrix == null) return false;

	matrix = matrix.trim();
	if (BLOSUM45_KEY.equals(matrix))      score_matrix_ = BLOSUM45;
	else if (BLOSUM62_KEY.equals(matrix)) score_matrix_ = BLOSUM62;
	else if (BLOSUM80_KEY.equals(matrix)) score_matrix_ = BLOSUM80;
	else if (PAM30_KEY.equals(matrix))    score_matrix_ = PAM30;
	else if (PAM70_KEY.equals(matrix))    score_matrix_ = PAM70;
	else return false;

	matrix_ = matrix;
	return true;
    }
    /**
     * Returns current scoring matrix.
     *
     * @return current scoring matrix.
     */
    public String getScoringMatrix() { return matrix_; }


    // Penalty for gap opening
    private final static int DEF_GAP_OPEN   = -10;
    private              int gap_open_      = DEF_GAP_OPEN;
    /**
     * Sets new value for gap open penatly.
     *
     * @param gap_open gap open penalty.
     * @return true if value is accepted, false otherwise.
     */
    public boolean setGapOpen(int gap_open)
    {
	if (gap_open > 0) return false;
	gap_open_ = gap_open;
	return true;
    }
    /**
     * Returns current value of gap open penatly.
     *
     * @return current value of gap open penatly.
     */
    public int getGapOpen() { return gap_open_; }

    // Pentalty for gap extension
    private final static int DEF_GAP_EXTEND = -1;
    private              int gap_extend_    = DEF_GAP_EXTEND;
    /**
     * Sets new value for gap extention penatly.
     *
     * @param gap_extend gap extend penalty.
     * @return true if value is accepted, false otherwise.
     */
    public boolean setGapExtend(int gap_extend)
    {
	if (gap_extend > 0) return false;
	gap_extend_ = gap_extend;
	return true;
    }
    /**
     * Returns current value of gap extension penatly.
     *
     * @return current value of gap extension penatly.
     */
    public int getGapExtend() { return gap_extend_; }

    /**
     * Alignes two sets of assemblies (residues/nucleotides) with default
     * parameters using Smith-Waterman method.
     *
     * @param chain1 first chain to be aligned.
     * @param chain2 second chain to be aligned.
     * @return score of alignment.
     */
    public int align_sw(Chain chain1,Chain chain2)
    {
	return align_nw(chain1,chain2);
    }


    /**
     * Alignes two sets of assemblies (residues/nucleotides) with default
     * parameters using Needleman-Wunsch method.
     *
     * @param chain1 first chain to be aligned.
     * @param chain2 second chain to be aligned.
     * @return score of alignment.
     */
    public int align_nw(Chain chain1,Chain chain2)
    {
	if (chain1 == null || chain2 == null) return -1;

	chain1_ = chain1;
	chain2_ = chain2;

	len1_ = chain1_.countAssemblies();
	len2_ = chain2_.countAssemblies();

	Assembly[] arr1 = new Assembly[len1_];
	Assembly[] arr2 = new Assembly[len2_];
	int[] inds1 = new int[len1_];
	int[] inds2 = new int[len2_];
	len2_ = len1_ = 0;
	for (Assembly a = chain1_.assemblyList();a != null;a = a.next()) {
	    if (a.isGap()) continue;
	    arr1[len1_] = a;
	    inds1[len1_] = getScoreIndex(a.getLetterName());
	    len1_++;
	}
	for (Assembly a = chain2_.assemblyList();a != null;a = a.next()) {
	    if (a.isGap()) continue;
	    arr2[len2_] = a;
	    inds2[len2_] = getScoreIndex(a.getLetterName());
	    len2_++;
	}

	// Filling up scoring and trace matrices
	int[][]   score   = new int[len1_ + 1][len2_ + 1];
	short[][] trace_x = new short[len1_ + 1][len2_ + 1]; // Negative means
	short[][] trace_y = new short[len1_ + 1][len2_ + 1]; // gap is opened
	score[0][0] = 0; trace_x[0][0] =  0; trace_y[0][0] =  0;
	for (short i1 = 1;i1 < len1_ + 1;i1++) {
	    int i1m = i1 - 1;
	    score[i1][0]   = 0;
	    trace_x[i1][0] = (short)(-i1m);
	    trace_y[i1][0] = 0;
	}
	for (short i2 = 1;i2 < len2_ + 1;i2++) {
	    int i2m = i2 - 1;
	    score[0][i2]   = 0;
	    trace_x[0][i2] = 0;
	    trace_y[0][i2] = (short)(-i2m);
	}
	for (short i1 = 1;i1 < len1_ + 1;i1++) {
	    int i1m = i1 - 1;
	    for (short i2 = 1;i2 < len2_ + 1;i2++) {
		int i2m = i2 - 1;
		int diag = score[i1m][i2m] + 
		    score_matrix_[inds1[i1m]][inds2[i2m]];
		int left = score[i1m][i2];
		int up   = score[i1][i2m];
		boolean gap_opened_left = trace_x[i1m][i2] < 0;
		boolean gap_opened_up   = trace_y[i1][i2m] < 0;
		if (i2 != len2_)
		    if (gap_opened_left) left += gap_extend_;
		    else                 left += gap_open_;
		if (i1 != len1_)
		    if (gap_opened_up)   up   += gap_extend_;
		    else                 up   += gap_open_;
		if (diag >= left && diag >= up) {
		    score[i1][i2]   = diag;
		    trace_x[i1][i2] = (short)i1m;
		    trace_y[i1][i2] = (short)i2m;
		} else if (left >= diag && left >= up) {
		    score[i1][i2]   = left;
		    trace_x[i1][i2] = (short)(-i1m);
		    trace_y[i1][i2] = i2;
		} else {
		    score[i1][i2]   = up;
		    trace_x[i1][i2] = i1;
		    trace_y[i1][i2] = (short)(-i2m);
		}
	    }
	}

	// Trace back
	int tmp = len1_; if (len2_ > tmp) tmp = len2_;
	StringBuffer ali1 = new StringBuffer(tmp);
	StringBuffer ali2 = new StringBuffer(tmp);

	int ind1 = len1_, ind2 = len2_;
	int new1 = Math.abs(trace_x[ind1][ind2]);
	int new2 = Math.abs(trace_y[ind1][ind2]);
	int[][] tmp_trace = new int[len1_ + len2_][2];
	int n_trace = 0;
	score_ = score[ind1][ind2];
	n_ali_ = n_ident_ = n_pos_ = n_gaps_ = 0;
	while (new1 != ind1 || new2 != ind2) {
	    int ind1m = ind1 - 1, ind2m = ind2 - 1;
	    tmp_trace[n_trace][0] = ind1m;
	    tmp_trace[n_trace][1] = ind2m;
	    n_trace++;
	    if (ind1m >= 0 && ind2m >= 0) { // Indexes referr to sequences
		boolean non_gap = ind1m == new1 && ind2m == new2;
		if (non_gap || n_ali_ > 0 ) {
		    n_ali_++;
		    Assembly a1 = arr1[ind1m];
		    Assembly a2 = arr2[ind2m];
		    char c1 = '?', c2 = '?';
		    if (a1 != null) c1 = a1.getLetterName();
		    if (a2 != null) c2 = a2.getLetterName();
		    if (non_gap) {
			if (c1 == c2) n_ident_++;
			if (score_matrix_[inds1[ind1m]][inds2[ind2m]] > 0)
			    n_pos_++;
		    } else {
			n_gaps_++;
			if (new1 == ind1)
			    c1 = Assembly.createGap().getLetterName();
			if (new2 == ind2)
			    c2 = Assembly.createGap().getLetterName();
		    }
		    ali1.insert(0,c1);
		    ali2.insert(0,c2);
		}
	    }
	    ind1 = new1;
	    ind2 = new2;
	    new1 = Math.abs(trace_x[ind1][ind2]);
	    new2 = Math.abs(trace_y[ind1][ind2]);
	}

	ali_seq1_ = ali1.toString();
	ali_seq2_ = ali2.toString();

	trace_ = new int[n_trace][2];
	for (int i = 0, j = n_trace - 1;i < n_trace;i++,j--) {
	    trace_[j][0] = tmp_trace[i][0];
	    trace_[j][1] = tmp_trace[i][1];
	}

	hasAli_ = true;
	return score_;
    }

    /**
     * For a given residues name returns index to access value in scoring
     * matrices.
     *
     * @param name letter name of residues/nucleotide.
     * @return index to access value in scoring matrices.
     */
    private int getScoreIndex(char name)
    {
	Character key = new Character(name);
	if (index_hash.containsKey(key))
	    return (Integer)index_hash.get(key).intValue();
	else 
	    return (Integer)index_hash.get(new Character('*')).intValue();
    }












    // BLOSUM45 scoring matrix as used by BLAST
    private final static String BLOSUM45_KEY = "BLOSUM45";
    private final static byte[][] BLOSUM45 = {
	{ 5,-2,-1,-2,-1,-1,-1, 0,-2,-1,-1,-1,-1,-2,-1, 1, 0,-2,-2, 0,-1,-1,-1,-1,-5},
	{-2, 7, 0,-1,-3, 1, 0,-2, 0,-3,-2, 3,-1,-2,-2,-1,-1,-2,-1,-2,-1,-3, 1,-1,-5},
	{-1, 0, 6, 2,-2, 0, 0, 0, 1,-2,-3, 0,-2,-2,-2, 1, 0,-4,-2,-3, 5,-3, 0,-1,-5},
	{-2,-1, 2, 7,-3, 0, 2,-1, 0,-4,-3, 0,-3,-4,-1, 0,-1,-4,-2,-3, 6,-3, 1,-1,-5},
	{-1,-3,-2,-3,12,-3,-3,-3,-3,-3,-2,-3,-2,-2,-4,-1,-1,-5,-3,-1,-2,-2,-3,-1,-5},
	{-1, 1, 0, 0,-3, 6, 2,-2, 1,-2,-2, 1, 0,-4,-1, 0,-1,-2,-1,-3, 0,-2, 4,-1,-5},
	{-1, 0, 0, 2,-3, 2, 6,-2, 0,-3,-2, 1,-2,-3, 0, 0,-1,-3,-2,-3, 1,-3, 5,-1,-5},
	{ 0,-2, 0,-1,-3,-2,-2, 7,-2,-4,-3,-2,-2,-3,-2, 0,-2,-2,-3,-3,-1,-4,-2,-1,-5},
	{-2, 0, 1, 0,-3, 1, 0,-2,10,-3,-2,-1, 0,-2,-2,-1,-2,-3, 2,-3, 0,-2, 0,-1,-5},
	{-1,-3,-2,-4,-3,-2,-3,-4,-3, 5, 2,-3, 2, 0,-2,-2,-1,-2, 0, 3,-3, 4,-3,-1,-5},
	{-1,-2,-3,-3,-2,-2,-2,-3,-2, 2, 5,-3, 2, 1,-3,-3,-1,-2, 0, 1,-3, 4,-2,-1,-5},
	{-1, 3, 0, 0,-3, 1, 1,-2,-1,-3,-3, 5,-1,-3,-1,-1,-1,-2,-1,-2, 0,-3, 1,-1,-5},
	{-1,-1,-2,-3,-2, 0,-2,-2, 0, 2, 2,-1, 6, 0,-2,-2,-1,-2, 0, 1,-2, 2,-1,-1,-5},
	{-2,-2,-2,-4,-2,-4,-3,-3,-2, 0, 1,-3, 0, 8,-3,-2,-1, 1, 3, 0,-3, 1,-3,-1,-5},
	{-1,-2,-2,-1,-4,-1, 0,-2,-2,-2,-3,-1,-2,-3, 9,-1,-1,-3,-3,-3,-2,-3,-1,-1,-5},
	{ 1,-1, 1, 0,-1, 0, 0, 0,-1,-2,-3,-1,-2,-2,-1, 4, 2,-4,-2,-1, 0,-2, 0,-1,-5},
	{ 0,-1, 0,-1,-1,-1,-1,-2,-2,-1,-1,-1,-1,-1,-1, 2, 5,-3,-1, 0, 0,-1,-1,-1,-5},
	{-2,-2,-4,-4,-5,-2,-3,-2,-3,-2,-2,-2,-2, 1,-3,-4,-3,15, 3,-3,-4,-2,-2,-1,-5},
	{-2,-1,-2,-2,-3,-1,-2,-3, 2, 0, 0,-1, 0, 3,-3,-2,-1, 3, 8,-1,-2, 0,-2,-1,-5},
	{ 0,-2,-3,-3,-1,-3,-3,-3,-3, 3, 1,-2, 1, 0,-3,-1, 0,-3,-1, 5,-3, 2,-3,-1,-5},
	{-1,-1, 5, 6,-2, 0, 1,-1, 0,-3,-3, 0,-2,-3,-2, 0, 0,-4,-2,-3, 5,-3, 1,-1,-5},
	{-1,-3,-3,-3,-2,-2,-3,-4,-2, 4, 4,-3, 2, 1,-3,-2,-1,-2, 0, 2,-3, 4,-2,-1,-5},
	{-1, 1, 0, 1,-3, 4, 5,-2, 0,-3,-2, 1,-1,-3,-1, 0,-1,-2,-2,-3, 1,-2, 5,-1,-5},
	{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-5},
	{-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5, 1}};

    // BLOSUM62 scoring matrix as used by BLAST
    private final static String BLOSUM62_KEY = "BLOSUM62";
    private final static byte[][] BLOSUM62 = {
	{ 4,-1,-2,-2, 0,-1,-1, 0,-2,-1,-1,-1,-1,-2,-1, 1, 0,-3,-2, 0,-2,-1,-1,-1,-4},
	{-1, 5, 0,-2,-3, 1, 0,-2, 0,-3,-2, 2,-1,-3,-2,-1,-1,-3,-2,-3,-1,-2, 0,-1,-4},
	{-2, 0, 6, 1,-3, 0, 0, 0, 1,-3,-3, 0,-2,-3,-2, 1, 0,-4,-2,-3, 4,-3, 0,-1,-4},
	{-2,-2, 1, 6,-3, 0, 2,-1,-1,-3,-4,-1,-3,-3,-1, 0,-1,-4,-3,-3, 4,-3, 1,-1,-4},
	{ 0,-3,-3,-3, 9,-3,-4,-3,-3,-1,-1,-3,-1,-2,-3,-1,-1,-2,-2,-1,-3,-1,-3,-1,-4},
	{-1, 1, 0, 0,-3, 5, 2,-2, 0,-3,-2, 1, 0,-3,-1, 0,-1,-2,-1,-2, 0,-2, 4,-1,-4},
	{-1, 0, 0, 2,-4, 2, 5,-2, 0,-3,-3, 1,-2,-3,-1, 0,-1,-3,-2,-2, 1,-3, 4,-1,-4},
	{ 0,-2, 0,-1,-3,-2,-2, 6,-2,-4,-4,-2,-3,-3,-2, 0,-2,-2,-3,-3,-1,-4,-2,-1,-4},
	{-2, 0, 1,-1,-3, 0, 0,-2, 8,-3,-3,-1,-2,-1,-2,-1,-2,-2, 2,-3, 0,-3, 0,-1,-4},
	{-1,-3,-3,-3,-1,-3,-3,-4,-3, 4, 2,-3, 1, 0,-3,-2,-1,-3,-1, 3,-3, 3,-3,-1,-4},
	{-1,-2,-3,-4,-1,-2,-3,-4,-3, 2, 4,-2, 2, 0,-3,-2,-1,-2,-1, 1,-4, 3,-3,-1,-4},
	{-1, 2, 0,-1,-3, 1, 1,-2,-1,-3,-2, 5,-1,-3,-1, 0,-1,-3,-2,-2, 0,-3, 1,-1,-4},
	{-1,-1,-2,-3,-1, 0,-2,-3,-2, 1, 2,-1, 5, 0,-2,-1,-1,-1,-1, 1,-3, 2,-1,-1,-4},
	{-2,-3,-3,-3,-2,-3,-3,-3,-1, 0, 0,-3, 0, 6,-4,-2,-2, 1, 3,-1,-3, 0,-3,-1,-4},
	{-1,-2,-2,-1,-3,-1,-1,-2,-2,-3,-3,-1,-2,-4, 7,-1,-1,-4,-3,-2,-2,-3,-1,-1,-4},
	{ 1,-1, 1, 0,-1, 0, 0, 0,-1,-2,-2, 0,-1,-2,-1, 4, 1,-3,-2,-2, 0,-2, 0,-1,-4},
	{ 0,-1, 0,-1,-1,-1,-1,-2,-2,-1,-1,-1,-1,-2,-1, 1, 5,-2,-2, 0,-1,-1,-1,-1,-4},
	{-3,-3,-4,-4,-2,-2,-3,-2,-2,-3,-2,-3,-1, 1,-4,-3,-2,11, 2,-3,-4,-2,-2,-1,-4},
	{-2,-2,-2,-3,-2,-1,-2,-3, 2,-1,-1,-2,-1, 3,-3,-2,-2, 2, 7,-1,-3,-1,-2,-1,-4},
	{ 0,-3,-3,-3,-1,-2,-2,-3,-3, 3, 1,-2, 1,-1,-2,-2, 0,-3,-1, 4,-3, 2,-2,-1,-4},
	{-2,-1, 4, 4,-3, 0, 1,-1, 0,-3,-4, 0,-3,-3,-2, 0,-1,-4,-3,-3, 4,-3, 0,-1,-4},
	{-1,-2,-3,-3,-1,-2,-3,-4,-3, 3, 3,-3, 2, 0,-3,-2,-1,-2,-1, 2,-3, 3,-3,-1,-4},
	{-1, 0, 0, 1,-3, 4, 4,-2, 0,-3,-3, 1,-1,-3,-1, 0,-1,-2,-2,-2, 0,-3, 4,-1,-4},
	{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-4},
	{-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4, 1}};


    // BLOSUM80 scoring matrix as used by BLAST
    private final static String BLOSUM80_KEY = "BLOSUM80";
    private final static byte[][] BLOSUM80 = {
	{ 5,-2,-2,-2,-1,-1,-1, 0,-2,-2,-2,-1,-1,-3,-1, 1, 0,-3,-2, 0,-2,-2,-1,-1,-6},
	{-2, 6,-1,-2,-4, 1,-1,-3, 0,-3,-3, 2,-2,-4,-2,-1,-1,-4,-3,-3,-1,-3, 0,-1,-6},
	{-2,-1, 6, 1,-3, 0,-1,-1, 0,-4,-4, 0,-3,-4,-3, 0, 0,-4,-3,-4, 5,-4, 0,-1,-6},
	{-2,-2, 1, 6,-4,-1, 1,-2,-2,-4,-5,-1,-4,-4,-2,-1,-1,-6,-4,-4, 5,-5, 1,-1,-6},
	{-1,-4,-3,-4, 9,-4,-5,-4,-4,-2,-2,-4,-2,-3,-4,-2,-1,-3,-3,-1,-4,-2,-4,-1,-6},
	{-1, 1, 0,-1,-4, 6, 2,-2, 1,-3,-3, 1, 0,-4,-2, 0,-1,-3,-2,-3, 0,-3, 4,-1,-6},
	{-1,-1,-1, 1,-5, 2, 6,-3, 0,-4,-4, 1,-2,-4,-2, 0,-1,-4,-3,-3, 1,-4, 5,-1,-6},
	{ 0,-3,-1,-2,-4,-2,-3, 6,-3,-5,-4,-2,-4,-4,-3,-1,-2,-4,-4,-4,-1,-5,-3,-1,-6},
	{-2, 0, 0,-2,-4, 1, 0,-3, 8,-4,-3,-1,-2,-2,-3,-1,-2,-3, 2,-4,-1,-4, 0,-1,-6},
	{-2,-3,-4,-4,-2,-3,-4,-5,-4, 5, 1,-3, 1,-1,-4,-3,-1,-3,-2, 3,-4, 3,-4,-1,-6},
	{-2,-3,-4,-5,-2,-3,-4,-4,-3, 1, 4,-3, 2, 0,-3,-3,-2,-2,-2, 1,-4, 3,-3,-1,-6},
	{-1, 2, 0,-1,-4, 1, 1,-2,-1,-3,-3, 5,-2,-4,-1,-1,-1,-4,-3,-3,-1,-3, 1,-1,-6},
	{-1,-2,-3,-4,-2, 0,-2,-4,-2, 1, 2,-2, 6, 0,-3,-2,-1,-2,-2, 1,-3, 2,-1,-1,-6},
	{-3,-4,-4,-4,-3,-4,-4,-4,-2,-1, 0,-4, 0, 6,-4,-3,-2, 0, 3,-1,-4, 0,-4,-1,-6},
	{-1,-2,-3,-2,-4,-2,-2,-3,-3,-4,-3,-1,-3,-4, 8,-1,-2,-5,-4,-3,-2,-4,-2,-1,-6},
	{ 1,-1, 0,-1,-2, 0, 0,-1,-1,-3,-3,-1,-2,-3,-1, 5, 1,-4,-2,-2, 0,-3, 0,-1,-6},
	{ 0,-1, 0,-1,-1,-1,-1,-2,-2,-1,-2,-1,-1,-2,-2, 1, 5,-4,-2, 0,-1,-1,-1,-1,-6},
	{-3,-4,-4,-6,-3,-3,-4,-4,-3,-3,-2,-4,-2, 0,-5,-4,-4,11, 2,-3,-5,-3,-3,-1,-6},
	{-2,-3,-3,-4,-3,-2,-3,-4, 2,-2,-2,-3,-2, 3,-4,-2,-2, 2, 7,-2,-3,-2,-3,-1,-6},
	{ 0,-3,-4,-4,-1,-3,-3,-4,-4, 3, 1,-3, 1,-1,-3,-2, 0,-3,-2, 4,-4, 2,-3,-1,-6},
	{-2,-1, 5, 5,-4, 0, 1,-1,-1,-4,-4,-1,-3,-4,-2, 0,-1,-5,-3,-4, 5,-4, 0,-1,-6},
	{-2,-3,-4,-5,-2,-3,-4,-5,-4, 3, 3,-3, 2, 0,-4,-3,-1,-3,-2, 2,-4, 3,-3,-1,-6},
	{-1, 0, 0, 1,-4, 4, 5,-3, 0,-4,-3, 1,-1,-4,-2, 0,-1,-3,-3,-3, 0,-3, 5,-1,-6},
	{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-6},
	{-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6,-6, 1}};

    // PAM30 scoring matrix as used by BLAST
    private final static String PAM30_KEY = "PAM30";
    private final static byte[][] PAM30 = {
	{  6, -7, -4, -3, -6, -4, -2, -2, -7, -5, -6, -7, -5, -8, -2,  0, -1,-13, -8, -2, -3, -6, -3, -1,-17},
	{ -7,  8, -6,-10, -8, -2, -9, -9, -2, -5, -8,  0, -4, -9, -4, -3, -6, -2,-10, -8, -7, -7, -4, -1,-17},
	{ -4, -6,  8,  2,-11, -3, -2, -3,  0, -5, -7, -1, -9, -9, -6,  0, -2, -8, -4, -8,  6, -6, -3, -1,-17},
	{ -3,-10,  2,  8,-14, -2,  2, -3, -4, -7,-12, -4,-11,-15, -8, -4, -5,-15,-11, -8,  6,-10,  1, -1,-17},
	{ -6, -8,-11,-14, 10,-14,-14, -9, -7, -6,-15,-14,-13,-13, -8, -3, -8,-15, -4, -6,-12, -9,-14, -1,-17},
	{ -4, -2, -3, -2,-14,  8,  1, -7,  1, -8, -5, -3, -4,-13, -3, -5, -5,-13,-12, -7, -3, -5,  6, -1,-17},
	{ -2, -9, -2,  2,-14,  1,  8, -4, -5, -5, -9, -4, -7,-14, -5, -4, -6,-17, -8, -6,  1, -7,  6, -1,-17},
	{ -2, -9, -3, -3, -9, -7, -4,  6, -9,-11,-10, -7, -8, -9, -6, -2, -6,-15,-14, -5, -3,-10, -5, -1,-17},
	{ -7, -2,  0, -4, -7,  1, -5, -9,  9, -9, -6, -6,-10, -6, -4, -6, -7, -7, -3, -6, -1, -7, -1, -1,-17},
	{ -5, -5, -5, -7, -6, -8, -5,-11, -9,  8, -1, -6, -1, -2, -8, -7, -2,-14, -6,  2, -6,  5, -6, -1,-17},
	{ -6, -8, -7,-12,-15, -5, -9,-10, -6, -1,  7, -8,  1, -3, -7, -8, -7, -6, -7, -2, -9,  6, -7, -1,-17},
	{ -7,  0, -1, -4,-14, -3, -4, -7, -6, -6, -8,  7, -2,-14, -6, -4, -3,-12, -9, -9, -2, -7, -4, -1,-17},
	{ -5, -4, -9,-11,-13, -4, -7, -8,-10, -1,  1, -2, 11, -4, -8, -5, -4,-13,-11, -1,-10,  0, -5, -1,-17},
	{ -8, -9, -9,-15,-13,-13,-14, -9, -6, -2, -3,-14, -4,  9,-10, -6, -9, -4,  2, -8,-10, -2,-13, -1,-17},
	{ -2, -4, -6, -8, -8, -3, -5, -6, -4, -8, -7, -6, -8,-10,  8, -2, -4,-14,-13, -6, -7, -7, -4, -1,-17},
	{  0, -3,  0, -4, -3, -5, -4, -2, -6, -7, -8, -4, -5, -6, -2,  6,  0, -5, -7, -6, -1, -8, -5, -1,-17},
	{ -1, -6, -2, -5, -8, -5, -6, -6, -7, -2, -7, -3, -4, -9, -4,  0,  7,-13, -6, -3, -3, -5, -6, -1,-17},
	{-13, -2, -8,-15,-15,-13,-17,-15, -7,-14, -6,-12,-13, -4,-14, -5,-13, 13, -5,-15,-10, -7,-14, -1,-17},
	{ -8,-10, -4,-11, -4,-12, -8,-14, -3, -6, -7, -9,-11,  2,-13, -7, -6, -5, 10, -7, -6, -7, -9, -1,-17},
	{ -2, -8, -8, -8, -6, -7, -6, -5, -6,  2, -2, -9, -1, -8, -6, -6, -3,-15, -7,  7, -8,  0, -6, -1,-17},
	{ -3, -7,  6,  6,-12, -3,  1, -3, -1, -6, -9, -2,-10,-10, -7, -1, -3,-10, -6, -8,  6, -8,  0, -1,-17},
	{ -6, -7, -6,-10, -9, -5, -7,-10, -7,  5,  6, -7,  0, -2, -7, -8, -5, -7, -7,  0, -8,  6, -6, -1,-17},
	{ -3, -4, -3,  1,-14,  6,  6, -5, -1, -6, -7, -4, -5,-13, -4, -5, -6,-14, -9, -6,  0, -6,  6, -1,-17},
	{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,-17},
	{-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,-17,  1}};

    // PAM70 scoring matrix as used by BLAST
    private final static String PAM70_KEY = "PAM70";
    private final static byte[][] PAM70 = {
	{  5, -4, -2, -1, -4, -2, -1,  0, -4, -2, -4, -4, -3, -6,  0,  1,  1, -9, -5, -1, -1, -3, -1, -1,-11},
	{ -4,  8, -3, -6, -5,  0, -5, -6,  0, -3, -6,  2, -2, -7, -2, -1, -4,  0, -7, -5, -4, -5, -2, -1,-11},
	{ -2, -3,  6,  3, -7, -1,  0, -1,  1, -3, -5,  0, -5, -6, -3,  1,  0, -6, -3, -5,  5, -4, -1, -1,-11},
	{ -1, -6,  3,  6, -9,  0,  3, -1, -1, -5, -8, -2, -7,-10, -4, -1, -2,-10, -7, -5,  5, -7,  2, -1,-11},
	{ -4, -5, -7, -9,  9, -9, -9, -6, -5, -4,-10, -9, -9, -8, -5, -1, -5,-11, -2, -4, -8, -7, -9, -1,-11},
	{ -2,  0, -1,  0, -9,  7,  2, -4,  2, -5, -3, -1, -2, -9, -1, -3, -3, -8, -8, -4, -1, -3,  5, -1,-11},
	{ -1, -5,  0,  3, -9,  2,  6, -2, -2, -4, -6, -2, -4, -9, -3, -2, -3,-11, -6, -4,  2, -5,  5, -1,-11},
	{  0, -6, -1, -1, -6, -4, -2,  6, -6, -6, -7, -5, -6, -7, -3,  0, -3,-10, -9, -3, -1, -7, -3, -1,-11},
	{ -4,  0,  1, -1, -5,  2, -2, -6,  8, -6, -4, -3, -6, -4, -2, -3, -4, -5, -1, -4,  0, -4,  1, -1,-11},
	{ -2, -3, -3, -5, -4, -5, -4, -6, -6,  7,  1, -4,  1,  0, -5, -4, -1, -9, -4,  3, -4,  4, -4, -1,-11},
	{ -4, -6, -5, -8,-10, -3, -6, -7, -4,  1,  6, -5,  2, -1, -5, -6, -4, -4, -4,  0, -6,  5, -4, -1,-11},
	{ -4,  2,  0, -2, -9, -1, -2, -5, -3, -4, -5,  6,  0, -9, -4, -2, -1, -7, -7, -6, -1, -5, -2, -1,-11},
	{ -3, -2, -5, -7, -9, -2, -4, -6, -6,  1,  2,  0, 10, -2, -5, -3, -2, -8, -7,  0, -6,  2, -3, -1,-11},
	{ -6, -7, -6,-10, -8, -9, -9, -7, -4,  0, -1, -9, -2,  8, -7, -4, -6, -2,  4, -5, -7, -1, -9, -1,-11},
	{  0, -2, -3, -4, -5, -1, -3, -3, -2, -5, -5, -4, -5, -7,  7,  0, -2, -9, -9, -3, -4, -5, -2, -1,-11},
	{  1, -1,  1, -1, -1, -3, -2,  0, -3, -4, -6, -2, -3, -4,  0,  5,  2, -3, -5, -3,  0, -5, -2, -1,-11},
	{  1, -4,  0, -2, -5, -3, -3, -3, -4, -1, -4, -1, -2, -6, -2,  2,  6, -8, -4, -1, -1, -3, -3, -1,-11},
	{ -9,  0, -6,-10,-11, -8,-11,-10, -5, -9, -4, -7, -8, -2, -9, -3, -8, 13, -3,-10, -7, -5,-10, -1,-11},
	{ -5, -7, -3, -7, -2, -8, -6, -9, -1, -4, -4, -7, -7,  4, -9, -5, -4, -3,  9, -5, -4, -4, -7, -1,-11},
	{ -1, -5, -5, -5, -4, -4, -4, -3, -4,  3,  0, -6,  0, -5, -3, -3, -1,-10, -5,  6, -5,  1, -4, -1,-11},
	{ -1, -4,  5,  5, -8, -1,  2, -1,  0, -4, -6, -1, -6, -7, -4,  0, -1, -7, -4, -5,  5, -5,  1, -1,-11},
	{ -3, -5, -4, -7, -7, -3, -5, -7, -4,  4,  5, -5,  2, -1, -5, -5, -3, -5, -4,  1, -5,  5, -4, -1,-11},
	{ -1, -2, -1,  2, -9,  5,  5, -3,  1, -4, -4, -2, -3, -9, -2, -2, -3,-10, -7, -4,  1, -4,  5, -1,-11},
	{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,-11},
	{-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,-11,  1}};
}
