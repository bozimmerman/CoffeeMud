const mapper = win.mapper;

let cofudlet = win.cofudlet || {};
cofudlet.map = cofudlet.map || {};
const map = cofudlet.map;
var cleanUrl = window.url;
if(cleanUrl.indexOf('://')>0)
	cleanUrl = cleanUrl.split('://')[1];
if(cleanUrl.indexOf('/')>0)
	cleanUrl = cleanUrl.split('/')[0];
cleanUrl = cleanUrl.replace(/[^a-zA-Z0-9]/g,'_');
const mapPath = '/Mapper/'+cleanUrl+'.json';

map.terrainColors = 
{
	// Admire the colors at https://wiki.mudlet.org/w/Manual:Lua_Functions#setCustomEnvColor - they're also configurable by the end user.
	city: 263,
	woods: 258,
	rocky: 272,
	plains: 266,
	underwater: 260,
	air: 270,
	watersurface: 268,
	jungle: 262,
	swamp: 259,
	desert: 267,
	hills: 261,
	mountains: 269,
	spaceport: 271,
	seaport: 271,

	// indoors
	stone: 263,
	wooden: 259,
	cave: 272,
	magic: 269,
	in_underwater: 260,
	gap: 271,
	cavelakesurface: 268,
	metal: 272,
	innerseaport: 271,
	caveseaport: 271,
};

map.normalizedDirections = 
{
	N: 'north',
	E: 'east',
	S: 'south',
	W: 'west',
	U: 'up',
	D: 'down',
	n: 'north',
	e: 'east',
	s: 'south',
	w: 'west',
	u: 'up',
	d: 'down',
	north: 'north',
	east: 'east',
	south: 'south',
	west: 'west',
	up: 'up',
	down: 'down',
	nw: 'northwest',
	ne: 'northeast',
	sw: 'southwest',
	se: 'southeast',
	northwest: 'northwest',
	northeast: 'northeast',
	southwest: 'southwest',
	southeast: 'southeast',
};

map.shortDirections = 
{
	north: 'n',
	east: 'e',
	south: 's',
	west: 'w',
	up: 'u',
	down: 'd',
	northwest: 'nw',
	northeast: 'ne',
	southwest: 'sw',
	southeast: 'se',
};

