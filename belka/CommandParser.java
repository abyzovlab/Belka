package belka;

//--- Java import ---
import java.awt.*;
import java.util.*;
import java.lang.*;

/**
 * Object of this class parses inputed commands. The set of commands is
 * predefined. The parsed values can be accessed though different methods.
 * <p>
 * Simple usage: <p>
 * <code>
 * ... <p>
 * Color newColor = Color.BLACK; <p>
 * CommandParser commParser = new CommandParser(inputLine); <p>
 * int commandVal = commParser.parseCommand(); <p>
 * if (commandVal == CommandParser.BLUE_VAL) { <p>
 *  newColor = Color.BLUE; <p>
 * } <p>
 * ... <p>
 * </code>
 *
 * @author Alexej Abyzov
 */

public class CommandParser
{
    /**
     * Object constructor.
     *
     * @param input string to be parsed.
     */
    public CommandParser(String input)
    {
	if (input == null) input_ = "";
  	else               input_ = input;
	firstToRead_ = 0;


	// Checking for output redirection
	StringTokenizer tokenizer = new StringTokenizer(input_);
	while (tokenizer.hasMoreTokens()) {
	    String tok = tokenizer.nextToken();
	    if (!tok.equals(">") && !tok.equals(">>")) continue;
	    if (tok.equals(">>")) fileAppend_ = true;
	    if (tokenizer.hasMoreTokens()) {
		outputFileName_ = tokenizer.nextToken();
		if (tokenizer.hasMoreTokens()) {
		    System.err.println("Umbiguous redirection!");
		    String tmp = tokenizer.nextToken();
		    firstToRead_ = input_.indexOf(tmp) + tmp.length();
		    outputFileName_ = "";
		} else input_ = input_.substring(0,input_.indexOf('>') - 1);
	    } else {
		System.err.println("No file for redirection!");
		outputFileName_ = "";
		firstToRead_ = input_.indexOf('>') + 1;
	    }
	    break;
	}
    }
    
    // Input line
    private String input_ = "";
    /**
     * The function returns the input string of the parser.
     *
     * @return the input string of the parser.
     */
    public String getInput() { return input_; }

    // File for output redirection
    private String outputFileName_ = null;
    /**
     * The function returns the file name to redirect output.
     * Output redirection is supported by few commands.
     *
     * @return the file name to redirect output.
     */
    public String getOutputFileName() { return outputFileName_; }

    // Flag for overwrite/append to output redirection
    private boolean fileAppend_ = false;
    /**
     * Return flag indicting how to handle output file.
     *
     * @return flag indicting how to handle output file.
     */
    public boolean outputFileAppend() { return fileAppend_; }

    // Last parsed word
    private String word_ = "";
    /**
     * The function returns the last parsed word.
     *
     * @return the last parsed word.
     */
    public String getParsedWord() { return word_; }

    // Parsing index
    private int firstToRead_ = 0;

    // Parsed int
    private int parsedInt_ = 0;
    /**
     * Returns parsed integer number. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _INTEGER_NUM_VAL meaning
     * that integer was parsed.
     *
     * @return parsed integer number.
     */
    public  int getParsedInt() { return parsedInt_; }

    // Parsed double
    private double parsedDouble_ = 0;
    /**
     * Returns parsed double number. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _DOUBLE_NUM_VAL meaning
     * that double was parsed.
     *
     * @return parsed double number.
     */
    public  double getParsedDouble() { return parsedDouble_; }

    // Parsed int range
    private int parsedIntRangeStart_ = 0, parsedIntRangeEnd_ = 0;
    /**
     * Returns start of the parsed integer range. It make sense to call this
     * function if the function {@link #parseCommand()} returned value
     * _INTEGER_RANGE_VAL meaning that integer range was parsed.
     *
     * @return parsed start of integer range.
     */
    public  int getParsedIntRangeStart() { return parsedIntRangeStart_; }

    /**
     * Returns end of the parsed integer range. It make sense to call this
     * function if the function {@link #parseCommand()} returned value
     * _INTEGER_RANGE_VAL meaning that integer range was parsed.
     *
     * @return parsed end of integer range.
     */
    public  int getParsedIntRangeEnd()   { return parsedIntRangeEnd_; }
	
    // Not recognized command
    public final static int _ERROR_VAL       =  0;

    // Values for numbers
    public final static int _INTEGER_NUM_VAL   = -1;
    public final static int _DOUBLE_NUM_VAL    = -2;
    public final static int _INTEGER_RANGE_VAL = -3;
    public final static int _USERCOLOR_VAL     = -4; // [r,g,b]
    public final static int _EXPRESSION_VAL    = -5; // cys-10A.CA
    public final static int _NONE_VAL          = -6;

    /**
     * Returns 'true' if there are more non-space characters in input line,
     * 'false' otherwise.
     *
     * @return 'true' if there are more non-space characters in input line,
     * 'false' otherwise.
     */
    public boolean hasMoreInput()
    {
	if (firstToRead_ >= input_.length()) return false;
	String tmp = input_.substring(firstToRead_,input_.length());
	if (tmp == null)           return false;
	if (tmp.trim().equals("")) return false;
	return true;
    }

    /**
     * Returns the parsed content of the command.
     *
     * @return the parsed content of the command.
     */
    public String getParsedContent()
    {
	return input_.substring(0,firstToRead_);
    }

    /**
     * Returns the remaining content of the command.
     *
     * @return the remaining content of the command.
     */
    public String getRemainingContent()
    {
	return input_.substring(firstToRead_);
    }

