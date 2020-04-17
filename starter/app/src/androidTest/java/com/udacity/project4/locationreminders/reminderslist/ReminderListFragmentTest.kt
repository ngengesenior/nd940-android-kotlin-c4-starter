package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest:AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
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
        saveReminderViewModel = get()


        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Before
    fun registerIdlingResources(){
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }


    @Test
    fun testShowDataInRecyclerView(){

        runBlocking {
            val reminder = ReminderDTO(
                title = "title", description = "Description",
                location = "Location",
                latitude = 12.0, longitude = 13.0

            )
            repository.saveReminder(reminder)
            val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),
                R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)
            val navController = mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!,navController)
            }

            onView(withText("title")).check(matches(isDisplayed()))
            onView(withText("Description")).check(matches(isDisplayed()))
            onView(withText("Location")).check(matches(isDisplayed()))
        }


    }


    @Test
    fun testThatNavigationToAddReminderSucceeded(){
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun testRemindersShowNoDataDisplayed() {

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(),R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
        val error = saveReminderViewModel.showErrorMessage.value

    }

    @Test
    fun testErrorMessageNoTitle() {
        runBlocking {
            val scenario =
                launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)
            val navController = mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            onView(withId(R.id.saveReminder)).perform(click())
            onView(withText(appContext.getString(R.string.err_enter_title))).check(matches(
                isDisplayed()))


        }
    }


    @Test
    fun testErrorMessageNoLocation() {
        runBlocking {
            val scenario =
                launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
            dataBindingIdlingResource.monitorFragment(scenario)
            val navController = mock(NavController::class.java)
            scenario.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }

            onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
            closeSoftKeyboard()
            onView(withId(R.id.saveReminder)).perform(click())
            onView(withText(appContext.getString(R.string.err_select_location))).check(matches(
                isDisplayed()))


        }
    }
}