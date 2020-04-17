package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertTrue

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var reminder1:ReminderDTO
    private lateinit var reminder2:ReminderDTO

//    TODO: Add testing implementation to the RemindersDao.kt
// Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

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
    }

    @After
    fun cleanUp() {
        database.close()
    }


    @Test
    fun insertReminderAndGetId() = runBlockingTest {
        database.reminderDao().saveReminder(reminder1)
        val loaded = database.reminderDao().getReminderById(reminder1.id)
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder1.id))
        assertThat(loaded.description, `is`(reminder1.description))
        assertThat(loaded.location, `is`(reminder1.location))
        assertThat(loaded.title, `is`(reminder1.title))
        assertThat(loaded.longitude, `is`(reminder1.longitude))
        assertThat(loaded.latitude, `is`(reminder1.latitude))
    }

    @Test
    fun testDeleteRemindersAndReturnEmptyList() = runBlockingTest {
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders, hasItem(reminder1))

        database.reminderDao().deleteAllReminders()

        val remindersEmpty = database.reminderDao().getReminders()
        assertTrue(remindersEmpty.isEmpty())
    }


}