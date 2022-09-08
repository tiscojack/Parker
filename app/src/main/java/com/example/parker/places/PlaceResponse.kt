package com.example.parker.places

import com.google.android.gms.maps.model.LatLng

class PlaceResponse() {
    var name: String? = null
        private set
    var vicinity: String? = null
        private set
    var geometry: Geometry? = null
        private set
    var rating: Float? = null
        private set

    class Geometry() {
        var location: GeometryLocation? = null
            private set

        constructor(location: GeometryLocation?) : this() {
            this.location = location
        }

    }

    class GeometryLocation() {
        var lat: Double? = null
            private set
        var lng: Double? = null
            private set

        constructor(lat: Double?, lng: Double?) : this() {
            this.lat = lat
            this.lng = lng
        }
    }

    constructor(name: String?, vicinity: String?, geometry: Geometry?, rating: Float?) : this() {
        this.name = name
        this.vicinity = vicinity
        this.rating = rating
        this.geometry = geometry
    }
}

fun PlaceResponse.toPlace(): PlaceLocal? = name?.let {
    vicinity?.let { it1 ->
        rating?.let { it2 ->
            geometry?.location?.let { it3 ->
                it3.lat?.let { it4 ->
                    geometry?.location!!.lng?.let { it5 ->
                        LatLng(
                            it4,
                            it5
                        )
                    }
                }
            }
                ?.let { it4 ->
                    PlaceLocal(
                        name = it,
                        address = it1,
                        latLng = it4,
                        rating = it2
                    )
                }
        }
    }
}