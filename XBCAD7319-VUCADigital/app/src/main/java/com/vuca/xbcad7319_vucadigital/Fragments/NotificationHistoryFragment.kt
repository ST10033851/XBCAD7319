package com.vuca.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.NotificationHistoryAdapter
import com.vuca.xbcad7319_vucadigital.databinding.FragmentNotificationHistoryBinding
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.NotificationHistoryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationHistoryFragment : Fragment() {
    private lateinit var binding: FragmentNotificationHistoryBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationHistoryAdapter: NotificationHistoryAdapter
    private var notificationList: MutableList<NotificationHistoryModel> = mutableListOf()

    private lateinit var filteredNotificationList: MutableList<NotificationHistoryModel>
    private var tempNotifications : MutableList<NotificationHistoryModel> = mutableListOf()
    private var sbHelper: SupabaseHelper = SupabaseHelper()

    private lateinit var allFilterBtn: Button
    private lateinit var visibleFilterBtn: Button
    private lateinit var hiddenFilterBtn: Button
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the ViewBinding object and return the root view
        binding = FragmentNotificationHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.notificationHistoryRecyclerView
        searchView = binding.notificationHistorySearchView
        allFilterBtn = binding.AllFilterBtn
        visibleFilterBtn = binding.visibleFilterBtn
        hiddenFilterBtn = binding.hiddenFilterBtn

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationHistoryAdapter = NotificationHistoryAdapter(notificationList, ::changeNotificationState, ::deleteNotification)
        recyclerView.adapter = notificationHistoryAdapter

        Handler(Looper.getMainLooper()).postDelayed({
            val shimmerLayout = binding.shimmerTasks
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Load notifications from Supabase
            loadNotifications()

            setUpSearchView()

            // Set up the click listeners for each filter button
            setFilterButtonClickListener(allFilterBtn, null)
            setFilterButtonClickListener(visibleFilterBtn, true)
            setFilterButtonClickListener(hiddenFilterBtn, false)
            selectButton(allFilterBtn)

        },2000)
    }

        private fun selectButton(selectedButton: Button) {
        allFilterBtn.isSelected = selectedButton == allFilterBtn
        visibleFilterBtn.isSelected = selectedButton == visibleFilterBtn
        hiddenFilterBtn.isSelected = selectedButton == hiddenFilterBtn
    }

    // StackOverflow post
    // Titled: How can I filter an ArrayList in Kotlin so I only have elements which match my condition?
    // Posted by: Nithinlal
    // Available at: https://stackoverflow.com/questions/44098709/how-can-i-filter-an-arraylist-in-kotlin-so-i-only-have-elements-which-match-my-c
    private fun setFilterButtonClickListener(button: Button, filterVisibility: Boolean?) {
        try {
            button.setOnClickListener {
                filteredNotificationList = when (filterVisibility) {
                    null -> {
                        tempNotifications
                    }
                    true -> {
                        tempNotifications.filter { it.visible == true }.toMutableList()
                    }
                    else -> {
                        tempNotifications.filter { it.visible == false }.toMutableList()
                    }
                }

                selectButton(button)
                notificationHistoryAdapter.updateNotifications(filteredNotificationList)
            }
        }
        catch (e : Exception){
            Toast.makeText(context, "Empty History! No notifications available to filter.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpSearchView() {
        try{
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { searchNotificationHistoryByCustomerName(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { searchNotificationHistoryByCustomerName(it) }
                    return true
                }
            })
        }
        catch (e : Exception){
            Toast.makeText(context, "Empty History! No notifications available to filter.", Toast.LENGTH_SHORT).show()
        }
    }

    // StackOverflow post
    // Titled: How can I filter an ArrayList in Kotlin so I only have elements which match my condition?
    // Posted by: Nithinlal
    // Available at: https://stackoverflow.com/questions/44098709/how-can-i-filter-an-arraylist-in-kotlin-so-i-only-have-elements-which-match-my-c
    private fun searchNotificationHistoryByCustomerName(query: String) {
        val queryLower = query.lowercase()

        filteredNotificationList = tempNotifications.filter { notification ->
            notification.customerName.lowercase().contains(queryLower)
        }.toMutableList()

        notificationHistoryAdapter.updateNotifications(filteredNotificationList)

        // Show a toast message if filteredTasks is empty
        if (filteredNotificationList.isEmpty()) {
            Toast.makeText(context, "Operation failure! Customer \"$query\" not found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                tempNotifications = sbHelper.getAllNotificationHistory().toMutableList()
                //notificationList = notifications.toMutableList()
                withContext(Dispatchers.Main) {
                    // Update RecyclerView on the main thread
                    notificationHistoryAdapter.updateNotifications(tempNotifications)
                }
            } catch (e: Exception) {
                Log.e("NotificationLoadError", "Error loading notifications", e)
                withContext(Dispatchers.Main) {
                    // Show an error message if loading fails
                    Toast.makeText(requireContext(), "Operation failure! Couldn't load notifications from DB.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun changeNotificationState(notification: NotificationHistoryModel, visibilityState: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Update the notification in the database
                sbHelper.updateNotificationHistory(notification.id!!, visibilityState)

                // Update the notification in memory as well (to reflect the change in the adapter)
                 notification.visible = visibilityState

                withContext(Dispatchers.Main) {
                    // Update the notification in the adapter
                    notificationHistoryAdapter.updateNotification(notification)
                    Toast.makeText(requireContext(), "Operation Success! Notification visibility has been changed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HideNotificationError", "Error hiding notification", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Operation failure! Failed to hide notification", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteNotification(notification: NotificationHistoryModel) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Delete the notification from the database
                sbHelper.deleteNotificationHistory(notification.id!!)
                withContext(Dispatchers.Main) {
                    // Delete the notification in the adapter
                    notificationHistoryAdapter.removeNotification(notification)
                    Toast.makeText(requireContext(), "Operation Success! Notification deleted.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Operation failure! Could not delete notification.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.GONE
        }
    }
}