    /**
     * Parses command and returns value of the current command.
     * _ERROR_VAL is returned if command
     * is unknow or error happend during parsing (for example non-valid 
     * selection expression). Commands are supposed to be
     * separated by spaces. The parsing is done from postion of the current
     * parsing index. After parsing is done the parsing index is moved to a new
     * position. 
     *
     * @return value of the current command. _ERROR_VAL is returned if command
     * is unknow or error happend during parsing.
     */
    public int parseCommand()
    {
	word_ = "";

	if (outputFileName_ != null && outputFileName_ == "")
	    return _ERROR_VAL; // Error while parsing of redirection in contr.

	// Scroll to the next not space
	for (;firstToRead_ < input_.length();firstToRead_++) {
	    char inChar = input_.charAt(firstToRead_);
	    if (!Character.isSpaceChar(inChar)) break;
	}

	if ((input_.length() - firstToRead_) == 0) return _NONE_VAL;

	// Special case of '!'
	if (input_.charAt(firstToRead_) == '!') {
	    firstToRead_++;
	    word_ = "!";
	    return NOT_VAL;
	}

	// Special case of ','
	if (input_.charAt(firstToRead_) == ',') {
	    firstToRead_++;
	    word_ = ",";
	    return OR_VAL;
	}

	for (;firstToRead_ < input_.length();firstToRead_++) {
	    char inChar = input_.charAt(firstToRead_);
	    if (inChar == ',') break;
	    if (Character.isSpaceChar(inChar)) break;
	    word_ += inChar;
	}

	// Scroll to the next not space
	for (;firstToRead_ < input_.length();firstToRead_++) {
	    char inChar = input_.charAt(firstToRead_);
	    if (!Character.isSpaceChar(inChar)) break;
	}

	int last_ind = word_.length() - 1;
	char firstChar = Character.toLowerCase(word_.charAt(0));
	char lastChar  = Character.toLowerCase(word_.charAt(last_ind));
	int ret = _ERROR_VAL;
	if (Character.isLetter(firstChar)) {
	    if (firstChar == 'a') ret = parseACommand(word_.toLowerCase());
	    if (firstChar == 'b') ret = parseBCommand(word_.toLowerCase());
	    if (firstChar == 'c') ret = parseCCommand(word_.toLowerCase());
	    if (firstChar == 'd') ret = parseDCommand(word_.toLowerCase());
	    if (firstChar == 'e') ret = parseECommand(word_.toLowerCase());
	    if (firstChar == 'f') ret = parseFCommand(word_.toLowerCase());
	    if (firstChar == 'g') ret = parseGCommand(word_.toLowerCase());
	    if (firstChar == 'h') ret = parseHCommand(word_.toLowerCase());
	    if (firstChar == 'i') ret = parseICommand(word_.toLowerCase());
	    if (firstChar == 'j') ret = parseJCommand(word_.toLowerCase());
	    if (firstChar == 'k') ret = parseKCommand(word_.toLowerCase());
	    if (firstChar == 'l') ret = parseLCommand(word_.toLowerCase());
	    if (firstChar == 'm') ret = parseMCommand(word_.toLowerCase());
	    if (firstChar == 'n') ret = parseNCommand(word_.toLowerCase());
	    if (firstChar == 'o') ret = parseOCommand(word_.toLowerCase());
	    if (firstChar == 'p') ret = parsePCommand(word_.toLowerCase());
	    if (firstChar == 'q') ret = parseQCommand(word_.toLowerCase());
	    if (firstChar == 'r') ret = parseRCommand(word_.toLowerCase());
	    if (firstChar == 's') ret = parseSCommand(word_.toLowerCase());
	    if (firstChar == 't') ret = parseTCommand(word_.toLowerCase());
	    if (firstChar == 'u') ret = parseUCommand(word_.toLowerCase());
	    if (firstChar == 'v') ret = parseVCommand(word_.toLowerCase());
	    if (firstChar == 'w') ret = parseWCommand(word_.toLowerCase());
	    if (firstChar == 'x') ret = parseXCommand(word_.toLowerCase());
	    if (firstChar == 'y') ret = parseYCommand(word_.toLowerCase());
	    if (firstChar == 'z') ret = parseZCommand(word_.toLowerCase());
	}

 	if (ret != _ERROR_VAL) return ret;
	else {

	    int n_digits = 0, n_letters = 0, n_dots =  0, n_dash = 0;
	    int len = word_.length();
	    for (int i = 0;i < len;i++) {
		char currChar = word_.charAt(i);
		if (Character.isDigit(currChar))  n_digits++;
		if (Character.isLetter(currChar)) n_letters++;
		if (currChar == '.')              n_dots++;
		if (currChar == '-')              n_dash++;
	    }

	    if (n_letters == 0 && n_dots == 0)
		try { // Try to parse int
		    parsedInt_ = Integer.parseInt(word_);
		    return _INTEGER_NUM_VAL;
		} catch (Exception e) {}

	    if (n_letters == 0)
		try { // Try to parse double
		    parsedDouble_ = Double.parseDouble(word_);
		    return _DOUBLE_NUM_VAL;
		} catch (Exception e) {}

	    if (n_letters == 0 && n_dash == 1 && n_dots == 0)
		try { // Try to parse int range
		    int fInd = word_.indexOf('-');
		    int lInd = word_.lastIndexOf('-');
		    if (fInd > 0 && lInd == fInd) {
			parsedIntRangeStart_ =
			    Integer.parseInt(word_.substring(0,fInd));
			parsedIntRangeEnd_ =
			    Integer.parseInt(word_.substring(fInd + 1,len));
			return _INTEGER_RANGE_VAL;
		    }
		} catch (Exception e) {}

	    if (word_.equals("|") || // Try to parse OR 
		word_.equals("||")) return OR_VAL;

	    if (word_.equals("&") || // Try to parse AND
		word_.equals("&&")) return AND_VAL;

	    if (firstChar == '[') {
		try { // Try to parse color
		    return parseColor(word_);
		} catch (Exception e) {}
	    }

	    try { // Try to parse expression
		return parseExpression(word_);
	    } catch (Exception e) { }
	}
	return _ERROR_VAL;
    }

    public final static int ALICEBLUE_VAL    = 1000; // aliceblue
    public final static int ALIGN_SW_VAL     = 1001; // align_sw
    public final static int ALIGN_NW_VAL     = 1002; // align_nw
    public final static int ALIGNED_VAL      = 1003; // aligned
    public final static int ALL_VAL          = 1004; // all
    public final static int AND_VAL          = 1005; // and
    public final static int ANTIQUEWHITE_VAL = 1006; // antiquewhite
    public final static int ASSIGN_VAL       = 1007; // assign
    public final static int ATOM_VAL         = 1008; // atom
    public final static int AQUA_VAL         = 1009; // aqua
    public final static int AQUAMARINE_VAL   = 1010; // aquamarine
    public final static int AZURE_VAL        = 1011; // azure
    int parseACommand(String command)
    {
	if (command.equals("aliceblue"))    return ALICEBLUE_VAL;
	if (command.equals("align_sw"))     return ALIGN_SW_VAL;
	if (command.equals("align_nw"))     return ALIGN_NW_VAL;
	if (command.equals("aligned"))      return ALIGNED_VAL;
	if (command.equals("all"))          return ALL_VAL;
	if (command.equals("and"))          return AND_VAL;
	if (command.equals("antiquewhite")) return ANTIQUEWHITE_VAL;
	if (command.equals("assign"))       return ASSIGN_VAL;
	if (command.equals("atom"))         return ATOM_VAL;
	if (command.equals("aqua"))         return AQUA_VAL;
	if (command.equals("aquamarine"))   return AQUAMARINE_VAL;
	if (command.equals("azure"))        return AZURE_VAL;
	return _ERROR_VAL;
    }

