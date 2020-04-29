package com.example.ing_app.ui.posts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ing_app.common.Result
import com.example.ing_app.common.ResultType
import com.example.ing_app.domain.Post as DomainPost
import com.example.ing_app.ui.posts.Post as UiPost
import com.example.ing_app.repository.PostRepository
import kotlinx.coroutines.launch
import timber.log.Timber


class PostViewModel(private val postRepository: PostRepository) : ViewModel() {
    val posts: MutableLiveData<List<UiPost>> = MutableLiveData()

    init {
        getPosts()
    }

    fun getPosts() {
        viewModelScope.launch {
            Timber.d("getPosts")
            val apiResult = postRepository.getPosts()
            transformPost(apiResult)
        }
    }

    private fun transformPost(domainPost: Result<List<DomainPost>>) {
        var uiPostList: MutableList<UiPost> = mutableListOf()
        viewModelScope.launch {
            // Not the best way
            for(post in domainPost.data!!) {
                val userResult = postRepository.getUserFromPost(post.userId)
                val commentResult = postRepository.getCommentsFromPost(post.id)
                if(isResultSuccess(userResult.resultType) && isResultSuccess(commentResult.resultType)) {
                    val postData = UiPost(
                        id = post.id,
                        userName = userResult.data!!.username,
                        title = post.title,
                        body = post.body,
                        commentsAmount = commentResult.data!!.size
                    )
                    Timber.d("Postdata = ${postData}")
                    uiPostList.add(postData)
                }
            }
            updatePosts(uiPostList.toList())
        }
    }

    private fun updatePosts(result: List<UiPost>) {
        posts.postValue(result)
    }

    private fun isResultSuccess(resultType: ResultType): Boolean {
        return resultType == ResultType.SUCCESS
    }
}