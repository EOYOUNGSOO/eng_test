package com.euysoo.engtest.data.dao;

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
import com.euysoo.engtest.data.entity.TestResult;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TestResultDao_Impl implements TestResultDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TestResult> __insertionAdapterOfTestResult;

  public TestResultDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTestResult = new EntityInsertionAdapter<TestResult>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `test_results` (`id`,`testDateMillis`,`score`,`details`,`difficulty`,`test_type`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TestResult entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTestDateMillis());
        statement.bindLong(3, entity.getScore());
        statement.bindString(4, entity.getDetails());
        statement.bindString(5, entity.getDifficulty());
        statement.bindString(6, entity.getTestType());
      }
    };
  }

  @Override
  public Object insert(final TestResult result, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTestResult.insertAndReturnId(result);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TestResult>> getAllResults() {
    final String _sql = "SELECT * FROM test_results ORDER BY testDateMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"test_results"}, new Callable<List<TestResult>>() {
      @Override
      @NonNull
      public List<TestResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTestDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "testDateMillis");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfTestType = CursorUtil.getColumnIndexOrThrow(_cursor, "test_type");
          final List<TestResult> _result = new ArrayList<TestResult>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TestResult _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTestDateMillis;
            _tmpTestDateMillis = _cursor.getLong(_cursorIndexOfTestDateMillis);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpTestType;
            _tmpTestType = _cursor.getString(_cursorIndexOfTestType);
            _item = new TestResult(_tmpId,_tmpTestDateMillis,_tmpScore,_tmpDetails,_tmpDifficulty,_tmpTestType);
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
  public Flow<List<TestResult>> getResultsBetween(final long fromMillis, final long toMillis) {
    final String _sql = "SELECT * FROM test_results WHERE testDateMillis >= ? AND testDateMillis <= ? ORDER BY testDateMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromMillis);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toMillis);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"test_results"}, new Callable<List<TestResult>>() {
      @Override
      @NonNull
      public List<TestResult> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTestDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "testDateMillis");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfTestType = CursorUtil.getColumnIndexOrThrow(_cursor, "test_type");
          final List<TestResult> _result = new ArrayList<TestResult>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TestResult _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTestDateMillis;
            _tmpTestDateMillis = _cursor.getLong(_cursorIndexOfTestDateMillis);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpTestType;
            _tmpTestType = _cursor.getString(_cursorIndexOfTestType);
            _item = new TestResult(_tmpId,_tmpTestDateMillis,_tmpScore,_tmpDetails,_tmpDifficulty,_tmpTestType);
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
  public Object getById(final long id, final Continuation<? super TestResult> $completion) {
    final String _sql = "SELECT * FROM test_results WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TestResult>() {
      @Override
      @Nullable
      public TestResult call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTestDateMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "testDateMillis");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final int _cursorIndexOfDetails = CursorUtil.getColumnIndexOrThrow(_cursor, "details");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfTestType = CursorUtil.getColumnIndexOrThrow(_cursor, "test_type");
          final TestResult _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTestDateMillis;
            _tmpTestDateMillis = _cursor.getLong(_cursorIndexOfTestDateMillis);
            final int _tmpScore;
            _tmpScore = _cursor.getInt(_cursorIndexOfScore);
            final String _tmpDetails;
            _tmpDetails = _cursor.getString(_cursorIndexOfDetails);
            final String _tmpDifficulty;
            _tmpDifficulty = _cursor.getString(_cursorIndexOfDifficulty);
            final String _tmpTestType;
            _tmpTestType = _cursor.getString(_cursorIndexOfTestType);
            _result = new TestResult(_tmpId,_tmpTestDateMillis,_tmpScore,_tmpDetails,_tmpDifficulty,_tmpTestType);
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
  public Flow<Integer> getCountFlow() {
    final String _sql = "SELECT COUNT(*) FROM test_results";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"test_results"}, new Callable<Integer>() {
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
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Float> getAverageScoreFlow() {
    final String _sql = "SELECT AVG(score * 10) FROM test_results";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"test_results"}, new Callable<Float>() {
      @Override
      @Nullable
      public Float call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Float _result;
          if (_cursor.moveToFirst()) {
            final Float _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getFloat(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
