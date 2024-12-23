package com.vuca.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.AchievementAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.AchievementModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.launch

class AchievementFragment : Fragment() {

    //Global variables for later use
    private lateinit var sbHelper: SupabaseHelper
    private lateinit var achievements: List<AchievementModel>
    private lateinit var gridView: GridView
    private lateinit var backButton: ImageView
    private lateinit var achievementCountTextView: TextView

    //Ensures that the Navbar is gone and plus btn is gone
    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.GONE
            plusBtn.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initElement(view)
        Handler(Looper.getMainLooper()).postDelayed({
            val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerAchievement)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            gridView.visibility = View.VISIBLE

            //Fetches all Achievements so be displayed to the user and updates the achievement Grid
            lifecycleScope.launch {
                achievements = sbHelper.getAllAchievements()

                displayCompletedAchievementsCount()
                updateAchievementGrid(gridView, achievements)

            }

        },2000)

        //Directs the user to the previous fragment on click
        backButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    //Displays the count of the achievements with the status "completed"
    private fun displayCompletedAchievementsCount() {
        lifecycleScope.launch {
            try {
                val achievements = sbHelper.getAllAchievements()
                val completedCount = achievements.count { it.Status == "completed" }
                "$completedCount/${achievements.count() }".also { achievementCountTextView.text = it }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    //Updates the achievement grid
    private fun updateAchievementGrid(gridView: GridView, achievements: List<AchievementModel>) {
        val adapter = AchievementAdapter(requireContext(), achievements)
        gridView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_achievement, container, false)
    }

    private fun initElement(view: View){
        gridView = view.findViewById(R.id.AchievementGridView)
        sbHelper = SupabaseHelper()
        backButton = view.findViewById(R.id.back_btn)
        achievementCountTextView = view.findViewById(R.id.achievementCount)
    }
}