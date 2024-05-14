package edu.put.inf151785

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.put.inf151785.databinding.ActivityAnimationBinding

// Aktywność animacji
class AnimationActivity : AppCompatActivity() {
    private val animationLength: Long = 4000 // ile ms trwać będzie animacja

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAnimationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // animacja tekstu
        val animatedText = binding.animationTitle
        animatedText.measure(0,0)
        val xDelta = ((getScreenWidth(this)/2)-(animatedText.measuredWidth/2)).toFloat()
        val textAnimator = ObjectAnimator.ofFloat(animatedText, "translationX", xDelta)
        textAnimator.setDuration(animationLength)
        val textAlphaAnimator = ObjectAnimator.ofFloat(animatedText, "alpha", 0.0f, 1.0f)
        textAlphaAnimator.setDuration(animationLength)

        // animacja logo
        val animatedLogo = binding.animationIcon
        animatedLogo.measure(0,0)
        val yDelta = (-((getScreenHeight(this))/*-(animatedLogo.drawable.intrinsicHeight)*/)).toFloat()
        val logoAnimator = ObjectAnimator.ofFloat(animatedLogo, "translationY", yDelta)
        logoAnimator.setDuration(animationLength)

        // kod do wykonania po zakończeniu animacji
        logoAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // uruchamiamy aktywność główną z flagą informującą, że nie wrócimy już do obecnej aktywności
                val intent = Intent(this@AnimationActivity, MainActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        })

        // łączymy animacje w set i uruchamiamy
        val animatorSet = AnimatorSet()
        animatorSet.play(textAnimator).with(logoAnimator).with(textAlphaAnimator)
        animatorSet.start()
    }
}