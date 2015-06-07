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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Dakror
 */
public class ZipAssistant {
	public static final byte[] BUFFER = new byte[0xFFFF];
	
	public static File unzip(File zip, File dest) {
		
		try {
			ZipFile zipFile = new ZipFile(zip);
			for (ZipEntry entry : Collections.list(zipFile.entries())) {
				extractEntry(zipFile, entry, dest.getPath().replace("\\", "/"));
			}
			zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return zip;
	}
	
	public static void extractEntry(ZipFile zipFile, ZipEntry entry, String destDir) throws IOException {
		File file = new File(destDir + "/" + entry.getName().replace("\\", "/"));
		if (entry.isDirectory()) file.mkdirs();
		else {
			file.getParentFile().mkdirs();
			InputStream is = null;
			OutputStream os = null;
			try {
				is = zipFile.getInputStream(entry);
				os = new FileOutputStream(file);
				for (int len; (len = is.read(BUFFER)) != -1;) {
					os.write(BUFFER, 0, len);
				}
			} finally {
				if (os != null) os.close();
				if (is != null) is.close();
			}
			file.setExecutable(true, false);
			file.setWritable(true, false);
		}
	}
}