function stripColors(str) 
{
	str = str.replace(/\^[MRGYBPCWkrgybpcw?]/g, "");
	return str.replace(/\^#\d{3}/g, "");
}

function trim(s) 
{
	const match = s.match(/^\s*(.*?)\s*$/);
	return match ? match[1] : "";
}

map.currentArea = function() 
{
	const areaName = gmcp.room.info.zone;
	return getAreaTable()[areaName];
};

map.isInstanced = function() 
{
	return !!String(gmcp.room.info.zone).match(/ of Mystery/);
};

const currentArea = () => map.currentArea();

// visitRoom returns null to continue search, or a value to signal the end of search (like path to desired room).
// visitExit returns true if we should stop BFSing after this exit.
function search(here, visitRoom, visitExit, bypassLocks, inArea, breadthFirst) 
{
	const area1 = mapper.getRoomArea(mapper.getPlayerRoom());
	const visited = {};
	const roomq = [];
	roomq.push(here);  // pushRight equivalent
	while (roomq.length > 0) 
	{
		const room = breadthFirst ? roomq.shift() : roomq.pop();  // popLeft = shift, popRight = pop
		if (visited[room] === undefined) 
		{
			const ex = mapper.getRoomExits(room);
			for (let [exDir, exTgt] of Object.entries(ex)) 
			{
				if ((bypassLocks || !hasExitLock(room, exDir)) && (!inArea || mapper.getRoomArea(exTgt) === -1 || area1 === mapper.getRoomArea(exTgt)) /*&& not isHardLocked(room, exDir) */) 
				{
					// TODO: add options to path through swim-only rooms, etc
					/*
					  if (true && ex[exDir].get('data', {}).get('swim')) {
							continue;
					  }
					  if (true && ex[exDir].get('data', {}).get('crawl')) {
							continue;
					  }
					  if (true && ex[exDir].get('data', {}).get('climb')) {
							continue;
					  }
					  if (true && ex[exDir].get('data', {}).get('fly')) {
							continue;
					  }
					*/
					if (!visitExit(room, exDir, exTgt))
						roomq.push(exTgt);
				}
			}
			const res = visitRoom(room);
			if (res !== null)
				return res;
			visited[room] = true;
		}
	}
}

map.bfs = function(here, visitRoom, visitExit, bypassLocks, inArea) 
{
	return search(here, visitRoom, visitExit, bypassLocks, inArea, true);
};

map.dfs = function(here, visitRoom, visitExit, bypassLocks, inArea) 
{
	return search(here, visitRoom, visitExit, bypassLocks, inArea, false);
};

function runifyDirs(directions) 
{
	let count = 1;
	let out = "";
	let first = true;
	for (let i = 1; i < directions.length; i++) 
	{  // 1-based, but JS arrays 0-based
		if (directions[i-1] === directions[i])
			count++;
		else 
		{
			if (first)
				first = false;
			else
				out += ' ';
			out += (count === 1 ? "" : count) + directions[i-1];
			count = 1;
		}
	}
	if(!first)
		out += ' ';
	out += (count === 1 ? "" : count) + directions[directions.length - 1];
	return out;
}

console.assert(runifyDirs(['e']) === 'e');
console.assert(runifyDirs(['e', 'e']) === '2e');
console.assert(runifyDirs(['n', 'e', 'e']) === 'n 2e');
console.assert(runifyDirs(['e', 'e', 'n']) === '2e n');

cofudlet.map.assemble = function(dirs, mode) 
{
	const directions = [];  // streak of non-special exits
	const out = [];
	for(let compoundDir of dirs) 
	{
		const splitDirs = compoundDir.split(';;');
		for (let dir of splitDirs) 
		{
			if(dir === 'down') 
				dir = 'd';
			else 
			if (dir === 'up') 
				dir = 'u';
			if(!map.normalizedDirections[dir]) 
			{
				if (directions.length > 0)
					out.push(mode + ' ' + runifyDirs(directions));
				out.push(dir);
				directions.length = 0;  // clear
			} 
			else
				directions.push(dir);
		}
	}
	if(directions.length > 0)
		out.push(mode + ' ' + runifyDirs(directions));
	return out.join(';;');
};

console.assert(cofudlet.map.assemble(['e'], 'go') === 'go e');
console.assert(cofudlet.map.assemble(['e', 'e'], 'go') === 'go 2e');
console.assert(cofudlet.map.assemble(['n', 'e', 'e'], 'go') === 'go n 2e');
console.assert(cofudlet.map.assemble(['e', 'e', 'n'], 'go') === 'go 2e n');

console.assert(cofudlet.map.assemble(['open e;;e', 'e'], 'go') === 'open e;;go 2e');
console.assert(cofudlet.map.assemble(['open e;;e', 'e', 'e'], 'go') === 'open e;;go 3e');
console.assert(cofudlet.map.assemble(['open e;;e', 'n', 'e', 'e'], 'go') === 'open e;;go e n 2e');
console.assert(cofudlet.map.assemble(['open e;;e', 'e', 'e', 'n'], 'go') === 'open e;;go 3e n');

console.assert(cofudlet.map.assemble(['e', 'open e;;e', 'n'], 'go') === 'go e;;open e;;go e n');
console.assert(cofudlet.map.assemble(['e', 'e', 'open e;;e', 'n'], 'go') === 'go 2e;;open e;;go e n');

// TODO: implement swim/crawl/fly/climb locks.
// TODO: implement level locks.
function doSpeedWalk() 
{
	const goCmd = map.goCmd || "go";
	send(cofudlet.map.assemble(speedWalkDir, goCmd));
}

cofudlet.map.oppositeDirections = 
{
	north: "south",
	east: "west",
	south: "north",
	west: "east",
	up: "down",
	down: "up",
	northwest: "southeast",
	northeast: "southwest",
	southwest: "northeast",
	southeast: "southwest",  // Note: Likely typo in original; should be "northwest"?
};

function targetToCid(there) 
{
	const bookmarks = JSON.parse(getMapUserData("bookmarks") || "{}");
	if (bookmarks[there]) 
	{
		there = bookmarks[there];
		console.info(`Map: going to bookmark ${parameter} Cid ${there}`);  // Assuming 'parameter' is a global or replace with 'there'
		return there;
	}

	let areaId = getAreaTable()[there];
	if(areaId) 
	{
		there = getAreaUserData(areaId, "startRoomCid");
		console.info(`Map: going to area ${parameter}, start room ${there}`);
		return there;
	}

	areaId = getAreaTable()[there.replace(/'/g, '`')];
	if(areaId) 
	{
		there = getAreaUserData(areaId, "startRoomCid");
		console.info(`Map: going to area ${parameter}, start room ${there}`);
		return there;
	}

	if(!isNaN(there)) 
	{
		const cid = mapper.getRoomIDbyHash(there);
		if((cid != -1) && (!mapper.roomExists(cid)))
			cid = -1;
		if(cid !== -1) 
		{
			console.info(`Map: going to ${mapper.getRoomUserData(cid, 'id')} ${mapper.getRoomName(cid)}`);
			return cid;
		}

		const num = mapper.getRoomHashByID(there);
		if(num) 
		{
			console.info(`Map: going to ${mapper.getRoomUserData(there, 'id')} ${mapper.getRoomName(there)}`);
			return there;
		}
	}

	if(there.match(/.+#\d+$/)) 
	{
		const areaName = there.replace(/#\d+$/, "");
		areaId = getAreaTable()[areaName];
		const areaRooms = getAreaRooms(areaId);
		for (let cid in areaRooms) 
		{
			if(mapper.getRoomUserData(cid, "id") === there) 
			{
				console.info(`Map: going to area ${areaId}, ${areaName} room ${there}`);
				return cid;
			}
		}
		console.info(`Map: couldn't find room ${there} in area ${areaId} ${areaName}`);
	}

	const allRooms = mapper.getRooms();  // Assuming {cid: name}
	const exactMatches = [];
	const upperThere = there.toUpperCase();
	for(let [cid, rname] of Object.entries(allRooms)) 
	{
		if (rname.toUpperCase().replace(/`/g, "'") === upperThere.replace(/`/g, "'"))
			exactMatches.push(cid);
	}

	if(exactMatches.length === 1) 
	{
		console.info(`Map: going to room ${there}, Cid ${exactMatches[0]}`);
		return exactMatches[0];
	} 
	else 
	if(exactMatches.length > 1) 
	{
		console.info(`Map: TODO implement room selection. Meanwhile, \`map go\` to one of these rooms:`);
		console.console.info(exactMatches);
		return;
	}

	const fuzzyMatches = [];
	for(let [cid, rname] of Object.entries(allRooms)) 
	{
		const gsubbedName = rname.toUpperCase().replace(/`/g, "'");
		const gsubbedThere = upperThere.replace(/`/g, "'");
		if (gsubbedName.includes(gsubbedThere))
			fuzzyMatches.push({cid: cid, name: rname});
	}
	if(fuzzyMatches.length === 1) 
	{
		console.info(`Map: going to room ${fuzzyMatches[0].cid} ${fuzzyMatches[0].name}`);
		return fuzzyMatches[0].cid;
	} 
	else 
	if(fuzzyMatches.length > 1) 
	{
		console.info(`Map: TODO implement room selection. Meanwhile, \`map go\` to one of these rooms:`);
		console.console.info(fuzzyMatches);
		return;
	}
	console.info(`Map: Room not found: ${parameter}`);
}

function goImpl(dest) 
{
	if(!dest) 
		return;
	if(getPlayerRoom() === parseInt(dest))
		console.info("Map: already there!");
	else 
	{
		const [ok, err] = gotoRoom(dest);  // Assuming gotoRoom returns [bool, string]
		if (!ok)
			console.info(`Map: ${err}`);
	}
}

// As a weird side effect, these aliases also change what double-click on the graphical map does.
cofudlet.map.go = function(to) 
{
	map.goCmd = "go";
	goImpl(targetToCid(to));
};

cofudlet.map.run = function(to) 
{
	map.goCmd = "run";
	goImpl(targetToCid(to));
};

cofudlet.map.path = function(to) 
{
	const toCid = targetToCid(to);
	if (getPath(getPlayerRoom(), toCid))
		console.info(cofudlet.map.assemble(speedWalkDir, 'go'));
	else
		console.info(`No path found from ${getPlayerRoom()} to ${to}`);
};

cofudlet.map.cover = function(noSwimNoFly, excludedRooms) 
{
	const here = mapper.getPlayerRoom();
	const area1 = mapper.getRoomArea(mapper.getPlayerRoom());
	const roomsList = [];
	const roomsSet = {};

	function visitExit(room, exDir, tgt) 
	{
		return (excludedRooms || {})[tgt];
	}

	function visitRoom(room) 
	{
		if (!roomsSet[room]) 
		{
			const terrain = mapper.getRoomUserData(room, "terrain");
			if (noSwimNoFly && (terrain === "underwater" || terrain === "watersurface" || terrain === "in_underwater" || terrain === "cavelakesurface"
				|| terrain === "air" || terrain === "gap"))
				return;
			roomsSet[room] = true;
			roomsList.push(room);
		}
	}
	map.dfs(here, visitRoom, visitExit, false, true);
	return roomsList;
};

var gmcp = {};

function updateCoords(coords, direction) 
{
	switch(direction)
	{
		case "north": coords.y -= 1; break;
		case "east": coords.x += 1; break;
		case "south": coords.y += 1; break;
		case "west": coords.x -= 1; break;
		case "up": coords.z += 1; break;
		case "down": coords.z -= 1; break;
		case "northeast": coords.y -= 1; coords.x += 1; break;
		case "northwest": coords.y -= 1; coords.x -= 1; break;
		case "southeast": coords.y += 1; coords.x += 1; break;
		case "southwest": coords.y += 1; coords.x -= 1; break;
		default: return null;
	}
	return coords;
}

function getDirectionBetweenRooms(fromId, toId) 
{
	const exits = mapper.getRoomExits(fromId);
	for (let [dir, tgt] of Object.entries(exits)) 
	{
		if (tgt === toId)
			return dir;
	}
}

map.setCoords = function(fromId, toId, dir) 
{
	let prevCoords = {x: 0, y: 0, z: 0};
	[prevCoords.x, prevCoords.y, prevCoords.z] = mapper.getRoomCoordinates(fromId);
	// console.info(`Room prevCoords: ${prevCoords.x} ${prevCoords.y} ${prevCoords.z}`)
	const nDir = map.normalizedDirections[String(dir).toLowerCase()];
	let coords = updateCoords({...prevCoords}, nDir);
	// console.info(`Map debug: checking for collisions at area ${getRoomArea(fromId)}, room ${toId}: ${coords.x} ${coords.y} ${coords.z}`)
	let collisions = mapper.getRoomsByPosition(mapper.getRoomArea(fromId), coords.x, coords.y, coords.z);
	// idempotency
	for (let rid of collisions) 
	{
		if (rid === toId)
			return;
	}

	if (collisions.length > 0) 
	{
		let direction = getDirectionBetweenRooms(fromId, toId);
		console.info(`Map: Collision detected. Shifting rooms to fit this one. Direction: ${direction}`);
		const areaRooms = mapper.getAreaRooms(mapper.getRoomArea(fromId));
		for (let cid in areaRooms) 
		{
			if (cid !== fromId) 
			{
				let [shiftX, shiftY, shiftZ] = mapper.getRoomCoordinates(cid);
				if (direction === 'down' && shiftZ <= coords.z) 
					mapper.setRoomCoordinates(cid, shiftX, shiftY, shiftZ - 1);
				else 
				if(direction === 'up' && shiftX >= coords.z) 
					mapper.setRoomCoordinates(cid, shiftX, shiftY, shiftZ + 1);
				else 
				if(shiftZ === coords.z) 
				{
					if(direction === 'north' && shiftY >= coords.y) 
						mapper.setRoomCoordinates(cid, shiftX, shiftY + 1, shiftZ);
					else 
					if(direction === 'south' && shiftY <= coords.y) 
						mapper.setRoomCoordinates(cid, shiftX, shiftY - 1, shiftZ);
					else 
					if(direction === 'west' && shiftX <= coords.x) 
						mapper.setRoomCoordinates(cid, shiftX - 1, shiftY, shiftZ);
					else 
					if(direction === 'east' && shiftX >= coords.x) 
						mapper.setRoomCoordinates(cid, shiftX + 1, shiftY, shiftZ);
				}
			}
		}
	}
	// console.info(`Room ${toId} coords: ${coords.x} ${coords.y} ${coords.z}`)
	mapper.setRoomCoordinates(toId, coords.x, coords.y, coords.z);
}

function abortMapping() 
{
	map.mapping = false;
}

function maybeAddArea(startRoom) 
{
	const areaName = gmcp.room.info.zone;
	let area = mapper.getAreaTable()[areaName];
	if (area === undefined) 
	{
		area = mapper.addAreaName(areaName);
		if (startRoom !== -1)
			mapper.getAreaUserData(area, "startRoomCid", startRoom);
		console.info(`Map: New area ${area}: ${areaName}, startRoom=${startRoom}`);
		return [area, true];
	}
	return [area, false];
}

function getHighestCoord(areaId) 
{
	const rooms = mapper.getAreaRooms(areaId);
	let [_, __, maxZ] = mapper.getRoomCoordinates(rooms[0]);
	for (let cid in rooms) 
	{
		let [x, y, z] = mapper.getRoomCoordinates(cid);
		if (z > maxZ)
			maxZ = z;
	}
	return maxZ;
}

// Mazes
// ~~~~~
//
// There are at least 4 kinds of mazes:
// 1. fully connected blocks of rooms (every room, except edges, has 4 doors leading to all adjacent rooms): Goblin Mountains#24#(x, y)
// 2. sparsely connected rooms (rooms are connected to adjacent rooms, but it's more difficult to find a way through corridors): Orthindar#214#(x, y), Medley Orchard's storyquest mazes
// For all cases, track exits - if exits have changed at all (which will never trigger for case 1), then remove & rediscover all exits in the maze.
const mazePattern = /^[^#]+#(\d+)#\((\d+),(\d+)\)$/;
function isMaze(roomID) 
{
	const match = String(roomID).match(mazePattern);
	if (match)
		return [match[1], parseInt(match[2]), parseInt(match[3])];
}

// lua for _, cid in pairs(getAreaRooms(getRoomArea(getPlayerRoom()))) do local id = getRoomUserData(cid, "id"); console.info(f"{cid} {id}") end
function tryCalculateMazeCoordinates(roomID, exitID) 
{
	// CoffeeMUD mazes are described with just one room ID + coords: Sewers#7019#(9,5)
	let [_, __, thisX, thisY] = String(roomID).match(/^[^#]+#\d+#\((\d+),(\d+)\)$/ || []) || [null, null, null, null];
	// console.info("TODO calculate mazy coordinates")
	return !!String(roomID).match(/^[^#]+#\d+#\(\d+,\d+\)$/);
}

// Incomplete. It might happen that we walk through half the maze, which randomly happens to have the same
// layout like last time, until finally hitting a room which we retain, but delete the rest. Instead,
// we should also retain the path traversed so far - yet that's too much of a hassle.
function deleteAllExitsFromThisMazeExcept(cid, niceId) 
{
	const result = isMaze(niceId);
	if (!result) 
		return;
	const [mazeId, x, y] = result;
	const areaRooms = mapper.getAreaRooms(mapper.getRoomArea(cid));
	for(let rid in areaRooms) 
	{
		const niceId2 = mapper.getRoomUserData(rid, "id");
		const result2 = isMaze(niceId2);
		if(result2) 
		{
			const [mazeId2, x2, y2] = result2;
			if(mazeId2 === mazeId && rid !== cid) 
			{
				console.info(`Removing exits from maze: ${niceId2}`);
				const exits = mapper.getRoomExits(rid);
				for (let dir in exits)
					mapper.setExit(rid, -1, dir);
			}
		}
	}
}

// TODO: split and clean up
function onGmcpRoomInfo() 
{
	const num = gmcp.room.info.num;
	let roomID = mapper.getRoomIdbyHash(num);
	if ((roomID != -1) && (!mapper.roomExists(roomID)))
		roomID = -1;

	const niceId = gmcp.room.info.id;

	let [areaId, newArea] = maybeAddArea(roomID);
	if(roomID === -1 && !newArea) 
	{
		console.info(`Map: teleported. Resetting coordinates to highest+100.`);
		let z = getHighestCoord(areaId);
		roomID = mapper.createRoomId();
		mapper.addRoom(roomID, areaId);
		mapper.setRoomIdByHash(roomID, num);

		let positioned = false;
		for(let [exitDir, targetNum] of Object.entries(gmcp.room.info.exits)) 
		{
			let targetID = mapper.getRoomIdbyHash(targetNum);
			if(targetID != -1 && mapper.roomExists(targetID)) 
			{
				const nDir = map.normalizedDirections[String(exitDir).toLowerCase()];
				const oppDir = cofudlet.map.oppositeDirections[nDir];
				if(oppDir) 
				{  // cardinal/reversible
					let prevCoords = {x: 0, y: 0, z: 0};
					[prevCoords.x, prevCoords.y, prevCoords.z] = mapper.getRoomCoordinates(targetID);
					let coords = updateCoords({...prevCoords}, oppDir);
					let collisions = mapper.getRoomsByPosition(areaId, coords.x, coords.y, coords.z);
					if(collisions.length > 0) 
					{
						console.info(`Map: Collision detected on reverse. Shifting rooms to fit this one. Direction: ${oppDir}`);
						const areaRooms = mapper.getAreaRooms(areaId);
						for (let cid of areaRooms) 
						{
							if (cid !== roomID) 
							{
								let [shiftX, shiftY, shiftZ] = mapper.getRoomCoordinates(cid);
								if (oppDir === 'down' && shiftZ <= coords.z) 
									mapper.setRoomCoordinates(cid, shiftX, shiftY, shiftZ - 1);
								else 
								if(oppDir === 'up' && shiftZ >= coords.z) 
									mapper.setRoomCoordinates(cid, shiftX, shiftY, shiftZ + 1);
								else 
								if(shiftZ === coords.z) {
									if(oppDir === 'north' && shiftY >= coords.y) 
										mapper.setRoomCoordinates(cid, shiftX, shiftY + 1, shiftZ);
									else 
									if(oppDir === 'south' && shiftY <= coords.y) 
										mapper.setRoomCoordinates(cid, shiftX, shiftY - 1, shiftZ);
									else 
									if(oppDir === 'west' && shiftX <= coords.x) 
										mapper.setRoomCoordinates(cid, shiftX - 1, shiftY, shiftZ);
									else 
									if(oppDir === 'east' && shiftX >= coords.x) 
										mapper.setRoomCoordinates(cid, shiftX + 1, shiftY, shiftZ);
								}
							}
						}
					}
					mapper.setRoomCoordinates(roomID, coords.x, coords.y, coords.z);
					positioned = true;
					break;  // use first reverse found
				}
			}
		}
		if (!positioned)
			mapper.setRoomCoordinates(roomID, 0, 0, z + 100);
	} 
	if(roomID === -1 && newArea) 
	{
		roomID = mapper.createRoomId();
		mapper.addRoom(roomID, areaId);
		mapper.setRoomIdByHash(roomID, num);
		mapper.setAreaUserData(areaId, "startRoomCid", roomID);
		console.info(`Map: backfilled missing area ${areaId} ${gmcp.room.info.zone} start room to ${roomID}`);
	}

	if(newArea)
		mapper.setRoomCoordinates(roomID, 0, 0, 0);

	// Normally this would go into the "Things that never change" section, but my imported maps lack IDs :(
	// TODO: map all the IDs, then move this line there.
	mapper.setRoomUserData(roomID, "id", niceId);

	// TODO: estimate move cost by moves lost (what about flight, mounts, etc?)

	// Things that might change: when you visit a previously hinted-at room,
	// its exits get populated
	for(let [exitDir, targetNum] of Object.entries(gmcp.room.info.exits)) 
	{
		let exitID = mapper.getRoomIdbyHash(targetNum);
		if ((exitID != -1) && (!mapper.roomExists(exitID)))
			exitID = -1;
		// console.info(`Map: exit stub towards ${exitDir}`)

		if(exitID === -1) 
		{
			exitID = mapper.createRoomId();
			mapper.addRoom(exitID, areaId);
			//setTimeout(function() { raiseEvent('cofudlet.onNewRoom', exitID); }, 0);
			mapper.setRoomIdByHash(exitID, targetNum);
			if (exitDir === "V")
				mapper.addSpecialExit(roomID, exitID, "enter there");
			else
				mapper.setExit(roomID, exitID, exitDir);

			map.setCoords(roomID, exitID, exitDir);
			mapper.setRoomArea(exitID, areaId);
		} 
		else 
		{
			// console.info(`Not populating already seen exit from ${roomID} towards ${exitDir} to ${exitID}, coords from ${coords(roomID)} to ${coords(exitID)}`)
			// tryCalculateMazeCoordinates(roomID, exitID)
			// Partially connected mazes lose some exits every now and then. If exits disappear for this room, remove them for the whole maze - it's probably better to rediscover the whole maze rather than speedwalk through missing exits.
			// CoffeeMUD's exits don't normally disappear, not even secret exits.
			// This means we can just prune any missing exits regardless of whether they're mazy.
			let found = false;
			const normalizedDir = map.normalizedDirections[exitDir];
			const roomExits = mapper.getRoomExits(roomID);
			for(let [mapDir, mapTgt] of Object.entries(roomExits)) 
			{
				if(map.normalizedDirections[mapDir] === normalizedDir) 
				{
					found = true;
					if (mapTgt !== exitID) 
					{
						console.info(`Exit from ${roomID} ${mapDir}ward to ${mapTgt} reconnected to ${exitID}`);
						mapper.setExit(roomID, exitID, mapDir);
					}
					break;
				}
			}
			if(!found) 
			{
				if (mapper.getRoomUserData(roomID, "terrain") !== "")
					console.info(`Existing room got a new exit from ${roomID} ${exitDir}ward to ${exitID}`);
				mapper.setExit(roomID, exitID, exitDir);
			}
		}
	}

	// Prune disappeared exits
	const roomExits = mapper.getRoomExits(roomID);
	for(let [mapDir, mapTgt] of Object.entries(roomExits)) 
	{
		let found = false;
		for(let [exitDir, targetNum] of Object.entries(gmcp.room.info.exits)) 
		{
			const normalizedExDir = map.normalizedDirections[exitDir];
			if (normalizedExDir === map.normalizedDirections[mapDir])
				found = true;
		}
		if(!found) 
		{
			console.info("no match");
			console.info(`Exit from ${roomID} ${mapDir}wards to ${mapTgt} disappeared, removing`);
			mapper.setExit(roomID, -1, mapDir);

			deleteAllExitsFromThisMazeExcept(roomID, niceId);
		}
	}

	// Things that never change
	if (!mapper.getRoomUserData(roomID, "terrain")) 
	{
		// 1st visit to the room populates this
		const roomName = stripColors(gmcp.room.info.name);
		console.info(`Map: new room ${roomID}: ${niceId}: ${roomName}`);
		mapper.setRoomName(roomID, roomName);
		mapper.setRoomArea(roomID, areaId);
		const terrain = gmcp.room.info.terrain;
		mapper.setRoomUserData(roomID, "terrain", terrain);
		if (terrain === "underwater" || terrain === "watersurface" || terrain === "in_underwater" || terrain === "cavelakesurface")
			mapper.setRoomWeight(roomID, 10);
		else
		if (terrain === "air" || terrain === "gap")
			mapper.setRoomWeight(roomID, 20);

		const color = cofudlet.map.terrainColors[gmcp.room.info.terrain];
		if (color !== undefined)  // Might want to consider adding new ones dynamically
			mapper.setRoomEnv(roomID, color);
		else
			console.info(`Previously unseen terrain, need to add color: ${gmcp.room.info.terrain}`);

		// Rooms in randomly connected mazes don't get placed correctly in the
		// 1st try - walk S of (0,0) and we expect to end up at (0,1), but
		// could really go anywhere.
		// Upon entering a new maze room for the 1st time, readjust its coordinates
	}

	map.lastRoom = map.thisRoom;
	map.thisRoom = roomID;
	mapper.centerview(roomID);
}

function isLocked(room, direction) 
{
	const rdSerialized = mapper.getRoomUserData(room, "exitLocks");
	if (!rdSerialized)
		return false;
	const rd = JSON.parse(rdSerialized); // Assuming yajl.to_value is equivalent to JSON.parse
	// TODO: check for my level
	return rd[direction] !== undefined;
}

var mapon = false;
var gmcpinit = false;
var saveInterval = null;
var lastSaveTime = null;
var activitySinceLastSave = false;

function openMap()
{
	mapper.createMapper(200,200,200,200);
	mapper.loadJsonMap(mapPath);
	lastSaveTime = Date.now();
	saveInterval = setInterval(function() 
	{
		if (activitySinceLastSave && (Date.now() - lastSaveTime > 60000)) 
		{
			lastSaveTime = Date.now();
			activitySinceLastSave = false;
			mapper.saveJsonMap(mapPath);
			console.info('Map: autosaved');
		}
	}, 10000);
	mapon = true;
	win.sendGMCP('Room.Info', {});
	mapper.recenterOnCurrent();
		setTimeout(mapper.updateMap,500);
}

function closeMap()
{
	if(Object.keys(mapper.getAreaTable()).length>0)
	{
		mapper.saveJsonMap(mapPath);
		mapper.closeMapWidget();
	}
	clearInterval(saveInterval);
	mapon = false;
}

function showHelp()
{
	win.displayText('<FRAME MAPPERHELP SCROLLING=y TITLE="Button Bar Help" ACTION=OPEN FLOATING=close SCROLLING=y TOP=10% LEFT=25% WIDTH=250 HEIGHT=250>');
	var html = '<DEST NAME=MAPPERHELP EOF><FONT COLOR=WHITE>';
	html += 'Right-Click in the Mapper Widget for options.  Use mouse-wheel to zoom in/out.  ';
	html += 'Left click and drag the map with to move it around, or leftclick a room node to move it.  ';
	html += '<BR><BR>Enter /map to toggle map on/off.';
	win.displayAt(html,'MAPPERHELP');
}

window.onevent=function(event)
{
	//console.info(event);
	if((event.type==='gmcp')&&(mapon))
	{
		var c = event.command.split('.');
		var g = gmcp;
		for(i=0;i<c.length-1;i++)
		{
			g[c[i]] = g[c[i]] || {};
			g=g[c[i]];
		}
		g[c[c.length-1]] = event.data || {};
		if(event.command === 'room.info')
		{
			onGmcpRoomInfo();
			mapper.recenterOnCurrent();
			mapper.updateMap();
			activitySinceLastSave = true;
		}
	}
	else
	if(event.type == 'map')
	{
		if(mapon)
			closeMap();
		else
			openMap();
	}
	else
	if(event.data && event.data === 'Help')
		showHelp();
};

function startPlugin()
{
	win.sendGMCP('Core.Supports.Add', ["room.info 1"]);
	openMap();  
};

setTimeout(startPlugin,1000);
