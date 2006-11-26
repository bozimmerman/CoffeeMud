package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import org.mozilla.javascript.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class Scriptable extends StdBehavior implements ScriptingEngine
{
	public String ID(){return "Scriptable";}
	protected int canImproveCode(){return Behavior.CAN_MOBS|Behavior.CAN_ITEMS|Behavior.CAN_ROOMS;}
	protected MOB lastToHurtMe=null;
	protected Room lastKnownLocation=null;
	protected Tickable altStatusTickable=null;
	protected Vector que=new Vector();
	protected static final Hashtable funcH=new Hashtable();
	protected static final Hashtable methH=new Hashtable();
	protected static final Hashtable progH=new Hashtable();
    private static Hashtable patterns=new Hashtable();
	protected Vector oncesDone=new Vector();
	protected Hashtable delayTargetTimes=new Hashtable();
	protected Hashtable delayProgCounters=new Hashtable();
	protected Hashtable lastTimeProgsDone=new Hashtable();
	protected Hashtable lastDayProgsDone=new Hashtable();
    private HashSet registeredSpecialEvents=new HashSet();
    private Hashtable noTrigger=new Hashtable();
	protected long tickStatus=Tickable.STATUS_NOT;
	private Quest defaultQuest=null;

	public long getTickStatus()
	{
	    Tickable T=altStatusTickable;
	    if(T!=null) return T.getTickStatus();
	    return tickStatus;
	}

    public void registerDefaultQuest(Quest Q){
    	defaultQuest=Q;
    }
    public boolean endQuest(Environmental hostObj, MOB mob, String quest)
    {
        if(mob!=null)
        {
            Vector scripts=getScripts();
            if(!mob.amDead()) lastKnownLocation=mob.location();
            for(int v=0;v<scripts.size();v++)
            {
                Vector script=(Vector)scripts.elementAt(v);
                String trigger="";
                if(script.size()>0)
                    trigger=((String)script.elementAt(0)).toUpperCase().trim();
                if((getTriggerCode(trigger)==13) //questtimeprog
                &&(!oncesDone.contains(script))
                &&(CMParms.getCleanBit(trigger,1).equalsIgnoreCase(quest)||(quest.equalsIgnoreCase("*")))
                &&(CMath.s_int(CMParms.getCleanBit(trigger,2).trim())<0))
                {
                    oncesDone.addElement(script);
                    execute(hostObj,mob,mob,mob,null,null,script,null,new Object[10]);
                    return true;
                }
            }
        }
        return false;
    }

	public Vector externalFiles()
	{
	    Vector xmlfiles=new Vector();
	    parseParmFilenames(getParms(),xmlfiles,0);
		return xmlfiles;
	}
	
    public String getVarHost(Environmental E, 
    						 String rawHost, 
    						 MOB source, 
    						 Environmental target,
                             MOB monster, 
                             Item primaryItem, 
                             Item secondaryItem, 
                             String msg,
                             Object[] tmp)
    {
        if(!rawHost.equals("*"))
        {
            if(E==null)
                rawHost=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,rawHost);
            else
            if(E instanceof Room)
                rawHost=CMLib.map().getExtendedRoomID((Room)E);
            else
                rawHost=E.Name();
        }
        return rawHost;
    }
    
    public String getVar(Environmental E, String rawHost, String var, MOB source, Environmental target,
                         MOB monster, Item primaryItem, Item secondaryItem, String msg, Object[] tmp)
    { return getVar(getVarHost(E,rawHost,source,target,monster,primaryItem,secondaryItem,msg,tmp),var); }
             
    public static String getVar(String host, String var)
    {
        Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+host);
        String val="";
        if(H!=null)
        {
            val=(String)H.get(var.toUpperCase());
            if(val==null) val="";
        }
        return val;
    }

    protected static class JScriptEvent extends ScriptableObject
    {
        public String getClassName(){ return "JScriptEvent";}
        static final long serialVersionUID=43;
        Environmental h=null;
        MOB s=null;
        Environmental t=null;
        MOB m=null;
        Item pi=null;
        Item si=null;
        Vector scr;
        String message=null;
        public Environmental host(){return h;}
        public MOB source(){return s;}
        public Environmental target(){return t;}
        public MOB monster(){return m;}
        public Item item(){return pi;}
        public Item item2(){return si;}
        public String message(){return message;}
        public void setVar(String host, String var, String value)
        {
            Scriptable.mpsetvar(host.toString(),var.toString().toUpperCase(),value.toString());
        }
        public String getVar(String host, String var)
        { return Scriptable.getVar(host,var);}
        public String toJavaString(Object O){return Context.toString(O);}
        
        public JScriptEvent(Environmental host,
                            MOB source,
                            Environmental target,
                            MOB monster,
                            Item primaryItem,
                            Item secondaryItem,
                            String msg)
        {
            h=host;
            s=source;
            t=target;
            m=monster;
            pi=primaryItem;
            si=secondaryItem;
            message=msg;
        }
    }
    
    
	public void setParms(String newParms)
	{
		newParms=CMStrings.replaceAll(newParms,"'","`");
		if(newParms.startsWith("+"))
		{
			String superParms=super.getParms();
			if(superParms.length()>100)
				Resources.removeResource("PARSEDPRG: "+superParms.substring(0,100)+superParms.length()+superParms.hashCode());
			else
				Resources.removeResource("PARSEDPRG: "+superParms);
			newParms=super.getParms()+";"+newParms.substring(1);
		}
        que=new Vector();
        oncesDone=new Vector();
        delayTargetTimes=new Hashtable();
        delayProgCounters=new Hashtable();
        lastTimeProgsDone=new Hashtable();
        lastDayProgsDone=new Hashtable();
        registeredSpecialEvents=new HashSet();
        noTrigger=new Hashtable();
		super.setParms(newParms);
		if(oncesDone.size()>0) 
			oncesDone.clear();
	}

	protected void parseParmFilenames(String parse, Vector filenames, int depth)
	{
		if(depth>10) return;  // no including off to infinity
		while(parse.length()>0)
		{
			int y=parse.toUpperCase().indexOf("LOAD=");
			if(y>=0)
			{
				int z=parse.indexOf("~",y);
				while((z>0)&&(parse.charAt(z-1)=='\\'))
				    z=parse.indexOf("~",z+1);
				if(z>0)
				{
					String filename=parse.substring(y+5,z).trim();
					parse=parse.substring(z+1);
					filenames.addElement(filename);
                    parseParmFilenames(new CMFile(Resources.makeFileResourceName(filename),null,true).text().toString(),filenames,depth+1);
				}
				else
				{
					String filename=parse.substring(y+5).trim();
					filenames.addElement(filename);
					parseParmFilenames(new CMFile(Resources.makeFileResourceName(filename),null,true).text().toString(),filenames,depth+1);
					break;
				}
			}
			else
				break;
		}
	}
	
	protected String parseLoads(String text, int depth)
	{
		StringBuffer results=new StringBuffer("");
		String parse=text;
		if(depth>10) return "";  // no including off to infinity
		while(parse.length()>0)
		{
			int y=parse.toUpperCase().indexOf("LOAD=");
			if(y>=0)
			{
				results.append(parse.substring(0,y).trim()+"\n");
				int z=parse.indexOf("~",y);
				while((z>0)&&(parse.charAt(z-1)=='\\'))
				    z=parse.indexOf("~",z+1);
				if(z>0)
				{
					String filename=parse.substring(y+5,z).trim();
					parse=parse.substring(z+1);
					results.append(parseLoads(new CMFile(Resources.makeFileResourceName(filename),null,true).text().toString(),depth+1));
				}
				else
				{
					String filename=parse.substring(y+5).trim();
					results.append(parseLoads(new CMFile(Resources.makeFileResourceName(filename),null,true).text().toString(),depth+1));
					break;
				}
			}
			else
			{
				results.append(parse);
				break;
			}
		}
		return results.toString();
	}

	protected Vector parseScripts(String text)
	{
		synchronized(funcH)
		{
			if(funcH.size()==0)
			{
				for(int i=0;i<funcs.length;i++)
					funcH.put(funcs[i],new Integer(i+1));
				for(int i=0;i<methods.length;i++)
					methH.put(methods[i],new Integer(i+1));
				for(int i=0;i<progs.length;i++)
					progH.put(progs[i],new Integer(i+1));
			}
		}
		Vector V=new Vector();
		text=parseLoads(text,0);
		int y=0;
		while((text!=null)&&(text.length()>0))
		{
			y=text.indexOf("~");
			while((y>0)&&(text.charAt(y-1)=='\\'))
			    y=text.indexOf("~",y+1);
			String script="";
			if(y<0)
			{
				script=text.trim();
				text="";
			}
			else
			{
				script=text.substring(0,y).trim();
				text=text.substring(y+1).trim();
			}
			if(script.length()>0)
				V.addElement(script);
		}
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			Vector script=new Vector();
			while(s.length()>0)
			{
				y=-1;
				int yy=0;
				while(yy<s.length())
					if((s.charAt(yy)==';')&&((yy<=0)||(s.charAt(yy-1)!='\\'))) {y=yy;break;}
					else
					if(s.charAt(yy)=='\n'){y=yy;break;}
					else
					if(s.charAt(yy)=='\r'){y=yy;break;}
					else yy++;
				String cmd="";
				if(y<0)
				{
					cmd=s.trim();
					s="";
				}
				else
				{
					cmd=s.substring(0,y).trim();
					s=s.substring(y+1).trim();
				}
				if((cmd.length()>0)&&(!cmd.startsWith("#")))
				{
				    cmd=CMStrings.replaceAll(cmd,"\\~","~");
				    cmd=CMStrings.replaceAll(cmd,"\\=","=");
					script.addElement(CMStrings.replaceAll(cmd,"\\;",";"));
				}
			}
			V.setElementAt(script,v);
		}
		V.trimToSize();
		return V;
	}

	protected Room getRoom(String thisName, Room imHere)
	{
		if(thisName.length()==0) return null;
		Room room=CMLib.map().getRoom(thisName);
		if((room!=null)&&(room.roomID().equalsIgnoreCase(thisName)))
			return room;
		Room inAreaRoom=null;
		try
		{
            for(Enumeration p=CMLib.map().players();p.hasMoreElements();)
            {
                MOB M=(MOB)p.nextElement();
                if((M.Name().equalsIgnoreCase(thisName))
                &&(M.location()!=null)
                &&(CMLib.flags().isInTheGame(M,true)))
                    inAreaRoom=M.location();
            }
            if(inAreaRoom==null)
            for(Enumeration p=CMLib.map().players();p.hasMoreElements();)
            {
                MOB M=(MOB)p.nextElement();
                if((M.name().equalsIgnoreCase(thisName))
                &&(M.location()!=null)
                &&(CMLib.flags().isInTheGame(M,true)))
                    inAreaRoom=M.location();
            }
            if(inAreaRoom==null)
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R.roomID().endsWith("#"+thisName))
				||(R.roomID().endsWith(thisName)))
				{
					if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
						inAreaRoom=R;
					else
						room=R;
				}
			}
	    }catch(NoSuchElementException nse){}
		if(inAreaRoom!=null) return inAreaRoom;
		if(room!=null) return room;
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(CMLib.english().containsString(R.displayText(),thisName))
				{
					if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
						inAreaRoom=R;
					else
						room=R;
				}
			}
	    }catch(NoSuchElementException nse){}
		if(inAreaRoom!=null) return inAreaRoom;
		if(room!=null) return room;
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(CMLib.english().containsString(R.description(),thisName))
				{
					if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
						inAreaRoom=R;
					else
						room=R;
				}
			}
	    }catch(NoSuchElementException nse){}
		if(inAreaRoom!=null) return inAreaRoom;
		if(room!=null) return room;
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((R.fetchInhabitant(thisName)!=null)
				||(R.fetchItem(null,thisName)!=null))
				{
					if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
						inAreaRoom=R;
					else
						room=R;
				}
			}
	    }catch(NoSuchElementException nse){}
		if(inAreaRoom!=null) return inAreaRoom;
		return room;
	}


	protected void scriptableError(Environmental scripted, String cmdName, String errType, String errMsg)
	{
		if(scripted!=null)
		{
			Room R=CMLib.map().roomLocation(scripted);
			Log.errOut("Scriptable",scripted.name()+"/"+CMLib.map().getExtendedRoomID(R)+"/"+ cmdName+"/"+errType+"/"+errMsg);
			if(R!=null) R.showHappens(CMMsg.MSG_OK_VISUAL,"Scriptable Error: "+scripted.name()+"/"+CMLib.map().getExtendedRoomID(R)+"/"+CMParms.toStringList(externalFiles())+"/"+ cmdName+"/"+errType+"/"+errMsg);
		}
		else
			Log.errOut("Scriptable","*/*/"+CMParms.toStringList(externalFiles())+"/"+cmdName+"/"+errType+"/"+errMsg);

	}

	protected boolean simpleEvalStr(Environmental scripted,
									String arg1,
									String arg2,
									String cmp,
									String cmdName)
	{
		int x=arg1.compareToIgnoreCase(arg2);
		if(cmp.equalsIgnoreCase("==")) return (x==0);
		else if(cmp.equalsIgnoreCase(">=")) return (x==0)||(x>0);
		else if(cmp.equalsIgnoreCase("<=")) return (x==0)||(x<0);
		else if(cmp.equalsIgnoreCase(">"))  return (x>0); 
        else if(cmp.equalsIgnoreCase("<"))  return (x<0);
		else if(cmp.equalsIgnoreCase("!=")) return (x!=0);
		else
		{
			scriptableError(scripted,cmdName,"Syntax",arg1+" "+cmp+" "+arg2);
			return false;
		}
	}


	protected boolean simpleEval(Environmental scripted, String arg1, String arg2, String cmp, String cmdName)
	{
		long val1=CMath.s_long(arg1.trim());
		long val2=CMath.s_long(arg2.trim());
		if(cmp.equalsIgnoreCase("=="))      return (val1==val2);
		else if(cmp.equalsIgnoreCase(">=")) return val1>=val2;
		else if(cmp.equalsIgnoreCase("<=")) return val1<=val2;
		else if(cmp.equalsIgnoreCase(">"))  return (val1>val2); 
        else if(cmp.equalsIgnoreCase("<"))  return (val1<val2);
		else if(cmp.equalsIgnoreCase("!=")) return (val1!=val2);
		else
		{
			scriptableError(scripted,cmdName,"Syntax",val1+" "+cmp+" "+val2);
			return false;
		}
	}

    protected boolean simpleExpressionEval(Environmental scripted, String arg1, String arg2, String cmp, String cmdName)
    {
        double val1=CMath.s_parseMathExpression(arg1.trim());
        double val2=CMath.s_parseMathExpression(arg2.trim());
        if(cmp.equalsIgnoreCase("=="))      return (val1==val2);
        else if(cmp.equalsIgnoreCase(">=")) return val1>=val2;
        else if(cmp.equalsIgnoreCase("<=")) return val1<=val2;
        else if(cmp.equalsIgnoreCase(">"))  return (val1>val2); 
        else if(cmp.equalsIgnoreCase("<"))  return (val1<val2);
        else if(cmp.equalsIgnoreCase("!=")) return (val1!=val2);
        else
        {
            scriptableError(scripted,cmdName,"Syntax",val1+" "+cmp+" "+val2);
            return false;
        }
    }
	protected Vector loadMobsFromFile(Environmental scripted, String filename)
	{
		filename=filename.trim();
		Vector monsters=(Vector)Resources.getResource("RANDOMMONSTERS-"+filename);
		if(monsters!=null) return monsters;
		StringBuffer buf=new CMFile(filename,null,true).text();
		String thangName="null";
		Room R=CMLib.map().roomLocation(scripted);
		if(R!=null)
		    thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
		else
		if(scripted!=null)
		    thangName=scripted.name();
		if((buf==null)||((buf!=null)&&(buf.length()<20)))
		{
			scriptableError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
			return null;
		}
		if(buf.substring(0,20).indexOf("<MOBS>")<0)
		{
			scriptableError(scripted,"XMLLOAD","?","Invalid XML file: '"+filename+"' in "+thangName);
			return null;
		}
		monsters=new Vector();
		String error=CMLib.coffeeMaker().addMOBsFromXML(buf.toString(),monsters,null);
		if(error.length()>0)
		{
			scriptableError(scripted,"XMLLOAD","?","Error in XML file: '"+filename+"'");
			return null;
		}
		if(monsters.size()<=0)
		{
			scriptableError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"'");
			return null;
		}
		Resources.submitResource("RANDOMMONSTERS-"+filename,monsters);
		return monsters;
	}

	protected Vector loadItemsFromFile(Environmental scripted, String filename)
	{
		filename=filename.trim();
		Vector items=(Vector)Resources.getResource("RANDOMITEMS-"+filename);
		if(items!=null) return items;
		StringBuffer buf=new CMFile(filename,null,true).text();
		String thangName="null";
		Room R=CMLib.map().roomLocation(scripted);
		if(R!=null)
		    thangName=scripted.name()+" at "+CMLib.map().getExtendedRoomID((Room)scripted);
		else
		if(scripted!=null)
		    thangName=scripted.name();
		if((buf==null)||((buf!=null)&&(buf.length()<20)))
		{
			scriptableError(scripted,"XMLLOAD","?","Unknown XML file: '"+filename+"' in "+thangName);
			return null;
		}
		if(buf.substring(0,20).indexOf("<ITEMS>")<0)
		{
			scriptableError(scripted,"XMLLOAD","?","Invalid XML file: '"+filename+"' in "+thangName);
			return null;
		}
		items=new Vector();
		String error=CMLib.coffeeMaker().addItemsFromXML(buf.toString(),items,null);
		if(error.length()>0)
		{
			scriptableError(scripted,"XMLLOAD","?","Error in XML file: '"+filename+"'");
			return null;
		}
		if(items.size()<=0)
		{
			scriptableError(scripted,"XMLLOAD","?","Empty XML file: '"+filename+"'");
			return null;
		}
		Resources.submitResource("RANDOMITEMS-"+filename,items);
		return items;
	}

	protected Environmental findSomethingCalledThis(String thisName, MOB meMOB, Room imHere, Vector OBJS, boolean mob)
	{
		if(thisName.length()==0) return null;
		Environmental thing=null;
		Environmental areaThing=null;
        ShopKeeper SK=null;
		if(thisName.toUpperCase().trim().startsWith("FROMFILE "))
		{
			try{
				Vector V=null;
				if(mob)
					V=loadMobsFromFile(null,CMParms.getCleanBit(thisName,1));
				else
					V=loadItemsFromFile(null,CMParms.getCleanBit(thisName,1));
				if(V!=null)
				{
					String name=CMParms.getPastBit(thisName,1);
					if(name.equalsIgnoreCase("ALL"))
						OBJS=V;
					else
					if(name.equalsIgnoreCase("ANY"))
					{
						if(V.size()>0)
							areaThing=(Environmental)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
					}
					else
					{
						areaThing=CMLib.english().fetchEnvironmental(V,name,true);
						if(areaThing==null)
							areaThing=CMLib.english().fetchEnvironmental(V,name,false);
					}
				}
			}
			catch(Exception e){}
		}
		else
		{
	    	if(!mob) areaThing=meMOB.fetchInventory(thisName); 
		    try
		    {
		    	if(areaThing==null)
				for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					Environmental E=null;
					if(mob)
						E=R.fetchInhabitant(thisName);
					else
					{
						E=R.fetchItem(null,thisName);
						if(E==null)
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if(M!=null)
							{
								E=M.fetchInventory(thisName);
                                SK=CMLib.coffeeShops().getShopKeeper(M);
								if((SK!=null)&&(E==null))
									E=SK.getShop().getStock(thisName,null,SK.whatIsSold(),M.getStartRoom());
							}
						}
					}
					if(E!=null)
					{
						if((imHere!=null)&&(imHere.getArea().Name().equals(R.getArea().Name())))
							areaThing=E;
						else
							thing=E;
					}
				}
		    }catch(NoSuchElementException nse){}
		}
		if(areaThing!=null)
			OBJS.addElement(areaThing);
		else
		if(thing!=null)
			OBJS.addElement(thing);
		if(OBJS.size()>0)
			return (Environmental)OBJS.firstElement();
		return null;
	}

	public Environmental getArgumentMOB(String str, 
										MOB source, 
										MOB monster, 
										Environmental target, 
										Item primaryItem, 
										Item secondaryItem, 
										String msg,
										Object[] tmp)
	{
        return getArgumentItem(str,source,monster,monster,target,primaryItem,secondaryItem,msg,tmp);
	}
	
	public Environmental getArgumentItem(String str, 
										 MOB source, 
										 MOB monster, 
										 Environmental scripted, 
										 Environmental target, 
										 Item primaryItem, 
										 Item secondaryItem, 
										 String msg,
										 Object[] tmp)
	{
		if(str.length()<2) return null;
		if(str.charAt(0)=='$')
		{
			if(Character.isDigit(str.charAt(1)))
			{
				Object O=tmp[CMath.s_int(Character.toString(str.charAt(1)))];
				if(O instanceof Environmental) 
					return (Environmental)O;
				else
				if((O instanceof Vector)&&(str.length()>3)&&(str.charAt(2)=='.'))
				{
					Vector V=(Vector)O;
					String back=str.substring(2);
	                if(back.charAt(1)=='$')
	                    back=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,back);
	    			if((back.length()>1)&&Character.isDigit(back.charAt(1)))
	    			{
	    				int x=1;
	    				while((x<back.length())&&(Character.isDigit(back.charAt(x)))) x++;
	    				int y=CMath.s_int(back.substring(1,x).trim());
	    				if((V.size()>0)&&(y>=0))
	    				{
	    					if(y>=V.size()) return null;
	    					O=V.elementAt(y);
	    					if(O instanceof Environmental) return (Environmental)O;
	    				}
						str=O.toString(); // will fall through
	    			}
				}
				else
				if(O!=null)
					str=O.toString(); // will fall through
				else
					return null;
			}
			else
			switch(str.charAt(1))
			{
			case 'a': return (lastKnownLocation!=null)?lastKnownLocation.getArea():null;
			case 'N':
			case 'n': return source;
			case 'I':
			case 'i': return scripted;
			case 'T':
			case 't': return target;
			case 'O':
			case 'o': return primaryItem;
			case 'P':
			case 'p': return secondaryItem;
			case 'd': 
			case 'D': return lastKnownLocation;
			case 'F':
			case 'f': if((monster!=null)&&(monster.amFollowing()!=null))
						return monster.amFollowing();
					  return null;
			case 'r':
			case 'R': return getFirstPC(monster,null,lastKnownLocation);
			case 'c':
			case 'C': return getFirstAnyone(monster,null,lastKnownLocation);
			case 'w': return primaryItem!=null?primaryItem.owner():null;
			case 'W': return secondaryItem!=null?secondaryItem.owner():null;
			case 'x':
			case 'X':
				if(lastKnownLocation!=null)
				{
					if((str.length()>2)&&(Directions.getGoodDirectionCode(""+str.charAt(2))>=0))
						return lastKnownLocation.getExitInDir(Directions.getGoodDirectionCode(""+str.charAt(2)));
					int i=0;
					Exit E=null;
					while(((++i)<100)||(E!=null))
						E=lastKnownLocation.getExitInDir(CMLib.dice().roll(1,Directions.NUM_DIRECTIONS,-1));
					return E;
				}
				return null;
			}
		}
		if(lastKnownLocation!=null)
		{
			str=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,str);
			Environmental E=lastKnownLocation.fetchFromRoomFavorMOBs(null,str,Item.WORNREQ_ANY);
			if(E==null) E=lastKnownLocation.fetchFromMOBRoomFavorsItems(monster,null,str,Item.WORNREQ_ANY);
			return E;
		}
		return null;
	}

	private String makeNamedString(Object O)
	{
		if(O instanceof Vector)
			return makeParsableString((Vector)O);
		else
		if(O instanceof Room) 
			return ((Room)O).roomTitle();
		else
		if(O instanceof Environmental) 
			return ((Environmental)O).Name();
		else
		if(O!=null) 
			return O.toString();
		return "";
	}
	
	private String makeParsableString(Vector V)
	{
		if((V==null)||(V.size()==0)) return "";
		if(V.firstElement() instanceof String) return CMParms.combineWithQuotes(V,0);
		StringBuffer ret=new StringBuffer("");
		String S=null;
		for(int v=0;v<V.size();v++)
		{
			S=makeNamedString(V.elementAt(v)).trim();
			if(S.length()==0)
				ret.append("? ");
			else
			if(S.indexOf(" ")>=0)
				ret.append("\""+S+"\" ");
			else
				ret.append(S+" ");
		}
		return ret.toString();
	}
	
	public String varify(MOB source, 
						 Environmental target, 
						 MOB monster, 
						 Item primaryItem, 
						 Item secondaryItem, 
						 String msg,
						 Object[] tmp,
						 String varifyable)
	{
		int t=varifyable.indexOf("$");
		if((monster!=null)&&(monster.location()!=null))
			lastKnownLocation=monster.location();
        if(lastKnownLocation==null) lastKnownLocation=source.location();
		MOB randMOB=null;
		while((t>=0)&&(t<varifyable.length()-1))
		{
			char c=varifyable.charAt(t+1);
			String middle="";
			String front=varifyable.substring(0,t);
			String back=varifyable.substring(t+2);
			if(Character.isDigit(c))
				middle=makeNamedString(tmp[CMath.s_int(Character.toString(c))]);
			else
			switch(c)
			{
			case 'a':
				if(lastKnownLocation!=null)
					middle=lastKnownLocation.getArea().name();
				break;
			case 'c':
			case 'C':
				randMOB=getFirstAnyone(monster,randMOB,lastKnownLocation);
				if(randMOB!=null)
					middle=randMOB.name();
				break;
			case 'i':
				if(monster!=null)
					middle=monster.name();
				break;
			case 'I':
				if(monster!=null)
					middle=monster.displayText();
				break;
			case 'n':
			case 'N':
				if(source!=null)
					middle=source.name();
				break;
			case 't':
			case 'T':
				if(target!=null)
					middle=target.name();
				break;
            case 'y':
                if(source!=null)
                    middle=source.charStats().sirmadam();
                break;
            case 'Y':
                if((target!=null)&&(target instanceof MOB))
                    middle=((MOB)target).charStats().sirmadam();
                break;
			case 'r':
			case 'R':
				randMOB=getFirstPC(monster,randMOB,lastKnownLocation);
				if(randMOB!=null)
					middle=randMOB.name();
				break;
			case 'j':
				if(monster!=null)
					middle=monster.charStats().heshe();
				break;
			case 'f':
				if((monster!=null)&&(monster.amFollowing()!=null))
					middle=monster.amFollowing().name();
				break;
			case 'F':
				if((monster!=null)&&(monster.amFollowing()!=null))
					middle=monster.amFollowing().charStats().heshe();
				break;
			case 'e':
				if(source!=null)
					middle=source.charStats().heshe();
				break;
			case 'E':
				if((target!=null)&&(target instanceof MOB))
					middle=((MOB)target).charStats().heshe();
				break;
			case 'J':
				randMOB=getFirstPC(monster,randMOB,lastKnownLocation);
				if(randMOB!=null)
					middle=randMOB.charStats().heshe();
				break;
			case 'k':
				if(monster!=null)
					middle=monster.charStats().hisher();
				break;
			case 'm':
				if(source!=null)
					middle=source.charStats().hisher();
				break;
			case 'M':
				if((target!=null)&&(target instanceof MOB))
					middle=((MOB)target).charStats().hisher();
				break;
			case 'K':
				randMOB=getFirstPC(monster,randMOB,lastKnownLocation);
				if(randMOB!=null)
					middle=randMOB.charStats().hisher();
				break;
			case 'o':
			case 'O':
				if(primaryItem!=null)
					middle=primaryItem.name();
				break;
			case 'g': middle=((msg==null)?"":msg.toLowerCase()); break;
			case 'G': middle=((msg==null)?"":msg); break;
			case 'd': middle=(lastKnownLocation!=null)?lastKnownLocation.roomTitle():""; break;
			case 'D': middle=(lastKnownLocation!=null)?lastKnownLocation.roomDescription():""; break;
			case 'p':
			case 'P':
				if(secondaryItem!=null)
					middle=secondaryItem.name();
				break;
			case 'w': 
			    middle=primaryItem!=null?primaryItem.owner().Name():middle;
			    break;
			case 'W': 
			    middle=secondaryItem!=null?secondaryItem.owner().Name():middle;
			    break;
			case 'l':
				if(lastKnownLocation!=null)
				{
					StringBuffer str=new StringBuffer("");
					for(int i=0;i<lastKnownLocation.numInhabitants();i++)
					{
						MOB M=lastKnownLocation.fetchInhabitant(i);
						if((M!=null)&&(M!=monster)&&(CMLib.flags().canBeSeenBy(M,monster)))
						   str.append("\""+M.name()+"\" ");
					}
					middle=str.toString();
					break;
				}
			case 'L':
				if(lastKnownLocation!=null)
				{
					StringBuffer str=new StringBuffer("");
					for(int i=0;i<lastKnownLocation.numItems();i++)
					{
						Item I=lastKnownLocation.fetchItem(i);
						if((I!=null)&&(I.container()==null)&&(CMLib.flags().canBeSeenBy(I,monster)))
						   str.append("\""+I.name()+"\" ");
					}
					middle=str.toString();
					break;
				}
			case '<':
				{
					int x=back.indexOf(">");
					if(x>=0)
					{
						String mid=back.substring(0,x);
						int y=mid.indexOf(" ");
						Environmental E=null;
                        String arg1="";
						if(y>=0)
						{
                            arg1=mid.substring(0,y).trim();
							E=getArgumentItem(arg1,source,monster,monster,target,primaryItem,secondaryItem,msg,tmp);
							mid=mid.substring(y+1).trim();
						}
                        if(arg1.length()>0)
                            middle=getVar(E,arg1,mid,source,target,monster,primaryItem,secondaryItem,msg,tmp);
                        back=back.substring(x+1);
					}
				}
				break;
			case '[':
				{
					middle="";
					int x=back.indexOf("]");
					if(x>=0)
					{
						String mid=back.substring(0,x);
						int y=mid.indexOf(" ");
						if(y>0)
						{
							int num=CMath.s_int(mid.substring(0,y).trim());
							mid=mid.substring(y+1).trim();
							Quest Q=mid.equals("*")?defaultQuest:CMLib.quests().fetchQuest(mid);
							if(Q!=null)	middle=Q.getQuestItemName(num);
						}
						back=back.substring(x+1);
					}
				}
				break;
			case '{':
				{
					middle="";
					int x=back.indexOf("}");
					if(x>=0)
					{
						String mid=back.substring(0,x).trim();
						int y=mid.indexOf(" ");
						if(y>0)
						{
							int num=CMath.s_int(mid.substring(0,y).trim());
							mid=mid.substring(y+1).trim();
							Quest Q=mid.equals("*")?defaultQuest:CMLib.quests().fetchQuest(mid);
							if(Q!=null)	middle=Q.getQuestMobName(num);
						}
						back=back.substring(x+1);
					}
				}
				break;
			case '%':
				{
					middle="";
					int x=back.indexOf("%");
					if(x>=0)
					{
						middle=functify(monster,source,target,monster,primaryItem,secondaryItem,msg,tmp,back.substring(0,x).trim());
						back=back.substring(x+1);
					}
				}
				break;
			//case 'a':
			case 'A':
				// unnecessary, since, in coffeemud, this is part of the name
				break;
			case 'x':
			case 'X':
				if(lastKnownLocation!=null)
				{
					middle="";
					Exit E=null;
					int dir=-1;
					if((t<varifyable.length()-2)&&(Directions.getGoodDirectionCode(""+varifyable.charAt(t+2))>=0))
					{
						dir=Directions.getGoodDirectionCode(""+varifyable.charAt(t+2));
						E=lastKnownLocation.getExitInDir(dir);
					}
					else
					{
						int i=0;
						while(((++i)<100)||(E!=null))
						{
							dir=CMLib.dice().roll(1,Directions.NUM_DIRECTIONS,-1);
							E=lastKnownLocation.getExitInDir(dir);
						}
					}
					if((dir>=0)&&(E!=null))
					{
						if(c=='x')
							middle=Directions.getDirectionName(dir);
						else
							middle=E.name();
					}
				}
				break;
			}
            if((middle.length()>0)
            &&(back.startsWith("."))
            &&(back.length()>1))
            {
                if(back.charAt(1)=='$')
                    back=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,back);
    			if((back.length()>1)&&Character.isDigit(back.charAt(1)))
    			{
    				int x=1;
    				while((x<back.length())
    				&&(Character.isDigit(back.charAt(x))))
    					x++;
    				int y=CMath.s_int(back.substring(1,x).trim());
    				back=back.substring(x);
    				boolean rest=back.startsWith("..");
    				if(rest) back=back.substring(2);
    				Vector V=CMParms.parse(middle);
    				if((V.size()>0)&&(y>=0))
    				{
    					if(y>=V.size())
    						middle="";
    					else
    					if(rest)
    					    middle=CMParms.combine(V,y);
    					else
    						middle=(String)V.elementAt(y);
    				}
    			}
            }
			varifyable=front+middle+back;
			t=varifyable.indexOf("$");
		}
		return varifyable;
	}

	public static DVector getScriptVarSet(String mobname, String varname)
	{
		DVector set=new DVector(2);
		if(mobname.equals("*"))
		{
			Vector V=Resources.findResourceKeys("SCRIPTVAR-");
			for(int v=0;v<V.size();v++)
			{
				String key=(String)V.elementAt(v);
				if(key.startsWith("SCRIPTVAR-"))
				{
					Hashtable H=(Hashtable)Resources.getResource(key);
					if(varname.equals("*"))
					{
						for(Enumeration e=H.keys();e.hasMoreElements();)
						{
							String vn=(String)e.nextElement();
							set.addElement(key.substring(10),vn);
						}
					}
					else
						set.addElement(key.substring(10),varname);
				}
			}
		}
		else
		{
			Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+mobname);
			if(varname.equals("*"))
			{
				for(Enumeration e=H.keys();e.hasMoreElements();)
				{
					String vn=(String)e.nextElement();
					set.addElement(mobname,vn);
				}
			}
			else
				set.addElement(mobname,varname);
		}
		return set;
	}
    
    public String getStatValue(Environmental E, String arg2)
    {
        boolean found=false;
        String val="";
        for(int i=0;i<E.getStatCodes().length;i++)
        {
            if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
            {
                val=E.getStat(arg2);
                found=true;
                break;
            }
        }
        if((!found)&&(E instanceof MOB))
        {
            MOB M=(MOB)E;
            for(int i=0;i<CharStats.STAT_DESCS.length;i++)
                if(CharStats.STAT_DESCS[i].equalsIgnoreCase(arg2))
                {
                    val=""+M.charStats().getStat(CharStats.STAT_DESCS[i]);
                    found=true;
                    break;
                }
            if(!found)
            for(int i=0;i<M.curState().getStatCodes().length;i++)
                if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
                {
                    val=M.curState().getStat(M.curState().getStatCodes()[i]);
                    found=true;
                    break;
                }
            if(!found)
            for(int i=0;i<M.envStats().getCodes().length;i++)
                if(M.envStats().getCodes()[i].equalsIgnoreCase(arg2))
                {
                    val=M.envStats().getStat(M.envStats().getCodes()[i]);
                    found=true;
                    break;
                }
			if((!found)&&(M.playerStats()!=null))
				for(int i=0;i<M.playerStats().getStatCodes().length;i++)
					if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
					{
						val=M.playerStats().getStat(M.playerStats().getStatCodes()[i]);
						found=true;
						break;
					}
            if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                for(int i=0;i<M.baseState().getStatCodes().length;i++)
                    if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                    {
                        val=M.baseState().getStat(M.baseState().getStatCodes()[i]);
                        found=true;
                        break;
                    }
        }
        if(!found)return null;
        return val;
    }
    public String getGStatValue(Environmental E, String arg2)
    {
        if(E==null) return null;
        boolean found=false;
        String val="";
        for(int i=0;i<E.getStatCodes().length;i++)
        {
            if(E.getStatCodes()[i].equalsIgnoreCase(arg2))
            {
                val=E.getStat(arg2);
                found=true; break;
            }
        }
        if(!found)
        if(E instanceof MOB)
        {
            for(int i=0;i<CMObjectBuilder.GENMOBCODES.length;i++)
            {
                if(CMObjectBuilder.GENMOBCODES[i].equalsIgnoreCase(arg2))
                {
                    val=CMLib.coffeeMaker().getGenMobStat((MOB)E,CMObjectBuilder.GENMOBCODES[i]);
                    found=true; break;
                }
            }
            if(!found)
            {
                MOB M=(MOB)E;
                for(int i=0;i<CharStats.STAT_DESCS.length;i++)
                    if(CharStats.STAT_DESCS[i].equalsIgnoreCase(arg2))
                    {
                        val=""+M.charStats().getStat(CharStats.STAT_DESCS[i]);
                        found=true;
                        break;
                    }
                if(!found)
                for(int i=0;i<M.curState().getStatCodes().length;i++)
                    if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
                    {
                        val=M.curState().getStat(M.curState().getStatCodes()[i]);
                        found=true;
                        break;
                    }
                if(!found)
                for(int i=0;i<M.envStats().getCodes().length;i++)
                    if(M.envStats().getCodes()[i].equalsIgnoreCase(arg2))
                    {
                        val=M.envStats().getStat(M.envStats().getCodes()[i]);
                        found=true;
                        break;
                    }
    			if((!found)&&(M.playerStats()!=null))
				for(int i=0;i<M.playerStats().getStatCodes().length;i++)
					if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
					{
						val=M.playerStats().getStat(M.playerStats().getStatCodes()[i]);
						found=true;
						break;
					}
                if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                    for(int i=0;i<M.baseState().getStatCodes().length;i++)
                        if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                        {
                            val=M.baseState().getStat(M.baseState().getStatCodes()[i]);
                            found=true;
                            break;
                        }
            }
        }
        else
        if(E instanceof Item)
        {
            for(int i=0;i<CMObjectBuilder.GENITEMCODES.length;i++)
            {
                if(CMObjectBuilder.GENITEMCODES[i].equalsIgnoreCase(arg2))
                {
                    val=CMLib.coffeeMaker().getGenItemStat((Item)E,CMObjectBuilder.GENITEMCODES[i]);
                    found=true; break;
                }
            }
        }
        if(found) return val;
        return null;
    }
    

	public static void mpsetvar(String name, String key, String val)
	{
		DVector V=getScriptVarSet(name,key);
		for(int v=0;v<V.size();v++)
		{
			name=(String)V.elementAt(v,1);
			key=((String)V.elementAt(v,2)).toUpperCase();
			Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+name);
			if(H==null)
			{
				if(val.length()==0)
					continue;

				H=new Hashtable();
				Resources.submitResource("SCRIPTVAR-"+name,H);
			}
			if(val.equals("++"))
			{
				String num=(String)H.get(key);
				if(num==null) num="0";
				val=new Integer(CMath.s_int(num.trim())+1).toString();
			}
			else
			if(val.equals("--"))
			{
				String num=(String)H.get(key);
				if(num==null) num="0";
				val=new Integer(CMath.s_int(num.trim())-1).toString();
			}
			else
			if(val.startsWith("+"))
			{
				// add via +number form
				val=val.substring(1);
				int amount=CMath.s_int(val.trim());
				String num=(String)H.get(key);
				val=new Integer(CMath.s_int(num.trim())+amount).toString();
			}
			else
			if(val.startsWith("-"))
			{
				// subtract -number form
				val=val.substring(1);
				int amount=CMath.s_int(val.trim());
				String num=(String)H.get(key);
				val=new Integer(CMath.s_int(num.trim())-amount).toString();
			}
			else
			if(val.startsWith("*"))
			{
				// multiply via *number form
				val=val.substring(1);
				int amount=CMath.s_int(val.trim());
				String num=(String)H.get(key);
				val=new Integer(CMath.s_int(num.trim())*amount).toString();
			}
			else
			if(val.startsWith("/"))
			{
				// divide /number form
				val=val.substring(1);
				int amount=CMath.s_int(val.trim());
				String num=(String)H.get(key);
				val=new Integer(CMath.s_int(num.trim())/amount).toString();
			}
			if(H.containsKey(key))
				H.remove(key);
			if(val.trim().length()>0)
				H.put(key,val);
			if(H.size()==0)
				Resources.removeResource("SCRIPTVAR-"+name);
		}
	}


	public boolean eval(Environmental scripted,
						MOB source,
						Environmental target,
						MOB monster,
						Item primaryItem,
						Item secondaryItem,
						String msg,
						Object[] tmp,
						String evaluable)
	{
		Vector formatCheck=CMParms.parse(evaluable);
		for(int i=1;i<(formatCheck.size()-1);i++)
			if((" == >= > < <= => =< != ".indexOf(" "+((String)formatCheck.elementAt(i))+" ")>=0)
			&&(((String)formatCheck.elementAt(i-1)).endsWith(")")))
			{
				String ps=(String)formatCheck.elementAt(i-1);
				ps=ps.substring(0,ps.length()-1);
				if(ps.length()==0) ps=" ";
				formatCheck.setElementAt(ps,i-1);

				String os=null;
				if((((String)formatCheck.elementAt(i+1)).startsWith("'")
				   ||((String)formatCheck.elementAt(i+1)).startsWith("`")))
				{
					os="";
					while((i<(formatCheck.size()-1))
					&&((!((String)formatCheck.elementAt(i+1)).endsWith("'"))
					&&(!((String)formatCheck.elementAt(i+1)).endsWith("`"))))
					{
						os+=((String)formatCheck.elementAt(i+1))+" ";
						formatCheck.removeElementAt(i+1);
					}
					os=(os+((String)formatCheck.elementAt(i+1))).trim();
				}
				else
				if((i==(formatCheck.size()-3))
				&&(((String)formatCheck.lastElement()).indexOf("(")<0))
				{
					os=((String)formatCheck.elementAt(i+1))
					+" "+((String)formatCheck.elementAt(i+2));
					formatCheck.removeElementAt(i+2);
				}
				else
					os=(String)formatCheck.elementAt(i+1);
				os=os+")";
				formatCheck.setElementAt(os,i+1);
				i+=2;
			}
		evaluable=CMParms.combine(formatCheck,0);
		String uevaluable=evaluable.toUpperCase().trim();
		boolean returnable=false;
		boolean lastreturnable=true;
		int joined=0;
		while(evaluable.length()>0)
		{
			int y=evaluable.indexOf("(");
			int z=y+1;
			int numy=1;
			while((y>=0)&&(numy>0)&&(z<evaluable.length()))
			{
				if(evaluable.charAt(z)=='(')
					numy++;
				else
				if(evaluable.charAt(z)==')')
					numy--;
				z++;
			}
			if((y<0)||(numy>0)||(z<=y))
			{
				scriptableError(scripted,"EVAL","Format",evaluable);
				return false;
			}
			z--;
			String preFab=uevaluable.substring(0,y).trim();
			Integer funcCode=(Integer)funcH.get(preFab);
			if(funcCode==null)
			    funcCode=new Integer(0);
			if(y==0)
			{
				int depth=0;
				int i=0;
				while((++i)<evaluable.length())
				{
					char c=evaluable.charAt(i);
					if((c==')')&&(depth==0))
					{
						String expr=evaluable.substring(1,i);
						evaluable=evaluable.substring(i+1).trim();
						uevaluable=uevaluable.substring(i+1).trim();
						returnable=eval(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,expr);
						switch(joined)
						{
						case 1: returnable=lastreturnable&&returnable; break;
						case 2: returnable=lastreturnable||returnable; break;
						case 4: returnable=!returnable; break;
						case 5: returnable=lastreturnable&&(!returnable); break;
						case 6: returnable=lastreturnable||(!returnable); break;
						default: break;
						}
						joined=0;
						break;
					}
					else
					if(c=='(') depth++;
					else
					if(c==')') depth--;
				}
				z=evaluable.indexOf(")");
			}
			else
			if(evaluable.startsWith("!"))
			{
			    joined=joined|4;
				evaluable=evaluable.substring(1).trim();
				uevaluable=uevaluable.substring(1).trim();
			}
			else
			if(uevaluable.startsWith("AND "))
			{
			    joined=1;
			    lastreturnable=returnable;
				evaluable=evaluable.substring(4).trim();
				uevaluable=uevaluable.substring(4).trim();
			}
			else
			if(uevaluable.startsWith("OR "))
			{
			    joined=2;
			    lastreturnable=returnable;
				evaluable=evaluable.substring(3).trim();
				uevaluable=uevaluable.substring(3).trim();
			}
			else
			if((y<0)||(z<y))
			{
				scriptableError(scripted,"()","Syntax",evaluable);
				break;
			}
			else
			{
		    tickStatus=Tickable.STATUS_MISC+funcCode.intValue();
			switch(funcCode.intValue())
			{
			case 1: // rand
			{
				int arg=CMath.s_int(evaluable.substring(y+1,z).trim());
				if(CMLib.dice().rollPercentage()<arg)
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 2: // has
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					scriptableError(scripted,"HAS","Syntax",evaluable);
					return returnable;
				}
				Environmental E2=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
				{
				    if(E2!=null)
						returnable=((MOB)E).isMine(E2);
			        else
						returnable=(((MOB)E).fetchInventory(arg2)!=null);
				}
				else
				if(E instanceof Item)
					returnable=CMLib.english().containsString(E.name(),arg2);
				else
				if(E instanceof Room)
				{
				    if(E2 instanceof Item)
						returnable=((Room)E).isContent((Item)E2);
			        else
			            returnable=(((Room)E).fetchItem(null,arg2)!=null);
				}
				else
					returnable=false;
				break;
			}
            case 74: // hasnum
            {
                String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                String item=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
                String cmp=CMParms.getCleanBit(evaluable.substring(y+1,z),2);
                String value=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),2));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((value.length()==0)||(item.length()==0)||(cmp.length()==0))
                {
                    scriptableError(scripted,"HASNUM","Syntax",evaluable);
                    return returnable;
                }
                Item I=null;
                int num=0;
                if(E==null)
                    returnable=false;
                else
                if(E instanceof MOB)
                {
                    MOB M=(MOB)E;
                    for(int i=0;i<M.inventorySize();i++)
                    {
                        I=M.fetchInventory(i);
                        if(I==null) break;
                        if((item.equalsIgnoreCase("all"))
                        ||(CMLib.english().containsString(I.Name(),item)))
                            num++;
                    }
                    returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
                }
                else
                if(E instanceof Item)
                {
                    num=CMLib.english().containsString(E.name(),item)?1:0;
                    returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
                }
                else
                if(E instanceof Room)
                {
                    Room R=(Room)E;
                    for(int i=0;i<R.numItems();i++)
                    {
                        I=R.fetchItem(i);
                        if(I==null) break;
                        if((item.equalsIgnoreCase("all"))
                        ||(CMLib.english().containsString(I.Name(),item)))
                            num++;
                    }
                    returnable=simpleEval(scripted,""+num,value,cmp,"HASNUM");
                }
                else
                    returnable=false;
                break;
            }
			case 67: // hastitle
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					scriptableError(scripted,"HASTITLE","Syntax",evaluable);
					return returnable;
				}
				if(E instanceof MOB)
				{
				    MOB M=(MOB)E;
				    returnable=(M.playerStats()!=null)&&(M.playerStats().getTitles().contains(arg2));
				}
				else
				    returnable=false;
				break;
			}
			case 3: // worn
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					scriptableError(scripted,"WORN","Syntax",evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
					returnable=(((MOB)E).fetchWornItem(arg2)!=null);
				else
				if(E instanceof Item)
					returnable=(CMLib.english().containsString(E.name(),arg2)&&(!((Item)E).amWearingAt(Item.IN_INVENTORY)));
				else
					returnable=false;
				break;
			}
			case 4: // isnpc
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isMonster();
				break;
			}
			case 5: // ispc
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=!((MOB)E).isMonster();
				break;
			}
			case 6: // isgood
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CMLib.flags().isGood(E);
				break;
			}
			case 8: // isevil
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CMLib.flags().isEvil(E);
				break;
			}
			case 9: // isneutral
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CMLib.flags().isNeutral(E);
				break;
			}
			case 54: // isalive
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 58: // isable
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
				{
					ExpertiseLibrary X=(ExpertiseLibrary)CMLib.expertises().findDefinition(arg2,true);
					if(X!=null)
						returnable=((MOB)E).fetchExpertise(X.ID())!=null;
					else
						returnable=((MOB)E).findAbility(arg2)!=null;
				}
				else
					returnable=false;
				break;
			}
			case 59: // isopen
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				int dir=Directions.getGoodDirectionCode(arg1);
				returnable=false;
				if(dir<0)
				{
					Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						returnable=((Container)E).isOpen();
					else
					if((E!=null)&&(E instanceof Exit))
					    returnable=((Exit)E).isOpen();
				}
				else
				if(lastKnownLocation!=null)
				{
					Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null) returnable= E.isOpen();
				}
				break;
			}
			case 60: // islocked
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				int dir=Directions.getGoodDirectionCode(arg1);
				returnable=false;
				if(dir<0)
				{
					Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						returnable=((Container)E).isLocked();
					else
					if((E!=null)&&(E instanceof Exit))
					    returnable=((Exit)E).isLocked();
				}
				else
				if(lastKnownLocation!=null)
				{
					Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null) returnable= E.isLocked();
				}
				break;
			}
			case 10: // isfight
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=((MOB)E).isInCombat();
				break;
			}
			case 11: // isimmort
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CMSecurity.isAllowed(((MOB)E),lastKnownLocation,"IMMORT");
				break;
			}
			case 12: // ischarmed
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
					returnable=CMLib.flags().flaggedAffects(E,Ability.FLAG_CHARMING).size()>0;
				break;
			}
			case 15: // isfollow
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(((MOB)E).amFollowing()==null)
					returnable=false;
				else
				if(((MOB)E).amFollowing().location()!=lastKnownLocation)
					returnable=false;
				else
					returnable=true;
				break;
			}
            case 73: // isservant
            {
                String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB))||(lastKnownLocation==null))
                    returnable=false;
                else
                if((((MOB)E).getLiegeID()==null)||(((MOB)E).getLiegeID().length()==0))
                    returnable=false;
                else
                if(lastKnownLocation.fetchInhabitant("$"+((MOB)E).getLiegeID()+"$")==null)
                    returnable=false;
                else
                    returnable=true;
                break;
            }
			case 55: // ispkill
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(CMath.bset(((MOB)E).getBitmap(),MOB.ATT_PLAYERKILL))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 7: // isname
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=CMLib.english().containsString(E.name(),arg2);
				break;
			}
			case 56: // name
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=simpleEvalStr(scripted,E.Name(),arg3,arg2,"NAME");
				break;
			}
            case 75: // currency
            {
                String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
                String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(evaluable.substring(y+1,z),1));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    returnable=false;
                else
                    returnable=simpleEvalStr(scripted,CMLib.beanCounter().getCurrency(E),arg3,arg2,"CURRENCY");
                break;
            }
			case 61: // strin
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Vector V=CMParms.parse(arg1.toUpperCase());
				returnable=V.contains(arg2.toUpperCase());
				break;
			}
			case 62: // callfunc
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				String found=null;
				boolean validFunc=false;
				Vector scripts=getScripts();
				for(int v=0;v<scripts.size();v++)
				{
					Vector script2=(Vector)scripts.elementAt(v);
					if(script2.size()<1) continue;
					String trigger=((String)script2.elementAt(0)).toUpperCase().trim();
					if(getTriggerCode(trigger)==17)
					{
						String fnamed=CMParms.getCleanBit(trigger,1);
						if(fnamed.equalsIgnoreCase(arg1))
						{
							validFunc=true;
							found=
							execute(scripted,
									source,
									target,
									monster,
									primaryItem,
									secondaryItem,
									script2,
									varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,arg2),
									tmp);
							if(found==null) found="";
							break;
						}
					}
				}
				if(!validFunc)
					scriptableError(scripted,"CALLFUNC","Unknown","Function: "+arg1);
				else
					returnable=!(found.trim().length()==0);
				break;
			}
			case 14: // affected
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=(E.fetchEffect(arg2)!=null);
				break;
			}
			case 69: // isbehave
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
					returnable=(E.fetchBehavior(arg2)!=null);
				break;
			}
            case 70: // ipaddress
            {
                String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
                String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(evaluable.substring(y+1,z),1));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB))||(((MOB)E).isMonster()))
                    returnable=false;
                else
                    returnable=simpleEvalStr(scripted,((MOB)E).session().getAddress(),arg3,arg2,"ADDRESS");
                break;
            }
			case 28: // questwinner
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				Quest Q=arg2.equals("*")?defaultQuest:CMLib.quests().fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
				{
					if(E!=null) arg1=E.Name();
					returnable=Q.wasWinner(arg1);
				}
				break;
			}
			case 29: // questmob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Quest Q=arg2.equals("*")?defaultQuest:CMLib.quests().fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=(Q.wasQuestMob(arg1)>=0);
				break;
			}
			case 31: // isquestmobalive
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Quest Q=arg2.equals("*")?defaultQuest:CMLib.quests().fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
				{
					MOB M=null;
					if(CMath.s_int(arg1.trim())>0)
						M=Q.getQuestMob(CMath.s_int(arg1.trim()));
					else
						M=Q.getQuestMob(Q.wasQuestMob(arg1));
					if(M==null) returnable=false;
					else returnable=!M.amDead();
				}
				break;
			}
			case 32: // nummobsinarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				int num=0;
                Vector MASK=null;
                if((arg3.toUpperCase().startsWith("MASK")&&(arg3.substring(4).trim().startsWith("="))))
                { 
                    arg3=arg3.substring(4).trim(); 
                    arg3=arg3.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg3);
                }
				for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
                        if(M==null) continue;
                        if(MASK!=null)
                        {
                            if(CMLib.masking().maskCheck(MASK,M))
                                num++;
                        }
                        else
                        if(CMLib.english().containsString(M.name(),arg1))
                            num++;
					}
				}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMMOBSINAREA");
				break;
			}
			case 33: // nummobs
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				int num=0;
                Vector MASK=null;
                if((arg3.toUpperCase().startsWith("MASK")&&(arg3.substring(4).trim().startsWith("="))))
                { 
                    arg3=arg3.substring(4).trim(); 
                    arg3=arg3.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg3);
                }
				try
				{
					for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M=R.fetchInhabitant(m);
                            if(M==null) continue;
                            if(MASK!=null)
                            {
                                if(CMLib.masking().maskCheck(MASK,M))
                                    num++;
                            }
                            else
                            if(CMLib.english().containsString(M.name(),arg1))
								num++;
						}
					}
			    }catch(NoSuchElementException nse){}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMMOBS");
				break;
			}
			case 34: // numracesinarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				int num=0;
				Room R=null;
				MOB M=null;
				for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMRACESINAREA");
				break;
			}
			case 35: // numraces
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase();
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				int num=0;
				try
				{
					for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M=R.fetchInhabitant(m);
							if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
								num++;
						}
					}
			    }catch(NoSuchElementException nse){}
				returnable=simpleEval(scripted,""+num,arg3,arg2,"NUMRACES");
				break;
			}
			case 30: // questobj
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Quest Q=arg2.equals("*")?defaultQuest:CMLib.quests().fetchQuest(arg2);
				if(Q==null)
					returnable=false;
				else
					returnable=(Q.wasQuestItem(arg1)>=0);
				break;
			}
			case 16: // hitprcnt
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"HITPRCNT","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					double hitPctD=CMath.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
					int val1=(int)Math.round(hitPctD*100.0);
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"HITPRCNT");
				}
				break;
			}
			case 50: // isseason
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				returnable=false;
				if(monster.location()!=null)
				for(int a=0;a<TimeClock.SEASON_DESCS.length;a++)
					if((TimeClock.SEASON_DESCS[a]).startsWith(arg1.toUpperCase())
					&&(monster.location().getArea().getTimeObj().getSeasonCode()==a))
					{returnable=true; break;}
				break;
			}
			case 51: // isweather
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				returnable=false;
				if(monster.location()!=null)
				for(int a=0;a<Climate.WEATHER_DESCS.length;a++)
					if((Climate.WEATHER_DESCS[a]).startsWith(arg1.toUpperCase())
					&&(monster.location().getArea().getClimateObj().weatherType(monster.location())==a))
					{returnable=true; break;}
				break;
			}
			case 57: // ismoon
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				returnable=false;
				if(monster.location()!=null)
				{
					if(arg1.length()==0)
						returnable=monster.location().getArea().getClimateObj().canSeeTheStars(monster.location());
					else
					for(int a=0;a<TimeClock.PHASE_DESC.length;a++)
						if((TimeClock.PHASE_DESC[a]).startsWith(arg1.toUpperCase())
						&&(monster.location().getArea().getTimeObj().getMoonPhase()==a))
						{
							returnable=true;
							break;
						}
				}
				break;
			}
			case 38: // istime
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				if(monster.location()==null)
					returnable=false;
				else
				if(("daytime").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_DAY))
					returnable=true;
				else
				if(("dawn").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_DAWN))
					returnable=true;
				else
				if(("dusk").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_DUSK))
					returnable=true;
				else
				if(("nighttime").startsWith(arg1.toLowerCase())
				&&(monster.location().getArea().getTimeObj().getTODCode()==TimeClock.TIME_NIGHT))
					returnable=true;
				else
				if((monster.location().getArea().getTimeObj().getTODCode()==CMath.s_int(arg1.trim())))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 39: // isday
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				if((monster.location()!=null)&&(monster.location().getArea().getTimeObj().getDayOfMonth()==CMath.s_int(arg1.trim())))
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 45: // nummobsroom
			{
                int num=0;
                int startbit=0;
                if(lastKnownLocation!=null) 
                {
                    num=lastKnownLocation.numInhabitants();
                    if((CMParms.numBits(evaluable.substring(y+1,z))>2)
                    &&(!CMath.isInteger(CMParms.getCleanBit(evaluable.substring(y+1,z),1).trim())))
                    {
                        String name=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                        startbit++;
                        if(!name.equalsIgnoreCase("*"))
                        {
                            num=0;
                            Vector MASK=null;
                            if((name.toUpperCase().startsWith("MASK")&&(name.substring(4).trim().startsWith("="))))
                            { 
                                name=name.substring(4).trim(); 
                                name=name.substring(1).trim();
                                MASK=CMLib.masking().maskCompile(name);
                            }
                            for(int i=0;i<lastKnownLocation.numInhabitants();i++)
                            {
                                MOB M=lastKnownLocation.fetchInhabitant(i);
                                if(M==null) continue;
                                if(MASK!=null)
                                {
                                    if(CMLib.masking().maskCheck(MASK,M))
                                        num++;
                                }
                                else
                                if(CMLib.english().containsString(M.Name(),name)
                                ||CMLib.english().containsString(M.displayText(),name))
                                    num++;
                            }
                        }
                    }
                }
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),startbit));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),startbit));
				if(lastKnownLocation!=null)
					returnable=simpleEval(scripted,""+num,arg2,arg1,"NUMMOBSROOM");
				break;
			}
			case 63: // numpcsroom
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				if(lastKnownLocation!=null)
					returnable=simpleEval(scripted,""+lastKnownLocation.numPCInhabitants(),arg2,arg1,"NUMPCSROOM");
				break;
			}
			case 79: // numpcsarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				if(lastKnownLocation!=null)
				{
					int num=0;
					for(int s=0;s<CMLib.sessions().size();s++)
					{
						Session S=CMLib.sessions().elementAt(s);
						if((S!=null)&&(S.mob()!=null)&&(S.mob().location()!=null)&&(S.mob().location().getArea()==lastKnownLocation.getArea()))
							num++;
					}
					returnable=simpleEval(scripted,""+num,arg2,arg1,"NUMPCSAREA");
				}
				break;
			}
            case 77: // explored
            {
                String whom=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
                String where=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
                String cmp=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),2));
                String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),2));
                Environmental E=getArgumentItem(whom,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((E==null)||(!(E instanceof MOB)))
                {
                    scriptableError(scripted,"EXPLORED","Unknown Code",whom);
                    return returnable;
                }
                Area A=null;
                if(!where.equalsIgnoreCase("world"))
                {
                    Environmental E2=getArgumentItem(where,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                    if(E2 instanceof Area)
                        A=(Area)E2;
                    else
                        A=CMLib.map().getArea(where);
                    if(A==null)
                    {
                        scriptableError(scripted,"EXPLORED","Unknown Area",where);
                        return returnable;
                    }
                }
                if(lastKnownLocation!=null)
                {
                    int pct=0;
                    MOB M=(MOB)E;
                    if(M.playerStats()!=null)
                        pct=M.playerStats().percentVisited(M,A);
                    returnable=simpleEval(scripted,""+pct,arg2,cmp,"EXPLORED");
                }
                break;
            }
            case 72: // faction
            {
                String whom=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
                String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
                String cmp=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),2));
                String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),3));
                Environmental E=getArgumentItem(whom,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                Faction F=CMLib.factions().getFaction(arg1);
                if((E==null)||(!(E instanceof MOB)))
                {
                    scriptableError(scripted,"FACTION","Unknown Code",whom);
                    return returnable;
                }
                if(F==null)
                {
                    scriptableError(scripted,"FACTION","Unknown Faction",arg1);
                    return returnable;
                }
                MOB M=(MOB)E;
                String value=null;
                if(!M.hasFaction(F.factionID()))
                    value="";
                else
                {
                    int myfac=M.fetchFaction(F.factionID());
                    if(CMath.isNumber(arg2.trim()))
                        value=new Integer(myfac).toString();
                    else
                    {
                        Faction.FactionRange FR=CMLib.factions().getRange(F.factionID(),myfac);
                        if(FR==null) 
                            value="";
                        else 
                            value=FR.name();
                    }
                }
                if(lastKnownLocation!=null)
                    returnable=simpleEval(scripted,value,arg2,cmp,"FACTION");
                break;
            }
			case 46: // numitemsroom
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				int ct=0;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				returnable=simpleEval(scripted,""+ct,arg2,arg1,"NUMITEMSROOM");
				break;
			}
			case 47: //mobitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				MOB M=null;
				if(lastKnownLocation!=null)
					M=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
				Item which=null;
				int ct=1;
				if(M!=null)
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I=M.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==CMath.s_int(arg2.trim()))
						{ which=I; break;}
						ct++;
					}
				}
				if(which==null)
					returnable=false;
				else
					returnable=(CMLib.english().containsString(which.name(),arg3)
								||CMLib.english().containsString(which.Name(),arg3)
								||CMLib.english().containsString(which.displayText(),arg3));
				break;
			}
			case 49: // hastattoo
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					scriptableError(scripted,"HASTATTOO","Syntax",evaluable);
					break;
				}
				else
				if((E!=null)&&(E instanceof MOB))
					returnable=(((MOB)E).fetchTattoo(arg2)!=null);
				else
					returnable=false;
				break;
			}
			case 48: // numitemsmob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				MOB which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
				int ct=1;
				if(which!=null)
				for(int i=0;i<which.inventorySize();i++)
				{
					Item I=which.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				returnable=simpleEval(scripted,""+ct,arg3,arg2,"NUMITEMSMOB");
				break;
			}
			case 43: // roommob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
				if(which==null)
					returnable=false;
				else
					returnable=(CMLib.english().containsString(which.name(),arg2)
								||CMLib.english().containsString(which.Name(),arg2)
								||CMLib.english().containsString(which.displayText(),arg2));
				break;
			}
			case 44: // roomitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental which=null;
				int ct=1;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==CMath.s_int(arg1.trim()))
						{ which=I; break;}
						ct++;
					}
				}
				if(which==null)
					returnable=false;
				else
					returnable=(CMLib.english().containsString(which.name(),arg2)
								||CMLib.english().containsString(which.Name(),arg2)
								||CMLib.english().containsString(which.displayText(),arg2));
				break;
			}
			case 36: // ishere
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				if(lastKnownLocation!=null)
					returnable=((lastKnownLocation.fetchAnyItem(arg1)!=null)||(lastKnownLocation.fetchInhabitant(arg1)!=null));
				else
					returnable=false;
				break;
			}
			case 17: // inroom
			{
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String comp="==";
				Environmental E=monster;
				if((" == >= > < <= => =< != ".indexOf(" "+CMParms.getCleanBit(evaluable.substring(y+1,z),1)+" ")>=0))
				{
					E=getArgumentItem(CMParms.getCleanBit(evaluable.substring(y+1,z),0),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					comp=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
					arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				}
				else
				{
					scriptableError(scripted,"INROOM","Syntax",evaluable);
					return returnable;
				}
				Room R=null;
				if(arg2.startsWith("$"))
					R=CMLib.map().roomLocation(this.getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
				if(R==null)
					R=getRoom(arg2,lastKnownLocation);
				if(E==null)
					returnable=false;
				else
				{
					Room R2=CMLib.map().roomLocation(E);
					if((R==null)&&((arg2.length()==0)||(R2==null)))
						returnable=true;
					else
					if((R==null)||(R2==null))
						returnable=false;
					else
						returnable=simpleEvalStr(scripted,CMLib.map().getExtendedRoomID(R2),CMLib.map().getExtendedRoomID(R),comp,"INROOM");
				}
				break;
			}
			case 37: // inlocale
			{
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				Environmental E=monster;
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				if(arg2.length()==0)
					returnable=true;
				else
				if(CMClass.classID(((MOB)E).location()).toUpperCase().indexOf(arg2.toUpperCase())>=0)
					returnable=true;
				else
					returnable=false;
				break;
			}
			case 18: // sex
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0).toUpperCase());
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase();
				if(CMath.isNumber(arg3.trim()))
					switch(CMath.s_int(arg3.trim()))
					{
					case 0: arg3="NEUTER"; break;
					case 1: arg3="MALE"; break;
					case 2: arg3="FEMALE"; break;
					}
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"SEX","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=(""+((char)((MOB)E).charStats().getStat(CharStats.STAT_GENDER))).toUpperCase();
					if(arg2.equals("=="))
						returnable=arg3.startsWith(sex);
					else
					if(arg2.equals("!="))
						returnable=!arg3.startsWith(sex);
					else
					{
						scriptableError(scripted,"SEX","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 13: // stat
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=CMParms.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"STAT","Syntax",evaluable);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					String val=getStatValue(E,arg2);
                    if(val==null)
					{
						scriptableError(scripted,"STAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}

					if(arg3.equals("=="))
						returnable=val.equalsIgnoreCase(arg4);
					else
					if(arg3.equals("!="))
						returnable=!val.equalsIgnoreCase(arg4);
					else
						returnable=simpleEval(scripted,val,arg4,arg3,"STAT");
				}
				break;
			}
			case 52: // gstat
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=CMParms.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"GSTAT","Syntax",evaluable);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					String val=getGStatValue(E,arg2);
					if(val==null)
					{
						scriptableError(scripted,"GSTAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}

					if(arg3.equals("=="))
						returnable=val.equalsIgnoreCase(arg4);
					else
					if(arg3.equals("!="))
						returnable=!val.equalsIgnoreCase(arg4);
					else
						returnable=simpleEval(scripted,val,arg4,arg3,"GSTAT");
				}
				break;
			}
			case 19: // position
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"POSITION","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex="STANDING";
					if(CMLib.flags().isSleeping(E))
						sex="SLEEPING";
					else
					if(CMLib.flags().isSitting(E))
						sex="SITTING";
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						scriptableError(scripted,"POSITION","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 20: // level
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"LEVEL","Syntax",evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=E.envStats().level();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"LEVEL");
				}
				break;
			}
			case 80: // questpoints
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"QUESTPOINTS","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					int val1=((MOB)E).getQuestPoint();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"QUESTPOINTS");
				}
				break;
			}
			case 83: // qvar
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
				String arg3=CMParms.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),2));
				Quest Q=arg1.equals("*")?defaultQuest:CMLib.quests().fetchQuest(arg1);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"QUESTPOINTS","Syntax",evaluable);
					return returnable;
				}
				if(Q==null)
					returnable=false;
				else
					returnable=simpleEvalStr(scripted,Q.getStat(arg2),arg4,arg3,"QVAR");
				break;
			}
            case 84: // math
            {
                String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
                String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
                String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
                if(!CMath.isMathExpression(arg1))
                {
                    scriptableError(scripted,"MATH","Syntax",evaluable);
                    return returnable;
                }
                if(!CMath.isMathExpression(arg3))
                {
                    scriptableError(scripted,"MATH","Syntax",evaluable);
                    return returnable;
                }
                returnable=simpleExpressionEval(scripted,arg1,arg3,arg2,"MATH");
                break;
            }
			case 81: // trains
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"TRAINS","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					int val1=((MOB)E).getTrains();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"TRAINS");
				}
				break;
			}
			case 82: // pracs
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"PRACS","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					int val1=((MOB)E).getPractices();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"PRACS");
				}
				break;
			}
			case 66: // clanrank
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"CLANRANK","Syntax",evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=(E instanceof MOB)?((MOB)E).getClanRole():-1;
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"CLANRANK");
				}
				break;
			}
			case 64: // deity 
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					scriptableError(scripted,"DEITY","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).getWorshipCharID();
					if(arg2.equals("=="))
						returnable=sex.equalsIgnoreCase(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.equalsIgnoreCase(arg3);
					else
					{
						scriptableError(scripted,"DEITY","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 68: // clandata
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=CMParms.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),2).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"CLANDATA","Syntax",evaluable);
					return returnable;
				}
				String clanID=null;
				if((E!=null)&&(E instanceof MOB))
				    clanID=((MOB)E).getClanID();
				else
					clanID=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,arg1);
				Clan C=CMLib.clans().findClan(clanID);
				if(C!=null)
				{
				    int whichVar=-1;
				    for(int i=0;i<clanVars.length;i++)
				        if(arg2.equalsIgnoreCase(clanVars[i]))
				        { whichVar=i; break;}
				    String whichVal="";
				    switch(whichVar)
				    {
				    case 0: whichVal=C.getAcceptanceSettings(); break;
				    case 1: whichVal=C.getDetail(monster); break;
				    case 2: whichVal=C.getDonation(); break;
				    case 3: whichVal=""+C.getExp(); break;
				    case 4: whichVal=Clan.GVT_DESCS[C.getGovernment()]; break;
				    case 5: whichVal=C.getMorgue(); break;
				    case 6: whichVal=C.getPolitics(); break;
				    case 7: whichVal=C.getPremise(); break;
				    case 8: whichVal=C.getRecall(); break;
				    case 9: whichVal=""+C.getSize(); break; // size
				    case 10: whichVal=Clan.CLANSTATUS_DESC[C.getStatus()]; break;
				    case 11: whichVal=""+C.getTaxes(); break;
				    case 12: whichVal=""+C.getTrophies(); break;
				    case 13: whichVal=""+C.getType(); break; // type
				    case 14: {
			        	 Vector areas=C.getControlledAreas();
			        	 StringBuffer list=new StringBuffer("");
			        	 for(int i=0;i<areas.size();i++)
			        	     list.append("\""+((Environmental)areas.elementAt(i)).name()+"\" ");
			        	 whichVal=list.toString().trim();
			    		 break; // areas
				    }
				    case 15: {
				        	 DVector members=C.getMemberList();
				        	 StringBuffer list=new StringBuffer("");
				        	 for(int i=0;i<members.size();i++)
				        	     list.append("\""+((String)members.elementAt(i,1))+"\" ");
				        	 whichVal=list.toString().trim();
				    		 break; // memberlist
				    }
				    case 16: MOB M=C.getResponsibleMember();
				    		 if(M!=null) whichVal=M.Name();
				    		 break; // topmember
				    default:
						scriptableError(scripted,"CLANDATA","RunTime",arg2+" is not a valid clan variable.");
				    	break;
				    }
                    if(CMath.isNumber(whichVal.trim())&&CMath.isNumber(arg4.trim()))
    				    returnable=simpleEval(scripted,whichVal,arg4,arg3,"CLANDATA");
                    else
                        returnable=simpleEvalStr(scripted,whichVal,arg4,arg3,"CLANDATA");
				}
				break;
			}
			case 65: // clan 
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()==0)
				{
					scriptableError(scripted,"CLAN","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).getClanID();
					if(arg2.equals("=="))
						returnable=sex.equalsIgnoreCase(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.equalsIgnoreCase(arg3);
					else
					{
						scriptableError(scripted,"CLAN","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 21: // class
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"CLASS","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().displayClassName().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						scriptableError(scripted,"CLASS","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 22: // baseclass
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"CLASS","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().getCurrentClass().baseClass().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						scriptableError(scripted,"CLASS","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 23: // race
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"RACE","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().raceName().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						scriptableError(scripted,"RACE","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 24: //racecat
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"RACECAT","Syntax",evaluable);
					return returnable;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					String sex=((MOB)E).charStats().getMyRace().racialCategory().toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.startsWith(arg3);
					else
					if(arg2.equals("!="))
						returnable=!sex.startsWith(arg3);
					else
					{
						scriptableError(scripted,"RACECAT","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 25: // goldamt
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"GOLDAMT","Syntax",evaluable);
					break;
				}
				if(E==null)
					returnable=false;
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,CMLib.beanCounter().getCurrency(scripted)));
					else
					if(E instanceof Coins)
						val1=(int)Math.round(((Coins)E).getTotalValue());
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						scriptableError(scripted,"GOLDAMT","Syntax",evaluable);
						return returnable;
					}

					returnable=simpleEval(scripted,""+val1,arg3,arg2,"GOLDAMT");
				}
				break;
			}
			case 78: // exp
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"EXP","Syntax",evaluable);
					break;
				}
				if((E==null)||(!(E instanceof MOB)))
					returnable=false;
				else
				{
					int val1=((MOB)E).getExperience();
					returnable=simpleEval(scripted,""+val1,arg3,arg2,"EXP");
				}
				break;
			}
            case 76: // value
            {
                String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
                String arg3=CMParms.getCleanBit(evaluable.substring(y+1,z),2);
                String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),2));
                if((arg2.length()==0)||(arg3.length()==0)||(arg4.length()==0))
                {
                    scriptableError(scripted,"VALUE","Syntax",evaluable);
                    break;
                }
                if(!CMLib.beanCounter().getAllCurrencies().contains(arg2.toUpperCase()))
                {
                    scriptableError(scripted,"VALUE","Syntax",arg2+" is not a valid designated currency.");
                    break;
                }
                if(E==null)
                    returnable=false;
                else
                {
                    int val1=0;
                    if(E instanceof MOB)
                        val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,arg2.toUpperCase()));
                    else
                    if(E instanceof Coins)
                    {
                        if(((Coins)E).getCurrency().equalsIgnoreCase(arg2))
                            val1=(int)Math.round(((Coins)E).getTotalValue());
                    }
                    else
                    if(E instanceof Item)
                        val1=((Item)E).value();
                    else
                    {
                        scriptableError(scripted,"VALUE","Syntax",evaluable);
                        return returnable;
                    }

                    returnable=simpleEval(scripted,""+val1,arg4,arg3,"GOLDAMT");
                }
                break;
            }
			case 26: // objtype
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1).toUpperCase());
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"OBJTYPE","Syntax",evaluable);
					return returnable;
				}
				if(E==null)
					returnable=false;
				else
				{
					String sex=CMClass.classID(E).toUpperCase();
					if(arg2.equals("=="))
						returnable=sex.indexOf(arg3)>=0;
					else
					if(arg2.equals("!="))
						returnable=sex.indexOf(arg3)<0;
					else
					{
						scriptableError(scripted,"OBJTYPE","Syntax",evaluable);
						return returnable;
					}
				}
				break;
			}
			case 27: // var
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1).toUpperCase();
				String arg3=CMParms.getCleanBit(evaluable.substring(y+1,z),2);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(evaluable.substring(y+1,z),2));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()==0)||(arg3.length()==0))
				{
					scriptableError(scripted,"VAR","Syntax",evaluable);
					return returnable;
				}
                String val=getVar(E,arg1,arg2,source,target,monster,primaryItem,secondaryItem,msg,tmp);
				if(arg3.equals("=="))
					returnable=val.equals(arg4);
				else
				if(arg3.equals("!="))
					returnable=!val.equals(arg4);
				else
				if(arg3.equals(">"))
					returnable=CMath.s_int(val.trim())>CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<"))
					returnable=CMath.s_int(val.trim())<CMath.s_int(arg4.trim());
				else
				if(arg3.equals(">="))
					returnable=CMath.s_int(val.trim())>=CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<="))
					returnable=CMath.s_int(val.trim())<=CMath.s_int(arg4.trim());
				else
				{
					scriptableError(scripted,"VAR","Syntax",evaluable);
					return returnable;
				}
				break;
			}
			case 41: // eval
			{
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg3=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(evaluable.substring(y+1,z),1));
				if(arg3.length()==0)
				{
					scriptableError(scripted,"EVAL","Syntax",evaluable);
					return returnable;
				}
				if(arg3.equals("=="))
					returnable=val.equals(arg4);
				else
				if(arg3.equals("!="))
					returnable=!val.equals(arg4);
				else
				if(arg3.equals(">"))
					returnable=CMath.s_int(val.trim())>CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<"))
					returnable=CMath.s_int(val.trim())<CMath.s_int(arg4.trim());
				else
				if(arg3.equals(">="))
					returnable=CMath.s_int(val.trim())>=CMath.s_int(arg4.trim());
				else
				if(arg3.equals("<="))
					returnable=CMath.s_int(val.trim())<=CMath.s_int(arg4.trim());
				else
				{
					scriptableError(scripted,"EVAL","Syntax",evaluable);
					return returnable;
				}
				break;
			}
			case 40: // number
			{
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z))).trim();
				boolean isnumber=(val.length()>0);
				for(int i=0;i<val.length();i++)
					if(!Character.isDigit(val.charAt(i)))
					{ isnumber=false; break;}
				returnable=isnumber;
				break;
			}
			case 42: // randnum
			{
				String arg1s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase().trim();
				int arg1=0;
				if(CMath.isMathExpression(arg1s.trim()))
					arg1=CMath.s_parseIntExpression(arg1s.trim());
				else
					arg1=CMParms.parse(arg1s.trim()).size();
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1)).trim();
				int arg3=0;
				if(CMath.isMathExpression(arg3s.trim()))
					arg3=CMath.s_parseIntExpression(arg3s.trim());
				else
					arg3=CMParms.parse(arg3s.trim()).size();
				arg3=CMLib.dice().roll(1,arg3,0);
				returnable=simpleEval(scripted,""+arg1,""+arg3,arg2,"RANDNUM");
				break;
			}
            case 71: // rand0num
            {
				String arg1s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0)).toUpperCase().trim();
				int arg1=0;
				if(CMath.isMathExpression(arg1s))
					arg1=CMath.s_parseIntExpression(arg1s);
				else
					arg1=CMParms.parse(arg1s).size();
				String arg2=CMParms.getCleanBit(evaluable.substring(y+1,z),1);
				String arg3s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),1)).trim();
				int arg3=0;
				if(CMath.isMathExpression(arg3s))
					arg3=CMath.s_parseIntExpression(arg3s);
				else
					arg3=CMParms.parse(arg3s).size();
				arg3=CMLib.dice().roll(1,arg3,-1);
                returnable=simpleEval(scripted,""+arg1,""+arg3,arg2,"RAND0NUM");
                break;
            }
			case 53: // incontainer
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E2=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					returnable=false;
				else
				if(E instanceof MOB)
				{
					if(arg2.length()==0)
						returnable=(((MOB)E).riding()==null);
					else
					if(E2!=null)
						returnable=(((MOB)E).riding()==E2);
					else
						returnable=false;
				}
				else
				if(E instanceof Item)
				{
					if(arg2.length()==0)
						returnable=(((Item)E).container()==null);
					else
					if(E2!=null)
						returnable=(((Item)E).container()==E2);
					else
						returnable=false;
				}
				else
					returnable=false;
				break;
			}
			default:
				scriptableError(scripted,"Unknown Eval",preFab,evaluable);
				return returnable;
			}
			if((z>=0)&&(z<=evaluable.length()))
			{
				evaluable=evaluable.substring(z+1).trim();
				uevaluable=uevaluable.substring(z+1).trim();
			}
			switch(joined)
			{
			case 1: returnable=lastreturnable&&returnable; break;
			case 2: returnable=lastreturnable||returnable; break;
			case 4: returnable=!returnable; break;
			case 5: returnable=lastreturnable&&(!returnable); break;
			case 6: returnable=lastreturnable||(!returnable); break;
			default: break;
			}
			joined=0;
		}
		}
		return returnable;
	}

	public String functify(Environmental scripted,
						   MOB source,
						   Environmental target,
						   MOB monster,
						   Item primaryItem,
						   Item secondaryItem,
						   String msg,
						   Object[] tmp,
						   String evaluable)
	{
		String uevaluable=evaluable.toUpperCase().trim();
		StringBuffer results = new StringBuffer("");
		while(evaluable.length()>0)
		{
			int y=evaluable.indexOf("(");
			int z=evaluable.indexOf(")");
			String preFab=(y>=0)?uevaluable.substring(0,y).trim():"";
			Integer funcCode=(Integer)funcH.get(preFab);
			if(funcCode==null) funcCode=new Integer(0);
			if(y==0)
			{
				int depth=0;
				int i=0;
				while((++i)<evaluable.length())
				{
					char c=evaluable.charAt(i);
					if((c==')')&&(depth==0))
					{
						String expr=evaluable.substring(1,i);
						evaluable=evaluable.substring(i+1);
						uevaluable=uevaluable.substring(i+1);
						results.append(functify(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,expr));
						break;
					}
					else
					if(c=='(') depth++;
					else
					if(c==')') depth--;
				}
				z=evaluable.indexOf(")");
			}
			else
			if((y<0)||(z<y))
			{
				scriptableError(scripted,"()","Syntax",evaluable);
				break;
			}
			else
			{
	        tickStatus=Tickable.STATUS_MISC2+funcCode.intValue();
			switch(funcCode.intValue())
			{
			case 1: // rand
			{
				results.append(CMLib.dice().rollPercentage());
				break;
			}
			case 2: // has
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				Vector choices=new Vector();
				if(E==null)
					choices=new Vector();
				else
				if(E instanceof MOB)
				{
					for(int i=0;i<((MOB)E).inventorySize();i++)
					{
						Item I=((MOB)E).fetchInventory(i);
						if((I!=null)&&(I.amWearingAt(Item.IN_INVENTORY))&&(I.container()==null))
							choices.addElement(I);
					}
				}
				else
				if(E instanceof Item)
				{
					choices.addElement(E);
					if(E instanceof Container)
						choices=((Container)E).getContents();
				}
				else
				if(E instanceof Room)
				{
					for(int i=0;i<((Room)E).numItems();i++)
					{
						Item I=((Room)E).fetchItem(i);
						if((I!=null)&&(I.container()==null))
							choices.addElement(I);
					}
				}
				if(choices.size()>0)
					results.append(((Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).name());
				break;
			}
            case 74: // hasnum
            {
                String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                String item=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((item.length()==0)||(E==null))
                    scriptableError(scripted,"HASNUM","Syntax",evaluable);
                else
                {
                    Item I=null;
                    int num=0;
                    if(E instanceof MOB)
                    {
                        MOB M=(MOB)E;
                        for(int i=0;i<M.inventorySize();i++)
                        {
                            I=M.fetchInventory(i);
                            if(I==null) break;
                            if((item.equalsIgnoreCase("all"))
                            ||(CMLib.english().containsString(I.Name(),item)))
                                num++;
                        }
                        results.append(""+num);
                    }
                    else
                    if(E instanceof Item)
                    {
                        num=CMLib.english().containsString(E.name(),item)?1:0;
                        results.append(""+num);
                    }
                    else
                    if(E instanceof Room)
                    {
                        Room R=(Room)E;
                        for(int i=0;i<R.numItems();i++)
                        {
                            I=R.fetchItem(i);
                            if(I==null) break;
                            if((item.equalsIgnoreCase("all"))
                            ||(CMLib.english().containsString(I.Name(),item)))
                                num++;
                        }
                        results.append(""+num);
                    }
                }
                break;
            }
			case 3: // worn
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				Vector choices=new Vector();
				if(E==null)
					choices=new Vector();
				else
				if(E instanceof MOB)
				{
					for(int i=0;i<((MOB)E).inventorySize();i++)
					{
						Item I=((MOB)E).fetchInventory(i);
						if((I!=null)&&(!I.amWearingAt(Item.IN_INVENTORY))&&(I.container()==null))
							choices.addElement(I);
					}
				}
				else
				if((E instanceof Item)&&(!(((Item)E).amWearingAt(Item.IN_INVENTORY))))
				{
					choices.addElement(E);
					if(E instanceof Container)
						choices=((Container)E).getContents();
				}
				if(choices.size()>0)
					results.append(((Item)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).name());
				break;
			}
			case 4: // isnpc
			case 5: // ispc
				results.append("[unimplemented function]");
				break;
			case 6: // isgood
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB)))
				{
				    Faction.FactionRange FR=CMLib.factions().getRange(CMLib.factions().AlignID(),((MOB)E).fetchFaction(CMLib.factions().AlignID()));
				    if(FR!=null)
						results.append(FR.name());
				    else
				        results.append(((MOB)E).fetchFaction(CMLib.factions().AlignID()));
				}
				break;
			}
			case 8: // isevil
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB)))
					results.append(CMStrings.capitalizeAndLower(CMLib.flags().getAlignmentName(E)).toLowerCase());
				break;
			}
			case 9: // isneutral
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB)))
					results.append(((MOB)E).fetchFaction(CMLib.factions().AlignID()));
				break;
			}
			case 11: // isimmort
				results.append("[unimplemented function]");
				break;
			case 54: // isalive
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
					results.append(((MOB)E).healthText(null));
				else
					results.append(E.name()+" is dead.");
				break;
			}
			case 58: // isable
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(!((MOB)E).amDead()))
				{
					ExpertiseLibrary X=(ExpertiseLibrary)CMLib.expertises().findDefinition(arg2,true);
					if(X!=null)
					{
						String s=((MOB)E).fetchExpertise(X.ID());
						if(s!=null) results.append(s);
					}
					else
					{
						Ability A=((MOB)E).findAbility(arg2);
						if(A!=null) results.append(""+A.proficiency());
					}
				}
				break;
			}
			case 59: // isopen
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				int dir=Directions.getGoodDirectionCode(arg1);
				boolean returnable=false;
				if(dir<0)
				{
					Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						returnable=((Container)E).isOpen();
					else
					if((E!=null)&&(E instanceof Exit))
					    returnable=((Exit)E).isOpen();
				}
				else
				if(lastKnownLocation!=null)
				{
					Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null) returnable= E.isOpen();
				}
				results.append(""+returnable);
				break;
			}
			case 60: // islocked
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				int dir=Directions.getGoodDirectionCode(arg1);
				if(dir<0)
				{
					Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if((E!=null)&&(E instanceof Container))
						results.append(((Container)E).keyName());
					else
					if((E!=null)&&(E instanceof Exit))
					    results.append(((Exit)E).keyName());
				}
				else
				if(lastKnownLocation!=null)
				{
					Exit E=lastKnownLocation.getExitInDir(dir);
					if(E!=null)
						results.append(E.keyName());
				}
				break;
			}
			case 62: // callfunc
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				String found=null;
				boolean validFunc=false;
				Vector scripts=getScripts();
				for(int v=0;v<scripts.size();v++)
				{
					Vector script2=(Vector)scripts.elementAt(v);
					if(script2.size()<1) continue;
					String trigger=((String)script2.elementAt(0)).toUpperCase().trim();
					if(getTriggerCode(trigger)==17)
					{
						String fnamed=CMParms.getCleanBit(trigger,1);
						if(fnamed.equalsIgnoreCase(arg1))
						{
							validFunc=true;
							found=
							execute(scripted,
									source,
									target,
									monster,
									primaryItem,
									secondaryItem,
									script2,
									varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,arg2),
									tmp);
							if(found==null) found="";
							break;
						}
					}
				}
				if(!validFunc)
					scriptableError(scripted,"CALLFUNC","Unknown","Function: "+arg1);
				else
					results.append(found);
				break;
			}
			case 61: // strin
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Vector V=CMParms.parse(arg1.toUpperCase());
				results.append(V.indexOf(arg2.toUpperCase()));
				break;
			}
			case 55: // ispkill
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E==null)||(!(E instanceof MOB)))
					results.append("false");
				else
				if(CMath.bset(((MOB)E).getBitmap(),MOB.ATT_PLAYERKILL))
					results.append("true");
				else
					results.append("false");
				break;
			}
			case 10: // isfight
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&((E instanceof MOB))&&(((MOB)E).isInCombat()))
					results.append(((MOB)E).getVictim().name());
				break;
			}
			case 12: // ischarmed
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					Vector V=CMLib.flags().flaggedAffects(E,Ability.FLAG_CHARMING);
					for(int v=0;v<V.size();v++)
						results.append((((Ability)V.elementAt(v)).name())+" ");
				}
				break;
			}
			case 15: // isfollow
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB)&&(((MOB)E).amFollowing()!=null)
				&&(((MOB)E).amFollowing().location()==lastKnownLocation))
					results.append(((MOB)E).amFollowing().name());
				break;
			}
            case 73: // isservant
            {
                String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
                Environmental E=getArgumentMOB(arg1,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(((MOB)E).getLiegeID()!=null)&&(((MOB)E).getLiegeID().length()>0))
                    results.append(((MOB)E).getLiegeID());
                break;
            }
			case 56: // name
			case 7: // isname
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)	results.append(E.name());
				break;
			}
            case 75: // currency
            {
                String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E!=null)results.append(CMLib.beanCounter().getCurrency(E));
                break;
            }
			case 14: // affected
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
				{
					if(((MOB)E).numAllEffects()>0)
						results.append(E.fetchEffect(CMLib.dice().roll(1,((MOB)E).numAllEffects(),-1)).name());
				}
				else
				if(E.numEffects()>0)
					results.append(E.fetchEffect(CMLib.dice().roll(1,E.numEffects(),-1)).name());
				break;
			}
			case 69: // isbehave
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				for(int i=0;i<E.numBehaviors();i++)
					results.append(E.fetchBehavior(i).ID()+" ");
				break;
			}
            case 70: // ipaddress
            {
                String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if((E!=null)&&(E instanceof MOB)&&(!((MOB)E).isMonster()))
                    results.append(((MOB)E).session().getAddress());
                break;
            }
			case 28: // questwinner
			case 29: // questmob
			case 31: // isquestmobalive
				results.append("[unimplemented function]");
				break;
			case 32: // nummobsinarea
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				int num=0;
                Vector MASK=null;
                if((arg1.toUpperCase().startsWith("MASK")&&(arg1.substring(4).trim().startsWith("="))))
                { 
                    arg1=arg1.substring(4).trim(); 
                    arg1=arg1.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg1);
                }
				for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB M=R.fetchInhabitant(m);
                        if(M==null) continue;
                        if(MASK!=null)
                        {
                            if(CMLib.masking().maskCheck(MASK,M))
                                num++;
                        }
                        else
                        if(CMLib.english().containsString(M.name(),arg1))
                            num++;
					}
				}
				results.append(num);
				break;
			}
			case 33: // nummobs
			{
				int num=0;
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
                Vector MASK=null;
                if((arg1.toUpperCase().startsWith("MASK")&&(arg1.substring(4).trim().startsWith("="))))
                { 
                    arg1=arg1.substring(4).trim(); 
                    arg1=arg1.substring(1).trim();
                    MASK=CMLib.masking().maskCompile(arg1);
                }
				try
				{
					for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB M=R.fetchInhabitant(m);
                            if(M==null) continue;
                            if(MASK!=null)
                            {
                                if(CMLib.masking().maskCheck(MASK,M))
                                    num++;
                            }
                            else
							if(CMLib.english().containsString(M.name(),arg1))
								num++;
						}
					}
			    }catch(NoSuchElementException nse){}
				results.append(num);
				break;
			}
			case 34: // numracesinarea
			{
				int num=0;
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				Room R=null;
				MOB M=null;
				for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
				{
					R=(Room)e.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						M=R.fetchInhabitant(m);
						if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
							num++;
					}
				}
				results.append(num);
				break;
			}
			case 35: // numraces
			{
				int num=0;
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				Room R=null;
				MOB M=null;
				try
				{
					for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						R=(Room)e.nextElement();
						for(int m=0;m<R.numInhabitants();m++)
						{
							M=R.fetchInhabitant(m);
							if((M!=null)&&(M.charStats().raceName().equalsIgnoreCase(arg1)))
								num++;
						}
					}
			    }catch(NoSuchElementException nse){}
				results.append(num);
				break;
			}
			case 30: // questobj
				results.append("[unimplemented function]");
				break;
			case 16: // hitprcnt
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					double hitPctD=CMath.div(((MOB)E).curState().getHitPoints(),((MOB)E).maxState().getHitPoints());
					int val1=(int)Math.round(hitPctD*100.0);
					results.append(val1);
				}
				break;
			}
			case 50: // isseason
			{
				if(monster.location()!=null)
					results.append(TimeClock.SEASON_DESCS[monster.location().getArea().getTimeObj().getSeasonCode()]);
				break;
			}
			case 51: // isweather
			{
				if(monster.location()!=null)
					results.append(Climate.WEATHER_DESCS[monster.location().getArea().getClimateObj().weatherType(monster.location())]);
				break;
			}
			case 57: // ismoon
			{
				if(monster.location()!=null)
					results.append(TimeClock.PHASE_DESC[monster.location().getArea().getTimeObj().getMoonPhase()]);
				break;
			}
			case 38: // istime
			{
				if(lastKnownLocation!=null)
					results.append(TimeClock.TOD_DESC[lastKnownLocation.getArea().getTimeObj().getTODCode()].toLowerCase());
				break;
			}
			case 39: // isday
			{
				if(lastKnownLocation!=null)
					results.append(""+lastKnownLocation.getArea().getTimeObj().getDayOfMonth());
				break;
			}
			case 43: // roommob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				Environmental which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 44: // roomitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				Environmental which=null;
				int ct=1;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==CMath.s_int(arg1.trim()))
						{ which=I; break;}
						ct++;
					}
				}
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 45: // nummobsroom
			{
                int num=0;
                if(lastKnownLocation!=null)
                {
                    num=lastKnownLocation.numInhabitants();
                    String name=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
                    if((name.length()>0)&&(!name.equalsIgnoreCase("*")))
                    {
                        num=0;
                        Vector MASK=null;
                        if((name.toUpperCase().startsWith("MASK")&&(name.substring(4).trim().startsWith("="))))
                        { 
                            name=name.substring(4).trim(); 
                            name=name.substring(1).trim();
                            MASK=CMLib.masking().maskCompile(name);
                        }
                        for(int i=0;i<lastKnownLocation.numInhabitants();i++)
                        {
                            MOB M=lastKnownLocation.fetchInhabitant(i);
                            if(M==null) continue;
                            if(MASK!=null)
                            {
                                if(CMLib.masking().maskCheck(MASK,M))
                                    num++;
                            }
                            else
                            if(CMLib.english().containsString(M.Name(),name)
                            ||CMLib.english().containsString(M.displayText(),name))
                                num++;
                        }
                    }
                }
				results.append(""+num);
				break;
			}
			case 63: // numpcsroom
			{
				if(lastKnownLocation!=null)
					results.append(""+lastKnownLocation.numPCInhabitants());
				break;
			}
			case 79: // numpcsarea
			{
				if(lastKnownLocation!=null)
				{
					int num=0;
					for(int s=0;s<CMLib.sessions().size();s++)
					{
						Session S=CMLib.sessions().elementAt(s);
						if((S!=null)&&(S.mob()!=null)&&(S.mob().location()!=null)&&(S.mob().location().getArea()==lastKnownLocation.getArea()))
							num++;
					}
					results.append(""+num);
				}
				break;
			}
            case 77: // explored
            {
                String whom=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
                String where=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),1));
                Environmental E=getArgumentItem(whom,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E instanceof MOB)
                {
                    Area A=null;
                    if(!where.equalsIgnoreCase("world"))
                    {
                        Environmental E2=getArgumentItem(where,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                        if(E2 instanceof Area)
                            A=(Area)E2;
                        else
                            A=CMLib.map().getArea(where);
                    }
                    if((lastKnownLocation!=null)
                    &&((A!=null)||(where.equalsIgnoreCase("world"))))
                    {
                        int pct=0;
                        MOB M=(MOB)E;
                        if(M.playerStats()!=null)
                            pct=M.playerStats().percentVisited(M,A);
                        results.append(""+pct);
                    }
                }
                break;
            }
            case 72: // faction
            {
                String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                String arg2=CMParms.getPastBit(evaluable.substring(y+1,z),0);
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                Faction F=CMLib.factions().getFaction(arg2);
                if(F==null)
                    scriptableError(scripted,"FACTION","Unknown Faction",arg1);
                else
                if((E!=null)&&(E instanceof MOB)&&(((MOB)E).hasFaction(F.factionID())))
                {
                    int value=((MOB)E).fetchFaction(F.factionID());
                    Faction.FactionRange FR=CMLib.factions().getRange(F.factionID(),value);
                    if(FR!=null)
                        results.append(FR.name());
                }
                break;
            }
			case 46: // numitemsroom
			{
				int ct=0;
				if(lastKnownLocation!=null)
				for(int i=0;i<lastKnownLocation.numItems();i++)
				{
					Item I=lastKnownLocation.fetchItem(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				results.append(""+ct);
				break;
			}
			case 47: //mobitem
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(evaluable.substring(y+1,z),0));
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				MOB M=null;
				if(lastKnownLocation!=null)
					M=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
				Item which=null;
				int ct=1;
				if(M!=null)
				for(int i=0;i<M.inventorySize();i++)
				{
					Item I=M.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
					{
						if(ct==CMath.s_int(arg2.trim()))
						{ which=I; break;}
						ct++;
					}
				}
				if(which!=null)
					results.append(which.name());
				break;
			}
			case 48: // numitemsmob
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z)));
				MOB which=null;
				if(lastKnownLocation!=null)
					which=lastKnownLocation.fetchInhabitant(CMath.s_int(arg1.trim())-1);
				int ct=1;
				if(which!=null)
				for(int i=0;i<which.inventorySize();i++)
				{
					Item I=which.fetchInventory(i);
					if((I!=null)&&(I.container()==null))
						ct++;
				}
				results.append(""+ct);
				break;
			}
			case 36: // ishere
			{
				if(lastKnownLocation!=null)
					results.append(lastKnownLocation.getArea().name());
				break;
			}
			case 17: // inroom
			{
				if(lastKnownLocation!=null)
					results.append(CMLib.map().getExtendedRoomID(lastKnownLocation));
				break;
			}
			case 37: // inlocale
			{
				if(lastKnownLocation!=null)
					results.append(lastKnownLocation.name());
				break;
			}
			case 18: // sex
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().genderName());
				break;
			}
			case 13: // stat
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
                    String val=getStatValue(E,arg2);
                    if(val==null)
					{
						scriptableError(scripted,"STAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}

					results.append(val);
					break;
				}
				break;
			}
			case 52: // gstat
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
                    String val=getGStatValue(E,arg2);
                    if(val==null)
					{
						scriptableError(scripted,"GSTAT","Syntax","Unknown stat: "+arg2+" for "+E.name());
						break;
					}

					results.append(val);
					break;
				}
				break;
			}
			case 19: // position
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex="STANDING";
					if(CMLib.flags().isSleeping(E))
						sex="SLEEPING";
					else
					if(CMLib.flags().isSitting(E))
						sex="SITTING";
					results.append(sex);
					break;
				}
				break;
			}
			case 20: // level
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
					results.append(E.envStats().level());
				break;
			}
			case 80: // questpoints
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
					results.append(((MOB)E).getQuestPoint());
				break;
			}
			case 83: // qvar
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				if((arg1.length()!=0)&&(arg2.length()!=0))
				{
					Quest Q=arg1.equals("*")?defaultQuest:CMLib.quests().fetchQuest(arg1);
					if(Q!=null) results.append(Q.getStat(arg2));
				}
				break;
			}
            case 84: // math
            {
                String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
                results.append(""+Math.round(CMath.s_parseMathExpression(arg1)));
            }
			case 81: // trains
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
					results.append(((MOB)E).getTrains());
				break;
			}
			case 82: // pracs
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
					results.append(((MOB)E).getPractices());
				break;
			}
			case 68: // clandata
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String clanID=null;
				if((E!=null)&&(E instanceof MOB))
				    clanID=((MOB)E).getClanID();
				else
					clanID=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,arg1);
				Clan C=CMLib.clans().findClan(clanID);
				if(C!=null)
				{
				    int whichVar=-1;
				    for(int i=0;i<clanVars.length;i++)
				        if(arg2.equalsIgnoreCase(clanVars[i]))
				        { whichVar=i; break;}
				    String whichVal="";
				    switch(whichVar)
				    {
				    case 0: whichVal=C.getAcceptanceSettings(); break;
				    case 1: whichVal=C.getDetail(monster); break;
				    case 2: whichVal=C.getDonation(); break;
				    case 3: whichVal=""+C.getExp(); break;
				    case 4: whichVal=Clan.GVT_DESCS[C.getGovernment()]; break;
				    case 5: whichVal=C.getMorgue(); break;
				    case 6: whichVal=C.getPolitics(); break;
				    case 7: whichVal=C.getPremise(); break;
				    case 8: whichVal=C.getRecall(); break;
				    case 9: whichVal=""+C.getSize(); break; // size
				    case 10: whichVal=Clan.CLANSTATUS_DESC[C.getStatus()]; break;
				    case 11: whichVal=""+C.getTaxes(); break;
				    case 12: whichVal=""+C.getTrophies(); break;
				    case 13: whichVal=""+C.getType(); break; // type
				    case 14: {
			        	 Vector areas=C.getControlledAreas();
			        	 StringBuffer list=new StringBuffer("");
			        	 for(int i=0;i<areas.size();i++)
			        	     list.append("\""+((Environmental)areas.elementAt(i)).name()+"\" ");
			        	 whichVal=list.toString().trim();
			    		 break; // areas
				    }
				    case 15: {
				        	 DVector members=C.getMemberList();
				        	 StringBuffer list=new StringBuffer("");
				        	 for(int i=0;i<members.size();i++)
				        	     list.append("\""+((String)members.elementAt(i,1))+"\" ");
				        	 whichVal=list.toString().trim();
				    		 break; // memberlist
				    }
				    case 16: MOB M=C.getResponsibleMember();
				    		 if(M!=null) whichVal=M.Name();
				    		 break; // topmember
				    default:
						scriptableError(scripted,"CLANDATA","RunTime",arg2+" is not a valid clan variable.");
				    	break;
				    }
				    results.append(whichVal);
				}
				break;
			}
			case 67: // hastitle
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(evaluable.substring(y+1,z),0));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((arg2.length()>0)&&(E instanceof MOB)&&(((MOB)E).playerStats()!=null))
				{
				    MOB M=(MOB)E;
				    results.append(M.playerStats().getActiveTitle());
				}
				break;
			}
			case 66: // clanrank
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).getClanRole()+"");
				break;
			}
			case 21: // class
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().displayClassName());
				break;
			}
			case 64: // deity
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex=((MOB)E).getWorshipCharID();
					results.append(sex);
				}
				break;
			}
			case 65: // clan
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
				{
					String sex=((MOB)E).getClanID();
					results.append(sex);
				}
				break;
			}
			case 22: // baseclass
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().getCurrentClass().baseClass());
				break;
			}
			case 23: // race
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().raceName());
				break;
			}
			case 24: //racecat
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((E!=null)&&(E instanceof MOB))
					results.append(((MOB)E).charStats().getMyRace().racialCategory());
				break;
			}
			case 25: // goldamt
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					results.append(false);
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,CMLib.beanCounter().getCurrency(scripted)));
					else
					if(E instanceof Coins)
						val1=(int)Math.round(((Coins)E).getTotalValue());
					else
					if(E instanceof Item)
						val1=((Item)E).value();
					else
					{
						scriptableError(scripted,"GOLDAMT","Syntax",evaluable);
						return results.toString();
					}
					results.append(val1);
				}
				break;
			}
			case 89: // exp
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E==null)
					results.append(false);
				else
				{
					int val1=0;
					if(E instanceof MOB)
						val1=((MOB)E).getExperience();
					results.append(val1);
				}
				break;
			}
            case 76: // value
            {
                String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
                String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0);
                if(!CMLib.beanCounter().getAllCurrencies().contains(arg2.toUpperCase()))
                {
                    scriptableError(scripted,"VALUE","Syntax",arg2+" is not a valid designated currency.");
                    return results.toString();
                }
                Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(E==null)
                    results.append(false);
                else
                {
                    int val1=0;
                    if(E instanceof MOB)
                        val1=(int)Math.round(CMLib.beanCounter().getTotalAbsoluteValue((MOB)E,arg2));
                    else
                    if(E instanceof Coins)
                    {
                        if(((Coins)E).getCurrency().equalsIgnoreCase(arg2))
                            val1=(int)Math.round(((Coins)E).getTotalValue());
                    }
                    else
                    if(E instanceof Item)
                        val1=((Item)E).value();
                    else
                    {
                        scriptableError(scripted,"GOLDAMT","Syntax",evaluable);
                        return results.toString();
                    }
                    results.append(val1);
                }
                break;
            }
			case 26: // objtype
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					String sex=CMClass.classID(E).toLowerCase();
					results.append(sex);
				}
				break;
			}
			case 53: // incontainer
			{
				String arg1=CMParms.cleanBit(evaluable.substring(y+1,z));
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E!=null)
				{
					if(E instanceof MOB)
					{
						if(((MOB)E).riding()!=null)
							results.append(((MOB)E).riding().Name());
					}
					else
					if(E instanceof Item)
					{
						if(((Item)E).riding()!=null)
							results.append(((Item)E).container().Name());
					}
				}
				break;
			}
			case 27: // var
			{
				String arg1=CMParms.getCleanBit(evaluable.substring(y+1,z),0);
				String arg2=CMParms.getPastBitClean(evaluable.substring(y+1,z),0).toUpperCase();
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String val=getVar(E,arg1,arg2,source,target,monster,primaryItem,secondaryItem,msg,tmp);
				results.append(val);
				break;
			}
			case 41: // eval
				results.append("[unimplemented function]");
				break;
			case 40: // number
			{
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z))).trim();
				boolean isnumber=(val.length()>0);
				for(int i=0;i<val.length();i++)
					if(!Character.isDigit(val.charAt(i)))
					{ isnumber=false; break;}
				if(isnumber)
					results.append(CMath.s_long(val.trim()));
				break;
			}
			case 42: // randnum
			{
				String arg1String=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z))).toUpperCase();
				int arg1=0;
				if(CMath.isMathExpression(arg1String))
					arg1=CMath.s_parseIntExpression(arg1String.trim());
				else
					arg1=CMParms.parse(arg1String.trim()).size();
				results.append(CMLib.dice().roll(1,arg1,0));
				break;
			}
            case 71: // rand0num
            {
                String arg1String=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.cleanBit(evaluable.substring(y+1,z))).toUpperCase();
				int arg1=0;
				if(CMath.isMathExpression(arg1String))
					arg1=CMath.s_parseIntExpression(arg1String.trim());
				else
					arg1=CMParms.parse(arg1String.trim()).size();
                results.append(CMLib.dice().roll(1,arg1,-1));
                break;
            }
			default:
				scriptableError(scripted,"Unknown Val",preFab,evaluable);
				return results.toString();
			}
			}
			if((z>=0)&&(z<=evaluable.length()))
			{
				evaluable=evaluable.substring(z+1).trim();
				uevaluable=uevaluable.substring(z+1).trim();
			}
		}
		return results.toString();
	}

	protected MOB getFirstPC(MOB monster, MOB randMOB, Room room)
	{
		if((randMOB!=null)&&(randMOB!=monster))
			return randMOB;
		MOB M=null;
		if(room!=null)
			for(int p=0;p<room.numInhabitants();p++)
			{
				M=room.fetchInhabitant(p);
				if((!M.isMonster())&&(M!=monster))
				{
                    HashSet seen=new HashSet();
					while((M.amFollowing()!=null)&&(!M.amFollowing().isMonster())&&(!seen.contains(M)))
                    {
                        seen.add(M);
						M=M.amFollowing();
                    }
					return M;
				}
			}
		return null;
	}
	protected MOB getFirstAnyone(MOB monster, MOB randMOB, Room room)
	{
		if((randMOB!=null)&&(randMOB!=monster))
			return randMOB;
		MOB M=null;
		if(room!=null)
			for(int p=0;p<room.numInhabitants();p++)
			{
				M=room.fetchInhabitant(p);
				if(M!=monster)
				{
                    HashSet seen=new HashSet();
                    while((M.amFollowing()!=null)&&(!M.amFollowing().isMonster())&&(!seen.contains(M)))
                    {
                        seen.add(M);
                        M=M.amFollowing();
                    }
					return M;
				}
			}
		return null;
	}

	public String execute(Environmental scripted,
						  MOB source,
						  Environmental target,
						  MOB monster,
						  Item primaryItem,
						  Item secondaryItem,
						  Vector script,
					  	  String msg,
					  	  Object[] tmp)
	{
        tickStatus=Tickable.STATUS_START;
        for(int si=1;si<script.size();si++)
		{
			String s=((String)script.elementAt(si)).trim();
			String cmd=CMParms.getCleanBit(s,0).toUpperCase();
			Integer methCode=(Integer)methH.get(cmd);
			if((methCode==null)&&(cmd.startsWith("MP")))
			    for(int i=0;i<methods.length;i++)
			        if(methods[i].startsWith(cmd))
			            methCode=new Integer(i);
			if(methCode==null) methCode=new Integer(0);
		    tickStatus=Tickable.STATUS_MISC3+methCode.intValue();
			if(cmd.length()==0)
				continue;
			switch(methCode.intValue())
			{
            case 57: // <SCRIPT>
            {
                StringBuffer jscript=new StringBuffer("");
                while((++si)<script.size())
                {
                    s=((String)script.elementAt(si)).trim();
                    cmd=CMParms.getCleanBit(s,0).toUpperCase();
                    if(cmd.equalsIgnoreCase("</SCRIPT>"))
                        break;
                    jscript.append(s+"\n");
                }
                if(CMSecurity.isApprovedJScript(jscript))
                {
                    Context cx = Context.enter();
                    try
                    {
                        JScriptEvent scope = new JScriptEvent(scripted,source,target,monster,primaryItem,secondaryItem,msg);
                        cx.initStandardObjects(scope);
                        String[] names = { "host", "source", "target", "monster", "item", "item2", "message" ,"getVar", "setVar", "toJavaString"};
                        scope.defineFunctionProperties(names, JScriptEvent.class,
                                                       ScriptableObject.DONTENUM);
                        cx.evaluateString(scope, jscript.toString(),"<cmd>", 1, null);
                    }
                    catch(Exception e)
                    {
                        Log.errOut("Scriptable",scripted.name()+"/"+CMLib.map().getExtendedRoomID(lastKnownLocation)+"/JSCRIPT Error: "+e.getMessage());
                    }
                    Context.exit();
                }
                else
                if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)==1)
                {
                    if(lastKnownLocation!=null)
                        lastKnownLocation.showHappens(CMMsg.MSG_OK_ACTION,"A Javascript was not authorized.  Contact an Admin to use MODIFY JSCRIPT to authorize this script.");
                }
                break;
            }
			case 19: // if
			{
				String conditionStr=(s.substring(2).trim());
				boolean condition=eval(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,conditionStr);
				Vector V=new Vector();
				V.addElement("");
				int depth=0;
				boolean foundendif=false;
                boolean ignoreUntilEndScript=false;
				si++;
				while(si<script.size())
				{
					s=((String)script.elementAt(si)).trim();
					cmd=CMParms.getCleanBit(s,0).toUpperCase();
                    if(cmd.equals("<SCRIPT>"))
                        ignoreUntilEndScript=true;
                    else
                    if(cmd.equals("</SCRIPT>"))
                        ignoreUntilEndScript=false;
                    else
                    if(ignoreUntilEndScript){}
                    else
					if(cmd.equals("ENDIF")&&(depth==0))
					{
						foundendif=true;
						break;
					}
					else
					if(cmd.equals("ELSE")&&(depth==0))
					{
						condition=!condition;
						if(s.substring(4).trim().length()>0)
						{
							script.setElementAt("ELSE",si);
							script.insertElementAt(s.substring(4).trim(),si+1);
						}
					}
					else
					{
						if(condition)
							V.addElement(s);
						if(cmd.equals("IF"))
							depth++;
						else
						if(cmd.equals("ENDIF"))
							depth--;
					}
					si++;
				}
				if(!foundendif)
				{
					scriptableError(scripted,"IF","Syntax"," Without ENDIF!");
                    tickStatus=Tickable.STATUS_END;
					return null;
				}
				if(V.size()>1)
				{
					//source.tell("Starting "+conditionStr);
					//for(int v=0;v<V.size();v++)
					//	source.tell("Statement "+((String)V.elementAt(v)));
					String response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
					if(response!=null) 
                    {
                        tickStatus=Tickable.STATUS_END;
                        return response;
                    }
					//source.tell("Stopping "+conditionStr);
				}
				break;
			}
			case 62: // for x = 1 to 100
			{
				if(CMParms.numBits(s)<6)
				{
					scriptableError(scripted,"FOR","Syntax","5 parms required!");
                    tickStatus=Tickable.STATUS_END;
					return null;
				}
				String varStr=CMParms.getBit(s,1);
				if((varStr.length()!=2)||(varStr.charAt(0)!='$')||(!Character.isDigit(varStr.charAt(1))))
				{
					scriptableError(scripted,"FOR","Syntax","'"+varStr+"' is not a tmp var $1, $2..");
                    tickStatus=Tickable.STATUS_END;
					return null;
				}
				int whichVar=CMath.s_int(Character.toString(varStr.charAt(1)));
				if((tmp[whichVar] instanceof String)
				&&(((String)tmp[whichVar]).length()>1)
				&&(((String)tmp[whichVar]).startsWith(" "))
				&&(CMath.isInteger(((String)tmp[whichVar]).trim())))
				{
					scriptableError(scripted,"FOR","Syntax","'"+whichVar+"' is already in use! Use a different one!");
                    tickStatus=Tickable.STATUS_END;
					return null;
				}
				if((!CMParms.getBit(s,2).equals("="))&&(!CMParms.getBit(s,4).equalsIgnoreCase("to")))
				{
					scriptableError(scripted,"FOR","Syntax","'"+s+"' is illegal for syntax!");
                    tickStatus=Tickable.STATUS_END;
					return null;
				}
				String from=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,3).trim()).trim();
				String to=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,4).trim()).trim();
				if((!CMath.isInteger(from))||(!CMath.isInteger(to)))
				{
					scriptableError(scripted,"FOR","Syntax","'"+from+"-"+to+"' is illegal range!");
                    tickStatus=Tickable.STATUS_END;
					return null;
				}
				Vector V=new Vector();
				V.addElement("");
				int depth=0;
				boolean foundnext=false;
                boolean ignoreUntilEndScript=false;
				si++;
				while(si<script.size())
				{
					s=((String)script.elementAt(si)).trim();
					cmd=CMParms.getCleanBit(s,0).toUpperCase();
                    if(cmd.equals("<SCRIPT>"))
                        ignoreUntilEndScript=true;
                    else
                    if(cmd.equals("</SCRIPT>"))
                        ignoreUntilEndScript=false;
                    else
                    if(ignoreUntilEndScript){}
                    else
					if(cmd.equals("NEXT")&&(depth==0))
					{
						foundnext=true;
						break;
					}
					else
					{
						V.addElement(s);
						if(cmd.equals("FOR"))
							depth++;
						else
						if(cmd.equals("NEXT"))
							depth--;
					}
					si++;
				}
				if(!foundnext)
				{
					scriptableError(scripted,"FOR","Syntax"," Without NEXT!");
                    tickStatus=Tickable.STATUS_END;
					return null;
				}
				if(V.size()>1)
				{
					//source.tell("Starting "+conditionStr);
					//for(int v=0;v<V.size();v++)
					//	source.tell("Statement "+((String)V.elementAt(v)));
					int toInt=CMath.s_int(to);
					int fromInt=CMath.s_int(from);
					int increment=(toInt>=fromInt)?1:-1;
					String response=null;
					for(int forLoop=fromInt;forLoop!=toInt;forLoop+=increment)
					{
						tmp[whichVar]=" "+forLoop;
						response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
						if(response!=null) break;
					}
					tmp[whichVar]=" "+toInt;
					response=execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
					if(response!=null) 
                    {
                        tickStatus=Tickable.STATUS_END;
                        return response;
                    }
					tmp[whichVar]=null;
					//source.tell("Stopping "+conditionStr);
				}
				break;
			}
			case 50: // break;
                tickStatus=Tickable.STATUS_END;
				return null;
			case 1: // mpasound
			{
				String echo=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s.substring(8).trim());
				//lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,echo);
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R2=lastKnownLocation.getRoomInDir(d);
					Exit E2=lastKnownLocation.getExitInDir(d);
					if((R2!=null)&&(E2!=null)&&(E2.isOpen()))
						R2.showOthers(monster,null,null,CMMsg.MSG_OK_ACTION,echo);
				}
				break;
			}
			case 4: // mpjunk
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s.substring(6).trim());
				if(s.equalsIgnoreCase("all"))
				{
					while(monster.inventorySize()>0)
					{
						Item I=monster.fetchInventory(0);
						if(I!=null) I.destroy();
					}
				}
				else
				{
					Item I=monster.fetchInventory(s);
					if(I!=null)
						I.destroy();
				}
				break;
			}
			case 2: // mpecho
			{
				if(lastKnownLocation!=null)
					lastKnownLocation.show(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s.substring(6).trim()));
				break;
			}
			case 13: // mpunaffect
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String which=CMParms.getPastBitClean(s,1);
				if(newTarget!=null)
				if(which.equalsIgnoreCase("all")||(which.length()==0))
				{
					for(int a=newTarget.numEffects()-1;a>=0;a--)
					{
						Ability A=newTarget.fetchEffect(a);
						if(A!=null)
							A.unInvoke();
					}
				}
				else
				{
					Ability A=newTarget.fetchEffect(which);
					if(A!=null)
					{
						A.unInvoke();
						if(newTarget.fetchEffect(which)==A)
							newTarget.delEffect(A);
					}
				}
				break;
			}
			case 3: // mpslay
            {
				Environmental newTarget=getArgumentItem(CMParms.getPastBitClean(s,0),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB))
					CMLib.combat().postDeath(monster,(MOB)newTarget,null);
				break;
			}
			case 16: // mpset
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String arg2=CMParms.getCleanBit(s,2);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,2));
				if(newTarget!=null)
				{
					boolean found=false;
					for(int i=0;i<newTarget.getStatCodes().length;i++)
					{
						if(newTarget.getStatCodes()[i].equalsIgnoreCase(arg2))
						{
							newTarget.setStat(arg2,arg3);
							found=true;
							break;
						}
					}
					if((!found)&&(newTarget instanceof MOB))
					{
						MOB M=(MOB)newTarget;
						for(int i=0;i<CharStats.STAT_DESCS.length;i++)
							if(CharStats.STAT_DESCS[i].equalsIgnoreCase(arg2))
							{
								M.baseCharStats().setStat(i,CMath.s_int(arg3.trim()));
								M.recoverCharStats();
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.curState().getStatCodes().length;i++)
							if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
							{
								M.curState().setStat(arg2,arg3);
								found=true;
								break;
							}
						if(!found)
						for(int i=0;i<M.baseEnvStats().getCodes().length;i++)
							if(M.baseEnvStats().getCodes()[i].equalsIgnoreCase(arg2))
							{
								M.baseEnvStats().setStat(arg2,arg3);
								found=true;
								break;
							}
						if((!found)&&(M.playerStats()!=null))
						for(int i=0;i<M.playerStats().getStatCodes().length;i++)
							if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
							{
								M.playerStats().setStat(arg2,arg3);
								found=true;
								break;
							}
                        if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                            for(int i=0;i<M.baseState().getStatCodes().length;i++)
                                if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                                {
                                    M.curState().setStat(arg2.substring(4),arg3);
                                    found=true;
                                    break;
                                }
					}
					
					if(!found)
					{
						scriptableError(scripted,"MPSET","Syntax","Unknown stat: "+arg2+" for "+newTarget.Name());
						break;
					}
					if(newTarget instanceof MOB)
						((MOB)newTarget).recoverCharStats();
					newTarget.recoverEnvStats();
					if(newTarget instanceof MOB)
						((MOB)newTarget).recoverMaxState();
				}
				break;
			}
			case 63: // mpargset
			{
				String arg1=CMParms.getCleanBit(s,1);
				String arg2=CMParms.getPastBitClean(s,1);
				if((arg1.length()!=2)||(!arg1.startsWith("$")))
				{
					scriptableError(scripted,"MPARGSET","Syntax","Invalid argument var: "+arg1+" for "+scripted.Name());
					break;
				}
				Object O=getArgumentMOB(arg2,source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if(O==null) O=getArgumentItem(arg2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((O==null)&&(!arg2.trim().startsWith("$"))) 
					O=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,arg2);
				char c=arg1.charAt(1);
				if(Character.isDigit(c))
				{
					if((O instanceof String)&&(((String)O).equalsIgnoreCase("null")))
						O=null;
					tmp[CMath.s_int(Character.toString(c))]=O;
				}
				else
				switch(arg1.charAt(1))
				{
				case 'N': 
				case 'n': if(O instanceof MOB) source=(MOB)O; break;
				case 'I':
				case 'i': if(O instanceof Environmental) scripted=(Environmental)O;
						  if(O instanceof MOB) monster=(MOB)O;
						  break;
				case 'T':
				case 't': if(O instanceof Environmental) target=(Environmental)O; break;
				case 'O':
				case 'o': if(O instanceof Item) primaryItem=(Item)O; break;
				case 'P':
				case 'p': if(O instanceof Item) secondaryItem=(Item)O; break;
				case 'd': 
				case 'D': if(O instanceof Room) lastKnownLocation=(Room)O; break;
				default:
					scriptableError(scripted,"MPARGSET","Syntax","Invalid argument var: "+arg1+" for "+scripted.Name());
					break;
				}
				break;
			}
			case 35: // mpgset
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String arg2=CMParms.getCleanBit(s,2);
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,2));
				if(newTarget!=null)
				{
                    
					boolean found=false;
                    for(int i=0;i<newTarget.getStatCodes().length;i++)
                    {
                        if(newTarget.getStatCodes()[i].equalsIgnoreCase(arg2))
                        {
                            newTarget.setStat(newTarget.getStatCodes()[i],arg3);
                            found=true; break;
                        }
                    }
                    if(!found)
					if(newTarget instanceof MOB)
					{
						for(int i=0;i<CMObjectBuilder.GENMOBCODES.length;i++)
						{
							if(CMObjectBuilder.GENMOBCODES[i].equalsIgnoreCase(arg2))
							{
								CMLib.coffeeMaker().setGenMobStat((MOB)newTarget,CMObjectBuilder.GENMOBCODES[i],arg3);
								found=true;
								break;
							}
						}
						if(!found)
						{
							MOB M=(MOB)newTarget;
							for(int i=0;i<CharStats.STAT_DESCS.length;i++)
							{
								if(CharStats.STAT_DESCS[i].equalsIgnoreCase(arg2))
								{
                                    if((arg3.length()==1)&&(Character.isLetter(arg3.charAt(0))))
    									M.baseCharStats().setStat(i,arg3.charAt(0));
                                    else
                                        M.baseCharStats().setStat(i,CMath.s_int(arg3.trim()));
									M.recoverCharStats();
									found=true;
									break;
								}
							}
							if(!found)
							for(int i=0;i<M.curState().getStatCodes().length;i++)
							{
								if(M.curState().getStatCodes()[i].equalsIgnoreCase(arg2))
								{
									M.curState().setStat(arg2,arg3);
									found=true;
									break;
								}
							}
							if(!found)
							for(int i=0;i<M.baseEnvStats().getCodes().length;i++)
							{
								if(M.baseEnvStats().getCodes()[i].equalsIgnoreCase(arg2))
								{
									M.baseEnvStats().setStat(arg2,arg3);
									found=true;
									break;
								}
							}
							if((!found)&&(M.playerStats()!=null))
							for(int i=0;i<M.playerStats().getStatCodes().length;i++)
								if(M.playerStats().getStatCodes()[i].equalsIgnoreCase(arg2))
								{
									M.playerStats().setStat(arg2,arg3);
									found=true;
									break;
								}
                            if((!found)&&(arg2.toUpperCase().startsWith("BASE")))
                                for(int i=0;i<M.baseState().getStatCodes().length;i++)
                                    if(M.baseState().getStatCodes()[i].equalsIgnoreCase(arg2.substring(4)))
                                    {
                                        M.curState().setStat(arg2.substring(4),arg3);
                                        found=true;
                                        break;
                                    }
						}
					}
					else
					if(newTarget instanceof Item)
					{
						for(int i=0;i<CMObjectBuilder.GENITEMCODES.length;i++)
						{
							if(CMObjectBuilder.GENITEMCODES[i].equalsIgnoreCase(arg2))
							{
								CMLib.coffeeMaker().setGenItemStat((Item)newTarget,CMObjectBuilder.GENITEMCODES[i],arg3);
								found=true;
								break;
							}
						}
					}

					if(!found)
					{
						scriptableError(scripted,"MPGSET","Syntax","Unknown stat: "+arg2+" for "+newTarget.Name());
						break;
					}
					if(newTarget instanceof MOB)
						((MOB)newTarget).recoverCharStats();
					newTarget.recoverEnvStats();
					if(newTarget instanceof MOB)
						((MOB)newTarget).recoverMaxState();
				}
				break;
			}
			case 11: // mpexp
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String amtStr=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,1)).trim();
				int t=CMath.s_int(amtStr);
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					if((amtStr.endsWith("%"))
					&&(((MOB)newTarget).getExpNeededLevel()<Integer.MAX_VALUE))
                    {
                        int baseLevel=newTarget.baseEnvStats().level();
                        int lastLevelExpNeeded=(baseLevel<=1)?0:CMLib.leveler().getLevelExperience(baseLevel-1);
                        int thisLevelExpNeeded=CMLib.leveler().getLevelExperience(baseLevel);
						t=(int)Math.round(CMath.mul(thisLevelExpNeeded-lastLevelExpNeeded,
											CMath.div(CMath.s_int(amtStr.substring(0,amtStr.length()-1)),100.0)));
                    }
					if(t!=0) CMLib.leveler().postExperience((MOB)newTarget,null,null,t,false);
				}
				break;
			}
			case 59: // mpquestpoints
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,1));
				if(newTarget instanceof MOB)
				{
					if(CMath.isNumber(val.trim())) 
						((MOB)newTarget).setQuestPoint(CMath.s_int(val.trim()));
					else
					if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setQuestPoint(((MOB)newTarget).getQuestPoint()+CMath.s_int(val.substring(2).trim()));
					else
					if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setQuestPoint(((MOB)newTarget).getQuestPoint()-CMath.s_int(val.substring(2).trim()));
					else
                        scriptableError(scripted,"QUESTPOINTS","Syntax","Bad syntax "+val+" for "+scripted.Name());
				}
				break;
			}
			case 65: // MPQSET
			{
				String qstr=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				String var=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,2));
				Quest Q=qstr.equals("*")?defaultQuest:CMLib.quests().fetchQuest(qstr);
				if(Q==null)
                    scriptableError(scripted,"QUESTPOINTS","Syntax","Unknown quest "+qstr+" for "+scripted.Name());
				else
					Q.setStat(var,val);
				break;
			}
			case 66: // MPLOG
			{
				String type=CMParms.getCleanBit(s,1).toUpperCase();
				String head=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,2));
				if(type.startsWith("E")) Log.errOut(head,val);
				else
				if(type.startsWith("I")||type.startsWith("S")) Log.infoOut(head,val);
				else
				if(type.startsWith("D")) Log.debugOut(head,val);
				else
                    scriptableError(scripted,"MPLOG","Syntax","Unknown log type "+type+" for "+scripted.Name());
				break;
			}
			case 67: // MPCHANNEL
			{
				String channel=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				boolean sysmsg=channel.startsWith("!");
				if(sysmsg) channel=channel.substring(1);
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,1));
				if(CMLib.channels().getChannelCodeNumber(channel)<0)
                    scriptableError(scripted,"MPCHANNEL","Syntax","Unknown channel "+channel+" for "+scripted.Name());
				else
					CMLib.commands().postChannel(monster,channel,val,sysmsg);
				break;
			}
            case 68: // cd
            {
                String scriptname=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
                if(!new CMFile(Resources.makeFileResourceName(scriptname),null,false,true).exists())
                    scriptableError(scripted,"MPUNLOADSCRIPT","Runtime","File does not exist: "+Resources.makeFileResourceName(scriptname));
                else
                {
                    Vector delThese=new Vector();
                    boolean foundKey=false;
                    scriptname=scriptname.toUpperCase().trim();
                    String parmname=scriptname;
                    Vector V=Resources.findResourceKeys(parmname);
                    for(Enumeration e=V.elements();e.hasMoreElements();)
                    {
                        String key=(String)e.nextElement();
                        if(key.startsWith("PARSEDPRG: ")&&(key.toUpperCase().endsWith(parmname)))
                        { foundKey=true; delThese.addElement(key);}
                    }
                    if(foundKey)
                        for(int i=0;i<delThese.size();i++)
                            Resources.removeResource((String)delThese.elementAt(i));
                }
                
                break;
            }
			case 60: // trains
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,1));
				if(newTarget instanceof MOB)
				{
					if(CMath.isNumber(val.trim())) 
						((MOB)newTarget).setTrains(CMath.s_int(val.trim()));
					else
					if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setTrains(((MOB)newTarget).getTrains()+CMath.s_int(val.substring(2).trim()));
					else
					if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setTrains(((MOB)newTarget).getTrains()-CMath.s_int(val.substring(2).trim()));
					else
                        scriptableError(scripted,"TRAINS","Syntax","Bad syntax "+val+" for "+scripted.Name());
				}
				break;
			}
			case 61: // pracs
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String val=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,1));
				if(newTarget instanceof MOB)
				{
					if(CMath.isNumber(val.trim())) 
						((MOB)newTarget).setPractices(CMath.s_int(val.trim()));
					else
					if(val.startsWith("++")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setPractices(((MOB)newTarget).getPractices()+CMath.s_int(val.substring(2).trim()));
					else
					if(val.startsWith("--")&&(CMath.isNumber(val.substring(2).trim())))
						((MOB)newTarget).setPractices(((MOB)newTarget).getPractices()-CMath.s_int(val.substring(2).trim()));
					else
                        scriptableError(scripted,"PRACS","Syntax","Bad syntax "+val+" for "+scripted.Name());
				}
				break;
			}
			case 5: // mpmload
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0).trim());
				Vector Ms=new Vector();
				MOB m=CMClass.getMOB(s);
				if(m!=null) Ms.addElement(m);
				if(lastKnownLocation!=null)
				{
					if(Ms.size()==0)
						findSomethingCalledThis(s,monster,lastKnownLocation,Ms,true);
					for(int i=0;i<Ms.size();i++)
					{
						if(Ms.elementAt(i) instanceof MOB)
						{
							m=(MOB)((MOB)Ms.elementAt(i)).copyOf();
							m.text();
							m.recoverEnvStats();
							m.recoverCharStats();
							m.resetToMaxState();
							m.bringToLife(lastKnownLocation,true);
						}
					}
				}
				break;
			}
			case 6: // mpoload
			{
				// if not mob
				if(scripted instanceof MOB)
				{
					s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0).trim());
					int containerIndex=s.toUpperCase().indexOf(" INTO ");
					Container container=null;
					if(containerIndex>=0)
					{
						Vector containers=new Vector();
						findSomethingCalledThis(s.substring(containerIndex+6).trim(),monster,lastKnownLocation,containers,false);
						for(int c=0;c<containers.size();c++)
							if((containers.elementAt(c) instanceof Container)
							&&(((Container)containers.elementAt(c)).capacity()>0))
							{
								container=(Container)containers.elementAt(c);
								s=s.substring(0,containerIndex).trim();
								break;
							}
					}
					long coins=CMLib.english().numPossibleGold(null,s);
					if(coins>0)
					{
					    String currency=CMLib.english().numPossibleGoldCurrency(scripted,s);
					    double denom=CMLib.english().numPossibleGoldDenomination(scripted,currency,s);
					    Coins C=CMLib.beanCounter().makeCurrency(currency,denom,coins);
					    monster.addInventory(C);
					    C.putCoinsBack();
					}
					else
					if(lastKnownLocation!=null)
					{
						Vector Is=new Vector();
						Item m=CMClass.getItem(s);
						if(m!=null) 
							Is.addElement(m);
						else
							findSomethingCalledThis(s,(MOB)scripted,lastKnownLocation,Is,false);
						for(int i=0;i<Is.size();i++)
						{
							if(Is.elementAt(i) instanceof Item)
							{
								m=(Item)Is.elementAt(i);
								if((m!=null)&&(!(m instanceof ArchonOnly)))
								{
									m=(Item)m.copyOf();
									m.recoverEnvStats();
									m.setContainer(container);
									if(container instanceof MOB)
										((MOB)container.owner()).addInventory(m);
									else
									if(container instanceof Room)
										((Room)container.owner()).addItemRefuse(m,Item.REFUSE_PLAYER_DROP);
									else
										monster.addInventory(m);
								}
							}
						}
						lastKnownLocation.recoverRoomStats();
						monster.recoverCharStats();
						monster.recoverEnvStats();
						monster.recoverMaxState();
					}
					break;
				}
			}
			case 41: // mpoloadroom
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0).trim());
				if(lastKnownLocation!=null)
				{
					Vector Is=new Vector();
					int containerIndex=s.toUpperCase().indexOf(" INTO ");
					Container container=null;
					if(containerIndex>=0)
					{
						Vector containers=new Vector();
						findSomethingCalledThis(s.substring(containerIndex+6).trim(),null,lastKnownLocation,containers,false);
						for(int c=0;c<containers.size();c++)
							if((containers.elementAt(c) instanceof Container)
							&&(((Container)containers.elementAt(c)).capacity()>0))
							{
								container=(Container)containers.elementAt(c);
								s=s.substring(0,containerIndex).trim();
								break;
							}
					}
					long coins=CMLib.english().numPossibleGold(null,s);
					if(coins>0)
					{
					    String currency=CMLib.english().numPossibleGoldCurrency(monster,s);
					    double denom=CMLib.english().numPossibleGoldDenomination(monster,currency,s);
					    Coins C=CMLib.beanCounter().makeCurrency(currency,denom,coins);
					    Is.addElement(C);
					}
					else
					{
						Item I=CMClass.getItem(s);
						if(I!=null) 
							Is.addElement(I);
						else
							findSomethingCalledThis(s,monster,lastKnownLocation,Is,false);
					}
					for(int i=0;i<Is.size();i++)
					{
						if(Is.elementAt(i) instanceof Item)
						{
							Item I=(Item)Is.elementAt(i);
							if((I!=null)&&(!(I instanceof ArchonOnly)))
							{
								I=(Item)I.copyOf();
								I.recoverEnvStats();
								lastKnownLocation.addItemRefuse(I,Item.REFUSE_MONSTER_EQ);
								I.setContainer(container);
								if(I instanceof Coins)
								    ((Coins)I).putCoinsBack();
								if(I instanceof RawMaterial)
									((RawMaterial)I).rebundle();
							}
						}
					}
					lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 42: // mphide
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(newTarget!=null)
				{
					newTarget.baseEnvStats().setDisposition(newTarget.baseEnvStats().disposition()|EnvStats.IS_NOT_SEEN);
					newTarget.recoverEnvStats();
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				break;
			}
            case 58: // mpreset
            {
                String arg=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0));
                if(arg.equalsIgnoreCase("area"))
                {
                    if(lastKnownLocation!=null) 
                        CMLib.map().resetArea(lastKnownLocation.getArea());
                }
                else
                if(arg.equalsIgnoreCase("room"))
                {
                    if(lastKnownLocation!=null) 
                        CMLib.map().resetRoom(lastKnownLocation);
                }
                else
                {
                    Room R=CMLib.map().getRoom(arg);
                    if(R!=null) 
                        CMLib.map().resetRoom(R);
                    else
                    {
                        Area A=CMLib.map().findArea(arg);
                        if(A!=null)
                            CMLib.map().resetArea(A);
                        else
                            scriptableError(scripted,"MPRESET","Syntax","Unknown location: "+arg+" for "+scripted.Name());
                    }
                }
                break;
            }
            case 56: // mpstop
            {
                Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                if(newTarget instanceof MOB)
                {
                    MOB mob=(MOB)newTarget;
                    Ability A=null;
                    for(int a=mob.numEffects();a>=0;a--)
                    {
                        A=mob.fetchEffect(a);
                        if(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
                        &&(A.canBeUninvoked())
                        &&(!A.isAutoInvoked()))
                            A.unInvoke();
                    }
                    mob.makePeace();
                    if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
                }
                break;
            }
			case 43: // mpunhide
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(CMath.bset(newTarget.baseEnvStats().disposition(),EnvStats.IS_NOT_SEEN)))
				{
					newTarget.baseEnvStats().setDisposition(newTarget.baseEnvStats().disposition()-EnvStats.IS_NOT_SEEN);
					newTarget.recoverEnvStats();
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 44: // mpopen
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)&&(((Exit)newTarget).hasADoor()))
				{
					Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),true,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)&&(((Container)newTarget).hasALid()))
				{
					Container E=(Container)newTarget;
					E.setLidsNLocks(E.hasALid(),true,E.hasALock(),false);
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 45: // mpclose
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)&&(((Exit)newTarget).hasADoor())&&(((Exit)newTarget).isOpen()))
				{
					Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)&&(((Container)newTarget).hasALid())&&(((Container)newTarget).isOpen()))
				{
					Container E=(Container)newTarget;
					E.setLidsNLocks(E.hasALid(),false,E.hasALock(),false);
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 46: // mplock
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)&&(((Exit)newTarget).hasALock()))
				{
					Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),true,E.defaultsLocked());
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)&&(((Container)newTarget).hasALock()))
				{
					Container E=(Container)newTarget;
					E.setLidsNLocks(E.hasALid(),false,E.hasALock(),true);
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 47: // mpunlock
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget instanceof Exit)&&(((Exit)newTarget).isLocked()))
				{
					Exit E=(Exit)newTarget;
					E.setDoorsNLocks(E.hasADoor(),false,E.defaultsClosed(),E.hasALock(),false,E.defaultsLocked());
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				else
				if((newTarget instanceof Container)&&(((Container)newTarget).isLocked()))
				{
					Container E=(Container)newTarget;
					E.setLidsNLocks(E.hasALid(),false,E.hasALock(),false);
					if(lastKnownLocation!=null) lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 48: // return
                tickStatus=Tickable.STATUS_END;
                return varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s.substring(6).trim());
			case 7: // mpechoat
			{
                String parm=CMParms.getCleanBit(s,1);
                Environmental newTarget=getArgumentMOB(parm,source,monster,target,primaryItem,secondaryItem,msg,tmp);
                if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
                {
                    s=CMParms.getPastBit(s,1).trim();
                    if(newTarget==monster)
                        lastKnownLocation.showSource(monster,null,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s));
                    else
                        lastKnownLocation.show(monster,newTarget,null,CMMsg.MSG_OK_ACTION,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s),CMMsg.NO_EFFECT,null);
                }
                else
                if(parm.equalsIgnoreCase("world"))
                {
                    lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,1).trim()));
                    for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        if(R.numInhabitants()>0)
                            R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s));
                    }
                }
                else
                if(parm.equalsIgnoreCase("area")&&(lastKnownLocation!=null))
                {
                    lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,1).trim()));
                    for(Enumeration e=lastKnownLocation.getArea().getProperMap();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        if(R.numInhabitants()>0)
                            R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s));
                    }
                }
                else
                if(CMLib.map().getRoom(parm)!=null)
                    CMLib.map().getRoom(parm).show(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,1).trim()));
                else
                if(CMLib.map().findArea(parm)!=null)
                {
                    lastKnownLocation.showSource(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,1).trim()));
                    for(Enumeration e=CMLib.map().findArea(parm).getMetroMap();e.hasMoreElements();)
                    {
                        Room R=(Room)e.nextElement();
                        if(R.numInhabitants()>0)
                            R.showOthers(monster,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,1).trim()));
                    }
                }
				break;
			}
			case 8: // mpechoaround
			{
				Environmental newTarget=getArgumentMOB(CMParms.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB)&&(lastKnownLocation!=null))
				{
					s=CMParms.getPastBit(s,1).trim();
					lastKnownLocation.showOthers((MOB)newTarget,null,CMMsg.MSG_OK_ACTION,varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s));
				}
				break;
			}
			case 9: // mpcast
			{
				String cast=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,2),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				Ability A=null;
				if(cast!=null) A=CMClass.findAbility(cast);
				if((newTarget!=null)&&(A!=null))
				{
					A.setProficiency(100);
					A.invoke(monster,newTarget,false,0);
				}
				break;
			}
			case 30: // mpaffect
			{
                String cast=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,2),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String m2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,2));
				Ability A=null;
				if(cast!=null) A=CMClass.findAbility(cast);
				if((newTarget!=null)&&(A!=null))
				{
					A.setMiscText(m2);
					if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY)
						newTarget.addNonUninvokableEffect(A);
					else
						A.invoke(monster,CMParms.parse(m2),newTarget,true,0);
				}
				break;
			}
			case 31: // mpbehave
			{
                String cast=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,2),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String m2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,2));
				Behavior A=null;
				if((cast!=null)&&(newTarget!=null))
				{
					A=newTarget.fetchBehavior(cast);
					if(A==null) A=CMClass.getBehavior(cast);
				}
				if((newTarget!=null)&&(A!=null))
				{
					A.setParms(m2);
					if(newTarget.fetchBehavior(A.ID())==null)
						newTarget.addBehavior(A);
				}
				break;
			}
			case 32: // mpunbehave
			{
                String cast=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,2),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(newTarget!=null)
				{
					Behavior A=newTarget.fetchBehavior(cast);
					if(A!=null) newTarget.delBehavior(A);
				}
				break;
			}
			case 33: // mptattoo
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String tattooName=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
				if((newTarget!=null)&&(tattooName.length()>0)&&(newTarget instanceof MOB))
				{
					MOB themob=(MOB)newTarget;
					boolean tattooMinus=tattooName.startsWith("-");
					if(tattooMinus)	tattooName=tattooName.substring(1);
					String tattoo=tattooName;
					if((tattoo.length()>0)
					&&(Character.isDigit(tattoo.charAt(0)))
					&&(tattoo.indexOf(" ")>0)
					&&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")).trim())))
						tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
					if(themob.fetchTattoo(tattoo)!=null)
					{
						if(tattooMinus)
							themob.delTattoo(tattooName);
					}
					else
					if(!tattooMinus)
						themob.addTattoo(tattooName);
				}
				break;
			}
            case 55: // mpnotrigger
            {
                String trigger=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
                String time=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
                int triggerCode=-1;
                for(int i=0;i<progs.length;i++)
                    if(trigger.equalsIgnoreCase(progs[i]))
                        triggerCode=i;
                if(triggerCode<0)
                    scriptableError(scripted,"MPNOTRIGGER","RunTime",trigger+" is not a valid trigger name.");
                else
                if(!CMath.isInteger(time.trim()))
                    scriptableError(scripted,"MPNOTRIGGER","RunTime",time+" is not a valid milisecond time.");
                else
                {
                    noTrigger.remove(new Integer(triggerCode));
                    noTrigger.put(new Integer(triggerCode),new Long(System.currentTimeMillis()+CMath.s_long(time.trim())));
                }
                break;
            }
			case 54: // mpfaction
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String faction=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
                String range=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,2));
                Faction F=CMLib.factions().getFaction(faction);
				if((newTarget!=null)&&(F!=null)&&(newTarget instanceof MOB))
				{
					MOB themob=(MOB)newTarget;
					if((range.startsWith("--"))&&(CMath.isInteger(range.substring(2).trim())))
						range=""+(themob.fetchFaction(faction)-CMath.s_int(range.substring(2).trim()));
					else
					if((range.startsWith("+"))&&(CMath.isInteger(range.substring(1).trim())))
						range=""+(themob.fetchFaction(faction)-CMath.s_int(range.substring(1).trim()));
                    if(CMath.isInteger(range.trim()))
                        themob.addFaction(F.factionID(),CMath.s_int(range.trim()));
                    else
                    {
                        Vector V=CMLib.factions().getRanges(F.factionID());
                        Faction.FactionRange FR=null;
                        for(int v=0;v<V.size();v++)
                        {
                            Faction.FactionRange FR2=(Faction.FactionRange)V.elementAt(v);
                            if(FR2.name().equalsIgnoreCase(range))
                            { FR=FR2; break;}
                        }
                        if(FR==null)
                            scriptableError(scripted,"MPFACTION","RunTime",range+" is not a valid range for "+F.name()+".");
                        else
                            themob.addFaction(F.factionID(),FR.low()+((FR.high()-FR.low())/2));
                    }
				}
				break;
			}
            case 49: // mptitle
            {
                Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                String tattooName=CMParms.getPastBitClean(s,1);
                if((newTarget!=null)&&(tattooName.length()>0)&&(newTarget instanceof MOB))
                {
                    MOB themob=(MOB)newTarget;
                    boolean tattooMinus=tattooName.startsWith("-");
                    if(tattooMinus) tattooName=tattooName.substring(1);
                    String tattoo=tattooName;
                    if((tattoo.length()>0)
                    &&(Character.isDigit(tattoo.charAt(0)))
                    &&(tattoo.indexOf(" ")>0)
                    &&(CMath.isNumber(tattoo.substring(0,tattoo.indexOf(" ")).trim())))
                        tattoo=tattoo.substring(tattoo.indexOf(" ")+1).trim();
                    if(themob.playerStats()!=null)
                    {
                        if(themob.playerStats().getTitles().contains(tattoo))
                        {
                            if(tattooMinus)
                                themob.playerStats().getTitles().removeElement(tattooName);
                        }
                        else
                        if(!tattooMinus)
                            themob.playerStats().getTitles().insertElementAt(tattooName,0);
                    }
                }
                break;
            }
			case 10: // mpkill
			{
				Environmental newTarget=getArgumentMOB(CMParms.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB))
					monster.setVictim((MOB)newTarget);
				break;
			}
			case 51: // mpsetclandata
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String clanID=null;
				if((newTarget!=null)&&(newTarget instanceof MOB))
				    clanID=((MOB)newTarget).getClanID();
				else
					clanID=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				String clanvar=CMParms.getCleanBit(s,2);
				String clanval=CMParms.getPastBitClean(s,2);
				Clan C=CMLib.clans().getClan(clanID);
				if(C!=null)
				{
				    int whichVar=-1;
				    for(int i=0;i<clanVars.length;i++)
				        if(clanvar.equalsIgnoreCase(clanVars[i]))
				        { whichVar=i; break;}
				    boolean nosave=false;
				    switch(whichVar)
				    {
				    case 0: C.setAcceptanceSettings(clanval); break;
				    case 1: nosave=true; break; // detail
				    case 2: C.setDonation(clanval); break;
				    case 3: C.setExp(CMath.s_long(clanval.trim())); break;
				    case 4: C.setGovernment(CMath.s_int(clanval.trim())); break;
				    case 5: C.setMorgue(clanval); break;
				    case 6: C.setPolitics(clanval); break;
				    case 7: C.setPremise(clanval); break;
				    case 8: C.setRecall(clanval); break;
				    case 9: nosave=true; break; // size
				    case 10: C.setStatus(CMath.s_int(clanval.trim())); break;
				    case 11: C.setTaxes(CMath.s_double(clanval.trim())); break;
				    case 12: C.setTrophies(CMath.s_int(clanval.trim())); break;
				    case 13: nosave=true; break; // type
				    case 14: nosave=true; break; // areas
				    case 15: nosave=true; break; // memberlist
				    case 16: nosave=true; break; // topmember
				    default:
						scriptableError(scripted,"MPSETCLANDATA","RunTime",clanvar+" is not a valid clan variable.");
					    nosave=true; 
				    	break;
				    }
				    if(!nosave) C.update();
				}
				break;
			}
			case 52: // mpplayerclass
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
				    Vector V=CMParms.parse(CMParms.getPastBit(s,1));
				    for(int i=0;i<V.size();i++)
				    {
				        if(CMath.isInteger(((String)V.elementAt(i)).trim()))
				            ((MOB)newTarget).baseCharStats().setClassLevel(((MOB)newTarget).baseCharStats().getCurrentClass(),CMath.s_int(((String)V.elementAt(i)).trim()));
				        else
				        {
				            CharClass C=CMClass.findCharClass((String)V.elementAt(i));
				            if(C!=null)
					            ((MOB)newTarget).baseCharStats().setCurrentClass(C);
				        }
				    }
				    ((MOB)newTarget).recoverCharStats();
				}
				break;
			}
			case 12: // mppurge
			{
				if(lastKnownLocation!=null)
				{
					String s2=CMParms.getPastBitClean(s,0).trim();
					Environmental E=null;
					if(s2.equalsIgnoreCase("self")||s2.equalsIgnoreCase("me"))
						E=scripted;
					else
						E=getArgumentItem(s2,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
					if(E!=null)
					{
						if(E instanceof MOB)
						{
							if(!((MOB)E).isMonster())
							{
								if(((MOB)E).getStartRoom()!=null)
									((MOB)E).getStartRoom().bringMobHere((MOB)E,false);
								((MOB)E).session().setKillFlag(true);
							}
							else
							if(((MOB)E).getStartRoom()!=null)
								((MOB)E).killMeDead(false);
							else
								((MOB)E).destroy();
						}
						else
						if(E instanceof Item)
						{
							Environmental oE=((Item)E).owner();
							((Item)E).destroy();
							if(oE!=null) oE.recoverEnvStats();
						}
					}
					lastKnownLocation.recoverRoomStats();
				}
				break;
			}
			case 14: // mpgoto
			{
				s=s.substring(6).trim();
				if((s.length()>0)&&(lastKnownLocation!=null))
				{
					Room goHere=null;
					if(s.startsWith("$"))
						goHere=CMLib.map().roomLocation(this.getArgumentItem(s,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
					if(goHere==null)
						goHere=getRoom(varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s),lastKnownLocation);
					if(goHere!=null)
					{
						if(scripted instanceof MOB)
							goHere.bringMobHere((MOB)scripted,true);
						else
						if(scripted instanceof Item)
							goHere.bringItemHere((Item)scripted,Item.REFUSE_PLAYER_DROP,true);
						else
						{
							goHere.bringMobHere(monster,true);
							if(!(scripted instanceof MOB))
								goHere.delInhabitant(monster);
						}
						if(CMLib.map().roomLocation(scripted)==goHere)
							lastKnownLocation=goHere;
					}
				}
				break;
			}
			case 15: // mpat
			if(lastKnownLocation!=null)
			{
				Room lastPlace=lastKnownLocation;
				String roomName=CMParms.getCleanBit(s,1);
				if(roomName.length()>0)
				{
					s=CMParms.getPastBit(s,1).trim();
					Room goHere=null;
					if(roomName.startsWith("$"))
						goHere=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
					if(goHere==null)
						goHere=getRoom(varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
					if(goHere!=null)
					{
						goHere.bringMobHere(monster,true);
						Vector V=new Vector();
						V.addElement("");
						V.addElement(s.trim());
                        lastKnownLocation=goHere;
						execute(scripted,source,target,monster,primaryItem,secondaryItem,V,msg,tmp);
                        lastKnownLocation=lastPlace;
						lastPlace.bringMobHere(monster,true);
						if(!(scripted instanceof MOB))
						{
							goHere.delInhabitant(monster);
							lastPlace.delInhabitant(monster);
						}
					}
				}
				break;
			}
			case 17: // mptransfer
			{
				String mobName=CMParms.getCleanBit(s,1);
                String roomName="";
				Room newRoom=null;
                if(CMParms.numBits(s)>2)
                {
                    roomName=CMParms.getPastBit(s,1);
					if(roomName.startsWith("$"))
						newRoom=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
                }
				if((roomName.length()==0)&&(lastKnownLocation!=null))
					roomName=lastKnownLocation.roomID();
				if(roomName.length()>0)
				{
					if(newRoom==null)
						newRoom=getRoom(varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
					if(newRoom!=null)
					{
						Vector V=new Vector();
						if(mobName.startsWith("$"))
						{
							Environmental E=getArgumentItem(mobName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
							if(E!=null) V.addElement(E);
						}
						if(V.size()==0)
						{
							mobName=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,mobName);
							if(mobName.equalsIgnoreCase("all"))
							{
								if(lastKnownLocation!=null)
								{
									for(int x=0;x<lastKnownLocation.numInhabitants();x++)
									{
										MOB m=lastKnownLocation.fetchInhabitant(x);
										if((m!=null)&&(m!=monster)&&(!V.contains(m)))
											V.addElement(m);
									}
								}
							}
							else
							{
								MOB findOne=null;
								Area A=null;
								if(lastKnownLocation!=null)
								{
									findOne=lastKnownLocation.fetchInhabitant(mobName);
									A=lastKnownLocation.getArea();
	                                if((findOne!=null)&&(findOne!=monster))
	                                    V.addElement(findOne);
								}
	                            if(findOne==null)
	                            {
	                                findOne=CMLib.map().getPlayer(mobName);
	                                if((findOne!=null)&&(!CMLib.flags().isInTheGame(findOne,true)))
	                                    findOne=null;
	                                if((findOne!=null)&&(findOne!=monster))
	                                    V.addElement(findOne);
	                            }
								if((findOne==null)&&(A!=null))
									for(Enumeration r=A.getProperMap();r.hasMoreElements();)
									{
										Room R=(Room)r.nextElement();
										findOne=R.fetchInhabitant(mobName);
	                                    if((findOne!=null)&&(findOne!=monster))
	                                        V.addElement(findOne);
									}
							}
						}
						for(int v=0;v<V.size();v++)
						{
							if(V.elementAt(v) instanceof MOB)
							{
								MOB mob=(MOB)V.elementAt(v);
								HashSet H=mob.getGroupMembers(new HashSet());
								for(Iterator e=H.iterator();e.hasNext();)
								{
									MOB M=(MOB)e.next();
									if((!V.contains(M))&&(M.location()==mob.location()))
									   V.addElement(M);
								}
							}
						}
						for(int v=0;v<V.size();v++)
						{
							if(V.elementAt(v) instanceof MOB)
							{
								MOB follower=(MOB)V.elementAt(v);
								Room thisRoom=follower.location();
								CMMsg enterMsg=CMClass.getMsg(follower,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,"<S-NAME> appears in a puff of smoke."+CMProps.msp("appear.wav",10));
								CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,null,CMMsg.MSG_LEAVE,"<S-NAME> disappear(s) in a puff of smoke.");
								if(thisRoom.okMessage(follower,leaveMsg)&&newRoom.okMessage(follower,enterMsg))
								{
									if(follower.isInCombat())
									{
										CMLib.commands().postFlee(follower,("NOWHERE"));
										follower.makePeace();
									}
									thisRoom.send(follower,leaveMsg);
									newRoom.bringMobHere(follower,false);
									newRoom.send(follower,enterMsg);
									follower.tell("\n\r\n\r");
									CMLib.commands().postLook(follower,true);
								}
							}
							else
							if((V.elementAt(v) instanceof Item)
							&&(newRoom!=CMLib.map().roomLocation((Environmental)V.elementAt(v))))
								newRoom.bringItemHere((Item)V.elementAt(v),Item.REFUSE_PLAYER_DROP,true);
							if(V.elementAt(v)==scripted)
								lastKnownLocation=newRoom;
						}
					}
				}
				break;
			}
			case 25: // mpbeacon
			{
				String roomName=CMParms.getCleanBit(s,1);
				Room newRoom=null;
				if((roomName.length()>0)&&(lastKnownLocation!=null))
				{
					s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,1));
					if(roomName.startsWith("$"))
						newRoom=CMLib.map().roomLocation(this.getArgumentItem(roomName,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp));
					if(newRoom==null)
						newRoom=getRoom(varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,roomName),lastKnownLocation);
					if((newRoom!=null)&&(lastKnownLocation!=null))
					{
						Vector V=new Vector();
						if(s.equalsIgnoreCase("all"))
						{
							for(int x=0;x<lastKnownLocation.numInhabitants();x++)
							{
								MOB m=lastKnownLocation.fetchInhabitant(x);
								if((m!=null)&&(m!=monster)&&(!m.isMonster())&&(!V.contains(m)))
									V.addElement(m);
							}
						}
						else
						{
							MOB findOne=lastKnownLocation.fetchInhabitant(s);
							if((findOne!=null)&&(findOne!=monster)&&(!findOne.isMonster()))
								V.addElement(findOne);
						}
						for(int v=0;v<V.size();v++)
						{
							MOB follower=(MOB)V.elementAt(v);
							if(!follower.isMonster())
								follower.setStartRoom(newRoom);
						}
					}
				}
				break;
			}
			case 18: // mpforce
			{
				Environmental newTarget=getArgumentMOB(CMParms.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg,tmp);
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,1));
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					Vector V=CMParms.parse(s);
					((MOB)newTarget).doCommand(V);
				}
			}
			case 20: // mpsetvar
			{
				String which=CMParms.getCleanBit(s,1);
				Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,2));
				if(!which.equals("*"))
				{
					if(E==null)
					    which=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,which);
					else
					if(E instanceof Room)
					    which=CMLib.map().getExtendedRoomID((Room)E);
					else
						which=E.Name();
				}
				if((which.length()>0)&&(arg2.length()>0))
					mpsetvar(which,arg2,arg3);
				break;
			}
			case 36: // mpsavevar
			{
				String which=CMParms.getCleanBit(s,1);
				String arg2=CMParms.getCleanBit(s,2).toUpperCase();
				Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
                which=getVarHost(E,which,source,target,monster,primaryItem,secondaryItem,msg,tmp);
				if((which.length()>0)&&(arg2.length()>0))
				{
					DVector V=getScriptVarSet(which,arg2);
					for(int v=0;v<V.size();v++)
					{
                        which=(String)V.elementAt(0,1);
						arg2=((String)V.elementAt(0,2)).toUpperCase();
						Hashtable H=(Hashtable)Resources.getResource("SCRIPTVAR-"+which);
						String val="";
						if(H!=null)
						{
							val=(String)H.get(arg2);
							if(val==null) val="";
						}
                        if(val.length()>0)
    						CMLib.database().DBReCreateData(which,"SCRIPTABLEVARS",arg2,val);
                        else
                            CMLib.database().DBDeleteData(which,"SCRIPTABLEVARS",arg2);
					}
				}
				break;
			}
			case 39: // mploadvar
			{
				String which=CMParms.getCleanBit(s,1);
				String arg2=CMParms.getCleanBit(s,2).toUpperCase();
				Environmental E=getArgumentItem(which,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(arg2.length()>0)
				{
					Vector V=null;
                    which=getVarHost(E,which,source,target,monster,primaryItem,secondaryItem,msg,tmp);
					if(arg2.equals("*"))
						V=CMLib.database().DBReadData(which,"SCRIPTABLEVARS");
					else
						V=CMLib.database().DBReadData(which,"SCRIPTABLEVARS",arg2);
                    if((V!=null)&&(V.size()>0))
                    {
                        V=(Vector)V.firstElement();
    					if(V.size()>3)
    						mpsetvar(which,arg2,(String)V.elementAt(3));
                    }
				}
				break;
			}
			case 40: // MPM2I2M
			{
				String arg1=CMParms.getCleanBit(s,1);
				Environmental E=getArgumentItem(arg1,source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				if(E instanceof MOB)
				{
					String arg2="";
					String arg3="";
					if(CMParms.numBits(s)>2)
					{
						arg2=CMParms.getCleanBit(s,2);
						if(CMParms.numBits(s)>3)
							arg3=CMParms.getPastBit(s,2);
					}

					CagedAnimal caged=(CagedAnimal)CMClass.getItem("GenCaged");
					if(caged!=null)
					{
						((Item)caged).baseEnvStats().setAbility(1);
						((Item)caged).recoverEnvStats();
					}
					if((caged!=null)&&caged.cageMe((MOB)E)&&(lastKnownLocation!=null))
					{
						if(arg2.length()>0) ((Item)caged).setName(arg2);
						if(arg3.length()>0) ((Item)caged).setDisplayText(arg3);
						lastKnownLocation.addItemRefuse((Item)caged,Item.REFUSE_PLAYER_DROP);
						((MOB)E).killMeDead(false);
					}
				}
				else
				if(E instanceof CagedAnimal)
				{
					MOB M=((CagedAnimal)E).unCageMe();
					if((M!=null)&&(lastKnownLocation!=null))
					{
						M.bringToLife(lastKnownLocation,true);
						((Item)E).destroy();
					}
				}
				else
					scriptableError(scripted,"MPM2I2M","RunTime",arg1+" is not a mob or a caged item.");
				break;
			}
			case 28: // mpdamage
			{
				Environmental newTarget=getArgumentItem(CMParms.getCleanBit(s,1),source,monster,scripted,target,primaryItem,secondaryItem,msg,tmp);
				String arg2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
				String arg3=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,3));
				String arg4=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,3));
				if((newTarget!=null)&&(arg2.length()>0))
				{
					if(newTarget instanceof MOB)
					{
						MOB E=(MOB)newTarget;
						int min=CMath.s_int(arg2.trim());
						int max=CMath.s_int(arg3.trim());
						if(max<min) max=min;
						if(min>0)
						{
							int dmg=(max==min)?min:CMLib.dice().roll(1,max-min,min);
							if((dmg>=E.curState().getHitPoints())&&(!arg4.equalsIgnoreCase("kill")))
								dmg=E.curState().getHitPoints()-1;
							if(dmg>0)
								CMLib.combat().postDamage(E,E,null,dmg,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,null);
						}
					}
					else
					if(newTarget instanceof Item)
					{
						Item E=(Item)newTarget;
						int min=CMath.s_int(arg2.trim());
						int max=CMath.s_int(arg3.trim());
						if(max<min) max=min;
						if(min>0)
						{
							int dmg=(max==min)?min:CMLib.dice().roll(1,max-min,min);
							boolean destroy=false;
							if(E.subjectToWearAndTear())
							{
								if((dmg>=E.usesRemaining())&&(!arg4.equalsIgnoreCase("kill")))
									dmg=E.usesRemaining()-1;
								if(dmg>0)
									E.setUsesRemaining(E.usesRemaining()-dmg);
								if(E.usesRemaining()<=0) destroy=true;
							}
							else
							if(arg4.equalsIgnoreCase("kill"))
								destroy=true;
							if(destroy)
							{
								if(lastKnownLocation!=null)
									lastKnownLocation.showHappens(CMMsg.MSG_OK_VISUAL,E.name()+" is destroyed!");
								E.destroy();
							}
						}
					}
				}
				break;
			}
			case 29: // mptrackto
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,0));
				Ability A=CMClass.getAbility("Skill_Track");
				if(A!=null)	
				{
				    altStatusTickable=A;
				    A.invoke(monster,CMParms.parse(arg1),null,true,0);
				    altStatusTickable=null;
				}
				break;
			}
			case 53: // mpwalkto
			{
				String arg1=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0));
				Ability A=CMClass.getAbility("Skill_Track");
				if(A!=null)	
				{
				    altStatusTickable=A;
				    A.invoke(monster,CMParms.parse(arg1+" LANDONLY"),null,true,0);
				    altStatusTickable=null;
				}
				break;
			}
			case 21: //MPENDQUEST
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0).trim());
				Quest Q=s.equals("*")?defaultQuest:CMLib.quests().fetchQuest(s);
				if(Q!=null) Q.stopQuest();
				else
					scriptableError(scripted,"MPENDQUEST","Unknown","Quest: "+s);
				break;
			}
            case 69: // MPSTEPQUEST
            {
                s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0).trim());
                Quest Q=s.equals("*")?defaultQuest:CMLib.quests().fetchQuest(s);
                if(Q!=null) Q.stepQuest();
                else
                    scriptableError(scripted,"MPSTEPQUEST","Unknown","Quest: "+s);
                break;
            }
			case 23: //MPSTARTQUEST
			{
				s=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,0).trim());
				Quest Q=s.equals("*")?defaultQuest:CMLib.quests().fetchQuest(s);
				if(Q!=null) Q.startQuest();
				else
					scriptableError(scripted,"MPSTARTQUEST","Unknown","Quest: "+s);
				break;
			}
			case 64: //MPLOADQUESTOBJ
			{
				String questName=CMParms.getCleanBit(s,1).trim();
				questName=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,questName);
				Quest Q=questName.equals("*")?defaultQuest:CMLib.quests().fetchQuest(questName);
				if(Q==null)
				{
					scriptableError(scripted,"MPLOADQUESTOBJ","Unknown","Quest: "+questName);
					break;
				}
				Object O=Q.getQuestObject(varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2)));
				if(O==null)
				{
					scriptableError(scripted,"MPLOADQUESTOBJ","Unknown","Unknown var "+CMParms.getCleanBit(s,2)+" for Quest: "+questName);
					break;
				}
				String varArg=CMParms.getPastBit(s,2);
				if((varArg.length()!=2)||(!varArg.startsWith("$")))
				{
					scriptableError(scripted,"MPLOADQUESTOBJ","Syntax","Invalid argument var: "+varArg+" for "+scripted.Name());
					break;
				}
				
				char c=varArg.charAt(1);
				if(Character.isDigit(c))
					tmp[CMath.s_int(Character.toString(c))]=O;
				else
				switch(c)
				{
				case 'N': 
				case 'n': if(O instanceof MOB) source=(MOB)O; break;
				case 'I':
				case 'i': if(O instanceof Environmental) scripted=(Environmental)O;
						  if(O instanceof MOB) monster=(MOB)O;
						  break;
				case 'T':
				case 't': if(O instanceof Environmental) target=(Environmental)O; break;
				case 'O':
				case 'o': if(O instanceof Item) primaryItem=(Item)O; break;
				case 'P':
				case 'p': if(O instanceof Item) secondaryItem=(Item)O; break;
				case 'd': 
				case 'D': if(O instanceof Room) lastKnownLocation=(Room)O; break;
				default:
					scriptableError(scripted,"MPLOADQUESTOBJ","Syntax","Invalid argument var: "+varArg+" for "+scripted.Name());
					break;
				}
				break;
			}
			case 22: //MPQUESTWIN
			{
				String whoName=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				MOB M=null;
				if(lastKnownLocation!=null)
					M=lastKnownLocation.fetchInhabitant(whoName);
                if(M==null) M=CMLib.map().getPlayer(whoName);
				if(M!=null) whoName=M.Name();
				if(whoName.length()>0)
				{
					s=CMParms.getPastBitClean(s,1);
					Quest Q=s.equals("*")?defaultQuest:CMLib.quests().fetchQuest(s);
					if(Q!=null) 
                        Q.declareWinner(whoName);
					else
						scriptableError(scripted,"MYQUESTWIN","Unknown","Quest: "+s);
				}
				break;
			}
			case 24: // MPCALLFUNC
			{
				String named=CMParms.getCleanBit(s,1);
				String parms=CMParms.getPastBit(s,1).trim();
				boolean found=false;
				Vector scripts=getScripts();
				for(int v=0;v<scripts.size();v++)
				{
					Vector script2=(Vector)scripts.elementAt(v);
					if(script2.size()<1) continue;
					String trigger=((String)script2.elementAt(0)).toUpperCase().trim();
					if(getTriggerCode(trigger)==17)
					{
						String fnamed=CMParms.getCleanBit(trigger,1);
						if(fnamed.equalsIgnoreCase(named))
						{
							found=true;
							execute(scripted,
									source,
									target,
									monster,
									primaryItem,
									secondaryItem,
									script2,
									varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,parms),
									tmp);
							break;
						}
					}
				}
				if(!found)
					scriptableError(scripted,"MPCALLFUNC","Unknown","Function: "+named);
				break;
			}
			case 27: // MPWHILE
			{
				String conditionStr=(s.substring(2).trim());
				int x=conditionStr.indexOf("(");
				if(x<0)
				{
					scriptableError(scripted,"MPWHILE","Unknown","Condition: "+s);
					break;
				}
				conditionStr=conditionStr.substring(x+1);
				x=-1;
				int depth=0;
				for(int i=0;i<conditionStr.length();i++)
					if(conditionStr.charAt(i)=='(')
						depth++;
					else
					if((conditionStr.charAt(i)==')')&&((--depth)<0))
					{
						x=i;
						break;
					}
				if(x<0)
				{
					scriptableError(scripted,"MPWHILE","Syntax"," no closing ')': "+s);
					break;
				}
				String cmd2=conditionStr.substring(x+1).trim();
				conditionStr=conditionStr.substring(0,x);
				Vector vscript=new Vector();
				vscript.addElement("FUNCTION_PROG MPWHILE_"+Math.random());
				vscript.addElement(cmd2);
				long time=System.currentTimeMillis();
				while((eval(scripted,source,target,monster,primaryItem,secondaryItem,msg,tmp,conditionStr))&&((System.currentTimeMillis()-time)<4000))
					execute(scripted,source,target,monster,primaryItem,secondaryItem,vscript,msg,tmp);
				if((System.currentTimeMillis()-time)>=4000)
				{
					scriptableError(scripted,"MPWHILE","RunTime","4 second limit exceeded: "+conditionStr);
					break;
				}
				break;
			}
			case 26: // MPALARM
			{
				String time=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,1));
				String parms=CMParms.getPastBit(s,1).trim();
				if(CMath.s_int(time.trim())<=0)
				{
					scriptableError(scripted,"MPALARM","Syntax","Bad time "+time);
					break;
				}
				if(parms.length()==0)
				{
					scriptableError(scripted,"MPALARM","Syntax","No command!");
					break;
				}
				Vector vscript=new Vector();
				vscript.addElement("FUNCTION_PROG ALARM_"+time+Math.random());
				vscript.addElement(parms);
				que.insertElementAt(new ScriptableResponse(scripted,source,target,monster,primaryItem,secondaryItem,vscript,CMath.s_int(time.trim()),msg),0);
				break;
			}
			case 37: // mpenable
			{
				Environmental newTarget=getArgumentMOB(CMParms.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg,tmp);
                String cast=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,2));
				String p2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getCleanBit(s,3));
				String m2=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBit(s,3));
				Ability A=null;
				if(cast!=null)
				{
					if(newTarget instanceof MOB) A=((MOB)newTarget).fetchAbility(cast);
					if(A==null) A=CMClass.getAbility(cast);
                    if(A==null)
                    {
                        ExpertiseLibrary.ExpertiseDefinition D=CMLib.expertises().findDefinition(cast,false);
                        if(D==null)
                            scriptableError(scripted,"MPENABLE","Syntax","Unknown skill/expertise: "+cast);
                        else
                        if((newTarget!=null)&&(newTarget instanceof MOB))
                            ((MOB)newTarget).addExpertise(D.ID);
                    }
				}
				if((newTarget!=null)&&(A!=null)&&(newTarget instanceof MOB))
				{
					if(p2.trim().startsWith("++"))
						p2=""+(CMath.s_int(p2.trim().substring(2))+A.proficiency());
					else
					if(p2.trim().startsWith("--"))
						p2=""+(A.proficiency()-CMath.s_int(p2.trim().substring(2)));
					A.setProficiency(CMath.s_int(p2.trim()));
					A.setMiscText(m2);
					if(((MOB)newTarget).fetchAbility(A.ID())==null)
						((MOB)newTarget).addAbility(A);
				}
				break;
			}
			case 38: // mpdisable
			{
				Environmental newTarget=getArgumentMOB(CMParms.getCleanBit(s,1),source,monster,target,primaryItem,secondaryItem,msg,tmp);
                String cast=varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,CMParms.getPastBitClean(s,1));
				if((newTarget!=null)&&(newTarget instanceof MOB))
				{
					Ability A=((MOB)newTarget).findAbility(cast);
					if(A!=null)((MOB)newTarget).delAbility(A);
                    ExpertiseLibrary.ExpertiseDefinition D=CMLib.expertises().findDefinition(cast,false);
                    if((newTarget!=null)&&(newTarget instanceof MOB)&&(D!=null))
                        ((MOB)newTarget).delExpertise(D.ID);
				}
				break;
			}
			default:
				if(cmd.length()>0)
				{
					Vector V=CMParms.parse(varify(source,target,monster,primaryItem,secondaryItem,msg,tmp,s));
					if(V.size()>0)
						monster.doCommand(V);
				}
				break;
			}
		}
        tickStatus=Tickable.STATUS_END;
		return null;
	}

	protected static final Vector empty=new Vector();

	protected Vector getScripts()
	{
		if(CMSecurity.isDisabled("SCRIPTABLE"))
			return empty;
		Vector scripts=null;
		if(getParms().length()>100)
			scripts=(Vector)Resources.getResource("PARSEDPRG: "+getParms().substring(0,100)+getParms().length()+getParms().hashCode());
		else
			scripts=(Vector)Resources.getResource("PARSEDPRG: "+getParms());
		if(scripts==null)
		{
			String script=getParms();
			script=CMStrings.replaceAll(script,"`","'");
			scripts=parseScripts(script);
			if(getParms().length()>100)
				Resources.submitResource("PARSEDPRG: "+getParms().substring(0,100)+getParms().length()+getParms().hashCode(),scripts);
			else
				Resources.submitResource("PARSEDPRG: "+getParms(),scripts);
		}
		return scripts;
	}

	public boolean match(String str, String patt)
	{
		if(patt.trim().equalsIgnoreCase("ALL"))
			return true;
		if(patt.length()==0)
			return true;
		if(str.length()==0)
			return false;
		if(str.equalsIgnoreCase(patt))
			return true;
		return false;
	}
    
    private Item makeCheapItem(Environmental E)
    {
        Item product=null;
        if(E instanceof Item)
            product=(Item)E;
        else
        {
            product=CMClass.getItem("StdItem");
            product.setName(E.Name());
            product.setDisplayText(E.displayText());
            product.setDescription(E.description());
            product.setBaseEnvStats((EnvStats)E.baseEnvStats().copyOf());
            product.recoverEnvStats();
        }
        return product;
    }

	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg)) return false;
		if((affecting==null)||(msg.source()==null))
			return true;
		
		Vector scripts=getScripts();
		Vector script=null;
		boolean tryIt=false;
		String trigger=null;
		int triggerCode=0;
		String str=null;
		for(int v=0;v<scripts.size();v++)
		{
			tryIt=false;
			script=(Vector)scripts.elementAt(v);
			if(script.size()<1) continue;

			trigger=((String)script.elementAt(0)).toUpperCase().trim();
			triggerCode=getTriggerCode(trigger);
			switch(triggerCode)
			{
			case 42: // cnclmsg_prog
				if(canTrigger(42))
				{
					trigger=trigger.substring(12).trim();
					String command=CMParms.getCleanBit(trigger,0).toUpperCase().trim();
					if(msg.isSource(command)||msg.isTarget(command)||msg.isOthers(command))
					{
						trigger=CMParms.getPastBit(trigger.trim(),0).trim().toUpperCase();
						str="";
						if((msg.source().session()!=null)&&(msg.source().session().previousCMD()!=null))
							str=" "+CMParms.combine(msg.source().session().previousCMD(),0).toUpperCase()+" ";
						if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
						{
							trigger=trigger.substring(1).trim();
							if(match(str.trim(),trigger))
								tryIt=true;
						}
						else
						if(trigger.trim().equalsIgnoreCase("ALL"))
							tryIt=true;
						else
						{
							int num=CMParms.numBits(trigger);
							for(int i=0;i<num;i++)
							{
								String t=CMParms.getCleanBit(trigger,i).trim();
								if(str.indexOf(" "+t+" ")>=0)
								{
									str=(t.trim()+" "+str.trim()).trim();
									tryIt=true;
									break;
								}
							}
						}
					}
				}
				break;
			}
			if(tryIt)
			{
				MOB monster=getScriptableMOB(affecting);
				if(lastKnownLocation==null) lastKnownLocation=msg.source().location();
				if((monster==null)||(monster.amDead())||(lastKnownLocation==null)) return true;
				Item defaultItem=(affecting instanceof Item)?(Item)affecting:null;
				Item Tool=null;
				if(msg.tool() instanceof Item)
					Tool=(Item)msg.tool();
				if(Tool==null) Tool=defaultItem;
				String resp=null;
				if(msg.target() instanceof MOB)
                    resp=execute(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,str,new Object[10]);
				else
				if(msg.target() instanceof Item)
					resp=execute(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,str,new Object[10]);
				else
					resp=execute(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,str,new Object[10]);
				if((resp!=null)&&(resp.equalsIgnoreCase("CANCEL")))
					return false;
			}
		}
		return true;
	}
		
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((affecting==null)||(msg.source()==null)) 
			return;
		
		MOB monster=getScriptableMOB(affecting);
		
		if(lastKnownLocation==null) lastKnownLocation=msg.source().location();
		if((monster==null)||(monster.amDead())||(lastKnownLocation==null)) return;
		
		Item defaultItem=(affecting instanceof Item)?(Item)affecting:null;
		MOB eventMob=monster;
		if((defaultItem!=null)&&(defaultItem.owner() instanceof MOB))
			eventMob=(MOB)defaultItem.owner();

		Vector scripts=getScripts();

		if(msg.amITarget(eventMob)
		&&(!msg.amISource(monster))
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(msg.source()!=monster))
			lastToHurtMe=msg.source();
		Vector script=null;
		for(int v=0;v<scripts.size();v++)
		{
			script=(Vector)scripts.elementAt(v);
			if(script.size()<1) continue;

			String trigger=((String)script.elementAt(0)).toUpperCase().trim();
			int triggerCode=getTriggerCode(trigger);
			int targetMinorTrigger=-1;
			switch(triggerCode)
			{
			case 1: // greet_prog
				if((msg.targetMinor()==CMMsg.TYP_ENTER)
				&&(msg.amITarget(lastKnownLocation))
				&&(!msg.amISource(eventMob))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
				&&canTrigger(1)
				&&((!(affecting instanceof MOB))||CMLib.flags().canSenseMoving(msg.source(),(MOB)affecting)))
				{
					int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
					if(CMLib.dice().rollPercentage()<prcnt)
					{
						que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null));
						return;
					}
				}
				break;
			case 2: // all_greet_prog
				if((msg.targetMinor()==CMMsg.TYP_ENTER)&&canTrigger(2)
				&&(msg.amITarget(lastKnownLocation))
				&&(!msg.amISource(eventMob))
				&&(canActAtAll(monster)))
				{
					int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
					if(CMLib.dice().rollPercentage()<prcnt)
					{
						que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null));
						return;
					}
				}
				break;
			case 3: // speech_prog
				if(((msg.sourceMinor()==CMMsg.TYP_SPEAK)||(msg.targetMinor()==CMMsg.TYP_SPEAK))&&canTrigger(3)
				&&(!msg.amISource(monster))
				&&(((msg.othersMessage()!=null)&&((msg.tool()==null)||(!(msg.tool() instanceof Ability))||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_LANGUAGE)))
                   ||((msg.target()==monster)&&(msg.targetMessage()!=null)&&(msg.tool()==null)))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
                    String str=null;
                    if(msg.othersMessage()!=null)
                        str=CMStrings.replaceAll(CMStrings.getSayFromMessage(msg.othersMessage().toUpperCase()),"`","'");
                    else
                        str=CMStrings.replaceAll(CMStrings.getSayFromMessage(msg.targetMessage().toUpperCase()),"`","'");
					str=(" "+str+" ").toUpperCase();
                    str=CMStrings.removeColors(str);
                    str=CMStrings.replaceAll(str,"\n\r"," ");
					trigger=trigger.substring(11).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim();
						if(match(str.trim(),trigger))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i);
                            int x=str.indexOf(" "+t+" ");
							if(x>=0)
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,defaultItem,null,script,1,str.substring(x).trim()));
								return;
							}
						}
					}
				}
				break;
			case 4: // give_prog
				if((msg.targetMinor()==CMMsg.TYP_GIVE)
                &&canTrigger(4)
				&&((msg.amITarget(monster))
                        ||(msg.tool()==affecting)
                        ||(affecting instanceof Room)
                        ||(affecting instanceof Area))
				&&(!msg.amISource(monster))
				&&(msg.tool() instanceof Item)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(9).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if((trigger.equalsIgnoreCase(msg.tool().Name()))
						||(msg.tool().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.tool(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.tool().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.tool().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.tool(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 40: // llook_prog
				if((msg.targetMinor()==CMMsg.TYP_EXAMINE)&&canTrigger(40)
				&&((msg.amITarget(affecting))||(affecting instanceof Area))
				&&(!msg.amISource(monster))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(10).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 41: // execmsg_prog
				if(canTrigger(41))
				{
					trigger=trigger.substring(12).trim();
					String command=CMParms.getCleanBit(trigger,0).toUpperCase().trim();
					if(msg.isSource(command)||msg.isTarget(command)||msg.isOthers(command))
					{
						trigger=CMParms.getPastBit(trigger.trim(),0).trim().toUpperCase();
						String str="";
						if((msg.source().session()!=null)&&(msg.source().session().previousCMD()!=null))
							str=" "+CMParms.combine(msg.source().session().previousCMD(),0).toUpperCase()+" ";
						boolean doIt=false;
						if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
						{
							trigger=trigger.substring(1).trim();
							if(match(str.trim(),trigger))
								doIt=true;
						}
						else
						if(trigger.trim().equalsIgnoreCase("ALL"))
							doIt=true;
						else
						{
							int num=CMParms.numBits(trigger);
							for(int i=0;i<num;i++)
							{
								String t=CMParms.getCleanBit(trigger,i).trim();
								if(str.indexOf(" "+t+" ")>=0)
								{
									str=(t.trim()+" "+str.trim()).trim();
									doIt=true;
									break;
								}
							}
						}
						if(doIt)
						{
							Item Tool=null;
							if(msg.tool() instanceof Item)
								Tool=(Item)msg.tool();
							if(Tool==null) Tool=defaultItem;
							if(msg.target() instanceof MOB)
								que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str));
							else
							if(msg.target() instanceof Item)
								que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,Tool,(Item)msg.target(),script,1,str));
							else
								que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str));
							return;
						}
					}
				}
				break;
			case 39: // look_prog
				if((msg.targetMinor()==CMMsg.TYP_LOOK)&&canTrigger(39)
				&&((msg.amITarget(affecting))||(affecting instanceof Area))
				&&(!msg.amISource(monster))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(9).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 20: // get_prog
				if((msg.targetMinor()==CMMsg.TYP_GET)&&canTrigger(20)
				&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
				&&(!msg.amISource(monster))
				&&(msg.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(8).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 22: // drop_prog
				if((msg.targetMinor()==CMMsg.TYP_DROP)&&canTrigger(22)
				&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
				&&(!msg.amISource(monster))
				&&(msg.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(9).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							if(msg.target() instanceof Coins)
								execute(affecting,msg.source(),monster,monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,null,new Object[10]);
							else
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								if(msg.target() instanceof Coins)
									execute(affecting,msg.source(),monster,monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,null,new Object[10]);
								else
									que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 24: // remove_prog
				if((msg.targetMinor()==CMMsg.TYP_REMOVE)&&canTrigger(24)
				&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
				&&(!msg.amISource(monster))
				&&(msg.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(11).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 34: // open_prog
				if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_OPEN;
			case 35: // close_prog
				if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_CLOSE;
			case 36: // lock_prog 
				if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_LOCK;
			case 37: // unlock_prog
			{
				if(targetMinorTrigger<0) targetMinorTrigger=CMMsg.TYP_UNLOCK;
				if((msg.targetMinor()==targetMinorTrigger)&&canTrigger(triggerCode)
				&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
				&&(!msg.amISource(monster))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					switch(triggerCode)
					{
					case 34:
					case 36:
						trigger=trigger.substring(9).trim(); break;
					case 35:
						trigger=trigger.substring(10).trim(); break;
					case 37:
						trigger=trigger.substring(11).trim(); break;
					}
					Item I=(msg.target() instanceof Item)?(Item)msg.target():defaultItem;
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,I,defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,I,defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			}
			case 25: // consume_prog
				if(((msg.targetMinor()==CMMsg.TYP_EAT)||(msg.targetMinor()==CMMsg.TYP_DRINK))
				&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
				&&(!msg.amISource(monster))&&canTrigger(25)
				&&(msg.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(12).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 21: // put_prog
				if((msg.targetMinor()==CMMsg.TYP_PUT)&&canTrigger(21)
				&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
				&&(msg.tool() instanceof Item)
				&&(!msg.amISource(monster))
				&&(msg.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(8).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.tool().Name().toUpperCase())>=0)
						||(msg.tool().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							if((msg.tool() instanceof Coins)&&(((Item)msg.target()).owner() instanceof Room))
								execute(affecting,msg.source(),monster,monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,null,new Object[10]);
							else
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),(Item)msg.tool(),script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.tool().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.tool().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								if((msg.tool() instanceof Coins)&&(((Item)msg.target()).owner() instanceof Room))
									execute(affecting,msg.source(),monster,monster,(Item)msg.target(),(Item)((Item)msg.target()).copyOf(),script,null,new Object[10]);
								else
									que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),(Item)msg.tool(),script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 27: // buy_prog
				if((msg.targetMinor()==CMMsg.TYP_BUY)&&canTrigger(27)
				&&((!(affecting instanceof ShopKeeper))
                    ||msg.amITarget(affecting))
				&&(!msg.amISource(monster))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(8).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.tool().Name().toUpperCase())>=0)
						||(msg.tool().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
                            Item product=makeCheapItem(msg.tool());
							if((product instanceof Coins)
                            &&(product.owner() instanceof Room))
								execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,null,new Object[10]);
							else
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,product,product,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.tool().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.tool().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
                                Item product=makeCheapItem(msg.tool());
								if((product instanceof Coins)
                                &&(product.owner() instanceof Room))
									execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,null,new Object[10]);
								else
									que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,product,product,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 28: // sell_prog
				if((msg.targetMinor()==CMMsg.TYP_SELL)&&canTrigger(28)
				&&((msg.amITarget(affecting))||(!(affecting instanceof ShopKeeper)))
				&&(!msg.amISource(monster))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(8).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.tool().Name().toUpperCase())>=0)
						||(msg.tool().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
                            Item product=makeCheapItem(msg.tool());
                            if((product instanceof Coins)
                            &&(product.owner() instanceof Room))
                                execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,null,new Object[10]);
                            else
                                que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,product,product,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.tool().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.tool().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
                                Item product=makeCheapItem(msg.tool());
                                if((product instanceof Coins)
                                &&(product.owner() instanceof Room))
                                    execute(affecting,msg.source(),monster,monster,product,(Item)product.copyOf(),script,null,new Object[10]);
                                else
                                    que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,product,product,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 23: // wear_prog
				if(((msg.targetMinor()==CMMsg.TYP_WEAR)
					||(msg.targetMinor()==CMMsg.TYP_HOLD)
					||(msg.targetMinor()==CMMsg.TYP_WIELD))
				&&((msg.amITarget(affecting))||(affecting instanceof Room)||(affecting instanceof Area)||(affecting instanceof MOB))
				&&(!msg.amISource(monster))&&canTrigger(23)
				&&(msg.target() instanceof Item)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(9).trim();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim().toUpperCase();
						if(((" "+trigger+" ").indexOf(msg.target().Name().toUpperCase())>=0)
						||(msg.target().ID().equalsIgnoreCase(trigger))
						||(trigger.equalsIgnoreCase("ALL")))
						{
							que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
							return;
						}
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).toUpperCase();
							if(((" "+msg.target().Name().toUpperCase()+" ").indexOf(" "+t+" ")>=0)
							||(msg.target().ID().equalsIgnoreCase(t))
							||(t.equalsIgnoreCase("ALL")))
							{
								que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.target(),defaultItem,script,1,null));
								return;
							}
						}
					}
				}
				break;
			case 19: // bribe_prog
				if((msg.targetMinor()==CMMsg.TYP_GIVE)
				&&(msg.amITarget(eventMob)||(!(affecting instanceof MOB)))
				&&(!msg.amISource(monster))&&canTrigger(19)
				&&(msg.tool() instanceof Coins)
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					trigger=trigger.substring(10).trim();
					if(trigger.toUpperCase().startsWith("ANY"))
						trigger=trigger.substring(3).trim();
					else
	                if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(monster)))
	                	break;
					double t=0.0;
					if(CMath.isDouble(trigger.trim()))
					    t=CMath.s_double(trigger.trim());
					else
					    t=new Integer(CMath.s_int(trigger.trim())).doubleValue();
					if((((Coins)msg.tool()).getTotalValue()>=t)
					||(trigger.equalsIgnoreCase("ALL")))
					{
						que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,(Item)msg.tool(),defaultItem,script,1,null));
						return;
					}
				}
				break;
			case 8: // entry_prog
				if((msg.targetMinor()==CMMsg.TYP_ENTER)&&canTrigger(8)
				&&(msg.amISource(eventMob)
					||(msg.target()==affecting)
					||(msg.tool()==affecting)
					||(affecting instanceof Item))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
					if(CMLib.dice().rollPercentage()<prcnt)
					{
						que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null));
						return;
					}
				}
				break;
			case 9: // exit prog
				if((msg.targetMinor()==CMMsg.TYP_LEAVE)&&canTrigger(9)
				&&(msg.amITarget(lastKnownLocation))
				&&(!msg.amISource(eventMob))
				&&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB))))
				{
					int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
					if(CMLib.dice().rollPercentage()<prcnt)
					{
						que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null));
						return;
					}
				}
				break;
			case 10: // death prog
				if((msg.sourceMinor()==CMMsg.TYP_DEATH)&&canTrigger(10)
				&&(msg.amISource(eventMob)||(!(affecting instanceof MOB))))
				{
					MOB ded=msg.source();
					MOB src=lastToHurtMe;
                    if(msg.tool() instanceof MOB)
                        src=(MOB)msg.tool();
					if((src==null)||(src.location()!=monster.location()))
					   src=ded;
					execute(affecting,src,ded,ded,defaultItem,null,script,null,new Object[10]);
					return;
				}
				break;
			case 26: // damage prog
				if((msg.targetMinor()==CMMsg.TYP_DAMAGE)&&canTrigger(26)
				&&(msg.amITarget(eventMob)||(msg.tool()==affecting)))
				{
					Item I=null;
					if(msg.tool() instanceof Item)
						I=(Item)msg.tool();
					execute(affecting,msg.source(),msg.target(),eventMob,defaultItem,I,script,""+msg.value(),new Object[10]);
					return;
				}
				break;
            case 29: // login_prog
                if(!registeredSpecialEvents.contains(new Integer(CMMsg.TYP_LOGIN)))
                {
                    CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_LOGIN);
                    registeredSpecialEvents.add(new Integer(CMMsg.TYP_LOGIN));
                }
                if((msg.sourceMinor()==CMMsg.TYP_LOGIN)&&canTrigger(29)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
                &&(!CMLib.flags().isCloaked(msg.source())))
                {
                    int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null));
                        return;
                    }
                }
                break;
            case 32: // level_prog
                if(!registeredSpecialEvents.contains(new Integer(CMMsg.TYP_LEVEL)))
                {
                    CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_LEVEL);
                    registeredSpecialEvents.add(new Integer(CMMsg.TYP_LEVEL));
                }
                if((msg.sourceMinor()==CMMsg.TYP_LEVEL)&&canTrigger(32)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
                &&(!CMLib.flags().isCloaked(msg.source())))
                {
                    int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null));
                        return;
                    }
                }
                break;
            case 30: // logoff_prog
                if((msg.sourceMinor()==CMMsg.TYP_QUIT)&&canTrigger(30)
                &&(canFreelyBehaveNormal(monster)||(!(affecting instanceof MOB)))
                &&(!CMLib.flags().isCloaked(msg.source())))
                {
                    int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
                    if(CMLib.dice().rollPercentage()<prcnt)
                    {
                        que.addElement(new ScriptableResponse(affecting,msg.source(),monster,monster,defaultItem,null,script,1,null));
                        return;
                    }
                }
                break;
			case 12: // mask_prog
				if(!canTrigger(12)) 
					break;
			case 18: // act_prog
				if((!msg.amISource(monster))
				||((triggerCode==18)&&(!canTrigger(18)))) 
					break;
			case 43: // imask_prog
				if((triggerCode!=43)||(msg.amISource(monster)&&canTrigger(43)))
				{
					boolean doIt=false;
					String str=msg.othersMessage();
					if(str==null) str=msg.targetMessage();
					if(str==null) str=msg.sourceMessage();
					if(str==null) break;
					str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false);
                    str=CMStrings.removeColors(str);
                    str=" "+CMStrings.replaceAll(str,"\n\r"," ").toUpperCase().trim()+" ";
					trigger=CMParms.getPastBit(trigger.trim(),0).trim().toUpperCase();
					if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
					{
						trigger=trigger.substring(1).trim();
						if(match(str.trim(),trigger))
							doIt=true;
					}
					else
					{
						int num=CMParms.numBits(trigger);
						for(int i=0;i<num;i++)
						{
							String t=CMParms.getCleanBit(trigger,i).trim();
							if(str.indexOf(" "+t+" ")>=0)
							{
								str=t;
								doIt=true;
								break;
							}
						}
					}
					if(doIt)
					{
						Item Tool=null;
						if(msg.tool() instanceof Item)
							Tool=(Item)msg.tool();
						if(Tool==null) Tool=defaultItem;
						if(msg.target() instanceof MOB)
							que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str));
						else
						if(msg.target() instanceof Item)
							que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,(Item)msg.target(),script,1,str));
						else
							que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,defaultItem,script,1,str));
						return;
					}
				}
				break;
			case 38: // social prog
				if(!msg.amISource(monster)
				&&canTrigger(38)
				&&(msg.tool() instanceof Social))
				{
					trigger=CMParms.getPastBit(trigger.trim(),0);
					if(((Social)msg.tool()).Name().toUpperCase().startsWith(trigger.toUpperCase()))
					{
						Item Tool=defaultItem;
						if(msg.target() instanceof MOB)
							que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,msg.tool().Name()));
						else
						if(msg.target() instanceof Item)
							que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,(Item)msg.target(),script,1,msg.tool().Name()));
						else
							que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,defaultItem,script,1,msg.tool().Name()));
						return;
					}
				}
				break;
            case 33: // channel prog
                if(!registeredSpecialEvents.contains(new Integer(CMMsg.TYP_CHANNEL)))
                {
                    CMLib.map().addGlobalHandler(affecting,CMMsg.TYP_CHANNEL);
                    registeredSpecialEvents.add(new Integer(CMMsg.TYP_CHANNEL));
                }
                if(!msg.amISource(monster)
                &&(CMath.bset(msg.othersMajor(),CMMsg.MASK_CHANNEL))
                &&canTrigger(33))
                {
                    boolean doIt=false;
                    String channel=CMParms.getBit(trigger.trim(),1);
                    int channelInt=msg.othersMinor()-CMMsg.TYP_CHANNEL;
                    String str=null;
                    if(channel.equalsIgnoreCase(CMLib.channels().getChannelName(channelInt)))
                    {
                        str=msg.sourceMessage();
                        if(str==null) str=msg.othersMessage();
                        if(str==null) str=msg.targetMessage();
                        if(str==null) break;
                        str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false).toUpperCase().trim();
                        int dex=str.indexOf("["+channel+"]");
                        if(dex>0) 
                            str=str.substring(dex+2+channel.length()).trim();
                        else
                        {
                            dex=str.indexOf("'");
                            int edex=str.lastIndexOf("'");
                            if(edex>dex) str=str.substring(dex+1,edex);
                        }
                        str=" "+CMStrings.removeColors(str)+" ";
                        str=CMStrings.replaceAll(str,"\n\r"," ");
                        trigger=CMParms.getPastBit(trigger.trim(),1);
                        if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
                        {
                            trigger=trigger.substring(1).trim().toUpperCase();
                            if(match(str.trim(),trigger))
                                doIt=true;
                        }
                        else
                        {
                            int num=CMParms.numBits(trigger);
                            for(int i=0;i<num;i++)
                            {
                                String t=CMParms.getCleanBit(trigger,i).trim();
                                if(str.indexOf(" "+t+" ")>=0)
                                {
                                    str=t;
                                    doIt=true;
                                    break;
                                }
                            }
                        }
                    }
                    if(doIt)
                    {
                        Item Tool=null;
                        if(msg.tool() instanceof Item)
                            Tool=(Item)msg.tool();
                        if(Tool==null) Tool=defaultItem;
                        if(msg.target() instanceof MOB)
                            que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str));
                        else
                        if(msg.target() instanceof Item)
                            que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,(Item)msg.target(),script,1,str));
                        else
                            que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,defaultItem,script,1,str));
                        return;
                    }
                }
                break;
            case 31: // regmask prog
                if(!msg.amISource(monster)&&canTrigger(31))
                {
                    boolean doIt=false;
                    String str=msg.othersMessage();
                    if(str==null) str=msg.targetMessage();
                    if(str==null) str=msg.sourceMessage();
                    if(str==null) break;
                    str=CMLib.coffeeFilter().fullOutFilter(null,monster,msg.source(),msg.target(),msg.tool(),str,false);
                    trigger=CMParms.getPastBit(trigger.trim(),0);
                    if(CMParms.getCleanBit(trigger,0).equalsIgnoreCase("p"))
                        doIt=str.trim().equals(trigger.substring(1).trim());
                    else
                    {
                        Pattern P=(Pattern)patterns.get(trigger);
                        if(P==null)
                        {
                            P=Pattern.compile(trigger, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                            patterns.put(trigger,P);
                        }
                        Matcher M=P.matcher(str);
                        doIt=M.find();
                        if(doIt) str=str.substring(M.start()).trim();
                    }
                    if(doIt)
                    {
                        Item Tool=null;
                        if(msg.tool() instanceof Item)
                            Tool=(Item)msg.tool();
                        if(Tool==null) Tool=defaultItem;
                        if(msg.target() instanceof MOB)
                            que.addElement(new ScriptableResponse(affecting,msg.source(),msg.target(),monster,Tool,defaultItem,script,1,str));
                        else
                        if(msg.target() instanceof Item)
                            que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,(Item)msg.target(),script,1,str));
                        else
                            que.addElement(new ScriptableResponse(affecting,msg.source(),null,monster,Tool,defaultItem,script,1,str));
                        return;
                    }
                }
                break;
			}
		}
	}

	protected int getTriggerCode(String trigger)
	{
		int x=trigger.indexOf(" ");
		Integer I=null;
		if(x<0)
			I=(Integer)progH.get(trigger.toUpperCase().trim());
		else
			I=(Integer)progH.get(trigger.substring(0,x).toUpperCase().trim());
		if(I==null) return 0;
		return I.intValue();
	}

	public MOB backupMOB=null;
	public MOB getScriptableMOB(Tickable ticking)
	{
		MOB mob=null;
		if(ticking instanceof MOB)
		{
			mob=(MOB)ticking;
			if(!mob.amDead())
				lastKnownLocation=mob.location();
		}
		else
		if(ticking instanceof Environmental)
		{
            Room R=CMLib.map().roomLocation((Environmental)ticking);
            if(R!=null) lastKnownLocation=R;

			if((backupMOB==null)||(backupMOB.amDestroyed())||(backupMOB.amDead()))
			{
				backupMOB=CMClass.getMOB("StdMOB");
				if(backupMOB!=null)
				{
					backupMOB.setName(ticking.name());
					backupMOB.setDisplayText(ticking.name()+" is here.");
					backupMOB.setDescription("");
					mob=backupMOB;
					if(backupMOB.location()!=lastKnownLocation)
						backupMOB.setLocation(lastKnownLocation);
				}
			}
			else
			{
				mob=backupMOB;
				if(backupMOB.location()!=lastKnownLocation)
					backupMOB.setLocation(lastKnownLocation);
			}
		}
		return mob;
	}
    
    public boolean canTrigger(int triggerCode)
    {
        Long L=(Long)noTrigger.get(new Integer(triggerCode));
        if(L==null) return true;
        if(System.currentTimeMillis()<L.longValue())
            return false;
        noTrigger.remove(new Integer(triggerCode));
        return true;
    }

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
        if(!CMProps.getBoolVar(CMProps.SYSTEMB_MUDSTARTED))
            return false;
        
		MOB mob=getScriptableMOB(ticking);
		Item defaultItem=(ticking instanceof Item)?(Item)ticking:null;

		if((mob==null)||(lastKnownLocation==null))
		{
			altStatusTickable=null;
			return true;
		}

		Environmental affecting=(ticking instanceof Environmental)?((Environmental)ticking):null;

		Vector scripts=getScripts();

		int triggerCode=-1;
		for(int thisScriptIndex=0;thisScriptIndex<scripts.size();thisScriptIndex++)
		{
			Vector script=(Vector)scripts.elementAt(thisScriptIndex);
			String trigger="";
			if(script.size()>0)
				trigger=((String)script.elementAt(0)).toUpperCase().trim();
			triggerCode=getTriggerCode(trigger);
		    tickStatus=Tickable.STATUS_BEHAVIOR+triggerCode;
			switch(triggerCode)
			{
			case 5: // rand_Prog
				if((!mob.amDead())&&canTrigger(5))
				{
					int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
					if(CMLib.dice().rollPercentage()<prcnt)
						execute(affecting,mob,mob,mob,defaultItem,null,script,null,new Object[10]);
				}
				break;
			case 16: // delay_prog
				if((!mob.amDead())&&canTrigger(16))
				{
					int targetTick=-1;
					if(delayTargetTimes.containsKey(new Integer(thisScriptIndex)))
						targetTick=((Integer)delayTargetTimes.get(new Integer(thisScriptIndex))).intValue();
					else
					{
						int low=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
						int high=CMath.s_int(CMParms.getCleanBit(trigger,2).trim());
						if(high<low) high=low;
						targetTick=CMLib.dice().roll(1,high-low+1,low-1);
						delayTargetTimes.put(new Integer(thisScriptIndex),new Integer(targetTick));
					}
					int delayProgCounter=0;
					if(delayProgCounters.containsKey(new Integer(thisScriptIndex)))
						delayProgCounter=((Integer)delayProgCounters.get(new Integer(thisScriptIndex))).intValue();
					else
						delayProgCounters.put(new Integer(thisScriptIndex),new Integer(0));
					if(delayProgCounter==targetTick)
					{
						execute(affecting,mob,mob,mob,defaultItem,null,script,null,new Object[10]);
						delayProgCounter=-1;
					}
					delayProgCounters.remove(new Integer(thisScriptIndex));
					delayProgCounters.put(new Integer(thisScriptIndex),new Integer(delayProgCounter+1));
				}
				break;
			case 7: // fightProg
				if((mob.isInCombat())&&(!mob.amDead())&&canTrigger(7))
				{
					int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
					if(CMLib.dice().rollPercentage()<prcnt)
						execute(affecting,mob.getVictim(),mob,mob,defaultItem,null,script,null,new Object[10]);
				}
				else
				if((ticking instanceof Item)
                &&canTrigger(7)
				&&(((Item)ticking).owner() instanceof MOB)
				&&(((MOB)((Item)ticking).owner()).isInCombat()))
				{
					int prcnt=CMath.s_int(CMParms.getCleanBit(trigger,1).trim());
					if(CMLib.dice().rollPercentage()<prcnt)
					{
					    MOB M=(MOB)((Item)ticking).owner();
					    if(!M.amDead())
							execute(affecting,M,mob.getVictim(),mob,defaultItem,null,script,null,new Object[10]);
					}
				}
				break;
			case 11: // hitprcnt
				if((mob.isInCombat())&&(!mob.amDead())&&canTrigger(11))
				{
					int floor=(int)Math.round(CMath.mul(CMath.div(CMath.s_int(CMParms.getCleanBit(trigger,1).trim()),100.0),mob.maxState().getHitPoints()));
					if(mob.curState().getHitPoints()<=floor)
						execute(affecting,mob.getVictim(),mob,mob,defaultItem,null,script,null,new Object[10]);
				}
				else
				if((ticking instanceof Item)
                &&canTrigger(11)
				&&(((Item)ticking).owner() instanceof MOB)
				&&(((MOB)((Item)ticking).owner()).isInCombat()))
				{
				    MOB M=(MOB)((Item)ticking).owner();
				    if(!M.amDead())
				    {
						int floor=(int)Math.round(CMath.mul(CMath.div(CMath.s_int(CMParms.getCleanBit(trigger,1).trim()),100.0),M.maxState().getHitPoints()));
						if(M.curState().getHitPoints()<=floor)
							execute(affecting,M,mob.getVictim(),mob,defaultItem,null,script,null,new Object[10]);
				    }
				}
				break;
			case 6: // once_prog
				if(!oncesDone.contains(script)&&canTrigger(6))
				{
					oncesDone.addElement(script);
					execute(affecting,mob,mob,mob,defaultItem,null,script,null,new Object[10]);
				}
				break;
			case 14: // time_prog
				if((mob.location()!=null)
                &&canTrigger(14)
				&&(!mob.amDead()))
				{
					int lastTimeProgDone=-1;
					if(lastTimeProgsDone.containsKey(new Integer(thisScriptIndex)))
						lastTimeProgDone=((Integer)lastTimeProgsDone.get(new Integer(thisScriptIndex))).intValue();
					int time=mob.location().getArea().getTimeObj().getTimeOfDay();
					if(lastTimeProgDone!=time)
					{
						boolean done=false;
						for(int i=1;i<CMParms.numBits(trigger);i++)
						{
							if(time==CMath.s_int(CMParms.getCleanBit(trigger,i).trim()))
							{
								done=true;
								execute(affecting,mob,mob,mob,defaultItem,null,script,null,new Object[10]);
								lastTimeProgsDone.remove(new Integer(thisScriptIndex));
								lastTimeProgsDone.put(new Integer(thisScriptIndex),new Integer(time));
								break;
							}
						}
						if(!done)
						    lastTimeProgsDone.remove(new Integer(thisScriptIndex));
					}
				}
				break;
			case 15: // day_prog
				if((mob.location()!=null)&&canTrigger(15)
				&&(!mob.amDead()))
				{
					int lastDayProgDone=-1;
					if(lastDayProgsDone.containsKey(new Integer(thisScriptIndex)))
						lastDayProgDone=((Integer)lastDayProgsDone.get(new Integer(thisScriptIndex))).intValue();
					int day=mob.location().getArea().getTimeObj().getDayOfMonth();
					if(lastDayProgDone!=day)
					{
						boolean done=false;
						for(int i=1;i<CMParms.numBits(trigger);i++)
						{
							if(day==CMath.s_int(CMParms.getCleanBit(trigger,i).trim()))
							{
								done=true;
								execute(affecting,mob,mob,mob,defaultItem,null,script,null,new Object[10]);
								lastDayProgsDone.remove(new Integer(thisScriptIndex));
								lastDayProgsDone.put(new Integer(thisScriptIndex),new Integer(day));
								break;
							}
						}
						if(!done)
							lastDayProgsDone.remove(new Integer(thisScriptIndex));
					}
				}
				break;
			case 13: // questtimeprog
				if(!oncesDone.contains(script)&&canTrigger(13))
				{
					Quest Q=CMParms.getCleanBit(trigger,1).equals("*")?defaultQuest:CMLib.quests().fetchQuest(CMParms.getCleanBit(trigger,1));
					if((Q!=null)&&(Q.running())&&(!Q.stopping()))
					{
						int time=CMath.s_int(CMParms.getCleanBit(trigger,2).trim());
						if(time>=Q.minsRemaining())
						{
							oncesDone.addElement(script);
							execute(affecting,mob,mob,mob,defaultItem,null,script,null,new Object[10]);
						}
					}
				}
				break;
			default:
			    break;
			}
		}
	    tickStatus=Tickable.STATUS_BEHAVIOR+100;
	    dequeResponses();
		altStatusTickable=null;
		return true;
	}
	
	public void dequeResponses()
	{
		try{
		    tickStatus=Tickable.STATUS_BEHAVIOR+100;
			for(int q=que.size()-1;q>=0;q--)
			{
				ScriptableResponse SB=null;
				try{SB=(ScriptableResponse)que.elementAt(q);}catch(ArrayIndexOutOfBoundsException x){continue;}
				if(SB.checkTimeToExecute())
                {
                    execute(SB.h,SB.s,SB.t,SB.m,SB.pi,SB.si,SB.scr,SB.message,new Object[10]);
                    que.removeElement(SB);
                }
			}
		}catch(Exception e){Log.errOut("Scriptable",e);}
	}
}
