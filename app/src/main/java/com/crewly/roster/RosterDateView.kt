package com.crewly.roster

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.crewly.R
import com.crewly.duty.Duty
import com.crewly.duty.DutyIcon
import com.crewly.duty.ryanair.RyanairDutyIcon
import com.crewly.duty.ryanair.RyanairDutyType
import com.crewly.utils.evenPadding
import com.crewly.utils.inflate
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.calendar_date_view.view.*

/**
 * Created by Derek on 07/07/2018
 */
class RosterDateView @JvmOverloads constructor(
  context: Context,
  attributes: AttributeSet? = null,
  defStyle: Int = 0,
  var imageTintList: ColorStateList? = null,
  var offImageTintList: ColorStateList? = null
):
  RelativeLayout(context, attributes, defStyle) {

  private val fullImagePadding = context.resources.getDimensionPixelOffset(R.dimen.roster_date_full_image_padding)
  private val imagePadding = context.resources.getDimensionPixelOffset(R.dimen.roster_date_image_padding)

  private val disposables = CompositeDisposable()
  private var rosterDate: RosterPeriod.RosterDate? = null
  private var clickAction: ((rosterDate: RosterPeriod.RosterDate) -> Unit)? = null

  init {
    inflate(R.layout.calendar_date_view, attachToRoot = true)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    observeViewClicks()
  }

  override fun onDetachedFromWindow() {
    disposables.clear()
    super.onDetachedFromWindow()
  }

  fun bindToRosterDate(
    rosterDate: RosterPeriod.RosterDate,
    isCurrentDay: Boolean,
    clickAction: ((rosterDate: RosterPeriod.RosterDate) -> Unit)? = null
  ) {
    this.rosterDate = rosterDate
    this.clickAction = clickAction

    text_date.text = rosterDate.date.dayOfMonth().asText
    text_date.isSelected = isCurrentDay

    val duties = rosterDate.duties
    if (dutiesContainsFlight(duties)) {
      val sectorSize = rosterDate.sectors.size
      text_number.text = if (sectorSize > 0) sectorSize.toString() else ""
      showImage(false)
    } else {

      val duty = duties[0]
      val dutyIcon = RyanairDutyIcon(dutyName = duty.type)
      val dutyIconResource = if (dutyIcon.iconResourceId == DutyIcon.NO_ICON) {
        R.drawable.icon_off
      } else {
        dutyIcon.iconResourceId
      }

      image_calendar_date.setImageResource(dutyIconResource)
      showImage(true)

      when (duty.type) {
        RyanairDutyType.AIRPORT_STANDBY.dutyName -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
        }

        RyanairDutyType.HOME_STANDBY.dutyName -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
        }

        RyanairDutyType.OFF.dutyName -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_XY
          image_calendar_date.evenPadding(fullImagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, offImageTintList)
        }

        RyanairDutyType.ANNUAL_LEAVE.dutyName -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, null)
        }

        RyanairDutyType.SICK.dutyName -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, null)
        }

        RyanairDutyType.PARENTAL_LEAVE.dutyName -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, null)
        }

        else -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_XY
          image_calendar_date.evenPadding(fullImagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, offImageTintList)
        }
      }
    }

    observeViewClicks()
  }

  private fun showImage(show: Boolean) {
    image_calendar_date.isVisible = show
    text_number.isVisible = !show
  }

  private fun observeViewClicks() {
    disposables + this
      .throttleClicks()
      .subscribe {
        val rosterDate = this.rosterDate
        if (rosterDate != null) {
          clickAction?.invoke(rosterDate)
        }
      }
  }

  private fun dutiesContainsFlight(duties: List<Duty>): Boolean {
    run loop@{
      duties.forEach {
        if (it.type == RyanairDutyType.FLIGHT.dutyName) {
          return true
        }
      }
    }

    return false
  }
}