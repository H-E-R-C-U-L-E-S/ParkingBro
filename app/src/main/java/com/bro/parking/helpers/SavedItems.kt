package com.bro.parking.helpers

import android.content.Context
import com.bro.parking.models.MainRecyclerItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SavedItems(context: Context) {

    // Shared prefs
    private var sharedPrefs = context.getSharedPreferences("savedItems",Context.MODE_PRIVATE)

    // Gson
    private var gson = Gson()


    fun saveItems(savedItems:ArrayList<MainRecyclerItem>){
        val editor = sharedPrefs.edit()
        editor.putString("savedItems",gson.toJson(savedItems))
        editor.apply()
    }

    fun getSavedItems() : ArrayList<MainRecyclerItem> {
        val savedItems = sharedPrefs.getString("savedItems", null)
        val t = object : TypeToken<ArrayList<MainRecyclerItem>>() {}.type
        return gson.fromJson(savedItems, t) ?: ArrayList()
    }

}