    public final static int BACKBONE_VAL       = 2000; // backbone
    public final static int BACKGROUND_VAL     = 2001; // background
    public final static int BEIGE_VAL          = 2002; // beige
    public final static int BISQUE_VAL         = 2003; // bisque
    public final static int BLACK_VAL          = 2004; // black
    public final static int BLANCHEDALMOND_VAL = 2005; // blanchedalmond
    public final static int BLUE_VAL           = 2006; // blue
    public final static int BLUEVIOLET_VAL     = 2007; // blueviolet
    public final static int BROWN_VAL          = 2008; // brown
    public final static int BURLYWOOD_VAL      = 2009; // burlywood
    int parseBCommand(String command)
    {
	if (command.equals("backbone"))       return BACKBONE_VAL;
	if (command.equals("background"))     return BACKGROUND_VAL;
	if (command.equals("beige"))          return BEIGE_VAL;
	if (command.equals("bisque"))         return BISQUE_VAL;
	if (command.equals("black"))          return BLACK_VAL;
	if (command.equals("blanchedalmond")) return BLANCHEDALMOND_VAL;
	if (command.equals("blue"))           return BLUE_VAL;
	if (command.equals("blueviolet"))     return BLUEVIOLET_VAL;
	if (command.equals("brown"))          return BROWN_VAL;
	if (command.equals("burlywood"))      return BURLYWOOD_VAL;
	return _ERROR_VAL;
    }

    public final static int CADETBLUE_VAL      = 3000; // cadetblue
    public final static int CARTOONS_VAL       = 3001; // cartoons
    public final static int CENTER_VAL         = 3002; // center
    public final static int CHAIN_VAL          = 3003; // chain
    public final static int CHARTREUSE_VAL     = 3004; // chartreuse
    public final static int CHOCOLATE_VAL      = 3005; // chocolate
    public final static int CLUSTER_VAL        = 3006; // cluster
    public final static int COLOR_VAL          = 3007; // color
    public final static int COMPARE_VAL        = 3008; // compare
    public final static int CONNECT_VAL        = 3009; // connect
    public final static int CORAL_VAL          = 3010; // coral
    public final static int CORNFLOWERBLUE_VAL = 3011; // cornflowerblue
    public final static int CORNSILK_VAL       = 3012; // cornsilk
    public final static int CPK_VAL            = 3013; // cpk
    public final static int CRIMSON_VAL        = 3014; // crimson
    public final static int CYAN_VAL           = 3015; // cyan
    int parseCCommand(String command)
    {
	if (command.equals("cadetblue"))      return CADETBLUE_VAL;
	if (command.equals("cartoon"))        return CARTOONS_VAL;
	if (command.equals("cartoons"))       return CARTOONS_VAL;
	if (command.equals("center"))         return CENTER_VAL;
	if (command.equals("chain"))          return CHAIN_VAL;
	if (command.equals("chartreuse"))     return CHARTREUSE_VAL;
	if (command.equals("chocolate"))      return CHOCOLATE_VAL;
	if (command.equals("cluster"))        return CLUSTER_VAL;
	if (command.equals("color"))          return COLOR_VAL;
	if (command.equals("compare"))        return COMPARE_VAL;
	if (command.equals("connect"))        return CONNECT_VAL;
	if (command.equals("coral"))          return CORAL_VAL;
	if (command.equals("cornflowerblue")) return CORNFLOWERBLUE_VAL;
	if (command.equals("cornsilk"))       return CORNSILK_VAL;
	if (command.equals("cpk"))            return CPK_VAL;
	if (command.equals("crimson"))        return CRIMSON_VAL;
	if (command.equals("cyan"))           return CYAN_VAL;
	return _ERROR_VAL;
    }

    public final static int DARKBLUE_VAL       = 4000; // darkblue
    public final static int DARKCYAN_VAL       = 4001; // darkcyan
    public final static int DARKGOLDENROD_VAL  = 4002; // darkgoldenrod
    public final static int DARKGRAY_VAL       = 4003; // darkgray
    public final static int DARKGREEN_VAL      = 4004; // darkgreen
    public final static int DARKKHAKI_VAL      = 4005; // darkkhaki
    public final static int DARKMAGENTA_VAL    = 4006; // darkmagenta
    public final static int DARKOLIVEGREEN_VAL = 4007; // darkolivegreen
    public final static int DARKORANGE_VAL     = 4008; // darkorange
    public final static int DARKORCHID_VAL     = 4009; // darkorchid
    public final static int DARKRED_VAL        = 4010; // darkred
    public final static int DARKSALMON_VAL     = 4011; // darksalmon
    public final static int DARKSEAGREEN_VAL   = 4012; // darkseagreen
    public final static int DARKSLATEBLUE_VAL  = 4013; // darkslateblue
    public final static int DARKSLATEGRAY_VAL  = 4014; // darkslategray
    public final static int DARKTURQUOISE_VAL  = 4015; // darkturquoise
    public final static int DARKVIOLET_VAL     = 4016; // darkviolet
    public final static int DEEPPINK_VAL       = 4017; // deeppink
    public final static int DEEPSKYBLUE_VAL    = 4018; // deepskyblue
    public final static int DIMGRAY_VAL        = 4019; // dimgray
    public final static int DISPLACEMENT_VAL   = 4020; // displacement
    public final static int DODGERBLUE_VAL     = 4021; // dodgerblue
    public final static int DRAG_VAL           = 4022; // drag
    int parseDCommand(String command)
    {
	if (command.equals("darkblue"))       return DARKBLUE_VAL;
	if (command.equals("darkcyan"))       return DARKCYAN_VAL;
	if (command.equals("darkgoldenrod"))  return DARKGOLDENROD_VAL;
	if (command.equals("darkgray"))       return DARKGRAY_VAL;
	if (command.equals("darkgrey"))       return DARKGRAY_VAL;
	if (command.equals("darkgreen"))      return DARKGREEN_VAL;
	if (command.equals("darkkhaki"))      return DARKKHAKI_VAL;
	if (command.equals("darkmagenta"))    return DARKMAGENTA_VAL;
	if (command.equals("darkolivegreen")) return DARKOLIVEGREEN_VAL;
	if (command.equals("darkorange"))     return DARKORANGE_VAL;
	if (command.equals("darkorchid"))     return DARKORCHID_VAL;
	if (command.equals("darkred"))        return DARKRED_VAL;
	if (command.equals("darksalmon"))     return DARKSALMON_VAL;
	if (command.equals("darkseagreen"))   return DARKSEAGREEN_VAL;
	if (command.equals("darkslateblue"))  return DARKSLATEBLUE_VAL;
	if (command.equals("darkslategray"))  return DARKSLATEGRAY_VAL;
	if (command.equals("darkslategrey"))  return DARKSLATEGRAY_VAL;
	if (command.equals("darkturquoise"))  return DARKTURQUOISE_VAL;
	if (command.equals("darkviolet"))     return DARKVIOLET_VAL;
	if (command.equals("deeppink"))       return DEEPPINK_VAL;
	if (command.equals("deepskyblue"))    return DEEPSKYBLUE_VAL;
	if (command.equals("dimgray"))        return DIMGRAY_VAL;
	if (command.equals("dimgrey"))        return DIMGRAY_VAL;
	if (command.equals("displacement"))   return DISPLACEMENT_VAL;
	if (command.equals("dodgerblue"))     return DODGERBLUE_VAL;
	if (command.equals("drag"))           return DRAG_VAL;
	return _ERROR_VAL;
    }

