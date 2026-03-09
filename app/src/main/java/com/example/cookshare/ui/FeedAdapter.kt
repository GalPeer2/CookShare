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
                Picasso.get()
                    .load(recipe.pictureUrl)
                    .placeholder(R.drawable.ic_chef_hat)
                    .error(R.drawable.ic_chef_hat)
                    .into(binding.feedRecipeImage)
            } else {
                binding.feedRecipeImage.setImageResource(R.drawable.ic_chef_hat)
            }

            if (user != null) {
                binding.feedPublisherName.text = user.name
                if (user.profileImageUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(user.profileImageUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.feedPublisherAvatar)
                } else {
                    binding.feedPublisherAvatar.setImageResource(R.drawable.ic_person)
                }
            } else {
                binding.feedPublisherName.text = "Unknown User"
                binding.feedPublisherAvatar.setImageResource(R.drawable.ic_person)
            }

            // Set listener on the specific container instead of root to ensure it catches clicks
            binding.root.setOnClickListener {
                binding.feedPublisherName.text = "fffffff User"
                val action = FeedFragmentDirections.actionFeedFragmentToRecipeDetailsFragment(recipe.id)
                it.findNavController().navigate(action)
            }
        }
    }


    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<RecipeWithUser>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = RecipeFeedRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }
}
