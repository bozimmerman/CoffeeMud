package com.planet_ink.coffee_mud.interfaces;

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
public interface Trap extends Ability
{
	public final static int TRAP_NEEDLE=0;
	public final static int TRAP_PIT_BLADE=1;
	public final static int TRAP_GAS=2;
	public final static int TRAP_SPELL=3;
	
	public boolean isABomb();
	public void activateBomb();

	public boolean disabled();
	public void disable();
	public void spring(MOB target);
	public boolean sprung();
	public void setReset(int reset);
	public int getReset();
	
	public boolean maySetTrap(MOB mob, int asLevel);
	public boolean canSetTrapOn(MOB mob, Environmental E);
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel);
	public String requiresToSet();
	
}
