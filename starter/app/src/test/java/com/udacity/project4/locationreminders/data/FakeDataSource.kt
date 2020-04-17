package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders:MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        reminders?.let {
            if (it.isEmpty()){
                return Result.Error("No reminders found")
            }
            return Result.Success(ArrayList(it)) }
        return Result.Error(message = "No reminders")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        reminders?.let {
            for (reminder in it){
                if (reminder.id == id){
                    return Result.Success(reminder)
                }
            }

            return Result.Error("No such reminder")
        }
        return Result.Error("No reminders")
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}