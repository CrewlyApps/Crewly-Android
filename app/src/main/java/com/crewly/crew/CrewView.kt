package com.crewly.crew

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.models.Rank
import com.crewly.models.crew.Crew
import com.crewly.utils.getColorCompat
import kotlinx.android.synthetic.main.crew_view.view.*

/**
 * Created by Derek on 18/05/2019
 */
class CrewView: ConstraintLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  var crew: Crew? = null
  set(value) {
    if (field == value) return
    value?.let {
      displayCrewCode(it.id)
      displayCrewName(it.name)
      displayCrewRank(it.rank)
    }
    field = value
  }

  init {
    View.inflate(context, R.layout.crew_view, this)
    setBackgroundColor(context.getColorCompat(R.color.highlight_background))
  }

  fun addBottomMargin() {
    val bottomMargin = context.resources.getDimensionPixelOffset(R.dimen.roster_details_event_bottom_margin)
    var layoutParams = layoutParams

    if (layoutParams != null) {
      (layoutParams as MarginLayoutParams).bottomMargin = bottomMargin
    } else {
      layoutParams = MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.WRAP_CONTENT)
      layoutParams.bottomMargin = bottomMargin
    }

    this.layoutParams = layoutParams
  }

  private fun displayCrewCode(crewCode: String) {
    text_crew_code.text = crewCode
  }

  private fun displayCrewName(name: String) {
    text_crew_name.text = name
  }

  private fun displayCrewRank(rank: Rank) {
    text_crew_rank.text = rank.getName()
  }
}