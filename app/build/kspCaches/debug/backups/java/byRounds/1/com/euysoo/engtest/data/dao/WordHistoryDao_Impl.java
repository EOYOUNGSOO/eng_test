package com.euysoo.engtest.data.dao;

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
import com.euysoo.engtest.data.entity.WordHistoryEntity;
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
public final class WordHistoryDao_Impl implements WordHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WordHistoryEntity> __insertionAdapterOfWordHistoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public WordHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWordHistoryEntity = new EntityInsertionAdapter<WordHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `word_history` (`id`,`word`,`action`,`beforePos`,`beforeMeaning`,`beforeLevel`,`afterPos`,`afterMeaning`,`afterLevel`,`sourceVersion`,`recordedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getWord());
        statement.bindString(3, entity.getAction());
        if (entity.getBeforePos() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getBeforePos());
        }
        if (entity.getBeforeMeaning() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBeforeMeaning());
        }
        if (entity.getBeforeLevel() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBeforeLevel());
        }
        if (entity.getAfterPos() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAfterPos());
        }
        if (entity.getAfterMeaning() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getAfterMeaning());
        }
        if (entity.getAfterLevel() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAfterLevel());
        }
        statement.bindString(10, entity.getSourceVersion());
        statement.bindLong(11, entity.getRecordedAt());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM word_history";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final WordHistoryEntity history,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWordHistoryEntity.insert(history);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<WordHistoryEntity> histories,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWordHistoryEntity.insert(histories);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getHistoryByWord(final String word,
      final Continuation<? super List<WordHistoryEntity>> $completion) {
    final String _sql = "SELECT * FROM word_history WHERE word = ? ORDER BY recordedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, word);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordHistoryEntity>>() {
      @Override
      @NonNull
      public List<WordHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfBeforePos = CursorUtil.getColumnIndexOrThrow(_cursor, "beforePos");
          final int _cursorIndexOfBeforeMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeMeaning");
          final int _cursorIndexOfBeforeLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeLevel");
          final int _cursorIndexOfAfterPos = CursorUtil.getColumnIndexOrThrow(_cursor, "afterPos");
          final int _cursorIndexOfAfterMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "afterMeaning");
          final int _cursorIndexOfAfterLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "afterLevel");
          final int _cursorIndexOfSourceVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceVersion");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final List<WordHistoryEntity> _result = new ArrayList<WordHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordHistoryEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final String _tmpBeforePos;
            if (_cursor.isNull(_cursorIndexOfBeforePos)) {
              _tmpBeforePos = null;
            } else {
              _tmpBeforePos = _cursor.getString(_cursorIndexOfBeforePos);
            }
            final String _tmpBeforeMeaning;
            if (_cursor.isNull(_cursorIndexOfBeforeMeaning)) {
              _tmpBeforeMeaning = null;
            } else {
              _tmpBeforeMeaning = _cursor.getString(_cursorIndexOfBeforeMeaning);
            }
            final String _tmpBeforeLevel;
            if (_cursor.isNull(_cursorIndexOfBeforeLevel)) {
              _tmpBeforeLevel = null;
            } else {
              _tmpBeforeLevel = _cursor.getString(_cursorIndexOfBeforeLevel);
            }
            final String _tmpAfterPos;
            if (_cursor.isNull(_cursorIndexOfAfterPos)) {
              _tmpAfterPos = null;
            } else {
              _tmpAfterPos = _cursor.getString(_cursorIndexOfAfterPos);
            }
            final String _tmpAfterMeaning;
            if (_cursor.isNull(_cursorIndexOfAfterMeaning)) {
              _tmpAfterMeaning = null;
            } else {
              _tmpAfterMeaning = _cursor.getString(_cursorIndexOfAfterMeaning);
            }
            final String _tmpAfterLevel;
            if (_cursor.isNull(_cursorIndexOfAfterLevel)) {
              _tmpAfterLevel = null;
            } else {
              _tmpAfterLevel = _cursor.getString(_cursorIndexOfAfterLevel);
            }
            final String _tmpSourceVersion;
            _tmpSourceVersion = _cursor.getString(_cursorIndexOfSourceVersion);
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            _item = new WordHistoryEntity(_tmpId,_tmpWord,_tmpAction,_tmpBeforePos,_tmpBeforeMeaning,_tmpBeforeLevel,_tmpAfterPos,_tmpAfterMeaning,_tmpAfterLevel,_tmpSourceVersion,_tmpRecordedAt);
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
  public Object getHistoryByAction(final String action,
      final Continuation<? super List<WordHistoryEntity>> $completion) {
    final String _sql = "SELECT * FROM word_history WHERE action = ? ORDER BY recordedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, action);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WordHistoryEntity>>() {
      @Override
      @NonNull
      public List<WordHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfBeforePos = CursorUtil.getColumnIndexOrThrow(_cursor, "beforePos");
          final int _cursorIndexOfBeforeMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeMeaning");
          final int _cursorIndexOfBeforeLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "beforeLevel");
          final int _cursorIndexOfAfterPos = CursorUtil.getColumnIndexOrThrow(_cursor, "afterPos");
          final int _cursorIndexOfAfterMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "afterMeaning");
          final int _cursorIndexOfAfterLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "afterLevel");
          final int _cursorIndexOfSourceVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceVersion");
          final int _cursorIndexOfRecordedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedAt");
          final List<WordHistoryEntity> _result = new ArrayList<WordHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordHistoryEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final String _tmpBeforePos;
            if (_cursor.isNull(_cursorIndexOfBeforePos)) {
              _tmpBeforePos = null;
            } else {
              _tmpBeforePos = _cursor.getString(_cursorIndexOfBeforePos);
            }
            final String _tmpBeforeMeaning;
            if (_cursor.isNull(_cursorIndexOfBeforeMeaning)) {
              _tmpBeforeMeaning = null;
            } else {
              _tmpBeforeMeaning = _cursor.getString(_cursorIndexOfBeforeMeaning);
            }
            final String _tmpBeforeLevel;
            if (_cursor.isNull(_cursorIndexOfBeforeLevel)) {
              _tmpBeforeLevel = null;
            } else {
              _tmpBeforeLevel = _cursor.getString(_cursorIndexOfBeforeLevel);
            }
            final String _tmpAfterPos;
            if (_cursor.isNull(_cursorIndexOfAfterPos)) {
              _tmpAfterPos = null;
            } else {
              _tmpAfterPos = _cursor.getString(_cursorIndexOfAfterPos);
            }
            final String _tmpAfterMeaning;
            if (_cursor.isNull(_cursorIndexOfAfterMeaning)) {
              _tmpAfterMeaning = null;
            } else {
              _tmpAfterMeaning = _cursor.getString(_cursorIndexOfAfterMeaning);
            }
            final String _tmpAfterLevel;
            if (_cursor.isNull(_cursorIndexOfAfterLevel)) {
              _tmpAfterLevel = null;
            } else {
              _tmpAfterLevel = _cursor.getString(_cursorIndexOfAfterLevel);
            }
            final String _tmpSourceVersion;
            _tmpSourceVersion = _cursor.getString(_cursorIndexOfSourceVersion);
            final long _tmpRecordedAt;
            _tmpRecordedAt = _cursor.getLong(_cursorIndexOfRecordedAt);
            _item = new WordHistoryEntity(_tmpId,_tmpWord,_tmpAction,_tmpBeforePos,_tmpBeforeMeaning,_tmpBeforeLevel,_tmpAfterPos,_tmpAfterMeaning,_tmpAfterLevel,_tmpSourceVersion,_tmpRecordedAt);
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
  public Object getSummaryAfter(final long since,
      final Continuation<? super List<ActionSummary>> $completion) {
    final String _sql = "\n"
            + "        SELECT action, COUNT(*) as count\n"
            + "        FROM word_history\n"
            + "        WHERE recordedAt >= ?\n"
            + "        GROUP BY action\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ActionSummary>>() {
      @Override
      @NonNull
      public List<ActionSummary> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfAction = 0;
          final int _cursorIndexOfCount = 1;
          final List<ActionSummary> _result = new ArrayList<ActionSummary>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ActionSummary _item;
            final String _tmpAction;
            _tmpAction = _cursor.getString(_cursorIndexOfAction);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new ActionSummary(_tmpAction,_tmpCount);
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
