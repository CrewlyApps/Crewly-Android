package com.crewly.roster

import android.content.Context
import android.content.res.ColorStateList
import android.support.v4.widget.ImageViewCompat
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import com.crewly.R
import com.crewly.duty.DutyType
import com.crewly.utils.getColorCompat
import com.crewly.utils.inflate
import com.crewly.utils.smartPadding
import com.crewly.utils.visible
import kotlinx.android.synthetic.main.calendar_date_view.view.*

/**
 * Created by Derek on 07/07/2018
 */
class RosterDateView @JvmOverloads constructor(context: Context,
                                               attributes: AttributeSet? = null,
                                               defStyle: Int = 0):
        RelativeLayout(context, attributes, defStyle) {

    private val imageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.roster_image_tint))
    private val offImageTintList = ColorStateList.valueOf(context.getColorCompat(R.color.roster_off_image_tint))

    private val fullImagePadding = context.resources.getDimensionPixelOffset(R.dimen.roster_date_full_image_padding)
    private val imagePadding = context.resources.getDimensionPixelOffset(R.dimen.roster_date_image_padding)

    init {
        inflate(R.layout.calendar_date_view, attachToRoot = true)
    }

    fun bindToRosterDate(rosterDate: RosterPeriod.RosterDate,
                         isCurrentDay: Boolean) {
        text_date.text = rosterDate.date.dayOfMonth().asText
        text_date.isSelected = isCurrentDay

        when (rosterDate.dutyType.type) {
            DutyType.NONE -> {
                val sectorSize = rosterDate.sectors.size
                text_number.text = if (sectorSize > 0) sectorSize.toString() else ""
                showImage(false)
            }

            DutyType.ASBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_asby)
                image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
                image_calendar_date.smartPadding(imagePadding)
                ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
                showImage(true)
            }

            DutyType.HSBY -> {
                image_calendar_date.setImageResource(R.drawable.icon_home)
                image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
                image_calendar_date.smartPadding(imagePadding)
                ImageViewCompat.setImageTintList(image_calendar_date, imageTintList)
                showImage(true)
            }

            DutyType.OFF -> {
                image_calendar_date.setImageResource(R.drawable.icon_off)
                image_calendar_date.scaleType = ImageView.ScaleType.FIT_XY
                image_calendar_date.smartPadding(fullImagePadding)
                ImageViewCompat.setImageTintList(image_calendar_date, offImageTintList)
                showImage(true)
            }

            DutyType.ANNUAL_LEAVE -> {
                image_calendar_date.setImageResource(R.drawable.icon_annual_leave)
                image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
                image_calendar_date.smartPadding(imagePadding)
                ImageViewCompat.setImageTintList(image_calendar_date, null)
                showImage(true)
            }

            DutyType.SICK -> {
                image_calendar_date.setImageResource(R.drawable.icon_sick)
                image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
                image_calendar_date.smartPadding(imagePadding)
                ImageViewCompat.setImageTintList(image_calendar_date, null)
                showImage(true)
            }

            DutyType.PARENTAL_LEAVE -> {
                image_calendar_date.setImageResource(R.drawable.icon_parental_leave)
                image_calendar_date.scaleType = ImageView.ScaleType.FIT_CENTER
                image_calendar_date.smartPadding(imagePadding)
                ImageViewCompat.setImageTintList(image_calendar_date, null)
                showImage(true)
            }
        }
    }

    private fun showImage(show: Boolean) {
        image_calendar_date.visible(show)
        text_number.visible(!show)
    }
}