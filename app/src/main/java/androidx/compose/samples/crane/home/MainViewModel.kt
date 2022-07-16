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

import androidx.compose.runtime.mutableStateOf
import androidx.compose.samples.crane.calendar.model.CalendarState
import androidx.compose.samples.crane.data.DestinationsRepository
import androidx.compose.samples.crane.data.ExploreModel
import androidx.compose.samples.crane.di.DefaultDispatcher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.random.Random

const val MAX_PEOPLE = 4

/*MainViewModel 클래스 생성 constructor 생성자 호출 (init과 달리 매개변수 지정)*/
@HiltViewModel
class MainViewModel @Inject constructor(
    // 목적지 저장소에 관한 접근 지정자 변수 생성
    private val destinationsRepository: DestinationsRepository,
    // 기본 스레딩 작업 변수를 생성하여 코투린 스레딩을 선언
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    // shownSplash라는 변수를 생성 -> 변하지 않는 값으로 지정
    val shownSplash = mutableStateOf(SplashState.Shown)
    // hotels라는 변수를 생성하는데 그것은 데이터 클래스의 ExploreModel에 접근하는 것, 즉 data 클래스에 호텔 목적지 저장소를 넣음
    val hotels: List<ExploreModel> = destinationsRepository.hotels
    // restaurants라는 변수를 생성하는데 그것은 데이터 클래스의 ExploreModel에 접근하는 것, 즉 data 클래스에 레스토랑 목적지 저장소를 넣음
    val restaurants: List<ExploreModel> = destinationsRepository.restaurants

    // 캘린더 상태를 변수를 선언. androidx.compose.samples.crane.calendar.model에서 가져옴
    val calendarState = CalendarState()

    private val _suggestedDestinations = MutableLiveData<List<ExploreModel>>()

    // 목적지 제안 변수 생성 : 데이터를 가지고 있는 LiveData 클래스를 생성 ( 목록별 -> 호텔, 레스토랑)
    val suggestedDestinations: LiveData<List<ExploreModel>>
    // 변하지 않는 데이터 클래스 목록을 가져온다
        get() = _suggestedDestinations

    // init 함수는 매개변수가 없고 반환되는 값이 없는 특별한 함수
    init {
        // 변하지 않는 제한된 목적지 리스트에 목적지 저장소를 넣는다. ( 데이타 목적지에 접근 )
        _suggestedDestinations.value = destinationsRepository.destinations
    }

    // 날짜를 선택할 때 -> 날짜를 받음
    fun onDaySelected(daySelected: LocalDate) {
        // ViewModel이 묶여 있는 CoroutineScope이다 그것을 vieModelScope으로 변수 생성함
        viewModelScope.launch {
            // 선택한 날짜를 놓고 캘린더 상태에 실행
            calendarState.setSelectedDay(daySelected)
        }
    }

    // 사람들 인원수 업데이트?
    fun updatePeople(people: Int) {
        viewModelScope.launch {
            if (people > MAX_PEOPLE) {
                // 수정 불가능 읽기 전용으로 전환
                _suggestedDestinations.value = emptyList()
            } else {
                val newDestinations = withContext(defaultDispatcher) {
                    // 목적지 저장소에 있는 목적지를 순서를 무작위로 함 ( 인원수에 1~100을 곱해 무작위로 하여 처음 값)
                    destinationsRepository.destinations
                        .shuffled(Random(people * (1..100).shuffled().first()))
                }
                _suggestedDestinations.value = newDestinations
            }
        }
    }

    // 목적지가 바뀌었을 때
    fun toDestinationChanged(newDestination: String) {
        // ViewModel이 묶여 있는 CoroutineScope이다
        viewModelScope.launch {
            // 새로운 변수에 문자를 적절한 스레드에 작업을 전달한다
            val newDestinations = withContext(defaultDispatcher) {
                // 목적지 저장소에 있는 목적지에 접근하여 목적지 내용을 포함한 새로운 도시의 이름을 필터링
                destinationsRepository.destinations
                    .filter { it.city.nameToDisplay.contains(newDestination) }
            }
            // 레스토랑, 호텔의 값은 새로운 목적지로 생각함
            _suggestedDestinations.value = newDestinations
        }
    }
}
