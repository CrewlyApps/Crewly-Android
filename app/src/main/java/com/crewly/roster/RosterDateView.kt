package com.crewly.roster

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.crewly.R
import com.crewly.models.duty.DutyIcon
import com.crewly.models.duty.FullDuty
import com.crewly.models.roster.RosterPeriod
import com.crewly.utils.evenPadding
import com.crewly.utils.inflate
import com.crewly.utils.plus
import com.crewly.utils.throttleClicks
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.calendar_date_view.view.*

/**
 * Created by Derek on 07/07/2018
 */
class RosterDateView: RelativeLayout {

  constructor(context: Context): super(context)
  constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
  constructor(context: Context, attributes: AttributeSet?, defStyle: Int = 0): super(context, attributes, defStyle)

  var imageTintList: ColorStateList? = null
  var offImageTintList: ColorStateList? = null

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

    val fullDuties = rosterDate.fullDuties
    if (dutiesContainsFlight(fullDuties)) {
      val sectorSize = rosterDate.sectors.size
      text_number.text = if (sectorSize > 0) sectorSize.toString() else ""
      showImage(false)
    } else {

      val fullDuty = fullDuties[0]
      val dutyIcon = fullDuty.dutyIcon
      val hasIcon = dutyIcon.iconResourceId != DutyIcon.NO_ICON
      if (hasIcon) image_calendar_date.setImageResource(dutyIcon.iconResourceId)
      showImage(hasIcon)

      when {
        fullDuty.dutyType.isAirportStandby() -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
        }

        fullDuty.dutyType.isHomeStandby() -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
        }

        fullDuty.dutyType.isOff() -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_XY
          image_calendar_date.evenPadding(fullImagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, offImageTintList)
        }

        fullDuty.dutyType.isAnnualLeave() -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, null)
        }

        fullDuty.dutyType.isSick() -> {
          image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
          image_calendar_date.evenPadding(imagePadding)
          ImageViewCompat.setImageTintList(image_calendar_date, null)
        }

        fullDuty.dutyType.isParentalLeave() -> {
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

  private fun dutiesContainsFlight(duties: List<FullDuty>): Boolean {
    run loop@{
      duties.forEach {
        if (it.dutyType.isFlight()) {
          return true
        }
      }
    }

    return false
  }
}