#!/bin/sh
#The following either will echo (send to display), read (get in a variable) or sleep (wait for a #few seconds).
#Written by: Wolf a.k.a. TheOneWolf a.k.a. TheSimDude a.k.a. The_One a.k.a. Wolfgang Richter
#VERSION: TEST5

echo "Hello, and welcome to the UNIX CoffeeMud automated configuration helper."
sleep 1
echo "I am going to ask you a few questions, and as you answer them I will create your MUD."
sleep 1
echo "* MAKE SURE YOU HAVE WRITE PERMISSION TO mud.sh *"
sleep 1
echo "***** MAKE SURE YOU ARE EXECUTING THIS SCRIPT FROM THE MAIN COFFEEMUD DIR *****"

sleep 2

echo "Questions:"
echo "1. What is the location of JAVA V1.8+ (example: /opt/jdk1.8.0_09 - just to main dir NO TRAILING SLASH)?"
read Java_Home

echo "
2. Do you want to compile docs about the source code (not needed to run - javadoc etc.; more time consuming; you may need to be root...) ? [y/n]
"
read yesnodocs

echo "3. Do you want to compile the full program right now (say no if you're only doing the docs, or changing the name of your MUD)? [y/n]"
read yesnofullcompile

echo "4. What have you decided to name your MUD (you can change this later by editing the mud.sh file) ?"
read MUDname

echo "5. Do you want your passwords hashed? [y/n]"
read yesnohashpasswords

if [ $yesnohashpasswords = y ] ; then
	sed -i 's/HASHPASSWORDS=NO/HASHPASSWORDS=YES/g' coffeemud.ini
fi

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

find com -name "*.java" | xargs $JAVACPATH -nowarn -Xlint:none -g -classpath ".:lib/jzlib.jar:lib/js.jar"

else

echo "What!? No main compile for you...maybe you just wanted to compile the docs? Or change your MUD's name...any ways...I'll make this more intuitive in the future!"
fi
echo "Writing your new mud.sh..."
rm mud.sh
echo "#You should really input a name for your MUD below...." >> mud.sh
echo "#Before using this on a UNIX machine, you must 'chmod 755 mud.sh' to make this file executable by the UNIX machine" >> mud.sh
echo "#FYI - the nohup command will make a nohup.out file, usually in the CofferMud (directory where you start this from) directory - it will log the server messages..." >> mud.sh
echo "" >> mud.sh
echo "nohup $Java_Home/bin/java -classpath \".:./lib/js.jar:./lib/jzlib.jar\" -Xms129m -Xmx256m com.planet_ink.coffee_mud.application.MUD \"$MUDname\" &" >> mud.sh
chmod 755 mud.sh
echo "Your mud.sh script has been written."
echo "To change memory or other settings, you must MANUALLY edit mud.sh after every time you run this script."
echo "Would you like to start your mud up? [y/n]"
read startyeanay

if [ $startyeanay = y ] ; then
sleep 1
echo "OK your MUD is now starting..."
sh mud.sh

else
echo "Alright, MUD not starting now, if you wish to start it later just do sh mud.sh"
echo "Hope you enjoy CoffeeMud!"
fi
