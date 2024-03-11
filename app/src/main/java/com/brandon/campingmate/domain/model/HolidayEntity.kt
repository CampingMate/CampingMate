package com.brandon.campingmate.domain.model
data class HolidayEntity(val response : HolidayResponse)

data class HolidayResponse(
    val header : HolidayHeader,
    val body : HolidayBody
)

data class HolidayHeader(
    val resultCode : String?,
    val resultMsg : String?
)

data class HolidayBody(
    val numOfRows : Int?,
    val pageNo : Int?,
    val totalCount : Int?,
    val items : HolidayItems
)

data class HolidayItems (
    val item:MutableList<HolidayItem>
)

data class HolidayItem (
    val dateKind : String?,
    val dateName : String?,
    val isHoliday : String?,
    val locdate : Int?,
    val seq : Int?,
    var dDay : Long
)
