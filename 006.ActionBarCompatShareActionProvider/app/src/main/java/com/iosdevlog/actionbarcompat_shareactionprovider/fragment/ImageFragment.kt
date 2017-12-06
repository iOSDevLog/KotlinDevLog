package com.iosdevlog.actionbarcompat_shareactionprovider.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iosdevlog.actionbarcompat_shareactionprovider.entity.ContentItem
import com.iosdevlog.actionbarcompat_shareactionprovider.R
import kotlinx.android.synthetic.main.fragment_main_image.view.*

class ImageFragment : Fragment() {
    var item: ContentItem? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_image, container, false)
        rootView.section_image.setImageURI(item?.contentUri)
        return rootView
    }

    companion object {
        fun newInstance(item: ContentItem?): ImageFragment {
            val fragment = ImageFragment()
            fragment.item = item
            return fragment
        }
    }
}