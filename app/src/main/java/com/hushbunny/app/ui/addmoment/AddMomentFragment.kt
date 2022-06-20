package com.hushbunny.app.ui.addmoment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentAddMomentBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.ui.BaseActivity

class AddMomentFragment : Fragment(R.layout.fragment_add_moment) {

    private var _binding: FragmentAddMomentBinding? = null
    private val binding get() = _binding!!


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddMomentBinding.bind(view)

    }
    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}