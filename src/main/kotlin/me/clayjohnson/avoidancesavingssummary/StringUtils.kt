package me.clayjohnson.avoidancesavingssummary

import java.net.URLEncoder

fun String.urlEncode(): String = URLEncoder.encode(this, "utf-8")