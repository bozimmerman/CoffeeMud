package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class LawBook extends GenReadable
{
	public String ID(){	return "LawBook";}
	public LawBook()
	{
		super();
		setName("a law book");
		setDisplayText("a law book sits here.");
		setDescription("Enter `READ [PAGE NUMBER] \"law book\"` to read an entry.%0D%0AUse your WRITE skill to add new entries. ");
		material=EnvResource.RESOURCE_PAPER;
		isReadable=true;
	}

	public Environmental newInstance()
	{
		return new LawBook();
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_WRITE:
			if(!affect.source().isASysOp(affect.source().location()))
			{
				affect.source().tell("You are not allowed to write on "+name());
				return false;
			}
			return true;
		}
		return super.okAffect(myHost,affect);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		switch(affect.targetMinor())
		{
		case Affect.TYP_READSOMETHING:
			if(!Sense.canBeSeenBy(this,mob))
				mob.tell("You can't see that!");
			else
			if(!mob.isMonster())
			{
				Area A=CMMap.getArea(text());
				Vector VB=null;
				if(A!=null)	VB=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
				if((VB==null)||(VB.size()==0))
				{
					affect.source().tell("The pages appear blank, and damaged."); 
					return;
				}
				Behavior B=(Behavior)VB.firstElement();
				VB=new Vector();
				VB.addElement(new Integer(Law.MOD_LEGALINFO));
				B.modifyBehavior(A,mob,VB);
				Law theLaw=(Law)VB.firstElement();
				
				int which=-1;
				if(Util.s_long(affect.targetMessage())>0)
					which=Util.s_int(affect.targetMessage());
				try{
					if(which<1)
					{
						if(mob.session()!=null)
						{
							StringBuffer str=new StringBuffer();
							str.append("^hLaws of "+A.name()+"^?\n\r\n\r");
							str.append(getFromTOC("TOC"));
							mob.session().colorOnlyPrintln(str.toString());
						}
					}
					else
					switch(which)
					{
					case 1:
						if(mob.session()!=null)
							mob.session().colorOnlyPrintln(getFromTOC("P1"+(theLaw.hasModifiableLaws()?"MOD":"")+(theLaw.hasModifiableNames()?"NAM":"")));
						break;
					case 2:	doOfficersAndJudges(A,B,theLaw,mob); break;
					case 3:	doVictimsOfCrime(A,B,theLaw,mob); break;
					case 4: doJailPolicy(A,B,theLaw,mob); break;
					}
				}
				catch(Exception e)
				{
					Log.errOut("LawBook",e);
				}
			}
			return;
		case Affect.TYP_WRITE:
			try
			{
				Area A=CMMap.getArea(text());
				Vector VB=null;
				if(A!=null)	VB=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
				if((VB==null)||(VB.size()==0))
				{
					affect.source().tell("The pages appear blank, and too damaged to write on.");
					return;
				}
				Behavior B=(Behavior)VB.firstElement();
				
				if(!mob.isMonster())
				{
				}
				return;
			}
			catch(Exception e)
			{
				Log.errOut("LawBook",e);
			}
			return;
		}
		super.affect(myHost,affect);
	}
	
	public String getFromTOC(String tag)
	{
		Properties lawProps=(Properties)Resources.getResource("LAWBOOKTOC");
		try{
			if((lawProps==null)||(lawProps.isEmpty()))
			{
				lawProps.load(new FileInputStream("resources"+File.separatorChar+"lawtoc.ini"));
				Resources.submitResource("LAWBOOKTOC",lawProps);
			}
			String s=(String)lawProps.get(tag);
			if(s==null) return "";
			return s;
		}
		catch(Exception e)
		{
			Log.errOut("LawBook",e);
		}
		return "";
	}
	
	public void changeTheLaw(Area A, 
							 Behavior B, 
							 MOB mob, 
							 Law theLaw, 
							 String tag, 
							 String newValue)
	{
		theLaw.setInternalStr(tag,newValue);
		B.modifyBehavior(A,mob,new Integer(Law.MOD_SETNEWLAW));
	}
	
	public void doJailPolicy(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.session().colorOnlyPrintln(getFromTOC("P4"+(theLaw.hasModifiableLaws()?"MOD":"")));
		StringBuffer str=new StringBuffer("");
		str.append("1. LEVEL 1 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL1TIME"))*Host.TICK_TIME/1000)+" seconds.\n\r");
		str.append("2. LEVEL 2 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL2TIME"))*Host.TICK_TIME/1000)+" seconds.\n\r");
		str.append("3. LEVEL 3 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL3TIME"))*Host.TICK_TIME/1000)+" seconds.\n\r");
		str.append("4. LEVEL 4 JAIL TIME: "+(Util.s_int(theLaw.getInternalStr("JAIL4TIME"))*Host.TICK_TIME/1000)+" seconds.\n\r");
		str.append("\n\r");
		Vector V=theLaw.jailRooms();
		int highest=4;
		for(int v=0;v<V.size();v++)
		{
			String s=(String)V.elementAt(v);
			if(!s.equals("@"))
			{
				highest++;
				Room R=CMMap.getRoom(s);
				if(R!=null)
					str.append((5+v)+". JAIL ROOM: "+R.displayText()+"\n\r");
				else
					str.append((5+v)+". JAIL ROOM: Rooms called '"+s+"'.\n\r");
			}
		}
		mob.tell(str.toString());
		if(theLaw.hasModifiableLaws())
		{
			String s=mob.session().prompt("\n\rEnter 'A' to add a new jail room, or enter a number to modify: ","");
			boolean changed=false;
			if(s.equalsIgnoreCase("A"))
			{
				if(mob.location().getArea()!=A)
					mob.tell("You can not add this room as a jail, as it is not in the area.");
				else
				if(mob.session().confirm("Add this room as a new jail room (y/N)? ","N"))
				{
					V.addElement(CMMap.getExtendedRoomID(mob.location()));
					changed=true;
				}
			}
			else
			{
				int x=Util.s_int(s);
				if((x>0)||(x<=highest))
				{
					if(x>4)
					{
						if(mob.session().confirm("Remove this room as a new jail room (y/N)? ","N"))
						{
							V.removeElementAt(x-5);
							changed=true;
						}
					}
					else
					{
						int oldTime=Util.s_int(theLaw.getInternalStr("JAIL"+x+"TIME"));
						s=mob.session().prompt("Enter a new number of seconds ("+oldTime+"): ",""+oldTime);
						if((Util.s_int(s)!=oldTime)&&(Util.s_int(s)>0))
						{
							changeTheLaw(A,B,mob,theLaw,"JAIL"+x+"TIME",""+Util.s_int(s));
							mob.tell("Changed.");
						}
					}
				}
			}
			if(changed)
			{
				StringBuffer s2=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					s2.append(((String)V.elementAt(v))+";");
				if(s2.length()==0)
					s2.append("@");
				else
					s2.deleteCharAt(s2.length()-1);
				changeTheLaw(A,B,mob,theLaw,"JAIL",s2.toString());
				mob.tell("Changed.");
			}
		}
	}
	
	
	public void doVictimsOfCrime(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.session().colorOnlyPrintln(getFromTOC("P3"+(theLaw.hasModifiableLaws()?"MOD":"")));
		mob.tell(SaucerSupport.zapperDesc(theLaw.getInternalStr("PROTECTED")));
		if(theLaw.hasModifiableLaws())
		{
			String s="?";
			while(s.trim().equals("?"))
			{
				s=mob.session().prompt("Enter a new mask, ? for help, or RETURN=["+theLaw.getInternalStr("PROTECTED")+"]\n\r: ",theLaw.getInternalStr("PROTECTED"));
				if(s.trim().equals("?"))
					mob.tell(SaucerSupport.zapperInstructions("\n\r","protects"));
				else
				if(!s.equals(theLaw.getInternalStr("PROTECTED")))
				{
					changeTheLaw(A,B,mob,theLaw,"PROTECTED",s);
					mob.tell("Changed.");
				}
			}
		}
	}
	
	public void doOfficersAndJudges(Area A, Behavior B, Law theLaw, MOB mob)
		throws IOException
	{
		if(mob.session()==null) return;
		mob.session().colorOnlyPrintln(getFromTOC("P2"+(theLaw.hasModifiableLaws()?"MOD":"")+(theLaw.hasModifiableNames()?"NAM":"")));
		String duhJudge="No Judge Found!\n\r";
		StringBuffer duhOfficers=new StringBuffer("No Officers Found!\n\r");
		for(Enumeration e=A.getMap();e.hasMoreElements();)
		{
			Room R=(Room)e.nextElement();
			for(int i=0;i<R.numInhabitants();i++)
			{
				MOB M=(MOB)R.fetchInhabitant(i);
				if(M!=null)
				{
					Room R2=M.getStartRoom();
					if(R==null) R=M.location();
					if(B.modifyBehavior(A,M,new Integer(Law.MOD_ISOFFICER)))
						duhOfficers.append(M.name()+" from room '"+R2.displayText()+"'\n\r");
					else
					if(B.modifyBehavior(A,M,new Integer(Law.MOD_ISJUDGE)))
						duhJudge=M.name()+" from room '"+R2.displayText()+"'\n\r";
				}
			}
		}
		mob.tell("1. Area Judge: \n\r"+duhJudge+"\n\r2. Area Officers: \n\r"+duhOfficers.toString());
		if(theLaw.hasModifiableNames()&&theLaw.hasModifiableLaws())
		{
			int w=Util.s_int(mob.session().choose("Enter one to modify, or RETURN to cancel: ","12",""));
			if(w==0) return;
			String modifiableTag=(w==1)?"JUDGE":"OFFICERS";
			String s=mob.session().prompt("Enter key words from officials name(s) ["+theLaw.getInternalStr(modifiableTag)+"]\n\r: ",theLaw.getInternalStr(modifiableTag));
			if(!s.equals(theLaw.getInternalStr(modifiableTag)))
			{
				changeTheLaw(A,B,mob,theLaw,modifiableTag,s);
				mob.tell("Changed.");
			}
		}
	}
}
