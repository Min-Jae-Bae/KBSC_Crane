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

import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.samples.crane.R
import androidx.compose.samples.crane.calendar.CalendarScreen
import androidx.compose.samples.crane.details.launchDetailsActivity
import androidx.compose.samples.crane.login.LoginHome
import androidx.compose.samples.crane.login.LoginViewModel
import androidx.compose.samples.crane.ui.CraneTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class) // 어노테이션이 연결된 파일, 선언, 식에서 opt-in API를 사용할 수 있습니다.
@AndroidEntryPoint // 프로젝트의 각 Android 클래스에 관한 개별 Hilt 구성요소를 생성
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // false하면 프레임워크가 콘텐츠 보기를 삽입에 맞추지 않고 콘텐츠 보기를 통해 전달
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 블록은 구성 가능한 함수가 호출되는 활동의 레이아웃을 정의
        setContent {
            CraneTheme {
                // 장치가 회전하거나 창 크기가 조정되는 경우와 같이 구성 변경으로 인해 창의 너비 또는 높이가 중단점을 넘을 때마다 new 가 반환
                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

                // 앱의 화면과 각 화면 상태를 구성하는 컴포저블의 백 스택을 추적
                val navController = rememberNavController()
                //  NavHost는 구성 가능한 대상을 지정하는 탐색 그래프, 로그인 부터 시작
                NavHost(navController = navController, startDestination = Routes.Login.route) {

                    composable(Routes.Login.route) {
                        val loginViewModel = hiltViewModel<LoginViewModel>()
                        LoginScreen()
                    }


                    // composable() 메서드를 사용하여 탐색 구조에 추가, 경로뿐만 아니라 대상에 연결해야 할 컴포저블도 제공
                    composable(Routes.Home.route) {
                        // hilt 모델 가져오기 MainViewModel
                        val mainViewModel = hiltViewModel<MainViewModel>()

                        // MainScreen 불러오기
                        MainScreen(
                            widthSize = widthSizeClass,
                            onExploreItemClicked = {
                                launchDetailsActivity(context = this@MainActivity, item = it)
                            },
                            onDateSelectionClicked = {
                                // 이동
                                navController.navigate(Routes.Calendar.route)
                            },
                            //  ViewModel 객체는 구성이 변경되는 동안 자동으로 보관,객체가 보유한 데이터는 다음 활동
                            //  또는 프래그먼트 인스턴스에서 즉시 사용할 수 있다
                            mainViewModel = mainViewModel
                        )
                    }
                    composable(Routes.Calendar.route) {
                        // 이전에 돌아가는 것 인데 그것을 기억
                        val parentEntry = remember {
                            navController.getBackStackEntry(Routes.Home.route)
                        }
                        val parentViewModel = hiltViewModel<MainViewModel>(
                            parentEntry
                        )
                        CalendarScreen(onBackPressed = {
                            //  종료 조건과 관련된 함수
                            navController.popBackStack()
                        }, mainViewModel = parentViewModel)
                    }
                }
            }
        }
    }
}

// 동일 파일에 정의된 하위 클래스 외에 다른 하위 클래스는 존재하지 않는다는 것을 컴파일러에게 알려주는 것
sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Calendar : Routes("calendar")
    object Login : Routes("login")
}

