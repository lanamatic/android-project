package com.example.myapplicationprojekat

class User {
    var username: String? = null
    var email: String? = null
    var uid: String? = null
    var goal_steps: Int = 10000
    var todays_steps: Int = 0
    var streak: Int = 0
    var week_steps: ArrayList<Int>? = ArrayList(7)

    constructor(){} //empty constructor

    constructor(name: String?, email: String?, uid: String?, goal: Int){
        this.username = name
        this.email = email
        this.uid = uid
        this.goal_steps = goal
    }

    override fun toString(): String {
        return this.username + "\n" + this.todays_steps + " / "+ this.goal_steps
    }



}