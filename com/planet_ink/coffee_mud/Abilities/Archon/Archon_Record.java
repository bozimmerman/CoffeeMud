package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

/*
   Copyright 2006-2025 Bo Zimmerman

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
public class Archon_Record extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Record";
	}

	private final static String localizedName = CMLib.lang().L("Record");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings = I(new String[] { "RECORD" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected volatile Session	sess		= null;
	protected volatile String	filename	= null;
	protected volatile boolean	stripSnoop	= false;
	protected volatile boolean	stripCRLF	= false;

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if(mob.session()==null)
				mob.setSession(null);
			else
			if(sess!=null)
				mob.session().setBeingSnoopedBy(sess,false);
			sess=null;
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(sess==null)
			return false;
		if((affected instanceof MOB)
		&&(((MOB)affected).session()!=null)
		&&(!(((MOB)affected).session().isBeingSnoopedBy(sess))))
			((MOB)affected).session().setBeingSnoopedBy(sess, true);
		return true;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()>2)
		{
			if(commands.get(0).equalsIgnoreCase("set"))
			{
				if(commands.get(1).equalsIgnoreCase("filename"))
				{
					filename = CMParms.combine(commands,2);
					mob.tell(L("Ok."));
					return true;
				}
				else
				if(commands.get(1).equalsIgnoreCase("stripsnoop"))
				{
					stripSnoop = CMath.s_bool(CMParms.combine(commands,2));
					mob.tell(L("Ok."));
					return true;
				}
				else
				if(commands.get(1).equalsIgnoreCase("stripcrlf"))
				{
					stripCRLF = CMath.s_bool(CMParms.combine(commands,2));
					mob.tell(L("Ok."));
					return true;
				}
			}
		}
		MOB target=CMLib.players().getLoadPlayer(CMParms.combine(commands,0));
		if(target==null)
			target=getTargetAnywhere(mob,commands,givenTarget,false,true,false);
		if(target==null)
			return false;

		final Archon_Record A=(Archon_Record)target.fetchEffect(ID());
		if(A!=null)
		{
			final Session S = A.sess;
			A.unInvoke();
			target.delEffect(A);
			if(target.playerStats()!=null)
				target.playerStats().setLastUpdated(0);
			if(target.session()==S)
				target.setSession(null);
			else
			if((target.session()!=null)
			&&(target.session().isBeingSnoopedBy(S)))
				target.session().setBeingSnoopedBy(A.sess, false);
			mob.tell(L("@x1 will no longer be recorded.",target.Name()));
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),L("^F<S-NAME> begin(s) recording <T-NAMESELF>.^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final String filename = (this.filename!=null)?this.filename:
									"/"+target.Name()+System.currentTimeMillis()+".log";
				this.filename=null;
				final CMFile file=new CMFile(filename,mob,CMFile.FLAG_LOGERRORS);
				if(!file.canWrite())
				{
					if(!CMSecurity.isASysOp(mob)||(CMSecurity.isASysOp(target)))
						Log.sysOut("Record",mob.Name()+" failed to start recording "+target.name()+".");
				}
				else
				{
					if(!CMSecurity.isASysOp(mob)||(CMSecurity.isASysOp(target)))
						Log.sysOut("Record",mob.Name()+" started recording "+target.name()+" to /"+filename+".");
					final Archon_Record A2=(Archon_Record)copyOf();
					final Session sessF=(Session)CMClass.getCommon("FakeSession");
					if(this.stripSnoop)
						sessF.setStat("STRIPSNOOP", "true");
					if(this.stripCRLF)
						sessF.setStat("STRIPCRLF", "true");
					this.stripSnoop = this.stripCRLF = false;
					sessF.initializeSession(null,Thread.currentThread().getThreadGroup().getName(),filename);
					sessF.setMob(target);
					if(target.session()==null)
						target.setSession(sessF);
					A2.sess=sessF;
					target.addNonUninvokableEffect(A2);
					mob.tell(L("Enter RECORD @x1 again to stop recording.",target.Name()));
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to record <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
