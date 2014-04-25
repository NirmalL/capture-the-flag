/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */
 
var requester = require('request');

// Add radian conversion to numbers
if (typeof(Number.prototype.toRad) === "undefined") {
    Number.prototype.toRad = function() {
        return this * Math.PI / 180;
    }
}

module.exports.generateId = function() {
    return Math.floor((1 + Math.random()) * 0x10000);
}

var containsPlayer = function(player, game) {
    var index = -1;
    
    for (var i = 0; i < game.players.length; i++) {
        if (game.players[i].id === player.id) {
            index = i;
            break;
        }
    }
    
    return index;
}

module.exports.updatePlayerInGame = function(player, game) {
    var index = containsPlayer(player, game);
    
    if (index > -1) {
        game.players[index] = player;
    }
    else {
        console.log("adding player to game:" + player.name);
        game.players.push(player);
    }
}

/**
 * Calculates the distance between to geo-locations using the haversine formula
 * @param lat1
 * @param lon1
 * @param lat2
 * @param long2
 * @returns distance in meters between points
 */
module.exports.distanceBetweenPoints = function(lat1, lon1, lat2, lon2) {
    var R = 6371000; // m
    var dLat = (lat2-lat1).toRad();
    var dLon = (lon2-lon1).toRad();
    var lat1 = lat1.toRad();
    var lat2 = lat2.toRad();

    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    var d = R * c;
    return d;
}

module.exports.sendPushNotification = function(players, payload) {
    console.log("players:" + JSON.stringify(players));
    var regids = [];
    
    for (var i = 0; i < players.length; i++) {
        regids.push(players[i].regId);
    }
    
    var options = {
        url: 'https://nnapi.ovi.com/nnapi/2.0/send',
        headers: {
            'Authorization': 'key=Y2FwdHVyZS10aGUtZmxhZzpxTW9aK1FtUU1JNHFTdkRIekVNcTVJdldvN1RmaEFvUlhRdFNJZ201enhZPQ==',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({'registration_ids': regids, 'data': {'payload': payload}})
    }
    
    console.log("options:" + JSON.stringify(options));
    
    requester.post(
        options,
        function (error, response, body) {
            if (!error && response.statusCode == 200) {
                console.log("push response:"+body);
            }
        });
}
