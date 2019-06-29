package bask.learnbulgarian.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import bask.learnbulgarian.R
import bask.learnbulgarian.fragments.LoginFragment
import bask.learnbulgarian.fragments.RegisterFragment
import com.google.android.material.tabs.TabLayout


class AuthActivity : AppCompatActivity() {

    // ViewPager and TabLayout widgets. ViewPager takes care of swipe actions (switching between the two fragments).
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth)

        // Instantiate widgets.
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        val adapter = ScreenSlidePagerAdapter(supportFragmentManager)

        // Assign adapter and TabLayout to the ViewPager.
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    // An Adapter for use by the ViewPager.
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment =
            when (position) {
                0 -> LoginFragment.newInstance()
                1 -> RegisterFragment.newInstance()
                else -> LoginFragment.newInstance()
            }


        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return getItem(position)::class.java.simpleName.removeSuffix("Fragment")
        }
    }
}