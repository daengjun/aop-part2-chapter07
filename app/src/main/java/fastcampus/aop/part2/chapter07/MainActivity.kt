package fastcampus.aop.part2.chapter07

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    // 입력 되는 음성 값에 따라서 높낮이를 화면에 표시 (커스텀뷰 사용)
    private val soundVisualizerView: SoundVisualizerView by lazy {
        findViewById(R.id.soundVisualizerView)
    }

    // 녹화 파일 시간 표시 버튼 (커스텀뷰 사용)
    private val recordTimeTextView: CountUpView by lazy {
        findViewById(R.id.recordTimeTextView)
    }

    // 초기화 버튼
    private val resetButton: Button by lazy {
        findViewById(R.id.resetButton)
    }

    // 녹화 , 재생 , 정지 버튼 (커스텀뷰 사용)
    private val recordButton: RecordButton by lazy {
        findViewById(R.id.recordButton)
    }


    // 녹화하기 위해서 사용자에게 권한을 허용 받아야됨
    // Manifest.permission.RECORD_AUDIO 권한 추가

    private val requiredPermissions = arrayOf(Manifest.permission.RECORD_AUDIO)


    // 오디오 저장할 경로 (외장 메모리 캐시 폴더에 저장)
    private val recordingFilePath: String by lazy {
        "${externalCacheDir?.absolutePath}/recording.3gp"

    }

    // MediaRecorder 객체 선언
    // lateinit(늦은 초기화)은 non-null만 가능함
    private var recorder: MediaRecorder? = null

    // player 객체 선언
    private var player: MediaPlayer? = null

    // 레코드 버튼의 상태 지정 (녹화전 상수 대입)
    private var state = State.BEFORE_RECORDING
        // 프로퍼티 속성을 이용함 (입력받을때) var은 set 사용가능
        set(value) {
            // field <- state 현재의 값
            field = value
            // 리셋 버튼은 현재 재생중 , 재생전인 상태만 사용가능
            resetButton.isEnabled = (value == State.AFTER_RECORDING) || (value == State.ON_PLAYING)
            recordButton.updateIconWithState(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 권한 허용
        requestAudioPermission()

        // 레코드 상태 초기화
        initViews()

        // 레코드 버튼과  soundVisualizerView 초기화
        bindViews()

        // 처음에 리셋 버튼 비 활성화 시키기 위해서
        recordVariables()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
                    grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (!audioRecordPermissionGranted) {
            if (!shouldShowRequestPermissionRationale(permissions.first())) {
                showPermissionExplanationDialog()
            } else {
                finish()
            }
        }
    }

    private fun requestAudioPermission() {
        requestPermissions(requiredPermissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun initViews() {
        recordButton.updateIconWithState(state)
    }

    private fun bindViews() {
        // 고차 함수 이용 매개변수 x ,반환값만 존재
        soundVisualizerView.onRequestCurrentAmplitude = {
            recorder?.maxAmplitude ?: 0 // 엘비스 연산자 이용해서 값이 null인 경우 0을 넣어줌
       //  maxAmplitude 메서드 : 이 메서드에 대한 마지막 호출 이후 샘플링된 최대 절대 진폭을 반환합니다. setAudioSource() 다음에만 호출합니다.
        }

        // 리셋 버튼 onclick 설정
        resetButton.setOnClickListener {

            // stopPlaying()의경우 재생중 일때도 종료 하기 위해서 +  player 해제 + 레코드 버튼 아이콘 변경
            stopPlaying()
            soundVisualizerView.clearVisualization() // 화면 비우기
            recordTimeTextView.clearCountTime() // 시간 초기화
            recordVariables(); // 상태 레코딩 으로 변경

        }

        // 레코드 버튼의 클릭 이벤트 , state 상태 값에 따라 다르게 실행
        recordButton.setOnClickListener {
            when (state) {
                // 녹화 전일때 클릭시
                State.BEFORE_RECORDING -> {
                    startRecording()
                }
                // 녹화중일때 클릭시
                State.ON_RECORDING -> {
                    stopRecording()
                }
                // 재생전일때 클릭시
                State.AFTER_RECORDING -> {
                    startPlaying()
                }
                // 재생 중일때 클릭시
                State.ON_PLAYING -> {
                    stopPlaying()
                }
            }
        }
    }

    // 녹화 전 상태로 상수값 변경
    private fun recordVariables() {
        state = State.BEFORE_RECORDING
    }


    // 녹음하기 전에 기본 설정들
    private fun startRecording() {

        // MediaRecorder() 초기화 자세한 내용을 개발자 문서 참고
        recorder = MediaRecorder()
            .apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                // 파일이 저장될 경로
                setOutputFile(recordingFilePath)
                prepare()
            }
        recorder?.start()

        soundVisualizerView.startVisualizing(false)

        // 시간 카운트 시작
        recordTimeTextView.startCountUp()
        state = State.ON_RECORDING // 레코드 아이콘 상태 변경
    }

    // 녹음 종료
    private fun stopRecording() {

        recorder?.run {
            stop()
            release()
        }
        recorder = null


        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING  // 레코드 아이콘 상태 변경
    }

    // 재생 시작
    private fun startPlaying() {
        player = MediaPlayer()
            .apply {
                // 파일을 읽어옴
                setDataSource(recordingFilePath) // 미디어가 저장된 경로 전달
                prepare()
            }

        // 파일을 다읽었을때 동작하는 리스너 설정
        player?.setOnCompletionListener {
            stopPlaying() // 종료
            state = State.AFTER_RECORDING  // 레코드 아이콘 상태 변경
        }

        player?.start() // 재생
        soundVisualizerView.startVisualizing(true)
        recordTimeTextView.startCountUp()
        state = State.ON_PLAYING  // 레코드 아이콘 상태 변경
    }

    // 재생 종료
    private fun stopPlaying() {
        player?.release() // player 해제
        player = null
        soundVisualizerView.stopVisualizing()
        recordTimeTextView.stopCountUp()
        state = State.AFTER_RECORDING  // 레코드 아이콘 상태 변경
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜주셔야지 앱을 정상적으로 사용할 수 있습니다. 앱 설정 화면으로 진입하셔서 권한을 켜주세요.")
            .setPositiveButton("권한 변경하러 가기") { _, _ -> navigateToAppSetting() }
            .setNegativeButton("앱 종료하기") { _, _ -> finish() }
            .show()
    }

    private fun navigateToAppSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 201
    }
}
