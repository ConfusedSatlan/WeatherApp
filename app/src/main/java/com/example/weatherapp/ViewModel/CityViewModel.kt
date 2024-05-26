package com.example.weatherapp.ViewModel

import androidx.lifecycle.ViewModel
import com.example.weatherapp.Repository.CityRepository
import com.example.weatherapp.Service.ApiClient
import com.example.weatherapp.Service.ApiServices

class CityViewModel(val repository: CityRepository) :ViewModel() {
    constructor():this(CityRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCity(q:String,limit: Int)=
        repository.getCities(q, limit)
}