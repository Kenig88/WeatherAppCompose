package com.kenig.weatherappcompose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.kenig.weatherappcompose.R
import com.kenig.weatherappcompose.WeatherModel
import com.kenig.weatherappcompose.ui.theme.Neon
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


//1
@Composable
fun MainCard(currentDay: MutableState<WeatherModel>, onClickSync: () -> Unit, onClickSearch: () -> Unit) { //5.5 //6
    Column(
        modifier = Modifier.padding(5.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            backgroundColor = Neon,
            elevation = 0.dp,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.padding(top = 5.dp, start = 5.dp),
                        text = currentDay.value.time,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    AsyncImage(
                        model = "https:"+currentDay.value.icon,
                        contentDescription = "im2",
                        modifier = Modifier
                            .size(35.dp)
                            .padding(top = 5.dp, end = 5.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDay.value.city,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Text(
                        text = if(currentDay.value.currentTemp.isNotEmpty())
                            currentDay.value.currentTemp
                        else "${currentDay.value.maxTemp.toFloat().toInt()}°" +
                                "/${currentDay.value.minTemp.toFloat().toInt()}°",
                        fontSize = 65.sp,
                        color = Color.White
                    )
                    Text(
                        text = currentDay.value.condition,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = {
                            onClickSearch.invoke()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = "im3",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "${currentDay.value.maxTemp.toFloat().toInt()}°" +
                                "/${currentDay.value.minTemp.toFloat().toInt()}°",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { onClickSync.invoke() //6.1
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sync),
                            contentDescription = "im4",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>,
              currentDay: MutableState<WeatherModel>) { //4.6
    val tabList = listOf("HOURS", "DAYS")
    val pagerState = rememberPagerState()
    val tabIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        TabRow(
            selectedTabIndex = tabIndex,
            indicator = { pos ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, pos)
                )
            },
            backgroundColor = Neon,
            contentColor = Color.White //(цвет текста)
        ) {
            tabList.forEachIndexed { index, text ->
                Tab(
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = text) }
                )
            }
        }
        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when(index) { //6.2
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currentDay)
        }
    }
}

@Composable //6
fun MainList(list: List<WeatherModel>, currentDay: MutableState<WeatherModel>){
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            list
        ){
                _, item -> ListItem(item, currentDay)
        }
    }
}

@Composable
fun ListItem(item: WeatherModel, currentDay: MutableState<WeatherModel>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .clickable {
                if (item.hours.isEmpty()) return@clickable
                currentDay.value = item
            },
        backgroundColor = Neon,
        elevation = 0.dp,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(start = 5.dp, top = 5.dp),
            ) {
                Text(
                    text = item.time,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Text(
                    modifier = Modifier.padding(bottom = 5.dp),
                    text = item.condition,
                    color = Color.White,
                    fontSize = 13.sp,
                )
            }
            Text(
                text = item.currentTemp.ifEmpty {"${item.maxTemp}°/${item.minTemp}°"},
                fontSize = 30.sp,
                color = Color.White
            )
            AsyncImage(
                model = "https:${item.icon}",
                contentDescription = "im4",
                modifier = Modifier
                    .padding(top = 5.dp, end = 5.dp, bottom = 5.dp)
                    .size(30.dp)
            )
        }
    }
}


private fun getWeatherByHours(hours: String): List<WeatherModel> { //6.1
    if (hours.isEmpty()) return listOf()
    val hoursArray = JSONArray(hours)
    val list = ArrayList<WeatherModel>()
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WeatherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString()+"°",
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                ""
            )
        )
    }
    return list
}

@Composable
fun DialogSearch(dialogState: MutableState<Boolean>, onSubmit: (String) -> Unit){
    val dialogText = remember{
        mutableStateOf("")
    }
    AlertDialog(
        onDismissRequest = {
            dialogState.value = false
        },
        confirmButton = {
            TextButton(onClick = {
                onSubmit(dialogText.value)
                dialogState.value = false
            }) {
                Text(text = "OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                dialogState.value = false
            }) {
                Text(text = "Cancel")
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = "Enter the name of the city:")
                TextField(
                    value = dialogText.value,
                    onValueChange = { dialogText.value = it }
                )
            }
        }
    )
}

