package com.udacity.project4.locationreminders.data.local

import android.content.Context
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
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
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

    private lateinit var repository: RemindersLocalRepository
    private lateinit var db: RemindersDatabase

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDB() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(
            db.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanUp() {
        db.close()
    }

    @Test
    fun addLocationReminder() = runBlocking  {
        // GIVEN a REMINDER
        val r1 = ReminderDTO("Urucará","d3",
            "l3",
            -2.536467, -57.753788)
        // WHEN SAVE IT
        repository.saveReminder(r1)

        // THEN the ID into database must be equals to ID
        val result = repository.getReminder(r1.id)

        assertThat(result is Result.Success, `is`(true))
    }

   @Test
   fun deleteAll() = runBlocking {
       val reminder =  ReminderDTO("Urucará","d3",
       "l3",
       -2.536467, -57.753788)
       repository.saveReminder(reminder)
       repository.deleteAllReminders()

       val fetchedReminder = repository.getReminder(reminder.id)

       assertThat(fetchedReminder is Result.Error, `is`(true))
       fetchedReminder as Result.Error
       assertThat(fetchedReminder.message, `is`("Reminder not found!"))
   }

}