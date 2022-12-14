package com.example.newsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.newsapp.databinding.FragmentArticleBinding
import com.example.newsapp.network.ConnectivityObserver
import com.example.newsapp.ui.viewmodel.NewsViewModel
import com.example.newsapp.utils.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ArticleFragment : Fragment() {
    private lateinit var binding: FragmentArticleBinding
    private lateinit var newsViewModel: NewsViewModel
    private val args: ArticleFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isAdded = false

        newsViewModel = (activity as MainActivity).newsViewModel
        val article = args.article

        lifecycleScope.launch {

            newsViewModel.connectivityObserver.observe().collect { status ->
                hideWebView()
                if (status == ConnectivityObserver.Status.UNAVAILABLE || status == ConnectivityObserver.Status.LOST) {
                    hideFAB()
                    stopShimmer()
                    showLottie()
                } else if (status == ConnectivityObserver.Status.AVAILABLE) {
                    hideLottie()
                    showFAB()
                    showShimmer()
                    binding.webView.apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                stopShimmer()
                                showWebView()
                            }
                        }
                        article.url?.let { loadUrl(it) }
                    }

                    binding.fab.setOnClickListener {
                        if (!isAdded) {
                            newsViewModel.insertArticle(article)
                            Snackbar.make(it, "Saved Article Successfully", Snackbar.LENGTH_SHORT)
                                .show()
                            isAdded = true
                        } else {
                            Snackbar.make(it, "Article already added !!!", Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

            }
        }


    }


    private fun showShimmer() {
        binding.shimmerArticle.apply {
            visibility = View.VISIBLE
            startShimmer()
        }
    }

    private fun stopShimmer() {
        binding.shimmerArticle.apply {
            visibility = View.INVISIBLE
            stopShimmer()
        }
    }

    private fun showFAB() {
        binding.fab.visibility = View.VISIBLE
    }

    private fun hideFAB() {
        binding.fab.visibility = View.INVISIBLE
    }

    private fun hideWebView() {
        binding.webView.visibility = View.INVISIBLE
    }

    private fun showWebView() {
        binding.webView.visibility = View.VISIBLE
    }

    private fun hideLottie() {
        binding.lottieNoArticle.visibility = View.INVISIBLE
    }

    private fun showLottie() {
        binding.lottieNoArticle.visibility = View.VISIBLE
    }
}