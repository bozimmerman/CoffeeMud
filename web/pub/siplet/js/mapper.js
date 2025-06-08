// uses SortArray from util.js
// uses window.sipfs in loadJsonMap and saveJsonMap from filesys.js


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
	south: 6,
	east: 4,
	west: 5,
	up: 9,
	down: 10,
	in: 11,
	out: 12,
	northeast: 2,
	northwest: 3,
	southeast: 7,
	southwest: 8,
	northup: 13,
	northdown: 16,
	southup: 15,
	southdown: 14,
	eastup: 17,
	eastdown: 20,
	westup: 19,
	westdown: 18
};

window.DirNumCodes = [
	'', 'n', 'ne', 'nw', 'east', 'west', 
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
	272: { r:47, g:79, b:79, a:255 },
}

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
	// this.userData = undefined;
	if(args)
	{
		for(var key in args)
			this[key] = args[key]
	}
	this.getExitDir = function(dir)
	{
		if(dir !== 'special')
			dir = GetDirCode(dir);
		if(!dir)
			return null;
		for(var i=0;i<this.exits.length;i++)
			if(this.exits[i].dir == dir)
				return this.exits[i];
		return null;
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

window.MapBlock = {
	areas: {},
	nextAreaId: 1,
	rooms: {},
	nextRoomId: 1,
	envs: {},
	nextEnvId: 1,
	charDefault: ' ',
	colorDefault: [0,0,0],
	roomIdHash: {}
};

function Map(sipwin)
{
	var SpacingRatio = 0.4;
	var self = this;

	this.centerView = null;
	this.deleteMap = function() {
		for(var k in window.MapBlock)
		{
			var value = window.MapBlock[k];
			this[k] = JSON.parse(JSON.stringify(value));
		}
	};
	this.deleteMap();
	
	this.findAreaId = function(areaName) {
		for(var k in this.areas)
			if(this.areas[k].name == areaName)
				return k;
		return null;
	}
	
	this.findRoomId = function(roomName) {
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
	this.addCustomLine = function(roomId, idOrTo, direction, style, color, arrow) {}; //TODO:
	this.addMapEvent = function(uniquename, eventName, parent, displayName, arguments) {}; //TODO:
	this.addMapMenu = function(uniquename, parent, displayName) {}; //TODO:
	this.addRoom = function(roomId, areaId) {
		if((roomId in this.rooms) || !(areaId in this.areas))
			return null;
		var room = new MapRoom({
			areaId: areaId
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
	this.auditAreas = function() {}; //TODO:
	this.autoLayout = function(areaId, startRoomId) {} //TODO:
	this.centerview = function(roomId) {
		if(roomId in this.rooms)
			this.centerView = roomId;
	};
	this.clearAreaUserData = function(areaId) {
		if(areaId in this.areas && this.areas[areaId].userData) {
			delete this.areas[areaId].userData;
			return true;
		}
		return false;
	};
	this.clearAreaUserDataItem = function(areaId, key) {
		if(key && areaId in this.areas && this.areas[areaId].userData) {
			var area = this.areas[areaId];
			if(key in area.userData) {
				delete area.userData[key];
				return true;
			}
		}
		return false;
	};
	this.clearMapSelection = function() {}; //TODO
	this.clearMapUserData = function() {
		if(this.userData) {
			delete this.userData;
			return true;
		}
		return false;
	};
	this.clearMapUserDataItem = function(key) {
		if(this.userData && key) {
			if(key in this.userData) {
				delete this.userData[key];
				return true;
			}
		}
		return false;
	};
	this.clearRoomUserData = function(roomId) {
		if(roomId in this.rooms && this.rooms[roomId].userData) {
			delete this.rooms[roomId].userData;
			return true;
		}
		return false;
	};
	this.clearRoomUserDataItem = function(roomId, key) {
		if(key && roomId in this.rooms && this.rooms[roomId].userData) {
			var room = this.rooms[roomId];
			if(key in room.userData) {
				delete room.userData[key];
				return true;
			}
		}
		return false;
	};
	this.clearSpecialExits = function(roomId) {
		if(roomId in this.rooms && this.rooms[roomId].exits) {
			var room = this.rooms[roomId];
			var exit = room.getExitDir('special');
			while(exit != null)
			{
				var x = room.exits.indexOf(exit);
				if(x<0) break;
				room.exits.splice(x,1);
				exit = room.getExitDir('special');
			}
		}
	};
	this.closeMapWidget = function() {}; //TODO
	
	this.connectExitStub = function(fromId, toId, direction) {
		if (!(fromId in this.rooms) || !this.rooms[fromId].exits)
			return false;
		var fromR = this.rooms[fromId];
		if (direction === undefined) {
			if (toId in this.rooms) {
				var toR = this.rooms[toId];
				var dx = toR.x - fromR.x;
				var dy = toR.y - fromR.y;
				var dz = toR.z - fromR.z;
				if (dx === 0 && dy === 0 && dz === 0)
					return false;
				var dir;
				for (var d in window.DirCodeDeltas) {
					var delta = window.DirCodeDeltas[d];
					if (delta.x === dx && delta.y === dy && delta.z === dz) {
						dir = d;
						break;
					}
				}
				if (!dir)
					return false;
				var existingExit = fromR.getExitDir(dir);
				if (existingExit && existingExit.roomId === toId)
					return true;
				if (existingExit)
					return false;
				var exit = new MapExit({
					roomId: toId,
					dir: dir
				});
				fromR.exits.push(exit);
				return true;
			} else {
				direction = GetDirCode(toId);
				if (!direction) {
					return false;
				}
				toId = null;
			}
		} else {
			direction = GetDirCode(direction);
			if (!direction) {
				return false;
			}
		}
	
		if (toId in this.rooms) {
			var existingExit = fromR.getExitDir(direction);
			if (existingExit) {
				if (existingExit.roomId === toId)
					return true;
				fromR.exits.splice(fromR.exits.indexOf(existingExit), 1);
			}
			var exit = new MapExit({
				roomId: toId,
				dir: direction
			});
			fromR.exits.push(exit);
			return true;
		} else if (direction && !toId) {
			var oppositeDir = window.OpDirCodes[direction];
			if (!oppositeDir)
				return false;
			var delta = window.DirCodeDeltas[direction];
			if (!delta)
				return false; // Invalid direction
			var existingExit = fromR.getExitDir(direction);
			if (existingExit !== null)
				return false;
			var expectedX = fromR.x + delta.x;
			var expectedY = fromR.y + delta.y;
			var expectedZ = fromR.z + delta.z;
			for (var id in this.rooms) {
				var room = this.rooms[id];
				if (room.x === expectedX && room.y === expectedY && room.z === expectedZ &&
					room.exits && room.getExitDir(oppositeDir) && room.getExitDir(oppositeDir).roomId === '') {
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
								   backgroundTransparency, temporary) {}; //TODO:
	this.createMapImageLabel = function(areaId, filePath, posx, posy, posz, width, height, zoom, showOnTop, temporary) {}; //TODO
	this.createMapper = function(x, y, width, height) {
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
			canvas.width = targetWidth;
			canvas.height = targetHeight;
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
			var titleBar = document.createElement('div');
			titleBar.style.height = "20px";
			titleBar.style.backgroundColor = "white";
			titleBar.style.color = "black";
			titleBar.style.fontSize = "14px";
			titleBar.style.padding = "2px 5px";
			titleBar.style.userSelect = "none";
			titleBar.textContent = "Map";
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
		}
		
		var ctx = canvas.getContext('2d');
		if (!ctx) {
			console.error("Failed to get 2D rendering context");
			return null;
		}
		var isPanning = false;
		var panStartX, panStartY, panOffsetX, panOffsetY;
		var mouseDownHandler = function(event) {
			if (!self.mapWidget.layout || !self.mapWidget.layout.rooms)
				return;
			var rect = canvas.getBoundingClientRect();
			var mouseX = event.clientX - rect.left;
			var mouseY = event.clientY - rect.top;
			var { rooms } = self.mapWidget.layout;
			var overNode = false;
			for (var roomId in rooms) {
				var { centerX, centerY, radius } = rooms[roomId];
				var dx = mouseX - centerX;
				var dy = mouseY - centerY;
				var dist = Math.sqrt(dx * dx + dy * dy);
				if (dist < radius) {
					overNode = true;
					break;
				}
			}
			if (!overNode) {
				isPanning = true;
				panStartX = event.clientX;
				panStartY = event.clientY;
				panOffsetX = self.mapWidget.layout.offsetX || (canvas.width / 2 - self.rooms[self.centerView].x * self.mapWidget.layout.tileSize * SpacingRatio);
				panOffsetY = self.mapWidget.layout.offsetY || (canvas.height / 2 - self.rooms[self.centerView].y * self.mapWidget.layout.tileSize * SpacingRatio);
				canvas.style.cursor = "grabbing";
			}
		};
		var mouseMoveHandler = function(event) {
			if (!self.mapWidget.layout || !self.mapWidget.layout.rooms) {
				canvas.style.cursor = "default";
				return;
			}
			var rect = canvas.getBoundingClientRect();
			var mouseX = event.clientX - rect.left;
			var mouseY = event.clientY - rect.top;
			var { rooms } = self.mapWidget.layout;
			var overNode = false;
			for (var roomId in rooms) {
				var { centerX, centerY, radius } = rooms[roomId];
				var dx = mouseX - centerX;
				var dy = mouseY - centerY;
				var dist = Math.sqrt(dx * dx + dy * dy);
				if (dist < radius) {
					overNode = true;
					break;
				}
			}
			if (isPanning) {
				var dx = event.clientX - panStartX;
				var dy = event.clientY - panStartY;
				self.mapWidget.layout.offsetX = panOffsetX + dx;
				self.mapWidget.layout.offsetY = panOffsetY + dy;
				self.updateMap();
				canvas.style.cursor = "grabbing";
			} else {
				canvas.style.cursor = overNode ? "pointer" : "grab";
			}
		};
		var mouseUpHandler = function(e) {
			if (isPanning) {
				isPanning = false;
				canvas.style.cursor = "grab";
			}
		};
		var clickHandler = function(event) {
			if (!self.mapWidget.layout || !self.mapWidget.layout.rooms) {
				console.error("Click: No layout or rooms data");
				return;
			}
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
				self.centerView = closestRoomId;
				event.stopPropagation();
				self.updateMap();
			}
		};
		var zoomHandler = function(event) {
			event.preventDefault();
			var delta = event.deltaY < 0 ? 0.1 : -0.1;
			self.zoom = Math.max(0.5, Math.min(2.0, (self.zoom || 1.0) + delta));
			self.updateMap();
		};
		canvas.addEventListener('mousedown', mouseDownHandler);
		canvas.addEventListener('mousemove', mouseMoveHandler);
		document.addEventListener('mouseup', mouseUpHandler);
		canvas.addEventListener('click', clickHandler);
		canvas.addEventListener('wheel', zoomHandler);
		this.mapWidget = {
			canvas: canvas,
			ctx: ctx,
			x: typeof x === 'string' ? 0 : (x || 0),
			y: typeof x === 'string' ? 0 : (y || 0),
			width: targetWidth,
			height: targetHeight,
			frame: typeof x === 'string' ? x : null,
			zoomHandler: zoomHandler,
			clickHandler: clickHandler,
			mouseDownHandler: mouseDownHandler,
			mouseMoveHandler: mouseMoveHandler,
			mouseUpHandler: mouseUpHandler
		};
		this.zoom = 1.0;
		this.centerView = this.centerView || Object.keys(this.rooms)[0] || null;
		this.updateMap();
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
			delete this.rooms[k];
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
		if(roomId in this.rooms)
		{
			delete this.rooms[roomId];
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
			return true;
		}
		return false;
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
		if(!(roomId in this.rooms))
			return null;
		if(this.rooms[roomId].userData)
			return this.rooms[roomId].userData;
		return {};
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
	this.getCustomLines = function() {}; //TODO
	this.getCustomLines1 = function() {}; //TODO
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
	this.getMapEvents = function() {}; //TODO:
	this.getMapLabel = function() {};  //TODO:
	this.getMapLabels = function() {}; //TODO:
	this.getMapMenus = function() {}; //TODO:
	this.getMapSelection = function() {}; //TODO:
	this.getMapUserData = function(key) {
		if(!key || (!this.userData))
			return null;
		if(key in this.userData)
			return this.userData[key];
		return null;
	};
	this.getMapZoom = function() {}; //TODO:
	this.getPath = function() {}; //TODO:
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
			return null;
		var room = this.rooms[roomId];
		return {x:room.x,y:room.y,z:room.z};
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
	this.gotoRoom = function() {}; //TODO:
	this.hasExitLock = function(roomId, direction) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		if(!room.exits)
			return false;
		direction = GetDirCode(direction);
		if(!direction)
			return false;
		var ex = room.getExitDir(direction);
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
	this.highlightRoom = function(roomId, color1Red, color1Green, color1Blue, color2Red, color2Green, color2Blue, highlightRadius, color1Alpha, color2Alpha) {}; //TODO:
	this.killMapInfo = function() {}; //TODO:
	this.loadJsonMap = function(pathFileName) {
		var self = this;
		window.sipfs.load(pathFileName,function(err, fdata) {
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
		var ex = room.getExitDir(direction);
		if(ex == null)
			return;
		ex.blocked = lockIfTrue;
	};
	this.lockRoom = function(roomId, lockIfTrue) {
		if(!(roomId in this.rooms))
			return;
		var room = this.rooms[roomId];
		room.blocked = lockIfTrue;
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
	this.moveMapWidget = function(Xpos, Ypos) {}; //TODO:
	this.openMapWidget = function(Xpos, Ypos, width, height) {}; //TODO: XPos == dockingArea
	this.pauseSpeedwalk = function() {}; //TODO:
	this.registerMapInfo = function(label, func) {}; //TODO:
	this.resumeSpeedwalk = function() {}; //TODO:
	this.removeCustomLine = function() {}; //TODO:
	this.removeMapEvent = function() {}; //TODO:
	this.removeMapMenu = function() {}; //TODO:
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
	this.resizeMapWidget = function() {}; //TODO
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
			doc[k] = this[k];
		doc = JSON.stringify(doc);
		window.sipfs.save(pathFileName, doc, function(e){});
	};
	this.saveMap = this.saveJsonMap;
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
		var exit = room.getExitDir(dirCode);
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
		var fexit = fromRoom.getExitDir(direction);
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
		var exit = room.getExitDir(direction);
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
			exit = room.getExitDir(dir);
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
	this.setMapZoom = function(zoom, areaId) {}; //TODO:
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
					areaId = this.createAreaId();
					this.areas[areaId] = new MapArea({
						name: name
					});
					this.rooms[roomId].areaId = areaId;
				} 
			}
		}
	}
	this.setRoomChar = function(roomId, char) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		room.char = char;
		return true;
	};
	this.setRoomCharColor = function(roomId, color) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		room.color = color;
		return true;
	};
	this.setRoomCoordinates = function(roomId, x, y, z) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		room.x = x;
		room.y = y;
		room.z = z;
		return true;
	};
	this.setRoomEnv = function(roomId, envId) {
		if(!(roomId in this.rooms))
			return false;
		this.rooms[roomId].envId = envId;
		return true;
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
		if(!(roomId in this.rooms))
			return false;
		this.rooms[roomId].name = name;
		return true;
	};
	this.setRoomUserData = function(roomId, key, value) {
		if(!key || !(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		if(!room.userData)
			room.userData = {};
		if(key in room.userData)
			delete room.userData[key];
		if(value != null)
			room.userData[key] = value;
		return true;
	};
	this.setRoomWeight = function(roomId, weight) {
		if(!(roomId in this.rooms))
			return false;
		this.rooms[roomId].weight = weight;
		return true;
	};
	this.speedwalk = function(dirString, backwards, delay, show) {}; //TODO: pretty trivial.. does same mud does
	this.stopSpeedwalk = function() {}; //TODO:
	this.unHighlightRoom = function() {}; //TODO:
	this.unsetRoomCharColor = function(roomId) {
		if(!(roomId in this.rooms))
			return false;
		var room = this.rooms[roomId];
		room.color = null;
		return true;
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
		var areaId = currentRoom.areaId;
		var zLevel = currentRoom.z;
		var roomsToDraw = Object.keys(this.rooms).filter(function(id) {
			var room = self.rooms[id];
			return room.areaId === areaId && room.z === zLevel;
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
		var scale = this.zoom || 1.0;
		var baseTileSize = Math.min(canvas.width / mapWidth, canvas.height / mapHeight);
		var tileSize = baseTileSize * scale;
		tileSize = Math.max(tileSize, Math.min(canvas.width, canvas.height) / 25);
		var offsetX = this.mapWidget.layout && this.mapWidget.layout.offsetX !== undefined
			? this.mapWidget.layout.offsetX
			: canvas.width / 2 - currentRoom.x * tileSize * SpacingRatio;
		var offsetY = this.mapWidget.layout && this.mapWidget.layout.offsetY !== undefined
			? this.mapWidget.layout.offsetY
			: canvas.height / 2 - currentRoom.y * tileSize * SpacingRatio;
		this.mapWidget.layout = { tileSize, offsetX, offsetY, mapWidth, mapHeight, rooms: {} };
		roomsToDraw.forEach(function(roomId) {
			var room = self.rooms[roomId];
			var exits = self.getRoomExits(roomId);
			for (var dir in exits) {
				var toRoomId = exits[dir];
				if (toRoomId in self.rooms && self.rooms[toRoomId].z === zLevel) {
					var toRoom = self.rooms[toRoomId];
					var x1 = offsetX + room.x * tileSize * SpacingRatio;
					var y1 = offsetY + room.y * tileSize * SpacingRatio;
					var x2 = offsetX + toRoom.x * tileSize * SpacingRatio;
					var y2 = offsetY + toRoom.y * tileSize * SpacingRatio;
	
					var diameter = tileSize / 5;
					var dx = x2 - x1;
					var dy = y2 - y1;
					var dist = Math.sqrt(dx * dx + dy * dy);
					if (dist > diameter) {
						var midX = (x1 + x2) / 2;
						var midY = (y1 + y2) / 2;
						var scaleFactor = (diameter / 2) / dist;
						var newX1 = midX - dx * scaleFactor;
						var newY1 = midY - dy * scaleFactor;
						var newX2 = midX + dx * scaleFactor;
						var newY2 = midY + dy * scaleFactor;
	
						ctx.beginPath();
						ctx.moveTo(newX1, newY1);
						ctx.lineTo(newX2, newY2);
						ctx.strokeStyle = "#666";
						ctx.lineWidth = Math.max(0.5, tileSize / 25);
						ctx.stroke();
					}
				}
			}
		});
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
			var color = self.colorDefault;
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
			var char = self.getRoomChar(roomId);
			ctx.fillStyle = room.color ? `rgb(${room.color[0]}, ${room.color[1]}, ${room.color[2]})` : "#000";
			var fontSize = Math.max(5, tileSize / 6);
			ctx.font = fontSize + 'px Arial';
			ctx.fillText(char, x, y);
		});
		if (areaId in this.areas && this.areas[areaId].labels) {
			var labels = this.areas[areaId].labels;
			for (var labelId in labels) {
				var label = labels[labelId];
				if (label.z === zLevel) {
					var x = offsetX + label.x * tileSize * SpacingRatio;
					var y = offsetY + label.y * tileSize * SpacingRatio;
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
					ctx.font = `${fontSize}px ${label.fontName || 'Arial'}`;
					ctx.fillText(label.text, x, y);
				}
			}
		}
		var x = offsetX + currentRoom.x * tileSize * SpacingRatio;
		var y = offsetY + currentRoom.y * tileSize * SpacingRatio;
		ctx.beginPath();
		ctx.arc(x, y, tileSize / 8, 0, 2 * Math.PI);
		ctx.strokeStyle = "red";
		ctx.lineWidth = Math.max(0.5, tileSize / 25);
		ctx.stroke();
		ctx.restore();
	};
}