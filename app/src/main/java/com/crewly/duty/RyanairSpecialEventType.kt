package com.crewly.duty

/**
 * Created by Derek on 18/08/2018
 * The different types of special events a user's roster can have.
 */
enum class RyanairSpecialEventType(val eventName: String) {

    ALWAYS_GETTING_BETTER_A("AGB"),
    ALWAYS_GETTING_BETTER_B("IAGB"),

    ASSESS("ASSESS"),

    AWARD_DAY("OSCARS"),

    BUS("BUS"),

    CHECK_A("CLCK"),
    CHECK_B("ICLCK"),
    CHECK_C("PICLCK"),
    CHECK_D("LCK"),
    CHECK_E("ILCK"),

    COMPASSIONATE_LEAVE("C/L"),

    DEAD_HEADED("DH"),

    EXPIRED_CHECK("EXPCHK"),

    GROUND_DUTY_A("G/D"),
    GROUND_DUTY_B("G/D(Z)"),
    GROUND_DUTY_C("G/D (Z)"),
    GROUND_DUTY_D("G/DT"),
    GROUND_DUTY_E("GRD SCH"),
    GROUND_DUTY_F("DSGD"),
    GROUND_DUTY_G("BSGD"),

    GROUND_TRANSPORTATION_A("DRIVE"),
    GROUND_TRANSPORTATION_B("GT/DRIVE"),
    GROUND_TRANSPORTATION_C("GT"),
    GROUND_TRANSPORTATION_D("GT G/T"),
    GROUND_TRANSPORTATION_E("G/T"),
    GROUND_TRANSPORTATION_F("CAR"),
    GROUND_TRANSPORTATION_G("SGT"),
    GROUND_TRANSPORTATION_H("SGT G/T"),
    GROUND_TRANSPORTATION_I("TAXI"),
    GROUND_TRANSPORTATION_J("TRAIN"),
    GROUND_TRANSPORTATION_K("GT SGT"),

    INTERVIEW("INTERVIEW"),

    LINE_OPS("LOPS"),

    MEETING("MEETING"),

    NIGHT_STOP_A("NISP"),
    NIGHT_STOP_B("INISP"),
    NIGHT_STOP_C("SUN-NTSP"),
    NIGHT_STOP_D("OVN"),

    NO_ID("NO ID"),
    NO_MED("NOMED"),

    NUMBER_ONE("PU"),

    PERFORMANCE_CHECK_A("+MNPS"),
    PERFORMANCE_CHECK_B("MNPS"),

    PREGNANT_DUTY_A("P/D"),
    PREGNANT_DUTY_B("P/D(Z)"),
    PREGNANT_DUTY_C("P/D (Z)"),
    PREGNANT_DUTY_D("P/DT"),
    PREGNANT_DUTY_E("P/D UK"),

    RECONVERSION("RECON"),

    SAFETY_A("SPF"),
    SAFETY_B("SFTY"),

    SPECIAL_DUTY_A("S/D"),
    SPECIAL_DUTY_B("S/D(Z)"),
    SPECIAL_DUTY_C("S/D (Z)"),
    SPECIAL_DUTY_D("S/DT"),

    SPLIT("SPLIT"),

    STUDY_DAY("STUDY DAY"),

    SUPER_NUMEROUS_A("SNYR"),
    SUPER_NUMEROUS_B("SNY"),
    SUPER_NUMEROUS_C("SNY CADET"),

    TO_BE_ROSTERED("TBR"),

    TRAINING_A("ICSS"),
    TRAINING_B("FSF"),
    TRAINING_C("TSIM"),
    TRAINING_D("LINETRAIN"),
    TRAINING_E("TRNREST"),
    TRAINING_F("BTRG"),
    TRAINING_G("45TG"),
    TRAINING_H("IGS"),
    TRAINING_I("6TRG"),
    TRAINING_J("CSS"),
    TRAINING_K("CSS PU"),
    TRAINING_L("CUST/C"),
    TRAINING_M("CRMS"),
    TRAINING_N("ICRMS"),
    TRAINING_O("3RTENROL"),
    TRAINING_P("3R/T"),
    TRAINING_Q("I3R/T"),
    TRAINING_R("RST"),
    TRAINING_S("SIM"),

    UNAUTHORISED_ABSENCE("UA/A"),

    UNKNOWN_A("COACH"),
    UNKNOWN_B("U/SELF"),
    UNKNOWN_C("CLMT"),
    UNKNOWN_D("ESCORT"),
    UNKNOWN_E("EXS"),
    UNKNOWN_F("DIFF CR"),
    UNKNOWN_G("ISD"),
    UNKNOWN_H("NOTCM"),
    UNKNOWN_I("IPOS"),
    UNKNOWN_J("FML"),

    WINGS_CEREMONY("WINGS"),

    WORKING_DAY_OFF_A("WOFF"),
    WORKING_DAY_OFF_B("IWOFF"),
    WORKING_DAY_OFF_C("FLEXI")
}