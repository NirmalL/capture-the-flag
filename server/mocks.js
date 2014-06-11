/**
 * Copyright (c) 2014 Microsoft Mobile.
 * See the license text file delivered with this project for more information.
 */
 
var mockgame = {
    "id": 123456,
    "redflag": {
        "latitude": 61.512449,
        "longitude": 23.780336
    },
    "blueflag": {
        "latitude": 61.500657,
        "longitude": 23.745661
    },
    "name": "Hervanta battle",
    "players": [],
    "premium": true
};

var fullgame = {
    "id": "654321",
    "redflag": {
        "latitude": 61.512249,
        "longitude": 23.781336
    },
    "blueflag": {
        "latitude": 61.500257,
        "longitude": 23.743661
    },
    "name": "full game",
    "players": [
        {
            "name": "test1",
            "id": 112233,
            "latitude": 61.514249,
            "longitude": 23.791336,
            "team": "red",
            "regId": "testregid"
        },
        {
            "name": "test2",
            "id": 332211,
            "latitude": 61.501257,
            "longitude": 23.746661,
            "team": "blue",
            "regId": "testregid"
        }
    ],
    "premium": false
};

module.exports.mockgame = mockgame;
module.exports.fullgame = fullgame;