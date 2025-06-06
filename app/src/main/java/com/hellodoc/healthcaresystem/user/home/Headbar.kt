package com.hellodoc.healthcaresystem.user.home

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hellodoc.healthcaresystem.R
import com.hellodoc.healthcaresystem.viewmodel.UserViewModel

fun shortenUserName(fullName: String): String {
    val parts = fullName.trim().split("\\s+".toRegex())
    return if (parts.size >= 2) {
        val firstInitial = parts.first().first().uppercaseChar()
        val lastName = parts.last()
        "$firstInitial. $lastName"
    } else {
        fullName // không cần rút gọn nếu chỉ có 1 từ
    }
}

@Composable
fun Headbar(
    sharedPreferences: SharedPreferences
) {
    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel(factory = viewModelFactory {
        initializer { UserViewModel(sharedPreferences) }
    })
    val users by viewModel.users.collectAsState()
    var userName by remember { mutableStateOf("Người dùng") }
    var role by remember { mutableStateOf("Người dùng") }

    LaunchedEffect(Unit) {
        viewModel.getAllUsers()
        userName = viewModel.getUserNameFromToken()
        role = viewModel.getUserRole()
    }

    Box(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .background(color = Color(0xFFE0E0E0)).padding(vertical = 5.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Hình bác sĩ ở giữa
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "com.hellodoc.healthcaresystem.user.home.doctor.com.hellodoc.healthcaresystem.user.home.doctor.Doctor Icon",
            modifier = Modifier.size(200.dp)
        )

        // Row for left icon and right user info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "HelloDoc",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            val shortName = truncateName(userName, 10)
            // Cột chứa Text và nút logout ở bên phải
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Xin chào \n$shortName",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Left,
                    lineHeight = 20.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.logout(context) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logout),
                        contentDescription = "Logout",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

fun truncateName(name: String, maxLength: Int): String {
    return if (name.length > maxLength) {
        name.take(maxLength) + "..."
    } else {
        name
    }
}
