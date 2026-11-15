package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.BadgeEntity
import com.example.data.model.DebtLoanItem
import com.example.data.model.FinanceItem
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirestoreSyncService(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    suspend fun syncUserDataToCloud(
        profile: UserProfile,
        tasks: List<TaskItem>,
        finances: List<FinanceItem>,
        debts: List<DebtLoanItem>,
        badges: List<BadgeEntity>
    ): Result<Int> {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: return Result.failure(IllegalStateException("Pengguna belum masuk ke Firebase Authentication."))

        val uid = currentUser.uid
        return try {
            withTimeout(20_000L) {
                val batch = db.batch()
                val userDoc = db.collection("users").document(uid)

                val profileMap = mapOf(
                    "userId" to uid,
                    "displayName" to profile.name.ifBlank { "Pengguna MyEvo" }.take(80),
                    "level" to profile.level,
                    "currentXp" to profile.currentXp,
                    "streakDays" to profile.streakDays,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(userDoc, profileMap, SetOptions.merge())

                var count = 1

                tasks.take(50).forEach { task ->
                    val docRef = userDoc.collection("tasks").document("task_${task.id}")
                    val taskMap = mapOf(
                        "title" to task.title.ifBlank { "Tugas" }.take(120),
                        "category" to task.category.name,
                        "priority" to task.priority.name,
                        "dueDate" to task.dueDate.take(20),
                        "isCompleted" to task.isCompleted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(docRef, taskMap, SetOptions.merge())
                    count++
                }

                finances.take(50).forEach { item ->
                    val docRef = userDoc.collection("finance").document("fin_${item.id}")
                    val finMap = mapOf(
                        "type" to item.type.name,
                        "category" to item.category.name,
                        "amount" to item.amount.coerceAtLeast(0.0),
                        "note" to item.note.take(200),
                        "date" to item.date.take(20),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(docRef, finMap, SetOptions.merge())
                    count++
                }

                debts.filter { it.amount > 0.0 }.take(50).forEach { debt ->
                    val docRef = userDoc.collection("debts").document("debt_${debt.id}")
                    val debtMap = mapOf(
                        "type" to debt.type.name,
                        "personName" to debt.personName.ifBlank { "Kontak" }.take(80),
                        "amount" to debt.amount,
                        "paidAmount" to debt.effectivePaidAmount.coerceIn(0.0, debt.amount),
                        "isSettled" to debt.isSettled,
                        "dueDate" to debt.dueDate.take(20),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(docRef, debtMap, SetOptions.merge())
                    count++
                }

                badges.take(30).forEach { badge ->
                    val docRef = userDoc.collection("badges").document(badge.id)
                    val badgeMap = mapOf(
                        "title" to badge.title.ifBlank { "Lencana" }.take(80),
                        "category" to badge.category.take(20),
                        "currentProgress" to badge.currentProgress.coerceAtLeast(0),
                        "targetProgress" to badge.targetProgress.coerceAtLeast(1),
                        "isUnlocked" to badge.isUnlocked,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(docRef, badgeMap, SetOptions.merge())
                    count++
                }

                batch.commit().await()
                Result.success(count)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Cloud sync failed for user $uid", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "FirestoreSyncService"
    }
}
