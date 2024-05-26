package com.example.weatherapp.Repository

import com.example.weatherapp.Service.ApiServices

class WeatherRepository(val api:ApiServices) {
    fun getCurrentWeather(lat: Double, lng: Double, unit:String) =
        api.getCurrentWeather(lat, lng, unit, "1cd5c442b6b09b8d87ab330b223fbce0")

    fun getForecastWeather(lat: Double, lng: Double, unit:String) =
        api.getForecastWeather(lat, lng, unit, "1cd5c442b6b09b8d87ab330b223fbce0")
}