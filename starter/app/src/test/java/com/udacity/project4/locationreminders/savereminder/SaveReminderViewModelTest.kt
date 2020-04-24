package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.hamcrest.Matchers.`is`
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var context:Application
    private lateinit var reminder:ReminderDataItem
    private lateinit var reminderNoTitle:ReminderDataItem
    private lateinit var reminderDataItemNoLocation:ReminderDataItem
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun init() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        fakeDataSource = FakeDataSource()
        reminder = ReminderDataItem(title = "Title",description = "Description",
            longitude = 12.0,latitude = 5.0,location = "Location")
        reminderNoTitle = reminder.copy(title = "")
        reminderDataItemNoLocation = reminder.copy(location = null)
        saveReminderViewModel = SaveReminderViewModel(context,fakeDataSource)
    }


    //TODO: provide testing to the SaveReminderView and its live data objects


    @Test
    fun saveReminder_showToastSaved(){
        saveReminderViewModel.saveReminder(reminder)
        val showToastValue = saveReminderViewModel.showToast.getOrAwaitValue()
        assertEquals(showToastValue, context.resources.getString(R.string.reminder_saved))

    }


    @Test
    fun saveReminder_returnsNavigationCommandBack() {
        saveReminderViewModel.saveReminder(reminder)
        val navigationCommand = saveReminderViewModel.navigationCommand.getOrAwaitValue()
        assertEquals(navigationCommand, NavigationCommand.Back)

    }


    @Test
    fun validateEnteredDataReturnsTrue() {
        val validatedReminder = saveReminderViewModel.validateEnteredData(reminder)
        assertThat(validatedReminder, `is`(true))

    }

    @Test
    fun validateEnteredDataReturnsFalseAndEmptyTitle() {
        val validatedResponse = saveReminderViewModel.validateEnteredData(reminderNoTitle)
        val snackBarHint = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertEquals(context.getString(snackBarHint) ,context.getString(R.string.err_enter_title))
        assertThat(validatedResponse, `is`(false))
    }


    @Test
    fun validateEnteredDataReturnsFalseAndEmptyLocation() {
        val validatedResult = saveReminderViewModel.validateEnteredData(reminderDataItemNoLocation)
        val snackBarHint = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertEquals(context.getString(snackBarHint), context.resources.getString(R.string.err_select_location))
        assertThat(validatedResult, `is`(false))
    }

    fun testShowLoadingReturnsFalseWhenResumed() {

        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)
        val showLoadingValue = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValue, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        val showLoadingValueAgain = saveReminderViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValueAgain, `is`(false))
    }


    fun testShouldReturnError() {

    }



}