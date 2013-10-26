package de.dakror.virtualhub.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dakror.virtualhub.data.Eticet;
import de.dakror.virtualhub.data.Tags;
import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class DBManager
{
	public static File database;
	
	static Connection connection;
	
	public static void init()
	{
		try
		{
			database = new File(Server.dir, "virtualhub.db");
			database.createNewFile();
			
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + database.getPath().replace("\\", "/"));
			
			Statement s = connection.createStatement();
			s.executeUpdate("CREATE TABLE IF NOT EXISTS ETICETS(PATH varchar(500) NOT NULL PRIMARY KEY, ETICET INT)");
			s.executeUpdate("CREATE TABLE IF NOT EXISTS TAGS(PATH varchar(500) NOT NULL PRIMARY KEY, TAGS TEXT)");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static Eticet eticet(File f, Eticet e)
	{
		try
		{
			if (e == Eticet.NULL)
			{
				ResultSet rs = connection.createStatement().executeQuery("SELECT ETICET FROM ETICETS WHERE PATH = \"" + f.getPath().replace("\\", "/") + "\"");
				if (!rs.next()) return Eticet.NONE;
				
				return Eticet.values()[rs.getInt(1)];
			}
			else
			{
				if (e == Eticet.NONE) connection.createStatement().executeUpdate("DELETE FROM ETICETS WHERE PATH = \"" + f.getPath().replace("\\", "/") + "\"");
				else connection.createStatement().executeUpdate("INSERT OR REPLACE INTO ETICETS VALUES(\"" + f.getPath().replace("\\", "/") + "\"," + e.ordinal() + ")");
			}
		}
		catch (SQLException e1)
		{
			e1.printStackTrace();
		}
		return null;
	}
	
	public static Tags tags(File f, Tags t)
	{
		try
		{
			if (t == null)
			{
				ResultSet rs = connection.createStatement().executeQuery("SELECT TAGS FROM TAGS WHERE PATH = \"" + f.getPath().replace("\\", "/") + "\"");
				if (!rs.next()) return new Tags();
				
				CFG.p("data: " + rs.getString(1));
				// return new Tags();
			}
		}
		catch (SQLException e1)
		{
			e1.printStackTrace();
		}
		return null;
	}
}
