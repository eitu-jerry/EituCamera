package com.eitu.eitucamera

enum class FlashType {

    OFF, AUTO, ON;

    operator fun inc() : FlashType {
        return when(this) {
            OFF -> AUTO
            AUTO -> ON
            ON -> OFF
        }
    }

}