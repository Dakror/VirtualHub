package de.dakror.virtualhub.util;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File>
{
	@Override
	public int compare(File f1, File f2)
	{
		String ext1 = Assistant.getFileExtension(f1);
		String ext2 = Assistant.getFileExtension(f2);
		
		String name1 = f1.getName().substring(0, f1.getName().indexOf(".") > -1 ? f1.getName().lastIndexOf(".") : f1.getName().length());
		String name2 = f2.getName().substring(0, f2.getName().indexOf(".") > -1 ? f2.getName().lastIndexOf(".") : f2.getName().length());
		
		name1 = name1.toLowerCase();
		name2 = name2.toLowerCase();
		
		if (ext1.equals(ext2)) return name1.compareTo(name2);
		else return ext1.compareTo(ext2);
	}
}
