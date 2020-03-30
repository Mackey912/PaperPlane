package jp.techacademy.shohei.paperplane

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout

import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class AndroidLauncher : AndroidApplication(), ActivityRequestHandler {
    private lateinit var mAdView: AdView

    private val SHOW_ADS = 1
    private val HIDE_ADS = 0

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                SHOW_ADS -> mAdView.visibility = View.VISIBLE
                HIDE_ADS -> mAdView.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = AndroidApplicationConfiguration()
        val gameView = initializeForView(PaperPlane(this), config)

        mAdView = AdView(this)
        mAdView.adSize = AdSize.BANNER
        mAdView.adUnitId = resources.getString(R.string.banner_ad_unit_id)
        mAdView.visibility = View.INVISIBLE
        mAdView.setBackgroundColor(Color.BLACK)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //レイアウト
        val layout = RelativeLayout(this)
        layout.addView(gameView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

        val params = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layout.addView(mAdView, params)

        setContentView(layout)
        showAds(false)
    }

    override fun showAds(show: Boolean) {
        if (show) {
            mHandler.sendEmptyMessage(SHOW_ADS)
        } else {
            mHandler.sendEmptyMessage(HIDE_ADS)
        }
    }
}