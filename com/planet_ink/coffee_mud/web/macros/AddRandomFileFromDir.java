package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;



public class AddRandomFileFromDir extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if((parms==null)||(parms.size()==0)) return "";
		StringBuffer buf=new StringBuffer("");
		Vector fileList=new Vector();
		for(Enumeration e=parms.elements();e.hasMoreElements();)
		{
			String filePath=(String)e.nextElement();
			File directory=new File(filePath);
			if((!filePath.endsWith(""+File.separatorChar))&&(!filePath.endsWith("/")))
				filePath+="/";
			if((directory!=null)&&(directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(int l=0;l<list.length;l++)
					fileList.addElement(filePath+list[l]);
			}
		}
		if(fileList.size()==0) return buf.toString();
		buf.append(httpReq.getPageContent((String)fileList.elementAt(Dice.roll(1,fileList.size(),-1))));
		return buf.toString();
	}
}
