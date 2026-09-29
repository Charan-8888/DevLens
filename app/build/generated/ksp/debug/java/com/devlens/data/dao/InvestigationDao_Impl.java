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
import com.devlens.data.entities.InvestigationEntity;
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
public final class InvestigationDao_Impl implements InvestigationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<InvestigationEntity> __insertionAdapterOfInvestigationEntity;

  public InvestigationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInvestigationEntity = new EntityInsertionAdapter<InvestigationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `investigations` (`id`,`incidentId`,`summary`,`hypothesesJson`,`missingEvidenceJson`,`recommendedAction`,`verificationMetric`,`confidenceLabel`,`rawAiResponse`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final InvestigationEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getIncidentId());
        statement.bindString(3, entity.getSummary());
        statement.bindString(4, entity.getHypothesesJson());
        statement.bindString(5, entity.getMissingEvidenceJson());
        statement.bindString(6, entity.getRecommendedAction());
        statement.bindString(7, entity.getVerificationMetric());
        statement.bindString(8, entity.getConfidenceLabel());
        statement.bindString(9, entity.getRawAiResponse());
        statement.bindLong(10, entity.getCreatedAt());
      }
    };
  }

  @Override
  public Object insert(final InvestigationEntity investigation,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfInvestigationEntity.insert(investigation);
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
      final Continuation<? super InvestigationEntity> $completion) {
    final String _sql = "SELECT * FROM investigations WHERE incidentId = ? ORDER BY createdAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, incidentId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<InvestigationEntity>() {
      @Override
      @Nullable
      public InvestigationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfHypothesesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "hypothesesJson");
          final int _cursorIndexOfMissingEvidenceJson = CursorUtil.getColumnIndexOrThrow(_cursor, "missingEvidenceJson");
          final int _cursorIndexOfRecommendedAction = CursorUtil.getColumnIndexOrThrow(_cursor, "recommendedAction");
          final int _cursorIndexOfVerificationMetric = CursorUtil.getColumnIndexOrThrow(_cursor, "verificationMetric");
          final int _cursorIndexOfConfidenceLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "confidenceLabel");
          final int _cursorIndexOfRawAiResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "rawAiResponse");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final InvestigationEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIncidentId;
            _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final String _tmpHypothesesJson;
            _tmpHypothesesJson = _cursor.getString(_cursorIndexOfHypothesesJson);
            final String _tmpMissingEvidenceJson;
            _tmpMissingEvidenceJson = _cursor.getString(_cursorIndexOfMissingEvidenceJson);
            final String _tmpRecommendedAction;
            _tmpRecommendedAction = _cursor.getString(_cursorIndexOfRecommendedAction);
            final String _tmpVerificationMetric;
            _tmpVerificationMetric = _cursor.getString(_cursorIndexOfVerificationMetric);
            final String _tmpConfidenceLabel;
            _tmpConfidenceLabel = _cursor.getString(_cursorIndexOfConfidenceLabel);
            final String _tmpRawAiResponse;
            _tmpRawAiResponse = _cursor.getString(_cursorIndexOfRawAiResponse);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new InvestigationEntity(_tmpId,_tmpIncidentId,_tmpSummary,_tmpHypothesesJson,_tmpMissingEvidenceJson,_tmpRecommendedAction,_tmpVerificationMetric,_tmpConfidenceLabel,_tmpRawAiResponse,_tmpCreatedAt);
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
  public Object getById(final String id,
      final Continuation<? super InvestigationEntity> $completion) {
    final String _sql = "SELECT * FROM investigations WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<InvestigationEntity>() {
      @Override
      @Nullable
      public InvestigationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfIncidentId = CursorUtil.getColumnIndexOrThrow(_cursor, "incidentId");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfHypothesesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "hypothesesJson");
          final int _cursorIndexOfMissingEvidenceJson = CursorUtil.getColumnIndexOrThrow(_cursor, "missingEvidenceJson");
          final int _cursorIndexOfRecommendedAction = CursorUtil.getColumnIndexOrThrow(_cursor, "recommendedAction");
          final int _cursorIndexOfVerificationMetric = CursorUtil.getColumnIndexOrThrow(_cursor, "verificationMetric");
          final int _cursorIndexOfConfidenceLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "confidenceLabel");
          final int _cursorIndexOfRawAiResponse = CursorUtil.getColumnIndexOrThrow(_cursor, "rawAiResponse");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final InvestigationEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpIncidentId;
            _tmpIncidentId = _cursor.getString(_cursorIndexOfIncidentId);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final String _tmpHypothesesJson;
            _tmpHypothesesJson = _cursor.getString(_cursorIndexOfHypothesesJson);
            final String _tmpMissingEvidenceJson;
            _tmpMissingEvidenceJson = _cursor.getString(_cursorIndexOfMissingEvidenceJson);
            final String _tmpRecommendedAction;
            _tmpRecommendedAction = _cursor.getString(_cursorIndexOfRecommendedAction);
            final String _tmpVerificationMetric;
            _tmpVerificationMetric = _cursor.getString(_cursorIndexOfVerificationMetric);
            final String _tmpConfidenceLabel;
            _tmpConfidenceLabel = _cursor.getString(_cursorIndexOfConfidenceLabel);
            final String _tmpRawAiResponse;
            _tmpRawAiResponse = _cursor.getString(_cursorIndexOfRawAiResponse);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new InvestigationEntity(_tmpId,_tmpIncidentId,_tmpSummary,_tmpHypothesesJson,_tmpMissingEvidenceJson,_tmpRecommendedAction,_tmpVerificationMetric,_tmpConfidenceLabel,_tmpRawAiResponse,_tmpCreatedAt);
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
