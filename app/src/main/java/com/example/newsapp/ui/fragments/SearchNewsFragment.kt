package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsRecyclerAdapter
import com.example.newsapp.databinding.FragmentSearchNewsBinding
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.utils.MainActivity
import com.example.newsapp.utils.Resource
import com.example.newsapp.utils.constants.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment() {
    private lateinit var binding: FragmentSearchNewsBinding
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsRecyclerAdapter: NewsRecyclerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsViewModel = (activity as MainActivity).newsViewModel
        setupRecyclerView()


        var job: Job? = null
        binding.search.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(500L)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        newsViewModel.searchNews(editable.toString())
                    }
                }
            }
        }
        newsRecyclerAdapter.setOnItemClickListener { article ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
        newsViewModel.searchNews.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showShimmer()
                    hideRV()
                    hideImage()
                    hideLottie()
                }
                is Resource.Error -> {
                    resource.message?.let {
                        Log.e("ERROR :::", it)
                    }
                    if (resource.message == Constants.NO_INTERNET_CONNECTION) {
                        hideShimmer()
                        hideRV()
                        hideImage()
                        showLottie()
                    }
                }
                is Resource.Success -> {
                    resource.data?.let { newsResponse ->
                        hideShimmer()
                        hideLottie()
                        if (newsResponse.articles.isEmpty()) {
                            showImage()
                            hideRV()
                        } else {
                            newsRecyclerAdapter.differ.submitList(newsResponse.articles)
                            hideImage()
                            showRV()
                        }
                    }
                }
            }
        }
    }


    private fun setupRecyclerView() {
        newsRecyclerAdapter = NewsRecyclerAdapter()
        binding.recyclerViewSearchedNews.apply {
            adapter = newsRecyclerAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun hideShimmer() {
        binding.shimmerSearch.apply {
            stopShimmer()
            visibility = View.INVISIBLE
        }
    }

    private fun showShimmer() {
        binding.shimmerSearch.apply {
            visibility = View.VISIBLE
            startShimmer()
        }
    }

    private fun showLottie() {
        binding.lottieNoInternetConnection.visibility = View.VISIBLE
    }

    private fun hideLottie() {
        binding.lottieNoInternetConnection.visibility = View.INVISIBLE
    }

    private fun hideRV() {
        binding.recyclerViewSearchedNews.visibility = View.INVISIBLE
    }

    private fun showRV() {
        binding.recyclerViewSearchedNews.visibility = View.VISIBLE
    }

    private fun hideImage() {
        binding.imageViewSearch.visibility = View.INVISIBLE
    }

    private fun showImage() {
        binding.imageViewSearch.visibility = View.VISIBLE
    }

}