@Composable
fun LoginScreen() {


    val focusManager = LocalFocusManager.current


    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    /*유효한지 확인*/
    val isEmailValid by derivedStateOf {
        Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val isPasswordValid by derivedStateOf {
        password.length > 7
    }

    var isPasswordVisible by remember {
        mutableStateOf(false)
    }



    Column(
        modifier = Modifier
            .background(color = Color.LightGray)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_baseline_account_circle_24),
            contentDescription = "Account Logo",
            modifier = Modifier.size(150.dp)

        )


        Text(
            text = "로그인",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            fontSize = 30.sp,
            modifier = Modifier.padding(bottom = 16.dp)

        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(all = 10.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("이메일 주소") },
                    placeholder = { Text("abc@domain.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = !isEmailValid,
                    trailingIcon = {
                        if (email.isNotBlank()) {
                            IconButton(onClick = { email = ""}) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "이메일 지우기")


                            }
                        }
                    }
                )


                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("비밀번호") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = !isPasswordValid,
                    trailingIcon = {
                        IconButton(
                            onClick = { isPasswordVisible = !isPasswordVisible }
                        ) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "비밀번호 보이게 전환")
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )

                Button(
                    // 클릭시 홈 화면으로 이동
                    onClick = {  /*TODO*/},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    enabled = isEmailValid && isPasswordValid
                ) {
                    Text(
                        text = "로그인",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) { TextButton(
            onClick = { /*TODO*/ }
        ) {
            Text(
                color = Color.Black,
                fontStyle = FontStyle.Italic,
                text = "비밀번호 찾기",
                modifier = Modifier.padding(end = 10.dp)
            )
        }
        }
        Button(
            onClick = { /*TODO*/ },
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
        ) {
            Text(
                text = "계정 만들기",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}






@VisibleForTesting
@Composable
fun MainScreen(
    // 여러가지 설정
    widthSize: WindowWidthSizeClass,
    onExploreItemClicked: OnExploreItemClicked,
    onDateSelectionClicked: () -> Unit,
    mainViewModel: MainViewModel
) {
    // 색상 지정 (표면)
    Surface(
        // insets내용이 공백에 들어가지 않도록 패딩을 추가함
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.Start + WindowInsetsSides.End)
        ),
        // 색상 지정
        color = MaterialTheme.colors.primary
    ) {
        //  메모리에 객체를 저장 (remember),  전환 애니메이션의 과정을 변경하도록 변경할 수 있다 (MutableTransitionState)
        val transitionState = remember { MutableTransitionState(mainViewModel.shownSplash.value) }
        // 상태 업데이트 -> splash 전환
        val transition = updateTransition(transitionState, label = "splashTransition")
        // splashAlpha라는 자식 에니메이션으로 전환
        val splashAlpha by transition.animateFloat(
            // 에니메이션 지속 시간 설정
            transitionSpec = { tween(durationMillis = 100) }, label = "splashAlpha"
        ) {
            // 처음 상태 else 나중 상태
            if (it == SplashState.Shown) 1f else 0f
        }
        val contentAlpha by transition.animateFloat(
            // 애니메이션 지속 시간 설정
            transitionSpec = { tween(durationMillis = 300) }, label = "contentAlpha"
        ) {
            // 처음 상태 else 나중 상태
            if (it == SplashState.Shown) 0f else 1f
        }
        // 에니메이션 전환을 DP로 관리
        val contentTopPadding by transition.animateDp(
            // 모든 저강성 사용을 정의 -> 대상에 반올림할 수 있을 정도로 시각적으로 가깝게 임계값 정의
            transitionSpec = { spring(stiffness = StiffnessLow) }, label = "contentTopPadding"
        ) {
            // 처음 상태 else 나중 상태
            if (it == SplashState.Shown) 100.dp else 0.dp
        }

        Box {
            LandingScreen(
                // 1보다 더 적은 알파 수정 내용을 그린다.
                modifier = Modifier.alpha(splashAlpha),
                onTimeout = {
                    // 가려는 상태와 메인 뷰 모델 값
                    transitionState.targetState = SplashState.Completed
                    mainViewModel.shownSplash.value = SplashState.Completed
                }
            )

            MainContent(
                modifier = Modifier.alpha(contentAlpha),
                topPadding = contentTopPadding,
                widthSize = widthSize,
                onExploreItemClicked = onExploreItemClicked,
                onDateSelectionClicked = onDateSelectionClicked,
                viewModel = mainViewModel
            )
        }
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp,
    widthSize: WindowWidthSizeClass,
    onExploreItemClicked: OnExploreItemClicked,
    onDateSelectionClicked: () -> Unit,
    viewModel: MainViewModel
) {

    Column(modifier = modifier) {
        // 빈 레이아웃 공간보여주는 컴포넌트
        Spacer(Modifier.padding(top = topPadding))
        CraneHome(
            widthSize = widthSize,
            modifier = modifier,
            onExploreItemClicked = onExploreItemClicked,
            onDateSelectionClicked = onDateSelectionClicked,
            viewModel = viewModel
        )
    }
}

enum class SplashState { Shown, Completed }


