package com.planet_ink.coffee_mud.interfaces;

public interface EnvResource extends Item
{
	// item materials
	public final static int MATERIAL_UNKNOWN=0;
	public final static int MATERIAL_CLOTH=1<<8;
	public final static int MATERIAL_LEATHER=2<<8;
	public final static int MATERIAL_METAL=3<<8;
	public final static int MATERIAL_MITHRIL=4<<8;
	public final static int MATERIAL_WOODEN=5<<8;
	public final static int MATERIAL_GLASS=6<<8;
	public final static int MATERIAL_VEGETATION=7<<8;
	public final static int MATERIAL_FLESH=8<<8;
	public final static int MATERIAL_PAPER=9<<8;
	public final static int MATERIAL_ROCK=10<<8;
	public final static int MATERIAL_LIQUID=11<<8;
	public final static int MATERIAL_PRECIOUS=12<<8;
	public final static int MATERIAL_ENERGY=13<<8;
	public final static int MATERIAL_PLASTIC=14<<8;

	public final static int MATERIAL_MASK=255<<8;
	
	
	public final static String[] MATERIAL_DESCS={
	"UNKNOWN",
	"CLOTH",
	"LEATHER",
	"METAL",
	"MITHRIL",
	"WOODEN",
	"GLASS",
	"VEGETATION",
	"FLESH",
	"PAPER",
	"ROCK",
	"LIQUID",
	"PRECIOUS",
	"ENERGY",
	"PLASTIC"};
	
