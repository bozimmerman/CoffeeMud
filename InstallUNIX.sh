#!/bin/sh
#The following either will echo (send to display), read (get in a variable) or sleep (wait for a #few seconds).
#Written by: Wolf a.k.a. TheOneWolf a.k.a. TheSimDude a.k.a. The_One a.k.a. Wolfgang Richter
#If you have a problem...e-mail the ghostbusters...I mean...e-mail wrichter@att.net...yeah :)
#VERSION: TEST5

echo "Hello, and welcome to the UNIX CoffeeMud automated installer."
sleep 1
echo "I am going to ask you a few questions, and as you answer them I will create your MUD."
sleep 1
echo "* MAKE SURE YOU HAVE WRITE PERMISSION TO mudUNIX.sh *"
sleep 1
echo "***** MAKE SURE YOU ARE EXECUTING THIS SCRIPT FROM THE MAIN COFFEEMUD DIR (not source dir)*****"

sleep 2

echo "Questions:"
echo "1. What is the location of JAVA (example: /opt/j2sdk1.4.2 - just to main dir NO TRAILING SLASH)?"
read Java_Home

echo "
2. Do you want to compile docs about the source code (not needed to run - javadoc etc.; more time consuming; you may need to be root...) ? [y/n]
"
read yesnodocs

echo "3. Do you want to compile the full program right now (say no if you're only doing the docs, or changing the name of your MUD)? [y/n]"
read yesnofullcompile

echo "3. What have you decided to name your MUD (you can change this later by editing the mudUNIX.sh file) ?"
read MUDname

echo "Alright, we're good to go, compiling will begin in 10 seconds."

sleep 10
#Next we will set up some needed variables for compiling.... and then run an if statement

JAVACPATH=$Java_Home/bin/javac

if [ $yesnodocs = y ] ; then

echo "/nBeginning compile of the source docs...this may take awhile...you were warned!/n"
sleep 1

$Java_Home/bin/javadoc -d ./docs -J-Xmx128m ./com/planet_ink/coffee_mud/Abilities/*.java ./com/planet_ink/coffee_mud/Abilities/Archon/*.java ./com/planet_ink/coffee_mud/common/*.java ./com/planet_ink/coffee_mud/Abilities/Diseases/*.java ./com/planet_ink/coffee_mud/Abilities/Druid/*.java ./com/planet_ink/coffee_mud/Abilities/Fighter/*.java ./com/planet_ink/coffee_mud/Abilities/Languages/*.java ./com/planet_ink/coffee_mud/Abilities/Misc/*.java ./com/planet_ink/coffee_mud/Abilities/Paladin/*.java ./com/planet_ink/coffee_mud/Abilities/Poisons/*.java ./com/planet_ink/coffee_mud/Abilities/Prayers/*.java ./com/planet_ink/coffee_mud/Abilities/Properties/*.java ./com/planet_ink/coffee_mud/Abilities/Ranger/*.java ./com/planet_ink/coffee_mud/Abilities/Skills/*.java ./com/planet_ink/coffee_mud/Abilities/Songs/*.java ./com/planet_ink/coffee_mud/Abilities/Specializations/*.java ./com/planet_ink/coffee_mud/Abilities/Spells/*.java ./com/planet_ink/coffee_mud/Abilities/Thief/*.java ./com/planet_ink/coffee_mud/Abilities/Traps/*.java ./com/planet_ink/coffee_mud/application/*.java ./com/planet_ink/coffee_mud/Areas/*.java ./com/planet_ink/coffee_mud/Behaviors/*.java ./com/planet_ink/coffee_mud/CharClasses/*.java ./com/planet_ink/coffee_mud/Commands/*.java ./com/planet_ink/coffee_mud/Commands/base/*.java ./com/planet_ink/coffee_mud/Commands/sysop/*.java ./com/planet_ink/coffee_mud/Commands/extra/*.java ./com/planet_ink/coffee_mud/common/*.java ./com/planet_ink/coffee_mud/exceptions/*.java ./com/planet_ink/coffee_mud/Exits/*.java ./com/planet_ink/coffee_mud/i3/*.java ./com/planet_ink/coffee_mud/i3/net/*.java ./com/planet_ink/coffee_mud/i3/packets/*.java ./com/planet_ink/coffee_mud/i3/persist/*.java ./com/planet_ink/coffee_mud/i3/server/*.java ./com/planet_ink/coffee_mud/interfaces/*.java ./com/planet_ink/coffee_mud/Items/*.java ./com/planet_ink/coffee_mud/Items/Weapons/*.java ./com/planet_ink/coffee_mud/Items/MiscMagic/*.java ./com/planet_ink/coffee_mud/Items/ClanItems/*.java ./com/planet_ink/coffee_mud/Items/Armor/*.java ./com/planet_ink/coffee_mud/Locales/*.java ./com/planet_ink/cof
fee_mud/MOBS/*.java ./com/planet_ink/coffee_mud/Races/*.java ./com/planet_ink/coffee_mud/system/*.java ./com/planet_ink/coffee_mud/utils/*.java ./com/planet_ink/coffee_mud/web/*.java ./com/planet_ink/coffee_mud/web/macros/*.java ./com/planet_ink/coffee_mud/web/macros/grinder/*.java

else

echo "OK no doc compiling for you...well its mainly for source developers anyways..."
echo "About to begin main compile....."
sleep 1

fi


if [ $yesnofullcompile = y ] ; then

$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Archon/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Common/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Diseases/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Druid/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Fighter/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Languages/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Misc/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Paladin/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Poisons/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Prayers/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Properties/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Ranger/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Skills/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Songs/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Specializations/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Spells/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Thief/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Abilities/Traps/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/application/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Areas/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Behaviors/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/CharClasses/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Commands/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Commands/base/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Commands/sysop/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Commands/extra/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/common/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/exceptions/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Exits/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/i3/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/i3/net/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/i3/packets/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/i3/persist/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/i3/server/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/interfaces/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Items/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Items/Weapons/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Items/MiscMagic/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Items/ClanItems/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Items/Armor/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Locales/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/MOBS/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/Races/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/system/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/utils/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/web/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/web/macros/*.java
$JAVACPATH -verbose ./com/planet_ink/coffee_mud/web/macros/grinder/*.java
$JAVACPATH -verbose ./com/planet_ink/fakedb/*.java

else

echo "What!? No main compile for you...maybe you just wanted to compile the docs? Or change your MUD's name...any ways...I'll make this more intuitive in the future!"
fi

#the following replaces ur mud name in the mudUNIX.sh, putting output in mudUNIX.new - then it copies mudUNIX.new to mudUNIX.sh
#mudUNIX.new gets deleted.....
echo "About to change the default name of the MUD in mudUNIX.sh to:"
echo $MUDname
sed -e 's/Your Muds Name Here/'$MUDname'/g' < mudUNIX.sh > mudUNIX.new
mv mudUNIX.new mudUNIX.sh
echo "Your MUD's name has been written..."
echo "In order to change the name again, you must MANUALLY edit mudUNIX.sh.....sorry thats not built in yet (to this script...)"
echo "Would you like to start your mud up? [y/n]"
read startyeanay

if [ $startyeanay = y ] ; then
sleep 1
echo "OK your MUD is now starting..."
sh mudUNIX.sh

else
echo "Alright, MUD not starting now, if you wish to start it later just do sh mudUNIX.sh"
echo "Hope you enjoy CoffeeMud!"
fi
