package com.planet_ink.coffee_mud.system;

import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class DataLoader
{
	public static void DBRead(Host myHost)
	{
		DBConnection D=null;
		try
		{
			D=DBConnector.DBFetch();
			ResultSet R=D.query("SELECT * FROM CMCLAN");
			while(R.next())
			{
				DBConnections.getRes(R,"CMDESC");
			}
			DBConnector.DBDone(D);
		}
		catch(SQLException sqle)
		{
			Log.errOut("DataLoader",sqle);
			if(D!=null) DBConnector.DBDone(D);
			return;
		}
		// log comment 
	}

}
