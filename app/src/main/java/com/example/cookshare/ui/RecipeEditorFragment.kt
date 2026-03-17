package com.example.cookshare.ui

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cookshare.databinding.FragmentRecipeEditorBinding
import com.example.cookshare.model.Model
import com.example.cookshare.data.models.Recipe
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

class RecipeEditorFragment : Fragment() {

    private var _binding: FragmentRecipeEditorBinding? = null
    private val binding get() = _binding!!
    private val args: RecipeEditorFragmentArgs by navArgs()
    private var isEditMode = false
    private var existingRecipe: Recipe? = null
    private var latestTmpUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            binding.recipeImageEditor.setImageURI(it)
            binding.recipeImageEditor.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            latestTmpUri?.let { uri ->
                binding.recipeImageEditor.setImageURI(uri)
                binding.recipeImageEditor.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }
        }
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
            binding.deleteRecipeButton.visibility = View.VISIBLE
            loadRecipeData(recipeId)
        } else {
            isEditMode = false
            binding.recipeEditorHeader.text = "Create Recipe"
            binding.deleteRecipeButton.visibility = View.GONE
            binding.recipeDisplayName.text = "New Recipe"
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recipeImageEditor.setOnClickListener {
            showImagePickerOptions()
        }

        binding.saveRecipeButton.setOnClickListener {
            saveRecipe()
        }

        binding.deleteRecipeButton.setOnClickListener {
            showDeleteConfirmation()
        }

        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        binding.recipeNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.recipeDisplayName.text = s?.toString() ?: ""
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadRecipeData(recipeId: String) {
        binding.recipeEditorProgressBar.visibility = View.VISIBLE
        Model.instance.getRecipeById(recipeId).observe(viewLifecycleOwner) { recipe ->
            binding.recipeEditorProgressBar.visibility = View.GONE
            if (recipe != null) {
                existingRecipe = recipe
                binding.recipeNameEditText.setText(recipe.name)
                binding.recipeDisplayName.text = recipe.name
                binding.recipeDescriptionEditText.setText(recipe.shortDescription)
                binding.recipeInstructionsEditText.setText(recipe.instructions)
                if (recipe.pictureUrl.isNotEmpty()) {
                    Picasso.get().load(recipe.pictureUrl).into(binding.recipeImageEditor)
                    binding.recipeImageEditor.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
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

        val recipe = existingRecipe?.copy() ?: Recipe(
            id = UUID.randomUUID().toString(),
            userId = Model.instance.getCurrentUserId() ?: "",
            name = name,
            shortDescription = description,
            instructions = instructions,
            createdAt = System.currentTimeMillis()
        )
        
        recipe.name = name
        recipe.shortDescription = description
        recipe.instructions = instructions

        val bitmap = (binding.recipeImageEditor.drawable as? BitmapDrawable)?.bitmap

        if (bitmap != null && (latestTmpUri != null || existingRecipe == null)) {
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

    private fun showDeleteConfirmation() {
        existingRecipe?.let { recipe ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Delete") { _, _ ->
                    binding.recipeEditorProgressBar.visibility = View.VISIBLE
                    Model.instance.deleteRecipe(recipe) { success ->
                        binding.recipeEditorProgressBar.visibility = View.GONE
                        if (success) {
                            findNavController().navigateUp()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete recipe", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Recipe Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun takePhoto() {
        getTmpFileUri().let { uri ->
            latestTmpUri = uri
            cameraLauncher.launch(uri)
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", tmpFile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
