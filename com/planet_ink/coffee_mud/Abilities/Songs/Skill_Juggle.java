package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Skill_Juggle extends StdAbility
{
	public String ID() { return "Skill_Juggle"; }
	public String name(){ return "Juggle";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"JUGGLE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Juggle();}
	public int usageType(){return USAGE_MOVEMENT;}
	protected Vector juggles=new Vector();
	protected long lastJuggle=-1;
	protected boolean pause=false;

	public int maxJuggles()
	{
		if((affected!=null)&&(affected instanceof MOB))
			return 5+(CMAble.qualifyingClassLevel((MOB)affected,this));
		return 5;
	}

	public int maxAttacks()
	{
		if((affected!=null)&&(affected instanceof MOB))
			return (int)Math.round(affected.envStats().speed())
				   +(CMAble.qualifyingClassLevel((MOB)affected,this)/5);
		return 1;
	}

	public String displayText()
	{
		if(juggles.size()>0)
		{
			StringBuffer str=new StringBuffer("(Juggling: ");
			Vector V=(Vector)juggles.clone();
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				boolean back=false;
				for(int ii=0;ii<i;ii++)
				{
					Item I2=(Item)V.elementAt(ii);
					if(I2.name().equals(I.name()))
					{ back=true; break;}
				}
				if(back) continue;
				boolean morethanone=false;
				for(int ii=i+1;ii<V.size();ii++)
				{
					Item I2=(Item)V.elementAt(ii);
					if(I2.name().equals(I.name()))
					{ morethanone=true; break;}
				}
				str.append(I.name()+(morethanone?"s":"")+" ");
			}
			return str.toString()+")";
		}
		else
			return "(Juggling??)";

	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Item)
		&&(juggles.contains(msg.target()))
		&&(affected instanceof MOB)
		&&(Dice.rollPercentage()<90)
		&&(msg.source()!=affected))
		{
			msg.source().tell(msg.source(),msg.target(),null,"<T-NAME> is moving too fast for you to grab it.");
			return false;
		}
		return true;
	}

	private void unJuggle(Item I)
	{
		if(I==null) return;
		Ability A=I.fetchEffect("Spell_Fly");
		if(A!=null) A.unInvoke();
		juggles.removeElement(I);
	}

	public void juggleItem(Item I)
	{
		if(I==null) return;
		if(juggles.contains(I)) return;
		if(I.fetchEffect("Spell_Fly")==null)
		{
			Ability A=CMClass.getAbility("Spell_Fly");
			if(A!=null)
			{
				I.addEffect(A);
				A.makeLongLasting();
				A.setBorrowed(I,true);
				I.recoverEnvStats();
			}
		}
		juggles.addElement(I);
	}

	private synchronized void juggle()
	{
		boolean anythingToDo=false;
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB M=(MOB)affected;
		Room R=M.location();
		if(R==null) return;
		for(int i=0;i<juggles.size();i++)
		{
			Item I=null;
			try{I=(Item)juggles.elementAt(i);}catch(Exception e){}
			if((I==null)
			||(I.owner()==null)
			||((I.owner() instanceof MOB)&&(I.owner()!=M))
			||((I.owner() instanceof Room)&&(I.owner()!=R)))
			{
				anythingToDo=true;
				break;
			}
		}
		if(anythingToDo)
		{
			Vector copy=(Vector)juggles.clone();
			for(int i=0;i<copy.size();i++)
			{
				Item I=(Item)copy.elementAt(i);
				if((I.owner()==null)
				||((I.owner() instanceof MOB)&&(I.owner()!=M)))
					unJuggle(I);
				else
				if((I.owner() instanceof Room)&&(I.owner()!=R))
					R.bringItemHere(I,Item.REFUSE_PLAYER_DROP);
			}
		}
		pause=true;
		for(int i=0;i<M.inventorySize();i++)
		{
			Item I=M.fetchInventory(i);
			if((I!=null)
			&&((I.amWearingAt(Item.WIELD)||I.amWearingAt(Item.HELD)))
			&&(!juggles.contains(I))
			&&(juggles.size()<maxJuggles()))
			{
				if(M.location().show(M,I,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> start(s) juggling <T-NAMESELF>."))
					juggleItem(I);
				else
				{
					unJuggle(I);
					CommonMsgs.drop(M,I,false,false);
					break;
				}
			}
		}
		pause=false;
		if(juggles.size()==0)
		{
			unInvoke();
			return;
		}
		if(lastJuggle>(System.currentTimeMillis()-500))
			return;
		lastJuggle=System.currentTimeMillis();
		Vector copy=(Vector)juggles.clone();
		int jug=-1;
		for(int i=0;i<copy.size();i++)
		{
			Item I=(Item)copy.elementAt(i);
			if(I.amWearingAt(Item.WIELD)||I.amWearingAt(Item.HELD))
			{
				I.setRawWornCode(Item.INVENTORY);
				jug=i;
			}
		}
		jug++;
		if((jug<0)||(jug>=copy.size()-2))
			jug=0;
		for(int i=0;i<copy.size();i++)
		{
			Item I=(Item)copy.elementAt(i);
			if((i==jug)||(i==jug+1))
			{
				if(!M.isMine(I))
					M.giveItem(I);
				if(i==jug)
					I.setRawWornCode(Item.WIELD);
				else
					I.setRawWornCode(Item.HELD);
			}
			else
			{
				I.unWear();
				if(!M.location().isContent(I))
					M.location().bringItemHere(I,Item.REFUSE_PLAYER_DROP);
			}
		}
		M.recoverEnvStats();
		M.recoverCharStats();
		M.recoverMaxState();
		M.location().recoverRoomStats();
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(!pause)
		{
			juggle();
			if(((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_REMOVE))
			&&(msg.target()!=null)
			&&(msg.target() instanceof Item)
			&&(juggles.contains(msg.target())))
			{
				unJuggle((Item)msg.target());
				if(juggles.size()==0)
					unInvoke();
			}
		}
		super.executeMsg(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!pause)
		{
			juggle();
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(juggles.size()>0))
			{
				MOB mob=(MOB)affected;
				if(mob.location()!=null)
				{
					if(!mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> juggle(s) "+juggles.size()+" items in the air."))
					   unInvoke();
					else
					if(mob.isInCombat())
					{
						Vector copy=(Vector)juggles.clone();
						int maxAttacks=maxAttacks();
						for(int i=0;((i<maxAttacks)&&(copy.size()>0));i++)
						{
							Item I=(Item)copy.elementAt(Dice.roll(1,copy.size(),-1));
							I.unWear();
							mob.giveItem(I);
							if((mob.isMine(I))&&(CommonMsgs.drop(mob,I,true,false)))
							{
								Weapon w=(Weapon)CMClass.getWeapon("StdWeapon");
								w.setName(I.name());
								w.setDisplayText(I.displayText());
								w.setDescription(I.description());
								copy.removeElement(I);
								unJuggle(I);
								w.setWeaponClassification(Weapon.CLASS_THROWN);
								w.setRanges(0,10);
								if(I instanceof Weapon)
									w.setWeaponType(((Weapon)I).weaponType());
								else
									w.setWeaponType(Weapon.TYPE_BASHING);
								w.baseEnvStats().setDamage(Dice.roll(1,adjustedLevel(mob),0));
								w.baseEnvStats().setWeight(I.baseEnvStats().weight());
								w.recoverEnvStats();
								MUDFight.postAttack(mob,mob.getVictim(),w);
								w.destroy();
							}
							else
								break;
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB M=(MOB)affected;
			while(juggles.size()>0)
			{
				Item I=(Item)juggles.elementAt(0);
				M.location().show(M,I,CMMsg.MSG_OK_ACTION,"<S-NAME> stop(s) juggling <T-NAMESELF>.");
				unJuggle(I);
				I.unWear();
				if(!M.isMine(I)) M.giveItem(I);
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String whatToJuggle=Util.combine(commands,0);
		Skill_Juggle A=(Skill_Juggle)mob.fetchEffect("Skill_Juggle");
		if(whatToJuggle.length()==0)
		{
			if(A==null)
			{
				mob.tell("Juggle what?");
				return false;
			}
			else
			{
				mob.tell("You stop juggling.");
				A.unInvoke();
				return true;
			}
		}

		if((A!=null)&&(A.juggles.size()>=A.maxJuggles()))
		{
			mob.tell("You are already juggling the most items you can.");
			return false;
		}

		int maxToJuggle=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToJuggle=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatToJuggle.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToJuggle="ALL "+whatToJuggle.substring(4);}
		if(whatToJuggle.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToJuggle="ALL "+whatToJuggle.substring(0,whatToJuggle.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Item juggleThis=mob.fetchInventory(null,whatToJuggle+addendumStr);
			if((juggleThis!=null)&&(!juggleThis.amWearingAt(Item.INVENTORY)))
			{
				if((!juggleThis.amWearingAt(Item.HELD))&&(!juggleThis.amWearingAt(Item.WIELD)))
				{
					addendumStr="."+(++addendum);
					continue;
				}
				else
				if(!CommonMsgs.remove(mob,juggleThis,true))
					return false;
			}
			if(juggleThis==null) break;
			if((Sense.canBeSeenBy(juggleThis,mob))
			&&((A==null)||(!A.juggles.contains(juggleThis)))
			&&(!V.contains(juggleThis)))
				V.addElement(juggleThis);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToJuggle));

		if(V.size()==0)
		{
			mob.tell("You don't seem to be carrying that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			if(A==null)
			{
				beneficialAffect(mob,mob,0);
				A=(Skill_Juggle)mob.fetchEffect(ID());
				if(A==null) return false;
			}
			A.makeLongLasting();
			A.pause=true;
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				FullMsg msg=new FullMsg(mob,I,this,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> start(s) juggling <T-NAMESELF>.");
				if((A.juggles.size()<A.maxJuggles())
				&&(mob.location().okMessage(mob,msg)))
				{
					mob.location().send(mob,msg);
					A.juggleItem(I);
				}
				else
					break;
			}
			A.pause=false;
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to juggle, but messes up.");


		// return whether it worked
		return success;
	}
}