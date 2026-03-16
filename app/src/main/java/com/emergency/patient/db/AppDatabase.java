package com.emergency.patient.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PatientEntity.class, EmergencyContactEntity.class, HealthDocumentEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PatientDao patientDao();
    public abstract EmergencyContactDao emergencyContactDao();
    public abstract HealthDocumentDao healthDocumentDao();
}
