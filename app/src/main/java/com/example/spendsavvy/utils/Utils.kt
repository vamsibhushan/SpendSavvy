package com.example.spendsavvy.utils

import com.example.spendsavvy.R
import com.example.spendsavvy.models.Transaction
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Utils {


    fun formatDayMonthYear(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }


    fun formatDateToHumanReadableForm(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd/MM/YYYY", Locale.getDefault())
        return dateFormatter.format(dateInMillis)
    }


    fun formatStringDateToMonthDayYear(date: String): String {
        val millis = getMillisFromDate(date)
        return formatDayMonthYear(millis)
    }

    fun getMillisFromDate(date: String): Long {
        return getMilliFromDate(date)
    }

    fun getMilliFromDate(dateFormat: String?): Long {
        var date = Date()
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            date = formatter.parse(dateFormat)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        println("Today is $date")
        return date.time
    }

    fun getItemIcon(item: Transaction): Int {
        return if (item.title == "Paypal") {
            R.drawable.ic_paypal
        } else if (item.title == "Netflix") {
            R.drawable.ic_netflix
        } else if (item.title == "Starbucks") {
            R.drawable.ic_starbucks
        } else if (item.title == "Upwork") {
            R.drawable.ic_upwork
        } else if (item.title == "Grocery") {
            R.drawable.groceries

        } else if (item.title == "Bonus" || item.title == "Freelance" || item.title == "Investments") {
            R.drawable.doller

        } else {
            R.drawable.money
        }
    }


}