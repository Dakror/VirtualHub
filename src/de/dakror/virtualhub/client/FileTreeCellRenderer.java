package de.dakror.virtualhub.client;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1L;
	
	ClientFrame frame;
	public int highlightedRow = -1;
	
	public FileTreeCellRenderer(ClientFrame frame)
	{
		this.frame = frame;
		
		setLeafIcon(CFG.FOLDER);
		setOpenIcon(CFG.FOLDER);
		setClosedIcon(CFG.FOLDER);
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		JLabel tce = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (row < 0 || row >= tree.getRowCount()) return tce;
		
		EticetableTreeNode etn = (EticetableTreeNode) value;
		
		if (highlightedRow == row)
		{
			selected = true;
			tce.setForeground(Color.white);
			this.hasFocus = true;
		}
		
		tce.setOpaque(true);
		tce.setBackground(etn.getEticet().getColor());
		
		return tce;
	}
}