	public final static int RESOURCE_NOTHING=MATERIAL_UNKNOWN|0;
	public final static int RESOURCE_MEAT=MATERIAL_FLESH|1;
	public final static int RESOURCE_BEEF=MATERIAL_FLESH|2;
	public final static int RESOURCE_PORK=MATERIAL_FLESH|3;
	public final static int RESOURCE_POULTRY=MATERIAL_FLESH|4;
	public final static int RESOURCE_MUTTON=MATERIAL_FLESH|5;
	public final static int RESOURCE_FISH=MATERIAL_FLESH|6;
	public final static int RESOURCE_WHEAT=MATERIAL_VEGETATION|7;
	public final static int RESOURCE_CORN=MATERIAL_VEGETATION|8;
	public final static int RESOURCE_RICE=MATERIAL_VEGETATION|9;
	public final static int RESOURCE_CARROTS=MATERIAL_VEGETATION|10;
	public final static int RESOURCE_TOMATOES=MATERIAL_VEGETATION|11;
	public final static int RESOURCE_PEPPERS=MATERIAL_VEGETATION|12;
	public final static int RESOURCE_GREENS=MATERIAL_VEGETATION|13;
	public final static int RESOURCE_FRUIT=MATERIAL_VEGETATION|14;
	public final static int RESOURCE_APPLES=MATERIAL_VEGETATION|15;
	public final static int RESOURCE_BERRIES=MATERIAL_VEGETATION|16;
	public final static int RESOURCE_ORANGES=MATERIAL_VEGETATION|17;
	public final static int RESOURCE_LEMONS=MATERIAL_VEGETATION|18;
	public final static int RESOURCE_GRAPES=MATERIAL_VEGETATION|19;
	public final static int RESOURCE_OLIVES=MATERIAL_VEGETATION|20;
	public final static int RESOURCE_POTATOES=MATERIAL_VEGETATION|21;
	public final static int RESOURCE_CACTUS=MATERIAL_VEGETATION|22;
	public final static int RESOURCE_DATES=MATERIAL_VEGETATION|23;
	public final static int RESOURCE_SEAWEED=MATERIAL_VEGETATION|24;
	public final static int RESOURCE_STONE=MATERIAL_ROCK|25;
	public final static int RESOURCE_LIMESTONE=MATERIAL_ROCK|26;
	public final static int RESOURCE_FLINT=MATERIAL_ROCK|27;
	public final static int RESOURCE_GRANITE=MATERIAL_ROCK|28;
	public final static int RESOURCE_OBSIDIAN=MATERIAL_ROCK|29;
	public final static int RESOURCE_MARBLE=MATERIAL_ROCK|30;
	public final static int RESOURCE_SAND=MATERIAL_ROCK|31;
	public final static int RESOURCE_JADE=MATERIAL_PRECIOUS|32;
	public final static int RESOURCE_IRON=MATERIAL_METAL|33;
	public final static int RESOURCE_LEAD=MATERIAL_METAL|34;
	public final static int RESOURCE_BRONZE=MATERIAL_METAL|35;
	public final static int RESOURCE_SILVER=MATERIAL_METAL|36;
	public final static int RESOURCE_GOLD=MATERIAL_METAL|37;
	public final static int RESOURCE_ZINC=MATERIAL_METAL|38;
	public final static int RESOURCE_COPPER=MATERIAL_METAL|39;
	public final static int RESOURCE_TIN=MATERIAL_METAL|40;
	public final static int RESOURCE_MITHRIL=MATERIAL_MITHRIL|41;
	public final static int RESOURCE_ADAMANTITE=MATERIAL_MITHRIL|42;
	public final static int RESOURCE_STEEL=MATERIAL_METAL|43;
	public final static int RESOURCE_BRASS=MATERIAL_METAL|44;
	public final static int RESOURCE_WOOD=MATERIAL_WOODEN|45;
	public final static int RESOURCE_PINE=MATERIAL_WOODEN|46;
	public final static int RESOURCE_BALSA=MATERIAL_WOODEN|47;
	public final static int RESOURCE_OAK=MATERIAL_WOODEN|48;
	public final static int RESOURCE_MAPLE=MATERIAL_WOODEN|49;
	public final static int RESOURCE_REDWOOD=MATERIAL_WOODEN|50;
	public final static int RESOURCE_HICKORY=MATERIAL_WOODEN|51;
	public final static int RESOURCE_SCALES=MATERIAL_LEATHER|52;
	public final static int RESOURCE_FUR=MATERIAL_CLOTH|53;
	public final static int RESOURCE_LEATHER=MATERIAL_LEATHER|54;
	public final static int RESOURCE_HIDE=MATERIAL_CLOTH|55;
	public final static int RESOURCE_WOOL=MATERIAL_CLOTH|56;
	public final static int RESOURCE_FEATHERS=MATERIAL_CLOTH|57;
	public final static int RESOURCE_COTTON=MATERIAL_CLOTH|58;
	public final static int RESOURCE_HEMP=MATERIAL_CLOTH|59;
	public final static int RESOURCE_FRESHWATER=MATERIAL_LIQUID|60;
	public final static int RESOURCE_SALTWATER=MATERIAL_LIQUID|61;
	public final static int RESOURCE_DRINKABLE=MATERIAL_LIQUID|62;
	public final static int RESOURCE_GLASS=MATERIAL_GLASS|63;
	public final static int RESOURCE_PAPER=MATERIAL_PAPER|64;
	public final static int RESOURCE_CLAY=MATERIAL_GLASS|65;
	public final static int RESOURCE_CHINA=MATERIAL_GLASS|66;
	public final static int RESOURCE_DIAMOND=MATERIAL_PRECIOUS|67;
	public final static int RESOURCE_CRYSTAL=MATERIAL_GLASS|68;
	public final static int RESOURCE_GEM=MATERIAL_PRECIOUS|69;
	public final static int RESOURCE_PEARL=MATERIAL_PRECIOUS|70;
	public final static int RESOURCE_PLATINUM=MATERIAL_METAL|71;
	public final static int RESOURCE_MILK=MATERIAL_LIQUID|72;
	public final static int RESOURCE_EGGS=MATERIAL_FLESH|73;
	public final static int RESOURCE_HOPS=MATERIAL_VEGETATION|74;
	public final static int RESOURCE_COFFEEBEANS=MATERIAL_VEGETATION|75;
	public final static int RESOURCE_COFFEE=MATERIAL_LIQUID|76;
	public final static int RESOURCE_OPAL=MATERIAL_PRECIOUS|77;
	public final static int RESOURCE_TOPAZ=MATERIAL_PRECIOUS|78;
	public final static int RESOURCE_AMETHYST=MATERIAL_PRECIOUS|79;
	public final static int RESOURCE_GARNET=MATERIAL_PRECIOUS|80;
	public final static int RESOURCE_AMBER=MATERIAL_PRECIOUS|81;
	public final static int RESOURCE_AQUAMARINE=MATERIAL_PRECIOUS|82;
	public final static int RESOURCE_CRYSOBERYL=MATERIAL_PRECIOUS|83;
	public final static int RESOURCE_IRONWOOD=MATERIAL_WOODEN|84;
	public final static int RESOURCE_SILK=MATERIAL_CLOTH|85;
	public final static int RESOURCE_COCOA=MATERIAL_VEGETATION|86;
	public final static int RESOURCE_BLOOD=MATERIAL_LIQUID|87;
	public final static int RESOURCE_BONE=MATERIAL_GLASS|88;
	public final static int RESOURCE_COAL=MATERIAL_ROCK|89;
	public final static int RESOURCE_LAMPOIL=MATERIAL_LIQUID|90;
	public final static int RESOURCE_POISON=MATERIAL_LIQUID|91;
	public final static int RESOURCE_LIQUOR=MATERIAL_LIQUID|92;
	public final static int RESOURCE_SUGAR=MATERIAL_VEGETATION|93;
	public final static int RESOURCE_HONEY=MATERIAL_LIQUID|94;
	public final static int RESOURCE_BARLEY=MATERIAL_VEGETATION|95;
	public final static int RESOURCE_MUSHROOMS=MATERIAL_VEGETATION|96;
	public final static int RESOURCE_HERBS=MATERIAL_VEGETATION|97;
	public final static int RESOURCE_VINE=MATERIAL_VEGETATION|98;
	public final static int RESOURCE_FLOWERS=MATERIAL_VEGETATION|99;
	public final static int RESOURCE_PLASTIC=MATERIAL_PLASTIC|100;
	public final static int RESOURCE_RUBBER=MATERIAL_PLASTIC|101;
	public final static int RESOURCE_EBONY=MATERIAL_ROCK|102;
	public final static int RESOURCE_IVORY=MATERIAL_ROCK|103;
	public final static int RESOURCE_WAX=MATERIAL_FLESH|104;
	public final static int RESOURCE_NUTS=MATERIAL_VEGETATION|105;
	public final static int RESOURCE_BREAD=MATERIAL_VEGETATION|106;
	public final static int RESOURCE_CRACKER=MATERIAL_VEGETATION|107;
	public final static int RESOURCE_YEW=MATERIAL_WOODEN|108;
	public final static int RESOURCE_DUST=MATERIAL_ROCK|109;
	public final static int RESOURCE_PIPEWEED=MATERIAL_VEGETATION|110;
	public final static int RESOURCE_ENERGY=MATERIAL_ENERGY|111;
	public final static int RESOURCE_STRAWBERRIES=MATERIAL_VEGETATION|112;
	public final static int RESOURCE_BLUEBERRIES=MATERIAL_VEGETATION|113;
	public final static int RESOURCE_RASPBERRIES=MATERIAL_VEGETATION|114;
	public final static int RESOURCE_BOYSENBERRIES=MATERIAL_VEGETATION|115;
	public final static int RESOURCE_BLACKBERRIES=MATERIAL_VEGETATION|116;
	public final static int RESOURCE_SMURFBERRIES=MATERIAL_VEGETATION|117;
	public final static int RESOURCE_PEACHES=MATERIAL_VEGETATION|118;
	public final static int RESOURCE_PLUMS=MATERIAL_VEGETATION|119;
	public final static int RESOURCE_ONIONS=MATERIAL_VEGETATION|120;
	public final static int RESOURCE_CHERRIES=MATERIAL_VEGETATION|121;
	public final static int RESOURCE_GARLIC=MATERIAL_VEGETATION|122;
	public final static int RESOURCE_PINEAPPLES=MATERIAL_VEGETATION|123;
	public final static int RESOURCE_COCONUTS=MATERIAL_VEGETATION|124;   
	public final static int RESOURCE_BANANAS=MATERIAL_VEGETATION|125;   
	public final static int RESOURCE_LIMES=MATERIAL_VEGETATION|126;
	public final static int RESOURCE_SAP=MATERIAL_LIQUID|127;      
	public final static int RESOURCE_ONYX=MATERIAL_PRECIOUS|128;
	public final static int RESOURCE_TURQUIOSE=MATERIAL_PRECIOUS|129;
	public final static int RESOURCE_PERIDOT=MATERIAL_PRECIOUS|130;
	public final static int RESOURCE_QUARTZ=MATERIAL_PRECIOUS|131;
	public final static int RESOURCE_LAPIS=MATERIAL_PRECIOUS|132;
	public final static int RESOURCE_BLOODSTONE=MATERIAL_PRECIOUS|133;
	public final static int RESOURCE_MOONSTONE=MATERIAL_PRECIOUS|134;
	public final static int RESOURCE_ALEXANDRITE=MATERIAL_PRECIOUS|135;
	public final static int RESOURCE_TEAK=MATERIAL_WOODEN|136;
	public final static int RESOURCE_CEDAR=MATERIAL_WOODEN|137;
	public final static int RESOURCE_ELM=MATERIAL_WOODEN|138;
	public final static int RESOURCE_CHERRYWOOD=MATERIAL_WOODEN|139;
	public final static int RESOURCE_BEECHWOOD=MATERIAL_WOODEN|140;
	public final static int RESOURCE_WILLOW=MATERIAL_WOODEN|141;
	public final static int RESOURCE_SYCAMORE=MATERIAL_WOODEN|142;
	public final static int RESOURCE_SPRUCE=MATERIAL_WOODEN|143;
	public final static int RESOURCE_MESQUITE=MATERIAL_WOODEN|144;
	public final static int RESOURCE_BASALT=MATERIAL_ROCK|145;
	public final static int RESOURCE_SHALE=MATERIAL_ROCK|146;
	public final static int RESOURCE_PUMICE=MATERIAL_ROCK|147;
	public final static int RESOURCE_SANDSTONE=MATERIAL_ROCK|148;   
	public final static int RESOURCE_SOAPSTONE=MATERIAL_ROCK|149;   
	public final static int RESOURCE_SALMON=MATERIAL_FLESH|150;
	public final static int RESOURCE_CARP=MATERIAL_FLESH|151;
	public final static int RESOURCE_TROUT=MATERIAL_FLESH|152;
	public final static int RESOURCE_SHRIMP=MATERIAL_FLESH|153;
	public final static int RESOURCE_TUNA=MATERIAL_FLESH|154;
	public final static int RESOURCE_CATFISH=MATERIAL_FLESH|155;
	public final static int RESOURCE_BAMBOO=MATERIAL_WOODEN|156;
   
	
	public final static int RESOURCE_MASK=255;	

	
	public final static String[] RESOURCE_DESCS={
	"NOTHING", //0
	"MEAT",  //1
	"BEEF", //2
	"PORK", //3
	"POULTRY", //4
	"MUTTON", //5
	"FISH",//6
	"WHEAT", //7
	"CORN", //8
	"RICE", //9
	"CARROTS", //10
	"TOMATOES", //11
	"PEPPERS", //12
	"GREENS",//13
	"FRUIT", //14
	"APPLES", //15
	"BERRIES", //16
	"ORANGES", //17
	"LEMONS", //18
	"GRAPES", //19
	"OLIVES",//20
	"POTATOES", //21
	"CACTUS", //22
	"DATES", //23
	"SEAWEED", //24
	"STONE", //25
	"LIMESTONE",//26
	"FLINT", //27
	"GRANITE", //28
	"OBSIDIAN", //29
	"MARBLE", //30
	"SAND", //31
	"JADE", //32
	"IRON",//33
	"LEAD", //34
	"BRONZE", //35
	"SILVER", //36
	"GOLD", //37
	"ZINC", //38
	"COPPER", //39
	"TIN", //40
	"MITHRIL",//41
	"ADAMANTITE", //42
	"STEEL", //43
	"BRASS", //44
	"WOOD", //45
	"PINE", //46
	"BALSA", //47
	"OAK", //48
	"MAPLE",//49
	"REDWOOD", //50
	"HICKORY", //51
	"SCALES", //52
	"FUR", //53
	"LEATHER", //54
	"HIDE", //55
	"WOOL",//56
	"FEATHERS",//57 
	"COTTON", //58
	"HEMP",//59
	"WATER",//60
	"SALT WATER",//61
	"LIQUID",//62
	"GLASS",//63
	"PAPER",//64
	"CLAY",//65
	"CHINA",//66
	"DIAMOND",//67
	"CRYSTAL",//68
	"GEM", //69
	"PEARL", //70
	"PLATINUM",//71
	"MILK",//72
	"EGGS",//73
	"HOPS",//74
	"COFFEEBEANS",//75
	"COFFEE",//76
	"OPAL",//77
	"TOPAZ",//78
	"AMETHYST",//79
	"GARNET",//80
	"AMBER", //81
	"AQUAMARINE", //82
	"CRYSOBERYL", //83
	"IRONWOOD", //84
	"SILK", //85
	"COCOA", //86
	"BLOOD", //87
	"BONE", //88
	"COAL", //89
	"LAMP OIL", //90
	"POISON", // 91
	"LIQUOR", // 92
	"SUGAR", // 93
	"HONEY", // 94
	"BARLEY", // 95
	"MUSHROOMS", // 96
	"HERBS", // 97
	"VINE", // 98
	"FLOWERS", // 99
	"PLASTIC", // 100
	"RUBBER", // 101
	"EBONY", // 102
	"IVORY", // 103
	"WAX", // 104
	"NUTS", // 105
	"BREAD", // 106
	"CRACKER", // 107
	"YEW", // 108
	"DUST", // 109
	"PIPEWEED", // 110
	"ENERGY", // 111
	"STRAWBERRIES", // 112
	"BLUEBERRIES", // 113
	"RASPBERRIES", // 114
	"BOYSENBERRIES", // 115
	"BLACKBERRIES", // 116
	"SMURFBERRIES", // 117
	"PEACHES", // 118
	"PLUMS", // 119
	"ONIONS", // 120
	"CHERRIES", // 121
	"GARLIC", // 122
	"PINEAPPLES", // 123
	"COCONUTS", // 124
	"BANANAS", // 125
	"LIMES", // 126
	"SAP", // 127
	"ONYX", // 128
	"TURQUOISE", // 129
	"PERIDOT", // 130
	"QUARTZ", // 131
	"LAPIS", // 133
	"BLOODSTONE", // 133
	"MOONSTONE", // 134
	"ALEXANDRITE", // 135
	"TEAK", // 136
	"CEDAR", // 137
	"ELM", // 138
	"CHERRYWOOD", // 139
	"BEECHWOOD", // 140
	"WILLOW", // 141
	"SYCAMORE", // 142
	"SPRUCE", // 143
	"MESQUITE", // 144
	"BASALT", // 145
	"SHALE", // 146
	"PUMICE", // 147
	"SANDSTONE", // 148
	"SOAPSTONE", // 149
	"SALMON", // 150
	"CARP", // 151
	"TROUT", // 152
	"SHRIMP", // 153
	"TUNA", // 154
	"CATFISH", // 155
	"BAMBOO", // 156
	};
	
