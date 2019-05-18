package com.crewly.crew

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.crewly.R
import com.crewly.account.Account
import com.crewly.utils.getColorCompat
import kotlinx.android.synthetic.main.crew_view.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
 * Created by Derek on 18/05/2019
 */
class CrewView: ConstraintLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  companion object {
    private val timeFormatter = DateTimeFormat.forPattern("yyyyMMdd")
  }

  var account: Account? = null
  set(value) {
    if (field == value) return
    value?.let {
      displayCrewCode(it.crewCode)
      displayCrewName(it.name)
      displayCrewRank(it.rank)
      displayCrewJoined(it.joinedCompanyAt)
    }
    field = value
  }

  init {
    View.inflate(context, R.layout.crew_view, this)
    setBackgroundColor(context.getColorCompat(R.color.highlight_background))
  }

  private fun displayCrewCode(crewCode: String) {
    text_crew_code.text = crewCode
  }

  private fun displayCrewName(name: String) {
    text_crew_name.text = name
  }

  private fun displayCrewRank(rank: Rank) {
    text_crew_rank.text = resources.getString(R.string.crew_rank, rank.getName())
  }

  private fun displayCrewJoined(joinedDate: DateTime) {
    val date = if (joinedDate.millis > 0) {
      timeFormatter.print(joinedDate)
    } else {
      resources.getString(R.string.unknown)
    }

    text_crew_joined.text = resources.getString(R.string.crew_joined, date)
  }
}