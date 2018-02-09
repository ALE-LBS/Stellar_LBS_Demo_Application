package com.ale.proserv.geoapp3.util;

import android.annotation.SuppressLint;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.List;

import io.mapwize.mapwizeformapbox.model.MapwizeObject;
import io.mapwize.mapwizeformapbox.model.Place;
import io.mapwize.mapwizeformapbox.model.PlaceList;
import io.mapwize.mapwizeformapbox.model.Translation;
import io.mapwize.mapwizeformapbox.model.Venue;

/**
 * Created by vaymonin on 06/02/2018.
 */

@SuppressLint("ParcelCreator")
public class MapwizeSuggestionWrapper implements SearchSuggestion {

    String name;
    String alias;
    String objectId;
    String objectClass;
    List<Translation> translations;
    MapwizeObject searchable;

    public MapwizeSuggestionWrapper(MapwizeObject object){
        name = object.getAlias();
        alias = object.getAlias();
        objectId = object.getId();
        if(object.getClass() == Place.class){
            objectClass = "Place";
        }
        if(object.getClass() == Venue.class){
            objectClass = "Venue";
        }
        if(object.getClass() == PlaceList.class){
            objectClass = "PlaceLits";
        }
        translations = object.getTranslations();
        this.searchable = object;
    }

    @Override
    public String getBody() {
        return translations.get(0).getTitle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
