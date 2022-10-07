package com.hushbunny.app.di

import com.hushbunny.app.core.HomeActivity
import com.hushbunny.app.ui.kids.AddKidFragment
import com.hushbunny.app.ui.kids.KidsListFragment
import com.hushbunny.app.ui.moment.AddMomentFragment
import com.hushbunny.app.ui.blockeduser.BlockedUserFragment
import com.hushbunny.app.ui.bookmark.BookMarkFragment
import com.hushbunny.app.ui.comment.CommentFragment
import com.hushbunny.app.ui.filter.FilterFragment
import com.hushbunny.app.ui.home.HomeFragment
import com.hushbunny.app.ui.kids.KidsProfileFragment
import com.hushbunny.app.ui.landing.SplashActivity
import com.hushbunny.app.ui.moment.MomentDateFragment
import com.hushbunny.app.ui.moment.MomentDetailFragment
import com.hushbunny.app.ui.notifications.NotificationsFragment
import com.hushbunny.app.ui.onboarding.OnBoardingActivity
import com.hushbunny.app.ui.onboarding.chagepassword.ChangePasswordFragment
import com.hushbunny.app.ui.onboarding.createaccount.CreateAccountFragment
import com.hushbunny.app.ui.onboarding.createnewpassword.CreateNewPasswordFragment
import com.hushbunny.app.ui.onboarding.forgetpassword.ForgetPasswordFragment
import com.hushbunny.app.ui.onboarding.introscreen.IntroScreenFragment
import com.hushbunny.app.ui.onboarding.login.LoginFragment
import com.hushbunny.app.ui.onboarding.signup.SignUpFragment
import com.hushbunny.app.ui.profile.*
import com.hushbunny.app.ui.reaction.ReactionLandingFragment
import com.hushbunny.app.ui.reaction.ReactionListingFragment
import com.hushbunny.app.ui.report.ReportReasonDialog
import com.hushbunny.app.ui.setting.InAppNotificationSettingFragment
import com.hushbunny.app.ui.setting.SettingFragment
import com.hushbunny.app.uitls.dialog.UserPictureDialog
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AppComponentModule::class]
)

interface AppComponent {
    fun inject(activity: SplashActivity)
    fun inject(activity: OnBoardingActivity)
    fun inject(activity: HomeActivity)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: ForgetPasswordFragment)
    fun inject(fragment: ChangePasswordFragment)
    fun inject(fragment: SignUpFragment)
    fun inject(fragment: CreateAccountFragment)
    fun inject(fragment: CreateNewPasswordFragment)
    fun inject(fragment: IntroScreenFragment)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: SettingFragment)
    fun inject(fragment: NotificationsFragment)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: AddMomentFragment)
    fun inject(fragment: AddKidFragment)
    fun inject(fragment: KidsListFragment)
    fun inject(fragment: EditProfileFragment)
    fun inject(fragment: InAppNotificationSettingFragment)
    fun inject(fragment: BlockedUserFragment)
    fun inject(fragment: BookMarkFragment)
    fun inject(fragment: InviteSpouseFragment)
    fun inject(fragment: CommentFragment)
    fun inject(fragment: VerifyProfileOTPFragment)
    fun inject(fragment: KidsProfileFragment)
    fun inject(fragment: ReactionLandingFragment)
    fun inject(fragment: ReactionListingFragment)
    fun inject(fragment: OtherUserProfileFragment)
    fun inject(fragment: FilterFragment)
    fun inject(fragment: ReportReasonDialog)
    fun inject(fragment: MomentDetailFragment)
    fun inject(fragment: UserPictureDialog)
    fun inject(fragment: MomentDateFragment)
}