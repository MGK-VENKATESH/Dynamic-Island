package com.example.myapplicationdynamic.dynamicisland



import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

object ContactHelper {

    fun getContactName(context: Context, phoneNumber: String): String {
        if (phoneNumber.isEmpty()) return "Unknown"

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        var cursor: Cursor? = null
        var contactName = phoneNumber

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (nameIndex != -1) {
                    contactName = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return contactName
    }

    fun formatPhoneNumber(phoneNumber: String): String {
        // Remove country code and format nicely
        val cleaned = phoneNumber.replace("[^0-9]".toRegex(), "")

        return when {
            cleaned.length == 10 -> {
                "${cleaned.substring(0, 3)} ${cleaned.substring(3, 6)} ${cleaned.substring(6)}"
            }
            cleaned.length > 10 -> {
                val lastTen = cleaned.takeLast(10)
                "${lastTen.substring(0, 3)} ${lastTen.substring(3, 6)} ${lastTen.substring(6)}"
            }
            else -> phoneNumber
        }
    }
}