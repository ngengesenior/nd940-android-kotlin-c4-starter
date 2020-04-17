package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    private lateinit var reminder1:ReminderDTO
    private lateinit var reminder2:ReminderDTO
    private lateinit var database: RemindersDatabase
    private lateinit var remindersRepository:RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        reminder1 = ReminderDTO(
            title = "Title",
            description = "Description",
            longitude = 12.0,
            latitude = 13.0,
            location = "Location"
        )

        reminder2 = ReminderDTO(
            title = "Title 2",
            description = "Description 2",
            longitude = 123.0,
            latitude = 132.0,
            location = "Location2"
        )

        remindersRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Unconfined
        )

    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun testInsertAndGetReminder() = runBlockingTest {
        remindersRepository.saveReminder(reminder1)
        val loadedResult = remindersRepository.getReminder(reminder1.id) as Result.Success
        val loaded = loadedResult.data
        assertThat(loaded, `is`(notNullValue()))
        assertThat(loaded.id, `is`(reminder1.id))
        assertThat(loaded.description, `is`(reminder1.description))
        assertThat(loaded.location, `is`(reminder1.location))
        assertThat(loaded.title, `is`(reminder1.title))
        assertThat(loaded.longitude, `is`(reminder1.longitude))
        assertThat(loaded.latitude, `is`(reminder1.latitude))

    }

    @Test
    fun testGetReminderReturnsError() = runBlockingTest {

        val loadedError = remindersRepository.getReminder(reminder1.id) as Result.Error
        assertThat(loadedError.message, `is`(notNullValue()))
        assertThat(loadedError.message, `is`("Reminder not found!"))
    }

    fun testDeleteRemindersReturnsEmptyReminders() = runBlockingTest {
        remindersRepository.saveReminder(reminder1)
        val reminders = remindersRepository.getReminders() as Result.Success
        assertThat(reminders.data, hasItem(reminder1))

        remindersRepository.deleteAllReminders()

        val remindersSuccess = remindersRepository.getReminders() as Result.Success
        assertThat(remindersSuccess.data.size, `is`(0))
    }



}