package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class StdDeity extends StdMOB implements Deity
{
	protected int xpwrath=100;
	protected String clericReqs="";
	protected String worshipReqs="";
	protected String clericRitual="";
	protected String worshipRitual="";
	protected Vector worshipTriggers=new Vector();
	protected Vector clericTriggers=new Vector();
	protected Vector blessings=new Vector();
	protected Hashtable trigParts=new Hashtable();
	protected Hashtable trigTimes=new Hashtable();
	protected int checkDown=0;
	protected boolean norecurse=false;

	public StdDeity()
	{
		super();
		Username="a Mighty Deity";
		setDescription("He is Mighty.");
		setDisplayText("A Mighty Deity stands here!");
		baseEnvStats().setWeight(700);
		baseEnvStats().setAbility(200);
		baseEnvStats().setArmor(0);
		baseEnvStats().setAttackAdjustment(1000);
		baseEnvStats().setDamage(1000);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdDeity();
	}

	public String getClericRequirements(){return clericReqs;}
	public void setClericRequirements(String reqs){clericReqs=reqs;}
	public String getWorshipRequirements(){return worshipReqs;}
	public void setWorshipRequirements(String reqs){worshipReqs=reqs;}
	public String getClericRitual(){
		if(clericRitual.trim().length()==0) return "SAY Bless me "+name();
		return clericRitual;}
	public void setClericRitual(String ritual){
		clericRitual=ritual;
		parseTriggers(clericTriggers,ritual);
	}
	public String getWorshipRitual(){
		if(worshipRitual.trim().length()==0) return "SAY Bless me "+name();
		return worshipRitual;}
	public void setWorshipRitual(String ritual){
		worshipRitual=ritual;
		parseTriggers(worshipTriggers,ritual);
	}


	public String getTriggerDesc(Vector V)
	{
		if((V==null)||(V.size()==0)) return "Never";
		StringBuffer buf=new StringBuffer("");
		for(int v=0;v<V.size();v++)
		{
			DeityTrigger DT=(DeityTrigger)V.elementAt(v);
			if(v>0) buf.append(", "+((DT.previousConnect==CONNECT_AND)?"and ":"or "));
			switch(DT.triggerCode)
			{
			case TRIGGER_SAY:
				buf.append("the player says '"+DT.parm1.toLowerCase()+"'");
				break;
			case TRIGGER_TIME:
				buf.append("the hour of the day is "+DT.parm1.toLowerCase()+"");
				break;
			case TRIGGER_PUTTHING:
				buf.append("the player puts "+DT.parm1.toLowerCase()+" in "+DT.parm2.toLowerCase());
				break;
			case TRIGGER_BURNTHING:
				buf.append("the player burns "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_DRINK:
				buf.append("the player drinks from "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_EAT:
				buf.append("the player eats "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_INROOM:
				{
				Room R=CMMap.getRoom(DT.parm1);
				if(R==null)
					buf.append("the player is in some unknown place");
				else
					buf.append("the player is in '"+R.displayText()+"'");
				}
				break;
			case TRIGGER_RIDING:
				buf.append("the player is on "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_CAST:
				{
				Ability A=CMClass.findAbility(DT.parm1);
				if(A==null)
					buf.append("the player casts '"+DT.parm1+"'");
				else
					buf.append("the player casts '"+A.name()+"'");
				}
				break;
			case TRIGGER_EMOTE:
				buf.append("the player emotes '"+DT.parm1.toLowerCase()+"'");
				break;
			case TRIGGER_PUTVALUE:
				buf.append("the player puts an item worth at least "+DT.parm1.toLowerCase()+" in "+DT.parm2.toLowerCase());
				break;
			case TRIGGER_PUTMATERIAL:
				{
					String material="something";
					int t=Util.s_int(DT.parm1);
					if(((t&EnvResource.RESOURCE_MASK)==0)
					&&((t>>8)<EnvResource.MATERIAL_MASK))
						material=EnvResource.MATERIAL_DESCS[t>>8].toLowerCase();
					else
					if(((t&EnvResource.RESOURCE_MASK)>0)
					&&((t&EnvResource.RESOURCE_MASK)<EnvResource.RESOURCE_DESCS.length))
						material=EnvResource.RESOURCE_DESCS[t&EnvResource.RESOURCE_MASK].toLowerCase();
					buf.append("the player puts an item made of "+material+" in "+DT.parm2.toLowerCase());
				}
				break;
			case TRIGGER_BURNMATERIAL:
				{
					String material="something";
					int t=Util.s_int(DT.parm1);
					if(((t&EnvResource.RESOURCE_MASK)==0)
					&&((t>>8)<EnvResource.MATERIAL_MASK))
						material=EnvResource.MATERIAL_DESCS[t>>8].toLowerCase();
					else
					if(((t&EnvResource.RESOURCE_MASK)>0)
					&&((t&EnvResource.RESOURCE_MASK)<EnvResource.RESOURCE_DESCS.length))
						material=EnvResource.RESOURCE_DESCS[t&EnvResource.RESOURCE_MASK].toLowerCase();
					buf.append("the player burns an item made of "+material);
				}
				break;
			case TRIGGER_BURNVALUE:
				buf.append("the player burns an item worth at least "+DT.parm1.toLowerCase());
				break;
			case TRIGGER_SITTING:
				buf.append("the player sits down");
				break;
			case TRIGGER_STANDING:
				buf.append("the player stands up");
				break;
			case TRIGGER_SLEEPING:
				buf.append("the player goes to sleep");
				break;
			}
		}
		return buf.toString();
	}

	public String getClericRequirementsDesc()
	{
		return "The following may be clerics of "+name()+": "+ExternalPlay.zapperDesc(getClericRequirements());
	}
	public String getClericTriggerDesc()
	{
		if(numBlessings()>0)
			return "The blessings of "+name()+" are bestowed to "+charStats().hisher()+" clerics whenever the cleric does the following: "+getTriggerDesc(clericTriggers)+".";
		return "";
	}
	public String getWorshipRequirementsDesc()
	{
		return "The following are acceptable worshipers of "+name()+": "+ExternalPlay.zapperDesc(getWorshipRequirements());
	}
	public String getWorshipTriggerDesc()
	{
		if(numBlessings()>0)
			return "The blessings of "+name()+" are bestowed to "+charStats().hisher()+" worshippers whenever they do the following: "+getTriggerDesc(worshipTriggers)+".";
		return "";
	}

	public void destroy()
	{
		super.destroy();
		CMMap.delDeity(this);
	}
	public void bringToLife(Room newLocation, boolean resetStats)
	{
		super.bringToLife(newLocation,resetStats);
		CMMap.addDeity(this);
	}

	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if(!super.okAffect(myHost,msg))
			return false;
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case Affect.TYP_SERVE:
			if(msg.source().getMyDeity()==this)
			{
				msg.source().tell("You already worship "+name()+".");
				return false;
			}
			if(msg.source().getMyDeity()!=null)
			{
				msg.source().tell("You already worship "+msg.source().getMyDeity().name()+".");
				return false;
			}
			if(msg.source().charStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
			{
				if(!ExternalPlay.zapperCheck(getClericRequirements(),msg.source()))
				{
					msg.source().tell("You are unworthy of serving "+name()+".");
					return false;
				}
			}
			else
			if(!ExternalPlay.zapperCheck(getWorshipRequirements(),msg.source()))
			{
				msg.source().tell("You are unworthy of "+name()+".");
				return false;
			}
			break;
		case Affect.TYP_REBUKE:
			if(!msg.source().getWorshipCharID().equals(name()))
			{
				msg.source().tell("You do not worship "+name()+".");
				return false;
			}
			break;
		}
		return true;
	}

	public synchronized void bestowBlessing(MOB mob, Ability Blessing)
	{
		Room prevRoom=location();
		mob.location().bringMobHere(this,false);
		Blessing.invoke(this,mob,true);
		prevRoom.bringMobHere(this,false);
		if(mob.location()!=prevRoom)
		{
			if(mob.getVictim()==this)
				mob.makePeace();
			if(getVictim()==mob)
				makePeace();
		}
	}

	public synchronized void bestowBlessings(MOB mob)
	{
		norecurse=true;
		try
		{
			if((!alreadyBlessed(mob))&&(numBlessings()>0))
			{
				mob.location().show(this,mob,Affect.MSG_OK_VISUAL,"You feel the presence of <S-NAME> in <T-NAME>.");
				if(mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
				{
					for(int b=0;b<numBlessings();b++)
					{
						Ability Blessing=fetchBlessing(b);
						if(Blessing!=null)
							bestowBlessing(mob,Blessing);
					}
				}
				else
				{
					Ability Blessing=fetchBlessing(Dice.roll(1,numBlessings(),-1));
					if(Blessing!=null)
						bestowBlessing(mob,Blessing);
				}
			}
		}
		catch(Exception e)
		{
			Log.errOut("StdDeity",e);
		}
		norecurse=false;
	}

	public void removeBlessings(MOB mob)
	{
		if((alreadyBlessed(mob))&&(mob.location()!=null))
		{
			mob.location().show(this,mob,Affect.MSG_OK_VISUAL,"<S-NAME> remove(s) <S-HIS-HER> blessings from <T-NAME>.");
			for(int a=mob.numAffects()-1;a>=0;a--)
			{
				Ability A=mob.fetchAffect(a);
				if((A!=null)&&(A.invoker()==this))
				{
					A.unInvoke();
					mob.delAffect(A);
				}
			}
		}
	}

	public boolean alreadyBlessed(MOB mob)
	{
		for(int a=0;a<mob.numAffects();a++)
		{
			Ability A=mob.fetchAffect(a);
			if((A!=null)&&(A.invoker()==this))
				return true;
		}
		return false;
	}

	public void affect(Environmental myHost, Affect msg)
	{
		super.affect(myHost,msg);
		if(norecurse) return;
		
		if(msg.amITarget(this))
		switch(msg.targetMinor())
		{
		case Affect.TYP_SERVE:
			msg.source().setWorshipCharID(name());
			break;
		case Affect.TYP_REBUKE:
			msg.source().setWorshipCharID("");
			removeBlessings(msg.source());
			if(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric"))
			{
				msg.source().tell("You feel the wrath of "+name()+"!");
				msg.source().charStats().getCurrentClass().unLevel(msg.source());
			}
			else
			{
				msg.source().tell(name()+" takes "+xpwrath+" of experience from you.");
				msg.source().charStats().getCurrentClass().loseExperience(msg.source(),xpwrath);
			}
			break;
		}
		else
		if(msg.source().getWorshipCharID().equals(name())&&(numBlessings()>0))
		{
			boolean recheck=false;
			Vector V=worshipTriggers;
			if(msg.source().charStats().getCurrentClass().baseClass().equals("Cleric"))
				V=clericTriggers;
			for(int v=0;v<V.size();v++)
			{
				boolean yup=false;
				DeityTrigger DT=(DeityTrigger)V.elementAt(v);
				if((msg.sourceMinor()==TRIG_WATCH[DT.triggerCode])
				||(TRIG_WATCH[DT.triggerCode]==-999))
				{
					switch(DT.triggerCode)
					{
					case TRIGGER_SAY:
						if(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0)
							yup=true;
						break;
					case TRIGGER_TIME:
						if((msg.source().location()!=null)
						&&(msg.source().location().getArea().getTimeOfDay()==Util.s_int(DT.parm1)))
						   yup=true;
						break;
					case TRIGGER_PUTTHING:
						if((msg.target()!=null)
						&&(msg.target() instanceof Container)
						&&(msg.tool()!=null)
						&&(msg.tool() instanceof Item)
						&&(CoffeeUtensils.containsString(msg.tool().name(),DT.parm1))
						&&(CoffeeUtensils.containsString(msg.target().name(),DT.parm2)))
							yup=true;
						break;
					case TRIGGER_BURNTHING:
					case TRIGGER_DRINK:
					case TRIGGER_EAT:
						if((msg.target()!=null)
						&&(CoffeeUtensils.containsString(msg.target().name(),DT.parm1)))
						   yup=true;
						break;
					case TRIGGER_INROOM:
						if((msg.source().location()!=null)
						&&(msg.source().location().ID().equals(DT.parm1)))
							yup=true;
						break;
					case TRIGGER_RIDING:
						if((msg.source().riding()!=null)
						&&(CoffeeUtensils.containsString(msg.source().riding().name(),DT.parm1)))
						   yup=true;
						break;
					case TRIGGER_CAST:
						if((msg.tool()!=null)
						&&((msg.tool().ID().equalsIgnoreCase(DT.parm1))
						||(CoffeeUtensils.containsString(msg.tool().name(),DT.parm1))))
							yup=true;
						break;
					case TRIGGER_EMOTE:
						if(msg.sourceMessage().toUpperCase().indexOf(DT.parm1)>0)
							yup=true;
						break;
					case TRIGGER_PUTVALUE:
						if((msg.tool()!=null)
						&&(msg.tool() instanceof Item)
						&&(((Item)msg.tool()).baseGoldValue()>=Util.s_int(DT.parm1))
						&&(msg.target()!=null)
						&&(msg.target() instanceof Container)
						&&(CoffeeUtensils.containsString(msg.target().name(),DT.parm2)))
							yup=true;
						break;
					case TRIGGER_PUTMATERIAL:
						if((msg.tool()!=null)
						&&(msg.tool() instanceof Item)
						&&(((((Item)msg.tool()).material()&EnvResource.RESOURCE_MASK)==Util.s_int(DT.parm1))
							||((((Item)msg.tool()).material()&EnvResource.MATERIAL_MASK)==Util.s_int(DT.parm1)))
						&&(msg.target()!=null)
						&&(msg.target() instanceof Container)
						&&(CoffeeUtensils.containsString(msg.target().name(),DT.parm2)))
							yup=true;
						break;
					case TRIGGER_BURNMATERIAL:
						if((msg.target()!=null)
						&&(msg.target() instanceof Item)
						&&(((((Item)msg.target()).material()&EnvResource.RESOURCE_MASK)==Util.s_int(DT.parm1))
							||((((Item)msg.target()).material()&EnvResource.MATERIAL_MASK)==Util.s_int(DT.parm1))))
								yup=true;
						break;
					case TRIGGER_BURNVALUE:
						if((msg.target()!=null)
						&&(msg.target() instanceof Item)
						&&(((Item)msg.target()).baseGoldValue()>=Util.s_int(DT.parm1)))
							yup=true;
						break;
					case TRIGGER_SITTING:
						yup=Sense.isSitting(msg.source());
						break;
					case TRIGGER_STANDING:
						yup=(!Sense.isSitting(msg.source()))&&(!Sense.isSleeping(msg.source()));
						break;
					case TRIGGER_SLEEPING:
						yup=Sense.isSleeping(msg.source());
						break;
					}
				}
				if((yup)||(TRIG_WATCH[DT.triggerCode]==-999))
				{
					boolean[] checks=(boolean[])trigParts.get(msg.source().name());
					if(yup)
					{
						recheck=true;
						trigTimes.remove(msg.source().name());
						trigTimes.put(msg.source().name(),new Long(System.currentTimeMillis()));
						if((checks==null)||(checks.length!=V.size()))
						{
							checks=new boolean[V.size()];
							trigParts.put(msg.source().name(),checks);
						}
					}
					if(checks!=null) checks[v]=yup;
				}
			}
			
			if((recheck)&&(!norecurse)&&(!alreadyBlessed(msg.source())))
			{
				boolean[] checks=(boolean[])trigParts.get(msg.source().name());
				if((checks!=null)&&(checks.length==V.size())&&(checks.length>0))
				{
					boolean rollingTruth=checks[0];
					for(int v=1;v<V.size();v++)
					{
						DeityTrigger DT=(DeityTrigger)V.elementAt(v);
						if(DT.previousConnect==CONNECT_AND)
							rollingTruth=rollingTruth&&checks[v];
						else
							rollingTruth=rollingTruth||checks[v];
					}
					if(rollingTruth)
						bestowBlessings(msg.source());
				}
			}
		}
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Host.MOB_TICK)
		&&((--checkDown)<0))
		{
			checkDown=10;
			for(Enumeration p=CMMap.players();p.hasMoreElements();)
			{
				MOB M=(MOB)p.nextElement();
				if((!M.isMonster())&&(M.getWorshipCharID().equals(name())))
				{
					if(M.charStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
					{
						if(!ExternalPlay.zapperCheck(getClericRequirements(),M))
						{
							FullMsg msg=new FullMsg(M,this,null,Affect.MSG_REBUKE,"<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!");
							if((M.location()!=null)&&(M.okAffect(M,msg)))
								M.location().send(M,msg);
						}
					}
					else
					if(!ExternalPlay.zapperCheck(getWorshipRequirements(),M))
					{
						FullMsg msg=new FullMsg(M,this,null,Affect.MSG_REBUKE,"<S-NAME> <S-HAS-HAVE> been rebuked by <T-NAME>!!");
						if((M.location()!=null)&&(M.okAffect(M,msg)))
							M.location().send(M,msg);
					}
				}
			}
			long curTime=System.currentTimeMillis()-60000;
			for(Enumeration e=trigTimes.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				Long L=(Long)trigTimes.get(key);
				if(L.longValue()<curTime)
				{
					trigTimes.remove(key);
					trigParts.remove(key);
				}
			}
		}
		return true;
	}

	public void addBlessing(Ability to)
	{
		if(to==null) return;
		for(int a=0;a<numBlessings();a++)
		{
			Ability A=fetchBlessing(a);
			if((A!=null)&&(A.ID().equals(to.ID())))
				return;
		}
		blessings.addElement(to);
	}
	public void delBlessing(Ability to)
	{
		blessings.removeElement(to);
	}
	public int numBlessings()
	{
		return blessings.size();
	}
	public Ability fetchBlessing(int index)
	{
		try
		{
			return (Ability)blessings.elementAt(index);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Ability fetchBlessing(String ID)
	{
		for(int a=0;a<numBlessings();a++)
		{
			Ability A=fetchBlessing(a);
			if((A!=null)&&((A.ID().equalsIgnoreCase(ID))||(A.name().equalsIgnoreCase(ID))))
				return A;
		}
		return (Ability)CoffeeUtensils.fetchEnvironmental(blessings,ID,false);
	}

	private void parseTriggers(Vector putHere, String trigger)
	{
		putHere.clear();
		trigger=trigger.toUpperCase().trim();
		int previousConnector=CONNECT_AND;
		if(!trigger.equals("-"))
		while(trigger.length()>0)
		{
			int div1=trigger.indexOf("&");
			int div2=trigger.indexOf("|");
			int div=div1;

			if((div2>=0)&&((div<0)||(div2<div)))
				div=div2;
			String trig=null;
			if(div<0)
			{
				trig=trigger;
				trigger="";
			}
			else
			{
				trig=trigger.substring(0,div).trim();
				trigger=trigger.substring(div+1);
			}
			if(trig.length()>0)
			{
				Vector V=Util.parse(trig);
				if(V.size()>1)
				{
					String cmd=(String)V.firstElement();
					DeityTrigger DT=new DeityTrigger();
					DT.previousConnect=previousConnector;
					if(cmd.equals("SAY"))
					{
						DT.triggerCode=TRIGGER_SAY;
						DT.parm1=Util.combine(V,1);
					}
					else
					if(cmd.equals("TIME"))
					{
						DT.triggerCode=TRIGGER_TIME;
						DT.parm1=""+Util.s_int(Util.combine(V,1));
					}
					else
					if((cmd.equals("PUTTHING"))||(cmd.equals("PUT")))
					{
						DT.triggerCode=TRIGGER_PUTTHING;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=Util.combine(V,1,V.size()-2);
						DT.parm2=(String)V.lastElement();
					}
					else
					if(cmd.equals("BURNTHING"))
					{
						DT.triggerCode=TRIGGER_BURNTHING;
						DT.parm1=Util.combine(V,1);
					}
					else
					if(cmd.equals("PUTVALUE"))
					{
						DT.triggerCode=TRIGGER_PUTVALUE;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=""+Util.s_int((String)V.elementAt(1));
						DT.parm2=(String)Util.combine(V,2);
					}
					else
					if(cmd.equals("BURNVALUE"))
					{
						DT.triggerCode=TRIGGER_BURNVALUE;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=""+Util.s_int((String)Util.combine(V,1));
					}
					else
					if((cmd.equals("BURNMATERIAL"))||(cmd.equals("BURN")))
					{
						DT.triggerCode=TRIGGER_BURNMATERIAL;
						DT.parm1=Util.combine(V,1);
						boolean found=false;
						for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
						{
							if(EnvResource.RESOURCE_DESCS[i].startsWith(DT.parm1))
							{
								DT.parm1=""+EnvResource.RESOURCE_DATA[i][0];
								found=true;
							}
						}
						if(!found)
						for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
						{
							if(EnvResource.MATERIAL_DESCS[i].startsWith(DT.parm1))
							{
								DT.parm1=""+(i<<8);
								found=true;
							}
						}
						if(!found)
						{
							Log.errOut("StdDeity",name()+"- Unknown material: "+trig);
							break;
						}
					}
					else
					if(cmd.equals("PUTMATERIAL"))
					{
						DT.triggerCode=TRIGGER_PUTMATERIAL;
						if(V.size()<3)
						{
							Log.errOut("StdDeity",name()+"- Illegal trigger: "+trig);
							break;
						}
						DT.parm1=(String)V.elementAt(1);
						DT.parm2=(String)Util.combine(V,2);
						boolean found=false;
						for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
						{
							if(EnvResource.RESOURCE_DESCS[i].startsWith(DT.parm1))
							{
								DT.parm1=""+EnvResource.RESOURCE_DATA[i][0];
								found=true;
							}
						}
						if(!found)
						for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
						{
							if(EnvResource.MATERIAL_DESCS[i].startsWith(DT.parm1))
							{
								DT.parm1=""+(i<<8);
								found=true;
							}
						}
						if(!found)
						{
							Log.errOut("StdDeity",name()+"- Unknown material: "+trig);
							break;
						}
					}
					else
					if(cmd.equals("EAT"))
					{
						DT.triggerCode=TRIGGER_EAT;
						DT.parm1=Util.combine(V,1);
					}
					else
					if(cmd.equals("DRINK"))
					{
						DT.triggerCode=TRIGGER_DRINK;
						DT.parm1=Util.combine(V,1);
					}
					else
					if(cmd.equals("INROOM"))
					{
						DT.triggerCode=TRIGGER_INROOM;
						DT.parm1=Util.combine(V,1);
					}
					else
					if(cmd.equals("RIDING"))
					{
						DT.triggerCode=TRIGGER_RIDING;
						DT.parm1=Util.combine(V,1);
					}
					else
					if(cmd.equals("CAST"))
					{
						DT.triggerCode=TRIGGER_CAST;
						DT.parm1=Util.combine(V,1);
						if(CMClass.findAbility(DT.parm1)==null)
						{
							Log.errOut("StdDeity",name()+"- Illegal SPELL in: "+trig);
							break;
						}
					}
					else
					if(cmd.equals("EMOTE"))
					{
						DT.triggerCode=TRIGGER_EMOTE;
						DT.parm1=Util.combine(V,1);
					}
					else
					if(cmd.startsWith("SIT"))
					{
						DT.triggerCode=TRIGGER_SITTING;
					}
					else
					if(cmd.startsWith("STAND"))
					{
						DT.triggerCode=TRIGGER_STANDING;
					}
					else
					if(cmd.startsWith("SLEEP"))
					{
						DT.triggerCode=TRIGGER_SLEEPING;
					}
					else
					{
						Log.errOut("StdDeity",name()+"- Illegal trigger: '"+cmd+"','"+trig+"'");
						break;
					}
					putHere.addElement(DT);
				}
				else
				{
					Log.errOut("StdDeity",name()+"- Illegal trigger (need more parameters): "+trig);
					break;
				}
			}
			if(div==div1)
				previousConnector=CONNECT_AND;
			else
				previousConnector=CONNECT_OR;
		}
	}

	private static final int TRIGGER_SAY=0;
	private static final int TRIGGER_TIME=1;
	private static final int TRIGGER_PUTTHING=2;
	private static final int TRIGGER_BURNTHING=3;
	private static final int TRIGGER_EAT=4;
	private static final int TRIGGER_DRINK=5;
	private static final int TRIGGER_INROOM=6;
	private static final int TRIGGER_RIDING=7;
	private static final int TRIGGER_CAST=8;
	private static final int TRIGGER_EMOTE=9;
	private static final int TRIGGER_PUTVALUE=10;
	private static final int TRIGGER_PUTMATERIAL=11;
	private static final int TRIGGER_BURNMATERIAL=12;
	private static final int TRIGGER_BURNVALUE=13;
	private static final int TRIGGER_SITTING=14;
	private static final int TRIGGER_STANDING=15;
	private static final int TRIGGER_SLEEPING=16;
	private static final int[] TRIG_WATCH={
		Affect.TYP_SPEAK,		//0
		-999,					//1
		Affect.TYP_PUT,			//2
		Affect.TYP_FIRE,		//3
		Affect.TYP_EAT,			//4
		Affect.TYP_DRINK,		//5
		Affect.TYP_ENTER,		//6
		-999,					//7
		Affect.TYP_CAST_SPELL,  //8
		Affect.TYP_EMOTE,		//9
		Affect.TYP_PUT,			//10
		Affect.TYP_PUT,			//11
		Affect.TYP_FIRE,		//12
		Affect.TYP_FIRE,		//13
		-999,					//14
		-999,					//15
		-999					//16
	};

	private static final int CONNECT_AND=0;
	private static final int CONNECT_OR=1;

	private class DeityTrigger
	{
		public int triggerCode=TRIGGER_SAY;
		public int previousConnect=CONNECT_AND;
		public String parm1=null;
		public String parm2=null;
	}
}
