package de.dakror.virtualhub.client.file;

import javax.swing.tree.DefaultMutableTreeNode;

import de.dakror.virtualhub.data.Eticet;

/**
 * @author Dakror
 */
public class EticetableTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	
	private Eticet eticet = Eticet.NONE;
	
	public EticetableTreeNode() {}
	
	public EticetableTreeNode(Object userObject) {
		super(userObject);
	}
	
	public EticetableTreeNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}
	
	public Eticet getEticet() {
		return eticet;
	}
	
	public void setEticet(Eticet eticet) {
		this.eticet = eticet;
	}
}
