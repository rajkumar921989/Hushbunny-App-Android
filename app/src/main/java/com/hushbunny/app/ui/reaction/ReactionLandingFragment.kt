package com.hushbunny.app.ui.reaction

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.addCallback
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.hushbunny.app.R
import com.hushbunny.app.databinding.FragmentReactionLandingPageBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.ReactionPageName
import com.hushbunny.app.ui.model.ReactionCountModel
import com.hushbunny.app.ui.repository.MomentRepository
import com.hushbunny.app.uitls.viewModelBuilderActivityScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class ReactionLandingFragment : Fragment(R.layout.fragment_reaction_landing_page) {
    private var _binding: FragmentReactionLandingPageBinding? = null
    private val binding get() = _binding!!
    private val navigationArgs: ReactionLandingFragmentArgs by navArgs()

    @Inject
    lateinit var resourceProvider: ResourceProvider

    @Inject
    lateinit var momentRepository: MomentRepository
    private val reactionViewModel: ReactionViewModel by viewModelBuilderActivityScope {
        ReactionViewModel(
            ioDispatcher = Dispatchers.IO,
            resourceProvider = resourceProvider,
            momentRepository = momentRepository
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReactionLandingPageBinding.bind(view)
        reactionViewModel.reactionList.clear()
        setupViewPager()
        updateReactionCount()
        setObserver()
        initClickListener()
        registerViewPagerCallback()
    }

    private fun setObserver() {
        reactionViewModel.reactionCountObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { reactionCount ->
                updateReactionCount(reactionCount)
            }
        }
    }

    private fun initClickListener() {
        binding.headerContainer.backImage.setImageResource(R.drawable.ic_close)
        binding.headerContainer.pageTitle.text = resourceProvider.getString(R.string.reactions)
        binding.headerContainer.backImage.setOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
        binding.pullRefresh.setOnRefreshListener {
            binding.pullRefresh.isRefreshing = false
            reactionViewModel.currentPage = 1
            reactionViewModel.reactionList.clear()
            setupViewPager()
        }
    }

    private fun initTabView() {
        binding.tabContainer.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach {
                val button = it as? RadioButton
                button?.setTextAppearance(R.style.style_tab_item_default)
            }
            group.findViewById<RadioButton>(checkedId).setTextAppearance(R.style.style_tab_item_selected)

            when (checkedId) {
                R.id.all_tab -> {
                    binding.viewPager.currentItem = ReactionPageName.ALL.pageIndex
                }
                R.id.heart_tab -> {
                    binding.viewPager.currentItem = ReactionPageName.HEART.pageIndex
                }
                R.id.laugh_tab -> {
                    binding.viewPager.currentItem = ReactionPageName.LAUGH.pageIndex
                }
                R.id.sad_tab -> {
                    binding.viewPager.currentItem = ReactionPageName.SAD.pageIndex
                }
            }
        }
    }

    private fun registerViewPagerCallback() {
        binding.viewPager.apply {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    when (position) {
                        ReactionPageName.ALL.pageIndex -> {
                            binding.allTab.isChecked = true
                            reactionViewModel.setReactionType(ReactionPageName.ALL.name)
                        }
                        ReactionPageName.HEART.pageIndex -> {
                            binding.heartTab.isChecked = true
                            reactionViewModel.setReactionType(ReactionPageName.HEART.name)
                        }
                        ReactionPageName.LAUGH.pageIndex -> {
                            binding.laughTab.isChecked = true
                            reactionViewModel.setReactionType(ReactionPageName.LAUGH.name)
                        }
                        ReactionPageName.SAD.pageIndex -> {
                            binding.sadTab.isChecked = true
                            reactionViewModel.setReactionType(ReactionPageName.SAD.name)
                        }
                    }
                    currentItem = position
                }
            })
        }
    }

    private fun setupViewPager() {
        initTabView()
        binding.viewPager.apply {
            adapter = PagerAdapter(fragment = this@ReactionLandingFragment, momentId = navigationArgs.momentID)
        }
    }

    private fun updateReactionCount(reactionCountModel: ReactionCountModel? = null) {
        binding.allTab.text = resourceProvider.getString(R.string.tab_all, reactionCountModel?.all ?: "0")
        binding.heartTab.text = reactionCountModel?.heart ?: "0"
        binding.laughTab.text = reactionCountModel?.laugh ?: "0"
        binding.sadTab.text = reactionCountModel?.sad ?: "0"
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.GONE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}