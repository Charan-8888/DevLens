package com.devlens.data;

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
import com.devlens.data.dao.ExperienceDao;
import com.devlens.data.dao.ExperienceDao_Impl;
import com.devlens.data.dao.IncidentDao;
import com.devlens.data.dao.IncidentDao_Impl;
import com.devlens.data.dao.InvestigationDao;
import com.devlens.data.dao.InvestigationDao_Impl;
import com.devlens.data.dao.TelemetryDao;
import com.devlens.data.dao.TelemetryDao_Impl;
import com.devlens.data.dao.VerificationDao;
import com.devlens.data.dao.VerificationDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile TelemetryDao _telemetryDao;

  private volatile IncidentDao _incidentDao;

  private volatile InvestigationDao _investigationDao;

  private volatile VerificationDao _verificationDao;

  private volatile ExperienceDao _experienceDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `telemetry_samples` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `fps` REAL NOT NULL, `frameTimeMs` REAL NOT NULL, `cpuPercent` REAL NOT NULL, `memoryMb` REAL NOT NULL, `jankDetected` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `incidents` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `type` TEXT NOT NULL, `severity` TEXT NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `baselineFps` REAL NOT NULL, `baselineCpu` REAL NOT NULL, `baselineMemoryMb` REAL NOT NULL, `peakFps` REAL NOT NULL, `peakCpu` REAL NOT NULL, `peakMemoryMb` REAL NOT NULL, `triggerScenario` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `investigations` (`id` TEXT NOT NULL, `incidentId` TEXT NOT NULL, `summary` TEXT NOT NULL, `hypothesesJson` TEXT NOT NULL, `missingEvidenceJson` TEXT NOT NULL, `recommendedAction` TEXT NOT NULL, `verificationMetric` TEXT NOT NULL, `confidenceLabel` TEXT NOT NULL, `rawAiResponse` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `verification_runs` (`id` TEXT NOT NULL, `incidentId` TEXT NOT NULL, `investigationId` TEXT NOT NULL, `beforeFps` REAL NOT NULL, `beforeCpu` REAL NOT NULL, `beforeMemoryMb` REAL NOT NULL, `beforeFrameTimeMs` REAL NOT NULL, `afterFps` REAL NOT NULL, `afterCpu` REAL NOT NULL, `afterMemoryMb` REAL NOT NULL, `afterFrameTimeMs` REAL NOT NULL, `outcome` TEXT NOT NULL, `fpsImprovementPercent` REAL NOT NULL, `cpuReductionPercent` REAL NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `experiences` (`id` TEXT NOT NULL, `incidentId` TEXT NOT NULL, `incidentType` TEXT NOT NULL, `symptomsJson` TEXT NOT NULL, `initialHypothesesJson` TEXT NOT NULL, `recommendedFix` TEXT NOT NULL, `developerAction` TEXT NOT NULL, `beforeFps` REAL NOT NULL, `beforeCpu` REAL NOT NULL, `beforeFrameTimeMs` REAL NOT NULL, `afterFps` REAL NOT NULL, `afterCpu` REAL NOT NULL, `afterFrameTimeMs` REAL NOT NULL, `outcome` TEXT NOT NULL, `lesson` TEXT NOT NULL, `fpsFallPercent` REAL NOT NULL, `cpuSpikePresent` INTEGER NOT NULL, `memorySpikePresent` INTEGER NOT NULL, `jankPresent` INTEGER NOT NULL, `durationSeconds` REAL NOT NULL, `triggerScenario` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '238631d0190bf199b7accf9e4bf55f26')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `telemetry_samples`");
        db.execSQL("DROP TABLE IF EXISTS `incidents`");
        db.execSQL("DROP TABLE IF EXISTS `investigations`");
        db.execSQL("DROP TABLE IF EXISTS `verification_runs`");
        db.execSQL("DROP TABLE IF EXISTS `experiences`");
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
        final HashMap<String, TableInfo.Column> _columnsTelemetrySamples = new HashMap<String, TableInfo.Column>(8);
        _columnsTelemetrySamples.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetrySamples.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetrySamples.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetrySamples.put("fps", new TableInfo.Column("fps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetrySamples.put("frameTimeMs", new TableInfo.Column("frameTimeMs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetrySamples.put("cpuPercent", new TableInfo.Column("cpuPercent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetrySamples.put("memoryMb", new TableInfo.Column("memoryMb", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTelemetrySamples.put("jankDetected", new TableInfo.Column("jankDetected", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTelemetrySamples = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTelemetrySamples = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTelemetrySamples = new TableInfo("telemetry_samples", _columnsTelemetrySamples, _foreignKeysTelemetrySamples, _indicesTelemetrySamples);
        final TableInfo _existingTelemetrySamples = TableInfo.read(db, "telemetry_samples");
        if (!_infoTelemetrySamples.equals(_existingTelemetrySamples)) {
          return new RoomOpenHelper.ValidationResult(false, "telemetry_samples(com.devlens.data.entities.TelemetrySample).\n"
                  + " Expected:\n" + _infoTelemetrySamples + "\n"
                  + " Found:\n" + _existingTelemetrySamples);
        }
        final HashMap<String, TableInfo.Column> _columnsIncidents = new HashMap<String, TableInfo.Column>(13);
        _columnsIncidents.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("severity", new TableInfo.Column("severity", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("baselineFps", new TableInfo.Column("baselineFps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("baselineCpu", new TableInfo.Column("baselineCpu", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("baselineMemoryMb", new TableInfo.Column("baselineMemoryMb", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("peakFps", new TableInfo.Column("peakFps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("peakCpu", new TableInfo.Column("peakCpu", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("peakMemoryMb", new TableInfo.Column("peakMemoryMb", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsIncidents.put("triggerScenario", new TableInfo.Column("triggerScenario", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysIncidents = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesIncidents = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoIncidents = new TableInfo("incidents", _columnsIncidents, _foreignKeysIncidents, _indicesIncidents);
        final TableInfo _existingIncidents = TableInfo.read(db, "incidents");
        if (!_infoIncidents.equals(_existingIncidents)) {
          return new RoomOpenHelper.ValidationResult(false, "incidents(com.devlens.data.entities.IncidentEntity).\n"
                  + " Expected:\n" + _infoIncidents + "\n"
                  + " Found:\n" + _existingIncidents);
        }
        final HashMap<String, TableInfo.Column> _columnsInvestigations = new HashMap<String, TableInfo.Column>(10);
        _columnsInvestigations.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("incidentId", new TableInfo.Column("incidentId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("summary", new TableInfo.Column("summary", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("hypothesesJson", new TableInfo.Column("hypothesesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("missingEvidenceJson", new TableInfo.Column("missingEvidenceJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("recommendedAction", new TableInfo.Column("recommendedAction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("verificationMetric", new TableInfo.Column("verificationMetric", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("confidenceLabel", new TableInfo.Column("confidenceLabel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("rawAiResponse", new TableInfo.Column("rawAiResponse", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsInvestigations.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysInvestigations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesInvestigations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoInvestigations = new TableInfo("investigations", _columnsInvestigations, _foreignKeysInvestigations, _indicesInvestigations);
        final TableInfo _existingInvestigations = TableInfo.read(db, "investigations");
        if (!_infoInvestigations.equals(_existingInvestigations)) {
          return new RoomOpenHelper.ValidationResult(false, "investigations(com.devlens.data.entities.InvestigationEntity).\n"
                  + " Expected:\n" + _infoInvestigations + "\n"
                  + " Found:\n" + _existingInvestigations);
        }
        final HashMap<String, TableInfo.Column> _columnsVerificationRuns = new HashMap<String, TableInfo.Column>(15);
        _columnsVerificationRuns.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("incidentId", new TableInfo.Column("incidentId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("investigationId", new TableInfo.Column("investigationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("beforeFps", new TableInfo.Column("beforeFps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("beforeCpu", new TableInfo.Column("beforeCpu", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("beforeMemoryMb", new TableInfo.Column("beforeMemoryMb", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("beforeFrameTimeMs", new TableInfo.Column("beforeFrameTimeMs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("afterFps", new TableInfo.Column("afterFps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("afterCpu", new TableInfo.Column("afterCpu", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("afterMemoryMb", new TableInfo.Column("afterMemoryMb", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("afterFrameTimeMs", new TableInfo.Column("afterFrameTimeMs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("outcome", new TableInfo.Column("outcome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("fpsImprovementPercent", new TableInfo.Column("fpsImprovementPercent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("cpuReductionPercent", new TableInfo.Column("cpuReductionPercent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVerificationRuns.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVerificationRuns = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVerificationRuns = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVerificationRuns = new TableInfo("verification_runs", _columnsVerificationRuns, _foreignKeysVerificationRuns, _indicesVerificationRuns);
        final TableInfo _existingVerificationRuns = TableInfo.read(db, "verification_runs");
        if (!_infoVerificationRuns.equals(_existingVerificationRuns)) {
          return new RoomOpenHelper.ValidationResult(false, "verification_runs(com.devlens.data.entities.VerificationRunEntity).\n"
                  + " Expected:\n" + _infoVerificationRuns + "\n"
                  + " Found:\n" + _existingVerificationRuns);
        }
        final HashMap<String, TableInfo.Column> _columnsExperiences = new HashMap<String, TableInfo.Column>(22);
        _columnsExperiences.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("incidentId", new TableInfo.Column("incidentId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("incidentType", new TableInfo.Column("incidentType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("symptomsJson", new TableInfo.Column("symptomsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("initialHypothesesJson", new TableInfo.Column("initialHypothesesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("recommendedFix", new TableInfo.Column("recommendedFix", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("developerAction", new TableInfo.Column("developerAction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("beforeFps", new TableInfo.Column("beforeFps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("beforeCpu", new TableInfo.Column("beforeCpu", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("beforeFrameTimeMs", new TableInfo.Column("beforeFrameTimeMs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("afterFps", new TableInfo.Column("afterFps", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("afterCpu", new TableInfo.Column("afterCpu", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("afterFrameTimeMs", new TableInfo.Column("afterFrameTimeMs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("outcome", new TableInfo.Column("outcome", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("lesson", new TableInfo.Column("lesson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("fpsFallPercent", new TableInfo.Column("fpsFallPercent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("cpuSpikePresent", new TableInfo.Column("cpuSpikePresent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("memorySpikePresent", new TableInfo.Column("memorySpikePresent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("jankPresent", new TableInfo.Column("jankPresent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("durationSeconds", new TableInfo.Column("durationSeconds", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("triggerScenario", new TableInfo.Column("triggerScenario", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsExperiences.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysExperiences = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesExperiences = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoExperiences = new TableInfo("experiences", _columnsExperiences, _foreignKeysExperiences, _indicesExperiences);
        final TableInfo _existingExperiences = TableInfo.read(db, "experiences");
        if (!_infoExperiences.equals(_existingExperiences)) {
          return new RoomOpenHelper.ValidationResult(false, "experiences(com.devlens.data.entities.ExperienceEntity).\n"
                  + " Expected:\n" + _infoExperiences + "\n"
                  + " Found:\n" + _existingExperiences);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "238631d0190bf199b7accf9e4bf55f26", "f8bae0c437cbe7635b24a96b085d0715");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "telemetry_samples","incidents","investigations","verification_runs","experiences");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `telemetry_samples`");
      _db.execSQL("DELETE FROM `incidents`");
      _db.execSQL("DELETE FROM `investigations`");
      _db.execSQL("DELETE FROM `verification_runs`");
      _db.execSQL("DELETE FROM `experiences`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
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
    _typeConvertersMap.put(TelemetryDao.class, TelemetryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(IncidentDao.class, IncidentDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(InvestigationDao.class, InvestigationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VerificationDao.class, VerificationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ExperienceDao.class, ExperienceDao_Impl.getRequiredConverters());
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
  public TelemetryDao telemetryDao() {
    if (_telemetryDao != null) {
      return _telemetryDao;
    } else {
      synchronized(this) {
        if(_telemetryDao == null) {
          _telemetryDao = new TelemetryDao_Impl(this);
        }
        return _telemetryDao;
      }
    }
  }

  @Override
  public IncidentDao incidentDao() {
    if (_incidentDao != null) {
      return _incidentDao;
    } else {
      synchronized(this) {
        if(_incidentDao == null) {
          _incidentDao = new IncidentDao_Impl(this);
        }
        return _incidentDao;
      }
    }
  }

  @Override
  public InvestigationDao investigationDao() {
    if (_investigationDao != null) {
      return _investigationDao;
    } else {
      synchronized(this) {
        if(_investigationDao == null) {
          _investigationDao = new InvestigationDao_Impl(this);
        }
        return _investigationDao;
      }
    }
  }

  @Override
  public VerificationDao verificationDao() {
    if (_verificationDao != null) {
      return _verificationDao;
    } else {
      synchronized(this) {
        if(_verificationDao == null) {
          _verificationDao = new VerificationDao_Impl(this);
        }
        return _verificationDao;
      }
    }
  }

  @Override
  public ExperienceDao experienceDao() {
    if (_experienceDao != null) {
      return _experienceDao;
    } else {
      synchronized(this) {
        if(_experienceDao == null) {
          _experienceDao = new ExperienceDao_Impl(this);
        }
        return _experienceDao;
      }
    }
  }
}
