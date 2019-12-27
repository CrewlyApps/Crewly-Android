package com.crewly.crew

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import kotlinx.android.synthetic.main.rank_view.view.*

/**
 * Created by Derek on 23/06/2018
 */
class RankView: ConstraintLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  var rankDisplayData: RankDisplayData? = null
    set(value) {
      value?.let {
        image_rank.setImageResource(it.iconRes)
        text_rank_name.text = it.rank.getName()
      }

      field = value
    }

  init {
    setBackgroundResource(R.drawable.rank_view_statelist)
    View.inflate(context, R.layout.rank_view, this)

    val horizontalPadding = context.resources.getDimensionPixelOffset(R.dimen.rank_view_horizontal_padding)
    val verticalPadding = context.resources.getDimensionPixelOffset(R.dimen.rank_view_vertical_padding)
    setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
  }
}