package de.threateningcodecomments.routinetimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.threateningcodecomments.accessibility.ResourceClass
import de.threateningcodecomments.accessibility.Routine
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.accessibility.UIContainer
import de.threateningcodecomments.adapters.TileSettingsViewpagerAdapter
import kotlinx.android.synthetic.main.fragment_tile_settings.*

class TileSettingsFragment : Fragment(), UIContainer {

    private lateinit var tileCard: MaterialCardView
    private lateinit var tileIcon: ImageView
    private lateinit var tileName: TextView

    private lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2

    private lateinit var currentRoutine: Routine
    lateinit var currentTile: Tile

    private val args: TileSettingsFragmentArgs by navArgs()

    override fun onStart() {
        super.onStart()
        MainActivity.currentFragment = this
        instance = this

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val routineMode = currentRoutine.mode
            val routineUid = currentRoutine.uid

            val directions =
                    if (routineMode == Routine.MODE_SEQUENTIAL)
                        TileSettingsFragmentDirections.actionTileSettingsFragmentToEditSequentialRoutineFragment(routineUid)
                    else
                        TileSettingsFragmentDirections.actionTileSettingsFragmentToEditContinuousRoutineFragment(routineUid)

            findNavController().navigate(directions)
        }

        currentRoutine = ResourceClass.getRoutineFromUid(args.routineUid)
        currentTile = ResourceClass.getTileFromUid(args.tileUid)
        currentRoutine.setAccessibility(MainActivity.isNightMode)

        initBuffers()

        initViewPager()

        updateUI()
    }

    override fun onStop() {
        super.onStop()
        ResourceClass.updateRoutineInDb(currentTile)
    }

    override fun updateUI() {
        tileCard.setCardBackgroundColor(currentTile.backgroundColor)

        tileIcon.setImageDrawable(ResourceClass.getIconDrawable(currentTile))
        tileIcon.setColorFilter(currentTile.contrastColor)

        tileName.text = currentTile.name
        tileName.setTextColor(currentTile.contrastColor)
    }

    override fun updateCurrentTile() {
        //this is a result of bad design
    }

    private fun initViewPager() {

        val adapter = TileSettingsViewpagerAdapter(this)
        adapter.currentTile = currentTile

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val element: TileSettingsViewpagerAdapter.Element =
                    when (position) {
                        0 -> TileSettingsViewpagerAdapter.CommonElement(currentTile)
                        1 -> TileSettingsViewpagerAdapter.AppearanceElement()
                        2 -> TileSettingsViewpagerAdapter.TimingElement()
                        else -> TileSettingsViewpagerAdapter.CommonElement(currentTile)
                    }

            tab.text = element.name
        }.attach()
    }

    private fun initBuffers() {
        tileCard = cv_TileSettings_tile_card
        tileIcon = iv_TileSettings_tile_icon
        tileName = tv_TileSettings_tile_name

        tabLayout = tl_TileSettings_settingsTabLayout
        viewPager = vp_TileSettings_viewpager
    }

    companion object {
        lateinit var instance: TileSettingsFragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tile_settings, container, false)
    }
}