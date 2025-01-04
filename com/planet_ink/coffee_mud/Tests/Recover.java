package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
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
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/*
Copyright 2024-2025 Bo Zimmerman

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
public class Recover extends StdTest
{
	@Override
	public String ID()
	{
		return "Recover";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] {"info_all"};
	}

	private int[] recoverMath(final int level, final int con, final int inte, final int wis, final int str, final boolean isHungry, final boolean isThirsty, final boolean isFatigued, final boolean isSleeping, final boolean isSittingOrRiding,final boolean isFlying,final boolean isSwimming)
	{
		/*	# @x1=stat(con/str/int-wis), @x2=level, @x3=hungry?1:0, @x4=thirsty?1:0, @x5=fatigued?0:1 # @x6=asleep?1:0, @x7=sitorride?1:0, @x8=flying?0:1, @x9=swimming?0:1 */
		final CompiledFormula stateHitPointRecoverFormula = CMath.compileMathExpression("5+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0))*@x2/9.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) - (@xx/2.0*@x9))");
		final CompiledFormula stateManaRecoverFormula = CMath.compileMathExpression("25+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0) - (@xx*@x5/2.0))*@x2/50.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) - (@xx/2.0*@x9))");
		final CompiledFormula stateMovesRecoverFormula = CMath.compileMathExpression("25+(((@x1 - (@xx*@x3/2.0) - (@xx*@x4/2.0) - (@xx*@x5/2.0))*@x2/10.0) + (@xx*@x6*.5) + (@xx/4.0*@x7) + (@xx/4.0*@x8) - (@xx/2.0*@x9))");
		final double[] vals=new double[]{
			con,
			level,
			isHungry?1.0:0.0,
			isThirsty?1.0:0.0,
			isFatigued?1.0:0.0,
			isSleeping?1.0:0.0,
			isSittingOrRiding?1.0:0.0,
			isFlying?1.0:0.0,
			isSwimming?1.0:0.0
		};
		final int[] v=new int[3];
		v[0]= (int)Math.round(CMath.parseMathExpression(stateHitPointRecoverFormula, vals, 0.0));

		vals[0]=((inte+wis));
		v[1]= (int)Math.round(CMath.parseMathExpression(stateManaRecoverFormula, vals, 0.0));

		vals[0]=str;
		v[2]= (int)Math.round(CMath.parseMathExpression(stateMovesRecoverFormula, vals, 0.0));
		return v;
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		for(final int level : new int[]{1,10,50,90})
		{
			int hp;
			int mana;
			int move;
			int stat;
			switch(level)
			{
			default:
			case 1:
				hp=20;
				mana=100;
				move=100;
				stat=15;
				break;
			case 10:
				hp=120;
				mana=150;
				move=190;
				stat=17;
				break;
			case 50:
				hp=500;
				mana=420;
				move=520;
				stat=18;
				break;
			case 90:
				hp=1200;
				mana=700;
				move=1000;
				stat=18;
				break;
			}
			final StringBuilder str=new StringBuilder("level: "+level+"\n\r");
			int[] resp;
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
			str.append(L("standing: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/true,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
			str.append(L("hungry: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/true,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
			str.append(L("thirsty: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/true,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/false);
			str.append(L("fatigued: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/true,/*Sit*/false,/*Fly*/false,/*Swim*/false);
			str.append(L("sleep: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/true,/*Fly*/false,/*Swim*/false);
			str.append(L("sitting: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/true,/*Swim*/false);
			str.append(L("flying: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			resp=recoverMath(level,stat,stat,stat-4,stat,/*Hun*/false,/*Thirs*/false,/*Fatig*/false,/*Sleep*/false,/*Sit*/false,/*Fly*/false,/*Swim*/true);
			str.append(L("swimming: hpticks=@x1,  manaticks=@x2,  moveticks=@x3\n\r",""+(hp/resp[0]),""+(mana/resp[1]),""+(move/resp[2])));
			str.append("\n\r");
			mob.tell(str.toString());
		}
		return null;
	}
}
