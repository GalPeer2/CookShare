package com.example.cookshare.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cookshare.databinding.FragmentRecipeEditorBinding
import com.example.cookshare.model.Model
import com.example.cookshare.model.Recipe
import com.squareup.picasso.Picasso
import java.util.*

class RecipeEditorFragment : Fragment() {

    private var _binding: FragmentRecipeEditorBinding? = null
    private val binding get() = _binding!!
    private val args: RecipeEditorFragmentArgs by navArgs()
    private var isEditMode = false
    private var existingRecipe: Recipe? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { binding.recipeImageEditor.setImageURI(it) }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { binding.recipeImageEditor.setImageBitmap(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeId = args.recipeId
        if (recipeId != null) {
            isEditMode = true
            binding.recipeEditorHeader.text = "Edit Recipe"
            loadRecipeData(recipeId)
        } else {
            binding.recipeEditorHeader.text = "Create Recipe"
        }

        binding.recipeImageEditor.setOnClickListener {
            showImagePickerOptions()
        }

        binding.saveRecipeButton.setOnClickListener {
            saveRecipe()
        }
    }

    private fun loadRecipeData(recipeId: String) {
        binding.recipeEditorProgressBar.visibility = View.VISIBLE
        Model.instance.getRecipeById(recipeId).observe(viewLifecycleOwner) { recipe ->
            binding.recipeEditorProgressBar.visibility = View.GONE
            if (recipe != null) {
                existingRecipe = recipe
                binding.recipeNameEditText.setText(recipe.name)
                binding.recipeDescriptionEditText.setText(recipe.shortDescription)
                binding.recipeInstructionsEditText.setText(recipe.instructions)
                if (recipe.pictureUrl.isNotEmpty()) {
                    Picasso.get().load(recipe.pictureUrl).into(binding.recipeImageEditor)
                }
            }
        }
    }

    private fun saveRecipe() {
        val name = binding.recipeNameEditText.text.toString()
        val description = binding.recipeDescriptionEditText.text.toString()
        val instructions = binding.recipeInstructionsEditText.text.toString()

        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        binding.recipeEditorProgressBar.visibility = View.VISIBLE
        binding.saveRecipeButton.isEnabled = false

        val recipe = existingRecipe ?: Recipe(
            id = UUID.randomUUID().toString(),
            userId = Model.instance.getCurrentUserId() ?: ""
        )
        
        recipe.name = name
        recipe.shortDescription = description
        recipe.instructions = instructions

        val bitmap = (binding.recipeImageEditor.drawable as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            Model.instance.uploadRecipeImage(recipe.id, bitmap) { url ->
                if (url != null) {
                    recipe.pictureUrl = url
                    performSave(recipe)
                } else {
                    binding.recipeEditorProgressBar.visibility = View.GONE
                    binding.saveRecipeButton.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            performSave(recipe)
        }
    }

    private fun performSave(recipe: Recipe) {
        Model.instance.addRecipe(recipe) {
            binding.recipeEditorProgressBar.visibility = View.GONE
            findNavController().navigateUp()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Recipe Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}