package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Song_Knowledge extends Song
{
	public String ID() { return "Song_Knowledge"; }
	public String name(){ return "Knowledge";}
	public int quality(){ return OK_OTHERS;}
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(invoker==null) return;
		affectableStats.setStat(CharStats.WISDOM,Math.round(affectableStats.getStat(CharStats.WISDOM)+2));
		affectableStats.setStat(CharStats.INTELLIGENCE,Math.round(affectableStats.getStat(CharStats.INTELLIGENCE)+2));
	}
}
