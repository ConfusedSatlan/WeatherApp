package com.example.weatherapp.Activity

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.Adapter.ForecastAdapter
import com.example.weatherapp.Model.CurrentResponseApi
import com.example.weatherapp.Model.ForecastResponseApi
import com.example.weatherapp.R
import com.example.weatherapp.ViewModel.WeatherViewModel
import com.example.weatherapp.databinding.ActivityMainBinding
import com.github.matteobattilana.weather.PrecipType
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val weatherViewModel:WeatherViewModel by viewModels()
    private val calendar by lazy { Calendar.getInstance() }
    private val forecastAdapter by lazy { ForecastAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding.apply {
            var lat=intent.getDoubleExtra("lat", 0.0)
            var lon=intent.getDoubleExtra("lon", 0.0)
            var name=intent.getStringExtra("name")

            if (lat == 0.0) {
                lat = 51.50
                lon = -0.12
                name = "London"
            }

            addCity.setOnClickListener{
                startActivity(Intent(this@MainActivity, CityListActivity::class.java))
            }
            //current Weather
            cityTxt.text = name
            progressBar.visibility = View.VISIBLE
            weatherViewModel.loadCurrentWeather(lat, lon, "metric").enqueue(object: Callback<CurrentResponseApi> {
                override fun onResponse(
                    call: Call<CurrentResponseApi>,
                    response: Response<CurrentResponseApi>
                ) {
                    if(response.isSuccessful) {
                        val data = response.body()
                        progressBar.visibility = View.GONE
                        detailLayout.visibility = View.VISIBLE
                        data?.let {
                            statusTxt.text = it.weather?.get(0)?.main ?: "-"
                            windTxt.text = it.wind?.speed.let { it -> (it ?: 0.0).roundToInt().toString() } + "Km"
                            humidityTxt.text = it.main?.humidity.toString() + "%"
                            currentTempTxt.text = it.main?.temp.let { it -> (it ?: 0.0).roundToInt().toString() } + "°"
                            maxTempTxt.text = it.main?.tempMax.let { it -> (it ?: 0.0).roundToInt().toString() } + "°"
                            minTempTxt.text = it.main?.tempMin.let { it -> (it ?: 0.0).roundToInt().toString() } + "°"

                            val drawable = if (isNightNow()) R.drawable.night_bg
                            else {
                                setDynamicallyWallpaper(it.weather?.get(0)?.icon?:"-")
                            }
                            bgImage.setImageResource(drawable)
                            setEffectRainSnow(it.weather?.get(0)?.icon?: "-")
                        }
                    }
                }

                override fun onFailure(call: Call<CurrentResponseApi>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_LONG).show()
                }

            })


            //settings blue View
            var radius=10f
            val devorView=window.decorView
            val rootView=(devorView.findViewById(android.R.id.content) as ViewGroup?)
            val windowBackground = devorView.background

            rootView?.let {
                blueView.setupWith(it, RenderScriptBlur(this@MainActivity))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blueView.outlineProvider=ViewOutlineProvider.BACKGROUND
                blueView.clipToOutline = true
            }

            //forecast temp
            weatherViewModel.loadForecastWeather(lat, lon, "metric").enqueue(object : Callback<ForecastResponseApi> {
                override fun onResponse(
                    call: Call<ForecastResponseApi>,
                    response: Response<ForecastResponseApi>
                ) {
                    if(response.isSuccessful) {
                        val data=response.body()
                        blueView.visibility=View.VISIBLE

                        data?.let {
                            forecastAdapter.differ.submitList(it.list)
                            forecastView.apply {
                                layoutManager=LinearLayoutManager(
                                    this@MainActivity,
                                    LinearLayoutManager.HORIZONTAL,
                                    false)
                                adapter=forecastAdapter
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ForecastResponseApi>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun isNightNow():Boolean {
        return calendar.get(Calendar.HOUR_OF_DAY) >= 19
    }

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when(icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.snow_bg
            }
            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.cloudy_bg
            }
            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)
                R.drawable.rainy_bg
            }
            "13" -> {
                initWeatherView(PrecipType.SNOW)
                R.drawable.snow_bg
            }
            "50" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.haze_bg
            }
            else -> 0
        }
    }

    private fun setEffectRainSnow(icon: String) {
        when(icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
            }
            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
            }
            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)
            }
            "13" -> {
                initWeatherView(PrecipType.SNOW)
            }
            "50" -> {
                initWeatherView(PrecipType.CLEAR)
            }
        }
    }

    private fun initWeatherView(type: PrecipType) {
        binding.weatherView.apply {
            setWeatherData(type)
            angle = -20
            emissionRate = 100.0f
        }
    }
}