    public final static int EXIT_VAL = 5000; // exit
    public final static int ECHO_VAL = 5001; // echo
    int parseECommand(String command)
    {
	if (command.equals("exit"))      return EXIT_VAL;
	else if (command.equals("echo")) return ECHO_VAL;
	return _ERROR_VAL;
    }

    public final static int FALSE_VAL       = 6000; // false
    public final static int FASTA_VAL       = 6001; // fasta
    public final static int FIREBRICK_VAL   = 6002; // firebrick
    public final static int FIT_VAL         = 6003; // fit
    public final static int FLORALWHITE_VAL = 6004; // floralwhite
    public final static int FORESTGREEN_VAL = 6005; // forestgreen
    public final static int FROM_VAL        = 6006; // from
    public final static int FUCHSIA_VAL     = 6007; // fuchsia
    int parseFCommand(String command)
    {
	if (command.equals("false"))       return FALSE_VAL;
	if (command.equals("fasta"))       return FASTA_VAL;
	if (command.equals("firebrick"))   return FIREBRICK_VAL;
	if (command.equals("fit"))         return FIT_VAL;
	if (command.equals("floralwhite")) return FLORALWHITE_VAL;
	if (command.equals("forestgreen")) return FORESTGREEN_VAL;
	if (command.equals("from"))        return FROM_VAL;
	if (command.equals("fuchsia"))     return FUCHSIA_VAL;
	return _ERROR_VAL;
    }

    public final static int GAINSBORO_VAL   = 7000; // gainsboro
    public final static int GET_VAL         = 7001; // get
    public final static int GHOSTWHITE_VAL  = 7002; // ghostwhite
    public final static int GOLD_VAL        = 7003; // gold
    public final static int GOLDENROD_VAL   = 7004; // goldenrod
    public final static int GRAY_VAL        = 7005; // gray
    public final static int GREEN_VAL       = 7006; // green
    public final static int GREENYELLOW_VAL = 7007; // greenyellow
    public final static int GROUP_VAL       = 7008; // group
    public final static int GROUPMOL_VAL    = 7009; // groupmol
    int parseGCommand(String command)
    {
	if (command.equals("gainsboro"))   return GAINSBORO_VAL;
	if (command.equals("get"))         return GET_VAL;
	if (command.equals("ghostwhite"))  return GHOSTWHITE_VAL;
	if (command.equals("gold"))        return GOLD_VAL;
	if (command.equals("goldenrod"))   return GOLDENROD_VAL;
	if (command.equals("gray"))        return GRAY_VAL;
	if (command.equals("grey"))        return GRAY_VAL;
	if (command.equals("green"))       return GREEN_VAL;
	if (command.equals("greenyellow")) return GREENYELLOW_VAL;
	if (command.equals("group"))       return GROUP_VAL;
	if (command.equals("groupmol"))    return GROUPMOL_VAL;
	return _ERROR_VAL;
    }

    public final static int HONEYDEW_VAL = 8000;
    public final static int HOTPINK_VAL  = 8001;
    int parseHCommand(String command)
    {
	if (command.equals("honeydew")) return HONEYDEW_VAL;
	if (command.equals("hotpink"))  return HOTPINK_VAL;
	return _ERROR_VAL;
    }

    public final static int INDIANRED_VAL = 9000; // indianred
    public final static int INDIGO_VAL    = 9001; // indigo
    public final static int IVORY_VAL     = 9002; // ivory
    int parseICommand(String command)
    {
	if (command.equals("indianred")) return INDIANRED_VAL;
	if (command.equals("indigo"))    return INDIGO_VAL;
	if (command.equals("ivory"))     return IVORY_VAL;
	return _ERROR_VAL;
    }

    public final static int JAR_VAL = 10000; // jar
    int parseJCommand(String command)
    {
	if (command.equals("jar")) return JAR_VAL;
	return _ERROR_VAL;
    }

    public final static int KHAKI_VAL = 11000; // khaki
    int parseKCommand(String command)
    {
	if (command.equals("khaki")) return KHAKI_VAL;
	return _ERROR_VAL;
    }

