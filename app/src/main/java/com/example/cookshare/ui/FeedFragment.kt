package com.example.cookshare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookshare.R
import com.example.cookshare.databinding.FragmentFeedBinding
import com.example.cookshare.model.Model

class FeedFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedViewModel by viewModels()
    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupToggle()
        setupObservers()
        
        binding.feedSwipeRefresh.setOnRefreshListener {
            Model.instance.refreshAllRecipes()
        }
    }

    private fun setupRecyclerView() {
        adapter = FeedAdapter(emptyList())
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.feedRecyclerView.adapter = adapter
    }

    private fun setupToggle() {
        binding.feedToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_newest -> viewModel.setOrder("newest")
                    R.id.button_popular -> viewModel.setOrder("popular")
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.orderedRecipes.observe(viewLifecycleOwner) { recipesWithUsers ->
            adapter.updateRecipes(recipesWithUsers)
        }

        Model.instance.recipesLoadingState.observe(viewLifecycleOwner) { state ->
            binding.feedSwipeRefresh.isRefreshing = state == Model.LoadingState.LOADING
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}