package com.deviange.daggerhilt

interface Repository {
    var counter: Int
}

class RealRepository : Repository {
    override var counter: Int = 0
}
