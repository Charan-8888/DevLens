package com.devlens.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devlens.data.entities.ExperienceEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ExperienceDao_Impl implements ExperienceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExperienceEntity> __insertionAdapterOfExperienceEntity;

  public ExperienceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExperienceEntity = new EntityInsertionAdapter<ExperienceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `experiences` (`id`,`incidentId`,`incidentType`,`symptomsJson`,`initialHypothesesJson`,`recommendedFix`,`developerAction`,`beforeFps`,`beforeCpu`,`beforeFrameTimeMs`,`afterFps`,`afterCpu`,`afterFrameTimeMs`,`outcome`,`lesson`,`fpsFallPercent`,`cpuSpikePresent`,`memorySpikePresent`,`jankPresent`,`durationSeconds`,`triggerScenario`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExperienceEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getIncidentId());
        statement.bindString(3, entity.getIncidentType());
        statement.bindString(4, entity.getSymptomsJson());
        statement.bindString(5, entity.getInitialHypothesesJson());
        statement.bindString(6, entity.getRecommendedFix());
        statement.bindString(7, entity.getDeveloperAction());
        statement.bindDouble(8, entity.getBeforeFps());
        statement.bindDouble(9, entity.getBeforeCpu());
        statement.bindDouble(10, entity.getBeforeFrameTimeMs());
        statement.bindDouble(11, entity.getAfterFps());
        statement.bindDouble(12, entity.getAfterCpu());
        statement.bindDouble(13, entity.getAfterFrameTimeMs());
        statement.bindString(14, entity.getOutcome());
        statement.bindString(15, entity.getLesson());
        statement.bindDouble(16, entity.getFpsFallPercent());
        final int _tmp = entity.getCpuSpikePresent() ? 1 : 0;
        statement.bindLong(17, _tmp);
        final int _tmp_1 = entity.getMemorySpikePresent() ? 1 : 0;
        statement.bindLong(18, _tmp_1);
        final int _tmp_2 = entity.getJankPresent() ? 1 : 0;
        statement.bindLong(19, _tmp_2);
        statement.bindDouble(20, entity.getDurationSeconds());
        statement.bindString(21, entity.getTriggerScenario());
        statement.bindLong(22, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insert(final ExperienceEntity experience,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfExperienceEntity.insert(experience);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ExperienceEntity>> getAllExperiences() {
    final String _sql = "SELECT * FROM experiences ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"experiences"}, new Callable<List<ExperienceEntity>>() {
      @Override
      @NonNull
      public List<ExperienceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final int _cursorIndexOfIncidentType = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentType");
          final int _cursorIndexOfSymptomsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "symptomsJson");
          final int _cursorIndexOfInitialHypothesesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "initialHypothesesJson");
          final int _cursorIndexOfRecommendedFix = CursorUtil.getColumnIndexOrThrow(_cursor, "recommendedFix");
          final int _cursorIndexOfDeveloperAction = CursorUtil.getColumnIndexOrThrow(_cursor, "developerAction");
          final int _cursorIndexOfBeforeFps = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFps");
          final int _cursorIndexOfBeforeCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeCpu");
          final int _cursorIndexOfBeforeFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFrameTimeMs");
          final int _cursorIndexOfAfterFps = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFps");
          final int _cursorIndexOfAfterCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "afterCpu");
          final int _cursorIndexOfAfterFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFrameTimeMs");
          final int _cursorIndexOfOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "outcome");
          final int _cursorIndexOfLesson = CursorUtil.getColumnIndexOrThrow(_cursor, "lesson");
          final int _cursorIndexOfFpsFallPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "fpsFallPercent");
          final int _cursorIndexOfCpuSpikePresent = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuSpikePresent");
          final int _cursorIndexOfMemorySpikePresent = CursorUtil.getColumnIndexOrThrow(_cursor, "memorySpikePresent");
          final int _cursorIndexOfJankPresent = CursorUtil.getColumnIndexOrThrow(_cursor, "jankPresent");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfTriggerScenario = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerScenario");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ExperienceEntity> _result = new ArrayList<ExperienceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExperienceEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIncidentId;
            _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            final String _tmpIncidentType;
            _tmpIncidentType = _cursor.getString(_cursorIndexOfIncidentType);
            final String _tmpSymptomsJson;
            _tmpSymptomsJson = _cursor.getString(_cursorIndexOfSymptomsJson);
            final String _tmpInitialHypothesesJson;
            _tmpInitialHypothesesJson = _cursor.getString(_cursorIndexOfInitialHypothesesJson);
            final String _tmpRecommendedFix;
            _tmpRecommendedFix = _cursor.getString(_cursorIndexOfRecommendedFix);
            final String _tmpDeveloperAction;
            _tmpDeveloperAction = _cursor.getString(_cursorIndexOfDeveloperAction);
            final float _tmpBeforeFps;
            _tmpBeforeFps = _cursor.getFloat(_cursorIndexOfBeforeFps);
            final float _tmpBeforeCpu;
            _tmpBeforeCpu = _cursor.getFloat(_cursorIndexOfBeforeCpu);
            final float _tmpBeforeFrameTimeMs;
            _tmpBeforeFrameTimeMs = _cursor.getFloat(_cursorIndexOfBeforeFrameTimeMs);
            final float _tmpAfterFps;
            _tmpAfterFps = _cursor.getFloat(_cursorIndexOfAfterFps);
            final float _tmpAfterCpu;
            _tmpAfterCpu = _cursor.getFloat(_cursorIndexOfAfterCpu);
            final float _tmpAfterFrameTimeMs;
            _tmpAfterFrameTimeMs = _cursor.getFloat(_cursorIndexOfAfterFrameTimeMs);
            final String _tmpOutcome;
            _tmpOutcome = _cursor.getString(_cursorIndexOfOutcome);
            final String _tmpLesson;
            _tmpLesson = _cursor.getString(_cursorIndexOfLesson);
            final float _tmpFpsFallPercent;
            _tmpFpsFallPercent = _cursor.getFloat(_cursorIndexOfFpsFallPercent);
            final boolean _tmpCpuSpikePresent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCpuSpikePresent);
            _tmpCpuSpikePresent = _tmp != 0;
            final boolean _tmpMemorySpikePresent;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMemorySpikePresent);
            _tmpMemorySpikePresent = _tmp_1 != 0;
            final boolean _tmpJankPresent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfJankPresent);
            _tmpJankPresent = _tmp_2 != 0;
            final float _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getFloat(_cursorIndexOfDurationSeconds);
            final String _tmpTriggerScenario;
            _tmpTriggerScenario = _cursor.getString(_cursorIndexOfTriggerScenario);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ExperienceEntity(_tmpId,_tmpIncidentId,_tmpIncidentType,_tmpSymptomsJson,_tmpInitialHypothesesJson,_tmpRecommendedFix,_tmpDeveloperAction,_tmpBeforeFps,_tmpBeforeCpu,_tmpBeforeFrameTimeMs,_tmpAfterFps,_tmpAfterCpu,_tmpAfterFrameTimeMs,_tmpOutcome,_tmpLesson,_tmpFpsFallPercent,_tmpCpuSpikePresent,_tmpMemorySpikePresent,_tmpJankPresent,_tmpDurationSeconds,_tmpTriggerScenario,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getByIncidentType(final String type,
      final Continuation<? super List<ExperienceEntity>> $completion) {
    final String _sql = "SELECT * FROM experiences WHERE incidentType = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, type);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExperienceEntity>>() {
      @Override
      @NonNull
      public List<ExperienceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final int _cursorIndexOfIncidentType = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentType");
          final int _cursorIndexOfSymptomsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "symptomsJson");
          final int _cursorIndexOfInitialHypothesesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "initialHypothesesJson");
          final int _cursorIndexOfRecommendedFix = CursorUtil.getColumnIndexOrThrow(_cursor, "recommendedFix");
          final int _cursorIndexOfDeveloperAction = CursorUtil.getColumnIndexOrThrow(_cursor, "developerAction");
          final int _cursorIndexOfBeforeFps = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFps");
          final int _cursorIndexOfBeforeCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeCpu");
          final int _cursorIndexOfBeforeFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFrameTimeMs");
          final int _cursorIndexOfAfterFps = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFps");
          final int _cursorIndexOfAfterCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "afterCpu");
          final int _cursorIndexOfAfterFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFrameTimeMs");
          final int _cursorIndexOfOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "outcome");
          final int _cursorIndexOfLesson = CursorUtil.getColumnIndexOrThrow(_cursor, "lesson");
          final int _cursorIndexOfFpsFallPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "fpsFallPercent");
          final int _cursorIndexOfCpuSpikePresent = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuSpikePresent");
          final int _cursorIndexOfMemorySpikePresent = CursorUtil.getColumnIndexOrThrow(_cursor, "memorySpikePresent");
          final int _cursorIndexOfJankPresent = CursorUtil.getColumnIndexOrThrow(_cursor, "jankPresent");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfTriggerScenario = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerScenario");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ExperienceEntity> _result = new ArrayList<ExperienceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExperienceEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIncidentId;
            _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            final String _tmpIncidentType;
            _tmpIncidentType = _cursor.getString(_cursorIndexOfIncidentType);
            final String _tmpSymptomsJson;
            _tmpSymptomsJson = _cursor.getString(_cursorIndexOfSymptomsJson);
            final String _tmpInitialHypothesesJson;
            _tmpInitialHypothesesJson = _cursor.getString(_cursorIndexOfInitialHypothesesJson);
            final String _tmpRecommendedFix;
            _tmpRecommendedFix = _cursor.getString(_cursorIndexOfRecommendedFix);
            final String _tmpDeveloperAction;
            _tmpDeveloperAction = _cursor.getString(_cursorIndexOfDeveloperAction);
            final float _tmpBeforeFps;
            _tmpBeforeFps = _cursor.getFloat(_cursorIndexOfBeforeFps);
            final float _tmpBeforeCpu;
            _tmpBeforeCpu = _cursor.getFloat(_cursorIndexOfBeforeCpu);
            final float _tmpBeforeFrameTimeMs;
            _tmpBeforeFrameTimeMs = _cursor.getFloat(_cursorIndexOfBeforeFrameTimeMs);
            final float _tmpAfterFps;
            _tmpAfterFps = _cursor.getFloat(_cursorIndexOfAfterFps);
            final float _tmpAfterCpu;
            _tmpAfterCpu = _cursor.getFloat(_cursorIndexOfAfterCpu);
            final float _tmpAfterFrameTimeMs;
            _tmpAfterFrameTimeMs = _cursor.getFloat(_cursorIndexOfAfterFrameTimeMs);
            final String _tmpOutcome;
            _tmpOutcome = _cursor.getString(_cursorIndexOfOutcome);
            final String _tmpLesson;
            _tmpLesson = _cursor.getString(_cursorIndexOfLesson);
            final float _tmpFpsFallPercent;
            _tmpFpsFallPercent = _cursor.getFloat(_cursorIndexOfFpsFallPercent);
            final boolean _tmpCpuSpikePresent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCpuSpikePresent);
            _tmpCpuSpikePresent = _tmp != 0;
            final boolean _tmpMemorySpikePresent;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMemorySpikePresent);
            _tmpMemorySpikePresent = _tmp_1 != 0;
            final boolean _tmpJankPresent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfJankPresent);
            _tmpJankPresent = _tmp_2 != 0;
            final float _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getFloat(_cursorIndexOfDurationSeconds);
            final String _tmpTriggerScenario;
            _tmpTriggerScenario = _cursor.getString(_cursorIndexOfTriggerScenario);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ExperienceEntity(_tmpId,_tmpIncidentId,_tmpIncidentType,_tmpSymptomsJson,_tmpInitialHypothesesJson,_tmpRecommendedFix,_tmpDeveloperAction,_tmpBeforeFps,_tmpBeforeCpu,_tmpBeforeFrameTimeMs,_tmpAfterFps,_tmpAfterCpu,_tmpAfterFrameTimeMs,_tmpOutcome,_tmpLesson,_tmpFpsFallPercent,_tmpCpuSpikePresent,_tmpMemorySpikePresent,_tmpJankPresent,_tmpDurationSeconds,_tmpTriggerScenario,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<ExperienceEntity>> $completion) {
    final String _sql = "SELECT * FROM experiences ORDER BY createdAt DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExperienceEntity>>() {
      @Override
      @NonNull
      public List<ExperienceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final int _cursorIndexOfIncidentType = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentType");
          final int _cursorIndexOfSymptomsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "symptomsJson");
          final int _cursorIndexOfInitialHypothesesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "initialHypothesesJson");
          final int _cursorIndexOfRecommendedFix = CursorUtil.getColumnIndexOrThrow(_cursor, "recommendedFix");
          final int _cursorIndexOfDeveloperAction = CursorUtil.getColumnIndexOrThrow(_cursor, "developerAction");
          final int _cursorIndexOfBeforeFps = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFps");
          final int _cursorIndexOfBeforeCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeCpu");
          final int _cursorIndexOfBeforeFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFrameTimeMs");
          final int _cursorIndexOfAfterFps = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFps");
          final int _cursorIndexOfAfterCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "afterCpu");
          final int _cursorIndexOfAfterFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFrameTimeMs");
          final int _cursorIndexOfOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "outcome");
          final int _cursorIndexOfLesson = CursorUtil.getColumnIndexOrThrow(_cursor, "lesson");
          final int _cursorIndexOfFpsFallPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "fpsFallPercent");
          final int _cursorIndexOfCpuSpikePresent = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuSpikePresent");
          final int _cursorIndexOfMemorySpikePresent = CursorUtil.getColumnIndexOrThrow(_cursor, "memorySpikePresent");
          final int _cursorIndexOfJankPresent = CursorUtil.getColumnIndexOrThrow(_cursor, "jankPresent");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfTriggerScenario = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerScenario");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ExperienceEntity> _result = new ArrayList<ExperienceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExperienceEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIncidentId;
            _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            final String _tmpIncidentType;
            _tmpIncidentType = _cursor.getString(_cursorIndexOfIncidentType);
            final String _tmpSymptomsJson;
            _tmpSymptomsJson = _cursor.getString(_cursorIndexOfSymptomsJson);
            final String _tmpInitialHypothesesJson;
            _tmpInitialHypothesesJson = _cursor.getString(_cursorIndexOfInitialHypothesesJson);
            final String _tmpRecommendedFix;
            _tmpRecommendedFix = _cursor.getString(_cursorIndexOfRecommendedFix);
            final String _tmpDeveloperAction;
            _tmpDeveloperAction = _cursor.getString(_cursorIndexOfDeveloperAction);
            final float _tmpBeforeFps;
            _tmpBeforeFps = _cursor.getFloat(_cursorIndexOfBeforeFps);
            final float _tmpBeforeCpu;
            _tmpBeforeCpu = _cursor.getFloat(_cursorIndexOfBeforeCpu);
            final float _tmpBeforeFrameTimeMs;
            _tmpBeforeFrameTimeMs = _cursor.getFloat(_cursorIndexOfBeforeFrameTimeMs);
            final float _tmpAfterFps;
            _tmpAfterFps = _cursor.getFloat(_cursorIndexOfAfterFps);
            final float _tmpAfterCpu;
            _tmpAfterCpu = _cursor.getFloat(_cursorIndexOfAfterCpu);
            final float _tmpAfterFrameTimeMs;
            _tmpAfterFrameTimeMs = _cursor.getFloat(_cursorIndexOfAfterFrameTimeMs);
            final String _tmpOutcome;
            _tmpOutcome = _cursor.getString(_cursorIndexOfOutcome);
            final String _tmpLesson;
            _tmpLesson = _cursor.getString(_cursorIndexOfLesson);
            final float _tmpFpsFallPercent;
            _tmpFpsFallPercent = _cursor.getFloat(_cursorIndexOfFpsFallPercent);
            final boolean _tmpCpuSpikePresent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfCpuSpikePresent);
            _tmpCpuSpikePresent = _tmp != 0;
            final boolean _tmpMemorySpikePresent;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfMemorySpikePresent);
            _tmpMemorySpikePresent = _tmp_1 != 0;
            final boolean _tmpJankPresent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfJankPresent);
            _tmpJankPresent = _tmp_2 != 0;
            final float _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getFloat(_cursorIndexOfDurationSeconds);
            final String _tmpTriggerScenario;
            _tmpTriggerScenario = _cursor.getString(_cursorIndexOfTriggerScenario);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ExperienceEntity(_tmpId,_tmpIncidentId,_tmpIncidentType,_tmpSymptomsJson,_tmpInitialHypothesesJson,_tmpRecommendedFix,_tmpDeveloperAction,_tmpBeforeFps,_tmpBeforeCpu,_tmpBeforeFrameTimeMs,_tmpAfterFps,_tmpAfterCpu,_tmpAfterFrameTimeMs,_tmpOutcome,_tmpLesson,_tmpFpsFallPercent,_tmpCpuSpikePresent,_tmpMemorySpikePresent,_tmpJankPresent,_tmpDurationSeconds,_tmpTriggerScenario,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM experiences";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
