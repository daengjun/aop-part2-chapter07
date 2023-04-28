package fastcampus.aop.part2.chapter07

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class SoundVisualizerView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onRequestCurrentAmplitude: (() -> Int)? = null //람다식 구현 int값 반환 초기 , oncreate에서 값 진폭값 가져옴


    // ANTI_ALIAS_FLAG - 곡선이 부드럽게 그려진다


    // Paint 객체 초기화
    private val amplitudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // 색깔 지정
        color = context.getColor(R.color.purple_500)
        // 굵기 지정
        strokeWidth = LINE_WIDTH

        // 양끝을 둥근 모양으로 설정
        strokeCap = Paint.Cap.ROUND
    }


    // 화면의 가로
    private var drawingWidth: Int = 0

    // 화면의 세로
    private var drawingHeight: Int = 0

    // 진폭값 저장
    private var drawingAmplitudes: List<Int> = emptyList()

    // 재생중인 지 확인
    private var isReplaying: Boolean = false


    private var replayingPosition: Int = 0

    var count : Int = 0; // 내가 추가한것 테스트용


    // 반복해서 그려주기

    private val visualizeRepeatAction: Runnable = object : Runnable {
        override fun run() {

            // isReplaying -> 녹음중인지 재생중인지 확인하는 메서드
            if (!isReplaying) {

                // 레코더의 MAXAMAmplitudes값 가져옴
                // onRequestCurrentAmplitude?.invoke() 잘모르겠는데
                // 메인에서 람다함수 초기화하고 , 초기화된 람다 함수 안에 maxAmplitude 실행해서 진폭값 가져옴
                val currentAmplitude = onRequestCurrentAmplitude?.invoke() ?: 0

                // 현재의 라인부터 보여줌
                // drawingAmplitudes 리스트 0번 인덱스에 가장 최신의 진폭값 (가장 최근 입력된값을 저장함)
                // 왼쪽에서 오른쪽으로 그려지는것이니
                // 가장 오른쪽에 그려지는것이 가장 최근 진폭값 이걸 계속 그려줌 (반복해서)
                // 값이 없으면 0값이 들어감
                // 우리눈에는 오른쪽에서 왼쪽으로 흐르는것처럼 보임
                // 화면에 계속 그리는것일뿐
                // 화면이 꽉차면 현재 진폭값 부터 그다음 인덱스까지 그리는데
                // 마지막 인덱스까지 그리기 전에 반복문을 종료하기때문에 뒷부분에 쌓이는것들은 화면에 안나오게 되고
                // 새로운값은 게속 그려지게됨 그래서 흐르는것처럼 보여짐

                drawingAmplitudes = listOf(currentAmplitude) + drawingAmplitudes
            } else {
                // 녹음중이 아닐경우 리플레이 포지션 증가 시킴 (재생할때 사용)

                // 재생 시킬 인덱스값을 저장 이렇게해야 하나씩 하나씩 추가되면서 새로 갱신되는것처럼 보임
                replayingPosition++
                Log.d("replayingPosition", "run: $replayingPosition")
            }

            // 새로 그리기 ondraw가 여기서 호출
            invalidate()

            // 20밀리세컨드 간격으로 반복
            handler?.postDelayed(this, ACTION_INTERVAL)

        }
    }

    // 기기의 화면 사이즈를 가져옴
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingWidth = w
        drawingHeight = h
    }

    // 화면에 그려줌
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return

        val centerY = drawingHeight / 2f  // 절반에 위치

        var offsetX = drawingWidth.toFloat()

        drawingAmplitudes
            .let { amplitudes ->
                if (isReplaying) {

                    // 재생일때 처음이 배열 마지막으로 가니까 amplitudes 배열에 저장된 마지막에서 부터
                    // takeLast 마지막에서부터 전달된 인덱스만큼 1개 2개 3개이런식으로 가져오는거
                    // 처음에는 가장 마지막에 쌓인거 (녹음 시작했던부분) 을 보여주고
                    // 그다음 인덱스가 2일때는 처음값이 왼쪽으로 하나씩 밀려가면서 순차적으로 보여지게 됨
                    // 여기서 의문이였던게 321 순으로 보여줘야하는거 아닐까 했는데
                    // 왼쪽에서 오른쪽으로 흐르는것이기 때문에 맨 오른쪽이 처음 그려지는 인덱스고
                    // 이렇게 해야지 순차적으로 오른쪽에서 부터 왼쪽으로 그려지는것처럼 보임
                    // 예를들어서 어레이의 사이즈가 5인경우에
                    // 인덱스 5번이 가장 처음 녹음했던 것이라면
                    // 처음에 5 가 나올것이고
                    // 그다음이 5 4
                    // 그다음이 5 4 3
                    // 이런식으로 순차적으로 그려짐 (왼쪽으로 흐르는것처럼 보임)
                    // 만약 4 5
                    // 3 4 5 순으로 그려진다면 (처음 녹음했던 것처럼 나오는게아니라 반대로 나오며 화면이 가득차면 갱신이 되지않음 무조건 5부터 시작하니까..) 끗..


                    amplitudes.takeLast(replayingPosition)
                } else {
                    // 녹음 일때 일반적으로 보여줌
                    amplitudes
                }
            }
            .forEach { amplitude ->

                val lineLength = amplitude / MAX_AMPLITUDE * drawingHeight * 0.8F
                // 최대 크기 보다 조금 적게하기 위해서 * 0.8

                offsetX -= LINE_SPACE //간격 만큼 줄여 나감

                // 제일 마지막에 도달하면 종료
                if (offsetX < 0) {
                    return@forEach
                }


                // 여기서 ondraw를 호출하는거같은데
                canvas.drawLine(
                    offsetX,
                    centerY - lineLength / 2F,
                    offsetX,
                    centerY + lineLength / 2F,
                    amplitudePaint
                )
            }


    }

    // 비주얼라이즈 시작 , 리플레잉인지 아닌지 인자값으로 받음
    fun startVisualizing(isReplaying: Boolean) {
        this.isReplaying = isReplaying
        handler?.post(visualizeRepeatAction)
    }

    // 비주얼 라이즈 종료
    fun stopVisualizing() {
        // 0으로 초기화 해줘야 여러번 재생해도 똑같이 비주얼라이징 표현됨
        replayingPosition = 0
        handler?.removeCallbacks(visualizeRepeatAction)
    }

    // 리셋버튼 눌렀을때 비주얼 라이징 초기화
    fun clearVisualization() {
        drawingAmplitudes = emptyList()
        invalidate()
    }

    companion object {
        // 라인의 굵기
        private const val LINE_WIDTH = 10F

        // 라인의 간격
        private const val LINE_SPACE = 20F

        private const val MAX_AMPLITUDE = Short.MAX_VALUE.toFloat()
        // 그냥 나누면 0이 되니까 float으로 변환

        private const val ACTION_INTERVAL = 20L
    }
}
