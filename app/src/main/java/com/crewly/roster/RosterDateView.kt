package com.crewly.roster

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.crewly.R
import com.crewly.db.sector.Sector
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
    if (fullDuties.containsFlight()) {
      val sectorSize = rosterDate.sectors.size
      if (sectorSize <= 0) return
      text_number.text = sectorSize.toString()
      showEarlyDayIndicator(rosterDate.sectors[0])
      showImage(false)

    } else {
      displayNonFlightDutyDay(fullDuties[0])
    }

    observeViewClicks()
  }

  private fun observeViewClicks() {
    disposables + throttleClicks()
      .subscribe {
        rosterDate?.let {
          clickAction?.invoke(it)
        }
      }
  }

  private fun displayNonFlightDutyDay(
    firstDutyOfDay: FullDuty
  ) {
    val dutyIcon = firstDutyOfDay.dutyIcon
    val hasIcon = dutyIcon.iconResourceId != DutyIcon.NO_ICON
    if (hasIcon) image_calendar_date.setImageResource(dutyIcon.iconResourceId)
    showImage(hasIcon)

    when {
      firstDutyOfDay.dutyType.isAirportStandby() -> {
        image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
        image_calendar_date.evenPadding(imagePadding)
        ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
      }

      firstDutyOfDay.dutyType.isHomeStandby() -> {
        image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
        image_calendar_date.evenPadding(imagePadding)
        ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
      }

      firstDutyOfDay.dutyType.isOff() -> {
        image_calendar_date.scaleType = ImageView.ScaleType.FIT_XY
        image_calendar_date.evenPadding(fullImagePadding)
        ImageViewCompat.setImageTintList(image_calendar_date, offImageTintList)
      }

      firstDutyOfDay.dutyType.isAnnualLeave() -> {
        image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
        image_calendar_date.evenPadding(imagePadding)
        ImageViewCompat.setImageTintList(image_calendar_date, null)
      }

      firstDutyOfDay.dutyType.isSick() -> {
        image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
        image_calendar_date.evenPadding(imagePadding)
        ImageViewCompat.setImageTintList(image_calendar_date, null)
      }

      firstDutyOfDay.dutyType.isParentalLeave() -> {
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

  private fun showImage(show: Boolean) {
    image_calendar_date.isVisible = show
    text_number.isVisible = !show
  }

  private fun showEarlyDayIndicator(
    firstSectorOfDay: Sector
  ) {
    val showEarlyDay = firstSectorOfDay.departureTime.hourOfDay < 10
    view_early_day.isVisible = showEarlyDay
  }

  private fun List<FullDuty>.containsFlight(): Boolean =
    find { fullDuty ->
      fullDuty.dutyType.isFlight()
    } != null
}