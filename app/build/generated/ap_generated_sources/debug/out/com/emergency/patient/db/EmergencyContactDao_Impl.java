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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class EmergencyContactDao_Impl implements EmergencyContactDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EmergencyContactEntity> __insertionAdapterOfEmergencyContactEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteContactsForPatient;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllContacts;

  public EmergencyContactDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEmergencyContactEntity = new EntityInsertionAdapter<EmergencyContactEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `emergency_contacts` (`id`,`patientUuid`,`name`,`phoneNumber`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final EmergencyContactEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.patientUuid == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.patientUuid);
        }
        if (entity.name == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.name);
        }
        if (entity.phoneNumber == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.phoneNumber);
        }
      }
    };
    this.__preparedStmtOfDeleteContactsForPatient = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM emergency_contacts WHERE patientUuid = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllContacts = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM emergency_contacts";
        return _query;
      }
    };
  }

  @Override
  public void insertAll(final List<EmergencyContactEntity> contacts) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfEmergencyContactEntity.insert(contacts);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteContactsForPatient(final String patientUuid) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteContactsForPatient.acquire();
    int _argIndex = 1;
    if (patientUuid == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, patientUuid);
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
      __preparedStmtOfDeleteContactsForPatient.release(_stmt);
    }
  }

  @Override
  public void deleteAllContacts() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllContacts.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAllContacts.release(_stmt);
    }
  }

  @Override
  public List<EmergencyContactEntity> getContactsForPatient(final String patientUuid) {
    final String _sql = "SELECT * FROM emergency_contacts WHERE patientUuid = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (patientUuid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, patientUuid);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfPatientUuid = CursorUtil.getColumnIndexOrThrow(_cursor, "patientUuid");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfPhoneNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "phoneNumber");
      final List<EmergencyContactEntity> _result = new ArrayList<EmergencyContactEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final EmergencyContactEntity _item;
        final String _tmpPatientUuid;
        if (_cursor.isNull(_cursorIndexOfPatientUuid)) {
          _tmpPatientUuid = null;
        } else {
          _tmpPatientUuid = _cursor.getString(_cursorIndexOfPatientUuid);
        }
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        final String _tmpPhoneNumber;
        if (_cursor.isNull(_cursorIndexOfPhoneNumber)) {
          _tmpPhoneNumber = null;
        } else {
          _tmpPhoneNumber = _cursor.getString(_cursorIndexOfPhoneNumber);
        }
        _item = new EmergencyContactEntity(_tmpPatientUuid,_tmpName,_tmpPhoneNumber);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _result.add(_item);
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
