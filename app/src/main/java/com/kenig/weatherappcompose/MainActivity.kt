package com.kenig.weatherappcompose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kenig.weatherappcompose.screens.DialogSearch
import com.kenig.weatherappcompose.screens.MainCard
import com.kenig.weatherappcompose.screens.TabLayout
import org.json.JSONObject


const val API_KEY = "7c8fb0a0fe16456290315624232105"
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val daysList = remember { //4.1
                mutableStateOf(listOf<WeatherModel>())
            }
            val currentDay = remember { //5
                mutableStateOf(
                    WeatherModel(
                        "",
                        "",
                        "0.0",
                        "",
                        "",
                        "0.0",
                        "0.0",
                        ""
                    )
                )
            }
            val dialogState = remember {
                mutableStateOf(false)
            }
            if (dialogState.value) {
                DialogSearch(dialogState, onSubmit = {
                    getData(it, this@MainActivity, daysList, currentDay)
                })
            }

            getData("Moscow", this, daysList, currentDay) //4.3 /5.1
            Image(
                painter = painterResource(R.drawable.weather_cropped),
                contentDescription = "im1",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 1f
            )
            Column {
                MainCard(currentDay, onClickSync = { //5.4 /6.2
                    getData("Moscow", this@MainActivity, daysList, currentDay)
                },
                onClickSearch = {
                    dialogState.value = true
                })
                TabLayout(daysList, currentDay) //4.5
            }
        }
    }
}


private fun getData(city: String, context: Context,
                    daysList: MutableState<List<WeatherModel>>, //2
                    currentDay: MutableState<WeatherModel>) { //5.2
    val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
            API_KEY +
            "&q=$city" +
            "&days=14" +
            "&aqi=no" +
            "&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val sRequest = StringRequest(
        Request.Method.GET,
        url,
        {
                response ->
            val list = getWeatherByDays(response) //4
            currentDay.value = list[0] //5.3
            daysList.value = list //4.4
        },
        {
                error ->
        }
    )
    queue.add(sRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel> { //3
    if (response.isEmpty()) return listOf()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition").getString("text"),
                item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("location").getString("localtime"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c") + "°"
    )
    return list
}


