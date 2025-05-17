package dev.sahildesai.maps.models

fun GeoResult.toAddress() = Address(
    formattedAddress = this.formattedAddress,
    lat = this.geometry.location.lat,
    lng = this.geometry.location.lng,
    name = this.name,
)