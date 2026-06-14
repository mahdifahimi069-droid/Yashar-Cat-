package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "cats")
data class Cat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val breed: String,
    val gender: String, // "male" or "female"
    val ageMonths: Int,
    val weight: Float,
    val color: String,
    val microchip: String,
    val isNeutered: Boolean,
    val photoIndex: Int = 0 // Locally simulated avatar image index
)

@Entity(tableName = "medical_records")
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int,
    val type: String, // "vaccine", "antiparasite_internal", "antiparasite_external", "disease", "surgery", "allergy"
    val title: String,
    val date: String, // YYYY-MM-DD
    val notes: String = "",
    val reminderDate: String = "", // YYYY-MM-DD (vaccine or parasite revaccination alert)
    val isCompleted: Boolean = false,
    val attachmentPath: String? = null,
    val attachmentType: String? = null, // "image" or "pdf"
    val attachmentName: String? = null
)

@Entity(tableName = "growth_records")
data class GrowthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int,
    val date: String, // YYYY-MM-DD
    val weight: Float,
    val height: Float,
    val note: String = ""
)

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int,
    val date: String, // YYYY-MM-DD
    val waterIntakeMl: Int = 0,
    val dryFoodGrams: Int = 0,
    val wetFoodGrams: Int = 0,
    val treatsCount: Int = 0,
    val vomitCount: Int = 0,
    val diarrheaCount: Int = 0,
    val sneezingCount: Int = 0,
    val scratchingLevel: Int = 0, // 0 to 3
    val isLethargic: Boolean = false,
    val litterBoxCleanCount: Int = 0
)

@Entity(tableName = "reproduction_records")
data class ReproductionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val catId: Int,
    val type: String, // "heat", "mating", "pregnancy", "birth"
    val startDate: String, // YYYY-MM-DD
    val endDate: String = "",
    val notes: String = ""
)

// 2. DAOs
@Dao
interface CatDao {
    @Query("SELECT * FROM cats ORDER BY id DESC")
    fun getAllCats(): Flow<List<Cat>>

    @Query("SELECT * FROM cats WHERE id = :id LIMIT 1")
    fun getCatById(id: Int): Flow<Cat?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCat(cat: Cat): Long

    @Update
    suspend fun updateCat(cat: Cat)

    @Delete
    suspend fun deleteCat(cat: Cat)
}

@Dao
interface MedicalDao {
    @Query("SELECT * FROM medical_records WHERE catId = :catId ORDER BY date DESC")
    fun getRecordsForCat(catId: Int): Flow<List<MedicalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MedicalRecord)

    @Delete
    suspend fun deleteRecord(record: MedicalRecord)
}

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_records WHERE catId = :catId ORDER BY date ASC")
    fun getGrowthForCat(catId: Int): Flow<List<GrowthRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowth(record: GrowthRecord)

    @Delete
    suspend fun deleteGrowth(record: GrowthRecord)
}

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE catId = :catId AND date = :date LIMIT 1")
    suspend fun getLogForDateDirect(catId: Int, date: String): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE catId = :catId AND date = :date LIMIT 1")
    fun getLogForDate(catId: Int, date: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs WHERE catId = :catId ORDER BY date DESC LIMIT 7")
    fun getRecentLogs(catId: Int): Flow<List<DailyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLog)
}

@Dao
interface ReproductionDao {
    @Query("SELECT * FROM reproduction_records WHERE catId = :catId ORDER BY startDate DESC")
    fun getRecordsForCat(catId: Int): Flow<List<ReproductionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReproduction(record: ReproductionRecord)

    @Delete
    suspend fun deleteReproduction(record: ReproductionRecord)
}

// 3. Database
@Database(
    entities = [
        Cat::class,
        MedicalRecord::class,
        GrowthRecord::class,
        DailyLog::class,
        ReproductionRecord::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catDao(): CatDao
    abstract fun medicalDao(): MedicalDao
    abstract fun growthDao(): GrowthDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun reproductionDao(): ReproductionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yasham_cat_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Repository
class CatRepository(private val db: AppDatabase) {
    val allCats: Flow<List<Cat>> = db.catDao().getAllCats()

    fun getCatById(id: Int): Flow<Cat?> = db.catDao().getCatById(id)

    suspend fun insertCat(cat: Cat): Long = db.catDao().insertCat(cat)
    suspend fun updateCat(cat: Cat) = db.catDao().updateCat(cat)
    suspend fun deleteCat(cat: Cat) = db.catDao().deleteCat(cat)

    fun getMedicalRecords(catId: Int): Flow<List<MedicalRecord>> = db.medicalDao().getRecordsForCat(catId)
    suspend fun insertMedicalRecord(record: MedicalRecord) = db.medicalDao().insertRecord(record)
    suspend fun deleteMedicalRecord(record: MedicalRecord) = db.medicalDao().deleteRecord(record)

    fun getGrowthRecords(catId: Int): Flow<List<GrowthRecord>> = db.growthDao().getGrowthForCat(catId)
    suspend fun insertGrowthRecord(record: GrowthRecord) = db.growthDao().insertGrowth(record)
    suspend fun deleteGrowthRecord(record: GrowthRecord) = db.growthDao().deleteGrowth(record)

    fun getDailyLog(catId: Int, date: String): Flow<DailyLog?> = db.dailyLogDao().getLogForDate(catId, date)
    suspend fun getDailyLogDirect(catId: Int, date: String): DailyLog? = db.dailyLogDao().getLogForDateDirect(catId, date)
    suspend fun insertDailyLog(log: DailyLog) = db.dailyLogDao().insertLog(log)
    fun getRecentDailyLogs(catId: Int): Flow<List<DailyLog>> = db.dailyLogDao().getRecentLogs(catId)

    fun getReproductionRecords(catId: Int): Flow<List<ReproductionRecord>> = db.reproductionDao().getRecordsForCat(catId)
    suspend fun insertReproductionRecord(record: ReproductionRecord) = db.reproductionDao().insertReproduction(record)
    suspend fun deleteReproductionRecord(record: ReproductionRecord) = db.reproductionDao().deleteReproduction(record)
}
