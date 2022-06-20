package com.hushbunny.app.ui.onboarding.introscreen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hushbunny.app.HomeActivity
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentIntroScreenBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import javax.inject.Inject

class IntroScreenFragment : Fragment(R.layout.fragment_intro_screen) {
    private var _fragmentIntroScreenBinding: FragmentIntroScreenBinding? = null
    private val fragmentIntroScreenBinding: FragmentIntroScreenBinding get() = _fragmentIntroScreenBinding!!
    var currentStep = 1
    var isFromOnBoarding = true

    @Inject
    lateinit var resourceProvider: ResourceProvider
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _fragmentIntroScreenBinding = FragmentIntroScreenBinding.bind(view)
        isFromOnBoarding = arguments?.getBoolean("isFromOnBoarding", true) ?: true
        initializeClickListener()
    }

    private fun initializeClickListener() {
        fragmentIntroScreenBinding.stepByStepContainer.stepFourView.visibility = View.GONE
        fragmentIntroScreenBinding.stepByStepContainer.stepFiveImage.visibility = View.GONE
        moveToScreenOne()
        fragmentIntroScreenBinding.skipButton.setOnClickListener {
            if (isFromOnBoarding) navigateToHomePage() else findNavController().popBackStack()
        }
        fragmentIntroScreenBinding.nextImage.setOnClickListener {
            handleNextClick()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            handleBackClick()
        }

    }

    private fun handleBackClick() {
        when (currentStep) {
            4 -> moveToScreenThree()
            3 -> moveToScreenTwo()
            2 -> moveToScreenOne()
            1 -> if (isFromOnBoarding) navigateToHomePage() else findNavController().popBackStack()
        }
    }

    private fun handleNextClick() {
        when (currentStep) {
            1 -> moveToScreenTwo()
            2 -> moveToScreenThree()
            3 -> moveToScreenFour()
            4 -> if (isFromOnBoarding) navigateToHomePage() else findNavController().popBackStack()
        }
    }

    private fun moveToScreenOne() {
        currentStep = 1
        fragmentIntroScreenBinding.imageLogo.visibility = View.VISIBLE
        fragmentIntroScreenBinding.addYourKidsText.visibility = View.VISIBLE
        fragmentIntroScreenBinding.addYourSpouseText.visibility = View.VISIBLE
        fragmentIntroScreenBinding.otherText.visibility = View.GONE
        fragmentIntroScreenBinding.introImage.setImageResource(R.drawable.ic_intro_screen_one)
        fragmentIntroScreenBinding.oneImage.text = "1"
        setUpStepByStepImage()
    }

    private fun moveToScreenTwo() {
        currentStep = 2
        fragmentIntroScreenBinding.imageLogo.visibility = View.VISIBLE
        fragmentIntroScreenBinding.addYourKidsText.visibility = View.GONE
        fragmentIntroScreenBinding.addYourSpouseText.visibility = View.GONE
        fragmentIntroScreenBinding.otherText.visibility = View.VISIBLE
        fragmentIntroScreenBinding.otherText.text = resourceProvider.getString(R.string.capture_all_moments_as_your_kids_grow)
        fragmentIntroScreenBinding.introImage.setImageResource(R.drawable.ic_intro_screen_two)
        fragmentIntroScreenBinding.oneImage.text = "2"
        setUpStepByStepImage()
    }

    private fun moveToScreenThree() {
        currentStep = 3
        fragmentIntroScreenBinding.imageLogo.visibility = View.GONE
        fragmentIntroScreenBinding.addYourKidsText.visibility = View.GONE
        fragmentIntroScreenBinding.addYourSpouseText.visibility = View.GONE
        fragmentIntroScreenBinding.otherText.visibility = View.VISIBLE
        fragmentIntroScreenBinding.otherText.text = resourceProvider.getString(R.string.give_your_kids_their_best_gift)
        fragmentIntroScreenBinding.introImage.setImageResource(R.drawable.ic_intro_screen_three)
        fragmentIntroScreenBinding.oneImage.text = "3"
        setUpStepByStepImage()
    }

    private fun moveToScreenFour() {
        currentStep = 4
        fragmentIntroScreenBinding.imageLogo.visibility = View.GONE
        fragmentIntroScreenBinding.addYourKidsText.visibility = View.GONE
        fragmentIntroScreenBinding.addYourSpouseText.visibility = View.GONE
        fragmentIntroScreenBinding.otherText.visibility = View.VISIBLE
        fragmentIntroScreenBinding.otherText.text = resourceProvider.getString(R.string.share_moments_with_your_family_and_friends)
        fragmentIntroScreenBinding.introImage.setImageResource(R.drawable.ic_intro_screen_four)
        fragmentIntroScreenBinding.oneImage.text = "4"
        setUpStepByStepImage()
    }

    private fun setUpStepByStepImage() {
        fragmentIntroScreenBinding.stepByStepContainer.stepOneView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_color_gray
            )
        )
        fragmentIntroScreenBinding.stepByStepContainer.stepTwoView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_color_gray
            )
        )
        fragmentIntroScreenBinding.stepByStepContainer.stepThreeView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.text_color_gray
            )
        )
        fragmentIntroScreenBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_gray)
        fragmentIntroScreenBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_gray)
        fragmentIntroScreenBinding.stepByStepContainer.stepThreeImage.setImageResource(R.drawable.ic_round_background_gray)
        fragmentIntroScreenBinding.stepByStepContainer.stepFourImage.setImageResource(R.drawable.ic_round_background_gray)
        when (currentStep) {
            1 -> {
                fragmentIntroScreenBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
            }
            2 -> {
                fragmentIntroScreenBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepOneView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
            }
            3 -> {
                fragmentIntroScreenBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepThreeImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepOneView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentIntroScreenBinding.stepByStepContainer.stepTwoView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
            }
            4 -> {
                fragmentIntroScreenBinding.stepByStepContainer.stepOneImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepTwoImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepThreeImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepFourImage.setImageResource(R.drawable.ic_round_background_pink)
                fragmentIntroScreenBinding.stepByStepContainer.stepOneView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentIntroScreenBinding.stepByStepContainer.stepTwoView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
                fragmentIntroScreenBinding.stepByStepContainer.stepThreeView.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color_pink
                    )
                )
            }

        }
    }

    private fun navigateToHomePage() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        activity?.finish()
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    companion object {
        fun getInstance(isFromOnBoarding: Boolean): IntroScreenFragment {
            val introScreenFragment = IntroScreenFragment()
            introScreenFragment.arguments = Bundle().apply {
                putBoolean("isFromOnBoarding", isFromOnBoarding)
            }
            return introScreenFragment
        }
    }
}