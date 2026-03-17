package com.example.cookshare.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cookshare.R
import com.example.cookshare.databinding.FragmentRecipeDetailsBinding
import com.example.cookshare.model.Model
import com.example.cookshare.data.models.Recipe
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

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.recipe.observe(viewLifecycleOwner) { recipe ->
            recipe?.let { updateUi(it) }
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.recipeDetailsPublisherName.text = it.name
                
                val localUserImage = Model.instance.getLocalImage("${it.id}.jpg")
                if (localUserImage != null) {
                    Picasso.get().load(localUserImage).placeholder(R.drawable.ic_person).into(binding.recipeDetailsPublisherImage)
                } else if (it.profileImageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(it.profileImageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.recipeDetailsPublisherImage)
                } else {
                    binding.recipeDetailsPublisherImage.setImageResource(R.drawable.ic_person)
                }
            }
        }
    }

    private fun updateUi(recipe: Recipe) {
        binding.recipeDetailsName.text = recipe.name
        binding.recipeDetailsShortDescription.text = recipe.shortDescription
        binding.recipeDetailsInstructions.text = recipe.instructions
        
        val localRecipeImage = Model.instance.getLocalImage("${recipe.id}.jpg")
        if (localRecipeImage != null) {
            Picasso.get().load(localRecipeImage).placeholder(R.drawable.recipe_placeholder).into(binding.recipeDetailsImage)
        } else if (recipe.pictureUrl.isNotEmpty()) {
            Picasso.get()
                .load(recipe.pictureUrl)
                .placeholder(R.drawable.recipe_placeholder)
                .error(R.drawable.recipe_placeholder)
                .into(binding.recipeDetailsImage)
        } else {
            binding.recipeDetailsImage.setImageResource(R.drawable.recipe_placeholder)
        }

        val currentUser = Model.instance.getCurrentUser()
        val isLiked = currentUser?.let { recipe.likedBy.contains(it.uid) } ?: false
        binding.recipeDetailsLikeButton.setIconResource(if (isLiked) R.drawable.ic_like_selected else R.drawable.ic_like_unselected)

        binding.recipeDetailsLikeButton.setOnClickListener {
            viewModel.toggleLike(recipe)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
