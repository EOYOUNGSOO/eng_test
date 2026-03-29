package com.euysoo.engtest.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.euysoo.engtest.data.entity.Word;
import com.euysoo.engtest.data.entity.WordBook;
import com.euysoo.engtest.data.entity.WordBookEntry;
import com.euysoo.engtest.data.entity.WordDifficulty;
import com.euysoo.engtest.data.entity.WordWithBookEntryMeta;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WordBookDao_Impl implements WordBookDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WordBook> __insertionAdapterOfWordBook;

  private final EntityInsertionAdapter<WordBookEntry> __insertionAdapterOfWordBookEntry;

  private final EntityDeletionOrUpdateAdapter<WordBook> __updateAdapterOfWordBook;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBookById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteEntry;

  public WordBookDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWordBook = new EntityInsertionAdapter<WordBook>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `word_books` (`id`,`name`,`createdAt`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordBook entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
      }
    };
    this.__insertionAdapterOfWordBookEntry = new EntityInsertionAdapter<WordBookEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `word_book_entries` (`bookId`,`wordId`,`addedAt`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordBookEntry entity) {
        statement.bindLong(1, entity.getBookId());
        statement.bindLong(2, entity.getWordId());
        statement.bindLong(3, entity.getAddedAt());
      }
    };
    this.__updateAdapterOfWordBook = new EntityDeletionOrUpdateAdapter<WordBook>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `word_books` SET `id` = ?,`name` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WordBook entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteBookById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM word_books WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteEntry = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM word_book_entries WHERE bookId = ? AND wordId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBook(final WordBook book, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfWordBook.insertAndReturnId(book);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertEntry(final WordBookEntry entry,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWordBookEntry.insert(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBook(final WordBook book, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfWordBook.handle(book);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBookById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBookById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteBookById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteEntry(final long bookId, final long wordId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteEntry.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, bookId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, wordId);
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
          __preparedStmtOfDeleteEntry.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WordBook>> getAllBooks() {
    final String _sql = "SELECT * FROM word_books ORDER BY createdAt DESC, id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"word_books"}, new Callable<List<WordBook>>() {
      @Override
      @NonNull
      public List<WordBook> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<WordBook> _result = new ArrayList<WordBook>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordBook _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new WordBook(_tmpId,_tmpName,_tmpCreatedAt);
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
  public Object getBookById(final long id, final Continuation<? super WordBook> $completion) {
    final String _sql = "SELECT * FROM word_books WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WordBook>() {
      @Override
      @Nullable
      public WordBook call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final WordBook _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new WordBook(_tmpId,_tmpName,_tmpCreatedAt);
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
  public Flow<List<WordWithBookEntryMeta>> getWordsInBook(final long bookId) {
    final String _sql = "\n"
            + "        SELECT w.*, e.addedAt AS entryAddedAt FROM words w\n"
            + "        INNER JOIN word_book_entries e ON w.id = e.wordId\n"
            + "        WHERE e.bookId = ?\n"
            + "        ORDER BY e.addedAt DESC, w.word ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, bookId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"words",
        "word_book_entries"}, new Callable<List<WordWithBookEntryMeta>>() {
      @Override
      @NonNull
      public List<WordWithBookEntryMeta> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSourceVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceVersion");
          final int _cursorIndexOfPhonetic = CursorUtil.getColumnIndexOrThrow(_cursor, "phonetic");
          final int _cursorIndexOfEntryAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "entryAddedAt");
          final List<WordWithBookEntryMeta> _result = new ArrayList<WordWithBookEntryMeta>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WordWithBookEntryMeta _item;
            final long _tmpEntryAddedAt;
            _tmpEntryAddedAt = _cursor.getLong(_cursorIndexOfEntryAddedAt);
            final Word _tmpWord;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord_1;
            _tmpWord_1 = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final WordDifficulty _tmpDifficulty;
            _tmpDifficulty = __WordDifficulty_stringToEnum(_cursor.getString(_cursorIndexOfDifficulty));
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSourceVersion;
            _tmpSourceVersion = _cursor.getString(_cursorIndexOfSourceVersion);
            final String _tmpPhonetic;
            if (_cursor.isNull(_cursorIndexOfPhonetic)) {
              _tmpPhonetic = null;
            } else {
              _tmpPhonetic = _cursor.getString(_cursorIndexOfPhonetic);
            }
            _tmpWord = new Word(_tmpId,_tmpWord_1,_tmpPartOfSpeech,_tmpMeaning,_tmpDifficulty,_tmpAddedAt,_tmpUpdatedAt,_tmpSourceVersion,_tmpPhonetic);
            _item = new WordWithBookEntryMeta(_tmpWord,_tmpEntryAddedAt);
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
  public Object getRandomWordsFromBook(final long bookId, final int limit,
      final Continuation<? super List<Word>> $completion) {
    final String _sql = "\n"
            + "        SELECT w.* FROM words w\n"
            + "        INNER JOIN word_book_entries e ON w.id = e.wordId\n"
            + "        WHERE e.bookId = ?\n"
            + "        ORDER BY RANDOM()\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, bookId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Word>>() {
      @Override
      @NonNull
      public List<Word> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfWord = CursorUtil.getColumnIndexOrThrow(_cursor, "word");
          final int _cursorIndexOfPartOfSpeech = CursorUtil.getColumnIndexOrThrow(_cursor, "partOfSpeech");
          final int _cursorIndexOfMeaning = CursorUtil.getColumnIndexOrThrow(_cursor, "meaning");
          final int _cursorIndexOfDifficulty = CursorUtil.getColumnIndexOrThrow(_cursor, "difficulty");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfSourceVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceVersion");
          final int _cursorIndexOfPhonetic = CursorUtil.getColumnIndexOrThrow(_cursor, "phonetic");
          final List<Word> _result = new ArrayList<Word>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Word _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpWord;
            _tmpWord = _cursor.getString(_cursorIndexOfWord);
            final String _tmpPartOfSpeech;
            _tmpPartOfSpeech = _cursor.getString(_cursorIndexOfPartOfSpeech);
            final String _tmpMeaning;
            _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
            final WordDifficulty _tmpDifficulty;
            _tmpDifficulty = __WordDifficulty_stringToEnum(_cursor.getString(_cursorIndexOfDifficulty));
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final String _tmpSourceVersion;
            _tmpSourceVersion = _cursor.getString(_cursorIndexOfSourceVersion);
            final String _tmpPhonetic;
            if (_cursor.isNull(_cursorIndexOfPhonetic)) {
              _tmpPhonetic = null;
            } else {
              _tmpPhonetic = _cursor.getString(_cursorIndexOfPhonetic);
            }
            _item = new Word(_tmpId,_tmpWord,_tmpPartOfSpeech,_tmpMeaning,_tmpDifficulty,_tmpAddedAt,_tmpUpdatedAt,_tmpSourceVersion,_tmpPhonetic);
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
  public Object countEntry(final long bookId, final long wordId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM word_book_entries WHERE bookId = ? AND wordId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, bookId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, wordId);
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

  private WordDifficulty __WordDifficulty_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "ELEMENTARY": return WordDifficulty.ELEMENTARY;
      case "MIDDLE": return WordDifficulty.MIDDLE;
      case "HIGH": return WordDifficulty.HIGH;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
