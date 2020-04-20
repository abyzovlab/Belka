package belka.draw;

//--- Java imports ---
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *  Panel for input commands and output messages
 *
 * @author Alexej Abyzov
 */
public class InOutPanel extends JPanel
{
    // Menu item containing invitation line
    private JMenuItem  invitationItem_ = null;

    // Text field for input commands
    private JTextField inTextField_    = null;

    // Text field for output messages
    private JTextField outTextField_   = null;

    // Flag to indicate that printed text has '\n' character
    private boolean previousCompleted_ = true;

    // Layout manager of the componenent
    private FlowLayout flowLayout_     = null;

    /**
     * Object constructor.
     *
     * @param invitation invitation text
     * @param inputFlag flag to indicate whether input field should 
     * be added.
     */
    public InOutPanel(String invitation,boolean inputFlag)
    {
	super();
	super.setLayout(flowLayout_ = new FlowLayout(FlowLayout.LEFT));
	if (invitation != null) {
	    invitationItem_ = new JMenuItem(invitation);
	    super.add(invitationItem_);
	}

	if (inputFlag) {
	    inTextField_  = new ScalableTextField("Input command here\n");
	    inTextField_.setEditable(true);
	    super.add(inTextField_);
	}

	outTextField_ = new ScalableTextField("Output message here\n");
	outTextField_.setEditable(false);
	super.add(outTextField_);
    }

    /**
     * Object constructor.
     *
     * @param invitation invitation text.
     */
    public InOutPanel(String invitation) { this(invitation,true); }

    /**
     * The function adds {@link ActionListener} to internal components.
     *
     * @param l {@link ActionListener} to add
     */
    public void addActionListener(ActionListener l)
    {
	if (inTextField_  != null) inTextField_.addActionListener(l);
	if (outTextField_ != null) outTextField_.addActionListener(l);
    }

    /**
     * Returns text inputed by user.
     *
     * @return text inputed by user
     */
    public String getInput()
    {
	if (inTextField_ == null) return "";
	String ret = inTextField_.getText();
	inTextField_.setText("");
	return ret;
    }
    
    /**
     * Sets text to display
     *
     * @param text text to display
     */
    public void setText(String text)
    {
 	if (text == null || text.length() == 0) return;
	
 	String old_text = "";
	if (!previousCompleted_) old_text = outTextField_.getText();

 	int end_char = text.length() - 1;
 	previousCompleted_ = false;
   	while (end_char >= 0 && text.charAt(end_char) == '\n') {
  	    end_char--;
  	    previousCompleted_ = true;
  	}
	if (end_char < 0) return;
	

 	int start_char = text.lastIndexOf("\n",end_char) + 1;
 	if (start_char > 0) old_text = "";

	String new_text = text.substring(start_char,end_char + 1);
  	outTextField_.setText(old_text + new_text);
    }

    class ScalableTextField extends JTextField implements MouseListener
    {
	public ScalableTextField(String text)
	{
	    super(text);
	    addMouseListener(this);
	}

	public Dimension getPreferredSize()
	{
	    double w = 0;
	    Component c = getParent();
	    if (c == null) return null;
	    Dimension d = c.getSize();
	    if (d == null) return null;
	    w = d.getWidth();
	    if (invitationItem_ != null && 
		invitationItem_.getPreferredSize() != null)
		w -= invitationItem_.getPreferredSize().getWidth();
	    w -= flowLayout_.getVgap()*4;
	    if (outTextField_ != null && inTextField_ != null) w /= 2.;
	    d = super.getPreferredSize();
	    d.setSize(w,d.getHeight());
	    return d;
	}

	// Methods of MouseListener interface
	private boolean firstTime = true;
	public void mouseClicked(MouseEvent e)  {}
	public void mouseEntered(MouseEvent e)  {}
	public void mouseExited(MouseEvent e)   {}
	public void mousePressed(MouseEvent e)
	{
	    if (firstTime) {
		if (isEditable()) setText("");
		firstTime = false;
	    }
	}
	public void mouseReleased(MouseEvent e) {}
    }
}
