package belka.chem;

//--- Java imports ---
import java.awt.*;
import java.util.*;
import java.io.*;

/**
 * Objects of this class represent chemical compounds.
 * 
 * @author Alexej Abyzov
 *
 */
public class Compound implements Serializable
{
    // Hash by name
    private static Hashtable<String,Compound> hash_by_name_ =
	new Hashtable<String,Compound>(50);

    // Hash by short name
    private static Hashtable<String,Compound> hash_by_short_name_ =
	new Hashtable<String,Compound>(50);

    /** Alanine */
    public final static Compound Alanine = 
	new Compound("Alanine","Ala",'A',new Color(140,255,140));

    /** Arginine */
    public final static Compound Arginine = 
	new Compound("Arginine","Arg",'R',new Color(0,0,124));

    /** Asparagine */
    public final static Compound Asparagine = 
	new Compound("Asparagine","Asn",'N',new Color(255,124,112));

    /** Aspartate */
    public final static Compound Aspartate = 
	new Compound("Aspartate","Asp",'D',new Color(160,0,66));

    /** Cysteine */
    public final static Compound Cysteine = 
	new Compound("Cysteine","Cys",'C',new Color(255,255,112));

    /** Glutamate */
    public final static Compound Glutamate = 
	new Compound("Glutamate","Glu",'E',new Color(102,0,0));

    /** Glutamine */
    public final static Compound Glutamine = 
	new Compound("Glutamine","Gln",'Q',new Color(255,76,76));

    /** Glycine */
    public final static Compound Glycine = 
	new Compound("Glycine","Gly",'G',new Color(255,255,255));

    /** Histidine */
    public final static Compound Histidine = 
	new Compound("Histidine","His",'H',new Color(112,112,255));

    /** Isoleucine */
    public final static Compound Isoleucine = 
	new Compound("Isoleucine","Ile",'I',new Color(0,76,0));

    /** Leucine */
    public final static Compound Leucine = 
	new Compound("Leucine","Leu",'L',new Color(69,94,69));

    /** Lysine */
    public final static Compound Lysine = 
	new Compound("Lysine","Lys",'K',new Color(71,71,184));

    /** Methionine */
    public final static Compound Methionine = 
	new Compound("Methionine","Met",'M',new Color(184,160,66));

    /** Selenomethionine (non-standard) */
    public final static Compound Selenomethionine = 
	new Compound("Selenomethionine","Mse",'M',new Color(184,160,66));

    /** Phenylalanine */
    public final static Compound Phenylalanine = 
	new Compound("Phenylalanine","Phe",'F',new Color(83,76,66));

    /** Proline */
    public final static Compound Proline = 
	new Compound("Proline","Pro",'P',new Color(82,82,82));

    /** Serine */
    public final static Compound Serine = 
	new Compound("Serine","Ser",'S',new Color(255,112,66));

    /** Threonine */
    public final static Compound Threonine = 
	new Compound("Threonine","Thr",'T',new Color(184,76,0));

    /** Tyrosine */
    public final static Compound Tyrosine = 
	new Compound("Tyrosine","Tyr",'Y',new Color(140,112,76));

    /** Tryptophan */
    public final static Compound Tryptophan = 
	new Compound("Tryptophan","Trp",'W',new Color(79,70,0));

    /** Valine */
    public final static Compound Valine = 
	new Compound("Valine","Val",'V',new Color(255,140,255));

    /** Selenocysteine (non-standard) */
    public final static Compound Selenocysteine = 
	new Compound("Selenocysteine","Sec",'U',new Color(255,255,112));

    /** Pyrrolysine (non-standard) */
    public final static Compound Pyrrolysine = 
	new Compound("Pyrrolysine","Pyl",'X',new Color(71,71,184));

    /** Aspartate or asparagine (uncertain result of hydrolysis) */
    public final static Compound Aspartate_or_Asparagine = 
	new Compound("Aspartate or asparagine","Asx",'B',new Color(255,0,255));

    /** Leucine or isoleucine (uncertain result of mass-spec) */
    public final static Compound Leucine_or_Isoleucine = 
	new Compound("Leucine or isoleucine","Xle",'J',new Color(255,0,255));

    /** Glutamate or glutamine (uncertain result of hydrolysis) */
    public final static Compound Glutamate_or_Glutamine = 
	new Compound("Glutamate or glutamine","Glx",'Z',new Color(255,0,255));


    // See http://www.chem.qmul.ac.uk/iubmb/misc/naseq.html

    /** Adenine */
    public final static Compound Adenine = 
	new Compound("Adenine","DA",'A',new Color(140,255,140));

