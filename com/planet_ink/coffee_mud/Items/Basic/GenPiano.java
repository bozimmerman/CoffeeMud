package com.planet_ink.coffee_mud.Items.Basic;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class GenPiano extends GenRideable implements MusicalInstrument
{
	public String ID(){	return "GenPiano";}
	public GenPiano()
	{
		super();
		setName("a generic piano");
		setDisplayText("a generic piano sits here.");
		setDescription("");
		baseGoldValue=1015;
		baseEnvStats().setLevel(1);
		recoverEnvStats();
		baseEnvStats().setWeight(2000);
		rideBasis=Rideable.RIDEABLE_SIT;
		riderCapacity=2;
		setMaterial(EnvResource.RESOURCE_OAK);
	}

	public void recoverEnvStats(){Sense.setReadable(this,false); super.recoverEnvStats();}
	public int instrumentType(){return Util.s_int(readableText);}
	public void setInstrumentType(int type){readableText=(""+type);}

}
