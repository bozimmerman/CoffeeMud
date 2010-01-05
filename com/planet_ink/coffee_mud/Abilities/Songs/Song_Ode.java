package com.planet_ink.coffee_mud.Abilities.Songs;
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


import java.util.*;


/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class Song_Ode extends Song
{
	public String ID() { return "Song_Ode"; }
	public String name(){ return "Ode";}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_OTHERS;}
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
		{""+CMMsg.TYP_GET,"cs",""+CharStats.STAT_STRENGTH,"The strength of <O-NAME> astounds us all!"},
		{""+CMMsg.TYP_GAS,"ca",""+CharStats.STAT_SAVE_GAS,"<O-NAME> has gas, and saves us from it!"},
		{""+CMMsg.TYP_FIRE,"ca",""+CharStats.STAT_SAVE_FIRE,"We learn from firey <O-NAME> to keep our cool!"},
		{""+CMMsg.TYP_DRINK,"s","t","<O-NAME> quenches our thirst for health!"},
		{""+CMMsg.TYP_DISEASE,"ca",""+CharStats.STAT_SAVE_DISEASE,"<O-NAME> worries so about our health!"},
		{""+CMMsg.TYP_UNDEAD,"ca",""+CharStats.STAT_SAVE_UNDEAD,"<O-NAME>, the deadly one, fills our hearts with life!"},
		{""+CMMsg.TYP_COLD,"ca",""+CharStats.STAT_SAVE_COLD,"Cold-hearted <O-NAME> has shown us how to keep warm!"},
		{""+CMMsg.TYP_CAST_SPELL,"ca",""+CharStats.STAT_SAVE_MAGIC,"<O-NAME>, the enchanting, helps us avoid magic spells!"},
		{""+CMMsg.TYP_ACID,"ca",""+CharStats.STAT_SAVE_ACID,"<O-NAME> drips with warnings about acid!"},
		{""+CMMsg.TYP_ELECTRIC,"ca",""+CharStats.STAT_SAVE_ELECTRIC,"The electric one, <O-NAME>, has shown us how to stay grounded!"},
		{""+CMMsg.TYP_LOOK,"es",""+(EnvStats.CAN_SEE_DARK|EnvStats.CAN_SEE_HIDDEN),"<O-NAME> is ever watchful, and has opened our eyes!"},
        {""+CMMsg.TYP_EXAMINE,"es",""+(EnvStats.CAN_SEE_BONUS|EnvStats.CAN_SEE_METAL),"<O-NAME> is a carefuly observer, and never misses the finest detail!"},
		{""+CMMsg.TYP_JUSTICE,"ca",""+CharStats.STAT_SAVE_JUSTICE,"The dreadful <O-NAME> shows us the way out of danger!"},
		{""+CMMsg.TYP_MIND,"ca",""+CharStats.STAT_SAVE_MIND,"<O-NAME>, the deceiver, reminds us to keep our wits!"},
		{""+CMMsg.TYP_PARALYZE,"ca",""+CharStats.STAT_SAVE_PARALYSIS,"<O-NAME> paralyzes <O-HIS-HER> enemies, that we may stay free!"},
		{""+CMMsg.TYP_POISON,"ca",""+CharStats.STAT_SAVE_POISON,"<O-NAME> is poison to others, but protects <O-HIS-HER> friends!"},
		{""+CMMsg.TYP_SLEEP,"s","i","<O-NAME>, the sleepy, reminds us to stay fit and rested!"},
		{""+CMMsg.TYP_SIT,"s","m","<O-NAME>, the contemplative, teaches us <O-HIS-HER> power!"},
		{""+CMMsg.TYP_SPEAK,"cs",""+CharStats.STAT_WISDOM,"<O-NAME>, the chatty one, fills us with <O-HIS-HER> wisdom!"},
		{""+CMMsg.TYP_WATER,"ca",""+CharStats.STAT_SAVE_WATER,"<O-NAME>, the wet one, keeps us so dry!"},
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
				int q=CMath.s_int(t.substring(0,x));
				t=t.substring(x+1);
				if(q>=0)
					for(int i=0;i<stuff.length;i++)
					{
						if(CMath.s_int(stuff[i][0])==q)
							counts[i]++;
					}
				x=t.indexOf(";");
			}
			int wa=-1;
			for(int i=0;i<stuff.length;i++)
				if(CMath.s_int(stuff[i][0])==CMMsg.TYP_WEAPONATTACK)
				{ wa=i; break;}

			if(wa>=0) counts[wa]=counts[wa]/25;

			Vector V=new Vector();
			while(V.size()<counts.length)
			{
				int high=-1;
				int which=-1;
				for(int i=0;i<counts.length;i++)
				{
					if((counts[i]>high)&&(!V.contains(Integer.valueOf(i))))
					{
						high=counts[i];
						which=i;
					}
				}
				if(which>=0)
					V.addElement(Integer.valueOf(which));
			}
			Vector V2=new Vector();
			for(int i=0;i<3;i++)
			{
				Integer ref=(Integer)V.elementAt(i);
				Integer which=null;
				while((which==null)||(V2.contains(which)))
				{
					Integer w=(Integer)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
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
						affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+ticks+getXLEVELLevel(invoker()));
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
					int stat=CMath.s_int(chk[2]);
					if(CharStats.CODES.isBASE(stat))
						if(ticks>5) ticks=5;
					affectableStats.setStat(stat,affectableStats.getStat(stat)+ticks+getXLEVELLevel(invoker()));
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
					case 'h': affectableStats.setHunger(affectableStats.getHunger()+ticks+getXLEVELLevel(invoker()));
							  break;
					case 't': affectableStats.setThirst(affectableStats.getThirst()+ticks+getXLEVELLevel(invoker()));
							  break;
					case 'v': affectableStats.setMovement(affectableStats.getMovement()+ticks+getXLEVELLevel(invoker()));
							  break;
					case 'm': affectableStats.setMana(affectableStats.getMana()+ticks+getXLEVELLevel(invoker()));
							  break;
					case 'i': affectableStats.setHitPoints(affectableStats.getHitPoints()+ticks+getXLEVELLevel(invoker()));
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
		&&(CMLib.flags().canBeSeenBy(whom,invoker())))
		{
			if(trail==null) trail=new StringBuffer("");
			ensureCmds();
			if(cmds.containsKey(""+msg.sourceMinor()))
				trail.append(msg.sourceMinor()+";");
		}
		super.executeMsg(myHost,msg);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if((mob.isMonster())&&(mob.isInCombat()))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean tick(Tickable ticking, int tickID)
	{
		MOB mob=(MOB)affected;
		if(mob==null)
			return false;
		if(song==null)
		{
			if((whom==null)
			   ||(commonRoomSet==null)
			   ||(!commonRoomSet.contains(whom.location()))
			   ||(CMLib.flags().isSleeping(invoker))
			   ||(!CMLib.flags().canBeSeenBy(whom,invoker)))
					return possiblyUnsing(mob,null,false);
		}

		if((whom!=null)&&(song!=null)&&(affected==invoker())
		   &&(CMLib.dice().rollPercentage()<10))
		{
			Hashtable H=getSongBenefits(song);
			Vector V=new Vector();
			for(Enumeration e=H.keys();e.hasMoreElements();)
				V.addElement(e.nextElement());
			Integer I=(Integer)V.elementAt(CMLib.dice().roll(1,V.size(),-1));
			String[] chk=stuff[I.intValue()];
			invoker().location().show(invoker(),this,whom,CMMsg.MSG_SPEAK,"<S-NAME> sing(s) '"+chk[3]+"'.");
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
			int code=CMath.s_int(s.substring(0,x));
			s=s.substring(x+1);
			x=s.indexOf(";");
			if(x>=0)
			{
				int tick=CMath.s_int(s.substring(0,x));
				s=s.substring(x+1);
				benefits.put(Integer.valueOf(code),Integer.valueOf(tick));
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        steadyDown=-1;
		if(auto) return false;

		Hashtable H=getSongs();
		if(commands.size()==0)
		{
			Song_Ode A=(Song_Ode)mob.fetchEffect(ID());
			if((A!=null)&&(A.whom!=null)&&(A.song==null))
			{
				String str="^S<S-NAME> finish(es) composing the "+A.songOf()+".^?";
				CMMsg msg=CMClass.getMsg(mob,null,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,str);
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
		String name=CMParms.combine(commands,0);
		for(Enumeration e=H.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(CMLib.english().containsString(key,name))
			{
                invoker=mob;
                originRoom=mob.location();
                commonRoomSet=getInvokerScopeRoomSet(null);
				name=key;
				song=(String)H.get(name);
				benefits=null;
				whom=mob.location().fetchInhabitant(name);
				if((whom==null)||(!whom.name().equals(name)))
					whom=CMLib.players().getPlayer(name);
				if((whom==null)||(!whom.name().equals(name)))
				{
					whom=CMClass.getMOB("StdMOB");
					whom.setName(name);
					whom.setLocation(mob.location());
				}
				return super.invoke(mob,commands,givenTarget,auto,asLevel);
			}
		}

		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(target==mob)
		{
			mob.tell("You may not compose an ode about yourself!");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			unsing(mob,mob,false);
            invoker=mob;
            originRoom=mob.location();
            commonRoomSet=getInvokerScopeRoomSet(null);
			whom=target;
			String str="^S<S-NAME> begin(s) to compose an "+songOf()+".^?";
			CMMsg msg=CMClass.getMsg(mob,null,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song_Ode newOne=(Song_Ode)copyOf();
				newOne.whom=target;
				newOne.trail=new StringBuffer("");
				newOne.song=null;
				mob.addEffect(newOne);
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> lose(s) <S-HIS-HER> inspiration.");
		return success;
	}
}
