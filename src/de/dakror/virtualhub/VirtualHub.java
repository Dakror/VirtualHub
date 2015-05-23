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
 

package de.dakror.virtualhub;

import java.util.Properties;

import javax.swing.UIManager;

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;

import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;


/**
 * @author Dakror
 */
public class VirtualHub {
	public static void init() {
		CFG.INTERNET = Assistant.isInternetReachable();
		
		try {
			Properties props = new Properties();
			props.put("logoString", "");
			AcrylLookAndFeel.setCurrentTheme(props);
			UIManager.setLookAndFeel(new AcrylLookAndFeel());
			UIManager.put("ProgressBar.cycleTime", new Integer(6000));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Reporter.init(new File(CFG.DIR, "Logs"));
	}
}
