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
echo "1. What is the location of JAVA V1.6+ (example: /opt/jdk1.6.0_09 - just to main dir NO TRAILING SLASH)?"
read Java_Home

echo "
2. Do you want to compile docs about the source code (not needed to run - javadoc etc.; more time consuming; you may need to be root...) ? [y/n]
"
read yesnodocs

echo "3. Do you want to compile the full program right now (say no if you're only doing the docs, or changing the name of your MUD)? [y/n]"
read yesnofullcompile

echo "4. What have you decided to name your MUD (you can change this later by editing the mudUNIX.sh file) ?"
read MUDname

echo "Alright, we're good to go, compiling will begin in 5 seconds."

sleep 5
#Next we will set up some needed variables for compiling.... and then run an if statement

JAVACPATH="$Java_Home/bin/javac -nowarn -g -deprecation -encoding UTF8 -classpath .:./lib/js.jar:./lib/jzlib.jar" 

if [ $yesnodocs = y ] ; then

echo "/nBeginning compile of the source docs...this may take awhile...you were warned!/n"
sleep 1

$Java_Home/bin/javadoc -d ./docs -J-Xmx256m -subpackages com.planet_ink.coffee_mud

else

echo "OK no doc compiling for you...well its mainly for source developers anyways..."
echo "About to begin main compile....."
sleep 1

fi


if [ $yesnofullcompile = y ] ; then

$JAVACPATH com/planet_ink/fakedb/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/*.java
$JAVACPATH com/planet_ink/coffee_mud/application/*.java
$JAVACPATH com/planet_ink/coffee_mud/Areas/*.java
$JAVACPATH com/planet_ink/coffee_mud/Behaviors/*.java
$JAVACPATH com/planet_ink/coffee_mud/CharClasses/*.java
$JAVACPATH com/planet_ink/coffee_mud/Commands/*.java
$JAVACPATH com/planet_ink/coffee_mud/Common/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/database/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/exceptions/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/cm1/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/cm1/commands/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/i3/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/i3/net/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/i3/packets/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/i3/persist/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/i3/server/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/intermud/imc2/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/smtp/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/threads/*.java
$JAVACPATH com/planet_ink/coffee_mud/core/collections/*.java
$JAVACPATH com/planet_ink/coffee_mud/Exits/*.java
$JAVACPATH com/planet_ink/coffee_mud/Libraries/*.java
$JAVACPATH com/planet_ink/coffee_mud/Locales/*.java
$JAVACPATH com/planet_ink/coffee_mud/MOBS/*.java
$JAVACPATH com/planet_ink/coffee_mud/Races/*.java
$JAVACPATH com/planet_ink/coffee_mud/WebMacros/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Archon/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Common/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Diseases/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Druid/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Fighter/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Languages/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Misc/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Paladin/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Poisons/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Prayers/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Properties/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Ranger/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Skills/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Songs/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Specializations/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Spells/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/SuperPowers/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Tech/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Thief/*.java
$JAVACPATH com/planet_ink/coffee_mud/Abilities/Traps/*.java
$JAVACPATH com/planet_ink/coffee_mud/Areas/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Behaviors/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/CharClasses/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Commands/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Common/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Exits/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/Armor/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/Basic/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/ClanItems/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/MiscMagic/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/BasicTech/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/CompTech/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/Software/*.java
$JAVACPATH com/planet_ink/coffee_mud/Items/Weapons/*.java
$JAVACPATH com/planet_ink/coffee_mud/Libraries/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Libraries/layouts/*.java
$JAVACPATH com/planet_ink/coffee_mud/Libraries/mcppkgs/*.java
$JAVACPATH com/planet_ink/coffee_mud/Locales/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/MOBS/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/Races/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_mud/WebMacros/grinder/*.java
$JAVACPATH com/planet_ink/coffee_mud/WebMacros/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_web/converters/*.java
$JAVACPATH com/planet_ink/coffee_web/http/*.java
$JAVACPATH com/planet_ink/coffee_web/interfaces/*.java
$JAVACPATH com/planet_ink/coffee_web/server/*.java
$JAVACPATH com/planet_ink/coffee_web/servlets/*.java
$JAVACPATH com/planet_ink/coffee_web/util/*.java
$JAVACPATH com/planet_ink/siplet/applet/*.java
$JAVACPATH com/planet_ink/siplet/support/*.java

else

echo "What!? No main compile for you...maybe you just wanted to compile the docs? Or change your MUD's name...any ways...I'll make this more intuitive in the future!"
fi
echo "Writing your new mudUNIX.sh..."
rm mudUNIX.sh
echo "#You should really input a name for your MUD below...." >> mudUNIX.sh
echo "#Before using this on a UNIX machine, you must 'chmod 755 mudUNIX.sh' to make this file executable by the UNIX machine" >> mudUNIX.sh
echo "#FYI - the nohup command will make a nohup.out file, usually in the CofferMud (directory where you start this from) directory - it will log the server messages..." >> mudUNIX.sh
echo "" >> mudUNIX.sh
echo "nohup $Java_Home/bin/java -classpath \".:./lib/js.jar:./lib/jzlib.jar\" -Xms129m -Xmx256m com.planet_ink.coffee_mud.application.MUD \"$MUDname\" &" >> mudUNIX.sh
chmod 755 mudUNIX.sh
echo "Your mudUNIX.sh script has been written."
echo "To change memory or other settings, you must MANUALLY edit mudUNIX.sh after every time you run this script."
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
