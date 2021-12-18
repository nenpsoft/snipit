package com.nenpsoft.snipit.annotation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.nenpsoft.snipit.R
import timber.log.Timber

class AnnotationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_annotation)
        if (savedInstanceState == null) {
            if (intent.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                    val bundle = bundleOf(ArgumentUri to it)
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<AnnotationFragment>(
                            R.id.annotation_fragment_container_view,
                            args = bundle
                        )
                    }
                }
            } else {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add<AnnotationFragment>(R.id.annotation_fragment_container_view)
                }
            }
        }
    }

    override fun onBackPressed() {
        val splashScreen = this.findViewById<WebView>(R.id.splash_screen) ?: return super.onBackPressed()
        if (splashScreen.isVisible) {
            super.onBackPressed()
        }
    }
}