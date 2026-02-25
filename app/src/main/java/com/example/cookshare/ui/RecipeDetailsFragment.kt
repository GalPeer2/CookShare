package com.example.cookshare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.cookshare.R
import com.example.cookshare.databinding.FragmentRecipeDetailsBinding
import com.example.cookshare.model.Model
import com.example.cookshare.model.Recipe
import com.squareup.picasso.Picasso

class RecipeDetailsFragment : Fragment() {

    private var _binding: FragmentRecipeDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: RecipeDetailsFragmentArgs by navArgs()
    private val viewModel: RecipeDetailsViewModel by viewModels { RecipeDetailsViewModelFactory(requireActivity().application, args.recipeId) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe?.let { updateUi(it) }
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.recipeDetailsPublisherName.text = it.name
                if (it.profileImageUrl.isNotEmpty()) {
                    Picasso.get().load(it.profileImageUrl).into(binding.recipeDetailsPublisherImage)
                }
            }
        }
    }

    private fun updateUi(recipe: Recipe) {
        binding.recipeDetailsName.text = recipe.name
        binding.recipeDetailsShortDescription.text = recipe.shortDescription
        binding.recipeDetailsInstructions.text = recipe.instructions
        if (recipe.pictureUrl.isNotEmpty()) {
            Picasso.get().load(recipe.pictureUrl).into(binding.recipeDetailsImage)
        }

        val currentUser = Model.instance.getCurrentUser()
        val isLiked = currentUser?.let { recipe.likedBy.contains(it.uid) } ?: false
        binding.recipeDetailsLikeButton.setImageResource(if (isLiked) R.drawable.ic_like_selected else R.drawable.ic_like_unselected)

        binding.recipeDetailsLikeButton.setOnClickListener {
            viewModel.toggleLike(recipe)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}