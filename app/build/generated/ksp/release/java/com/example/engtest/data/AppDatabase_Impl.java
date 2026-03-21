package com.example.engtest.data;

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
import com.example.engtest.data.dao.TestResultDao;
import com.example.engtest.data.dao.TestResultDao_Impl;
import com.example.engtest.data.dao.WordDao;
import com.example.engtest.data.dao.WordDao_Impl;
import com.example.engtest.data.dao.WordDetailDao;
import com.example.engtest.data.dao.WordDetailDao_Impl;
import com.example.engtest.data.dao.WordHistoryDao;
import com.example.engtest.data.dao.WordHistoryDao_Impl;
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

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(7) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `words` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `partOfSpeech` TEXT NOT NULL, `meaning` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `sourceVersion` TEXT NOT NULL, `phonetic` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_difficulty` ON `words` (`difficulty`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_words_word` ON `words` (`word`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `test_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `testDateMillis` INTEGER NOT NULL, `score` INTEGER NOT NULL, `details` TEXT NOT NULL, `difficulty` TEXT NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_test_results_testDateMillis` ON `test_results` (`testDateMillis`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `word` TEXT NOT NULL, `action` TEXT NOT NULL, `beforePos` TEXT, `beforeMeaning` TEXT, `beforeLevel` TEXT, `afterPos` TEXT, `afterMeaning` TEXT, `afterLevel` TEXT, `sourceVersion` TEXT NOT NULL, `recordedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `word_details` (`word` TEXT NOT NULL, `phonetic` TEXT, `meaningsJson` TEXT NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`word`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'be120e6c0a0fa8634866f5f47dbf9619')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `words`");
        db.execSQL("DROP TABLE IF EXISTS `test_results`");
        db.execSQL("DROP TABLE IF EXISTS `word_history`");
        db.execSQL("DROP TABLE IF EXISTS `word_details`");
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
          return new RoomOpenHelper.ValidationResult(false, "words(com.example.engtest.data.entity.Word).\n"
                  + " Expected:\n" + _infoWords + "\n"
                  + " Found:\n" + _existingWords);
        }
        final HashMap<String, TableInfo.Column> _columnsTestResults = new HashMap<String, TableInfo.Column>(5);
        _columnsTestResults.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("testDateMillis", new TableInfo.Column("testDateMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("details", new TableInfo.Column("details", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTestResults.put("difficulty", new TableInfo.Column("difficulty", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTestResults = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTestResults = new HashSet<TableInfo.Index>(1);
        _indicesTestResults.add(new TableInfo.Index("index_test_results_testDateMillis", false, Arrays.asList("testDateMillis"), Arrays.asList("ASC")));
        final TableInfo _infoTestResults = new TableInfo("test_results", _columnsTestResults, _foreignKeysTestResults, _indicesTestResults);
        final TableInfo _existingTestResults = TableInfo.read(db, "test_results");
        if (!_infoTestResults.equals(_existingTestResults)) {
          return new RoomOpenHelper.ValidationResult(false, "test_results(com.example.engtest.data.entity.TestResult).\n"
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
          return new RoomOpenHelper.ValidationResult(false, "word_history(com.example.engtest.data.entity.WordHistoryEntity).\n"
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
          return new RoomOpenHelper.ValidationResult(false, "word_details(com.example.engtest.data.entity.WordDetailEntity).\n"
                  + " Expected:\n" + _infoWordDetails + "\n"
                  + " Found:\n" + _existingWordDetails);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "be120e6c0a0fa8634866f5f47dbf9619", "9cb03e102253b355850270733b9b50ca");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "words","test_results","word_history","word_details");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `words`");
      _db.execSQL("DELETE FROM `test_results`");
      _db.execSQL("DELETE FROM `word_history`");
      _db.execSQL("DELETE FROM `word_details`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
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
}
