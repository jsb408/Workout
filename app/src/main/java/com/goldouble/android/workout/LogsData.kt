package com.goldouble.android.workout

import io.realm.RealmObject
import java.util.*

open class Logs(
    var date: Date = Date(),
    var time: Int = 0,
    var isWorkout: Boolean = true
) : RealmObject()