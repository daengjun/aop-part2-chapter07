package fastcampus.aop.part2.chapter07

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CountUpView(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private var startTimeStamp: Long = 0L

    private val countUpAction: Runnable = object : Runnable {
        override fun run() {

            //처음에 시작한 시간값 - 현재 시간값을 해주면 해당 차이만큼의 값이 나옴

            val currentTimeStamp = SystemClock.elapsedRealtime()
            val countTimeSeconds = ((currentTimeStamp - startTimeStamp) / 1000L).toInt()
            updateCountTime(countTimeSeconds)

            handler?.postDelayed(this, 1000L) // 1초 뒤 재시작
        }
    }

    // 시간 시작
    fun startCountUp() {
        startTimeStamp = SystemClock.elapsedRealtime()
        // runable 시작
        handler?.post(countUpAction)
    }

    // 시간 종료
    fun stopCountUp() {
        // runable 종료
        handler?.removeCallbacks(countUpAction)
    }

    // 시간 초기화
    fun clearCountTime() {
        updateCountTime(0)
    }


    // 텍스트뷰에 표시 (분,초)
    @SuppressLint("SetTextI18n")
    private fun updateCountTime(countTimeSeconds: Int) {
        val minutes = countTimeSeconds / 60
        // 60으로 나누면 분이고

        val seconds = countTimeSeconds % 60
        // 60의 나머지는 초임
        // % 연산자는 60이라고 치면 0~59까지만 결과값이 나옴

        // String format으로 입력해줌 02는 두자리수 입력 한자리일경우 0으로 채움
        text = "%02d:%02d".format(minutes, seconds)
    }
}
