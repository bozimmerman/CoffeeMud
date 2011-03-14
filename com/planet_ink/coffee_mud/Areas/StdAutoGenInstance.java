package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.GenShopkeeper;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

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
public class StdAutoGenInstance extends StdArea implements AutoGenArea
{
	public String ID(){	return "StdAutoGenInstance";}
	
	private long flags=Area.FLAG_INSTANCE_PARENT;
	public long flags(){return flags;}
	
	protected SVector<AreaInstanceChild> instanceChildren = new SVector<AreaInstanceChild>();
	protected volatile int instanceCounter=0;
	protected long childCheckDown=CMProps.getMillisPerMudHour()/CMProps.getTickMillis();
	protected WeakReference<Area> parentArea = null;
	protected String levelFormula = "(@x1-3)?(@x1+3)";
	protected String sizeFormula = "50";
	protected String aggroFormula = "@x1";
	protected String filePath = "/resources/examples/randomdata.xml";
	protected List<String> typeIds = new Vector<String>();
	protected List<String> themeIds = new Vector<String>();
	protected Map<String, String> varMap = new Hashtable<String, String>(1);
	
	protected String getStrippedRoomID(String roomID)
	{
		int x=roomID.indexOf('#');
		if(x<0) return null;
		return roomID.substring(x);
	}
	
	protected String convertToMyArea(String roomID)
	{
		String strippedID=getStrippedRoomID(roomID);
		if(strippedID==null) return null;
		return Name()+strippedID;
	}
	
