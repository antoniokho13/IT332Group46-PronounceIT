package com.example.pronounceit

import android.app.Activity
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper

object InAppPopupPoster {
    fun postPopupForAchievement(achTitle: String, achId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val activity: Activity? = ActivityTracker.getCurrentActivity()
                if (activity == null) {
                    Log.d("InAppPopupPoster", "No current activity to attach popup")
                    return@launch
                }

                val root = activity.findViewById<ViewGroup>(android.R.id.content) ?: run {
                    Log.w("InAppPopupPoster", "Activity root is null for popup")
                    return@launch
                }

                val popupView = LayoutInflater.from(activity).inflate(R.layout.popup_achievement_unlocked, root, false)
                val tag = "global_ach_popup_$achId"
                popupView.tag = tag
                // Prevent duplicates
                if (root.findViewWithTag<ViewGroup>(tag) != null) return@launch

                val popupText = popupView.findViewById<TextView>(R.id.popupText)
                popupText?.text = "You unlocked \"$achTitle\""

                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.gravity = Gravity.TOP
                params.topMargin = 24
                root.addView(popupView, params)

                // Entrance animation and auto-dismiss
                popupView.translationY = -200f
                popupView.alpha = 0f
                popupView.animate().translationY(0f).alpha(1f).setDuration(350).start()

                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        popupView.animate().translationY(-popupView.height.toFloat()).alpha(0f).setDuration(300).withEndAction {
                            try { (popupView.parent as? ViewGroup)?.removeView(popupView) } catch (_: Exception) {}
                        }.start()
                    } catch (e: Exception) { }
                }, 6000)

            } catch (e: Exception) {
                Log.e("InAppPopupPoster", "Failed posting in-app popup", e)
            }
        }
    }
}
