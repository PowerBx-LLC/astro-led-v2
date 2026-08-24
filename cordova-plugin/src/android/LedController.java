package com.powerbx.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.powerbx.astro.led.AstroLed;

public class LedController extends CordovaPlugin {

    private AstroLed ledController;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        
        try {
            switch (action) {
                case "setColor":
                    setColor(args.getString(0), callbackContext);
                    return true;
                case "sendCommand":
                    sendCommand(args.getString(0), callbackContext);
                    return true;
                case "getCurrentState":
                    getCurrentState(callbackContext);
                    return true;
                case "isAvailable":
                    isAvailable(callbackContext);
                    return true;
                case "getState":
                    getState(callbackContext);
                    return true;
                default:
                    callbackContext.error("Unknown action: " + action);
                    return false;
            }
        } catch (Exception e) {
            callbackContext.error("Error executing action: " + e.getMessage());
            return false;
        }
    }

    /**
     * Set the LED color by delegating to AstroLed library
     */
    private void setColor(String color, CallbackContext callbackContext) {
        try {
            if (ledController == null) {
                ledController = new AstroLed(cordova.getActivity().getApplicationContext());
            }
            
            boolean success = ledController.setColor(color);
            if (success) {
                callbackContext.success("Color set to: " + color);
            } else {
                callbackContext.error("Failed to set color: " + color);
            }
        } catch (Exception e) {
            callbackContext.error("Service unavailable or error setting color: " + e.getMessage());
        }
    }

    /**
     * Send a command to the LED by delegating to AstroLed library
     */
    private void sendCommand(String command, CallbackContext callbackContext) {
        try {
            if (ledController == null) {
                ledController = new AstroLed(cordova.getActivity().getApplicationContext());
            }
            
            boolean success = ledController.sendCommand(command);
            if (success) {
                callbackContext.success("Command executed: " + command);
            } else {
                callbackContext.error("Failed to execute command: " + command);
            }
        } catch (Exception e) {
            callbackContext.error("Service unavailable or error executing command: " + e.getMessage());
        }
    }

    /**
     * Get the current LED state
     */
    private void getCurrentState(CallbackContext callbackContext) {
        try {
            if (ledController == null) {
                ledController = new AstroLed(cordova.getActivity().getApplicationContext());
            }
            
            JSONObject state = ledController.getState();
            if (state != null) {
                callbackContext.success(state);
            } else {
                callbackContext.error("Failed to retrieve LED state");
            }
        } catch (Exception e) {
            callbackContext.error("Service unavailable or error retrieving state: " + e.getMessage());
        }
    }

    /**
     * Check if the LED service is available
     */
    private void isAvailable(CallbackContext callbackContext) {
        try {
            if (ledController == null) {
                ledController = new AstroLed(cordova.getActivity().getApplicationContext());
            }
            
            boolean available = ledController.isServiceAvailable();
            callbackContext.sendPluginResult(
                new org.apache.cordova.PluginResult(
                    org.apache.cordova.PluginResult.Status.OK,
                    available
                )
            );
        } catch (Exception e) {
            callbackContext.error("Service unavailable: " + e.getMessage());
        }
    }

    /**
     * Get the current LED state (async version)
     */
    private void getState(CallbackContext callbackContext) {
        try {
            if (ledController == null) {
                ledController = new AstroLed(cordova.getActivity().getApplicationContext());
            }
            
            JSONObject state = ledController.getState();
            if (state != null) {
                callbackContext.success(state);
            } else {
                callbackContext.error("Failed to retrieve LED state");
            }
        } catch (Exception e) {
            callbackContext.error("Service unavailable or error retrieving state: " + e.getMessage());
        }
    }
}
