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
import com.devlens.data.entities.VerificationRunEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class VerificationDao_Impl implements VerificationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VerificationRunEntity> __insertionAdapterOfVerificationRunEntity;

  public VerificationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVerificationRunEntity = new EntityInsertionAdapter<VerificationRunEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `verification_runs` (`id`,`incidentId`,`investigationId`,`beforeFps`,`beforeCpu`,`beforeMemoryMb`,`beforeFrameTimeMs`,`afterFps`,`afterCpu`,`afterMemoryMb`,`afterFrameTimeMs`,`outcome`,`fpsImprovementPercent`,`cpuReductionPercent`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VerificationRunEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getIncidentId());
        statement.bindString(3, entity.getInvestigationId());
        statement.bindDouble(4, entity.getBeforeFps());
        statement.bindDouble(5, entity.getBeforeCpu());
        statement.bindDouble(6, entity.getBeforeMemoryMb());
        statement.bindDouble(7, entity.getBeforeFrameTimeMs());
        statement.bindDouble(8, entity.getAfterFps());
        statement.bindDouble(9, entity.getAfterCpu());
        statement.bindDouble(10, entity.getAfterMemoryMb());
        statement.bindDouble(11, entity.getAfterFrameTimeMs());
        statement.bindString(12, entity.getOutcome());
        statement.bindDouble(13, entity.getFpsImprovementPercent());
        statement.bindDouble(14, entity.getCpuReductionPercent());
        statement.bindLong(15, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insert(final VerificationRunEntity run,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfVerificationRunEntity.insert(run);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestForIncident(final String incidentId,
      final Continuation<? super VerificationRunEntity> $completion) {
    final String _sql = "SELECT * FROM verification_runs WHERE incidentId = ? ORDER BY createdAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, incidentId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<VerificationRunEntity>() {
      @Override
      @Nullable
      public VerificationRunEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final int _cursorIndexOfInvestigationId = CursorUtil.getColumnIndexOrThrow(_cursor, "investigationId");
          final int _cursorIndexOfBeforeFps = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFps");
          final int _cursorIndexOfBeforeCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeCpu");
          final int _cursorIndexOfBeforeMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeMemoryMb");
          final int _cursorIndexOfBeforeFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeFrameTimeMs");
          final int _cursorIndexOfAfterFps = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFps");
          final int _cursorIndexOfAfterCpu = CursorUtil.getColumnIndexOrThrow(_cursor, "afterCpu");
          final int _cursorIndexOfAfterMemoryMb = CursorUtil.getColumnIndexOrThrow(_cursor, "afterMemoryMb");
          final int _cursorIndexOfAfterFrameTimeMs = CursorUtil.getColumnIndexOrThrow(_cursor, "afterFrameTimeMs");
          final int _cursorIndexOfOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "outcome");
          final int _cursorIndexOfFpsImprovementPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "fpsImprovementPercent");
          final int _cursorIndexOfCpuReductionPercent = CursorUtil.getColumnIndexOrThrow(_cursor, "cpuReductionPercent");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final VerificationRunEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIncidentId;
            _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            final String _tmpInvestigationId;
            _tmpInvestigationId = _cursor.getString(_cursorIndexOfInvestigationId);
            final float _tmpBeforeFps;
            _tmpBeforeFps = _cursor.getFloat(_cursorIndexOfBeforeFps);
            final float _tmpBeforeCpu;
            _tmpBeforeCpu = _cursor.getFloat(_cursorIndexOfBeforeCpu);
            final float _tmpBeforeMemoryMb;
            _tmpBeforeMemoryMb = _cursor.getFloat(_cursorIndexOfBeforeMemoryMb);
            final float _tmpBeforeFrameTimeMs;
            _tmpBeforeFrameTimeMs = _cursor.getFloat(_cursorIndexOfBeforeFrameTimeMs);
            final float _tmpAfterFps;
            _tmpAfterFps = _cursor.getFloat(_cursorIndexOfAfterFps);
            final float _tmpAfterCpu;
            _tmpAfterCpu = _cursor.getFloat(_cursorIndexOfAfterCpu);
            final float _tmpAfterMemoryMb;
            _tmpAfterMemoryMb = _cursor.getFloat(_cursorIndexOfAfterMemoryMb);
            final float _tmpAfterFrameTimeMs;
            _tmpAfterFrameTimeMs = _cursor.getFloat(_cursorIndexOfAfterFrameTimeMs);
            final String _tmpOutcome;
            _tmpOutcome = _cursor.getString(_cursorIndexOfOutcome);
            final float _tmpFpsImprovementPercent;
            _tmpFpsImprovementPercent = _cursor.getFloat(_cursorIndexOfFpsImprovementPercent);
            final float _tmpCpuReductionPercent;
            _tmpCpuReductionPercent = _cursor.getFloat(_cursorIndexOfCpuReductionPercent);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new VerificationRunEntity(_tmpId,_tmpIncidentId,_tmpInvestigationId,_tmpBeforeFps,_tmpBeforeCpu,_tmpBeforeMemoryMb,_tmpBeforeFrameTimeMs,_tmpAfterFps,_tmpAfterCpu,_tmpAfterMemoryMb,_tmpAfterFrameTimeMs,_tmpOutcome,_tmpFpsImprovementPercent,_tmpCpuReductionPercent,_tmpCreatedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