	public final static int DATA_CODE=0;
	public final static int DATA_VALUE=1;
	public final static int DATA_FREQ=2;
	public final static int DATA_STRENGTH=3;
	public final static int DATA_BOUANCY=4;
	
	
	public final static int[][] RESOURCE_DATA={ 
	// full code, base value, frequency, strength (1-10), bouancy
	{RESOURCE_NOTHING,		0,	0,	0,	0}, 
	{RESOURCE_MEAT,			4,	20,	1,	3000}, 
	{RESOURCE_BEEF,			6,	20,	1,	3000}, 
	{RESOURCE_PORK,			8,	20,	1,	2500}, 
	{RESOURCE_POULTRY,		3,	20,	1,	2000}, 
	{RESOURCE_MUTTON,		4,	20,	1,	2800}, 
	{RESOURCE_FISH,			5,	100,1,	590}, 
	{RESOURCE_WHEAT,		1,	20,	1,	770}, 
	{RESOURCE_CORN,			1,	20,	1,	720}, 
	{RESOURCE_RICE,			1,	20,	1,	750}, 
	{RESOURCE_CARROTS,		1,	5,	1,	720}, 
	{RESOURCE_TOMATOES,		1,	5,	1,	640}, 
	{RESOURCE_PEPPERS,		1,	5,	1,	640}, 
	{RESOURCE_GREENS,		1,	5,	1,	540}, 
	{RESOURCE_FRUIT,		2,	10,	1,	720}, 
	{RESOURCE_APPLES,		2,	10,	1,	640}, 
	{RESOURCE_BERRIES,		2,	15,	1,	720}, 
	{RESOURCE_ORANGES,		2,	10,	1,	640}, 
	{RESOURCE_LEMONS,		2,	10,	1,	480}, 
	{RESOURCE_GRAPES,		3,	5,	1,	680}, 
	{RESOURCE_OLIVES,		2,	5,	1,	640}, 
	{RESOURCE_POTATOES,		1,	5,	1,	770}, 
	{RESOURCE_CACTUS,		2,	5,	1,	680}, 
	{RESOURCE_DATES,		2,	2,	1,	720}, 
	{RESOURCE_SEAWEED,		1,	50,	1,	540}, 
	{RESOURCE_STONE,		1,	80,	5,	2500}, 
	{RESOURCE_LIMESTONE,	1,	20,	4,	1550}, 
	{RESOURCE_FLINT,		1,	10,	4,	2600}, 
	{RESOURCE_GRANITE,		2,	10,	6,	2690}, 
	{RESOURCE_OBSIDIAN,		10,	5,	6,	2650}, 
	{RESOURCE_MARBLE,		20,	5,	5,	2560}, 
	{RESOURCE_SAND,			1,	50,	1,	1600}, 
	{RESOURCE_JADE,			50,	2,	5,	3800}, 
	{RESOURCE_IRON,			20,	10,	6,	7900}, 
	{RESOURCE_LEAD,			10,	10,	5,	11300}, 
	{RESOURCE_BRONZE,		10,	10,	5,	8100}, 
	{RESOURCE_SILVER,		30,	2,	5,	10500}, 
	{RESOURCE_GOLD,			50,	1,	5,	19320}, 
	{RESOURCE_ZINC,			10,	5,	5,	7100}, 
	{RESOURCE_COPPER,		10,	10,	5,	8900}, 
	{RESOURCE_TIN,			10,	10,	4,	7300}, 
	{RESOURCE_MITHRIL,		200,1,	9,	3990}, 
	{RESOURCE_ADAMANTITE,	500,1,	10,	4500}, 
	{RESOURCE_STEEL,		150,0,	8,	7840}, 
	{RESOURCE_BRASS,		120,0,	6,	8500}, 
	{RESOURCE_WOOD,			2,	10,	3,	920}, 
	{RESOURCE_PINE,			4,	10,	3,	650}, 
	{RESOURCE_BALSA,		1,	5,	2,	130}, 
	{RESOURCE_OAK,			5,	10,	3,	720}, 
	{RESOURCE_MAPLE,		10,	5,	3,	689}, 
	{RESOURCE_REDWOOD,		20,	2,	3,	450}, 
	{RESOURCE_HICKORY,		5,	5,	3,	830}, 
	{RESOURCE_SCALES,		10,	20,	4,	1800}, 
	{RESOURCE_FUR,			20,	20,	2,	890}, 
	{RESOURCE_LEATHER,		10,	20,	2,	945}, 
	{RESOURCE_HIDE,			4,	20,	1,	920}, 
	{RESOURCE_WOOL,			10,	20,	1,	1310}, 
	{RESOURCE_FEATHERS,		10,	20,	1,	20}, 
	{RESOURCE_COTTON,		5,	20,	1,	590}, 
	{RESOURCE_HEMP,			4,	10,	1,	720}, 
	{RESOURCE_FRESHWATER,	0,	100,0,	1000}, 
	{RESOURCE_SALTWATER,	0,	100,0,	1030}, 
	{RESOURCE_DRINKABLE,	0,	1,	0,	1000}, 
	{RESOURCE_GLASS,		10,	0,	3,	2800}, 
	{RESOURCE_PAPER,		10,	0,	0,	920},
	{RESOURCE_CLAY,			1,	50,	1,	1750}, 
	{RESOURCE_CHINA,		30,	0,	3,	2400}, 
	{RESOURCE_DIAMOND,	  5000,	1,	9,	3510}, 
	{RESOURCE_CRYSTAL,		10,	5,	3,	2200}, 
	{RESOURCE_GEM,			100,1,	3,	3500}, 
	{RESOURCE_PEARL,	  1000,	1,	4,	2000}, 
	{RESOURCE_PLATINUM,		80,	1,	6,	21450}, 
	{RESOURCE_MILK,			2,	10,	0,	1020}, 
	{RESOURCE_EGGS,			2,	10,	0,	1120}, 
	{RESOURCE_HOPS,			2,	20,	1,	340}, 
	{RESOURCE_COFFEEBEANS,	2,	10,	1,	560}, 
	{RESOURCE_COFFEE,		0,	10,	0,	430}, 
	{RESOURCE_OPAL,			80,	2,	5,	2250}, 
	{RESOURCE_TOPAZ,		200,2,	5,	3570}, 
	{RESOURCE_AMETHYST,		300,2,	5,	2651}, 
	{RESOURCE_GARNET,		70,	2,	5,	3870}, 
	{RESOURCE_AMBER,		80,	5,	5,	2500}, 
	{RESOURCE_AQUAMARINE,	50,	2,	5,	2800}, 
	{RESOURCE_CRYSOBERYL,	50,	2,	5,	2800}, 
	{RESOURCE_IRONWOOD,		25,	5,	4,	990},
	{RESOURCE_SILK,			200,5,	1,	1600},
	{RESOURCE_COCOA,		4,	5,	0,	590},
	{RESOURCE_BLOOD,		1,	100,0,	1025},
	{RESOURCE_BONE,			1,	100,5,	1600},
	{RESOURCE_COAL,			1,	50,	1,	1800},
	{RESOURCE_LAMPOIL,		1,	10,	1,	880},
	{RESOURCE_POISON,		1,	1,	1,	1000},
	{RESOURCE_LIQUOR,		10,	1,	1,	790},
	{RESOURCE_SUGAR,		1,	50,	1,	1600}, 
	{RESOURCE_HONEY,		1,	50,	1,	1600}, 
	{RESOURCE_BARLEY,		1,	20,	1,	610}, 
	{RESOURCE_MUSHROOMS,	1,	20,	1,	500},
	{RESOURCE_HERBS,		1,	10,	1,	770},
	{RESOURCE_VINE,			1,	10,	1,	880},
	{RESOURCE_FLOWERS,		1,	10,	1,	720},
	{RESOURCE_PLASTIC,		25,	0,	4,	950}, 
	{RESOURCE_RUBBER,		25,	0,	1,	1506}, 
	{RESOURCE_EBONY,		5,	5,	5,	2910}, 
	{RESOURCE_IVORY,		5,	5,	3,	1840}, 
	{RESOURCE_WAX,			1,	0,	0,	900}, 
	{RESOURCE_NUTS,			0,	20,	0,	640}, 
	{RESOURCE_BREAD,		3,	0,	0,	660}, 
	{RESOURCE_CRACKER,		2,	0,	0,	200}, 
	{RESOURCE_YEW,			15,	2,	5,	850}, 
	{RESOURCE_DUST,			0,	20,	0,	1120}, 
	{RESOURCE_PIPEWEED,		3,	10,	1,	320}, 
	{RESOURCE_ENERGY,		30,	0,	4,	0}, 
	{RESOURCE_STRAWBERRIES, 10,	1,	1,	750},
	{RESOURCE_BLUEBERRIES,	10,	1,	1,	750},	
	{RESOURCE_RASPBERRIES,	10,	1,	1,	750},	
	{RESOURCE_BOYSENBERRIES,10,	1,	1,	750},	
	{RESOURCE_BLACKBERRIES,	10,	1,	1,	750},	
	{RESOURCE_SMURFBERRIES,	10,	1,	1,	750},	
	{RESOURCE_PEACHES,		10,	1,	1,	700},	
	{RESOURCE_PLUMS,		10,	1,	1,	710},	
	{RESOURCE_ONIONS,		10,	1,	1,	760},	
	{RESOURCE_CHERRIES,		10,	1,	1,	810},	
	{RESOURCE_GARLIC,		10,	1,	1,	815},	
	{RESOURCE_PINEAPPLES,	10,	1,	1,	500},	
	{RESOURCE_COCONUTS,		10,	1,	2,	250},	
	{RESOURCE_BANANAS,		10,	1,	1,	790},	
	{RESOURCE_LIMES,		10,	1,	1,	690},	
	{RESOURCE_SAP,			10,	1,	1,	1600},	
	{RESOURCE_ONYX,			70,	1,	8,	3300},	
	{RESOURCE_TURQUIOSE,	70,	1,	8,	3300},	
	{RESOURCE_PERIDOT,		65,	1,	6,	3300},	
	{RESOURCE_QUARTZ,		25,	1,	5,	3300},	
	{RESOURCE_LAPIS,		70,	1,	6,	3300},	
	{RESOURCE_BLOODSTONE,	85,	1,	8,	3300},	
	{RESOURCE_MOONSTONE,	90,	1,	8,	3300},	
	{RESOURCE_ALEXANDRITE,	95,	1,	9,	3300},	
	{RESOURCE_TEAK,			20,	2,	3,	1000},	
	{RESOURCE_CEDAR,		15,	2,	3,	900},	
	{RESOURCE_ELM,			15,	2,	3,	1100},	
	{RESOURCE_CHERRYWOOD,	17,	2,	3,	900},	
	{RESOURCE_BEECHWOOD,	12,	2,	3,	975},	
	{RESOURCE_WILLOW,		12,	2,	1,	1000},	
	{RESOURCE_SYCAMORE,		11,	2,	2,	1000},	
	{RESOURCE_SPRUCE,		12,	2,	3,	990},	
	{RESOURCE_MESQUITE,		9,	2,	3,	1150},	
	{RESOURCE_BASALT,		10,	2,	4,	3300},	
	{RESOURCE_SHALE,		5,	2,	2,	1200},	
	{RESOURCE_PUMICE,		5,	2,	4,	600},	
	{RESOURCE_SANDSTONE,	10,	2,	2,	3500},	
	{RESOURCE_SOAPSTONE,	60,	2,	5,	3600},	
	{RESOURCE_SALMON,		6,	1,	1,	1000},	
	{RESOURCE_CARP,			6,	1,	1,	1000},	
	{RESOURCE_TROUT,		6,	1,	1,	1000},	
	{RESOURCE_SHRIMP,		6,	1,	1,	1000},	
	{RESOURCE_TUNA,			6,	1,	1,	1000},	
	{RESOURCE_CATFISH,		6,	1,	1,	1000},	
	{RESOURCE_BAMBOO,		15,	10,	4,	120},	
	};
	
	
	public static final int[] FISHES={
	RESOURCE_FISH,
	RESOURCE_SALMON,
	RESOURCE_CARP,
	RESOURCE_TROUT,
	RESOURCE_SHRIMP,
	RESOURCE_TUNA,
	RESOURCE_CATFISH
	};
	public static final int[] BERRIES={
	RESOURCE_BERRIES,
	RESOURCE_STRAWBERRIES,
	RESOURCE_BLUEBERRIES,
	RESOURCE_RASPBERRIES,
	RESOURCE_BOYSENBERRIES,
	RESOURCE_BLACKBERRIES,
	RESOURCE_SMURFBERRIES
	};
	
}