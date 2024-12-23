package com.vuca.xbcad7319_vucadigital.Adapters

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.vuca.xbcad7319_vucadigital.R

// StackOverflow post
// Titled: Create a Custom Spinner Adapter in Android using Kotlin
// Asked by: Wael Fadl Ãllåh
// Available at: https://stackoverflow.com/questions/54539470/create-a-custom-spinner-adapter-in-android-using-kotlin
class CustomSpinnerAdapter(context: Context, private val items: List<String>) : ArrayAdapter<String>(context, R.layout.spinner_item, items) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(R.id.spinnerItem)
        if (position == 0) { // Default item
            textView.setTextColor(Color.GRAY) // Set text color to grey
        } else {
            textView.setTextColor(Color.BLACK) // Set text color to black
        }
        return view
    }

    override fun isEnabled(position: Int): Boolean {
        return position != 0 // Disable the default item
    }
}