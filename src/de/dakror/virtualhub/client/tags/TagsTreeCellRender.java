package de.dakror.virtualhub.client.tags;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.dakror.virtualhub.client.file.FileButton;

/**
 * @author Dakror
 */
public class TagsTreeCellRender extends DefaultTreeCellRenderer
{
	private static final long serialVersionUID = 1L;
	
	public FileButton selectedFile;
	
	public TagsTreeCellRender()
	{
		leafIcon = new ImageIcon(getClass().getResource("/img/tag.png"));
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		JLabel tce = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		if (row == 0) tce.setIcon(null);
		
		if (selectedFile != null && selectedFile.tags.contains(value.toString()))
		{
			selected = true;
		}
		
		return tce;
	}
}
