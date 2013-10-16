package de.dakror.virtualhub.util;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File>
{
  @Override
  public int compare(File f1, File f2)
  {
    if (f1.isDirectory() && f2.isDirectory())
      return f1.compareTo(f2);
    else if (f1.isDirectory())
      return compareToFile(f1, f2);
    else if (f2.isDirectory())
      return -compareToFile(f2, f1);
    else return compareFiles(f1, f2);
  }
  
  private int compareFiles(File f1, File f2)
  {
    File parentFile1 = f1.getParentFile();
    File parentFile2 = f2.getParentFile();
    if (isSubDir(parentFile1, parentFile2))
      return -1;
    else if (isSubDir(parentFile2, parentFile1))
      return 1;
    else return f1.compareTo(f2);
  }
  
  private int compareToFile(File directory, File file)
  {
    File fileParent = file.getParentFile();
    if (directory.equals(fileParent))
      return -1;
    else if (isSubDir(directory, fileParent))
      return -1;
    else return directory.compareTo(file);
  }
  
  private boolean isSubDir(File directory, File subDir)
  {
    for (File parentDir = directory.getParentFile(); parentDir != null; parentDir = parentDir.getParentFile())
      if (subDir.equals(parentDir))
        return true;
    return false;
  }
}
