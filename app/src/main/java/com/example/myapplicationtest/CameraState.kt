package com.example.myapplicationtest

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.Locale


//カメラの状態を表すデータクラスとして用意
data class CameraState (
    //コンストラクタで必要なパラメータを受け取り、これを通じてカメラ関連の操作を行う
    val context: Context,
    val cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    val LifeCycleOwne: LifecycleOwner,
    val imageCapture: ImageCapture
){
    //カメラを起動するためのメソッド   //Contextを受け取り、PreviewViewオブジェクトを返す
    fun startCamera(ctx:Context):PreviewView{
        //previewViewオブジェクトを生成し、インプリメンテーションモードをコンパチブルに設定
        //他にもパフォーマンスモードがある（一部の機能が制限されてる）
        val previewView = PreviewView(ctx).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }

        //コンテキストに関連されたメインメインスレッドでキューに入れられたタスクを実行するエグゼキューターを返す
        val executor = ContextCompat.getMainExecutor(ctx)
        //アプリコンポーネントの呼び出しを実行するスレッド

        //カメラプロバイダーの初期化が完了した時に呼び出されるリスナーを登録
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also{
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            //カメラプロバイダを取得、プレビューを設定 バックカメラをデフォで選択
            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            //bindライフサイクル関数で、プレビューとイメージキャプチャのユースケースをカメラに関連付ける
            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    LifeCycleOwne,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }catch (e:Exception){
                //エラーが発生した場合はログ
                Log.e("CameraPreview","Use case binding failed",e)
            }
        },executor
        )
        //完成したビューをリターンで戻す
        return  previewView
    }

    //写真保存、インテントを使い、写真を別アプリに共有する機能
    private  val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    fun takePhoto(){
        //現在日時からファイルを作成し、ゲームに格納
        val name = SimpleDateFormat(
            FILENAME_FORMAT,
            Locale.US
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {//データを格納するために使用するクラス
            put(
                //DISPLAY_NAMEがファイル名に
                android.provider.MediaStore.MediaColumns.DISPLAY_NAME,
                name
            )
            put(
                //MIMEタイプ
                android.provider.MediaStore.MediaColumns.MIME_TYPE,
                "image/jpeg"
            )
            put(
                //保存先の相対パスを渡す
                android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                //フォルダ名
                "Pictures/CameraX-Image"
            )
        }

        //どのように出力したいか
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        //出力をメディアストアに保存するため、メディアストアのエントリーを追加
        //ImageCaptureクラスインスタンスであるImageCaptureオブジェクトのtakePictureを呼び出して撮影
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            //エグゼキュータ(Executor)と画像が保存される時のためのコールバックを渡す
            object :ImageCapture.OnImageSavedCallback{
                //画像キャプチャの失敗や保存の失敗の場合、エラーメッセージ
                override fun onError(exc: ImageCaptureException){
                    val msg = "失敗: ${exc.message}"
                    //Toastは簡単なメッセージを表示させるUIコンポーネント
                    Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
                    Log.e("Camera",msg,exc)
                }
                //成功で呼び出される
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "JPEGで保存 ${output.savedUri}"
                    Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()
                    Log.d("Camera",msg)

                    //ほかのアプリケーションに送信
                    Intent(Intent.ACTION_SEND).also { share ->
                        share.type = "image/*"
                        share.putExtra(Intent.EXTRA_STREAM,output.savedUri)
                        context.startActivity(
                            Intent.createChooser(
                                share,
                                "Share to"
                            )
                        )
                    }
                }
            }
        )
    }
}

//UI構築の際に、カメラ関連の状態を管理するための関数
@Composable
fun remenbreCameraState(
    //カメラで使用するコンテキストを取得
    context: Context = LocalContext.current,
    //現在のプロセスに関連するプロセスカメラプロバイダのインスタンスを取得    //実際のカメラの処理
    caemraProviderFuture: ListenableFuture<ProcessCameraProvider> =
        ProcessCameraProvider.getInstance(context),
    //現在のライフサイクルオーナー
    //プレビューとイメージキャプチャのユースケースをカメラにバインドする時に使用 ライフサイクル管理をカメラが管理
    LifeCycleOwne: LifecycleOwner  = LocalLifecycleOwner.current,       //見本と少し違う変数名(?)
    //高解像度かつ高品質の写真をキャプチャできるカメラのコンポーネント
    imageCapture: ImageCapture = ImageCapture.Builder().build()
    //リメンバーを使い、CameraStateインスタンスをComposable関数内で状態を保持し利用できる
) = remember(context, caemraProviderFuture, LifeCycleOwne){
    //引数はキーになっている　変更があった場合はキャッシュを削除し、新たに作成
    CameraState(
        context = context,
        cameraProviderFuture = caemraProviderFuture,
        LifeCycleOwne  = LifeCycleOwne,
        imageCapture = imageCapture
    )
}
