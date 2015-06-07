/*******************************************************************************
 * Copyright 2015 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package de.dakror.virtualhub.util;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {
	@Override
	public int compare(File f1, File f2) {
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
