package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RoomData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	private static Vector mobs=new Vector();
	private static Vector items=new Vector(); 
	
	
	public static boolean MOBSsame(MOB M, MOB M2)
	{
		if((CMClass.className(M).equals(CMClass.className(M2)))
		&&(M.baseEnvStats().level()==M2.baseEnvStats().level())
		&&(M.baseEnvStats().ability()==M2.baseEnvStats().ability())
		&&(M.name().equals(M2.name()))
		&&(M.baseEnvStats().rejuv()==M2.baseEnvStats().rejuv()))
		{
			if(!M.text().equals(M2.text()))
			{
				String buf1=M.text();
				String buf2=M2.text();
				try{
					for(int l=0, l2=0;((l!=buf1.length())&&(l2!=buf2.length()));l++,l2++)
					{
						if(buf1.charAt(l)!=buf2.charAt(l2))
							return false;
						if(buf1.charAt(l)=='@')
						{
							while(buf1.charAt(++l)!='<');
							while(buf2.charAt(++l2)!='<');
						}
					}
				} catch(Exception e){return false;}
			}
			return true;
		}
		else
			return false;
	}
	
	public static boolean ItemsSame(Item I, Item I2)
	{
		if((CMClass.className(I).equals(CMClass.className(I2)))
		&&(I.baseEnvStats().level()==I2.baseEnvStats().level())
		&&(I.baseEnvStats().ability()==I2.baseEnvStats().ability())
		&&(I.baseEnvStats().rejuv()==I2.baseEnvStats().rejuv())
		&&(I.usesRemaining()==I2.usesRemaining())
		&&(I.name().equals(I2.name()))
		&&(I.baseEnvStats().height()==I2.baseEnvStats().height()))
		{
			if(!I.text().equals(I2.text()))
			{
				String buf1=I.text();
				String buf2=I2.text();
				try{
					for(int l=0, l2=0;((l!=buf1.length())&&(l2!=buf2.length()));l++,l2++)
					{
						if(buf1.charAt(l)!=buf2.charAt(l2))
							return false;
						if(buf1.charAt(l)=='@')
						{
							while(buf1.charAt(++l)!='<');
							while(buf2.charAt(++l2)!='<');
						}
					}
				} catch(Exception e){return false;}
			}
			return true;
		}
		else
			return false;
	}
	
	public static Vector contributeMOBs(Vector inhabs)
	{
		for(int i=0;i<inhabs.size();i++)
		{
			boolean found=false;
			MOB M=(MOB)inhabs.elementAt(i);
			for(int m=0;m<mobs.size();m++)
			{
				MOB M2=(MOB)mobs.elementAt(m);
				if(MOBSsame(M,M2))
				{	found=true;	break;	}
			}
			if(!found)
				mobs.addElement(M.copyOf());
		}
		return mobs;
	}
	
	public static Vector contributeItems(Vector inhabs)
	{
		for(int i=0;i<inhabs.size();i++)
		{
			boolean found=false;
			Item I=(Item)inhabs.elementAt(i);
			for(int i2=0;i2<items.size();i2++)
			{
				Item I2=(Item)items.elementAt(i2);
				if(ItemsSame(I,I2))
				{	found=true;	break;	}
			}
			if(!found)
				items.addElement(I.copyOf());
		}
		return items;
	}
	
	// valid parms include help, ranges, quality, target, alignment, domain, 
	// qualifyQ, auto
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";
		if(last.length()==0) return "";
		Room R=CMMap.getRoom(last);
		
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("NAME"))
		{
			String name=(String)httpReq.getRequestParameters().get("NAME");
			if((name==null)||(name.length()==0))
				name=R.displayText();
			str.append(name);
		}
		if(parms.containsKey("CLASSES"))
		{
			String className=(String)httpReq.getRequestParameters().get("CLASS");
			if((className==null)||(className.length()==0))
				className=CMClass.className(R);
			for(int r=0;r<CMClass.locales.size();r++)
			{
				Room cnam=(Room)CMClass.locales.elementAt(r);
				str.append("<OPTION VALUE=\""+CMClass.className(cnam)+"\"");
				if(className.equalsIgnoreCase(CMClass.className(cnam)))
					str.append(" SELECTED");
				str.append(">"+CMClass.className(cnam));
			}
		}
				
		str.append(AreaData.affectsNBehaves(R,httpReq,parms));
				
		if(parms.containsKey("DESCRIPTION"))
		{
			String desc=(String)httpReq.getRequestParameters().get("DESCRIPTION");
			if((desc==null)||(desc.length()==0))
				desc=R.description();
			str.append(desc);
		}
		
		if(parms.containsKey("MOBLIST"))
		{
			Vector classes=new Vector();
			Vector moblist=null;
			if(httpReq.getRequestParameters().containsKey("MOB1"))
			{
				moblist=mobs;
				for(int i=1;;i++)
				{
					String MATCHING=(String)httpReq.getRequestParameters().get("MOB"+i);
					boolean found=false;
					if(MATCHING==null)
						break;
					else
					for(int m=0;m<moblist.size();m++)
					{
						MOB M2=(MOB)moblist.elementAt(m);
						if(MATCHING.equals(""+M2))
						{
							found=true;
							classes.addElement(M2);
							break;
						}
					}
				}
			}
			else
			{
				ExternalPlay.resetRoom(R);
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB M=R.fetchInhabitant(m);
					if(M.isEligibleMonster())
						classes.addElement(M);
				}
				moblist=contributeMOBs(classes);
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<classes.size();i++)
			{
				MOB M=(MOB)classes.elementAt(i);
				MOB TM=null;
				str.append("<TR>");
				str.append("<TD WIDTH=90%>");
				str.append("<SELECT ONCHANGE=\"DelMOB(this);\" NAME=MOB"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				for(int b=0;b<moblist.size();b++)
				{
					MOB M2=(MOB)moblist.elementAt(b);
					str.append("<OPTION VALUE=\""+M2+"\"");
					if(MOBSsame(M,M2))
					{
						TM=M2;
						str.append(" SELECTED");
					}
					str.append(">"+M2.name()+" ("+CMClass.className(M2)+")");
				}
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=EDITMOB"+i+" VALUE=EDIT ONCLICK=\"EditMOB('"+i+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD COLSPAN=2 WIDTH=90% ALIGN=RIGHT>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDMOB VALUE=ADD ONCLICK=\"AddMOB();\">");
			str.append("</TD></TR></TABLE>");
		}
		
		if(parms.containsKey("ITEMLIST"))
		{
			Vector classes=new Vector();
			Vector itemlist=null;
			if(httpReq.getRequestParameters().containsKey("ITEM1"))
			{
				itemlist=items;
				for(int i=1;;i++)
				{
					String MATCHING=(String)httpReq.getRequestParameters().get("ITEM"+i);
					boolean found=false;
					if(MATCHING==null)
						break;
					else
					for(int m=0;m<itemlist.size();m++)
					{
						Item I2=(Item)itemlist.elementAt(m);
						if(MATCHING.equals(""+I2))
						{
							found=true;
							classes.addElement(I2);
							break;
						}
					}
				}
			}
			else
			{
				ExternalPlay.resetRoom(R);
				for(int m=0;m<R.numItems();m++)
				{
					Item I=R.fetchItem(m);
					if(I.container()==null)
						classes.addElement(I);
				}
				itemlist=contributeItems(classes);
			}
			str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
			for(int i=0;i<classes.size();i++)
			{
				Item I=(Item)classes.elementAt(i);
				Item TI=null;
				str.append("<TR>");
				str.append("<TD WIDTH=90%>");
				str.append("<SELECT ONCHANGE=\"DelItem(this);\" NAME=ITEM"+(i+1)+">");
				str.append("<OPTION VALUE=\"\">Delete!");
				for(int b=0;b<itemlist.size();b++)
				{
					Item I2=(Item)itemlist.elementAt(b);
					str.append("<OPTION VALUE=\""+I2+"\"");
					if(ItemsSame(I,I2))
					{
						TI=I2;
						str.append(" SELECTED");
					}
					str.append(">"+I2.name()+" ("+CMClass.className(I2)+")");
				}
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+i+" VALUE=EDIT ONCLICK=\"EditItem('"+i+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD COLSPAN=2 WIDTH=90% ALIGN=RIGHT>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=ADD ONCLICK=\"AddItem();\">");
			str.append("</TD></TR></TABLE>");
		}
		
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return strstr;
	}
}
