package com.example.engtest.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.engtest.data.entity.WordDetailEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class WordDetailDao_Impl implements WordDetailDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WordDetailEntity> __insertionAdapterOfWordDetailEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public WordDetailDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWordDetailEntity = new EntityInsertionAdapter<WordDetailEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `word_details` (`word`,`phonetic`,`meaningsJson`,`fetchedAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordDetailEntity entity) {
        statement.bindString(1, entity.getWord());
        if (entity.getPhonetic() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPhonetic());
        }
        statement.bindString(3, entity.getMeaningsJson());
        statement.bindLong(4, entity.getFetchedAt());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM word_details";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final WordDetailEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWordDetailEntity.insert(entity);
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
  public Object getByWord(final String word,
      final Continuation<? super WordDetailEntity> $completion) {
    final String _sql = "SELECT * FROM word_details WHERE LOWER(word) = LOWER(?) LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, word);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WordDetailEntity>() {
      @Override
      @Nullable
      public WordDetailEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPhonetic = CursorUtil.getColumnIndexOrThrow(_cursor, "phonetic");
          final int _cursorIndexOfMeaningsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "meaningsJson");
          final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
          final WordDetailEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPhonetic;
            if (_cursor.isNull(_cursorIndexOfPhonetic)) {
              _tmpPhonetic = null;
            } else {
              _tmpPhonetic = _cursor.getString(_cursorIndexOfPhonetic);
            }
            final String _tmpMeaningsJson;
            _tmpMeaningsJson = _cursor.getString(_cursorIndexOfMeaningsJson);
            final long _tmpFetchedAt;
            _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
            _result = new WordDetailEntity(_tmpWord,_tmpPhonetic,_tmpMeaningsJson,_tmpFetchedAt);
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
  public Object getCachedCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM word_details";
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