    public final static int LAVENDER_VAL       = 12000; // lavender
    public final static int LAVENDERBLUSH_VAL  = 12001; // lavenderblush
    public final static int LAWNGREEN_VAL      = 12002; // lawngreen
    public final static int LEMONCHIFFON_VAL   = 12003; // lemonchiffon
    public final static int LIGHTBLUE_VAL      = 12004; // lightblue
    public final static int LIGHTCORAL_VAL     = 12005; // lightcoral
    public final static int LIGHTCYAN_VAL      = 12006; // lightcyan
    public final static int LIGHTGOLDENRODYELLOW_VAL = 1207;
    public final static int LIGHTGREEN_VAL     = 12008; // lightgreen
    public final static int LIGHTGRAY_VAL      = 12009; // lightgray
    public final static int LIGHTPINK_VAL      = 12010; // lightpink
    public final static int LIGHTSALMON_VAL    = 12011; // lightsalmon
    public final static int LIGHTSEAGREEN_VAL  = 12012; // lightseagreen
    public final static int LIGHTSKYBLUE_VAL   = 12013; // lightskyblue
    public final static int LIGHTSLATEGRAY_VAL = 12014; // lightslategray
    public final static int LIGHTSTEELBLUE_VAL = 12015; // lightsteelblue
    public final static int LIGHTYELLOW_VAL    = 12016; // lightyellow
    public final static int LIME_VAL           = 12017; // lime
    public final static int LIMEGREEN_VAL      = 12018; // limegreen
    public final static int LINEN_VAL          = 12019; // linen
    public final static int LOAD_VAL           = 12020; // load
    int parseLCommand(String command)
    {
	if (command.equals("lavender"))       return LAVENDER_VAL;
	if (command.equals("lavenderblush"))  return LAVENDERBLUSH_VAL;
	if (command.equals("lawngreen"))      return LAWNGREEN_VAL;
	if (command.equals("lemonchiffon"))   return LEMONCHIFFON_VAL;
	if (command.equals("lightblue"))      return LIGHTBLUE_VAL;
	if (command.equals("lightcoral"))     return LIGHTCORAL_VAL;
	if (command.equals("lightcyan"))      return LIGHTCYAN_VAL;
	if (command.equals("lightgoldenrodyellow"))
	    return LIGHTGOLDENRODYELLOW_VAL;
	if (command.equals("lightgreen"))     return LIGHTGREEN_VAL;
	if (command.equals("lightgray"))      return LIGHTGRAY_VAL;
	if (command.equals("lightgrey"))      return LIGHTGRAY_VAL;
	if (command.equals("lightpink"))      return LIGHTPINK_VAL;
	if (command.equals("lightsalmon"))    return LIGHTSALMON_VAL;
	if (command.equals("lightseagreen"))  return LIGHTSEAGREEN_VAL;
	if (command.equals("lightskyblue"))   return LIGHTSKYBLUE_VAL;
	if (command.equals("lightslategray")) return LIGHTSLATEGRAY_VAL;
	if (command.equals("lightslategrey")) return LIGHTSLATEGRAY_VAL;
	if (command.equals("lightsteelblue")) return LIGHTSTEELBLUE_VAL;
	if (command.equals("lightyellow"))    return LIGHTYELLOW_VAL;
	if (command.equals("lime"))           return LIME_VAL;
	if (command.equals("limegreen"))      return LIMEGREEN_VAL;
	if (command.equals("linen"))          return LINEN_VAL;
	if (command.equals("load"))           return LOAD_VAL;
	return _ERROR_VAL;
    }

    public final static int MAGENTA_VAL           = 13000; // Fmagenta
    public final static int MAROON_VAL            = 13001; // maroon
    public final static int MEDIUMAQUAMARINE_VAL  = 13002; // mediumaquamarine
    public final static int MEDIUMBLUE_VAL        = 13003; // mediumblue
    public final static int MEDIUMORCHID_VAL      = 13004; // mediumorchid
    public final static int MEDIUMPURPLE_VAL      = 13005; // mediumpurple
    public final static int MEDIUMSEAGREEN_VAL    = 13006; // mediumseagreen
    public final static int MEDIUMSLATEBLUE_VAL   = 13007; // mediumslateblue
    public final static int MEDIUMSPRINGGREEN_VAL = 13008; // mediumspringgreen
    public final static int MEDIUMTURQUOISE_VAL   = 13009; // mediumturquoise
    public final static int MEDIUMVIOLETRED_VAL   = 13010; // mediumvioletred
    public final static int MIDNIGHTBLUE_VAL      = 13011; // midnightblue
    public final static int MINTCREAM_VAL         = 13012; // mintcream
    public final static int MISTYROSE_VAL         = 13013; // mistyrose
    public final static int MOCCASIN_VAL          = 13014; // moccasin
    public final static int MODEL_VAL             = 13015; // model
    public final static int MOLECULE_VAL          = 13016; // molecule
    public final static int MOTION_VAL            = 13017; // motion
    int parseMCommand(String command)
    {
	if (command.equals("magenta"))           return MAGENTA_VAL;
	if (command.equals("maroon"))            return MAROON_VAL;
	if (command.equals("mediumaquamarine"))  return MEDIUMAQUAMARINE_VAL;
	if (command.equals("mediumblue"))        return MEDIUMBLUE_VAL;
	if (command.equals("mediumorchid"))      return MEDIUMORCHID_VAL;
	if (command.equals("mediumpurple"))      return MEDIUMPURPLE_VAL;
	if (command.equals("mediumseagreen"))    return MEDIUMSEAGREEN_VAL;
	if (command.equals("mediumslateblue"))   return MEDIUMSLATEBLUE_VAL;
	if (command.equals("mediumspringgreen")) return MEDIUMSPRINGGREEN_VAL;
	if (command.equals("mediumturquoise"))   return MEDIUMTURQUOISE_VAL;
	if (command.equals("mediumvioletred"))   return MEDIUMVIOLETRED_VAL;
	if (command.equals("midnightblue"))      return MIDNIGHTBLUE_VAL;
	if (command.equals("mintcream"))         return MINTCREAM_VAL;
	if (command.equals("mistyrose"))         return MISTYROSE_VAL;
	if (command.equals("moccasin"))          return MOCCASIN_VAL;
	if (command.equals("model"))             return MODEL_VAL;
	if (command.equals("molecule"))          return MOLECULE_VAL;
	if (command.equals("motion"))            return MOTION_VAL;
	return _ERROR_VAL;
    }

    public final static int NAVAJOWHITE_VAL = 14000; // navajowhite
    public final static int NAVY_VAL        = 14001; // navy
    public final static int NMA_VAL         = 14002; // nma
    public final static int NO_VAL          = 14003; // no
    public final static int NOT_VAL         = 14004; // not
    public final static int NOCLUSTER_VAL   = 14005; // nocluster
    public final static int NOREFINE_VAL    = 14006; // norefine
    int parseNCommand(String command)
    {
	if (command.equals("navajowhite")) return NAVAJOWHITE_VAL;
	if (command.equals("navy"))        return NAVY_VAL;
	if (command.equals("nma"))         return NMA_VAL;
	if (command.equals("no"))          return NO_VAL;
	if (command.equals("not"))         return NOT_VAL;
	if (command.equals("nocluster"))   return NOCLUSTER_VAL;
	if (command.equals("norefine"))    return NOREFINE_VAL;
	return _ERROR_VAL;
    }

    public final static int OCCUPANCY_VAL = 15000; // occupancy
    public final static int OFF_VAL       = 15001; // off
    public final static int ON_VAL        = 15002; // on
    public final static int OLDLACE_VAL   = 15003; // oldlace
    public final static int OLIVE_VAL     = 15004; // olive
    public final static int OLIVEDRAB_VAL = 15005; // olivedrab
    public final static int OR_VAL        = 15006; // or
    public final static int ORANGE_VAL    = 15007; // orange
    public final static int ORANGERED_VAL = 15008; // orangered
    public final static int ORCHID_VAL    = 15009; // orchid
    int parseOCommand(String command)
    {
	if (command.equals("occupancy")) return OCCUPANCY_VAL;
	if (command.equals("off"))       return OFF_VAL;
	if (command.equals("on"))        return ON_VAL;
	if (command.equals("oldlace"))   return OLDLACE_VAL;
	if (command.equals("olive"))     return OLIVE_VAL;
	if (command.equals("olivedrab")) return OLIVEDRAB_VAL;
	if (command.equals("or"))        return OR_VAL;
	if (command.equals("orange"))    return ORANGE_VAL;
	if (command.equals("orangered")) return ORANGERED_VAL;
	if (command.equals("orchid"))    return ORCHID_VAL;
	return _ERROR_VAL;
    }

