package com.devlens.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devlens.data.entities.IncidentEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class IncidentDao_Impl implements IncidentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<IncidentEntity> __insertionAdapterOfIncidentEntity;

  public IncidentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfIncidentEntity = new EntityInsertionAdapter<IncidentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `incidents` (`id`,`sessionId`,`type`,`severity`,`startTime`,`endTime`,`baselineFps`,`baselineCpu`,`baselineMemoryMb`,`peakFps`,`peakCpu`,`peakMemoryMb`,`triggerScenario`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final IncidentEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindString(3, entity.getType());
        statement.bindString(4, entity.getSeverity());
        statement.bindLong(5, entity.getStartTime());
        statement.bindLong(6, entity.getEndTime());
        statement.bindDouble(7, entity.getBaselineFps());
        statement.bindDouble(8, entity.getBaselineCpu());
        statement.bindDouble(9, entity.getBaselineMemoryMb());
        statement.bindDouble(10, entity.getPeakFps());
        statement.bindDouble(11, entity.getPeakCpu());
        statement.bindDouble(12, entity.getPeakMemoryMb());
        statement.bindString(13, entity.getTriggerScenario());
      }
    };
  }

  @Override
  public Object insert(final IncidentEntity incident,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfIncidentEntity.insert(incident);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<IncidentEntity>> getAllIncidents() {
    final String _sql = "SELECT * FROM incidents ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"incidents"}, new Callable<List<IncidentEntity>>() {
      @Override
      @NonNull
      public List<IncidentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfBaselineFps = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineFps");
          final int _cursorIndexOfBaselineCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineCpu");
          final int _cursorIndexOfBaselineMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineMemoryMb");
          final int _cursorIndexOfPeakFps = CursorUtil.getColumnIndexOrThrow(_cursor, "peakFps");
          final int _cursorIndexOfPeakCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "peakCpu");
          final int _cursorIndexOfPeakMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "peakMemoryMb");
          final int _cursorIndexOfTriggerScenario = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerScenario");
          final List<IncidentEntity> _result = new ArrayList<IncidentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IncidentEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final float _tmpBaselineFps;
            _tmpBaselineFps = _cursor.getFloat(_cursorIndexOfBaselineFps);
            final float _tmpBaselineCpu;
            _tmpBaselineCpu = _cursor.getFloat(_cursorIndexOfBaselineCpu);
            final float _tmpBaselineMemoryMb;
            _tmpBaselineMemoryMb = _cursor.getFloat(_cursorIndexOfBaselineMemoryMb);
            final float _tmpPeakFps;
            _tmpPeakFps = _cursor.getFloat(_cursorIndexOfPeakFps);
            final float _tmpPeakCpu;
            _tmpPeakCpu = _cursor.getFloat(_cursorIndexOfPeakCpu);
            final float _tmpPeakMemoryMb;
            _tmpPeakMemoryMb = _cursor.getFloat(_cursorIndexOfPeakMemoryMb);
            final String _tmpTriggerScenario;
            _tmpTriggerScenario = _cursor.getString(_cursorIndexOfTriggerScenario);
            _item = new IncidentEntity(_tmpId,_tmpSessionId,_tmpType,_tmpSeverity,_tmpStartTime,_tmpEndTime,_tmpBaselineFps,_tmpBaselineCpu,_tmpBaselineMemoryMb,_tmpPeakFps,_tmpPeakCpu,_tmpPeakMemoryMb,_tmpTriggerScenario);
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
  public Object getById(final String id, final Continuation<? super IncidentEntity> $completion) {
    final String _sql = "SELECT * FROM incidents WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<IncidentEntity>() {
      @Override
      @Nullable
      public IncidentEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfBaselineFps = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineFps");
          final int _cursorIndexOfBaselineCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineCpu");
          final int _cursorIndexOfBaselineMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineMemoryMb");
          final int _cursorIndexOfPeakFps = CursorUtil.getColumnIndexOrThrow(_cursor, "peakFps");
          final int _cursorIndexOfPeakCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "peakCpu");
          final int _cursorIndexOfPeakMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "peakMemoryMb");
          final int _cursorIndexOfTriggerScenario = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerScenario");
          final IncidentEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final float _tmpBaselineFps;
            _tmpBaselineFps = _cursor.getFloat(_cursorIndexOfBaselineFps);
            final float _tmpBaselineCpu;
            _tmpBaselineCpu = _cursor.getFloat(_cursorIndexOfBaselineCpu);
            final float _tmpBaselineMemoryMb;
            _tmpBaselineMemoryMb = _cursor.getFloat(_cursorIndexOfBaselineMemoryMb);
            final float _tmpPeakFps;
            _tmpPeakFps = _cursor.getFloat(_cursorIndexOfPeakFps);
            final float _tmpPeakCpu;
            _tmpPeakCpu = _cursor.getFloat(_cursorIndexOfPeakCpu);
            final float _tmpPeakMemoryMb;
            _tmpPeakMemoryMb = _cursor.getFloat(_cursorIndexOfPeakMemoryMb);
            final String _tmpTriggerScenario;
            _tmpTriggerScenario = _cursor.getString(_cursorIndexOfTriggerScenario);
            _result = new IncidentEntity(_tmpId,_tmpSessionId,_tmpType,_tmpSeverity,_tmpStartTime,_tmpEndTime,_tmpBaselineFps,_tmpBaselineCpu,_tmpBaselineMemoryMb,_tmpPeakFps,_tmpPeakCpu,_tmpPeakMemoryMb,_tmpTriggerScenario);
          } else {
            _result = null;
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
  public Object getRecent(final Continuation<? super List<IncidentEntity>> $completion) {
    final String _sql = "SELECT * FROM incidents ORDER BY startTime DESC LIMIT 20";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<IncidentEntity>>() {
      @Override
      @NonNull
      public List<IncidentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfSeverity = CursorUtil.getColumnIndexOrThrow(_cursor, "severity");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfBaselineFps = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineFps");
          final int _cursorIndexOfBaselineCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineCpu");
          final int _cursorIndexOfBaselineMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineMemoryMb");
          final int _cursorIndexOfPeakFps = CursorUtil.getColumnIndexOrThrow(_cursor, "peakFps");
          final int _cursorIndexOfPeakCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "peakCpu");
          final int _cursorIndexOfPeakMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "peakMemoryMb");
          final int _cursorIndexOfTriggerScenario = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerScenario");
          final List<IncidentEntity> _result = new ArrayList<IncidentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final IncidentEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpSeverity;
            _tmpSeverity = _cursor.getString(_cursorIndexOfSeverity);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final long _tmpEndTime;
            _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            final float _tmpBaselineFps;
            _tmpBaselineFps = _cursor.getFloat(_cursorIndexOfBaselineFps);
            final float _tmpBaselineCpu;
            _tmpBaselineCpu = _cursor.getFloat(_cursorIndexOfBaselineCpu);
            final float _tmpBaselineMemoryMb;
            _tmpBaselineMemoryMb = _cursor.getFloat(_cursorIndexOfBaselineMemoryMb);
            final float _tmpPeakFps;
            _tmpPeakFps = _cursor.getFloat(_cursorIndexOfPeakFps);
            final float _tmpPeakCpu;
            _tmpPeakCpu = _cursor.getFloat(_cursorIndexOfPeakCpu);
            final float _tmpPeakMemoryMb;
            _tmpPeakMemoryMb = _cursor.getFloat(_cursorIndexOfPeakMemoryMb);
            final String _tmpTriggerScenario;
            _tmpTriggerScenario = _cursor.getString(_cursorIndexOfTriggerScenario);
            _item = new IncidentEntity(_tmpId,_tmpSessionId,_tmpType,_tmpSeverity,_tmpStartTime,_tmpEndTime,_tmpBaselineFps,_tmpBaselineCpu,_tmpBaselineMemoryMb,_tmpPeakFps,_tmpPeakCpu,_tmpPeakMemoryMb,_tmpTriggerScenario);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
