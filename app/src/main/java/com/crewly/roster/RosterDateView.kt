package com.crewly.roster

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.crewly.R
import com.crewly.models.duty.Duty
import com.crewly.models.duty.DutyType
import com.crewly.models.roster.RosterPeriod
import com.crewly.models.sector.Sector
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

    val duties = rosterDate.duties
    val sectors = rosterDate.sectors
    if (sectors.isNotEmpty()) {
      displayFlightDutyDay(
        flights = sectors
      )
    } else {
      val standbyDuty = duties.find { fullDuty ->
        fullDuty.type.isHomeStandby() || fullDuty.type.isAirportStandby()
      }

      text_number.text = ""
      showEarlyDayIndicator(standbyDuty)
      displayNonFlightDutyDay(duties[0])
    }

    setUpSpecialIcon(rosterDate)
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

  private fun setUpSpecialIcon(
    rosterDate: RosterPeriod.RosterDate
  ) {
    val hasSpecialEvent = rosterDate.duties.containsSpecialEvent()
    if (hasSpecialEvent) {
      image_extra_info.isVisible = hasSpecialEvent
      image_extra_info.setImageResource(R.drawable.icon_special_event)
      return
    }
  }

  private fun displayFlightDutyDay(
    flights: List<Sector>
  ) {
    if (flights.size == 1) {
      text_number.text = flights[0].arrivalAirport
    } else {
      text_number.text = flights.size.toString()
    }

    showEarlyDayIndicator(flights[0])
    showImage(false)
  }

  private fun displayNonFlightDutyDay(
    firstDutyOfDay: Duty
  ) {
    val dutyIcon = getDutyIcon(firstDutyOfDay.type)
    val hasIcon = dutyIcon != -1
    if (hasIcon) image_calendar_date.setImageResource(dutyIcon)
    showImage(hasIcon)

    when {
      firstDutyOfDay.type.isAirportStandby() ||
      firstDutyOfDay.type.isHomeStandby() ||
      firstDutyOfDay.type.isClear() -> {
        image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
        image_calendar_date.evenPadding(imagePadding)
        ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
      }

      firstDutyOfDay.type.isOff() -> {
        image_calendar_date.scaleType = ImageView.ScaleType.FIT_XY
        image_calendar_date.evenPadding(fullImagePadding)
        ImageViewCompat.setImageTintList(image_calendar_date, offImageTintList)
      }

      firstDutyOfDay.type.isAnnualLeave() ||
      firstDutyOfDay.type.isSick() ||
      firstDutyOfDay.type.isParentalLeave() -> {
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
    firstSectorOfDay: Sector?
  ) {
    val showEarlyDay = firstSectorOfDay?.departureTime?.hourOfDay ?: 100 < 10
    view_early_day.isVisible = showEarlyDay
  }

  private fun showEarlyDayIndicator(
    duty: Duty?
  ) {
    val showEarlyDay = duty?.startTime?.millis ?: 0 > 0 && duty?.startTime?.hourOfDay ?: 100 < 10
    view_early_day.isVisible = showEarlyDay
  }

  private fun List<Duty>.containsSpecialEvent(): Boolean =
    find { duty ->
      duty.type.isSpecial()
    } != null

  //TODO - merge this with DutyDisplayHelper
  private fun getDutyIcon(
    dutyType: DutyType
  ): Int =
    when {
      dutyType.isAirportStandby() -> R.drawable.icon_asby
      dutyType.isHomeStandby() -> R.drawable.icon_home
      dutyType.isOff() -> R.drawable.icon_off
      dutyType.isClear() -> R.drawable.icon_close
      dutyType.isAnnualLeave() -> R.drawable.icon_annual_leave
      dutyType.isSick() -> R.drawable.icon_sick
      dutyType.isParentalLeave() -> R.drawable.icon_parental_leave
      else -> -1
    }
}