    public final static int PALEGOLDENROD_VAL = 16000; // palegoldenrod
    public final static int PALEGREEN_VAL     = 16001; // palegreen
    public final static int PALETURQUOISE_VAL = 16002; // paleturquoise
    public final static int PALEVIOLETRED_VAL = 16003; // palevioletred
    public final static int PAPAYAWHIP_VAL    = 16004; // papayawhip
    public final static int PDB_VAL           = 16005; // pdb
    public final static int PDBLOAD_VAL       = 16006; // pdbload
    public final static int PEACHPUFF_VAL     = 16007; // peachpuff
    public final static int PERU_VAL          = 16008; // peru
    public final static int PINK_VAL          = 16009; // pink
    public final static int PLUM_VAL          = 16010; // plum
    public final static int POWDERBLUE_VAL    = 16011; // powderblue
    public final static int PRINT_VAL         = 16012; // print
    public final static int PROJECT_VAL       = 16013; // project
    public final static int PURPLE_VAL        = 16014; // purple
    int parsePCommand(String command)
    {
	if (command.equals("palegoldenrod")) return PALEGOLDENROD_VAL;
	if (command.equals("palegreen"))     return PALEGREEN_VAL;
	if (command.equals("paleturquoise")) return PALETURQUOISE_VAL;
	if (command.equals("palevioletred")) return PALEVIOLETRED_VAL;
	if (command.equals("papayawhip"))    return PAPAYAWHIP_VAL;
	if (command.equals("pdb"))           return PDB_VAL;
	if (command.equals("pdbload"))       return PDBLOAD_VAL;
	if (command.equals("peachpuff"))     return PEACHPUFF_VAL;
	if (command.equals("peru"))          return PERU_VAL;
	if (command.equals("pink"))          return PINK_VAL;
	if (command.equals("plum"))          return PLUM_VAL;
	if (command.equals("powderblue"))    return POWDERBLUE_VAL;
	if (command.equals("print"))         return PRINT_VAL;
	if (command.equals("project"))       return PROJECT_VAL;
	if (command.equals("purple"))        return PURPLE_VAL;
	return _ERROR_VAL;
    }

    public final static int QUIT_VAL = EXIT_VAL; // quit
    int parseQCommand(String command)
    {
	if (command.equals("quit")) return QUIT_VAL;
	return _ERROR_VAL;
    }

    public final static int RED_VAL       = 18000; // red
    public final static int REFINE_VAL    = 18001; // refine
    public final static int RIBBONS_VAL   = 18002; // ribbons
    public final static int RIGIDS_VAL    = 18003; // rigids
    public final static int ROSYBROWN_VAL = 18004; // rosybrown
    public final static int ROYALBLUE_VAL = 18005; // royalblue
    int parseRCommand(String command)
    {
	if (command.equals("red"))       return RED_VAL;
	if (command.equals("refine"))    return REFINE_VAL;
	if (command.equals("ribbon"))    return RIBBONS_VAL;
	if (command.equals("ribbons"))   return RIBBONS_VAL;
	if (command.equals("rigids"))    return RIGIDS_VAL;
	if (command.equals("rosybrown")) return ROSYBROWN_VAL;
	if (command.equals("royalblue")) return ROYALBLUE_VAL;
	return _ERROR_VAL;
    }

    public final static int SADDLEBROWN_VAL = 19000; // saddlebrown
    public final static int SALMON_VAL      = 19001; // salmon
    public final static int SANDYBROWN_VAL  = 19002; // sandybrown
    public final static int SAVE_VAL        = 19003; // save
    public final static int SEAGREEN_VAL    = 19004; // seagreen
    public final static int SEASHELL_VAL    = 19005; // seashell
    public final static int SELECT_VAL      = 19006; // select
    public final static int SELECTED_VAL    = 19007; // selected
    public final static int SET_VAL         = 19008; // set
    public final static int SEQUENCE_VAL    = 19009; // sequence
    public final static int SHAPELY_VAL     = 19010; // shapely
    public final static int SIENNA_VAL      = 19011; // sienna
    public final static int SILVER_VAL      = 19012; // silver
    public final static int SKYBLUE_VAL     = 19013; // skyblue
    public final static int SLATEBLUE_VAL   = 19014; // slateblue
    public final static int SLATEGRAY_VAL   = 19015; // slategray
    public final static int SNOW_VAL        = 19016; // snow
    public final static int SPACEFILL_VAL   = 19017; // spacefill
    public final static int SPLIT_VAL       = 19018; // split
    public final static int SPRINGGREEN_VAL = 19019; // springgreen
    public final static int STEELBLUE_VAL   = 19010; // steelblue
    public final static int STRANDS_VAL     = 19020; // strands
    int parseSCommand(String command)
    {
	if (command.equals("saddlebrown")) return SADDLEBROWN_VAL;
	if (command.equals("salmon"))      return SALMON_VAL;
	if (command.equals("sandybrown"))  return SANDYBROWN_VAL;
	if (command.equals("save"))        return SAVE_VAL;
	if (command.equals("seagreen"))    return SEAGREEN_VAL;
	if (command.equals("seashell"))    return SEASHELL_VAL;
	if (command.equals("select"))      return SELECT_VAL;
	if (command.equals("selected"))    return SELECTED_VAL;
	if (command.equals("set"))         return SET_VAL;
	if (command.equals("sequence"))    return SEQUENCE_VAL;
	if (command.equals("shapely"))     return SHAPELY_VAL;
	if (command.equals("sienna"))      return SIENNA_VAL;
	if (command.equals("silver"))      return SILVER_VAL;
	if (command.equals("skyblue"))     return SKYBLUE_VAL;
	if (command.equals("slateblue"))   return SLATEBLUE_VAL;
	if (command.equals("slategray"))   return SLATEGRAY_VAL;
	if (command.equals("slategrey"))   return SLATEGRAY_VAL;
	if (command.equals("snow"))        return SNOW_VAL;
	if (command.equals("spacefill"))   return SPACEFILL_VAL;
	if (command.equals("split"))       return SPLIT_VAL;
	if (command.equals("springgreen")) return SPRINGGREEN_VAL;
	if (command.equals("steelblue"))   return STEELBLUE_VAL;
	if (command.equals("strand"))      return STRANDS_VAL;
	if (command.equals("strands"))     return STRANDS_VAL;
	return _ERROR_VAL;
    }

