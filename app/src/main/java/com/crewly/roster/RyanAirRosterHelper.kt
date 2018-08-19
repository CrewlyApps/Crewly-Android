package com.crewly.roster

import android.app.Application
import com.crewly.R
import com.crewly.duty.RyanairSpecialEventType
import com.crewly.duty.SpecialEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Derek on 18/08/2018
 * Helps process data from a Ryanair roster.
 */
@Singleton
class RyanAirRosterHelper @Inject constructor(private val app: Application) {

    fun getSpecialEventType(text: String): String {
        var type = getTypeForExactNameMatch(text)
        if (type.isEmpty()) { type = getTypeForOtherConditions(text) }
        return type
    }

    /**
     * Generates a description for [specialEvent].
     */
    fun generateEventDescription(specialEvent: SpecialEvent): String {
        return when (specialEvent.type) {
            RyanairSpecialEventType.ALWAYS_GETTING_BETTER_A.eventName,
            RyanairSpecialEventType.ALWAYS_GETTING_BETTER_B.eventName -> app.getString(R.string.ryanair_description_agb)

            RyanairSpecialEventType.ASSESS.eventName -> app.getString(R.string.ryanair_description_assess)

            RyanairSpecialEventType.AWARD_DAY.eventName -> app.getString(R.string.ryanair_description_oscars)

            RyanairSpecialEventType.BUS.eventName -> app.getString(R.string.ryanair_description_bus)

            RyanairSpecialEventType.CHECK_A.eventName -> app.getString(R.string.ryanair_description_clck)
            RyanairSpecialEventType.CHECK_B.eventName -> app.getString(R.string.ryanair_description_iclck)
            RyanairSpecialEventType.CHECK_C.eventName -> app.getString(R.string.ryanair_description_piclck)
            RyanairSpecialEventType.CHECK_D.eventName,
            RyanairSpecialEventType.CHECK_E.eventName -> app.getString(R.string.ryanair_description_lck)

            RyanairSpecialEventType.COMPASSIONATE_LEAVE.eventName -> app.getString(R.string.ryanair_description_cl)

            RyanairSpecialEventType.DEAD_HEADED.eventName -> app.getString(R.string.ryanair_description_dh)

            RyanairSpecialEventType.EXPIRED_CHECK.eventName -> app.getString(R.string.ryanair_description_expchk)

            RyanairSpecialEventType.GROUND_DUTY_A.eventName,
            RyanairSpecialEventType.GROUND_DUTY_B.eventName,
            RyanairSpecialEventType.GROUND_DUTY_C.eventName,
            RyanairSpecialEventType.GROUND_DUTY_D.eventName,
            RyanairSpecialEventType.GROUND_DUTY_E.eventName -> app.getString(R.string.ryanair_description_gd)
            RyanairSpecialEventType.GROUND_DUTY_F.eventName -> app.getString(R.string.ryanair_description_dsgd)
            RyanairSpecialEventType.GROUND_DUTY_G.eventName -> app.getString(R.string.ryanair_description_bsgd)

            RyanairSpecialEventType.GROUND_TRANSPORTATION_A.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_B.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_C.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_D.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_E.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_F.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_G.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_H.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_I.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_J.eventName,
            RyanairSpecialEventType.GROUND_TRANSPORTATION_K.eventName -> app.getString(R.string.ryanair_description_gt, specialEvent.location)

            RyanairSpecialEventType.INTERVIEW.eventName -> app.getString(R.string.ryanair_description_interview, specialEvent.location)

            RyanairSpecialEventType.LINE_OPS.eventName -> app.getString(R.string.ryanair_description_lops)

            RyanairSpecialEventType.MEETING.eventName -> app.getString(R.string.ryanair_description_meeting, specialEvent.location)

            RyanairSpecialEventType.NIGHT_STOP_A.eventName,
            RyanairSpecialEventType.NIGHT_STOP_B.eventName,
            RyanairSpecialEventType.NIGHT_STOP_C.eventName -> app.getString(R.string.ryanair_description_ntsp)
            RyanairSpecialEventType.NIGHT_STOP_D.eventName -> app.getString(R.string.ryanair_description_ovn, specialEvent.location)

            RyanairSpecialEventType.NO_ID.eventName -> app.getString(R.string.ryanair_description_no_id)
            RyanairSpecialEventType.NO_MED.eventName -> app.getString(R.string.ryanair_description_nomed)

            RyanairSpecialEventType.NUMBER_ONE.eventName -> app.getString(R.string.ryanair_description_pu)

            RyanairSpecialEventType.PERFORMANCE_CHECK_A.eventName,
            RyanairSpecialEventType.PERFORMANCE_CHECK_B.eventName -> app.getString(R.string.ryanair_description_mnps)

            RyanairSpecialEventType.PREGNANT_DUTY_A.eventName,
            RyanairSpecialEventType.PREGNANT_DUTY_B.eventName,
            RyanairSpecialEventType.PREGNANT_DUTY_C.eventName,
            RyanairSpecialEventType.PREGNANT_DUTY_D.eventName,
            RyanairSpecialEventType.PREGNANT_DUTY_E.eventName -> app.getString(R.string.ryanair_description_pd)

            RyanairSpecialEventType.RECONVERSION.eventName -> app.getString(R.string.ryanair_description_recon)

            RyanairSpecialEventType.SAFETY_A.eventName -> app.getString(R.string.ryanair_description_spf)
            RyanairSpecialEventType.SAFETY_B.eventName -> app.getString(R.string.ryanair_description_sfty, specialEvent.location)

            RyanairSpecialEventType.SPECIAL_DUTY_A.eventName,
            RyanairSpecialEventType.SPECIAL_DUTY_B.eventName,
            RyanairSpecialEventType.SPECIAL_DUTY_C.eventName,
            RyanairSpecialEventType.SPECIAL_DUTY_D.eventName -> app.getString(R.string.ryanair_description_sd)

            RyanairSpecialEventType.SPLIT.eventName -> app.getString(R.string.ryanair_description_split)

            RyanairSpecialEventType.STUDY_DAY.eventName -> app.getString(R.string.ryanair_description_study)

            RyanairSpecialEventType.SUPER_NUMEROUS_A.eventName,
            RyanairSpecialEventType.SUPER_NUMEROUS_B.eventName -> app.getString(R.string.ryanair_description_sny)
            RyanairSpecialEventType.SUPER_NUMEROUS_C.eventName -> app.getString(R.string.ryanair_description_sny_cadet)

            RyanairSpecialEventType.TO_BE_ROSTERED.eventName -> app.getString(R.string.ryanair_description_tbr)

            RyanairSpecialEventType.TRAINING_A.eventName -> app.getString(R.string.ryanair_description_icss)
            RyanairSpecialEventType.TRAINING_B.eventName -> app.getString(R.string.ryanair_description_fsf)
            RyanairSpecialEventType.TRAINING_C.eventName -> app.getString(R.string.ryanair_description_tsim)
            RyanairSpecialEventType.TRAINING_D.eventName -> app.getString(R.string.ryanair_description_linetrain)
            RyanairSpecialEventType.TRAINING_E.eventName -> app.getString(R.string.ryanair_description_trnrest)
            RyanairSpecialEventType.TRAINING_F.eventName -> app.getString(R.string.ryanair_description_btrg, specialEvent.location)
            RyanairSpecialEventType.TRAINING_G.eventName -> app.getString(R.string.ryanair_description_tg, specialEvent.location)
            RyanairSpecialEventType.TRAINING_H.eventName -> app.getString(R.string.ryanair_description_igs, specialEvent.location)
            RyanairSpecialEventType.TRAINING_I.eventName -> app.getString(R.string.ryanair_description_trg)
            RyanairSpecialEventType.TRAINING_J.eventName,
            RyanairSpecialEventType.TRAINING_K.eventName -> app.getString(R.string.ryanair_description_css)
            RyanairSpecialEventType.TRAINING_L.eventName -> app.getString(R.string.ryanair_description_cust)
            RyanairSpecialEventType.TRAINING_M.eventName,
            RyanairSpecialEventType.TRAINING_N.eventName -> app.getString(R.string.ryanair_description_crms)
            RyanairSpecialEventType.TRAINING_O.eventName -> app.getString(R.string.ryanair_description_rtenrol)
            RyanairSpecialEventType.TRAINING_P.eventName,
            RyanairSpecialEventType.TRAINING_Q.eventName -> app.getString(R.string.ryanair_description_rt)
            RyanairSpecialEventType.TRAINING_R.eventName -> app.getString(R.string.ryanair_description_rst)
            RyanairSpecialEventType.TRAINING_S.eventName -> app.getString(R.string.ryanair_description_sim)

            RyanairSpecialEventType.UNAUTHORISED_ABSENCE.eventName -> app.getString(R.string.ryanair_description_ua)

            RyanairSpecialEventType.UNKNOWN_A.eventName,
            RyanairSpecialEventType.UNKNOWN_B.eventName,
            RyanairSpecialEventType.UNKNOWN_C.eventName,
            RyanairSpecialEventType.UNKNOWN_D.eventName,
            RyanairSpecialEventType.UNKNOWN_E.eventName,
            RyanairSpecialEventType.UNKNOWN_F.eventName,
            RyanairSpecialEventType.UNKNOWN_G.eventName,
            RyanairSpecialEventType.UNKNOWN_H.eventName,
            RyanairSpecialEventType.UNKNOWN_I.eventName,
            RyanairSpecialEventType.UNKNOWN_J.eventName -> app.getString(R.string.ryanair_description_unknown)

            RyanairSpecialEventType.WINGS_CEREMONY.eventName -> app.getString(R.string.ryanair_description_wings)

            RyanairSpecialEventType.WORKING_DAY_OFF_A.eventName,
            RyanairSpecialEventType.WORKING_DAY_OFF_B.eventName -> app.getString(R.string.ryanair_description_woff)
            RyanairSpecialEventType.WORKING_DAY_OFF_C.eventName -> app.getString(R.string.ryanair_description_flexi)

            else -> ""
        }
    }

    /**
     * If [text] exactly matches a special event name, the type for that event will be returned.
     */
    private fun getTypeForExactNameMatch(text: String): String {
        run {
            RyanairSpecialEventType.values().forEach {
                if (text == it.eventName) {
                    return it.eventName
                }
            }
        }

        return ""
    }

    /**
     * If [text] matches other conditions, type for that event will be returned.
     */
    private fun getTypeForOtherConditions(text: String): String {
        return when {
            text.contains(RyanairSpecialEventType.DEAD_HEADED.eventName) -> RyanairSpecialEventType.DEAD_HEADED.eventName
            text.contains(RyanairSpecialEventType.TRAINING_R.eventName) -> RyanairSpecialEventType.TRAINING_R.eventName
            text.contains(RyanairSpecialEventType.TRAINING_S.eventName) -> RyanairSpecialEventType.TRAINING_S.eventName
            else -> ""
        }
    }
}