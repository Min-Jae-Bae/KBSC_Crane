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

package androidx.compose.samples.crane.data

import javax.inject.Inject

class DestinationsRepository @Inject constructor(
    private val destinationsLocalDataSource: DestinationsLocalDataSource
) {
    // 목적지, 호텔, 식당에 관한 데이터 클래스 ExploreModel를 변수를 생성하고 안에 각각의 데이터 소스를 주입
    val destinations: List<ExploreModel> = destinationsLocalDataSource.craneDestinations
    val hotels: List<ExploreModel> = destinationsLocalDataSource.craneHotels
    val restaurants: List<ExploreModel> = destinationsLocalDataSource.craneRestaurants

    // 목적지를 얻는 기능 ( 도시 이름을 문자열로 받는데 그에 관련한 값이 ExploreMode안에 없다면? )
    fun getDestination(cityName: String): ExploreModel? {
        // 데이터에 접근해서 있으면 첫 값을 가져오고, 값이 접근하지 못한다면 널을 반환한다.
        return destinationsLocalDataSource.craneDestinations.firstOrNull {
            // 그 도시이름을 반환한다.
            it.city.name == cityName
        }
    }
}
