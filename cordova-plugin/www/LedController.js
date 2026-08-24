var exec = require('cordova/exec');

var LedController = {
    /**
     * Get available LED colors
     * @returns {Array} Array of color name strings
     */
    getColors: function() {
        var colors = [
            'red', 'green', 'blue', 'white', 'redOrange', 'mint', 'purple',
            'orange', 'turquoise', 'purplePink', 'orangeYellow', 'lightBlue',
            'pink', 'yellow', 'teal', 'magenta'
        ];
        return colors;
    },

    /**
     * Get available LED commands
     * @returns {Array} Array of command name strings
     */
    getCommands: function() {
        var commands = [
            'on', 'off', 'flash', 'strobe', 'fade', 'smooth',
            'brightnessUp', 'brightnessDown'
        ];
        return commands;
    },

    /**
     * Set the LED color
     * @param {String} color - Color name (case-insensitive)
     * @param {Function} successCallback - Callback on success
     * @param {Function} errorCallback - Callback on error
     */
    setColor: function(color, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'LedController', 'setColor', [color]);
    },

    /**
     * Send a command to the LED
     * @param {String} command - Command name
     * @param {Function} successCallback - Callback on success
     * @param {Function} errorCallback - Callback on error
     */
    sendCommand: function(command, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'LedController', 'sendCommand', [command]);
    },

    /**
     * Get the current LED state
     * @param {Function} successCallback - Callback with state object
     * @param {Function} errorCallback - Callback on error
     */
    getCurrentState: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'LedController', 'getCurrentState', []);
    },

    /**
     * Check if the LED service is available
     * @param {Function} successCallback - Callback with availability boolean
     * @param {Function} errorCallback - Callback on error
     */
    isAvailable: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'LedController', 'isAvailable', []);
    },

    /**
     * Get the current LED state (async version)
     * @param {Function} successCallback - Callback with state object
     * @param {Function} errorCallback - Callback on error
     */
    getState: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'LedController', 'getState', []);
    }
};

module.exports = LedController;
