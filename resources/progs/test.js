var lib=Packages.com.planet_ink.coffee_mud.core.CMLib;
var combatlib = lib.combat();
var IS_SITTING =  Packages.com.planet_ink.coffee_mud.Common.interfaces.EnvStats.IS_SITTING;
var IS_SLEEPING =  Packages.com.planet_ink.coffee_mud.Common.interfaces.EnvStats.IS_SLEEPING;
var CAN_NOT_SEE =  Packages.com.planet_ink.coffee_mud.Common.interfaces.EnvStats.CAN_NOT_SEE;
var STAT_STRENGTH =  Packages.com.planet_ink.coffee_mud.Common.interfaces.CharStats.STAT_STRENGTH;
var STAT_INTELLIGENCE =  Packages.com.planet_ink.coffee_mud.Common.interfaces.CharStats.STAT_INTELLIGENCE;
var STAT_DEXTERITY =  Packages.com.planet_ink.coffee_mud.Common.interfaces.CharStats.STAT_DEXTERITY;
	
function makemob()
{
	var mob = Packages.com.planet_ink.coffee_mud.core.CMClass.getMOB("StdMOB");
	var weap = Packages.com.planet_ink.coffee_mud.core.CMClass.getItem("StdWeapon");
	var intt = lib.dice().roll(1,10,0);
	var str = lib.dice().roll(1,10,0);
	var dex = lib.dice().roll(1,10,0);
	var intDiff = lib.dice().roll(1,3,0)-1;
	var strDiff = lib.dice().roll(1,3,0)-1;
	var dexDiff = lib.dice().roll(1,3,0)-1;
	var level = lib.dice().roll(1,10,0);
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
	
	var A = Packages.com.planet_ink.coffee_mud.core.CMClass.getAbility("Prop_Adjuster");
	A.setMiscText(toJavaString("str+"+strDiff+" dex+"+dexDiff+" intDiff+"+intDiff));
	mob.addNonUninvokableEffect(A);
	
	mob.baseEnvStats().setLevel(level);
	mob.baseCharStats().setCurrentClassLevel(level);
	mob.baseCharStats().setStat(STAT_DEXTERITY,10+dex);
	mob.baseCharStats().setStat(STAT_INTELLIGENCE,10+intt);
	mob.baseCharStats().setStat(STAT_STRENGTH,10+str);
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
var rand1 = lib.dice().newInstance();
var rand2 = lib.dice().newInstance();
var oldRandLib = lib.dice();

for(x=0;x<1000;x++)
{
	var attacker = makemob();
	var defender = makemob();
	var attWeap = attacker.fetchWieldedItem();
	
	var baseDamage = lib.dice().roll(1,300,0);

	var baseSeed = lib.dice().roll(1,9999999,0);
	rand1.getRandomizer().setSeed(baseSeed);
	rand2.getRandomizer().setSeed(baseSeed);
	if(rand1.rollPercentage() != rand2.rollPercentage())
		mob().tell("Fail #"+x+", ************************* rollPercentage!");
	
	rand1.getRandomizer().setSeed(baseSeed);
	rand2.getRandomizer().setSeed(baseSeed);
	lib.registerLibrary(rand1);
	lib.math().setRand(rand1.getRandomizer());
	var s1=combatlib.criticalSpellDamage(attacker,defender,baseDamage);
	lib.registerLibrary(rand2);
	lib.math().setRand(rand2.getRandomizer());
	var s2=combatlib.NEWcriticalSpellDamage(attacker,defender,baseDamage);
	if(s1 != s2)
		mob().tell("Fail #"+x+", criticalSpellDamage "+s1+" != "+s2);
	
	rand1.getRandomizer().setSeed(baseSeed);
	rand2.getRandomizer().setSeed(baseSeed);
	lib.registerLibrary(rand1);
	lib.math().setRand(rand1.getRandomizer());
	var d1=combatlib.adjustedDamage(attacker,attWeap,defender);
	lib.registerLibrary(rand2);
	lib.math().setRand(rand2.getRandomizer());
	var d2=combatlib.NEWadjustedDamage(attacker,attWeap,defender);
	if(d1 != d2)
	{
		mob().tell("Fail #"+x+", adjustedDamage "+d1+" != "+d2);
		/*
		if(mob().session().confirm("Try again (Y/n)?","Y"))
		{
			rand1.getRandomizer().setSeed(baseSeed);
			rand2.getRandomizer().setSeed(baseSeed);
			lib.registerLibrary(rand1);
			lib.math().setRand(rand1.getRandomizer());
			d1=combatlib.adjustedDamage(attacker,attWeap,defender);
			lib.registerLibrary(rand2);
			lib.math().setRand(rand2.getRandomizer());
			d2=combatlib.NEWadjustedDamage(attacker,attWeap,defender);
			mob().tell("!Fail #"+x+", adjustedDamage "+d1+" != "+d2);
		}
		*/
	}
	
	rand1.getRandomizer().setSeed(baseSeed);
	rand2.getRandomizer().setSeed(baseSeed);
	lib.registerLibrary(rand1);
	lib.math().setRand(rand1.getRandomizer());
	var t1=combatlib.adjustedAttackBonus(attacker,defender);
	lib.registerLibrary(rand2);
	lib.math().setRand(rand2.getRandomizer());
	var t2=combatlib.NEWadjustedAttackBonus(attacker,defender);
	if(t1 != t2)
		mob().tell("Fail #"+x+", adjustedAttackBonus "+t1+" != "+t2);
	
	rand1.getRandomizer().setSeed(baseSeed);
	rand2.getRandomizer().setSeed(baseSeed);
	lib.registerLibrary(rand1);
	lib.math().setRand(rand1.getRandomizer());
	var a1=combatlib.adjustedArmor(attacker);
	lib.registerLibrary(rand2);
	lib.math().setRand(rand2.getRandomizer());
	var a2=combatlib.NEWadjustedArmor(attacker);
	if(a1 != a2)
		mob().tell("Fail #"+x+", adjustedArmor "+a1+" != "+a2);
	
	lib.registerLibrary(oldRandLib);
	attacker.destroy();
	defender.destroy();
}