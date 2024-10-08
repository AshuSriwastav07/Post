package com.TLC_Developer.Post

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.TLC_Developer.DataManager.DataClass
import com.TLC_Developer.DataManager.currentUserProfileBlogAdapter
import com.TLC_Developer.functions.functionsManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class OnlyViewProfilePage : AppCompatActivity() {

    // Firebase authentication and Firestore instances
    private var db = Firebase.firestore

    // List to store blog data and adapter for RecyclerView
    private var userBlogListData: ArrayList<DataClass> = ArrayList()
    private val TAG = "UserProfileLogs"
    private lateinit var blogRecyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: currentUserProfileBlogAdapter
    // Function instance for additional operations
    private val functionCalls = functionsManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_only_view_profile_page)
        val showUserProfileData=intent?.extras?.getString("OpenedProfileUserId").toString()

        val userName=findViewById<TextView>(R.id.ViewProfilePageUserName)
        val instagramButton:ImageButton=findViewById(R.id.ViewProfileInstaButton)
        val YTButton:ImageButton=findViewById(R.id.ViewProfileYTButton)
        val XButton:ImageButton=findViewById(R.id.ViewProfileXImageButton)
        val FBButton:ImageButton=findViewById(R.id.ViewProfileFBImageButton)

        val profileImageImageView: ImageView = findViewById(R.id.ViewProfilePageProfileImage)

        functionCalls.showDataInOnlyViewProfile(this,showUserProfileData,profileImageImageView,userName,instagramButton,YTButton,XButton,FBButton)

    }

    override fun onStart() {
        super.onStart()
        val openedUserProfileID=intent?.extras?.getString("OpenedProfileUserId").toString() //get Data
        userProfileBlogData(openedUserProfileID)

        // Set up RecyclerView for displaying blogs
        blogRecyclerView = findViewById(R.id.ViewProfileBlogsRecyclerview)
        val viewManager = LinearLayoutManager(this)

        // Initialize RecyclerView adapter
        recyclerViewAdapter = currentUserProfileBlogAdapter(userBlogListData)
        blogRecyclerView.apply {
            setHasFixedSize(true) // Improves performance if size is constant
            layoutManager = viewManager // Set layout manager for RecyclerView
            adapter = recyclerViewAdapter // Set adapter for RecyclerView
        }

    }

    private fun userProfileBlogData(currentUserID:String) {

        db.collection("BlogsData")
            .whereEqualTo("userID", currentUserID) // Filter blogs by current user's ID
            .get()
            .addOnSuccessListener { documents ->
                Log.d(TAG, "Documents found: ${documents.size()}")  // Log the number of documents found
                userBlogListData.clear()  // Clear previous data
                for (document in documents) {
                    // Create a DataClass object for each document and add it to the list
                    val dataModel = DataClass(
                        blogTitle = document.getString("title") ?: "",
                        blogBody = document.getString("body") ?: "",
                        blogTags = document.getString("tags") ?: "",
                        blogUserID = document.getString("userID") ?: "",
                        blogDateAndTime = document.getString("BlogDateAndTime") ?: "",
                        blogImageURL = document.getString("BlogImageURL") ?: "",
                        blogUserProfileUrl = document.getString("BlogUserProfileUrl") ?: "",
                        blogDocumentID = document.id,
                        BlogUserName=document.getString("userName") ?: ""
                    )
                    userBlogListData.add(dataModel)  // Add data to ArrayList

                    // Sort the blog list by date and time in descending order
                    userBlogListData.sortByDescending {
                        functionCalls.convertStringToDate(it.BlogDateAndTime)?.time
                    }
                }
                recyclerViewAdapter.notifyDataSetChanged() // Notify adapter of data changes
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception) // Log any errors
            }
    }
}