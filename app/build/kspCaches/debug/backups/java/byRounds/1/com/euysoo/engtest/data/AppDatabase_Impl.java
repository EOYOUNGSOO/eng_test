package com.euysoo.engtest.data;

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
import com.euysoo.engtest.data.dao.TestResultDao;
import com.euysoo.engtest.data.dao.TestResultDao_Impl;
import com.euysoo.engtest.data.dao.WordBookDao;
import com.euysoo.engtest.data.dao.WordBookDao_Impl;
import com.euysoo.engtest.data.dao.WordDao;
import com.euysoo.engtest.data.dao.WordDao_Impl;
import com.euysoo.engtest.data.dao.WordDetailDao;
import com.euysoo.engtest.data.dao.WordDetailDao_Impl;
import com.euysoo.engtest.data.dao.WordHistoryDao;
import com.euysoo.engtest.data.dao.WordHistoryDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile WordDao _wordDao;

  private volatile TestResultDao _testResultDao;

  private volatile WordHistoryDao _wordHistoryDao;

  private volatile WordDetailDao _wordDetailDao;

  private volatile WordBookDao _wordBookDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(10) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `words` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `partOfSpeech` TEXT NOT NULL, `meaning` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `sourceVersion` TEXT NOT NULL, `phonetic` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_difficulty` ON `words` (`difficulty`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_word` ON `words` (`word`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `test_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `testDateMillis` INTEGER NOT NULL, `score` INTEGER NOT NULL, `details` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `test_type` TEXT NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_test_results_testDateMillis` ON `test_results` (`testDateMillis`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `action` TEXT NOT NULL, `beforePos` TEXT, `beforeMeaning` TEXT, `beforeLevel` TEXT, `afterPos` TEXT, `afterMeaning` TEXT, `afterLevel` TEXT, `sourceVersion` TEXT NOT NULL, `recordedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_details` (`word` TEXT NOT NULL, `phonetic` TEXT, `meaningsJson` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`word`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_books` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_book_entries` (`bookId` INTEGER NOT NULL, `wordId` INTEGER NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`bookId`, `wordId`), FOREIGN KEY(`bookId`) REFERENCES `word_books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`wordId`) REFERENCES `words`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_word_book_entries_bookId` ON `word_book_entries` (`bookId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_word_book_entries_wordId` ON `word_book_entries` (`wordId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'eadb586f8c31185ddfccba5d30420ff6')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `words`");
        db.execSQL("DROP TABLE IF EXISTS `test_results`");
        db.execSQL("DROP TABLE IF EXISTS `word_history`");
        db.execSQL("DROP TABLE IF EXISTS `word_details`");
        db.execSQL("DROP TABLE IF EXISTS `word_books`");
        db.execSQL("DROP TABLE IF EXISTS `word_book_entries`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsWords = new HashMap<String, TableInfo.Column>(9);
        _columnsWords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("word", new TableInfo.Column("word", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("partOfSpeech", new TableInfo.Column("partOfSpeech", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("meaning", new TableInfo.Column("meaning", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("sourceVersion", new TableInfo.Column("sourceVersion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWords.put("phonetic", new TableInfo.Column("phonetic", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWords = new HashSet<TableInfo.Index>(2);
        _indicesWords.add(new TableInfo.Index("index_words_difficulty", false, Arrays.asList("difficulty"), Arrays.asList("ASC")));
        _indicesWords.add(new TableInfo.Index("index_words_word", false, Arrays.asList("word"), Arrays.asList("ASC")));
        final TableInfo _infoWords = new TableInfo("words", _columnsWords, _foreignKeysWords, _indicesWords);
        final TableInfo _existingWords = TableInfo.read(db, "words");
        if (!_infoWords.equals(_existingWords)) {
          return new RoomOpenHelper.ValidationResult(false, "words(com.euysoo.engtest.data.entity.Word).\n"
                  + " Expected:\n" + _infoWords + "\n"
                  + " Found:\n" + _existingWords);
        }
        final HashMap<String, TableInfo.Column> _columnsTestResults = new HashMap<String, TableInfo.Column>(6);
        _columnsTestResults.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("testDateMillis", new TableInfo.Column("testDateMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("details", new TableInfo.Column("details", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("test_type", new TableInfo.Column("test_type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTestResults = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTestResults = new HashSet<TableInfo.Index>(1);
        _indicesTestResults.add(new TableInfo.Index("index_test_results_testDateMillis", false, Arrays.asList("testDateMillis"), Arrays.asList("ASC")));
        final TableInfo _infoTestResults = new TableInfo("test_results", _columnsTestResults, _foreignKeysTestResults, _indicesTestResults);
        final TableInfo _existingTestResults = TableInfo.read(db, "test_results");
        if (!_infoTestResults.equals(_existingTestResults)) {
          return new RoomOpenHelper.ValidationResult(false, "test_results(com.euysoo.engtest.data.entity.TestResult).\n"
                  + " Expected:\n" + _infoTestResults + "\n"
                  + " Found:\n" + _existingTestResults);
        }
        final HashMap<String, TableInfo.Column> _columnsWordHistory = new HashMap<String, TableInfo.Column>(11);
        _columnsWordHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("word", new TableInfo.Column("word", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("action", new TableInfo.Column("action", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("beforePos", new TableInfo.Column("beforePos", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("beforeMeaning", new TableInfo.Column("beforeMeaning", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("beforeLevel", new TableInfo.Column("beforeLevel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("afterPos", new TableInfo.Column("afterPos", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("afterMeaning", new TableInfo.Column("afterMeaning", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("afterLevel", new TableInfo.Column("afterLevel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("sourceVersion", new TableInfo.Column("sourceVersion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordHistory.put("recordedAt", new TableInfo.Column("recordedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWordHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWordHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWordHistory = new TableInfo("word_history", _columnsWordHistory, _foreignKeysWordHistory, _indicesWordHistory);
        final TableInfo _existingWordHistory = TableInfo.read(db, "word_history");
        if (!_infoWordHistory.equals(_existingWordHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "word_history(com.euysoo.engtest.data.entity.WordHistoryEntity).\n"
                  + " Expected:\n" + _infoWordHistory + "\n"
                  + " Found:\n" + _existingWordHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsWordDetails = new HashMap<String, TableInfo.Column>(4);
        _columnsWordDetails.put("word", new TableInfo.Column("word", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordDetails.put("phonetic", new TableInfo.Column("phonetic", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordDetails.put("meaningsJson", new TableInfo.Column("meaningsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordDetails.put("fetchedAt", new TableInfo.Column("fetchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWordDetails = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWordDetails = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWordDetails = new TableInfo("word_details", _columnsWordDetails, _foreignKeysWordDetails, _indicesWordDetails);
        final TableInfo _existingWordDetails = TableInfo.read(db, "word_details");
        if (!_infoWordDetails.equals(_existingWordDetails)) {
          return new RoomOpenHelper.ValidationResult(false, "word_details(com.euysoo.engtest.data.entity.WordDetailEntity).\n"
                  + " Expected:\n" + _infoWordDetails + "\n"
                  + " Found:\n" + _existingWordDetails);
        }
        final HashMap<String, TableInfo.Column> _columnsWordBooks = new HashMap<String, TableInfo.Column>(3);
        _columnsWordBooks.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordBooks.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordBooks.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWordBooks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWordBooks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWordBooks = new TableInfo("word_books", _columnsWordBooks, _foreignKeysWordBooks, _indicesWordBooks);
        final TableInfo _existingWordBooks = TableInfo.read(db, "word_books");
        if (!_infoWordBooks.equals(_existingWordBooks)) {
          return new RoomOpenHelper.ValidationResult(false, "word_books(com.euysoo.engtest.data.entity.WordBook).\n"
                  + " Expected:\n" + _infoWordBooks + "\n"
                  + " Found:\n" + _existingWordBooks);
        }
        final HashMap<String, TableInfo.Column> _columnsWordBookEntries = new HashMap<String, TableInfo.Column>(3);
        _columnsWordBookEntries.put("bookId", new TableInfo.Column("bookId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordBookEntries.put("wordId", new TableInfo.Column("wordId", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWordBookEntries.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWordBookEntries = new HashSet<TableInfo.ForeignKey>(2);
        _foreignKeysWordBookEntries.add(new TableInfo.ForeignKey("word_books", "CASCADE", "NO ACTION", Arrays.asList("bookId"), Arrays.asList("id")));
        _foreignKeysWordBookEntries.add(new TableInfo.ForeignKey("words", "CASCADE", "NO ACTION", Arrays.asList("wordId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesWordBookEntries = new HashSet<TableInfo.Index>(2);
        _indicesWordBookEntries.add(new TableInfo.Index("index_word_book_entries_bookId", false, Arrays.asList("bookId"), Arrays.asList("ASC")));
        _indicesWordBookEntries.add(new TableInfo.Index("index_word_book_entries_wordId", false, Arrays.asList("wordId"), Arrays.asList("ASC")));
        final TableInfo _infoWordBookEntries = new TableInfo("word_book_entries", _columnsWordBookEntries, _foreignKeysWordBookEntries, _indicesWordBookEntries);
        final TableInfo _existingWordBookEntries = TableInfo.read(db, "word_book_entries");
        if (!_infoWordBookEntries.equals(_existingWordBookEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "word_book_entries(com.euysoo.engtest.data.entity.WordBookEntry).\n"
                  + " Expected:\n" + _infoWordBookEntries + "\n"
                  + " Found:\n" + _existingWordBookEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "eadb586f8c31185ddfccba5d30420ff6", "ff87b39c8257e171c6c7c937094928f2");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "words","test_results","word_history","word_details","word_books","word_book_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `words`");
      _db.execSQL("DELETE FROM `test_results`");
      _db.execSQL("DELETE FROM `word_history`");
      _db.execSQL("DELETE FROM `word_details`");
      _db.execSQL("DELETE FROM `word_books`");
      _db.execSQL("DELETE FROM `word_book_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(WordDao.class, WordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TestResultDao.class, TestResultDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WordHistoryDao.class, WordHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WordDetailDao.class, WordDetailDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WordBookDao.class, WordBookDao_Impl.getRequiredConverters());
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
  public WordDao wordDao() {
    if (_wordDao != null) {
      return _wordDao;
    } else {
      synchronized(this) {
        if(_wordDao == null) {
          _wordDao = new WordDao_Impl(this);
        }
        return _wordDao;
      }
    }
  }

  @Override
  public TestResultDao testResultDao() {
    if (_testResultDao != null) {
      return _testResultDao;
    } else {
      synchronized(this) {
        if(_testResultDao == null) {
          _testResultDao = new TestResultDao_Impl(this);
        }
        return _testResultDao;
      }
    }
  }

  @Override
  public WordHistoryDao wordHistoryDao() {
    if (_wordHistoryDao != null) {
      return _wordHistoryDao;
    } else {
      synchronized(this) {
        if(_wordHistoryDao == null) {
          _wordHistoryDao = new WordHistoryDao_Impl(this);
        }
        return _wordHistoryDao;
      }
    }
  }

  @Override
  public WordDetailDao wordDetailDao() {
    if (_wordDetailDao != null) {
      return _wordDetailDao;
    } else {
      synchronized(this) {
        if(_wordDetailDao == null) {
          _wordDetailDao = new WordDetailDao_Impl(this);
        }
        return _wordDetailDao;
      }
    }
  }

  @Override
  public WordBookDao wordBookDao() {
    if (_wordBookDao != null) {
      return _wordBookDao;
    } else {
      synchronized(this) {
        if(_wordBookDao == null) {
          _wordBookDao = new WordBookDao_Impl(this);
        }
        return _wordBookDao;
      }
    }
  }
}
