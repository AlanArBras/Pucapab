package net.huitel.pucapab.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static net.huitel.pucapab.network.GlutenChecker.GlutenPresence.ERROR;
import static net.huitel.pucapab.network.GlutenChecker.GlutenPresence.GLUTEN;
import static net.huitel.pucapab.network.GlutenChecker.GlutenPresence.GLUTEN_FREE;
import static net.huitel.pucapab.network.GlutenChecker.GlutenPresence.NOT_FOUND;
import static net.huitel.pucapab.network.GlutenChecker.GlutenPresence.TRACES;
import static net.huitel.pucapab.network.GlutenChecker.GlutenPresence.UNKNOWN;

/**
 * Created by root on 1/8/18.
 */

public class GlutenChecker {
    public enum GlutenPresence {
        GLUTEN, TRACES, GLUTEN_FREE, UNKNOWN, NOT_FOUND, ERROR
    }

    public static String getProductName(JSONObject fullDetails){
        if(fullDetails == null || fullDetails.isNull("product"))
            return "Produit introuvable";
        try {
            JSONObject product = fullDetails.getJSONObject("product");
            return (String) product.get("product_name");
        } catch (JSONException e) {
            e.printStackTrace();
            return "Produit introuvable";
        }
    }
    public static GlutenPresence getGlutenPresence(JSONObject fullDetails) {
        try {
            if(fullDetails == null || fullDetails.isNull("product"))
                return NOT_FOUND;

            JSONObject product = fullDetails.getJSONObject("product");
            String states = (String) product.get("states");
            Boolean areIngredientsCompleted = states != null && states.toLowerCase().contains("ingredients-completed");
            if (states != null) {
                Log.d("states", states);
            }

            JSONArray allergens = (JSONArray) product.get("allergens_tags");
            Boolean glutenIsPresent = allergens != null && allergens.toString().toLowerCase().contains("gluten");
            if (allergens != null) {
                Log.d("allergens", allergens.toString());
            }

            JSONArray traces = (JSONArray) product.get("traces_tags");
            Boolean tracesOfGluten = traces != null && traces.toString().toLowerCase().contains("gluten");
            if (traces != null) {
                Log.d("traces", traces.toString());
            }

            JSONArray labels = product.getJSONArray("labels_tags");
            Boolean hasGlutenFreeLabel = labels != null && labels.toString().toLowerCase().contains("gluten-free");
            if (labels != null) {
                Log.d("labels", labels.toString());
            }
            if (glutenIsPresent) {
                return GLUTEN;
            } else if (tracesOfGluten) {
                return TRACES;
            } else if (hasGlutenFreeLabel || areIngredientsCompleted) {
                return GLUTEN_FREE;
            } else {
                return UNKNOWN;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return ERROR;
        }
    }

    public static GlutenPresence getGlutenPresence(JSONArray product) {
        try {
            return getGlutenPresence(product.getJSONObject(0));
        } catch (JSONException e) {
            e.printStackTrace();
            return ERROR;
        }
    }


}
