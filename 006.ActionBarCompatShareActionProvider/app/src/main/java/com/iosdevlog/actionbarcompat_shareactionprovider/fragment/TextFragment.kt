package com.iosdevlog.actionbarcompat_shareactionprovider.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.iosdevlog.actionbarcompat_shareactionprovider.entity.ContentItem
import com.iosdevlog.actionbarcompat_shareactionprovider.R
import kotlinx.android.synthetic.main.fragment_main_text.view.*

class TextFragment : Fragment() {
    var item: ContentItem? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_text, container, false)
        item?.contentResourceId?.let { rootView.section_label.setText(it) }
        return rootView
    }

    companion object {
        fun newInstance(item: ContentItem?): TextFragment {
            val fragment = TextFragment()
            fragment.item = item
            return fragment
        }
    }
}