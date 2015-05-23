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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class JHintTextField extends JTextField {
	
	private static final long serialVersionUID = 1L;
	String hint;
	
	
	public JHintTextField(String h) {
		super();
		hint = h;
		setHintVisible(true);
		addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if (getText().length() == 0 || getText().equals(hint)) setHintVisible(true);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				if (getText().length() == 0 || getText().equals(hint)) {
					if (getText().equals(hint)) setText("");
					setHintVisible(false);
				}
			}
		});
	}
	
	private void setHintVisible(boolean v) {
		if (v) {
			setFont(getFont().deriveFont(Font.ITALIC));
			setForeground(Color.decode("#888888"));
			setText(hint);
		} else {
			setFont(getFont().deriveFont(Font.PLAIN));
			setForeground(Color.black);
		}
	}
}
