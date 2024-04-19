package com.example.myapplicationprojekat

class User {
    var username: String? = null
    var email: String? = null
    var uid: String? = null
    var goal_steps: Int = 0
    var todays_steps: Int = 0
    var streak: Int = 0
    var pb: Int = 0
    var week_steps: ArrayList<Int>? = ArrayList(7)

    constructor(){} //empty constructor

    constructor(name: String?, email: String?, uid: String?, goal: Int){
        this.username = name
        this.email = email
        this.uid = uid
        this.goal_steps = goal
    }

    constructor(name: String?, email: String?, uid: String?, goal: Int, today: Int, streak: Int, pb: Int){
        this.username = name
        this.email = email
        this.uid = uid
        this.goal_steps = goal
        this.todays_steps = today
        this.streak = streak
        this.pb = pb
    }

    override fun toString(): String {
        return this.username + "\t" + this.todays_steps + " / "+ this.goal_steps
    }



}