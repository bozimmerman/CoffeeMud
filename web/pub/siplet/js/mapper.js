// uses SortArray from util.js
// uses SipletFileSystem in loadJsonMap and saveJsonMap from filesys.js

window.DirCodeNames =
{
	n: 'north',
	s: 'south',
	e: 'east',
	w: 'west',
	u: 'up',
	d: 'down',
	i: 'in',
	o: 'out',
	ne: 'northeast',
	nw: 'northwest',
	se: 'southeast',
	sw: 'southwest',
	nu: 'northup',
	nd: 'northdown',
	su: 'southup',
	sd: 'southdown',
	eu: 'eastup',
	ed: 'eastdown',
	wu: 'westup',
	wd: 'westdown'
};

window.DirNameNums =
{
	north: 1,
	northeast: 2,
	northwest: 3,
	east: 4,
	west: 5,
	south: 6,
	southeast: 7,
	southwest: 8,
	up: 9,
	down: 10,
	in: 11,
	out: 12,
	northup: 13,
	southdown: 14,
	southup: 15,
	northdown: 16,
	eastup: 17,
	westdown: 18,
	westup: 19,
	eastdown: 20
};

window.DirNumCodes = [
	'', 
	'n', 'ne', 'nw', 'e', 'w', 
	's', 'se', 'sw', 'u', 'd',
	'i', 'o', 'nu', 'sd', 'su',
	'nd', 'eu', 'wd', 'wu', 'ed'
];

window.OpDirCodes = {
	"n": "s", 
	"s": "n", 
	"e": "w", 
	"w": "e", 
	"u": "d", 
	"d": "u",
	"i": "o",
	"o": "i",
	"ne": "sw", 
	"sw": "ne", 
	"nw": "se", 
	"se": "nw",
	"nu": "sd", 
	"sd": "nu", 
	"nd": "su", 
	"su": "nd",
	"eu": "wd", 
	"wd": "eu", 
	"ed": "wu", 
	"wu": "ed"
};

window.DirCodeDeltas =
{
	n: {x:0, y:-1, z:0},
	s: {x:0, y:1, z:0},
	e: {x:1, y:0, z:0},
	w: {x:-1, y:0, z:0},
	u: {x:0, y:0, z:-1},
	d: {x:0, y:0, z:1},
	i: {x:0, y:0, z:0},
	o: {x:0, y:0, z:0},
	ne: {x:1, y:-1, z:0},
	nw: {x:-1, y:-1, z:0},
	se: {x:1, y:1, z:0},
	sw: {x:-1, y:1, z:0},
	nu: {x:0, y:-1, z:-1},
	nd: {x:0, y:-1, z:1},
	su: {x:0, y:1, z:-1},
	sd: {x:0, y:1, z:1},
	eu: {x:1, y:0, z:-1},
	ed: {x:1, y:0, z:1},
	wu: {x:-1, y:0, z:-1},
	wd: {x:-1, y:0, z:1}
};

window.MapEnvs = {
	1: { r:211, g:211, b:211, a:255 },
	2: { r:0, g:0, b:0, a:255 },
	3: { r:255, g:0, b:0, a:255 },
	4: { r:128, g:0, b:0, a:255 },
	5: { r:0, g:255, b:0, a:255 },
	6: { r:0, g:128, b:0, a:255 },
	7: { r:255, g:255, b:0, a:255 },
	8: { r:128, g:128, b:0, a:255 },
	9: { r:0, g:0, b:255, a:255 },
	10: { r:0, g:0, b:128, a:255 },
	11: { r:255, g:0, b:255, a:255 },
	12: { r:128, g:0, b:128, a:255 },
	13: { r:0, g:255, b:255, a:255 },
	14: { r:0, g:128, b:128, a:255 },
	15: { r:255, g:255, b:255, a:255 },
	16: { r:128, g:128, b:128, a:255 },
	257: { r:0, g:128, b:128, a:255 },
	258: { r:240, g:128, b:128, a:255 },
	259: { r:128, g:128, b:0, a:255 },
	260: { r:0, g:0, b:128, a:255 },
	261: { r:132, g:112, b:255, a:255 },
	262: { r:255, g:165, b:0, a:255 },
	263: { r:160, g:32, b:240, a:255 },
	264: { r:238, g:221, b:130, a:255 },
	265: { r:233, g:150, b:122, a:255 },
	266: { r:34, g:139, b:34, a:255 },
	267: { r:135, g:206, b:235, a:255 },
	268: { r:208, g:32, b:144, a:255 },
	269: { r:70, g:130, b:180, a:255 },
	270: { r:0, g:128, b:128, a:255 },
	271: { r:240, g:230, b:140, a:255 },
	272: { r:47, g:79, b:79, a:255 }
}

window.MapLineStyles = ['solid', 'dash', 'dot', 'dashdot'];

function GetDirCode(str)
{
	if(typeof str === 'string')
	{
		if(!str.trim())
			return '';
		str = str.toLowerCase().trim();
		if(str in window.DirCodeNames)
			return str;
		if(str in window.DirNameNums)
			return window.DirNumCodes[window.DirNameNums[str]];
	}
	else
	if(typeof str === 'number')
	{
		if((str > 0)&&(str < window.DirNumCodes.length))
			return window.DirNumCodes[str];
	}
	return '';
}

function MapEnv(r, g, b, alpha)
{
	this.r = r;
	this.g = g;
	this.b = b;
	this.alpha = alpha;
}

function MapArea(args)
{
	this.name = '';
	this.labels = {};
	this.gridMode = false;
	//this.userData = undefined;
	if(args)
	{
		for(var key in args)
			this[key] = args[key]
	}
}

function MapRoom(args)
{
	this.name = '';
	this.areaId = '';
	this.exits = [];
	this.x = 0;
	this.y = 0;
	this.z = 0;
	this.char = '';
	this.color = null;
	this.envId = '';
	this.hash = '';
	this.weight = 1;
	this.blocked = false;
	this.customLines = {};
	// this.userData = undefined;
	if(args)
	{
		for(var key in args)
			this[key] = args[key]
	}
}

function MapExit(args)
{
	this.roomId = '';
	this.dir = '';
	this.door = null; //0,1,2 == {"open", "closed", "locked"}
	this.weight = 0;
	this.blocked = false;
	//this.moveCommand: moveCommand
	if(args)
	{
		for(var key in args)
			this[key] = args[key]
	}
}

window.MapBlock = 
{
	areas: {},
	nextAreaId: 1,
	rooms: {},
	nextRoomId: 1,
	envs: {},
	nextEnvId: 1,
	charDefault: ' ',
	colorDefault: [0,0,0],
	roomIdHash: {},
	userData: undefined
};