    /** Cytosine */
    public final static Compound Cytosine = 
	new Compound("Cytosine","DC",'C',new Color(255,255,112));

    /** Guanine */
    public final static Compound Guanine = 
	new Compound("Guanine","DG",'G',new Color(255,255,255));

    /** Thymine */
    public final static Compound Thymine = 
	new Compound("Thymine","DT",'T',new Color(184,76,0));

    /** Uracil */
    public final static Compound Uracil = 
	new Compound("Uracil","DU",'U',new Color(255,255,112));

    /** Adenine or Guanine (ambiguous) */
    public final static Compound Adenine_or_Guanine =
	new Compound("Purine","Pur",'R',new Color(255,0,255));

    /** Cytosine or Thymine */
    public final static Compound Cytosine_or_Thymine = 
	new Compound("Pyrimidine","Pyr",'Y',new Color(255,0,255));

    /** Guanine or Cytosine */
    public final static Compound Guanine_or_Cytosine = 
	new Compound("Strong interaction (3 H bonds)","???",'S',
		     new Color(255,0,255));

    /** Adenine or Thymine */
    public final static Compound Adenine_or_Thymine =
	new Compound("Weak interaction (2 H bonds)","???",'W',
		     new Color(255,0,255));

    /** Guanine or Thymine */
    public final static Compound Guanine_or_Thymine =
	new Compound("Keto","???",'K',new Color(255,0,255));

    /** Adenine or Cytosine */
    public final static Compound Adenine_or_Cytosine =
	new Compound("Amino","???",'M',new Color(255,0,255));

    /** Guanine or Thymine or Adenine */
    public final static Compound Guanine_or_Thymine_or_Adenine =
	new Compound("Not C","???",'D',new Color(255,0,255));

    /** Thymine or Adenine or Cytosine */
    public final static Compound Thymine_or_Adenine_or_Cytosine =
	new Compound("Not G","???",'H',new Color(255,0,255));

    /** Guanine or Thymine or Cytosine */
    public final static Compound Guanine_or_Thymine_or_Cytosine =
	new Compound("Not A","???",'B',new Color(255,0,255));

    /** Guanine or Adenine or Cytosine */
    public final static Compound Guanine_or_Adenine_or_Cytosine =
	new Compound("Not T","???",'V',new Color(255,0,255));

    /** Water */
    public final static Compound hoh = 
	new Compound("Water","HOH",'X',new Color(255,0,0));
    public final static Compound h2o = 
	new Compound("Water","H2O",'X',new Color(255,0,0));

    // Constructor
    public Compound(String name,String short_name,char letter,
		     Color col)
    {
	name_       = name;
	short_name_ = short_name;
	letter_     = letter;
	col_        = col;

	if (name != null && name.length() > 0) {
	    String key = name.toLowerCase();
	    if (hash_by_name_.get(key) == null)
		hash_by_name_.put(key,this);
	    else
		System.err.println("WARNING: Compound '" + name + "' " +
				   "already exists.");
	}

	if (short_name != null && short_name.length() > 0) {
	    String key = short_name.toLowerCase();
	    if (hash_by_short_name_.get(key) == null)
		hash_by_short_name_.put(key,this);
	    else
		System.err.println("WARNING: Compound '" + short_name + "' " +
				   "already exists.");
	}

    }

    // Constructor
    public Compound(String short_name)
    {
	this("Unknown",short_name,'X',new Color(0,0,0));
    }

    // Compound name
    private String name_ = "";
    /**
     * Returns compound's full name, for example "Alanine".
     *
     * @return compound's full name.
     */
    public String getName() { return name_; }

    // Short name
    private String short_name_ = "";
    /**
     * Returns compound's short name (three-letter), for example "Ala".
     *
     * @return compound's short name.
     */
    public String getShortName() { return short_name_; }

    // One letter name
    private char letter_ = 'X';
    /**
     * Returns compound's one letter name, for example 'A' for Alanine.
     *
     * @return compound's one letter name.
     */
    public char getLetterName() { return letter_; }

    // Color
    private Color col_ = null;
    /**
     * Returns compound's color to be used to draw it.
     *
     * @return compound's color to be used to draw it.
     */
    public Color getColor() { return col_; }

    
    /**
     * Returns compound with given name.
     * 
     * @param name compounds's name.
     * @return compound with inputed name. Null if there is not such compound.
     */
    public static Compound getCompoundByName(String name)
    {
	if (name == null || name.length() == 0) return null;
	String key = name.toLowerCase();
	return hash_by_name_.get(key);
    }

