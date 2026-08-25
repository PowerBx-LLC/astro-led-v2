package com.powerbx.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import com.powerbx.astro.led.AstroLed;
import com.powerbx.astro.led.Color;
import com.powerbx.astro.led.Effect;
import com.powerbx.astro.led.Result;
import com.powerbx.astro.led.LedState;

/**
 * Cordova plugin bridge to AstroLed library.
 * Handles JS method calls and delegates to AstroLed (which handles HTTP + Intent fallback).
 * Plugin automatically calls intent.setPackage("com.powerbx.astro.ledservice").
 */
public class LedController extends CordovaPlugin {

    private static final String[] COLORS = {
        "red", "green", "blue", "white", "redOrange", "mint", "purple", "orange",
        "turquoise", "purplePink", "orangeYellow", "lightBlue", "pink", "yellow", "teal", "magenta"
    };

    private static final String[] COMMANDS = {
        "on", "off", "flash", "strobe", "fade", "smooth", "brightnessUp", "brightnessDown"
    };

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
                case "getColors":
                    getColors(callbackContext);
                    return true;
                case "getCommands":
                    getCommands(callbackContext);
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
            callbackContext.error("Error: " + e.getMessage());
            return false;
        }
    }

    private void setColor(String colorName, CallbackContext callbackContext) {
        try {
            Color color = parseColor(colorName);
            if (color == null) {
                callbackContext.error("Unknown color: " + colorName);
                return;
            }

            Result result = AstroLed.setColor(cordova.getActivity().getApplicationContext(), color);
            if (result instanceof Result.Success) {
                callbackContext.success(colorName);
            } else {
                callbackContext.error("Failed to set color");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void sendCommand(String commandName, CallbackContext callbackContext) {
        try {
            commandName = commandName.toLowerCase();

            Result result = null;
            switch (commandName) {
                case "on":
                    result = AstroLed.on(cordova.getActivity().getApplicationContext());
                    break;
                case "off":
                    result = AstroLed.off(cordova.getActivity().getApplicationContext());
                    break;
                case "flash":
                    result = AstroLed.setEffect(cordova.getActivity().getApplicationContext(), Effect.FLASH);
                    break;
                case "strobe":
                    result = AstroLed.setEffect(cordova.getActivity().getApplicationContext(), Effect.STROBE);
                    break;
                case "fade":
                    result = AstroLed.setEffect(cordova.getActivity().getApplicationContext(), Effect.FADE);
                    break;
                case "smooth":
                    result = AstroLed.setEffect(cordova.getActivity().getApplicationContext(), Effect.SMOOTH);
                    break;
                case "brightnessup":
                    result = AstroLed.brightnessUp(cordova.getActivity().getApplicationContext());
                    break;
                case "brightnessdown":
                    result = AstroLed.brightnessDown(cordova.getActivity().getApplicationContext());
                    break;
                default:
                    callbackContext.error("Unknown command: " + commandName);
                    return;
            }

            if (result instanceof Result.Success) {
                callbackContext.success(commandName);
            } else {
                callbackContext.error("Failed to execute command");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void getColors(CallbackContext callbackContext) {
        try {
            JSONArray colorArray = new JSONArray();
            for (String color : COLORS) {
                colorArray.put(color);
            }
            callbackContext.success(colorArray);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void getCommands(CallbackContext callbackContext) {
        try {
            JSONArray commandArray = new JSONArray();
            for (String command : COMMANDS) {
                commandArray.put(command);
            }
            callbackContext.success(commandArray);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void getCurrentState(CallbackContext callbackContext) {
        try {
            // For backward compatibility, just return "white" as default
            callbackContext.success("white");
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void isAvailable(CallbackContext callbackContext) {
        try {
            boolean available = AstroLed.isAvailable(cordova.getActivity().getApplicationContext());
            callbackContext.success(available ? 1 : 0);
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void getState(CallbackContext callbackContext) {
        try {
            Result result = AstroLed.getState(cordova.getActivity().getApplicationContext());
            if (result instanceof Result.Success) {
                LedState state = (LedState) ((Result.Success) result).getData();
                callbackContext.success(state.toString());
            } else {
                callbackContext.error("Failed to get state");
            }
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private Color parseColor(String colorName) {
        colorName = colorName.toUpperCase().replace("RED_ORANGE", "RED_ORANGE")
                .replace("REDORANGE", "RED_ORANGE")
                .replace("PURPLE_PINK", "PURPLE_PINK")
                .replace("PURPLEPINK", "PURPLE_PINK")
                .replace("ORANGE_YELLOW", "ORANGE_YELLOW")
                .replace("ORANGEYELLOW", "ORANGE_YELLOW")
                .replace("LIGHT_BLUE", "LIGHT_BLUE")
                .replace("LIGHTBLUE", "LIGHT_BLUE");

        try {
            return Color.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
