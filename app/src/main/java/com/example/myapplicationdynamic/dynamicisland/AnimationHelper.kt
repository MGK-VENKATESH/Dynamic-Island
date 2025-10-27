package com.example.myapplicationdynamic.dynamicisland



import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.*

object AnimationHelper {

    fun pulseAnimation(view: View): ValueAnimator {
        return ValueAnimator.ofFloat(1f, 1.1f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                view.scaleX = value
                view.scaleY = value
            }
        }
    }

    fun waveformAnimation(view: View): ValueAnimator {
        return ValueAnimator.ofFloat(0.8f, 1.2f).apply {
            duration = 300
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                view.scaleY = value
            }
        }
    }

    fun glowAnimation(view: View): ValueAnimator {
        return ValueAnimator.ofFloat(0.5f, 1f).apply {
            duration = 800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                view.alpha = value
            }
        }
    }

    fun expandAnimation(view: View, fromWidth: Int, toWidth: Int, fromHeight: Int, toHeight: Int, duration: Long = 400) {
        val widthAnimator = ValueAnimator.ofInt(fromWidth, toWidth).apply {
            addUpdateListener { animation ->
                val params = view.layoutParams
                params.width = animation.animatedValue as Int
                view.layoutParams = params
            }
        }

        val heightAnimator = ValueAnimator.ofInt(fromHeight, toHeight).apply {
            addUpdateListener { animation ->
                val params = view.layoutParams
                params.height = animation.animatedValue as Int
                view.layoutParams = params
            }
        }

        AnimatorSet().apply {
            playTogether(widthAnimator, heightAnimator)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    fun bounceIn(view: View) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(BounceInterpolator())
            .start()
    }

    fun slideInFromTop(view: View) {
        view.translationY = -view.height.toFloat()
        view.alpha = 0f
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(400)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun slideOutToTop(view: View, onComplete: () -> Unit) {
        view.animate()
            .translationY(-view.height.toFloat())
            .alpha(0f)
            .setDuration(300)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction(onComplete)
            .start()
    }

    fun morphShape(view: View, fromRadius: Float, toRadius: Float, duration: Long = 300) {
        ValueAnimator.ofFloat(fromRadius, toRadius).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val radius = animation.animatedValue as Float
                // Apply to drawable if it's a GradientDrawable
                view.background?.let { drawable ->
                    if (drawable is android.graphics.drawable.GradientDrawable) {
                        drawable.cornerRadius = radius
                    }
                }
            }
            start()
        }
    }

    fun rotateAnimation(view: View, degrees: Float, duration: Long = 300) {
        view.animate()
            .rotation(degrees)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun shimmerEffect(view: View): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                view.alpha = 0.5f + (value * 0.5f)
            }
        }
    }
}