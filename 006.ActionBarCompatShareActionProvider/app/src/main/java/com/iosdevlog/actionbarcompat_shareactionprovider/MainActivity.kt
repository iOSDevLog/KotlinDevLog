package com.iosdevlog.actionbarcompat_shareactionprovider

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ShareActionProvider
import android.view.*
import com.iosdevlog.actionbarcompat_shareactionprovider.entity.ContentItem
import com.iosdevlog.actionbarcompat_shareactionprovider.fragment.ImageFragment
import com.iosdevlog.actionbarcompat_shareactionprovider.fragment.TextFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    // Keep reference to the ShareActionProvider from the menu
    private var mShareActionProvider: ShareActionProvider? = null
    // The items to be displayed in the ViewPager
    private val mItems = getSampleContent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        // Set up the ViewPager with the sections adapter.
        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(mOnPageChangeListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        // Retrieve the share menu item
        val shareItem = menu.findItem(R.id.menu_share)

        // Now get the ShareActionProvider from the item
        mShareActionProvider = MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider

        // Get the ViewPager's current item position and set its ShareIntent.
        val currentViewPagerItem = container.currentItem
        setShareIntent(currentViewPagerItem)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.menu_share) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    // private
    private fun getSampleContent(): ArrayList<ContentItem> {
        val items = ArrayList<ContentItem>()

        items.add(ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_1.jpg"))
        items.add(ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_1))
        items.add(ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_2))
        items.add(ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_2.jpg"))
        items.add(ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_3))
        items.add(ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_3.jpg"))

        return items
    }

    private fun setShareIntent(currentViewPagerItem: Int) {
        if (mShareActionProvider != null) {
            // Get the currently selected item, and retrieve it's share intent
            val item = mItems[currentViewPagerItem]
            val shareIntent = item.getShareIntent(this@MainActivity)

            // Now update the ShareActionProvider with the new share intent
            mShareActionProvider!!.setShareIntent(shareIntent)
        }
    }

    private val mOnPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            // NO-OP
        }

        override fun onPageSelected(position: Int) {
            setShareIntent(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            // NO-OP
        }
    }

    // inner
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // Get the item for the requested position
            val item: ContentItem = mItems[position]
            // The view we need to inflate changes based on the type of content
            when (item.contentType) {
                ContentItem.CONTENT_TYPE_TEXT -> {
                    return TextFragment.newInstance(item)
                }
                ContentItem.CONTENT_TYPE_IMAGE -> {
                    return ImageFragment.newInstance(item)
                }
            }
            return Fragment()
        }

        override fun getCount(): Int {
            return mItems.size
        }
    }

}
