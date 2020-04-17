package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.Matchers.`is`

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var context: Application
    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var remindersDTO: MutableList<ReminderDTO>

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @Before
    fun init() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        remindersDTO = mutableListOf(
            ReminderDTO(
                title = "title", description = "Description",
                location = "Location",
                latitude = 12.0, longitude = 13.0

            )
        )
        fakeDataSource = FakeDataSource(remindersDTO)
        reminderListViewModel = RemindersListViewModel(context, fakeDataSource)
    }

    @Test
    fun testRemindersListNotEmpty() = runBlockingTest {
        reminderListViewModel.loadReminders()
        val remindersList = reminderListViewModel.remindersList.getOrAwaitValue()
        assertThat(remindersList.isEmpty(), `is`(false))

    }

    @Test
    fun testRemindersEmpty() = runBlockingTest {
        reminderListViewModel.loadReminders()
        val remindersList = reminderListViewModel.remindersList.getOrAwaitValue()
        assertThat(remindersList.isEmpty(), `is`(false))
        fakeDataSource.deleteAllReminders()
        reminderListViewModel.loadReminders()
        val noRemindersMessage = reminderListViewModel.showSnackBar.getOrAwaitValue()
        //assertEquals(noRemindersMessage, "No reminders found")
        assertThat(noRemindersMessage, `is`("No reminders found"))


    }


    @Test
    fun invalidateData_showNoDataReturnsFalse() = runBlockingTest {
        reminderListViewModel.loadReminders()
        val showNoData = reminderListViewModel.showNoData.getOrAwaitValue()
        assertThat(showNoData, `is`(false))

    }

    @Test
    fun testShowLoadingReturnsFalseWhenResumed() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        val showLoadingValue = reminderListViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValue, `is`(true))
        mainCoroutineRule.resumeDispatcher()
        val showLoadingValueAgain = reminderListViewModel.showLoading.getOrAwaitValue()
        assertThat(showLoadingValueAgain, `is`(false))
    }


}