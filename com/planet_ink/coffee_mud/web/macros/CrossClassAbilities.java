package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class CrossClassAbilities extends StdWebMacro
{
	public String name()	{return "CrossClassAbilities";}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Vector rowsFavoring=new Vector();
		Vector allOtherRows=new Vector();
		String sort=(String)httpReq.getRequestParameters().get("SORTBY");
		int sortByClassNum=-1;
		if((sort!=null)&&(sort.length()>0))
			for(int c=0;c<CMClass.charClasses.size();c++)
			{
				CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
				if((C.ID().equals(sort))&&(C.playerSelectable()))
					sortByClassNum=c;
			}
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			StringBuffer buf=new StringBuffer("");
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			int numFound=0;
			for(int c=0;c<CMClass.charClasses.size();c++)
			{
				CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
				if(C.playerSelectable()&&(CMAble.getQualifyingLevel(C.ID(),A.ID())>=0))
					if((++numFound)>1) break;
			}
			if(numFound>1)
			{
				buf.append("<TR><TD><B>"+A.name()+"</B></TD>");
				for(int c=0;c<CMClass.charClasses.size();c++)
				{
					CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
					if(C.playerSelectable())
					{
						int qual=CMAble.getQualifyingLevel(C.ID(),A.ID());
						if(qual>=0)
						{
							buf.append("<TD>"+qual+"</TD>");
							if((c==sortByClassNum)&&(!rowsFavoring.contains(buf)))
								rowsFavoring.addElement(buf);
						}
						else
							buf.append("<TD><BR></TD>");
					}
				}
				if(!rowsFavoring.contains(buf))
					allOtherRows.addElement(buf);
				buf.append("</TR>");
			}
		}
		StringBuffer buf=new StringBuffer("");
		for(int i=0;i<rowsFavoring.size();i++)
			buf.append((StringBuffer)rowsFavoring.elementAt(i));
		for(int i=0;i<allOtherRows.size();i++)
			buf.append((StringBuffer)allOtherRows.elementAt(i));
		return buf.toString();
	}

}