package com.example.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private lateinit var customerCountTextView: TextView
    private lateinit var opportunitiesCountTextView: TextView
    private lateinit var viewProductsBtn: CardView
    private lateinit var viewAnalyticsBtn: CardView
    private val supabaseHelper = SupabaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize
        customerCountTextView = view.findViewById(R.id.customerCount)
        opportunitiesCountTextView = view.findViewById(R.id.opportunityCount)
        viewProductsBtn = view.findViewById(R.id.viewProducts_btn)
        viewAnalyticsBtn = view.findViewById(R.id.viewAnalytics_btn)

        // Set onClickListeners
        viewProductsBtn.setOnClickListener {
            // Handle the click event for viewing products
            openProductsFragment()
        }

        viewAnalyticsBtn.setOnClickListener {
            // Handle the click event for viewing products
            openAnalyticsFragment()
        }

        // Fetch and display customer/opportunities count
        fetchAndDisplayCustomerCount()
        fetchAndDisplayOpportunitiesCount()
    }

    private fun fetchAndDisplayCustomerCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val customerCount = supabaseHelper.getCustomerCount()

                withContext(Dispatchers.Main) {
                    customerCountTextView.text = "$customerCount"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    customerCountTextView.text = "Null"
                }
            }
        }
    }

    private fun fetchAndDisplayOpportunitiesCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val opportunitiesCount = supabaseHelper.getOpportunityCount()

                withContext(Dispatchers.Main) {
                    opportunitiesCountTextView.text = "$opportunitiesCount"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    opportunitiesCountTextView.text = "Null"
                }
            }
        }
    }

    private fun openProductsFragment() {
        val fragment = ProductsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openAnalyticsFragment() {
        val fragment = AnalyticsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
