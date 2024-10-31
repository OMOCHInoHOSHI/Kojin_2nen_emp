package com.example.myapplicationtest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chaquo.python.Python.getInstance
import com.example.myapplicationtest.ui.theme.MyApplicationTestTheme
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()    //スマホの端を無くす

        // ContentResolverを取得
        val content_Resolver = contentResolver
        setContent {
            MyApplicationTestTheme {
                //Surfaceは content colorを決める役割がある   //ScaffoldにSurfaceが含まれる
                Scaffold(modifier = Modifier.fillMaxWidth()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )

                    var str by remember { mutableStateOf("あんどろ") }
                    Box {
                        Greeting(
                            str, onClick = {str = "ボタンがタップされました"},
                            modifier = Modifier.padding(innerPadding)   //余白
                        )
                    }

                    //カメラ起動S------------------------------------------------------------
                    var flg by remember { mutableIntStateOf(0) } // flg の状態を管理する
                    FilledTonalButton(
                        onClick = { flg = 1 },
                        modifier = Modifier.size(80.dp).padding(1.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoCamera, // カメラのアイコンに変更
                            contentDescription = "カメラ起動",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        //Text(text = "カメラ起動")
                    }
                    if(flg == 1){
                        //フォルダから写真を選択するS---------------------------------------------------------
                        //uri_getに取得したuriを格納する
                        var uri_get by remember { mutableStateOf(Uri.EMPTY) }
                        uri_get=
                            content(
                                //何も選択しない場合
                                onNothingSelected = {
                                    // Handle nothing selected, e.g., show a message or log an event
                                    Log.d("MainActivity", "No image selected")
                                }
                            )
                        //フォルダから写真を選択するE---------------------------------------------------------
                        //取得したUriをBitmapに変換
                        val bitmap: Bitmap? = uri_get.getBitmapOrNull(contentResolver)

                        //カメラ権限呼び出し
//                        CameraScreen()
                    }
                    //カメラ起動E------------------------------------------------------------

                }
            }
        }
        //python追加-------------------------------------------------------------
        val py = getInstance()
        val module = py.getModule("hello")
        val txt1 = module.callAttr("hello_world")
        val txt2 = module.callAttr("set_text", "Good Morning")
        println(txt1)   // logに出力。Logcatに出力される
        println(txt2)   // logに出力。Logcatに出力される

        val num1 = module.callAttr("test_numpy")
        val num2 = module.callAttr("test_pandas")
        println(num1)
        println(num2)

        val tex3 = module.callAttr("hellow_yolo")
        println(tex3)
        //追加-------------------------------------------------------------
    }
}

@Composable
fun content(
    onNothingSelected: () -> Unit
):Uri?{
    //初期値に空のURI
    var pickedImageUri by remember { mutableStateOf(Uri.EMPTY) }
    val launcher = rememberLauncherForActivityResult(
        //像選択のインテントを起動するための ActivityResultLauncher を作成
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->    //Uri?はヌル許容型
        uri?.let {
            //pickedImageUriに選択された画像のuriを代入
            pickedImageUri = it
        } ?: onNothingSelected()    //uriがnull
    }
    //ライフサイクル   //Composeが最初に組み込まれたときに実行
    LaunchedEffect(true) {
        //1つの画像を選択
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    return pickedImageUri
}

@Composable
fun Greeting(name: String,
             onClick: () -> Unit = {},
             modifier: Modifier = Modifier) {

    //ボタンコンポーザー
    //ボタンを押すとテキストを表示S---------------------
    Button(onClick = onClick) {
        //テキストを表示
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
    //ボタンを押すとテキストを表示E---------------------
}

@Composable
fun PhotoPicker(onPickPhoto: () -> Unit) {
    Button(onClick = { onPickPhoto() }) {
        Text(text = "写真を選択")
    }
}



//プレビューのためのテストコードS----------------------------------------------------
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTestTheme {
//        var str by remember { mutableStateOf("あんどろ") }
//
////        var moji="ボタンがタップされました-"
//        var moji= stringResource(id=R.string.app_name)
//
//        Box {
//            Greeting(str, onClick = {str = moji})
//        }

        var flg by remember { mutableIntStateOf(0) } // flg の状態を管理する

        FilledTonalButton(
            onClick = { flg = 1 },
            modifier = Modifier.size(80.dp).padding(8.dp)

        ) {
            Text(text = "カメラ起動")
        }
        if(flg == 1){
            //カメラ権限呼び出し
            CameraScreen()
        }

    }
}