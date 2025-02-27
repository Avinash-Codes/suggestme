package com.collegegrad.suggestme.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collegegrad.suggestme.dataclass.UserData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UserOperationResult {
    data class Success(val message: String) : UserOperationResult()
    data class Error(val message: String) : UserOperationResult()
    object Loading : UserOperationResult()
    object Idle : UserOperationResult()
}

class UserViewModel : ViewModel() {
    private val userCollectionReference = Firebase.firestore.collection("users")

    private val _operationState = mutableStateOf<UserOperationResult>(UserOperationResult.Idle)
    val operationState: State<UserOperationResult> = _operationState

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _successMessage = mutableStateOf("")
    val successMessage: State<String> = _successMessage

    fun addUserToFireStore(
        id: String,
        name: String,
        skills: String,
        experience: String = "", // Default to empty if not used
        yourEndGoal: List<String>,
        interests: List<String>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _operationState.value = UserOperationResult.Loading

                val user = UserData(
                    id = id,
                    name = name,
                    skills = skills,
                    experience = experience,
                    yourEndGoal = yourEndGoal,
                    interests = interests
                )

                // Option 1: Store as a single document in the users collection
                userCollectionReference.document(id).set(user).await()
                Log.d("UserStorage", "Successfully added user with ID: $id")

                // If you still want to use subcollections, you can use this approach instead:
                /*
                // First create the user document
                userCollectionReference.document(id)
                    .set(mapOf(
                        "userId" to id,
                        "name" to name,
                        "createdDate" to System.currentTimeMillis()
                    ))
                    .await()

                // Then add the full user data to the userProfiles subcollection
                val userProfileRef = userCollectionReference
                    .document(id)
                    .collection("userProfiles")
                    .document("profile") // Using a fixed ID for the profile document

                userProfileRef.set(user).await()
                */

                _operationState.value = UserOperationResult.Success("User added successfully")
                _successMessage.value = "User added successfully"

            } catch (e: Exception) {
                Log.e("UserStorage", "Error adding user", e)
                _operationState.value = UserOperationResult.Error(e.message ?: "An error occurred")
                _successMessage.value = "Error: ${e.message ?: "Unknown error occurred"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllUserDetails(onComplete: (List<UserData>) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("UserData", "Starting to fetch users...")

                // Direct approach - get all documents from the users collection
                val usersSnapshot = userCollectionReference.get().await()
                val allUsers = usersSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserData::class.java)
                }

                Log.d("UserData", "Total users fetched: ${allUsers.size}")
                onComplete(allUsers.sortedByDescending { it.createdDate })
            } catch (e: Exception) {
                Log.e("UserData", "Error fetching users", e)
                onComplete(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }
}