package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class ControlPanel extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String query=(String)parms.get("QUERY");
		if((query==null)||(query.length()==0))
			return "";
		if(query.equalsIgnoreCase("DISABLE"))
		{
			String field=(String)parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			if(CMSecurity.isDisabled(field))
				return " CHECKED ";
			return "";
		}
		else
		if(query.equalsIgnoreCase("CHANGEDISABLE"))
		{
			String field=(String)parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			String value=(String)parms.get("VALUE");
			CMSecurity.setDisableVar(field,((value!=null)&&(value.equalsIgnoreCase("on"))));
			return "";
		}
		else
		if(query.equalsIgnoreCase("QUERY"))
		{
			String field=(String)parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			if(field.equalsIgnoreCase("DATABASE"))
				return "Database Status: "+CMClass.DBEngine().errorStatus();
			return "";
		}
		else
		if(query.equalsIgnoreCase("RESET"))
		{
			String field=(String)parms.get("FIELD");
			if((field==null)||(field.length()==0))
				return "";
			if(field.equalsIgnoreCase("DATABASE"))
			{
				CMClass.DBEngine().resetconnections();
				return "Database successfully reset";
			}
			else
			if(field.equalsIgnoreCase("SAVETHREAD"))
			{
				
			}
			return "";
		}
		return "";
	}
}
