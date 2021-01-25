package com.agustinf1233.inappreview

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task


class MainActivity : AppCompatActivity() {
    private var reviewManager: ReviewManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        reviewManager = ReviewManagerFactory.create(this)
        findViewById<View>(R.id.btn_rate_app).setOnClickListener { showRateApp() }
    }

    /**
     * Shows rate app bottom sheet using In-App review API
     * The bottom sheet might or might not shown depending on the Quotas and limitations
     * https://developer.android.com/guide/playcore/in-app-review#quotas
     * We show fallback dialog if there is any error
     */
    private fun showRateApp() {
        val request: Task<ReviewInfo> = reviewManager?.requestReviewFlow() as Task<ReviewInfo>
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We can get the ReviewInfo object
                val reviewInfo: ReviewInfo = task.result
                val flow: Task<Void> = reviewManager?.launchReviewFlow(this, reviewInfo) as Task<Void>
                flow.addOnCompleteListener { task1 -> }
            } else {
                // There was some problem, continue regardless of the result.
                // show native rate app dialog on error
                showRateAppFallbackDialog()
            }
        }
    }

    /**
     * Showing native dialog with three buttons to review the app
     * Redirect user to PlayStore to review the app
     */
    private fun showRateAppFallbackDialog() {
        MaterialAlertDialogBuilder(this)
                .setTitle(R.string.rate_app_title)
                .setMessage(R.string.rate_app_message)
                .setPositiveButton(R.string.rate_btn_pos) { dialog, which -> redirectToPlayStore() }
                .setNegativeButton(R.string.rate_btn_neg
                ) { dialog, which -> }
                .setNeutralButton(R.string.rate_btn_nut
                ) { dialog, which -> }
                .setOnDismissListener { dialog: DialogInterface? -> }
                .show()
    }

    // redirecting user to PlayStore
    private fun redirectToPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (exception: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }
}