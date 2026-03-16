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
public final class HealthDocumentDao_Impl implements HealthDocumentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HealthDocumentEntity> __insertionAdapterOfHealthDocumentEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateExtractionResults;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDocument;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllDocuments;

  public HealthDocumentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHealthDocumentEntity = new EntityInsertionAdapter<HealthDocumentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `health_documents` (`id`,`patientUuid`,`displayName`,`internalFilePath`,`extractionStatus`,`extractedJson`,`uploadTimestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final HealthDocumentEntity entity) {
        statement.bindLong(1, entity.id);
        if (entity.patientUuid == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.patientUuid);
        }
        if (entity.displayName == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.displayName);
        }
        if (entity.internalFilePath == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.internalFilePath);
        }
        if (entity.extractionStatus == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.extractionStatus);
        }
        if (entity.extractedJson == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.extractedJson);
        }
        statement.bindLong(7, entity.uploadTimestamp);
      }
    };
    this.__preparedStmtOfUpdateExtractionResults = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE health_documents SET extractionStatus = ?, extractedJson = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDocument = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM health_documents WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllDocuments = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM health_documents";
        return _query;
      }
    };
  }

  @Override
  public long insertDocument(final HealthDocumentEntity document) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfHealthDocumentEntity.insertAndReturnId(document);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateExtractionResults(final int id, final String status, final String json) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateExtractionResults.acquire();
    int _argIndex = 1;
    if (status == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, status);
    }
    _argIndex = 2;
    if (json == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, json);
    }
    _argIndex = 3;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateExtractionResults.release(_stmt);
    }
  }

  @Override
  public void deleteDocument(final int id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDocument.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteDocument.release(_stmt);
    }
  }

  @Override
  public void deleteAllDocuments() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllDocuments.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAllDocuments.release(_stmt);
    }
  }

  @Override
  public List<HealthDocumentEntity> getDocumentsForPatient(final String patientUuid) {
    final String _sql = "SELECT * FROM health_documents WHERE patientUuid = ? ORDER BY uploadTimestamp DESC";
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
      final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
      final int _cursorIndexOfInternalFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "internalFilePath");
      final int _cursorIndexOfExtractionStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "extractionStatus");
      final int _cursorIndexOfExtractedJson = CursorUtil.getColumnIndexOrThrow(_cursor, "extractedJson");
      final int _cursorIndexOfUploadTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadTimestamp");
      final List<HealthDocumentEntity> _result = new ArrayList<HealthDocumentEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final HealthDocumentEntity _item;
        final String _tmpPatientUuid;
        if (_cursor.isNull(_cursorIndexOfPatientUuid)) {
          _tmpPatientUuid = null;
        } else {
          _tmpPatientUuid = _cursor.getString(_cursorIndexOfPatientUuid);
        }
        final String _tmpDisplayName;
        if (_cursor.isNull(_cursorIndexOfDisplayName)) {
          _tmpDisplayName = null;
        } else {
          _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
        }
        final String _tmpInternalFilePath;
        if (_cursor.isNull(_cursorIndexOfInternalFilePath)) {
          _tmpInternalFilePath = null;
        } else {
          _tmpInternalFilePath = _cursor.getString(_cursorIndexOfInternalFilePath);
        }
        _item = new HealthDocumentEntity(_tmpPatientUuid,_tmpDisplayName,_tmpInternalFilePath);
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfExtractionStatus)) {
          _item.extractionStatus = null;
        } else {
          _item.extractionStatus = _cursor.getString(_cursorIndexOfExtractionStatus);
        }
        if (_cursor.isNull(_cursorIndexOfExtractedJson)) {
          _item.extractedJson = null;
        } else {
          _item.extractedJson = _cursor.getString(_cursorIndexOfExtractedJson);
        }
        _item.uploadTimestamp = _cursor.getLong(_cursorIndexOfUploadTimestamp);
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
