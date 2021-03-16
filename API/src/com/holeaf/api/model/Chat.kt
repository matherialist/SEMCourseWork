package com.holeaf.api.model

data class MessageData(var from: Int, var to: Int, var time: String, var content: String)

data class NewMessage(var to: Int, var content: String)

data class NewEvent(var content: String)

