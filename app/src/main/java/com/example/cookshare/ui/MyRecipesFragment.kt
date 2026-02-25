package com.example.cookshare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookshare.R
import com.example.cookshare.databinding.FragmentMyRecipesBinding

class MyRecipesFragment : Fragment() {
    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyRecipesViewModel by viewModels()
    private lateinit var adapter: RecipesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        binding.addRecipeButton.setOnClickListener {
            findNavController().navigate(R.id.action_myRecipesFragment_to_recipeEditorFragment)
        }
    }

    private fun setupRecyclerView() {
        adapter = RecipesAdapter(emptyList()) { recipe ->
            val action = MyRecipesFragmentDirections.actionMyRecipesFragmentToRecipeEditorFragment(recipe.id)
            findNavController().navigate(action)
        }
        binding.myRecipesRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.myRecipesRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        binding.myRecipesProgressBar.visibility = View.VISIBLE
        viewModel.getRecipesForCurrentUser().observe(viewLifecycleOwner) { recipes ->
            adapter.updateRecipes(recipes)
            binding.myRecipesProgressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}