function Mapper(sipwin)
{
	var SpacingRatio = 0.4;
	var self = this;
	if(sipwin.mapper)
		sipwin.mapper.closeMapWidget();
	sipwin.mapper = self;

	this.centerView = null;
	this.deleteMap = function() {
		for(var k in window.MapBlock)
		{
			var value = window.MapBlock[k];
			if(value !== undefined)
				this[k] = JSON.parse(JSON.stringify(value));
		}
		this.customMenus = {};
		this.customEvents = {};
	};
	this.deleteMap();
	this.currentAreaView = null;
	
	this.getExitDir = function(room, dir)
	{
		if(dir !== 'special')
			dir = GetDirCode(dir);
		if(!dir)
			return null;
		for(var i=0;i<room.exits.length;i++)
			if(room.exits[i].dir == dir)
				return room.exits[i];
		return null;
	};
	
	this.findAreaId = function(areaName) {
		if(areaName && (areaName in this.areas))
			return areaName;
		for(var k in this.areas)
			if(this.areas[k].name == areaName)
				return k;
		return null;
	}
	
	this.findRoomId = function(roomName) {
		if(roomName && (roomName in this.rooms))
			return roomName;
		for(var k in this.rooms)
			if(this.rooms[k].name == roomName)
				return k;
		return null;
	};
	
	this.confirmRoomId = function(roomId) {
		if(roomId in this.rooms)
			return roomId;
		return this.findRoomId(roomId);
	}
	
	this.addAreaName = function(areaName) {
		var area = this.findAreaId(areaName);
		if(area != null)
			return area;
		area = new MapArea({
			name: areaName
		});
		var id = this.createAreaId();
		this.areas[id] = area;
		return id;
	};
	this.addCustomLine = function(roomId, idOrTo, direction, style, color, arrow) {
		if (!(roomId in this.rooms)) {
			return false;
		}
		var room = this.rooms[roomId];
		var to = null;
		if (idOrTo in this.rooms)
			to = idOrTo;
		else 
		if (Array.isArray(idOrTo) 
		&& idOrTo.length === 3 
		&& idOrTo.every(n => typeof n === 'number'))
			to = [...idOrTo];
		else
			return false;
		direction = direction ? GetDirCode(direction) : 'custom';
		style = window.MapLineStyles.includes(style) ? style : 'solid';
		if(Array.isArray(color) 
		&& color.length === 3 
		&& color.every(n => n >= 0 && n <= 255))
			color = [...color];
		else
			color = [255, 255, 255];
		arrow = !!arrow;
		room.customLines[direction] = {
			to: to,
			style: style,
			color: color,
			arrow: arrow
		};
		this.updateMap();
		return true;
	};
	
	this.restricted_addMapEvent = function(uniquename, eventName, parent, displayName, arguments) {
		if (arguments 
		&& (!Array.isArray(arguments) 
			|| arguments.some(arg => typeof arg === 'function' || arg === null))) 
		{
			throw new Error('Invalid arguments: must be an array of non-function, non-null values');
		}
		return this.addMapEvent(uniquename, eventName, parent, displayName, arguments);
	};
	
	this.addMapEvent = function(uniquename, eventName, parent, displayName, arguments) {
		if (!uniquename || !eventName)
			return false;
		if (!(uniquename in this.customMenus)) {
			if (!displayName)
				return false;
			this.addMapMenu(uniquename, parent, displayName);
		}
		var safeArgs = Array.isArray(arguments)?arguments.filter(function(arg){
			if((arg == self.customEvents[uniquename]) || (arg == arguments))
				return false;
			return true;
		}):[];
		this.customMenus[uniquename].eventName = eventName;
		this.customMenus[uniquename].arguments = safeArgs;
		this.customEvents[uniquename] = {
			eventName: eventName,
			arguments: safeArgs
		};
		return true;
	};

	this.addMapMenu = function(uniquename, parent, displayName) {
		if (!uniquename || !displayName)
			return false;
		if (parent 
		&& !(parent in this.customMenus) 
		&& parent !== 'room' 
		&& parent !== 'label' 
		&& parent !== 'empty')
			return false;
		this.customMenus[uniquename] = {
			parent: parent || '',
			displayName: displayName,
			eventName: null,
			arguments: null
		};
		return true;
	};

	this.addRoom = function(roomId, areaId) {
		if((roomId in this.rooms) || !(areaId in this.areas))
			return null;
		var room = new MapRoom({
			areaId: Number(areaId)
		});
		this.rooms[roomId] = room;
		return roomId;
	};
	this.addSpecialExit = function(roomIdFrom, roomIdTo, moveCommand) {
		if(!(roomIdFrom in this.rooms) || !(roomIdTo in this.rooms))
			return false;
		var froom = this.rooms[roomIdFrom];
		for(var i=0;i<froom.exits.length;i++)
		{
			var ex = froom.exits[i];
			if((ex.roomId == roomIdTo) && (ex.dir == 'special'))
			{
				ex.moveCommand = moveCommand;
				return true;
			}
		}
		var exit = new MapExit({
			roomId: roomIdTo,
			dir: 'special',
			moveCommand: moveCommand
		});
		froom.exits.push(exit);
		return true;
	};
	
	this.auditAreas = function() 
	{
		var report = { issues: [], areas: {}, orphanRooms: [] };
		for (var areaId in this.areas) 
		{
			var area = this.areas[areaId];
			if (!area.name || area.name.trim() === "") {
				report.issues.push("Area "+areaId+" has no name");
			}
			var rooms = this.getAreaRooms(areaId);
			if (!rooms || Object.keys(rooms).length === 0) {
				report.issues.push("Area "+areaId+" ("+area.name+") has no rooms");
			}
			report.areas[areaId] = { name: area.name, roomCount: Object.keys(rooms).length };
		}
		var nameCounts = {};
		for (var areaId in this.areas) 
		{
			var name = this.areas[areaId].name;
			nameCounts[name] = (nameCounts[name] || 0) + 1;
			if (nameCounts[name] > 1)
				report.issues.push("Duplicate area name "+name+" in area "+areaId);
		}
		for (var roomId in this.rooms) 
		{
			var room = this.rooms[roomId];
			if (!room.areaId || !(room.areaId in this.areas)) 
			{
				report.orphanRooms.push({ roomId: roomId, areaId: room.areaId || "none" });
				report.issues.push("Room "+roomId+" has invalid or missing areaId: "+room.areaId);
			}
		}
		
		return report;
	};

	this.autoLayout = function(areaId, startRoomId) {
		if (!(areaId in this.areas)) 
			return false;
		var rooms = this.getAreaRooms1(areaId);
		if (!rooms.length) 
			return false;
		var startRoom = startRoomId && rooms.includes(startRoomId) ? this.rooms[startRoomId] : this.rooms[rooms[0]];
		startRoom.x = startRoom.x || 0;
		startRoom.y = startRoom.y || 0;
		startRoom.z = startRoom.z || 0;
		var queue = [startRoomId || rooms[0]];
		var visited = new Set();
		visited.add(queue[0]);
		while (queue.length) 
		{
			var roomId = queue.shift();
			var room = this.rooms[roomId];
			var exits = this.getRoomExits(roomId);
	
			for (var dir in exits) 
			{
				var toId = exits[dir];
				if (toId in this.rooms 
				&& !visited.has(toId) 
				&& this.rooms[toId].areaId == areaId) 
				{
					var toRoom = this.rooms[toId];
					var delta = window.DirCodeDeltas[dir];
					toRoom.x = room.x + delta.x;
					toRoom.y = room.y + delta.y;
					toRoom.z = room.z + delta.z;
					visited.add(toId);
					queue.push(toId);
				}
			}
		}
		return true;
	}

	this.viewArea = function(areaId)
	{
		this.currentAreaView = areaId;
		var playerAreaId = this.rooms[this.centerView] ? this.rooms[this.centerView].areaId : null;
		if(areaId == playerAreaId) 
			this.currentAreaView = null;
		if(this.mapWidget && this.mapWidget.titleBar) 
			this.mapWidget.titleBar.textContent = this.areas[areaId].name;
		if(this.mapWidget && this.mapWidget.layout) 
		{
			delete this.mapWidget.layout.offsetX;
			delete this.mapWidget.layout.offsetY;
		}
	}

	this.centerview = function(roomId) 
	{
		if(roomId == null) 
		{
			this.centerView = null;
			if(this.mapWidget && this.mapWidget.titleBar)
				this.mapWidget.titleBar.textContent = 'Map';
		} 
		else 
		if(roomId in this.rooms) 
		{
			var room = this.rooms[roomId];
			if(this.mapWidget 
			&& this.mapWidget.titleBar 
			&& (room.areaId in this.areas))
				this.mapWidget.titleBar.textContent = this.areas[room.areaId].name;
			this.centerView = roomId;
			this.currentAreaView = null;
		}
	};

	this.getViewRoomId = function()
	{
		var currentRoomId = this.centerView;
		if(!currentRoomId || !(currentRoomId in this.rooms)) 
			return null;
		if(this.currentAreaView === null)
			return currentRoomId;
		var currentRoom = this.rooms[currentRoomId];
		var viewAreaId = this.currentAreaView || currentRoom.areaId;
		var viewRoomId = this.getAreaCenterRoom(viewAreaId);
		if(!viewRoomId) 
			viewRoomId = currentRoomId;
		return viewRoomId;
	};

	this.clearAreaUserData = function(areaId) 
	{
		if(areaId in this.areas && this.areas[areaId].userData) 
		{
			delete this.areas[areaId].userData;
			return true;
		}
		return false;
	};

	this.clearAreaUserDataItem = function(areaId, key) 
	{
		if(key && areaId in this.areas && this.areas[areaId].userData) 
		{
			var area = this.areas[areaId];
			if(key in area.userData) 
			{
				delete area.userData[key];
				return true;
			}
		}
		return false;
	};
	this.clearMapSelection = function() 
	{
		if (this.mapWidget) 
		{
			if(this.mapWidget.selectedRooms && this.mapWidget.selectedRooms.length)
			{
				this.mapWidget.selectedRooms = [];
				this.updateMap();
			}
			return true;
		}
		return false;
	};
	this.clearMapUserData = function() 
	{
		if(this.userData) 
		{
			delete this.userData;
			return true;
		}
		return false;
	};
	this.clearMapUserDataItem = function(key) 
	{
		if(this.userData && key) 
		{
			if(key in this.userData) 
			{
				delete this.userData[key];
				return true;
			}
		}
		return false;
	};
	this.clearRoomUserData = function(roomId) 
	{
		if(roomId in this.rooms && this.rooms[roomId].userData) 
		{
			delete this.rooms[roomId].userData;
			return true;
		}
		return false;
	};
	this.clearRoomUserDataItem = function(roomId, key) 
	{
		if(key && roomId in this.rooms && this.rooms[roomId].userData) 
		{
			var room = this.rooms[roomId];
			if(key in room.userData) {
				delete room.userData[key];
				return true;
			}
		}
		return false;
	};
	this.clearSpecialExits = function(roomId) 
	{
		if(roomId in this.rooms && this.rooms[roomId].exits) 
		{
			var room = this.rooms[roomId];
			var exit = this.getExitDir(room,'special');
			while(exit != null)
			{
				var x = room.exits.indexOf(exit);
				if(x<0) break;
				room.exits.splice(x,1);
				exit = this.getExitDir(room,'special');
			}
		}
	};
	this.closeMapWidget = function() 
	{
		if (!this.mapWidget || !this.mapWidget.canvas)
			return false;
		if (this.mapWidget.mouseUpHandler)
			document.removeEventListener('mouseup', this.mapWidget.mouseUpHandler);
		if (this.mapWidget.tooltip && this.mapWidget.tooltip.parentNode)
			this.mapWidget.tooltip.parentNode.removeChild(this.mapWidget.tooltip);
		if(this.mapWidget.canvas && this.mapWidget.canvas.parentNode)
		{
			if(this.mapWidget.frame !== null)
				this.mapWidget.canvas.outerHTML = '';
			else
			if(this.mapWidget.canvas.parentNode.parentNode)
				this.mapWidget.canvas.parentNode.outerHTML = '';
		}
		this.deleteMap();
		if(this.mapWidget)
		{
			delete this.mapWidget.draggedRooms;
			delete this.mapWidget.dragOriginalPositions;
		}
		this.mapWidget = null;
		this.canvas = null;
		this.centerView = null; // Reset view to avoid referencing closed widget
		return true;
	};

	this.connectExitStub = function(fromId, toId, direction) 
	{
		if (!(fromId in this.rooms) || !this.rooms[fromId].exits)
			return false;
		var fromR = this.rooms[fromId];
		if (direction === undefined) 
		{
			if (toId in this.rooms) 
			{
				var toR = this.rooms[toId];
				var dx = toR.x - fromR.x;
				var dy = toR.y - fromR.y;
				var dz = toR.z - fromR.z;
				if (dx === 0 && dy === 0 && dz === 0)
					return false;
				var dir;
				for (var d in window.DirCodeDeltas) 
				{
					var delta = window.DirCodeDeltas[d];
					if (delta.x === dx && delta.y === dy && delta.z === dz) 
					{
						dir = d;
						break;
					}
				}
				if (!dir)
					return false;
				var existingExit = this.getExitDir(fromR,dir);
				if (existingExit && existingExit.roomId == toId)
					return true;
				if (existingExit)
					return false;
				var exit = new MapExit({
					roomId: toId,
					dir: GetDirCode(dir)
				});
				fromR.exits.push(exit);
				return true;
			} 
			else 
			{
				direction = GetDirCode(toId);
				if (!direction)
					return false;
				toId = null;
			}
		} 
		else 
		{
			direction = GetDirCode(direction);
			if (!direction)
				return false;
		}
	
		if (toId in this.rooms) 
		{
			var existingExit = this.getExitDir(fromR,direction);
			if (existingExit) 
			{
				if (existingExit.roomId == toId)
					return true;
				fromR.exits.splice(fromR.exits.indexOf(existingExit), 1);
			}
			var exit = new MapExit({
				roomId: toId,
				dir: direction
			});
			fromR.exits.push(exit);
			return true;
		} 
		else 
		if (direction && !toId) 
		{
			var oppositeDir = window.OpDirCodes[direction];
			if (!oppositeDir)
				return false;
			var delta = window.DirCodeDeltas[direction];
			if (!delta)
				return false; // Invalid direction
			var existingExit = this.getExitDir(fromR,direction);
			if (existingExit !== null)
				return false;
			var expectedX = fromR.x + delta.x;
			var expectedY = fromR.y + delta.y;
			var expectedZ = fromR.z + delta.z;
			for (var id in this.rooms) 
			{
				var room = this.rooms[id];
				if (room.x === expectedX && room.y === expectedY && room.z === expectedZ &&
					room.exits && this.getExitDir(room,oppositeDir) && this.getExitDir(room,oppositeDir).roomId == '') 
				{
					var exit = new MapExit({
						roomId: id,
						dir: direction
					});
					fromR.exits.push(exit);
					return true;
				}
			}
			var exit = new MapExit({
				roomId: '',
				dir: direction
			});
			fromR.exits.push(exit);
			return true;
		}
		return false;
	};

	this.createMapLabel = function(areaId, text, posX, posY, posZ, fgRed, fgGreen, fgBlue, 
									bgRed, bgGreen, bgBlue, zoom, fontSize, showOnTop, 
									noScaling, fontName, foregroundTransparency, 
									backgroundTransparency, temporary) 
	{
		if (!(areaId in this.areas) || !text) 
			return false;
		var area = this.areas[areaId];
		var labelId = 1;
		while(labelId in area.labels)
			labelId++;
		area.labels[labelId] = 
		{
			text: text,
			x: posX,
			y: posY,
			z: posZ || 0,
			fgColor: `rgb(${fgRed || 255}, ${fgGreen || 255}, ${fgBlue || 255})`,
			bgColor: `rgb(${bgRed || 0}, ${bgGreen || 0}, ${bgBlue || 0})`,
			fontSize: fontSize || 10,
			fontName: fontName || 'Arial',
			zoom: zoom || 1,
			showOnTop: !!showOnTop,
			noScaling: !!noScaling,
			foregroundTransparency: foregroundTransparency || 1,
			backgroundTransparency: backgroundTransparency || 1,
			temporary: !!temporary
		};
		this.updateMap();
		return labelId;
	};

	this.createMapImageLabel = function(areaId, filePath, posx, posy, posz, width, height, zoom, showOnTop, temporary) 
	{
		if (!(areaId in this.areas) || !filePath) 
			return false;
		var area = this.areas[areaId];
		var labelId = 1;
		while (labelId in area.labels)
			labelId++;
		area.labels[labelId] = 
		{
			type: 'image',
			filePath: filePath,
			x: posx || 0,
			y: posy || 0,
			z: posz || 0,
			width: width || 100,
			height: height || 100,
			zoom: zoom || 1,
			showOnTop: !!showOnTop,
			temporary: !!temporary
		};
		return labelId;
	};

	this.createMapper = function(x, y, width, height) 
	{
		if(this.mapWidget)
		{
			console.warn("Map widget already exists. Use resizeMapWidget or moveMapWidget.");
			return null;
		}
		var targetWidth = width || 200;
		var targetHeight = height || 150;
		if(typeof x === 'string' && y === undefined)
		{
			var canvas = document.createElement('canvas');
			var titleBar = null;
			if(!(x in sipwin.mxp.frames))
			{
				console.error("no frame: "+x);
				return null;
			}
			var frame = sipwin.mxp.frames[x].firstChild;
			frame.innerHTML = '';
			var calced = getComputedStyle(frame);
			width = parseFloat(calced.width) || targetWidth;
			height = parseFloat(calced.height) || targetHeight;
			canvas.style.position = "absolute";
			canvas.style.left = '0px';
			canvas.style.top = '0px';
			canvas.width = width;
			canvas.height = height;
			frame.appendChild(canvas);
		}
		else
		{
			var container = document.createElement('div');
			container.style.position = "absolute";
			container.style.left = (x || 0) + 'px';
			container.style.top = (y || 0) + 'px';
			container.style.width = targetWidth + 'px';
			container.style.height = targetHeight + 'px';
			container.style.border = "1px solid white";
			container.style.backgroundColor = 'black';
			titleBar = document.createElement('div');
			titleBar.style.height = "20px";
			titleBar.style.backgroundColor = "white";
			titleBar.style.color = "black";
			titleBar.style.fontSize = "14px";
			titleBar.style.padding = "2px 5px";
			titleBar.style.userSelect = "none";
			titleBar.textContent = "Map";
			/*
			var closeIcon = document.createElement('img');
			closeIcon.style.float = "right";
			closeIcon.style.width = "16px";
			closeIcon.style.height = "16px";
			closeIcon.src = "images/close.gif";
			var me = this;
			closeIcon.onclick = function() {
				me.closeMapWidget();
			};
			titleBar.appendChild(closeIcon);
			*/
			container.appendChild(titleBar);
			var canvas = document.createElement('canvas');
			container.appendChild(canvas);
			canvas.style.position = "absolute";
			canvas.style.border = "1px solid white";
			canvas.width = targetWidth;
			canvas.height = targetHeight;
			canvas.style.backgroundColor = 'black';
			sipwin.topWindow.appendChild(container);
			MakeDraggable(container, titleBar);
			var resizeDebouncer = null;
			container.addEventListener('resize', function() 
			{
				if(resizeDebouncer == null)
					resizeDebouncer =setTimeout(function() 
					{
						var calced = getComputedStyle(container);
						canvas.width = parseFloat(calced.width);
						canvas.height = parseFloat(calced.height);
						self.updateMap();
						resizeDebouncer = null;
					}, 500);
			});
		}
		
		var ctx = canvas.getContext('2d');
		if (!ctx) 
		{
			console.error("Failed to get 2D rendering context");
			return null;
		}
		var isDraggingRoom = false;
		var potentialDrag = false;
		var dragStartMouseX = 0;
		var dragStartMouseY = 0;
		var isPanning = false;
		var panStartX, panStartY, panOffsetX, panOffsetY;
		var mouseDownHandler = function(event) 
		{
			if (!self.mapWidget.layout || !self.mapWidget.layout.rooms)
				return;
			if (self.mapWidget.tooltip)
				self.mapWidget.tooltip.style.display = 'none';
			var rect = canvas.getBoundingClientRect();
			var mouseX = event.clientX - rect.left;
			var mouseY = event.clientY - rect.top;
			if (event.button !== 0) return;
			var { rooms } = self.mapWidget.layout;
			var hoverRoomId = null;
			for (var roomId in rooms) 
			{
				var { centerX, centerY, radius } = rooms[roomId];
				var dx = mouseX - centerX;
				var dy = mouseY - centerY;
				var dist = Math.sqrt(dx * dx + dy * dy);
				if (dist < radius) 
				{
					hoverRoomId = roomId;
					break;
				}
			}
			if (hoverRoomId) 
			{
				potentialDrag = true;
				dragStartMouseX = mouseX;
				dragStartMouseY = mouseY;
				// Determine rooms to potentially drag: selected group if hovered is selected, else just hovered
				self.mapWidget.selectedRooms = self.mapWidget.selectedRooms || [];
				self.mapWidget.draggedRooms = self.mapWidget.selectedRooms.includes(hoverRoomId) 
					? [...self.mapWidget.selectedRooms] 
					: [hoverRoomId];
				// Store original positions
				self.mapWidget.dragOriginalPositions = {};
				self.mapWidget.draggedRooms.forEach(id => {
					var room = self.rooms[id];
					self.mapWidget.dragOriginalPositions[id] = { x: room.x, y: room.y };
				});
			} 
			else 
			{
				isPanning = true;
				panStartX = event.clientX;
				panStartY = event.clientY;
				var viewRoomId = self.getViewRoomId() || self.centerView;
				var viewRoom = self.rooms[viewRoomId];
				panOffsetX = self.mapWidget.layout.offsetX || (canvas.width / 2 - viewRoom.x * self.mapWidget.layout.tileSize * SpacingRatio);
				panOffsetY = self.mapWidget.layout.offsetY || (canvas.height / 2 - viewRoom.y * self.mapWidget.layout.tileSize * SpacingRatio);
				canvas.style.cursor = "grabbing";
			}
		};
		var mouseMoveHandler = function(event) 
		{
			if (!self.mapWidget.layout || !self.mapWidget.layout.rooms) 
			{
				canvas.style.cursor = "default";
				if (self.mapWidget.tooltip)
					self.mapWidget.tooltip.style.display = 'none';
				return;
			}
			var rect = canvas.getBoundingClientRect();
			var mouseX = event.clientX - rect.left;
			var mouseY = event.clientY - rect.top;
			var { rooms } = self.mapWidget.layout;
			var hoverRoomId = null;
			for (var roomId in rooms) 
			{
				var { centerX, centerY, radius } = rooms[roomId];
				var dx = mouseX - centerX;
				var dy = mouseY - centerY;
				var dist = Math.sqrt(dx * dx + dy * dy);
				if (dist < radius) 
				{
					hoverRoomId = roomId;
					break;
				}
			}
			if (potentialDrag || isDraggingRoom) 
			{
				var dx = mouseX - dragStartMouseX;
				var dy = mouseY - dragStartMouseY;
				var dist = Math.sqrt(dx * dx + dy * dy);
				if (potentialDrag && dist > 5) 
				{
					potentialDrag = false;
					isDraggingRoom = true;
					if (self.mapWidget.tooltip)
						self.mapWidget.tooltip.style.display = 'none';
				}
				if(isDraggingRoom) 
				{
					var tileSize = self.mapWidget.layout.tileSize;
					var gridDx = Math.round(dx / (tileSize * SpacingRatio));
					var gridDy = Math.round(dy / (tileSize * SpacingRatio));
					self.mapWidget.draggedRooms.forEach(id => {
						var orig = self.mapWidget.dragOriginalPositions[id];
						self.setRoomCoordinates(id, orig.x + gridDx, orig.y + gridDy, self.rooms[id].z);
					});
					self.updateMap();
					canvas.style.cursor = "move";
					return; // Prevent panning during room drag
				}
			} 
			else 
			if (isPanning) 
			{
				var dx = event.clientX - panStartX;
				var dy = event.clientY - panStartY;
				self.mapWidget.layout.offsetX = panOffsetX + dx;
				self.mapWidget.layout.offsetY = panOffsetY + dy;
				self.updateMap();
				canvas.style.cursor = "grabbing";
			}
			if (!self.mapWidget.tooltip) 
			{
				var div = document.createElement('div');
				div.style.position = "absolute";
				div.style.backgroundColor = "rgba(0, 0, 0, 0.8)";
				div.style.color = "white";
				div.style.padding = "5px";
				div.style.borderRadius = "3px";
				div.style.fontSize = "12px";
				div.style.pointerEvents = "none";
				div.style.zIndex = "1000";
				sipwin.topWindow.appendChild(div);
				self.mapWidget.tooltip = div;
			}
			if (hoverRoomId != null) 
			{
				var roomName = self.getRoomName(hoverRoomId) || "Unnamed";
				self.mapWidget.tooltip.textContent = 'ID: ' + hoverRoomId + '\nName: ' + roomName;
				self.mapWidget.tooltip.style.left = (event.clientX + 10) + 'px';
				self.mapWidget.tooltip.style.top = (event.clientY + 10) + 'px';
				self.mapWidget.tooltip.style.display = "block";
				canvas.style.cursor = "move"; // Indicate draggable
			} 
			else 
			{
				self.mapWidget.tooltip.style.display = 'none';
				canvas.style.cursor = "grab";
			}
		};
		var mouseUpHandler = function(event) 
		{
			if (event.button !== 0) return;
			if (isPanning) {
				isPanning = false;
				canvas.style.cursor = "grab";
			}
			if (isDraggingRoom) {
				isDraggingRoom = false;
				canvas.style.cursor = "grab";
				// Optional: Add any post-drag logic, e.g., check for overlaps
			} else if (potentialDrag) {
				potentialDrag = false;
				if (self.mapWidget.tooltip)
					self.mapWidget.tooltip.style.display = 'none';
				var rect = canvas.getBoundingClientRect();
				var clickX = event.clientX - rect.left;
				var clickY = event.clientY - rect.top;
				var { rooms } = self.mapWidget.layout;
				var closestRoomId = null;
				var minDist = Infinity;
				for (var roomId in rooms) {
					var { centerX, centerY, radius } = rooms[roomId];
					var dx = clickX - centerX;
					var dy = clickY - centerY;
					var dist = Math.sqrt(dx * dx + dy * dy);
					if (dist < radius && dist < minDist) {
						minDist = dist;
						closestRoomId = roomId;
					}
				}
				if (closestRoomId) {
					if (event.ctrlKey) {
						self.mapWidget.selectedRooms = self.mapWidget.selectedRooms || [];
						var index = self.mapWidget.selectedRooms.indexOf(closestRoomId);
						if (index >= 0)
							self.mapWidget.selectedRooms.splice(index, 1);
						else
							self.mapWidget.selectedRooms.push(closestRoomId);
					} else if (event.shiftKey) {
						if (self.mapWidget.selectedRooms && self.mapWidget.selectedRooms.length) {
							var lastRoom = self.rooms[self.mapWidget.selectedRooms[self.mapWidget.selectedRooms.length - 1]];
							var currRoom = self.rooms[closestRoomId];
							var minX = Math.min(lastRoom.x, currRoom.x);
							var maxX = Math.max(lastRoom.x, currRoom.x);
							var minY = Math.min(lastRoom.y, currRoom.y);
							var maxY = Math.max(lastRoom.y, currRoom.y);
							var zLevel = currRoom.z;
							self.mapWidget.selectedRooms = [];
							for (var roomId in self.rooms) {
								var room = self.rooms[roomId];
								if (room.areaId == currRoom.areaId && room.z == zLevel &&
										room.x >= minX && room.x <= maxX && room.y >= minY && room.y <= maxY &&
										(self.mapWidget.selectedRooms.indexOf(roomId) < 0))
									self.mapWidget.selectedRooms.push(roomId);
							}
						} else {
							self.mapWidget.selectedRooms = self.mapWidget.selectedRooms || [];
							var index = self.mapWidget.selectedRooms.indexOf(closestRoomId);
							if (index >= 0)
								self.mapWidget.selectedRooms.splice(index, 1);
							else
								self.mapWidget.selectedRooms.push(closestRoomId);
						}
					} else {
						self.centerview(closestRoomId);
						if (self.mapWidget.selectedRooms && (self.mapWidget.selectedRooms.length > 0))
							self.clearMapSelection();
					}
					event.stopPropagation();
					self.updateMap();
				} else {
					if (self.mapWidget.selectedRooms && self.mapWidget.selectedRooms.length > 0)
						self.clearMapSelection();
				}
			}
		};
		var menuHandler = function(e) {
			if(self.mapWidget.tooltip)
				self.mapWidget.tooltip.style.display = 'none';
			var rect = canvas.getBoundingClientRect();
			var clickX = e.clientX - rect.left;
			var clickY = e.clientY - rect.top;
			var rooms = {}, tileSize = 1, offsetX = 1, offsetY =1 ;
			if(self.mapWidget.layout && self.mapWidget.layout.rooms)
			{
				rooms = self.mapWidget.layout.rooms;
				tileSize = self.mapWidget.layout.tileSize;
				offsetX = self.mapWidget.layout.offsetX;
				offsetY = self.mapWidget.layout.offsetY;
			}
			var closestRoomId = null;
			var closestLabelId = null;
			var minDist = Infinity;
			var gridX = Math.round((clickX - offsetX) / (tileSize * SpacingRatio));
			var gridY = Math.round((clickY - offsetY) / (tileSize * SpacingRatio));
			var viewRoomId = self.getViewRoomId();
			var zLevel = self.rooms[viewRoomId] ? self.rooms[viewRoomId].z : 0;
			var areaId = self.rooms[viewRoomId] ? self.rooms[viewRoomId].areaId : Object.keys(self.areas)[0];
			for (var roomId in rooms) {
				var { centerX, centerY, radius } = rooms[roomId];
				var dx = clickX - centerX;
				var dy = clickY - centerY;
				var dist = Math.sqrt(dx * dx + dy * dy);
				if (dist < radius && dist < minDist) {
					minDist = dist;
					closestRoomId = roomId;
					if(self.mapWidget.selectedRooms 
					&& self.mapWidget.selectedRooms.length
					&& (self.mapWidget.selectedRooms.indexOf(roomId)<0))
						self.clearMapSelection();
				}
			}
			if (!closestRoomId && areaId in self.areas && self.areas[areaId].labels) {
				var labels = self.areas[areaId].labels;
				for (var labelId in labels) {
					var label = labels[labelId];
					if (label.z === zLevel) {
						var x = offsetX + label.x * tileSize * SpacingRatio;
						var y = offsetY + label.y * tileSize * SpacingRatio;
						var fontSize = Math.max(5, (label.fontSize || 10) * (self.zoom || 1));
						var width,height;
						if (label.type === 'image') {
							width = (label.width || 100) * (label.zoom || 1) * (self.zoom || 1);
							height = (label.height || 100) * (label.zoom || 1) * (self.zoom || 1);
						} else {
							var fontSize = Math.max(5, (label.fontSize || 10) * (self.zoom || 1));
							width = (label.text ? label.text.length : 0) * fontSize / 2;
							height = fontSize;
						}
						if (clickX >= x - width / 2 && clickX <= x + width / 2 &&
							clickY >= y - height / 2 && clickY <= y + height / 2) {
							closestLabelId = labelId;
							break;
						}
					}
				}
			}
			
			var isValidAddRoom = false;
			if (!closestRoomId && !closestLabelId && areaId) {
				var isOccupied = Object.keys(self.rooms).some(function(id) {
					var room = self.rooms[id];
					return room.areaId == areaId && room.z === zLevel && room.x === gridX && room.y === gridY;
				});
				isValidAddRoom = !isOccupied;
			}

			var buildCustomMenu = function(context, parent, visited = new Set()) 
			{
				var menuItems = [];
				for (var uniquename in self.customMenus)
				{
					if(visited.has(uniquename))
						continue;
					var menu = self.customMenus[uniquename];
					if ((context === 'room' && (menu.parent === 'room' || (!menu.parent && !parent))) ||
						(context === 'label' && (menu.parent === 'label' || (!menu.parent && !parent))) ||
						(context === 'empty' && (menu.parent === 'empty' || (!menu.parent && !parent))) ||
						(parent && menu.parent === parent)) 
					{
						visited.add(uniquename);
						var item = {
							n: menu.displayName,
							e: menu.eventName ? 'true' : '',
							a: ''
						};
						if (menu.eventName && uniquename in self.customEvents) 
						{
							var event = self.customEvents[uniquename];
							var args = event.arguments.map(arg => {
								if (typeof arg === 'string') {
									return "'" + arg.replace(/'/g, "\\'") + "'";
								}
								return JSON.stringify(arg);
							}).join(',');
							if (typeof window[event.eventName] === 'function' || 
								(sipwin[event.eventName] && typeof sipwin[event.eventName] === 'function')) {
								item.a = 'javascript:try {' 
									+'	window.currWin.'+event.eventName+'('+args+');'
								+'} catch(e) { console.error("Event error: " + e); };'
							} else {
								var cmd = event.eventName;
								if (args) cmd += ' ' + args.replace(/'/g, '');
								item.a = 'javascript:window.currWin.submitInput("'+cmd.replace(/'/g, "\\'")+'");';
							}
						}
						var subMenus = buildCustomMenu(context, uniquename, visited);
						if (subMenus.length > 0) 
						{
							item.c = false;
							item.a = "javascript:"
								+"var c = getComputedStyle(this.parentNode.parentNode);"
								+"var x = parseFloat(c.left) + parseFloat(c.width);"
								+"var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;"
								+"DropDownMenu(null, x, y, 'auto', 12, "+JSON.stringify(subMenus)+"}, true);";
						}
						menuItems.push(item);
					}
				}
				return menuItems;
			};
			
			if (!self.mapWidget.layout || !self.rooms || Object.keys(self.rooms).length === 0) 
			{
				var menus = [
					{
						"n": "Create Area",
						"a": function(event) {
								 SiPrompt('Enter area name:',function(name){
								if(name) {
									var areaId = self.addAreaName(name);
									self.updateMap();
								}
							})}
					},
					{
						"n": "Add Room",
						"a": function(event) {
								SiPrompt('Enter area ID or name:',function(areaInput){
							 		var areaId = self.addAreaName(areaInput);
							 	if(areaId) {
							 		var id = self.addRoom(self.createRoomId(), areaId);
							 		if(id) {
							 			self.setRoomCoordinates(id, 0, 0, 0);
							 			self.centerview(id);
							 			self.updateMap();
							 		}
							 	}
							 })}
					}
				];
				menus = menus.concat(buildCustomMenu('empty'));
				DropDownMenu(e, e.clientX, e.clientY, 200, 12, menus);
			}
			else
			if (closestRoomId) 
			{
				if((typeof closestRoomId == 'string')
				&&(closestRoomId.indexOf('\'')>=0))
					closestRoomId = closestRoomId.replace('\'','\\\''); 
				var menu = [
					{"n":"Set as Current Room",
					 "a":function(event) { 
					 	self.clearMapSelection();
					 	self.centerview(closestRoomId);
					 	self.updateMap();},
					 "e":""},
					{"n":"Speedwalk to Room",
					 "a": function(event) {
							self.clearMapSelection();
							var dirString = self.getPath(self.getPlayerRoom(), closestRoomId);
							if (dirString) self.speedwalk(dirString, false, 0, true);},
					 "e":""},
					{"n":"Add Exit",
					 "c": false,
					 "a":function(event) {
					 	var subMenu = [];
					 	self.clearMapSelection();
					 	for(var dir in window.DirNameNums) {
					 	  var exits = self.getRoomExits(closestRoomId);
					 	  var dirCode = window.DirNumCodes[window.DirNameNums[dir]];
					 	  if(!exits[dirCode])
					 		subMenu.push({n:dir,e:'',a:(function(dirCode){return function(event) {
					 			SiPrompt('Enter Target Room Id',function(i){
					 			  if(i && window.currWin.mapper.roomExists(i)) {
					 				 window.currWin.mapper.setExit(closestRoomId,i,dirCode);
					 				 window.currWin.mapper.updateMap();
					 			  }
					 		})}})(dirCode)
					 		});
					 	}
					 	subMenu.push({n:'special',e:'',a:(function(dirCode){return function(event) {
					 			SiPrompt('Enter Target Room Id',function(id){
					 			  if(id && window.currWin.mapper.roomExists(id)) {
									SiPrompt('Enter Move Command',function(cmd){
					 					if(cmd) {
					 					  window.currWin.mapper.addSpecialExit(closestRoomId,id,cmd);
					 					  window.currWin.mapper.updateMap();
					 					}});}});
					 		}})(dirCode)
					 	});
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);
					 	},
					 "e":""},
					{"n":"Remove Exit",
					 "a":function(event) {
					 	self.clearMapSelection();
					 	var subMenu = [];
					 	var exits = self.getRoomExits(closestRoomId);
					 	for(var k in exits) {
					 	  var dirName = window.DirCodeNames[k];
					 	  subMenu.push({n:dirName,a:(function(dirCode){return function(event) {
							   self.setExit(closestRoomId,-1,dirCode); 
							   window.currWin.mapper.updateMap();}})(k)});
					 	}
					 	var sexits = self.getSpecialExitsSwap(closestRoomId);
					 	for(var k in sexits)
					 	  subMenu.push({n:'special: '+k,a:(function(dirCode){return function(event) {
							   self.removeSpecialExit(closestRoomId,dirCode);}})(k)});
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);},
					 "e":"window.currWin.mapper.rooms['"+closestRoomId+"'].exits.length"},
					{"n":"Lock/Unlock Exit",
					 "a":function(event) {
					 	self.clearMapSelection();
					 	var subMenu = [];
					 	var exits = self.getRoomExits(closestRoomId);
					 	for(var k in exits) {
					 	  var dirName = window.DirCodeNames[k];
					 	  subMenu.push({n:((self.hasExitLock(closestRoomId,k)?'Unlock ':'Lock ')+dirName),
					 					a:(function(dirCode){return function(event) {
											 self.lockExit(closestRoomId,dirCode,!self.hasExitLock(closestRoomId,dirCode));
											 self.updateMap();}})(k)});
					 	}
					 	var sexits = self.getSpecialExitsSwap(closestRoomId);
					 	for(var k in sexits)
					 	  subMenu.push({n:'special: '+k,a:(function(dirCode){return function(event) {
							   self.lockSpecialExit(closestRoomId,sexits[dirCode],dirCode,!self.hasExitLock(closestRoomId,dirCode));}})(k)});
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);},
					 "e":"window.currWin.mapper.rooms['"+closestRoomId+"'].exits.length"},
					{"n":"Set Door Status",
					 "a":function(event) {
					 	self.clearMapSelection();
					 	var subMenu = [];
					 	var exits = self.getRoomExits(closestRoomId);
					 	var doors = self.getDoors(closestRoomId);
					 	var doorMap = doors.reduce(function(obj, item){ 
							 obj[item.direction] = item.door; 
							 return obj;
						}, {});
					 	for(var k in exits) {
					 	  var dirName = window.DirCodeNames[k];
					 	  var doorStatus = doorMap[k];
					 	  if(!(k in doorMap))
							subMenu.push({n:'Add '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,0); 
												window.currWin.mapper.updateMap();}})(k)});
						  else if (doorStatus == 0) {
							subMenu.push({n:'Remove '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,-1); 
												window.currWin.mapper.updateMap();}})(k)});
							subMenu.push({n:'Close '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,1); 
												window.currWin.mapper.updateMap();}})(k)});
						  } else if (doorStatus == 1) {
							subMenu.push({n:'Remove '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,-1); 
												window.currWin.mapper.updateMap();}})(k)});
							subMenu.push({n:'Open '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,0); 
												window.currWin.mapper.updateMap();}})(k)});
							subMenu.push({n:'Lock '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,2); 
												window.currWin.mapper.updateMap();}})(k)});
						  } else if (doorStatus == 2) {
							subMenu.push({n:'Remove '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,-1); 
												window.currWin.mapper.updateMap();}})(k)});
							subMenu.push({n:'Unlock '+dirName+' door',
											a:(function(dirCode){return function(event) {
												self.setDoor(closestRoomId,dirCode,1); 
												window.currWin.mapper.updateMap();}})(k)});
						  }
					 	}
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);},
					 "e":"window.currWin.mapper.rooms['"+closestRoomId+"'].exits.length"
					},
					{"n":"Set Exit Weight",
					 "a":function(event) {
					 	self.clearMapSelection();
					 	var subMenu = [];
					 	var exits = self.getRoomExits(closestRoomId);
					 	for(var k in exits) {
					 	  var dirName = window.DirCodeNames[k];
							subMenu.push({n:dirName,
											a:(function(dirCode){return function(event) {
					 						SiPrompt('Enter Exit Weight',function(i){
					 							if(!isNumber(i)) return;
												self.setExitWeight(closestRoomId,dirCode,Number(i)); 
												self.updateMap();
											});
										}})(k)});
					 	}
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);},
					 "e":"window.currWin.mapper.rooms['"+closestRoomId+"'].exits.length"
					},
					{"n":"Set Room ...",
					 "a":function(event) {
			 			var sel=self.getMapSelection();
			 			var rid=sel.length>0?sel:[closestRoomId];
			 			var msg=(rid.length>1?(rid.length+' rooms'):('room '+rid[0]));;
					 	var subMenu = [];
							subMenu.push({n:'Name',
								a:function(event) {
									SiPrompt('Enter name for '+msg+':',function(i) { 
										self.setRoomName(rid,i);
										self.updateMap();
								  })}
								});
						  subMenu.push({n:'Weight',
					 			a:function(event) {
					 				SiPrompt('Enter weight for '+msg+':',function(i){
					 				if (isNumber(i)) { 
					 					self.setRoomWeight(rid,Number(i));
					 					self.updateMap();
					 				}
					 			  });
					 			}});
					 	  subMenu.push({n:'Area',
					 			a:function(event) {
					 				SiPrompt('Enter area id/name for '+msg+':',function(i){
					 				if (i) {
					 					self.setRoomArea(rid,i);
					 					self.updateMap();
					 				}
					 			  });
					 			}});
					 	  subMenu.push({n:'Coordinates',
					 			a:function(event) {
									var coords = self.getRoomCoordinates(rid).join(',');
					 				SiPrompt('New coordinates ('+coords+') for '+msg+':',function(i){
					 				var regex = /^-?\\d+,-?\\d+,-?\\d+$/; 
					 	  			if (regex.test(i)) { 
					 					var xyz = i.split(',').map(Number);
					 					self.setRoomCoordinates(rid,xyz[0],xyz[1],xyz[2]);
					 					self.updateMap();
					 	  			}
					 			 });
								}});
					 	  subMenu.push({n:'Char',
					 			a:function(event) {
					 				SiPrompt('Enter character for '+msg+':',function(i){
					 				if(!i) return;
					 				SiColorPicker('Enter char color for '+msg+':',function(c){ 
					 					self.setRoomChar(rid,i.charAt(0));
					 					self.setRoomCharColor(rid,c);
					 		  			self.updateMap();
					 	 			});
					 			 });
								}});
					 	  subMenu.push({n:self.roomLocked(closestRoomId)?'Unlock':'Lock',
								a:function(event) {
									if(self.roomLocked(closestRoomId))
										self.lockRoom(rid,false);
									  else
										self.lockRoom(rid,true);
								}});
					 	  subMenu.push({n:'Highlight',
					 			a:function(event) {
					 				var room = self.rooms[closestRoomId];
					 				if(room.highlight) {
					 					self.unHighlightRoom(rid);
					 					self.updateMap();
					 				} else 
					 					SiColorPicker('Select first color:',function(color1) {
					 						SiColorPicker('Select second color:',function(color2) {
					 							self.highlightRoom(rid, color1[0], color1[1], color1[2], color2[0], color2[1], color2[2], null, color1[3], color2[3]);
					 							self.updateMap();
					 	  					},true);
					 					},true);
								}});
					 	  subMenu.push({n:'Color',
								a:function(event) {
									var colors = Object.values(window.MapEnvs).map(env => [env.r, env.g, env.b])
										.concat(Object.values(window.MapBlock.envs).map(env => [env.r, env.g, env.b]));
									SiSwatchPicker('Select color of '+msg+':',colors,function(color){
										var envId = null;
										for (var id in window.MapEnvs) {
											var env = window.MapEnvs[id];
											if (env.r === color[0] && env.g === color[1] && env.b === color[2]) {
												envId = id;
												break;
											}
										}
										if (!envId) {
											for (var id in window.MapBlock.envs) {
												var env = window.MapBlock.envs[id];
												if (env.r === color[0] && env.g === color[1] && env.b === color[2]) {
													envId = id;
													break;
												}
											}
										}
					 					if (envId) {
					 	  					self.setRoomEnv(rid,envId);
					 						self.updateMap(); 
					 					};
					 				});
								}});
					 	  subMenu.push({n:'User Data',
								a:function(event) {
					 				var all = self.getAllRoomUserData(rid);
					 				var ud = ''; 
					 				for(var k in all)
					 					ud += '<font color=yellow>'
					 						+escapeHTML(k+'='+all[k])
					 						+'</font><br>';
					 				SiPrompt(ud+'<p>Enter variable to add/edit/remove like var=value:',
					 					function(i){
							 	  			var x = i.indexOf('=');
							 	  			if(x>=0) { 
						 						var v = i.substr(0,x).trim(); var l = i.substr(x+1).trim();
						 						if(!l) l=null;
						 						if(v) 
						 							self.setRoomUserData(rid,v,l);
							 	  			}
						 				}
						 			);
								}});
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);
					 	},
					 "e":""},
					{"n":"Delete Room",
					 "a":function(event) {
					 	var sel=self.getMapSelection();
					 	var rid=sel.length>0?sel:[closestRoomId];
					 	var msg=rid.length>1?(''+rid.length+' rooms'):('room '+rid[0]);
					 	SiConfirm('Delete '+msg+'?', function(){
						 	self.deleteRoom(rid); 
						 	self.updateMap();
					 	})},
					 "e":""},
					{"n":"Add Custom Line",
					 "a":function(event) {
					 	self.clearMapSelection();
					 	var subMenu = [];
					 	var lines = self.getCustomLines(self.getPlayerRoom());
					 	for(var dir in window.DirNameNums) {
					 	  var dirNum = window.DirNameNums[dir];
					 	  var dirCode = window.DirNumCodes[dirNum];
					 	  if(!lines[dirCode])
					 		subMenu.push({n:dir,e:'',a:(function(dirCode){return function(event) {
					 			SiPrompt('Enter solid, dash, dot, or dashdot:',function(i){
					 			  if(i && (window.MapLineStyles.indexOf(i)>=0)) {
					 				 self.addCustomLine(self.getPlayerRoom(),closestRoomId,dirCode,i);
					 				 self.updateMap();
					 			  }})
					 		}})(dir)});
					 	}
					 	subMenu.push({n:'special',e:'',a:function(event) {
					 		SiPrompt('Enter special direction name',function(sd){
					 			if(!sd) return;
					 			SiPrompt('Enter solid, dash, dot, dashdot:',function(i){
					 				if(i && (window.MapLineStyles.indexOf(i)>=0)) {
					 			 		self.addCustomLine(self.getPlayerRoom(),closestRoomId,sd,i);
					 					self.updateMap();
					 			  }
					 		  	})});
					 	}});
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);
					 	},
					 "e":""},
					{"n":"Delete Custom Line",
					 "a":function(event) {
					 	self.clearMapSelection();
					 	var subMenu = [];
					 	var lines = self.getCustomLines(closestRoomId);
					 	for(var dir in lines) {
					 	  var dirNm = (dir in window.DirCodeNames)? window.DirCodeNames[dir]:dir;
					 		subMenu.push({n:dirNm,e:'',a:(function(dir){return function(event) {
					 			 self.removeCustomLine(closestRoomId,dir);
					 			 self.updateMap();
					 	  }})(dir)});
					 	}
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);
					 	},
					 "e":"Object.keys(window.currWin.mapper.getCustomLines('"+closestRoomId+"')).length > 0"},
				];
				menu = menu.concat(buildCustomMenu('room'));
				DropDownMenu(e, e.clientX, e.clientY, 'auto', 12, menu);
			}
			else
			if(closestLabelId != null)
			{
				var menu= [];
				if (self.areas[areaId].labels[closestLabelId].type !== 'image') {
					menu.push({"n":"Edit Text",
					 "a":function(event) {
					 	var label = self.getMapLabel(areaId,closestLabelId);
					 	  SiPrompt('Enter new text:',function(text){
							  label.text = text;
					 		  self.updateMap();
					 	  });
						}
					});
					menu.push({"n":"Label Color",
					 "a":function(event) {
					 	var label = self.getMapLabel(areaId,closestLabelId);
					 	  SiColorPicker('Pick foreground color:',function(fg){
					 		SiColorPicker('Pick foreground color:',function(bg) {
					 		  label.fgColor= `rgb(${fg[0]}, ${fg[1]}, ${fg[2]})`;
							  label.bgColor= `rgb(${bg[0]}, ${bg[1]}, ${bg[2]})`;
							  label.foregroundTransparency = fg[3];
							  label.backgroundTransparency = bg[3];
					 		  self.updateMap();
					 		},true);
					 	  },true);
						}
					});
					menu.push({"n":"Label Font",
					 "a":function(event) {
					 	var label = self.getMapLabel(areaId,closestLabelId);
					 	  SiFontPicker('Pick foreground color:',function(name,sz){
							  label.fontSize=sz;
							  label.fontName=name;
					 		  self.updateMap();
					 	  });
						}
					});
				}
				menu.push({"n":"Delete Label",
				 "a":function(event) {
				 	SiConfirm('Delete label?',function(){
					 	 self.deleteMapLabel(areaId,closestLabelId);
					 	 self.updateMap();
				 	 });
				}});
				menu = menu.concat(buildCustomMenu('room'));
				DropDownMenu(e, e.clientX, e.clientY, 200, 12, menu);
			}
			else
			{
				var menus =[
					{"n":"Change Area",
					 "a":function(event) {
					 	self.clearMapSelection();
					 	event.stopPropagation();
					 	var select = document.createElement('select');
					 	for(var areaId in self.areas) {
					 	  var area = self.areas[areaId];
					 	  if(self.getAreaCenterRoom(areaId)!=null)
							select.add(new Option(area.name,areaId));
					 	}
					 	select.selectedIndex = -1;
					 	var node = this;
					 	select.style.fontFamily = this.style.fontFamily;
					 	select.style.fontSize = this.style.fontSize;
					 	select.style.maxHeight=node.parentNode.style.height;
					 	select.style.maxWidth=node.parentNode.style.width;
					 	select.style.overflowY='auto';
					 	select.onclick=function(e){
							e.stopPropagation();
						};
					 	select.onchange=function(e){
							self.viewArea(select.value);
							self.updateMap();
							e.stopPropagation();
						};
						var currentAreaId = self.rooms[self.centerView] ? self.rooms[self.centerView].areaId : Object.keys(self.areas)[0] || '';
						select.value = currentAreaId;
					 	node.innerHTML='';
					 	node.appendChild(select);
					 	},
					 "e":"true"},
					{"n":"Add Room",
					 "a":function(event) {
					 	if (areaId) {
					 	 var id = self.addRoom(self.createRoomId(), areaId);
					 	 if(id !== false) {
					 	  self.centerview(id);
					 		self.setRoomCoordinates(id,gridX,gridY,zLevel);
					 		self.updateMap();
					 	 }
					 	}},
					 "e":"("+isValidAddRoom+") && ('"+areaId+"')"},
					{"n":"Center View",
					 "a":function(event) {
					 	  self.mapWidget.layout.offsetX = 
					 	  	(self.mapWidget.canvas.width / 2) 
					 	  	- (gridX * self.mapWidget.layout.tileSize * SpacingRatio);
					 	  self.mapWidget.layout.offsetY = 
					 	  	(self.mapWidget.canvas.height / 2) 
					 	  	- (gridY * self.mapWidget.layout.tileSize * SpacingRatio);
					 	  self.updateMap();
					 	},
					 "e":"true"},
					{"n":"Move Room To",
					 "a":function(event) {
					 	  self.setRoomCoordinates(self.centerView,gridX,gridY,zLevel);
					 	  self.updateMap();
					 	},
					 "e":"window.currWin.mapper.centerView"},
					{"n":"Add Label",
					 "a":function(event) {
					 	SiPrompt('Enter label text:',function(i){
					 		self.createMapLabel(areaId,i,gridX,gridY,zLevel,255,255,255,0,0,0);
					 		self.updateMap();
					 	});},
					 "e":"('"+areaId+"')"},
					{"n":"Add Image Label",
					 "a":function(event) {
						SiPrompt('Enter image file path (URL or local):',function(filePath){
							if(filePath) {
								SiPrompt('Enter width (pixels):',function(w){
									w=Math.max(parseFloat(w),5);
									SiPrompt('Enter height (pixels):',function(h){
										h=Math.max(parseFloat(h),5);
										self.createMapImageLabel(areaId,filePath,gridX,gridY,zLevel,w,h);
										self.updateMap();
									});
								});
							}
						});},
					 "e":"('"+areaId+"')"},
					{"n":"Add Custom Line",
					 "a":function(event) {
					 	var subMenu = [];
					 	var coords = [gridX,gridY,zLevel];
					 	var lines = self.getCustomLines(self.getPlayerRoom());
					 	for(var dir in window.DirNameNums) {
					 	  var dirNum = window.DirNameNums[dir];
					 	  var dirCode = window.DirNumCodes[dirNum];
					 	  if(!lines[dirCode])
					 		subMenu.push({n:dir,e:'',a:(function(dirNum){return function(event) {
					 			SiPrompt('Enter solid, dash, dot, or dashdot:',function(i){
					 			  if(i && (window.MapLineStyles.indexOf(i)>=0)) {
					 				 self.addCustomLine(self.getPlayerRoom(),coords,dirNum,i);
					 				 self.updateMap();
					 			  }})
					 		}})(dirNum)});
					 	}
					 	subMenu.push({n:'special',e:'',function(event) {
					 		SiPrompt('Enter special direction name',function(sd){
					 			if(!sd) return;
					 			SiPrompt('Enter solid, dash, dot, or dashdot:',function(i){
					 				if(i && (window.MapLineStyles.indexOf(i)>=0)) {
					 			 		self.addCustomLine(self.getPlayerRoom(),coords,sd,i);
					 					self.updateMap();
					 			  }
					 		  	})});
					 	}});
					 	var c = getComputedStyle(this.parentNode.parentNode);
					 	var x = parseFloat(c.left) + parseFloat(c.width);
					 	var y = event.currentTarget.getBoundingClientRect().top + window.scrollY;
					 	DropDownMenu(null,x,y,'auto',12,subMenu,true);
					 	},
					 "e":""},
					{"n":"Export Area",
					 "a":function(event) {
						var areaId = self.rooms[self.getViewRoomId()] ? self.rooms[self.getViewRoomId()].areaId : Object.keys(self.areas)[0];
						if(!areaId) return;
						var name = self.areas[areaId].name || 'area';
						self.exportAreaToUser(areaId, name+'.json');
					},
					 "e":"Object.keys(window.currWin.mapper.areas).length > 0"},
					{"n":"Import Area",
					 "a":function(event) {
						self.importAreaFromBrowser();
					},
					 "e":"true"},
				];
				menus = menus.concat(buildCustomMenu('room'));
				DropDownMenu(e, e.clientX, e.clientY, 200, 12, menus);
			}
		};
		var zoomHandler = function(event) {
			event.preventDefault();
			var delta = event.deltaY < 0 ? 0.1 : -0.1;
			var viewedAreaId = self.currentAreaView || (self.rooms[self.centerView] ? self.rooms[self.centerView].areaId : undefined);
			var currentZoom = self.getMapZoom(viewedAreaId);
			var newZoom = currentZoom + delta;
			self.setMapZoom(newZoom, viewedAreaId); // caps are in there
			self.updateMap();
		};
		canvas.addEventListener('mousedown', mouseDownHandler);
		canvas.addEventListener('mousemove', mouseMoveHandler);
		document.addEventListener('mouseup', mouseUpHandler);
		canvas.addEventListener('contextmenu', menuHandler);
		canvas.addEventListener('wheel', zoomHandler);
		this.mapWidget = {
			canvas: canvas,
			ctx: ctx,
			x: typeof x === 'string' ? 0 : (x || 0),
			y: typeof x === 'string' ? 0 : (y || 0),
			width: targetWidth,
			height: targetHeight,
			frame: typeof x === 'string' ? x : null,
			titleBar: titleBar,
			zoomHandler: zoomHandler,
			mouseDownHandler: mouseDownHandler,
			mouseMoveHandler: mouseMoveHandler,
			mouseUpHandler: mouseUpHandler
		};
		this.zoom = 1.0;
		this.centerview(this.centerView || Object.keys(this.rooms)[0] || null);
		this.updateMap();
		this.saveLayout = function(frameName, x, y, width, height) {
			this.userData = this.userData || {};
			this.userData.layout = { frameName, x, y, width, height };
		};
		this.saveLayout(typeof x === "string" ? x : "f", x, y, targetWidth, targetHeight);
		return this.mapWidget;
	};
	
	this.createRoomId = function(minimumStartingRoomId) {
		var x = this.nextRoomId;
		var useArg = (minimumStartingRoomId !== undefined)&&(typeof minimumStartingRoomId === 'number'); 
		if(useArg)
			x = minimumStartingRoomId;
		while(x in this.rooms)
			x++;
		if(!useArg)
		{
			var y = x+1;
			while(y in this.rooms)
				y++;
			this.nextRoomId = y;
		}
		return x;
	};
	this.createAreaId = function(minimumStartingAreaId) {
		var x = this.nextAreaId;
		var useArg = (minimumStartingAreaId !== undefined)&&(typeof minimumStartingAreaId === 'number'); 
		if(useArg)
			x = minimumStartingAreaId;
		while(x in this.areas)
			x++;
		if(!useArg)
		{
			var y = x+1;
			while(y in this.rooms)
				y++;
			this.nextAreaId = y;
		}
		return x;
	};
	this.deleteArea = function(areaId) {
		if(!(areaId in this.areas))
			areaId = this.findAreaId(areaId);
		if(areaId == null)
			return false;
		var rooms = this.getAreaRooms(areaId);
		for(var k in rooms)
		{
			delete this.rooms[k];
			if(k == this.centerView)
				centerview((Object.keys(this.rooms).length)?Object.keys(this.rooms)[0]:null);
		}
		delete this.areas[areaId];
		return true;
	};
	this.deleteMapLabel = function(areaId, labelId) {
		if(areaId in this.areas)
		{
			var area = this.areas[areaId];
			if(labelId in area.labels)
			{
				delete area.labels[labelId];
				return true;
			}
		}
		return false;
	};
	this.deleteRoom = function(roomId) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				delete this.rooms[roomId];
				if(roomId == this.centerView)
					centerview((Object.keys(this.rooms).length)?Object.keys(this.rooms)[0]:null);
				for(var k in this.rooms)
				{
					var room = this.rooms[k];
					if(room.exits)
					{
						for(var d=0;d<room.exits.length;d++)
						{
							var exit = room.exits[d];
							if(exit.roomId == roomId)
								exit.roomId = '';
						}
					}
				}
				success = true;
			}
		}
		return success;
	};
	this.disableMapInfo = function(label) {}; // TODO:
	this.enableMapInfo = function(label) {}; // TODO:
	this.getAllAreaUserData = function(areaId) {
		if(areaId in this.areas)
		{
			var area = this.areas[areaId];
			if(area.userData)
				return area.userData;
			return {};
		}
		return null;
	};
	this.getAllMapUserData = function() {
		if(this.userData)
			return this.userData;
		return {};
	};
	this.getAllRoomEntrances = function(roomId) {
		if(!(roomId in this.rooms))
			return [];
		var all = [];
		for(var k in this.rooms)
		{
			var room = this.rooms[k];
			if((k != roomId)&&(room.exits))
			{
				for(var d=0;d<room.exits.length;d++)
					if(room.exits[d].roomId == roomId) {
						var nex = JSON.parse(JSON.stringify(room.exits[d]));
						nex.roomId = k;
						all.push(nex);
					}
			}
		}
		return all;
	};
	this.getAllRoomUserData = function(roomId) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var all = {};
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				var room = this.rooms[roomId];
				if(room.userData)
				{
					for(var k in room.userData)
						if((k in all) && (all[k] != room.userData[k]))
							all[k] += room.userData[k];
						else
							all[k] = room.userData[k];
				}
			}
		}
		return all;
	};
	this.getAreaExits = function(areaId, showExits) {
		showExits = !!showExits;
		if(!(areaId in this.areas))
			return showExits?[]:{};
		var all = showExits?[]:{};
		for(var k in this.rooms)
		{
			var room = this.rooms[k];
			if(room.areaId == areaId && room.exits)
			{
				for(var d=0;d<room.exits.length;d++)
					if(room.exits[d].roomId in this.rooms) {
						var oroom = this.rooms[room.exits[d].roomId];
						if(oroom.areaId != areaId)
						{
							if(!showExits)
							{
								all[k] = true;
								break;
							}
							else
							{
								var nex = JSON.parse(JSON.stringify(room.exits[d]));
								nex.roomId = k;
								all.push(nex);
							}
						}
					}
			}
		}
		return all;
	};
	this.getAreaRooms = function(areaId) {
		if(areaId in this.areas) {
			var rooms = {};
			for(var k in this.rooms) {
				if(this.rooms[k].areaId == areaId)
					rooms[k] = this.rooms[k].name;
			}
			return rooms;
		}
		return null; 
	};
	this.getAreaRooms1 = function(areaId) {
		if(areaId in this.areas) {
			var rooms = [];
			for(var k in this.rooms) {
				if(this.rooms[k].areaId == areaId)
					rooms.push(k);
			}
			return SortArray(rooms);
		}
		return null; 
	};
	this.getAreaCenterRoom = function(areaId) {
		if(areaId in this.areas) {
			var rooms = this.getAreaRooms(areaId);
			if(!rooms || !Object.keys(rooms).length)
				return null;
			var minmax = null;
			for(var k in this.rooms)
			{
				var room = this.rooms[k];
				if(this.rooms[k].areaId == areaId)
				{
					if(minmax == null)
						minmax = [room.x,room.y,room.x,room.y];
					else
					{
						minmax[0] = (room.x < minmax[0])?room.x:minmax[0];
						minmax[1] = (room.y < minmax[1])?room.y:minmax[1];
						minmax[2] = (room.x > minmax[2])?room.x:minmax[2];
						minmax[3] = (room.y > minmax[3])?room.y:minmax[3];
					}
				}
			}
			var center = [(minmax[0]+((minmax[2]-minmax[0])/2)),
						  (minmax[1]+((minmax[3]-minmax[1])/2))];
			var diff = Infinity;
			var diffRoomId = null;
			for(var k in this.rooms)
			{
				var room = this.rooms[k];
				if(this.rooms[k].areaId == areaId)
				{
					var rdiff = (Math.abs(room.x-center[0]) + Math.abs(room.y-center[1]));
					if(rdiff < diff)
					{
						diffRoomId = k;
						diff = rdiff;
					}
				}
			}
			return diffRoomId;
		}
		return null; 
	};
	this.getAreaTable = function() {
		var all = {};
		for(var k in this.areas)
			all[this.areas[k].name] = k;
		return all;
	};
	this.getAreaTableSwap = function() {
		var all = {};
		for(var k in this.areas)
			all[k] = this.areas[k].name;
		return all;
	};
	this.getAreaUserData = function(areaId, key) {
		if(!key || !(areaId in this.areas))
			return null;
		if(this.areas[areaId].userData)
			if(key in this.areas[areaId].userData)
				return this.areas[areaId].userData[key];
		return null;
	};
	this.getCustomEnvColorTable = function() {
		return this.envs;
	};
	this.getCustomLines = function(roomId) {
		if (!(roomId in this.rooms))
			return {};
		var startRoom = this.rooms[roomId];
		var customLines = startRoom.customLines || {};
		var result = {};
		for (var direction in customLines) {
			var line = customLines[direction];
			var points = [];
			points.push({
				x: startRoom.x,
				y: startRoom.y
			});
			if (line.to in this.rooms) 
			{
				var endRoom = this.rooms[line.to];
				points.push({
					x: endRoom.x,
					y: endRoom.y
				});
			} 
			else 
			if (Array.isArray(line.to) && line.to.length === 3 && line.to.every(n => typeof n === 'number')) 
			{
				points.push({
					x: line.to[0],
					y: line.to[1]
				});
			} else
				continue;
			result[direction] = {
				attributes: {
					color: {
						r: line.color[0],
						g: line.color[1],
						b: line.color[2]
					},
					style: line.style,
					arrow: line.arrow
				},
				points: points
			};
		}
		return result;
	};
	this.getCustomLines1 = this.getCustomLines;
	this.getDoors = function(roomId) {
		if(!(roomId in this.rooms))
			return [];
		var room = this.rooms[roomId];
		if(!room.exits)
			return [];
		var all = [];
		for(var d=0;d<room.exits.length;d++)
		{
			var ex = room.exits[d];
			if(ex.door != null)
				all.push({'direction':ex.dir, 'door': ex.door});
		}
		return all;
	};
	this.getExitStubs = function(roomId) {
		if(!(roomId in this.rooms))
			return [];
		var room = this.rooms[roomId];
		if(!room.exits)
			return [];
		var all = [];
		for(var d=0;d<room.exits.length;d++)
		{
			var ex = room.exits[d];
			if(ex.roomId == '')
				if(ex.dir in window.DirNameNums)
					all.push(window.DirNameNums[ex.dir]);
		}
		return all;
	};
	this.getExitStubs1 = this.getExitStubs;
	
	this.getExitWeights = function(roomId) {
		if(!(roomId in this.rooms))
			return [];
		var room = this.rooms[roomId];
		if(!room.exits)
			return [];
		var all = {};
		for(var d=0;d<room.exits.length;d++)
		{
			var ex = room.exits[d];
			if(ex.weight != null)
				all[ex.dir] = ex.weight;
		}
		return all;
	};
	this.getGridMode = function(areaId) {
		if(!(areaId in this.areas))
			return false;
		var area = this.areas[areaId];
		return area.gridMode;
	};
	this.getMapEvents = function() {
		var events = {};
		for (var uniquename in this.customEvents) {
			events[uniquename] = {
				eventName: this.customEvents[uniquename].eventName,
				arguments: this.customEvents[uniquename].arguments
			};
		}
		return events;
	};

	this.getMapLabel = function(areaId, labelIdOrText) {
		if (!(areaId in this.areas) || !labelIdOrText) 
			return null;
		var area = this.areas[areaId];
		if(!(labelIdOrText in area.labels)) {
			for(var k in area.labels) {
				if(area.labels.text == labelIdOrText)
					labelIdOrText = k;
			}
			if(!(labelIdOrText in area.labels))
				return null;
		}
		return area.labels[labelIdOrText];
	};

	this.getMapLabels = function(areaId) {
		if (!(areaId in this.areas) || !labelIdOrText) 
			return null;
		var area = this.areas[areaId];
		if(!area.labels)
			return {};
		var all = {};
		for(var k in area.labels) {
			all[k] = area.labels[k].text || area.labels[k].filePath;
		}
		return all;
	};

	this.getMapMenus = function() {
		var menus = {};
		for (var uniquename in this.customMenus) {
			menus[uniquename] = {
				parent: this.customMenus[uniquename].parent,
				displayName: this.customMenus[uniquename].displayName
			};
		}
		return menus;
	};
	this.getMapSelection = function() {
		return this.mapWidget && this.mapWidget.selectedRooms ? this.mapWidget.selectedRooms : [];
	};
	this.getMapUserData = function(key) {
		if(!key || (!this.userData))
			return null;
		if(key in this.userData)
			return this.userData[key];
		return null;
	};

	this.getMapZoom = function(areaId) {
		if(areaId && (areaId in this.areas))
		{
			var area = this.areas[areaId];
			return area.zoom !== undefined ? area.zoom : this.zoom || 1.0;
		}
		return this.zoom || 1.0;
	};

	this.getPath = function(fromId, toId) {
		if (!fromId || !toId 
		|| !(fromId in this.rooms) 
		|| !(toId in this.rooms))
			return null;
		var distances = {};
		var previous = {};
		var commands = {};
		var queue = Object.keys(this.rooms);
		distances[fromId] = 0;
		queue.forEach(function(id) {
			if (id != fromId) 
				distances[id] = Infinity;
			previous[id] = null;
			commands[id] = null;
		});
		while (queue.length > 0) {
			var minDist = Infinity;
			var currentId = null;
			var currentIdx = null;
			queue.forEach(function(id, idx) {
				if (distances[id] < minDist) {
					minDist = distances[id];
					currentId = id;
					currentIdx = idx;
				}
			});
			if (!currentId || currentId == toId) 
				break;
			queue.splice(currentIdx,1);
			if (this.roomLocked(currentId)) 
				continue;
			var exits = this.getRoomExits(currentId);
			var specialExits = this.getSpecialExits(currentId);
			var roomWeight = this.getRoomWeight(currentId) || 1;
			for (var dir in exits) {
				var toId2 = exits[dir];
				if (!(toId2 in this.rooms) || !queue.some(id => id == toId2)) 
					continue;
				if (this.hasExitLock(currentId, dir))
					continue;
				var weight = (this.getExitWeights(currentId)[dir] || 1) + roomWeight;
				var altDist = distances[currentId] + weight;
				if (altDist < distances[toId2]) {
					distances[toId2] = altDist;
					previous[toId2] = currentId;
					commands[toId2] = dir;
				}
			}
			for (var toId2 in specialExits) 
			{
				if (!(toId2 in this.rooms) || !queue.some(id => id == toId2))
					continue;
				if (this.hasSpecialExitLock(currentId, toId2, specialExits[toId2][0])) 
					continue;
				var weight = (specialExits[toId2][0].weight || 1) + roomWeight;
				var altDist = distances[currentId] + weight;
				if (altDist < distances[toId2]) {
					distances[toId2] = altDist;
					previous[toId2] = currentId;
					commands[toId2] = specialExits[toId2][0];
				}
			}
		}
		if (distances[toId] == Infinity)
			return null;
		var path = [];
		var currentId = toId;
		while (currentId != fromId) {
			if (!previous[currentId]) 
				break;
			path.push(commands[currentId]);
			currentId = previous[currentId];
		}
		path.reverse();
		var dirString = [];
		var lastDir = null;
		var count = 0;
		path.forEach(function(item) 
		{
			var dir = typeof item === 'string' ? item : item.moveCommand;
			if (dir == lastDir)
				count++;
			else 
			{
				if (lastDir)
					dirString.push((count > 1 ? count : '') + lastDir);
				lastDir = dir;
				count = 1;
			}
		});
		if (lastDir)
			dirString.push((count > 1 ? count : '') + lastDir);
		return dirString.join(',');
	};

	this.getPlayerRoom = function() {
		return this.centerView;
	};
	this.getRoomArea = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		return this.rooms[roomId].areaId;
	};
	this.getRoomAreaName = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		var areaId = this.rooms[roomId].areaId;
		if(!(areaId in this.areas))
			return null;
		return this.areas[areaId].name;
	};
	this.getRoomChar = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		var room = this.rooms[roomId];
		if(room.char)
			return room.char;
		return this.charDefault;
	};
	this.getRoomCharColor = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		var room = this.rooms[roomId];
		if(room.color)
			return room.color;
		return this.colorDefault;
	};
	this.getRoomCoordinates = function(roomId) {
		if(!(roomId in this.rooms))
			return [null, null, null];
		var room = this.rooms[roomId];
		return [room.x,room.y,room.z];
	};
	this.getRoomEnv = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		return this.rooms[roomId].envId;
	};
	this.getRoomExits = function(roomId) {
		if(!(roomId in this.rooms))
			return [];
		var room = this.rooms[roomId];
		if(!room.exits)
			return [];
		var all = {};
		for(var d=0;d<room.exits.length;d++)
		{
			var ex = room.exits[d];
			if(ex.dir != 'special')
				all[ex.dir] = ex.roomId;
		}
		return all;
	};
	this.getRoomHashById = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		return this.rooms[roomId].hash;
	};
	this.getRoomIdbyHash = function(hash) {
		if(hash in this.roomIdHash)
			return this.roomIdHash[hash];
		return -1;
	};
	this.getRoomName = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		return this.rooms[roomId].name;
	};
	this.getRooms = function() {
		var all = {};
		for(var k in this.rooms)
			all[k] = this.rooms[k].name;
		return all;
	};
	this.getRoomsByPosition = function(areaId, x, y, z) {
		var all = [];
		for(var k in this.rooms)
		{
			var room = this.rooms[k];
			if((room.areaId == areaId)
			&&(room.x ==x)
			&&(room.y ==y)
			&&(room.z ==z))
				all.push(k);
		}
		return all;
	};
	this.getRoomUserData = function(fromId, key) {
		if(!key || !(fromId in this.rooms))
			return null;
		if(this.rooms[fromId].userData)
			if(key in this.rooms[fromId].userData)
				return this.rooms[fromId].userData[key];
		return null;
	};
	this.getRoomUserDataKeys = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		var all = [];
		if(this.rooms[roomId].userData)
			for(var key in this.rooms[roomId].userData)
				all.push(key);
		return all;
	};
	this.getRoomWeight = function(roomId) {
		if(!(roomId in this.rooms))
			return null;
		return this.rooms[roomId].weight;
	};
	this.getSpecialExits = function(roomId, listAllExits) {
		if(!(roomId in this.rooms))
			return [];
		var room = this.rooms[roomId];
		if(!room.exits)
			return [];
		var all = {};
		for(var d=0;d<room.exits.length;d++)
		{
			var ex = room.exits[d];
			if(ex.dir == 'special')
			{
				if(ex.roomId in all)
				{
					if(!listAllExits)
						continue;
					all[ex.roomId] = [all[ex.roomId]];
					all[ex.roomId].push([ex.moveCommand,ex.blocked?'1':'0']);
				}
				else
					all[ex.roomId] = [ex.moveCommand,ex.blocked?'1':'0'];
			}
		}
		return all;
	};
	this.getSpecialExitsSwap = function(roomId, listAllExits) {
		if(!(roomId in this.rooms))
			return [];
		var room = this.rooms[roomId];
		if(!room.exits)
			return [];
		var all = {};
		for(var d=0;d<room.exits.length;d++)
		{
			var ex = room.exits[d];
			if(ex.dir == 'special')
			{
				if(ex.moveCommand in all)
				{
					if(!listAllExits)
						continue;
					all[ex.moveCommand] = [all[ex.moveCommand]];
					all[ex.moveCommand].push([ex.roomId,ex.blocked?'1':'0']);
				}
				else
					all[ex.moveCommand] = [ex.roomId,ex.blocked?'1':'0'];
			}
		}
		return all;
	};
	this.gotoRoom = function(roomId) {
		if (!(roomId in this.rooms) || !this.centerView) 
			return false;
		var path = this.getPath(this.centerView, roomId);
		if (!path) 
			return false;
		this.speedwalk(path.join(','), false, 0, true);
		this.centerview(roomId);
		this.updateMap();
		return true;
	};
	this.hasExitLock = function(roomId, direction) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		if(!room.exits)
			return false;
		direction = GetDirCode(direction);
		if(!direction)
			return false;
		var ex = this.getExitDir(room,direction);
		if(ex == null)
			return false;
		return ex.blocked;
	};
	this.hasSpecialExitLock = function(fromId, toId, moveCommand) {
		if((!(fromId in this.rooms))||(!(toId in this.rooms)))
			return false;
		var room = this.rooms[fromId];
		if(!room.exits)
			return false;
		for(var i=0;i<room.exits.length;i++)
		{
			var ex = room.exits[i];
			if(ex.roomId == toId && ex.dir == 'special' && ex.moveCommand == moveCommand)
				return ex.blocked;
		}
		return false;
	};
	this.highlightRoom = function(roomId, color1Red, color1Green, color1Blue, color2Red, color2Green, color2Blue, highlightRadius, color1Alpha, color2Alpha) {
		color1Red = Math.max(0, Math.min(255, color1Red || 255));
		color1Green = Math.max(0, Math.min(255, color1Green || 255));
		color1Blue = Math.max(0, Math.min(255, color1Blue || 255));
		color2Red = Math.max(0, Math.min(255, color2Red || 0));
		color2Green = Math.max(0, Math.min(255, color2Green || 0));
		color2Blue = Math.max(0, Math.min(255, color2Blue || 0));
		color1Alpha = Math.max(0, Math.min(1, color1Alpha !== undefined ? color1Alpha : 1));
		color2Alpha = Math.max(0, Math.min(1, color2Alpha !== undefined ? color2Alpha : 0));
		highlightRadius = highlightRadius > 0 ? highlightRadius : 1.5;
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				this.rooms[roomId].highlight = {
					color1: { r: color1Red, g: color1Green, b: color1Blue, a: color1Alpha },
					color2: { r: color2Red, g: color2Green, b: color2Blue, a: color2Alpha },
					radius: highlightRadius
				};
				success = true;
			}
		}
		if(success)
			this.updateMap();
		return success;
	};
	this.killMapInfo = function() {}; //TODO:
	this.loadJsonMap = function(pathFileName) {
		var self = this;
		sipwin.sipfs.load(pathFileName,function(err, fdata) {
			if(!err)
			{
				var data = JSON.parse(fdata);
				for(var k in data)
					self[k] = data[k];
			}
		}, true);
	};
	this.loadMap = this.loadJsonMap;
	this.lockExit = function(roomId, direction, lockIfTrue) {
		if(!(roomId in this.rooms))
			return;
		var room = this.rooms[roomId];
		if(!room.exits)
			return;
		direction = GetDirCode(direction);
		if(!direction)
			return;
		var ex = this.getExitDir(room,direction);
		if(ex == null)
			return;
		ex.blocked = lockIfTrue;
	};
	this.lockRoom = function(roomId, lockIfTrue) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				var room = this.rooms[roomId];
				room.blocked = lockIfTrue;
				success = true;
			}
		}
		return success;
	};
	this.lockSpecialExit = function(fromId, toId, moveCommand, lockIfTrue) {
		if((!(fromId in this.rooms))||(!(toId in this.rooms)))
			return;
		var room = this.rooms[fromId];
		if(!room.exits)
			return;
		for(var i=0;i<room.exits.length;i++)
		{
			var ex = room.exits[i];
			if(ex.roomId == toId && ex.dir == 'special' && ex.moveCommand == moveCommand)
				ex.blocked = lockIfTrue;
		}
	};
	this.moveMapWidget = function(Xpos, Ypos) {
		if(this.mapWidget &&  this.mapWidget.canvas && (this.mapWidget.frame == null))
		{
			var div = this.mapWidget.canvas.parentNode;
			var rect = window.currWin.topWindow.getBoundingClientRect();
			var width = div.offsetWidth;
			var height = div.offsetHeight;
			var newX = Math.max(0, Math.min(rect.width - width, Xpos));
			var newY = Math.max(0, Math.min(rect.height - height, Ypos));
			div.style.left = newX+'px';
			div.style.top = newY+'px';
			this.mapWidget.x = newX;
			this.mapWidget.y = newY;
		}
	};
	this.openMapWidget = function(Xpos, Ypos, width, height) 
	{
		var saved = (this.userData && this.userData.layout)?this.userData.layout : null;
		if (arguments.length === 0) 
		{
			var frameName = (saved == null) ? null : saved.frameName;
			if((saved != null)&&(saved.frameName))
				return this.createMapper(frameName);
			x = (saved == null) ? 100 : saved.x;
			y = (saved == null) ? 100 : saved.y;
			width = (saved == null) ? 200 : saved.width;
			height = (saved == null) ? 150 : saved.height;
			return this.createMapper(x,y,width,height);
		}
		if (arguments.length === 1 && typeof Xpos === "string") 
		{
			return this.createMapper(Xpos);
		}
		if (arguments.length === 4 && typeof Xpos === "number" && 
			typeof Ypos === "number" && typeof width === "number" && 
			typeof height === "number") 
		{
			return this.createMapper(Xpos, Ypos, width, height);
		}
		console.error("Invalid parameters for openMapWidget.");
		return false;
	};
	this.pauseSpeedwalk = function() {
		this.pauseSpeedwalk = true;
	};
	this.registerMapInfo = function(label, func) {}; //TODO:

	this.resumeSpeedwalk = function() {
		this.pauseSpeedwalk = false;
	};
	this.removeCustomLine = function(roomId, direction) {
		if (!(roomId in this.rooms)) 
			return false;
		var room = this.rooms[roomId];
		if(!room.customLines[direction])
			return false;
		delete room.customLines[direction];
		this.updateMap();
		return true;
	};

	this.removeMapEvent = function() {
		if (uniquename in this.customEvents) {
			delete this.customEvents[uniquename];
			if (uniquename in this.customMenus) {
				this.customMenus[uniquename].eventName = null;
				this.customMenus[uniquename].arguments = null;
			}
			return true;
		}
		return false;
	};
	
	this.removeMapMenu = function() {
		if (uniquename in this.customMenus) {
			delete this.customMenus[uniquename];
			delete this.customEvents[uniquename];
			for (var key in this.customMenus) 
			{
				if (this.customMenus[key].parent === uniquename)
					this.removeMapMenu(key);
			}
			return true;
		}
		return false;
	};
	
	this.removeSpecialExit = function(roomId, moveCommand) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		if(!room.exits)
			return false;
		for(var i=0;i<room.exits.length;i++)
		{
			var ex = room.exits[i];
			if(ex.dir == 'special' && ex.moveCommand == moveCommand)
			{
				room.exits.splice(i,1);	
				return true;
			}
		}
		return false;
	};

	this.resetRoomArea = function(roomId) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		room.areaId = ''; // default empty case is ''
	};

	this.resizeMapWidget = function(width, height) {
		if(this.mapWidget 
		&& this.mapWidget.canvas)
		{
			if (this.mapWidget.frame == null)
			{
				if(isNumber(width)
				&& isNumber(height))
				{
					var div = this.mapWidget.canvas.parentNode;
					div.style.width = width;
					div.style.height = height;
					this.mapWidget.width = width;
					this.mapWidget.height = height;
					this.mapWidget.canvas.width = width;
					this.mapWidget.canvas.height = height;
					this.updateMap();
				}
			}
			else
			{
				var div = this.mapWidget.canvas.parentNode;
				var calced = getComputedStyle(div);
				div.style.width = parseFloat(calced.width);
				div.style.height = parseFloat(calced.width);
				canvas.width = parseFloat(calced.width);
				canvas.height = parseFloat(calced.height);
			}
		}
	};
	this.roomExists = function(roomId) {
		return (roomId in this.rooms);
	};
	this.roomLocked = function(roomId) {
		if(!(roomId in this.rooms))
			return false;
		return this.rooms[roomId].blocked;
	};
	this.saveJsonMap = function(pathFileName) {
		var doc = {};
		for(var k in window.MapBlock)
			if(this[k] !== undefined)
				doc[k] = this[k];
		doc = JSON.stringify(doc);
		sipwin.sipfs.save(pathFileName, doc, function(e){});
	};
	
	this.saveMap = this.saveJsonMap;
	
	this.exportAreaJson = function(areaId) 
	{
		if (!(areaId in this.areas)) 
			return null;
		var areaData = JSON.parse(JSON.stringify(this.areas[areaId])); // Deep copy area
		var roomsInArea = {};
		for (var roomId in this.rooms) 
		{
			if (this.rooms[roomId].areaId === areaId)
				roomsInArea[roomId] = JSON.parse(JSON.stringify(this.rooms[roomId]));
		}
		var usedEnvs = {};
		for (var roomId in roomsInArea) 
		{
			var envId = roomsInArea[roomId].envId;
			if (envId in this.envs) 
				usedEnvs[envId] = JSON.parse(JSON.stringify(this.envs[envId]));
		}
		var areaHashes = {};
		for (var hash in this.roomIdHash) 
		{
			var rid = this.roomIdHash[hash];
			if (rid in roomsInArea) 
				areaHashes[hash] = rid;
		}
		return {
			area: areaData,
			rooms: roomsInArea,
			envs: usedEnvs,
			hashes: areaHashes,
		};
	};
	
	this.exportAreaToUser = function(areaId, suggestedFileName) 
	{
		var areaJson = this.exportAreaJson(areaId);
		if (!areaJson) 
			return;
		var doc = JSON.stringify(areaJson, null, 2);
		var blob = new Blob([doc], { type: 'application/json' });
		var url = URL.createObjectURL(blob);
		var a = document.createElement('a');
		a.href = url;
		a.download = suggestedFileName || (this.areas[areaId].name + '.json');
		sipwin.topWindow.appendChild(a);
		a.click();
		sipwin.topWindow.removeChild(a);
		URL.revokeObjectURL(url);
	};
	
	this.importAreaFromBrowser = function() 
	{
		var self = this;
		var input = document.createElement('input');
		input.type = 'file';
		input.accept = '.json';
		input.onchange = function(e) 
		{
			var file = e.target.files[0];
			if (file) 
			{
				var reader = new FileReader();
				reader.onload = function(ev) 
				{
					try 
					{
						var data = JSON.parse(ev.target.result);
						self.mergeAreaJson(data);
						self.updateMap();
					} 
					catch (err) 
					{
						console.error('Invalid JSON: ' + err);
					}
				};
				reader.readAsText(file);
			}
		};
		sipwin.topWindow.appendChild(input);
		input.click();
		sipwin.topWindow.removeChild(input);
	};

	this.mergeAreaJson = function(data) 
	{
		var self = this;
		var existingAreaId = self.findAreaId(data.area.name);
		if (existingAreaId !== null)
			self.deleteArea(existingAreaId);
		var newAreaId = self.createAreaId();
		self.areas[newAreaId] = data.area;
		self.areas[newAreaId].name = data.area.name; // Ensure name is set
		var roomIdMap = {};
		for(var oldRoomId in data.rooms) 
		{
			var newRoomId = self.createRoomId();
			roomIdMap[oldRoomId] = newRoomId;
			var room = data.rooms[oldRoomId];
			room.areaId = newAreaId;
			self.rooms[newRoomId] = room;
		}
		for(var newRoomId in self.rooms) 
		{
			var room = self.rooms[newRoomId];
			if (room.exits) 
			{
				room.exits.forEach(function(exit) 
				{
					if (exit.roomId in roomIdMap)
						exit.roomId = roomIdMap[exit.roomId]; // Remap intra-area
					else 
					if (exit.roomId && !(exit.roomId in self.rooms))
						exit.roomId = '';
				});
			}
			if (room.customLines) 
			{
				for (var dir in room.customLines) 
				{
					var line = room.customLines[dir];
					if (line.to in roomIdMap)
						line.to = roomIdMap[line.to];
				}
			}
		}
		for (var envId in data.envs) 
		{
			if (!(envId in self.envs)) 
				self.envs[envId] = data.envs[envId];
		}
		for (var hash in data.hashes) 
		{
			var oldRoomId = data.hashes[hash];
			if (oldRoomId in roomIdMap) 
			{
				self.roomIdHash[hash] = roomIdMap[oldRoomId];
				self.rooms[roomIdMap[oldRoomId]].hash = hash;
			}
		}
		self.nextAreaId = Math.max(self.nextAreaId, parseInt(newAreaId) + 1 || 1);
		self.nextRoomId = Math.max(self.nextRoomId, Math.max(...Object.keys(self.rooms).map(Number)) + 1 || 1);
	};
	
	this.searchAreaUserData = function(key, value) {
		var all = [];
		var keyMap = {};
		for(var areaId in this.areas)
		{
			var area = this.areas[areaId];
			if(area.userData)
			{
				if(key !== undefined)
				{
					if(value !== undefined)
					{
						if(key in area.userData && area.userData[key] == value)
							all.push(areaId);
					}
					else
					if(key in area.userData)
						keyMap[area.userData[key]] = true;
				}
				else
					for(var k in area.userData)
						keyMap[k] = true;
			}
		}
		for(var k in keyMap)
			all.push(k);
		return SortArray(all);
	};
	
	this.searchRoom = function(roomId, caseSensitive, exactMatch)
	{
		var all = {};
		if(roomId in this.rooms)
		{
			all[roomId] = this.rooms[roomId].name;
			return all;
		}
		var lroomid = (''+roomId).toLowerCase();
		for(var id in this.rooms)
		{
			var room = this.rooms[id];
			var n = (''+room.name);
			var match = false;
			if(exactMatch)
			{
				if(caseSensitive)
					match = n == roomId;
				else
					match = n.toLowerCase() == lroomid;
			}
			else
			if(caseSensitive)
				match = n.indexOf(roomId) >=0;
			else
				match = n.toLowerCase().indexOf(lroomid) >=0;
			if(match)
				all[id] = room.name;
		}
		return all;
	}
	
	this.searchRoomUserData = function(key, value) {
		var all = [];
		var keyMap = {};
		for(var roomId in this.rooms)
		{
			var room = this.rooms[roomId];
			if(room.userData)
			{
				if(key !== undefined)
				{
					if(value !== undefined)
					{
						if(key in room.userData && room.userData[key] == value)
							all.push(roomId);
					}
					else
					if(key in room.userData)
						keyMap[room.userData[key]] = true;
				}
				else
					for(var k in room.userData)
						keyMap[k] = true;
			}
		}
		for(var k in keyMap)
			all.push(k);
		return SortArray(all);
	};

	this.setAreaName = function(areaId, newName) {
		if(!newName)
			return true;
		if(areaId in this.areas)
		{
			this.areas[areaId].name = newName;
			return;
		}
		for(var k in this.areas)
		{
			var area = this.areas[k];
			if(area.name == areaId)
			{
				area.name = newName;
				return true;
			}
		}
		return false;
	};

	this.setAreaUserData = function(areaId, key, value) {
		if(areaId in this.areas)
		{
			var area = this.areas[areaId];
			if(!area.userData)
				area.userData = {};
			area.userData[key] = value;
		}
	};

	this.setCustomEnvColor = function(environmentId, r,g,b,a) {
		if(environmentId in window.MapEnvs)
			return false; // reserved for system or user
		if(environmentId) {
			this.envs[environmentId] = new MapEnv(r,g,b,a);
			return true;
		}
		return false;
	};

	this.setDoor = function(roomId, exitCommand, doorStatus) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		var dirCode = GetDirCode(exitCommand);
		if((!dirCode)||(!room.exits))
			return false;
		var exit = this.getExitDir(room,dirCode);
		if(exit)
		{
			if(doorStatus < 0)
			{
				exit.door = null;
				return true;
			}
			else
			if(doorStatus < 4)
			{
				exit.door = doorStatus;
				return true;
			}
		}
		return false;
	};

	this.setExit = function(fromId, toId, direction) {
		direction = GetDirCode(direction);
		if(!direction)
			return false;
		if(!(fromId in this.rooms))
			return false;
		var fromRoom = this.rooms[fromId];
		var fexit = this.getExitDir(fromRoom,direction);
		if(fexit)
		{
			if(toId < 0)
			{
				var k = fromRoom.exits.indexOf(fexit);
				fromRoom.exits.splice(k,1);
				return true;
			}
			if(!(toId in this.rooms))
				return false;
			if(fexit.roomId == '')
			{
				fexit.roomId = toId;
				return true;
			}
			return false;
		}
		if(toId < 0)
			return false;
		if(!(toId in this.rooms))
			return false;
		fexit = new MapExit({
			roomId: toId,
			dir: direction
		});
		fromRoom.exits.push(fexit);
		return true;
	};

	this.setExitStub = function(roomId, direction, set) {
		direction = GetDirCode(direction);
		if(!direction)
			return false;
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		var exit = this.getExitDir(room,direction);
		if(exit)
		{
			if((exit.roomId !== '')
			&&(!(exit.roomId in this.rooms)))
				exit.roomId = '';
			if(exit.roomId == '')
			{
				if(set === false)
				{
					var k = room.exits.indexOf(exit);
					room.exits.splice(k,1);
				}
				return true;
			}
			return false;
		}
		if(set === false)
			return false;
		if(set === true)
		{
			exit = new MapExit({
				roomId: '',
				dir: direction
			});
			room.exits.push(exit);
			return true;
		}
		return false;
	};
	
	this.setExitWeight = function(roomId, exitCommand, weight) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		var dir = GetDirCode(exitCommand);
		if(!room.exits)
			return false;
		var exit = null;
		if(!dir) // might be special
		{
			for(var k=0;k<room.exits.length;k++)
				if((room.exits[k].dir == 'special')
				&&(room.exits[k].moveCommand == exitCommand))
					exit = room.exits[k];
		}
		else
			exit = this.getExitDir(room,dir);
		if(!exit)
			return false;
		exit.weight = weight;
		return true;
	};
	
	this.setGridMode = function(areaId, tf) {
		if(!(areaId in this.areas))
			return false;
		var area = this.areas[areaId];
		area.gridMode = tf;
		return true;
	};
	
	this.setMapUserData = function(key, value) {
		if(!this.userData)
			this.userData = {};
		this.userData[key] = value;
	};
	
	this.setMapZoom = function(zoom, areaId) {
		zoom = Math.max(0.5, Math.min(10.0, zoom));
		if(areaId && (areaId in this.areas))
		{
			var area = this.areas[areaId];
			area.zoom = zoom;
			this.updateMap();
			return true;
		}
		this.zoom = zoom;
		this.updateMap();
		return true;
	};
	
	this.setRoomArea = function(roomId, areaId) {
		var roomIds=Array.isArray(roomId)?roomId:[roomId];
		for(var k=0;k<roomIds.length;k++)
		{
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				if(areaId in this.areas)
					this.rooms[roomId].areaId = areaId;
				else
				{
					var name = areaId;
					areaId = this.findAreaId(name);
					if(areaId == null)
					{
						areaId = this.createAreaId();
						this.areas[areaId] = new MapArea({
							name: name
						});
					}
					this.rooms[roomId].areaId = areaId;
				} 
			}
		}
	};
	
	this.setRoomChar = function(roomId, char) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				var room = this.rooms[roomId];
				room.char = char;
				success = true;
			}
		}
		return success;
	};
	this.setRoomCharColor = function(roomId, color) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				var room = this.rooms[roomId];
				room.color = color;
				success = true;
			}
		}
		return success;
	};
	this.setRoomCoordinates = function(roomId, x, y, z) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				var room = this.rooms[roomId];
				room.x = x;
				room.y = y;
				room.z = z;
				success = true;
			}
		}
		return success;
	};
	this.setRoomEnv = function(roomId, envId) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				this.rooms[roomId].envId = envId;
				success = true;
			}
		}
		return success;
	};
	this.setRoomIdByHash = function(roomId, hash) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		if(hash in this.roomIdHash)
		{
			var oldId = this.roomIdHash[hash];
			if(oldId in this.rooms)
				this.rooms[oldId].hash = '';
			delete this.roomIdHash[hash];
		}
		this.roomIdHash[hash] = roomId;
		room.hash = hash;
		return true;
	};

	this.setRoomName = function(roomId, name) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms) {
				this.rooms[roomId].name = name;
				success = true;
			}
		}
		return success;
	};

	this.setRoomUserData = function(roomId, key, value) {
		if(!key)
			return false;
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				var room = this.rooms[roomId];
				if(!room.userData)
					room.userData = {};
				if(key in room.userData)
					delete room.userData[key];
				if(value != null)
					room.userData[key] = value;
				success = true;
			}
		}
		return success;
	};
	this.setRoomWeight = function(roomId, weight) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				this.rooms[roomId].weight = weight;
				success = true;
			}
		}
		return success;
	};
	this.speedwalk = function(dirString, backwards, delay, show) {
		if (!dirString)
			return false;
		backwards = backwards || false;
		delay = (typeof delay === 'number') ? delay : 0;
		show = show !== false;
		var commands = [];
		var pattern = /(\d+)?([^,]+)(?=,|$)/gi;
		var match;
		while ((match = pattern.exec(dirString.toLowerCase())) !== null) 
		{
			var count = parseInt(match[1] || 1, 10);
			var cmd = match[2];
			var fullCmd = (cmd in window.DirCodeNames) ? window.DirCodeNames[cmd] : cmd;
			for (var i = 0; i < count; i++)
				commands.push(fullCmd);
		}
		if (commands.length === 0)
			return false;
		if (backwards)
			commands.reverse();
		self.speedWalking = true;
		var sendCommand = function(cmd, index) 
		{
			if((index >= commands.length) 
			|| (!self.speedWalking)
			|| (!self.mapWidget)
			|| (!self.mapWidget.canvas)
			|| (!self.mapWidget.canvas.isConnected))
			{
				self.speedWalking = false;
				return;
			}
			if(self.pauseSpeedwalk === true)
			{
				setTimeout(function() {
					sendCommand(cmd, index);
				}, 1000);
				return;
			}
			if(show)
				sipwin.submitInput(cmd);
			else
				sipwin.submitHidden(cmd);
			if (index + 1 < commands.length) 
			{
				setTimeout(function() {
					sendCommand(commands[index + 1], index + 1);
				}, delay * 1000);
			} 
		};
		sendCommand(commands[0], 0);
		return true;
	};
	this.stopSpeedwalk = function() {
		this.speedWalking = false;
	};
	this.unHighlightRoom = function(roomId) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				if (this.rooms[roomId].highlight) {
					delete this.rooms[roomId].highlight;
					success = true;
				}
			}
		}
		return success;
	};

	this.unsetRoomCharColor = function(roomId) {
		var roomIds = Array.isArray(roomId) ? roomId : [roomId];
		var success = false;
		for (var k = 0; k < roomIds.length; k++) {
			roomId = roomIds[k];
			if(roomId in this.rooms)
			{
				var room = this.rooms[roomId];
				room.color = null;
				success = true;
			}
		}
		return success;
	};
	
	this.isMapperCreated = function() {
		if (!this.mapWidget || !this.mapWidget.ctx) {
			return false;
		}
		return true;
	}
	
	this.recenterOnCurrent = function() {
		if(!this.mapWidget || !this.mapWidget.canvas || !this.centerView || !(this.centerView in this.rooms))
			return;
		this.mapWidget.reCenter = true;
	};

	this.updateMap = function() {
		if (!this.mapWidget || !this.mapWidget.ctx) {
			console.warn("Map widget not found. Call createMapper first.");
			return;
		}
		var ctx = this.mapWidget.ctx;
		var canvas = this.mapWidget.canvas;
		var currentRoomId = this.centerView;
		ctx.save();
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		ctx.fillStyle = 'black';
		ctx.fillRect(0, 0, canvas.width, canvas.height);
		ctx.font = "8px Arial";
		ctx.textAlign = "center";
		ctx.textBaseline = "middle";
		if (!currentRoomId || !(currentRoomId in this.rooms)) {
			ctx.fillStyle = "#000";
			ctx.fillText("No room selected", canvas.width / 2, canvas.height / 2);
			ctx.restore();
			return;
		}
		var currentRoom = this.rooms[currentRoomId];
		var viewRoomId = this.getViewRoomId();
		if(!viewRoomId) 
		{
			ctx.fillStyle = "#000";
			ctx.fillText("No room selected", canvas.width / 2, canvas.height / 2);
			ctx.restore();
			return;
		}
		var viewRoom = this.rooms[viewRoomId];
		var areaId = viewRoom.areaId;
		var zLevel = viewRoom.z;
		var roomsToDraw = Object.keys(this.rooms).filter(function(id) {
			var room = self.rooms[id];
			return room.areaId == areaId && room.z == zLevel;
		});
		var minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
		roomsToDraw.forEach(function(roomId) {
			var room = self.rooms[roomId];
			minX = Math.min(minX, room.x);
			maxX = Math.max(maxX, room.x);
			minY = Math.min(minY, room.y);
			maxY = Math.max(maxY, room.y);
		});
		var mapWidth = Math.max(maxX - minX + 1, 1);
		var mapHeight = Math.max(maxY - minY + 1, 1);
		var scale = this.getMapZoom(areaId);
		var baseTileSize = Math.min(canvas.width / mapWidth, canvas.height / mapHeight);
		var tileSize = baseTileSize * scale;
		tileSize = Math.max(tileSize, Math.min(canvas.width, canvas.height) / 25);
		var offsetX = this.mapWidget.layout && this.mapWidget.layout.offsetX !== undefined
			? this.mapWidget.layout.offsetX
			: canvas.width / 2 - viewRoom.x * tileSize * SpacingRatio;
		var offsetY = this.mapWidget.layout && this.mapWidget.layout.offsetY !== undefined
			? this.mapWidget.layout.offsetY
			: canvas.height / 2 - viewRoom.y * tileSize * SpacingRatio;
			
		if(this.mapWidget.reCenter)
		{
			var currX = offsetX + viewRoom.x * tileSize * SpacingRatio;
			var currY = offsetY + viewRoom.y * tileSize * SpacingRatio;
			var radius = tileSize / 10;
			if (currX + radius < 0 || currX - radius > canvas.width || currY + radius < 0 || currY - radius > canvas.height) {
				this.mapWidget.reCenter = false;
				offsetX = canvas.width / 2 - viewRoom.x * tileSize * SpacingRatio;
				offsetY = canvas.height / 2 - viewRoom.y * tileSize * SpacingRatio;
				this.mapWidget.layout.offsetX = offsetX;
				this.mapWidget.layout.offsetY = offsetY;
			}
		}
		this.mapWidget.layout = { tileSize, offsetX, offsetY, mapWidth, mapHeight, rooms: {} };
		roomsToDraw.forEach(function(roomId) {
			var room = self.rooms[roomId];
			var exits = self.getRoomExits(roomId);
			for (var dir in exits) 
			{
				var toRoomId = exits[dir];
				if (toRoomId in self.rooms && self.rooms[toRoomId].z == zLevel && self.rooms[toRoomId].areaId == areaId) 
				{
					var toRoom = self.rooms[toRoomId];
					var x1 = offsetX + room.x * tileSize * SpacingRatio;
					var y1 = offsetY + room.y * tileSize * SpacingRatio;
					var x2 = offsetX + toRoom.x * tileSize * SpacingRatio;
					var y2 = offsetY + toRoom.y * tileSize * SpacingRatio;
					var radius  = tileSize / 10;
					var dx = x2 - x1;
					var dy = y2 - y1;
					var dist = Math.sqrt(dx * dx + dy * dy);
					if (dist > 2* radius) {
						var isTwoWay = false;
						var oppositeDir = window.OpDirCodes[dir];
						if(oppositeDir) {
							var toRoomExits = self.getRoomExits(toRoomId);
							isTwoWay = toRoomExits[oppositeDir] == roomId;
						}
						var scale = radius / dist;
						var startX = x1 + dx * scale;
						var startY = y1 + dy * scale;
						var endX = x2 - dx * scale;
						var endY = y2 - dy * scale;
						ctx.beginPath();
						ctx.moveTo(startX, startY);
						ctx.lineTo(endX, endY);
						ctx.strokeStyle = isTwoWay?'#fff':"#666";
						ctx.lineWidth = Math.max(0.5, tileSize / 25);
						ctx.stroke();
					}
				}
			}
		});

		for (var roomId in self.rooms) 
		{
			if (!(roomId in self.rooms) 
			|| self.rooms[roomId].z !== zLevel)
				continue;
			var room = self.rooms[roomId];
			var x1 = offsetX + room.x * tileSize * SpacingRatio;
			var y1 = offsetY + room.y * tileSize * SpacingRatio;
			var radius = tileSize / 10;
			for (var direction in room.customLines) 
			{
				var line = room.customLines[direction];
				var x2, y2;
				if (line.to in self.rooms
				&& self.rooms[line.to].z === zLevel) 
				{
					var toRoom = self.rooms[line.to];
					x2 = offsetX + toRoom.x * tileSize * SpacingRatio;
					y2 = offsetY + toRoom.y * tileSize * SpacingRatio;
				} 
				else 
				if (Array.isArray(line.to)) 
				{
					x2 = offsetX + line.to[0] * tileSize * SpacingRatio;
					y2 = offsetY + line.to[1] * tileSize * SpacingRatio;
					if (line.to[2] !== zLevel)
						continue;
				}
				else
					continue;
				var dx = x2 - x1;
				var dy = y2 - y1;
				var dist = Math.sqrt(dx * dx + dy * dy);
				if (dist > 2 * radius) 
				{
					var scale = radius / dist;
					var startX = x1 + dx * scale;
					var startY = y1 + dy * scale;
					var endX = x2 - dx * scale;
					var endY = y2 - dy * scale;
					ctx.beginPath();
					ctx.moveTo(startX, startY);
					ctx.lineTo(endX, endY);
					if (line.style === 'dash')
						ctx.setLineDash([tileSize / 10, tileSize / 10]);
					else 
					if (line.style === 'dot')
						ctx.setLineDash([tileSize / 50, tileSize / 50]);
					else 
					if (line.style === 'dashdot')
						ctx.setLineDash([tileSize / 10, tileSize / 20, tileSize / 50, tileSize / 20]);
					else
						ctx.setLineDash([]); // Solid
					ctx.strokeStyle = `rgb(${line.color[0]}, ${line.color[1]}, ${line.color[2]})`;
					ctx.lineWidth = Math.max(0.5, tileSize / 25);
					ctx.stroke();
					if (line.arrow)
					{
						var arrowSize = tileSize / 10;
						var angle = Math.atan2(endY - startY, endX - startX);
						ctx.beginPath();
						ctx.moveTo(endX, endY);
						ctx.lineTo(
							endX - arrowSize * Math.cos(angle - Math.PI / 6),
							endY - arrowSize * Math.sin(angle - Math.PI / 6)
						);
						ctx.moveTo(endX, endY);
						ctx.lineTo(
							endX - arrowSize * Math.cos(angle + Math.PI / 6),
							endY - arrowSize * Math.sin(angle + Math.PI / 6)
						);
						ctx.strokeStyle = `rgb(${line.color[0]}, ${line.color[1]}, ${line.color[2]})`;
						ctx.lineWidth = Math.max(0.5, tileSize / 25);
						ctx.stroke();
					}
					ctx.setLineDash([]);
				}
			}
		}

		roomsToDraw.forEach(function(roomId) {
			var room = self.rooms[roomId];
			var x = offsetX + room.x * tileSize * SpacingRatio;
			var y = offsetY + room.y * tileSize * SpacingRatio;
			var radius = tileSize / 10;
			self.mapWidget.layout.rooms[roomId] = {
				centerX: x,
				centerY: y,
				radius: radius
			};
			var color = 'white';
			if (room.envId in window.MapEnvs) {
				var env = window.MapEnvs[room.envId];
				color = `rgba(${env.r}, ${env.g}, ${env.b}, ${env.a / 255})`;
			} else if (room.envId in self.envs) {
				var env = self.envs[room.envId];
				color = `rgba(${env.r}, ${env.g}, ${env.b}, ${env.alpha / 255})`;
			}
			ctx.beginPath();
			ctx.arc(x, y, radius, 0, 2 * Math.PI);
			ctx.fillStyle = color;
			ctx.fill();
			if(room.highlight) {
				var hlRadius = room.highlight.radius * radius;
				if (room.highlight.color1.a > 0) {
					ctx.beginPath();
					ctx.arc(x, y, hlRadius, 0, 2 * Math.PI);
					ctx.strokeStyle = `rgba(${room.highlight.color1.r}, ${room.highlight.color1.g}, ${room.highlight.color1.b}, ${room.highlight.color1.a})`;
					ctx.lineWidth = tileSize / 20;
					ctx.stroke();
				}
				if (room.highlight.color2.a > 0) {
					ctx.beginPath();
					ctx.arc(x, y, hlRadius + tileSize / 40, 0, 2 * Math.PI);
					ctx.strokeStyle = `rgba(${room.highlight.color2.r}, ${room.highlight.color2.g}, ${room.highlight.color2.b}, ${room.highlight.color2.a})`;
					ctx.lineWidth = tileSize / 20;
					ctx.stroke();
				}
			}
			var char = self.getRoomChar(roomId);
			ctx.fillStyle = room.color ? `rgb(${room.color[0]}, ${room.color[1]}, ${room.color[2]})` : "#000";
			var fontSize = Math.max(5, tileSize / 6);
			ctx.font = fontSize + 'px Arial';
			ctx.fillText(char, x, y);
			if (self.mapWidget.selectedRooms && self.mapWidget.selectedRooms.includes(roomId)) 
			{
				ctx.beginPath();
				ctx.arc(x, y, radius * 1.2, 0, 2 * Math.PI);
				ctx.strokeStyle = "yellow";
				ctx.lineWidth = tileSize / 20;
				ctx.stroke();
			}
		});
		if (areaId in this.areas && this.areas[areaId].labels)
		{
			var labels = this.areas[areaId].labels;
			for (var labelId in labels) {
				var label = labels[labelId];
				if (label.z == zLevel) {
					var x = offsetX + label.x * tileSize * SpacingRatio;
					var y = offsetY + label.y * tileSize * SpacingRatio;
					if (label.type === 'image') {
						if (label.image && label.image.complete) {
							var imgWidth = label.width * (label.zoom || 1) * scale;
							var imgHeight = label.height * (label.zoom || 1) * scale;
							ctx.drawImage(label.image,x - imgWidth / 2,y - imgHeight / 2,imgWidth,imgHeight);
						} else if(!label._loading) {
							label._loading = true;
							sipwin.sipfs.load(label.filePath,function(err, dataUrl) {
								if(!err)
								{
									var img = new Image();
									img.src = dataUrl;
									img.onload = function() {
										label.image = img;
										label._loading = false;
										self.updateMap();
									}
									img.onerror = function() {
										label._loading = false;
										console.warn("Failed to load image: " + filePath);
										//TODO: delete the label? placeholder? something?
									}
								}
								else
								{
									console.warn("Failed to load image: " + filePath+': '+err);
									label._loading = false;
									//TODO: delete the label? placeholder? something?
								}
							}, false);
						}
					} else {
						var baseFontSize = label.fontSize || 10;
						var fontSize = Math.max(5, baseFontSize * scale);
						ctx.fillStyle = label.bgColor;
						ctx.fillRect(
							x - label.text.length * fontSize / 4,
							y - fontSize / 2,
							label.text.length * fontSize / 2,
							fontSize
						);
						ctx.fillStyle = label.fgColor;
						ctx.font = fontSize+'px '+(label.fontName || 'Arial');
						ctx.fillText(label.text, x, y);
					}
				}
			}
		}
		if(currentRoom.areaId === areaId && currentRoom.z === zLevel) 
		{
			var x = offsetX + currentRoom.x * tileSize * SpacingRatio;
			var y = offsetY + currentRoom.y * tileSize * SpacingRatio;
			ctx.beginPath();
			ctx.arc(x, y, tileSize / 8, 0, 2 * Math.PI);
			ctx.strokeStyle = "red";
			ctx.lineWidth = Math.max(0.5, tileSize / 25);
			ctx.stroke();
		}
		ctx.restore();
	};
}