package br.com.nutrisolver.tools

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.*

class TabsAdapter(fm: FragmentManager, internal var numberOfTabs: Int) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val listFragments = ArrayList<Fragment>()
    private val listFragmentsTitle = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return listFragments[position]
    }

    fun add(frag: Fragment, title: String) {
        this.listFragments.add(frag)
        this.listFragmentsTitle.add(title)
    }

    override fun getCount(): Int {
        return numberOfTabs
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listFragmentsTitle[position]
    }
}