package com.example.freshrssreader.data.api.dto

data class LoginResponse(
    val sid: String?,
    val lsid: String?,
    val auth: String?
) {
    companion object {
        fun parse(body: String): LoginResponse {
            val map = body.lines()
                .filter { it.contains("=") }
                .associate {
                    val (key, value) = it.split("=", limit = 2)
                    key to value
                }
            return LoginResponse(
                sid = map["SID"],
                lsid = map["LSID"],
                auth = map["Auth"]
            )
        }
    }
}
