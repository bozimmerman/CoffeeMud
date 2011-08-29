package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
import java.util.concurrent.TimeUnit;



/*
   Copyright 2000-2011 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class RoomData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	static final String[][] STAT_CHECKS={{"DISPLAY","NAME"},{"CLASS","CLASSES"},{"DESCRIPTION","DESCRIPTION"},{"XSIZE","XGRID"},{"YSIZE","XGRID"},{"IMAGE","IMAGE"}};
	
	public static List<MOB> getMOBCache()
	{
		List<MOB> mobSet=(List<MOB>)Resources.getResource("SYSTEM_WEB_MOB_CACHE");
		if(mobSet==null)
		{
			mobSet=new SLinkedList<MOB>();
			Resources.submitResource("SYSTEM_WEB_MOB_CACHE", mobSet);
		}
		return mobSet;
	}
	
	public static List<Item> getItemCache()
	{
		List<Item> itemSet=(List<Item>)Resources.getResource("SYSTEM_WEB_ITEM_CACHE");
		if(itemSet==null)
		{
			itemSet=new SLinkedList<Item>();
			Resources.submitResource("SYSTEM_WEB_ITEM_CACHE", itemSet);
		}
		return itemSet;
	}

	public static String getItemCode(Room R, Item I)
	{
		if(I==null) return "";
		for(int i=0;i<R.numItems();i++)
			if(R.getItem(i)==I)
				return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+i;
		return "";
	}

	public static String getItemCode(List<Item> allitems, Item I)
	{
		if(I==null) return "";
		int x=0;
		for(Iterator<Item> i=allitems.iterator(); i.hasNext();)
		{
			final Item I2=i.next();
			if(I2==I)
				return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+x;
			x++;
		}
		x=0;
		for(Iterator<Item> i=allitems.iterator(); i.hasNext();)
		{
			final Item I2=i.next();
            if(I2.sameAs(I))
                return Long.toString((I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5)+x;
			x++;
		}
		return "";
	}

	public static String getItemCode(MOB M, Item I)
	{
		if(I==null) return "";
		for(int i=0;i<M.numItems();i++)
			if(M.getItem(i)==I)
				return Long.toString( ( I.ID() + "/" + I.Name() + "/" + I.displayText() ).hashCode() << 5 ) + i;
		return "";
	}

	public static String getMOBCode(Room R, MOB M)
	{
		if(M==null) return "";
		int code=0;
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M2=R.fetchInhabitant(i);
			if(M==M2)
				return Long.toString( ( M.ID() + "/" + M.Name() + "/" + M.displayText() ).hashCode() << 5 ) + code;
			else
			if((M2!=null)&&(M2.isSavable()))
				code++;
		}
		return "";
	}

	public static String getMOBCode(List<MOB> mobs, MOB M)
	{
		if(M==null) return "";
		int i=0;
		for(Iterator<MOB> m=mobs.iterator(); m.hasNext();)
		{
			MOB M2=m.next();
			if(M2==M)
				return Long.toString( ( M.ID() + "/" + M.Name() + "/" + M.displayText() ).hashCode() << 5 ) + i;
			i++;
		}
		return "";
	}

	public static Item getItemFromCode(MOB M, String code)
	{
        if(M==null) return getItemFromCode(getItemCache(),code);
		for(int i=0;i<M.numItems();i++)
			if(getItemCode(M,M.getItem(i)).equals(code))
				return M.getItem(i);
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<M.numItems();i++)
			if(getItemCode(M,M.getItem(i)).startsWith(code))
				return M.getItem(i);
		return null;
	}

	public static Item getItemFromCode(Room R, String code)
	{
        if(R==null) return getItemFromCode(getItemCache(),code);
		for(int i=0;i<R.numItems();i++)
			if(getItemCode(R,R.getItem(i)).equals(code))
				return R.getItem(i);
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<R.numItems();i++)
			if(getItemCode(R,R.getItem(i)).startsWith(code))
				return R.getItem(i);
		return null;
	}

	public static Item getItemFromCode(List<Item> allitems, String code)
	{
        if(code.startsWith("CATALOG-"))
            return getItemFromCatalog(code);
		for(Iterator<Item> i=allitems.iterator(); i.hasNext();)
		{
			Item I=i.next();
			if(getItemCode(allitems,I).equals(code))
				return I;
		}
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(Iterator<Item> i=allitems.iterator(); i.hasNext();)
		{
			Item I=i.next();
			if(getItemCode(allitems,I).startsWith(code))
				return I;
		}
		return null;
	}

	public static MOB getMOBFromCode(Room R, String code)
	{
        if(R==null) return getMOBFromCode(getMOBCache(),code);
		for(int i=0;i<R.numInhabitants();i++)
			if(getMOBCode(R,R.fetchInhabitant(i)).equals(code))
				return R.fetchInhabitant(i);
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(int i=0;i<R.numInhabitants();i++)
			if(getMOBCode(R,R.fetchInhabitant(i)).startsWith(code))
				return R.fetchInhabitant(i);
		return null;
	}

	public static MOB getMOBFromCode(List<MOB> allmobs, String code)
	{
        if(code.startsWith("CATALOG-"))
            return getMOBFromCatalog(code);
		for(Iterator<MOB> m=allmobs.iterator(); m.hasNext();)
		{
			MOB M2=m.next();
			if(getMOBCode(allmobs,M2).equals(code))
				return M2;
		}
		if(code.length()>2) code=code.substring(0,code.length()-2);
		for(Iterator<MOB> m=allmobs.iterator(); m.hasNext();)
		{
			MOB M2=m.next();
			if(getMOBCode(allmobs,M2).startsWith(code))
				return M2;
		}
		return null;
	}


	public static MOB getMOBFromCatalog(String MATCHING)
	{
        if(!MATCHING.startsWith("CATALOG-"))
            return null;
        MOB M2=CMLib.catalog().getCatalogMob(MATCHING.substring(8));
        if(M2!=null)
        {
            M2=(MOB)M2.copyOf();
            CMLib.catalog().changeCatalogUsage(M2,true);
        }
        return M2;
	}


    public static Item getItemFromCatalog(String MATCHING)
    {
        if(!MATCHING.startsWith("CATALOG-"))
            return null;
        Item I=CMLib.catalog().getCatalogItem(MATCHING.substring(8));
        if(I!=null)
        {
            I=(Item)I.copyOf();
            CMLib.catalog().changeCatalogUsage(I,true);
        }
        return I;
    }

    public static String getAppropriateCode(Environmental E, Environmental RorM, List classes, List list)
    {
        if(((RorM instanceof Room)&&(((Room)RorM).isHere(E)))
        ||((RorM instanceof MOB)&&(((MOB)RorM).isMine(E))))
            return (E instanceof Item)?getItemCode(classes,(Item)E):getMOBCode(classes,(MOB)E);
        else
        if(list.contains(E))
            return ""+E;
        else
        if(CMLib.flags().isCataloged(E))
            return "CATALOG-"+E.Name();
        return E.ID();
    }


	public static Item getItemFromAnywhere(Object allitems, String MATCHING)
	{
		if(isAllNum(MATCHING))
		{
			if(allitems instanceof Room)
				return getItemFromCode((Room)allitems,MATCHING);
			else
			if(allitems instanceof MOB)
				return getItemFromCode((MOB)allitems,MATCHING);
			else
			if(allitems instanceof List)
				return getItemFromCode((List)allitems,MATCHING);
		}
		else
        if(MATCHING.startsWith("CATALOG-"))
            return getItemFromCatalog(MATCHING);
        else
		if(MATCHING.indexOf('@')>0)
		{
			for(Iterator<Item> i=getItemCache().iterator(); i.hasNext();)
			{
				Item I2=i.next();
				if(MATCHING.equals(""+I2))
					return I2;
			}
		}
		else
		{
			Item I=CMClass.getItem(MATCHING);
			if((I!=null)&&(!(I instanceof ArchonOnly))) return I;
		}
		return null;
	}

    public static MOB getReferenceMOB(MOB M)
    {
        if(M==null) return null;
		for(Iterator<MOB> m=getMOBCache().iterator(); m.hasNext();)
		{
			MOB M2=m.next();
            if(M.sameAs(M2)) return M2;
        }
        return null;
    }

    public static Item getReferenceItem(Item I)
    {
        if(I==null) return null;
		for(Iterator<Item> i=getItemCache().iterator(); i.hasNext();)
		{
			Item I2=i.next();
            if(I.sameAs(I2)) return I2;
        }
        return null;
    }

	public static List<MOB> contributeMOBs(List<MOB> inhabs)
	{
		for(Iterator<MOB> m=inhabs.iterator(); m.hasNext();)
		{
			MOB M=m.next();
			if(M.isGeneric())
			{
				if((getReferenceMOB(M)==null)&&(M.isSavable()))
				{
					MOB M3=(MOB)M.copyOf();
					M3.setExpirationDate(System.currentTimeMillis());
					getMOBCache().add(M3);
					for(int i3=0;i3<M3.numItems();i3++)
					{
						Item I3=M3.getItem(i3);
						if(I3!=null) I3.stopTicking();
					}
				}
			}
		}
		return getMOBCache();
	}

	public static boolean isAllNum(String str)
	{
		if(str.length()==0) return false;
		for(int c=0;c<str.length();c++)
			if((!Character.isDigit(str.charAt(c)))
			&&(str.charAt(c)!='-'))
				return false;
		return true;
	}

	public static List<Item> contributeItems(List<Item> inhabs)
	{
		for(Iterator<Item> i=inhabs.iterator(); i.hasNext();)
		{
			Item I=i.next();
			if(I.isGeneric())
			{
				boolean found=false;
				for(Iterator<Item> i2=getItemCache().iterator(); i2.hasNext();)
				{
					Item I2=i2.next();
					if(I.sameAs(I2))
					{	found=true;	break;	}
				}
				if(!found)
				{
					Item I2=(Item)I.copyOf();
                    I2.setContainer(null);
                    I2.wearAt(Wearable.IN_INVENTORY);
					I2.setExpirationDate(System.currentTimeMillis());
					getItemCache().add(I2);
					I2.stopTicking();
				}
			}
		}
		return getItemCache();
	}
	
	public static final String getObjIDSuffix(Environmental E)
	{
		if((E.expirationDate() > (System.currentTimeMillis() - TimeManager.MILI_DAY))
		&&(E.expirationDate() < System.currentTimeMillis()))
		{
			String time = CMLib.time().date2EllapsedTime(System.currentTimeMillis() - E.expirationDate(),TimeUnit.MINUTES, true);
			if(time.length()==0)
				return " (cached new)";
			else
				return " (cached "+time+")";
		}
		else
			return " ("+E.ID()+")";
	}


	public static Pair<String,String>[] makeMergableRoomFields(Room R, Vector<String> multiRoomList)
	{
		List<Pair<String,String>> fixtures=new Vector<Pair<String,String>>();
		R=(Room)R.copyOf();
		Vector<MOB> inhabs=new Vector<MOB>();
		for(Enumeration<MOB> a =R.inhabitants();a.hasMoreElements();)
		{
			final MOB M=a.nextElement();
			if(M!=null)
			{
            	CMLib.catalog().updateCatalogIntegrity(M);
				inhabs.add(M);
			}
		}
		Vector<Item> items=new Vector<Item>();
		for(Enumeration<Item> a =R.items();a.hasMoreElements();)
		{
			final Item I=a.nextElement();
			if(I!=null)
			{
            	CMLib.catalog().updateCatalogIntegrity(I);
				items.add(I);
			}
		}
		Vector<Ability> affects=new Vector<Ability>();
		for(Enumeration<Ability> a =R.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null) affects.add(A);
		}
		Vector<Behavior> behavs=new Vector<Behavior>();
		for(Enumeration<Behavior> b=R.behaviors();b.hasMoreElements();)
		{
			final Behavior B=b.nextElement();
			if(B!=null) behavs.add(B);
		}
		for(String roomID : multiRoomList)
			if(!roomID.equalsIgnoreCase(R.roomID()))
			{
				Room R2=CMLib.map().getRoom(roomID);
				if(R2!=null)
				{
					CMLib.map().resetRoom(R2);
					for(final String[] set : STAT_CHECKS)
						if(!R.getStat(set[0]).equalsIgnoreCase(R2.getStat(set[0])))
							fixtures.add(new Pair<String,String>(set[1], ""));
					for(Iterator<Ability> a=affects.iterator();a.hasNext();)
					{
						final Ability A=a.next();
						if((R2.fetchEffect(A.ID())==null)
						||(!R2.fetchEffect(A.ID()).text().equalsIgnoreCase(A.text())))
							a.remove();
					}
					for(Iterator<Behavior> b=behavs.iterator();b.hasNext();)
					{
						final Behavior B=b.next();
						if((R2.fetchBehavior(B.ID())==null)
						||(!R2.fetchBehavior(B.ID()).getParms().equalsIgnoreCase(B.getParms())))
							b.remove();
					}
					for(Iterator<MOB> m=inhabs.iterator();m.hasNext();)
					{
						MOB M=m.next();
						boolean found=false;
	                    if((M!=null)&&(M.isSavable()))
	                    {
	                		for(Enumeration<MOB> m2 =R2.inhabitants();m2.hasMoreElements();)
	                		{
	                			final MOB M2=m2.nextElement();
	                			if(M2!=null)
			                    {
			                    	CMLib.catalog().updateCatalogIntegrity(M2);
			                    	if((M2.isSavable())&&(M2.sameAs(M)))
			                    	{
			                    		found=true;
			                    		break;
			                    	}
			                    }
							}
	                    }
						if((M!=null)&&(!found))
							m.remove();
					}
					for(Iterator<Item> i=items.iterator();i.hasNext();)
					{
						final Item I=i.next();
						boolean found=false;
	                    if((I!=null)&&(I.isSavable()))
	                    {
	    					for(Enumeration<Item> i2 =R2.items();i2.hasMoreElements();)
	    					{
	    						final Item I2=i2.nextElement();
			                    if(I2!=null)
			                    {
			                    	CMLib.catalog().updateCatalogIntegrity(I2);
			                    	if((I2.isSavable())&&(I2.sameAs(I)))
			                    	{
			                    		found=true;
			                    		break;
			                    	}
			                    }
							}
	                    }
						if((I!=null)&&(!found))
							i.remove();
					}
				}
			}
		List<Item> itemCache=contributeItems(items);
		for(int i=0;i<items.size();i++)
		{
			Item I=items.get(i);
			String code=getAppropriateCode(I,R,items,itemCache);
			fixtures.add(new Pair<String,String>("ITEM"+(i+1), code));
			fixtures.add(new Pair<String,String>("ITEMWORN"+(i+1),""));
			fixtures.add(new Pair<String,String>("ITEMCONT"+(i+1),(I.container()==null)?"":""+(Object)I.container()));
		}
		if(items.size()==0)
			fixtures.add(new Pair<String,String>("ITEM1",""));
		else
			fixtures.add(new Pair<String,String>("ITEM"+(items.size()+1),null));
		List<MOB> mobCache=contributeMOBs(inhabs);
		for(int m=0;m<inhabs.size();m++)
		{
			MOB M=inhabs.get(m);
    		String code=getAppropriateCode(M,R,inhabs,mobCache);
    		fixtures.add(new Pair<String,String>("MOB"+(m+1),code));
		}
		if(inhabs.size()==0)
			fixtures.add(new Pair<String,String>("MOB1",""));
		else
			fixtures.add(new Pair<String,String>("MOB"+(inhabs.size()+1),null));
		for(int a=0;a<affects.size();a++)
		{
			final Ability A=affects.get(a);
			fixtures.add(new Pair<String,String>("AFFECT"+(a+1),A.ID()));
			fixtures.add(new Pair<String,String>("ADATA"+(a+1),A.text()));
		}
		if(affects.size()==0)
		{
			fixtures.add(new Pair<String,String>("AFFECT1",""));
			fixtures.add(new Pair<String,String>("ADATA1",""));
		}
		else
			fixtures.add(new Pair<String,String>("AFFECT"+(affects.size()+1),null));
		for(int b=0;b<behavs.size();b++)
		{
			final Behavior B=behavs.get(b);
			fixtures.add(new Pair<String,String>("BEHAV"+(b+1),B.ID()));
			fixtures.add(new Pair<String,String>("BDATA"+(b+1),B.getParms()));
		}
		if(behavs.size()==0)
		{
			fixtures.add(new Pair<String,String>("BEHAV1",""));
			fixtures.add(new Pair<String,String>("BDATA1",""));
		}
		else
			fixtures.add(new Pair<String,String>("BEHAV"+(behavs.size()+1),null));
		return fixtures.toArray(new Pair[0]);
	}
	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
		String last=httpReq.getRequestParameter("ROOM");
		if(last==null) return " @break@";
		if(last.length()==0) return "";

		if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
			return CMProps.getVar(CMProps.SYSTEM_MUDSTATUS);

		String multiFlagStr=httpReq.getRequestParameter("MULTIROOMFLAG");
		boolean multiFlag=(multiFlagStr!=null)&& multiFlagStr.equalsIgnoreCase("on");
		Vector<String> multiRoomList=CMParms.parseSemicolons(httpReq.getRequestParameter("MULTIROOMLIST"),false);
		Room R=(Room)httpReq.getRequestObjects().get(last);
		if(R==null)
		{
			R=CMLib.map().getRoom(last);
			if(R==null)
				return "No Room?!";
			CMLib.map().resetRoom(R);
			if(multiFlag 
			&&(multiRoomList.size()>1)
			&&(httpReq.getRequestParameter("MOB1")==null)
			&&(httpReq.getRequestParameter("ITEM1")==null))
			{
				Pair<String,String> pairs[]=makeMergableRoomFields(R,multiRoomList);
				if(pairs!=null)
					for(final Pair<String,String> p : pairs)
					{
						if(p.second==null)
							httpReq.removeRequestParameter(p.first);
						else
							httpReq.addRequestParameters(p.first,p.second);
					}
			}
			httpReq.getRequestObjects().put(last,R);
		}
    	synchronized(("SYNC"+R.roomID()).intern())
    	{
    		R=CMLib.map().getRoom(R);

			StringBuffer str=new StringBuffer("");
			if(parms.containsKey("NAME"))
			{
				String name=httpReq.getRequestParameter("NAME");
				if((name==null)||((name.length()==0)&&(!multiFlag)))
					name=R.displayText();
				str.append(name);
			}
			if(parms.containsKey("CLASSES"))
			{
				String className=httpReq.getRequestParameter("CLASSES");
				if((className==null)||((className.length()==0)&&(!multiFlag)))
					className=CMClass.classID(R);
				Object[] sorted=(Object[])Resources.getResource("MUDGRINDER-LOCALES");
				if(sorted==null)
				{
					Vector sortMe=new Vector();
					for(Enumeration l=CMClass.locales();l.hasMoreElements();)
						sortMe.addElement(CMClass.classID(l.nextElement()));
					sorted=(new TreeSet(sortMe)).toArray();
					Resources.submitResource("MUDGRINDER-LOCALES",sorted);
				}
				if(multiFlag)
				{
					str.append("<OPTION VALUE=\"\"");
					if(className.length()==0)
						str.append(" SELECTED");
					str.append(">&nbsp;&nbsp;");
				}
				for(int r=0;r<sorted.length;r++)
				{
					String cnam=(String)sorted[r];
					str.append("<OPTION VALUE=\""+cnam+"\"");
					if(className.equals(cnam))
						str.append(" SELECTED");
					str.append(">"+cnam);
				}
			}

			str.append(AreaData.affects(R,httpReq,parms,1));
			str.append(AreaData.behaves(R,httpReq,parms,1));
			if(parms.containsKey("IMAGE"))
			{
				String name=httpReq.getRequestParameter("IMAGE");
				if((name==null)||((name.length()==0)&&(!multiFlag)))
					name=R.rawImage();
				str.append(name);
			}
			if(parms.containsKey("DESCRIPTION"))
			{
				String desc=httpReq.getRequestParameter("DESCRIPTION");
				if((desc==null)||((desc.length()==0)&&(!multiFlag)))
					desc=R.description();
				str.append(desc);
			}
			if((parms.containsKey("XGRID"))&&(R instanceof GridLocale))
			{
				String size=httpReq.getRequestParameter("XGRID");
				if((size==null)||((size.length()==0)&&(!multiFlag)))
					size=((GridLocale)R).xGridSize()+"";
				str.append(size);
			}
			if((parms.containsKey("YGRID"))&&(R instanceof GridLocale))
			{
				String size=httpReq.getRequestParameter("YGRID");
				if((size==null)||((size.length()==0)&&(!multiFlag)))
					size=((GridLocale)R).yGridSize()+"";
				str.append(size);
			}
			if(parms.containsKey("ISGRID"))
			{
				if(R instanceof GridLocale)
					return "true";
				return "false";
			}
			if(parms.containsKey("MOBLIST"))
			{
				Vector classes=new Vector();
				List moblist=null;
				if(httpReq.isRequestParameter("MOB1"))
				{
					moblist=getMOBCache();
					for(int i=1;;i++)
					{
						String MATCHING=httpReq.getRequestParameter("MOB"+i);
						if(MATCHING==null)
							break;
						else
						if(isAllNum(MATCHING))
						{
							MOB M2=getMOBFromCode(R,MATCHING);
							if(M2!=null)
								classes.addElement(M2);
						}
						else
						if(MATCHING.startsWith("CATALOG-"))
						{
					        MOB M=CMLib.catalog().getCatalogMob(MATCHING.substring(8));
					        if(M!=null)
					        {
					            M=(MOB)M.copyOf();
					            CMLib.catalog().changeCatalogUsage(M,true);
                                classes.addElement(M);
					        }
						}
						else
						if(MATCHING.indexOf('@')>0)
						{
							for(Iterator<MOB> m=moblist.iterator(); m.hasNext();)
							{
								MOB M2=m.next();
								if(MATCHING.equals(""+M2))
								{	classes.addElement(M2);	break;	}
							}
						}
						else
						for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
						{
							MOB M2=(MOB)m.nextElement();
							if(CMClass.classID(M2).equals(MATCHING)
							   &&(!M2.isGeneric()))
							{	classes.addElement(M2.copyOf()); break;	}
						}
					}
				}
				else
				{
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
	                    if(M!=null)
	                    {
	                    	CMLib.catalog().updateCatalogIntegrity(M);
							if(M.isSavable())
								classes.addElement(M);
	                    }
					}
					moblist=contributeMOBs(classes);
				}
				str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
				for(int i=0;i<classes.size();i++)
				{
					MOB M=(MOB)classes.elementAt(i);
					str.append("<TR>");
					str.append("<TD WIDTH=90%>");
					str.append("<SELECT ONCHANGE=\"DelMOB(this);\" NAME=MOB"+(i+1)+">");
					str.append("<OPTION VALUE=\"\">Delete!");
					String code=getAppropriateCode(M,R,classes,moblist);
					str.append("<OPTION SELECTED VALUE=\""+code+"\">"+M.Name()+" ("+M.ID()+")");
					str.append("</SELECT>");
					str.append("</TD>");
					str.append("<TD WIDTH=10%>");
                    if(!CMLib.flags().isCataloged(M))
    					str.append("<INPUT TYPE=BUTTON NAME=EDITMOB"+(i+1)+" VALUE=EDIT ONCLICK=\"EditMOB('"+getMOBCode(classes,M)+"');\">");
					str.append("</TD></TR>");
				}
				str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
				str.append("<SELECT ONCHANGE=\"AddMOB(this);\" NAME=MOB"+(classes.size()+1)+">");
				str.append("<OPTION SELECTED VALUE=\"\">Select a new MOB");
				for(Iterator<MOB> m=moblist.iterator(); m.hasNext();)
				{
					MOB M=m.next();
					str.append("<OPTION VALUE=\""+M+"\">"+M.Name()+getObjIDSuffix(M));
				}
				StringBuffer mlist=(StringBuffer)Resources.getResource("MUDGRINDER-MOBLIST");
				if(mlist==null)
				{
					mlist=new StringBuffer("");
					for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
					{
						MOB M=(MOB)m.nextElement();
						if(!M.isGeneric())
							mlist.append("<OPTION VALUE=\""+M.ID()+"\">"+M.Name()+" ("+M.ID()+")");
					}
					Resources.submitResource("MUDGRINDER-MOBLIST",mlist);
				}
                str.append(mlist);
				str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
				String[] names=CMLib.catalog().getCatalogMobNames();
				for(int m=0;m<names.length;m++)
				    str.append("<OPTION VALUE=\"CATALOG-"+names[m]+"\">"+names[m]);
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=ADDMOB VALUE=\"NEW\" ONCLICK=\"AddNewMOB();\">");
				str.append("</TD></TR></TABLE>");
			}

			if(parms.containsKey("ITEMLIST"))
			{
				Vector classes=new Vector();
				Vector containers=new Vector();
				Vector beingWorn=new Vector();
				List<Item> itemlist=null;
				if(httpReq.isRequestParameter("ITEM1"))
				{
					itemlist=getItemCache();
					Vector cstrings=new Vector();
					for(int i=1;;i++)
					{
						String MATCHING=httpReq.getRequestParameter("ITEM"+i);
                        String WORN=httpReq.getRequestParameter("ITEMWORN"+i);
						if(MATCHING==null) break;
						Item I2=getItemFromAnywhere(R,MATCHING);
						if(I2!=null)
						{
							classes.addElement(I2);
                            beingWorn.addElement(Boolean.valueOf((WORN!=null)&&(WORN.equalsIgnoreCase("on"))));
                            String CONTAINER=httpReq.getRequestParameter("ITEMCONT"+i);
                            cstrings.addElement((CONTAINER==null)?"":CONTAINER);
                        }
                    }
                    for(int i=0;i<cstrings.size();i++)
                    {
                        String CONTAINER=(String)cstrings.elementAt(i);
                        Item C2=null;
                        if(CONTAINER.length()>0)
                            C2=(Item)CMLib.english().fetchEnvironmental(classes,CONTAINER,true);
                        containers.addElement((C2!=null)?(Object)C2:"");
                    }
				}
				else
				{
					for(int m=0;m<R.numItems();m++)
					{
						Item I2=R.getItem(m);
                        if(I2!=null)
                        {
                        	CMLib.catalog().updateCatalogIntegrity(I2);
    						classes.addElement(I2);
    						containers.addElement((I2.container()==null)?"":(Object)I2.container());
    						beingWorn.addElement(Boolean.valueOf(!I2.amWearingAt(Wearable.IN_INVENTORY)));
                        }
					}
					itemlist=contributeItems(classes);
				}
				str.append("<TABLE WIDTH=100% BORDER=1 CELLSPACING=0 CELLPADDING=0>");
				for(int i=0;i<classes.size();i++)
				{
					Item I=(Item)classes.elementAt(i);
					Item C=(classes.contains(containers.elementAt(i))?(Item)containers.elementAt(i):null);
					//Boolean W=(Boolean)beingWorn.elementAt(i);
					str.append("<TR>");
					str.append("<TD WIDTH=90%>");
					str.append("<SELECT ONCHANGE=\"DelItem(this);\" NAME=ITEM"+(i+1)+">");
					str.append("<OPTION VALUE=\"\">Delete!");
					String code=getAppropriateCode(I,R,classes,itemlist);
					str.append("<OPTION SELECTED VALUE=\""+code+"\">"+I.Name()+" ("+I.ID()+")");
					str.append("</SELECT><BR>");
					str.append("<FONT COLOR=WHITE SIZE=-1>");
					str.append("Container: ");
					str.append("<SELECT NAME=ITEMCONT"+(i+1)+">");
                    str.append("<OPTION VALUE=\"\" "+((C==null)?"SELECTED":"")+">On the ground");
	                for(int i2=0;i2<classes.size();i2++)
	                    if((classes.elementAt(i2) instanceof Container)&&(i2!=i))
    	                {
    	                    Container C2=(Container)classes.elementAt(i2);
    	                    String name=CMLib.english().getContextName(classes,C2);
    	                    str.append("<OPTION "+((C2==C)?"SELECTED":"")+" VALUE=\""+name+"\">"+name+" ("+C2.ID()+")");
    	                }
                    str.append("</SELECT>&nbsp;&nbsp;");
					//str.append("<INPUT TYPE=CHECKBOX NAME=ITEMWORN"+(i+1)+" "+(W.booleanValue()?"CHECKED":"")+">Worn/Wielded");
					str.append("</FONT></TD>");
					str.append("<TD WIDTH=10%>");
					if(!CMLib.flags().isCataloged(I))
					    str.append("<INPUT TYPE=BUTTON NAME=EDITITEM"+(i+1)+" VALUE=EDIT ONCLICK=\"EditItem('"+getItemCode(classes,I)+"');\">");
					str.append("</TD></TR>");
				}
				str.append("<TR><TD WIDTH=90% ALIGN=CENTER>");
				str.append("<SELECT ONCHANGE=\"AddItem(this);\" NAME=ITEM"+(classes.size()+1)+">");
				str.append("<OPTION SELECTED VALUE=\"\">Select a new Item");
				for(Iterator<Item> i=itemlist.iterator();i.hasNext();)
				{
					Item I=i.next();
					str.append("<OPTION VALUE=\""+I+"\">"+I.Name()+getObjIDSuffix(I));
				}
				StringBuffer ilist=(StringBuffer)Resources.getResource("MUDGRINDER-ITEMLIST");
				if(ilist==null)
				{
					ilist=new StringBuffer("");
					Vector sortMe=new Vector();
					CMClass.addAllItemClassNames(sortMe,true,true,false);
					Object[] sorted=(new TreeSet(sortMe)).toArray();
					for(int i=0;i<sorted.length;i++)
						ilist.append("<OPTION VALUE=\""+(String)sorted[i]+"\">"+(String)sorted[i]);
					Resources.submitResource("MUDGRINDER-ITEMLIST",ilist);
				}
                str.append(ilist);
                str.append("<OPTION VALUE=\"\">------ CATALOGED -------");
                String[] names=CMLib.catalog().getCatalogItemNames();
                for(int i=0;i<names.length;i++)
                    str.append("<OPTION VALUE=\"CATALOG-"+names[i]+"\">"+names[i]);
				str.append("</SELECT>");
				str.append("</TD>");
				str.append("<TD WIDTH=10%>");
				str.append("<INPUT TYPE=BUTTON NAME=ADDITEM VALUE=\"NEW\" ONCLICK=\"AddNewItem();\">");
				str.append("</TD></TR></TABLE>");
			}

			String strstr=str.toString();
			if(strstr.endsWith(", "))
				strstr=strstr.substring(0,strstr.length()-2);
	        return clearWebMacros(strstr);
    	}
	}
}
