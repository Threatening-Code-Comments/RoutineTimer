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
import de.threateningcodecomments.accessibility.RC
import de.threateningcodecomments.accessibility.Routine
import de.threateningcodecomments.accessibility.Tile
import de.threateningcodecomments.accessibility.UIContainer
import de.threateningcodecomments.adapters.TileSettingsViewpagerAdapter
import de.threateningcodecomments.routinetimer.databinding.FragmentTileSettingsBinding

class TileSettingsFragment : Fragment(), UIContainer {

    private var _binding: FragmentTileSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var tileCard: MaterialCardView
    private lateinit var tileIcon: ImageView
    private lateinit var tileName: TextView

    private lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2

    private lateinit var currentRoutine: Routine
    lateinit var currentTile: Tile

    private val args: TileSettingsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedElementEnterTransition = RC.Resources.sharedElementTransition

        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
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

        currentRoutine = RC.RoutinesAndTiles.getRoutineFromUid(args.routineUid)
        currentTile = RC.RoutinesAndTiles.getTileFromUid(args.tileUid)
        currentRoutine.setAccessibility(RC.isNightMode)

        initBuffers()

        initViewPager()

        updateUI()
    }

    override fun onStop() {
        super.onStop()
        RC.Db.updateRoutineInDb(currentTile)
    }

    override fun updateUI() {
        tileCard.setCardBackgroundColor(currentTile.backgroundColor)

        tileIcon.setImageDrawable(RC.getIconDrawable(currentTile))
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
        viewPager.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val element: TileSettingsViewpagerAdapter.Element =
                    when (position) {
                        0 -> TileSettingsViewpagerAdapter.CommonElement(currentTile)
                        1 -> TileSettingsViewpagerAdapter.AppearanceElement(currentTile)
                        2 -> TileSettingsViewpagerAdapter.ModeElement(currentTile)
                        else -> TileSettingsViewpagerAdapter.CommonElement(currentTile)
                    }

            tab.text = element.name
        }.attach()
    }

    private fun initBuffers() {
        tileCard = binding.cvTileSettingsTileCard
        tileIcon = binding.ivTileSettingsTileIcon
        tileName = binding.tvTileSettingsTileName

        tabLayout = binding.tlTileSettingsSettingsTabLayout
        viewPager = binding.vpTileSettingsViewpager
    }

    companion object {
        lateinit var instance: TileSettingsFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentTileSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
}