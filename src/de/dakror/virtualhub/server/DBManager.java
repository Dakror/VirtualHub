package de.dakror.virtualhub.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.dakror.virtualhub.data.Eticet;

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
			s.executeUpdate("CREATE TABLE IF NOT EXISTS VIRTUALHUB(PATH varchar(500) NOT NULL PRIMARY KEY, ETICET INT)"); /* missing tags column */
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
				ResultSet rs = connection.createStatement().executeQuery("SELECT ETICET FROM VIRTUALHUB WHERE PATH = \"" + f.getPath().replace("\\", "/") + "\"");
				if (!rs.next())
				{
					Server.currentServer.frame.log("doesn't exist: " + f);
					return Eticet.NONE;
				}
				
				Server.currentServer.frame.log("return entry: " + f);
				return Eticet.values()[rs.getInt(1)];
			}
			else
			{
				if (e == Eticet.NONE)
				{
					Server.currentServer.frame.log("clearing entry: " + f);
					connection.createStatement().executeUpdate("DELETE FROM VIRTUALHUB WHERE PATH = \"" + f.getPath().replace("\\", "/") + "\"");
				}
				else
				{
					Server.currentServer.frame.log("inserting/updating entry: " + f);
					connection.createStatement().executeUpdate("INSERT OR REPLACE INTO VIRTUALHUB VALUES(\"" + f.getPath().replace("\\", "/") + "\"," + e.ordinal() + ")");
				}
			}
		}
		catch (SQLException e1)
		{
			e1.printStackTrace();
		}
		return null;
	}
}
