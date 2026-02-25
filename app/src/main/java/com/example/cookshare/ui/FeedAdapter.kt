package com.example.cookshare.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cookshare.R
import com.example.cookshare.databinding.RecipeFeedRowBinding
import com.squareup.picasso.Picasso

class FeedAdapter(
    private var recipes: List<RecipeWithUser>
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    class FeedViewHolder(val binding: RecipeFeedRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipeWithUser: RecipeWithUser) {
            val (recipe, user) = recipeWithUser

            binding.feedRecipeName.text = recipe.name
            binding.feedRecipeDescription.text = recipe.shortDescription
            binding.feedLikesCount.text = recipe.likedBy.size.toString()

            if (recipe.pictureUrl.isNotEmpty()) {
                Picasso.get().load(recipe.pictureUrl).into(binding.feedRecipeImage)
            }

            if (user != null) {
                binding.feedPublisherName.text = user.name
                if (user.profileImageUrl.isNotEmpty()) {
                    Picasso.get().load(user.profileImageUrl).into(binding.feedPublisherAvatar)
                } else {
                    binding.feedPublisherAvatar.setImageResource(R.drawable.ic_launcher_background)
                }
            } else {
                binding.feedPublisherName.text = "Unknown User"
                binding.feedPublisherAvatar.setImageResource(R.drawable.ic_launcher_background)
            }

            binding.root.setOnClickListener {
                val action = FeedFragmentDirections.actionFeedFragmentToRecipeDetailsFragment(recipe.id)
                it.findNavController().navigate(action)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = RecipeFeedRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<RecipeWithUser>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}