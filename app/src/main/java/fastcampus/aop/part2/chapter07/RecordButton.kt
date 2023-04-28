package fastcampus.aop.part2.chapter07

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton


// 레코드 버튼 커스텀 뷰
class RecordButton(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageButton(context, attrs) {

    // shape drawable을 활용한 배경 변경 (init은 클래스가 시작될때 제일먼저 실행됨)
    init {
        setBackgroundResource(R.drawable.shape_oval_button)
    }

    // setImageResource 메서드를 사용해서 들어오는 상태값에 따라 이미지 변경
    fun updateIconWithState(state: State) {
        when (state) {
            State.BEFORE_RECORDING -> {
                setImageResource(R.drawable.ic_record)
            }
            State.ON_RECORDING -> {
                setImageResource(R.drawable.ic_stop)
            }
            State.AFTER_RECORDING -> {
                setImageResource(R.drawable.ic_play)
            }
            State.ON_PLAYING -> {
                setImageResource(R.drawable.ic_stop)
            }
        }
    }
}
