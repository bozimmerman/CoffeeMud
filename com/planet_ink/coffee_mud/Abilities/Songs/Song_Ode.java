package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Song_Ode extends Song
{
	public String ID() { return "Song_Ode"; }
	public String name(){ return "Ode";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public Environmental newInstance(){	return new Song_Ode();	}
	public MOB whom=null;
	public Hashtable benefits=null;

	protected String song=null;
	protected Hashtable songs=null;
	protected StringBuffer trail=null;
	protected String songOf(){ return "Ode"+((whom==null)?"":" to "+whom.name())+"";}
	protected boolean skipStandardSongTick(){return (song==null);}
	protected static final Hashtable cmds=new Hashtable();
	protected static final String[][] stuff={
		{""+CMMsg.TYP_EAT,"s","h","<O-NAME> knows our hunger pains!"},
		{""+CMMsg.TYP_GET,"cs",""+CharStats.STRENGTH,"The strength of <O-NAME> astounds us all!"},
		{""+CMMsg.TYP_GAS,"ca",""+CharStats.SAVE_GAS,"<O-NAME> has gas, and saves us from it!"},
		{""+CMMsg.TYP_FIRE,"ca",""+CharStats.SAVE_FIRE,"We learn from firey <O-NAME> to keep our cool!"},
		{""+CMMsg.TYP_DRINK,"s","t","<O-NAME> quenches our thirst for health!"},
		{""+CMMsg.TYP_DISEASE,"ca",""+CharStats.SAVE_DISEASE,"<O-NAME> worries so about our health!"},
		{""+CMMsg.TYP_UNDEAD,"ca",""+CharStats.SAVE_UNDEAD,"<O-NAME>, the deadly one, fills our hearts with life!"},
		{""+CMMsg.TYP_COLD,"ca",""+CharStats.SAVE_COLD,"Cold-hearted <O-NAME> has shown us how to keep warm!"},
		{""+CMMsg.TYP_CAST_SPELL,"ca",""+CharStats.SAVE_MAGIC,"<O-NAME>, the enchanting, helps us avoid magic spells!"},
		{""+CMMsg.TYP_ACID,"ca",""+CharStats.SAVE_ACID,"<O-NAME> drips with warnings about acid!"},
		{""+CMMsg.TYP_ELECTRIC,"ca",""+CharStats.SAVE_ELECTRIC,"The electric one, <O-NAME>, has shown us how to stay grounded!"},
		{""+CMMsg.TYP_EXAMINESOMETHING,"es",""+(EnvStats.CAN_SEE_DARK|EnvStats.CAN_SEE_HIDDEN),"<O-NAME> is ever watchful, and has opened our eyes!"},
		{""+CMMsg.TYP_JUSTICE,"ca",""+CharStats.SAVE_JUSTICE,"The dreadful <O-NAME> shows us the way out of danger!"},
		{""+CMMsg.TYP_MIND,"ca",""+CharStats.SAVE_MIND,"<O-NAME>, the deceiver, reminds us to keep our wits!"},
		{""+CMMsg.TYP_PARALYZE,"ca",""+CharStats.SAVE_PARALYSIS,"<O-NAME> paralyzes <O-HIS-HER> enemies, that we may stay free!"},
		{""+CMMsg.TYP_POISON,"ca",""+CharStats.SAVE_POISON,"<O-NAME> is poison to others, but protects <O-HIS-HER> friends!"},
		{""+CMMsg.TYP_SLEEP,"s","i","<O-NAME>, the sleepy, reminds us to stay fit and rested!"},
		{""+CMMsg.TYP_SIT,"s","m","<O-NAME>, the contemplative, teaches us <O-HIS-HER> power!"},
		{""+CMMsg.TYP_SPEAK,"cs",""+CharStats.WISDOM,"<O-NAME>, the chatty one, fills us with <O-HIS-HER> wisdom!"},
		{""+CMMsg.TYP_WATER,"ca",""+CharStats.SAVE_WATER,"<O-NAME>, the wet one, keeps us so dry!"},
		{""+CMMsg.TYP_WEAPONATTACK,"e","a","<O-NAME> the viscious has shown us to take arms!"},
		{""+CMMsg.TYP_LEAVE,"s","v","<O-NAME>, the wanderer, gives us our second wind!"}
	};

	public String composition()
	{
		String comp="";
		if(trail!=null)
		{
			String t=trail.toString();
			int[] counts=new int[stuff.length];
			int x=t.indexOf(";");
			while(x>=0)
			{
				int q=Util.s_int(t.substring(0,x));
				t=t.substring(x+1);
				if(q>=0)
					for(int i=0;i<stuff.length;i++)
					{
						if(Util.s_int(stuff[i][0])==q)
							counts[i]++;
					}
				x=t.indexOf(";");
			}
			int wa=-1;
			for(int i=0;i<stuff.length;i++)
				if(Util.s_int(stuff[i][0])==CMMsg.TYP_WEAPONATTACK)
				{ wa=i; break;}

			if(wa>=0) counts[wa]=counts[wa]/25;

			Vector V=new Vector();
			while(V.size()<counts.length)
			{
				int high=-1;
				int which=-1;
				for(int i=0;i<counts.length;i++)
				{
					if((counts[i]>high)&&(!V.contains(new Integer(i))))
					{
						high=counts[i];
						which=i;
					}
				}
				if(which>=0)
					V.addElement(new Integer(which));
			}
			Vector V2=new Vector();
			for(int i=0;i<3;i++)
			{
				Integer ref=(Integer)V.elementAt(i);
				Integer which=null;
				while((which==null)||(V2.contains(which)))
				{
					Integer w=(Integer)V.elementAt(Dice.roll(1,V.size(),-1));
					if(counts[w.intValue()]==counts[ref.intValue()])
						which=w;
				}
				V2.addElement(which);
				comp+=which.intValue()+";"+counts[which.intValue()]+";";
			}
		}
		return comp;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if((whom!=null)&&(song!=null))
		{
			Hashtable H=getSongBenefits(song);
			for(Enumeration e=H.keys();e.hasMoreElements();)
			{
				Integer I=(Integer)e.nextElement();
				String[] chk=stuff[I.intValue()];
				if((chk!=null)&&(chk[1].startsWith("e")))
				{
					int ticks=((Integer)H.get(I)).intValue();
					if(ticks<=0) ticks=1;
					switch(chk[2].charAt(0))
					{
					case 'a':
						if(ticks>25) ticks=25;
						affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+ticks);
						break;
					default:
						break;
					}
				}
			}
		}
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if((whom!=null)&&(song!=null))
		{
			Hashtable H=getSongBenefits(song);
			for(Enumeration e=H.keys();e.hasMoreElements();)
			{
				Integer I=(Integer)e.nextElement();
				String[] chk=stuff[I.intValue()];
				if((chk!=null)&&(chk[1].startsWith("c")))
				{
					int ticks=((Integer)H.get(I)).intValue();
					if(ticks>50) ticks=50;
					if(ticks<=0) ticks=1;
					int stat=Util.s_int(chk[2]);
					if(stat<CharStats.NUM_BASE_STATS)
						if(ticks>5) ticks=5;
					affectableStats.setStat(stat,affectableStats.getStat(stat)+ticks);
				}
			}
		}
	}

	public void affectCharState(MOB affected, CharState affectableStats)
	{
		if((whom!=null)&&(song!=null))
		{
			Hashtable H=getSongBenefits(song);
			for(Enumeration e=H.keys();e.hasMoreElements();)
			{
				Integer I=(Integer)e.nextElement();
				String[] chk=stuff[I.intValue()];
				if((chk!=null)&&(chk[1].startsWith("s")))
				{
					int ticks=((Integer)H.get(I)).intValue();
					if(ticks>50) ticks=50;
					if(ticks<=0) ticks=1;
					switch(chk[2].charAt(0))
					{
					case 'h': affectableStats.setHunger(affectableStats.getHunger()+ticks);
							  break;
					case 't': affectableStats.setThirst(affectableStats.getThirst()+ticks);
							  break;
					case 'v': affectableStats.setMovement(affectableStats.getMovement()+ticks);
							  break;
					case 'm': affectableStats.setMana(affectableStats.getMana()+ticks);
							  break;
					case 'i': affectableStats.setHitPoints(affectableStats.getHitPoints()+ticks);
							  break;
					}
				}
			}
		}
	}

	public void ensureCmds()
	{
		synchronized(cmds)
		{
			if(cmds.size()==0)
			{
				for(int i=0;i<stuff.length;i++)
					cmds.put(stuff[i][0],stuff[i]);
			}
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((whom!=null)
		&&(song==null)
		&&(msg.amISource(whom))
		&&(Sense.canBeSeenBy(whom,invoker())))
		{
			if(trail==null) trail=new StringBuffer("");
			ensureCmds();
			if(cmds.containsKey(""+msg.sourceMinor()))
				trail.append(msg.sourceMinor()+";");
		}
		super.executeMsg(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		if(song==null)
		{
			if((whom==null)
			   ||(whom.location()!=invoker.location())
			   ||(Sense.isSleeping(invoker))
			   ||(!Sense.canBeSeenBy(whom,invoker)))
			{
				unsing(mob,null,this);
				return false;
			}
		}

		if((whom!=null)&&(song!=null)&&(affected==invoker())
		   &&(Dice.rollPercentage()<10))
		{
			Hashtable H=getSongBenefits(song);
			Vector V=new Vector();
			for(Enumeration e=H.keys();e.hasMoreElements();)
				V.addElement(e.nextElement());
			Integer I=(Integer)V.elementAt(Dice.roll(1,V.size(),-1));
			String[] chk=stuff[I.intValue()];
			invoker().location().show(invoker(),null,whom,CMMsg.MSG_SPEAK,"<S-NAME> sing(s) '"+chk[3]+"'.");
		}


		if(!super.tick(ticking,tickID))
			return false;

		return true;
	}

	public Hashtable getSongBenefits(String s)
	{
		if(benefits!=null)
			return benefits;
		benefits=new Hashtable();

		int x=s.indexOf(";");
		while(x>=0)
		{
			int code=Util.s_int(s.substring(0,x));
			s=s.substring(x+1);
			x=s.indexOf(";");
			if(x>=0)
			{
				int tick=Util.s_int(s.substring(0,x));
				s=s.substring(x+1);
				benefits.put(new Integer(code),new Integer(tick));
			}
			x=s.indexOf(";");
		}
		return benefits;
	}

	public String text()
	{
		StringBuffer x=new StringBuffer("");
		for(Enumeration e=getSongs().keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			String notkey=(String)getSongs().get(key);
			x.append(key+"|~|"+notkey+"[|]");
		}
		miscText=x.toString();
		return x.toString();
	}

	public Hashtable getSongs()
	{
		if(songs!=null) return songs;
		String t=miscText;
		int x=t.indexOf("|~|");
		songs=new Hashtable();
		while(x>=0)
		{
			String n=t.substring(0,x);
			t=t.substring(x+3);
			x=t.indexOf("[|]");
			String y="";
			if(x>=0)
			{
				y=t.substring(0,x);
				t=t.substring(x+3);
				songs.put(n,y);
			}
			x=t.indexOf("|~|");
		}
		return songs;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(auto) return false;

		Hashtable H=getSongs();
		if(commands.size()==0)
		{
			Song_Ode A=(Song_Ode)mob.fetchEffect(ID());
			if((A!=null)&&(A.whom!=null)&&(A.song==null))
			{
				String str="^S<S-NAME> finish(es) composing the "+A.songOf()+".^?";
				FullMsg msg=new FullMsg(mob,null,this,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,str);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					mob.delEffect(A);
					getSongs().put(A.whom.name(),A.composition());
					whom=null;
					return true;
				}
				return false;
			}

			StringBuffer str=new StringBuffer("");
			for(Enumeration e=H.keys();e.hasMoreElements();)
				str.append((String)e.nextElement()+" ");
			mob.tell("Compose or sing an ode about whom?");
			if(str.length()>0)
				mob.tell("You presently have odes written about: "+str.toString().trim()+".");
			return false;
		}
		String name=Util.combine(commands,0);
		for(Enumeration e=H.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(CoffeeUtensils.containsString(key,name))
			{
				name=key;
				song=(String)H.get(name);
				benefits=null;
				whom=mob.location().fetchInhabitant(name);
				if((whom==null)||(!whom.name().equals(name)))
					whom=CMMap.getPlayer(name);
				if((whom==null)||(!whom.name().equals(name)))
				{
					whom=CMClass.getMOB("StdMOB");
					whom.setName(name);
					whom.setLocation(mob.location());
				}
				return super.invoke(mob,commands,givenTarget,auto);
			}
		}

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("You may not compose an ode about yourself!");
			return false;
		}

		boolean success=profficiencyCheck(0,auto);
		if(success)
		{
			unsing(mob,mob,null);
			whom=target;
			String str="^S<S-NAME> begin(s) to compose an "+songOf()+".^?";
			FullMsg msg=new FullMsg(mob,null,this,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song_Ode newOne=(Song_Ode)copyOf();
				newOne.whom=target;
				newOne.trail=new StringBuffer("");
				newOne.song=null;
				newOne.referenceSong=newOne;
				mob.addEffect(newOne);
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> lose(s) <S-HIS-HER> inspiration.");
		return success;
	}
}
