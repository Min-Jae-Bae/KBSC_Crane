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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class) // ?????????????????? ????????? ??????, ??????, ????????? opt-in API??? ????????? ??? ????????????.
@AndroidEntryPoint // ??????????????? ??? Android ???????????? ?????? ?????? Hilt ??????????????? ??????
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // false?????? ?????????????????? ????????? ????????? ????????? ????????? ?????? ????????? ????????? ?????? ??????
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ????????? ?????? ????????? ????????? ???????????? ????????? ??????????????? ??????
        setContent {
            CraneTheme {
                // ????????? ??????????????? ??? ????????? ???????????? ????????? ?????? ?????? ???????????? ?????? ?????? ?????? ?????? ????????? ???????????? ?????? ????????? new ??? ??????
                val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

                // ?????? ????????? ??? ?????? ????????? ???????????? ??????????????? ??? ????????? ??????
                val navController = rememberNavController()
                //  NavHost??? ?????? ????????? ????????? ???????????? ?????? ?????????, ????????? ?????? ??????
                NavHost(navController = navController, startDestination = Routes.Login.route) {

                /*  ????????? ????????????
               composable(Routes.Login.route) {
                        val loginViewModel = hiltViewModel<LoginViewModel>()
                        LoginScreen()
                    }*/


                    // composable() ???????????? ???????????? ?????? ????????? ??????, ???????????? ????????? ????????? ???????????? ??? ??????????????? ??????
                    composable(Routes.Home.route) {
                        // hilt ?????? ???????????? MainViewModel
                        val mainViewModel = hiltViewModel<MainViewModel>()

                        // MainScreen ????????????
                        MainScreen(
                            widthSize = widthSizeClass,
                            onExploreItemClicked = {
                                launchDetailsActivity(context = this@MainActivity, item = it)
                            },
                            onDateSelectionClicked = {
                                // ??????
                                navController.navigate(Routes.Calendar.route)
                            },
                            //  ViewModel ????????? ????????? ???????????? ?????? ???????????? ??????,????????? ????????? ???????????? ?????? ??????
                            //  ?????? ??????????????? ?????????????????? ?????? ????????? ??? ??????
                            mainViewModel = mainViewModel
                        )
                    }
                    composable(Routes.Calendar.route) {
                        // ????????? ???????????? ??? ?????? ????????? ??????
                        val parentEntry = remember {
                            navController.getBackStackEntry(Routes.Home.route)
                        }
                        val parentViewModel = hiltViewModel<MainViewModel>(
                            parentEntry
                        )
                        CalendarScreen(onBackPressed = {
                            //  ?????? ????????? ????????? ??????
                            navController.popBackStack()
                        }, mainViewModel = parentViewModel)
                    }
                }
            }
        }
    }
}

// ?????? ????????? ????????? ?????? ????????? ?????? ?????? ?????? ???????????? ???????????? ???????????? ?????? ?????????????????? ???????????? ???
sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Calendar : Routes("calendar")
    object Login : Routes("login")
}

/* ????????? ?????? ??????
@Composable
fun LoginScreen() {


    val focusManager = LocalFocusManager.current


    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    */
/*???????????? ??????*//*

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
            text = "?????????",
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
                    label = { Text("????????? ??????") },
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
                                    contentDescription = "????????? ?????????")


                            }
                        }
                    }
                )


                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("????????????") },
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
                                contentDescription = "???????????? ????????? ??????")
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )

                Button(
                    // ????????? ??? ???????????? ??????
                    onClick = {  */
/*TODO*//*
},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    enabled = isEmailValid && isPasswordValid
                ) {
                    Text(
                        text = "?????????",
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
            onClick = { */
/*TODO*//*
 }
        ) {
            Text(
                color = Color.Black,
                fontStyle = FontStyle.Italic,
                text = "???????????? ??????",
                modifier = Modifier.padding(end = 10.dp)
            )
        }
        }
        Button(
            onClick = { */
/*TODO*//*
 },
            enabled = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
        ) {
            Text(
                text = "?????? ?????????",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 16.sp
            )
        }
    }
}
*/






@VisibleForTesting
@Composable
fun MainScreen(
    // ???????????? ??????
    widthSize: WindowWidthSizeClass,
    onExploreItemClicked: OnExploreItemClicked,
    onDateSelectionClicked: () -> Unit,
    mainViewModel: MainViewModel
) {
    // ?????? ?????? (??????)
    Surface(
        // insets????????? ????????? ???????????? ????????? ????????? ?????????
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.navigationBars.only(WindowInsetsSides.Start + WindowInsetsSides.End)
        ),
        // ?????? ??????
        color = MaterialTheme.colors.primary
    ) {
        //  ???????????? ????????? ?????? (remember),  ?????? ?????????????????? ????????? ??????????????? ????????? ??? ?????? (MutableTransitionState)
        val transitionState = remember { MutableTransitionState(mainViewModel.shownSplash.value) }
        // ?????? ???????????? -> splash ??????
        val transition = updateTransition(transitionState, label = "splashTransition")
        // splashAlpha?????? ?????? ????????????????????? ??????
        val splashAlpha by transition.animateFloat(
            // ??????????????? ?????? ?????? ??????
            transitionSpec = { tween(durationMillis = 100) }, label = "splashAlpha"
        ) {
            // ?????? ?????? else ?????? ??????
            if (it == SplashState.Shown) 1f else 0f
        }
        val contentAlpha by transition.animateFloat(
            // ??????????????? ?????? ?????? ??????
            transitionSpec = { tween(durationMillis = 300) }, label = "contentAlpha"
        ) {
            // ?????? ?????? else ?????? ??????
            if (it == SplashState.Shown) 0f else 1f
        }
        // ??????????????? ????????? DP??? ??????
        val contentTopPadding by transition.animateDp(
            // ?????? ????????? ????????? ?????? -> ????????? ???????????? ??? ?????? ????????? ??????????????? ????????? ????????? ??????
            transitionSpec = { spring(stiffness = StiffnessLow) }, label = "contentTopPadding"
        ) {
            // ?????? ?????? else ?????? ??????
            if (it == SplashState.Shown) 100.dp else 0.dp
        }

        Box {
            LandingScreen(
                // 1?????? ??? ?????? ?????? ?????? ????????? ?????????.
                modifier = Modifier.alpha(splashAlpha),
                onTimeout = {
                    // ????????? ????????? ?????? ??? ?????? ???
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
        // ??? ???????????? ?????????????????? ????????????
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


