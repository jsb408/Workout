package com.goldouble.android.workout.db

import io.realm.RealmObject
import java.util.*

open class Logs(
    var set: Int = 1,
    var date: Date = Date(),
    var workoutTime: Int = 0,
    var restTime: Int = 0
) : RealmObject()