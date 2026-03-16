package com.emergency.patient.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PatientDao_Impl implements PatientDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PatientEntity> __insertionAdapterOfPatientEntity;

  private final SharedSQLiteStatement __preparedStmtOfSetOnboardingComplete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllPatients;

  public PatientDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPatientEntity = new EntityInsertionAdapter<PatientEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `patients` (`uuid`,`fullName`,`dobMillis`,`gender`,`bloodGroup`,`profilePhotoUri`,`pastMedicalDiagnosis`,`pharmacologicalStatus`,`clinicalAllergies`,`hereditaryConditions`,`lifestyleFactor`,`isOnboardingComplete`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PatientEntity entity) {
        if (entity.uuid == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.uuid);
        }
        if (entity.fullName == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.fullName);
        }
        statement.bindLong(3, entity.dobMillis);
        if (entity.gender == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.gender);
        }
        if (entity.bloodGroup == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.bloodGroup);
        }
        if (entity.profilePhotoUri == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.profilePhotoUri);
        }
        if (entity.pastMedicalDiagnosis == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.pastMedicalDiagnosis);
        }
        if (entity.pharmacologicalStatus == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.pharmacologicalStatus);
        }
        if (entity.clinicalAllergies == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.clinicalAllergies);
        }
        if (entity.hereditaryConditions == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.hereditaryConditions);
        }
        if (entity.lifestyleFactor == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.lifestyleFactor);
        }
        final int _tmp = entity.isOnboardingComplete ? 1 : 0;
        statement.bindLong(12, _tmp);
      }
    };
    this.__preparedStmtOfSetOnboardingComplete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patients SET isOnboardingComplete = ? WHERE uuid = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllPatients = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM patients";
        return _query;
      }
    };
  }

  @Override
  public void insertPatient(final PatientEntity patient) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPatientEntity.insert(patient);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void setOnboardingComplete(final String uuid, final boolean complete) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfSetOnboardingComplete.acquire();
    int _argIndex = 1;
    final int _tmp = complete ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 2;
    if (uuid == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, uuid);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfSetOnboardingComplete.release(_stmt);
    }
  }

  @Override
  public void deleteAllPatients() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllPatients.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAllPatients.release(_stmt);
    }
  }

  @Override
  public PatientEntity getPatient(final String uuid) {
    final String _sql = "SELECT * FROM patients WHERE uuid = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (uuid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, uuid);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfDobMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dobMillis");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBloodGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodGroup");
      final int _cursorIndexOfProfilePhotoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePhotoUri");
      final int _cursorIndexOfPastMedicalDiagnosis = CursorUtil.getColumnIndexOrThrow(_cursor, "pastMedicalDiagnosis");
      final int _cursorIndexOfPharmacologicalStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "pharmacologicalStatus");
      final int _cursorIndexOfClinicalAllergies = CursorUtil.getColumnIndexOrThrow(_cursor, "clinicalAllergies");
      final int _cursorIndexOfHereditaryConditions = CursorUtil.getColumnIndexOrThrow(_cursor, "hereditaryConditions");
      final int _cursorIndexOfLifestyleFactor = CursorUtil.getColumnIndexOrThrow(_cursor, "lifestyleFactor");
      final int _cursorIndexOfIsOnboardingComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isOnboardingComplete");
      final PatientEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpUuid;
        if (_cursor.isNull(_cursorIndexOfUuid)) {
          _tmpUuid = null;
        } else {
          _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
        }
        _result = new PatientEntity(_tmpUuid);
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _result.fullName = null;
        } else {
          _result.fullName = _cursor.getString(_cursorIndexOfFullName);
        }
        _result.dobMillis = _cursor.getLong(_cursorIndexOfDobMillis);
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _result.gender = null;
        } else {
          _result.gender = _cursor.getString(_cursorIndexOfGender);
        }
        if (_cursor.isNull(_cursorIndexOfBloodGroup)) {
          _result.bloodGroup = null;
        } else {
          _result.bloodGroup = _cursor.getString(_cursorIndexOfBloodGroup);
        }
        if (_cursor.isNull(_cursorIndexOfProfilePhotoUri)) {
          _result.profilePhotoUri = null;
        } else {
          _result.profilePhotoUri = _cursor.getString(_cursorIndexOfProfilePhotoUri);
        }
        if (_cursor.isNull(_cursorIndexOfPastMedicalDiagnosis)) {
          _result.pastMedicalDiagnosis = null;
        } else {
          _result.pastMedicalDiagnosis = _cursor.getString(_cursorIndexOfPastMedicalDiagnosis);
        }
        if (_cursor.isNull(_cursorIndexOfPharmacologicalStatus)) {
          _result.pharmacologicalStatus = null;
        } else {
          _result.pharmacologicalStatus = _cursor.getString(_cursorIndexOfPharmacologicalStatus);
        }
        if (_cursor.isNull(_cursorIndexOfClinicalAllergies)) {
          _result.clinicalAllergies = null;
        } else {
          _result.clinicalAllergies = _cursor.getString(_cursorIndexOfClinicalAllergies);
        }
        if (_cursor.isNull(_cursorIndexOfHereditaryConditions)) {
          _result.hereditaryConditions = null;
        } else {
          _result.hereditaryConditions = _cursor.getString(_cursorIndexOfHereditaryConditions);
        }
        if (_cursor.isNull(_cursorIndexOfLifestyleFactor)) {
          _result.lifestyleFactor = null;
        } else {
          _result.lifestyleFactor = _cursor.getString(_cursorIndexOfLifestyleFactor);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsOnboardingComplete);
        _result.isOnboardingComplete = _tmp != 0;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public PatientEntity getFirstPatient() {
    final String _sql = "SELECT * FROM patients LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "uuid");
      final int _cursorIndexOfFullName = CursorUtil.getColumnIndexOrThrow(_cursor, "fullName");
      final int _cursorIndexOfDobMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "dobMillis");
      final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
      final int _cursorIndexOfBloodGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "bloodGroup");
      final int _cursorIndexOfProfilePhotoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "profilePhotoUri");
      final int _cursorIndexOfPastMedicalDiagnosis = CursorUtil.getColumnIndexOrThrow(_cursor, "pastMedicalDiagnosis");
      final int _cursorIndexOfPharmacologicalStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "pharmacologicalStatus");
      final int _cursorIndexOfClinicalAllergies = CursorUtil.getColumnIndexOrThrow(_cursor, "clinicalAllergies");
      final int _cursorIndexOfHereditaryConditions = CursorUtil.getColumnIndexOrThrow(_cursor, "hereditaryConditions");
      final int _cursorIndexOfLifestyleFactor = CursorUtil.getColumnIndexOrThrow(_cursor, "lifestyleFactor");
      final int _cursorIndexOfIsOnboardingComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isOnboardingComplete");
      final PatientEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpUuid;
        if (_cursor.isNull(_cursorIndexOfUuid)) {
          _tmpUuid = null;
        } else {
          _tmpUuid = _cursor.getString(_cursorIndexOfUuid);
        }
        _result = new PatientEntity(_tmpUuid);
        if (_cursor.isNull(_cursorIndexOfFullName)) {
          _result.fullName = null;
        } else {
          _result.fullName = _cursor.getString(_cursorIndexOfFullName);
        }
        _result.dobMillis = _cursor.getLong(_cursorIndexOfDobMillis);
        if (_cursor.isNull(_cursorIndexOfGender)) {
          _result.gender = null;
        } else {
          _result.gender = _cursor.getString(_cursorIndexOfGender);
        }
        if (_cursor.isNull(_cursorIndexOfBloodGroup)) {
          _result.bloodGroup = null;
        } else {
          _result.bloodGroup = _cursor.getString(_cursorIndexOfBloodGroup);
        }
        if (_cursor.isNull(_cursorIndexOfProfilePhotoUri)) {
          _result.profilePhotoUri = null;
        } else {
          _result.profilePhotoUri = _cursor.getString(_cursorIndexOfProfilePhotoUri);
        }
        if (_cursor.isNull(_cursorIndexOfPastMedicalDiagnosis)) {
          _result.pastMedicalDiagnosis = null;
        } else {
          _result.pastMedicalDiagnosis = _cursor.getString(_cursorIndexOfPastMedicalDiagnosis);
        }
        if (_cursor.isNull(_cursorIndexOfPharmacologicalStatus)) {
          _result.pharmacologicalStatus = null;
        } else {
          _result.pharmacologicalStatus = _cursor.getString(_cursorIndexOfPharmacologicalStatus);
        }
        if (_cursor.isNull(_cursorIndexOfClinicalAllergies)) {
          _result.clinicalAllergies = null;
        } else {
          _result.clinicalAllergies = _cursor.getString(_cursorIndexOfClinicalAllergies);
        }
        if (_cursor.isNull(_cursorIndexOfHereditaryConditions)) {
          _result.hereditaryConditions = null;
        } else {
          _result.hereditaryConditions = _cursor.getString(_cursorIndexOfHereditaryConditions);
        }
        if (_cursor.isNull(_cursorIndexOfLifestyleFactor)) {
          _result.lifestyleFactor = null;
        } else {
          _result.lifestyleFactor = _cursor.getString(_cursorIndexOfLifestyleFactor);
        }
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsOnboardingComplete);
        _result.isOnboardingComplete = _tmp != 0;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
