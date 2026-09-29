package com.devlens.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.devlens.data.entities.TelemetrySample;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TelemetryDao_Impl implements TelemetryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TelemetrySample> __insertionAdapterOfTelemetrySample;

  private final SharedSQLiteStatement __preparedStmtOfDeleteForSession;

  public TelemetryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTelemetrySample = new EntityInsertionAdapter<TelemetrySample>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `telemetry_samples` (`id`,`sessionId`,`timestamp`,`fps`,`frameTimeMs`,`cpuPercent`,`memoryMb`,`jankDetected`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TelemetrySample entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSessionId());
        statement.bindLong(3, entity.getTimestamp());
        statement.bindDouble(4, entity.getFps());
        statement.bindDouble(5, entity.getFrameTimeMs());
        statement.bindDouble(6, entity.getCpuPercent());
        statement.bindDouble(7, entity.getMemoryMb());
        final int _tmp = entity.getJankDetected() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__preparedStmtOfDeleteForSession = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM telemetry_samples WHERE sessionId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final TelemetrySample sample, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTelemetrySample.insert(sample);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteForSession(final String sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteForSession.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, sessionId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteForSession.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getSamplesForSession(final String sessionId,
      final Continuation<? super List<TelemetrySample>> $completion) {
    final String _sql = "SELECT * FROM telemetry_samples WHERE sessionId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TelemetrySample>>() {
      @Override
      @NonNull
      public List<TelemetrySample> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFps = CursorUtil.getColumnIndexOrThrow(_cursor, "fps");
          final int _cursorIndexOfFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "frameTimeMs");
          final int _cursorIndexOfCpuPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuPercent");
          final int _cursorIndexOfMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "memoryMb");
          final int _cursorIndexOfJankDetected = CursorUtil.getColumnIndexOrThrow(_cursor, "jankDetected");
          final List<TelemetrySample> _result = new ArrayList<TelemetrySample>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TelemetrySample _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpFps;
            _tmpFps = _cursor.getFloat(_cursorIndexOfFps);
            final float _tmpFrameTimeMs;
            _tmpFrameTimeMs = _cursor.getFloat(_cursorIndexOfFrameTimeMs);
            final float _tmpCpuPercent;
            _tmpCpuPercent = _cursor.getFloat(_cursorIndexOfCpuPercent);
            final float _tmpMemoryMb;
            _tmpMemoryMb = _cursor.getFloat(_cursorIndexOfMemoryMb);
            final boolean _tmpJankDetected;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfJankDetected);
            _tmpJankDetected = _tmp != 0;
            _item = new TelemetrySample(_tmpId,_tmpSessionId,_tmpTimestamp,_tmpFps,_tmpFrameTimeMs,_tmpCpuPercent,_tmpMemoryMb,_tmpJankDetected);
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
  public Object getSamplesInWindow(final String sessionId, final long from, final long to,
      final Continuation<? super List<TelemetrySample>> $completion) {
    final String _sql = "SELECT * FROM telemetry_samples WHERE sessionId = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, from);
    _argIndex = 3;
    _statement.bindLong(_argIndex, to);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TelemetrySample>>() {
      @Override
      @NonNull
      public List<TelemetrySample> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfFps = CursorUtil.getColumnIndexOrThrow(_cursor, "fps");
          final int _cursorIndexOfFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "frameTimeMs");
          final int _cursorIndexOfCpuPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuPercent");
          final int _cursorIndexOfMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "memoryMb");
          final int _cursorIndexOfJankDetected = CursorUtil.getColumnIndexOrThrow(_cursor, "jankDetected");
          final List<TelemetrySample> _result = new ArrayList<TelemetrySample>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TelemetrySample _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final float _tmpFps;
            _tmpFps = _cursor.getFloat(_cursorIndexOfFps);
            final float _tmpFrameTimeMs;
            _tmpFrameTimeMs = _cursor.getFloat(_cursorIndexOfFrameTimeMs);
            final float _tmpCpuPercent;
            _tmpCpuPercent = _cursor.getFloat(_cursorIndexOfCpuPercent);
            final float _tmpMemoryMb;
            _tmpMemoryMb = _cursor.getFloat(_cursorIndexOfMemoryMb);
            final boolean _tmpJankDetected;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfJankDetected);
            _tmpJankDetected = _tmp != 0;
            _item = new TelemetrySample(_tmpId,_tmpSessionId,_tmpTimestamp,_tmpFps,_tmpFrameTimeMs,_tmpCpuPercent,_tmpMemoryMb,_tmpJankDetected);
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
