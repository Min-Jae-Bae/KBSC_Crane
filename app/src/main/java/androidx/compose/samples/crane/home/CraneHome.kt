/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.samples.crane.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.samples.crane.R
import androidx.compose.samples.crane.base.CraneDrawer
import androidx.compose.samples.crane.base.CraneTabBar
import androidx.compose.samples.crane.base.CraneTabs
import androidx.compose.samples.crane.base.ExploreSection
import androidx.compose.samples.crane.data.ExploreModel
import androidx.compose.samples.crane.ui.BottomSheetShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/*typealias는 존재하는 타입의 이름을 별명으로 바꿔서 짧게 쓰는 것을 말한다.
*OnExploreItemClicked 라는 이름이 (ExploreModel) -> Unit로 별명으로 지정하는 것이다. */
typealias OnExploreItemClicked = (ExploreModel) -> Unit

/*Enum에서는 특정 값을 single instance로서 하나의 객체만 제한적으로 사용하는 것
* Fly, Sleep, Eat이라는 객체를 사용하는 것*/
enum class CraneScreen {
    Fly, Sleep, Eat
}

@Composable
fun CraneHome(
    /*폭 사이즈: 윈도우 폭 사이즈 클래스
    * 본 아이템 클릭시: (ExploreModel) -> Unit
    * 날짜 선택 클릭시: 아무거나
    * Modifier이란 Compose의 UI 구성요소들을 꾸미거나 행동을 추가하기 위한 요소들의 모음이다
    * MainViewModel 파일 안에 있는 클래스 액세스*/
    widthSize: WindowWidthSizeClass,
    onExploreItemClicked: OnExploreItemClicked,
    onDateSelectionClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    /*Scaffold를 사용하기 위해서는 ScaffoldState가 필요하므로
    *rememberScaffoldState()를 이용해 scaffoldState를 만든다 */
    val scaffoldState = rememberScaffoldState()

    //Scaffold는 기본 머티리얼 디자인 레이아웃 구조로 UI를 구현
    Scaffold(

        /* scaffoldState를 넘긴다
        * Modifier 구성요소중 하나인 상태 바를 추가하는 것을 받아들인다.
        * 앱 서랍 내용을 놓고 그리는 작업
        * contentPadding은 content의 padding을 결정하는 것
        * */
        scaffoldState = scaffoldState,
        modifier = Modifier.statusBarsPadding(),
        drawerContent = {
            CraneDrawer()
        }
    ) { contentPadding ->
        val scope = rememberCoroutineScope()
        CraneHomeContent(
            modifier = modifier.padding(contentPadding),
            widthSize = widthSize,
            onExploreItemClicked = onExploreItemClicked,
            onDateSelectionClicked = onDateSelectionClicked,
            openDrawer = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            },
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CraneHomeContent(
    widthSize: WindowWidthSizeClass,
    onExploreItemClicked: OnExploreItemClicked,
    onDateSelectionClicked: () -> Unit,
    openDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val suggestedDestinations by viewModel.suggestedDestinations.observeAsState()

    val onPeopleChanged: (Int) -> Unit = { viewModel.updatePeople(it) }
    var tabSelected by remember { mutableStateOf(CraneScreen.Fly) }

    BackdropScaffold(
        modifier = modifier,
        scaffoldState = rememberBackdropScaffoldState(BackdropValue.Revealed),
        frontLayerShape = BottomSheetShape,
        frontLayerScrimColor = Color.Unspecified,
        appBar = {
            HomeTabBar(openDrawer, tabSelected, onTabSelected = { tabSelected = it })
        },
        backLayerContent = {
            SearchContent(
                widthSize,
                tabSelected,
                viewModel,
                onPeopleChanged,
                onDateSelectionClicked,
                onExploreItemClicked
            )
        },
        frontLayerContent = {
            when (tabSelected) {
                CraneScreen.Fly -> {
                    suggestedDestinations?.let { destinations ->
                        ExploreSection(
                            widthSize = widthSize,
                            title = stringResource(R.string.explore_flights_by_destination),
                            exploreList = destinations,
                            onItemClicked = onExploreItemClicked
                        )
                    }
                }
                CraneScreen.Sleep -> {
                    ExploreSection(
                        widthSize = widthSize,
                        title = stringResource(R.string.explore_properties_by_destination),
                        exploreList = viewModel.hotels,
                        onItemClicked = onExploreItemClicked
                    )
                }
                CraneScreen.Eat -> {
                    ExploreSection(
                        widthSize = widthSize,
                        title = stringResource(R.string.explore_restaurants_by_destination),
                        exploreList = viewModel.restaurants,
                        onItemClicked = onExploreItemClicked
                    )
                }
            }
        }
    )
}

@Composable
private fun HomeTabBar(
    openDrawer: () -> Unit,
    tabSelected: CraneScreen,
    onTabSelected: (CraneScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    CraneTabBar(
        modifier = modifier.wrapContentWidth().sizeIn(maxWidth = 500.dp),
        onMenuClicked = openDrawer
    ) { tabBarModifier ->
        CraneTabs(
            modifier = tabBarModifier,
            titles = CraneScreen.values().map { it.name },
            tabSelected = tabSelected,
            onTabSelected = { newTab -> onTabSelected(CraneScreen.values()[newTab.ordinal]) }
        )
    }
}

@Composable
private fun SearchContent(
    widthSize: WindowWidthSizeClass,
    tabSelected: CraneScreen,
    viewModel: MainViewModel,
    onPeopleChanged: (Int) -> Unit,
    onDateSelectionClicked: () -> Unit,
    onExploreItemClicked: OnExploreItemClicked
) {
    // Reading datesSelected State from here instead of passing the String from the ViewModel
    // to cause a recomposition when the dates change.
    val selectedDates = viewModel.calendarState.calendarUiState.value.selectedDatesFormatted

    when (tabSelected) {
        CraneScreen.Fly -> FlySearchContent(
            widthSize = widthSize,
            datesSelected = selectedDates,
            searchUpdates = FlySearchContentUpdates(
                onPeopleChanged = onPeopleChanged,
                onToDestinationChanged = { viewModel.toDestinationChanged(it) },
                onDateSelectionClicked = onDateSelectionClicked,
                onExploreItemClicked = onExploreItemClicked
            )
        )
        CraneScreen.Sleep -> SleepSearchContent(
            widthSize = widthSize,
            datesSelected = selectedDates,
            sleepUpdates = SleepSearchContentUpdates(
                onPeopleChanged = onPeopleChanged,
                onDateSelectionClicked = onDateSelectionClicked,
                onExploreItemClicked = onExploreItemClicked
            )
        )
        CraneScreen.Eat -> EatSearchContent(
            widthSize = widthSize,
            datesSelected = selectedDates,
            eatUpdates = EatSearchContentUpdates(
                onPeopleChanged = onPeopleChanged,
                onDateSelectionClicked = onDateSelectionClicked,
                onExploreItemClicked = onExploreItemClicked
            )
        )
    }
}

data class FlySearchContentUpdates(
    val onPeopleChanged: (Int) -> Unit,
    val onToDestinationChanged: (String) -> Unit,
    val onDateSelectionClicked: () -> Unit,
    val onExploreItemClicked: OnExploreItemClicked
)

data class SleepSearchContentUpdates(
    val onPeopleChanged: (Int) -> Unit,
    val onDateSelectionClicked: () -> Unit,
    val onExploreItemClicked: OnExploreItemClicked
)

data class EatSearchContentUpdates(
    val onPeopleChanged: (Int) -> Unit,
    val onDateSelectionClicked: () -> Unit,
    val onExploreItemClicked: OnExploreItemClicked
)
