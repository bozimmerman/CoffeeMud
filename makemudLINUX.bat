#This file assumes that Sun's SDK has been installed (to any directory...)
#Before using this on a UNIX machine, you must 'chmod 755 makemud.bat' to make this file executable by the UNIX machine


#place your path to javac here - if global just use javac! FYI this MUST be a full path...
JAVACPATH=/home/knoppix/j2sdk1.4.2/bin/javac

 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Archon/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Bard/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Common/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Druid/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Fighter/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Languages/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Misc/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Paladin/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Prayers/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Properties/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Poisons/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Diseases/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Ranger/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Skills/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Songs/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Specializations/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Spells/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Thief/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Abilities/Traps/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/application/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Areas/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Behaviors/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/CharClasses/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Clans/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Commands/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Commands/base/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Commands/sysop/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Commands/extra/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/common/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/exceptions/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Exits/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/interfaces/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Items/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Items/Weapons/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Items/MiscMagic/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Items/Armor/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Locales/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/MOBS/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/Races/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/system/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/system/I3/*.java
 #Added -deprecation flag to next line - to get more info when compiling...
 $JAVACPATH -deprecation  ./com/planet_ink/coffee_mud/system/I3/net/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/system/I3/packets/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/system/I3/persist/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/system/I3/server/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/utils/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/web/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/web/macros/*.java
 $JAVACPATH  ./com/planet_ink/coffee_mud/web/macros/grinder/*.java
 $JAVACPATH -deprecation   ./com/planet_ink/fakedb/*.java
