package edu.put.inf151785

import kotlin.properties.Delegates

// Obiekt pomocniczy do inicjalizacji bazy danych
class Trail() {
    lateinit var name: String
    lateinit var description: String
    lateinit var stages: List<String>
    var imgResourceId by Delegates.notNull<Int>()
    var length by Delegates.notNull<Float>()
    var fav by Delegates.notNull<Int>()

    constructor(name: String, description: String, stages: List<String>, imgResourceId: Int, length: Float, fav: Int) : this() {
        this.name = name
        this.description = description
        this.stages = stages
        this.imgResourceId = imgResourceId
        this.length = length
        this.fav = fav
    }
}