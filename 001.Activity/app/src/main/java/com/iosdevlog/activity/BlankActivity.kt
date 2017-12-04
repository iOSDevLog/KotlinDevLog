package com.iosdevlog.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_blank.*
import org.jetbrains.anko.toast

class BlankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blank)
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            startActivity(Intent(BlankActivity@ this, TabbedActivity::class.java))
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toast("BlankActivity")
    }

}