    /**
     * Returns compound with short (three-letter) name.
     * 
     * @param short_name compounds's short name.
     * @return compound with inputed short name. Null if there is not such
     * compound.
     */
    public static Compound getCompoundByShortName(String short_name)
    {
	if (short_name == null || short_name.length() == 0) return null;
	String key = short_name.toLowerCase();
	return hash_by_short_name_.get(key);
    }

    /**
     * Returns compound that corresponds to amino acid for the given
     * letter.
     * 
     * @param letter amino acid letter code.
     * @return compound that corresponds to amino acid for the given
     * letter.
     */
    public static Compound getAminoAcid(char letter)
    {
	if (Alanine.getLetterName()          == letter) return Alanine;
	if (Arginine.getLetterName()         == letter) return Arginine; 
	if (Asparagine.getLetterName()       == letter) return Asparagine; 
	if (Aspartate.getLetterName()        == letter) return Aspartate; 
	if (Cysteine.getLetterName()         == letter) return Cysteine; 
	if (Glutamate.getLetterName()        == letter) return Glutamate; 
	if (Glutamine.getLetterName()        == letter) return Glutamine;
	if (Glycine.getLetterName()          == letter) return Glycine;
	if (Histidine.getLetterName()        == letter) return Histidine;
	if (Isoleucine.getLetterName()       == letter) return Isoleucine;
	if (Leucine.getLetterName()          == letter) return Leucine; 
	if (Lysine.getLetterName()           == letter) return Lysine; 
	if (Methionine.getLetterName()       == letter) return Methionine; 
	if (Selenomethionine.getLetterName() == letter)
	    return Selenomethionine; 
	if (Phenylalanine.getLetterName()    == letter) return Phenylalanine; 
	if (Proline.getLetterName()          == letter) return Proline; 
	if (Serine.getLetterName()           == letter) return Serine; 
	if (Threonine.getLetterName()        == letter) return Threonine; 
	if (Tyrosine.getLetterName()         == letter) return Tyrosine; 
	if (Tryptophan.getLetterName()       == letter) return Tryptophan; 
	if (Valine.getLetterName()           == letter) return Valine; 
	if (Selenocysteine.getLetterName()   == letter) return Selenocysteine; 
	if (Pyrrolysine.getLetterName()      == letter) return Pyrrolysine; 
	if (Aspartate_or_Asparagine.getLetterName() == letter)
	    return Aspartate_or_Asparagine; 
	if (Leucine_or_Isoleucine.getLetterName() == letter)
	    return Leucine_or_Isoleucine; 
	if (Glutamate_or_Glutamine.getLetterName() == letter)
	    return Leucine_or_Isoleucine; 
	return null;
    }

    /**
     * Returns compound that corresponds to nucleic acid for the given
     * letter.
     * 
     * @param letter nucleic acid letter code.
     * @return compound that corresponds to nucleic acid for the given
     * letter.
     */
    public static Compound getNucleicAcid(char letter)
    {
	if (Adenine.getLetterName()  == letter) return Adenine;
	if (Cytosine.getLetterName() == letter) return Cytosine;
	if (Guanine.getLetterName()  == letter) return Guanine;
	if (Thymine.getLetterName()  == letter) return Thymine;
	if (Uracil.getLetterName()   == letter) return Uracil;
	if (Adenine_or_Guanine.getLetterName()  == letter)
	    return Adenine_or_Guanine;
	if (Cytosine_or_Thymine.getLetterName() == letter)
	    return Cytosine_or_Thymine;
	if (Guanine_or_Cytosine.getLetterName() == letter)
	    return Guanine_or_Cytosine;
	if (Adenine_or_Thymine.getLetterName()  == letter)
	    return Adenine_or_Thymine;
	if (Guanine_or_Thymine.getLetterName()  == letter)
	    return Adenine_or_Thymine;
	if (Adenine_or_Cytosine.getLetterName() == letter)
	    return Adenine_or_Cytosine;
	if (Guanine_or_Thymine_or_Adenine.getLetterName()  == letter)
	    return Guanine_or_Thymine_or_Adenine;
	if (Thymine_or_Adenine_or_Cytosine.getLetterName() == letter)
	    return Thymine_or_Adenine_or_Cytosine;
	if (Guanine_or_Thymine_or_Cytosine.getLetterName() == letter)
	    return Guanine_or_Thymine_or_Cytosine;
	if (Guanine_or_Adenine_or_Cytosine.getLetterName() == letter)
	    return Guanine_or_Adenine_or_Cytosine;
	return null;
    }    
}
