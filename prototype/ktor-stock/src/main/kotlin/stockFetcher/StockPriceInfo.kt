package com.stock.stockFetcher

data class StockPriceInfo(
    var date: String = "",
    var close: Double = 0.0,
    var open: Double = 0.0,
    var high: Double = 0.0,
    var low: Double = 0.0,
    var volume: Double = 0.0,
    var diff: Double = 0.0
)
