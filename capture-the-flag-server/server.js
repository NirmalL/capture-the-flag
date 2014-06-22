/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file delivered with this project for more information.
 */
 
Array.prototype.remove = function(from, to) {
    var rest = this.slice((to || from) + 1 || this.length);
    this.length = from < 0 ? this.length + from : from;
    return this.push.apply(this, rest);
};

var app = require('http').createServer(function(req, res) {
    res.writeHead(200);
    res.end('Capture the Flag server');
});

var io = require('socket.io').listen(app);
var port = Number(process.env.PORT || 8080);

console.log("Starting server on port " + port);
app.listen(port);

var utils = require('./utils.js');
var mocks = require('./mocks.js');

var GAME_FULL_ERROR = -1;
var DISTANCE = 20; // In meters
var CLEANUP_INTERVAL = 1000 * 60 * 15; // 1000 * 3600;
var games = {};

games[mocks.mockgame.id] = mocks.mockgame;
games[mocks.fullgame.id] = mocks.fullgame;

/**
 * Removes the player from the game by using the socket object. The Socket
 * object contains the player object.
 */
function removePlayerFromGame(sock) {
    if (sock.hasOwnProperty("gameid")) {
        var game = games[sock.gameid];
        
        if (game) {
            for (var j = 0; j < game.players.length; j++) {
                if (game.players[j].id === sock.player.id) {
                    console.log("removing player:" + sock.player.id + " from game:" + game.name);
                    game.players.remove(j);
                    break;
                }
            }
        }
    }
}


function cleanUpGames() {
    console.log("cleaning games");
    for (var key in games) {
        if (games.hasOwnProperty(key)) {
            var game = games[key];
            if (game.players.length === 0) {
                if (game.id === mocks.mockgame.id || game.id === mocks.fullgame.id) {
                    continue;
                }
                
                console.log("game \""+ game.name +"\" has no players so removing it");
                delete games[key];
            }
        }
    }
}

setInterval(cleanUpGames, CLEANUP_INTERVAL); 


io.sockets.on('connection', function(socket) {
    socket.emit('hello', { msg: 'hello user' });
    
    socket.on('gamelist',function() {
        console.log("gamelist event");
        var respgames = [];
        
        for (var key in games) {
            if (games.hasOwnProperty(key)) {
                respgames.push(games[key]);
            }
        }
        
        socket.emit('gamelist', { "games": respgames});
    });
    
    socket.on('join', function(dataObject) {
        if (dataObject.player.id === 0) {
            if (socket.hasOwnProperty("player")) {
                dataObject.player.id = socket.player.id;
            }
            else {
                dataObject.player.id = utils.generateId();
            }
        }
        
        socket['player'] = dataObject.player;
        console.log("join event");
        
        // In case of 0 game ID we have a new game so we add it to the list of
        // known games and join the player
        if (dataObject.gameid === 0) {
            var newgame = dataObject.game;
            console.log("new game:" + JSON.stringify(newgame));
            newgame['id'] = utils.generateId();
            games[newgame.id] = newgame;
            dataObject['gameid'] = newgame.id;
        }
        
        var game = games[dataObject.gameid];
        socket['gameid'] = dataObject.gameid;
        
        if (game.players.length == 2 && !game.premium) {
            socket.emit("error", {"code": GAME_FULL_ERROR});
        }
        else {
            utils.updatePlayerInGame(dataObject.player, game);
            socket.join(dataObject.gameid);
            socket.emit('joined', { "game": game, "player": socket.player});
        }
    });

    socket.on('update-player', function(dataObject) {
        var game = games[dataObject.gameid];
        
        // In case there was socket disconnect we make sure the game ID is
        // always the one client wants
        socket['gameid'] = dataObject.gameid;
        
        if (game) {
            socket.join(dataObject.gameid);
            utils.updatePlayerInGame(dataObject.player, game);
            var flag = {"latitude": 0, "longitude": 0};
            console.log("team:" + dataObject.player.team);
            
            if (dataObject.player.team === "red") {
                flag = game.blueflag;
            }
            else if (dataObject.player.team === "blue") {
                flag = game.redflag;
            }
            
            // Check the distance to the target flag
            var distance = utils.distanceBetweenPoints(dataObject.player.latitude, dataObject.player.longitude, flag.latitude, flag.longitude);            
            console.log("distance:" + distance);
            
            if (distance <= DISTANCE) {
                console.log("flag captured by:" + dataObject.player.name);
                var payload = {"type": "flag-captured", "captured_by_player": dataObject.player};
                utils.sendPushNotification(game.players, payload);
                delete games[game.id];
                //notifyPlayers(sock, dataObject.gameid,JSON.stringify(payload) + '\n');
            }
            else {
                socket.broadcast.to(socket['gameid']).emit("update-player",
                        {
                            "update-player": dataObject.player
                        });
            }
        }
    });
    
    socket.on('disconnect', function() {
        console.log("user disconnected");
        
        if (socket.hasOwnProperty("player")) {
            console.log('Disconnected:' + socket.player.name);
        }
        else {
            console.log("Disconnected: unknown player");
        }
        
        removePlayerFromGame(socket);
    });
});
