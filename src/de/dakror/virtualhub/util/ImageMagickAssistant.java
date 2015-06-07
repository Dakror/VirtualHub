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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.jtattoo.plaf.JTattooUtilities;

import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class ImageMagickAssistant {
	public static File dir = new File(Client.dir, "ImageMagick");
	
	public static void init() {
		if (dir.mkdir()) {
			File tmpFile = new File(dir, "tmp.zip");
			try {
				Assistant.copyInputStream(ImageMagickAssistant.class.getResourceAsStream("/ImageMagick.zip"), new FileOutputStream(tmpFile));
				ZipAssistant.unzip(tmpFile, dir);
				tmpFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static BufferedImage getThumbnail(File f) {
		try {
			if (f.isDirectory()) return null;
			
			String filePath = f.getPath().replace("\\", "/");
			filePath = filePath.substring(0, filePath.indexOf(".") > -1 ? filePath.lastIndexOf(".") : filePath.length()) + "CACHECACHE.png";
			
			ArrayList<String> cmds = new ArrayList<String>();
			for (String s : getAdditionalParameters())
				cmds.add(s);
			
			String exec = "";
			if (JTattooUtilities.isWindows()) exec = "/windows/convert.exe";
			if (JTattooUtilities.isMac()) exec = "/mac/convert.sh";
			
			if (JTattooUtilities.isMac()) cmds.add("export MAGICK_HOME=\"" + dir.getPath().replace("\\", "/")
					+ "/mac\"; export PATH=\"$MAGICK_HOME:$PATH\"; export DYLD_LIBRARY_PATH=\"$MAGICK_HOME/\"; \"" + dir.getPath().replace("\\", "/") + exec + "\" \""
					+ f.getPath().replace("\\", "/") + "\" -layers merge -thumbnail " + CFG.PREVIEWSIZE.width + "x" + CFG.PREVIEWSIZE.height + " \"" + filePath + "\"");
			else {
				cmds.add("\"" + dir.getPath().replace("\\", "/") + exec + "\"");
				cmds.add("\"" + f.getPath().replace("\\", "/") + "\"");
				cmds.add("-layers");
				cmds.add("merge");
				cmds.add("-thumbnail");
				cmds.add(CFG.PREVIEWSIZE.width + "x" + CFG.PREVIEWSIZE.height);
				cmds.add("\"" + filePath + "\"");
			}
			
			Process process = new ProcessBuilder(cmds.toArray(new String[] {})).start();
			process.waitFor();
			
			File dest = new File(filePath);
			if (!dest.exists()) {
				dest.delete();
				return null;
			}
			
			BufferedImage image = ImageIO.read(dest);
			
			dest.delete();
			
			return image;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Dimension getSize(File f) {
		try {
			if (f.isDirectory()) return null;
			
			ArrayList<String> cmds = new ArrayList<String>();
			for (String s : getAdditionalParameters())
				cmds.add(s);
			
			String exec = "";
			if (JTattooUtilities.isWindows()) exec = "/windows/identify.exe";
			if (JTattooUtilities.isMac()) exec = "/mac/identify.sh";
			
			if (JTattooUtilities.isMac()) cmds.add("export MAGICK_HOME=\"" + dir.getPath().replace("\\", "/")
					+ "/mac\"; export PATH=\"$MAGICK_HOME:$PATH\"; export DYLD_LIBRARY_PATH=\"$MAGICK_HOME/\"; \"" + dir.getPath().replace("\\", "/") + exec + "\" -format \"%wx%h\" \""
					+ f.getPath().replace("\\", "/") + "\"");
			else {
				cmds.add("\"" + dir.getPath().replace("\\", "/") + exec + "\"");
				cmds.add("-format");
				cmds.add("\"%wx%h\"");
				cmds.add("\"" + f.getPath().replace("\\", "/") + "\"");
			}
			
			Process process = new ProcessBuilder(cmds.toArray(new String[] {})).start();
			process.waitFor();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Assistant.copyInputStream(process.getInputStream(), baos);
			String s = new String(baos.toByteArray()).trim();
			
			if (s.length() == 0 || s.indexOf("x") == -1) return null;
			
			Dimension d = new Dimension();
			d.width = Integer.parseInt(s.substring(0, s.indexOf("x")));
			d.height = Integer.parseInt(s.substring(s.indexOf("x") + 1));
			
			return d;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String[] getAdditionalParameters() {
		if (JTattooUtilities.isMac()) return new String[] { "/bin/sh", "-c" };
		return new String[] {};
	}
}
