package com.crewly.crew

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.crewly.R
import kotlinx.android.synthetic.main.rank_view.view.*

/**
 * Created by Derek on 23/06/2018
 */
class RankView @JvmOverloads constructor(context: Context,
                                         attributes: AttributeSet? = null,
                                         defStyle: Int = 0):
        ConstraintLayout(context, attributes, defStyle) {

    var rank: Rank = Rank.NONE
    set(value) {
        image_rank.setImageResource(value.getIconRes())
        text_rank_name.text = value.getName()
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