package com.example.easyfood_app.ui.activites

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.example.easyfood_app.data.pojo.MealDB
import com.example.easyfood_app.data.pojo.MealDetail
import com.example.easyfood_app.mvvm.DetailsMVVM
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_ID
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_STR
import com.example.easyfood_app.extensions.Constants.Companion.MEAL_THUMB
import com.example.easyfood_app.R
import com.example.easyfood_app.databinding.ActivityMealDetailesBinding
import com.google.android.material.snackbar.Snackbar

class MealDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMealDetailesBinding
    private lateinit var detailsMVVM: DetailsMVVM
    private var mealId = ""
    private var mealStr = ""
    private var mealThumb = ""
    private var ytUrl = ""
    private lateinit var dtMeal: MealDetail

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealDetailesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detailsMVVM = ViewModelProviders.of(this).get(DetailsMVVM::class.java)

        showLoading()

        getMealInfoFromIntent()
        setUpViewWithMealInformation()
        setFloatingButtonStatus()

        detailsMVVM.getMealById(mealId)

        detailsMVVM.observeMealDetail().observe(this, Observer { mealDetails ->
            setTextsInViews(mealDetails[0])
            stopLoading()
        })

        binding.imgYoutube.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ytUrl)))
        }

        binding.btnSave.setOnClickListener {
            if (isMealSavedInDatabase()) {
                deleteMeal()
                binding.btnSave.setImageResource(R.drawable.ic_baseline_save_24)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Meal was deleted",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                saveMeal()
                binding.btnSave.setImageResource(R.drawable.ic_saved)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Meal saved",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteMeal() {
        detailsMVVM.deleteMealById(mealId)
    }

    private fun setFloatingButtonStatus() {
        if (isMealSavedInDatabase()) {
            binding.btnSave.setImageResource(R.drawable.ic_saved)
        } else {
            binding.btnSave.setImageResource(R.drawable.ic_baseline_save_24)
        }
    }

    private fun isMealSavedInDatabase(): Boolean {
        return detailsMVVM.isMealSavedInDatabase(mealId)
    }

    private fun saveMeal() {
        val meal = MealDB(
            dtMeal.idMeal.toInt(),
            dtMeal.strMeal,
            dtMeal.strArea,
            dtMeal.strCategory,
            dtMeal.strInstructions,
            dtMeal.strMealThumb,
            dtMeal.strYoutube
        )
        detailsMVVM.insertMeal(meal)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.visibility = View.GONE
        binding.imgYoutube.visibility = View.INVISIBLE
    }

    private fun stopLoading() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.btnSave.visibility = View.VISIBLE
        binding.imgYoutube.visibility = View.VISIBLE
    }

    private fun setTextsInViews(meal: MealDetail) {
        this.dtMeal = meal
        ytUrl = meal.strYoutube
        binding.apply {
            tvInstructions.text = "- Instructions : "
            tvContent.text = meal.strInstructions
            tvAreaInfo.visibility = View.VISIBLE
            tvCategoryInfo.visibility = View.VISIBLE
            tvAreaInfo.text = tvAreaInfo.text.toString() + meal.strArea
            tvCategoryInfo.text = tvCategoryInfo.text.toString() + meal.strCategory
            imgYoutube.visibility = View.VISIBLE
        }
    }

    private fun setUpViewWithMealInformation() {
        binding.apply {
            collapsingToolbar.title = mealStr
            Glide.with(applicationContext)
                .load(mealThumb)
                .into(imgMealDetail)
        }
    }

    private fun getMealInfoFromIntent() {
        val tempIntent = intent

        this.mealId = tempIntent.getStringExtra(MEAL_ID)!!
        this.mealStr = tempIntent.getStringExtra(MEAL_STR)!!
        this.mealThumb = tempIntent.getStringExtra(MEAL_THUMB)!!
    }
}
