/*
 * Copyright (c) 2014 Microsoft Mobile. All rights reserved.
 * See the license text file delivered with this project for more information.
 */
 
var requester = require('request');
var _ = require('lodash');

// API keys for push messaging services
var API_KEY_GOOGLE = 'AIzaSyDFHUHWDyzALTA69HsWgsa-SVNC90LIN90';
var API_KEY_NOKIA = 'Y2FwdHVyZS10aGUtZmxhZzpxTW9aK1FtUU1JNHFTdkRIekVNcTVJdldvN1RmaEFvUlhRdFNJZ201enhZPQ==';

// For Google Cloud Messaging support
var gcm = require('node-gcm');
var googleSender = new gcm.Sender(API_KEY_GOOGLE);

// Add radian conversion to numbers
if (typeof(Number.prototype.toRad) === "undefined") {
    Number.prototype.toRad = function() {
        return this * Math.PI / 180;
    }
}

module.exports.generateId = function() {
    return Math.floor((1 + Math.random()) * 0x10000);
};

var containsPlayer = function(player, game) {
    var index = -1;
    
    for (var i = 0; i < game.players.length; i++) {
        if (game.players[i].id === player.id) {
            index = i;
            break;
        }
    }
    
    return index;
};

module.exports.updatePlayerInGame = function(player, game) {
    var index = containsPlayer(player, game);
    
    if (index > -1) {
        game.players[index] = player;
    }
    else {
        console.log("adding player to game:" + player.name);
        game.players.push(player);
    }
};

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
};

/**
 * Sends Nokia Notifications push message to players.
 * @param players
 * @param payload
 */
var sendNokiaNotifications = function(players, payload) {
    if(players && players.length > 0) {
        console.log('Sending Nokia push message to ', players.length, ' clients...');

        var registrationIds = _.pluck(players, 'regId');
        var options = {
            url: 'https://nnapi.ovi.com/nnapi/2.0/send',
            headers: {
                'Authorization': 'key=' + API_KEY_NOKIA,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({'registration_ids': registrationIds, 'data': {'payload': payload}})
        };

        console.log("options:" + JSON.stringify(options));

        requester.post(options, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                console.log("Nokia Notification push response: " + body);
            }
        });

    }
};

/**
 * Sends GCM push message to players.
 * @param players
 * @param payload
 */
var sendGoogleNotifications = function(players, payload) {
    if(players && players.length > 0) {
        console.log('Sending GCM push message to ', players.length, ' clients...');

        var registrationIds = _.pluck(players, 'regId');
        var message = new gcm.Message({
            data: {
                'payload': payload
            }
        });

        googleSender.send(message, registrationIds, true, function (err, data) {
            console.log("Google Cloud Messaging push response: ", err, data);
        });
    }
};

/**
 * Sends push message to players.
 * @param players
 * @param payload
 */
module.exports.sendPushNotification = function(players, payload) {
    console.log("players:" + JSON.stringify(players));

    // Group players by platform
    var groups = _.groupBy(players, function(player) { return player.platform; });

    // Send push messages to players
    sendNokiaNotifications(groups['nokia'], payload);
    sendGoogleNotifications(groups['google'], payload);
};
