package com.hellodoc.healthcaresystem.user.personal
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.hellodoc.healthcaresystem.R
import com.hellodoc.healthcaresystem.responsemodel.User
import com.hellodoc.healthcaresystem.user.home.ZoomableImageDialog
import com.hellodoc.healthcaresystem.user.personal.otherusercolumn.InteractPostManager
import com.hellodoc.healthcaresystem.user.personal.otherusercolumn.PostColumn
import com.hellodoc.healthcaresystem.user.post.userId
import com.hellodoc.healthcaresystem.viewmodel.PostViewModel
import com.hellodoc.healthcaresystem.viewmodel.UserViewModel

var userId = ""
var userModel = ""

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewProfileUserPage() {
    // Fake SharedPreferences
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("preview_prefs", Context.MODE_PRIVATE)

    // Fake NavController
    val navController = rememberNavController()
    ProfileUserPage(
        sharedPreferences = sharedPreferences,
        navHostController = navController
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileUserPage(
    sharedPreferences: SharedPreferences,
    navHostController: NavHostController
) {
    val context = LocalContext.current

    // Khởi tạo ViewModel bằng custom factory để truyền SharedPreferences
    val userViewModel: UserViewModel = viewModel(factory = viewModelFactory {
        initializer { UserViewModel(sharedPreferences) }
    })

    val postViewModel: PostViewModel = viewModel(factory = viewModelFactory {
        initializer { PostViewModel(sharedPreferences) }
    })
//    val comments by postViewModel.comments.collectAsState()
    var shouldReloadPosts by remember { mutableStateOf(false) }
    val navEntry = navHostController.currentBackStackEntry
    val reloadTrigger = navEntry?.savedStateHandle?.getLiveData<Boolean>("shouldReload")?.observeAsState()

    LaunchedEffect(Unit) {
        userId = userViewModel.getUserAttributeString("userId")
        userModel = if (userViewModel.getUserAttributeString("role") == "user") "User" else "Doctor"
    }
    LaunchedEffect(reloadTrigger?.value) {
        if (reloadTrigger?.value == true) {
            postViewModel.getAllPosts() // gọi lại danh sách mới
            navHostController.currentBackStackEntry
                ?.savedStateHandle?.set("shouldReload", false)
        }
    }
    // Gọi API để fetch user từ server
    LaunchedEffect(userId, shouldReloadPosts) {
        if (userId.isNotEmpty()) {
            userViewModel.getUser(userId)
            postViewModel.getPostByUserId(userId)
        }
    }

    // Lấy dữ liệu user từ StateFlow
    val user by userViewModel.user.collectAsState()
    // Nếu chưa có user (null) thì không hiển thị giao diện
    if (user==null) return


    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    if (selectedImageUrl != null) {
        ZoomableImageDialog(selectedImageUrl = selectedImageUrl, onDismiss = { selectedImageUrl = null })
    }
    var reportedPostId by remember { mutableStateOf<String?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showFullScreenComment by remember { mutableStateOf(false) }
    var selectedPostIdForComment by remember { mutableStateOf<String?>(null) }
    var showReportBox by remember { mutableStateOf(false) }
    val posts by postViewModel.posts.collectAsState()


    // Nếu có user rồi thì hiển thị UI
    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures {
                postViewModel.closeAllPostMenus()  //tắt menu post
                showReportBox = false
            }
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ProfileSection(
                    navHostController = navHostController,
                    user = user!!,
                    onClickShowReport = { showReportDialog = true },
                    onImageClick = { selectedImageUrl = it },
                    showReportBox = showReportBox,
                    onToggleReportBox = { showReportBox = !showReportBox }
                )
            }
            item {
                PostColumn(
                    posts = posts,
                    postViewModel = postViewModel,
                    userId = userId ?: "",
                    navController = navHostController,
                    onClickReport = { postId ->
                        reportedPostId = postId
                        showReportDialog = true
                    },
                    onShowComment = { postId ->
                        selectedPostIdForComment = postId
                        showFullScreenComment = true
                    }
                )
            }
        }
        if ((showFullScreenComment && selectedPostIdForComment != null) ||
            (showReportDialog && user != null)) {
            print("vo duoc interact post manager")
            InteractPostManager(
                navHostController,
                user,
                postViewModel,
                reportedPostId,
                context,
                showFullScreenComment,
                selectedPostIdForComment,
                showReportDialog,
                onCloseComment = {
                    showFullScreenComment = false
                },
                onHideReportDialog = {
                    showReportDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileSection(
    navHostController: NavHostController,
    user: User, onClickShowReport: () -> Unit,
    onImageClick: (String) -> Unit,
    showReportBox: Boolean,
    onToggleReportBox: () -> Unit
)
{
    Column(
        modifier = Modifier.background(Color.Cyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            UserIntroSection(
                user = user,
                onClickShowReport = onClickShowReport,
                navController = navHostController,
                onImageClick = onImageClick,
                showReportBox = showReportBox,
                onToggleReportBox = onToggleReportBox
            )
            Spacer(modifier = Modifier.height(26.dp))
            UserProfileModifierSection(navHostController, user)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun UserIntroSection(
    user: User,
    onClickShowReport: () -> Unit,
    navController: NavHostController,
    onImageClick: (String) -> Unit,
    showReportBox: Boolean,
    onToggleReportBox: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            // Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.2f))
            ) {
                AsyncImage(
                    model = user.avatarURL,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable {
                            onImageClick(user.avatarURL)
                        },
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                user.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                user.email,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }

        // Icon 3 chấm
        IconButton(
            onClick = { onToggleReportBox() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more),
                contentDescription = "Menu",
                tint = Color.Black
            )
        }

        // Report Box
        if (showReportBox) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 8.dp)
                    .width(250.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onToggleReportBox()
                            onClickShowReport()
                        }
                ) {
                    Text("Tố cáo & Báo lỗi", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Phản ánh vi phạm hoặc lỗi hệ thống", fontSize = 13.sp)
                }

                Divider(
                    color = Color.LightGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onToggleReportBox()
                            navController.navigate("activity_manager")
                        }
                ) {
                    Text("Quản lý hoạt động", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Xem và kiểm soát các hoạt động", fontSize = 13.sp)
                }
            }
        }
    }
}


@Composable
fun UserProfileModifierSection(navHostController: NavHostController, user: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Button(
            onClick = { navHostController.navigate("editProfile") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(60.dp)
                .width(128.dp)
        ) {
            Text(
                text = "Chỉnh sửa hồ sơ",
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = {
                if (user == null) {
                    return@Button
                }
                else if (user.role=="user"){
                    navHostController.navigate("doctorRegister")
                }
                else{
                    navHostController.navigate("editClinic")
                }
                 },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(60.dp)
                .width(128.dp)
        ) {
            Text(
                text = "Quản lý phòng khám",
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}




