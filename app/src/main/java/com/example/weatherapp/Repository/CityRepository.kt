package com.example.weatherapp.Repository

import com.example.weatherapp.Service.ApiServices

class CityRepository(val api:ApiServices) {
    fun getCities(q:String, limit:Int)=
        api.getCitiesList(q, limit, "1cd5c442b6b09b8d87ab330b223fbce0")

}