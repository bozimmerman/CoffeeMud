#This file assumes that Sun's SDK has been installed (to any directory...)
#Before using this on a UNIX machine, you must 'chmod 755 makeUNIX.bat' to make this file executable by the UNIX machine
#place your path to javac here - if global just use javac! FYI this MUST be a full path...
Java_Home=/home/knoppix/j2sdk1.4.2
classpath=.;$Java_Home/lib/dt.jar;$Java_Home/lib/tools.jar
JAVACPATH=$Java_Home/bin/javac

if "$1" = "docs" then

	$Java_Home/bin/javadoc -d ./docs -J-Xmx128m ./com/planet_ink/coffee_mud/Abilities/*.java ./com/planet_ink/coffee_mud/Abilities/Archon/*.java ./com/planet_ink/coffee_mud/Abilitiescommon/*.java ./com/planet_ink/coffee_mud/Abilities/Diseases/*.java ./com/planet_ink/coffee_mud/Abilities/Druid/*.java ./com/planet_ink/coffee_mud/Abilities/Fighter/*.java ./com/planet_ink/coffee_mud/Abilities/Languages/*.java ./com/planet_ink/coffee_mud/Abilities/Misc/*.java ./com/planet_ink/coffee_mud/Abilities/Paladin/*.java ./com/planet_ink/coffee_mud/Abilities/Poisons/*.java ./com/planet_ink/coffee_mud/Abilities/Prayers/*.java ./com/planet_ink/coffee_mud/Abilities/Properties/*.java ./com/planet_ink/coffee_mud/Abilities/Ranger/*.java ./com/planet_ink/coffee_mud/Abilities/Skills/*.java ./com/planet_ink/coffee_mud/Abilities/Songs/*.java ./com/planet_ink/coffee_mud/Abilities/Specializations/*.java ./com/planet_ink/coffee_mud/Abilities/Spells/*.java ./com/planet_ink/coffee_mud/Abilities/Thief/*.java ./com/planet_ink/coffee_mud/Abilities/Traps/*.java ./com/planet_ink/coffee_mud/Application/*.java ./com/planet_ink/coffee_mud/Areas/*.java ./com/planet_ink/coffee_mud/Behaviors/*.java ./com/planet_ink/coffee_mud/CharClasses/*.java ./com/planet_ink/coffee_mud/Clans/*.java ./com/planet_ink/coffee_mud/commands/*.java ./com/planet_ink/coffee_mud/commands/base/*.java ./com/planet_ink/coffee_mud/commands/sysop/*.java ./com/planet_ink/coffee_mud/commands/extra/*.java ./com/planet_ink/coffee_mud/common/*.java ./com/planet_ink/coffee_mud/exceptions/*.java ./com/planet_ink/coffee_mud/Exits/*.java ./com/planet_ink/coffee_mud/i3/*.java ./com/planet_ink/coffee_mud/i3/net/*.java ./com/planet_ink/coffee_mud/i3/packets/*.java ./com/planet_ink/coffee_mud/i3/persist/*.java ./com/planet_ink/coffee_mud/i3/server/*.java ./com/planet_ink/coffee_mud/interfaces/*.java ./com/planet_ink/coffee_mud/Items/*.java ./com/planet_ink/coffee_mud/Items/Weapons/*.java ./com/planet_ink/coffee_mud/Items/MiscMagic/*.java ./com/planet_ink/coffee_mud/Items/Armor/*.java ./com/planet_ink/coffee_mud/Locales/*.java ./com/planet_ink/coffee_mud/MOBS/*.java ./com/planet_ink/coffee_mud/Races/*.java ./com/planet_ink/coffee_mud/system/*.java ./com/planet_ink/coffee_mud/utils/*.java ./com/planet_ink/coffee_mud/web/*.java ./com/planet_ink/coffee_mud/web/macros/*.java ./com/planet_ink/coffee_mud/web/macros/grinder/*.java 

else

	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Archon/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Common/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Diseases/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Druid/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Fighter/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Languages/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Misc/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Paladin/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Poisons/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Prayers/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Properties/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Ranger/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Skills/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Songs/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Specializations/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Spells/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Thief/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Abilities/Traps/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/application/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Areas/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Behaviors/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/CharClasses/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Clans/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/commands/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/commands/base/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/commands/sysop/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/commands/extra/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/common/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/exceptions/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Exits/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/i3/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/i3/net/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/i3/packets/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/i3/persist/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/i3/server/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/interfaces/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Items/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Items/Weapons/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Items/MiscMagic/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Items/Armor/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Locales/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/MOBS/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/Races/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/system/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/utils/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/web/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/web/macros/*.java
	$JAVACPATH -deprecation ./com/planet_ink/coffee_mud/web/macros/grinder/*.java
	$JAVACPATH -deprecation ./com/planet_ink/fakedb/*.java
fi
