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
