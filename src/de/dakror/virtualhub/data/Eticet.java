package de.dakror.virtualhub.data;

import java.awt.Color;

/**
 * @author Dakror
 */
public enum Eticet
{
	NULL("null", new Color(150, 100, 50)),
	NONE("Kein Etikett", new Color(0, 0, 0, 0)),
	RED("Rot", Color.red),
	ORANGE("Orange", Color.orange),
	YELLOW("Gelb", Color.yellow),
	GREEN("Gr\u00fcn", Color.green),
	BLUE("Blau", Color.blue),
	MAGENTA("Violett", Color.magenta),
	GRAY("Grau", Color.gray);
	
	private String name;
	private Color c;
	
	private Eticet(String name, Color c)
	{
		this.name = name;
		this.c = c;
	}
	
	public Color getColor()
	{
		return c;
	}
	
	public String getName()
	{
		return name;
	}
	
	public static Eticet getByName(String name)
	{
		for (Eticet e : values())
			if (e.getName().equals(name)) return e;
		
		return NONE;
	}
}