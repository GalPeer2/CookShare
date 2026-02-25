package com.example.cookshare.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cookshare.databinding.RecipeRowBinding
import com.example.cookshare.model.Recipe
import com.squareup.picasso.Picasso

class RecipesAdapter(
    private var recipes: List<Recipe>,
    private val onEditClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(
        val binding: RecipeRowBinding,
        val onEditClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            binding.recipeNameRow.text = recipe.name
            if (recipe.pictureUrl.isNotEmpty()) {
                Picasso.get().load(recipe.pictureUrl).into(binding.recipeImageRow)
            }
            binding.editRecipeButton.setOnClickListener {
                onEditClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecipeRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding, onEditClick)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}