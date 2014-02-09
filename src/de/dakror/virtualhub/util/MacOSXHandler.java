package de.dakror.virtualhub.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.apple.laf.AquaIcon;
/**
 * @author Dakror
 */
public class MacOSXHandler
{
	public static Icon getIcon(Icon input)
	{
		return new ImageIcon(AquaIcon.getImageForIcon(input));
	}
}
