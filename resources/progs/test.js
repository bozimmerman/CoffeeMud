var lib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var combatlib = lib.combat();

function makemob()
{
	var mob = Packages.com.planet_ink.coffee_mud.core.CMClass.getMOB("StdMOB");
	var weap = Packages.com.planet_ink.coffee_mud.core.CMClass.getItem("StdWeapon");
	var IS_SITTING =  Packages.com.planet_ink.coffee_mud.Common.EnvStats.IS_SITTING;
	var IS_SLEEPING =  Packages.com.planet_ink.coffee_mud.Common.EnvStats.IS_SLEEPING;
	var CAN_NOT_SEE =  Packages.com.planet_ink.coffee_mud.Common.EnvStats.CAN_NOT_SEE;
	var level = lib.dice().roll(1,100,0);
	var armor = lib.dice().roll(1,300,0);
	var attack = lib.dice().roll(1,300,0);
	var damage = lib.dice().roll(1,100,0);
	var isHungry = lib.dice().roll(1,20,0)==1;
	var isThirsty = lib.dice().roll(1,20,0)==1;
	var isFatigued = lib.dice().roll(1,20,0)==1;
	var isSleeping = lib.dice().roll(1,20,0)==1;
	var isBlind = lib.dice().roll(1,20,0)==1;
	var isSitting = false;
	if(!isSleeping)
		isSitting = lib.dice().roll(1,20,0)==1;
	
	var weaponDamage = lib.dice().roll(1,100,0);
	weap.baseEnvStats().setDamage(weaponDamage);
	weap.recoverEnvStats();
	
	mob.addInventory(weap);
	weap.wearEvenIfImpossible(mob);
	
	mob.baseEnvStats().setLevel(level);
	mob.baseCharStats().setCurrentClassLevel(level);
	mob.baseEnvStats().setAttackAdjustment(attack);
	mob.baseEnvStats().setArmor(100-armor);
	mob.baseEnvStats().setDamage(damage);
	if(isHungry)
		mob.curState().setHunger(-100);
	if(isThirsty)
		mob.curState().setThirst(-100);
	if(isFatigued)
		mob.curState().setFatigue(13000000);
	if(isSitting)
		mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|IS_SITTING);
	if(isSleeping)
		mob.baseEnvStats().setDisposition(mob.baseEnvStats().disposition()|IS_SLEEPING);
	if(isBlind)
		mob.baseEnvStats().setSensesMask(mob.baseEnvStats().sensesMask()|CAN_NOT_SEE);
	mob.recoverEnvStats();
	mob.recoverCharStats();
	mob.recoverMaxState();
	return mob;
}


var x = 0;
for(x=0;x<1000;x++)
{
	var attacker = makemob();
	var defender = makemob();
	var attWeap = attacker.fetchWieldedItem();
	
	var baseDamage = lib.dice().roll(1,300,0);
	
	var s1=combatlib.criticalSpellDamage(attacker,defender,baseDamage);
	var s2=combatlib.NEWcriticalSpellDamage(attacker,defender,baseDamage);
	if(s1 != s2)
		mob().tell("Fail #"+x+", criticalSpellDamage "+s1+" != "+s2);
	
	var d1=combatlib.adjustedDamage(attacker,attWeap,defender);
	var d2=combatlib.NEWadjustedDamage(attacker,attWeap,defender);
	if(d1 != d2)
		mob().tell("Fail #"+x+", adjustedDamage "+d1+" != "+d2);
	
	var t1=combatlib.adjustedAttackBonus(attacker,defender);
	var t2=combatlib.NEWadjustedAttackBonus(attacker,defender);
	if(t1 != t2)
		mob().tell("Fail #"+x+", adjustedAttackBonus "+t1+" != "+t2);
	
	var a1=combatlib.adjustedArmor(attacker);
	var a2=combatlib.NEWadjustedArmor(attacker);
	if(a1 != a2)
		mob().tell("Fail #"+x+", adjustedArmor "+a1+" != "+a2);
	
	attacker.destroy();
	defender.destroy();
}