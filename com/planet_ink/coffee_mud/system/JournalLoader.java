package com.planet_ink.coffee_mud.system;
import java.sql.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class JournalLoader
{
	public static synchronized StringBuffer DBRead(String Journal, String username, int which, long lastTimeDate)
	{
		Vector journal=null;//(Vector)Resources.getResource("JOURNAL_"+Journal);
		StringBuffer buf=new StringBuffer("");
		if(which<0)
		{
			buf.append(Util.padRight("#",6)+Util.padRight("From",16)+Util.padRight("To",16)+"Subject\n\r");
			buf.append("------------------------------------------------------------------------------\n\r");
		}
		if(journal==null)
		{
			journal=new Vector();
			//Resources.submitResource("JOURNAL_"+Journal);
			DBConnection D=null;
			try
			{
				D=DBConnector.DBFetch();
				ResultSet R=D.query("SELECT * FROM CMJRNL WHERE CMJRNL='"+Journal+"'");
				while(R.next())
				{
					Vector entry=new Vector();
					entry.addElement(DBConnections.getRes(R,"CMFROM"));
					entry.addElement(DBConnections.getRes(R,"CMDATE"));
					entry.addElement(DBConnections.getRes(R,"CMTONM"));
					entry.addElement(DBConnections.getRes(R,"CMSUBJ"));
					entry.addElement(DBConnections.getRes(R,"CMMSGT"));
					journal.addElement(entry);
				}
				DBConnector.DBDone(D);
			}
			catch(SQLException sqle)
			{
				Log.errOut("Journal",sqle);
				if(D!=null) DBConnector.DBDone(D);
			}
			return buf;
		}
		if((which<0)||(which>=journal.size()))
		{
			for(int j=0;j<journal.size();j++)
			{
				Vector entry=(Vector)journal.elementAt(j);
				String from=(String)entry.elementAt(0);
				String date=(String)entry.elementAt(1);
				String to=(String)entry.elementAt(2);
				String subject=(String)entry.elementAt(3);
				if(to.equals("ALL")||to.equalsIgnoreCase(username))
				{
					if(Util.s_int(date)>lastTimeDate)
						buf.append("*");
					else
						buf.append(" ");
					buf.append(Util.padRight((j+1)+"",3)+") "+Util.padRight(from,15)+" "+Util.padRight(to,15)+" "+subject+"\n\r");
				}
			}
		}
		else
		{
			Vector entry=(Vector)journal.elementAt(which);
			String from=(String)entry.elementAt(0);
			String date=(String)entry.elementAt(1);
			String to=(String)entry.elementAt(2);
			String subject=(String)entry.elementAt(3);
			String message=(String)entry.elementAt(4);
			if(to.equals("ALL")||to.equalsIgnoreCase(username))
				buf.append(Util.padRight((which+1)+"",3)+")\n\r"+"FROM: "+Util.padRight(from,15)+"\n\rTO  : "+Util.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
		}
		return buf;
	}

}