    public final static int TAN_VAL         = 20000; // tan
    public final static int TEAL_VAL        = 20001; // teal
    public final static int TEMPERATURE_VAL = 20002; // temperature
    public final static int THISTLE_VAL     = 20003; // thistle
    public final static int TOMATO_VAL      = 20004; // tomato
    public final static int TRACE_VAL       = 20005; // trace
    public final static int TRUE_VAL        = 20006; // true
    public final static int TURQUOISE_VAL   = 20007; // turquoise
    int parseTCommand(String command)
    {
	if (command.equals("tan"))         return TAN_VAL;
	if (command.equals("teal"))        return TEAL_VAL;
	if (command.equals("temperature")) return TEMPERATURE_VAL;
	if (command.equals("thistle"))     return THISTLE_VAL;
	if (command.equals("tomato"))      return TOMATO_VAL;
	if (command.equals("trace"))       return TRACE_VAL;
	if (command.equals("true"))        return TRUE_VAL;
	if (command.equals("turquoise"))   return TURQUOISE_VAL;
	return _ERROR_VAL;
    }

    public final static int UNLOAD_VAL = 21000; // unload
    int parseUCommand(String command)
    {
	if (command.equals("unload")) return UNLOAD_VAL;
	return _ERROR_VAL;
    }

    public final static int VDW_VAL    = 22000; // vdw
    public final static int VIOLET_VAL = 22000; // violet
    int parseVCommand(String command)
    {
	if (command.equals("vdw"))    return VDW_VAL;
	if (command.equals("violet")) return VIOLET_VAL;
	return _ERROR_VAL;
    }

    public final static int WHEAT_VAL      = 23000; // wheat
    public final static int WHITE_VAL      = 23001; // white
    public final static int WHITESMOKE_VAL = 23002; // whitesmoke
    public final static int WIREFRAME_VAL  = 23003; // wireframe
    int parseWCommand(String command)
    {
	if (command.equals("wheat"))      return WHEAT_VAL;
	if (command.equals("white"))      return WHITE_VAL;
	if (command.equals("whitesmoke")) return WHITESMOKE_VAL;
	if (command.equals("wireframe"))  return WIREFRAME_VAL;
	return _ERROR_VAL;
    }

    int parseXCommand(String command)
    {
	return _ERROR_VAL;
    }

    public final static int YELLOW_VAL      = 25000; // yellow
    public final static int YELLOWGREEN_VAL = 25001; // yellowgreen
    public final static int YES_VAL         = 25002; // yes
    int parseYCommand(String command)
    {
	if (command.equals("yellow"))      return YELLOW_VAL;
	if (command.equals("yellowgreen")) return YELLOWGREEN_VAL;
	if (command.equals("yes"))         return YES_VAL;
	return _ERROR_VAL;
    }

    public final static int ZAP_VAL = 26000; // zap
    int parseZCommand(String command)
    {
	if (command.equals("zap")) return ZAP_VAL;
	return _ERROR_VAL;
    }

    // Parsed color
    private Color parsedColor_ = null;
    /**
     * Returns parsed color. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _USERCOLOR_VAL meaning
     * that user defined color was parsed.
     *
     * @return parsed color.
     */
    public  Color getParsedColor() { return parsedColor_; }
    int parseColor(String command) throws Exception
    {
	if (command.charAt(0) != '[') return _ERROR_VAL;

	// Looking for next ']'
	int firstToRead = firstToRead_;
	for (;firstToRead < input_.length();firstToRead++) {
	    char inChar = input_.charAt(firstToRead);
	    command += inChar;
	    if (inChar == ']') {
		firstToRead++;
		break;
	    }
	}

	String tmp = command.substring(1,command.length() - 1);
	StringTokenizer colors = new StringTokenizer(tmp,",");

	if (colors.countTokens() != 3) return _ERROR_VAL;

	tmp = colors.nextToken().trim();
	int r = Integer.parseInt(tmp);
	if (r < 0 || r > 255) return _ERROR_VAL;

	tmp = colors.nextToken().trim();
	int g = Integer.parseInt(tmp);
	if (g < 0 || g > 255) return _ERROR_VAL;

	tmp = colors.nextToken().trim();
	int b = Integer.parseInt(tmp);
	if (b < 0 || b > 255) return _ERROR_VAL;

	parsedColor_ = new Color(r,g,b);
	firstToRead_ = firstToRead;
	return _USERCOLOR_VAL;
    }

    // Parsed molecule name
    private String parsedMolName_      = null;
    /**
     * Returns parsed molecule name. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _EXPRESSION_VAL meaning
     * that expression  was parsed.
     *
     * @return parsed molecule name.
     */
    public  String getParsedMolName() { return parsedMolName_; }

    // Parsed chain ids
    private String parsedChainIds_ = null; // Must be one char
    /**
     * Returns parsed chain id. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _EXPRESSION_VAL meaning
     * that expression  was parsed.
     *
     * @return parsed chain id.
     */
    public  String getParsedChainIds() { return parsedChainIds_; }

    // Parsed assembly name
    private String parsedAssemblyName_ = null;
    /**
     * Returns parsed assembly name. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _EXPRESSION_VAL meaning
     * that expression  was parsed.
     *
     * @return parsed assembly name.
     */
    public  String getParsedAssemblyName() { return parsedAssemblyName_; }

    // Parsed assembly number
    private String parsedAssemblyNum_  = null; // To be converted to int
    /**
     * Returns parsed assembly number. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _EXPRESSION_VAL meaning
     * that expression  was parsed.
     *
     * @return parsed assembly number.
     */
    public  String getParsedAssemblyNum() { return parsedAssemblyNum_; }

    // Parsed atom name
    private String parsedAtomName_     = null;
    /**
     * Returns parsed atom name. It make sense to call this function if 
     * function {@link #parseCommand()} returned value _EXPRESSION_VAL meaning
     * that expression  was parsed.
     *
     * @return parsed atom name.
     */
    public  String getParsedAtomName() { return parsedAtomName_; }