	protected Area getParentArea()
	{
		if((parentArea!=null)&&(parentArea.get()!=null))
			return parentArea.get();
		int x=Name().indexOf('_');
		if(x<0) return null;
		if(!CMath.isNumber(Name().substring(0,x))) return null;
		Area parentA = CMLib.map().getArea(Name().substring(x+1));
		if((parentA==null)
		||(!CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_PARENT))
		||(CMath.bset(parentA.flags(),Area.FLAG_INSTANCE_CHILD)))
			return null;
		parentArea=new WeakReference<Area>(parentA);
		return parentA;
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
		if((--childCheckDown)<=0)
		{
			childCheckDown=CMProps.getMillisPerMudHour()/CMProps.getTickMillis();
	    	synchronized(instanceChildren)
	    	{
        		for(int i=instanceChildren.size()-1;i>=0;i--) 
        		{
        			Area childA=instanceChildren.elementAt(i).A;
        			if(childA.getAreaState() > Area.STATE_ACTIVE)
        			{
        				List<WeakReference<MOB>> V=instanceChildren.elementAt(i).mobs;
        				boolean anyInside=false;
        				for(WeakReference<MOB> wmob : V)
        				{
        					MOB M=wmob.get();
        					if((M!=null)
        					&&CMLib.flags().isInTheGame(M,true)
        					&&(M.location()!=null)
        					&&(M.location().getArea()==childA))
        					{
        						anyInside=true;
        						break;
        					}
        				}
        				if(!anyInside)
        				{
        					instanceChildren.remove(i);
            				for(WeakReference<MOB> wmob : V)
            				{
            					MOB M=wmob.get();
            					if((M!=null)
            					&&(M.location()!=null)
            					&&(M.location().getArea()==this))
            						M.setLocation(M.getStartRoom());
            				}
        					MOB mob=CMClass.sampleMOB();
        					for(Enumeration<Room> e=childA.getProperMap();e.hasMoreElements();)
        					{
        						Room R=e.nextElement();
        				    	R.executeMsg(mob,CMClass.getMsg(mob,R,null,CMMsg.MSG_EXPIRE,null));
        					}
        					CMLib.map().delArea(childA);
        					childA.destroy();
        				}
        			}
        		}
	    	}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(CMath.bset(flags(),Area.FLAG_INSTANCE_CHILD))
			return true;
    	setAreaState(Area.STATE_PASSIVE);
        if((msg.sourceMinor()==CMMsg.TYP_ENTER)
        &&(msg.target() instanceof Room)
        &&(CMath.bset(flags(),Area.FLAG_INSTANCE_PARENT))
        &&(isRoom((Room)msg.target()))
        &&(((msg.source().getStartRoom()==null)||(msg.source().getStartRoom().getArea()!=this))))
        {
        	synchronized(instanceChildren)
        	{
        		int myDex=-1;
        		for(int i=0;i<instanceChildren.size();i++) 
        		{
        			List<WeakReference<MOB>> V=instanceChildren.elementAt(i).mobs;
        			for(Iterator<WeakReference<MOB>> vi = V.iterator();vi.hasNext();)
        			if(msg.source() == vi.next().get())
        			{  
        				myDex=i; break;
        			}
        		}
        		Set<MOB> grp = msg.source().getGroupMembers(new HashSet<MOB>());
        		for(int i=0;i<instanceChildren.size();i++) {
        			if(i!=myDex)
        			{
        				List<WeakReference<MOB>> V=instanceChildren.elementAt(i).mobs;
	        			for(int v=V.size()-1;v>=0;v--)
	        			{
	        				final WeakReference<MOB> wmob=V.get(v);
	        				if(wmob==null) continue;
	        				final MOB M=wmob.get();
	        				if(grp.contains(M))
		        			{
		        				if(myDex<0)
		        				{
		        					myDex=i;
		        					break;
		        				}
		        				else
		        				if((CMLib.flags().isInTheGame(M,true))
	        					&&(M.location().getArea()!=instanceChildren.elementAt(i).A))
		        				{
		        					V.remove(M);
		        					instanceChildren.get(myDex).mobs.add(new WeakReference<MOB>(M));
		        				}
		        			}
	        			}
        			}
        		}
        		Area redirectA = null;
    			int direction = CMLib.map().getRoomDir(msg.source().location(), (Room)msg.target());
    			if((direction<0)&&(msg.tool() instanceof Exit)) 
    				direction = CMLib.map().getExitDir(msg.source().location(), (Exit)msg.target());
    			if(direction < 0)
    			{
    				msg.source().tell("Can't figure out where you're coming from?!");
    				return false;
    			}
        		if(myDex<0)
        		{
        			final StdAutoGenInstance newA=(StdAutoGenInstance)this.copyOf();
        			newA.properRooms=new STreeMap<String, Room>(new Area.RoomIDComparator());
        			newA.properRoomIDSet = null;
        			newA.metroRoomIDSet = null;
        		    newA.blurbFlags=new STreeMap<String,String>();
        			newA.setName((++instanceCounter)+"_"+Name());
        			newA.flags |= Area.FLAG_INSTANCE_CHILD;
        			Set<MOB> myGroup=msg.source().getGroupMembers(new HashSet<MOB>());
        			StringBuffer xml = Resources.getFileResource(getGeneratorXmlPath(), true);
        			if((xml==null)||(xml.length()==0))
        			{
        				msg.source().tell("Unable to load this area.  Please try again later.");
        				return false;
        			}
        	        List<XMLLibrary.XMLpiece> xmlRoot = CMLib.xml().parseAllXML(xml);
        	        Hashtable definedIDs = new Hashtable();
        	        CMLib.percolator().buildDefinedIDSet(xmlRoot,definedIDs);
        	        String idName = "";
        	        List<String> idChoices = getXmlAreaTypeIds();
        	        if(idChoices.size()==0)
        	        {
        	        	idChoices = new Vector<String>();
	        	        for(Object key : definedIDs.keySet())
	        	        {
	        	        	Object val=definedIDs.get(key);
	        	        	if((key instanceof String)
	        	        	&&(val instanceof XMLLibrary.XMLpiece)
	        	        	&&(((XMLLibrary.XMLpiece)val).tag.equalsIgnoreCase("area")))
	        	        		idChoices.add((String)key);
	        	        }
        	        }
        	        if(idChoices.size()>0)
        	        	idName=idChoices.get(CMLib.dice().roll(1, idChoices.size(), -1)).toUpperCase().trim();
        	        	
        	        if((!(definedIDs.get(idName) instanceof XMLLibrary.XMLpiece))
        	        ||(!((XMLLibrary.XMLpiece)definedIDs.get(idName)).tag.equalsIgnoreCase("area")))
        	        {
        	        	msg.source().tell("The area id '"+idName+"' has not been defined in the data file.");
        	        	return false;
        	        }
        	        XMLLibrary.XMLpiece piece=(XMLLibrary.XMLpiece)definedIDs.get(idName);
        	        definedIDs.putAll(getOtherAutoGenVars());
        	        definedIDs.put("AREANAME", Name());
        	        String sizeFormula=CMStrings.replaceAll(this.getAreaSizeFormula(), "@x1", ""+msg.source().basePhyStats().level());
        	        definedIDs.put("AREASIZE", sizeFormula);
        	        String levelFormula=CMStrings.replaceAll(this.getAreaLevelFormula(), "@x1", ""+msg.source().basePhyStats().level());
        	        definedIDs.put("LEVEL_RANGE", levelFormula);
        	        String aggroFormula=CMStrings.replaceAll(this.getAggroFormula(), "@x1", ""+msg.source().basePhyStats().level());
        	        definedIDs.put("AGGROCHANCE", aggroFormula);
        	        if(getXmlThemeIds().size()>0)
            	        definedIDs.put("THEME", getXmlThemeIds().get(CMLib.dice().roll(1, getXmlThemeIds().size(), -1)).toUpperCase().trim());
        	        else
        	        {
        	            Map<String,String> unfilled = CMLib.percolator().getUnfilledRequirements(definedIDs,piece);
        	            List<String> themes = CMParms.parseCommas(unfilled.get("THEME"), true);
        	            if(themes.size()>0)
                	        definedIDs.put("THEME", themes.get(CMLib.dice().roll(1, themes.size(), -1)).toUpperCase().trim());
        	        }
        	        try 
        	        {
        	        	CMLib.percolator().checkRequirements(piece, definedIDs);
        	        } 
        	        catch(CMException cme) 
        	        {
        	        	msg.source().tell("Required ids for "+idName+" were missing: "+cme.getMessage());
        	        	return false;
        	        }
        			for(MOB M : myGroup)
        				M.tell("^x------------------------------------------------------\n\r" +
        						 "Preparing to enter "+Name()+", please stand by...\n\r" +
        						 "------------------------------------------------------^N^.");
        	        try
        	        {
	    	    		if(!CMLib.percolator().fillInArea(piece, definedIDs, newA, direction))
	    	    		{
	        				msg.source().tell("Failed to enter the new area.  Try again later.");
	        				return false;
	    	    		}
        	        }
        	        catch(CMException cme)
        	        {
        	        	Log.errOut("StdAutoGenInstance","Unable to build area: "+cme.getMessage());
        				msg.source().tell("Failed to finish entering the new area.  Try again later.");
        				return false;
        	        }
        			redirectA=newA;
        			CMLib.map().addArea(newA);
        			newA.setAreaState(Area.STATE_ACTIVE); // starts ticking
        			final List<WeakReference<MOB>> newMobList = new SVector<WeakReference<MOB>>(5);
        			newMobList.add(new WeakReference<MOB>(msg.source()));
        			final AreaInstanceChild child = new AreaInstanceChild(redirectA,newMobList); 
        			instanceChildren.add(child);
        			
	        		Room R=redirectA.getRoom(redirectA.Name()+"#0");
	        		if(R!=null) 
        			{
	        	    	Exit E=R.getExitInDir(Directions.getOpDirectionCode(direction));
	        	    	if(E==null) E = CMClass.getExit("Open");
	        			int opDir=Directions.getOpDirectionCode(direction);
	        			if(R.getRoomInDir(opDir)!=null)
	        				msg.source().tell("An error has caused the following exit to be one-way.");
	        			else
	        			{
	        				R.setRawExit(opDir, E);
	        				R.rawDoors()[opDir]=msg.source().location();
	        			}
        			}
        		}
        		else
        			redirectA=instanceChildren.get(myDex).A;
        		if(redirectA instanceof StdAutoGenInstance)
        		{
	        		Room R=redirectA.getRoom(redirectA.Name()+"#0");
	        		if(R!=null) 
        			{
        				msg.setTarget(R);
        			}
        		}
        	}
        }
        return true;
	}
	
