package com.aisino.tool.http

class FailData(url:String,note: String) {
    var submitTime=""
    var failMsg=""
    init {
        failMsg=note
    }
}