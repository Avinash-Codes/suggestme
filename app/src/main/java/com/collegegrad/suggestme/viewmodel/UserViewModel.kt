package com.collegegrad.suggestme.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.collegegrad.suggestme.dataclass.UserData
import com.google.firebase.Firebase
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UserOperationResult {
    data class Success(val message: String) : UserOperationResult()
    data class Error(val message: String) : UserOperationResult()
    object Loading : UserOperationResult()
    object Idle : UserOperationResult()
}

class UserViewModel:ViewModel() {
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
        skillsAndExperience: Pair<String, String>,
        yourEndGoal: List<String>
    ) {
        Log.d("Post", "Adding post to firestore under userId: $id")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _operationState.value = UserOperationResult.Loading

                val user = UserData(
                    id = id,
                    name = name,
                    skillsAndExperience = skillsAndExperience,
                    yourEndGoal = yourEndGoal
                )

                // First, ensure the user directory exists
                userCollectionReference.document(id).set(mapOf("userId" to id)).await()

                // Then add the post to the userPosts subcollection
                val userPostsRef = userCollectionReference
                    .document(id)
                    .collection("userPosts")
                    .document(user.id)

                userPostsRef.set(user).await()
                Log.d("Post", "Successfully added post ${user.id} for user $id")

                _operationState.value = UserOperationResult.Success("Post added successfully")
                _successMessage.value = "Post added successfully"

            } catch (e: Exception) {
                Log.e("Post", "Error adding post", e)
                _operationState.value = UserOperationResult.Error(e.message ?: "An error occurred")
                _successMessage.value = "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllUserDetails(onComplete: (List<UserData>) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val allPosts = mutableListOf<UserData>()

                Log.d("UserData", "Starting to fetch posts...")

                // Try getting posts from the new structure first
                val userDirectoriesSnapshot = userCollectionReference.get().await()
                Log.d("UserData", "Found ${userDirectoriesSnapshot.documents.size} user directories")

                if (userDirectoriesSnapshot.documents.isEmpty()) {
                    // If no user directories found, try getting posts from the old structure
                    Log.d("UserData", "No user directories found, checking old structure...")
                    val oldPostsSnapshot = userCollectionReference.get().await()
                    val oldPosts = oldPostsSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(UserData::class.java)
                    }
                    allPosts.addAll(oldPosts)
                    Log.d("UserData", "Found ${oldPosts.size} posts in old structure")
                } else {
                    // Fetch from new structure
                    for (userDir in userDirectoriesSnapshot.documents) {
                        try {
                            val userPostsSnapshot = userCollectionReference
                                .document(userDir.id)
                                .collection("userPosts")
                                .get()
                                .await()

                            val userPosts = userPostsSnapshot.documents
                                .mapNotNull { doc ->
                                    doc.toObject(UserData::class.java)
                                }
                            allPosts.addAll(userPosts)
                            Log.d("UserData", "Fetched ${userPosts.size} posts for user ${userDir.id}")
                        } catch (e: Exception) {
                            Log.e("UserData", "Error fetching posts for user ${userDir.id}", e)
                        }
                    }
                }

                Log.d("UserData", "Total posts fetched: ${allPosts.size}")
                onComplete(allPosts.sortedByDescending { it.createdDate })
            } catch (e: Exception) {
                Log.e("UserData", "Error fetching posts", e)
                onComplete(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

}