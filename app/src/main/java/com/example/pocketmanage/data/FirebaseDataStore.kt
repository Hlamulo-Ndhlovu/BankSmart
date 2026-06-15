package com.example.pocketmanage.data

import android.content.Context
import android.util.Log
import com.example.pocketmanage.auth.LocalAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirebaseDataStore {
    private const val TAG = "FirebaseDataStore"
    private var enabled = false

    fun init(context: Context) {
        enabled = FirebaseApp.getApps(context).isNotEmpty()
    }

    fun isEnabled(): Boolean = enabled

    fun syncFinanceSnapshot(
        accounts: List<FinanceAccount>,
        budgets: List<BudgetCategoryEntity>,
        transactions: List<FinanceTransaction>,
    ) {
        val userId = LocalAuth.currentUserId() ?: return
        val db = firestoreOrNull() ?: return
        val user = db.collection("users").document(userDocumentId(userId))
        val batch = db.batch()

        batch.set(user, userMetadata(userId), SetOptions.merge())
        accounts.forEach { account ->
            batch.set(user.collection("accounts").document(account.id.toString()), account.toCloudMap())
        }
        budgets.forEach { budget ->
            batch.set(user.collection("budgets").document(budget.id.toString()), budget.toCloudMap())
        }
        transactions.forEach { transaction ->
            batch.set(user.collection("transactions").document(transaction.id.toString()), transaction.toCloudMap())
        }

        batch.commit().addOnFailureListener { error ->
            Log.w(TAG, "Could not sync finance snapshot", error)
        }
    }

    fun syncAccount(account: FinanceAccount) {
        setUserDocument("accounts", account.id, account.toCloudMap())
    }

    fun syncBudgetCategory(category: BudgetCategoryEntity) {
        setUserDocument("budgets", category.id, category.toCloudMap())
    }

    fun syncTransaction(transaction: FinanceTransaction) {
        setUserDocument("transactions", transaction.id, transaction.toCloudMap())
    }

    fun syncCategoryEntry(entry: CategoryEntry) {
        setUserDocument("categoryEntries", entry.id, entry.toCloudMap())
    }

    fun deleteCategoryEntry(id: Long) {
        val user = userDocumentOrNull() ?: return
        user.collection("categoryEntries")
            .document(id.toString())
            .delete()
            .addOnFailureListener { error ->
                Log.w(TAG, "Could not delete category entry from Firebase", error)
            }
    }

    private fun setUserDocument(collection: String, id: Long, values: Map<String, Any?>) {
        val user = userDocumentOrNull() ?: return
        user.collection(collection)
            .document(id.toString())
            .set(values, SetOptions.merge())
            .addOnFailureListener { error ->
                Log.w(TAG, "Could not sync $collection/$id", error)
            }
    }

    private fun userDocumentOrNull() =
        LocalAuth.currentUserId()?.let { userId ->
            firestoreOrNull()?.collection("users")?.document(userDocumentId(userId))
        }

    private fun firestoreOrNull(): FirebaseFirestore? =
        if (enabled) FirebaseFirestore.getInstance() else null

    private fun userDocumentId(userId: Long): String = "local_$userId"

    private fun userMetadata(userId: Long): Map<String, Any?> =
        mapOf(
            "localUserId" to userId,
            "updatedAt" to FieldValue.serverTimestamp(),
        )

    private fun FinanceAccount.toCloudMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "name" to name,
            "balanceCents" to balanceCents,
            "iconKey" to iconKey,
            "updatedAt" to FieldValue.serverTimestamp(),
        )

    private fun BudgetCategoryEntity.toCloudMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "name" to name,
            "budgetLimitCents" to budgetLimitCents,
            "sortOrder" to sortOrder,
            "updatedAt" to FieldValue.serverTimestamp(),
        )

    private fun FinanceTransaction.toCloudMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "accountId" to accountId,
            "amountCents" to amountCents,
            "category" to category,
            "note" to note,
            "dateMillis" to dateMillis,
            "updatedAt" to FieldValue.serverTimestamp(),
        )

    private fun CategoryEntry.toCloudMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "entryDateMillis" to entryDateMillis,
            "startDateMillis" to startDateMillis,
            "endDateMillis" to endDateMillis,
            "description" to description,
            "categoryName" to categoryName,
            "photoPath" to photoPath,
            "updatedAt" to FieldValue.serverTimestamp(),
        )
}
