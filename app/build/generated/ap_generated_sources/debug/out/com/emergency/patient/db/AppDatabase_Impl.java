package com.emergency.patient.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile PatientDao _patientDao;

  private volatile EmergencyContactDao _emergencyContactDao;

  private volatile HealthDocumentDao _healthDocumentDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `patients` (`uuid` TEXT NOT NULL, `fullName` TEXT, `dobMillis` INTEGER NOT NULL, `gender` TEXT, `bloodGroup` TEXT, `profilePhotoUri` TEXT, `pastMedicalDiagnosis` TEXT, `pharmacologicalStatus` TEXT, `clinicalAllergies` TEXT, `hereditaryConditions` TEXT, `lifestyleFactor` TEXT, `isOnboardingComplete` INTEGER NOT NULL, PRIMARY KEY(`uuid`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `emergency_contacts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `patientUuid` TEXT, `name` TEXT, `phoneNumber` TEXT, FOREIGN KEY(`patientUuid`) REFERENCES `patients`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emergency_contacts_patientUuid` ON `emergency_contacts` (`patientUuid`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `health_documents` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `patientUuid` TEXT, `displayName` TEXT, `internalFilePath` TEXT, `extractionStatus` TEXT, `extractedJson` TEXT, `uploadTimestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_health_documents_patientUuid` ON `health_documents` (`patientUuid`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '17e7f50c058dbe244c2402df91e9f5a7')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `patients`");
        db.execSQL("DROP TABLE IF EXISTS `emergency_contacts`");
        db.execSQL("DROP TABLE IF EXISTS `health_documents`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsPatients = new HashMap<String, TableInfo.Column>(12);
        _columnsPatients.put("uuid", new TableInfo.Column("uuid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("fullName", new TableInfo.Column("fullName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("dobMillis", new TableInfo.Column("dobMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("gender", new TableInfo.Column("gender", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("bloodGroup", new TableInfo.Column("bloodGroup", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("profilePhotoUri", new TableInfo.Column("profilePhotoUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("pastMedicalDiagnosis", new TableInfo.Column("pastMedicalDiagnosis", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("pharmacologicalStatus", new TableInfo.Column("pharmacologicalStatus", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("clinicalAllergies", new TableInfo.Column("clinicalAllergies", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("hereditaryConditions", new TableInfo.Column("hereditaryConditions", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("lifestyleFactor", new TableInfo.Column("lifestyleFactor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPatients.put("isOnboardingComplete", new TableInfo.Column("isOnboardingComplete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPatients = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPatients = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPatients = new TableInfo("patients", _columnsPatients, _foreignKeysPatients, _indicesPatients);
        final TableInfo _existingPatients = TableInfo.read(db, "patients");
        if (!_infoPatients.equals(_existingPatients)) {
          return new RoomOpenHelper.ValidationResult(false, "patients(com.emergency.patient.db.PatientEntity).\n"
                  + " Expected:\n" + _infoPatients + "\n"
                  + " Found:\n" + _existingPatients);
        }
        final HashMap<String, TableInfo.Column> _columnsEmergencyContacts = new HashMap<String, TableInfo.Column>(4);
        _columnsEmergencyContacts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("patientUuid", new TableInfo.Column("patientUuid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEmergencyContacts.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEmergencyContacts = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysEmergencyContacts.add(new TableInfo.ForeignKey("patients", "CASCADE", "NO ACTION", Arrays.asList("patientUuid"), Arrays.asList("uuid")));
        final HashSet<TableInfo.Index> _indicesEmergencyContacts = new HashSet<TableInfo.Index>(1);
        _indicesEmergencyContacts.add(new TableInfo.Index("index_emergency_contacts_patientUuid", false, Arrays.asList("patientUuid"), Arrays.asList("ASC")));
        final TableInfo _infoEmergencyContacts = new TableInfo("emergency_contacts", _columnsEmergencyContacts, _foreignKeysEmergencyContacts, _indicesEmergencyContacts);
        final TableInfo _existingEmergencyContacts = TableInfo.read(db, "emergency_contacts");
        if (!_infoEmergencyContacts.equals(_existingEmergencyContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "emergency_contacts(com.emergency.patient.db.EmergencyContactEntity).\n"
                  + " Expected:\n" + _infoEmergencyContacts + "\n"
                  + " Found:\n" + _existingEmergencyContacts);
        }
        final HashMap<String, TableInfo.Column> _columnsHealthDocuments = new HashMap<String, TableInfo.Column>(7);
        _columnsHealthDocuments.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthDocuments.put("patientUuid", new TableInfo.Column("patientUuid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthDocuments.put("displayName", new TableInfo.Column("displayName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthDocuments.put("internalFilePath", new TableInfo.Column("internalFilePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthDocuments.put("extractionStatus", new TableInfo.Column("extractionStatus", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthDocuments.put("extractedJson", new TableInfo.Column("extractedJson", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthDocuments.put("uploadTimestamp", new TableInfo.Column("uploadTimestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHealthDocuments = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHealthDocuments = new HashSet<TableInfo.Index>(1);
        _indicesHealthDocuments.add(new TableInfo.Index("index_health_documents_patientUuid", false, Arrays.asList("patientUuid"), Arrays.asList("ASC")));
        final TableInfo _infoHealthDocuments = new TableInfo("health_documents", _columnsHealthDocuments, _foreignKeysHealthDocuments, _indicesHealthDocuments);
        final TableInfo _existingHealthDocuments = TableInfo.read(db, "health_documents");
        if (!_infoHealthDocuments.equals(_existingHealthDocuments)) {
          return new RoomOpenHelper.ValidationResult(false, "health_documents(com.emergency.patient.db.HealthDocumentEntity).\n"
                  + " Expected:\n" + _infoHealthDocuments + "\n"
                  + " Found:\n" + _existingHealthDocuments);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "17e7f50c058dbe244c2402df91e9f5a7", "4e9c0aac21c54b7622e6d185e06191ae");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "patients","emergency_contacts","health_documents");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `patients`");
      _db.execSQL("DELETE FROM `emergency_contacts`");
      _db.execSQL("DELETE FROM `health_documents`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(PatientDao.class, PatientDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EmergencyContactDao.class, EmergencyContactDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HealthDocumentDao.class, HealthDocumentDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public PatientDao patientDao() {
    if (_patientDao != null) {
      return _patientDao;
    } else {
      synchronized(this) {
        if(_patientDao == null) {
          _patientDao = new PatientDao_Impl(this);
        }
        return _patientDao;
      }
    }
  }

  @Override
  public EmergencyContactDao emergencyContactDao() {
    if (_emergencyContactDao != null) {
      return _emergencyContactDao;
    } else {
      synchronized(this) {
        if(_emergencyContactDao == null) {
          _emergencyContactDao = new EmergencyContactDao_Impl(this);
        }
        return _emergencyContactDao;
      }
    }
  }

  @Override
  public HealthDocumentDao healthDocumentDao() {
    if (_healthDocumentDao != null) {
      return _healthDocumentDao;
    } else {
      synchronized(this) {
        if(_healthDocumentDao == null) {
          _healthDocumentDao = new HealthDocumentDao_Impl(this);
        }
        return _healthDocumentDao;
      }
    }
  }
}
