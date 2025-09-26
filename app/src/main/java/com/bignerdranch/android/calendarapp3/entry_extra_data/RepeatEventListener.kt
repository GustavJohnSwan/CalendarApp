package com.bignerdranch.android.calendarapp3.entry_extra_data

/* This should listen to both NewEntry and EditEntry and see if an event has
* repeat properties defined. If so, it will transform the RRule string into
* a list of dates that will be stored in a separate DB table along with the
* id (FK) of the original event and also exception boolean */

fun repeatEventListener(
    entryId : Int,
    repeatDetails : String
) {
    
}