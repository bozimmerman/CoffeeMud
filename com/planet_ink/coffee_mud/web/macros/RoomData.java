package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class RoomData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public static Vector mobs=new Vector();
	public static Vector items=new Vector();

	public static int getItemCardinality(Room R, Item I)
	{
		for(int i=0;i<R.numItems();i++)
		{
			Item Ir=R.fetchItem(i);
			
			if(Ir==I) return i;
		}
		return -1;
	}
	
	public static int getItemCardinality(MOB M, Item I)
	{
		for(int i=0;i<M.inventorySize();i++)
		{
			Item Im=M.fetchInventory(i);
			if(Im==I) return i;
		}
		return -1;
	}
	
	public static int getMOBCardinality(Room R, MOB M)
	{
		int card=0;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M2=R.fetchInhabitant(i);
			if(M2==M) return card;
			if(M2.isEligibleMonster())
				card++;
		}
		return -1;
	}
	
	public static Item getItemFromAnywhere(Object allitems, String MATCHING)
	{
		if(Util.s_int(MATCHING)>0)
		{
			Item I2=null;
			if(allitems instanceof Room)
				return ((Room)allitems).fetchItem(Util.s_int(MATCHING)-1);
			else
			if(allitems instanceof MOB)
				return ((MOB)allitems).fetchInventory(Util.s_int(MATCHING)-1);
			else
			if((allitems instanceof Vector)
			&&((Util.s_int(MATCHING)-1)<((Vector)allitems).size()))
				return ((Item)((Vector)allitems).elementAt(Util.s_int(MATCHING)-1));
		}
		else
		if(MATCHING.indexOf("@")>0)
		{
			for(int m=0;m<items.size();m++)
			{
				Item I2=(Item)items.elementAt(m);
				if(MATCHING.equals(""+I2))
					return I2;
			}
		}
		else
		{
			for(int m=0;m<CMClass.items.size();m++)
			{
				Item I=(Item)CMClass.items.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
			for(int m=0;m<CMClass.armor.size();m++)
			{
				Item I=(Item)CMClass.armor.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
			for(int m=0;m<CMClass.weapons.size();m++)
			{
				Item I=(Item)CMClass.weapons.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
			for(int m=0;m<CMClass.miscMagic.size();m++)
			{
				Item I=(Item)CMClass.miscMagic.elementAt(m);
				if(CMClass.className(I).equals(MATCHING))
					return I;
			}
		}
		return null;
	}
	
	public static MOB getMOBAtCardinality(Room R, int here)
	{
		int card=0;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M2=R.fetchInhabitant(i);
			if(M2.isEligibleMonster())
			if(card==here)
				return M2;
			else
				card++;
		}
		return null;
	}
	
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
			boolean returning=true;
			I=(Item)I.copyOf();
			I.wearAt(Item.INVENTORY);
			I.setContainer(null);
			if(!I.text().equals(I2.text()))
			{
				String buf1=I.text();
				String buf2=I2.text();
				try{
					for(int l=0, l2=0;((l!=buf1.length())&&(l2!=buf2.length()));l++,l2++)
					{
						if(buf1.charAt(l)!=buf2.charAt(l2))
						{ returning=false; break;}
						if(buf1.charAt(l)=='@')
						{
							while(buf1.charAt(++l)!='<');
							while(buf2.charAt(++l2)!='<');
						}
					}
				} catch(Exception e){returning=false;}
			}
			return returning;
		}
		else
			return false;
	}
	
	public static Vector contributeMOBs(Vector inhabs)
	{
		for(int i=0;i<inhabs.size();i++)
		{
			MOB M=(MOB)inhabs.elementAt(i);
			if(M.isGeneric())
			{
				boolean found=false;
				for(int m=0;m<mobs.size();m++)
				{
					MOB M2=(MOB)mobs.elementAt(m);
					if(MOBSsame(M,M2))
					{	found=true;	break;	}
				}
				if(!found)
					mobs.addElement((MOB)M.copyOf());
			}
		}
		return mobs;
	}
	
	public static Vector contributeItems(Vector inhabs)
	{
		for(int i=0;i<inhabs.size();i++)
		{
			Item I=(Item)inhabs.elementAt(i);
			if(I.isGeneric())
			{
				boolean found=false;
				for(int i2=0;i2<items.size();i2++)
				{
					Item I2=(Item)items.elementAt(i2);
					if(ItemsSame(I,I2))
					{	found=true;	break;	}
				}
				if(!found)
				{
					Item I2=(Item)I.copyOf();
					I.setContainer(null);
					I.wearAt(Item.INVENTORY);
					items.addElement(I.copyOf());
				}
			}
		}
		return items;
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=(String)httpReq.getRequestParameters().get("ROOM");
		if(last==null) return " @break@";
		if(last.length()==0) return "";
		Room R=null;
		for(int i=0;i<httpReq.cache().size();i++)
		{
			Object O=httpReq.cache().elementAt(i);
			if((O instanceof Room)&&(((Room)O).ID().equals(last)))
				R=(Room)O;
		}
		if(R==null)
		{
			R=CMMap.getRoom(last);
			if(R==null)
				return "No Room?!";
			ExternalPlay.resetRoom(R);
			httpReq.cache().addElement(R);
		}
		
		StringBuffer str=new StringBuffer("");
		if(parms.containsKey("NAME"))
		{
			String name=(String)httpReq.getRequestParameters().get("NAME");
			if((name==null)||(name.length()==0))
				name=R.displayText();
			str.append(name);
		}
		boolean classChanged=(((String)httpReq.getRequestParameters().get("CLASSCHANGED")!=null)
							 &&(((String)httpReq.getRequestParameters().get("CLASSCHANGED")).equals("true")));
		if(parms.containsKey("CLASSES"))
		{
			String className=(String)httpReq.getRequestParameters().get("CLASSES");
			if((className==null)||(className.length()==0))
				className=CMClass.className(R);
			Vector sortMe=new Vector();
			for(int r=0;r<CMClass.locales.size();r++)
				sortMe.addElement(CMClass.className(CMClass.locales.elementAt(r)));
			Object[] sorted=(Object[])(new TreeSet(sortMe)).toArray();
			for(int r=0;r<sorted.length;r++)
			{
				String cnam=(String)sorted[r];
				str.append("<OPTION VALUE=\""+cnam+"\"");
				if(className.equals(cnam))
					str.append(" SELECTED");
				str.append(">"+cnam);
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
					if(MATCHING==null)
						break;
					else
					if(Util.s_int(MATCHING)>0)
					{
						MOB M2=getMOBAtCardinality(R,Util.s_int(MATCHING)-1);
						if(M2!=null)
							classes.addElement(M2);
					}
					else
					if(MATCHING.indexOf("@")>0)
					{
						for(int m=0;m<moblist.size();m++)
						{
							MOB M2=(MOB)moblist.elementAt(m);
							if(MATCHING.equals(""+M2))
							{	classes.addElement(M2);	break;	}
						}
					}
					else
					for(int m=0;m<CMClass.MOBs.size();m++)
					{
						MOB M2=(MOB)CMClass.MOBs.elementAt(m);
						if(CMClass.className(M2).equals(MATCHING)&&(!M2.isGeneric()))
						{	classes.addElement(M2.copyOf()); break;	}
					}
				}
			}
			else
			{
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
				if(R.isInhabitant(M))
					str.append("<OPTION SELECTED VALUE=\""+(getMOBCardinality(R,M)+1)+"\">"+M.name()+" ("+CMClass.className(M)+")");
				else
				if(moblist.contains(M))
					str.append("<OPTION SELECTED VALUE=\""+M+"\">"+M.name()+" ("+CMClass.className(M)+")");
				else
					str.append("<OPTION SELECTED VALUE=\""+CMClass.className(M)+"\">"+M.name()+" ("+CMClass.className(M)+")");
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=EDITMOB"+(i+1)+" VALUE=EDIT ONCLICK=\"EditMOB('"+(i+1)+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
			str.append("<SELECT ONCHANGE=\"AddMOB(this);\" NAME=MOB"+(classes.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a new MOB");
			for(int i=0;i<moblist.size();i++)
			{
				MOB M=(MOB)moblist.elementAt(i);
				str.append("<OPTION VALUE=\""+M+"\">"+M.name()+" ("+CMClass.className(M)+")");
			}
			for(int i=0;i<CMClass.MOBs.size();i++)
			{
				MOB M=(MOB)CMClass.MOBs.elementAt(i);
				if(!M.isGeneric())
				str.append("<OPTION VALUE=\""+CMClass.className(M)+"\">"+M.name()+" ("+CMClass.className(M)+")");
			}
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDMOB VALUE=\"NEW\" ONCLICK=\"AddNewMOB();\">");
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
					if(MATCHING==null)
						break;
					else
					{
						Item I2=getItemFromAnywhere(R,MATCHING);
						if(I2!=null)
							classes.addElement(I2);
					}
				}
			}
			else
			{
				for(int m=0;m<R.numItems();m++)
				{
					Item I2=R.fetchItem(m);
					classes.addElement(I2);
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
				if(R.isContent(I))
					str.append("<OPTION SELECTED VALUE=\""+(getItemCardinality(R,I)+1)+"\">"+I.name()+" ("+CMClass.className(I)+")"+((I.container()==null)?"":(" in "+I.container().name())));
				else
				if(itemlist.contains(I))
					str.append("<OPTION SELECTED VALUE=\""+I+"\">"+I.name()+" ("+CMClass.className(I)+")"+((I.container()==null)?"":(" in "+I.container().name())));
				else
					str.append("<OPTION SELECTED VALUE=\""+CMClass.className(I)+"\">"+I.name()+" ("+CMClass.className(I)+")");
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+(i+1)+"');\">");
				str.append("</TD></TR>");
			}
			str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
			str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME=ITEM"+(classes.size()+1)+">");
			str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
			for(int i=0;i<itemlist.size();i++)
			{
				Item I=(Item)itemlist.elementAt(i);
				str.append("<OPTION VALUE=\""+I+"\">"+I.name()+" ("+CMClass.className(I)+")");
			}
			Vector sortMe=new Vector();
			for(int r=0;r<CMClass.items.size();r++)
				if(!((Item)CMClass.items.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.items.elementAt(r)));
			for(int r=0;r<CMClass.weapons.size();r++)
				if(!((Item)CMClass.weapons.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.weapons.elementAt(r)));
			for(int r=0;r<CMClass.armor.size();r++)
				if(!((Item)CMClass.armor.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.armor.elementAt(r)));
			for(int r=0;r<CMClass.miscMagic.size();r++)
				if(!((Item)CMClass.miscMagic.elementAt(r)).isGeneric())
					sortMe.addElement(CMClass.className(CMClass.miscMagic.elementAt(r)));
			Object[] sorted=(Object[])(new TreeSet(sortMe)).toArray();
			for(int i=0;i<sorted.length;i++)
				str.append("<OPTION VALUE=\""+(String)sorted[i]+"\">"+(String)sorted[i]);
			str.append("</SELECT>");
			str.append("</TD>");
			str.append("<TD WIDTH=10%>");
			str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
			str.append("</TD></TR></TABLE>");
		}
		
		String strstr=str.toString();
		if(strstr.endsWith(", "))
			strstr=strstr.substring(0,strstr.length()-2);
		return strstr;
	}
}
