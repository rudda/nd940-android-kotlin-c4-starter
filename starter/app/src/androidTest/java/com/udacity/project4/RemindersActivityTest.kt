package com.udacity.project4

import android.app.Application
import androidx.core.content.ContentProviderCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.ToastMatcher
import org.hamcrest.CoreMatchers
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var activity: RemindersActivity

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()
        viewModel = get()
        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }
     // begin test

    @Test
    fun createReminder_NoLocation() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("TITLE"))
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_select_location)))

        activityScenario.close()
    }

    @Test
    fun createReminder_noTitle() {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())
        onView(ViewMatchers.withId(com.google.android.material.R.id.snackbar_text))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun createReminder() = runBlocking {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        onView(ViewMatchers.withId(R.id.noDataTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        // more click
        onView(ViewMatchers.withId(R.id.addReminderFAB)).perform(ViewActions.click())
        // set title
        onView(ViewMatchers.withId(R.id.reminderTitle))
            .perform(ViewActions.replaceText("TITLE"))
        // set description
        onView(ViewMatchers.withId(R.id.reminderDescription))
            .perform(ViewActions.replaceText("DESCRIPTION"))
        // SET LOCATION MOCK


        viewModel.selectedPOI.postValue(PointOfInterest(LatLng(0.0, 0.0), "TestLocation", "ID"))
        viewModel.reminderSelectedLocationStr.postValue("TestLocation")
        viewModel.latitude.postValue(0.0)
        viewModel.longitude.postValue(0.0)

        //save
        onView(ViewMatchers.withId(R.id.saveReminder)).perform(ViewActions.click())


        // Then I should see the select POI message
        onView(withText(R.string.reminder_saved)).inRoot(ToastMatcher())
            .check(ViewAssertions.matches(isDisplayed()))

        activityScenario.close()
    }

}