	private final static String[] MYCODES={"GENERATIONFILEPATH","SIZEFORMULA","AREATYPES","LEVELFORMULA","OTHERVARS","AREATHEMES","AGGROFORMULA"};
	public String getStat(String code)
	{
		if(CMParms.indexOfIgnoreCase(STDAREACODES, code)>=0)
			return super.getStat(code);
		else
		switch(getCodeNum(code))
		{
		case 0: return this.getGeneratorXmlPath();
		case 1: return this.getAreaSizeFormula();
		case 2: return CMParms.toStringList(this.getXmlAreaTypeIds());
		case 3: return this.getAreaLevelFormula();
		case 4: return CMParms.toStringEqList(this.getOtherAutoGenVars());
		case 5: return CMParms.toStringList(this.getXmlThemeIds());
		case 6: return this.getAggroFormula();
        default: break;
        }
		return "";
	}
	
	public void setStat(String code, String val)
	{
		if(CMParms.indexOfIgnoreCase(STDAREACODES, code)>=0)
			super.setStat(code, val);
		else
		switch(getCodeNum(code))
		{
		case 0: setGeneratorXmlPath(val); break;
		case 1: setAreaSizeFormula(val); break;
		case 2: setXmlAreaTypeIds(val); break;
		case 3: setAreaLevelFormula(val); break;
		case 4: setOtherAutoGenVars(val); break;
		case 5: setXmlThemeIds(val); break;
		case 6: setAggroFormula(val); break;
        default: break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
        String[] MYCODES=CMProps.getStatCodesList(StdAutoGenInstance.MYCODES,this);
		String[] superCodes=STDAREACODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdAutoGenInstance)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}

	public String getAreaLevelFormula() { return levelFormula;}
	public String getAreaSizeFormula() { return sizeFormula;}
	public String getGeneratorXmlPath() { return filePath;}
	public Map<String, String> getOtherAutoGenVars() { return varMap;}
	public List<String> getXmlAreaTypeIds() { return typeIds;}
	public void setAreaLevelFormula(String formula) { levelFormula = formula;}
	public void setAreaSizeFormula(String formula) { sizeFormula = formula;}
	public void setGeneratorXmlPath(String path) { filePath=path;}
	public void setXmlAreaTypeIds(List<String> list) { typeIds = list;}
	public void setOtherAutoGenVars(Map<String, String> vars) { varMap = vars;}
	public void setOtherAutoGenVars(String vars) { setOtherAutoGenVars(CMParms.parseEQParms(vars)); }
	public void setXmlAreaTypeIds(String commaList) { setXmlAreaTypeIds(CMParms.parseCommas(commaList, true)); }
	public String getAggroFormula() { return aggroFormula;}
	public List<String> getXmlThemeIds() { return themeIds;}
	public void setAggroFormula(String formula) { aggroFormula=formula;}
	public void setXmlThemeIds(List<String> list) { themeIds=list;}
	public void setXmlThemeIds(String commaList) { setXmlThemeIds(CMParms.parseCommas(commaList, true)); }
}