    /**
     * Parses expression stating set of atoms.
     * Expression has general form of
     * <code>@[molName].[assName]assNum:chainName.atomName</code>.
     * <ul>
     * <li> molName -- molecule name (String). All the molecules having
     * substring 'molName' in their name may be affected. Naming is case
     * insensitive. Brackets can be
     * ommited if the name does not contain white spaces or dots. Please,
     * note that character '@' at the beginning is obligatory if you
     * specify molecule name. If the molecule name is ommited than
     * '@' and first dot in expression must be ommited as well.
     * <li> assName -- name of assembly (String). All the assemblies having
     * exactly the 'assName' may be affected. Naming is case insensitive.
     * Brackets can be ommited if the name contains only letters.
     * <li> assNum -- assembly number (int). Serial number of an assembly.
     * Can be negative. All the assemblies having the 'assNum' may be
     * affected.
     * <li> chainName -- single character representing chain name (char).
     * Naming is case sensitive. Must be not a digit to avoid unambiguity
     * with 'assNum'.
     * <li> atomName -- atom name in assembly (String). All atoms having the
     * exact 'atomName' may be affected. Naming is case insensitive. If 
     * atom name is ommited than preceeding dot must be ommited as well.
     * </ul>
     * 
     * Wildcard '*' stating 'any' can be applyed insted of 'assName',
     * 'assNum' and 'chainName'.
     * 
     * Examples:
     * <code>
     * <ul>
     * <li> *.CA            -- all CA atoms
     * <li> arg             -- all atoms of all argenines
     * <li> cys10           -- all atoms of cysteine #10
     * <li> cys-20          -- all atoms of cysteine #-20
     * <li> *A or *:A or :A -- all atoms in chain 'A'
     * <li> 1D or 1:D       -- all atoms of residues with serial number 1 in 
     * chain D
     * <li> @1crn           -- all atoms in molecule containing '1crn' in name
     * <li> @1crn.*.CA      -- all CA atoms in molecule containing '1crn' in
     * name
     * <li> @1crn.pro22_.CG -- atom CG atoms in proline #22 of
     * chain '_' of molecule containint '1crn' in name.
     * <li> *D and 1-20     -- all atoms in residues with serial numbers from
     * 1 to 20 in chain 'D'.
     * </ul>
     * </code>
     *
     * @param command command to be parsed
     * @return _EXPRESSION_VAL in case of success and _ERROR_VAL otherwise.
     *
     */
    public int parseExpression(String command) throws Exception
    {
	if (command == null) return _ERROR_VAL;
	if (command.equals("")) return _ERROR_VAL;

 	command = command.replaceAll("\\*+","\\*").trim();

	// Resetting values
	parsedMolName_      = null;
	parsedChainIds_     = null;
	parsedAssemblyName_ = null;
	parsedAssemblyNum_  = null;
	parsedAtomName_     = null;

	// Parsing molecule name
	char firstChar = command.charAt(0);
	if (firstChar == '@') {
	    if (command.length() < 2) return _ERROR_VAL;
	    char secondChar = command.charAt(1);
	    if (secondChar == '[') { // @[1crn].[cys]-10A.CA
		int indCloseBracket = command.indexOf(']');
		if (indCloseBracket < 0) return _ERROR_VAL;
		parsedMolName_ = command.substring(2,indCloseBracket);
		command = command.substring(indCloseBracket + 1);
	    } else {
		int len = command.length();
		int index = 1;
		for (;index < len;index++) {
		    char currChar = command.charAt(index);
		    if (currChar != '.') {
			if (parsedMolName_ == null)
			    parsedMolName_ = new String("");
			parsedMolName_ += currChar;
		    } else {
			index++;
			if (index >= len) index--;
			break;
		    }
		}
		command = command.substring(index);
	    }
	    if (command.startsWith("."))
		command = command.substring(1);
	}

	// Parsing assembly name
	if (command.equals("")) return _EXPRESSION_VAL;
	firstChar = command.charAt(0);
	if (firstChar == '*') { // *-10A.CA or *A.CA
	    command = command.substring(1);
	} else if (firstChar == '.') { // .CA
	    // Do nothing
	} else if (firstChar == ':') { // :A.CA
	    // Do nothing
	} else if (firstChar == '[') { // [cys]-10A.CA
	    int indCloseBracket = command.indexOf(']');
	    if (indCloseBracket < 0) return _ERROR_VAL;
	    parsedAssemblyName_ = command.substring(1,indCloseBracket);
	    command = command.substring(indCloseBracket + 1);
	} else { // cys-10A.CA
	    parsedAssemblyName_ = new String("");
	    int  len = command.length();
	    int  index = 0;
	    for (;index < len;index++) {
		char currChar = command.charAt(index);
		if (Character.isLetter(currChar))
		    parsedAssemblyName_ += currChar;
		else break;
	    }
	    command = command.substring(index);
	}

	// Parsing assembly number
	if (command.equals("")) return _EXPRESSION_VAL;
	int  index = 0;
	firstChar = command.charAt(index);
	if (firstChar == ':') { // :A.CA
	    // Do nothing
	} else if (firstChar == '.') { // .CA
	    // Do nothing
	} else { // -10A.CA
	    int  len = command.length();
	    if (firstChar == '-') {
		parsedAssemblyNum_ = new String("-");
		if (index >= len) return _ERROR_VAL;
		firstChar = command.charAt(++index);
		if (!Character.isDigit(firstChar)) return _ERROR_VAL;
	    }
	    
	    for (;index < len;index++) {
		char currChar = command.charAt(index);
		if (Character.isDigit(currChar)) {
		    if (parsedAssemblyNum_ == null)
			parsedAssemblyNum_ = new String("");
		    parsedAssemblyNum_ += currChar;
		}
		else break;
	    }
	    command = command.substring(index);
	}

	// Parsing chain name
	if (command.equals("")) return _EXPRESSION_VAL;
	index = 0;
	firstChar = command.charAt(index);
	if (firstChar == ':') { // :A.CA
	    command = command.substring(1);
	    firstChar = command.charAt(index);
	}

	if (firstChar == '*') { // *.CA
	    command = command.substring(1);
	} else if (firstChar == '.') { // .CA
	    // Do nothing
	} else { // A.CA
	    int len = command.length();
	    for (;index < len;index++) {
		char currChar = command.charAt(index);
		if (currChar != '.') {
		    if (parsedChainIds_ == null)
			parsedChainIds_ = new String("");
		    parsedChainIds_ += currChar;
		} else {
		    index++;
		    if (index >= len) index--;
		    break;
		}
		    
	    }

	    command = command.substring(index);
	}

	// Parsing atom name
	if (command.equals("")) return _EXPRESSION_VAL;

	firstChar = command.charAt(0);
	if (firstChar == '.') // .CA
	    command = command.substring(1);

	parsedAtomName_ = command;

	return _EXPRESSION_VAL;
    }
}
