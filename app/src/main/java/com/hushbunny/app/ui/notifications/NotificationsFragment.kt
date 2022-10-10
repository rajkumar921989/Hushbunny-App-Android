package com.hushbunny.app.ui.notifications

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hushbunny.app.R
import com.hushbunny.app.core.HomeSharedViewModel
import com.hushbunny.app.databinding.FragmentNotificationsBinding
import com.hushbunny.app.di.AppComponentProvider
import com.hushbunny.app.providers.ResourceProvider
import com.hushbunny.app.ui.BaseActivity
import com.hushbunny.app.ui.enumclass.NotificationType
import com.hushbunny.app.ui.model.NotificationModel
import com.hushbunny.app.ui.repository.HomeRepository
import com.hushbunny.app.ui.sealedclass.NotificationInfoList
import com.hushbunny.app.uitls.APIConstants
import com.hushbunny.app.uitls.dialog.DialogUtils
import com.hushbunny.app.uitls.viewModelBuilderActivityScope
import com.hushbunny.app.uitls.viewModelBuilderFragmentScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var notificationAdapter: NotificationAdapter
    var currentPage = 1
    private var notificationList = ArrayList<NotificationModel>()
    private var isLoading = true

    @Inject
    lateinit var notificationRepository: HomeRepository

    @Inject
    lateinit var resourceProvider: ResourceProvider
    private val notificationsViewModel: NotificationsViewModel by viewModelBuilderFragmentScope {
        NotificationsViewModel(
            ioDispatcher = Dispatchers.IO,
            notificationRepository = notificationRepository
        )
    }

    private val homeSharedViewModel: HomeSharedViewModel by viewModelBuilderActivityScope {
        HomeSharedViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotificationsBinding.bind(view)
        currentPage = 1
        notificationList.clear()
        setAdapter()
        getNotificationList(true)
        setObserver()
        binding.pullRefresh.setOnRefreshListener {
            onPullToRefresh()
        }
    }

    private fun onPullToRefresh() {
        currentPage = 1
        notificationList.clear()
        binding.notificationList.visibility = View.GONE
        getNotificationList(true)
        binding.pullRefresh.isRefreshing = false
        homeSharedViewModel.refreshNotificationUnReadCount()
    }

    override fun onResume() {
        super.onResume()
        (activity as? BaseActivity)?.setBottomNavigationVisibility(visibility = View.VISIBLE)
    }

    private fun setObserver() {
        notificationsViewModel.notificationObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { notification ->
                binding.shimmerContainer.visibility = View.GONE
                when (notification) {
                    is NotificationInfoList.HaveList -> {
                        isLoading = notification.notificationList.size < 30
                        notificationList.addAll(notification.notificationList)
                        loadNotification()
                    }
                    is NotificationInfoList.Error -> {
                        DialogUtils.sessionExpiredDialog(requireActivity())
                    }
                    else -> {
                        isLoading = true
                        loadNotification()
                    }
                }
            }

        }
        notificationsViewModel.acceptOrRejectNotificationObserver.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { notification ->
                binding.progressIndicator.hideProgressbar()
                when (notification.statusCode) {
                    APIConstants.API_RESPONSE_200 -> {
                        getNotificationList(true)
                    }
                    APIConstants.UNAUTHORIZED_CODE -> {
                        DialogUtils.sessionExpiredDialog(requireActivity())
                    }
                    else -> {
                        DialogUtils.showErrorDialog(
                            requireActivity(),
                            buttonText = resourceProvider.getString(R.string.ok),
                            message = notification.message,
                            title = resourceProvider.getString(R.string.app_name)
                        )
                    }
                }
            }
        }
        homeSharedViewModel.notificationTabClickedObserver.observe(viewLifecycleOwner) {
            setNotificationListScrollBehaviour()
        }
    }

    private fun setNotificationListScrollBehaviour() {
        if (binding.notificationList.isVisible && notificationList.isNotEmpty()) {
            val isFirstItem = binding.notificationList.computeVerticalScrollOffset() == 0
            if (isFirstItem) {
                binding.pullRefresh.run {
                    post {
                        this.isRefreshing = true
                        onPullToRefresh()
                    }
                }
            } else {
                binding.notificationList.smoothScrollToPosition(0)
            }
        }
    }


    private fun loadNotification() {
        if (notificationList.isNotEmpty()) {
            binding.notificationList.visibility = View.VISIBLE
            binding.noDataFoundText.visibility = View.GONE
            notificationAdapter.submitList(notificationList.toList())
        } else {
            binding.notificationList.visibility = View.GONE
            binding.noDataFoundText.visibility = View.VISIBLE
        }

    }

    private fun getNotificationList(isShimmerRequired: Boolean) {
        isLoading = true
        if (isShimmerRequired) {
            currentPage = 1
            notificationList.clear()
            binding.shimmerContainer.visibility = View.VISIBLE
        }
        notificationsViewModel.getNotificationList(currentPage)
    }

    private fun setAdapter() {
        notificationAdapter = NotificationAdapter(onAcceptClick = {
            homeSharedViewModel.refreshNotificationUnReadCount()
            binding.progressIndicator.showProgressbar()
            notificationsViewModel.acceptOrRejectNotification(type = APIConstants.ACCEPTED, shareId = it.shareId)
        }, onRejectClick = {
            homeSharedViewModel.refreshNotificationUnReadCount()
            binding.progressIndicator.showProgressbar()
            notificationsViewModel.acceptOrRejectNotification(type = APIConstants.REJECTED, shareId = it.shareId)
        }, onItemClick = {
            notificationsViewModel.unReadNotification(it.notificationId)
            homeSharedViewModel.refreshNotificationUnReadCount()
            when (it.type) {
                NotificationType.COMMENT_ON_MOMENT.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionCommentFragment(momentID = it.momentID))
                }
                NotificationType.REACTION_ON_MOMENT.name, NotificationType.LAUGH_REACTION_ON_MOMENT.name, NotificationType.LOVE_REACTION_ON_MOMENT.name, NotificationType.SAD_REACTION_ON_MOMENT.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionReactionListFragment(momentID = it.momentID))
                }
                NotificationType.REMINDER_TO_ADD_SPOUSE_AFTER_72_HOURS.name, NotificationType.REMINDER_ADD_OTHER_PARENT.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionInviteSpouseFragment(it.kidID))
                }
                NotificationType.WELCOME_NOTIFICATION.name, NotificationType.ADD_KID.name, NotificationType.REMINDER_ADD_KID.name, NotificationType.REMINDER_TO_ADD_KID_AFTER_72_HOURS.name, NotificationType.ADD_KID_WITHOUT_ADDED_SPOUSE.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionAddKidFragment())
                }
                NotificationType.ADD_OTHER_PARENT.name -> {
                    if (it.status.equals("ACCEPTED", true))
                        findNavController().navigate(NotificationsFragmentDirections.actionKidsProfileFragment(kidId = it.kidID))
                }
                NotificationType.OTHER_PARENT_INVITATION.name, NotificationType.REMINDER_OTHER_PARENT_INVITATION.name, NotificationType.FINAL_REMINDER_OTHER_PARENT_INVITATION.name, NotificationType.OTHER_PARENT_ACCEPTED_INVITATION.name, NotificationType.PARENT_DELETED_ACCOUNT.name, NotificationType.PARENT_DEACTIVATED_ACCOUNT.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionKidsProfileFragment(kidId = it.kidID))
                }
                NotificationType.PARENT_REACTIVATED_ACCOUNT.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionOtherUserProfileFragment(it.kidID))
                }
                NotificationType.ADD_FIRST_MOMENT_BY_FIRST_PARENT.name, NotificationType.ADD_FIRST_MOMENT_BY_OTHER_PARENT.name, NotificationType.REMINDER_BOTH_PARENT_ADD_FIRST_MOMENT.name, NotificationType.REMINDER_ADD_MOMENT.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionAddMomentFragment())
                }
                NotificationType.PARENT_MARKED_MOMENT_IMPORTANT.name -> {
                    findNavController().navigate(NotificationsFragmentDirections.actionMomentDetailFragment(momentID = it.momentID))
                }
            }
        })
        binding.notificationList.adapter = notificationAdapter
        binding.notificationList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = binding.notificationList.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount == lastVisibleItem) {
                    ++currentPage
                    getNotificationList(false)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}