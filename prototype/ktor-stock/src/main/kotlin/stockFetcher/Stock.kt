package com.stock.stockFetcher

data class Stock(
    val code: String,
    val name: String,
    val originMinimumSellingPrice: Double,
    val originExpectedSellingPrice: Double,
    val minimumSellingPrice: Double,
    val expectedSellingPrice: Double,
    val tempPrice: Double,
    val settingPrice: Double
)
